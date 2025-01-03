package com.example.projetv4;

public class User {
private int id;
private String name;
private String statue;
private String prenom;

    public User(int id, String name, String statue, String prenom) {
        this.id = id;
        this.name = name;
        this.statue = statue;
        this.prenom = prenom;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatue() {
        return statue;
    }

    public void setStatue(String statue) {
        this.statue = statue;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", statue='" + statue + '\'' +
                ", prenom='" + prenom + '\'' +
                '}';
    }
}
