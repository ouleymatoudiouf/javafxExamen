package sn.ouleymatou.hotelmanagement.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "reservations")
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "numero", nullable = false, unique = true)
    private String numero; // ex: RSV-2025-001

    @Column(name = "date_reservation", nullable = false)
    private LocalDateTime dateReservation; // Date + Heure

    @Column(name = "nom_client", nullable = false)
    private String nomClient;

    @Column(name = "prenom_client", nullable = false)
    private String prenomClient;

    @Column(name = "telephone_client", nullable = false)
    private String telephone;

    @Column(name = "email")
    private String email;

    @Column(name = "date_arrivee", nullable = false)
    private LocalDateTime dateArrivee; // Date + Heure

    @Column(name = "date_depart", nullable = false)
    private LocalDateTime dateDepart; // Date + Heure

    @Column(name = "nombre_personnes", nullable = false)
    private Integer nombrePersonnes;

    @Column(name = "nombre_nuits", nullable = false)
    private int nombreNuits;

    @Column(name = "montant_total", nullable = false)
    private double montantTotal;

    @Column(name = "acompte", nullable = false)
    private double acompte;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut", nullable = false)
    private StatutReservation statut;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chambre_id", nullable = false)
    private Chambre chambre;

    public enum StatutReservation {
        CONFIRMEE,
        EN_COURS,
        TERMINEE,
        ANNULEE
    }

    // ==== Constructeurs ====
    public Reservation() {
    }

    public Reservation(String numero, LocalDateTime dateReservation, String nomClient, String prenomClient,
                       String telephone, String email, LocalDateTime dateArrivee, LocalDateTime dateDepart,
                       int nombrePersonnes, int nombreNuits, double montantTotal, double acompte,
                       StatutReservation statut, Chambre chambre) {
        this.numero = numero;
        this.dateReservation = dateReservation;
        this.nomClient = nomClient;
        this.prenomClient = prenomClient;
        this.telephone = telephone;
        this.email = email;
        this.dateArrivee = dateArrivee;
        this.dateDepart = dateDepart;
        this.nombrePersonnes = nombrePersonnes;
        this.nombreNuits = nombreNuits;
        this.montantTotal = montantTotal;
        this.acompte = acompte;
        this.statut = statut;
        this.chambre = chambre;
    }

    // ==== Getters & Setters ====
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNumero() { return numero; }
    public void setNumero(String numero) { this.numero = numero; }

    public LocalDateTime getDateReservation() { return dateReservation; }
    public void setDateReservation(LocalDateTime dateReservation) { this.dateReservation = dateReservation; }

    public String getNomClient() { return nomClient; }
    public void setNomClient(String nomClient) { this.nomClient = nomClient; }

    public String getPrenomClient() { return prenomClient; }
    public void setPrenomClient(String prenomClient) { this.prenomClient = prenomClient; }

    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public LocalDateTime getDateArrivee() { return dateArrivee; }
    public void setDateArrivee(LocalDateTime dateArrivee) { this.dateArrivee = dateArrivee; }

    public LocalDateTime getDateDepart() { return dateDepart; }
    public void setDateDepart(LocalDateTime dateDepart) { this.dateDepart = dateDepart; }

    public int getNombrePersonnes() { return nombrePersonnes; }
    public void setNombrePersonnes(int nombrePersonnes) { this.nombrePersonnes = nombrePersonnes; }

    public int getNombreNuits() { return nombreNuits; }
    public void setNombreNuits(int nombreNuits) { this.nombreNuits = nombreNuits; }

    public double getMontantTotal() { return montantTotal; }
    public void setMontantTotal(double montantTotal) { this.montantTotal = montantTotal; }

    public double getAcompte() { return acompte; }
    public void setAcompte(double acompte) { this.acompte = acompte; }

    public StatutReservation getStatut() { return statut; }
    public void setStatut(StatutReservation statut) { this.statut = statut; }

    public Chambre getChambre() { return chambre; }
    public void setChambre(Chambre chambre) { this.chambre = chambre; }

    // ==== Méthodes utilitaires ====
    public String getNomCompletClient() {
        return (nomClient != null ? nomClient : "") + " " + (prenomClient != null ? prenomClient : "");
    }

    public String getNumeroChambre() {
        return (chambre != null && chambre.getNumero() != null) ? chambre.getNumero() : "";
    }

    public String getDateArriveeString() {
        return dateArrivee != null ? dateArrivee.toString() : "";
    }

    public String getDateDepartString() {
        return dateDepart != null ? dateDepart.toString() : "";
    }

    public String getStatutString() {
        return statut != null ? statut.toString() : "";
    }
}
