package com.example.libs.mapper;

import com.example.libs.annotations.entity.ColumnName;
import com.example.libs.annotations.entity.TableName;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class EntityMapper<T> {
    private final Class<T> clazz;

    public EntityMapper(Class<T> clazz) {
        this.clazz = clazz;
    }

    public String getTableName() {
        return clazz.getAnnotation(TableName.class).name();
    }

    public Map<String, Field> getColumnFields() {
        Map<String, Field> map = new LinkedHashMap<>();
        for (Field field : clazz.getDeclaredFields()) {
            ColumnName column = field.getAnnotation(ColumnName.class);
            if (column != null) {
                field.setAccessible(true);
                map.put(column.name(), field);
            }
        }
        return map;
    }

    public Map<String, Object> getColumnsAndValues(T entity) {
        Map<String, Object> map = new LinkedHashMap<>();
        getColumnFields().forEach((column, field) -> {
            try {
                map.put(column, field.get(entity));
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        });
        return map;
    }

    public T mapResultSetToEntity(ResultSet rs) {
        try {
            T entity = clazz.getDeclaredConstructor().newInstance();
            for (Map.Entry<String, Field> entry : getColumnFields().entrySet()) {
                Object value = rs.getObject(entry.getKey());
                entry.getValue().set(entity, value);
            }
            return entity;
        } catch (Exception e) {
            throw new RuntimeException("Erro ao mapear ResultSet para entidade", e);
        }
    }

    public String generateInsertQuery(T entity) {
        String table = getTableName();
        Map<String, Object> columns = getColumnsAndValues(entity);
        String colNames = String.join(", ", columns.keySet());
        String placeholders = columns.keySet().stream().map(col -> "?").collect(Collectors.joining(", "));
        return String.format("INSERT INTO %s (%s) VALUES (%s)", table, colNames, placeholders);
    }
    
    public String generateSelectQuery() {
        String table = getTableName();
        return String.format("SELECT * FROM %s WHERE id = ?", table);
    }
    
    public String generateUpdateQuery(T entity) {
        String table = getTableName();
        Map<String, Object> columns = getColumnsAndValues(entity);
        String setClause = columns.keySet().stream()
                .filter(col -> !col.equals("id"))
                .map(col -> col + " = ?")
                .collect(Collectors.joining(", "));
        return String.format("UPDATE %s SET %s WHERE id = ?", table, setClause);
    }
    
    public String generateDeleteQuery() {
        String table = getTableName();
        return String.format("DELETE FROM %s WHERE id = ?", table);
    }
}
