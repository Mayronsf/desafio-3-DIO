package br.com.dio.board.ui;

import java.sql.SQLException;

// Ação de menu: erros de regra de negócio e de banco viram mensagens para o usuário
@FunctionalInterface
interface MenuAction {

    void run() throws SQLException;

    static void execute(MenuAction action) {
        try {
            action.run();
        } catch (SQLException e) {
            System.out.println("Erro ao acessar o banco de dados: " + e.getMessage());
        } catch (RuntimeException e) {
            System.out.println(e.getMessage());
        }
    }
}
