package com.yildizan.bot.kandilli.utility;

import java.util.List;

public final class Parser {

    private Parser() {}

    public static int extractCount(List<String> tokens) {
        try {
            return Math.min(Integer.parseInt(tokens.get(tokens.indexOf("son") + 1)), Constants.MAX_COUNT);
        }
        catch (Exception e) {
            return 0;
        }
    }

    public static double extractThreshold(List<String> tokens) {
        try {
            return Double.parseDouble(tokens.get(tokens.indexOf("büyük") + 1));
        }
        catch (Exception e) {
            return 0.0d;
        }
    }

    public static double extractMagnitude(List<String> tokens) {
        try {
            return Double.parseDouble(tokens.get(tokens.indexOf("dürt") + 1));
        }
        catch (Exception e) {
            return 0.0d;
        }
    }

}
