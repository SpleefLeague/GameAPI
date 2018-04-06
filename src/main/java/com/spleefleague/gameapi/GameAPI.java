/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.spleefleague.gameapi;

import com.spleefleague.core.player.LocalPlayerManager;
import com.spleefleague.core.player.PlayerManager;
import com.spleefleague.core.plugin.CorePlugin;
import com.spleefleague.gameapi.listener.EnvironmentListener;
import com.spleefleague.gameapi.player.GamePlayer;
import org.bukkit.ChatColor;

/**
 *
 * @author jonas
 */
public class GameAPI extends CorePlugin {
    
    private static GameAPI instance;
    private PlayerManager<GamePlayer> playerManager;
    
    public GameAPI() {
        super(ChatColor.GRAY + "[" + ChatColor.GOLD + "GameAPI" + ChatColor.GRAY + "]" + ChatColor.RESET);
    }
    
    @Override
    public void start() {
        instance = this;
        playerManager = new LocalPlayerManager(this, GamePlayer.class);
        EnvironmentListener.init();
    }

    public PlayerManager<GamePlayer> getPlayerManager() {
        return playerManager;
    }
    
    public static GameAPI getInstance() {
        return instance;
    }
}
