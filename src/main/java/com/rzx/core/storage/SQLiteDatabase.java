package com.rzx.core.storage;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * SQLite-backed {@link DatabaseManager} using HikariCP.
 */
public class SQLiteDatabase implements DatabaseManager {

    private final File dataFolder;
    private final int maxPoolSize;
    private HikariDataSource dataSource;

    public SQLiteDatabase(File dataFolder, int maxPoolSize) {
        this.dataFolder = dataFolder;
        this.maxPoolSize = maxPoolSize;
    }

    @Override
    public void connect() throws Exception {
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
        File dbFile = new File(dataFolder, "rzxcore.db");

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:sqlite:" + dbFile.getAbsolutePath());
        config.setMaximumPoolSize(maxPoolSize);
        config.setPoolName("RZXCore-SQLite");
        config.setConnectionTestQuery("SELECT 1");

        this.dataSource = new HikariDataSource(config);
    }

    @Override
    public Connection getConnection() throws SQLException {
        if (dataSource == null) {
            throw new SQLException("DataSource is not initialized.");
        }
        return dataSource.getConnection();
    }

    @Override
    public void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }

    @Override
    public boolean isConnected() {
        return dataSource != null && !dataSource.isClosed();
    }
}

