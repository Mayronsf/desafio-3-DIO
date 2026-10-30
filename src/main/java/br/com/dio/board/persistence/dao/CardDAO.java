package br.com.dio.board.persistence.dao;

import br.com.dio.board.dto.CardDetailsDTO;
import br.com.dio.board.persistence.entity.CardEntity;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Optional;

public class CardDAO {

    private final Connection connection;

    public CardDAO(Connection connection) {
        this.connection = connection;
    }

    public CardEntity insert(CardEntity entity) throws SQLException {
        String sql = "INSERT INTO CARDS (title, description, board_column_id) VALUES (?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, entity.getTitle());
            statement.setString(2, entity.getDescription());
            statement.setLong(3, entity.getBoardColumn().getId());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    entity.setId(keys.getLong(1));
                }
            }
        }
        return entity;
    }

    public void moveToColumn(Long columnId, Long cardId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "UPDATE CARDS SET board_column_id = ? WHERE id = ?")) {
            statement.setLong(1, columnId);
            statement.setLong(2, cardId);
            statement.executeUpdate();
        }
    }

    public Optional<CardDetailsDTO> findById(Long id) throws SQLException {
        String sql = """
                SELECT c.id, c.title, c.description, b.blocked_at, b.block_reason,
                       c.board_column_id, bc.name AS column_name,
                       (SELECT COUNT(sub_b.id) FROM BLOCKS sub_b WHERE sub_b.card_id = c.id) blocks_amount
                  FROM CARDS c
                  LEFT JOIN BLOCKS b ON c.id = b.card_id AND b.unblocked_at IS NULL
                 INNER JOIN BOARDS_COLUMNS bc ON bc.id = c.board_column_id
                 WHERE c.id = ?
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return Optional.empty();
                }
                Timestamp blockedAt = resultSet.getTimestamp("blocked_at");
                return Optional.of(new CardDetailsDTO(
                        resultSet.getLong("id"),
                        resultSet.getString("title"),
                        resultSet.getString("description"),
                        blockedAt != null,
                        blockedAt == null ? null : blockedAt.toLocalDateTime(),
                        resultSet.getString("block_reason"),
                        resultSet.getInt("blocks_amount"),
                        resultSet.getLong("board_column_id"),
                        resultSet.getString("column_name")
                ));
            }
        }
    }
}
