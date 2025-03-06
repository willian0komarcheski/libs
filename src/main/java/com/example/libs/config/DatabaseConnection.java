package com.example.libs.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

@Component
public class DatabaseConnection {

    private static DatabaseConnection instance;

    private Connection connection;

    @Value("${database.url}")
    private String url;

    @Value("${database.username}")
    private String username;

    @Value("${database.password}")
    private String password;

    @Value("${database.driver-class-name}")
    private String driverClassName;

    private DatabaseConnection() {
        try {
            Class.forName(driverClassName);    
            this.connection = DriverManager.getConnection(this.url, this.username, this.password);
        } catch (SQLException ex) {
            ex.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static synchronized DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }

        return instance;
    }
    public Connection getConnection() {
        return this.connection;
    }
}