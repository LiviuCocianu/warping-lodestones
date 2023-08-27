package io.github.idoomful.lodestonewarp.events;

import io.github.idoomful.bukkitutils.statics.Events;
import io.github.idoomful.lodestonewarp.DMain;
import io.github.idoomful.lodestonewarp.data.WarpingLodestone;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.event.entity.EntityExplodeEvent;

import java.util.List;

public class GriefPreventionEvents {
    public GriefPreventionEvents(DMain main) {
        // Prevent lodestone from being exploded
        Events.listen(main, EntityExplodeEvent.class, e -> {
            List<WarpingLodestone> lodestones = main.getCache().getLodestones();

            for(WarpingLodestone lodestone : lodestones) {
                final Location databaseLoc = lodestone.getLocation().getLocation();

                for(Block block : e.blockList().toArray(new Block[0])) {
                    if(block.getLocation().toString().equals(databaseLoc.toString())) {
                        e.blockList().remove(block);
                        break;
                    }
                }
            }
        });
    }
}
