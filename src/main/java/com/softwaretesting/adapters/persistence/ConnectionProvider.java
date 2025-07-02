package com.softwaretesting.adapters.persistence;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;

public interface ConnectionProvider {
    Connection getConnection() throws SQLException;
    void initializeDatabase(InputStream inputStream) throws SQLException, IOException;
    void closeConnection() throws SQLException;
}
