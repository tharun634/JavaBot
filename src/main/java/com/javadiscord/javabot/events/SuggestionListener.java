package com.javadiscord.javabot.events;

import com.javadiscord.javabot.Bot;
import com.javadiscord.javabot.other.Constants;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageType;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.Date;

public class SuggestionListener extends ListenerAdapter {

    @Override
    public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
        if (event.getAuthor().isBot() || event.getAuthor().isSystem()
                || event.getMessage().getType() == MessageType.THREAD_CREATED) return;

            if (!event.getChannel().equals(
                    Bot.config.get(event.getGuild()).getModeration().getSuggestionChannel())) return;

                var eb = new EmbedBuilder()
                        .setColor(Constants.GRAY)
                        .setImage(null)
                        .setAuthor(event.getAuthor().getAsTag() + " · Suggestion", null, event.getAuthor().getEffectiveAvatarUrl())
                        .setTimestamp(new Date().toInstant())
                        .setDescription(event.getMessage().getContentRaw())
                        .build();

                if (!event.getMessage().getAttachments().isEmpty()) {
                    Message.Attachment attachment = event.getMessage().getAttachments().get(0);
                    try {
                        event.getChannel().sendFile(attachment.retrieveInputStream().get(), "attachment." + attachment.getFileExtension()).setEmbeds(eb).queue(message -> {
                            message.addReaction(Constants.REACTION_UPVOTE).queue();
                            message.addReaction(Constants.REACTION_DOWNVOTE).queue();
                        });
                    } catch (Exception e) { event.getChannel().sendMessage(e.getMessage()).queue(); }
                } else {
                    event.getChannel().sendMessageEmbeds(eb).queue(message -> {
                        message.addReaction(Constants.REACTION_UPVOTE).queue();
                        message.addReaction(Constants.REACTION_DOWNVOTE).queue();
                    });
                }
                event.getMessage().delete().queue();
        }
    }
