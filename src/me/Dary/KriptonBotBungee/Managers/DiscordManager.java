package me.Dary.KriptonBotBungee.Managers;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import lombok.Getter;
import me.Dary.KriptonBotBungee.KriptonBot;
import me.Dary.KriptonBotBungee.Objects.NekoCommand;
import me.Dary.KriptonBotBungee.Objects.NekoEmbed;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.Command.Type;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.requests.ErrorResponse;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.md_5.bungee.config.Configuration;

public class DiscordManager {

	public HashMap<Long, Command> allCommands = new HashMap<Long, Command>();
	public HashMap<Long, NekoCommand> customCommands = new HashMap<Long, NekoCommand>();
	public HashMap<String, List<String>> buttons_functions = new HashMap<String, List<String>>();
	public HashMap<String, Button> buttons = new HashMap<String, Button>();
	Map<String, CommandData> systemCommandData = new HashMap<String, CommandData>();
	List<String> valid_options = List.of("ATTACHMENT", "BOOLEAN", "CHANNEL", "MENTIONABLE", "NUMBER", "INTEGER", "ROLE", "STRING", "USER");
	List<String> system_commands = List.of("vincular", "resetpassword", "kripton", "level", "leaderboard", "card", "Perfil", "embed", "color");
	KriptonBot plugin;
	@Getter
	private Guild kripton;

	public DiscordManager(KriptonBot plugin) {
		this.plugin = plugin;
		for (Command command : plugin.jda().retrieveCommands().complete())
			allCommands.put(command.getIdLong(), command);
		updateSystemInteractions();
		kripton = plugin.jda().getGuildById(424615933919625216l);
	}

	public void load() {
		checkCacheCommands();
		loadButtons();
		loadCustomCommands();
	}

	public String getButtonFunctions(String key) {
		return new Gson().toJson(buttons_functions.getOrDefault(key, new ArrayList<String>())).toString();
	}

	public void loadButtons() {
		for (String key : plugin.dataManager().buttons.getKeys()) {
			loadbutton(key.toLowerCase());
			buttons_functions.put("KPB:" + key.toLowerCase(),
					plugin.dataManager().buttons.getStringList(key.toLowerCase() + ".functions"));
		}
	}

