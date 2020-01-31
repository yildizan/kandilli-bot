package com.yildizan.bot.kandilli.utility;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public final class Validator {

    private Validator() {}

    public static boolean commands(String text) {
        return Arrays.stream(Constants.COMMANDS).anyMatch(text::contains);
    }

    public static boolean parameters(List<String> tokens) {
        return Arrays.asList(Constants.PARAMETERS).containsAll(
                tokens.stream()
                        .filter(t -> t.matches("[a-zA-Z]+"))
                        .collect(Collectors.toList()));
    }
}
