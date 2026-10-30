package br.com.dio.board.service;

import br.com.dio.board.persistence.dao.BoardColumnDAO;
import br.com.dio.board.persistence.entity.BoardColumnEntity;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

public class BoardColumnQueryService {

    private final Connection connection;

    public BoardColumnQueryService(Connection connection) {
        this.connection = connection;
    }

    public Optional<BoardColumnEntity> findById(Long id) throws SQLException {
        return new BoardColumnDAO(connection).findById(id);
    }
}
