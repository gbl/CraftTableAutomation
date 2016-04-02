/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.guntram.bukkit.crafttableautomation;

import java.util.HashMap;
import java.util.Map;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Hopper;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author gbl
 */
public class CraftTableProcessor implements Runnable {
    
    private final CraftTableAutomation plugin;
    private final Map<Location, CraftTableConfiguration> workBenches;

    CraftTableProcessor(CraftTableAutomation cta, HashMap<Location, CraftTableConfiguration> allWorkBenches) {
        plugin=cta;
        workBenches=allWorkBenches;
    }

    @Override
    public void run() {
        for (Map.Entry<Location, CraftTableConfiguration> entry : workBenches.entrySet()) {
            Location loc=entry.getKey();
            CraftTableConfiguration config=entry.getValue();
            int x=loc.getBlockX();
            int y=loc.getBlockY();
            int z=loc.getBlockZ();
            World world=loc.getWorld();
            processHopper(world, x+1, y, z, BlockFace.EAST,  config);
            processHopper(world, x-1, y, z, BlockFace.WEST,  config);
            processHopper(world, x, y, z+1, BlockFace.SOUTH, config);
            processHopper(world, x, y, z-1, BlockFace.NORTH, config);
            processHopper(world, x, y+1, z, BlockFace.DOWN,  config);
        };
    }

    public void processHopper(World world, int x, int y, int z, BlockFace face, CraftTableConfiguration config) {
        Block block;
        if ((block=world.getBlockAt(x, y, z)).getType()!=Material.HOPPER)
            return;
        Hopper hopper=(Hopper)(block.getState());
        int facing=block.getData();
        if (facing==2 && face==BlockFace.SOUTH
        ||  facing==3 && face==BlockFace.NORTH
        ||  facing==4 && face==BlockFace.EAST
        ||  facing==5 && face==BlockFace.WEST
        ||  facing==0 && face==BlockFace.DOWN) {
            plugin.getLogger().info("found hopper at "+x+"/"+y+"/"+z);
            Inventory inv=hopper.getInventory();
            for (int i=1; i<config.size(); i++) {
                RecipeComponent component=config.get(i);
                for (int j=0; j<inv.getSize(); j++) {
                    ItemStack stack=inv.getItem(j);
                    if (stack!=null && stack.getType()==component.getMaterial())
                        plugin.getLogger().info("  hopper has "+stack.getAmount()+" needed "+stack.getType()+ "at "+j);
                }
            }
        }
    }
}
