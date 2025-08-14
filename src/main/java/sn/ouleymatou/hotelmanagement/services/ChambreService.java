package sn.ouleymatou.hotelmanagement.services;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;
import sn.ouleymatou.hotelmanagement.entities.Chambre;
import sn.ouleymatou.hotelmanagement.entities.Reservation;
import sn.ouleymatou.hotelmanagement.entities.TypeChambre;
import sn.ouleymatou.hotelmanagement.utils.JPAUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class ChambreService {

    public List<Chambre> lister() {
        EntityManager em = JPAUtils.getEntityManager();
        try {
            return em.createQuery("SELECT c FROM Chambre c", Chambre.class).getResultList();
        } finally {
            if (em != null) em.close();
        }
    }

    public Chambre save(Chambre chambre) {
        EntityManager em = JPAUtils.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            if (chambre.getId() == null) {
                em.persist(chambre);
            } else {
                chambre = em.merge(chambre);
            }
            tx.commit();
            return chambre;
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw new RuntimeException("Erreur lors de l'enregistrement de la chambre", e);
        } finally {
            if (em != null) em.close();
        }
    }

    public void supprimerChambre(Long chambreId) {
        EntityManager em = JPAUtils.getEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();
            Chambre chambre = em.find(Chambre.class, chambreId);
            if (chambre == null) throw new IllegalArgumentException("Chambre non trouvée.");

            TypedQuery<Long> query = em.createQuery(
                    "SELECT COUNT(r) FROM Reservation r WHERE r.chambre = :chambre AND r.dateArrivee >= :aujourdHui AND r.statut != :annule",
                    Long.class
            );
            query.setParameter("chambre", chambre);
            query.setParameter("aujourdHui", LocalDate.now());
            query.setParameter("annule", Reservation.StatutReservation.ANNULEE);

            Long countReservationsFutures = query.getSingleResult();
            if (countReservationsFutures > 0)
                throw new IllegalStateException("Impossible de supprimer la chambre car elle a des réservations futures.");

            em.remove(chambre);
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw new RuntimeException("Erreur lors de la suppression de la chambre : " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }

    // Méthode pour compatibilité avec le controller
    public void supprimer(Chambre chambre) {
        if (chambre != null) {
            supprimerChambre(chambre.getId());
        }
    }

    public List<Chambre> getChambresDisponibles(LocalDateTime dateArrivee, LocalDateTime dateDepart) {
        EntityManager em = JPAUtils.getEntityManager();
        try {
            String jpql = "SELECT c FROM Chambre c WHERE c.statut = :statut AND " +
                    "c.id NOT IN (SELECT r.chambre.id FROM Reservation r WHERE " +
                    "(:dateArrivee < r.dateDepart AND :dateDepart > r.dateArrivee))";
            TypedQuery<Chambre> query = em.createQuery(jpql, Chambre.class);
            query.setParameter("statut", Chambre.StatutChambre.LIBRE);
            query.setParameter("dateArrivee", dateArrivee);
            query.setParameter("dateDepart", dateDepart);
            return query.getResultList();
        } finally {
            if (em != null) em.close();
        }
    }

    public Chambre findById(Long id) {
        EntityManager em = JPAUtils.getEntityManager();
        try {
            return em.find(Chambre.class, id);
        } finally {
            if (em != null) em.close();
        }
    }

    public void ajouter(Chambre chambre) {
        if (chambre == null) throw new IllegalArgumentException("La chambre ne peut pas être nulle.");
        if (chambre.getNumero() == null || chambre.getNumero().isEmpty()) {
            chambre.setNumero(genererNumero(chambre.getTypeChambre(), chambre.getEtage()));
        }
        if (chambre.getStatut() == null) chambre.setStatut(Chambre.StatutChambre.LIBRE);
        save(chambre);
    }

    public String genererNumero(TypeChambre type, int etage) {
        if (type == null || type.getCode() == null)
            throw new IllegalArgumentException("Le type de chambre est requis pour générer un numéro.");

        String typeCode = type.getCode().toUpperCase();
        String prefix = "CH";
        String etageStr = String.format("%02d", etage);

        List<Chambre> chambres = lister();
        int maxSeq = 0;

        for (Chambre c : chambres) {
            String num = c.getNumero();
            if (num != null && num.startsWith(prefix + "-" + typeCode + "-" + etageStr + "-")) {
                String[] parts = num.split("-");
                if (parts.length == 4) {
                    try {
                        int seq = Integer.parseInt(parts[3]);
                        if (seq > maxSeq) maxSeq = seq;
                    } catch (NumberFormatException ignored) {}
                }
            }
        }

        return String.format("%s-%s-%s-%03d", prefix, typeCode, etageStr, maxSeq + 1);
    }

    public Chambre findByNumero(String numero) {
        EntityManager em = JPAUtils.getEntityManager();
        try {
            return em.createQuery("SELECT c FROM Chambre c WHERE c.numero = :numero", Chambre.class)
                    .setParameter("numero", numero)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        } finally {
            em.close();
        }
    }

    public long count() {
        EntityManager em = JPAUtils.getEntityManager();
        try {
            return em.createQuery("SELECT COUNT(c) FROM Chambre c", Long.class).getSingleResult();
        } finally {
            if (em != null) em.close();
        }
    }

    public void modifierChambre(Chambre chambreModifiee) {
        EntityManager em = JPAUtils.getEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();
            Chambre chambreExistante = em.find(Chambre.class, chambreModifiee.getId());
            if (chambreExistante == null)
                throw new IllegalArgumentException("Chambre non trouvée.");

            LocalDate aujourdHui = LocalDate.now();

            TypedQuery<Long> query = em.createQuery(
                    "SELECT COUNT(r) FROM Reservation r " +
                            "WHERE r.chambre = :chambre AND r.dateArrivee >= :aujourdHui AND r.statut != :annule",
                    Long.class
            );
            query.setParameter("chambre", chambreExistante);
            query.setParameter("aujourdHui", aujourdHui);
            query.setParameter("annule", Reservation.StatutReservation.ANNULEE);

            Long countReservationsFutures = query.getSingleResult();
            if (countReservationsFutures > 0)
                throw new IllegalStateException("Impossible de modifier la chambre car elle a des réservations futures.");

            chambreExistante.setNumero(chambreModifiee.getNumero());
            chambreExistante.setTypeChambre(chambreModifiee.getTypeChambre());
            chambreExistante.setStatut(chambreModifiee.getStatut());
            chambreExistante.setEtage(chambreModifiee.getEtage());
            chambreExistante.setClimatisation(chambreModifiee.isClimatisation());
            chambreExistante.setBalcon(chambreModifiee.isBalcon());
            chambreExistante.setVueOcean(chambreModifiee.isVueOcean());
            chambreExistante.setDateDerniereRenovation(chambreModifiee.getDateDerniereRenovation());

            em.merge(chambreExistante);
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw new RuntimeException("Erreur lors de la modification de la chambre : " + e.getMessage(), e);
        } finally {
            em.close();
        }
    }
    public List<Chambre> filtrer(String type, String statut) {
        EntityManager em = JPAUtils.getEntityManager();
        try {
            StringBuilder jpql = new StringBuilder("SELECT c FROM Chambre c WHERE 1=1");

            if (type != null && !type.equals("Tous")) {
                jpql.append(" AND c.typeChambre.libelle = :type");
            }
            if (statut != null && !statut.equals("Tous")) {
                jpql.append(" AND c.statut = :statut");
            }

            TypedQuery<Chambre> query = em.createQuery(jpql.toString(), Chambre.class);

            if (type != null && !type.equals("Tous")) {
                query.setParameter("type", type);
            }
            if (statut != null && !statut.equals("Tous")) {
                query.setParameter("statut", Chambre.StatutChambre.valueOf(statut));
            }

            return query.getResultList();
        } finally {
            if (em != null) em.close();
        }
    }

}
