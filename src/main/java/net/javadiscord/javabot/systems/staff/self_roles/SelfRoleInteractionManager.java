package net.javadiscord.javabot.systems.staff.self_roles;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.text.Modal;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.javadiscord.javabot.Bot;
import net.javadiscord.javabot.data.config.GuildConfig;
import net.javadiscord.javabot.data.config.guild.ModerationConfig;

/**
 * Handles all Interactions related to the Self Role System.
 */
@Slf4j
public class SelfRoleInteractionManager {

	/**
	 * Handles all Button Interactions regarding the Self Role System.
	 *
	 * @param event The {@link ButtonInteractionEvent} that is fired upon clicking a button.
	 * @param args  The button's id, split by ":".
	 */
	public void handleButton(ButtonInteractionEvent event, String[] args) {
		Role role = event.getGuild().getRoleById(args[2]);
		if (role == null) return;
		boolean permanent = Boolean.parseBoolean(args[3]);
		switch (args[1]) {
			case "default" -> this.handleSelfRole(event, role, permanent);
			case "staff" -> this.handleStaffApplication(event, role, event.getUser());
			case "expert" -> this.handleExpertApplication(event, event.getUser());
		}
	}

	/**
	 * Handles all Modal Submit Interactions regarding the Self Role System.
	 *
	 * @param event The {@link ModalInteractionEvent} that is fired upon submitting a Modal.
	 * @param args  The modal's id, split by ":".
	 */
	public void handleModalSubmit(ModalInteractionEvent event, String[] args) {
		event.deferReply(true).queue();
		var config = Bot.config.get(event.getGuild());
		switch (args[1]) {
			case "staff" -> this.sendStaffSubmission(event, config, args[2], args[3]);
			case "expert" -> this.sendExpertSubmission(event, config.getModeration(), args[2]);
		}
		event.getHook().sendMessage("Your Submission has been sent to our Moderators! Please note that spamming submissions may result in a ban.")
				.queue();
	}

	/**
	 * Builds and replies with a Staff Application Modal.
	 *
	 * @param event     The {@link ButtonInteractionEvent} that is fired upon clicking a button.
	 * @param role      The corresponding {@link Role}.
	 * @param applicant The Applicant.
	 */
	private void handleStaffApplication(ButtonInteractionEvent event, Role role, User applicant) {
		TextInput name = TextInput.create("name", "Real Name", TextInputStyle.SHORT)
				.setRequired(true)
				.setPlaceholder("John Doe")
				.build();
		TextInput age = TextInput.create("age", "Age", TextInputStyle.SHORT)
				.setRequired(true)
				.setPlaceholder("24")
				.build();
		TextInput email = TextInput.create("email", "Email", TextInputStyle.SHORT)
				.setRequired(true)
				.setPlaceholder("moontm@javadiscord.net")
				.build();
		TextInput timezone = TextInput.create("timezone", "Timezone", TextInputStyle.SHORT)
				.setRequired(true)
				.build();
		TextInput extraRemarks = TextInput.create("extra-remarks", "Anything else?", TextInputStyle.PARAGRAPH)
				.build();
		Modal modal = Modal.create(String.format("self-role:staff:%s:%s", role.getId(), applicant.getId()), "Apply for @" + role.getName())
				.addActionRows(ActionRow.of(name), ActionRow.of(age), ActionRow.of(email), ActionRow.of(timezone), ActionRow.of(extraRemarks))
				.build();
		event.replyModal(modal).queue();
	}

	/**
	 * Builds and replies with an Expert Application Modal.
	 *
	 * @param event     The {@link ButtonInteractionEvent} that is fired upon clicking a button.
	 * @param applicant The Applicant.
	 */
	private void handleExpertApplication(ButtonInteractionEvent event, User applicant) {
		TextInput experience = TextInput.create("java-experience", "Java Experience", TextInputStyle.PARAGRAPH)
				.setPlaceholder("How much experience do you have with the Java Programming Language?")
				.setRequired(true)
				.build();
		TextInput projectInfo = TextInput.create("project-info", "Project Info", TextInputStyle.PARAGRAPH)
				.setPlaceholder("Choose a fitting Java Project you've done yourself and present it to us.")
				.setRequired(true)
				.build();
		TextInput projectLinks = TextInput.create("project-links", "Link to your Project", TextInputStyle.SHORT)
				.build();
		TextInput reason = TextInput.create("reason", "Why should we accept this submission?", TextInputStyle.PARAGRAPH)
				.setRequired(true)
				.build();
		Modal modal = Modal.create(String.format("self-role:expert:%s", applicant.getId()), "Apply for Expert")
				.addActionRows(ActionRow.of(experience), ActionRow.of(projectInfo), ActionRow.of(projectLinks), ActionRow.of(reason))
				.build();
		event.replyModal(modal).queue();
	}

