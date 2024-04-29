package me.Dary.KriptonBotBungee.Managers;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import me.Dary.KriptonBotBungee.KriptonBot;
import me.Dary.KriptonBotBungee.Managers.storage.AccountsManager.LinkedAccount;
import me.Dary.KriptonBotBungee.Utils.JSParser;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.Message.Attachment;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.entities.channel.unions.GuildChannelUnion;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

public class PlaceholderManager {

	KriptonBot plugin;

	public PlaceholderManager(KriptonBot plugin) {
		this.plugin = plugin;
	}

	public String setPlaceholders(String str, User user, List<OptionMapping> options, MessageChannel channel, boolean forcedPlaceholders) {
		if (str == null || str.isEmpty())
			return null;
		String c = forcedPlaceholders ? "!" : "(?<!!)";	
		if(user != null) {
			LinkedAccount account = plugin.accountsManager().getAccount(user.getIdLong());
			str = str.replaceAll("(?i)" + c + "\\{user_id\\}", user.getId())	
					.replaceAll("(?i)" + c + "\\{user_avatar\\}", user.getAvatarUrl() != null ? user.getAvatarUrl() : user.getDefaultAvatarUrl())
					.replaceAll("(?i)" + c + "\\{user_tag\\}", plugin.utils().getUserTag(user))
					.replaceAll("(?i)" + c + "\\{user_name\\}", user.getName())
					.replaceAll("(?i)" + c + "\\{player_name\\}", account != null ? account.name() : "null")
					.replaceAll("(?i)" + c + "\\{skin\\}", ""+plugin.utils().getSkinUrl(user, true));
		}
		if(channel != null) {
			str = str.replaceAll("(?i)" + c + "\\{channel_name\\}", channel.getName())
					.replaceAll("(?i)" + c + "\\{channel_id\\}", channel.getId())
					.replaceAll("(?i)" + c + "\\{channel_type\\}", channel.getType().name());
		}

		str = parseJavaScripts(str, user, options, channel);
		if (!forcedPlaceholders && options != null && !options.isEmpty()
				&& StringUtils.substringBetween(str, "{option_", "}") != null) {
			str = parseOptions(str, options);
			str = str.replaceAll("\\{option_[^\\s}]*}", "null");
		}
		str = parseSkinPlaceholders(str);
		return str;

	}

	String parseOptions(String str, List<OptionMapping> options) {
		for (OptionMapping option : options) {
			switch (option.getType()) {
			case STRING:
				str = parseString(str, option);
				break;
			case USER:
				str = parseUser(str, option);
				break;
			case ROLE:
				str = parseRole(str, option);
				break;
			case ATTACHMENT:
				str = parseAttachment(str, option);
				break;
			case BOOLEAN:
				str = parseBoolean(str, option);
				break;
			case CHANNEL:
				str = parseChannel(str, option);
				break;
			case INTEGER:
				str = parseInteger(str, option);
				break;
			case NUMBER:
				str = parseNumber(str, option);
				break;
			default:
				break;
			}
		}
		return str;
	}

	private String parseAttachment(String str, OptionMapping option) {
		Attachment object = option.getAsAttachment();
		return str.replaceAll("(?i)\\{option_" + Pattern.quote(option.getName()) + ":URL\\}", object.getUrl())
				.replaceAll("(?i)\\{option_" + Pattern.quote(option.getName()) + ":NAME\\}", object.getFileName())
				.replaceAll("(?i)\\{option_" + Pattern.quote(option.getName()) + ":ISIMAGE\\}", object.isImage() + "");
	}

	private String parseSkinPlaceholders(String str) {
		Matcher matcher = Pattern.compile("\\{skin_(.*?)\\}", Pattern.CASE_INSENSITIVE).matcher(str);
		StringBuffer sb = new StringBuffer();
		while (matcher.find()) {
			String name = matcher.group(1);
			String skin = plugin.utils().getSkinUrl(name, true);
			if (skin != null) {
				matcher.appendReplacement(sb, skin);
			}
		}
		matcher.appendTail(sb);
		return sb.toString();
	}

