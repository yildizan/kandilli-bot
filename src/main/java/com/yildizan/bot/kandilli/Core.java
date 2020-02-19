package com.yildizan.bot.kandilli;

import com.yildizan.bot.kandilli.model.Earthquake;
import com.yildizan.bot.kandilli.model.Watch;
import com.yildizan.bot.kandilli.service.Server;
import com.yildizan.bot.kandilli.utility.Constants;
import com.yildizan.bot.kandilli.utility.Parser;
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
import java.util.Objects;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class Core extends ListenerAdapter {

    private ScheduledFuture<?> future;
    private final ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1);
    private final List<Watch> watches = new ArrayList<>();

    public static void main(String[] args) throws Exception {
        new JDABuilder(Constants.TOKEN)
            .addEventListeners(new Core())
            .setActivity(Activity.listening("type !deprem help"))
            .build();
        System.out.println(Constants.SELFNAME + " is online!");
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if(event.getAuthor().isBot() || !Validator.validateCommand(event.getMessage().getContentRaw())) {
            return;
        }
        Message message = event.getMessage();
        TextChannel channel = event.getTextChannel();
        String command = message.getContentRaw().toLowerCase();

        List<String> tokens = Arrays.asList(command.split("\\s+"));
        if(!Validator.validateParameter(tokens)) {
            send(channel, ":relieved: bilmediğim parametreler giriyorsun...");
            return;
        }

        if(tokens.size() == 1) {
            send(channel, display(Server.last()));
        }
        else if(tokens.size() == 2) {
            switch (tokens.get(1)) {
                // guide
                case "help":
                    showHelp(channel);
                    break;
                case "clear":
                    clear(channel);
                    break;
                case "dürtme":
                    stopWatching(channel);
                    break;
                case "son":
                    send(channel, display(Server.last()));
                    break;
                default:
                    send(channel, ":face_with_monocle: komutu yanlış/eksik girmiş olabilir misin?");
                    break;
            }
        }
        // tokens > 2
        else {
            // parse count
            int count = Parser.extractCount(tokens);

            // parse threshold
            double threshold = Parser.extractThreshold(tokens);

            // parse magnitude
            double magnitude = Parser.extractMagnitude(tokens);

            List<Earthquake> earthquakes;

            // valid magnitude
            if(magnitude > 0.0d) {
                startWatching(channel, magnitude);
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

    private void watch() {
        List<Earthquake> earthquakes = Server.lastIn(Constants.WATCH_PERIOD);
        watches.forEach(w ->
            earthquakes.stream()
                .filter(e -> e.getMagnitude() >= w.getMagnitude())
                .forEach(e -> send(w.getChannel(), display(e)))
        );
    }

    private void startWatching(TextChannel channel, double magnitude) {
        if(!isWatching(channel)) {
            if(watches.isEmpty()) {
                future = executor.scheduleAtFixedRate(this::watch, 0, Constants.WATCH_PERIOD, TimeUnit.MINUTES);
            }
            watches.add(new Watch(channel, magnitude));
        }
        send(channel, ":detective: izlemedeyim.");
    }

    private void stopWatching(TextChannel channel) {
        if(isWatching(channel)) {
            watches.removeIf(c -> c.getChannel().getGuild().getName().equals(channel.getGuild().getName()));
            if(watches.isEmpty()) {
                future.cancel(true);
            }
        }
        send(channel, ":hand_splayed: saldım.");
    }

    private void send(TextChannel channel, String message) {
        channel.sendMessage(message.substring(0, Math.min(message.length(), 200))).queue();
    }

    private String display(Earthquake earthquake) {
        return Objects.isNull(earthquake) ? ":fireworks: deprem kaydı yok." : earthquake.toString();
    }

    private boolean isWatching(TextChannel channel) {
        return watches.stream().anyMatch(w -> w.getChannel().getGuild().getName().equals(channel.getGuild().getName()));
    }

    private void showHelp(TextChannel channel) {
        send(channel, "`!deprem`: gerçekleşen son depremi gösterir.");
        send(channel, "`!deprem son <sayı>`: son <sayı> kadar depremi gösterir. *(en fazla " + Constants.MAX_COUNT + ")*");
        send(channel, "`!deprem büyük <sayı>`: <sayı> şiddetinden büyük ilk depremi gösterir.");
        send(channel, "`!deprem son <sayı1> büyük <sayı2>`: <sayı2> şiddetinden büyük, son <sayı1> kadar depremi gösterir. *(en fazla " + Constants.MAX_COUNT + ")*");
        send(channel, "`!deprem dürt <sayı>`: " + Constants.WATCH_PERIOD + " dakikada bir şiddeti <sayı>dan büyük deprem olup olmadığını kontrol eder, varsa dürter.");
        send(channel, "`!deprem dürtme`: dürtülmeyi durdurur.");
        send(channel, "`!deprem clear`: bot tarafından gönderilen mesajları temizler.");
    }

    private void clear(TextChannel channel) {
        OffsetDateTime twoWeeksAgo = OffsetDateTime.now().minus(2, ChronoUnit.WEEKS);
        final List<Message> messages = new ArrayList<>();
        for(Message m : channel.getIterableHistory()) {
            if(m.getTimeCreated().isBefore(twoWeeksAgo)) {
                break;
            }
            else if((m.getAuthor().isBot() && m.getAuthor().getName().equals(Constants.SELFNAME)) ||
                    Validator.validateCommand(m.getContentRaw())) {
                messages.add(m);
            }
        }
        // jda api restriction: deleting count should be between 2 and 100
        if(messages.size() > 1) {
            channel.deleteMessages(messages.stream().limit(Math.min(messages.size(), 100)).collect(Collectors.toList()))
                    .delay(1, TimeUnit.SECONDS)
                    .flatMap(report -> channel.sendMessage(":wastebasket: bugün de temizlendik çok şükür. (" + Math.min(messages.size(), 100) + " mesaj)"))
                    .delay(3, TimeUnit.SECONDS)
                    .flatMap(Message::delete)
                    .queue();

        }
    }

}
