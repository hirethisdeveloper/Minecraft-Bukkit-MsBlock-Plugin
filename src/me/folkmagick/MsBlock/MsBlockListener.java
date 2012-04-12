package me.folkmagick.MsBlock;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class MsBlockListener implements Listener {

	public static MsBlock plugin;

	@EventHandler
	public void onBlockBreak(BlockBreakEvent event) {

		if (MsBlock._msbauto == 1) {

			event.setCancelled(true);

			Player player = (Player) event.getPlayer();

			MsBlock.msSet(player, MsBlock._msbauto_type, MsBlock._MAXDISTANCE);

		}

	}

}
