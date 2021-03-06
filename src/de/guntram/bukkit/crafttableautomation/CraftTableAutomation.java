/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.guntram.bukkit.crafttableautomation;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.logging.Level;
import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.DataStore;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

/**
 *
 * @author gbl
 */
public class CraftTableAutomation extends JavaPlugin  {


    private HashMap<Location,CraftTableConfiguration>allWorkBenches;
    private File autoTablesFile;
    private Plugin griefPrevention;
    private boolean useGP;
    private boolean displayItemCount;
    
    @Override
    public void onEnable() {
        saveDefaultConfig();
        
        CTABlockListener blockListener=new CTABlockListener(this);
        getServer().getPluginManager().registerEvents(blockListener, this);
        getServer().getPluginManager().registerEvents(new CraftItemEventListener(this), this);

        griefPrevention=getServer().getPluginManager().getPlugin("GriefPrevention");
        useGP=getConfig().getBoolean("usegriefprevention", true);
        displayItemCount=getConfig().getBoolean("displayitemcount", false);

        autoTablesFile=new File(getDataFolder(), "tables.txt");
        allWorkBenches=new HashMap<>();
        loadAutoTablesFile(autoTablesFile);
        this.getServer().getScheduler().scheduleSyncRepeatingTask((Plugin)this, 
                       new CraftTableProcessor(this, allWorkBenches), 200L, 20L);        
    }
    
    @Override
    public void onDisable() {
        saveAutoTablesFile(autoTablesFile);
    }
    
    public CraftTableConfiguration updateBlock(Location location, Material material, CTABlockListener.UpdateType event) {
        return updateBlock(location, material, event, null);
    }
    
    public CraftTableConfiguration updateBlock(Location location, Material material, CTABlockListener.UpdateType event, CraftTableConfiguration stack) {
        if (material==Material.HOPPER || material==Material.WORKBENCH)
            getLogger().log(Level.FINE, "World {0} at {1}/{2}/{3} Material {4} by event {5}", new Object[]{location.getWorld().getName(), location.getBlockX(), location.getBlockY(), location.getBlockZ(), material, event});
        
        Location above=location.clone().add(0, 1, 0);
        Location below=location.clone().subtract(0, 1, 0);
        if (material==Material.HOPPER && above.getBlock().getType()==Material.WORKBENCH)
            return changeWorkbenchAt(above, event, stack);
        else if (material==Material.WORKBENCH && below.getBlock().getType()==Material.HOPPER)
            return changeWorkbenchAt(location, event, stack);
        else
            return null;
    }
    
    private CraftTableConfiguration changeWorkbenchAt(Location loc, CTABlockListener.UpdateType event, CraftTableConfiguration stack) {
        if (event==CTABlockListener.UpdateType.PLACE) {
            allWorkBenches.put(loc, stack==null ? new CraftTableConfiguration(0) : stack);
        } else if (event==CTABlockListener.UpdateType.BREAK) {
            CraftTableConfiguration previous=allWorkBenches.get(loc);
            allWorkBenches.remove(loc);
            return previous;
        }
        return null;
    }
    
    public ConfigureBenchResult configureBenchAt(Location loc, CraftTableConfiguration config, Player player) {
        for (Location x: allWorkBenches.keySet()) {
            getLogger().log(Level.FINE, "loc={0} ,x={1}equal={2}", new Object[]{loc.toString(), x.toString(), loc.equals(x)});
        }
        if (allWorkBenches.get(loc)!=null) {
            if (player.hasPermission("cta.use")) {
                if (hasClaimPermission(loc, player)) {
                    config.setOwner(player.getName());
                    config.setProducedItems(0);
                    allWorkBenches.put(loc, config);
                    CraftTableProcessor.updateStatus(loc, config.get(0).getMaterial().toString(), 0, player.getName());
                    return ConfigureBenchResult.OK;
                } else {
                    return ConfigureBenchResult.NOTINCLAIM;
                }
            } else {
                return ConfigureBenchResult.NOPERMISSION;
            }
        } else {
            return ConfigureBenchResult.NOWORKBENCHATTHISLOC;
        }
    }
    
    public boolean isBenchAt(Location loc) {
        return allWorkBenches.get(loc)!=null;
    }
    
    public void saveAutoTablesFile(File file) {
        try (PrintWriter writer=new PrintWriter(new FileWriter(file))) {
            for (Location location:allWorkBenches.keySet()) {
                CraftTableConfiguration config=allWorkBenches.get(location);
                writer.print(location.getWorld().getName()+"/"+location.getBlockX()+"/"+location.getBlockY()+"/"+location.getBlockZ());
                writer.print("/"+config.getProducedItems());
                writer.print("/"+config.getOwner());
                writer.print(":");
                writer.print(config.toString());
                writer.println();
            }
        } catch (IOException ex) {
            getLogger().log(Level.SEVERE, null, ex);
        }
    }
    
