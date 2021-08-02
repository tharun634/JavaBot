package com.javadiscord.javabot.commands.user_commands;

import com.javadiscord.javabot.commands.SlashCommandHandler;
import com.javadiscord.javabot.events.Startup;
import com.javadiscord.javabot.other.Constants;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyAction;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.concurrent.TimeUnit;

public class Uptime implements SlashCommandHandler {

    @Override
    public ReplyAction handle(SlashCommandEvent event) {
        RuntimeMXBean rb = ManagementFactory.getRuntimeMXBean();
        long uptimeMS = rb.getUptime();

        long uptimeDAYS = TimeUnit.MILLISECONDS.toDays(uptimeMS);
        uptimeMS -= TimeUnit.DAYS.toMillis(uptimeDAYS);
        long uptimeHRS = TimeUnit.MILLISECONDS.toHours(uptimeMS);
        uptimeMS -= TimeUnit.HOURS.toMillis(uptimeHRS);
        long uptimeMIN = TimeUnit.MILLISECONDS.toMinutes(uptimeMS);
        uptimeMS -= TimeUnit.MINUTES.toMillis(uptimeMIN);
        long uptimeSEC = TimeUnit.MILLISECONDS.toSeconds(uptimeMS);

        String botImage = event.getJDA().getSelfUser().getAvatarUrl();

        EmbedBuilder eb = new EmbedBuilder()
            .setColor(Constants.GRAY)
            .setAuthor(uptimeDAYS + "d " + uptimeHRS + "h " + uptimeMIN + "min " + uptimeSEC + "s", null, botImage);

        return event.replyEmbeds(eb.build());
    }
}