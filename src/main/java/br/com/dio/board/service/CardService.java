package br.com.dio.board.service;

import br.com.dio.board.dto.BoardColumnInfoDTO;
import br.com.dio.board.dto.CardDetailsDTO;
import br.com.dio.board.exception.CardBlockedException;
import br.com.dio.board.exception.CardFinishedException;
import br.com.dio.board.exception.EntityNotFoundException;
import br.com.dio.board.persistence.dao.BlockDAO;
import br.com.dio.board.persistence.dao.CardDAO;
import br.com.dio.board.persistence.entity.CardEntity;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import static br.com.dio.board.persistence.entity.BoardColumnKindEnum.CANCEL;
import static br.com.dio.board.persistence.entity.BoardColumnKindEnum.FINAL;

public class CardService {

    private final Connection connection;

    public CardService(Connection connection) {
        this.connection = connection;
    }

    public CardEntity create(CardEntity entity) throws SQLException {
        Transactions.run(connection, () -> new CardDAO(connection).insert(entity));
        return entity;
    }

    public void moveToNextColumn(Long cardId, List<BoardColumnInfoDTO> boardColumnsInfo) throws SQLException {
        Transactions.run(connection, () -> {
            CardDetailsDTO card = findCardOfBoard(cardId, boardColumnsInfo);
            if (card.blocked()) {
                throw new CardBlockedException(
                        "O card %s está bloqueado, é necessário desbloqueá-lo para mover".formatted(cardId));
            }
            BoardColumnInfoDTO currentColumn = findColumnOfCard(card, boardColumnsInfo);
            checkNotFinishedOrCanceled(cardId, currentColumn);
            BoardColumnInfoDTO nextColumn = boardColumnsInfo.stream()
                    .filter(column -> column.order() == currentColumn.order() + 1)
                    .findFirst()
                    .orElseThrow();
            new CardDAO(connection).moveToColumn(nextColumn.id(), cardId);
        });
    }

    public void cancel(Long cardId, Long cancelColumnId, List<BoardColumnInfoDTO> boardColumnsInfo)
            throws SQLException {
        Transactions.run(connection, () -> {
            CardDetailsDTO card = findCardOfBoard(cardId, boardColumnsInfo);
            if (card.blocked()) {
                throw new CardBlockedException(
                        "O card %s está bloqueado, é necessário desbloqueá-lo para cancelar".formatted(cardId));
            }
            checkNotFinishedOrCanceled(cardId, findColumnOfCard(card, boardColumnsInfo));
            new CardDAO(connection).moveToColumn(cancelColumnId, cardId);
        });
    }

    public void block(Long cardId, String reason, List<BoardColumnInfoDTO> boardColumnsInfo) throws SQLException {
        Transactions.run(connection, () -> {
            CardDetailsDTO card = findCardOfBoard(cardId, boardColumnsInfo);
            if (card.blocked()) {
                throw new CardBlockedException("O card %s já está bloqueado".formatted(cardId));
            }
            BoardColumnInfoDTO currentColumn = findColumnOfCard(card, boardColumnsInfo);
            if (currentColumn.kind() == FINAL || currentColumn.kind() == CANCEL) {
                throw new IllegalStateException(
                        "O card está em uma coluna do tipo %s e não pode ser bloqueado".formatted(currentColumn.kind()));
            }
            new BlockDAO(connection).block(reason, cardId);
        });
    }

    public void unblock(Long cardId, String reason, List<BoardColumnInfoDTO> boardColumnsInfo) throws SQLException {
        Transactions.run(connection, () -> {
            CardDetailsDTO card = findCardOfBoard(cardId, boardColumnsInfo);
            if (!card.blocked()) {
                throw new CardBlockedException("O card %s não está bloqueado".formatted(cardId));
            }
            new BlockDAO(connection).unblock(reason, cardId);
        });
    }

    // Garante que o card existe e pertence ao board selecionado
    private CardDetailsDTO findCardOfBoard(Long cardId, List<BoardColumnInfoDTO> boardColumnsInfo)
            throws SQLException {
        CardDetailsDTO card = new CardDAO(connection).findById(cardId)
                .orElseThrow(() -> new EntityNotFoundException("O card de id %s não foi encontrado".formatted(cardId)));
        boolean belongsToBoard = boardColumnsInfo.stream().anyMatch(column -> column.id().equals(card.columnId()));
        if (!belongsToBoard) {
            throw new EntityNotFoundException("O card de id %s não pertence a este board".formatted(cardId));
        }
        return card;
    }

    private BoardColumnInfoDTO findColumnOfCard(CardDetailsDTO card, List<BoardColumnInfoDTO> boardColumnsInfo) {
        return boardColumnsInfo.stream()
                .filter(column -> column.id().equals(card.columnId()))
                .findFirst()
                .orElseThrow();
    }

    private void checkNotFinishedOrCanceled(Long cardId, BoardColumnInfoDTO column) {
        if (column.kind() == FINAL) {
            throw new CardFinishedException("O card %s já foi finalizado".formatted(cardId));
        }
        if (column.kind() == CANCEL) {
            throw new CardFinishedException("O card %s já foi cancelado".formatted(cardId));
        }
    }
}
