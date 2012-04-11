package me.folkmagick.MsBlock;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import net.milkbowl.vault.permission.Permission;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;




public class MsBlock extends JavaPlugin implements Listener {

	public static MsBlock plugin;
	public static final Logger log = Logger.getLogger("Minecraft");
	
	public static Permission permission = null;
	
	public int _MINDISTANCE = 2;
	public int _MAXDISTANCE = 10;
	public int _SETDISTANCE = _MINDISTANCE;
	//public String[] denySpawn = {"enderdragon","giant","ghast","irongolem","magmacube","slime"};
	public static final List<String> denySpawn = Arrays.asList("enderdragon","giant","ghast","irongolem","magmacube","slime");
	
	
	public void onEnable() {	
		if (setupPermission()) {
			this.logMessage("is now enabled!");
		}
	}

	public void onDisable() {
		this.logMessage("is now disabled!");
	}



	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String cmdLabel, String[] args) {		

		if(!(sender instanceof Player)) {
            log.info("Must be a player, not a console command.");
            return true;
        }
		
		Player player = (Player) sender;
		
		boolean hasPerm = MsBlock.checkPermission(sender, "msblock.use");

		if (hasPerm) {
		
			if (cmdLabel.equalsIgnoreCase("msb")) {
	
				if ( args.length == 0 ) 						{ helpscreen(player); } 
				else if (args.length > 0 && args[0] == "help") 	{ helpscreen(player); }
				else if (args.length == 1 && args[0] != "help") { 
					
					_SETDISTANCE = _MINDISTANCE; 
					msSet(player, args[0], _SETDISTANCE);
					
				}
				else if (args.length == 2 && args[0] != "help") {
					
					if ( args[1] != null) {
						int i = Integer.parseInt(args[1]);
						
						if ( i >= _MINDISTANCE && i <= _MAXDISTANCE ) {
							_SETDISTANCE = i;
						}
						else if ( i > _MAXDISTANCE) {
							_SETDISTANCE = _MAXDISTANCE;
						}
						else if ( i < _MINDISTANCE ) {
							_SETDISTANCE = _MINDISTANCE;
						}
					}
					
					msSet(player, args[0], _SETDISTANCE);
				} 
				else { helpscreen(player); }
			}
		} else {
			player.sendMessage("You do not have permission to do this.");
		}
		
		return true;
	}

	
	public void msSet(Player player, String msType, int i) {
		
		Block target = player.getTargetBlock(null, i);

		if (target.getType() == Material.MOB_SPAWNER) {

			BlockState state = target.getState();
			if (state instanceof CreatureSpawner) {
				CreatureSpawner spawner = (CreatureSpawner) state;
				player.sendMessage( "This is a [" + spawner.getSpawnedType().getName() + "] spawner." );
			}

		}
		else {
			
			// ----------------
			if ( denySpawn.contains(msType.toLowerCase()) ) {
				player.sendMessage( msType + " spawners are disabled here.");
			} else {
				// set block to spawner
				target.setType( Material.MOB_SPAWNER );

				// change spawner to type entered
				BlockState state = target.getState();
				if (state instanceof CreatureSpawner) {
					CreatureSpawner spawner = (CreatureSpawner) state;
					spawner.setCreatureTypeByName( msType.toUpperCase() );
					player.sendMessage( msType + " spawner created at (" + target.getX() + ", " + target.getY() + ", " + target.getZ() + ")" );
				}
			}
		}
		
	}
	

	public void helpscreen(Player player) {
		
		String	help =  "\n\nUsage: /msb <monster type> #distance\n";
				help += "---------------------------\n";
				help += "  Monster Types\n";
				help += "---------------------------\n";
				help += "\n";
				help += "Blaze, CaveSpider, Chicken, Creeper, Enderman, " +
						"MushroomCow, Ocelot, Pig, PigZombie, Sheep, Silverfish, Skeleton, SnowMan " +
						"Spider, Squid, Wolf, Zombie";
		
		sendMultilineMessage(player, help);
	}





	// ========================================================================
	// HELPERS
	
	public static boolean checkPermission(CommandSender sender, String permission) {
		if (MsBlock.permission != null) { // use Vault to check permissions - actually does nothing different than sender.hasPermission at the moment, but might change
			try {
				return MsBlock.permission.has(sender, permission);
			} catch (NoSuchMethodError e) {
				// if for some reason there is a problem with Vault, fall back to default permissions
				MsBlock.log.info("[MsBlock] Checking Vault permission threw an exception. Are you using the most recent version? Falling back to to default permission checking.");
			}
		}
		// fallback to default Bukkit permission checking system
		return sender.hasPermission(permission) || sender.hasPermission("msblock.*");
	}
	
	
	private boolean setupPermission() {
		if (this.getServer().getPluginManager().getPlugin("Vault") != null) {
			RegisteredServiceProvider<Permission> permissionProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
	        if (permissionProvider != null) {
	            permission = permissionProvider.getProvider();
	        }

	        MsBlock.log.info("[MsBlock] Vault hooked as permission plugin.");
	        return (permission != null);
		}
		permission = null; // if the plugin is reloaded during play, possibly kill permissions
		MsBlock.log.info("[MsBlock] Vault plugin not found - defaulting to Bukkit permission system.");
		return false;
	}
	
	
	

	public void sendMultilineMessage(Player player, String message){
		if (player != null && message != null && player.isOnline()){
			String[] s = message.split("\n");
			for (String m : s){
				player.sendMessage(m);
			}
		}
	}

	public void msg(Player player, String msg) {
		player.sendMessage("[MsBlock] - " + msg);
	}

	public void logMessage(String msg) {
		PluginDescriptionFile pdFile = this.getDescription();
		MsBlock.log.info(pdFile.getName() + " " + pdFile.getVersion() + ": " + msg);
	}	


	String capitalCase(String s) {
		return s.toUpperCase().charAt(0) + s.toLowerCase().substring(1);
	}


}
