/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.spleefleague.gameapi.player;

import com.mongodb.client.MongoCollection;
import com.spleefleague.core.player.GeneralPlayer;
import com.spleefleague.entitybuilder.DBLoad;
import com.spleefleague.entitybuilder.DBSave;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.bson.Document;

/**
 *
 * @author jonas
 * @param <M>
 */
public abstract class RatedPlayer<M extends Enum<M>> extends GeneralPlayer {
    
    private final Rating<M> rating;
    private final Class<M> enumClass;
    private final MongoCollection<Document> collection;
    
    public RatedPlayer(Class<M> enumClass, MongoCollection<Document> collection) {
        this.enumClass = enumClass;
        this.collection = collection;
        this.rating = new Rating();
    }
    
    public void setRating(M mode, int rating) {
        this.rating.setRating(mode, rating);
    }

    public int getRating(M mode) {
        return this.rating.getRating(mode);
    }
    
    @DBLoad(fieldName = "rating")
    private void loadRating(List<Document> ratingValues) {
        for(Document doc : ratingValues) {
            try {
                M mode = M.valueOf(enumClass, doc.get("mode", String.class));
                int rating = doc.get("rating", Integer.class);
                this.rating.setRating(mode, rating);
            } catch(Exception e) {
                System.out.println("Failed to load rating data:\n" + doc);
                e.printStackTrace();
            }
        }
    }
    
    @DBSave(fieldName = "rating")
    private List<Document> saveRating() {
        return rating.rating
                .entrySet()
                .stream()
                .map(e -> new Document("mode", e.getKey().name()).append("rating", e.getValue()))
                .collect(Collectors.toList());
    }
    
    public int getRank(M mode) {
        if(collection == null || !rating.isRated(mode)) return -1;
        Document query = new Document("rating.mode", mode.name())
                .append("rating.rating", new Document("$gt", rating.getRating(mode)));
        return (int) collection.count(query) + 1;
    }
    
    public static class Rating<M> {
        
        private final Map<M, Integer> rating;
        
        public Rating() {
            this.rating = new HashMap<>();
        }
        
        public boolean isRated(M mode) {
            return rating.containsKey(mode);
        }
        
        public int getRating(M mode) {
            return rating.getOrDefault(mode, 1000);
        }
        
        public void setRating(M mode, int rating) {
            if(mode == null) return;
            this.rating.put(mode, rating);
        }
    }
}
