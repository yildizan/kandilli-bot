package com.yildizan.bot.kandilli.utility;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public final class Validator {

    private Validator() {}

    public static boolean validateCommand(String text) {
        return Objects.nonNull(text) && text.startsWith("!deprem");
    }

    public static boolean validateParameter(List<String> tokens) {
        return Arrays.asList(Constants.PARAMETERS).containsAll(
                tokens.stream()
                        .filter(t -> t.matches("[a-zA-Z]+"))
                        .collect(Collectors.toList()));
    }
}
