package com.yildizan.bot.kandilli.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serializable;

@AllArgsConstructor
public class Watch implements Serializable {

    @Getter private final long channelId;
    @Getter private final double magnitude;

}
