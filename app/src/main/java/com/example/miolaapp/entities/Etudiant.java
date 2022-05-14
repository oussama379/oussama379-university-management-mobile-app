package com.example.miolaapp.entities;

public class Etudiant {
    private String nom;
    private String prenom;
    private String email, tele, filiere;
    private String image;

    public Etudiant() { }

    public Etudiant(String nom, String prenom, String email, String tele, String filiere, String image) {
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.tele = tele;
        this.filiere = filiere;
        this.image = image;
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

    public String getTele() {
        return tele;
    }

    public void setTele(String tele) {
        this.tele = tele;
    }

    public String getFiliere() {
        return filiere;
    }

    public void setFiliere(String filiere) {
        this.filiere = filiere;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getFullName(){
        return nom+" "+prenom;
    }

    @Override
    public String toString() {
        return "Etudiant{" +
                "nom='" + nom + '\'' +
                ", prenom='" + prenom + '\'' +
                ", email='" + email + '\'' +
                ", tele='" + tele + '\'' +
                ", filiere='" + filiere + '\'' +
                ", image='" + image + '\'' +
                '}';
    }
}
