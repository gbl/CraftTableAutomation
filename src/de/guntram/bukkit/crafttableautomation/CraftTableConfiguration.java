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

/*
    static CraftTableConfiguration fromCraftStack(ItemStack[] craftstack) {
        CraftTableConfiguration result=new CraftTableConfiguration(craftstack.length);
        
        for (int i=0; i<craftstack.length; i++) {
            result.components[i]=new RecipeComponent(craftstack[i].getAmount(), 
                            craftstack[i].getType(), craftstack[i].getDurability());
        }
        return result;
    }
*/
    static CraftTableConfiguration fromRecipe(Recipe recipe) {
        
        ArrayList<RecipeComponent> list=new ArrayList<>();
        ItemStack stack=recipe.getResult();
        list.add(new RecipeComponent(stack.getAmount(), stack.getType(), stack.getDurability()));

        Map<Character, ItemStack> ingredients=CraftItemEventHelper.getInstance().getIngredientMap(recipe);
        for (Map.Entry<Character, ItemStack> ingredient : ingredients.entrySet()) {
            stack=ingredient.getValue();
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
