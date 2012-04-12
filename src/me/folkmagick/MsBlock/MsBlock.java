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
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class MsBlock extends JavaPlugin implements Listener {

	public static MsBlock plugin;
	public static final Logger log = Logger.getLogger("Minecraft");

	public static Permission permission = null;

	public static int _MINDISTANCE = 5;
	public static int _MAXDISTANCE = 10;
	public int _SETDISTANCE = _MINDISTANCE;
	public static final List<String> denySpawn = Arrays.asList("enderdragon",
			"giant", "ghast", "irongolem", "magmacube", "slime");
	public static int _msbauto = 0;
	public static String _msbauto_type = "";

	public void onEnable() {
		if (setupPermission()) {

			PluginManager pm = this.getServer().getPluginManager();
			pm.registerEvents(new MsBlockListener(), this);

			this.logMessage("is now enabled!");
		}
	}

	public void onDisable() {
		this.logMessage("is now disabled!");
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd,
			String cmdLabel, String[] args) {

		if (!(sender instanceof Player)) {
			log.info("Must be a player, not a console command.");
			return true;
		}

		Player player = (Player) sender;

		boolean hasPerm = MsBlock.checkPermission(sender, "msblock.use");

		if (hasPerm) {

			if (cmdLabel.equalsIgnoreCase("msb")) {

				if (args.length == 0) {

					// scan max distance for block
					Block target = player.getTargetBlock(null, _MAXDISTANCE);

					if (target.getType() == Material.MOB_SPAWNER) {

						BlockState state = target.getState();
						if (state instanceof CreatureSpawner) {
							CreatureSpawner spawner = (CreatureSpawner) state;
							player.sendMessage("This is a ["
									+ spawner.getSpawnedType().getName()
									+ "] spawner.");
							player.sendMessage("Spawn delay: "
									+ spawner.getDelay());
							player.sendMessage("Location: (" + target.getX()
									+ ", " + target.getY() + ", "
									+ target.getZ() + ")");
						}

					} else {
						helpscreen(player);
					}

				} else if (args.length == 1 && args[0].equalsIgnoreCase("help")) {
					helpscreen(player);
				} else if (args.length == 1
						&& !args[0].equalsIgnoreCase("help")) {

					_SETDISTANCE = _MINDISTANCE;
					msSet(player, args[0], _SETDISTANCE);

				} else if (args.length == 2 && args[0] != "help") {

					if (args[1] != null) {
						int i = Integer.parseInt(args[1]);

						if (i >= _MINDISTANCE && i <= _MAXDISTANCE) {
							_SETDISTANCE = i;
						} else if (i > _MAXDISTANCE) {
							_SETDISTANCE = _MAXDISTANCE;
						} else if (i < _MINDISTANCE) {
							_SETDISTANCE = _MINDISTANCE;
						}
					}

					msSet(player, args[0], _SETDISTANCE);
				} else {
					helpscreen(player);
				}
			} // end msb command

			else if (cmdLabel.equalsIgnoreCase("msbauto")) {

				boolean hasAutoPerm = MsBlock.checkPermission(sender,
						"msblock.auto");

				if (hasAutoPerm) {

					if (args.length > 0) {

						if (args[0].equalsIgnoreCase("on")) {

							if (_msbauto < 1)
								_msbauto = 1;

							if (args[1] != null) {

								if (!denySpawn.contains(args[1].toLowerCase())) {
									_msbauto_type = args[1].toLowerCase();
									player.sendMessage("[MsBlock] Auto set, bound to type: "
											+ _msbauto_type);
								}

							}

						} // end auto on
						else if (args[0].equalsIgnoreCase("off")) {

							// turn auto off, and default type to blank
							_msbauto = 0;
							_msbauto_type = "";
							player.sendMessage("MsBlock auto disabled.");
						} else {
							return false;
						}

					} else {
						return false;

					}

				} // end hasAutoPerm
				else {
					player.sendMessage("You do not have MsBlock auto permission.");
				}

			} // end msbauto command
		} else {
			player.sendMessage("You do not have permission to do this.");
		}

		return true;
	}

	public static void msSet(Player player, String msType, int i) {

		Block target = player.getTargetBlock(null, i);

		if (target.getType() == Material.MOB_SPAWNER) {

			BlockState state = target.getState();
			if (state instanceof CreatureSpawner) {
				CreatureSpawner spawner = (CreatureSpawner) state;
				player.sendMessage("This is a ["
						+ spawner.getSpawnedType().getName() + "] spawner.");
			}

		} else {

			// ----------------
			if (denySpawn.contains(msType.toLowerCase())) {
				player.sendMessage(msType + " spawners are disabled here.");
			} else {
				// set block to spawner
				target.setType(Material.MOB_SPAWNER);

				// change spawner to type entered
				BlockState state = target.getState();
				if (state instanceof CreatureSpawner) {
					CreatureSpawner spawner = (CreatureSpawner) state;
					spawner.setCreatureTypeByName(msType.toUpperCase());
					player.sendMessage(msType + " spawner created at ("
							+ target.getX() + ", " + target.getY() + ", "
							+ target.getZ() + ")");
				}
			}
		}

	}

	public void helpscreen(Player player) {

		String help = "\n\nUsage: /msb <monster type> #distance\n";
		help += "---------------------------\n";
		help += "  Monster Types\n";
		help += "---------------------------\n";
		help += "\n";
		help += "Blaze, CaveSpider, Chicken, Creeper, Enderman, "
				+ "MushroomCow, Ocelot, Pig, PigZombie, Sheep, Silverfish, Skeleton, SnowMan "
				+ "Spider, Squid, Wolf, Zombie";

		sendMultilineMessage(player, help);
	}

	// ========================================================================
	// HELPERS

	public static boolean checkPermission(CommandSender sender,
			String permission) {
		if (MsBlock.permission != null) { // use Vault to check permissions -
			// actually does nothing different
			// than sender.hasPermission at the
			// moment, but might change
			try {
				return MsBlock.permission.has(sender, permission);
			} catch (NoSuchMethodError e) {
				// if for some reason there is a problem with Vault, fall back
				// to default permissions
				MsBlock.log
				.info("[MsBlock] Checking Vault permission threw an exception. Are you using the most recent version? Falling back to to default permission checking.");
			}
		}
		// fallback to default Bukkit permission checking system
		return sender.hasPermission(permission)
				|| sender.hasPermission("msblock.*");
	}

	private boolean setupPermission() {
		if (this.getServer().getPluginManager().getPlugin("Vault") != null) {
			RegisteredServiceProvider<Permission> permissionProvider = getServer()
					.getServicesManager().getRegistration(
							net.milkbowl.vault.permission.Permission.class);
			if (permissionProvider != null) {
				permission = permissionProvider.getProvider();
			}

			MsBlock.log.info("[MsBlock] Vault hooked as permission plugin.");
			return (permission != null);
		}
		permission = null; // if the plugin is reloaded during play, possibly
		// kill permissions
		MsBlock.log
		.info("[MsBlock] Vault plugin not found - defaulting to Bukkit permission system.");
		return false;
	}

	public void sendMultilineMessage(Player player, String message) {
		if (player != null && message != null && player.isOnline()) {
			String[] s = message.split("\n");
			for (String m : s) {
				player.sendMessage(m);
			}
		}
	}

	public void msg(Player player, String msg) {
		player.sendMessage("[MsBlock] - " + msg);
	}

	public void logMessage(String msg) {
		PluginDescriptionFile pdFile = this.getDescription();
		MsBlock.log.info(pdFile.getName() + " " + pdFile.getVersion() + ": "
				+ msg);
	}

	String capitalCase(String s) {
		return s.toUpperCase().charAt(0) + s.toLowerCase().substring(1);
	}

}
