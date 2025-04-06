package com.example.libs.repository;

import com.example.libs.config.DatabaseConnection;
import com.example.libs.mapper.EntityMapper;

import java.lang.reflect.ParameterizedType;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class AbstractRepository<T> {
    protected Connection connection;
    protected EntityMapper<T> entityMapper;

    @SuppressWarnings("unchecked")
    public AbstractRepository() {
        this.connection = DatabaseConnection.getInstance().getConnection();
        
        Class<T> entityClass = (Class<T>) ((ParameterizedType) getClass()
                .getGenericSuperclass())
                .getActualTypeArguments()[0];

        this.entityMapper = new EntityMapper<>(entityClass);
    }

    public void create(T entity) {
        try {
            String query = entityMapper.generateInsertQuery(entity);
            PreparedStatement ps = connection.prepareStatement(query);
            Map<String, Object> columns = entityMapper.getColumnsAndValues(entity);
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
            String query = entityMapper.generateSelectQuery();
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setObject(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return entityMapper.mapResultSetToEntity(rs);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void update(T entity) {
        try {
            String query = entityMapper.generateUpdateQuery(entity);
            PreparedStatement ps = connection.prepareStatement(query);
            Map<String, Object> columns = entityMapper.getColumnsAndValues(entity);
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
            String query = entityMapper.generateDeleteQuery();
            PreparedStatement ps = connection.prepareStatement(query);
            ps.setObject(1, id);
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<T> getAll() {
        String query = entityMapper.generateSelectAllQuery();
        List<T> users = new ArrayList<>();

        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                T obj = entityMapper.mapResultSetToEntity(rs);
                users.add(obj);
            }
        } catch(Exception ex) {
            System.err.println("erro ao requisitar dados");
        }
        return users;
    }
}
