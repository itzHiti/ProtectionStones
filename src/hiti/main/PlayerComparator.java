package hiti.main;

import java.util.*;
import org.bukkit.*;

class PlayerComparator implements Comparator<OfflinePlayer>
{
    @Override
    public int compare(final OfflinePlayer o1, final OfflinePlayer o2) {
        return o1.getName().compareTo(o2.getName());
    }
}

