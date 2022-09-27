package hiti.main;

import org.bukkit.plugin.java.*;
import org.bukkit.plugin.*;
import java.util.logging.*;
import java.io.*;
import org.bukkit.event.*;
import org.bukkit.command.*;
import org.bukkit.entity.*;
import com.sk89q.worldguard.bukkit.*;
import org.bukkit.configuration.file.*;
import com.sk89q.worldguard.protection.regions.*;
import org.bukkit.*;
import org.bukkit.Location;
import org.bukkit.block.*;
import com.sk89q.worldguard.protection.managers.*;
import com.sk89q.worldguard.LocalPlayer;

import java.util.*;
import com.sk89q.worldguard.domains.*;
import com.sk89q.worldedit.*;
import com.sk89q.worldedit.Vector;

import org.bukkit.inventory.*;
import com.sk89q.worldguard.protection.flags.*;
import org.bukkit.configuration.*;

public class Main extends JavaPlugin
{
    public static Plugin plugin;
    public static Plugin wgd;
    public static File psStoneData;
    public static File conf;
    public static FileConfiguration config;
    public static List<String> flags;
    public static List<String> toggleList;
    public static List<String> allowedFlags;
    public static List<String> deniedWorlds;
    public static Collection<String> mats;
    public static boolean uuid;
    public static int x;
    public static int y;
    public static int z;
    public static int priority;
    public Map<CommandSender, Integer> viewTaskList;
    StoneTypeData StoneTypeData;
    
    static {
        Main.flags = new ArrayList<String>();
        Main.toggleList = new ArrayList<String>();
        Main.allowedFlags = new ArrayList<String>();
        Main.deniedWorlds = new ArrayList<String>();
        Main.mats = new HashSet<String>();
    }
    
    public Main() {
        this.StoneTypeData = new StoneTypeData();
    }
    
