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
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

@SuppressWarnings("deprecation")
public class Plugin extends JavaPlugin implements Listener {
	public static HashSet<String>	validLogMaterials		= new HashSet<>(Arrays.asList("LOG", "LOG_2", "LEGACY_LOG", "LEGACY_LOG_2", "ACACIA_LOG", "BIRCH_LOG", "DARK_OAK_LOG", "JUNGLE_LOG", "OAK_LOG", "SPRUCE_LOG"));
	public static HashSet<String>	validAxeMaterials		= new HashSet<>(Arrays.asList("DIAMOND_AXE", "GOLDEN_AXE", "IRON_AXE", "STONE_AXE", "WOODEN_AXE", "GOLD_AXE", "WOOD_AXE"));
	public static HashSet<Material>	logMaterials			= new HashSet<>();
	public static HashSet<Material>	axeMaterials			= new HashSet<>();
	public static boolean			reverseSneakFunction	= false;

	public void initializeHashSets()
	{
		for (Material material : Material.values())
		{
			if (validLogMaterials.contains(material.name()))
			{
				logMaterials.add(material);
			}
			if (validAxeMaterials.contains(material.name()))
			{
				axeMaterials.add(material);
			}
		}
		Bukkit.getLogger().log(Level.INFO, "Timber Log Materials: " + logMaterials.toString());
		Bukkit.getLogger().log(Level.INFO, "Timber Axe Materials: " + axeMaterials.toString());
	}

	@Override
	public void onEnable()
	{
		saveDefaultConfig();
		reverseSneakFunction = getConfig().getBoolean("reverseSneakFunction");
		initializeHashSets();
		getServer().getPluginManager().registerEvents(this, this);
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onBlockBreak(BlockBreakEvent e)
	{
		Player player = e.getPlayer();
		boolean check = !player.isSneaking();
		if (reverseSneakFunction)
		{
			check = !check;
		}
		if (check)
		{
			if (!player.hasPermission("timber.disallow") || player.isOp())
			{
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
