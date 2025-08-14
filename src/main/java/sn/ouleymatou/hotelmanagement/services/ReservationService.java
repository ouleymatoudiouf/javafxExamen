package sn.ouleymatou.hotelmanagement.services;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;
import sn.ouleymatou.hotelmanagement.entities.Chambre;
import sn.ouleymatou.hotelmanagement.entities.Reservation;
import sn.ouleymatou.hotelmanagement.utils.JPAUtils;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class ReservationService {

    // === Génération d'un numéro de réservation unique par jour ===
    public String genererNumeroReservation() {
        EntityManager em = JPAUtils.getEntityManager();
        try {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime startOfDay = now.toLocalDate().atStartOfDay();
            LocalDateTime endOfDay = startOfDay.plusDays(1);

            TypedQuery<Long> query = em.createQuery(
                    "SELECT COUNT(r) FROM Reservation r WHERE r.dateReservation >= :start AND r.dateReservation < :end",
                    Long.class);
            query.setParameter("start", startOfDay);
            query.setParameter("end", endOfDay);

            Long count = query.getSingleResult();
            String datePart = now.format(java.time.format.DateTimeFormatter.BASIC_ISO_DATE);
            return String.format("RSV-%s-%03d", datePart, count + 1);
        } finally {
            if (em.isOpen()) em.close();
        }
    }

    // === Liste des chambres libres ===
    public List<Chambre> getAllChambresLibres() {
        EntityManager em = JPAUtils.getEntityManager();
        try {
            return em.createQuery("SELECT c FROM Chambre c WHERE c.statut = :statut", Chambre.class)
                    .setParameter("statut", Chambre.StatutChambre.LIBRE)
                    .getResultList();
        } finally {
            if (em.isOpen()) em.close();
        }
    }

    // === Enregistrement d'une réservation avec validation et chevauchement ===
    public void enregistrerReservation(Reservation reservation) {
        EntityManager em = JPAUtils.getEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            // Validation client
            if (reservation.getNomClient() == null || reservation.getNomClient().length() < 2
                    || !reservation.getNomClient().matches("^[A-Za-zÀ-ÖØ-öø-ÿ\\s'-]+$"))
                throw new IllegalArgumentException("Nom client invalide.");
            if (reservation.getPrenomClient() == null || reservation.getPrenomClient().length() < 2
                    || !reservation.getPrenomClient().matches("^[A-Za-zÀ-ÖØ-öø-ÿ\\s'-]+$"))
                throw new IllegalArgumentException("Prénom client invalide.");
            if (reservation.getTelephone() == null || !reservation.getTelephone().matches("^(77|78|75|76|70)\\d{7}$"))
                throw new IllegalArgumentException("Téléphone invalide.");
            if (reservation.getEmail() != null && !reservation.getEmail().isEmpty()
                    && !reservation.getEmail().matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$"))
                throw new IllegalArgumentException("Email invalide.");

            // Validation dates
            LocalDateTime maintenant = LocalDateTime.now();
            LocalDateTime arrivee = reservation.getDateArrivee();
            LocalDateTime depart = reservation.getDateDepart();
            if (arrivee == null || arrivee.isBefore(maintenant))
                throw new IllegalArgumentException("Date d'arrivée dans le futur obligatoire.");
            if (depart == null || !depart.isAfter(arrivee))
                throw new IllegalArgumentException("Date de départ après l'arrivée obligatoire.");

            // Validation chambre
            Chambre chambre = em.find(Chambre.class, reservation.getChambre().getId());
            if (chambre == null) throw new IllegalArgumentException("Chambre introuvable.");
            if (chambre.getStatut() == Chambre.StatutChambre.HORS_SERVICE)
                throw new IllegalArgumentException("Chambre hors service.");

            // Capacité
            if (reservation.getNombrePersonnes() < 1 || reservation.getNombrePersonnes() > chambre.getCapacite())
                throw new IllegalArgumentException("Nombre de personnes dépasse la capacité.");

            // Vérification chevauchement
            TypedQuery<Long> query = em.createQuery(
                    "SELECT COUNT(r) FROM Reservation r WHERE r.chambre = :chambre AND r.statut <> :terminee AND " +
                            "r.dateDepart > :arrivee AND r.dateArrivee < :depart",
                    Long.class);
            query.setParameter("chambre", chambre);
            query.setParameter("arrivee", arrivee);
            query.setParameter("depart", depart);
            query.setParameter("terminee", Reservation.StatutReservation.TERMINEE);
            if (query.getSingleResult() > 0)
                throw new IllegalArgumentException("Chambre déjà réservée pour ces dates.");

            // Calcul nuits
            int nuits = (int) ChronoUnit.DAYS.between(arrivee.toLocalDate(), depart.toLocalDate());
            if (nuits < 1) nuits = 1;
            reservation.setNombreNuits(nuits);

            // Calcul montant
            double montantTotal = nuits * chambre.getTarifParNuit();
            reservation.setMontantTotal(montantTotal);

            // Vérification acompte
            if (reservation.getAcompte() < montantTotal * 0.3 || reservation.getAcompte() > montantTotal)
                throw new IllegalArgumentException("Acompte entre 30% et 100% du montant.");

            // Générer numéro si vide
            if (reservation.getNumero() == null || reservation.getNumero().isEmpty())
                reservation.setNumero(genererNumeroReservation());

            reservation.setDateReservation(LocalDateTime.now());
            reservation.setStatut(Reservation.StatutReservation.CONFIRMEE);

            em.persist(reservation);
            tx.commit();

        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw new RuntimeException("Erreur lors de la réservation : " + e.getMessage(), e);
        } finally {
            if (em.isOpen()) em.close();
        }
    }

    // === Méthodes statistiques ===
    public List<Reservation> getAllReservations() {
        EntityManager em = JPAUtils.getEntityManager();
        try {
            return em.createQuery("SELECT r FROM Reservation r", Reservation.class).getResultList();
        } finally {
            if (em.isOpen()) em.close();
        }
    }

    public List<Reservation> getArriveesDuJour() {
        EntityManager em = JPAUtils.getEntityManager();
        try {
            LocalDateTime debut = LocalDateTime.now().toLocalDate().atStartOfDay();
            LocalDateTime fin = debut.plusDays(1);
            TypedQuery<Reservation> query = em.createQuery(
                    "SELECT r FROM Reservation r WHERE r.dateArrivee >= :debut AND r.dateArrivee < :fin AND r.statut = :statut",
                    Reservation.class);
            query.setParameter("debut", debut);
            query.setParameter("fin", fin);
            query.setParameter("statut", Reservation.StatutReservation.CONFIRMEE);
            return query.getResultList();
        } finally {
            if (em.isOpen()) em.close();
        }
    }

    public List<Reservation> getDepartsDuJour() {
        EntityManager em = JPAUtils.getEntityManager();
        try {
            LocalDateTime debut = LocalDateTime.now().toLocalDate().atStartOfDay();
            LocalDateTime fin = debut.plusDays(1);
            TypedQuery<Reservation> query = em.createQuery(
                    "SELECT r FROM Reservation r WHERE r.dateDepart >= :debut AND r.dateDepart < :fin AND r.statut = :statut",
                    Reservation.class);
            query.setParameter("debut", debut);
            query.setParameter("fin", fin);
            query.setParameter("statut", Reservation.StatutReservation.EN_COURS);
            return query.getResultList();
        } finally {
            if (em.isOpen()) em.close();
        }
    }

    public double getChiffreAffairesDuJour() {
        EntityManager em = JPAUtils.getEntityManager();
        try {
            LocalDateTime debut = LocalDateTime.now().toLocalDate().atStartOfDay();
            LocalDateTime fin = debut.plusDays(1);
            TypedQuery<Double> query = em.createQuery(
                    "SELECT COALESCE(SUM(r.montantTotal), 0) FROM Reservation r WHERE r.dateArrivee >= :debut AND r.dateArrivee < :fin",
                    Double.class);
            query.setParameter("debut", debut);
            query.setParameter("fin", fin);
            return query.getSingleResult();
        } finally {
            if (em.isOpen()) em.close();
        }
    }

    public long getTotalReservations() {
        EntityManager em = JPAUtils.getEntityManager();
        try {
            return em.createQuery("SELECT COUNT(r) FROM Reservation r", Long.class).getSingleResult();
        } finally {
            if (em.isOpen()) em.close();
        }
    }

    public double calculerTauxOccupation() {
        EntityManager em = JPAUtils.getEntityManager();
        try {
            long totalChambres = em.createQuery("SELECT COUNT(c) FROM Chambre c", Long.class).getSingleResult();
            if (totalChambres == 0) return 0.0;
            long chambresOccupees = em.createQuery(
                    "SELECT COUNT(r) FROM Reservation r WHERE r.statut = :statut",
                    Long.class).setParameter("statut", Reservation.StatutReservation.EN_COURS).getSingleResult();
            return (double) chambresOccupees / totalChambres * 100;
        } finally {
            if (em.isOpen()) em.close();
        }
    }

    // === Check-in / Check-out ===
    public void checkIn(Reservation reservation) {
        EntityManager em = JPAUtils.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Reservation r = em.find(Reservation.class, reservation.getId());
            if (r != null && r.getStatut() == Reservation.StatutReservation.CONFIRMEE) {
                r.setStatut(Reservation.StatutReservation.EN_COURS);
            }
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw new RuntimeException("Erreur lors du check-in : " + e.getMessage(), e);
        } finally {
            if (em.isOpen()) em.close();
        }
    }

    public void checkOut(Reservation reservation) {
        EntityManager em = JPAUtils.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Reservation r = em.find(Reservation.class, reservation.getId());
            if (r != null && r.getStatut() == Reservation.StatutReservation.EN_COURS) {
                r.setStatut(Reservation.StatutReservation.TERMINEE);
            }
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw new RuntimeException("Erreur lors du check-out : " + e.getMessage(), e);
        } finally {
            if (em.isOpen()) em.close();
        }
    }
}
