package me.Dary.KriptonBotBungee.Managers;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import me.Dary.KriptonBotBungee.KriptonBot;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.requests.restaction.CacheRestAction;
import net.md_5.bungee.api.scheduler.ScheduledTask;

public class RoleManager {
	
	KriptonBot plugin;
	DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
	Integer timer;
	Map<String, ScheduledTask> tasks = new HashMap<String, ScheduledTask>();
	ArrayList<Role> vipRoles = new ArrayList<>();
	
	public RoleManager(KriptonBot plugin) {
		this.plugin = plugin;
		this.timer = plugin.getConfig().getInt("TempRoles.UpdateIterval", 5);
		for(long id : plugin.getConfig().getLongList("VipRoles")) {
			try {
				vipRoles.add(plugin.jda().getRoleById(id));
			} catch(Exception e) {
				plugin.getLogger().severe("El rol vip `"+id+"` es inválido.");
			}
		}
		startTask();
	}
	
	
	void startTask() {
		plugin.getProxy().getScheduler().schedule(plugin, new Runnable() {
		    @Override
		    public void run() {
		    	updateRoles();
		    }
		}, 0, timer, TimeUnit.MINUTES);
	}	
	
	public LocalDateTime getTime() {
		return LocalDateTime.now();
	}
	public void updateRoles() {
		plugin.getLogger().info("[Roles] Verificando usuarios...");
        LocalDateTime expirationTime = getTime();
        String selectQuery = "SELECT * FROM roles WHERE ExpirationDate < ?";
        try (PreparedStatement selectStmt = plugin.getSQL().connection.prepareStatement(selectQuery)) {
            selectStmt.setTimestamp(1, Timestamp.valueOf(expirationTime));
            ResultSet resultSet = selectStmt.executeQuery();
            List<Map<String, Object>> rowsToDelete = new ArrayList<Map<String, Object>>();
            while (resultSet.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("ID", resultSet.getInt("ID"));
                row.put("UserID", resultSet.getLong("UserID"));
                row.put("RoleID", resultSet.getLong("RoleID"));
                row.put("ExpirationDate", resultSet.getTimestamp("ExpirationDate").toLocalDateTime());
                rowsToDelete.add(row);
            }
            if (!rowsToDelete.isEmpty()) {
                for(Map<String, Object> row : rowsToDelete) {
                	Long userID = (Long) row.get("UserID");
                	Long roleID = (Long) row.get("RoleID");
                	CacheRestAction<User> user = plugin.jda().retrieveUserById(userID);
                	Role role = plugin.jda().getRoleById(roleID);
                	if(user != null && role != null) takeRole(user.complete(), role);
                }
                String deleteQuery = "DELETE FROM roles WHERE ExpirationDate < ?";
                try (PreparedStatement deleteStmt = plugin.getSQL().connection.prepareStatement(deleteQuery)) {
                    deleteStmt.setTimestamp(1, Timestamp.valueOf(expirationTime));
                    deleteStmt.executeUpdate();
                }
            } 
        } catch (SQLException e) {
            e.printStackTrace();
        }
	}

	public void takeRole(User user, Role role) {
		sheduleRemove(user, role, -1);
		role.getGuild().removeRoleFromMember(user, role).queue(s -> {
			try {
				 String deleteQuery = "DELETE FROM roles WHERE UserID = ? AND RoleID = ?";
				 PreparedStatement deleteStmt = plugin.getSQL().connection.prepareStatement(deleteQuery);
			     deleteStmt.setLong(1, user.getIdLong());
			     deleteStmt.setLong(2, role.getIdLong());
			     deleteStmt.executeUpdate();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		});	
	}
	
	public void sheduleRemove(User user, Role role, long seconds) {
		String hash = user.getId()+":"+role.getId();
		ScheduledTask task = tasks.get(hash);
		if(task != null) {
			plugin.getProxy().getScheduler().cancel(task.getId());
			tasks.remove(hash);
		}
		if(seconds != -1) {
			task = plugin.getProxy().getScheduler().schedule(plugin, new Runnable() {
			    @Override
			    public void run() {
			    	takeRole(user, role);
			    }
			}, seconds, TimeUnit.SECONDS);
			tasks.put(hash, task);
		}
	}
	public void giveRole(User user, Role role, LocalDateTime expiration, boolean force) {
		plugin.getLogger().info("[Roles] "+plugin.utils().getUserTag(user)+" recibió el rango "+role.getName());
		role.getGuild().addRoleToMember(user, role) .queue(s -> {
			if(expiration != null) {
				if(saveTempRole(user, role, expiration) && force) {
					Duration duration = Duration.between(Instant.now(), expiration.atZone(ZoneId.systemDefault()).toInstant());
					if(duration.getSeconds() < timer*60) sheduleRemove(user, role, duration.toSeconds());			
				}
			} else {		
				try {
					sheduleRemove(user, role, -1);	
					String deleteQuery = "DELETE FROM roles WHERE UserID = ? AND RoleID = ?";
					PreparedStatement deleteStmt = plugin.getSQL().connection.prepareStatement(deleteQuery);
				    deleteStmt.setLong(1, user.getIdLong());
				    deleteStmt.setLong(2, role.getIdLong());
				    deleteStmt.executeUpdate();
				} catch (SQLException e) {
					e.printStackTrace();
				}	
			}
		});
	}

	boolean saveTempRole(User user, Role role, LocalDateTime expiration) {
	    String query = "SELECT * FROM roles WHERE UserID = ? AND RoleID = ?";
	    try (PreparedStatement statement = plugin.getSQL().getConnection().prepareStatement(query)) {
	        statement.setLong(1, user.getIdLong());
	        statement.setLong(2, role.getIdLong());
	        ResultSet result = statement.executeQuery();
	        if (result.next()) {
	            query = "UPDATE roles SET ExpirationDate = ? WHERE UserID = ? AND RoleID = ?";
	            PreparedStatement updateStatement = plugin.getSQL().getConnection().prepareStatement(query);
                updateStatement.setTimestamp(1, Timestamp.valueOf(expiration));
                updateStatement.setLong(2, user.getIdLong());
                updateStatement.setLong(3, role.getIdLong());
                int rows = updateStatement.executeUpdate();
                if (rows == 1) return true;
	        } else {
	            query = "INSERT INTO roles (UserID, RoleID, ExpirationDate) VALUES (?, ?, ?)";
	            PreparedStatement insertStatement = plugin.getSQL().getConnection().prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
                insertStatement.setLong(1, user.getIdLong());
                insertStatement.setLong(2, role.getIdLong());
                insertStatement.setTimestamp(3, Timestamp.valueOf(expiration));
                int rows = insertStatement.executeUpdate();
                if (rows == 1) {
                    try (ResultSet generatedKeys = insertStatement.getGeneratedKeys()) {
                        if (generatedKeys.next()) return true;
                    }
                }
	        }
	    } catch (SQLException e) {
	    	e.printStackTrace();
	    }
		return false;
	}
	
	public void giveRole(User user, Role role, String reason) {
		try {
			if(role != null && user != null) role.getGuild().addRoleToMember(user, role).reason(reason).queue();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public void takeRole(User user, Role role, String reason) {
		try {
			if(role != null && user != null) role.getGuild().removeRoleFromMember(user, role).reason(reason).queue();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public boolean isVip(Member member) {
        for (Role rol : member.getRoles()) {
            if (vipRoles.contains(rol))return true;        
        }
        return false;
	}
}
