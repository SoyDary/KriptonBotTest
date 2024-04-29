package me.Dary.KriptonBotBungee;

import me.Dary.KriptonBotBungee.Listeners.CustomInteractionsListener;
import me.Dary.KriptonBotBungee.Listeners.EmbedCreatorListener;
import me.Dary.KriptonBotBungee.Listeners.JDAListener;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;

public class Bot {
	private JDA jda;
	KriptonBot plugin;
	JDAListener listener;
	EmbedCreatorListener embedCreatorListener;
	CustomInteractionsListener customInteractionsListener;
	String token;
	Guild kripton;
	
	public Bot(KriptonBot plugin) {
		this.plugin = plugin;
		token = plugin.dataManager().config.getString("BotToken");
	}
	
	public void registerEvents() {
		jda.addEventListener(customInteractionsListener = new CustomInteractionsListener(plugin));
		jda.addEventListener(listener = new JDAListener(plugin));
		jda.addEventListener(embedCreatorListener = new EmbedCreatorListener(plugin));
	}
	
	public void loadMembers() {
		kripton = jda.getGuildById(424615933919625216l);
		kripton.findMembers(member -> true).get();
		plugin.getLogger().info("Cargados "+jda.getUsers().size()+" usuarios");
	}

	public boolean initBot() {
	    stopBot();
	    if(token == null) {
	    	plugin.getLogger().severe("El bot no pudo ser iniciado porque falta un token en la configuración");
	    	return false;
	    }
	    try { 
	    	jda = JDABuilder.createLight(token)     
	    			.setMemberCachePolicy(MemberCachePolicy.ALL)
	    			.enableIntents(GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_MESSAGES, GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_MESSAGE_REACTIONS)
	    			.build();
	    } catch (Exception ex) {
	    	ex.printStackTrace();
	    	return false;
	    }	  
	   return true;
	}
	
	  public JDA getJda() {
		  return this.jda;
	  }
	  
	  
	  public void stopBot() {
		  if (jda != null) {
			  jda.shutdown();
		  }
		  if(customInteractionsListener != null) jda.removeEventListener(customInteractionsListener);
		  if(listener != null) jda.removeEventListener(listener);
		  if(embedCreatorListener != null) jda.removeEventListener(embedCreatorListener);
	  }  
	  public String getInviteLink() {
		  return this.jda.getInviteUrl(new Permission[] {Permission.ADMINISTRATOR, Permission.USE_APPLICATION_COMMANDS});
	  }
}