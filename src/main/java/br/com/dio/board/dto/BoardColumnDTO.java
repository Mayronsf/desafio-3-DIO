package br.com.dio.board.dto;

import br.com.dio.board.persistence.entity.BoardColumnKindEnum;

public record BoardColumnDTO(Long id, String name, BoardColumnKindEnum kind, int cardsAmount) {
}
