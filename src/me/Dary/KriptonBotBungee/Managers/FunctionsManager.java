package me.Dary.KriptonBotBungee.Managers;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import me.Dary.KriptonBotBungee.KriptonBot;
import me.Dary.KriptonBotBungee.Objects.NekoEmbed;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.ItemComponent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.requests.ErrorResponse;

public class FunctionsManager {
	
	public static KriptonBot plugin = KriptonBot.getInstance();
	
	public static Object disableButtonFunction(ButtonInteractionEvent button_event, String... ids) {
    	Runnable task = () -> {
    		try {
    			List<ActionRow> components = new ArrayList<ActionRow>();
    			for(ActionRow row : button_event.getMessage().getActionRows()) {
    				List<Button> bts = new ArrayList<Button>();
    				for(Button button : row.getButtons()) {
    					boolean toDisable = Arrays.stream(ids).anyMatch(id -> id.equalsIgnoreCase(button.getId()));
    					bts.add(toDisable ? button.asDisabled() : button);	
    				}	
    				components.add(ActionRow.of(bts.toArray(new ItemComponent[0])));			
    			}	
    			button_event.getMessage().editMessageComponents(components).queue();
    		} catch(Exception e) {
    			e.printStackTrace();
    		}
    	};
    	return task;  
	}
			
	public static Object disableButtonFunction(ButtonInteractionEvent button_event) {
    	Runnable task = () -> {
    		try {
    			button_event.editButton(button_event.getButton().asDisabled()).queue();
    		} catch(Exception e) {
    			e.printStackTrace();
    		}
    	};
    	return task;  
	}
	
	public static Object deleteMessageFunction(MessageChannelUnion channel) {
    	Runnable task = () -> {
    		try {
    			channel.delete().queue();
    		} catch(Exception e) {
    			e.printStackTrace();
    		}
    	};
    	return task;  
	}


    public static Object privateMessageFunction(String input, User uu, MessageChannelUnion ch,  List<OptionMapping> options) {
    	String error = "";
    	try {
    		String[] a = input.split(" ");
    		if(a.length < 2) {
    			error = "Hubo un error al ejecutar la acción '[PRIVATEMESSAGE] "+input+"' (Falta de argumentos)";
    			plugin.getLogger().severe(error);
    			return error;
    		}
    		
    		long userID = 0;  		
        	try {
        		userID =  Long.parseLong(a[0]);
        	} catch(NumberFormatException e) {
        		error = "Hubo un error al ejecutar la acción '[PRIVATEMESSAGE] "+input+"' ('"+a[0]+"' debe ser la ID de un usuario)";
        		plugin.getLogger().severe(error);
        		return error;
        	}
        	User usr = plugin.utils().user_cache.get(userID);
        	if(usr == null) {
            	try {
            		usr = plugin.jda().retrieveUserById(userID).complete();
            	} catch(Exception e) {
            		error = "Hubo un error al ejecutar la acción '[PRIVATEMESSAGE] "+input+"' (Más info en consola)";
            		plugin.getLogger().severe(error);	
            		e.printStackTrace();
            		return error;
            	}
        	}
        	plugin.utils().user_cache.put(userID, usr);
        	String msg = input.replaceAll(userID+" ", "");
        	final User user = usr;
        	Runnable task = () -> {
        		try {
                	user.openPrivateChannel().queue(channel ->{
                		channel.sendMessage(new NekoEmbed(msg).getMessage(user, options, ch)).queue(null, new ErrorHandler().ignore(ErrorResponse.UNKNOWN_MESSAGE).handle(ErrorResponse.CANNOT_SEND_TO_USER, x -> {}));
                	});
        		} catch(Exception e) {
        			e.printStackTrace();
        		}
        	};
        	return task;     
    	} catch(Exception e) {
    		error = "Hubo un error al ejecutar la acción '[PRIVATEMESSAGE] "+input+"'";
    		plugin.getLogger().severe(error);
    		e.printStackTrace();
			return error;
    	}
    }
    
