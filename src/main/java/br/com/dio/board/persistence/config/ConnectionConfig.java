package br.com.dio.board.persistence.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

// Dados de acesso lidos de variáveis de ambiente, com valores padrão para rodar localmente
public final class ConnectionConfig {

    private static final String URL = env("DB_URL",
            "jdbc:mysql://localhost:3306/board?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true");
    private static final String USER = env("DB_USER", "root");
    private static final String PASSWORD = env("DB_PASSWORD", "root");

    private ConnectionConfig() {
    }

    public static Connection getConnection() throws SQLException {
        Connection connection = DriverManager.getConnection(URL, USER, PASSWORD);
        connection.setAutoCommit(false);
        return connection;
    }

    private static String env(String name, String defaultValue) {
        String value = System.getenv(name);
        return value == null || value.isBlank() ? defaultValue : value;
    }
}
