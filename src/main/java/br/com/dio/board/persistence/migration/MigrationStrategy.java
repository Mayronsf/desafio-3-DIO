package br.com.dio.board.persistence.migration;

import liquibase.Liquibase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.sql.Connection;
import java.util.logging.Level;
import java.util.logging.Logger;

// Cria/atualiza as tabelas do banco a partir dos changelogs do Liquibase
public record MigrationStrategy(Connection connection) {

    private static final Logger LIQUIBASE_LOGGER = Logger.getLogger("liquibase");

    public void executeMigration() {
        PrintStream originalOut = System.out;
        PrintStream originalErr = System.err;
        LIQUIBASE_LOGGER.setLevel(Level.WARNING);

        // As mensagens do Liquibase vão para liquibase.log em vez de poluir o terminal
        try (PrintStream logStream = new PrintStream(new FileOutputStream("liquibase.log"), true)) {
            System.setOut(logStream);
            System.setErr(logStream);
            try (JdbcConnection jdbcConnection = new JdbcConnection(connection);
                 Liquibase liquibase = new Liquibase("db/changelog/db.changelog-master.yml",
                         new ClassLoaderResourceAccessor(), jdbcConnection)) {
                liquibase.update();
            }
        } catch (IOException | LiquibaseException e) {
            throw new IllegalStateException("Falha ao executar as migrations: " + e.getMessage(), e);
        } finally {
            System.setOut(originalOut);
            System.setErr(originalErr);
        }
    }
}