    public static Object messageFunction(String input, User u, MessageChannelUnion ch,  List<OptionMapping> options) {
    	String error = "";
    	try {
    		String[] a = input.split(" ");
    		if(a.length < 2) {
    			error = "Hubo un error al ejecutar la acción '[MESSAGE] "+input+"' (Falta de argumentos)";
    			plugin.getLogger().severe(error);	
    			return error;
    		}
    		
    		TextChannel c = null;;
    		try {
    			c = plugin.jda().getTextChannelById(a[0]);
    		} catch (Exception e) {}
    		if(c == null) {
    			error = "Hubo un error al ejecutar la acción '[MESSAGE] "+input+"' (ID del canal inválida)";
    			plugin.getLogger().severe(error);	
    			return error;
    		}
    		
    		String msg = input.replaceAll(c.getId()+" ", "");
    		NekoEmbed embed = new NekoEmbed(msg);
    		final TextChannel channel = c;
        	Runnable task = () -> {
        		try {
        			channel.sendMessage(embed.getMessage(u, options, ch)).queue();
        		} catch(Exception e) {
        			e.printStackTrace();
        		}
        	};
        	return task;     
    	} catch(Exception e) {
    		error = "Hubo un error al ejecutar la acción '[MESSAGE] "+input+"'";
    		plugin.getLogger().severe(error);	
    		e.printStackTrace();
    		return error;
    	}
    }
    
    public static Object giveRoleFunction(String input) {
    	String[] a = input.split(" ");
    	String error = "";
    	try {        
    		if(a.length < 2) {
    			error = "Hubo un error al ejecutar la acción '[GIVEROLE] "+input+"' (Falta de argumentos)";		
    			plugin.getLogger().severe(error);	
    			return error;
    		}
    		long roleID = 0;
    		long userID = 0;
        	try {
        		roleID =  Long.parseLong(a[0]);
        	} catch(NumberFormatException e) {
        		error= "Hubo un error al ejecutar la acción '[GIVEROLE] "+input+"' ('"+a[0]+"' debe ser la ID de un rol)";
        		plugin.getLogger().severe(error);	
        		return error;
        	}
        	Role role = plugin.jda().getRoleById(roleID);
          	if(role == null) {
          		error = "Hubo un error al ejecutar la acción '[GIVEROLE] "+input+"' (ID de rol inexistente)";
          		plugin.getLogger().severe(error);	
          		return error;
        	}
        	try {
        		userID =  Long.parseLong(a[1]);
        	} catch(NumberFormatException e) {
        		error ="Hubo un error al ejecutar la acción '[GIVEROLE] "+input+"' ('"+a[1]+"' debe ser la ID de un usuario)";
        		plugin.getLogger().severe(error);	
        		return error;
        	}
        	User u = plugin.utils().user_cache.get(userID);
        	if(u == null) {
            	try {
            		u = plugin.jda().retrieveUserById(userID).complete();
            	} catch(Exception e) {
            		error = "Hubo un error al ejecutar la acción '[GIVEROLE] "+input+"' (Más info en consola)";
            		plugin.getLogger().severe(error);	
            		e.printStackTrace();
            		return error;
            	}
        	}
        	plugin.utils().user_cache.put(userID, u);
        	final User user = u;
        	Runnable task = () -> {
        		try {
        			plugin.roleManager().giveRole(user, role, null, false);  	
        		} catch(Exception e) {
        			e.printStackTrace();
        		}
        	};
        	return task;     	
    	} catch(Exception e) {
    		error = "Hubo un error al ejecutar la acción '[GIVEROLE] "+input+"'";
    		e.printStackTrace();
			return error;
    	}
    }
    
    public static Object takeRoleFunction(String input) {
    	String[] a = input.split(" ");
    	String error = "";
    	try {
    		if(a.length < 2) {
    			error = "Hubo un error al ejecutar la acción '[TAKEROLE] "+input+"' (Falta de argumentos)";	
    			plugin.getLogger().severe(error);
    			return error;
    		}
    		long roleID = 0;
    		long userID = 0;
        	try {
        		roleID =  Long.parseLong(a[0]);
        	} catch(NumberFormatException e) {
        		error = "Hubo un error al ejecutar la acción '[TAKEROLE] "+input+"' ('"+a[0]+"' debe ser la ID de un rol)";
        		plugin.getLogger().severe(error);
        		return error;
        	}
        	Role role = plugin.jda().getRoleById(roleID);
          	if(role == null) {
          		error = "Hubo un error al ejecutar la acción '[TAKEROLE] "+input+"' (ID de rol inexistente)";
          		plugin.getLogger().severe(error);
        		return error;
        	}

        	try {
        		userID = Long.parseLong(a[1]);
        	} catch(NumberFormatException e) {
        		error = "Hubo un error al ejecutar la acción '[TAKEROLE] "+input+"' ('"+a[1]+"' debe ser la ID de un usuario)";
        		plugin.getLogger().severe(error);
        		return error;
        	} 
        	User u = plugin.utils().user_cache.get(userID);
        	if(u == null) {
            	try {
            		u = plugin.jda().retrieveUserById(userID).complete();
            	} catch(Exception e) {
            		error = "Hubo un error al ejecutar la acción '[TAKEROLE] "+input+"' (Más info en consola)";
            		plugin.getLogger().severe(error);	
            		e.printStackTrace();
            		return error;
            	}
        	}
        	plugin.utils().user_cache.put(userID, u);
        	final User user = u;
        	Runnable task = () -> {
        		try {
        			plugin.roleManager().takeRole(user, role);  	
        		} catch(Exception e) {
        			e.printStackTrace();
        		}
        	};
        	return task;
    	} catch(Exception e) {
    		error = "Hubo un error al ejecutar la acción '[TAKEROLE] "+input+"'";
    		e.printStackTrace();
			return error;
    	}
    }
    
