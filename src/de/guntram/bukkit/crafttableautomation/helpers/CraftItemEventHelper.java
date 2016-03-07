/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.guntram.bukkit.crafttableautomation.helpers;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.InventoryView;

/**
 *
 * @author gbl
 */
public interface CraftItemEventHelper {
    public Location getInventoryViewLocation(InventoryView view);
    public Player getPlayer(InventoryHolder holder);
}
