package hiti.main;

import com.sk89q.worldguard.bukkit.*;
import org.bukkit.permissions.*;
import com.sk89q.worldguard.protection.managers.storage.*;
import com.sk89q.worldguard.protection.flags.*;
import org.bukkit.configuration.file.*;
import java.util.logging.*;
import java.io.*;
import org.bukkit.entity.*;
import org.bukkit.block.*;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldedit.*;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldguard.protection.managers.*;
import com.sk89q.worldguard.protection.regions.*;
import org.bukkit.*;
import org.bukkit.inventory.*;
import java.util.*;
import org.bukkit.event.*;
import org.bukkit.event.block.*;

public class ListenerClass implements Listener
{
    StoneTypeData StoneTypeData;
    
    public ListenerClass() {
        this.StoneTypeData = new StoneTypeData();
    }
    
    @EventHandler
    public void onBlockPlace(final BlockPlaceEvent e) {
        final WorldGuardPlugin wg = (WorldGuardPlugin)Main.wgd;
        final Player p = e.getPlayer();
        final Block b = e.getBlock();
        final LocalPlayer lp = wg.wrapPlayer(p);
        final int count = wg.getRegionManager(p.getWorld()).getRegionCountOfPlayer(lp);
        if (Main.mats == null) {
            e.setCancelled(false);
            return;
        }
        int type = 0;
        String blocktypedata = String.valueOf(b.getType().toString()) + "-" + b.getData();
        String blocktype = b.getType().toString();
        if (Main.mats.contains(blocktypedata)) {
            type = 1;
        }
        else if (Main.mats.contains(blocktype)) {
            type = 2;
        }
        if (type > 0) {
            if (wg.canBuild(p, b.getLocation())) {
                if (p.hasPermission("protectionstones.create")) {
                    if (Main.toggleList != null) {
                        for (final String temp : Main.toggleList) {
                            if (temp.equalsIgnoreCase(p.getName())) {
                                e.setCancelled(false);
                                return;
                            }
                        }
                    }
                    if (!p.hasPermission("protectionstones.admin")) {
                        int max = 0;
                        for (final PermissionAttachmentInfo rawperm : p.getEffectivePermissions()) {
                            final String perm = rawperm.getPermission();
                            if (perm.startsWith("protectionstones.limit")) {
                                try {
                                    final int lim = Integer.parseInt(perm.substring(23));
                                    if (lim <= max) {
                                        continue;
                                    }
                                    max = lim;
                                }
                                catch (Exception er) {
                                    max = 0;
                                }
                            }
                        }
                        if (count >= max && max != 0) {
                            p.sendMessage(ChatColor.RED + "You can not create any more protected regions");
                            e.setCancelled(true);
                            return;
                        }
                        for (final String world : Main.deniedWorlds) {
                            if (world.equals(p.getLocation().getWorld().getName())) {
                                p.sendMessage(ChatColor.RED + "You can not create protections in this world");
                                e.setCancelled(true);
                                return;
                            }
                        }
                    }
                    final double bx = b.getLocation().getX();
                    final double by = b.getLocation().getY();
                    final double bz = b.getLocation().getZ();
                    Vector v1 = null;
                    Vector v2 = null;
                    blocktypedata = String.valueOf(b.getType().toString()) + "-" + b.getData();
                    blocktype = b.getType().toString();
                    if (type == 1) {
                        if (this.StoneTypeData.RegionY(blocktypedata) == -1) {
                            v1 = new Vector(bx - this.StoneTypeData.RegionX(blocktypedata), 0.0, bz - this.StoneTypeData.RegionZ(blocktypedata));
                            v2 = new Vector(bx + this.StoneTypeData.RegionX(blocktypedata), (double)p.getWorld().getMaxHeight(), bz + this.StoneTypeData.RegionZ(blocktypedata));
                        }
                        else {
                            v1 = new Vector(bx - this.StoneTypeData.RegionX(blocktypedata), by - this.StoneTypeData.RegionY(blocktypedata), bz - this.StoneTypeData.RegionZ(blocktypedata));
                            v2 = new Vector(bx + this.StoneTypeData.RegionX(blocktypedata), by + this.StoneTypeData.RegionY(blocktypedata), bz + this.StoneTypeData.RegionZ(blocktypedata));
                        }
                    }
                    else if (this.StoneTypeData.RegionY(b.getType().toString()) == -1) {
                        v1 = new Vector(bx - this.StoneTypeData.RegionX(blocktype), 0.0, bz - this.StoneTypeData.RegionZ(blocktype));
                        v2 = new Vector(bx + this.StoneTypeData.RegionX(blocktype), (double)p.getWorld().getMaxHeight(), bz + this.StoneTypeData.RegionZ(blocktype));
                    }
                    else {
                        v1 = new Vector(bx - this.StoneTypeData.RegionX(blocktype), by - this.StoneTypeData.RegionY(blocktype), bz - this.StoneTypeData.RegionZ(blocktype));
                        v2 = new Vector(bx + this.StoneTypeData.RegionX(blocktype), by + this.StoneTypeData.RegionY(blocktype), bz + this.StoneTypeData.RegionZ(blocktype));
                    }
                    final BlockVector min = v1.toBlockVector();
                    final BlockVector max2 = v2.toBlockVector();
                    final String id = "ps" + (int)bx + "x" + (int)by + "y" + (int)bz + "z";
                    final RegionManager rgm = wg.getRegionManager(p.getWorld());
                    final ProtectedRegion region = (ProtectedRegion)new ProtectedCuboidRegion(id, min, max2);
                    region.getOwners().addPlayer(p.getName());
                    if (Main.uuid) {
                        region.getOwners().addPlayer(p.getUniqueId());
                    }
                    rgm.addRegion(region);
                    final boolean overLap = rgm.overlapsUnownedRegion(region, lp);
                    if (overLap) {
                        rgm.removeRegion(id);
                        p.updateInventory();
                        try {
                            rgm.saveChanges();
                            rgm.save();
                        }
                        catch (StorageException e2) {
                            e2.printStackTrace();
                        }
                        if (!p.hasPermission("protectionstones.admin")) {
                            p.sendMessage(ChatColor.RED + "You can not a protection here as it overlaps another unowned region");
                            e.setCancelled(true);
                            return;
                        }
                    }
                    final HashMap<Flag<?>, Object> newFlags = new HashMap<Flag<?>, Object>();
                    for (int i = 0; i < DefaultFlag.flagsList.length; ++i) {
                        for (int j = 0; j < Main.flags.size(); ++j) {
                            final String[] rawflag = Main.flags.get(j).split(" ");
                            final String flag = rawflag[0];
                            final String setting = Main.flags.get(j).replace(String.valueOf(flag) + " ", "");
                            if (DefaultFlag.flagsList[i].getName().equalsIgnoreCase(flag)) {
                                if (setting != null) {
                                    if (DefaultFlag.flagsList[i].getName().equalsIgnoreCase("greeting") || DefaultFlag.flagsList[i].getName().equalsIgnoreCase("farewell")) {
                                        final String msg = setting.replaceAll("%player%", p.getName());
                                        newFlags.put((Flag<?>)DefaultFlag.flagsList[i], msg);
                                    }
                                    else if (setting.equalsIgnoreCase("allow")) {
                                        newFlags.put((Flag<?>)DefaultFlag.flagsList[i], StateFlag.State.ALLOW);
                                    }
                                    else if (setting.equalsIgnoreCase("deny")) {
                                        newFlags.put((Flag<?>)DefaultFlag.flagsList[i], StateFlag.State.DENY);
                                    }
                                    else if (setting.equalsIgnoreCase("true")) {
                                        newFlags.put((Flag<?>)DefaultFlag.flagsList[i], true);
                                    }
                                    else if (setting.equalsIgnoreCase("false")) {
                                        newFlags.put((Flag<?>)DefaultFlag.flagsList[i], false);
                                    }
                                    else {
                                        newFlags.put((Flag<?>)DefaultFlag.flagsList[i], setting);
                                    }
                                }
                                else {
                                    newFlags.put((Flag<?>)DefaultFlag.flagsList[i], null);
                                }
                            }
                        }
                    }
                    region.setFlags((Map)newFlags);
                    region.setPriority(Main.priority);
                    p.sendMessage(ChatColor.YELLOW + "This area is now protected.");
                    try {
                        rgm.saveChanges();
                        rgm.save();
                    }
                    catch (StorageException e3) {
                        e3.printStackTrace();
                    }
                    if (type == 2) {
                        blocktypedata = b.getType().toString();
                    }
                    if (this.StoneTypeData.AutoHide(blocktypedata)) {
                        final ItemStack ore = p.getItemInHand();
                        ore.setAmount(ore.getAmount() - 1);
                        p.setItemInHand((ore.getAmount() == 0) ? null : ore);
                        final Block blockToHide = p.getWorld().getBlockAt((int)bx, (int)by, (int)bz);
                        final YamlConfiguration hideFile = YamlConfiguration.loadConfiguration(Main.psStoneData);
                        String entry = String.valueOf((int)blockToHide.getLocation().getX()) + "x";
                        entry = String.valueOf(entry) + (int)blockToHide.getLocation().getY() + "y";
                        entry = String.valueOf(entry) + (int)blockToHide.getLocation().getZ() + "z";
                        hideFile.set(entry, (Object)blockToHide.getType().toString());
                        b.setType(Material.AIR);
                        try {
                            hideFile.save(Main.psStoneData);
                        }
                        catch (IOException ex) {
                            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
                else {
                    p.sendMessage(ChatColor.RED + "You don't have permission to place a ProtectionStone.");
                    e.setCancelled(true);
                }
            }
            else {
                p.sendMessage(ChatColor.RED + "You can't protect that area.");
                e.setCancelled(true);
            }
        }
    }
    
    @EventHandler
    public void onBlockBreak(final BlockBreakEvent e) {
        final WorldGuardPlugin wg = (WorldGuardPlugin)Main.wgd;
        final Player player = e.getPlayer();
        final Block pb = e.getBlock();
        final RegionManager rgm = wg.getRegionManager(player.getWorld());
        if (Main.mats == null) {
            e.setCancelled(false);
            return;
        }
        int type = 0;
        String blocktypedata = String.valueOf(pb.getType().toString()) + "-" + pb.getData();
        final String blocktype = pb.getType().toString();
        if (Main.mats.contains(blocktypedata)) {
            type = 1;
        }
        else if (Main.mats.contains(blocktype)) {
            type = 2;
        }
        if (type > 0) {
            final World world = player.getWorld();
            final RegionManager regionManager = wg.getRegionManager(world);
            final String psx = Double.toString(pb.getLocation().getX());
            final String psy = Double.toString(pb.getLocation().getY());
            final String psz = Double.toString(pb.getLocation().getZ());
            final String id = "ps" + psx.substring(0, psx.indexOf(".")) + "x" + psy.substring(0, psy.indexOf(".")) + "y" + psz.substring(0, psz.indexOf(".")) + "z";
            if (wg.canBuild(player, pb.getLocation())) {
                if (player.hasPermission("protectionstones.destroy")) {
                    if (type == 2) {
                        blocktypedata = pb.getType().toString();
                    }
                    if (regionManager.getRegion(id) != null) {
                        final LocalPlayer localPlayer = wg.wrapPlayer(player);
                        if (regionManager.getRegion(id).isOwner(localPlayer) || player.hasPermission("protectionstones.superowner")) {
                            if (!this.StoneTypeData.NoDrop(blocktypedata)) {
                                final ItemStack oreblock = new ItemStack(pb.getType(), 1, (short)pb.getData());
                                int freeSpace = 0;
                                for (final ItemStack i : player.getInventory()) {
                                    if (i == null) {
                                        freeSpace += oreblock.getType().getMaxStackSize();
                                    }
                                    else {
                                        if (i.getType() != oreblock.getType()) {
                                            continue;
                                        }
                                        freeSpace += i.getType().getMaxStackSize() - i.getAmount();
                                    }
                                }
                                if (freeSpace >= 1) {
                                    final PlayerInventory inventory = player.getInventory();
                                    inventory.addItem(new ItemStack[] { oreblock });
                                    pb.setType(Material.AIR);
                                    regionManager.removeRegion(id);
                                    try {
                                        rgm.save();
                                    }
                                    catch (Exception e2) {
                                        System.out.println("[ProtectionStones] WorldGuard Error [" + e2 + "] during Region File Save");
                                    }
                                    player.sendMessage(ChatColor.YELLOW + "This area is no longer protected.");
                                }
                                else {
                                    player.sendMessage(ChatColor.RED + "You don't have enough room in your inventory.");
                                }
                            }
                            else {
                                pb.setType(Material.AIR);
                                regionManager.removeRegion(id);
                                try {
                                    rgm.save();
                                }
                                catch (Exception e3) {
                                    System.out.println("[ProtectionStones] WorldGuard Error [" + e3 + "] during Region File Save");
                                }
                                player.sendMessage(ChatColor.YELLOW + "This area is no longer protected.");
                            }
                            e.setCancelled(true);
                        }
                        else {
                            player.sendMessage(ChatColor.YELLOW + "You are not the owner of this region.");
                            e.setCancelled(true);
                        }
                    }
                    else if (this.StoneTypeData.SilkTouch(blocktypedata)) {
                        pb.breakNaturally();
                        e.setCancelled(true);
                    }
                    else {
                        e.setCancelled(false);
                    }
                }
                else {
                    e.setCancelled(true);
                }
            }
            else {
                e.setCancelled(true);
            }
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onPistonExtend(final BlockPistonExtendEvent e) {
        final List<Block> pushedBlocks = (List<Block>)e.getBlocks();
        if (pushedBlocks != null) {
            for (final Block b : pushedBlocks) {
                int type = 0;
                String blocktypedata = String.valueOf(b.getType().toString()) + "-" + b.getData();
                if (Main.mats.contains(blocktypedata)) {
                    type = 1;
                }
                else if (Main.mats.contains(b.getType().toString())) {
                    type = 2;
                }
                if (type == 2) {
                    blocktypedata = b.getType().toString();
                }
                if (type > 0 && this.StoneTypeData.BlockPiston(blocktypedata)) {
                    e.setCancelled(true);
                }
            }
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onPistonRetract(final BlockPistonRetractEvent e) {
        final List<Block> retractedBlocks = (List<Block>)e.getBlocks();
        if (retractedBlocks != null) {
            for (final Block b : retractedBlocks) {
                int type = 0;
                String blocktypedata = String.valueOf(b.getType().toString()) + "-" + b.getData();
                if (Main.mats.contains(blocktypedata)) {
                    type = 1;
                }
                else if (Main.mats.contains(b.getType().toString())) {
                    type = 2;
                }
                if (type == 2) {
                    blocktypedata = b.getType().toString();
                }
                if (type > 0 && this.StoneTypeData.BlockPiston(blocktypedata)) {
                    e.setCancelled(true);
                }
            }
        }
    }
}