	private String parseJavaScripts(String str, User user, List<OptionMapping> options, MessageChannel channel) {
		Matcher matcher = Pattern.compile("\\{javascript_(.*?)\\}", Pattern.CASE_INSENSITIVE).matcher(str);
		StringBuffer sb = new StringBuffer();
		while (matcher.find()) {
			String var = matcher.group(1);
			String replace = JSParser.parseJS(var, user, options, channel);
			if (replace != null)
				matcher.appendReplacement(sb, replace);
		}
		matcher.appendTail(sb);
		return sb.toString();
	}

	private String parseBoolean(String str, OptionMapping option) {
		boolean object = option.getAsBoolean();
		return str.replaceAll("(?i)\\{option_" + Pattern.quote(option.getName()) + ":VALUE\\}", object + "");
	}

	private String parseChannel(String str, OptionMapping option) {
		GuildChannelUnion object = option.getAsChannel();
		return str.replaceAll("(?i)\\{option_" + Pattern.quote(option.getName()) + ":ID\\}", object.getId())
				.replaceAll("(?i)\\{option_" + Pattern.quote(option.getName()) + ":NAME\\}", object.getName())
				.replaceAll("(?i)\\{option_" + Pattern.quote(option.getName()) + ":TYPE\\}", object.getType().name());
	}

	private String parseNumber(String str, OptionMapping option) {
		double object = option.getAsDouble();
		return str.replaceAll("(?i)\\{option_" + Pattern.quote(option.getName()) + ":VALUE\\}", object + "");
	}

	private String parseInteger(String str, OptionMapping option) {
		int object = option.getAsInt();
		return str.replaceAll("(?i)\\{option_" + Pattern.quote(option.getName()) + ":VALUE\\}", object + "");
	}

	private String parseRole(String str, OptionMapping option) {
		Role object = option.getAsRole();
		return str.replaceAll("(?i)\\{option_" + Pattern.quote(option.getName()) + ":ID\\}", object.getId())
				.replaceAll("(?i)\\{option_" + Pattern.quote(option.getName()) + ":NAME\\}", object.getName())
				.replaceAll("(?i)\\{option_" + Pattern.quote(option.getName()) + ":COLOR\\}",
						object.getColor() != null ? String.format("#%06x", object.getColor().getRGB() & 0xFFFFFF)
								: null);
	}

	private String parseString(String str, OptionMapping option) {
		String object = option.getAsString();
		return str.replaceAll("(?i)\\{option_" + Pattern.quote(option.getName()) + ":VALUE\\}", object).replaceAll(
				"(?i)\\{option_" + Pattern.quote(option.getName()) + ":ISTIMEFORMAT\\}",
				(plugin.utils().getSeconds(object) > 0) + "");
	}

	private String parseUser(String str, OptionMapping option) {
		User object = option.getAsUser();
		LinkedAccount account = plugin.accountsManager().getAccount(object.getIdLong());
		return str.replaceAll("(?i)\\{option_" + Pattern.quote(option.getName()) + ":ID\\}", object.getId())
				.replaceAll("(?i)\\{option_" + Pattern.quote(option.getName()) + ":AVATAR\\}", object.getAvatarUrl() != null ? object.getAvatarUrl() : object.getDefaultAvatarUrl())
				.replaceAll("(?i)\\{option_" + Pattern.quote(option.getName()) + ":TAG\\}",plugin.utils().getUserTag(object))
				.replaceAll("(?i)\\{option_" + Pattern.quote(option.getName()) + ":NAME\\}", object.getName())
				.replaceAll("(?i)\\{option_" + Pattern.quote(option.getName()) + ":PLAYERNAME\\}", account != null ? account.name() : "null")
				.replaceAll("(?i)\\{option_" + Pattern.quote(option.getName()) + ":SKIN\\}", ""+plugin.utils().getSkinUrl(object, true));
		
	}

}
