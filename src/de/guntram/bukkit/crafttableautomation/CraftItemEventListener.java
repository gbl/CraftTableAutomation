/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.guntram.bukkit.crafttableautomation;

import de.guntram.bukkit.crafttableautomation.helpers.CraftItemEventHelper;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_9_R1.inventory.CraftShapedRecipe;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.Recipe;

/**
 *
 * @author gbl
 */
public class CraftItemEventListener implements Listener {

    private final CraftTableAutomation plugin;
    private final CraftItemEventHelper helper;
    
    CraftItemEventListener(CraftTableAutomation p) {
        plugin=p;
        helper=CraftItemEventHelper.getInstance();
    }

    @EventHandler
    public void onWhatever(CraftItemEvent event) {            
//    public void onWhatever(PrepareItemCraftEvent event) {
        
        String name=event.getEventName();
        CraftingInventory inventory=event.getInventory();
        InventoryView view=event.getView();
        
        if (view.getType()==InventoryType.WORKBENCH) {
            Recipe recipe=event.getRecipe();
//            ItemStack resultStack=recipe.getResult();
//            int  amount    =resultStack.getAmount();
//            int  durability=resultStack.getDurability();
//            Material type  =resultStack.getType();
//            getLogger().log(Level.INFO, "Crafted "+amount+" of "+type+" with durability "+durability);
            
//            ItemStack[] sources=inventory.getContents();
//            for (ItemStack source : sources) {
//                getLogger().log(Level.INFO, "Used " + source.getAmount() + " of " + source.getType() + " with durability " + source.getDurability());
//            }
            
            InventoryHolder holder=inventory.getHolder();
            Player player=null;
            if (holder!=null)
                player=helper.getPlayer(holder);
//            if (holder==null)
//                getLogger().log(Level.INFO, "No Holder");
//            else
//                getLogger().log(Level.INFO, "Holder is a "+holder.getClass().toString());
            
            Location loc=helper.getInventoryViewLocation(view);
//            helper.getPlayer(holder).sendMessage(loc.toString());
            
            String feedback="I can't make sense of that recipe";
            if (recipe instanceof CraftShapedRecipe) {
                CraftTableConfiguration clone=CraftTableConfiguration.fromRecipe(recipe);
                if (plugin.configureBenchAt(loc, clone))
                    feedback="You configured your crafttable";
                else
                    feedback="This is not an automated crafttable";
            }
            if (player!=null)
                player.sendMessage(feedback);
        }
    }
}
