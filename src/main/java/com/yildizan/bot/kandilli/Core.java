package com.yildizan.bot.kandilli;

import com.yildizan.bot.kandilli.model.Earthquake;
import com.yildizan.bot.kandilli.model.Watch;
import com.yildizan.bot.kandilli.service.Server;
import com.yildizan.bot.kandilli.utility.Constants;
import com.yildizan.bot.kandilli.utility.Validator;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class Core extends ListenerAdapter {

    private ScheduledFuture<?> future;
    private ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1);
    private List<Watch> watches = new ArrayList<>();

    public static void main(String[] args) throws Exception {
        new JDABuilder(Constants.TOKEN)
            .addEventListeners(new Core())
            .setActivity(Activity.listening("type !help"))
            .build();
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if(event.getAuthor().isBot() || !Validator.commands(event.getMessage().getContentRaw())) {
            return;
        }
        Message message = event.getMessage();
        TextChannel channel = event.getTextChannel();
        String command = message.getContentRaw().toLowerCase();

        if(command.equals("!help")) {
            send(channel, "`!deprem`: gerçekleşen son depremi gösterir.");
            send(channel, "`!deprem son <sayı>`: son <sayı> kadar depremi gösterir. *(en fazla " + Constants.MAX_COUNT + ")*");
            send(channel, "`!deprem büyük <sayı>`: <sayı> şiddetinden büyük ilk depremi gösterir.");
            send(channel, "`!deprem son <sayı1> büyük <sayı2>`: <sayı2> şiddetinden büyük, son <sayı1> kadar depremi gösterir. *(en fazla " + Constants.MAX_COUNT + ")*");
            send(channel, "`!deprem dürt <sayı>`: " + Constants.WATCH_PERIOD + " dakikada bir şiddeti <sayı>dan büyük deprem olup olmadığını kontrol eder, varsa dürter.");
            send(channel, "`!deprem dürtme`: dürtülmeyi durdurur.");
            send(channel, "`!clear`: bot tarafından gönderilen mesajları temizler.");
        }
        else if(command.equals("!deprem kimleri dürtüyosun")) {
            watches.forEach(c -> send(channel, c.getChannel().getGuild().getName() + " - " + c.getChannel().getName() + " - " + c.getMagnitude()));
        }
        else if(command.equals("!deprem kimlere bağlısın")) {
            event.getJDA().getGuilds().forEach(g -> send(channel, g.getName()));
        }
        else if(command.startsWith("!deprem")) {
            List<String> tokens = Arrays.asList(command.split("\\s+"));
            if(!Validator.parameters(tokens)) {
                send(channel, ":relieved: bilmediğim parametreler giriyorsun...");
                return;
            }

            if(tokens.size() == 1) {
                send(channel, display(Server.last()));
            }
            else if(tokens.size() == 2) {
                if(tokens.get(1).equals("dürtme")) {
                    if(isWatching(channel)) {
                        watches.removeIf(c -> c.getChannel().getGuild().getName().equals(channel.getGuild().getName()));
                        if(watches.isEmpty()) {
                            future.cancel(true);
                        }
                    }
                    send(channel, ":hand_splayed: saldım.");
                }
                else if(tokens.get(1).equals("son")) {
                    send(channel, display(Server.last()));
                }
                else {
                    send(channel, ":face_with_monocle: komutu yanlış/eksik girmiş olabilir misin?");
                }
            }
            else if(tokens.size() > 2) {
                // parse count
                int count = extractCount(tokens);

                // parse threshold
                double threshold = extractThreshold(tokens);

                // parse magnitude
                double magnitude = extractMagnitude(tokens);

                List<Earthquake> earthquakes;

                // valid magnitude
                if(magnitude > 0.0d) {
                    if(!isWatching(channel)) {
                        if(watches.isEmpty()) {
                            future = executor.scheduleAtFixedRate(this::watch, 0, Constants.WATCH_PERIOD, TimeUnit.MINUTES);
                        }
                        watches.add(new Watch(channel, magnitude));
                    }
                    send(channel, ":detective: izlemedeyim.");
                    return;
                }
                // valid count && valid threshold
                else if(count > 0 && threshold > 0.0d) {
                    earthquakes = Server.lastGreater(threshold, count);
                }
                // valid count && invalid threshold
                else if(count > 0) {
                    earthquakes = Server.last(count);
                }
                // invalid count && valid threshold
                else if(threshold > 0.0d) {
                    earthquakes = new ArrayList<>();
                    earthquakes.add(Server.lastGreater(threshold));
                }
                // invalid count && invalid threshold
                else {
                    send(channel, ":pensive: geçersiz parametre girdin, niye öyle oldu?");
                    return;
                }

                earthquakes.forEach(e -> send(channel, display(e)));
            }
        }
        else if(command.equals("!clear")) {
            OffsetDateTime twoWeeksAgo = OffsetDateTime.now().minus(2, ChronoUnit.WEEKS);
            List<Message> messages = new ArrayList<>();
            for(Message m : channel.getIterableHistory()) {
                if(m.getTimeCreated().isBefore(twoWeeksAgo)) {
                    break;
                }
                else if((m.getAuthor().isBot() && m.getAuthor().getName().equals(Constants.SELFNAME)) ||
                        Validator.commands(m.getContentRaw())) {
                    messages.add(m);
                }
            }
            // jda api restriction: deleting count should be between 2 and 100
            if(messages.size() > 1) {
                channel.deleteMessages(messages.stream().limit(Math.min(messages.size(), 100)).collect(Collectors.toList())).queue();
            }
        }
        else {
            send(channel, ":pensive: geçersiz parametre girdin, niye öyle oldu?");
        }
    }

    private void watch() {
        List<Earthquake> earthquakes = Server.lastIn(Constants.WATCH_PERIOD);
        watches.forEach(w ->
            earthquakes.stream()
                .filter(e -> e.getMagnitude() >= w.getMagnitude())
                .forEach(e -> send(w.getChannel(), display(e)))
        );
    }

    private int extractCount(List<String> tokens) {
        try {
            return Math.min(Integer.parseInt(tokens.get(tokens.indexOf("son") + 1)), Constants.MAX_COUNT);
        }
        catch (Exception e) {
            return 0;
        }
    }

    private double extractThreshold(List<String> tokens) {
        try {
            return Double.parseDouble(tokens.get(tokens.indexOf("büyük") + 1));
        }
        catch (Exception e) {
            return 0.0d;
        }
    }

    private double extractMagnitude(List<String> tokens) {
        try {
            return Double.parseDouble(tokens.get(tokens.indexOf("dürt") + 1));
        }
        catch (Exception e) {
            return 0.0d;
        }
    }

    private void send(TextChannel channel, String message) {
        channel.sendMessage(message.substring(0, Math.min(message.length(), 200))).queue();
    }

    private String display(Earthquake earthquake) {
        if(earthquake == null) {
            return ":fireworks: deprem kaydı yok.";
        }
        else {
            return earthquake.toString();
        }
    }

    private boolean isWatching(TextChannel channel) {
        return watches.stream().anyMatch(w -> w.getChannel().getGuild().getName().equals(channel.getGuild().getName()));
    }

}