    public void loadAutoTablesFile(File file) {
        if (!file.exists()) {
            getLogger().log(Level.INFO, "No tables file found");
            return;
        }
        String s;
        HashMap<Location,CraftTableConfiguration> tempWorkBenches=new HashMap<>();
        try (BufferedReader reader=new BufferedReader(new FileReader(file))) {
            while ((s=reader.readLine())!=null) {
                String[] keyval=s.split(":");
                assert(keyval.length==2);
                String[] locstr=keyval[0].split("/");
                assert(locstr.length>=4);
                World world=Bukkit.getWorld(locstr[0]);
                Location loc=new Location(world, Integer.parseInt(locstr[1]), Integer.parseInt(locstr[2]), Integer.parseInt(locstr[3]));
                CraftTableConfiguration config=CraftTableConfiguration.fromString(keyval[1]);
                if (locstr.length>=5) {
                    config.setProducedItems(Long.parseLong(locstr[4]));
                }
                if (locstr.length>=6) {
                    config.setOwner(locstr[5]);
                } else {
                    config.setOwner("");
                }
                tempWorkBenches.put(loc, config);
            }
            allWorkBenches.clear();
            allWorkBenches.putAll(tempWorkBenches);
        } catch (IOException ex) {
            getLogger().log(Level.SEVERE, null, ex);
        }
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String commandName=command.getName();
        if (commandName.equalsIgnoreCase("cta")) {
            if (args.length==1 && args[0].equalsIgnoreCase("list")) {
                sender.sendMessage("§2"+allWorkBenches.size()+" Automatic Crafttables");
                for (Location location:allWorkBenches.keySet()) {
                    sender.sendMessage("§2-----------------------");
                    sender.sendMessage(location.toString());
                    CraftTableConfiguration content=allWorkBenches.get(location);
                    if (content!=null && content.size()>0) {
                        sender.sendMessage("   owned by "+content.getOwner());
                        sender.sendMessage("   creating "+content.get(0).getAmount()+" of "+content.get(0).getMaterial()+" subtype "+content.get(0).getSubtype());
                        for (int i=1; i<content.size(); i++) {
                            //if (content[i].getAmount()!=0) {
                                sender.sendMessage("     using "+content.get(i).getAmount()+" of "+content.get(i).getMaterial()+" subtype "+content.get(i).getSubtype());
                            //}
                        }
                    }
                }
                return true;
            }
            if (args.length>=1 && args[0].equalsIgnoreCase("usegp")) {
                if (args.length>=2) {
                    if (args[1].equalsIgnoreCase("true") || args[1].equalsIgnoreCase("yes"))
                        useGP=true;
                    else
                        useGP=false;
                }
                sender.sendMessage("use Grief Prevention is now "+useGP);
                return true;
            }
            if (args.length==1 && args[0].equalsIgnoreCase("testlocs") && sender instanceof Player) {
                Location l1=new Location(((Player)sender).getWorld(), 10, 11, 12);
                Location l2=new Location(((Player)sender).getWorld(), 20, 21, 22);
                Location l3=new Location(((Player)sender).getWorld(), 10, 11, 12);
                
                ((Player)sender).sendMessage("L1==L2?" + l1.equals(l2));
                ((Player)sender).sendMessage("L1==L3?" + l1.equals(l3));
                ((Player)sender).sendMessage("hashcode L1 "+l1.hashCode());
                ((Player)sender).sendMessage("hashcode L2 "+l2.hashCode());
                ((Player)sender).sendMessage("hashcode L3 "+l3.hashCode());
                return true;
            }
            if (args.length==1 && args[0].equalsIgnoreCase("save")) {
                getDataFolder().mkdirs();
                saveAutoTablesFile(autoTablesFile);
                sender.sendMessage("saved current cta config");
                return true;
            }
            if (args.length==1 && args[0].equalsIgnoreCase("load")) {
                loadAutoTablesFile(autoTablesFile);
                sender.sendMessage("loaded disk version of cta config");
                return true;
            }
        }
        return false;
    }

    private boolean hasClaimPermission(Location loc, CommandSender sender) {
        if (griefPrevention==null || useGP==false)
            return true;
        if (!(sender instanceof Player))
            return false;
        DataStore store=GriefPrevention.instance.dataStore;
        Claim claim = store.getClaimAt(loc, true, null);
        if (claim==null || claim.allowAccess((Player)sender)==null) {
            return true;
        }
        return false;
    }

    public boolean getDisplayItemCount() {
        return displayItemCount;
    }
}
