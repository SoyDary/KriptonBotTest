package me.Dary.KriptonBotBungee.Utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.script.ScriptEngine;
import javax.script.ScriptException;

import org.openjdk.nashorn.api.scripting.NashornScriptEngineFactory;

import me.Dary.KriptonBotBungee.KriptonBot;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

public class JSParser {
	
	private static KriptonBot plugin = KriptonBot.getInstance();
	private static NashornScriptEngineFactory factory = new NashornScriptEngineFactory();
	
    public static String parseJS(String name) {
    	if(!plugin.dataManager().javascripts.containsKey(name.toLowerCase())) return null;
        ScriptEngine engine = factory.getScriptEngine();
        try (FileReader reader = new FileReader(plugin.dataManager().javascripts.get(name))) {
            engine.eval(reader);
            Object result = engine.eval(name.toLowerCase()+"()");
            return result == null ? "null" : result.toString();
        } catch(IOException | ScriptException e) {     
            e.printStackTrace(); 
            return "Script error (check console)";
        }
    }

    public static String parseJS(String name, User user, List<OptionMapping> options, MessageChannel channel) {	
    	String[] args = name.split(";");
    	name = args[0];
    	if(!plugin.dataManager().javascripts.containsKey(name.toLowerCase())) return null;
        ScriptEngine engine = factory.getScriptEngine();
        engine.put("args", Arrays.copyOfRange(args, 1, args.length));
        try (FileReader reader = new FileReader(plugin.dataManager().javascripts.get(name))) {
        	BufferedReader bufferedReader = new BufferedReader(reader);
            String line;
            StringBuilder sb = new StringBuilder();
            while ((line = bufferedReader.readLine()) != null) {
            	if(line.isEmpty()) continue;
            	line = plugin.placeholderManager().setPlaceholders(line, user, options, channel, false);
                sb.append(line).append("\n");
            }
            String content = sb.toString();
            engine.eval(content);
            Object result = engine.eval(name.toLowerCase()+"()");
            return result == null ? "null" : result.toString();
        } catch(IOException | ScriptException e) {     
            e.printStackTrace(); 
            return "Script error (check console)";
        }
    }
    
}