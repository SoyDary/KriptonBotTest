package me.Dary.KriptonBotBungee.Commands.link;

import me.Dary.KriptonBotBungee.KriptonBot;
import net.dv8tion.jda.api.entities.User;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class Desvincular extends Command {

	KriptonBot plugin = KriptonBot.getInstance();

	public Desvincular() {
		super("desvincular", "kriptonbot.unlink");
	}

	@Override
	public void execute(CommandSender s, String[] a) {
		if (!(s instanceof ProxiedPlayer p))
			return;
		Long discord = plugin.accountsManager().getAccount(p.getName());
		if (discord == -1) {
			p.sendMessage(plugin.utils().component(plugin.getConfig().getString("Linking.Messages.MinecraftMessages.NoAccount")));
			return;
		}
		User user = plugin.jda().retrieveUserById(discord).complete();
		p.sendMessage(plugin.utils().component(plugin.placeholderManager().setPlaceholders(plugin.getConfig().getString("Linking.Messages.MinecraftMessages.UnlinkedAccount"), user, null, null, false)));
		plugin.accountsManager().unlink(discord, true);
	}

}
