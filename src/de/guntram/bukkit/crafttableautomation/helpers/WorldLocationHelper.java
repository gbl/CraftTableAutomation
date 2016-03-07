/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.guntram.bukkit.crafttableautomation.helpers;

import java.util.Objects;
import org.bukkit.Location;
import org.bukkit.World;

/**
 *
 * @author gbl
 */
public class WorldLocationHelper {
    
    private final World world;
    private final Location loc;
    
    public WorldLocationHelper(World world, Location loc) {
        this.world=world;
        this.loc=loc;
    }
    
    @Override
    public String toString() {
        return world.getName()+":"+loc.getBlockX()+"/"+loc.getBlockY()+"/"+loc.getBlockZ();
    }
    
    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    public static WorldLocationHelper fromString() {
        throw new UnsupportedOperationException("parse WLH string");
    }
    
    @Override
    public boolean equals(Object l2) {
        return  l2!=null
            &&  l2.getClass()==this.getClass()
            &&  world.getName().equals(((WorldLocationHelper)l2).world.getName())
            &&  loc.getBlockX()==((WorldLocationHelper)l2).loc.getBlockX()
            &&  loc.getBlockY()==((WorldLocationHelper)l2).loc.getBlockY()
            &&  loc.getBlockZ()==((WorldLocationHelper)l2).loc.getBlockZ();
    }
}
