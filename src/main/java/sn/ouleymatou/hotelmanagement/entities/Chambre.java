package sn.ouleymatou.hotelmanagement.entities;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "chambres")
public class Chambre {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Transient
    public int getCapacite() {
        return typeChambre != null ? typeChambre.getCapacitePersonnes() : 0;
    }

    @Column(unique = true, nullable = false, length = 20)
    private String numero;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "type_chambre_id", nullable = false)
    private TypeChambre typeChambre;


    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutChambre statut;

    @Column(nullable = false)
    private int etage;

    private boolean climatisation;
    private boolean balcon;

    @Column(name = "vue_ocean")
    private boolean vueOcean;

    @Column(name = "date_derniere_renovation")
    private LocalDate dateDerniereRenovation;

    @CreationTimestamp
    @Column(name = "date_creation")
    private LocalDate dateCreation;

    @OneToMany(mappedBy = "chambre", cascade = CascadeType.ALL)
    private List<Reservation> reservations = new ArrayList<>();

    public double getTarifParNuit() {
        return typeChambre != null ? typeChambre.getTarif() : 0.0;
    }

    public Chambre() {}

    public Long getId() {
        return id;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public TypeChambre getTypeChambre() {
        return typeChambre;
    }

    public void setTypeChambre(TypeChambre typeChambre) {
        this.typeChambre = typeChambre;
    }

    public StatutChambre getStatut() {
        return statut;
    }

    public void setStatut(StatutChambre statut) {
        this.statut = statut;
    }

    public int getEtage() {
        return etage;
    }

    public void setEtage(int etage) {
        this.etage = etage;
    }

    public boolean isClimatisation() {
        return climatisation;
    }

    public void setClimatisation(boolean climatisation) {
        this.climatisation = climatisation;
    }

    public boolean isBalcon() {
        return balcon;
    }

    public void setBalcon(boolean balcon) {
        this.balcon = balcon;
    }

    public boolean isVueOcean() {
        return vueOcean;
    }

    public void setVueOcean(boolean vueOcean) {
        this.vueOcean = vueOcean;
    }

    public LocalDate getDateDerniereRenovation() {
        return dateDerniereRenovation;
    }

    public void setDateDerniereRenovation(LocalDate dateDerniereRenovation) {
        this.dateDerniereRenovation = dateDerniereRenovation;
    }

    public LocalDate getDateCreation() {
        return dateCreation;
    }

    public List<Reservation> getReservations() {
        return reservations;
    }

    public void setReservations(List<Reservation> reservations) {
        this.reservations = reservations;
    }

    @Override
    public String toString() {
        return "Chambre n°" + numero;
    }

    public enum StatutChambre {
        LIBRE, OCCUPEE, MAINTENANCE, HORS_SERVICE
    }
}

