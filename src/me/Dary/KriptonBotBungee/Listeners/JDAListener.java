package me.Dary.KriptonBotBungee.Listeners;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.imageio.ImageIO;

import me.Dary.KriptonBotBungee.KriptonBot;
import me.Dary.KriptonBotBungee.Managers.FunctionsManager;
import me.Dary.KriptonBotBungee.Managers.storage.AccountsManager.LinkedAccount;
import me.Dary.KriptonBotBungee.Objects.LevelsData;
import me.Dary.KriptonBotBungee.Objects.NekoEmbed;
import me.Dary.KriptonBotBungee.Objects.RoleColor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message.Attachment;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdateBoostTimeEvent;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.Command.Choice;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.requests.ErrorResponse;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.dv8tion.jda.api.utils.messages.MessageEditData;

public class JDAListener extends ListenerAdapter {

	KriptonBot plugin;
	List<String> blockedReactions = List.of("💤", "😴", "🛌", "😪", "🥱", "🖕");
	List<RoleColor> roleSelectors = new ArrayList<>();
	List<Role> colorRoles = new ArrayList<>();
	List<Role> alloredColorRoles = new ArrayList<>();
	
	public JDAListener(KriptonBot plugin) {
		this.plugin = plugin;
		this.roleSelectors = Arrays.asList(
				  new RoleColor(plugin.jda().getRoleById(1178051684568662046l), Emoji.fromFormatted("<:Rojo:1178714098012274688>")),
				  new RoleColor(plugin.jda().getRoleById(1178051854140199032l), Emoji.fromFormatted("<:Naranja:1178714064101322772>")),
				  new RoleColor(plugin.jda().getRoleById(1178051910104797204l), Emoji.fromFormatted("<:Amarillo:1178713952591564810>")),
				  new RoleColor(plugin.jda().getRoleById(1178052070696308878l), Emoji.fromFormatted("<:Lima:1178713961655435334>")),	
				  new RoleColor(plugin.jda().getRoleById(1178053843267878932l), Emoji.fromFormatted("<:Morado:1178713968064339988>")),
				  new RoleColor(plugin.jda().getRoleById(1178053994065690777l), Emoji.fromFormatted("<:Rosa:1178714151338659930>")),
				  new RoleColor(plugin.jda().getRoleById(1178054148579676190l), Emoji.fromFormatted("<:Marron:1178713964151050240>")),
				  new RoleColor(plugin.jda().getRoleById(1178054775573585990l), Emoji.fromFormatted("<:Fucsia:1178713958820106250>")),
				  new RoleColor(plugin.jda().getRoleById(1178055163412492441l), Emoji.fromFormatted("<:Celeste:1178713956932649100>")),
				  new RoleColor(plugin.jda().getRoleById(1178055243267846166l), Emoji.fromFormatted("<:Azul:1178713953799512144>")),
				  new RoleColor(plugin.jda().getRoleById(1178055465427542098l), Emoji.fromFormatted("<:Salmon:1178713974066384896>")),
				  new RoleColor(plugin.jda().getRoleById(1178055857280389171l), Emoji.fromFormatted("<:Menta:1178713965757480980>")),
				  new RoleColor(plugin.jda().getRoleById(1178056400270786730l), Emoji.fromFormatted("<:Gris:1178713960300679288>")),
				  new RoleColor(plugin.jda().getRoleById(1178056501521297548l), Emoji.fromFormatted("<:Negro:1178713970153099355>")),
				  new RoleColor(plugin.jda().getRoleById(1178057168914751530l), Emoji.fromFormatted("<:Blanco:1178713955741474946>")),
				  new RoleColor(plugin.jda().getRoleById(1159168640118161449l), Emoji.fromFormatted("<:Cian:1178809684786946080>")));	  
		this.colorRoles = new ArrayList<>(roleSelectors.stream().map(RoleColor::role).toList());
		
		alloredColorRoles.add(plugin.jda().getRoleById(1218418808377708674l)); //Oganesón
		alloredColorRoles.add(plugin.jda().getRoleById(724009935599304856l));  //Radio
		alloredColorRoles.add(plugin.jda().getRoleById(1218418793143861311l)); //Radón
		alloredColorRoles.add(plugin.jda().getRoleById(1218419736925306953l)); //Livermorio
		
		alloredColorRoles.add(plugin.jda().getRoleById(724009934571831307l));  //Bario
		alloredColorRoles.add(plugin.jda().getRoleById(1218418678496890921l)); //Xenón
		alloredColorRoles.add(plugin.jda().getRoleById(1218419666121134160L)); //Polonio
		alloredColorRoles.add(plugin.jda().getRoleById(724009941207089193l));  //Estroncio
		alloredColorRoles.add(plugin.jda().getRoleById(1218418565674307624l)); //Kriptón
		alloredColorRoles.add(plugin.jda().getRoleById(1218419600736256053l)); //Telurio
		
	}

