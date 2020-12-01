package com.yildizan.bot.kandilli.worker;

import com.yildizan.bot.kandilli.Listener;
import com.yildizan.bot.kandilli.model.Earthquake;
import com.yildizan.bot.kandilli.model.Watch;
import com.yildizan.bot.kandilli.utility.Constants;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class Watcher {

    private ScheduledFuture<?> future;
    private final ScheduledThreadPoolExecutor executor;
    private final List<Watch> watches;
    private final Server server;

    public Watcher() {
        executor = new ScheduledThreadPoolExecutor(1);
        watches = new ArrayList<>();
        server = new Server();
    }

    private void watch(JDA jda) {
        List<Earthquake> earthquakes = server.lastIn(Constants.WATCH_PERIOD);
        for(Watch watch : watches) {
            TextChannel channel = jda.getTextChannelById(watch.getChannelId());
            if(channel == null) {
                watches.remove(watch);
                continue;
            }
            List<Earthquake> channelEarthquakes = earthquakes.stream().filter(e -> e.getMagnitude() >= watch.getMagnitude()).collect(Collectors.toList());
            if(!channelEarthquakes.isEmpty()) {
                Listener.send(channel, channelEarthquakes);
            }
        }
    }

    public void start(TextChannel channel, double magnitude) {
        if(!isWatching(channel)) {
            if(watches.isEmpty()) {
                future = executor.scheduleAtFixedRate(() -> watch(channel.getJDA()), 0, Constants.WATCH_PERIOD, TimeUnit.MINUTES);
            }
            watches.add(new Watch(channel.getIdLong(), magnitude));
        }
        Listener.send(channel, ":detective: izlemedeyim.");
    }

    public void stop(TextChannel channel) {
        if(isWatching(channel)) {
            watches.removeIf(c -> c.getChannelId() == channel.getIdLong());
            if(watches.isEmpty()) {
                future.cancel(true);
            }
        }
        Listener.send(channel, ":hand_splayed: saldım.");
    }

    private boolean isWatching(TextChannel channel) {
        return watches.stream().anyMatch(w -> w.getChannelId() == channel.getIdLong());
    }

}
