package com.lezhin.support;

import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

/**
 * @author geunwoo.shin auth-server
 */
public class IterableDataSource implements DataSource {
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(IterableDataSource.class);

    private final List<DataSource> dataSources = new ArrayList<>();
    private Iterator<DataSource> iterator;

    public IterableDataSource(Iterable<DataSource> dataSources) {
        dataSources.forEach(this.dataSources::add);
        iterator = this.dataSources.iterator();
    }

    @Override
    public Connection getConnection() throws SQLException {
        int tryCount = 0;
        while (true) {
            if (++tryCount > dataSources.size()) {
                throw new SQLException("All nodes down");
            }

            if (!iterator.hasNext()) {
                iterator = dataSources.iterator();
            }

            try {
                return iterator.next().getConnection();
            } catch (SQLException e) {
                LOGGER.error(e.getLocalizedMessage(), e);
            }
        }
    }

    @Override
    public Connection getConnection(final String username, final String password) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> T unwrap(final Class<T> iface) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isWrapperFor(final Class<?> iface) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public PrintWriter getLogWriter() throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setLogWriter(final PrintWriter out) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getLoginTimeout() throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setLoginTimeout(final int seconds) throws SQLException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        throw new UnsupportedOperationException();
    }
}
