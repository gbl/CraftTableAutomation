/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.guntram.bukkit.crafttableautomation.helpers;

import java.util.Map;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;

/**
 *
 * @author gbl
 */
public abstract class CraftItemEventHelper {
    static CraftItemEventHelper instance;
    
    public abstract Location getInventoryViewLocation(InventoryView view);
    public abstract Player getPlayer(InventoryHolder holder);
    public abstract Map<Character, ItemStack> getIngredientMap(Recipe recipe);
    
    public static CraftItemEventHelper getInstance() {
        if (instance==null)
            instance=new CraftItemEventHelper_1_9_R1();
        return instance;
    }
}
