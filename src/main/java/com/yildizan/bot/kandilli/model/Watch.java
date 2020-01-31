package com.yildizan.bot.kandilli.model;

import net.dv8tion.jda.api.entities.TextChannel;

public class Watch {

    private TextChannel channel;
    private double magnitude;

    public Watch(TextChannel channel, double magnitude) {
        this.channel = channel;
        this.magnitude = magnitude;
    }

    public TextChannel getChannel() {
        return channel;
    }

    public void setChannel(TextChannel channel) {
        this.channel = channel;
    }

    public double getMagnitude() {
        return magnitude;
    }

    public void setMagnitude(double magnitude) {
        this.magnitude = magnitude;
    }
}
