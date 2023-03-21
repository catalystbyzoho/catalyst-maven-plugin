package com.zoho.catalyst.enums;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Getter;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum DC {
    @JsonAlias("US")
    COM("com", "US", ".com"),
    @JsonAlias("IN")
    IN("in", "IN", ".in"),
    @JsonAlias("EU")
    EU("eu", "EU", ".eu"),
    @JsonAlias("AU")
    AU("au", "AU", ".com.au");

    @Getter
    private final String key;
    @Getter
    private final String label;
    @Getter
    private final String ext;
    private static final Map<String, DC> DC_MAP = Stream
            .of(DC.values())
            .collect(Collectors.toMap(s -> s.label, Function.identity()));

    DC(String key, String label, String ext) {
        this.key = key;
        this.label = label;
        this.ext = ext;
    }

    public static DC fromString(String givenStr) {
        return Optional
                .ofNullable(DC_MAP.get(givenStr))
                .orElse(COM);
    }
}
