package br.com.dio.board.service;

import br.com.dio.board.persistence.dao.BoardColumnDAO;
import br.com.dio.board.persistence.dao.BoardDAO;
import br.com.dio.board.persistence.entity.BoardColumnEntity;
import br.com.dio.board.persistence.entity.BoardEntity;

import java.sql.Connection;
import java.sql.SQLException;

public class BoardService {

    private final Connection connection;

    public BoardService(Connection connection) {
        this.connection = connection;
    }

    public BoardEntity insert(BoardEntity entity) throws SQLException {
        BoardDAO boardDAO = new BoardDAO(connection);
        BoardColumnDAO boardColumnDAO = new BoardColumnDAO(connection);
        Transactions.run(connection, () -> {
            boardDAO.insert(entity);
            for (BoardColumnEntity column : entity.getBoardColumns()) {
                column.setBoard(entity);
                boardColumnDAO.insert(column);
            }
        });
        return entity;
    }

    public boolean delete(Long id) throws SQLException {
        BoardDAO dao = new BoardDAO(connection);
        if (!dao.exists(id)) {
            return false;
        }
        Transactions.run(connection, () -> dao.delete(id));
        return true;
    }
}
