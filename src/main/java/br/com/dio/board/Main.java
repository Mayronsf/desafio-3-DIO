package br.com.dio.board;

import br.com.dio.board.persistence.migration.MigrationStrategy;
import br.com.dio.board.ui.MainMenu;

import java.sql.Connection;
import java.sql.SQLException;

import static br.com.dio.board.persistence.config.ConnectionConfig.getConnection;

public class Main {

    public static void main(String[] args) {
        try (Connection connection = getConnection()) {
            new MigrationStrategy(connection).executeMigration();
        } catch (SQLException e) {
            System.out.println("Não foi possível conectar ao banco de dados: " + e.getMessage());
            System.out.println("Confira as variáveis DB_URL, DB_USER e DB_PASSWORD (veja o README)");
            return;
        }
        new MainMenu().execute();
    }
}
