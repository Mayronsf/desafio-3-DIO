package br.com.dio.board.persistence.dao;

import br.com.dio.board.persistence.entity.BoardEntity;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BoardDAO {

    private final Connection connection;

    public BoardDAO(Connection connection) {
        this.connection = connection;
    }

    public BoardEntity insert(BoardEntity entity) throws SQLException {
        String sql = "INSERT INTO BOARDS (name) VALUES (?)";
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, entity.getName());
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    entity.setId(keys.getLong(1));
                }
            }
        }
        return entity;
    }

    public void delete(Long id) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("DELETE FROM BOARDS WHERE id = ?")) {
            statement.setLong(1, id);
            statement.executeUpdate();
        }
    }

    public Optional<BoardEntity> findById(Long id) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("SELECT id, name FROM BOARDS WHERE id = ?")) {
            statement.setLong(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? Optional.of(toEntity(resultSet)) : Optional.empty();
            }
        }
    }

    public List<BoardEntity> findAll() throws SQLException {
        List<BoardEntity> boards = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement("SELECT id, name FROM BOARDS ORDER BY id");
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                boards.add(toEntity(resultSet));
            }
        }
        return boards;
    }

    public boolean exists(Long id) throws SQLException {
        return findById(id).isPresent();
    }

    private BoardEntity toEntity(ResultSet resultSet) throws SQLException {
        BoardEntity entity = new BoardEntity();
        entity.setId(resultSet.getLong("id"));
        entity.setName(resultSet.getString("name"));
        return entity;
    }
}
