package me.Dary.KriptonBotBungee.Utils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Charsets;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

import me.Dary.KriptonBotBungee.KriptonBot;
import me.Dary.KriptonBotBungee.Managers.storage.AccountsManager.LinkedAccount;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.skinsrestorer.api.SkinsRestorer;
import net.skinsrestorer.api.SkinsRestorerProvider;
import net.skinsrestorer.api.property.SkinProperty;

public class Utils {

	KriptonBot plugin;
	Pattern hex_separator_pattern = Pattern.compile("(&#|#)([A-Fa-f0-9]{6})");
	public Map<Long, User> user_cache = new HashMap<Long, User>();
	SkinsRestorer skins;

	public Utils(KriptonBot plugin) {
		this.plugin = plugin;
		if(plugin.getProxy().getPluginManager().getPlugin("SkinsRestorer") != null) {
			skins = SkinsRestorerProvider.get();
		}
	}

	public boolean isInt(String str) {
		try {
			Integer.valueOf(str);
		} catch (NumberFormatException e) {
			return false;
		}
		return true;
	}
	
    public String formatTime(long time) {
        long d = TimeUnit.MILLISECONDS.toDays(time);
        long h = TimeUnit.MILLISECONDS.toHours(time) % 24;
        long m = TimeUnit.MILLISECONDS.toMinutes(time) % 60;
        long s = TimeUnit.MILLISECONDS.toSeconds(time) % 60;

        StringBuilder result = new StringBuilder();

        if (d > 0) {
        	result.append(d).append("d ");
        }
        if (h > 0) {
        	result.append(h).append("h ");
        }
        if (m > 0) {
        	result.append(m).append("m ");
        }
        if (s > 0 || (d == 0 && h == 0 && m == 0)) {
        	result.append(s).append("s");
        }
        
        return result.toString().trim();
    }
	
