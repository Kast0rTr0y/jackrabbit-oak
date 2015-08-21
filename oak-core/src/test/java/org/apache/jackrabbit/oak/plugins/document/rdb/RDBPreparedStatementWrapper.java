/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.jackrabbit.oak.plugins.document.rdb;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.Date;
import java.sql.NClob;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public class RDBPreparedStatementWrapper implements PreparedStatement {

    private final RDBDataSourceWrapper datasource;
    private final PreparedStatement statement;
    private ResultSet resultSet = null;
    private List<Object> parameters = new ArrayList<Object>();

    public RDBPreparedStatementWrapper(RDBDataSourceWrapper datasource, PreparedStatement statement) {
        this.datasource = datasource;
        this.statement = statement;
    }

    public void addBatch() throws SQLException {
        long start = System.nanoTime();
        statement.addBatch();
        List<RDBLogEntry> l = datasource.getLog();
        if (l != null) {
            l.add(new RDBLogEntry(start, "statement.addBatch() with parameters: " + parameters));
        }
        parameters.clear();
    }

    public void addBatch(String sql) throws SQLException {
        statement.addBatch(sql);
    }

    public void cancel() throws SQLException {
        statement.cancel();
    }

    public void clearBatch() throws SQLException {
        statement.clearBatch();
    }

    public void clearParameters() throws SQLException {
        statement.clearParameters();
        parameters.clear();
    }

    public void clearWarnings() throws SQLException {
        statement.clearWarnings();
    }

    public void close() throws SQLException {
        if (resultSet != null) {
            resultSet.close();
        }
        statement.close();
    }

    // needed in Java 7...
    @SuppressWarnings("unused")
    public void closeOnCompletion() throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    public boolean execute() throws SQLException {
        return statement.execute();
    }

    public boolean execute(String sql, int autoGeneratedKeys) throws SQLException {
        return statement.execute(sql, autoGeneratedKeys);
    }

    public boolean execute(String sql, int[] columnIndexes) throws SQLException {
        return statement.execute(sql, columnIndexes);
    }

    public boolean execute(String sql, String[] columnNames) throws SQLException {
        return statement.execute(sql, columnNames);
    }

    public boolean execute(String sql) throws SQLException {
        return statement.execute(sql);
    }

    public int[] executeBatch() throws SQLException {
        long start = System.nanoTime();
        int results[] = {};
        SQLException x = null;
        try {
            results = statement.executeBatch();
            return results;
        } catch (SQLException ex) {
            x = ex;
            throw ex;
        } finally {
            List<RDBLogEntry> l = datasource.getLog();
            if (l != null) {
                String message = "statement.executeBatch() -> " + Arrays.toString(results);
                if (x != null) {
                    message += " -> " + x.getMessage();
                }
                l.add(new RDBLogEntry(start, message));
            }
        }
    }

    public ResultSet executeQuery() throws SQLException {
        long start = System.nanoTime();
        SQLException x = null;
        try {
            resultSet = new RDBResultSetWrapper(datasource, statement.executeQuery());
            return resultSet;
        } catch (SQLException ex) {
            x = ex;
            throw ex;
        } finally {
            List<RDBLogEntry> l = datasource.getLog();
            if (l != null) {
                String message = "statement.executeQuery() with parameters: " + parameters;
                if (x != null) {
                    message += " -> " + x.getMessage();
                }
                l.add(new RDBLogEntry(start, message));
            }
        }
    }

    public ResultSet executeQuery(String sql) throws SQLException {
        return statement.executeQuery(sql);
    }

    public int executeUpdate() throws SQLException {
        long start = System.nanoTime();
        SQLException x = null;
        Integer result = null;
        try {
            result = statement.executeUpdate();
            return result;
        } catch (SQLException ex) {
            x = ex;
            throw ex;
        } finally {
            List<RDBLogEntry> l = datasource.getLog();
            if (l != null) {
                String message = "statement.executeUpdate() with parameters: " + parameters;
                if (result != null) {
                    message += " -> " + result;
                }
                if (x != null) {
                    message += " -> " + x.getMessage();
                }
                l.add(new RDBLogEntry(start, message));
            }
        }
    }

    public int executeUpdate(String sql, int autoGeneratedKeys) throws SQLException {
        return statement.executeUpdate(sql, autoGeneratedKeys);
    }

    public int executeUpdate(String sql, int[] columnIndexes) throws SQLException {
        return statement.executeUpdate(sql, columnIndexes);
    }

    public int executeUpdate(String sql, String[] columnNames) throws SQLException {
        return statement.executeUpdate(sql, columnNames);
    }

    public int executeUpdate(String sql) throws SQLException {
        return statement.executeUpdate(sql);
    }

    public Connection getConnection() throws SQLException {
        return statement.getConnection();
    }

    public int getFetchDirection() throws SQLException {
        return statement.getFetchDirection();
    }

    public int getFetchSize() throws SQLException {
        return statement.getFetchSize();
    }

    public ResultSet getGeneratedKeys() throws SQLException {
        return statement.getGeneratedKeys();
    }

    public int getMaxFieldSize() throws SQLException {
        return statement.getMaxFieldSize();
    }

    public int getMaxRows() throws SQLException {
        return statement.getMaxRows();
    }

    public ResultSetMetaData getMetaData() throws SQLException {
        return statement.getMetaData();
    }

    public boolean getMoreResults() throws SQLException {
        return statement.getMoreResults();
    }

    public boolean getMoreResults(int current) throws SQLException {
        return statement.getMoreResults(current);
    }

    public ParameterMetaData getParameterMetaData() throws SQLException {
        return statement.getParameterMetaData();
    }

    public int getQueryTimeout() throws SQLException {
        return statement.getQueryTimeout();
    }

    public ResultSet getResultSet() throws SQLException {
        return statement.getResultSet();
    }

    public int getResultSetConcurrency() throws SQLException {
        return statement.getResultSetConcurrency();
    }

    public int getResultSetHoldability() throws SQLException {
        return statement.getResultSetHoldability();
    }

    public int getResultSetType() throws SQLException {
        return statement.getResultSetType();
    }

    public int getUpdateCount() throws SQLException {
        return statement.getUpdateCount();
    }

    public SQLWarning getWarnings() throws SQLException {
        return statement.getWarnings();
    }

    // needed in Java 7...
    @SuppressWarnings("unused")
    public boolean isCloseOnCompletion() throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    public boolean isClosed() throws SQLException {
        return statement.isClosed();
    }

    public boolean isPoolable() throws SQLException {
        return statement.isPoolable();
    }

    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return statement.isWrapperFor(iface);
    }

    public void setArray(int parameterIndex, Array x) throws SQLException {
        statement.setArray(parameterIndex, x);
        record(parameterIndex, x);
    }

    public void setAsciiStream(int parameterIndex, InputStream x, int length) throws SQLException {
        statement.setAsciiStream(parameterIndex, x, length);
        record(parameterIndex, x);
    }

    public void setAsciiStream(int parameterIndex, InputStream x, long length) throws SQLException {
        statement.setAsciiStream(parameterIndex, x, length);
        record(parameterIndex, x);
    }

    public void setAsciiStream(int parameterIndex, InputStream x) throws SQLException {
        statement.setAsciiStream(parameterIndex, x);
        record(parameterIndex, x);
    }

    public void setBigDecimal(int parameterIndex, BigDecimal x) throws SQLException {
        statement.setBigDecimal(parameterIndex, x);
        record(parameterIndex, x);
    }

    public void setBinaryStream(int parameterIndex, InputStream x, int length) throws SQLException {
        statement.setBinaryStream(parameterIndex, x, length);
        record(parameterIndex, x);
    }

    public void setBinaryStream(int parameterIndex, InputStream x, long length) throws SQLException {
        statement.setBinaryStream(parameterIndex, x, length);
        record(parameterIndex, x);
    }

    public void setBinaryStream(int parameterIndex, InputStream x) throws SQLException {
        statement.setBinaryStream(parameterIndex, x);
        record(parameterIndex, x);
    }

    public void setBlob(int parameterIndex, Blob x) throws SQLException {
        statement.setBlob(parameterIndex, x);
        record(parameterIndex, x);
    }

    public void setBlob(int parameterIndex, InputStream inputStream, long length) throws SQLException {
        statement.setBlob(parameterIndex, inputStream, length);
        record(parameterIndex, inputStream);
    }

    public void setBlob(int parameterIndex, InputStream inputStream) throws SQLException {
        statement.setBlob(parameterIndex, inputStream);
        record(parameterIndex, inputStream);
    }

    public void setBoolean(int parameterIndex, boolean x) throws SQLException {
        statement.setBoolean(parameterIndex, x);
        record(parameterIndex, x);
    }

    public void setByte(int parameterIndex, byte x) throws SQLException {
        statement.setByte(parameterIndex, x);
        record(parameterIndex, x);
    }

    public void setBytes(int parameterIndex, byte[] x) throws SQLException {
        statement.setBytes(parameterIndex, x);
        record(parameterIndex, Arrays.asList(x));
    }

    public void setCharacterStream(int parameterIndex, Reader reader, int length) throws SQLException {
        statement.setCharacterStream(parameterIndex, reader, length);
        record(parameterIndex, reader);
    }

    public void setCharacterStream(int parameterIndex, Reader reader, long length) throws SQLException {
        statement.setCharacterStream(parameterIndex, reader, length);
        record(parameterIndex, reader);
    }

    public void setCharacterStream(int parameterIndex, Reader reader) throws SQLException {
        statement.setCharacterStream(parameterIndex, reader);
        record(parameterIndex, reader);
    }

    public void setClob(int parameterIndex, Clob x) throws SQLException {
        statement.setClob(parameterIndex, x);
        record(parameterIndex, x);
    }

    public void setClob(int parameterIndex, Reader reader, long length) throws SQLException {
        statement.setClob(parameterIndex, reader, length);
        record(parameterIndex, reader);
    }

    public void setClob(int parameterIndex, Reader reader) throws SQLException {
        statement.setClob(parameterIndex, reader);
        record(parameterIndex, reader);
    }

    public void setCursorName(String name) throws SQLException {
        statement.setCursorName(name);
    }

    public void setDate(int parameterIndex, Date x, Calendar cal) throws SQLException {
        statement.setDate(parameterIndex, x, cal);
        record(parameterIndex, x);
    }

    public void setDate(int parameterIndex, Date x) throws SQLException {
        statement.setDate(parameterIndex, x);
        record(parameterIndex, x);
    }

    public void setDouble(int parameterIndex, double x) throws SQLException {
        statement.setDouble(parameterIndex, x);
        record(parameterIndex, x);
    }

    public void setEscapeProcessing(boolean enable) throws SQLException {
        statement.setEscapeProcessing(enable);
    }

    public void setFetchDirection(int direction) throws SQLException {
        statement.setFetchDirection(direction);
    }

    public void setFetchSize(int rows) throws SQLException {
        statement.setFetchSize(rows);
    }

    public void setFloat(int parameterIndex, float x) throws SQLException {
        statement.setFloat(parameterIndex, x);
        record(parameterIndex, x);
    }

    public void setInt(int parameterIndex, int x) throws SQLException {
        statement.setInt(parameterIndex, x);
        record(parameterIndex, x);
    }

    public void setLong(int parameterIndex, long x) throws SQLException {
        statement.setLong(parameterIndex, x);
        record(parameterIndex, x);
    }

    public void setMaxFieldSize(int max) throws SQLException {
        statement.setMaxFieldSize(max);
    }

    public void setMaxRows(int max) throws SQLException {
        statement.setMaxRows(max);
    }

    public void setNCharacterStream(int parameterIndex, Reader value, long length) throws SQLException {
        statement.setNCharacterStream(parameterIndex, value, length);
        record(parameterIndex, value);
    }

    public void setNCharacterStream(int parameterIndex, Reader value) throws SQLException {
        statement.setNCharacterStream(parameterIndex, value);
        record(parameterIndex, value);
    }

    public void setNClob(int parameterIndex, NClob value) throws SQLException {
        statement.setNClob(parameterIndex, value);
        record(parameterIndex, value);
    }

    public void setNClob(int parameterIndex, Reader reader, long length) throws SQLException {
        statement.setNClob(parameterIndex, reader, length);
        record(parameterIndex, reader);
    }

    public void setNClob(int parameterIndex, Reader reader) throws SQLException {
        statement.setNClob(parameterIndex, reader);
        record(parameterIndex, reader);
    }

    public void setNString(int parameterIndex, String value) throws SQLException {
        statement.setNString(parameterIndex, value);
        record(parameterIndex, value);
    }

    public void setNull(int parameterIndex, int sqlType, String typeName) throws SQLException {
        statement.setNull(parameterIndex, sqlType, typeName);
        record(parameterIndex, "(null)");
    }

    public void setNull(int parameterIndex, int sqlType) throws SQLException {
        statement.setNull(parameterIndex, sqlType);
        record(parameterIndex, "(null)");
    }

    public void setObject(int parameterIndex, Object x, int targetSqlType, int scaleOrLength) throws SQLException {
        statement.setObject(parameterIndex, x, targetSqlType, scaleOrLength);
        record(parameterIndex, x);
    }

    public void setObject(int parameterIndex, Object x, int targetSqlType) throws SQLException {
        statement.setObject(parameterIndex, x, targetSqlType);
        record(parameterIndex, x);
    }

    public void setObject(int parameterIndex, Object x) throws SQLException {
        statement.setObject(parameterIndex, x);
        record(parameterIndex, x);
    }

    public void setPoolable(boolean poolable) throws SQLException {
        statement.setPoolable(poolable);
    }

    public void setQueryTimeout(int seconds) throws SQLException {
        statement.setQueryTimeout(seconds);
    }

    public void setRef(int parameterIndex, Ref x) throws SQLException {
        statement.setRef(parameterIndex, x);
        record(parameterIndex, x);
    }

    public void setRowId(int parameterIndex, RowId x) throws SQLException {
        statement.setRowId(parameterIndex, x);
    }

    public void setSQLXML(int parameterIndex, SQLXML xmlObject) throws SQLException {
        statement.setSQLXML(parameterIndex, xmlObject);
        record(parameterIndex, xmlObject);
    }

    public void setShort(int parameterIndex, short x) throws SQLException {
        statement.setShort(parameterIndex, x);
        record(parameterIndex, x);
    }

    public void setString(int parameterIndex, String x) throws SQLException {
        statement.setString(parameterIndex, x);
        record(parameterIndex, x);
    }

    public void setTime(int parameterIndex, Time x, Calendar cal) throws SQLException {
        statement.setTime(parameterIndex, x, cal);
        record(parameterIndex, x);
    }

    public void setTime(int parameterIndex, Time x) throws SQLException {
        statement.setTime(parameterIndex, x);
        record(parameterIndex, x);
    }

    public void setTimestamp(int parameterIndex, Timestamp x, Calendar cal) throws SQLException {
        statement.setTimestamp(parameterIndex, x, cal);
        record(parameterIndex, x);
    }

    public void setTimestamp(int parameterIndex, Timestamp x) throws SQLException {
        statement.setTimestamp(parameterIndex, x);
        record(parameterIndex, x);
    }

    public void setURL(int parameterIndex, URL x) throws SQLException {
        statement.setURL(parameterIndex, x);
        record(parameterIndex, x);
    }

    public void setUnicodeStream(int parameterIndex, InputStream x, int length) throws SQLException {
        statement.setUnicodeStream(parameterIndex, x, length);
        record(parameterIndex, x);
    }

    public <T> T unwrap(Class<T> iface) throws SQLException {
        return statement.unwrap(iface);
    }

    private void record(int index, Object x) {
        List<RDBLogEntry> l = datasource.getLog();
        if (l != null) {
            while (parameters.size() < index) {
                parameters.add("UNSET");
            }
            parameters.set(index - 1, x);
        }
    }
}