    public static Object giveTempRoleFunction(String input) {
    	String[] a = input.split(" ");
    	String error = "";
    	try {
    		if(a.length < 3) {
    			error = "Hubo un error al ejecutar la acción '[GIVETEMPROLE] "+input+"' (Falta de argumentos)";
    			plugin.getLogger().severe(error);
    			return error;
    		}
        	long roleID = 0;
        	long userID = 0;
        	String time = "";
        	Boolean f = false;
        	roleID = 0;
        	try {
        		roleID =  Long.parseLong(a[0]);
        	} catch(NumberFormatException e) {
        		error = "Hubo un error al ejecutar la acción '[GIVETEMPROLE] "+input+"' ('"+a[0]+"' debe ser la ID de un rol)";
        		plugin.getLogger().severe(error);
        		return error;
        	}
        	String[] options = new String[0];
        	for(int i = 1; i < a.length; i++) {
        		String x = a[i];
        		if(x.matches("\\d+[d|h|m|s]")) {
        			time = time+(i > 1 ? " " : "")+x;
        		} else {
        			if(i == 1) {
        				error = "No se pudo ejecutar la accion '[GIVETEMPROLE] "+input+"' (El formato de tiempo debe ser Xd Xh Xm Xs)";
        				plugin.getLogger().severe(error);
        				return error;
        			}
        			options = Arrays.copyOfRange(a, i, a.length);
        			break;
        		}
        	}
        	if(options.length == 0) {
    			error = "Hubo un error al ejecutar la acción '[GIVETEMPROLE] "+input+"' (Falta de argumentos)";
    			plugin.getLogger().severe(error);
    			return error;
        	}
       
        	try {
        		userID = Long.parseLong(options[0]);
        	} catch(NumberFormatException e) {
        		error = "Hubo un error al ejecutar la acción '[GIVETEMPROLE] "+input+"' ('"+options[0]+"' debe ser la ID de un usuario)";
        		plugin.getLogger().severe(error);
        		return error;
        	} 
        	if(options.length >= 2) {
            	try {
            		f = options[1].equalsIgnoreCase("#force");
            	} catch(Exception e) {}
        	}
        	Role role = plugin.jda().getRoleById(roleID);
        	if(role == null) {
        		error = "Hubo un error al ejecutar la acción '[GIVETEMPROLE] "+input+"' (ID de rol inexistente)";
        		plugin.getLogger().severe(error);
        		return error;
        	}
        	User u = plugin.utils().user_cache.get(userID);
        	if(u == null) {
            	try {
            		u = plugin.jda().retrieveUserById(userID).complete();
            	} catch(Exception e) {
            		error = "Hubo un error al ejecutar la acción '[GIVETEMPROLE] "+input+"' (Más info en consola)";
            		plugin.getLogger().severe(error);	
            		e.printStackTrace();
            		return error;
            	}
        	}
        	plugin.utils().user_cache.put(userID, u);
        	LocalDateTime date = plugin.utils().getFutureDate(plugin.utils().getSeconds(time));
        	final boolean force = f;
        	final User user = u;
        	Runnable task = () -> {
        		try {
        			plugin.roleManager().giveRole(user, role, date, force);  	
        		} catch(Exception e) {
        			e.printStackTrace();
        		}
        	};
        	return task;
    		
    	} catch (Exception e) {
    		error = "Hubo un error al ejecutar la acción '[GIVETEMPROLE] "+input+"' (Más info en consola)";
    		plugin.getLogger().severe(error);	
    		e.printStackTrace();
    		return error;
    	}
    }

}
