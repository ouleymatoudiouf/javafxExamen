package sn.ouleymatou.hotelmanagement.services;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;
import sn.ouleymatou.hotelmanagement.entities.Chambre;
import sn.ouleymatou.hotelmanagement.entities.Reservation;
import sn.ouleymatou.hotelmanagement.utils.JPAUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ReservationService {

    // Génération du numéro de réservation
    public String genererNumeroReservation() {
        EntityManager em = JPAUtils.getEntityManager();
        try {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime startOfDay = now.toLocalDate().atStartOfDay();
            LocalDateTime endOfDay = startOfDay.plusDays(1);

            TypedQuery<Long> query = em.createQuery(
                    "SELECT COUNT(r) FROM Reservation r WHERE r.dateReservation >= :startOfDay AND r.dateReservation < :endOfDay",
                    Long.class);
            query.setParameter("startOfDay", startOfDay);
            query.setParameter("endOfDay", endOfDay);

            Long count = query.getSingleResult();
            String datePart = now.format(java.time.format.DateTimeFormatter.BASIC_ISO_DATE);
            return String.format("RSV-%s-%03d", datePart, count + 1);
        } finally {
            if (em.isOpen()) em.close();
        }
    }

    // Enregistrement d'une réservation
    public void enregistrerReservation(Reservation reservation) {
        EntityManager em = JPAUtils.getEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            // Validation client
            if (reservation.getNomClient() == null || reservation.getNomClient().length() < 2
                    || !reservation.getNomClient().matches("^[A-Za-zÀ-ÖØ-öø-ÿ\\s'-]+$")) {
                throw new IllegalArgumentException("Nom client invalide : minimum 2 caractères.");
            }
            if (reservation.getPrenomClient() == null || reservation.getPrenomClient().length() < 2
                    || !reservation.getPrenomClient().matches("^[A-Za-zÀ-ÖØ-öø-ÿ\\s'-]+$")) {
                throw new IllegalArgumentException("Prénom client invalide : minimum 2 caractères.");
            }
            if (reservation.getTelephone() == null || !reservation.getTelephone().matches("^(77|78|75|76|70)\\d{7}$")) {
                throw new IllegalArgumentException("Téléphone invalide.");
            }
            if (reservation.getEmail() != null && !reservation.getEmail().isEmpty()) {
                if (!reservation.getEmail().matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) {
                    throw new IllegalArgumentException("Email invalide.");
                }
            }

            // Validation dates
            LocalDateTime maintenant = LocalDateTime.now();
            LocalDateTime arrivee = reservation.getDateArrivee();
            LocalDateTime depart = reservation.getDateDepart();

            if (arrivee.toLocalTime().equals(LocalTime.MIDNIGHT)) arrivee = arrivee.withHour(14);
            if (depart.toLocalTime().equals(LocalTime.MIDNIGHT)) depart = depart.withHour(12);

            if (arrivee.isBefore(maintenant)) throw new IllegalArgumentException("Date d'arrivée dans le passé.");
            if (!depart.isAfter(arrivee)) throw new IllegalArgumentException("Date de départ avant arrivée.");

            // Vérification chambre
            Chambre chambre = em.find(Chambre.class, reservation.getChambre().getId());
            if (chambre == null) throw new IllegalArgumentException("Chambre non trouvée.");
            if (chambre.getStatut() == Chambre.StatutChambre.HORS_SERVICE)
                throw new IllegalArgumentException("Chambre hors service.");

            // Capacité
            if (reservation.getNombrePersonnes() < 1 || reservation.getNombrePersonnes() > chambre.getCapacite()) {
                throw new IllegalArgumentException("Nombre de personnes dépasse capacité.");
            }

            // Disponibilité (pas de chevauchement)
            TypedQuery<Long> query = em.createQuery(
                    "SELECT COUNT(r) FROM Reservation r WHERE r.chambre = :chambre AND r.statut <> :terminee " +
                            "AND r.dateDepart > :arrivee AND r.dateArrivee < :depart",
                    Long.class);
            query.setParameter("chambre", chambre);
            query.setParameter("arrivee", arrivee);
            query.setParameter("depart", depart);
            query.setParameter("terminee", Reservation.StatutReservation.TERMINEE);

            if (query.getSingleResult() > 0) throw new IllegalArgumentException("Chambre déjà réservée.");

            // Calcul nuits et montant
            int nuits = (int) ChronoUnit.DAYS.between(arrivee.toLocalDate(), depart.toLocalDate());
            if (nuits < 1) nuits = 1;
            reservation.setNombreNuits(nuits);
            double montantTotal = nuits * chambre.getTarifParNuit();
            reservation.setMontantTotal(montantTotal);

            // Vérification acompte
            if (reservation.getAcompte() < montantTotal * 0.3 || reservation.getAcompte() > montantTotal) {
                throw new IllegalArgumentException("Acompte entre 30% et 100% du total.");
            }

            // Numéro réservation
            if (reservation.getNumero() == null || reservation.getNumero().isEmpty()) {
                reservation.setNumero(genererNumeroReservation());
            }

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

    // Liste complète
    public List<Reservation> getAllReservations() {
        EntityManager em = JPAUtils.getEntityManager();
        try {
            return em.createQuery("SELECT r FROM Reservation r JOIN FETCH r.chambre", Reservation.class)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    // Arrivées du jour
    public List<Reservation> getArriveesDuJour() {
        EntityManager em = JPAUtils.getEntityManager();
        try {
            LocalDate aujourdHui = LocalDate.now();
            TypedQuery<Reservation> query = em.createQuery(
                    "SELECT r FROM Reservation r JOIN FETCH r.chambre " +
                            "WHERE FUNCTION('DATE', r.dateArrivee) = :aujourdHui " +
                            "AND r.statut = :statut", Reservation.class
            );
            query.setParameter("aujourdHui", aujourdHui);
            query.setParameter("statut", Reservation.StatutReservation.CONFIRMEE);
            return query.getResultList();
        } finally {
            if (em.isOpen()) em.close();
        }
    }

    // Départs du jour
    public List<Reservation> getDepartsDuJour() {
        EntityManager em = JPAUtils.getEntityManager();
        try {
            LocalDate aujourdHui = LocalDate.now();
            TypedQuery<Reservation> query = em.createQuery(
                    "SELECT r FROM Reservation r JOIN FETCH r.chambre " +
                            "WHERE FUNCTION('DATE', r.dateDepart) = :aujourdHui " +
                            "AND r.statut = :statut", Reservation.class
            );
            query.setParameter("aujourdHui", aujourdHui);
            query.setParameter("statut", Reservation.StatutReservation.EN_COURS);
            return query.getResultList();
        } finally {
            if (em.isOpen()) em.close();
        }
    }

    // Chiffre d'affaires du jour
    public double getChiffreAffairesDuJour() {
        EntityManager em = JPAUtils.getEntityManager();
        try {
            LocalDateTime debut = LocalDate.now().atStartOfDay();
            LocalDateTime fin = debut.plusDays(1);
            TypedQuery<Double> query = em.createQuery(
                    "SELECT COALESCE(SUM(r.montantTotal), 0) FROM Reservation r WHERE r.dateArrivee >= :debut AND r.dateArrivee < :fin",
                    Double.class
            );
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
                            "SELECT COUNT(r) FROM Reservation r WHERE r.statut = :statut", Long.class)
                    .setParameter("statut", Reservation.StatutReservation.EN_COURS)
                    .getSingleResult();
            return (double) chambresOccupees / totalChambres * 100;
        } finally {
            if (em.isOpen()) em.close();
        }
    }

    // Check-in
    public void checkIn(Reservation reservation) {
        EntityManager em = JPAUtils.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Reservation r = em.find(Reservation.class, reservation.getId());
            if (r != null && r.getStatut() == Reservation.StatutReservation.CONFIRMEE) {
                r.setStatut(Reservation.StatutReservation.EN_COURS);
                Chambre chambre = r.getChambre();
                chambre.setStatut(Chambre.StatutChambre.OCCUPEE);
                em.merge(chambre);
                em.merge(r);
            }
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw new RuntimeException("Erreur lors du check-in : " + e.getMessage(), e);
        } finally {
            if (em.isOpen()) em.close();
        }
    }

    // Check-out
    public void checkOut(Reservation reservation) {
        EntityManager em = JPAUtils.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Reservation r = em.find(Reservation.class, reservation.getId());
            if (r != null && r.getStatut() == Reservation.StatutReservation.EN_COURS) {
                r.setStatut(Reservation.StatutReservation.TERMINEE);
                Chambre chambre = r.getChambre();
                chambre.setStatut(Chambre.StatutChambre.LIBRE);
                em.merge(chambre);
                em.merge(r);
            }
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw new RuntimeException("Erreur lors du check-out : " + e.getMessage(), e);
        } finally {
            if (em.isOpen()) em.close();
        }
    }

    // Chiffre d’affaires entre deux dates
    public double calculChiffreAffaires(LocalDate debut, LocalDate fin) {
        EntityManager em = JPAUtils.getEntityManager();
        try {
            LocalDateTime start = debut.atStartOfDay();
            LocalDateTime end = fin.plusDays(1).atStartOfDay();
            TypedQuery<Double> query = em.createQuery(
                    "SELECT COALESCE(SUM(r.montantTotal), 0) FROM Reservation r WHERE r.dateArrivee >= :start AND r.dateArrivee < :end",
                    Double.class
            );
            query.setParameter("start", start);
            query.setParameter("end", end);
            return query.getSingleResult();
        } finally {
            if (em.isOpen()) em.close();
        }
    }

    // Client ayant le plus de réservations
    public String getClientLePlusFidele(LocalDate debut, LocalDate fin) {
        EntityManager em = JPAUtils.getEntityManager();
        try {
            LocalDateTime start = debut.atStartOfDay();
            LocalDateTime end = fin.plusDays(1).atStartOfDay();
            TypedQuery<Object[]> query = em.createQuery(
                    "SELECT r.nomClient, r.prenomClient, COUNT(r) FROM Reservation r " +
                            "WHERE r.dateArrivee >= :start AND r.dateArrivee < :end " +
                            "GROUP BY r.nomClient, r.prenomClient ORDER BY COUNT(r) DESC",
                    Object[].class
            );
            query.setParameter("start", start);
            query.setParameter("end", end);
            query.setMaxResults(1);
            List<Object[]> result = query.getResultList();
            if (!result.isEmpty()) {
                Object[] row = result.get(0);
                return row[0] + " " + row[1];
            }
            return null;
        } finally {
            if (em.isOpen()) em.close();
        }
    }

    // Durée moyenne de séjour
    public double getDureeMoyenneSejour(LocalDate debut, LocalDate fin) {
        EntityManager em = JPAUtils.getEntityManager();
        try {
            LocalDateTime start = debut.atStartOfDay();
            LocalDateTime end = fin.plusDays(1).atStartOfDay();
            TypedQuery<Double> query = em.createQuery(
                    "SELECT COALESCE(AVG(r.nombreNuits), 0) FROM Reservation r " +
                            "WHERE r.dateArrivee >= :start AND r.dateArrivee < :end",
                    Double.class
            );
            query.setParameter("start", start);
            query.setParameter("end", end);
            return query.getSingleResult();
        } finally {
            if (em.isOpen()) em.close();
        }
    }

    // Nombre d’annulations
    public int getNombreAnnulations(LocalDate debut, LocalDate fin) {
        EntityManager em = JPAUtils.getEntityManager();
        try {
            LocalDateTime start = debut.atStartOfDay();
            LocalDateTime end = fin.plusDays(1).atStartOfDay();
            TypedQuery<Long> query = em.createQuery(
                    "SELECT COUNT(r) FROM Reservation r WHERE r.statut = :annulee AND r.dateArrivee >= :start AND r.dateArrivee < :end",
                    Long.class
            );
            query.setParameter("annulee", Reservation.StatutReservation.ANNULEE);
            query.setParameter("start", start);
            query.setParameter("end", end);
            return query.getSingleResult().intValue();
        } finally {
            if (em.isOpen()) em.close();
        }
    }

    // Nombre total de nuits vendues
    public int getNombreNuitsVendues(LocalDate debut, LocalDate fin) {
        EntityManager em = JPAUtils.getEntityManager();
        try {
            LocalDateTime start = debut.atStartOfDay();
            LocalDateTime end = fin.plusDays(1).atStartOfDay();
            TypedQuery<Long> query = em.createQuery(
                    "SELECT COALESCE(SUM(r.nombreNuits), 0) FROM Reservation r WHERE r.dateArrivee >= :start AND r.dateArrivee < :end",
                    Long.class
            );
            query.setParameter("start", start);
            query.setParameter("end", end);
            return query.getSingleResult().intValue();
        } finally {
            if (em.isOpen()) em.close();
        }
    }
    // Réservations par mois (pour LineChart)
    public Map<String, Integer> getNombreReservationsParMois(LocalDate debut, LocalDate fin) {
        EntityManager em = JPAUtils.getEntityManager();
        try {
            LocalDateTime start = debut.atStartOfDay();
            LocalDateTime end = fin.plusDays(1).atStartOfDay();

            TypedQuery<Object[]> query = em.createQuery(
                    "SELECT EXTRACT(MONTH FROM r.dateArrivee), COUNT(r) " +
                            "FROM Reservation r " +
                            "WHERE r.dateArrivee >= :start AND r.dateArrivee < :end " +
                            "GROUP BY EXTRACT(MONTH FROM r.dateArrivee) " +
                            "ORDER BY EXTRACT(MONTH FROM r.dateArrivee)",
                    Object[].class
            );

            query.setParameter("start", start);
            query.setParameter("end", end);

            List<Object[]> resultList = query.getResultList();

            Map<String, Integer> stats = new LinkedHashMap<>(); // pour garder l'ordre des mois
            for (Object[] row : resultList) {
                int mois = ((Number) row[0]).intValue();
                String moisNom = java.time.Month.of(mois).name();
                stats.put(moisNom, ((Number) row[1]).intValue());
            }
            return stats;
        } finally {
            if (em.isOpen()) em.close();
        }
    }


}
