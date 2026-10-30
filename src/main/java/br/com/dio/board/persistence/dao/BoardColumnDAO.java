package br.com.dio.board.persistence.dao;

import br.com.dio.board.dto.BoardColumnDTO;
import br.com.dio.board.persistence.entity.BoardColumnEntity;
import br.com.dio.board.persistence.entity.CardEntity;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static br.com.dio.board.persistence.entity.BoardColumnKindEnum.findByName;

public class BoardColumnDAO {

    private final Connection connection;

    public BoardColumnDAO(Connection connection) {
        this.connection = connection;
    }

    public BoardColumnEntity insert(BoardColumnEntity entity) throws SQLException {
        String sql = "INSERT INTO BOARDS_COLUMNS (name, `order`, kind, board_id) VALUES (?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, entity.getName());
            statement.setInt(2, entity.getOrder());
            statement.setString(3, entity.getKind().name());
            statement.setLong(4, entity.getBoard().getId());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    entity.setId(keys.getLong(1));
                }
            }
        }
        return entity;
    }

    public List<BoardColumnEntity> findByBoardId(Long boardId) throws SQLException {
        String sql = "SELECT id, name, `order`, kind FROM BOARDS_COLUMNS WHERE board_id = ? ORDER BY `order`";
        List<BoardColumnEntity> columns = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, boardId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    BoardColumnEntity entity = new BoardColumnEntity();
                    entity.setId(resultSet.getLong("id"));
                    entity.setName(resultSet.getString("name"));
                    entity.setOrder(resultSet.getInt("order"));
                    entity.setKind(findByName(resultSet.getString("kind")));
                    columns.add(entity);
                }
            }
        }
        return columns;
    }

    public List<BoardColumnDTO> findByBoardIdWithDetails(Long boardId) throws SQLException {
        String sql = """
                SELECT bc.id, bc.name, bc.kind,
                       (SELECT COUNT(c.id) FROM CARDS c WHERE c.board_column_id = bc.id) cards_amount
                  FROM BOARDS_COLUMNS bc
                 WHERE bc.board_id = ?
                 ORDER BY bc.`order`
                """;
        List<BoardColumnDTO> columns = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, boardId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    columns.add(new BoardColumnDTO(
                            resultSet.getLong("id"),
                            resultSet.getString("name"),
                            findByName(resultSet.getString("kind")),
                            resultSet.getInt("cards_amount")
                    ));
                }
            }
        }
        return columns;
    }

    public Optional<BoardColumnEntity> findById(Long id) throws SQLException {
        String sql = """
                SELECT bc.name AS column_name, bc.kind, c.id AS card_id, c.title, c.description
                  FROM BOARDS_COLUMNS bc
                  LEFT JOIN CARDS c ON c.board_column_id = bc.id
                 WHERE bc.id = ?
                 ORDER BY c.id
                """;
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return Optional.empty();
                }
                BoardColumnEntity entity = new BoardColumnEntity();
                entity.setId(id);
                entity.setName(resultSet.getString("column_name"));
                entity.setKind(findByName(resultSet.getString("kind")));
                do {
                    // Coluna sem cards: o LEFT JOIN traz uma linha com o card nulo
                    if (resultSet.getString("title") == null) {
                        break;
                    }
                    CardEntity card = new CardEntity();
                    card.setId(resultSet.getLong("card_id"));
                    card.setTitle(resultSet.getString("title"));
                    card.setDescription(resultSet.getString("description"));
                    entity.getCards().add(card);
                } while (resultSet.next());
                return Optional.of(entity);
            }
        }
    }
}
