package timber.core;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class Plugin extends JavaPlugin implements Listener {
	public static HashSet<Material>	logMaterials;
	public static HashSet<Material>	axeMaterials;

	public void initializeHashSets()
	{
		String version = Bukkit.getVersion();
		if (version.contains("1.13") || version.contains("1.14"))
		{
			logMaterials = new HashSet<>(Arrays.asList(Material.getMaterial("ACACIA_LOG"), Material.getMaterial("BIRCH_LOG"), Material.getMaterial("DARK_OAK_LOG"), Material.getMaterial("JUNGLE_LOG"), Material.getMaterial("OAK_LOG"),
					Material.getMaterial("SPRUCE_LOG")));
			axeMaterials = new HashSet<>(Arrays.asList(Material.getMaterial("DIAMOND_AXE"), Material.getMaterial("GOLDEN_AXE"), Material.getMaterial("IRON_AXE"), Material.getMaterial("STONE_AXE"), Material.getMaterial("WOODEN_AXE")));
		} else
		{
			logMaterials = new HashSet<>(Arrays.asList(Material.getMaterial("LOG"), Material.getMaterial("LOG_2")));
			axeMaterials = new HashSet<>(Arrays.asList(Material.getMaterial("DIAMOND_AXE"), Material.getMaterial("GOLD_AXE"), Material.getMaterial("IRON_AXE"), Material.getMaterial("STONE_AXE"), Material.getMaterial("WOOD_AXE")));
		}
		Bukkit.getLogger().log(Level.INFO, "Timber Log Materials: " + logMaterials.toString());
		Bukkit.getLogger().log(Level.INFO, "Timber Axe Materials: " + axeMaterials.toString());
	}

	@Override
	public void onEnable()
	{
		initializeHashSets();
		getServer().getPluginManager().registerEvents(this, this);
	}

	@EventHandler
	public void onBlockBreak(BlockBreakEvent e)
	{
		Player player = e.getPlayer();
		if (!player.isSneaking())
		{
			if (!player.hasPermission("timber.disallow") || player.isOp())
			{
				@SuppressWarnings("deprecation")
				ItemStack handStack = player.getItemInHand();
				if (axeMaterials.contains(handStack.getType()))
				{
					Block block = e.getBlock();
					if (logMaterials.contains(block.getType()))
					{
						cutDownTree(block.getLocation(), player.getGameMode() == GameMode.CREATIVE ? handStack.clone() : handStack);
					}
				}
			}
		}
	}

	@SuppressWarnings("deprecation")
	private void cutDownTree(Location location, ItemStack handStack)
	{
		LinkedList<Block> blocks = new LinkedList<>();
		for (int i = location.getBlockY(); i < location.getWorld().getHighestBlockYAt(location.getBlockX(), location.getBlockZ());)
		{
			Location l = location.add(0.0D, 1.0D, 0.0D);
			Block block = l.getBlock();
			if (logMaterials.contains(block.getType()))
			{
				blocks.add(l.getBlock());
				l = null;
				i++;
			} else
			{
				break;
			}
		}
		for (Block block : blocks)
		{
			if (block.breakNaturally(handStack))
			{
				handStack.setDurability((short) (handStack.getDurability() + 1));
				if (handStack.getType().getMaxDurability() == handStack.getDurability())
				{
					handStack.setType(Material.AIR);
					return;
				}
			}
		}
		blocks = null;
	}

}
