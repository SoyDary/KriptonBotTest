package me.Dary.KriptonBotBungee.Objects;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

import com.google.common.collect.Lists;

import me.Dary.KriptonBotBungee.KriptonBot;
import me.Dary.KriptonBotBungee.Utils.Utils.PreparedEmbed;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.md_5.bungee.config.Configuration;

public class NekoEmbed {
	private String txt;
	private KriptonBot plugin = KriptonBot.getInstance();
	private Configuration embeds = plugin.dataManager().embeds;
	PreparedEmbed preparedEmbed;
	String embedTag;
	List<Button> buttons = new ArrayList<Button>();
	List<List<Button>> actionRows = new ArrayList<List<Button>>();
	public boolean isEphemeral;
	
	public NekoEmbed(String txt) {
		this.txt = (txt == null || txt.isBlank()) ? "null" : txt;	
		this.preparedEmbed = plugin.utils().getEmbedTag(txt);
		if(preparedEmbed !=  null) {
			embedTag = preparedEmbed.getKey();
			loadButtons();		
		}
	}
	
	public NekoEmbed(String txt, Button... buttons) {
		this.txt = (txt == null || txt.isBlank()) ? "null" : txt;	
		this.preparedEmbed = plugin.utils().getEmbedTag(txt);
		for(Button bt : buttons) this.buttons.add(bt);
		if(preparedEmbed !=  null) {
			embedTag = preparedEmbed.getKey();	
			loadButtons();		
		} else {
			if(!this.buttons.isEmpty()) actionRows = Lists.partition(this.buttons, 5);
		}
		
	}
	
	private void loadButtons() {
		if(embeds.getStringList(embedTag+".Buttons").isEmpty()) return;		
		for(String str : embeds.getStringList(embedTag+".Buttons")) {
			if(plugin.discord().buttons.containsKey(str.toLowerCase())) buttons.add(plugin.discord().buttons.get(str.toLowerCase()));	
		}	
		if(!buttons.isEmpty()) actionRows = Lists.partition(buttons, 5);
	}
	
