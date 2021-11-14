package me.kodysimpson.bodied;

import me.kodysimpson.bodied.tasks.BodyRemover;
import org.bukkit.plugin.java.JavaPlugin;

public final class Bodied extends JavaPlugin {

    private BodyRemover bodyRemover;

    @Override
    public void onEnable() {

        getServer().getPluginManager().registerEvents(new DeathListener(this), this);

        bodyRemover = new BodyRemover(this);
        bodyRemover.runTaskTimerAsynchronously(this, 20L, 20L);

    }

    public BodyRemover getBodyRemover() {
        return bodyRemover;
    }
}