	//Detectar fin del boost	
	public void onGuildMemberUpdateBoostTime(GuildMemberUpdateBoostTimeEvent e) {
		OffsetDateTime total = e.getNewTimeBoosted();
		if(total != null) return;
		if(e.getGuild() != plugin.discord().kripton()) return;
		List<Role> roles = new ArrayList<Role>(colorRoles);
		if(!plugin.roleManager().isVip(e.getMember())) roles.add(plugin.jda().getRoleById(845753583492136960l));
		plugin.discord().kripton().modifyMemberRoles(e.getMember(), null, roles).reason("Fin de un boost").queue();
	}
	

    public void onCommandAutoCompleteInteraction(CommandAutoCompleteInteractionEvent e) {
        if (e.getName().equals("card") && e.getFocusedOption().getName().equals("color")) {
        	e.replyChoices(
        			new Choice("Rojo", "#ff3333"),
        			new Choice("Naranja", "#ff9933"),
        			new Choice("Amarillo", "#ffff33"),
        			new Choice("Verde", "#33ff33"),
        			new Choice("Menta", "#33ff99"),
        			new Choice("Negro", "#333333"),
        			new Choice("Celeste", "#33ffff"),
        			new Choice("Azul", "#3333ff"),
        			new Choice("Gris", "#f2f2f2"),              		
        			new Choice("Morado", "#cc33ff"),
        			new Choice("Rosa", "#ff33ff")
        			).queue();
        }
    }
	
	public void onMessageReceived(MessageReceivedEvent e) {
		if (e.getChannelType() != ChannelType.TEXT) return;
		User user = e.getMessage().getAuthor();
		if(user == null || user.isBot() || user.isSystem()) return;
		
		String msg = e.getMessage().getContentRaw();
		String[] a = e.getMessage().getContentRaw().split(" ");
		//Comando de pruebas
		if (a[0].equals("!test")) {
			if(e.getAuthor().getIdLong() == 751152465415503952l) {
				plugin.levelsManager().getStats(user.getIdLong()).announceLevelUP();
			}
		}
		
		//Detectar boosteos al servidor
		if(e.getMessage().getType().name().contains("BOOST")) {
			plugin.roleManager().giveRole(user, plugin.jda().getRoleById(845753583492136960l), "Boosteo del servidor");
		}
		
		//Listener de niveles
		if(!msg.startsWith("$") && !msg.startsWith("!") && !msg.toLowerCase().startsWith("t!")) {
			plugin.levelsManager().getStats(user.getIdLong()).performMessage();
		}		
		
		//Reacciones para el canal de temática
		if (e.getChannel().getIdLong() == 1081927732096675890l) {
			e.getMessage().addReaction(Emoji.fromCustom("estrella", 1103456217080283248l, true)).queue();
			return;
		}	
		
	}
    