	public MessageCreateData getMessage(User user, List<OptionMapping> options, MessageChannel channel) {	
		MessageCreateBuilder mb = new MessageCreateBuilder();
		this.isEphemeral = this.setEphemeral(user, options, channel);
		/////////////////CARGAR BOTONES///////////////////////////
		if(!buttons.isEmpty()) {
			String id = UUID.randomUUID().toString().replaceAll("-", "");
			List<Button> row = new ArrayList<Button>();
			for(Button button : buttons) {
				String bt_id = button.getId();
				
				row.add(button.getId() == null ? button : (bt_id.startsWith("KPB:") ? button.withId(bt_id+"="+id) : button));
				if(bt_id != null && bt_id.startsWith("KPB:")) {
					String response = plugin.dataManager().buttons.getString(bt_id.split("KPB:")[1]+".response");
					if(response != null && !response.isEmpty()) {
						String functions = plugin.discord().getButtonFunctions(bt_id);
						plugin.discord().saveInteractionResponse(bt_id+"="+id, replace(response, user, options, channel), replace(functions, user, options, channel), isEphemeral);					
					}
				}
				if(row.size() == 5) {
					mb.addActionRow(row);
					row.clear();
				}
			}
			if(!row.isEmpty()) mb.addActionRow(row);
		}
		/////////////////////////////////////////////////////////
		if(embedTag == null) return mb.setContent(replace(txt, user, options, channel)).build();		
		String content = embeds.getString(embedTag+".Content");	
		if(content != null && !content.isEmpty()) mb.setContent(content);
		if(embeds.getBoolean(embedTag+".Embed.Enabled")) {
			EmbedBuilder eb = new EmbedBuilder();
			String color = embeds.getString(embedTag+".Embed.Color");
			String author_name = embeds.getString(embedTag+".Embed.Author.Name");
			String author_url = embeds.getString(embedTag+".Embed.Author.Url");
			String author_image = embeds.getString(embedTag+".Embed.Author.ImageUrl");	
			String thumbnailurl = embeds.getString(embedTag+".Embed.ThumbnailUrl");	
			String title_text = embeds.getString(embedTag+".Embed.Title.Text");
			String title_url = embeds.getString(embedTag+".Embed.Title.Url");
			String description = embeds.getString(embedTag+".Embed.Description");				
			List<String> fields = embeds.getStringList(embedTag+".Embed.Fields");
			String imageurl = embeds.getString(embedTag+".Embed.ImageUrl");
			String footer_text = embeds.getString(embedTag+".Embed.Footer.Text");
			String footer_icon_url = embeds.getString(embedTag+".Embed.Footer.IconUrl");
			/////////////////Build embed///////////////////////////
			String validColor = color != null ? replace(color, user, options, channel) : "null";	
			if(validColor.matches("^&?#([aA-fF0-9]{6})$")) eb.setColor(Color.decode(validColor));		
			if(author_name != null && !author_name.isEmpty()) eb.setAuthor(replace(author_name, user, options, channel), replace(author_url, user, options, channel), replace(author_image, user, options, channel)); 
			if(thumbnailurl != null && !thumbnailurl.isEmpty()) eb.setThumbnail(replace(thumbnailurl, user, options, channel));
			if(title_text != null && !title_text.isEmpty()) eb.setTitle(replace(title_text, user, options, channel), replace(title_url, user, options, channel));  
			if(description != null && !description.isEmpty()) eb.setDescription(replace(description, user, options, channel));
			for(String str : fields) {
				str = plugin.placeholderManager().setPlaceholders(str, user, options, channel, false);
				String[] aa = str.split(";");
				if(aa.length == 1 || str.isEmpty()) continue;	
				if(aa[0].equals("blank")) {		
						eb.addBlankField(Boolean.valueOf(aa[1])); 
				} else {
					String title = str.split(";")[0];
					String desc = str.split(";")[1];
					Boolean inline = Boolean.valueOf(str.split(";")[2]);
					desc = desc.replaceAll(Pattern.quote("\\n"), "\n");					
					eb.addField(replace(title, user, options, channel), replace(desc, user, options, channel), inline);
				}							
			}
			if(imageurl != null && !imageurl.isEmpty()) eb.setImage(replace(imageurl, user, options, channel)); 
			if(footer_icon_url != null && !footer_icon_url.isEmpty()) eb.setFooter(replace(footer_text, user, options, channel), replace(footer_icon_url, user, options, channel)); else eb.setFooter(replace(footer_icon_url, user, options, channel));
			eb.setTimestamp(null);
			mb.setEmbeds(eb.build());
		}
		return mb.build();	
	}
	
	boolean setEphemeral(User user, List<OptionMapping> options, MessageChannel channel) {
		String b = embeds.getString(embedTag+".Ephemeral");
		if(b != null && !b.isEmpty()) {
			try {
				b = Boolean.valueOf(replace(b, user, options, channel))+"";
			} catch(Exception e) {
				b = "false";
			}
		} else {
			b = String.valueOf(embeds.getBoolean(embedTag+".Ephemeral"));
		}
		return Boolean.valueOf(b);	
	}
	
	String replace(String str, User user, List<OptionMapping> options, MessageChannel channel) {
		if(str == null || str.isEmpty()) return null;
		if(!embeds.getBoolean(embedTag+".IgnorePlaceholders")) 
			str = plugin.placeholderManager().setPlaceholders(str, user, options, channel, false);
		if(preparedEmbed != null) {
			if(!preparedEmbed.getArgs().isEmpty()) str = parseCustomArgs(str, user, options, channel);	
		}
		return str;
		
	}
	String replace(String str, User user, List<OptionMapping> options, MessageChannel channel, boolean args) {
		if(str == null || str.isEmpty()) return null;
		if(!embeds.getBoolean(embedTag+".IgnorePlaceholders")) 
			str = plugin.placeholderManager().setPlaceholders(str, user, options, channel, false);
		return str;
		
	}
	
	String parseCustomArgs(String str, User user, List<OptionMapping> options, MessageChannel channel) {
		List<String> args = preparedEmbed.getArgs();
		for(int i = 1; i <= args.size();  i++) {
			str = str.replaceAll("(?i)\\{arg"+Pattern.quote(String.valueOf(i))+"\\}", replace(args.get(i-1), user, options, channel, true));
		}
		return str;
	}
}