/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.spleefleague.gameapi.queue;

import com.spleefleague.core.SpleefLeague;
import com.spleefleague.core.utils.Tuple;
import com.spleefleague.gameapi.GameAPI;
import com.spleefleague.gameapi.GamePlugin;
import com.spleefleague.gameapi.player.GamePlayer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 *
 * @author Jonas
 * @param <P>
 */
public abstract class Challenge<P extends Player> {

    private final P challenger;
    private final int duration = 60;
    private final Map<UUID, Tuple<P, GamePlayer>> challenged;
    private final UUID challengeId;
    private final int required;
    private boolean active = true;
    
    public Challenge(P challenger, Collection<P> challenged) {
        this.challenger = challenger;
        challengeId = UUID.randomUUID();
        this.challenged = new HashMap<>();
        for(P p : challenged) {
            GamePlayer gp = GameAPI.getInstance().getPlayerManager().get(p);
            this.challenged.put(p.getUniqueId(), new Tuple<>(p, gp));
            gp.getActiveChallenges().put(challengeId, this);
        }
        if(challenger != null) {
            GamePlayer gp = GameAPI.getInstance().getPlayerManager().get(challenger);
            this.challenged.put(challenger.getUniqueId(), new Tuple<>(challenger, gp));
        }
        this.required = challenged.size();
        Bukkit.getScheduler().runTaskLater(GameAPI.getInstance(), this::done, duration * 20);
    }
    
    private void done() {
        if(!active) return;
        active = false;
        List<P> accepted = getAccepted();
        accepted.removeIf(p -> GamePlugin.isIngameGlobal(p));
        challenged.values().forEach(p -> p.y.getActiveChallenges().remove(challengeId));
        if(accepted.size() >= required) {
            start(accepted);
        }
    }
    
    public List<P> getAccepted() {
        return challenged.values()
                .stream()
                .filter(p -> !p.y.getActiveChallenges().containsKey(challengeId))
                .map(p -> p.x)
                .collect(Collectors.toList());
    }

    public void accept(Player player) {
        GamePlayer gamePlayer = GameAPI.getInstance().getPlayerManager().get(player);
        if(!gamePlayer.getActiveChallenges().containsKey(challengeId)) {
            gamePlayer.sendMessage(SpleefLeague.getInstance().getChatPrefix() + " " + ChatColor.RED + "Your challenge is invalid");
            return;
        }
        gamePlayer.getActiveChallenges().remove(challengeId);
        if(challenger != null) {
            challenger.sendMessage(SpleefLeague.getInstance().getChatPrefix() + " " + ChatColor.RED + gamePlayer.getName() + ChatColor.GREEN + " has accepted your challenge.");
        }
        //Start if all players accepted, no need to wait for the timeout
        if(getAccepted().size() == challenged.size()) {
            done();
        }
    }

    public void decline(Player player) {
        GamePlayer gamePlayer = GameAPI.getInstance().getPlayerManager().get(player);
        gamePlayer.getActiveChallenges().remove(challengeId);
        challenged.remove(gamePlayer.getUniqueId());
        //Cancel the challenge if not enough players are left to accept it
        if(challenged.size() < required) {
            active = false;
        }
        if(challenger != null) {
            challenger.sendMessage(SpleefLeague.getInstance().getChatPrefix() + " " + ChatColor.RED + gamePlayer.getName() + " has declined your challenge.");
        }
    }

    public abstract void start(List<P> accepted);

    public P getChallengingPlayer() {
        return challenger;
    }

    public void sendMessages(String prefix, String arena, Collection<? extends Player> target) {
        BaseComponent[] intro;
        if(arena != null) {
            intro = new ComponentBuilder(prefix).append(" ").append(challenger.getName() + " has challenged you to play on ").color(ChatColor.GREEN.asBungee()).append(arena + "!").color(ChatColor.RED.asBungee()).create();
        }
        else {
            intro = new ComponentBuilder(prefix).append(" ").append(challenger.getName() + " has challenged you to play").append(arena + "!").color(ChatColor.RED.asBungee()).create();
        }
        BaseComponent[] accept
                = new ComponentBuilder(prefix)
                .append(" [").color(ChatColor.GRAY.asBungee()).append("Accept").color(ChatColor.DARK_GREEN.asBungee()).event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/challenge accept " + challenger.getName())).append("]").color(ChatColor.GRAY.asBungee())
                .append(" - ")
                .append("[").color(ChatColor.GRAY.asBungee()).append("Decline").color(ChatColor.RED.asBungee()).event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/challenge decline " + challenger.getName())).append("]").color(ChatColor.GRAY.asBungee())
                .create();
        for (Player player : target) {
            player.spigot().sendMessage(intro);
            player.spigot().sendMessage(accept);
        }
    }
}
