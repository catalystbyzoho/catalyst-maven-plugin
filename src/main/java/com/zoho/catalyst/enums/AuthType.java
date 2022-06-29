package com.zoho.catalyst.enums;

import lombok.Getter;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum AuthType {
    OAUTH2("oauth2"),
    ZCATALYST_CLI("zcatalyst-cli");

    @Getter
    private final String type;
    private static final Map<String, AuthType> TYPE_MAP = Stream
            .of(AuthType.values())
            .collect(Collectors.toMap(s -> s.type, Function.identity()));

    AuthType(String name) {
        type = name;
    }

    public static AuthType fromString(String givenStr) {
        return Optional
                .ofNullable(TYPE_MAP.get(givenStr))
                .orElse(OAUTH2);
    }
}
