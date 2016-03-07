/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.guntram.bukkit.crafttableautomation;


import de.guntram.bukkit.crafttableautomation.helpers.WorldLocationHelper;
import java.util.HashSet;
import java.util.logging.Level;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

/**
 *
 * @author gbl
 */
public class CraftTableAutomation extends JavaPlugin  {


    private HashSet<WorldLocationHelper>allWorkBenches;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        CTABlockListener blockListener=new CTABlockListener(this);
        getServer().getPluginManager().registerEvents(blockListener, this);
        allWorkBenches=new HashSet();
    }
    
    @Override
    public void onDisable() {
    }
    
    public void updateBlock(World world, Location location, Material material, CTABlockListener.UpdateType event) {
        if (material==Material.HOPPER || material==Material.WORKBENCH)
            getLogger().log(Level.INFO, "World "+world.getName()+" at "+location.getBlockX()+"/"+location.getBlockY()+
                "/"+location.getBlockZ()+" Material "+material+" by event "+event);
        
        if (material==Material.HOPPER && world.getBlockAt(location.add(0, 1, 0)).getType()==Material.WORKBENCH)
            changeWorkbenchAt(world, location.add(0, 1, 0), event);
        else if (material==Material.WORKBENCH && world.getBlockAt(location.subtract(0, 1, 0)).getType()==Material.HOPPER)
            changeWorkbenchAt(world, location, event);
    }
    
    private void changeWorkbenchAt(World world, Location loc, CTABlockListener.UpdateType event) {
        if (event==CTABlockListener.UpdateType.PLACE) {
            allWorkBenches.add(new WorldLocationHelper(world, loc));
        } else if (event==CTABlockListener.UpdateType.BREAK) {
            allWorkBenches.remove(new WorldLocationHelper(world, loc));
        }
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String commandName=command.getName();
        if (commandName.equalsIgnoreCase("cta")) {
            if (args.length==1 && args[0].equalsIgnoreCase("list")) {
                sender.sendMessage("§2"+allWorkBenches.size()+" Automatic Crafttables");
                for (WorldLocationHelper helper:allWorkBenches) {
                    sender.sendMessage(helper.toString());
                }
                return true;
            }
        }
        return false;
    }

/*    
    @EventHandler
    public void onWhatever(CraftItemEvent event) {            
//    public void onWhatever(PrepareItemCraftEvent event) {
        
        String name=event.getEventName();
        CraftingInventory inventory=event.getInventory();
        InventoryView view=event.getView();
        
        if (view.getType()==InventoryType.WORKBENCH) {
            Recipe recipe=event.getRecipe();
            ItemStack resultStack=recipe.getResult();
            int  amount    =resultStack.getAmount();
            int  durability=resultStack.getDurability();
            Material type  =resultStack.getType();
            getLogger().log(Level.INFO, "Crafted "+amount+" of "+type+" with durability "+durability);
            
            ItemStack[] sources=inventory.getContents();
            for (int i=0; i<sources.length; i++) {
                getLogger().log(Level.INFO, "Used "+sources[i].getAmount()+" of "+sources[i].getType()+" with durability "+sources[i].getDurability());
            }
            
            InventoryHolder holder=inventory.getHolder();
            if (holder==null)
                getLogger().log(Level.INFO, "No Holder");
            else
                getLogger().log(Level.INFO, "Holder is a "+holder.getClass().toString());
            
            CraftInventoryView cview=(CraftInventoryView)view;
            ContainerWorkbench workbench=(ContainerWorkbench) cview.getHandle();
            
            BlockPosition pos=null; //=workbench.h;
            World world=null; //=workbench.g;
            try {
                Field field=workbench.getClass().getDeclaredField("h");
                field.setAccessible(true);
                pos=(BlockPosition)field.get(workbench);
                
                field=workbench.getClass().getDeclaredField("g");
                field.setAccessible(true);
                world=(World) field.get(workbench);
            } catch (NoSuchFieldException ex) {
                getLogger().log(Level.SEVERE, null, ex);
            } catch (SecurityException ex) {
                getLogger().log(Level.SEVERE, null, ex);
            } catch (IllegalArgumentException ex) {
                getLogger().log(Level.SEVERE, null, ex);
            } catch (IllegalAccessException ex) {
                getLogger().log(Level.SEVERE, null, ex);
            }
            if (pos==null) {
                getLogger().log(Level.INFO, "workbench has no block position");
            } else {
                getLogger().log(Level.INFO, "workbench is at "+pos.getX()+"/"+pos.getY()+"/"+pos.getZ());
                Block block=world.getType(pos).getBlock();
                Block below;;
                if (block!=Blocks.CRAFTING_TABLE) {
                    getLogger().log(Level.INFO, "There is no crafting table there");
                } else if ((below=world.getType(pos.down()).getBlock())!=Blocks.HOPPER) {
                    getLogger().log(Level.INFO, "There is no hopper below");
                } else {
                    getLogger().log(Level.INFO, "AutoCraftTable set");
                }
            }
        }
    }
*/    

}
