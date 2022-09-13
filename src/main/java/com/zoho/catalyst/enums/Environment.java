package com.zoho.catalyst.enums;

import lombok.Getter;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum Environment {
    DEVELOPMENT("Development");

    @Getter
    private final String env;
    private static final Map<String, Environment> ENV_MAP = Stream
            .of(Environment.values())
            .collect(Collectors.toMap(s -> s.env, Function.identity()));

    Environment(String name) {
        env = name;
    }

    public static Environment fromString(String givenStr) {
        return Optional
                .ofNullable(ENV_MAP.get(givenStr))
                .orElse(DEVELOPMENT);
    }
}
