package me.Dary.KriptonBotBungee.Objects;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import javax.imageio.ImageIO;

import lombok.Getter;
import lombok.Setter;
import me.Dary.KriptonBotBungee.KriptonBot;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;

public class LevelsData {
	
	private KriptonBot plugin = KriptonBot.getInstance();
	private Random random = new Random();
    private int baseExp = 75;
    private int expRangeMin = 8; 
    private int expRangeMax = 16; 
    private int expIncreasePerLevel = 100;
    
	@Getter
	private long discordID;	
	@Getter
	private int messages;
	@Getter
	private int level;
	@Getter
	private int experience;
	@Getter
	private String color;
	@Getter
	private String customBackground;
	private BufferedImage backgroundImage;
	@Getter
	private boolean modified;
	@Setter
	private int boardPosition = -1;
	@Getter
	private boolean levelNotifications = true;
	private long lastMessage;
	@Getter @Setter
	private boolean saved = false;
	
    public LevelsData(long discordID) {
        this.discordID = discordID;
    }
    
    public LevelsData(long discordID, int messages, int level, int experience, String color, String customBackground, boolean levelNotifications) {
        this.discordID = discordID;
        this.messages = messages;
        this.level = level;
        this.experience = experience;
        this.color = color;
        this.customBackground = customBackground;
        this.levelNotifications = levelNotifications;
    }
    
    public void performMessage() {
    	saved = false;
		if(lastMessage != 0 && (System.currentTimeMillis() - lastMessage) < 60000) return;
        int expGain = calculateRandomExpGain();
        experience = experience+expGain;
        checkLevelUp();
    	messages++;
    	lastMessage = System.currentTimeMillis();
    }

    private int calculateRandomExpGain() {
        return random.nextInt(expRangeMax - expRangeMin + 1) + expRangeMin;
    }
    
    private void checkLevelUp() {
        int expRequired = getExpRequired();
        if (experience >= expRequired) {
        	level++;
        	experience -= expRequired;
        	if(level >= 15) checkLevelRewards();
        	announceLevelUP();
        }
    }
    
    public int getBoardPosition() {
    	if(boardPosition == -1) return plugin.levelsManager().leaderboard().size()+1;
    	return boardPosition;
    }
    
    public User getUser() {
    	User user = KriptonBot.getInstance().jda().getUserById(discordID);
    	if(user != null) return user;
    	return plugin.jda().retrieveUserById(discordID).complete();
    }
    
    public String getColor() {
    	if(color == null) return "#5663f7";
    	return color;
    }
    
    public double getLevelProgress() {
    	return (Double.valueOf(experience) / Double.valueOf(getExpRequired())) * 100;	
    }
    
    public int getExpRequired() {
        return ((level >= 1) ? (baseExp + (expIncreasePerLevel * (level))) : baseExp);
    }
    
    public int getTotalXp() {
    	return ((level) * (baseExp + (baseExp + ((level) - 1) * expIncreasePerLevel)) / 2)+experience;
    }
    
    public void announceLevelUP() {
		MessageCreateBuilder builder = new MessageCreateBuilder();
		builder.setContent("<@"+discordID+">");
    	EmbedBuilder embed = new EmbedBuilder();
    	embed.setAuthor("Subiste de nivel a "+level, null, "https://images.emojiterra.com/twitter/512px/1f389.png");			
    	embed.setColor(Color.decode(getColor()));
    	embed.setImage("attachment://card.png");
    	builder.setEmbeds(embed.build());
    	builder.addActionRow(Button.secondary("TOGGLE_LEVEL_NOTIFICATIONS", Emoji.fromFormatted("<:noping:1161383951953313852>")));
    	if(!this.levelNotifications) {
	    	builder.setSuppressedNotifications(true);
	    	builder.setAllowedMentions(Collections.emptySet());   
    	}
    	builder.addFiles(FileUpload.fromData(plugin.levelsManager().image(plugin.levelsManager().getStats(discordID)), "card.png"));
    	plugin.jda().getTextChannelById(482162341979619338l).sendMessage(builder.build()).queue();
    }
    
    public boolean toggleLevelNotifications() {
    	levelNotifications = levelNotifications ? false : true;
    	return levelNotifications;	
    }
    
    private void checkLevelRewards() {
    	switch(level) {
    		case 60 : {
    			List<Role> rewards = List.of(
    					plugin.levelsManager().roleRewards().get(60),
    					plugin.levelsManager().roleRewards().get(45),
    					plugin.levelsManager().roleRewards().get(30),
    					plugin.levelsManager().roleRewards().get(15));
    			plugin.discord().kripton().modifyMemberRoles(plugin.discord().kripton().getMember(getUser()), rewards, null).reason("Recompensas de nivel").queue();
    			return; 			
    		}
    		case 45 : {
    			List<Role> rewards = List.of(
    					plugin.levelsManager().roleRewards().get(45),
    					plugin.levelsManager().roleRewards().get(30),
    					plugin.levelsManager().roleRewards().get(15));
    			plugin.discord().kripton().modifyMemberRoles(plugin.discord().kripton().getMember(getUser()), rewards, null).reason("Recompensas de nivel").queue();
    			return;     			
    		}
    		case 30 : {
    			List<Role> rewards = List.of(
    					plugin.levelsManager().roleRewards().get(30),
    					plugin.levelsManager().roleRewards().get(15));
    			plugin.discord().kripton().modifyMemberRoles(plugin.discord().kripton().getMember(getUser()), rewards, null).reason("Recompensas de nivel").queue();
    			return;    			
    		}
    		case 15 : {
    			List<Role> rewards = List.of(
    					plugin.levelsManager().roleRewards().get(15));
    			plugin.discord().kripton().modifyMemberRoles(plugin.discord().kripton().getMember(getUser()), rewards, null).reason("Recompensas de nivel").queue();
    			return;    			
    		}
    	}
    }
    
    public BufferedImage background() {
    	if(customBackground == null) return null;
    	if(backgroundImage != null) return backgroundImage;
        try {
            BufferedImage image = ImageIO.read(new File(plugin.dataManager().imageFolder()+ "/"+discordID+".png"));
            if(image == null) {
            	customBackground = null;
            	return null;
            }
            backgroundImage = image;
            return image;

        } catch (IOException e) {}
    	return null;
    }
    
    public void color(String color) {
    	saved = false;
    	this.color = color;
    }
    
    public void background(BufferedImage image) {
    	saved = false;
    	if(image == null) {
    		customBackground = null;
    		return;
    	}
    	try {
			ImageIO.write(image, "png", new File(plugin.dataManager().imageFolder()+ "/"+discordID+".png"));
			customBackground = "true";
			backgroundImage = image;
		} catch (IOException e) {
			plugin.getLogger().severe("Hubo un error al guardar la imagen de perfil de "+discordID);
			e.printStackTrace();
			
		}
    }
}
