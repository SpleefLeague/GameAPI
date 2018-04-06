/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.spleefleague.gameapi.queue;

import com.spleefleague.core.player.GeneralPlayer;
import java.util.List;

/**
 *
 * @author Jonas
 * @param <P>
 */
public interface QueueableArena<P extends GeneralPlayer> {

    public boolean isOccupied();

    public boolean isAvailable(P p);

    public int getSize();
    
    public int getRequiredPlayers();

    public String getName();

    public boolean isQueued();

    public boolean isPaused();
    
    public List<String> getDescription();
}
