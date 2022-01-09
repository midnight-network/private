package net.midnightmc.core.api;

import javax.sql.rowset.CachedRowSet;
import javax.sql.rowset.RowSetProvider;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class EasyStatement implements AutoCloseable {

    private final PreparedStatement pstmt;

    public EasyStatement(Connection connection, String sql) throws SQLException {
        if (sql == null) {
            throw new SQLException("sql is null");
        }
        pstmt = connection.prepareStatement(sql);
    }

    public EasyStatement setArg(int index, String value) throws SQLException {
        pstmt.setString(index, value);
        return this;
    }

    public EasyStatement setArg(int index, int value) throws SQLException {
        pstmt.setInt(index, value);
        return this;
    }

    public EasyStatement setArg(int index, long value) throws SQLException {
        pstmt.setLong(index, value);
        return this;
    }

    public EasyStatement setArg(int index, InputStream is) throws SQLException {
        pstmt.setBlob(index, is);
        return this;
    }

    public CachedRowSet executeQuery() throws SQLException {
        CachedRowSet rowset = RowSetProvider.newFactory().createCachedRowSet();
        ResultSet rs = pstmt.executeQuery();
        rowset.populate(rs);
        rs.close();
        close();
        return rowset;
    }

    public boolean execute() throws SQLException {
        boolean result = pstmt.execute();
        close();
        return result;
    }

    public int executeUpdate() throws SQLException {
        int result = pstmt.executeUpdate();
        close();
        return result;
    }

    @Override
    public void close() throws SQLException {
        pstmt.close();
    }

}