    public void onEnable() {
        this.viewTaskList = new HashMap<CommandSender, Integer>();
        this.saveDefaultConfig();
        this.getConfig().options().copyDefaults(true);
        Main.plugin = (Plugin)this;
        Main.conf = new File(this.getDataFolder() + "/config.yml");
        Main.psStoneData = new File(this.getDataFolder() + "/hiddenpstones.yml");
        if (!Main.psStoneData.exists()) {
            try {
                Main.psStoneData.createNewFile();
            }
            catch (IOException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        this.getServer().getPluginManager().registerEvents((Listener)new ListenerClass(), (Plugin)this);
        if (this.getServer().getPluginManager().getPlugin("WorldGuard").isEnabled() && this.getServer().getPluginManager().getPlugin("WorldGuard").isEnabled()) {
            Main.wgd = this.getServer().getPluginManager().getPlugin("WorldGuard");
        }
        else {
            this.getLogger().info("WorldGuard и WorldEdit не включены! Отключаем ProtectionStones...");
            this.getServer().getPluginManager().disablePlugin((Plugin)this);
        }
        String[] split2;
        for (int length = (split2 = this.getConfig().getString("Blocks").split(",")).length, i = 0; i < length; ++i) {
            final String material = split2[i];
            final String[] split = material.split("-");
            if (split.length > 1 && split.length < 3) {
                if (Material.getMaterial(split[0]) != null) {
                    Main.mats.add(material);
                }
            }
            else {
                Main.mats.add(split[0]);
            }
        }
        Main.flags = (List<String>)this.getConfig().getStringList("Flags");
        Main.allowedFlags = Arrays.asList(this.getConfig().getString("Allowed Flags").toLowerCase().split(","));
        Main.deniedWorlds = Arrays.asList(this.getConfig().getString("Worlds Denied").toLowerCase().split(","));
        Main.uuid = this.getConfig().getBoolean("UUID");
        this.initConfig();
        this.getLogger().info("ProtectionStones успешно запустился!");
        this.getLogger().info("Плагин создал itzHiti");
    }
    
    public boolean onCommand(final CommandSender s, final Command cmd, final String label, final String[] args) {
        if (s instanceof Player) {
            final Player p = (Player)s;
            if (cmd.getName().equalsIgnoreCase("ps")) {
                final WorldGuardPlugin wg = (WorldGuardPlugin)Main.wgd;
                final RegionManager rgm = wg.getRegionManager(p.getWorld());
                if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
                    p.sendMessage(ChatColor.GRAY + "Плагин " + ChatColor.GOLD + "ProtectionStones v 1.0" + ChatColor.GRAY + " от " + ChatColor.RED +"itzHiti" + ChatColor.GRAY + ".");
                    p.sendMessage("");
                    p.sendMessage(ChatColor.YELLOW + "/ps info members|owners|flags");
                    p.sendMessage(ChatColor.YELLOW + "/ps add|remove {playername}");
                    p.sendMessage(ChatColor.YELLOW + "/ps addowner|removeowner {playername}");
                    p.sendMessage(ChatColor.YELLOW + "/ps count [player]");
                    p.sendMessage(ChatColor.YELLOW + "/ps flag {flagname} {setting|null}");
                    p.sendMessage(ChatColor.YELLOW + "/ps home {num} - " + ChatColor.GREEN + "{num} has to be within the number of protected regions you own. Use /ps count to check");
                    p.sendMessage(ChatColor.YELLOW + "/ps tp {player} {num}");
                    p.sendMessage(ChatColor.YELLOW + "/ps hide|unhide");
                    p.sendMessage(ChatColor.YELLOW + "/ps toggle");
                    p.sendMessage(ChatColor.YELLOW + "/ps view");
                    p.sendMessage(ChatColor.YELLOW + "/ps reclaim");
                    p.sendMessage(ChatColor.YELLOW + "/ps priority {number|null}");
                    p.sendMessage(ChatColor.YELLOW + "/ps region count|list|remove|regen|disown {playername}");
                    p.sendMessage(ChatColor.YELLOW + "/ps admin {version|settings|hide|unhide|");
                    p.sendMessage(ChatColor.YELLOW + "           cleanup|lastlogon|lastlogons|stats}");
                    return true;
                }
                if (args[0].equalsIgnoreCase("toggle")) {
                    if (p.hasPermission("protectionstones.toggle")) {
                        if (Main.toggleList != null) {
                            if (!Main.toggleList.contains(p.getName())) {
                                Main.toggleList.add(p.getName());
                                p.sendMessage(ChatColor.YELLOW + "Устанавливать приваты больше нельзя");
                            }
                            else {
                                Main.toggleList.remove(p.getName());
                                p.sendMessage(ChatColor.YELLOW + "Устанавливать приваты больше теперь можно");
                            }
                        }
                        else {
                            Main.toggleList.add(p.getName());
                            p.sendMessage(ChatColor.YELLOW + "Устанавливать приваты больше нельзя");
                        }
                    }
                    else {
                        p.sendMessage(ChatColor.RED + "У Вас нет прав!");
                    }
                    return true;
                }
                final double x = p.getLocation().getX();
                final double y = p.getLocation().getY();
                final double z = p.getLocation().getZ();
                final Vector v = new Vector(x, y, z);
                String id = "";
                final List<String> idList = (List<String>)rgm.getApplicableRegionsIDs(v);
                if (idList.size() == 1) {
                    id = idList.toString();
                    id = id.substring(1, id.length() - 1);
                }
                else {
                    double distanceToPS = 10000.0;
                    double tempToPS = 0.0;
                    String namePSID = "";
                    for (final String currentID : idList) {
                        if (currentID.substring(0, 2).equals("ps")) {
                            final int indexX = currentID.indexOf("x");
                            final int indexY = currentID.indexOf("y");
                            final int indexZ = currentID.length() - 1;
                            final double psx = Double.parseDouble(currentID.substring(2, indexX));
                            final double psy = Double.parseDouble(currentID.substring(indexX + 1, indexY));
                            final double psz = Double.parseDouble(currentID.substring(indexY + 1, indexZ));
                            final Location psLocation = new Location(p.getWorld(), psx, psy, psz);
                            tempToPS = p.getLocation().distance(psLocation);
                            if (tempToPS >= distanceToPS) {
                                continue;
                            }
                            distanceToPS = tempToPS;
                            namePSID = currentID;
                        }
                    }
                    id = namePSID;
                }
                LocalPlayer localPlayer = wg.wrapPlayer(p);
                if (rgm.getRegion(id) != null && (rgm.getRegion(id).isOwner(localPlayer) || p.hasPermission("protectionstones.superowner"))) {
                    if (args[0].equalsIgnoreCase("add")) {
                        if (!p.hasPermission("protectionstones.members")) {
                            p.sendMessage(ChatColor.RED + "У Вас нет прав использовать команды участников привата");
                            return true;
                        }
                        if (args.length < 2) {
                            p.sendMessage(ChatColor.RED + "Для этой команды необходимо использовать никнейм игрока.");
                            return true;
                        }
                        final String playerName = args[1];
                        final UUID uid = Bukkit.getOfflinePlayer(playerName).getUniqueId();
                        final DefaultDomain members = rgm.getRegion(id).getMembers();
                        members.addPlayer(playerName);
                        if (Main.uuid) {
                            members.addPlayer(uid);
                        }
                        rgm.getRegion(id).setMembers(members);
                        try {
                            rgm.save();
                        }
                        catch (Exception e) {
                            System.out.println("[ProtectionStones] Ошибка WorldGuard [" + e + "] во время сохранения файла привата.");
                        }
                        p.sendMessage(ChatColor.YELLOW + playerName + " был добавлен в Ваш приват.");
                        return true;
                    }
                    else {
                        if (args[0].equalsIgnoreCase("remove")) {
                            if (p.hasPermission("protectionstones.members")) {
                                if (args.length < 2) {
                                    p.sendMessage(ChatColor.RED + "Для выполнения данной команды необходим никнейм игрока!");
                                    return true;
                                }
                                final String playerName = args[1];
                                final UUID uid = Bukkit.getOfflinePlayer(playerName).getUniqueId();
                                final DefaultDomain members = rgm.getRegion(id).getMembers();
                                members.removePlayer(playerName);
                                if (Main.uuid) {
                                    members.removePlayer(uid);
                                }
                                rgm.getRegion(id).setMembers(members);
                                try {
                                    rgm.save();
                                }
                                catch (Exception e) {
                                    System.out.println("[ProtectionStones] Ошибка WorldGuard [" + e + "] во время сохранения файла привата.");
                                }
                                p.sendMessage(ChatColor.YELLOW + playerName + " has been removed from region.");
                            }
                            else {
                                p.sendMessage(ChatColor.RED + "У Вас нет прав, чтобы использовать команды участников!");
                            }
                            return true;
                        }
                        if (args[0].equalsIgnoreCase("addowner")) {
                            if (!p.hasPermission("protectionstones.owners")) {
                                p.sendMessage(ChatColor.RED + "У Вас нет прав, чтобы использовать команды овнеров!");
                                return true;
                            }
                            if (args.length < 2) {
                                p.sendMessage(ChatColor.RED + "Для этой команды необходимо использовать никнейм игрока.");
                                return true;
                            }
                            final String playerName = args[1];
                            final UUID uid = Bukkit.getOfflinePlayer(playerName).getUniqueId();
                            final DefaultDomain owners = rgm.getRegion(id).getOwners();
                            owners.addPlayer(playerName);
                            if (Main.uuid) {
                                owners.addPlayer(uid);
                            }
                            rgm.getRegion(id).setOwners(owners);
                            try {
                                rgm.save();
                            }
                            catch (Exception e) {
                                System.out.println("[ProtectionStones] Ошибка WorldGuard [" + e + "] во время сохранения файла привата.");
                            }
                            p.sendMessage(ChatColor.YELLOW + playerName + " был успешно добавлен в Ваш приват!");
                            return true;
                        }
                        else {
                            if (args[0].equalsIgnoreCase("removeowner")) {
                                if (p.hasPermission("protectionstones.owners")) {
                                    if (args.length < 2) {
                                        p.sendMessage(ChatColor.RED + "Для этой команды необходимо использовать никнейм игрока.");
                                        return true;
                                    }
                                    final String playerName = args[1];
                                    final UUID uid = Bukkit.getOfflinePlayer(playerName).getUniqueId();
                                    final DefaultDomain owners = rgm.getRegion(id).getOwners();
                                    owners.removePlayer(playerName);
                                    if (Main.uuid) {
                                        owners.addPlayer(uid);
                                    }
                                    rgm.getRegion(id).setOwners(owners);
                                    try {
                                        rgm.save();
                                    }
                                    catch (Exception e) {
                                        System.out.println("[ProtectionStones] Ошибка WorldGuard [" + e + "] во время сохранения файла привата.");
                                    }
                                    p.sendMessage(ChatColor.YELLOW + playerName + " был убран с Вашего привата.");
                                }
                                else {
                                    p.sendMessage(ChatColor.RED + "У Вас нет прав использовать команды овнеров привата");
                                }
                                return true;
                            }
                            if (args[0].equalsIgnoreCase("view")) {
                                if (p.hasPermission("protectionstones.view")) {
                                    if (!this.viewTaskList.isEmpty()) {
                                        int playerTask = 0;
                                        try {
                                            playerTask = this.viewTaskList.get(p);
                                        }
                                        catch (Exception e2) {
                                            playerTask = 0;
                                        }
                                        if (playerTask != 0 && Bukkit.getScheduler().isQueued(playerTask)) {
                                            return true;
                                        }
                                    }
                                    final Vector minVector = (Vector)rgm.getRegion(id).getMinimumPoint();
                                    final Vector maxVector = (Vector)rgm.getRegion(id).getMaximumPoint();
                                    final int minX = minVector.getBlockX();
                                    final int minY = minVector.getBlockY();
                                    final int minZ = minVector.getBlockZ();
                                    final int maxX = maxVector.getBlockX();
                                    final int maxY = maxVector.getBlockY();
                                    final int maxZ = maxVector.getBlockZ();
                                    final double px = p.getLocation().getX();
                                    final double py = p.getLocation().getY();
                                    final double pz = p.getLocation().getZ();
                                    final Vector playerVector = new Vector(px, py, pz);
                                    final int playerY = playerVector.getBlockY();
                                    final World theWorld = p.getWorld();
                                    final Material bm1 = this.getBlock(theWorld, minX, playerY, minZ);
                                    final Material bm2 = this.getBlock(theWorld, maxX, playerY, minZ);
                                    final Material bm3 = this.getBlock(theWorld, minX, playerY, maxZ);
                                    final Material bm4 = this.getBlock(theWorld, maxX, playerY, maxZ);
                                    final Material bm5 = this.getBlock(theWorld, minX, maxY, minZ);
                                    final Material bm6 = this.getBlock(theWorld, maxX, maxY, minZ);
                                    final Material bm7 = this.getBlock(theWorld, minX, maxY, maxZ);
                                    final Material bm8 = this.getBlock(theWorld, maxX, maxY, maxZ);
                                    final Material bm9 = this.getBlock(theWorld, minX, minY, minZ);
                                    final Material bm10 = this.getBlock(theWorld, maxX, minY, minZ);
                                    final Material bm11 = this.getBlock(theWorld, minX, minY, maxZ);
                                    final Material bm12 = this.getBlock(theWorld, maxX, minY, maxZ);
                                    this.setBlock(theWorld, minX, playerY, minZ, Material.GLASS);
                                    this.setBlock(theWorld, maxX, playerY, minZ, Material.GLASS);
                                    this.setBlock(theWorld, minX, playerY, maxZ, Material.GLASS);
                                    this.setBlock(theWorld, maxX, playerY, maxZ, Material.GLASS);
                                    this.setBlock(theWorld, minX, maxY, minZ, Material.GLASS);
                                    this.setBlock(theWorld, maxX, maxY, minZ, Material.GLASS);
                                    this.setBlock(theWorld, minX, maxY, maxZ, Material.GLASS);
                                    this.setBlock(theWorld, maxX, maxY, maxZ, Material.GLASS);
                                    this.setBlock(theWorld, minX, minY, minZ, Material.GLASS);
                                    this.setBlock(theWorld, maxX, minY, minZ, Material.GLASS);
                                    this.setBlock(theWorld, minX, minY, maxZ, Material.GLASS);
                                    this.setBlock(theWorld, maxX, minY, maxZ, Material.GLASS);
                                    final int taskID = this.getServer().getScheduler().scheduleSyncDelayedTask((Plugin)this, (Runnable)new Runnable() {
                                        @Override
                                        public void run() {
                                            Main.this.setBlock(theWorld, minX, playerY, minZ, bm1);
                                            Main.this.setBlock(theWorld, maxX, playerY, minZ, bm2);
                                            Main.this.setBlock(theWorld, minX, playerY, maxZ, bm3);
                                            Main.this.setBlock(theWorld, maxX, playerY, maxZ, bm4);
                                            Main.this.setBlock(theWorld, minX, maxY, minZ, bm5);
                                            Main.this.setBlock(theWorld, maxX, maxY, minZ, bm6);
                                            Main.this.setBlock(theWorld, minX, maxY, maxZ, bm7);
                                            Main.this.setBlock(theWorld, maxX, maxY, maxZ, bm8);
                                            Main.this.setBlock(theWorld, minX, minY, minZ, bm9);
                                            Main.this.setBlock(theWorld, maxX, minY, minZ, bm10);
                                            Main.this.setBlock(theWorld, minX, minY, maxZ, bm11);
                                            Main.this.setBlock(theWorld, maxX, minY, maxZ, bm12);
                                        }
                                    }, 600L);
                                    this.viewTaskList.put((CommandSender)p, taskID);
                                }
                                else {
                                    p.sendMessage(ChatColor.RED + "У Вас нет прав");
                                }
                                return true;
                            }
                            if (args[0].equalsIgnoreCase("unhide")) {
                                if (p.hasPermission("protectionstones.unhide")) {
                                    if (id.substring(0, 2).equals("ps")) {
                                        final int indexX2 = id.indexOf("x");
                                        final int indexY2 = id.indexOf("y");
                                        final int indexZ2 = id.length() - 1;
                                        final int psx2 = Integer.parseInt(id.substring(2, indexX2));
                                        final int psy2 = Integer.parseInt(id.substring(indexX2 + 1, indexY2));
                                        final int psz2 = Integer.parseInt(id.substring(indexY2 + 1, indexZ2));
                                        final Block blockToUnhide = p.getWorld().getBlockAt(psx2, psy2, psz2);
                                        final YamlConfiguration hideFile = YamlConfiguration.loadConfiguration(Main.psStoneData);
                                        String entry = String.valueOf((int)blockToUnhide.getLocation().getX()) + "x";
                                        entry = String.valueOf(entry) + (int)blockToUnhide.getLocation().getY() + "y";
                                        entry = String.valueOf(entry) + (int)blockToUnhide.getLocation().getZ() + "z";
                                        String setmat = hideFile.getString(entry);
                                        String subtype = null;
                                        if (blockToUnhide.getType() == Material.AIR) {
                                            if (setmat.contains("-")) {
                                                final String[] str = setmat.split("-");
                                                setmat = str[0];
                                                subtype = str[1];
                                            }
                                            hideFile.set(entry, (Object)null);
                                            try {
                                                hideFile.save(Main.psStoneData);
                                            }
                                            catch (IOException ex) {
                                                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                                            }
                                            blockToUnhide.setType(Material.getMaterial(setmat));
                                            if (subtype != null) {
                                                blockToUnhide.setData((byte)Integer.parseInt(subtype));
                                            }
                                        }
                                        else {
                                            p.sendMessage(ChatColor.YELLOW + "Этот приват не скрыт...");
                                        }
                                    }
                                    else {
                                        p.sendMessage(ChatColor.YELLOW + "Не приват");
                                    }
                                }
                                else {
                                    p.sendMessage(ChatColor.RED + "У Вас нет прав");
                                }
                                return true;
                            }
                            if (args[0].equalsIgnoreCase("hide")) {
                                if (p.hasPermission("protectionstones.hide")) {
                                    if (id.substring(0, 2).equals("ps")) {
                                        final int indexX2 = id.indexOf("x");
                                        final int indexY2 = id.indexOf("y");
                                        final int indexZ2 = id.length() - 1;
                                        final int psx2 = Integer.parseInt(id.substring(2, indexX2));
                                        final int psy2 = Integer.parseInt(id.substring(indexX2 + 1, indexY2));
                                        final int psz2 = Integer.parseInt(id.substring(indexY2 + 1, indexZ2));
                                        final Block blockToHide = p.getWorld().getBlockAt(psx2, psy2, psz2);
                                        final YamlConfiguration hideFile = YamlConfiguration.loadConfiguration(Main.psStoneData);
                                        String entry = String.valueOf((int)blockToHide.getLocation().getX()) + "x";
                                        entry = String.valueOf(entry) + (int)blockToHide.getLocation().getY() + "y";
                                        entry = String.valueOf(entry) + (int)blockToHide.getLocation().getZ() + "z";
                                        if (blockToHide.getType() != Material.AIR) {
                                            hideFile.set(entry, (Object)(String.valueOf(blockToHide.getType().toString()) + "-" + blockToHide.getData()));
                                            try {
                                                hideFile.save(Main.psStoneData);
                                            }
                                            catch (IOException ex2) {
                                                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex2);
                                            }
                                            blockToHide.setType(Material.AIR);
                                        }
                                        else {
                                            p.sendMessage(ChatColor.YELLOW + "Приват уже скрыт.");
                                        }
                                    }
                                    else {
                                        p.sendMessage(ChatColor.YELLOW + "Не приват");
                                    }
                                }
                                else {
                                    p.sendMessage(ChatColor.RED + "У Вас нет прав");
                                }
                                return true;
                            }
                            if (args[0].equalsIgnoreCase("priority")) {
                                if (p.hasPermission("protectionstones.priority")) {
                                    if (args.length < 2) {
                                        final int priority = rgm.getRegion(id).getPriority();
                                        p.sendMessage(ChatColor.YELLOW + "Priority: " + priority);
                                        return true;
                                    }
                                    final int priority = Integer.valueOf(Integer.parseInt(args[1]));
                                    rgm.getRegion(id).setPriority(priority);
                                    try {
                                        rgm.save();
                                    }
                                    catch (Exception e2) {
                                        System.out.println("[ProtectionStones] Ошибка WorldGuard [" + e2 + "] во время сохранения файла привата.");
                                    }
                                    p.sendMessage(ChatColor.YELLOW + "Приоритет был установлен.");
                                }
                                else {
                                    p.sendMessage(ChatColor.RED + "У Вас нет прав, чтобы использовать приоритеты привата");
                                }
                                return true;
                            }
                            if (args[0].equalsIgnoreCase("flag")) {
                                if (p.hasPermission("protectionstones.flags")) {
                                    if (args.length >= 3) {
                                        if (Main.allowedFlags.contains(args[1].toLowerCase()) || p.hasPermission("protectionstones.flag." + args[1].toLowerCase()) || p.hasPermission("protectionstones.flag.*")) {
                                            final FlagHandler fh = new FlagHandler();
                                            fh.setFlag(args, rgm.getRegion(id), p);
                                        }
                                        else {
                                            p.sendMessage(ChatColor.RED + "У Вас нет прав устанавливать данный флаг");
                                        }
                                    }
                                    else {
                                        p.sendMessage(ChatColor.RED + "Используй:  /ps flag {flagname} {flagvalue}");
                                    }
                                }
                                else {
                                    p.sendMessage(ChatColor.RED + "У Вас нет прав, чтобы использовать флаги привата");
                                }
                                return true;
                            }
                            if (args[0].equalsIgnoreCase("info")) {
                                if (args.length == 1) {
                                    if (p.hasPermission("protectionstones.info")) {
                                        if (id != "") {
                                            final ProtectedRegion region = rgm.getRegion(id);
                                            if (region != null) {
                                                p.sendMessage(ChatColor.GRAY + "================ PS Info ================");
                                                p.sendMessage(ChatColor.BLUE + "Приват:" + ChatColor.YELLOW + id + ChatColor.BLUE + ", Приоритет: " + ChatColor.YELLOW + rgm.getRegion(id).getPriority());
                                                String myFlag = "";
                                                String myFlagValue = "";
                                                for (int n = DefaultFlag.flagsList.length, i = 0; i < n; ++i) {
                                                    final Flag<?> flag = (Flag<?>)DefaultFlag.flagsList[i];
                                                    if (region.getFlag((Flag)flag) != null) {
                                                        myFlagValue = region.getFlag((Flag)flag).toString();
                                                        final RegionGroupFlag groupFlag = flag.getRegionGroupFlag();
                                                        RegionGroup group = null;
                                                        if (groupFlag != null) {
                                                            group = (RegionGroup)region.getFlag((Flag)groupFlag);
                                                        }
                                                        if (group != null) {
                                                            myFlag = String.valueOf(myFlag) + flag.getName() + " -g " + region.getFlag((Flag)groupFlag) + " " + myFlagValue + ", ";
                                                        }
                                                        else {
                                                            myFlag = String.valueOf(myFlag) + flag.getName() + ": " + myFlagValue + ", ";
                                                        }
                                                    }
                                                }
                                                if (myFlag.length() > 2) {
                                                    myFlag = String.valueOf(myFlag.substring(0, myFlag.length() - 2)) + ".";
                                                    p.sendMessage(ChatColor.BLUE + "Флаги: " + ChatColor.YELLOW + myFlag);
                                                }
                                                else {
                                                    p.sendMessage(ChatColor.BLUE + "Флаги: " + ChatColor.RED + "(none)");
                                                }
                                                final DefaultDomain owners2 = region.getOwners();
                                                String ownerNames = owners2.getPlayers().toString();
                                                if (ownerNames != "[]") {
                                                    ownerNames = ownerNames.substring(1, ownerNames.length() - 1);
                                                    p.sendMessage(ChatColor.BLUE + "Овнеры: " + ChatColor.YELLOW + ownerNames);
                                                }
                                                else {
                                                    p.sendMessage(ChatColor.BLUE + "Овнеры: " + ChatColor.RED + "(no owners)");
                                                }
                                                final DefaultDomain members2 = region.getMembers();
                                                String memberNames = members2.getPlayers().toString();
                                                if (memberNames != "[]") {
                                                    memberNames = memberNames.substring(1, memberNames.length() - 1);
                                                    p.sendMessage(ChatColor.BLUE + "Участники: " + ChatColor.YELLOW + memberNames);
                                                }
                                                else {
                                                    p.sendMessage(ChatColor.BLUE + "Участники: " + ChatColor.RED + "(no members)");
                                                }
                                                final BlockVector min = region.getMinimumPoint();
                                                final BlockVector max = region.getMaximumPoint();
                                                p.sendMessage(ChatColor.BLUE + "Границы: " + ChatColor.YELLOW + "(" + min.getBlockX() + "," + min.getBlockY() + "," + min.getBlockZ() + ") -> (" + max.getBlockX() + "," + max.getBlockY() + "," + max.getBlockZ() + ")");
                                                return true;
                                            }
                                            p.sendMessage(ChatColor.YELLOW + "Регион не существует");
                                        }
                                        else {
                                            p.sendMessage(ChatColor.YELLOW + "Не найдено ни одного региона");
                                        }
                                    }
                                    else {
                                        p.sendMessage(ChatColor.RED + "У Вас нет прав использовать команду получения информации о привате");
                                    }
                                }
                                else if (args.length == 2) {
                                    if (args[1].equalsIgnoreCase("members")) {
                                        if (p.hasPermission("protectionstones.members")) {
                                            final DefaultDomain members3 = rgm.getRegion(id).getMembers();
                                            String memberNames2 = members3.getPlayers().toString();
                                            if (memberNames2 != "[]") {
                                                memberNames2 = memberNames2.substring(1, memberNames2.length() - 1);
                                                p.sendMessage(ChatColor.BLUE + "Участники: " + ChatColor.YELLOW + memberNames2);
                                            }
                                            else {
                                                p.sendMessage(ChatColor.BLUE + "Участники: " + ChatColor.RED + "(no members)");
                                            }
                                        }
                                        else {
                                            p.sendMessage(ChatColor.RED + "У Вас нет прав, чтобы использовать команды участников привата");
                                        }
                                    }
                                    else if (args[1].equalsIgnoreCase("owners")) {
                                        if (p.hasPermission("protectionstones.owners")) {
                                            final DefaultDomain owners3 = rgm.getRegion(id).getOwners();
                                            String ownerNames2 = owners3.getPlayers().toString();
                                            if (ownerNames2 != "[]") {
                                                ownerNames2 = ownerNames2.substring(1, ownerNames2.length() - 1);
                                                p.sendMessage(ChatColor.BLUE + "Овнеры: " + ChatColor.YELLOW + ownerNames2);
                                            }
                                            else {
                                                p.sendMessage(ChatColor.BLUE + "Овнеры: " + ChatColor.RED + "(no owners)");
                                            }
                                        }
                                        else {
                                            p.sendMessage(ChatColor.RED + "У Вас нет прав, чтобы использовать команды овнеров привата");
                                        }
                                    }
                                    else if (args[1].equalsIgnoreCase("flags")) {
                                        if (p.hasPermission("protectionstones.flags")) {
                                            String myFlag2 = "";
                                            String myFlagValue2 = "";
                                            for (int n2 = DefaultFlag.flagsList.length, j = 0; j < n2; ++j) {
                                                final Flag<?> flag2 = (Flag<?>)DefaultFlag.flagsList[j];
                                                if (rgm.getRegion(id).getFlag((Flag)flag2) != null) {
                                                    myFlagValue2 = rgm.getRegion(id).getFlag((Flag)flag2).toString();
                                                    myFlag2 = String.valueOf(myFlag2) + flag2.getName() + ": " + myFlagValue2 + ", ";
                                                }
                                            }
                                            if (myFlag2.length() > 2) {
                                                myFlag2 = String.valueOf(myFlag2.substring(0, myFlag2.length() - 2)) + ".";
                                                p.sendMessage(ChatColor.BLUE + "Флаги: " + ChatColor.YELLOW + myFlag2);
                                            }
                                            else {
                                                p.sendMessage(ChatColor.BLUE + "Флаги: " + ChatColor.RED + "(none)");
                                            }
                                        }
                                        else {
                                            p.sendMessage(ChatColor.RED + "У Вас нет прав, чтобы использовать команды для флагов привата");
                                        }
                                    }
                                    else {
                                        p.sendMessage(ChatColor.RED + "Используй:  /ps info members|owners|flags");
                                    }
                                }
                                else {
                                    p.sendMessage(ChatColor.RED + "Используй:  /ps info members|owners|flags");
                                }
                                return true;
                            }
                        }
                    }
                }
                if (args[0].equalsIgnoreCase("count")) {
                    int count = 0;
                    String playerName2 = null;
                    UUID playerid = null;
                    if (args.length == 1) {
                        if (!p.hasPermission("protectionstones.count")) {
                            p.sendMessage(ChatColor.RED + "У Вас нет прав!");
                        }
                        playerName2 = wg.wrapPlayer(p).getName();
                        if (Main.uuid) {
                            playerid = wg.wrapPlayer(p).getUniqueId();
                        }
                        try {
                            final Map<String, ProtectedRegion> regions = (Map<String, ProtectedRegion>)rgm.getRegions();
                            for (final String selected : regions.keySet()) {
                                if (Main.uuid) {
                                    if (!regions.get(selected).getOwners().contains(playerid) || !regions.get(selected).getId().startsWith("ps")) {
                                        continue;
                                    }
                                    ++count;
                                }
                                else {
                                    if (!regions.get(selected).getOwners().contains(playerName2) || !regions.get(selected).getId().startsWith("ps")) {
                                        continue;
                                    }
                                    ++count;
                                }
                            }
                        }
                        catch (Exception ex4) {}
                        p.sendMessage(ChatColor.YELLOW + "Количество твоих приватов: " + count);
                        return true;
                    }
                    if (args.length == 2) {
                        if (!p.hasPermission("protectionstones.count.others")) {
                            p.sendMessage(ChatColor.RED + "У Вас нет прав!");
                        }
                        playerName2 = wg.wrapOfflinePlayer(Bukkit.getOfflinePlayer(args[1])).getName();
                        if (Main.uuid) {
                            playerid = wg.wrapOfflinePlayer(Bukkit.getOfflinePlayer(args[1])).getUniqueId();
                        }
                        try {
                            final Map<String, ProtectedRegion> regions = (Map<String, ProtectedRegion>)rgm.getRegions();
                            for (final String selected : regions.keySet()) {
                                if (Main.uuid) {
                                    if (!regions.get(selected).getOwners().contains(playerid) || !regions.get(selected).getId().startsWith("ps")) {
                                        continue;
                                    }
                                    ++count;
                                }
                                else {
                                    if (!regions.get(selected).getOwners().contains(playerName2) || !regions.get(selected).getId().startsWith("ps")) {
                                        continue;
                                    }
                                    ++count;
                                }
                            }
                        }
                        catch (Exception ex5) {}
                        p.sendMessage(ChatColor.YELLOW + "Количество приватов " + args[1] + ": " + count);
                        return true;
                    }
                    p.sendMessage(ChatColor.RED + "Использование: /ps count, /ps count [player]");
                    return true;
                }
                else {
                    if (args[0].equalsIgnoreCase("region")) {
                        if (args.length < 3) {
                            p.sendMessage(ChatColor.YELLOW + "/ps region {count|list|remove|regen|disown} {playername}");
                            return true;
                        }
                        if (p.hasPermission("protectionstones.region")) {
                            final OfflinePlayer p2 = Bukkit.getOfflinePlayer(args[2]);
                            if (args[1].equalsIgnoreCase("count")) {
                                String playerName2 = null;
                                UUID playerId = null;
                                int count2 = 0;
                                try {
                                    if (Main.uuid) {
                                        playerId = wg.wrapPlayer(p2.getPlayer()).getUniqueId();
                                    }
                                    else {
                                        playerName2 = wg.wrapPlayer(p2.getPlayer()).getName();
                                    }
                                    final Map<String, ProtectedRegion> regions2 = (Map<String, ProtectedRegion>)rgm.getRegions();
                                    for (final String selected2 : regions2.keySet()) {
                                        if (Main.uuid) {
                                            if (!regions2.get(selected2).getOwners().contains(playerId) || !regions2.get(selected2).getId().startsWith("ps")) {
                                                continue;
                                            }
                                            ++count2;
                                        }
                                        else {
                                            if (!regions2.get(selected2).getOwners().contains(playerName2) || !regions2.get(selected2).getId().startsWith("ps")) {
                                                continue;
                                            }
                                            ++count2;
                                        }
                                    }
                                }
                                catch (Exception ex6) {}
                                p.sendMessage(ChatColor.YELLOW + "Количество приватов игрока " + args[2] + ": " + count2);
                                return true;
                            }
                            if (args[1].equalsIgnoreCase("list")) {
                                final Map<String, ProtectedRegion> regions3 = (Map<String, ProtectedRegion>)rgm.getRegions();
                                final String name = args[2].toLowerCase();
                                UUID playerid2 = null;
                                if (Main.uuid) {
                                    playerid2 = Bukkit.getOfflinePlayer(args[2]).getUniqueId();
                                }
                                final int size = regions3.size();
                                final String[] regionIDList = new String[size];
                                String regionMessage = "";
                                int index = 0;
                                for (final String idname : regions3.keySet()) {
                                    try {
                                        if (!idname.startsWith("ps")) {
                                            continue;
                                        }
                                        if (Main.uuid) {
                                            if (!regions3.get(idname).getOwners().contains(playerid2)) {
                                                continue;
                                            }
                                            regionIDList[index] = idname;
                                            regionMessage = String.valueOf(regionMessage) + regionIDList[index] + ", ";
                                            ++index;
                                        }
                                        else {
                                            if (!regions3.get(idname).getOwners().contains(name)) {
                                                continue;
                                            }
                                            regionIDList[index] = idname;
                                            regionMessage = String.valueOf(regionMessage) + regionIDList[index] + ", ";
                                            ++index;
                                        }
                                    }
                                    catch (Exception ex7) {}
                                }
                                if (index == 0) {
                                    p.sendMessage(ChatColor.YELLOW + "Не найдено приватов для " + name);
                                }
                                else {
                                    regionMessage = String.valueOf(regionMessage.substring(0, regionMessage.length() - 2)) + ".";
                                    p.sendMessage(ChatColor.YELLOW + "Приваты игрока " + args[2] + ": " + regionMessage);
                                }
                                return true;
                            }
                            if (args[1].equalsIgnoreCase("remove") || args[1].equalsIgnoreCase("regen") || args[1].equalsIgnoreCase("disown")) {
                                final RegionManager mgr = wg.getRegionManager(p.getWorld());
                                final Map<String, ProtectedRegion> regions3 = (Map<String, ProtectedRegion>)mgr.getRegions();
                                final String name = args[2].toLowerCase();
                                UUID playerid2 = null;
                                if (Main.uuid) {
                                    playerid2 = Bukkit.getOfflinePlayer(name).getUniqueId();
                                }
                                final int size = regions3.size();
                                final String[] regionIDList = new String[size];
                                int index2 = 0;
                                for (final String idname2 : regions3.keySet()) {
                                    try {
                                        if (!idname2.startsWith("ps")) {
                                            continue;
                                        }
                                        if (Main.uuid) {
                                            if (!regions3.get(idname2).getOwners().contains(playerid2)) {
                                                continue;
                                            }
                                            regionIDList[index2] = idname2;
                                            ++index2;
                                        }
                                        else {
                                            if (!regions3.get(idname2).getOwners().getPlayers().contains(name)) {
                                                continue;
                                            }
                                            regionIDList[index2] = idname2;
                                            ++index2;
                                        }
                                    }
                                    catch (Exception ex8) {}
                                }
                                if (index2 == 0) {
                                    p.sendMessage(ChatColor.YELLOW + "Не найдено приватов для " + args[2]);
                                }
                                else {
                                    for (int k = 0; k < index2; ++k) {
                                        if (args[1].equalsIgnoreCase("disown")) {
                                            final DefaultDomain owners4 = rgm.getRegion(regionIDList[k]).getOwners();
                                            owners4.removePlayer(name);
                                            if (Main.uuid) {
                                                owners4.removePlayer(playerid2);
                                            }
                                            rgm.getRegion(regionIDList[k]).setOwners(owners4);
                                        }
                                        else {
                                            if (args[1].equalsIgnoreCase("regen")) {
                                                if (this.getServer().getPluginManager().getPlugin("WorldEdit") != null) {
                                                    Bukkit.dispatchCommand((CommandSender)p, "приват выбран " + regionIDList[k]);
                                                    Bukkit.dispatchCommand((CommandSender)p, "/regen");
                                                }
                                            }
                                            else if (regionIDList[k].substring(0, 2).equals("ps")) {
                                                final int indexX3 = regionIDList[k].indexOf("x");
                                                final int indexY3 = regionIDList[k].indexOf("y");
                                                final int indexZ3 = regionIDList[k].length() - 1;
                                                final int psx3 = Integer.parseInt(regionIDList[k].substring(2, indexX3));
                                                final int psy3 = Integer.parseInt(regionIDList[k].substring(indexX3 + 1, indexY3));
                                                final int psz3 = Integer.parseInt(regionIDList[k].substring(indexY3 + 1, indexZ3));
                                                final Block blockToRemove = p.getWorld().getBlockAt(psx3, psy3, psz3);
                                                blockToRemove.setType(Material.AIR);
                                            }
                                            mgr.removeRegion(regionIDList[k]);
                                        }
                                    }
                                    p.sendMessage(ChatColor.YELLOW + "Приваты " + name + " были убраны.");
                                    try {
                                        rgm.save();
                                    }
                                    catch (Exception e3) {
                                        System.out.println("[ProtectionStones] Ошибка WorldGuard [" + e3 + "] во время сохранения файла привата.");
                                    }
                                }
                                return true;
                            }
                        }
                        else {
                            p.sendMessage(ChatColor.RED + "У Вас нет прав использовать команды для своего привата.");
                        }
                    }
                    if ((args[0].equalsIgnoreCase("tp") && p.hasPermission("protectionstones.tp")) || (args[0].equalsIgnoreCase("home") && p.hasPermission("protectionstones.home"))) {
                        final String name2 = p.getName().toLowerCase();
                        UUID playerid3 = null;
                        int rgnum = 0;
                        int index3 = 0;
                        final Map<Integer, String> playerRegions = new HashMap<Integer, String>();
                        if (args[0].equalsIgnoreCase("tp")) {
                            if (args.length != 3) {
                                p.sendMessage(ChatColor.RED + "Использование: /ps tp [player] [num]");
                                return true;
                            }
                            rgnum = Integer.parseInt(args[2]);
                        }
                        else {
                            if (args.length != 2) {
                                p.sendMessage(ChatColor.RED + "Использование: /ps home [num]");
                                p.sendMessage(ChatColor.YELLOW + "Чтобы узнать количество Ваших приватов, введите /ps count. Используйте любое число в пределах диапазона, чтобы телепортироваться на этот приват");
                                return true;
                            }
                            rgnum = Integer.parseInt(args[1]);
                        }
                        if (Main.uuid) {
                            playerid3 = p.getUniqueId();
                        }
                        try {
                            final Map<String, ProtectedRegion> regions4 = (Map<String, ProtectedRegion>)rgm.getRegions();
                            for (final String selected3 : regions4.keySet()) {
                                if (selected3.startsWith("ps")) {
                                    if (Main.uuid) {
                                        if (!regions4.get(selected3).getOwners().contains(playerid3)) {
                                            continue;
                                        }
                                        ++index3;
                                        playerRegions.put(index3, selected3);
                                    }
                                    else {
                                        if (!regions4.get(selected3).getOwners().contains(name2)) {
                                            continue;
                                        }
                                        ++index3;
                                        playerRegions.put(index3, selected3);
                                    }
                                }
                            }
                        }
                        catch (Exception ex9) {}
                        if (args[0].equalsIgnoreCase("tp")) {
                            LocalPlayer lp;
                            try {
                                lp = wg.wrapOfflinePlayer(Bukkit.getOfflinePlayer(args[1]));
                            }
                            catch (Exception e4) {
                                p.sendMessage(ChatColor.RED + "Ошибка при поиске приватов игрока " + args[1] + ". Пожалуйста проверьте вписанный Вами никнейм.");
                                return true;
                            }
                            if (rgnum <= 0) {
                                p.sendMessage(ChatColor.RED + "Пожалуйста введите число больше 0.");
                                return true;
                            }
                            if (index3 <= 0) {
                                p.sendMessage(ChatColor.RED + lp.getName() + " не имеет ни одного привата!");
                                return true;
                            }
                            if (rgnum > index3) {
                                p.sendMessage(ChatColor.RED + lp.getName() + " имеет только " + index3 + " приватов!");
                                return true;
                            }
                        }
                        else {
                            if (!args[0].equalsIgnoreCase("home")) {
                                p.sendMessage(ChatColor.RED + "У Вас нет прав.");
                                return true;
                            }
                            try {
                                wg.wrapPlayer(p);
                            }
                            catch (Exception e7) {
                                p.sendMessage(ChatColor.RED + "Ошибка при поиске ваших приватов.");
                                return true;
                            }
                            if (rgnum <= 0) {
                                p.sendMessage(ChatColor.RED + "Пожалуйста введите число больше 0.");
                                return true;
                            }
                            if (index3 <= 0) {
                                p.sendMessage(ChatColor.RED + "У Вас нет ни одного привата!");
                            }
                            if (rgnum > index3) {
                                p.sendMessage(ChatColor.RED + "У Вас  есть только " + index3 + " приватов!");
                                return true;
                            }
                        }
                        if (rgnum <= index3) {
                            final String region5 = rgm.getRegion((String)playerRegions.get(rgnum)).getId();
                            System.out.print(region5);
                            final String[] pos = region5.split("x|y|z");
                            System.out.print(pos.toString());
                            if (pos.length == 3) {
                                pos[0] = pos[0].substring(2);
                                p.sendMessage(ChatColor.GREEN + "Телепортируем...");
                                final int tpx = Integer.parseInt(pos[0]);
                                final int tpy = Integer.parseInt(pos[1]);
                                final int tpz = Integer.parseInt(pos[2]);
                                final Location tploc = new Location(p.getWorld(), (double)tpx, (double)tpy, (double)tpz);
                                p.teleport(tploc);
                            }
                            else {
                                p.sendMessage(ChatColor.RED + "Ошибка при телепортации в защищенный приват!");
                            }
                            return true;
                        }
                        p.sendMessage(ChatColor.RED + "Ошибка в поиске привата для телепортации!");
                    }
                    else if (args[0].equalsIgnoreCase("admin")) {
                        if (!p.hasPermission("protectionstones.admin")) {
                            p.sendMessage(ChatColor.RED + "У Вас нет прав.");
                        }
                        else if (args.length < 2) {
                            p.sendMessage(ChatColor.RED + "Правильное Использование: /ps admin {version|settings|hide|unhide|");
                            p.sendMessage(ChatColor.RED + "                          cleanup|lastlogon|lastlogons|stats}");
                        }
                        else if (args.length > 1) {
                            if (args[1].equalsIgnoreCase("version")) {
                                p.sendMessage(ChatColor.YELLOW + "ProtectionStones " + this.getDescription().getVersion());
                                p.sendMessage(ChatColor.YELLOW + "CraftBukkit  " + Bukkit.getVersion());
                            }
                            else if (args[1].equalsIgnoreCase("settings")) {
                                p.sendMessage(this.getConfig().saveToString().split("\n"));
                            }
                            if (args[1].equalsIgnoreCase("hide") || args[1].equalsIgnoreCase("unhide")) {
                                final RegionManager mgr = wg.getRegionManager(p.getWorld());
                                final Map<String, ProtectedRegion> regions3 = (Map<String, ProtectedRegion>)mgr.getRegions();
                                if (regions3.isEmpty()) {
                                    p.sendMessage(ChatColor.YELLOW + "Не найдено приватов ProtectionStones");
                                }
                                final int regionSize = regions3.size();
                                final String[] regionIDList2 = new String[regionSize];
                                String blockMaterial = "AIR";
                                String hMessage = "скрыты";
                                int index2 = 0;
                                for (final String idname2 : regions3.keySet()) {
                                    try {
                                        if (!idname2.substring(0, 2).equals("ps")) {
                                            continue;
                                        }
                                        regionIDList2[index2] = idname2;
                                        ++index2;
                                    }
                                    catch (Exception ex10) {}
                                }
                                if (index2 == 0) {
                                    p.sendMessage(ChatColor.YELLOW + "Не найдено приватов ProtectionStones");
                                }
                                else {
                                    for (int k = 0; k < index2; ++k) {
                                        final int indexX3 = regionIDList2[k].indexOf("x");
                                        final int indexY3 = regionIDList2[k].indexOf("y");
                                        final int indexZ3 = regionIDList2[k].length() - 1;
                                        final int psx3 = Integer.parseInt(regionIDList2[k].substring(2, indexX3));
                                        final int psy3 = Integer.parseInt(regionIDList2[k].substring(indexX3 + 1, indexY3));
                                        final int psz3 = Integer.parseInt(regionIDList2[k].substring(indexY3 + 1, indexZ3));
                                        final Block blockToChange = p.getWorld().getBlockAt(psx3, psy3, psz3);
                                        blockMaterial = "AIR";
                                        String entry2 = String.valueOf((int)blockToChange.getLocation().getX()) + "x";
                                        entry2 = String.valueOf(entry2) + (int)blockToChange.getLocation().getY() + "y";
                                        entry2 = String.valueOf(entry2) + (int)blockToChange.getLocation().getZ() + "z";
                                        String subtype2 = null;
                                        if (args[1].equalsIgnoreCase("unhide")) {
                                            if (blockToChange.getType() == Material.getMaterial(blockMaterial)) {
                                                final YamlConfiguration hideFile2 = YamlConfiguration.loadConfiguration(Main.psStoneData);
                                                blockMaterial = hideFile2.getString(entry2);
                                                if (blockMaterial.contains("-")) {
                                                    final String[] str2 = blockMaterial.split("-");
                                                    blockMaterial = str2[0];
                                                    subtype2 = str2[1];
                                                }
                                                hideFile2.set(entry2, (Object)null);
                                                try {
                                                    hideFile2.save(Main.psStoneData);
                                                }
                                                catch (IOException ex3) {
                                                    Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex3);
                                                }
                                            }
                                        }
                                        else if (args[1].equalsIgnoreCase("hide")) {
                                            if (blockToChange.getType() != Material.getMaterial(blockMaterial)) {
                                                final YamlConfiguration hideFile2 = YamlConfiguration.loadConfiguration(Main.psStoneData);
                                                hideFile2.set(entry2, (Object)blockToChange.getType().toString());
                                                try {
                                                    hideFile2.save(Main.psStoneData);
                                                }
                                                catch (IOException ex3) {
                                                    Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex3);
                                                }
                                            }
                                            else if (subtype2 == null || blockToChange.getData() != (byte)Integer.parseInt(subtype2)) {}
                                        }
                                        blockToChange.setType(Material.getMaterial(blockMaterial));
                                        if (subtype2 != null) {
                                            blockToChange.setData((byte)Integer.parseInt(subtype2));
                                        }
                                    }
                                }
                                if (args[1].equalsIgnoreCase("unhide")) {
                                    hMessage = "показаны";
                                }
                                p.sendMessage(ChatColor.YELLOW + "Все ProtectionStones были " + hMessage);
                            }
                            else if (args[1].equalsIgnoreCase("cleanup")) {
                                if (args.length < 3) {
                                    p.sendMessage(ChatColor.YELLOW + "/ps admin cleanup {remove|regen|disown} {days}");
                                    return true;
                                }
                                if (args[2].equalsIgnoreCase("remove") || args[2].equalsIgnoreCase("regen") || args[2].equalsIgnoreCase("disown")) {
                                    int days = 30;
                                    if (args.length > 3) {
                                        days = Integer.parseInt(args[3]);
                                    }
                                    p.sendMessage(ChatColor.YELLOW + "Очистка " + args[2] + " " + days + " days");
                                    p.sendMessage(ChatColor.YELLOW + "================");
                                    final RegionManager mgr2 = wg.getRegionManager(p.getWorld());
                                    final Map<String, ProtectedRegion> regions = (Map<String, ProtectedRegion>)mgr2.getRegions();
                                    final int size2 = regions.size();
                                    String name3 = "";
                                    int index4 = 0;
                                    final String[] regionIDList3 = new String[size2];
                                    final OfflinePlayer[] offlinePlayerList = this.getServer().getOfflinePlayers();
                                    for (int playerCount2 = offlinePlayerList.length, iii = 0; iii < playerCount2; ++iii) {
                                        final long lastPlayed = (System.currentTimeMillis() - offlinePlayerList[iii].getLastPlayed()) / 86400000L;
                                        if (lastPlayed >= days) {
                                            index4 = 0;
                                            name3 = offlinePlayerList[iii].getName().toLowerCase();
                                            for (final String idname3 : regions.keySet()) {
                                                try {
                                                    if (!regions.get(idname3).getOwners().getPlayers().contains(name3)) {
                                                        continue;
                                                    }
                                                    regionIDList3[index4] = idname3;
                                                    ++index4;
                                                }
                                                catch (Exception ex11) {}
                                            }
                                            if (index4 == 0) {
                                                p.sendMessage(ChatColor.YELLOW + "Не найдено приватов для " + name3);
                                            }
                                            else {
                                                p.sendMessage(ChatColor.YELLOW + args[2] + ": " + name3);
                                                for (int l = 0; l < index4; ++l) {
                                                    if (args[2].equalsIgnoreCase("disown")) {
                                                        final DefaultDomain owners5 = rgm.getRegion(regionIDList3[l]).getOwners();
                                                        owners5.removePlayer(name3);
                                                        rgm.getRegion(regionIDList3[l]).setOwners(owners5);
                                                    }
                                                    else {
                                                        if (args[2].equalsIgnoreCase("regen")) {
                                                            if (this.getServer().getPluginManager().getPlugin("WorldEdit") != null) {
                                                                Bukkit.dispatchCommand((CommandSender)p, "region select " + regionIDList3[l]);
                                                                Bukkit.dispatchCommand((CommandSender)p, "/regen");
                                                            }
                                                        }
                                                        else if (regionIDList3[l].substring(0, 2).equals("ps")) {
                                                            final int indexX4 = regionIDList3[l].indexOf("x");
                                                            final int indexY4 = regionIDList3[l].indexOf("y");
                                                            final int indexZ4 = regionIDList3[l].length() - 1;
                                                            final int psx4 = Integer.parseInt(regionIDList3[l].substring(2, indexX4));
                                                            final int psy4 = Integer.parseInt(regionIDList3[l].substring(indexX4 + 1, indexY4));
                                                            final int psz4 = Integer.parseInt(regionIDList3[l].substring(indexY4 + 1, indexZ4));
                                                            final Block blockToRemove2 = p.getWorld().getBlockAt(psx4, psy4, psz4);
                                                            blockToRemove2.setType(Material.AIR);
                                                        }
                                                        mgr2.removeRegion(regionIDList3[l]);
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    try {
                                        rgm.save();
                                    }
                                    catch (Exception e4) {
                                        System.out.println("[ProtectionStones] Ошибка WorldGuard [" + e4 + "] во время сохранения файла привата.");
                                    }
                                    p.sendMessage(ChatColor.YELLOW + "================");
                                    p.sendMessage(ChatColor.YELLOW + "Очистка " + args[2] + " закончена");
                                    return true;
                                }
                            }
                            else if (args[1].equalsIgnoreCase("lastlogon")) {
                                System.out.print("ProtectionStones // Последние заходы // debug #0");
                                if (args.length > 2) {
                                    final String playerName2 = args[2];
                                    System.out.print("ProtectionStones // Последние заходы // debug #1");
                                    if (Bukkit.getOfflinePlayer(playerName2).getFirstPlayed() > 0L) {
                                        System.out.print("ProtectionStones LastLogon debug #2");
                                        final long lastPlayed2 = (System.currentTimeMillis() - Bukkit.getOfflinePlayer(playerName2).getLastPlayed()) / 86400000L;
                                        p.sendMessage(ChatColor.YELLOW + playerName2 + " последний раз играл " + lastPlayed2 + " дней назад");
                                        if (Bukkit.getOfflinePlayer(playerName2).isBanned()) {
                                            System.out.print("ProtectionStones // Последние заходы // debug #3");
                                            p.sendMessage(ChatColor.YELLOW + playerName2 + " заблокирован");
                                        }
                                    }
                                    else {
                                        p.sendMessage(ChatColor.YELLOW + "Игрок не найден.");
                                    }
                                }
                                else {
                                    p.sendMessage(ChatColor.YELLOW + "Необходимо имя игрока.");
                                }
                            }
                            else if (args[1].equalsIgnoreCase("lastlogons")) {
                                int days = 0;
                                if (args.length > 2) {
                                    days = Integer.parseInt(args[2]);
                                }
                                final OfflinePlayer[] offlinePlayerList2 = this.getServer().getOfflinePlayers();
                                final int playerCount3 = offlinePlayerList2.length;
                                int playerCounter = 0;
                                p.sendMessage(new StringBuilder().append(ChatColor.YELLOW).append(days).append(" Дней Плюс:").toString());
                                p.sendMessage(ChatColor.YELLOW + "================");
                                Arrays.sort(offlinePlayerList2, new PlayerComparator());
                                for (int iii2 = 0; iii2 < playerCount3; ++iii2) {
                                    final long lastPlayed3 = (System.currentTimeMillis() - offlinePlayerList2[iii2].getLastPlayed()) / 86400000L;
                                    if (lastPlayed3 >= days) {
                                        ++playerCounter;
                                        p.sendMessage(ChatColor.YELLOW + offlinePlayerList2[iii2].getName() + " " + lastPlayed3 + " дней");
                                    }
                                }
                                p.sendMessage(ChatColor.YELLOW + "================");
                                p.sendMessage(new StringBuilder().append(ChatColor.YELLOW).append(playerCounter).append(" Всего игроков показано").toString());
                                p.sendMessage(new StringBuilder().append(ChatColor.YELLOW).append(playerCount3).append(" Всего игроков проверено").toString());
                            }
                            else if (args[1].equalsIgnoreCase("stats")) {
                                if (args.length > 2) {
                                    final String playerName2 = args[2];
                                    if (Bukkit.getOfflinePlayer(playerName2).getFirstPlayed() > 0L) {
                                        p.sendMessage(ChatColor.YELLOW + playerName2 + ":");
                                        p.sendMessage(ChatColor.YELLOW + "================");
                                        final long firstPlayed = (System.currentTimeMillis() - Bukkit.getOfflinePlayer(playerName2).getFirstPlayed()) / 86400000L;
                                        p.sendMessage(ChatColor.YELLOW + "Первый раз играл " + firstPlayed + " дней назад.");
                                        final long lastPlayed4 = (System.currentTimeMillis() - Bukkit.getOfflinePlayer(playerName2).getLastPlayed()) / 86400000L;
                                        p.sendMessage(ChatColor.YELLOW + "Последний раз играл " + lastPlayed4 + " дней назад.");
                                        String banMessage = "Не заблокирован";
                                        if (Bukkit.getOfflinePlayer(playerName2).isBanned()) {
                                            banMessage = "Заблокирован";
                                        }
                                        p.sendMessage(ChatColor.YELLOW + banMessage);
                                        int count3 = 0;
                                        try {
                                            LocalPlayer thePlayer = null;
                                            thePlayer = wg.wrapOfflinePlayer(Bukkit.getOfflinePlayer(args[2]));
                                            count3 = rgm.getRegionCountOfPlayer(thePlayer);
                                        }
                                        catch (Exception ex12) {}
                                        p.sendMessage(ChatColor.YELLOW + "Приваты " + count3);
                                        p.sendMessage(ChatColor.YELLOW + "================");
                                    }
                                    else {
                                        p.sendMessage(ChatColor.YELLOW + "Игрок не найден.");
                                    }
                                    return true;
                                }
                                p.sendMessage(ChatColor.YELLOW + "Мир:");
                                p.sendMessage(ChatColor.YELLOW + "================");
                                int count4 = 0;
                                try {
                                    count4 = rgm.size();
                                }
                                catch (Exception ex13) {}
                                p.sendMessage(ChatColor.YELLOW + "Приваты: " + count4);
                                p.sendMessage(ChatColor.YELLOW + "================");
                            }
                        }
                    }
                    else {
                        if (args[0].equalsIgnoreCase("reclaim")) {
                            if (p.hasPermission("protectionstones.reclaim")) {
                                final ProtectedRegion region = rgm.getRegion(id);
                                if (region != null) {
                                    if (id.substring(0, 2).equals("ps")) {
                                        final int indexX5 = id.indexOf("x");
                                        final int indexY5 = id.indexOf("y");
                                        final int indexZ5 = id.length() - 1;
                                        final int psx5 = Integer.parseInt(id.substring(2, indexX5));
                                        final int psy5 = Integer.parseInt(id.substring(indexX5 + 1, indexY5));
                                        final int psz5 = Integer.parseInt(id.substring(indexY5 + 1, indexZ5));
                                        final Block blockToUnhide2 = p.getWorld().getBlockAt(psx5, psy5, psz5);
                                        String entry = null;
                                        String setmat = null;
                                        if (blockToUnhide2.getType() == Material.AIR) {
                                            final YamlConfiguration hideFile3 = YamlConfiguration.loadConfiguration(Main.psStoneData);
                                            entry = String.valueOf((int)blockToUnhide2.getLocation().getX()) + "x";
                                            entry = String.valueOf(entry) + (int)blockToUnhide2.getLocation().getY() + "y";
                                            entry = String.valueOf(entry) + (int)blockToUnhide2.getLocation().getZ() + "z";
                                            setmat = hideFile3.getString(entry);
                                            hideFile3.set(entry, (Object)null);
                                            try {
                                                hideFile3.save(Main.psStoneData);
                                            }
                                            catch (IOException ex) {
                                                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
                                            }
                                        }
                                        int type = 0;
                                        String blocktypedata = String.valueOf(blockToUnhide2.getType().toString()) + "-" + blockToUnhide2.getData();
                                        if (Main.mats.contains(blocktypedata)) {
                                            type = 1;
                                        }
                                        else if (Main.mats.contains(blockToUnhide2.getType().toString())) {
                                            type = 2;
                                        }
                                        if (setmat != null) {
                                            blockToUnhide2.setType(Material.getMaterial(setmat));
                                        }
                                        final BlockVector max2 = region.getMaximumPoint();
                                        final BlockVector min2 = region.getMinimumPoint();
                                        final Vector middle = max2.add((Vector)min2).multiply(0.5);
                                        final Collection<Block> blocks = new HashSet<Block>();
                                        if (type == 2) {
                                            blocktypedata = blockToUnhide2.getType().toString();
                                        }
                                        if (this.StoneTypeData.RegionY(blocktypedata) == 0) {
                                            final double xx = middle.getX();
                                            final double zz = middle.getZ();
                                            for (double yy = 0.0; yy <= p.getWorld().getMaxHeight(); ++yy) {
                                                final Block block = new Location(p.getWorld(), xx, yy, zz).getBlock();
                                                if (Main.mats.contains(String.valueOf(block.getType().toString()) + "-" + block.getData())) {
                                                    blocks.add(new Location(p.getWorld(), xx, yy, zz).getBlock());
                                                }
                                                else if (Main.mats.contains(block.getType())) {
                                                    blocks.add(new Location(p.getWorld(), xx, yy, zz).getBlock());
                                                }
                                            }
                                        }
                                        localPlayer = wg.wrapPlayer(p);
                                        if (region.isOwner(localPlayer) || p.hasPermission("protectionstones.superowner")) {
                                            Block middleblock = null;
                                            Block it = null;
                                            if (!blocks.isEmpty()) {
                                                it = blocks.iterator().next();
                                            }
                                            if (it != null && this.StoneTypeData.RegionY(String.valueOf(it.getType().toString()) + "-" + it.getData()) == 0) {
                                                middleblock = it;
                                            }
                                            else if (it != null && this.StoneTypeData.RegionY(it.getType().toString()) == 0) {
                                                middleblock = it;
                                            }
                                            else {
                                                middleblock = p.getWorld().getBlockAt((int)middle.getX(), (int)middle.getY(), (int)middle.getZ());
                                            }
                                            if (!this.StoneTypeData.NoDrop(String.valueOf(middleblock.getType().toString()) + "-" + middleblock.getData()) && !this.StoneTypeData.NoDrop(middleblock.getType().toString())) {
                                                final ItemStack oreblock = new ItemStack(middleblock.getType(), 1, (short)middleblock.getData());
                                                boolean freeSpace = false;
                                                ItemStack[] contents;
                                                for (int length = (contents = p.getInventory().getContents()).length, n3 = 0; n3 < length; ++n3) {
                                                    final ItemStack is = contents[n3];
                                                    if (!freeSpace && is == null) {
                                                        freeSpace = true;
                                                        break;
                                                    }
                                                }
                                                if (freeSpace) {
                                                    final PlayerInventory inventory = p.getInventory();
                                                    inventory.addItem(new ItemStack[] { oreblock });
                                                    middleblock.setType(Material.AIR);
                                                    rgm.removeRegion(id);
                                                    try {
                                                        rgm.save();
                                                    }
                                                    catch (Exception e5) {
                                                        System.out.println("[ProtectionStones] Ошибка WorldGuard [" + e5 + "] во время сохранения файла привата.");
                                                    }
                                                    p.sendMessage(ChatColor.YELLOW + "Приват был успешно убран!");
                                                }
                                                else {
                                                    p.sendMessage(ChatColor.RED + "Ваш инвентарь заполнен! Освободите пару лишних слотов.");
                                                }
                                            }
                                            else {
                                                middleblock.setType(Material.AIR);
                                                rgm.removeRegion(id);
                                                try {
                                                    rgm.save();
                                                }
                                                catch (Exception e6) {
                                                    System.out.println("[ProtectionStones] Ошибка WorldGuard [" + e6 + "] во время сохранения файла привата.");
                                                }
                                                p.sendMessage(ChatColor.YELLOW + "Приват был успешно убран!");
                                            }
                                        }
                                        else {
                                            p.sendMessage(ChatColor.YELLOW + "Вы не являетесь овнером данного привата.");
                                        }
                                    }
                                    else {
                                        p.sendMessage(ChatColor.YELLOW + "Приват не является приватом плагина ProtectionStones.");
                                    }
                                }
                            }
                            else {
                                p.sendMessage(ChatColor.RED + "У Вас нет прав, чтобы использовать Reclaim команду.");
                            }
                            return true;
                        }
                        p.sendMessage(ChatColor.RED + "Неизвестная команда. Используйте /ps help, чтобы получить больше информации.");
                    }
                }
            }
        }
        else {
            s.sendMessage(ChatColor.RED + "PS не может быть использован в консоли :(");
        }
        return true;
    }
    
    public static Object getFlagValue(final Flag<?> flag, final Object value) {
        if (value == null) {
            return null;
        }
        final String valueString = value.toString().trim();
        if (!(flag instanceof StateFlag)) {
            return null;
        }
        if (valueString.equalsIgnoreCase("allow")) {
            return StateFlag.State.ALLOW;
        }
        if (valueString.equalsIgnoreCase("deny")) {
            return StateFlag.State.DENY;
        }
        return null;
    }
    
    protected void setBlock(final World theWorld, final int x, final int y, final int z, final Material mat) {
        final Block blockToChange = theWorld.getBlockAt(x, y, z);
        blockToChange.setType(mat);
    }
    
    protected Material getBlock(final World theWorld, final int x, final int y, final int z) {
        final Block blockToReturn = theWorld.getBlockAt(x, y, z);
        return blockToReturn.getType();
    }
    
    private boolean initConfig() {
        Main.config = (FileConfiguration)new YamlConfiguration();
        try {
            Main.config.load(Main.conf);
        }
        catch (IOException | InvalidConfigurationException ex3) {
            final Exception ex2 = null;
            final Exception ex = ex2;
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.print("[ProtectionStones] Checking Configuration Version");
        if (this.getConfig().get("ConfVer") == null) {
            System.out.print("Config is outdated, this WILL generate errors, please refresh it!");
            return false;
        }
        if (Main.config.getInt("ConfVer") == 1) {
            System.out.print("Config is Correct version, continuing start-up");
            return true;
        }
        if (Main.config.getInt("ConfVer") > 1) {
            System.out.print("Config version is higher than required version, this might cause trouble");
            return true;
        }
        this.fixInitialHidden(Main.config.get("Block"));
        System.out.print("Config is outdated, this WILL generate errors, please refresh it!");
        return true;
    }
    
    private void fixInitialHidden(final Object block) {
        final YamlConfiguration hideFile = YamlConfiguration.loadConfiguration(Main.psStoneData);
        final WorldGuardPlugin wg = (WorldGuardPlugin)Main.wgd;
        System.out.print("Patching initial hiddenpstones.yml");
        hideFile.set("InitialHideDone", (Object)true);
        for (final World world : Bukkit.getWorlds()) {
            final RegionManager rgm = wg.getRegionManager(world);
            final Map<String, ProtectedRegion> regions = (Map<String, ProtectedRegion>)rgm.getRegions();
            for (final String selected : regions.keySet()) {
                if (selected.startsWith("ps")) {
                    final Material mat = Material.valueOf(block.toString());
                    String sub = null;
                    if (block.toString().contains("-")) {
                        sub = block.toString().split("-")[1];
                    }
                    if (sub != null) {
                        hideFile.set(selected, (Object)(String.valueOf(mat.toString()) + "-" + sub));
                    }
                    else {
                        hideFile.set(selected, (Object)(String.valueOf(mat.toString()) + "-0"));
                    }
                }
            }
        }
        try {
            hideFile.save(Main.psStoneData);
        }
        catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}

