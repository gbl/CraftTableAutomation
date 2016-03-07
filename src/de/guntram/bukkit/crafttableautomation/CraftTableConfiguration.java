/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.guntram.bukkit.crafttableautomation;

import de.guntram.bukkit.crafttableautomation.helpers.CraftItemEventHelper;
import java.util.ArrayList;
import java.util.Map;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;

/**
 *
 * @author gbl
 */
public class CraftTableConfiguration {
    private RecipeComponent[] components;
    
    public CraftTableConfiguration(int size) {
        components=new RecipeComponent[size];
    }
    
    static CraftTableConfiguration fromCraftStack(ItemStack[] craftstack) {
        CraftTableConfiguration result=new CraftTableConfiguration(craftstack.length);
        
        for (int i=0; i<craftstack.length; i++) {
            result.components[i]=new RecipeComponent(craftstack[i].getAmount(), 
                            craftstack[i].getType(), craftstack[i].getDurability());
        }
        return result;
    }
    static CraftTableConfiguration fromRecipe(Recipe recipe) {
        
      ArrayList<RecipeComponent> list=new ArrayList<>();
      ItemStack stack=recipe.getResult();
      list.add(new RecipeComponent(stack.getAmount(), stack.getType(), stack.getDurability()));
//      Map<Character, ItemStack> ingredients=((CraftShapedRecipe)recipe).getIngredientMap();
      Map<Character, ItemStack> ingredients=CraftItemEventHelper.getInstance().getIngredientMap(recipe);
      for (Map.Entry<Character, ItemStack> ingredient : ingredients.entrySet()) {
          stack=ingredient.getValue();
          if (stack!=null)
              list.add(new RecipeComponent(stack.getAmount(), stack.getType(), stack.getDurability()));
      }
      
      CraftTableConfiguration result=new CraftTableConfiguration(list.size());
      result.components=list.toArray(result.components);
      return result;
    }
    
    RecipeComponent get(int i) { return components[i]; }
    int size() { return components.length; }
}