	/**
	 * Handles a single Self Role Interaction.
	 *
	 * @param event     The {@link ButtonInteractionEvent} that is fired upon clicking a button.
	 * @param role      The Role that should be assigned/removed
	 * @param permanent Whether the user is able to remove the role again.
	 */
	private void handleSelfRole(ButtonInteractionEvent event, Role role, boolean permanent) {
		event.deferEdit().queue();
		event.getGuild().retrieveMemberById(event.getUser().getId()).queue(member -> {
			if (member.getRoles().contains(role)) {
				if (!permanent) {
					event.getGuild().removeRoleFromMember(member, role).queue();
					event.getHook().sendMessage("Removed Role: " + role.getAsMention()).setEphemeral(true).queue();
				} else {
					event.getHook().sendMessage("You already have Role: " + role.getAsMention()).setEphemeral(true).queue();
				}
			} else {
				event.getGuild().addRoleToMember(member, role).queue();
				event.getHook().sendMessage("Added Role: " + role.getAsMention()).setEphemeral(true).queue();
			}
		}, e -> log.error("Could not retrieve Member with Id: " + event.getUser().getIdLong()));
	}

	/**
	 * Sends the Staff Applications contents to the {@link ModerationConfig#getApplicationChannel()}.
	 *
	 * @param event  The {@link ModalInteractionEvent} that is fired upon submitting a Modal.
	 * @param config The {@link GuildConfig} for the current Guild.
	 * @param roleId The role's id that was applied for.
	 * @param userId The applicant's id.
	 */
	private void sendStaffSubmission(ModalInteractionEvent event, GuildConfig config, String roleId, String userId) {
		var nameOption = event.getValue("name");
		var ageOption = event.getValue("age");
		var emailOption = event.getValue("email");
		var timezoneOption = event.getValue("timezone");
		var extraRemarksOption = event.getValue("extra-remarks");
		if (nameOption == null || ageOption == null || emailOption == null || timezoneOption == null) {
			return;
		}
		event.getGuild().retrieveMemberById(userId).queue(
				member -> {
					User user = member.getUser();
					Role role = event.getGuild().getRoleById(roleId);
					MessageEmbed embed = new EmbedBuilder()
							.setAuthor(user.getAsTag(), null, user.getEffectiveAvatarUrl())
							.setTitle(String.format("%s applied for @%s", user.getAsTag(), role.getName()))
							.setColor(config.getSlashCommand().getSuccessColor())
							.addField("Real Name", nameOption.getAsString(), false)
							.addField("Age", ageOption.getAsString(), true)
							.addField("Email", String.format("`%s`", emailOption.getAsString()), true)
							.addField("Timezone", String.format("`%s`", timezoneOption.getAsString()), true)
							.addField("Server joined", String.format("<t:%s:R>", member.getTimeJoined().toEpochSecond()), true)
							.addField("Account created", String.format("<t:%s:R>", member.getUser().getTimeCreated().toEpochSecond()), true)
							.addField("Extra Remarks", extraRemarksOption.getAsString(), false)
							.build();
					config.getModeration().getApplicationChannel().sendMessageEmbeds(embed).queue();
				}
		);
	}

	/**
	 * Sends the Expert Applications contents to the {@link ModerationConfig#getApplicationChannel()}.
	 *
	 * @param event  The {@link ModalInteractionEvent} that is fired upon submitting a Modal.
	 * @param config The {@link ModerationConfig} for the current Guild.
	 * @param userId The applicant's id.
	 */
	private void sendExpertSubmission(ModalInteractionEvent event, ModerationConfig config, String userId) {
		var experienceOption = event.getValue("java-experience");
		var projectInfoOption = event.getValue("project-info");
		var projectLinksOption = event.getValue("project-links");
		var reasonOption = event.getValue("reason");
		event.getGuild().retrieveMemberById(userId).queue(
				member -> {
					User user = member.getUser();
					EmbedBuilder embed = new EmbedBuilder()
							.setAuthor(user.getAsTag(), null, user.getEffectiveAvatarUrl())
							.setTitle(String.format("%s applied for Expert", user.getAsTag()))
							.setColor(config.getExpertRole().getColor())
							.addField("Java Experience", experienceOption.getAsString(), false)
							.addField("Project Info", projectInfoOption.getAsString(), false)
							.addField("Project Links", projectLinksOption.getAsString(), false)
							.addField("Why should we accept this submission", reasonOption.getAsString(), false);
					config.getApplicationChannel().sendMessageEmbeds(embed.build()).queue();
				}
		);
	}
}
