/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.spleefleague.gameapi.player;

import com.spleefleague.core.player.GeneralPlayer;
import com.spleefleague.gameapi.queue.Challenge;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 *
 * @author jonas
 */
public class GamePlayer extends GeneralPlayer {
    
    private final Map<UUID, Challenge<?>> activeChallenges = new HashMap<>();
    
    public Map<UUID, Challenge<?>> getActiveChallenges() {
        return activeChallenges;
    }
}
