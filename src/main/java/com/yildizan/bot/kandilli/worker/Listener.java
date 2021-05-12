package com.yildizan.bot.kandilli.worker;

import com.yildizan.bot.kandilli.model.Earthquake;
import com.yildizan.bot.kandilli.utility.Constants;
import com.yildizan.bot.kandilli.utility.Parser;
import com.yildizan.bot.kandilli.utility.Validator;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import javax.annotation.Nonnull;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class Listener extends ListenerAdapter {

    private final Server server = new Server();
    private final Watcher watcher = new Watcher();

    public static void main(String[] args) throws Exception {
        JDABuilder.createDefault(Constants.TOKEN)
            .addEventListeners(new Listener())
            .setActivity(Activity.listening("the underground"))
            .build();
    }

    @Override
    public void onReady(@Nonnull ReadyEvent event) {
        super.onReady(event);
        System.out.println(Constants.SELFNAME + " is online for " + event.getGuildTotalCount() + " servers!");
    }

    @Override
    public void onMessageReceived(@Nonnull MessageReceivedEvent event) {
        super.onMessageReceived(event);
        if(event.getAuthor().isBot() || !event.isFromType(ChannelType.TEXT) || !Validator.validateCommand(event.getMessage().getContentRaw())) {
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
            send(channel, server.last());
        }
        else if(tokens.size() == 2) {
            switch (tokens.get(1)) {
                case "help":
                    showHelp(channel);
                    break;
                case "clear":
                    clear(channel);
                    break;
                case "dürtme":
                    watcher.stop(channel);
                    break;
                case "son":
                    send(channel, server.last());
                    break;
                default:
                    send(channel, ":face_with_monocle: komutu yanlış/eksik girmiş olabilir misin?");
                    break;
            }
        }
        else {
            // parse count
            int count = Parser.extractCount(tokens);

            // parse threshold
            double threshold = Parser.extractThreshold(tokens);

            // parse magnitude
            double magnitude = Parser.extractMagnitude(tokens);

            List<Earthquake> earthquakes = null;

            // valid magnitude
            if(magnitude > 0.0d) {
                watcher.start(channel, magnitude);
            }
            // valid count && valid threshold
            else if(count > 0 && threshold > 0.0d) {
                earthquakes = server.lastGreater(threshold, count);
            }
            // valid count && invalid threshold
            else if(count > 0) {
                earthquakes = server.last(count);
            }
            // invalid count && valid threshold
            else if(threshold > 0.0d) {
                earthquakes = server.lastGreater(threshold, 1);
            }
            // invalid count && invalid threshold
            else {
                send(channel, ":pensive: geçersiz parametre girdin, niye öyle oldu?");
            }

            if(earthquakes != null) {
                send(channel, earthquakes);
            }
        }
    }

    public static void send(TextChannel channel, Earthquake earthquake) {
        send(channel, earthquake == null ? ":fireworks: deprem kaydı yok." : earthquake.toString());
    }

    public static void send(TextChannel channel, List<Earthquake> earthquakes) {
        if(earthquakes.isEmpty()) {
            send(channel, ":fireworks: deprem kaydı yok.");
        }
        else {
            StringBuilder message = new StringBuilder("```");
            for(Earthquake earthquake : earthquakes) {
                message.append(earthquake.toString(true)).append("\n\n");
            }
            send(channel, message.append("```").toString());
        }
    }

    public static void send(TextChannel channel, String message) {
        channel.sendMessage(message).queue();
    }

    private void showHelp(TextChannel channel) {
        String commands = "`!deprem`: gerçekleşen son depremi gösterir.\n" +
                "`!deprem son <sayı>`: son <sayı> kadar depremi gösterir. *(en fazla " + Constants.MAX_COUNT + ")*\n" +
                "`!deprem büyük <sayı>`: <sayı> şiddetinden büyük ilk depremi gösterir.\n" +
                "`!deprem son <sayı1> büyük <sayı2>`: <sayı2> şiddetinden büyük, son <sayı1> kadar depremi gösterir. *(en fazla " + Constants.MAX_COUNT + ")*\n" +
                "`!deprem dürt <sayı>`: " + Constants.WATCH_PERIOD + " dakikada bir şiddeti <sayı>dan büyük deprem olup olmadığını kontrol eder, varsa dürter.\n" +
                "`!deprem dürtme`: dürtülmeyi durdurur.\n" +
                "`!deprem clear`: bot tarafından gönderilen mesajları temizler.";
        send(channel, commands);
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
