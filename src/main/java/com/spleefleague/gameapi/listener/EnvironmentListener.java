/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.spleefleague.gameapi.listener;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.PlayerInfoData;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.comphenix.protocol.wrappers.WrappedGameProfile;
import com.google.common.collect.Lists;
import com.spleefleague.core.SpleefLeague;
import com.spleefleague.core.player.GeneralPlayer;
import com.spleefleague.core.player.SLPlayer;
import com.spleefleague.gameapi.GamePlugin;
import com.spleefleague.gameapi.events.BattleStartEvent;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftPlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 *
 * @author jonas
 */
public class EnvironmentListener implements Listener {
    
    private static Listener instance;

    public static void init() {
        if (instance == null) {
            instance = new EnvironmentListener();
            Bukkit.getPluginManager().registerEvents(instance, SpleefLeague.getInstance());
        }
    }

    private EnvironmentListener() {

    }
    
    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        GamePlugin.unspectateGlobal(event.getPlayer());
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onStart(BattleStartEvent e) {
        if (e.isCancelled()) {
            return;
        }
        Bukkit.getScheduler().runTaskLater(SpleefLeague.getInstance(), () -> {
            try {
                //Handle people in battle seeing others.
                List<PlayerInfoData> list = new ArrayList<>();
                SpleefLeague.getInstance().getPlayerManager().getAll().forEach((SLPlayer slPlayer) -> list.add(
                        new PlayerInfoData(
                                WrappedGameProfile.fromPlayer(slPlayer.getPlayer()),
                                ((CraftPlayer) slPlayer.getPlayer()).getHandle().ping,
                                EnumWrappers.NativeGameMode.SURVIVAL,
                                WrappedChatComponent.fromText(slPlayer.getTabName()))));
                PacketContainer packet = new PacketContainer(PacketType.Play.Server.PLAYER_INFO);
                packet.getPlayerInfoAction().write(0, EnumWrappers.PlayerInfoAction.ADD_PLAYER);
                packet.getPlayerInfoDataLists().write(0, list);
                for (Object battlePlayer : e.getBattle().getActivePlayers()) {
                    GeneralPlayer generalPlayer = (GeneralPlayer) battlePlayer;
                    ProtocolLibrary.getProtocolManager().sendServerPacket(generalPlayer.getPlayer(), packet);
                }

                //Handle others seeing people in battle.
                packet = new PacketContainer(PacketType.Play.Server.PLAYER_INFO);
                packet.getPlayerInfoAction().write(0, EnumWrappers.PlayerInfoAction.ADD_PLAYER);
                e.getBattle().getActivePlayers().forEach((Object ratedPlayer) -> {
                        SLPlayer generalPlayer = SpleefLeague.getInstance().getPlayerManager().get(((GeneralPlayer) ratedPlayer).getPlayer());
                        list.add(
                            new PlayerInfoData(
                                    WrappedGameProfile.fromPlayer(generalPlayer.getPlayer()),
                                    1,
                                    EnumWrappers.NativeGameMode.fromBukkit(generalPlayer.getPlayer().getGameMode()),
                                    WrappedChatComponent.fromText(generalPlayer.getDisplayName())));});
                packet.getPlayerInfoDataLists().write(0, list);
                for (SLPlayer slPlayer : SpleefLeague.getInstance().getPlayerManager().getAll()) {
                    ProtocolLibrary.getProtocolManager().sendServerPacket(slPlayer.getPlayer(), packet);
                }
            } catch (InvocationTargetException ex) {
                Logger.getLogger(EnvironmentListener.class.getName()).log(Level.SEVERE, null, ex);
            }
        }, 10);
    }
}
