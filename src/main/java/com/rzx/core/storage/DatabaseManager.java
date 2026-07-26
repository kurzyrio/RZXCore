package com.rzx.core.storage;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Contract for database backends. Implementations manage a connection pool
 * and provide {@link Connection} instances to callers.
 */
public interface DatabaseManager {

    /**
     * Opens the connection pool / establishes the initial connection.
     *
     * @throws Exception if the connection cannot be established
     */
    void connect() throws Exception;

    /**
     * Borrows a connection from the pool.
     *
     * @return a JDBC Connection
     * @throws SQLException if a connection cannot be obtained
     */
    Connection getConnection() throws SQLException;

    /** Closes the connection pool and releases all resources. */
    void close();

    /** Returns true if the pool is open and healthy. */
    boolean isConnected();
}

