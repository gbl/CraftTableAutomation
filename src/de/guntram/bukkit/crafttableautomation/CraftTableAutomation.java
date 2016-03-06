/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.guntram.bukkit.crafttableautomation;

import java.lang.reflect.Field;
import java.util.logging.Level;
import net.minecraft.server.v1_9_R1.Block;
import net.minecraft.server.v1_9_R1.BlockPosition;
import net.minecraft.server.v1_9_R1.Blocks;
import net.minecraft.server.v1_9_R1.ContainerWorkbench;
import net.minecraft.server.v1_9_R1.World;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_9_R1.inventory.CraftInventoryView;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.plugin.java.JavaPlugin;

/**
 *
 * @author gbl
 */
public class CraftTableAutomation extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        saveDefaultConfig();
        getServer().getPluginManager().registerEvents(this, this);
    }    
    
    @Override
    public void onDisable() {
    }
    
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
}
