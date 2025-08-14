package sn.ouleymatou.hotelmanagement.entities;


import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "types_chambres")
public class TypeChambre {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 10)
    private String code;

    @Column(nullable = false, length = 100)
    private String libelle;

    @Column(name = "tarif_nuit", nullable = false)
    private double tarifNuit;

    @Column(length = 255)
    private String description;

    @Column(name = "capacite_personnes", nullable = false)
    private int capacitePersonnes;

    @OneToMany(mappedBy = "typeChambre", cascade = CascadeType.ALL)
    private List<Chambre> chambres = new ArrayList<>();

    // === Constructeurs ===
    public TypeChambre() {}

    // === Getters & Setters ===

    public Long getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getLibelle() {
        return libelle;
    }

    public void setLibelle(String libelle) {
        this.libelle = libelle;
    }

    public double getTarifNuit() {
        return tarifNuit;
    }

    public void setTarifNuit(double tarifNuit) {
        this.tarifNuit = tarifNuit;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getCapacitePersonnes() {
        return capacitePersonnes;
    }

    public void setCapacitePersonnes(int capacitePersonnes) {
        this.capacitePersonnes = capacitePersonnes;
    }

    public List<Chambre> getChambres() {
        return chambres;
    }

    public void setChambres(List<Chambre> chambres) {
        this.chambres = chambres;
    }

    public double getTarif() {
        return tarifNuit;
    }

    @Override
    public String toString() {
        return libelle;
    }
}

