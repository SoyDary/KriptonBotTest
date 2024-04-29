package me.Dary.KriptonBotBungee.Managers.storage;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.util.HashMap;

import lombok.Getter;
import me.Dary.KriptonBotBungee.KriptonBot;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

public class DataManager {
	
	KriptonBot plugin;
	@Getter
	private File configFile;
	@Getter
	private File embedsFile;
	@Getter
	private File buttonsFile;
	@Getter
	private File imageFolder;
	public Configuration config;
	public Configuration embeds;
	public Configuration buttons;
	public SQLConnection sqlconnection;
	public Connection connection;
	public HashMap<String, String> javascripts = new HashMap<String, String>();
	
	public DataManager(KriptonBot plugin) {
		this.plugin = plugin;
		if(loadConfig()) {
			loadJavaScripts();
			connect();
		}
	}

	private void connect() {
		String host = config.getString("Connection.host");
		String database = config.getString("Connection.database");
		String username = config.getString("Connection.username");
		String password = config.getString("Connection.password");
		if((host.isEmpty() || database.isEmpty() || username.isEmpty() || password.isEmpty())) {
			plugin.getLogger().severe("Faltan datos para poder conectarse a una base de datos!");
			return;
		}
		sqlconnection = new SQLConnection(host, database, username, password);
		connection = sqlconnection.connection;		
	}
	
	private boolean loadConfig() {	
	    if(!plugin.getDataFolder().exists()) plugin.getDataFolder().mkdirs(); 
		this.configFile = new File(plugin.getDataFolder()+"/config.yml");
		this.embedsFile = new File(plugin.getDataFolder()+"/embeds.yml");
		this.buttonsFile = new File(plugin.getDataFolder()+"/buttons.yml");
		this.imageFolder = new File(plugin.getDataFolder()+"/data/image");
		if(!imageFolder.exists()) imageFolder.mkdirs(); 
		if(!imageFolder.exists()) imageFolder.mkdirs(); 
		if(!configFile.exists()) {
			try {
				plugin.getLogger().info("Archivo de configuración creado! "+configFile.getPath());
				configFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if(!embedsFile.exists()) {
			try {
				embedsFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if(!buttonsFile.exists()) {
			try {
				buttonsFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return reloadConfig();
	}
	
	public void loadJavaScripts() {
		File JSFolder = new File(plugin.getDataFolder()+"/javascripts");
		if(!JSFolder.exists()) {
			plugin.saveResource("javascripts/example.js");
		}
    	for(File file : JSFolder.listFiles()) {
    		if(!file.getName().endsWith(".js")) continue;
    		try {
    			javascripts.put(file.getName().replaceAll(".js", "").toLowerCase(), file.getPath());
    		} catch (Exception e) {}
    		
    	}
    	plugin.getLogger().info("[JavaScript] Cargados "+javascripts.size()+" placeholders.");
	}
	
	public boolean reloadConfig() {
		try {
			this.config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(this.configFile);
			this.embeds = ConfigurationProvider.getProvider(YamlConfiguration.class).load(this.embedsFile);
			this.buttons = ConfigurationProvider.getProvider(YamlConfiguration.class).load(this.buttonsFile);
		} catch(IOException e) {
			plugin.getLogger().severe("Hubo un error al crear los crear los archivos de configuración");
			return false;
		}
		return true;
	}

	public Configuration getConfig() {
		return config;
	}
}

