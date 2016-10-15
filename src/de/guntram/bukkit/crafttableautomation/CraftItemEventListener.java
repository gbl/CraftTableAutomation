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
            ConfigureBenchResult result;
            CraftTableConfiguration clone=CraftTableConfiguration.fromRecipe(recipe);
            if (player==null) {
                feedback=getFeedback("playerunknown");  // which is nice but how to send it ??
            } else if (clone==null) {
                feedback=getFeedback("nmsunsupported");
            } else  if ((result=ctaPlugin.configureBenchAt(loc, clone, player))==ConfigureBenchResult.OK) {
                feedback=getFeedback("configuredok");
            } else if (result==ConfigureBenchResult.NOWORKBENCHATTHISLOC) {           // not a automated table
                feedback=getFeedback("nocrafttablehere");
            } else if (result==ConfigureBenchResult.NOPERMISSION) {
                feedback=getFeedback("nopermission");
            } else if (result==ConfigureBenchResult.NOTINCLAIM) {
                feedback=getFeedback("notinclaim");
            } else {
                feedback=getFeedback("unknownerror");
            }
            if (player!=null && feedback!=null)
                player.sendMessage(feedback);
        }
    }

    String getFeedback(String key) {
        String result;
        result=ctaPlugin.getConfig().getString("messages."+key, "Error: "+key);
        if (result==null || result.isEmpty())
            return null;
        return result;
    }
}