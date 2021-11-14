package me.kodysimpson.bodied.tasks;

import me.kodysimpson.bodied.Bodied;
import me.kodysimpson.bodied.data.Body;
import net.minecraft.network.protocol.game.*;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

public class BodyRemover extends BukkitRunnable {

    private final List<Body> bodies;
    private final Bodied plugin;

    public BodyRemover(Bodied plugin) {
        this.plugin = plugin;
        this.bodies = new ArrayList<>();
    }

    @Override
    public void run() {

        long now = System.currentTimeMillis();
        for (Iterator<Body> iterator = bodies.iterator(); iterator.hasNext(); ) {
            Body body = iterator.next();
            if (now - body.getWhenDied() >= 5000){

                iterator.remove();

                new BukkitRunnable(){
                    @Override
                    public void run() {

                        //remove the body npc and give the items back to the player if they are online
                        //have the body slowly seep into the ground
                        Location location = body.getNpc().getBukkitEntity().getLocation().clone();
                        Bukkit.getOnlinePlayers().forEach(player -> {
                            ServerGamePacketListenerImpl ps = ((CraftPlayer) player).getHandle().connection;
                            body.getNpc().setPos(location.getX(), location.getY() - 0.01, location.getZ());
                            ps.send(new ClientboundTeleportEntityPacket(body.getNpc()));
                        });

                        //see if they are fully underground or not
                        if (!location.add(0, 1, 0).getBlock().isPassable()){
                            this.cancel();
                        }

                    }
                }.runTaskTimerAsynchronously(plugin, 0L, 5L);


                Player whoDied = Bukkit.getServer().getPlayer(body.getWhoDied());
                if (whoDied != null){

                    //give the items back one by one, if they do not fit then drop at their feet
                    Inventory inventory = whoDied.getInventory();
                    inventory.addItem(body.getItems()).values().stream().filter(Objects::nonNull).forEach(itemStack -> {
                        whoDied.getWorld().dropItem(whoDied.getLocation(), itemStack);
                    });

                    whoDied.sendMessage("Your dead body has rotted and your items have been returned if you died with any.");
                }

            }
        }

    }

    //getter so the list can be accessed outside
    public List<Body> getBodies() {
        return bodies;
    }
}
