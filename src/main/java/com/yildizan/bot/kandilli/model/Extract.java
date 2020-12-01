package com.yildizan.bot.kandilli.model;

import com.yildizan.bot.kandilli.model.Earthquake;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Extract {

    private Earthquake earthquake;
    private String remaining;

}
