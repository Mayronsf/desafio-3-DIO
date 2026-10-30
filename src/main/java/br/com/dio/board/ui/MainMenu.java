package br.com.dio.board.ui;

import br.com.dio.board.persistence.entity.BoardColumnEntity;
import br.com.dio.board.persistence.entity.BoardColumnKindEnum;
import br.com.dio.board.persistence.entity.BoardEntity;
import br.com.dio.board.service.BoardQueryService;
import br.com.dio.board.service.BoardService;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static br.com.dio.board.persistence.config.ConnectionConfig.getConnection;
import static br.com.dio.board.persistence.entity.BoardColumnKindEnum.CANCEL;
import static br.com.dio.board.persistence.entity.BoardColumnKindEnum.FINAL;
import static br.com.dio.board.persistence.entity.BoardColumnKindEnum.INITIAL;
import static br.com.dio.board.persistence.entity.BoardColumnKindEnum.PENDING;

public class MainMenu {

    public void execute() {
        System.out.println("Bem-vindo ao gerenciador de boards!");
        while (true) {
            System.out.println("""

                    ===== MENU PRINCIPAL =====
                    1 - Criar um novo board
                    2 - Selecionar um board existente
                    3 - Excluir um board
                    4 - Sair""");
            switch (Input.readInt("Escolha uma opção: ", 1, 4)) {
                case 1 -> MenuAction.execute(this::createBoard);
                case 2 -> MenuAction.execute(this::selectBoard);
                case 3 -> MenuAction.execute(this::deleteBoard);
                case 4 -> {
                    System.out.println("Até a próxima!");
                    return;
                }
            }
        }
    }

    private void createBoard() throws SQLException {
        BoardEntity board = new BoardEntity();
        board.setName(Input.readText("Informe o nome do seu board: "));

        int additionalColumns = Input.readInt(
                "Seu board terá colunas além das 3 padrões? Se sim, informe quantas; senão, digite 0: ", 0, 20);

        List<BoardColumnEntity> columns = new ArrayList<>();
        columns.add(createColumn(Input.readText("Informe o nome da coluna inicial do board: "), INITIAL, 0));
        for (int i = 0; i < additionalColumns; i++) {
            columns.add(createColumn(Input.readText("Informe o nome da coluna de tarefa pendente: "), PENDING, i + 1));
        }
        columns.add(createColumn(Input.readText("Informe o nome da coluna final: "), FINAL, additionalColumns + 1));
        columns.add(createColumn(Input.readText("Informe o nome da coluna de cancelamento: "), CANCEL,
                additionalColumns + 2));
        board.setBoardColumns(columns);

        try (Connection connection = getConnection()) {
            new BoardService(connection).insert(board);
        }
        System.out.printf("Board '%s' criado com o id %s%n", board.getName(), board.getId());
    }

    private void selectBoard() throws SQLException {
        try (Connection connection = getConnection()) {
            BoardQueryService queryService = new BoardQueryService(connection);
            if (!printBoards(queryService.findAll())) return;

            long id = Input.readLong("Informe o id do board que deseja selecionar: ");
            Optional<BoardEntity> board = queryService.findById(id);
            if (board.isEmpty()) {
                System.out.printf("Não foi encontrado um board com id %s%n", id);
                return;
            }
            new BoardMenu(board.get()).execute();
        }
    }

    private void deleteBoard() throws SQLException {
        try (Connection connection = getConnection()) {
            if (!printBoards(new BoardQueryService(connection).findAll())) return;

            long id = Input.readLong("Informe o id do board que será excluído: ");
            if (new BoardService(connection).delete(id)) {
                System.out.printf("O board %s foi excluído%n", id);
            } else {
                System.out.printf("Não foi encontrado um board com id %s%n", id);
            }
        }
    }

    // Lista os boards cadastrados; retorna false se não houver nenhum
    private boolean printBoards(List<BoardEntity> boards) {
        if (boards.isEmpty()) {
            System.out.println("Nenhum board cadastrado ainda");
            return false;
        }
        System.out.println("Boards cadastrados:");
        boards.forEach(board -> System.out.printf("  [%s] %s%n", board.getId(), board.getName()));
        return true;
    }

    private BoardColumnEntity createColumn(String name, BoardColumnKindEnum kind, int order) {
        BoardColumnEntity column = new BoardColumnEntity();
        column.setName(name);
        column.setKind(kind);
        column.setOrder(order);
        return column;
    }
}