	public String resetPassword(String player) {
		try {
			ServerInfo server = ProxyServer.getInstance().getServerInfo(plugin.getConfig().getString("LoginServer"));
			ByteArrayDataOutput out = ByteStreams.newDataOutput();
			String password = String.format("%0" + 5 + "d", new Random().nextInt(99999) + 1);		
			out.writeUTF("changepassword");
			out.writeUTF(player);
			out.writeUTF(password);
			server.sendData("kriptonbot:authme", out.toByteArray());
			return password;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public String getUserTag(User user) {
		return user.getAsTag().endsWith("#0000") ? user.getName() : user.getAsTag();
	}

	public LocalDateTime getFutureDate(long seconds) {
		LocalDateTime expiration = LocalDateTime.now().plusSeconds(seconds);
		return expiration;
	}

	public long getSeconds(String time) {
		long seconds = 0;
		String[] args = time.split(" ");
		try {
			for (String arg : args) {
				char x = arg.charAt(arg.length() - 1);
				int value = Integer.parseInt(arg.substring(0, arg.length() - 1));
				switch (x) {
				case 'd':
					seconds += value * 24 * 60 * 60;
					break;
				case 'h':
					seconds += value * 60 * 60;
					break;
				case 'm':
					seconds += value * 60;
					break;
				case 's':
					seconds += value;
					break;
				default:
					continue;
				}
			}
		} catch (Exception e) {
			return -1;
		}
		return seconds;
	}
	
	
	public String getSkinUrl(User user, boolean id) {
		LinkedAccount player = plugin.accountsManager().getAccount(user.getIdLong());
		if(player == null) return null;
		if(skins == null) return player.name();
		SkinProperty data = skins.getPlayerStorage().getSkinOfPlayer(player.uuid()).orElse(null);
		if(data == null) return player.name();
        try {
			JsonNode json = new ObjectMapper().readTree(Base64.getDecoder().decode(data.getValue()));
			String url = json.get("textures").get("SKIN").get("url").asText();
			return id ? url.split("texture/")[1] : url;
		} catch (Exception  e) {
			return player.name();
		}
	}
	
	public String getSkinUrl(String name, boolean id) {
		if(skins == null) return name;
		UUID uuid = UUID.nameUUIDFromBytes(("OfflinePlayer:"+name).getBytes(Charsets.UTF_8));	
		SkinProperty data = skins.getPlayerStorage().getSkinOfPlayer(uuid).orElse(null);
		if(data == null) return name;
        try {
			JsonNode json = new ObjectMapper().readTree(Base64.getDecoder().decode(data.getValue()));
			String url = json.get("textures").get("SKIN").get("url").asText();
			return id ? url.split("texture/")[1] : url;
		} catch (Exception  e) {
			return name;
		}
	}

	public User findUser(String id) {
		Member member = null;
		try {
			if (id.matches("\\d{18}"))
				return plugin.jda().retrieveUserById(id).complete();

			Guild kape = plugin.jda().getGuildById(424615933919625216l);
			boolean tag = id.substring(id.length() - 5).matches("#\\d{4}");
			if (tag) {
				member = kape.findMembers(mem -> {
					if (mem.getUser().getAsTag().toLowerCase().replaceAll(" ", "").equals(id))
						return true;
					return false;
				}).get().get(0);
			} else {
				member = kape.findMembers(mem -> {
					if (mem.getUser().getName().equals(id) && mem.getUser().getAsTag().endsWith("#0000"))
						return true;
					return false;
				}).get().get(0);
			}
		} catch (Exception e) {
		}

		return member != null ? member.getUser() : null;
	}
	
	public PreparedEmbed getEmbedTag(String str) {
		Pattern pattern = Pattern.compile("^<embed:(.*)>$");
		Matcher matcher = pattern.matcher(str);
		if (matcher.find()) {
			String key = matcher.group(1);
			String[] parts = key.split("\\(");
			key = parts[0];
			String args_key = "";
			if (parts.length > 1) {
				args_key = parts[1].replaceAll("\\)", "");
			}
			if (plugin.dataManager().embeds.getSection(key).getKeys().isEmpty())
				return null;
			String[] array = args_key.isEmpty() ? new String[0] : args_key.split(";");
			List<String> args = Arrays.asList(array);
			return new PreparedEmbed(key, args);
		}
		return null;
	}

	public TextComponent component(String text) {
		TextComponent base = new TextComponent();
		
		String color = null;
		for(String part : hexSeparator(text)) {
			if(isHex(part)) {
				color = part;
			}else {
				TextComponent component = new TextComponent(color(part));
				if(color != null)
					component.setColor(net.md_5.bungee.api.ChatColor.of(color.startsWith("&") ? color.substring(1, color.length()) : color));
				base.addExtra(component);
			}
		}
		return base;
	}
	
	public String color(String text) {
		return ChatColor.translateAlternateColorCodes('&', colorHex(text));
	}
	
	private String colorHex(String text) {
		String message = "";
		
		Matcher matcher = hex_separator_pattern.matcher(text);
		
		int index = 0;
		while(matcher.find()) {
			message += text.substring(index, matcher.start()) + net.md_5.bungee.api.ChatColor.of((matcher.group().startsWith("&") ? matcher.group().substring(1, matcher.group().length()) : matcher.group()));
			index = matcher.end();
		}
		return message += text.substring(index, text.length());
	}
	
	private List<String> hexSeparator(String text) {
		List<String> texts = new ArrayList<String>();
		
		 Matcher matcher = hex_separator_pattern.matcher(text);
		 
		 int index = 0;
		 
		 while(matcher.find()) {
			 texts.add(text.substring(index, matcher.start()));
			 texts.add(matcher.group().startsWith("&") ? matcher.group() : "&"+matcher.group());
			 index = matcher.end();
		 }
		 
		 texts.add(text.substring(index, text.length()));
		 return texts;
	}
	
	private Boolean isHex(String text) {
		if(text == null)
			return false;
		
		if(text.startsWith("&"))
			text = text.substring(1, text.length());
		
		 Matcher matcher = hex_separator_pattern.matcher(text);
	 
	     return matcher.matches();
	}
	public class PreparedEmbed {

		String key;
		List<String> args = new ArrayList<String>();

		public PreparedEmbed(String key, List<String> args) {
			this.key = key;
			this.args = args;
		}

		public String getKey() {
			return this.key;
		}

		public List<String> getArgs() {
			return args;
		}
	}

}
