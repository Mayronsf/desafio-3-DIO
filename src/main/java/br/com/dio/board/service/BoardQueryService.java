package br.com.dio.board.service;

import br.com.dio.board.dto.BoardDetailsDTO;
import br.com.dio.board.persistence.dao.BoardColumnDAO;
import br.com.dio.board.persistence.dao.BoardDAO;
import br.com.dio.board.persistence.entity.BoardEntity;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class BoardQueryService {

    private final Connection connection;

    public BoardQueryService(Connection connection) {
        this.connection = connection;
    }

    public List<BoardEntity> findAll() throws SQLException {
        return new BoardDAO(connection).findAll();
    }

    public Optional<BoardEntity> findById(Long id) throws SQLException {
        Optional<BoardEntity> optional = new BoardDAO(connection).findById(id);
        if (optional.isPresent()) {
            BoardEntity entity = optional.get();
            entity.setBoardColumns(new BoardColumnDAO(connection).findByBoardId(id));
        }
        return optional;
    }

    public Optional<BoardDetailsDTO> showBoardDetails(Long id) throws SQLException {
        Optional<BoardEntity> optional = new BoardDAO(connection).findById(id);
        if (optional.isEmpty()) {
            return Optional.empty();
        }
        BoardEntity entity = optional.get();
        return Optional.of(new BoardDetailsDTO(entity.getId(), entity.getName(),
                new BoardColumnDAO(connection).findByBoardIdWithDetails(id)));
    }
}
