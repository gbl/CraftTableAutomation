/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.guntram.bukkit.crafttableautomation;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.util.Vector;

/**
 *
 * @author gbl
 */
public class CTABlockListener implements Listener {
    
    public enum UpdateType {
        PLACE,
        FORM,
        DAMAGE,
        BREAK,
        EXTEND,
        RETRACT,
        FROMTO
    };
    
    final CraftTableAutomation plugin;
    
    public CTABlockListener(CraftTableAutomation cta) {
        plugin=cta;
    }


    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockPlace(BlockPlaceEvent e) {
        if (e.isCancelled())      return;
        Block block=e.getBlock();
        World world=block.getWorld();
        Material material=block.getType();
        Location location=block.getLocation();
        
        plugin.updateBlock(world, location, material, UpdateType.PLACE);
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockForm(BlockFormEvent e) {
        if (e.isCancelled())      return;
        Block block=e.getBlock();
        World world=block.getWorld();
        Material material=block.getType();
        Location location=block.getLocation();
        
        plugin.updateBlock(world, location, material, UpdateType.FORM);
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockDamage(BlockDamageEvent e) {
        if (e.isCancelled())      return;

        Block block=e.getBlock();
        World world=block.getWorld();
        Material material=block.getType();
        Location location=block.getLocation();
        
        plugin.updateBlock(world, location, material, UpdateType.DAMAGE);
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockBreak(BlockBreakEvent e) {
        if (e.isCancelled())      return;

        Block block=e.getBlock();
        World world=block.getWorld();
        Material material=block.getType();
        Location location=block.getLocation();
        
        plugin.updateBlock(world, location, material, UpdateType.BREAK);
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockPistonExtend(BlockPistonExtendEvent e) {
        if (e.isCancelled())      return;
        
        BlockFace direction=e.getDirection();
        List<Block>blocks=e.getBlocks();
        List<Block>withpiston=new ArrayList<Block>();
        withpiston.add(e.getBlock());
        withpiston.addAll(blocks);

        World world=e.getBlock().getWorld();
        Vector delta=vectorForDirection(direction);
        for (Block block:withpiston) {
            Material material=block.getType();
            Location location=block.getLocation();
            plugin.updateBlock(world, location, material, UpdateType.BREAK);
        }
        for (Block block:withpiston) {
            Material material=block.getType();
            Location location=block.getLocation();
            plugin.updateBlock(world, location.add(delta), material, UpdateType.PLACE);
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockPistonRetract(BlockPistonRetractEvent e) {
        if (e.isCancelled())      return;

        BlockFace direction=e.getDirection();
        List<Block>blocks=e.getBlocks();
        List<Block>withpiston=new ArrayList<Block>();
        withpiston.add(e.getBlock());
        withpiston.addAll(blocks);

        World world=e.getBlock().getWorld();
        Vector delta=vectorForDirection(direction);
        for (Block block:withpiston) {
            Material material=block.getType();
            Location location=block.getLocation();
            plugin.updateBlock(world, location, material, UpdateType.BREAK);
        }
        for (Block block:withpiston) {
            Material material=block.getType();
            Location location=block.getLocation();
            plugin.updateBlock(world, location.add(delta), material, UpdateType.PLACE);
        }
    }
    
    public void onBlockFromTo(BlockFromToEvent e) {
        if (e.isCancelled())      return;
        
        Block block=e.getBlock();
        World world=block.getWorld();
        Material material=block.getType();
        Location location=block.getLocation();

        plugin.updateBlock(world, location, material, UpdateType.FROMTO);
    }
    
    private Vector vectorForDirection(BlockFace direction) {
        return new Vector(direction.getModX(), direction.getModY(), direction.getModZ());
    }
}
