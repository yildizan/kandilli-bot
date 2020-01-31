package com.yildizan.bot.kandilli.model;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Earthquake {

    private Date date;
    private String time;
    private double latitude;
    private double longitude;
    private double depth;
    private double magnitude;
    private String location;

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getDepth() {
        return depth;
    }

    public void setDepth(double depth) {
        this.depth = depth;
    }

    public double getMagnitude() {
        return magnitude;
    }

    public void setMagnitude(double magnitude) {
        this.magnitude = magnitude;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    @Override
    public String toString() {
        return "```⏰: " + new SimpleDateFormat("HH:mm:ss").format(date) + '\n' +
                "\uD83D\uDCC5: " + new SimpleDateFormat("dd.MM.yyyy").format(date) + '\n' +
                "\uD83D\uDCC8: " + magnitude + '\n' +
                "\uD83D\uDCCD : " + location + "```";
    }
}
