/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.guntram.bukkit.crafttableautomation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Hopper;
import org.bukkit.block.Sign;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author gbl
 */
public class CraftTableProcessor implements Runnable {
    
    private final CraftTableAutomation plugin;
    private final Map<Location, CraftTableConfiguration> workBenches;
    private final long logInterval;
    private long nextLogTime;
    private long accumulatedTime;
    private int skippedChunks, processedChunks, producedItems;

    CraftTableProcessor(CraftTableAutomation cta, HashMap<Location, CraftTableConfiguration> allWorkBenches) {
        plugin=cta;
        workBenches=allWorkBenches;
        logInterval=cta.getConfig().getLong("loginterval", 600)*1000; // 1000 ms per sec
        nextLogTime=System.currentTimeMillis()+logInterval;
        accumulatedTime=skippedChunks=processedChunks=producedItems=0;
    }
    
    private class BlockPositionDirection {
        public int x, y, z;
        public BlockFace face;
        BlockPositionDirection(int _x, int _y, int _z, BlockFace _face) {
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
        long startTime=System.currentTimeMillis();

        for (Map.Entry<Location, CraftTableConfiguration> entry : workBenches.entrySet()) {
            CraftTableConfiguration config=entry.getValue();
            
            // Table not configured yet?
            if (config.size()<1)
                continue;
            
            Location loc=entry.getKey();
            // if (!(loc.getChunk().isLoaded())) {
            if (!(loc.getWorld().isChunkLoaded(loc.getBlockX()>>4, loc.getBlockZ()>>4))) {
                skippedChunks++;
                continue;
            }
            processedChunks++;
            
            int x=loc.getBlockX();
            int y=loc.getBlockY();
            int z=loc.getBlockZ();
            World world=loc.getWorld();

            // Search the hopper below the table for a slot that can accept the
            // craft result. Store that slot number in receivingSlot. If we 
            // can't find a suitable slot, stop processing this table.
            Block blockBelow;
            if ((blockBelow=world.getBlockAt(x, y-1, z)).getType()!=Material.HOPPER) {
                // Removed spam that can happen when the hopper is destroyed by lava or explosions
                // plugin.getLogger().log(Level.WARNING, "No hopper below bench at {0}/{1}/{2}", new Object[]{x, y-1, z});
                // but be nice and do NOT make the CT a non-auto-CT
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
            plugin.getLogger().log(Level.FINE, "Receiving slot is {0}", receivingSlot);
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
            BlockPositionDirection[] neighbors={
                new BlockPositionDirection(x+1, y, z, BlockFace.EAST),
                new BlockPositionDirection(x-1, y, z, BlockFace.WEST),
                new BlockPositionDirection(x, y, z+1, BlockFace.SOUTH),
                new BlockPositionDirection(x, y, z-1, BlockFace.NORTH),
                new BlockPositionDirection(x, y+1, z, BlockFace.DOWN),
            };
            if (!processHoppers(world, neighbors, components, toRemove))
                continue;
            
            plugin.getLogger().log(Level.FINE, "Requirements fulfilled at {0}", entry);
            ItemStack stack;
            if ((stack=receivingInventory.getItem(receivingSlot))==null) {
                plugin.getLogger().log(Level.FINER, "    create new itemstack at {0}", receivingSlot);
                receivingInventory.setItem(receivingSlot, new ItemStack(config.get(0).getMaterial(), config.get(0).getAmount(), (short) config.get(0).getSubtype()));
            } else {
                plugin.getLogger().log(Level.FINER, "    incrementing at {0}", receivingSlot);
                stack.setAmount(stack.getAmount()+config.get(0).getAmount());
                // receivingInventory.setItem(receivingSlot, stack); // is this neccesary?
            }
            removeInput(world, neighbors, toRemove);
            producedItems++;
            config.setProducedItems(config.getProducedItems()+1);
            updateStatus(world, neighbors, ""+config.get(0).getMaterial(), config.getProducedItems());
        }
        long endTime=System.currentTimeMillis();
        accumulatedTime+=endTime-startTime;
                
        if (logInterval>0 && endTime > nextLogTime) {
            plugin.getLogger().log(Level.INFO, "Used {0} Milliseconds in {4} seconds to process {1} tables, produced {2} items, skipped {3} due to unloaded chunks", 
                new Object[]{accumulatedTime, processedChunks, producedItems, skippedChunks, logInterval/1000});
            accumulatedTime=skippedChunks=processedChunks=producedItems=0;
            nextLogTime=endTime+logInterval;
        }
        
    }
    
    private void updateStatus(World world, BlockPositionDirection[] neighbors, String itemName, long itemcount) {
        for (int i=0; i<neighbors.length; i++) {
            Block neighborBlock=world.getBlockAt(neighbors[i].x, neighbors[i].y, neighbors[i].z);
            if (neighborBlock.getType()!=Material.WALL_SIGN)
                continue;
            @SuppressWarnings("deprecation")
            int facing=neighborBlock.getData();
            
            // Unfortunately, these are swapped compared to the hopper stuff ...
            if (!(facing==2 && neighbors[i].face==BlockFace.NORTH
              ||  facing==3 && neighbors[i].face==BlockFace.SOUTH
              ||  facing==4 && neighbors[i].face==BlockFace.WEST
              ||  facing==5 && neighbors[i].face==BlockFace.EAST
              ||  facing==0 && neighbors[i].face==BlockFace.UP)) {
                plugin.getLogger().log(Level.FINE, "ignoring sign facing wrong");
                continue;
            }
            plugin.getLogger().log(Level.FINE, "updating sign");
            
            Sign sign=(Sign) neighborBlock.getState();
            sign.setLine(0, "Automatic table");
            sign.setLine(1, "");
            sign.setLine(2, itemName);
            sign.setLine(3, ""+itemcount);
            sign.update(false, false);
        }
    }
    
    private void removeInput(World world, BlockPositionDirection[] neighbors, ArrayList<RemovalEntry> toRemove) {
        for (RemovalEntry re:toRemove) {
            plugin.getLogger().log(Level.FINE, "    remove {0} from hopper {1} slot {2}", new Object[]{re.amount, re.hopper, re.slot});
            
            Block block=world.getBlockAt(neighbors[re.hopper].x, neighbors[re.hopper].y, neighbors[re.hopper].z);
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
                plugin.getLogger().finer("Removed last item");
                inv.setItem(re.slot, null);
            } else {
                plugin.getLogger().log(Level.FINER, "Removing {0} items", re.amount);
                stack.setAmount(stack.getAmount()-re.amount);
                // inv.setItem(re.slot, stack); // needed?
            }
        }
    }

