package me.Dary.KriptonBotBungee.Objects;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import me.Dary.KriptonBotBungee.KriptonBot;
import me.Dary.KriptonBotBungee.Managers.DiscordManager.CommandCondition;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.requests.ErrorResponse;

public class NekoCommand {

	KriptonBot plugin = KriptonBot.getInstance();
	public String key;
	public String label;
	public String description;
	public String response;
	public OptionData[] options;
	public Long commandID;
	public List<CommandCondition> conditions;
	public Collection<Permission> permissions = new ArrayList<Permission>();
	public Permission permission;
	public SlashCommandData commandTemplate;
	public List<String> functions;
	
	
	public NekoCommand(String key, String label, String description, Permission permission, String response, OptionData[] options, List<CommandCondition> conditions) {
		this.key = key;
		this.label = label.toLowerCase();
		this.description = description;
		if(permission != null) permissions.add(permission);
		this.permission = permission;
		this.response = response;
		this.options = options;
		this.conditions = conditions;
		this.commandTemplate = Commands.slash(label, description);
		this.commandTemplate.setDefaultPermissions(DefaultMemberPermissions.enabledFor(permissions));
		if(options.length > 0) commandTemplate.addOptions(options);
		this.functions = plugin.getConfig().getStringList("Commands."+key+".functions");
		this.commandID = plugin.discord().getCommandID(key);	
	}


	public NekoEmbed getResponse() {
		return new NekoEmbed(response);	
	}
	
	public List<String> getFunctions() {
		return functions;
	}
	
	public void registerCommnand() {	
		if(commandID == 0) {
			plugin.jda().upsertCommand(commandTemplate).queue((command) ->
			{
				plugin.discord().customCommands.put(command.getIdLong(), this);
				plugin.discord().saveCommandCache(command.getIdLong(), key);
				plugin.getLogger().info("[^^^^] Registrado el comando "+command.getName());
			}, new ErrorHandler().handle(ErrorResponse.APPLICATION_COMMAND_NAME_ALREADY_EXISTS, (e)-> {
				plugin.getLogger().warning("El comando '"+label+"' no pudo ser registrado porque ya hay otro con ese nombre.");
			}));
			
		} else {
			updateCommand();
		}
	}
	
	void updateCommand() {
		if(commandID != 0) {
			Command command = plugin.discord().allCommands.get(commandID);
			try {
				List<OptionData> new_options = commandTemplate.getOptions();
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
				
				long new_perms = 0; try {new_perms = commandTemplate.getDefaultPermissions().getPermissionsRaw();} catch(Exception e) {}
				long old_perms = 0; try {old_perms = command.getDefaultPermissions().getPermissionsRaw();} catch(Exception e) {}		
				if(new_perms != old_perms) {
					command.editCommand().setDefaultPermissions(DefaultMemberPermissions.enabledFor(permissions)).queue(s -> {
						plugin.getLogger().info("[>>>>] Actualizados los permisos del comando '"+s.getName()+"'");
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
		    	
			} catch (Exception e) {
				plugin.getLogger().severe("[Comando eliminado] Hubo un error al actualizar los datos comando "+command.getName()+"  >  "+e.getLocalizedMessage());
				command.delete().queue();
				plugin.discord().deleteCommandCache(key);
				return;
			}
			plugin.discord().customCommands.put(command.getIdLong(), this);
		}
	}
	
	public CommandCondition checkConditions(User user, List<OptionMapping> options, MessageChannelUnion channel) {
		if(conditions.isEmpty()) return null;
		for(CommandCondition condition : conditions) {
			if(!condition.check(user, options, channel)) return condition;
		}
		return null;
		
	}
}