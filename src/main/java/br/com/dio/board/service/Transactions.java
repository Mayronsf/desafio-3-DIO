package br.com.dio.board.service;

import java.sql.Connection;
import java.sql.SQLException;

// Executa uma ação no banco com commit em caso de sucesso e rollback em caso de erro
final class Transactions {

    @FunctionalInterface
    interface SqlAction {
        void run() throws SQLException;
    }

    private Transactions() {
    }

    static void run(Connection connection, SqlAction action) throws SQLException {
        try {
            action.run();
            connection.commit();
        } catch (SQLException | RuntimeException e) {
            connection.rollback();
            throw e;
        }
    }
}
