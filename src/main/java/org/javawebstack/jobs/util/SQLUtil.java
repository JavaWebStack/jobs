package org.javawebstack.jobs.util;

import org.javawebstack.abstractdata.AbstractElement;
import org.javawebstack.orm.wrapper.SQL;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

public class SQLUtil {

    public static int insert(SQL sql, String table, Map<String, Object> values) {
        List<String> keys = new ArrayList<>(values.keySet());
        Object[] valueArray = new Object[keys.size()];
        for(int i=0; i<valueArray.length; i++)
            valueArray[i] = transformValue(values.get(keys.get(i)));
        try {
            return sql.write("INSERT INTO `" + table + "` (" + keys.stream().map(k -> "`" + k + "`").collect(Collectors.joining(",")) + ") VALUES (" + keys.stream().map(k -> "?").collect(Collectors.joining(",")) + ");", valueArray);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void update(SQL sql, String table, Map<String, Object> values, String where, Object... params) {
        List<String> keys = new ArrayList<>(values.keySet());
        Object[] valueArray = new Object[keys.size() + params.length];
        for(int i=0; i<keys.size(); i++)
            valueArray[i] = transformValue(values.get(keys.get(i)));
        for(int i=0; i<params.length; i++)
            valueArray[i + keys.size()] = transformValue(params[i]);
        try {
            sql.write("UPDATE `" + table + "` SET " + keys.stream().map(k -> "`" + k + "`=?").collect(Collectors.joining(",")) + (((where != null) && (where.length() > 0)) ? (" WHERE " + where) : "") + ";", valueArray);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void delete(SQL sql, String table, String where, Object... params) {
        try {
            sql.write("DELETE FROM `" + table + "`" + (((where != null) && (where.length() > 0)) ? (" WHERE " + where) : "") + ";", params);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static List<Map<String, Object>> select(SQL sql, String table, String columns, String query, Object... params) {
        ResultSet rs;
        try {
            rs = sql.read("SELECT " + columns + " FROM `" + table + "`" + (((query != null) && (query.length() > 0)) ? (" " + query) : "") + ";", params);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        try {
            List<Map<String, Object>> results = new ArrayList<>();
            String[] columnNames = new String[rs.getMetaData().getColumnCount()];
            for(int i=0; i<columnNames.length; i++) {
                columnNames[i] = rs.getMetaData().getColumnLabel(i+1);
                if(columnNames[i] == null)
                    columnNames[i] = rs.getMetaData().getColumnName(i+1);
            }
            while (rs.next()) {
                Map<String, Object> result = new HashMap<>();
                for(int i=0; i<columnNames.length; i++)
                    result.put(columnNames[i], rs.getObject(i+1));
                results.add(result);
            }
            return results;
        } catch (SQLException e) {
            sql.close(rs);
            throw new RuntimeException(e);
        }
    }

    private static Object transformValue(Object v) {
        if(v == null)
            return null;
        if(v.getClass().equals(UUID.class))
            return v.toString();
        if(v.getClass().isEnum())
            return ((Enum<?>) v).name();
        if(v instanceof AbstractElement)
            return ((AbstractElement) v).toJsonString();
        return v;
    }

}
