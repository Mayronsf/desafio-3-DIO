package br.com.dio.board.service;

import br.com.dio.board.dto.CardDetailsDTO;
import br.com.dio.board.persistence.dao.CardDAO;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

public class CardQueryService {

    private final Connection connection;

    public CardQueryService(Connection connection) {
        this.connection = connection;
    }

    public Optional<CardDetailsDTO> findById(Long id) throws SQLException {
        return new CardDAO(connection).findById(id);
    }
}
