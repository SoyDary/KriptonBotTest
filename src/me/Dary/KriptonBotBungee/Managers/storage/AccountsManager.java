package me.Dary.KriptonBotBungee.Managers.storage;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import lombok.Getter;
import me.Dary.KriptonBotBungee.KriptonBot;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class AccountsManager {

	KriptonBot plugin;
	boolean discordsrv = false;
	private Cache<String, String> codes = CacheBuilder.newBuilder().expireAfterWrite(5, TimeUnit.MINUTES).build();
	private List<Role> linkedRoles = new ArrayList<>();

	public AccountsManager(KriptonBot plugin) {
		this.plugin = plugin;
		this.discordsrv = checkDiscordSRV();
		for(long id : plugin.getConfig().getLongList("Linking.LinkedRoles")) {
			try {
				linkedRoles.add(plugin.jda().getRoleById(id));
			} catch(Exception e) {
				plugin.getLogger().severe("El rol de verificación `"+id+"` es inválido.");
			}
		}
		
	}

	public String createLinkRequest(Long id, String name, UUID uuid) {
		String code = findKey(id+";"+name+";"+uuid.toString());
		if(code != null) return code;
		code = getRandomCode();
		codes.put(code, id+";"+name+";"+uuid.toString());
		return code;
	}

	public boolean confirmLinkRequest(String code) {
		String request = getLinkRequest(code);
		if(request == null) return false;
		codes.invalidate(code);
		Long user = Long.valueOf(request.split(";")[0]);
		String player = request.split(";")[1];
		UUID uuid = UUID.fromString(request.split(";")[2]);
		return link(user, player, uuid);
	
	}
	
	public void cancelLinkRequest(String code) {
		String request = getLinkRequest(code);
		if(request == null) return;
		codes.invalidate(code);
	}
	
	public String getLinkRequest(String code) {
		return codes.getIfPresent(code);
	}

	public long getAccount(String name) {
		try {
			ResultSet result = plugin.getSQL().getData("accounts", "Name", name);
			if (result.next())
				return result.getLong("ID");
		} catch (SQLException e) {
			return -1;
		}
		return -1;
	}

	public LinkedAccount getAccount(Long id) {
		try {
			ResultSet result = plugin.getSQL().getData("accounts", "ID", String.valueOf(id));
            while (result.next()) {
            	return new LinkedAccount(
                        result.getLong("ID"),
                        result.getString("Name"),
                        UUID.fromString(result.getString("UUID"))   
                );
            } 
		} catch (SQLException e) {
			return null;
		}
		return null;
	}

	public boolean link(Long id, String name, UUID uuid) {
		try {
			LinkedAccount account = getAccount(id);
			if(account != null) unlink(id, false);
			ProxiedPlayer player = plugin.getProxy().getPlayer(name);
			User user = plugin.jda().retrieveUserById(id).complete();	
			plugin.getSQL().setData("accounts", "ID", String.valueOf(id), "Name", name, "UUID", uuid.toString());
			if(discordsrv) updateDiscordSRV(id.toString(), uuid.toString());
			linkedRoles.forEach(rol -> plugin.roleManager().giveRole(user, rol, "Vinculación de cuenta"));
			if (player != null) {
				player.sendMessage(plugin.utils().component(plugin.placeholderManager().setPlaceholders(plugin.getConfig().getString("Linking.Messages.MinecraftMessages.Link_sucess"), user, null, null, false)));
			}
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	public void unlink(Long id, boolean updateRoles) {
		try {
			plugin.getSQL().deleteData("accounts", "ID", String.valueOf(id));
			if(discordsrv) deleteDiscordSRV(id.toString());
			User user = plugin.jda().retrieveUserById(id).complete();
			if(updateRoles) linkedRoles.forEach(rol -> plugin.roleManager().takeRole(user, rol, "Desvinculación de cuenta"));
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}
	
	private String findKey(String value) {
		String key = null;
        for (Map.Entry<String, String> entry : codes.asMap().entrySet()) {
            if (entry.getValue().equals(value)) {
                key = entry.getKey();
                break;
            }
        }
		return key;
	}
	
	private String getRandomCode() {
        return String.format("%0" + 4 + "d", new Random().nextInt(9999) + 1);
	}
	
	public void clearCache() {
		codes.cleanUp();
	}

	//DiscordSRV
	
	void updateDiscordSRV(String discord, String uuid) {
	    try {
	        String query = "INSERT INTO discordsrv_accounts (discord, uuid) VALUES (?, ?) ON DUPLICATE KEY UPDATE uuid = ?";
	        PreparedStatement insertStatement = plugin.getSQL().getConnection().prepareStatement(query);
	        insertStatement.setString(1, discord);
	        insertStatement.setString(2, uuid);
	        insertStatement.setString(3, uuid);
	        insertStatement.executeUpdate();
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	}
	
	void deleteDiscordSRV(String discord) {
	    try {
	        String query = "DELETE FROM discordsrv_accounts WHERE discord = ?";
	        PreparedStatement deleteStatement = plugin.getSQL().getConnection().prepareStatement(query);
	        deleteStatement.setString(1, discord);
	        deleteStatement.executeUpdate();
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	}
	
	boolean checkDiscordSRV() {
        try (var resultado = plugin.getSQL().getConnection().getMetaData().getTables(null, null, "discordsrv_accounts", null)) {
            return resultado.next();
        } catch (SQLException e) {
        	return false;
        }
	}
	
	public class LinkedAccount {
		
		@Getter
		Long id;
		@Getter
		String name;
		@Getter
		UUID uuid;
		
		public LinkedAccount(Long id, String name, UUID uuid) {
			this.id = id;
			this.name = name;
			this.uuid = uuid;
		}
	}
}