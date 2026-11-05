package chargegrid.dao;

import chargegrid.model.User;
import chargegrid.model.UserRole;
import java.sql.SQLException;
import java.util.List;

public interface UserDAO {
    int addUser(User user) throws SQLException;
    User findByUsername(String username) throws SQLException;
    User findById(int id) throws SQLException;
    List<User> findAll() throws SQLException;
    List<User> findByRole(UserRole role) throws SQLException;
    boolean usernameExists(String username) throws SQLException;
    boolean updatePassword(int userId, String newHash) throws SQLException;
}
