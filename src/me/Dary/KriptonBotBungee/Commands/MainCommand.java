package me.Dary.KriptonBotBungee.Commands;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import me.Dary.KriptonBotBungee.KriptonBot;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageHistory;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

public class MainCommand extends Command implements TabExecutor {
	KriptonBot plugin = KriptonBot.getInstance();

	public MainCommand() {
		super("kriptonbot", "kriptonbot.*");
	}

	@Override
	public void execute(CommandSender s, String[] a) {
		if (a.length == 0) {
			s.sendMessage(plugin.utils().component("&8"+StringUtils.repeat("-", 50)));
			s.sendMessage(plugin.utils().component("&6&l| &f/kriptonbot reload &8-> &7Recarga toda la configuración y comandos."));
			s.sendMessage(plugin.utils().component("&6&l| &f/kriptonbot setupcommands &8-> &7Reinstala los comandos propios del plugin en el bot."));
			s.sendMessage(plugin.utils().component("&6&l| &f/kriptonbot purgecommands &8-> &7Elimina completamente todos los comandos instalados en el bot."));
			s.sendMessage(plugin.utils().component("&8"+StringUtils.repeat("-", 50)));
			return;
		}
		switch (a[0].toLowerCase()) {
		case "reload": {
			commandReload(s);
			return;
		}
		case "setupcommands": {
			setupCommands(s);
			return;
		}
		case "purgecommands": {
			purgeCommand(s);
			return;
		}
		case "tematica": {
			tematica(s);
			return;
		}
		
		}
		s.sendMessage(plugin.utils().component("&cComando desconocido."));
		
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////
	
	public void tematica(CommandSender s) {
		//1️⃣2️⃣3️⃣4️⃣5️⃣
		MessageHistory h = plugin.jda().getTextChannelById(1157927888994189313l).getHistoryFromBeginning(10).complete();
		for(Message msg : h.getRetrievedHistory()) {
			msg.addReaction(Emoji.fromUnicode("1️⃣")).queue(
					m -> msg.addReaction(Emoji.fromUnicode("2️⃣")).queue(
					mm -> msg.addReaction(Emoji.fromUnicode("3️⃣")).queue(
					mmm -> msg.addReaction(Emoji.fromUnicode("4️⃣")).queue(
					mmmm -> msg.addReaction(Emoji.fromUnicode("5️⃣")).queue()))));
			
		}
	
	}
	
	public void commandReload(CommandSender s) {
		s.sendMessage(plugin.utils().component("Bot recargado."));
		plugin.onDisable();
		plugin.onEnable();
	}
	
	public void setupCommands(CommandSender s) {
		s.sendMessage(plugin.utils().component("Actualizando interacciones.."));
		plugin.discord().updateSystemInteractions();
	}
	
	public void purgeCommand(CommandSender s) {
    	List<net.dv8tion.jda.api.interactions.commands.Command> commands = plugin.jda().retrieveCommands().complete();
    	if(commands.isEmpty()) {
    		s.sendMessage(new TextComponent("No hay comandos para ser eliminados."));
    		return;
    	}
    	s.sendMessage(new TextComponent("Eliminando todos los comandos registrados en "+plugin.utils().getUserTag(plugin.jda().getSelfUser())+" ...."));
    	for(net.dv8tion.jda.api.interactions.commands.Command command : plugin.jda().retrieveCommands().complete()) {
    		command.delete().queue(x -> {
    			plugin.discord().deleteCommandCache(command.getIdLong());
    			s.sendMessage(new TextComponent("Eliminado el comando: "+command.getName()+"."));
    		});        		
    	}
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////

	@Override
	public Iterable<String> onTabComplete(CommandSender s, String[] a) {
		if (a.length == 1) {
			return List.of("reload", "setupcommands", "purgecommands", "test", "tematica").stream().filter(str -> str.toLowerCase().startsWith(a[0]))
					.collect(Collectors.toList());
		}
		return Collections.emptyList();
	}

}
