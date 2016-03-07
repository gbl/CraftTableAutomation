/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.guntram.bukkit.crafttableautomation;

import java.util.ArrayList;
import java.util.Map;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_9_R1.inventory.CraftShapedRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;

/**
 *
 * @author gbl
 */
public class SimpleRecipe {
    private int amount;
    private Material material;
    private int subtype;
    
    SimpleRecipe(int amount, Material material, int subtype) {
        this.amount=amount;
        this.material=material;
        this.subtype=subtype;
    }
    int         getAmount()     { return amount; }
    Material    getMaterial()   { return material; }
    int         getSubtype()    { return subtype; }
    
    static SimpleRecipe[] fromCraftStack(ItemStack[] craftstack) {
        SimpleRecipe[] result=new SimpleRecipe[craftstack.length];
        
        for (int i=0; i<craftstack.length; i++) {
            result[i]=new SimpleRecipe(craftstack[i].getAmount(), craftstack[i].getType(), craftstack[i].getDurability());
        }
        
        return result;
    }
    
    static SimpleRecipe[] fromRecipe(CraftShapedRecipe recipe) {
      ArrayList<SimpleRecipe> list=new ArrayList<>();
      ItemStack stack=recipe.getResult();
      list.add(new SimpleRecipe(stack.getAmount(), stack.getType(), stack.getDurability()));
      Map<Character, ItemStack> ingredients=recipe.getIngredientMap();
      for (Map.Entry<Character, ItemStack> ingredient : ingredients.entrySet()) {
          stack=ingredient.getValue();
          if (stack!=null)
              list.add(new SimpleRecipe(stack.getAmount(), stack.getType(), stack.getDurability()));
      }
      return list.toArray(new SimpleRecipe[list.size()]);
    }
}
