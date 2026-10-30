package br.com.dio.board.dto;

import java.time.LocalDateTime;

public record CardDetailsDTO(Long id,
                             String title,
                             String description,
                             boolean blocked,
                             LocalDateTime blockedAt,
                             String blockReason,
                             int blocksAmount,
                             Long columnId,
                             String columnName) {
}
