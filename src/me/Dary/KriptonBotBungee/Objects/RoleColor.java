package me.Dary.KriptonBotBungee.Objects;

import lombok.Getter;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.emoji.Emoji;

public class RoleColor {
	
	@Getter
	private Role role;
	@Getter
	private Emoji emoji;
	
	public RoleColor(Role role, Emoji emoji) {
		this.role = role;
		this.emoji = emoji;
	}	
}