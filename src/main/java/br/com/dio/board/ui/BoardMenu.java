package br.com.dio.board.ui;

import br.com.dio.board.dto.BoardColumnInfoDTO;
import br.com.dio.board.dto.BoardDetailsDTO;
import br.com.dio.board.dto.CardDetailsDTO;
import br.com.dio.board.persistence.entity.BoardColumnEntity;
import br.com.dio.board.persistence.entity.BoardEntity;
import br.com.dio.board.persistence.entity.CardEntity;
import br.com.dio.board.service.BoardColumnQueryService;
import br.com.dio.board.service.BoardQueryService;
import br.com.dio.board.service.CardQueryService;
import br.com.dio.board.service.CardService;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import static br.com.dio.board.persistence.config.ConnectionConfig.getConnection;

public class BoardMenu {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final BoardEntity board;

    public BoardMenu(BoardEntity board) {
        this.board = board;
    }

    public void execute() {
        System.out.printf("Bem-vindo ao board '%s' (id %s)%n", board.getName(), board.getId());
        while (true) {
            System.out.println("""

                    ===== BOARD =====
                    1 - Criar um card
                    2 - Mover um card para a próxima coluna
                    3 - Bloquear um card
                    4 - Desbloquear um card
                    5 - Cancelar um card
                    6 - Ver board
                    7 - Ver coluna com cards
                    8 - Ver card
                    9 - Voltar para o menu anterior
                    10 - Sair""");
            switch (Input.readInt("Escolha uma opção: ", 1, 10)) {
                case 1 -> MenuAction.execute(this::createCard);
                case 2 -> MenuAction.execute(this::moveCardToNextColumn);
                case 3 -> MenuAction.execute(this::blockCard);
                case 4 -> MenuAction.execute(this::unblockCard);
                case 5 -> MenuAction.execute(this::cancelCard);
                case 6 -> MenuAction.execute(this::showBoard);
                case 7 -> MenuAction.execute(this::showColumn);
                case 8 -> MenuAction.execute(this::showCard);
                case 9 -> {
                    return;
                }
                case 10 -> {
                    System.out.println("Até a próxima!");
                    System.exit(0);
                }
            }
        }
    }

    private void createCard() throws SQLException {
        CardEntity card = new CardEntity();
        card.setTitle(Input.readText("Informe o título do card: "));
        card.setDescription(Input.readText("Informe a descrição do card: "));
        card.setBoardColumn(board.getInitialColumn());
        try (Connection connection = getConnection()) {
            new CardService(connection).create(card);
        }
        System.out.printf("Card criado com o id %s na coluna '%s'%n", card.getId(), board.getInitialColumn().getName());
    }

    private void moveCardToNextColumn() throws SQLException {
        long cardId = Input.readLong("Informe o id do card que deseja mover para a próxima coluna: ");
        try (Connection connection = getConnection()) {
            new CardService(connection).moveToNextColumn(cardId, columnsInfo());
        }
        System.out.println("Card movido para a próxima coluna");
    }

    private void blockCard() throws SQLException {
        long cardId = Input.readLong("Informe o id do card que será bloqueado: ");
        String reason = Input.readText("Informe o motivo do bloqueio: ");
        try (Connection connection = getConnection()) {
            new CardService(connection).block(cardId, reason, columnsInfo());
        }
        System.out.println("Card bloqueado");
    }

    private void unblockCard() throws SQLException {
        long cardId = Input.readLong("Informe o id do card que será desbloqueado: ");
        String reason = Input.readText("Informe o motivo do desbloqueio: ");
        try (Connection connection = getConnection()) {
            new CardService(connection).unblock(cardId, reason, columnsInfo());
        }
        System.out.println("Card desbloqueado");
    }

    private void cancelCard() throws SQLException {
        long cardId = Input.readLong("Informe o id do card que deseja mover para a coluna de cancelamento: ");
        try (Connection connection = getConnection()) {
            new CardService(connection).cancel(cardId, board.getCancelColumn().getId(), columnsInfo());
        }
        System.out.println("Card cancelado");
    }

    private void showBoard() throws SQLException {
        try (Connection connection = getConnection()) {
            Optional<BoardDetailsDTO> details = new BoardQueryService(connection).showBoardDetails(board.getId());
            details.ifPresent(b -> {
                System.out.printf("Board [%s, %s]%n", b.id(), b.name());
                b.columns().forEach(c -> System.out.printf("  Coluna [%s] tipo [%s] tem %s card(s)%n",
                        c.name(), c.kind(), c.cardsAmount()));
            });
        }
    }

    private void showColumn() throws SQLException {
        System.out.printf("Escolha uma coluna do board '%s':%n", board.getName());
        board.getBoardColumns().forEach(c -> System.out.printf("  [%s] %s - %s%n", c.getId(), c.getName(), c.getKind()));

        long columnId = Input.readLong("Informe o id da coluna: ");
        boolean belongsToBoard = board.getBoardColumns().stream().anyMatch(c -> c.getId() == columnId);
        if (!belongsToBoard) {
            System.out.printf("A coluna %s não pertence a este board%n", columnId);
            return;
        }

        try (Connection connection = getConnection()) {
            Optional<BoardColumnEntity> column = new BoardColumnQueryService(connection).findById(columnId);
            column.ifPresent(co -> {
                System.out.printf("Coluna %s tipo %s%n", co.getName(), co.getKind());
                if (co.getCards().isEmpty()) {
                    System.out.println("  Nenhum card nesta coluna");
                }
                co.getCards().forEach(ca -> System.out.printf("  Card %s - %s%n  Descrição: %s%n",
                        ca.getId(), ca.getTitle(), ca.getDescription()));
            });
        }
    }

    private void showCard() throws SQLException {
        long cardId = Input.readLong("Informe o id do card que deseja visualizar: ");
        try (Connection connection = getConnection()) {
            Optional<CardDetailsDTO> card = new CardQueryService(connection).findById(cardId);
            if (card.isEmpty()) {
                System.out.printf("Não existe um card com o id %s%n", cardId);
                return;
            }
            CardDetailsDTO c = card.get();
            System.out.printf("Card %s - %s%n", c.id(), c.title());
            System.out.printf("Descrição: %s%n", c.description());
            System.out.println(c.blocked()
                    ? "Está bloqueado desde %s. Motivo: %s".formatted(c.blockedAt().format(DATE_FORMAT), c.blockReason())
                    : "Não está bloqueado");
            System.out.printf("Já foi bloqueado %s vez(es)%n", c.blocksAmount());
            System.out.printf("Está no momento na coluna %s - %s%n", c.columnId(), c.columnName());
        }
    }

    private List<BoardColumnInfoDTO> columnsInfo() {
        return board.getBoardColumns().stream()
                .map(c -> new BoardColumnInfoDTO(c.getId(), c.getOrder(), c.getKind()))
                .toList();
    }
}
