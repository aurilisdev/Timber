package timber.core;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

public class Plugin extends JavaPlugin implements Listener {
	@SuppressWarnings("deprecation")
	public static HashSet<Material>	logMaterials	= new HashSet<>(
			Arrays.asList(Material.LEGACY_LOG, Material.LEGACY_LOG_2, Material.ACACIA_LOG, Material.BIRCH_LOG, Material.DARK_OAK_LOG, Material.JUNGLE_LOG, Material.OAK_LOG, Material.SPRUCE_LOG));
	public static HashSet<Material>	axeMaterials	= new HashSet<>(Arrays.asList(Material.DIAMOND_AXE, Material.GOLDEN_AXE, Material.IRON_AXE, Material.STONE_AXE, Material.WOODEN_AXE));

	@Override
	public void onEnable()
	{
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
				ItemStack handStack = player.getInventory().getItemInMainHand();
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
				ItemMeta meta = handStack.getItemMeta();
				if (meta != null)
				{
					Damageable damage = (Damageable) meta;
					damage.setDamage(damage.getDamage() + 1);
					handStack.setItemMeta(meta);
					if (handStack.getType().getMaxDurability() == damage.getDamage())
					{
						handStack.setType(Material.AIR);
						return;
					}
				}
			}
		}
		blocks = null;
	}

}
