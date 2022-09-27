package hiti.main;

import com.sk89q.worldguard.bukkit.*;
import com.sk89q.worldguard.protection.regions.*;
import org.bukkit.entity.*;
import org.bukkit.*;
import java.util.*;
import com.google.common.base.*;
import com.sk89q.worldguard.protection.flags.*;

public class FlagHandler
{
    WorldGuardPlugin wg;
    
    public FlagHandler() {
        this.wg = (WorldGuardPlugin)Main.wgd;
    }
    
    @SuppressWarnings("unchecked")
	public void setFlag(final String[] args, final ProtectedRegion region, final Player p) {
        final Flag<?> rawFlag = (Flag<?>)DefaultFlag.fuzzyMatchFlag(null, args[1]);
        if (rawFlag instanceof StateFlag) {
            final StateFlag flag = (StateFlag)rawFlag;
            if (args[2].equalsIgnoreCase("default")) {
                region.setFlag((Flag)flag, (Object)flag.getDefault());
                region.setFlag((Flag)flag.getRegionGroupFlag(), (Object)null);
                p.sendMessage(ChatColor.YELLOW + args[1] + " flag has been set.");
            }
            else {
                RegionGroup group = null;
                if (Arrays.toString(args).contains("-g")) {
                    int i = 0;
                    for (final String s : args) {
                        ++i;
                        if (s.equalsIgnoreCase("-g")) {
                            group = this.getRegionGroup(args[i]);
                        }
                    }
                }
                if (Arrays.toString(args).contains("allow")) {
                    region.setFlag((Flag)flag, (Object)StateFlag.State.ALLOW);
                    if (group != null) {
                        region.setFlag((Flag)flag.getRegionGroupFlag(), (Object)group);
                    }
                    p.sendMessage(ChatColor.YELLOW + args[1] + " flag has been set.");
                }
                else if (Arrays.toString(args).contains("deny")) {
                    region.setFlag((Flag)flag, (Object)StateFlag.State.DENY);
                    if (group != null) {
                        region.setFlag((Flag)flag.getRegionGroupFlag(), (Object)group);
                    }
                    p.sendMessage(ChatColor.YELLOW + args[1] + " flag has been set.");
                }
                else if (group != null) {
                    region.setFlag((Flag)flag.getRegionGroupFlag(), (Object)group);
                    p.sendMessage(ChatColor.YELLOW + args[1] + " flag has been set.");
                }
                else {
                    p.sendMessage(ChatColor.YELLOW + args[1] + " flag has " + ChatColor.RED + "not" + ChatColor.YELLOW + " been set.");
                }
            }
        }
        else if (rawFlag instanceof DoubleFlag) {
            final DoubleFlag flag2 = (DoubleFlag)rawFlag;
            if (args[2].equalsIgnoreCase("default")) {
                region.setFlag((Flag)flag2, (Object)flag2.getDefault());
                region.setFlag((Flag)flag2.getRegionGroupFlag(), (Object)null);
            }
            else {
                region.setFlag((Flag)flag2, (Object)Double.parseDouble(args[1]));
            }
            p.sendMessage(ChatColor.YELLOW + args[1] + " flag has been set.");
        }
        else if (rawFlag instanceof IntegerFlag) {
            final IntegerFlag flag3 = (IntegerFlag)rawFlag;
            if (args[2].equalsIgnoreCase("default")) {
                region.setFlag((Flag)flag3, (Object)flag3.getDefault());
                region.setFlag((Flag)flag3.getRegionGroupFlag(), (Object)null);
            }
            else {
                region.setFlag((Flag)flag3, (Object)Integer.parseInt(args[1]));
            }
            p.sendMessage(ChatColor.YELLOW + args[1] + " flag has been set.");
        }
        else if (rawFlag instanceof StringFlag) {
            final StringFlag flag4 = (StringFlag)rawFlag;
            if (args[2].equalsIgnoreCase("default")) {
                region.setFlag((Flag)flag4, (Object)flag4.getDefault());
                region.setFlag((Flag)flag4.getRegionGroupFlag(), (Object)null);
            }
            else {
                final String flagValue = Joiner.on(" ").join((Object[])args).substring(args[0].length() + args[1].length() + 2);
                final String msg = flagValue.replaceAll("%player%", p.getName());
                region.setFlag((Flag)flag4, (Object)msg);
            }
            p.sendMessage(ChatColor.YELLOW + args[1] + " flag has been set.");
        }
        else if (rawFlag instanceof BooleanFlag) {
            final BooleanFlag flag5 = (BooleanFlag)rawFlag;
            if (args[2].equalsIgnoreCase("default")) {
                region.setFlag((Flag)flag5, (Object)flag5.getDefault());
                region.setFlag((Flag)flag5.getRegionGroupFlag(), (Object)null);
                p.sendMessage(ChatColor.YELLOW + args[1] + " flag has been set.");
            }
            else if (args[2].equalsIgnoreCase("true")) {
                region.setFlag((Flag)flag5, (Object)true);
                p.sendMessage(ChatColor.YELLOW + args[1] + " flag has been set.");
            }
            else if (args[2].equalsIgnoreCase("false")) {
                region.setFlag((Flag)flag5, (Object)false);
                p.sendMessage(ChatColor.YELLOW + args[1] + " flag has been set.");
            }
        }
    }
    
    private RegionGroup getRegionGroup(final String arg) {
        if (arg.equalsIgnoreCase("member") || arg.equalsIgnoreCase("members")) {
            return RegionGroup.MEMBERS;
        }
        if (arg.equalsIgnoreCase("nonmembers") || arg.equalsIgnoreCase("nonmember") || arg.equalsIgnoreCase("nomember") || arg.equalsIgnoreCase("nomembers") || arg.equalsIgnoreCase("non_members") || arg.equalsIgnoreCase("non_member") || arg.equalsIgnoreCase("no_member") || arg.equalsIgnoreCase("no_members")) {
            return RegionGroup.NON_MEMBERS;
        }
        if (arg.equalsIgnoreCase("nonowners") || arg.equalsIgnoreCase("nonowner") || arg.equalsIgnoreCase("noowner") || arg.equalsIgnoreCase("noowners") || arg.equalsIgnoreCase("non_owners") || arg.equalsIgnoreCase("non_owner") || arg.equalsIgnoreCase("no_owner") || arg.equalsIgnoreCase("no_owners")) {
            return RegionGroup.NON_OWNERS;
        }
        if (arg.equalsIgnoreCase("owner") || arg.equalsIgnoreCase("owners")) {
            return RegionGroup.OWNERS;
        }
        if (arg.equalsIgnoreCase("none") || arg.equalsIgnoreCase("noone")) {
            return RegionGroup.NONE;
        }
        if (arg.equalsIgnoreCase("all") || arg.equalsIgnoreCase("everyone")) {
            return RegionGroup.ALL;
        }
        if (arg.endsWith("empty")) {
            return null;
        }
        return null;
    }
}

