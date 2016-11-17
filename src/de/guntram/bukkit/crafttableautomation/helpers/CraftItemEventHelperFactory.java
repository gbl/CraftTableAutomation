/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.guntram.bukkit.crafttableautomation.helpers;

import java.util.logging.Level;
import org.bukkit.Bukkit;

/**
 *
 * @author gbl
 */
public class CraftItemEventHelperFactory {
    static CraftItemEventHelper instance;

    public static CraftItemEventHelper getInstance() {
        String version;
        if (instance==null) {
            version=Bukkit.getServer().getClass().getPackage().getName();
            version=version.substring(version.lastIndexOf(".")+1);
            if (version.equals("v1_9_R1")) 
                instance=new CraftItemEventHelper_1_9_R1();
            else if (version.equals("v1_10_R1"))
                instance=new CraftItemEventHelper_1_10_R1();
            else if (version.equals("v1_11_R1"))
                instance=new CraftItemEventHelper_1_11_R1();
            else
                Bukkit.getLogger().log(Level.WARNING, "No NMS code for MC Version {0}", version);
        }
        return instance;
    }
}
