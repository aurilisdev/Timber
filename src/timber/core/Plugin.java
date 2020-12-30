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
	public static HashSet<String> 	validLeaveMaterials 	= new HashSet<>(Arrays.asList("ACACIA_LEAVES", "BIRCH_LEAVES", "DARK_OAK_LEAVES", "JUNGLE_LEAVES", "LEGACY_LEAVES", "LEGACY_LEAVES_2", "OAK_LEAVES", "SPRUCE_LEAVES"));
	public static HashSet<Material>	logMaterials			= new HashSet<>();
	public static HashSet<Material>	axeMaterials			= new HashSet<>();
	public static HashSet<Material>	leaveMaterials			= new HashSet<>();
	public static boolean			reverseSneakFunction	= false;
	public static boolean 			distanceLimit			= true;
	public static int				maxBlocksInTree 		= 1500;

	/**
	 * Init all hash sets
	 */
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
			if(validLeaveMaterials.contains(material.name())) {
				leaveMaterials.add(material);
			}
		}
		Bukkit.getLogger().log(Level.INFO, "Timber Log Materials: " + logMaterials.toString());
		Bukkit.getLogger().log(Level.INFO, "Timber Axe Materials: " + axeMaterials.toString());
		Bukkit.getLogger().log(Level.INFO, "Timber Leave Materials: " + leaveMaterials.toString());
		Bukkit.getLogger().log(Level.INFO, "Sneak only: " + reverseSneakFunction);
		Bukkit.getLogger().log(Level.INFO, "distanceLimit: " + distanceLimit);
		Bukkit.getLogger().log(Level.INFO, "maxBlocksInTree: " + maxBlocksInTree);
		
	}
	
	/**
	 * If plugin is enabled
	 */
	@Override
	public void onEnable()
	{
		saveDefaultConfig();
		reverseSneakFunction = getConfig().getBoolean("reverseSneakFunction");
		maxBlocksInTree = getConfig().getInt("maxBlocksInTree");
		if(maxBlocksInTree == -1) distanceLimit = false;
		initializeHashSets();
		getServer().getPluginManager().registerEvents(this, this);
	}

	/**
	 * Handle the block break event
	 * @param e
	 */
	@EventHandler(priority = EventPriority.MONITOR)
	public void onBlockBreak(BlockBreakEvent e)
	{
		Player player = e.getPlayer();
		boolean check = reverseSneakFunction ? player.isSneaking() : !player.isSneaking();
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
	
	/**
	 * Finds all blocks in a cube around the block
	 * @param location of the block
	 * @return All connected blocks
	 */
	private HashSet<Location> findConnectedBlocks(Location location){
		HashSet<Location> blocks = new HashSet<>(); 
		int xpos = location.getBlockX();
		int ypos = location.getBlockY();
		int zpos = location.getBlockZ();
		for(int x = xpos - 1; x < xpos + 2; x++) {
			for(int y = ypos - 1; y < ypos + 2; y++) {
				for(int z = zpos - 1; z < zpos + 2; z++) {
					// Do not add the block itself
					if(x == xpos && y == ypos && z == zpos) continue;
					Block b = location.getWorld().getBlockAt(x, y, z);
					// Add to the set if its part of the tree
					if(logMaterials.contains(b.getType()) || leaveMaterials.contains(b.getType())) {
						Location l = new Location(location.getWorld(), (double)x, (double)y, (double)z);
						blocks.add(l);
					}
					
				}
			}
		}
		return blocks;
	}
	
	/**
	 * Find all blocks that are part of a tree
	 * @param location of the mineed block
	 * @return All blocks of the tree
	 */
	private LinkedList<Location> findTreeComponents(Location location){
		// HashSet that contains all blocks of the tree
		LinkedList<Location> tree = new LinkedList<>();
		LinkedList<Location> list = new LinkedList<Location>();
		list.add(location);
		boolean stopAddingBlocks = false;
		while(!list.isEmpty()) {
			Location l = list.remove(0);
			if(l == null) break;
			tree.add(l);
			if(!stopAddingBlocks) {
				HashSet<Location> locs = findConnectedBlocks(l);
				for(Location loc : locs) {
					if(!tree.contains(loc) && !list.contains(loc)) {
						list.add(loc);
					}
				}
			}
			// Stop the iteration if maxBlocksInTree have been found
			if(distanceLimit && (list.size() + tree.size() > maxBlocksInTree)) { 
				tree.addAll(list);
				break;
			}
		}
		return tree;
	}
	
	/**
	 * Check if the mined block is part of a tree.
	 * @param tree The HashSet that contains all tree blocks
	 * @return If the set is a tree
	 */
	private boolean isTree(LinkedList<Location> tree) {
		for(Location l : tree) {
			if(leaveMaterials.contains(l.getBlock().getType())) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Cut down the tree
	 * @param location of the first mined block
	 * @param handStack Item the player is holding in hand
	 */
	private void cutDownTree(Location location, ItemStack handStack)
	{
		Block b = location.getBlock();
		// Only execute function if block is log
		if(!logMaterials.contains(b.getType())) return;
		handStack.getDurability();
		LinkedList<Location> tree = findTreeComponents(location);
		// Check if block is a tree and not part of a building
		if(!isTree(tree)) {
			location.getBlock().breakNaturally();
			return;
		}
		
		for(Location l : tree) {
			Block block = l.getBlock();
			Material material = block.getType();
			// Break leaves without costing durability
			if(leaveMaterials.contains(material)) {
				block.breakNaturally(handStack);
				continue;
			}
			if(block.breakNaturally(handStack)) {
				handStack.setDurability((short) (handStack.getDurability() + 1));
				if (handStack.getType().getMaxDurability() < handStack.getDurability())
				{
					// Break item
					handStack.setAmount(0);
					return;
				}
			}
		}
	}

}
