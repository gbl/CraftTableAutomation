/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.guntram.bukkit.crafttableautomation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Hopper;
import org.bukkit.inventory.Inventory;
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
    
    private class HopperConfig {
        public int x, y, z;
        public BlockFace face;
        HopperConfig(int _x, int _y, int _z, BlockFace _face) {
            x=_x; y=_y; z=_z; face=_face;
        }
    }
    
    private class RemovalEntry {
        public int hopper, slot, amount;
        RemovalEntry(int _h, int _s, int _a) {
            hopper=_h; slot=_s; amount=_a;
        }
    }

    @Override
    public void run() {
        for (Map.Entry<Location, CraftTableConfiguration> entry : workBenches.entrySet()) {
            Location loc=entry.getKey();
            
            int x=loc.getBlockX();
            int y=loc.getBlockY();
            int z=loc.getBlockZ();
            World world=loc.getWorld();

            // Search the hopper below the table for a slot that can accept the
            // craft result. Store that slot number in receivingSlot. If we 
            // can't find a suitable slot, stop processing this table.
            Block blockBelow;
            if ((blockBelow=world.getBlockAt(x, y-1, z)).getType()!=Material.HOPPER) {
                plugin.getLogger().warning("No hopper below bench at "+x+"/"+(y-1)+"/"+z);
                continue;
            }
            CraftTableConfiguration config=entry.getValue();
            Hopper receivingHopper=(Hopper)(blockBelow.getState());
            Inventory receivingInventory=receivingHopper.getInventory();
            int maxStack=receivingInventory.getMaxStackSize();
            int receivingSlot=-1;
            
            for (int i=0; i<receivingInventory.getSize(); i++) {
                if (receivingInventory.getItem(i).getType()==config.get(0).getMaterial()
                &&  receivingInventory.getItem(i).getAmount()+config.get(0).getAmount() <= maxStack) {
                    receivingSlot=i;
                    break;
                }
            }
            if (receivingSlot==-1) {
                for (int i=0; i<receivingInventory.getSize(); i++) {
                    if (receivingInventory.getItem(i)==null) {
                        receivingSlot=i;
                        break;
                    }
                }
            }
            if (receivingSlot==-1)
                continue;

            RecipeComponent[] components=new RecipeComponent[config.size()-1];
            ArrayList<RemovalEntry>toRemove=new ArrayList<>();
            
            for (int i=0; i<config.size()-1; i++) {
                RecipeComponent configEntry = config.get(i+1);
                components[i]=new RecipeComponent(configEntry.getAmount(), configEntry.getMaterial(), configEntry.getSubtype());
            }

            HopperConfig[] hoppers={
                new HopperConfig(x+1, y, z, BlockFace.EAST),
                new HopperConfig(x-1, y, z, BlockFace.WEST),
                new HopperConfig(x, y, z+1, BlockFace.SOUTH),
                new HopperConfig(x, y, z-1, BlockFace.NORTH),
                new HopperConfig(x, y+1, z, BlockFace.DOWN),
            };
            if (processHoppers(world, hoppers, components, toRemove)) {
                plugin.getLogger().info("Requirements fulfilled at "+entry);
                for (RemovalEntry re:toRemove) {
                    plugin.getLogger().info("    remove "+re.amount+" from hopper "+re.hopper+" slot "+re.slot);
                }
            }
        };
    }

    public boolean processHoppers(World world, HopperConfig[] hc, RecipeComponent[] components, ArrayList<RemovalEntry>toRemove) {
        Block block;

        for (int i=0; i<hc.length; i++) {
            if ((block=world.getBlockAt(hc[i].x, hc[i].y, hc[i].z)).getType()!=Material.HOPPER)
                continue;
            Hopper hopper=(Hopper)(block.getState());
            int facing=block.getData();
            if (!(facing==2 && hc[i].face==BlockFace.SOUTH
              ||  facing==3 && hc[i].face==BlockFace.NORTH
              ||  facing==4 && hc[i].face==BlockFace.EAST
              ||  facing==5 && hc[i].face==BlockFace.WEST
              ||  facing==0 && hc[i].face==BlockFace.DOWN))
                continue;
            
            plugin.getLogger().info("found hopper at "+hc[i].x+"/"+hc[i].y+"/"+hc[i].z);
            Inventory inv=hopper.getInventory();
            for (RecipeComponent component:components) {
                for (int j=0; j<inv.getSize(); j++) {
                    ItemStack stack=inv.getItem(j);
                    if (stack!=null)
                        plugin.getLogger().info("  hopper has "+stack.getAmount()+
                                                " of "+stack.getType()+
                                                "/"+stack.getDurability()+
                                                " at "+j);
                    if (component==null)  {
                        plugin.getLogger().warning("Component is null??");
                        continue;
                    }
                    plugin.getLogger().info("  need "+component.getAmount()+
                                            " of "+component.getMaterial()+
                                            " subtype "+component.getSubtype());
                    if (stack!=null 
                    && component.getMaterial()==stack.getType()
                    && (component.getSubtype()==32767 || component.getSubtype()==stack.getDurability())) {
                        plugin.getLogger().info("  hopper has "+stack.getAmount()+
                                                " needed "+stack.getType()+
                                                "/"+stack.getDurability()+
                                                " at "+j);
                        int satisfyFromThis=Math.min(component.getAmount(), stack.getAmount());
                        component.setAmount(component.getAmount()-satisfyFromThis);
                        plugin.getLogger().info("satisfied "+satisfyFromThis+", still need "+component.getAmount());
                        toRemove.add(new RemovalEntry(i, j, satisfyFromThis));
                        // If we just set the last of the required
                        // amounts to 0, we're done.
                        if (component.getAmount()==0) {
                            boolean allNull=true;
                            for (int k=0; k<components.length; k++)
                                if (components[k].getAmount()!=0) {
                                    allNull=false;
                                    break;
                            }
                            if (allNull==true)
                                return true;
                            // If other components are still missing, we can at
                            // least stop processing this component.
                            break;
                        }
                    }
                }
            }
        }
        return false;
    }
}
