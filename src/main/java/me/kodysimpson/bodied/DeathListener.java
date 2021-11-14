package me.kodysimpson.bodied;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import me.kodysimpson.bodied.data.Body;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundAddPlayerPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundSetPlayerTeamPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.Team;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;

public class DeathListener implements Listener {

    private final Bodied bodied;

    public DeathListener(Bodied bodied) {
        this.bodied = bodied;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent e){

        //spawn a corpse where the player died
        Player p = e.getEntity();
        p.sendMessage("Bruh, the goal is to NOT die.");
        Body body = new Body(p.getUniqueId(), spawnCorpse(p), e.getDrops().toArray(new ItemStack[0]), System.currentTimeMillis());
        bodied.getBodyRemover().getBodies().add(body);

        e.getDrops().clear();

    }

    private ServerPlayer spawnCorpse(Player deadPerson) {
        CraftPlayer craftPlayer = (CraftPlayer) deadPerson;

        //NMS representation of the MC server
        MinecraftServer server = craftPlayer.getHandle().getServer();
        //NMS representation of the MC world
        ServerLevel level = craftPlayer.getHandle().getLevel();

        //Create a new NPC named Billy Bob, with a new GameProfile to uniquely identify them
        ServerPlayer npc = new ServerPlayer(server, level, new GameProfile(UUID.randomUUID(), ChatColor.stripColor(" ")));
        //Set their position. They will be here when we call the packets below to spawn them

        //spawn them and spawn them on ground level

        //find the proper place to put the body
        System.out.println(deadPerson.getLocation().getBlock().getType());
        Location pl = deadPerson.getLocation().getBlock().getLocation().clone();
        System.out.println(pl);
        while(pl.getBlock().getType() == Material.AIR){
            pl = pl.subtract(0, 1, 0);
        }

        npc.setPos(deadPerson.getLocation().getX(), pl.getY() + 1, deadPerson.getLocation().getZ());
        npc.setPose(Pose.SLEEPING);

        //default skin data
        String signature = "ArwoD4sGhthC32Qaq1oSwNOWPciJN54mLj+Tq0tZBUMCaw7Gnpj6W9HJhLrax6gVs8X3O5cWUrgLbAIF8uelb5jLdUpm9ZFsAFUo/MtE3oqCXBjoXw8+Wn8y8WR1UAXwv0ts+C6OSyOfLGk0tR7Jmkac6G7bUKYOAMFtCGcppdmoxvhALHPkcsPmdlE8SsHhOVDBp+SE9SBA0V5Z2YDTua34bLdCh4jHibb9x6D8yLxos5ksqcUzsLW9HZ6gqt29GqRD3+M2q1VyXyOjQCR1MD/5A0WfFAFBtExWPRn4V8Fl8a6+814a84H6apaoIN0e6rZHC9ArLEbfSStS54YbjFZ5jfUHx4jkyg0n16B14Z7KLVRmWJjUPtICWaW7zlOOzzq+ZkV1fckVmXEA0Ri349DnWMSGU44nkgPsjD5PL9PLdDqhWqXQGL9f3C+XmUC+5WWdE1cA2W+ZrTN0mZajlkmcwYL0priAZZfzubhVV6PqWAaM9phgaoK7s5oQc6ruaXObauGZvxZ2p+LDx8A+AKnpxSPvjE+fVoOZUAvzVIhwXkFo8Y7+lJi29GjNS8f+fZctPivnABnK2oHXVapvdWlOfpTg/Y8cgc+GHhsvY82f9p7tyFAjV59Ps2G3TDjNbxm7iRaNs4MBUf2e8+mQFt/MbbblCfDBMUOprV0vjks=";
        String texture = "ewogICJ0aW1lc3RhbXAiIDogMTYzMzI2Mzg5NjIyNSwKICAicHJvZmlsZUlkIiA6ICIwNjlhNzlmNDQ0ZTk0NzI2YTViZWZjYTkwZTM4YWFmNSIsCiAgInByb2ZpbGVOYW1lIiA6ICJOb3RjaCIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS8yOTIwMDlhNDkyNWI1OGYwMmM3N2RhZGMzZWNlZjA3ZWE0Yzc0NzJmNjRlMGZkYzMyY2U1NTIyNDg5MzYyNjgwIgogICAgfQogIH0KfQ==";

        GameProfile gameProfile = ((CraftPlayer) deadPerson).getHandle().getGameProfile();
        Property property = (Property) gameProfile.getProperties().get("textures").toArray()[0];

        signature = property.getSignature();
        texture = property.getValue();

        npc.getGameProfile().getProperties().put("textures", new Property("textures", texture, signature));

        PlayerTeam team = new PlayerTeam(new Scoreboard(), npc.getName().getString());
        team.setNameTagVisibility(Team.Visibility.NEVER);
        team.getPlayers().add(npc.getName().getString());

        //Send the packets to artificially spawn this entity, only the clients we are sending the packet to will know of its existence
        Bukkit.getOnlinePlayers().forEach(player -> {
            ServerGamePacketListenerImpl ps = ((CraftPlayer) player).getHandle().connection;
            ps.send(new ClientboundPlayerInfoPacket(ClientboundPlayerInfoPacket.Action.ADD_PLAYER, npc));
            ps.send(new ClientboundAddPlayerPacket(npc));
            ps.send(new ClientboundSetEntityDataPacket(npc.getId(), npc.getEntityData(), true));

            //remove the team
            ps.send(ClientboundSetPlayerTeamPacket.createRemovePacket(team));
            //add the team
            ps.send(ClientboundSetPlayerTeamPacket.createAddOrModifyPacket(team, true));

            new BukkitRunnable(){
                @Override
                public void run() {
                    ps.send(new ClientboundPlayerInfoPacket(ClientboundPlayerInfoPacket.Action.REMOVE_PLAYER, npc));

                }
            }.runTaskLaterAsynchronously(bodied, 20L);

        });

        return npc;
    }

}
