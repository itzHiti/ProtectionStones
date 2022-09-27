package hiti.main;

public class StoneTypeData
{
    public int RegionX(final String StoneType) {
        final String ConfigString = "Region." + StoneType + ".X Radius";
        final int xradius = Main.plugin.getConfig().getInt(ConfigString);
        return xradius;
    }
    
    public int RegionY(final String StoneType) {
        final String ConfigString = "Region." + StoneType + ".Y Radius";
        final int yradius = Main.plugin.getConfig().getInt(ConfigString);
        return yradius;
    }
    
    public int RegionZ(final String StoneType) {
        final String ConfigString = "Region." + StoneType + ".Z Radius";
        final int zradius = Main.plugin.getConfig().getInt(ConfigString);
        return zradius;
    }
    
    public Boolean AutoHide(final String StoneType) {
        final String ConfigString = "Region." + StoneType + ".Auto Hide";
        final boolean autohide = Main.plugin.getConfig().getBoolean(ConfigString);
        return autohide;
    }
    
    public boolean NoDrop(final String StoneType) {
        final String ConfigString = "Region." + StoneType + ".No Drop";
        final boolean nodrop = Main.plugin.getConfig().getBoolean(ConfigString);
        return nodrop;
    }
    
    public boolean BlockPiston(final String StoneType) {
        final String ConfigString = "Region." + StoneType + ".Block Piston";
        final boolean blockpiston = Main.plugin.getConfig().getBoolean(ConfigString);
        return blockpiston;
    }
    
    public boolean SilkTouch(final String StoneType) {
        final String ConfigString = "Region." + StoneType + ".Silk Touch";
        final boolean silktouch = Main.plugin.getConfig().getBoolean(ConfigString);
        return silktouch;
    }
    
    public int DefaultPriority(final String StoneType) {
        final String ConfigString = "Region." + StoneType + ".Priority";
        final int priority = Main.plugin.getConfig().getInt(ConfigString);
        return priority;
    }
}