	public void onSlashCommandInteraction(SlashCommandInteractionEvent e) {
		User user = e.getUser();	
		if (e.getName().equals("color")) {		
			e.reply(colorMenu(user)).setEphemeral(true).queue();
			return;
		}
		if (e.getName().equals("resetpassword")) {
			LinkedAccount account = plugin.accountsManager().getAccount(user.getIdLong());
			if(account == null) {
				NekoEmbed embed = new NekoEmbed(plugin.getConfig().getString("Messages.NoAccount"));
				e.reply(embed.getMessage(user, null, e.getChannel())).setEphemeral(embed.isEphemeral).queue();
				return;
			}
			NekoEmbed embed = new NekoEmbed(
					plugin.getConfig().getString("Messages.ResetPassword"), 
					Button.success("RESET_PASSWORD", Emoji.fromUnicode("✅")));
			e.reply(embed.getMessage(user, null, e.getChannel())).setEphemeral(true).queue();
			return;
		}
		if (e.getName().equals("vincular")) {
			int code = e.getOption("codigo").getAsInt();
			if(plugin.accountsManager().confirmLinkRequest(""+code)) {
				plugin.log("Vinculó su cuenta a `"+plugin.accountsManager().getAccount(user.getIdLong()).name()+"` mediante un comando.", user);
				NekoEmbed embed = new NekoEmbed(plugin.getConfig().getString("Linking.Messages.DiscordMessages.Link_sucess"));
				ReplyCallbackAction reply = e.reply(embed.getMessage(user, null, e.getChannel()));
				reply.setEphemeral(embed.isEphemeral).queue();	
			} else {
				NekoEmbed embed = new NekoEmbed(plugin.getConfig().getString("Linking.Messages.DiscordMessages.Invalid_code"));
				ReplyCallbackAction reply = e.reply(embed.getMessage(user, null, e.getChannel()));
				reply.setEphemeral(embed.isEphemeral).queue();	
			}
			return;
			
		}
		if (e.getName().equals("level")) {
			User u = e.getOptions().isEmpty() ? user : e.getOption("miembro").getAsUser();
			if(u.isBot()) u = user;
			InputStream s = plugin.levelsManager().image(plugin.levelsManager().getStats(u.getIdLong()));

			boolean hidden = false;
			if(e.getGuild() != null) hidden = e.getChannel().asTextChannel().getParentCategory().getId().equals("424615933919625218");
			
			e.replyFiles(FileUpload.fromData(s, "card.png")).addActionRow(Button.secondary("generic", "Usa /card para modificar")
					.withEmoji(Emoji.fromUnicode("🎨")).asDisabled()).setEphemeral(hidden).queue();
			return;	
		}
		if (e.getName().equals("leaderboard")) {
			e.deferReply().queue();
			EmbedBuilder embed = new EmbedBuilder();
			embed.setColor(Color.decode("#5663f7"));
			embed.setTitle(":star2: Podio de usuarios con más experiencia");
			embed.setImage("attachment://leaderboard.png");
			e.getHook().sendFiles(plugin.levelsManager().leaderboardIMG()).setEmbeds(embed.build()).queue();
			return;	
		}
		if (e.getName().equals("card")) {
			LevelsData stats = plugin.levelsManager().getStats(user.getIdLong());
            switch (e.getSubcommandName()) {
            case "background": {
            	Attachment image =  e.getOption("imagen") == null ? null :  e.getOption("imagen").getAsAttachment();
            	/*
                if(stats.level() < 10) {
                	e.reply("> Necesitas llegar a nivel 10 para poder usar esto.").setEphemeral(true).queue();
                	return;
                }
                */
            	if(image == null) {
            		if(stats.background() == null) {
            			e.reply("> Tu tarjeta de nivel no tiene ningún fondo personalizado.").setEphemeral(true).queue();
            		} else {
            			e.reply("> El fondo de tu tarjeta de nivel ha sido reestablecido.").setEphemeral(true).queue();
            			stats.background(null);
            		}
            		return;
            	}
            	if(!image.isImage()) {
            		e.reply("> El archivo adjuntado debe ser una imagen.").setEphemeral(true).queue();
            		return;
            	}
            	if(image.getWidth() > 5000) {
            		e.reply("> El ancho de la imagen no puede ser mayor a 5000px\n> \n> _Tamaño recomendado:_ `(1000 x 250)`")
            		.addActionRow(Button.link("https://www.iloveimg.com/resize-image#resize-options,pixels", "Recortar imagenes online").withEmoji(Emoji.fromUnicode("✂️")))
            		.setEphemeral(true).queue();
            		return;
            	}
            	if(image.getHeight() > 1250) {
            		e.reply("> La altura de la imagen no puede ser mayor a 1250px\n> \n> _Tamaño recomendado:_ `(1000 x 250)`")
            		.addActionRow(Button.link("https://www.iloveimg.com/resize-image#resize-options,pixels", "Recortar imagenes online").withEmoji(Emoji.fromUnicode("✂️")))
            		.setEphemeral(true).queue();
            		return;
            	}
            	BufferedImage fixedImage = fixImage(image.getUrl());
            	if(fixedImage == null) {
            		e.reply("> Hubo un error al guardar la imagen.").setEphemeral(true).queue();
            		return;
            	}
            	stats.background(fixedImage);
		    	EmbedBuilder embed = new EmbedBuilder();
		    	embed.setDescription("> Actualizaste el fondo de tu tarjeta de nivel.");	
		    	embed.setColor(Color.decode(stats.getColor()));
		    	embed.setImage("attachment://card.png");
		    	e.replyFiles(FileUpload.fromData(plugin.levelsManager().image(plugin.levelsManager().getStats(user.getIdLong())), "card.png")).addEmbeds(embed.build()).setEphemeral(true).queue();
                break;
            }
            case "color": {
            	String color =  e.getOption("color") == null ? "" :  e.getOption("color").getAsString();
            	if(color.isEmpty()) {
            		if(stats.color() == null) {
            			e.reply("> Tu tarjeta de nivel no tiene ningún color personalizado.").setEphemeral(true).queue();
            		} else {
            			e.reply("> El color de tu tarjeta de nivel ha sido reestablecido.").setEphemeral(true).queue();
            			stats.color(null);
            		}
            		return;
            	}
            	String hexColor = getColor(color);
            	if(hexColor == null) {
            		e.reply("> `"+color.toUpperCase()+"` no es un color hex válido.").setEphemeral(true).queue();
            		return;
            	}      	
            	stats.color(hexColor);
		    	EmbedBuilder embed = new EmbedBuilder();
		    	embed.setDescription("> Actualizaste el color de tu tarjeta de nivel a `"+hexColor+"`");	
		    	embed.setColor(Color.decode(hexColor));
		    	embed.setImage("attachment://card.png");
		    	e.replyFiles(FileUpload.fromData(plugin.levelsManager().image(plugin.levelsManager().getStats(user.getIdLong())), "card.png")).addEmbeds(embed.build()).setEphemeral(true).queue();
                break;
            }
            default:
                break;
            }
            return;	
		}
	}

