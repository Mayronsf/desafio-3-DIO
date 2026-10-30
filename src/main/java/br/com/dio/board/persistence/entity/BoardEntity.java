package br.com.dio.board.persistence.entity;

import java.util.ArrayList;
import java.util.List;

import static br.com.dio.board.persistence.entity.BoardColumnKindEnum.CANCEL;
import static br.com.dio.board.persistence.entity.BoardColumnKindEnum.INITIAL;

public class BoardEntity {

    private Long id;
    private String name;
    private List<BoardColumnEntity> boardColumns = new ArrayList<>();

    public BoardColumnEntity getInitialColumn() {
        return getColumnByKind(INITIAL);
    }

    public BoardColumnEntity getCancelColumn() {
        return getColumnByKind(CANCEL);
    }

    private BoardColumnEntity getColumnByKind(BoardColumnKindEnum kind) {
        return boardColumns.stream()
                .filter(column -> column.getKind() == kind)
                .findFirst()
                .orElseThrow();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<BoardColumnEntity> getBoardColumns() {
        return boardColumns;
    }

    public void setBoardColumns(List<BoardColumnEntity> boardColumns) {
        this.boardColumns = boardColumns;
    }
}
