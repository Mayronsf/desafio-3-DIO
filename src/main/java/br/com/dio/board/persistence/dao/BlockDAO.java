package br.com.dio.board.persistence.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

public class BlockDAO {

    private final Connection connection;

    public BlockDAO(Connection connection) {
        this.connection = connection;
    }

    public void block(String reason, Long cardId) throws SQLException {
        String sql = "INSERT INTO BLOCKS (blocked_at, block_reason, card_id) VALUES (?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            statement.setString(2, reason);
            statement.setLong(3, cardId);
            statement.executeUpdate();
        }
    }

    public void unblock(String reason, Long cardId) throws SQLException {
        String sql = """
                UPDATE BLOCKS
                   SET unblocked_at = ?, unblock_reason = ?
                 WHERE card_id = ? AND unblocked_at IS NULL
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            statement.setString(2, reason);
            statement.setLong(3, cardId);
            statement.executeUpdate();
        }
    }
}
