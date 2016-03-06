/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.guntram.bukkit.crafttableautomation;

import java.util.HashMap;
import java.util.logging.Level;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
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
    public void onWhatever(PrepareItemCraftEvent event) {
        getLogger().log(Level.INFO, "Player "+event.getRecipe()+" crafting in "+event.getView().getType());
    }
}
