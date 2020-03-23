package com.yildizan.bot.kandilli.model;

import lombok.Data;

import java.text.SimpleDateFormat;
import java.util.Date;

@Data
public class Earthquake {

    private Date date;
    private double latitude;
    private double longitude;
    private double depth;
    private double magnitude;
    private String location;

    public String toString() {
        return toString(false);
    }

    public String toString(boolean inline) {
        return (inline ? "" : "```") + "⏰: " + new SimpleDateFormat("HH:mm:ss").format(date) + '\n' +
                "\uD83D\uDCC5: " + new SimpleDateFormat("dd.MM.yyyy").format(date) + '\n' +
                "\uD83D\uDCC8: " + magnitude + '\n' +
                "\uD83D\uDCCD: " + location + (inline ? "" : "```");
    }
}
