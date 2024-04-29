package me.Dary.KriptonBotBungee.Listeners;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;

import me.Dary.KriptonBotBungee.KriptonBot;
import me.Dary.KriptonBotBungee.Objects.ProxyMessageBroker;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class ProxyMessageListener implements Listener {

	private final ProxyMessageBroker broker;
	private KriptonBot plugin;

	public ProxyMessageListener(ProxyMessageBroker broker, KriptonBot plugin) {
		this.broker = broker;
		this.plugin = plugin;
	}

	@EventHandler
	public void onPluginMessage(PluginMessageEvent e) {
		switch(e.getTag()) {
			case "kriptonbot:functions" : {	
				functionsChannel(e.getData());			
				return;			
			}	
			case "kriptonbot:authme" : {
				authmeChannel(e.getData());
				return;
				
			}	
		}
	}
	
	public void functionsChannel(byte[] data) {
		ByteArrayDataInput in = ByteStreams.newDataInput(data);	
		String d = in.readUTF();
		Object function = plugin.conditionsManager().sendFunctions(d);
		if(!(function instanceof Runnable task)) return;
			task.run();
	}
	
	public void authmeChannel(byte[] data) {
		ByteArrayDataInput in = ByteStreams.newDataInput(data);
		long id = in.readLong();
		String result = in.readUTF();
		broker.consume("kriptonbot:authme", id , result);
	}
}