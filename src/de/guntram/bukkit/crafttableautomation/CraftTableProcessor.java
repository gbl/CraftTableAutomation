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
            CraftTableConfiguration config=entry.getValue();
            
            // Table not configured yet?
            if (config.size()<1)
                continue;
            
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

            Hopper receivingHopper=(Hopper)(blockBelow.getState());
            Inventory receivingInventory=receivingHopper.getInventory();
            int maxStack=receivingInventory.getMaxStackSize();
            int receivingSlot=-1;
            
            // First, check for a slot that we can add to.
            for (int i=0; i<receivingInventory.getSize(); i++) {
                ItemStack stack=receivingInventory.getItem(i);
                if (stack==null)
                    continue;
                if (stack.getType()==config.get(0).getMaterial()
                &&  stack.getDurability()==config.get(0).getSubtype()
                &&  stack.getAmount()+config.get(0).getAmount() <= maxStack) {
                    receivingSlot=i;
                    break;
                }
            }
            // If nothing found, search for an empty slot.
            if (receivingSlot==-1) {
                for (int i=0; i<receivingInventory.getSize(); i++) {
                    if (receivingInventory.getItem(i)==null) {
                        receivingSlot=i;
                        break;
                    }
                }
            }
            plugin.getLogger().info("Receiving slot is "+receivingSlot);
            if (receivingSlot==-1)
                continue;

            // Copy the recipe to a temporary array that holds how many items
            // of which type we need. 
            RecipeComponent[] components=new RecipeComponent[config.size()-1];
            ArrayList<RemovalEntry>toRemove=new ArrayList<>();
            
            for (int i=0; i<config.size()-1; i++) {
                RecipeComponent configEntry = config.get(i+1);
                components[i]=new RecipeComponent(configEntry.getAmount(), configEntry.getMaterial(), configEntry.getSubtype());
            }

            // Search hoppers around the table
            // for these items. If we don't have enough input materials, 
            // we're done.
            HopperConfig[] hoppers={
                new HopperConfig(x+1, y, z, BlockFace.EAST),
                new HopperConfig(x-1, y, z, BlockFace.WEST),
                new HopperConfig(x, y, z+1, BlockFace.SOUTH),
                new HopperConfig(x, y, z-1, BlockFace.NORTH),
                new HopperConfig(x, y+1, z, BlockFace.DOWN),
            };
            if (!processHoppers(world, hoppers, components, toRemove))
                continue;
            
            plugin.getLogger().info("Requirements fulfilled at "+entry);
            ItemStack stack;
            if ((stack=receivingInventory.getItem(receivingSlot))==null) {
                plugin.getLogger().info("    create new itemstack at "+receivingSlot);
                receivingInventory.setItem(receivingSlot, new ItemStack(config.get(0).getMaterial(), config.get(0).getAmount(), (short) config.get(0).getSubtype()));
            } else {
                plugin.getLogger().info("    incrementing at "+receivingSlot);
                stack.setAmount(stack.getAmount()+config.get(0).getAmount());
                // receivingInventory.setItem(receivingSlot, stack); // is this neccesary?
            }
            
            removeInput(world, hoppers, toRemove);
        }
    }
    
    public void removeInput(World world, HopperConfig[] hc, ArrayList<RemovalEntry> toRemove) {
        for (RemovalEntry re:toRemove) {
            plugin.getLogger().info("    remove "+re.amount+" from hopper "+re.hopper+" slot "+re.slot);
            
            Block block=world.getBlockAt(hc[re.hopper].x, hc[re.hopper].y, hc[re.hopper].z);
            if (block.getType()!=Material.HOPPER) {
                plugin.getLogger().severe("Block stopped being a hopper");
                continue;
            }
            Hopper hopper=(Hopper)(block.getState());
            Inventory inv=hopper.getInventory();
            ItemStack stack=inv.getItem(re.slot);
            if (stack==null) {
                plugin.getLogger().severe("Stack suddenly disappeared");
                continue;
            }
            if (stack.getAmount()<=re.amount) {
                plugin.getLogger().info("Removed last item");
                inv.setItem(re.slot, null);
            } else {
                plugin.getLogger().info("Removing "+re.amount+" items");
                stack.setAmount(stack.getAmount()-re.amount);
                // inv.setItem(re.slot, stack); // needed?
            }
        }
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
