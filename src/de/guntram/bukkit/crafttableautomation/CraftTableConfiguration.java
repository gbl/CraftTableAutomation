/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.guntram.bukkit.crafttableautomation;

import de.guntram.bukkit.crafttableautomation.helpers.CraftItemEventHelperFactory;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;

/**
 *
 * @author gbl
 */
public class CraftTableConfiguration {
    private RecipeComponent[] components;
    private long producedItems;
    private String owner;
    
    public CraftTableConfiguration(int size) {
        components=new RecipeComponent[size];
    }

    static CraftTableConfiguration fromRecipe(Recipe recipe) {
        
        ArrayList<RecipeComponent> list=new ArrayList<>();
        ItemStack stack=recipe.getResult();
        list.add(new RecipeComponent(stack.getAmount(), stack.getType(), stack.getDurability()));

        Collection<ItemStack> ingredients=CraftItemEventHelperFactory.getInstance().getIngredientCollection(recipe);
        if (ingredients==null) {
            Bukkit.getLogger().log(Level.WARNING, "ingredients is null");
            Bukkit.getLogger().log(Level.WARNING, "Crafting: "+stack.getType().toString());
            return null;
        }
        if (ingredients.isEmpty()) {
            Bukkit.getLogger().log(Level.WARNING, "ingredients is empty");
            Bukkit.getLogger().log(Level.WARNING, "Crafting: "+stack.getType().toString());
            return null;
        }

        for (ItemStack ingredient: ingredients) {
            stack=ingredient;               // to be refactored; remains from old code
            if (stack!=null) {
                // try to find an existing entry that has the same material/subtype combination, and merge with it
                for (int i=1; i<list.size(); i++) {
                    RecipeComponent present = list.get(i);
                    if (present.getMaterial()==stack.getType()
                    &&  present.getSubtype()==stack.getDurability()) {
                        list.set(i, new RecipeComponent(present.getAmount()+stack.getAmount(), stack.getType(), stack.getDurability()));
                        stack=null;
                        break;
                    }
                }
                if (stack!=null)
                  list.add(new RecipeComponent(stack.getAmount(), stack.getType(), stack.getDurability()));
            }
        }

        CraftTableConfiguration result=new CraftTableConfiguration(list.size());
        result.components=list.toArray(result.components);
        return result;
    }
    
    RecipeComponent get(int i) { return components[i]; }
    int size() { return components.length; }
    long getProducedItems() { return producedItems; }
    void setProducedItems(long l) { producedItems=l; }
    String getOwner() { return owner==null ? "" : owner; }
    void setOwner(String s) { owner=s; }
    
    @Override
    public String toString() {
        StringBuilder buf=new StringBuilder();
        buf.append(components.length);
        for (RecipeComponent component:components)
            buf.append(";").append(component.toString());
        return buf.toString();
    }
    
    static public CraftTableConfiguration fromString(String s) {
        String[] parts=s.split(";");
        int size=Integer.parseInt(parts[0]);
        if (parts.length != size+1)
            throw new IllegalArgumentException("not a valid config string");
        CraftTableConfiguration result=new CraftTableConfiguration(size);
        for (int i=1; i<=size; i++) {
            result.components[i-1]=RecipeComponent.fromString(parts[i]);
        }
        return result;
    }
}
