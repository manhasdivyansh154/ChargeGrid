package chargegrid.dao;

import chargegrid.model.Wallet;
import java.sql.Connection;
import java.sql.SQLException;

public interface WalletDAO {
    void createWallet(int userId) throws SQLException;
    Wallet findByUser(int userId) throws SQLException;
    boolean topUp(int userId, double amount) throws SQLException;
    /** Deducts only if balance is enough. Returns false when balance is too low. */
    boolean deduct(Connection con, int userId, double amount) throws SQLException;
}
