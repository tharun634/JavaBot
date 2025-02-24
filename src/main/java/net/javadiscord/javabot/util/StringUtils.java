package net.javadiscord.javabot.util;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.GuildMessageChannel;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageReaction;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.utils.MarkdownSanitizer;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.concurrent.TimeUnit;

/**
 * Contains some utility methods for dealing with Strings.
 */
@Slf4j
public class StringUtils {

	private StringUtils() {
	}

	/**
	 * Builds and returns the standard MarkdownSanitizer used to sanitize code blocks.
	 *
	 * @return The built {@link MarkdownSanitizer}.
	 * @see net.javadiscord.javabot.listener.GitHubLinkListener
	 */
	public static MarkdownSanitizer standardSanitizer() {
		return new MarkdownSanitizer()
				.withStrategy(MarkdownSanitizer.SanitizationStrategy.REMOVE)
				.withIgnored(MarkdownSanitizer.ITALICS_A)
				.withIgnored(MarkdownSanitizer.ITALICS_U)
				.withIgnored(MarkdownSanitizer.BOLD)
				.withIgnored(MarkdownSanitizer.SPOILER)
				.withIgnored(MarkdownSanitizer.QUOTE)
				.withIgnored(MarkdownSanitizer.QUOTE_BLOCK)
				.withIgnored(MarkdownSanitizer.UNDERLINE)
				.withIgnored(MarkdownSanitizer.STRIKE);
	}

	/**
	 * Converts a {@link BufferedReader} to a {@link String}.
	 *
	 * @param in   The {@link BufferedReader}.
	 * @param from The {@link String}s first line.
	 * @param to   The {@link String}s last line.
	 * @return The String object.
	 * @throws IOException If an error occurs.
	 */
	public static String fromBufferedReader(BufferedReader in, int from, int to) throws IOException {
		StringBuilder output = new StringBuilder();
		String line;
		int count = 1;
		while ((line = in.readLine()) != null) {
			if (from > to || count >= from && count <= to) {
				output.append(line).append("\n");
			}
			count++;
		}
		return output.toString();
	}

	/**
	 * Builds a progress bar out of Strings.
	 *
	 * @param current The current value.
	 * @param max     The max value.
	 * @param off     The String that should be used for the "off" state
	 * @param on      The String that should be used for the "on" state
	 * @param length  The amount of repetitions.
	 * @return The formatted String.
	 */
	public static String buildProgressBar(double current, double max, String off, String on, int length) {
		if (current > max) current = max;
		double percent = current / max;
		int onLength = (int) (length * percent);
		return on.repeat(onLength) + off.repeat(length - onLength);
	}

	/**
	 * Builds a progress bar that uses block characters.
	 * <p>
	 * For example: <pre>[███████░░░░]</pre>
	 * </p>
	 *
	 * @param value  The floating-point value. This should be between 0 and 1,
	 *               inclusive. Any value outside that range will be truncated
	 *               to either 0 or 1, depending on if it's positive or negative.
	 * @param length The length of the progress bar string, including the
	 *               brackets. It must be at least 5.
	 * @return The progress bar string.
	 */
	public static String buildTextProgressBar(double value, int length) {
		if (value < 0.0) value = 0.0;
		if (value > 1.0) value = 1.0;
		if (length < 5) throw new IllegalArgumentException("Length must be at least 5.");
		StringBuilder sb = new StringBuilder(length);
		sb.append('[');
		int barElements = length - 2;
		double elementsFilled = barElements * value;
		int wholeElementsFilled = (int) Math.floor(elementsFilled);
		sb.append("█".repeat(wholeElementsFilled));
		sb.append("░".repeat(barElements - wholeElementsFilled));
		sb.append(']');
		return sb.toString();
	}

	/**
	 * Capitalizes the given word.
	 *
	 * @param word The word to capitalize.
	 * @return The capitalized word.
	 */
	public static String capitalize(String word) {
		if (word == null || word.isEmpty()) return word;
		return word.substring(0, 1).toUpperCase() + word.substring(1);
	}

	/**
	 * Tries to get the current operating system.
	 *
	 * @return The current operating system, as a {@link String}.
	 */
	public static String getOperatingSystem() {
		String os = System.getProperty("os.name");
		if (os.equals("Linux")) {
			try {
				String[] cmd = {"/bin/sh", "-c", "cat /etc/*-release"};
				Process p = Runtime.getRuntime().exec(cmd);
				try (BufferedReader bri = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
					String line = "";
					while ((line = bri.readLine()) != null) {
						if (line.startsWith("PRETTY_NAME")) {
							return line.split("\"")[1];
						}
					}
				}
			} catch (IOException e) {
				ExceptionLogger.capture(e, StringUtils.class.getSimpleName());
				log.error("Error while getting Linux Distribution.");
			}
		}
		return os;
	}

	/**
	 * Calculates the Uptimes and returns a formatted String.
	 *
	 * @return The current Uptime as a String.
	 */
	public static String formatUptime() {
		RuntimeMXBean rb = ManagementFactory.getRuntimeMXBean();
		long uptimeMS = rb.getUptime();
		long uptimeDAYS = TimeUnit.MILLISECONDS.toDays(uptimeMS);
		uptimeMS -= TimeUnit.DAYS.toMillis(uptimeDAYS);
		long uptimeHRS = TimeUnit.MILLISECONDS.toHours(uptimeMS);
		uptimeMS -= TimeUnit.HOURS.toMillis(uptimeHRS);
		long uptimeMIN = TimeUnit.MILLISECONDS.toMinutes(uptimeMS);
		uptimeMS -= TimeUnit.MINUTES.toMillis(uptimeMIN);
		long uptimeSEC = TimeUnit.MILLISECONDS.toSeconds(uptimeMS);
		return String.format("%sd %sh %smin %ss",
				uptimeDAYS, uptimeHRS, uptimeMIN, uptimeSEC);
	}

	/**
	 * Counts the amount of reactions on a single message.
	 *
	 * @param msg   The {@link Message}.
	 * @param emoji The {@link Emoji} which should be counted.
	 * @return The amount of reactions.
	 */
	public static int countReactions(@NotNull Message msg, Emoji emoji) {
		MessageReaction reaction = msg.getReaction(emoji);
		if (reaction == null) return 0;
		return (int) reaction.retrieveUsers().stream()
				.filter(user -> !user.isBot() && !user.isSystem())
				.count();
	}

	public static String buildChannelJumpUrl(@NotNull GuildMessageChannel channel) {
		return String.format("https://discord.com/channels/%s/%s", channel.getGuild().getId(), channel.getIdLong());
	}
}
