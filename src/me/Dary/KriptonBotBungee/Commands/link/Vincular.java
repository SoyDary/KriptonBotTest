package me.Dary.KriptonBotBungee.Commands.link;

import me.Dary.KriptonBotBungee.KriptonBot;
import me.Dary.KriptonBotBungee.Objects.NekoEmbed;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.requests.ErrorResponse;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class Vincular extends Command {

	KriptonBot plugin = KriptonBot.getInstance();

	public Vincular() {
		super("vincular", "kriptonbot.link");
	}
	
	@Override
	public void execute(CommandSender s, String[] a) {
		if(!(s instanceof ProxiedPlayer p)) return;	
		Long discord = plugin.accountsManager().getAccount(p.getName());
		if(discord != -1) {
			User user = plugin.jda().retrieveUserById(discord).complete();		
			s.sendMessage(plugin.utils().component(plugin.placeholderManager().setPlaceholders(plugin.getConfig().getString("Linking.Messages.MinecraftMessages.AlreadyLinked"), user, null, null, false)));
			return;
		}
		if (a.length == 0) {
			s.sendMessage(plugin.utils().component(plugin.getConfig().getString("Linking.Messages.MinecraftMessages.Usage")));
			return;
		}
		User user = plugin.utils().findUser(String.join(" ", a).toLowerCase());
		if (user == null) {
			s.sendMessage(plugin.utils().component(plugin.getConfig().getString("Linking.Messages.MinecraftMessages.Unknown_user")));
			return;
		}

		String code = plugin.accountsManager().createLinkRequest(user.getIdLong(), p.getName(), p.getUniqueId());

		NekoEmbed embed = new NekoEmbed(plugin.getConfig().getString("Linking.Messages.DiscordMessages.Link_request").replaceAll("(?i)\\{code\\}", code).replaceAll("(?i)\\{player_name\\}", p.getName()),
				Button.primary("LINK_REQUEST_CONFIRM:" + code, "Verificar").withEmoji(Emoji.fromFormatted("✅")),
				Button.danger("LINK_REQUEST_DENY:" + code, Emoji.fromFormatted("🗑️")));
		
		user.openPrivateChannel().queue((channel) -> {
			channel.sendMessage(embed.getMessage(user, null, channel))
			.queue(rm -> s.sendMessage(plugin.utils().component(plugin.placeholderManager().setPlaceholders(plugin.getConfig().getString("Linking.Messages.MinecraftMessages.Sucess_message"), user, null, channel, false))),
					new ErrorHandler().ignore(ErrorResponse.UNKNOWN_MESSAGE).handle(ErrorResponse.CANNOT_SEND_TO_USER, (e) -> s.sendMessage(plugin.utils().component(plugin.getConfig().getString("Linking.Messages.MinecraftMessages.Error_message").replaceAll("(?i)\\{code\\}", code).replaceAll("(?i)\\{player_name\\}", p.getName())))));
		});
	}
}
