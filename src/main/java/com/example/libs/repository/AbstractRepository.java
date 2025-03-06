package com.example.libs.repository;

import com.example.libs.config.DatabaseConnection;
import com.example.libs.mapper.EntityMapper;

import java.lang.reflect.ParameterizedType;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;

public abstract class AbstractRepository<T> {
    protected Connection connection;
    protected EntityMapper<T> mapper;

    @SuppressWarnings("unchecked")
    public AbstractRepository() {
        this.connection = DatabaseConnection.getInstance().getConnection();
        
        Class<T> entityClass = (Class<T>) ((ParameterizedType) getClass()
                .getGenericSuperclass())
                .getActualTypeArguments()[0];

        this.mapper = new EntityMapper<>(entityClass);
    }

    public void create(T entity) {
        try {
            String query = mapper.generateInsertQuery(entity);
            PreparedStatement ps = connection.prepareStatement(query);
            Map<String, Object> columns = mapper.getColumnsAndValues(entity);
            int index = 1;
            for (Object value : columns.values()) {
                ps.setObject(index++, value);
            }
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public T read(Object id) {
        try {
            String query = mapper.generateSelectQuery();
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setObject(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapper.mapResultSetToEntity(rs);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void update(T entity) {
        try {
            String query = mapper.generateUpdateQuery(entity);
            PreparedStatement ps = connection.prepareStatement(query);
            Map<String, Object> columns = mapper.getColumnsAndValues(entity);
            int index = 1;
            for (String column : columns.keySet()) {
                if (!column.equals("id")) {
                    ps.setObject(index++, columns.get(column));
                }
            }
            ps.setObject(index, columns.get("id"));
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void delete(Object id) {
        try {
            String query = mapper.generateDeleteQuery();
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setObject(1, id);
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
