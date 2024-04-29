package me.Dary.KriptonBotBungee.Listeners;

import java.util.ArrayList;
import java.util.List;

import me.Dary.KriptonBotBungee.KriptonBot;
import me.Dary.KriptonBotBungee.Managers.DiscordManager.CommandCondition;
import me.Dary.KriptonBotBungee.Objects.NekoCommand;
import me.Dary.KriptonBotBungee.Objects.NekoEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction;

public class CustomInteractionsListener extends ListenerAdapter {

	KriptonBot plugin;

	public CustomInteractionsListener(KriptonBot plugin) {
		this.plugin = plugin;
	}
	
	public void onSlashCommandInteraction(SlashCommandInteractionEvent e) {
		User user = e.getUser();
		NekoCommand cmd = plugin.discord().customCommands.get(e.getCommandIdLong());
		if (cmd == null) return;
		if (e.getChannelType() == ChannelType.PRIVATE && !cmd.permissions.isEmpty()) {
			e.reply("> No puedes usar este comando mediante mensajes privados.").setEphemeral(true).queue();
			return;
		}
		plugin.log("Utilizó el comando personalizado: `/"+cmd.label+"`", user);
		plugin.utils().user_cache.put(user.getIdLong(), user);
		plugin.getProxy().getScheduler().runAsync(plugin, () -> {
			List<OptionMapping> options = e.getOptions();
			MessageChannelUnion channel = e.getChannel();
			CommandCondition condition = cmd.checkConditions(user, options, channel);
			NekoEmbed embed = condition == null ? cmd.getResponse() : condition.getDenyResponse();
			if (condition != null) {
				ReplyCallbackAction reply = e.reply(embed.getMessage(user, options, channel));
				reply.setEphemeral(embed.isEphemeral).queue();
				return;
			}

			String success = "";
			List<Runnable> functions = new ArrayList<Runnable>();
			for (String str : cmd.getFunctions()) {
				Object function = plugin.conditionsManager().sendFunctions(str, user, options, channel, false);
				if (function instanceof String error) {
					if (!error.isEmpty())
						success = (success.isEmpty() ? "```diff\n" : "") + success + "-" + error + "\n";
					continue;
				}
				functions.add((Runnable) function);
			}
			if (!success.isEmpty()) {
				success = success + "\n```";
				e.reply(success).setEphemeral(true).queue();
				return;
			}
			ReplyCallbackAction reply = e.reply(embed.getMessage(user, options, channel));
			reply.setEphemeral(embed.isEphemeral).queue();
			for (Runnable task : functions)
				task.run();
		});
	}

	public void onButtonInteraction(ButtonInteractionEvent e) {
		String id = e.getButton().getId();
		if (!id.startsWith("KPB:")) return;
		User user = e.getUser();
		plugin.utils().user_cache.put(user.getIdLong(), user);
		plugin.getProxy().getScheduler().runAsync(plugin, () -> {
			String response = plugin.discord().getInteractionResponse(id);
			MessageChannelUnion channel = e.getChannel();
			List<Runnable> functions = new ArrayList<Runnable>();
			if (!response.isEmpty()) {
				String success = "";
				for (String str : plugin.discord().getInteractionFunctions(id)) {
					Object function = plugin.conditionsManager().sendButtonFunctions(str, user, channel, e);
					if (function instanceof String error) {
						if (!error.isEmpty())
							success = (success.isEmpty() ? "```diff\n" : "") + success + "-" + error + "\n";
						continue;
					}
					functions.add((Runnable) function);
				}
				if (!success.isEmpty()) {
					success = success + "\n```";
					e.reply(success).setEphemeral(true).queue();
					return;
				}
			}
			NekoEmbed embed = new NekoEmbed(response);
			ReplyCallbackAction reply = e.reply(embed.getMessage(user, null, channel));
			reply.setEphemeral(embed.isEphemeral).queue();
			for (Runnable task : functions)
				task.run();
		});
	}

}
