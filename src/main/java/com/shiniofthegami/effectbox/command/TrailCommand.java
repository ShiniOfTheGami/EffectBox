package com.shiniofthegami.effectbox.command;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.shiniofthegami.effectbox.CommandHandler;
import com.shiniofthegami.effectbox.EffectBox;
import com.shiniofthegami.effectbox.Expiry;
import com.shiniofthegami.effectbox.effectlibrary.ParticleEffect;
import com.shiniofthegami.effectbox.serializable.Trail;
import com.shiniofthegami.effectbox.handlers.EffectHandler;

public class TrailCommand extends CommandHandler{
	private final static String TRAIL_BASE_PERM = "effectbox.trail.";
	public TrailCommand(EffectBox pl) {
		super(pl);
	}

	public boolean onCommand(CommandSender sender, Command command,	String label, String[] args) {
		if(args.length < 1){
			return false;
		}
		if(args[0].equalsIgnoreCase("add")){
			executeAddCommand(sender, Arrays.copyOfRange(args, 1, args.length));
			return true;
		}else if(args[0].equalsIgnoreCase("remove")){
			executeRemoveCommand(sender, Arrays.copyOfRange(args, 1, args.length));
			return true;
		}
		return false;
	}
	
	private void addTrail(CommandSender sender, Player target, ParticleEffect effect){
		Trail t = new Trail(target, effect);
		pl.getTrailHandler().addTrail(t);
		if((Player) sender != target){
			sender.sendMessage(ChatColor.GOLD + "The " + EffectHandler.getEffectName(effect) + " trail has been applied to " + target.getDisplayName());
		}
		target.sendMessage(ChatColor.GOLD + "The " + EffectHandler.getEffectName(effect) + " trail has been applied to you");
	}
	
	private void addTrail(CommandSender sender, Player target, ParticleEffect effect, Expiry expiry){
		Trail t = new Trail(target, effect, expiry);
		pl.getTrailHandler().addTrail(t);
		if((Player) sender != target){
			sender.sendMessage(ChatColor.GOLD + "The " + EffectHandler.getEffectName(effect) + " trail has been applied to " + target.getDisplayName() +" with an expiry time of 1 " + expiry.getName() + ".");
		}
		target.sendMessage(ChatColor.GOLD + "The " + EffectHandler.getEffectName(effect) + " trail has been applied to you with an expiry time of 1 " + expiry.getName() + ".");
	}
	
	private void addAllTrails(CommandSender sender, Player target, Expiry expiry){
		if(sender instanceof Player){
			Player p = (Player) sender;
			if(!p.getUniqueId().equals(target.getUniqueId())){
				if(!p.hasPermission("effectbox.other")){
					p.sendMessage(ChatColor.RED + "You are not allowed to modify other Players' trails!");
					return;
				}
			}
		}
		
		Set<ParticleEffect> effects = new HashSet<ParticleEffect>(EffectHandler.getEffects());
		for(ParticleEffect effect : effects){
			if(sender.hasPermission(TRAIL_BASE_PERM + EffectHandler.getEffectName(effect))){
				if(expiry == null){
					addTrail(sender, target, effect);
				}else{
					addTrail(sender, target, effect, expiry);
				}
				
			}
		}
		
	}
	
	private void removeAllTrails(CommandSender sender, Player target){
		if(sender instanceof Player){
			Player p = (Player) sender;
			if(!p.getUniqueId().equals(target.getUniqueId())){
				if(!p.hasPermission("effectbox.other")){
					p.sendMessage(ChatColor.RED + "You are not allowed to remove other Players' trails!");
					return;
				}
			}
		}
		Set<Trail> trails = new HashSet<Trail>(pl.getTrailHandler().getTrails(target));
		for(Trail t : trails){
			pl.getTrailHandler().removeTrail(t);
		}
		target.sendMessage(ChatColor.GOLD + "All your trails have been removed.");
	}
	
	private void executeAddCommand(CommandSender sender, String[] args){
		if(args.length < 1 || args.length > 3){
			sender.sendMessage("Usage: /trail add <effect> [target] [m|y]");
			return;
		}
		ParticleEffect effect = null;
		Player p = null;
		Expiry expire = null;
		String lastArg = args[args.length-1];
		if(lastArg.equalsIgnoreCase("m")||lastArg.equalsIgnoreCase("y")){
			if(args.length==3){
				p = Bukkit.getPlayer(args[1]);
			}else{
				if(!(sender instanceof Player)){
					sender.sendMessage("You have to be ingame to apply a trail to yourself.");
					return;
				}
				p = (Player) sender;
			}
			if(lastArg.equalsIgnoreCase("m")){
				expire = Expiry.MONTH;
			}else if(lastArg.equalsIgnoreCase("y")){
				expire = Expiry.YEAR;
			}
		}else{
			if(args.length == 2){
				p = Bukkit.getPlayer(lastArg);
			}else{
				if(!(sender instanceof Player)){
					sender.sendMessage("You have to be ingame to apply a trail to yourself.");
					return;
				}
				p = (Player) sender;
			}
		}
		if(p == null){
			sender.sendMessage(ChatColor.RED + "Targeted Player could not be found");
			return;
		}
		if(args[0].equalsIgnoreCase("*")){
			addAllTrails(sender, p, expire);
			return;
		}else{
			effect = EffectHandler.getEffect(args[0]);
		}
		if(effect == null){
			sender.sendMessage(ChatColor.RED + "The specified effect could not be found");
			return;
		}
		if(!checkPermission(sender, effect, p, expire)){
			return;
		}
		if(expire == null){
			addTrail(sender, p, effect);
		}else{
			addTrail(sender, p, effect, expire);
		}
	}
	
	private void executeRemoveCommand(CommandSender sender, String[] args){
		if(args.length < 1 || args.length > 2){
			sender.sendMessage("Usage: /trail remove <effect> [target]");
			return;
		}
		ParticleEffect effect = null;
		Player p = null;
		
		if(args.length == 1){
			if(!(sender instanceof Player)){
				sender.sendMessage("You have to be ingame to remove a trail from yourself.");
				return;
			}
			p = (Player) sender;
			
		}else{
			p = Bukkit.getPlayer(args[1]);
		}
		if(p == null){
			sender.sendMessage(ChatColor.RED + "Targeted Player could not be found");
			return;
		}
		
		if(args[0].equalsIgnoreCase("*")){
			removeAllTrails(sender, p);
			return;
		}else{
			effect = EffectHandler.getEffect(args[0]);
		}
		
		if(effect == null){
			sender.sendMessage(ChatColor.RED + "The specified effect could not be found");
			return;
		}
		if(!checkPermission(sender, effect, p, null)){
			return;
		}
		Trail t = new Trail(p, effect);
		pl.getTrailHandler().removeTrail(t);
		p.sendMessage(ChatColor.GOLD + "All trails of type " + EffectHandler.getEffectName(effect) + " have been removed from you.");
	}

	private boolean checkPermission(CommandSender sender, ParticleEffect effect, Player target, Expiry expire){
		if(!(sender instanceof Player))
			return true;
		Player p = (Player) sender;
		if(!p.getUniqueId().equals(target.getUniqueId())){
			if(!p.hasPermission("effectbox.other")){
				p.sendMessage(ChatColor.RED + "You are not allowed to modify other players' trails!");
				return false;
			}
		}
		if(!p.hasPermission(TRAIL_BASE_PERM + EffectHandler.getEffectName(effect))){
			p.sendMessage(ChatColor.RED + "You are not allowed to use that Trail!");
			return false;
		}
		return true;
	}
}