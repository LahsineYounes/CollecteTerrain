package com.example.collecteterrainapp.shared.models;

import java.util.List;

public class User {
    private String uid;
    private String email;
    private String displayName;
    private String photoUrl;
    private String role; // "manager" ou "representative"
    private List<String> managedTeams; // Pour managers
    private String assignedRegion; // Pour représentants
    private transient int collecteCount; // Transient: not stored in Firestore

    // Constructeurs
    public User() {}

    // Méthodes utilitaires
    public boolean isManager() {
        return "manager".equals(role);
    }

    // Getters/Setters
    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public String getPhotoUrl() { return photoUrl; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public List<String> getManagedTeams() { return managedTeams; }
    public void setManagedTeams(List<String> managedTeams) { this.managedTeams = managedTeams; }

    public String getAssignedRegion() { return assignedRegion; }
    public void setAssignedRegion(String assignedRegion) { this.assignedRegion = assignedRegion; }

    public int getCollecteCount() { return collecteCount; }
    public void setCollecteCount(int collecteCount) { this.collecteCount = collecteCount; }
}

