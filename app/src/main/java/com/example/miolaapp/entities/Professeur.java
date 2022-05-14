package com.example.miolaapp.entities;

public class Professeur {
    private String nom;
    private String prenom;
    private String email, tele, depart;
    private boolean cord;
    private String image;

    public Professeur() { }

    public Professeur(String nom, String prenom, String email, String image) {
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.image = image;
    }

    public Professeur(String nom, String prenom, String email, String tele, String depart, boolean cord, String image) {
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.tele = tele;
        this.depart = depart;
        this.cord = cord;
        this.image = image;
    }

    public Professeur(String nom, String prenom, String email, String tele, String depart, boolean cord) {
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.tele = tele;
        this.depart = depart;
        this.cord = cord;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getTele() {
        return tele;
    }

    public void setTele(String tele) {
        this.tele = tele;
    }

    public String getDepart() {
        return depart;
    }

    public void setDepart(String depart) {
        this.depart = depart;
    }

    public boolean isCord() {
        return cord;
    }

    public void setCord(boolean cord) {
        this.cord = cord;
    }

    public String getFullName(){
        return nom+" "+prenom;
    }

    @Override
    public String toString() {
        return "Professeur{" +
                "nom='" + nom + '\'' +
                ", prenom='" + prenom + '\'' +
                ", email='" + email + '\'' +
                ", tele='" + tele + '\'' +
                ", depart='" + depart + '\'' +
                ", cord=" + cord +
                ", image='" + image + '\'' +
                '}';
    }
}
