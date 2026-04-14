package com.ideas.notetaker.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;
import java.io.InputStream;

@Configuration
public class FirebaseConfig {

    @PostConstruct
    public void init() {
        try {
            FirebaseOptions options = null;
            String configJson = System.getenv("FIREBASE_CONFIG_JSON");

            if (configJson != null && !configJson.isEmpty()) {
                // Try to load from environment variable
                InputStream serviceAccount = new java.io.ByteArrayInputStream(configJson.getBytes());
                options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                        .build();
                System.out.println("Firebase App initialized from environment variable.");
            } else {
                // Fallback to local file
                InputStream serviceAccount = getClass().getClassLoader().getResourceAsStream("serviceAccountKey.json");
                if (serviceAccount != null) {
                    options = FirebaseOptions.builder()
                            .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                            .build();
                    System.out.println("Firebase App initialized from serviceAccountKey.json.");
                }
            }

            if (options != null) {
                if (FirebaseApp.getApps().isEmpty()) {
                    FirebaseApp.initializeApp(options);
                }
            } else {
                System.out.println("WARNING: No Firebase credentials found. App will run with restricted functionality.");
            }
        } catch (Exception e) {
            System.err.println("Error initializing Firebase: " + e.getMessage());
        }
    }
}
