package io.github.idoomful.lodestonewarp.events;

import io.github.idoomful.bukkitutils.statics.*;
import io.github.idoomful.lodestonewarp.DMain;
import io.github.idoomful.lodestonewarp.configuration.SettingsYML;
import io.github.idoomful.lodestonewarp.data.WarpingLodestone;
import org.bukkit.*;

public class EventsClass {
    private int idleSchID = 0;

    public EventsClass(DMain main) {
        // Lodestone placing and breaking
        new InstallUninstallEvents(main);
        // Prevents stuff like explosions from destroying the lodestones
        new GriefPreventionEvents(main);
        // Open interfaces for the lodestones and linking functionality
        new InteractionEvents(main);
        // Adds functionality to the icons in UIs
        new UIEvents(main);
        // Handle any chat input from setups
        new ChatEvents(main);
        // Clean residual cache
        new CleaningEvents(main);

        restartIdleParticles(main);
    }

    public void restartIdleParticles(DMain main) {
        Bukkit.getScheduler().cancelTask(idleSchID);

        if(SettingsYML.Options.IDLE_PARTICLES_EVERY.get(Integer.class) > 0) {
            idleSchID = Bukkit.getScheduler().scheduleSyncRepeatingTask(main, () -> {
                for(WarpingLodestone lodestone : main.getCache().getLodestones()) {
                    final String particles = lodestone.isPublic()
                            ? SettingsYML.Options.IDLE_PARTICLES_PUBLIC.get(String.class)
                            : lodestone.isGlobal()
                            ? SettingsYML.Options.IDLE_PARTICLES_GLOBAL.get(String.class)
                            : SettingsYML.Options.IDLE_PARTICLES_PRIVATE.get(String.class);

                    BeautifyUtils.displayTexturedParticle(lodestone.getLocation().getLocation().clone().add(0.5, 0.5, 0.5),
                            Bukkit.getOnlinePlayers(), particles
                    );
                }
            }, 0, SettingsYML.Options.IDLE_PARTICLES_EVERY.get(Integer.class));
        }
    }

    public int getIdleScheduleID() {
        return idleSchID;
    }
}