	public void onButtonInteraction(ButtonInteractionEvent e) {
		String id = e.getButton().getId();
		User user = e.getUser();
		if (id.equals("TOGGLE_LEVEL_NOTIFICATIONS")) {
			LevelsData profile = plugin.levelsManager().getStats(user.getIdLong());
			e.reply((profile.toggleLevelNotifications() ? "> ﻿:white_check_mark:﻿  Notificaciones de nivel `activadas` ": "> ﻿:x:﻿ Notificaciones de nivel `desactivadas`")).setEphemeral(true).queue();
		}
		if (id.equals("RESET_PASSWORD")) {
			LinkedAccount account = plugin.accountsManager().getAccount(user.getIdLong());
			if(account == null) {
				NekoEmbed embed = new NekoEmbed(plugin.getConfig().getString("Messages.NoAccount"));
				e.reply(embed.getMessage(user, null, e.getChannel())).setEphemeral(embed.isEphemeral).queue();
				return;
			}
			String password = plugin.utils().resetPassword(account.name());
			if(password != null) {
				NekoEmbed embed = new NekoEmbed(plugin.getConfig().getString("Messages.ResetPasswordSucess").replaceAll("(?i)\\{password\\}", password));
				plugin.log("Recuperó su contraseña mediante un comando.", user);
				e.reply(embed.getMessage(user, null, e.getChannel())).setEphemeral(true).queue(hook ->{
					e.editButton(e.getButton().asDisabled().withStyle(ButtonStyle.SECONDARY)).queue();
				});
			}
		}
		if (id.startsWith("LINK_REQUEST_CONFIRM:")) {
			String code = id.split(":")[1];
			if(plugin.accountsManager().confirmLinkRequest(code)) {
				plugin.log("Vinculó su cuenta a `"+plugin.accountsManager().getAccount(user.getIdLong()).name()+"` mediante un botón.", user);
				NekoEmbed embed = new NekoEmbed(plugin.getConfig().getString("Linking.Messages.DiscordMessages.Link_sucess"));
				ReplyCallbackAction reply = e.reply(embed.getMessage(user, null, e.getChannel()));
				reply.setEphemeral(embed.isEphemeral).queue((hook) -> ((Runnable) FunctionsManager.disableButtonFunction(e, "LINK_REQUEST_CONFIRM:"+code, "LINK_REQUEST_DENY:"+code)).run());	
			} else {
				NekoEmbed embed = new NekoEmbed(plugin.getConfig().getString("Linking.Messages.DiscordMessages.Invalid_code"));
				ReplyCallbackAction reply = e.reply(embed.getMessage(user, null, e.getChannel()));
				reply.setEphemeral(embed.isEphemeral).queue((hook) -> ((Runnable) FunctionsManager.disableButtonFunction(e, "LINK_REQUEST_CONFIRM:"+code, "LINK_REQUEST_DENY:"+code)).run());	
			}
		}
		if (id.startsWith("LINK_REQUEST_DENY:")) {
			String code = id.split(":")[1];
			plugin.accountsManager().cancelLinkRequest(code);
			e.deferEdit().queue(r -> e.getMessage().delete().queue());
		}
		if (id.startsWith("COLOR_SELECTOR:")) {
			Guild kripton = plugin.discord().kripton();
			Member member = kripton.getMember(user);
			if(id.equals("COLOR_SELECTOR:RESET")) {
				kripton.modifyMemberRoles(member, null, colorRoles).reason("Color reseteado").queue(r -> e.editMessage((MessageEditData.fromCreateData(colorMenu(user)))).queue());
				return;
			}
			Role rol = plugin.jda().getRoleById(id.split(":")[1]);
			
			if(!(member.hasPermission(Permission.ADMINISTRATOR) ||
				     member.isBoosting() ||
				     isSuperiorVIP(member) ||
				     plugin.levelsManager().getStats(user.getIdLong()).level() > 60)) {
			    e.reply("> <:Booster:1178807462401429624> Necesitas ser booster o llegar a <@&1178869781135364126> para usar esto.").setEphemeral(true).queue();
			    return;
			}
			List<Role> memberRoles = new ArrayList<Role>();
			for(Role r : member.getRoles()) if(colorRoles.contains(r)) memberRoles.add(r);	
			kripton.modifyMemberRoles(member, List.of(rol), memberRoles).reason("Selección de color").queue(r -> e.editMessage((MessageEditData.fromCreateData(colorMenu(user)))).queue());
		}
	}

