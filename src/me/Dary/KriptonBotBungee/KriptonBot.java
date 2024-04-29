package me.Dary.KriptonBotBungee;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;

import javax.swing.JOptionPane;

import lombok.Getter;
import me.Dary.KriptonBotBungee.Commands.MainCommand;
import me.Dary.KriptonBotBungee.Commands.test;
import me.Dary.KriptonBotBungee.Commands.link.Desvincular;
import me.Dary.KriptonBotBungee.Commands.link.Vincular;
import me.Dary.KriptonBotBungee.Listeners.ProxyMessageListener;
import me.Dary.KriptonBotBungee.Managers.ConditionManager;
import me.Dary.KriptonBotBungee.Managers.DiscordManager;
import me.Dary.KriptonBotBungee.Managers.PlaceholderManager;
import me.Dary.KriptonBotBungee.Managers.RoleManager;
import me.Dary.KriptonBotBungee.Managers.storage.AccountsManager;
import me.Dary.KriptonBotBungee.Managers.storage.DataManager;
import me.Dary.KriptonBotBungee.Managers.storage.LevelsManager;
import me.Dary.KriptonBotBungee.Managers.storage.SQLConnection;
import me.Dary.KriptonBotBungee.Objects.ProxyMessageBroker;
import me.Dary.KriptonBotBungee.Utils.Utils;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;

public class KriptonBot extends Plugin {
	
	private static KriptonBot instance;
	long startTime = System.currentTimeMillis();
	@Getter
    private DataManager dataManager;
    @Getter
    private Bot bot;
    @Getter
    private JDA jda;
    @Getter
    private Utils utils;
    @Getter
    private DiscordManager discord;
    @Getter
    private ConditionManager conditionsManager;
    @Getter
    private PlaceholderManager placeholderManager;
    @Getter
    private RoleManager roleManager;
    @Getter
    private AccountsManager accountsManager;
    @Getter
    private LevelsManager levelsManager;
    @Getter
    private ProxyMessageBroker proxyMessageBroker;
    private TextChannel log_channel;
	
	public void onEnable() {
		instance = this;
		this.dataManager = new DataManager(this);
		if(dataManager.connection == null) {
			onDisable();
			return;
		}
		this.utils = new Utils(this);
		this.loadBot();	
		this.accountsManager = new AccountsManager(this);
		this.discord = new DiscordManager(this);
		this.levelsManager = new LevelsManager(this);
		this.discord.load();	
		this.conditionsManager = new ConditionManager(this);
		this.placeholderManager = new PlaceholderManager(this);
		this.roleManager = new RoleManager(this);
		this.proxyMessageBroker = new ProxyMessageBroker();
		getProxy().getPluginManager().registerListener(this, new ProxyMessageListener(proxyMessageBroker, this));
		if(getConfig().getBoolean("LogChannel.enabled")) this.log_channel = jda().getTextChannelById(getConfig().getLong("LogChannel.id"));	
		getProxy().registerChannel("kriptonbot:authme");
		getProxy().registerChannel("kriptonbot:functions");
		getProxy().getPluginManager().registerCommand(this, new Vincular());
		getProxy().getPluginManager().registerCommand(this, new Desvincular());
		getProxy().getPluginManager().registerCommand(this, new test());
		getProxy().getPluginManager().registerCommand(this, new MainCommand());
		
		getLogger().info("§aPlugin activado!");	
	}

	public void onDisable() {	
		try {
			if(bot != null) bot.stopBot();	
			if(accountsManager != null) accountsManager.clearCache();
			if(levelsManager != null) levelsManager.saveData();
			if(dataManager.connection != null) dataManager.sqlconnection.disconnect();
			getProxy().getScheduler().cancel(this);
		} catch(Exception e) {}
		getLogger().info("§cPlugin desactivado!");	
	}
	
	void loadBot() {
		long start = System.currentTimeMillis();
		bot = new Bot(this);
		if(bot.initBot()) {
			jda = bot.getJda();
			try {
				jda.awaitReady();
				getLogger().info("["+utils().getUserTag(jda.getSelfUser())+"] JDA iniciada en "+(System.currentTimeMillis() - start)+"ms");				
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			bot.registerEvents();
			bot.loadMembers();
		} 
	}
	
	public static KriptonBot getInstance() {
		return instance;
	}
	
	public SQLConnection getSQL() {
		return dataManager.sqlconnection;
	}
	
	public Configuration getConfig() {
		return dataManager.getConfig();
	}
	
	public ProxyMessageBroker getProxyMessageBroker() {
		return proxyMessageBroker;
	}
	
	public void saveResource(String filePath) {
	    InputStream inputStream = getResourceAsStream(filePath);
	    File outputFile = new File(getDataFolder(), filePath);
	    try {
	        File parentDirectory = outputFile.getParentFile();
	        if(parentDirectory != null && !parentDirectory.exists()) parentDirectory.mkdirs();
	    
	        try (OutputStream outputStream = new FileOutputStream(outputFile)) {
	            byte[] buffer = new byte[4096];
	            int bytesRead;
	            while((bytesRead = inputStream.read(buffer)) != -1) {
	                outputStream.write(buffer, 0, bytesRead);
	            }
	        }
	    }catch(IOException e) {
	        e.printStackTrace();
	    }
	}
	
	public void log(String text, User user) {
		if(log_channel == null) return;
		MessageCreateBuilder mb = new MessageCreateBuilder();
		mb.setContent("> **"+user.getName()+"** ("+user.getAsMention()+") "+text);
		mb.setAllowedMentions(Collections.emptySet());	
		log_channel.sendMessage(mb.build()).queue();
	}

    public static void main(String[] args)  {
    	if(System.console() == null) {
    		JOptionPane.showMessageDialog(null, "No se puede ejecutar así", "Error", JOptionPane.INFORMATION_MESSAGE);
    		return;
    	}	
    }

}
