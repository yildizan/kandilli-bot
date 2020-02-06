package com.yildizan.bot.kandilli.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.dv8tion.jda.api.entities.TextChannel;

@AllArgsConstructor
public class Watch {

    @Getter private TextChannel channel;
    @Getter private double magnitude;

}
