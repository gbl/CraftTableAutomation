/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.guntram.bukkit.crafttableautomation;

import de.guntram.bukkit.crafttableautomation.helpers.CraftItemEventHelper;
import java.util.ArrayList;
import java.util.Map;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;

/**
 *
 * @author gbl
 */
public class RecipeComponent {
    private final int amount;
    private final Material material;
    private final int subtype;
    
    RecipeComponent(int amount, Material material, int subtype) {
        this.amount=amount;
        this.material=material;
        this.subtype=subtype;
    }
    int         getAmount()     { return amount; }
    Material    getMaterial()   { return material; }
    int         getSubtype()    { return subtype; }
    
    @Override
    public String toString() {
        return getAmount()+","+material+","+subtype;
    }
    
    public static RecipeComponent fromString(String s) {
        int amount;
        Material material;
        int subtype;
        String[] parts=s.split(",");
        if (parts.length!=2)
            throw new IllegalArgumentException("not 2 components");
        amount=Integer.parseInt(parts[0]);
        material=Material.getMaterial(parts[1]);
        subtype=Integer.parseInt(parts[2]);
        return new RecipeComponent(amount, material, subtype);
    }
}
