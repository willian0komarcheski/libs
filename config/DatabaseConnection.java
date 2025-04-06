package com.example.libs.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    private static DatabaseConnection instance;
    private Connection connection;
    
    private String url;
    private String username;
    private String password;
    private String driverClassName;

    private DatabaseConnection() {
        try {
            this.url = System.getenv("SPRING_DATASOURCE_URL");
            this.username = System.getenv("SPRING_DATASOURCE_USERNAME");
            this.password = System.getenv("SPRING_DATASOURCE_PASSWORD");
            this.driverClassName = System.getenv("SPRING_DATASOURCE_DRIVER");

            if (url == null || username == null || password == null) {
                throw new RuntimeException("Database connection environment variables are not fully configured");
            }

            Class.forName(this.driverClassName);

            this.connection = DriverManager.getConnection(this.url, this.username, this.password);

        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
            throw new RuntimeException("Erro ao conectar com o banco de dados", ex);
        } catch (SQLException ex) {
            throw new RuntimeException("chamada com sintaxe errada no banco",ex);
        }
    }

    public static synchronized DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    public Connection getConnection() {
        return connection;
    }
}