	public void onMessageReactionAdd(MessageReactionAddEvent e) {
		if (!e.isFromGuild())
			return;
		if (e.getUser().isBot())
			return;
		if (e.getChannel().getType() != ChannelType.NEWS)
			return;
		if (blockedReactions.contains(e.getEmoji().getName()) || e.getEmoji().getName().toLowerCase().contains("zzz")) {
			Member member = e.getMember();
			if (member.hasPermission(Permission.MANAGE_CHANNEL))
				return;
			e.getReaction().removeReaction(member.getUser()).queue(r -> {
				member.getUser().openPrivateChannel().queue(channel -> {
					channel.sendMessage(e.getEmoji().getFormatted()).queue(null, new ErrorHandler()
							.ignore(ErrorResponse.UNKNOWN_MESSAGE).handle(ErrorResponse.CANNOT_SEND_TO_USER, x -> {
							}));
				});
			});
		}
	}
	
    public void onUserContextInteraction(UserContextInteractionEvent e) {
    	User user = e.getTarget();
    	if(e.getName().equals("Perfil")) {
    		if(user.isBot()) {
    			e.reply("> No se puede hacer esto con bots").setEphemeral(true).queue();
    			return;
    		}
    		LinkedAccount account = plugin.accountsManager().getAccount(user.getIdLong());
    		if(account == null) {
    			e.reply("> Este usuario no tiene ninguna cuenta de minecraft vinculada").setEphemeral(true).queue();
    			return;
    		}	
    		e.reply("Cuenta asociada: "+account.name()).setEphemeral(true).queue();
    	}
    }
	
	private MessageCreateData colorMenu(User user) {
		MessageCreateBuilder message = new MessageCreateBuilder();
		List<Button> actions = new ArrayList<Button>();
		EmbedBuilder embed = new EmbedBuilder();
		embed.setColor(Color.decode("#5663f7"));
		embed.setTitle("Selecciona un color para tu nombre");
		boolean hasRoles = false;
		for(RoleColor color : roleSelectors) {
			if(actions.size() == 5) {
				message.addActionRow(actions);
				actions = new ArrayList<Button>();
			}
			boolean hasRole = color.role().getGuild().getMember(user).getRoles().contains(color.role());		
			if(!hasRoles && hasRole) {
				hasRoles = true;
				embed.setColor(color.role().getColor());
			}
			actions.add(Button.of(hasRole ? ButtonStyle.SUCCESS : ButtonStyle.SECONDARY, "COLOR_SELECTOR:"+color.role().getId(), color.emoji()).withDisabled(hasRole));
		}
		message.addActionRow(actions);
		if(hasRoles) message.addActionRow(Button.danger("COLOR_SELECTOR:RESET", Emoji.fromUnicode("🗑️")).withLabel("Resetear"));
		message.setEmbeds(embed.build());	
		return message.build();
	}
	
	private BufferedImage fixImage(String url) {
        try {
        	BufferedImage original = ImageIO.read(new URL(url));
            BufferedImage redimensionada = new BufferedImage(1000, 250, original.getType());
            Graphics2D g = redimensionada.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.drawImage(original, 0, 0, 1000, 250, 0, 0, original.getWidth(), original.getHeight(), null);
            g.dispose();
            return redimensionada;
        } catch (IOException e) {
            return null;
        }
	}
	
	private String getColor(String color) {
		if(color.matches("^#[0-9A-Fa-f]{6}$")) return color.toUpperCase();
		if(color.matches("^[0-9A-Fa-f]{6}$")) return "#"+color.toUpperCase();
		return null;
	}
	
	public boolean isSuperiorVIP(Member member) {
        for (Role rol : member.getRoles()) {
            if (alloredColorRoles.contains(rol))return true;        
        }
        return false;
	}
}