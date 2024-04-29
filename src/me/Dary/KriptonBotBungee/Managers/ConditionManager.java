package me.Dary.KriptonBotBungee.Managers;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import me.Dary.KriptonBotBungee.KriptonBot;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

public class ConditionManager {

	KriptonBot plugin;
	
	public ConditionManager(KriptonBot plugin) {
		this.plugin = plugin;
	}
	
	public Object sendFunctions(String text, User user,  List<OptionMapping> options, MessageChannelUnion channel, boolean force) {
		String option = text.split(" ")[0];
		String value = text.replaceFirst(Pattern.quote(option)+" ", "");
		value = plugin.placeholderManager().setPlaceholders(value, user, options, channel, force);
		switch (option.toUpperCase()) {
	    case "[MESSAGE]":
	    	return FunctionsManager.messageFunction(value, user, channel, options);
	    	
	    case "[GIVEROLE]":
	    	return FunctionsManager.giveRoleFunction(value);
	    	
	    case "[GIVETEMPROLE]":
	    	return FunctionsManager.giveTempRoleFunction(value);
	    	
	    case "[TAKEROLE]":
	    	return FunctionsManager.takeRoleFunction(value);
	    	
	    case "[PRIVATEMESSAGE]":
	    	return FunctionsManager.privateMessageFunction(value, user, channel, options);

	    default:
	    	String error = "'"+option.toLowerCase()+"' no es una opción valida.";
	    	plugin.getLogger().severe(error);
	    	return error;
		}
	}
	
	public Object sendFunctions(String text) {
		String option = text.split(" ")[0];
		String value = text.replaceFirst(Pattern.quote(option)+" ", "");
		switch (option.toUpperCase()) {
	    case "[MESSAGE]":
	    	return FunctionsManager.messageFunction(value, null, null, null);
	    	
	    case "[GIVEROLE]":
	    	return FunctionsManager.giveRoleFunction(value);
	    	
	    case "[GIVETEMPROLE]":
	    	return FunctionsManager.giveTempRoleFunction(value);
	    	
	    case "[TAKEROLE]":
	    	return FunctionsManager.takeRoleFunction(value);
	    	
	    case "[PRIVATEMESSAGE]":
	    	return FunctionsManager.privateMessageFunction(value, null, null, null);

	    default:
	    	String error = "'"+option.toLowerCase()+"' no es una opción valida.";
	    	plugin.getLogger().severe(error);
	    	return error;
		}
	}

	
	public Object sendButtonFunctions(String text, User user, MessageChannelUnion channel, ButtonInteractionEvent button_event) {
		String option = text.split(" ")[0];
		String value = text.replaceFirst(Pattern.quote(option)+" ", "");
		value = plugin.placeholderManager().setPlaceholders(value, user, null, channel, false);
		switch (option.toUpperCase()) {
	    case "[MESSAGE]":
	    	return FunctionsManager.messageFunction(value, user, channel, null);
	    	
	    case "[GIVEROLE]":
	    	return FunctionsManager.giveRoleFunction(value);
	    	
	    case "[GIVETEMPROLE]":
	    	return FunctionsManager.giveTempRoleFunction(value);
	    	
	    case "[TAKEROLE]":
	    	return FunctionsManager.takeRoleFunction(value);
	    	
	    case "[PRIVATEMESSAGE]":
	    	return FunctionsManager.privateMessageFunction(value, user, channel, null);
	    	
	    case "[DISABLEBUTTON]":
	    	return FunctionsManager.disableButtonFunction(button_event);
	    	
	    case "[DELETEMESSAGE]":
	    	return FunctionsManager.deleteMessageFunction(channel);

	    default:
	    	String error = "Hubo un error al ejecutar esta interacción: "+option.toUpperCase()+" no es una opción valida.";
	    	plugin.getLogger().severe(error);
	    	return error;

		}
	}
 
	public boolean checkCondition(String text) {
        text = text.contains(" || ") ? text.replaceAll(" \\|\\| ", "||") : text;
        text = text.contains(" && ") ? text.replaceAll(" && ", "&&") : text;
        Boolean or = false;
        ArrayList<Boolean> counts = new ArrayList<Boolean>();
        if(text.contains("||")) {
            or = true;
        }
        if(or) {
            for(String t : text.split("\\|\\|")) {
                counts.add(checkBool(t));
            }
            Boolean end = false;
            for(Boolean b : counts) {
                if(b) {
                    return true;
                }
            }
            return end;
        }else {
            for(String t : text.split(Pattern.quote("&&"))) {
                counts.add(checkBool(t));
            }
            Boolean end = true;
            for(Boolean b : counts) {
                if(!b) {
                    end = false;
                    break;
                }
            }
            return end;
        }
    }
    
    private boolean checkBool(String text) {
        text = text.contains(" >= ") ? text.replaceAll(" >= ", ">=") : text;
        text = text.contains(" <= ") ? text.replaceAll(" <= ", "<=") : text;
        text = text.contains(" == ") ? text.replaceAll(" == ", "==") : text;
        text = text.contains(" > ") ? text.replaceAll(" > ", ">") : text;
        text = text.contains(" < ") ? text.replaceAll(" < ", "<") : text;
        text = text.contains(" != ") ? text.replaceAll(" != ", "!=") : text;
        
        if(text.contains(">=")) {
            String p1 = text.split(">=")[0];
            String p2 = text.split(">=")[1];
            if(plugin.utils().isInt(p1) && plugin.utils().isInt(p2)) {
                if(Integer.parseInt(p1) >= Integer.parseInt(p2)) {
                    return true;
                }else {
                    return false;
                }
            }else {
                return false;
            }
        }else if(text.contains("<=")) {
            String p1 = text.split("<=")[0];
            String p2 = text.split("<=")[1];
            if(plugin.utils().isInt(p1) && plugin.utils().isInt(p2)) {
                if(Integer.parseInt(p1) <= Integer.parseInt(p2)) {
                    return true;
                }else {
                    return false;
                }
            }else {
                return false;
            }
        }else if(text.contains("==")) {
            String p1 = text.split("==")[0];
            String p2 = text.split("==")[1];
            if(p1 == p2 || p1.matches(p2)) {
                return true;
            }else {
                return false;
            }
        }else if(text.contains(">")) {
            String p1 = text.split(">")[0];
            String p2 = text.split(">")[1];
            if(plugin.utils().isInt(p1) && plugin.utils().isInt(p2)) {
                if(Integer.parseInt(p1) > Integer.parseInt(p2)) {
                    return true;
                }else {
                    return false;
                }
            }else {
                return false;
            }
        }else if(text.contains("<")) {
            String p1 = text.split("<")[0];
            String p2 = text.split("<")[1];
            if(plugin.utils().isInt(p1) && plugin.utils().isInt(p2)) {
                if(Integer.parseInt(p1) < Integer.parseInt(p2)) {
                    return true;
                }else {
                    return false;
                }
            }else {
                return false;
            }
        }else if(text.contains("!=")) {
            String p1 = text.split("!=")[0];
            String p2 = text.split("!=")[1];
            return !p1.matches(p2);
        }
        return false;
    }
    
    public class Function {
    	
    	
    }
}
