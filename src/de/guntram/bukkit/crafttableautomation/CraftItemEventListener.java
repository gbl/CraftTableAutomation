/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.guntram.bukkit.crafttableautomation;

import de.guntram.bukkit.crafttableautomation.helpers.CraftItemEventHelper;
import de.guntram.bukkit.crafttableautomation.helpers.CraftItemEventHelperFactory;
import org.bukkit.Location;
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

    private final CraftTableAutomation ctaPlugin;
    private final CraftItemEventHelper helper;
    
    CraftItemEventListener(CraftTableAutomation p) {
        ctaPlugin=p;
        helper=CraftItemEventHelperFactory.getInstance();
    }

    @EventHandler
    public void onPlayerCraftedItem(CraftItemEvent event) {            
        
        String name=event.getEventName();
        CraftingInventory inventory=event.getInventory();
        InventoryView view=event.getView();
        
        // If we don't have a fitting NMS code, do nothing.
        if (helper==null)
            return;
        
        if (view.getType()==InventoryType.WORKBENCH) {
            Recipe recipe=event.getRecipe();

            InventoryHolder holder=inventory.getHolder();
            Player player=null;
            if (holder!=null)
                player=helper.getPlayer(holder);
            
            Location loc=helper.getInventoryViewLocation(view);

            String feedback;
            int result;
            CraftTableConfiguration clone=CraftTableConfiguration.fromRecipe(recipe);
            if (player==null) {
                feedback="I don't know who you are!";  // which is nice but how to send it ??
            } else if (clone==null) {
                feedback="Failed to get the recipe from this workbench - probably missing support for this MC version";
            } else  if ((result=ctaPlugin.configureBenchAt(loc, clone, player))==0)
                feedback="You configured your crafttable";
            else if (result==1) {           // not a automated table
                feedback=null;
            } else if (result==2) {
                feedback="You may not configure auto craft tables (need cta.use)";
            } else {
                feedback="I can't make sense of that recipe";
            }
            if (player!=null && feedback!=null)
                player.sendMessage(feedback);
        }
    }
}