    private boolean processHoppers(World world,
            BlockPositionDirection[] neighbors,
            RecipeComponent[] components,
            ArrayList<RemovalEntry>toRemove) {
        Block neighborBlock;
        Material neighborMaterial;

        for (int i=0; i<neighbors.length; i++) {
            neighborBlock=world.getBlockAt(neighbors[i].x, neighbors[i].y, neighbors[i].z);
            neighborMaterial=neighborBlock.getType();
            
            if (neighborMaterial != Material.HOPPER)
                continue;

            // Check if the hopper emits into the CT / the sign is attached to the CT
            @SuppressWarnings("deprecation")
            int facing=neighborBlock.getData();
            
            if (!(facing==2 && neighbors[i].face==BlockFace.SOUTH
              ||  facing==3 && neighbors[i].face==BlockFace.NORTH
              ||  facing==4 && neighbors[i].face==BlockFace.EAST
              ||  facing==5 && neighbors[i].face==BlockFace.WEST
              ||  facing==0 && neighbors[i].face==BlockFace.DOWN))
                continue;
            
            Hopper hopper=(Hopper)(neighborBlock.getState());
            plugin.getLogger().log(Level.FINE, "found hopper at {0}/{1}/{2}", new Object[]{neighbors[i].x, neighbors[i].y, neighbors[i].z});
            Inventory inv=hopper.getInventory();
            for (RecipeComponent component:components) {
                for (int j=0; j<inv.getSize(); j++) {
                    ItemStack stack=inv.getItem(j);
                    if (stack!=null)
                        plugin.getLogger().log(Level.FINER, "  hopper has {0} of {1}/{2} at {3}", new Object[]{stack.getAmount(), stack.getType(), stack.getDurability(), j});
                    if (component==null)  {
                        plugin.getLogger().warning("Component is null??");
                        continue;
                    }
                    plugin.getLogger().log(Level.FINER, "  need {0} of {1} subtype {2}", new Object[]{component.getAmount(), component.getMaterial(), component.getSubtype()});
                    if (stack!=null 
                    && component.getMaterial()==stack.getType()
                    && (component.getSubtype()==32767 || component.getSubtype()==stack.getDurability())) {
                        plugin.getLogger().log(Level.FINER, "  hopper has {0} needed {1}/{2} at {3}", new Object[]{stack.getAmount(), stack.getType(), stack.getDurability(), j});
                        int satisfyFromThis=Math.min(component.getAmount(), stack.getAmount());
                        component.setAmount(component.getAmount()-satisfyFromThis);
                        plugin.getLogger().log(Level.FINE, "satisfied {0}, still need {1}", new Object[]{satisfyFromThis, component.getAmount()});
                        toRemove.add(new RemovalEntry(i, j, satisfyFromThis));
                        // If we just set the last of the required
                        // amounts to 0, we're done.
                        if (component.getAmount()==0) {
                            boolean allNull=true;
                            for (RecipeComponent testcomp : components) {
                                if (testcomp.getAmount() != 0) {
                                    allNull=false;
                                    break;
                                }
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
