/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.spleefleague.gameapi.events;

import com.spleefleague.gameapi.queue.Battle;
import org.bukkit.event.HandlerList;

/**
 *
 * @author Jonas
 */
public class BattleStartEvent extends BattleEvent {

    private static final HandlerList handlers = new HandlerList();
    private final StartReason reason;

    public BattleStartEvent(Battle battle, StartReason reason) {
        super(battle);
        this.reason = reason;
    }

    public StartReason getReason() {
        return reason;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public static enum StartReason {
        FORCE,
        QUEUE,
        CHALLENGE;
    }
}
