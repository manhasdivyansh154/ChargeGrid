package chargegrid.service;

import chargegrid.dao.StationDAO;
import chargegrid.dao.UserDAO;
import chargegrid.dao.WalletDAO;
import chargegrid.exception.AuthenticationException;
import chargegrid.exception.ChargeGridException;
import chargegrid.exception.InvalidInputException;
import chargegrid.model.*;
import chargegrid.util.PasswordUtil;
import chargegrid.util.Validator;

import java.sql.SQLException;
import java.util.List;

public class AuthService {
    private final UserDAO userDAO;
    private final WalletDAO walletDAO;
    private final StationDAO stationDAO;

    public AuthService(UserDAO userDAO, WalletDAO walletDAO, StationDAO stationDAO) {
        this.userDAO = userDAO;
        this.walletDAO = walletDAO;
        this.stationDAO = stationDAO;
    }

    public User login(String username, String password) throws SQLException, AuthenticationException {
        User u = userDAO.findByUsername(username);
        if (u == null || !u.getPasswordHash().equals(PasswordUtil.hash(password))) {
            throw new AuthenticationException("Invalid username or password.");
        }
        return u;
    }

    private void validateProfile(String username, String password, String fullName, String email, String phone)
            throws SQLException, InvalidInputException {
        if (!Validator.isValidUsername(username))
            throw new InvalidInputException("Username must be 4-20 characters (letters, digits, underscore).");
        if (!Validator.isValidPassword(password))
            throw new InvalidInputException("Password must be at least 6 characters.");
        if (Validator.isBlank(fullName))
            throw new InvalidInputException("Full name is required.");
        if (!Validator.isValidEmail(email))
            throw new InvalidInputException("Invalid email address.");
        if (!Validator.isValidPhone(phone))
            throw new InvalidInputException("Phone must be a 10-digit mobile number.");
        if (userDAO.usernameExists(username))
            throw new InvalidInputException("Username '" + username + "' is already taken.");
    }

    public Customer registerCustomer(String username, String password, String fullName, String email, String phone)
            throws SQLException, ChargeGridException {
        validateProfile(username, password, fullName, email, phone);
        Customer c = new Customer(0, username, PasswordUtil.hash(password), fullName, email, phone);
        int id = userDAO.addUser(c);
        c.setId(id);
        walletDAO.createWallet(id);
        return c;
    }

    public StationManager createManager(String username, String password, String fullName, String email,
                                        String phone, int stationId) throws SQLException, ChargeGridException {
        validateProfile(username, password, fullName, email, phone);
        if (stationDAO.findById(stationId) == null) throw new InvalidInputException("Station " + stationId + " does not exist.");
        StationManager m = new StationManager(0, username, PasswordUtil.hash(password), fullName, email, phone, stationId);
        m.setId(userDAO.addUser(m));
        return m;
    }

    public List<User> listUsers() throws SQLException {
        return userDAO.findAll();
    }

    public void changePassword(User user, String oldPassword, String newPassword) throws SQLException, ChargeGridException {
        if (!user.getPasswordHash().equals(PasswordUtil.hash(oldPassword)))
            throw new AuthenticationException("Current password is incorrect.");
        if (!Validator.isValidPassword(newPassword))
            throw new InvalidInputException("New password must be at least 6 characters.");
        String hash = PasswordUtil.hash(newPassword);
        userDAO.updatePassword(user.getId(), hash);
        user.setPasswordHash(hash);
    }
}
