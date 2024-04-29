package me.Dary.KriptonBotBungee.Commands;

import me.Dary.KriptonBotBungee.KriptonBot;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;

public class test extends Command {

	KriptonBot plugin = KriptonBot.getInstance();
	
	public test() {
		super("test", "kriptonbot.*");
	}

	@Override
	public void execute(CommandSender s, String[] a) {
		if(a.length == 0) {
			s.sendMessage("Faltan argumentos");
			return;
		}
		String msg = String.join(" ", a);
		s.sendMessage(plugin.utils().component(msg));
	}	
}
