package com.ideas.notetaker.controller;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.Query;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.WriteResult;
import com.google.firebase.cloud.FirestoreClient;
import com.ideas.notetaker.model.Idea;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

@Controller
@CrossOrigin(origins = "*")
public class IdeaController {

    private Firestore getFirestore() {
        return FirestoreClient.getFirestore();
    }

    @MessageMapping("/newIdea")
    @SendTo("/topic/ideas")
    public Idea broadcastIdea(Idea idea) {
        if (idea.getId() == null || idea.getId().isEmpty()) {
            idea.setId(UUID.randomUUID().toString());
            idea.setTimestamp(System.currentTimeMillis());
        }
        
        try {
            Firestore db = getFirestore();
            ApiFuture<WriteResult> result = db.collection("ideas").document(idea.getId()).set(idea);
            result.get(); // wait for save to complete
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return idea;
    }

    @GetMapping("/api/ideas")
    @ResponseBody
    public List<Idea> getAllIdeas() {
        List<Idea> ideas = new ArrayList<>();
        try {
            Firestore db = getFirestore();
            ApiFuture<QuerySnapshot> query = db.collection("ideas").orderBy("timestamp", Query.Direction.ASCENDING).get();
            QuerySnapshot querySnapshot = query.get();
            for (QueryDocumentSnapshot document : querySnapshot.getDocuments()) {
                ideas.add(document.toObject(Idea.class));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ideas;
    }
}
