package br.com.dio.board.persistence.entity;

import java.util.stream.Stream;

public enum BoardColumnKindEnum {

    INITIAL, PENDING, FINAL, CANCEL;

    public static BoardColumnKindEnum findByName(String name) {
        return Stream.of(values())
                .filter(kind -> kind.name().equals(name))
                .findFirst()
                .orElseThrow();
    }
}