	public void saveInteractionResponse(String hash, String response, String functions, boolean ephemeral) {
		if (response == null || response.isEmpty())
			return;
		try {
			plugin.getSQL().setData("interactions", "Hash", hash, "Response", response, "Functions",
					functions.length() < 2 ? null : functions, "Ephemeral", ephemeral ? "1" : "0");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public String getInteractionResponse(String hash) {
		try {
			return plugin.getSQL().getValue("interactions", "Response", "Hash", hash);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return "";
	}

	public List<String> getInteractionFunctions(String hash) {
		String functions = null;
		try {
			String query = "UPDATE interactions SET Used = ? WHERE Hash = ?";
			PreparedStatement statement = plugin.getSQL().getConnection().prepareStatement(query);
			statement.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
			statement.setString(2, hash);
			statement.executeUpdate();

			functions = plugin.getSQL().getValue("interactions", "Functions", "Hash", hash);
		} catch (SQLException e) {
			functions = null;
			e.printStackTrace();
		}
		if (functions == null || functions.isBlank())
			return new ArrayList<String>();
		Gson gson = new Gson();
		return gson.fromJson(functions, new TypeToken<List<String>>() {
		}.getType());
	}

	public long getCommandID(String key) {
		try {
			ResultSet result = plugin.getSQL().getData("commands", "Name", key);
			if (result.next())
				return result.getLong("ID");
		} catch (SQLException e) {
			return 0;
		}
		return 0;
	}

	public void saveCommandCache(long id, String key) {
		try {
			plugin.getSQL().setData("commands", "ID", String.valueOf(id), "Name", key);
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	public void deleteCommandCache(String key) {
		try {
			plugin.getSQL().deleteData("commands", "Name", key);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void deleteCommandCache(Long id) {
		try {
			plugin.getSQL().deleteData("commands", "ID", "" + id);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void checkCacheCommands() {
		try {
			ResultSet resultSet = plugin.getSQL().getColumn("commands", "Name");
			while (resultSet.next()) {
				String key = resultSet.getString("Name");
				String cache = plugin.getConfig().getString("Commands." + key + ".label");
				if (cache == null || cache.isEmpty()) {
					Long command_ID = getCommandID(key);
					Command command = plugin.jda().retrieveCommandById(command_ID).complete();
					plugin.getLogger().info("[-----] Elminado el " + command.getName());
					command.delete().complete();
					deleteCommandCache(key);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private void loadCustomCommands() {
		for (String str : plugin.getConfig().getSection("Commands").getKeys()) {
			String id = str;
			String cmd_label = plugin.getConfig().getString("Commands." + str + ".label");
			if (cmd_label == null || cmd_label.isEmpty()) continue;
			if (system_commands.contains(cmd_label.toLowerCase())) {
				plugin.getLogger().warning("El comando '"+cmd_label+"' no pudo ser registrado porque no está disponible.");
				continue;
			}
			String description = plugin.getConfig().getString("Commands." + str + ".description");
			String response = plugin.getConfig().getString("Commands." + str + ".response");

			Permission permission = null;
			String perm = plugin.getConfig().getString("Commands." + str + ".permission");
			if (perm != null && !perm.isBlank()) {
				try {
					permission = Permission.valueOf(perm.toUpperCase());
				} catch (Exception e) {
					plugin.getLogger().warning("El permiso '" + perm + "' para el comando '" + id
							+ "' no es válido. Puedes ver todos los permisos en https://github.com/DV8FromTheWorld/JDA/blob/master/src/main/java/net/dv8tion/jda/api/Permission.java");
				}
			}

			OptionData[] options = getOptions(str, plugin.getConfig().getSection("Commands." + str + ".options"));
			List<CommandCondition> conditions = new ArrayList<CommandCondition>();
			for (String s : plugin.getConfig().getSection("Commands." + id + ".conditions").getKeys()) {
				String condition_id = s;
				String condition = plugin.getConfig()
						.getString("Commands." + id + ".conditions." + condition_id + ".condition");
				String deny_response = plugin.getConfig()
						.getString("Commands." + id + ".conditions." + condition_id + ".deny_response");
				if (condition != null && !condition.isBlank() && deny_response != null && !deny_response.isBlank())
					conditions.add(new CommandCondition(condition_id, condition, deny_response));
			}
			NekoCommand command = new NekoCommand(id, cmd_label.toLowerCase(), description, permission, response,
					options, conditions);
			plugin.getProxy().getScheduler().runAsync(plugin, new Runnable() {
				public void run() {
					command.registerCommnand();
				}
			});
		}
	}

	public OptionData[] getOptions(String command, Configuration section) {
		List<DataObject> optionList = new ArrayList<DataObject>();
		if (section.getKeys().isEmpty())
			return new OptionData[0];
		for (String key : section.getKeys()) {
			String label = key.toLowerCase();
			if (!key.matches("[a-z0-9_]+")) {
				plugin.getLogger()
						.warning("Omitida la opción '" + key + ":" + label + "' por tener un nombre inválido");
				continue;
			}
			String description = section.getString(key + ".description");
			if (description.isEmpty()) {
				plugin.getLogger()
						.warning("Omitida la opción '" + key + ":" + label + "' por no tener una descripción.");
				continue;
			}
			OptionType type = null;
			boolean required = section.getBoolean(key + ".required");
			String T = section.getString(key + ".type").toUpperCase();
			if (T.isEmpty()) {
				plugin.getLogger().warning("Omitida la opción '" + key + ":" + label + "' por no definir el tipo.");
				continue;
			}
			if (valid_options.contains(T))
				type = OptionType.valueOf(T);
			else {
				plugin.getLogger()
						.warning("Omitida la opción '" + key + ":" + label + "' por ser inválida (" + T + ")");
				continue;
			}			
			StringBuilder json = new StringBuilder();
			json.append("{");
			json.append("\"name\":\"" + label + "\"");
			json.append(",\"description\":\"" + description + "\"");
			json.append(",\"required\":\"" + required + "\"");
			json.append(",\"type\":\"" + type.getKey() + "\"");
			if (type.canSupportChoices()) {
				List<String> choices = section.getStringList(key + ".choices");
				if (type == OptionType.STRING) {
					if (!choices.isEmpty()) {
						StringBuilder sb = new StringBuilder();
						sb.append("[");
						int i = 0;
						for (String c : choices) {
							String[] values = c.split(";");
							String v = (values.length == 2) ? values[1] : values[0];
							sb.append((i > 0 ? "," : "") + "{\"name\":\"" + values[0] + "\"," + "\"value\":\"" + v
									+ "\"}");
							i++;
						}
						sb.append("]");
						json.append(",\"choices\":" + sb.toString() + "");
					}
					Integer min = section.getInt(key + ".min_length");
					if (min >= 1)
						json.append(",\"min_length\":" + min + "");
					Integer max = section.getInt(key + ".max_length");
					if (max >= 1 && max >= min)
						json.append(",\"max_length\":" + max + "");
				} else {
					if (type == OptionType.INTEGER || type == OptionType.NUMBER) {
						if (!choices.isEmpty()) {
							StringBuilder sb = new StringBuilder();
							sb.append("[");
							int i = 0;
							for (String c : choices) {
								String[] values = c.split(";");
								Double v;
								try {
									v = (values.length == 2) ? Double.valueOf(values[1]) : Double.valueOf(values[0]);
								} catch (NumberFormatException e) {
									continue;
								}
								sb.append((i > 0 ? "," : "") + "{\"name\":\"" + values[0] + "\"," + "\"value\":"
										+ (type == OptionType.INTEGER ? v.intValue() + "" : v + "") + "}");
								i++;
							}
							sb.append("]");
							json.append(",\"choices\":" + sb.toString() + "");
						}
						Double def = -55325.5;
						Double min = section.getDouble(key + ".min_value", def);
						if (min != def)
							json.append(
									",\"min_value\":" + (type == OptionType.INTEGER ? min.intValue() + "" : min + ""));
						Double max = section.getDouble(key + ".max_value", def);
						if (max != def && max >= min)
							json.append(
									",\"max_value\":" + (type == OptionType.INTEGER ? max.intValue() + "" : max + ""));
					}
				}
			}
			json.append("}");
			optionList.add(DataObject.fromJson(json.toString()));
		}
		OptionData[] options = new OptionData[optionList.size()];
		for (int i = 0; i < optionList.size(); i++) {
			options[i] = OptionData.fromData(optionList.get(i));
		}
		return options;
	}

	public void loadbutton(String key) {
		try {
			String type = plugin.dataManager().buttons.getString(key + ".type").toUpperCase();
			String label = plugin.dataManager().buttons.getString(key + ".label");
			String keyEmoji = plugin.dataManager().buttons.getString(key + ".emoji");
			String link = plugin.dataManager().buttons.getString(key + ".link");
			Emoji emoji = null;
			if (keyEmoji != null && !keyEmoji.isEmpty())
				emoji = Emoji.fromFormatted(keyEmoji);
			if (type.equals("LINK")) {
				if (label.isEmpty())
					buttons.put(key, Button.of(ButtonStyle.LINK, link, emoji));
				else
					buttons.put(key, Button.of(ButtonStyle.LINK, link, label, emoji));
				return;
			}
			if (label.isEmpty())
				buttons.put(key, Button.of(ButtonStyle.valueOf(type), "KPB:" + key, emoji));
			else
				buttons.put(key, Button.of(ButtonStyle.valueOf(type), "KPB:" + key, label, emoji));
		} catch (Exception e) {
			plugin.getLogger().warning("Hubo un error al cargar el emoji '" + key + "' " + e.getLocalizedMessage());
		}
	}
	
	public void updateSystemInteractions() {
		systemCommandData.put("embed", Commands.slash("embed", "Crea un embed")
				.addOption(OptionType.STRING, "json", "Embed a partir de un json").setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR)));
		systemCommandData.put("Editar", Commands.message("Editar").setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR)));
		//systemCommandData.put("Terminar sorteo", Commands.message("Terminar sorteo").setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR)).setGuildOnly(true));
		systemCommandData.put("Copiar embed", Commands.message("Copiar embed").setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR)));
		systemCommandData.put("Perfil", Commands.user("Perfil"));	
		systemCommandData.put("leaderboard", Commands.slash("leaderboard", "🌟 Mustra el podio de los miembros con más experiencia de chat."));
		systemCommandData.put("level", Commands.slash("level", "💬 Muestra el nivel y experiencia de chat tuyo o de otra persona.").addOption(OptionType.USER, "miembro", "Perfil de usuario"));
		systemCommandData.put("card", Commands.slash("card", "🏷 Modifica el color y fondo de tu tarjeta de nivel.").addSubcommands(
                new SubcommandData("background", "🖼 Coloca una imagen de fondo para tu tarjeta de nivel.").addOption(OptionType.ATTACHMENT, "imagen", "Adjunta una imagen (Tamaño ideal: 1000 x 250 px)", false),
                new SubcommandData("color", "🎨 Coloca un color principal para tu tarjeta de nivel.").addOptions(new OptionData(OptionType.STRING, "color", "Color hexadecimal").setAutoComplete(true))));
		systemCommandData.put("vincular", Commands.slash("vincular", "✅ Introduce un código de vinculación para conectar tu cuenta con minecraft.").addOption(OptionType.INTEGER, "codigo", "Código de vinculación", true));
		systemCommandData.put("resetpassword", Commands.slash("resetpassword", "🔓 Genera una nueva contraseña de tu cuenta de minecraft vinculada."));
		systemCommandData.put("color", Commands.slash("color", "🌈 Definie el color para tu nombre."));
			
		List<String> commands = new ArrayList<String>();
		for(Command cmd : allCommands.values()) {
			CommandData data = systemCommandData.get(cmd.getName());
			commands.add(cmd.getName());
			if(data != null) updateCommand(data, cmd);
		}
		systemCommandData.values().forEach(cmd -> {
			if(!commands.contains(cmd.getName())) registerCommand(cmd);
		});
	}
	
	public void updateCommand(CommandData commandData, Command command) {
		if(commandData.getType() == Type.SLASH) {
			updateSlashCommand((SlashCommandData )commandData, command);
			return;
		}
	}
	
	void registerCommand(CommandData command) {
		try {	
			plugin.jda().upsertCommand(command).queue(cmd ->
			{
				plugin.getLogger().info("[^^^^] Registrado el comando "+command.getName());
			}, new ErrorHandler().handle(ErrorResponse.APPLICATION_COMMAND_NAME_ALREADY_EXISTS, (e)-> {
				plugin.getLogger().warning("El comando '"+command.getName()+"' no pudo ser registrado porque ya hay otro con ese nombre.");
			}));
		} catch (Exception e) {
			e.printStackTrace();
		}	
	}
	
	public boolean updateSlashCommand(SlashCommandData commandData, Command command) {
		List<OptionData> options = commandData.getOptions();
		DefaultMemberPermissions permissions = commandData.getDefaultPermissions();
		String label = commandData.getName();
		String description = commandData.getDescription();
		List<SubcommandData> oldSubCommands = command.getSubcommands().stream().map(SubcommandData::fromSubcommand).collect(Collectors.toList());
		List<SubcommandData> subCommands = commandData.getSubcommands();
		boolean updateSubCommands = false;
		if(oldSubCommands.size() == subCommands.size()) {		
			for (int i = 0; i < oldSubCommands.size(); i++) {	
				String osc = oldSubCommands.get(i).toData().toString();
				String sc = subCommands.get(i).toData().toString();
				if(osc.equals(sc)) continue;
				updateSubCommands = true;
				break;
			}
		} else {
			updateSubCommands = true;
		}
		
		try {
			
			List<OptionData> new_options = commandData.getOptions();
			List<OptionData> old_options = new ArrayList<OptionData>();
			old_options.addAll(command.getOptions().stream().map(OptionData::fromOption).collect(Collectors.toList()));
			
			String n_options = new_options.stream()
				    .map(OptionData::toData)
				    .map(Object::toString)
				    .collect(Collectors.joining());
		    String o_options = old_options.stream()
				    .map(OptionData::toData)
				    .map(Object::toString)
				    .collect(Collectors.joining());
		    
			if(!n_options.toString().equals(o_options.toString())) 
				command.editCommand().addOptions(options).queue(s -> {
					plugin.getLogger().info("[>>>>] Actualizadas las opciones del comando '"+s.getName()+"'");
				});
			
			long new_perms = 0; try {new_perms = commandData.getDefaultPermissions().getPermissionsRaw();} catch(Exception e) {}
			long old_perms = 0; try {old_perms = command.getDefaultPermissions().getPermissionsRaw();} catch(Exception e) {}		
			if(new_perms != old_perms) {
				command.editCommand().setDefaultPermissions(permissions).queue(s -> {
					plugin.getLogger().info("[>>>>] Actualizados los permisos del comando '"+s.getName()+"'");
				});
			} 
			
			if(command.isGuildOnly() != commandData.isGuildOnly()) {
	    		command.editCommand().setGuildOnly(commandData.isGuildOnly()).queue(s -> {
	    			plugin.getLogger().info("[>>>>] Actualizado el guild-mode del comando '"+s.getName()+"'");
	    		});	 
			}
			
			if(command.isNSFW() != commandData.isNSFW()) {
	    		command.editCommand().setNSFW(commandData.isNSFW()).queue(s -> {
	    			plugin.getLogger().info("[>>>>] Actualizado el modo NSFW del comando '"+s.getName()+"'");
	    		});	 
			}
			
	    	if(!command.getName().equals(label))
	    		command.editCommand().setName(label).queue(s -> {
	    			plugin.getLogger().info("[>>>>] Actualizado el label del comando '"+s.getName()+"'");
	    		});	 
	    	
	    	if(!command.getDescription().equals(description)) 
	    		command.editCommand().setDescription(description).queue(s -> {
	    			plugin.getLogger().info("[>>>>] Actualizada la descripcion del comando '"+s.getName()+"'");
	    		});
	    	
			if(updateSubCommands)
				command.editCommand().addSubcommands(subCommands).queue(s -> {
					plugin.getLogger().info("[>>>>] Actualizados los sub comandos del comando '"+s.getName()+"'");
				});
	    	return true;
		} catch (Exception e) {
			plugin.getLogger().severe("Hubo un error al actualizar los datos comando "+command.getName()+"  >  "+e.getLocalizedMessage());
			return false;
		}
	}

	public class CommandCondition {

		String id;
		String condition;
		String deny_response;

		public CommandCondition(String id, String condition, String deny_response) {
			this.id = id;
			this.condition = condition;
			this.deny_response = deny_response;
		}

		public NekoEmbed getDenyResponse() {
			return new NekoEmbed(deny_response);
		}

		public String getConditionID() {
			return id;
		}

		public String getCondition() {
			return condition;
		}

		public boolean check(User user, List<OptionMapping> options, MessageChannelUnion channel) {
			String con = plugin.placeholderManager().setPlaceholders(condition, user, options, channel, false);
			return plugin.conditionsManager().checkCondition(con);
		}
	}
}
