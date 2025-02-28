package org.mycore.ubo.dedup.jpa;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import jakarta.persistence.metamodel.SingularAttribute;
import org.mycore.backend.jpa.MCREntityManagerProvider;
import org.mycore.common.config.MCRConfiguration2;
import org.mycore.ubo.dedup.PossibleDuplicate;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Stream;

public class DeduplicationKeyManager {

    public DeduplicationKeyManager() {
    }

    public static DeduplicationKeyManager getInstance() {
        return MCRConfiguration2.getSingleInstanceOf("UBO.DeduplicationKeyManager", DeduplicationKeyManager.class)
                .get();
    }

    private static SingularAttribute getOrderColumn(DeduplicationNoDuplicateOrderFields by) {
        return switch (by) {
            case MCR_ID_1 -> DeduplicationNoDuplicate_.mcrId1;
            case MCR_ID_2 -> DeduplicationNoDuplicate_.mcrId2;
            case CREATOR -> DeduplicationNoDuplicate_.creator;
            case DATE -> DeduplicationNoDuplicate_.creationDate;
        };
    }

    public void clearDeduplicationKeys(String mcrId) {
        String deduplicationKeyNamedQuery = DeduplicationKey.DEDUPLICATION_KEY_DELETE_BY_MCR_ID;
        MCREntityManagerProvider.getCurrentEntityManager().createNamedQuery(deduplicationKeyNamedQuery)
                .setParameter("mcrId", mcrId)
                .executeUpdate();
    }

    public void clearNoDuplicates(String mcrId) {
        String deduplicationNoDuplicateNamedQuery = DeduplicationNoDuplicate.DEDUPLICATION_KEY_DELETE_BY_MCR_ID;
        MCREntityManagerProvider.getCurrentEntityManager().createNamedQuery(deduplicationNoDuplicateNamedQuery)
                .setParameter("mcrId", mcrId)
                .executeUpdate();
    }

    public void addDeduplicationKey(String mcrId, String type, String key) {
        EntityManager em = MCREntityManagerProvider.getCurrentEntityManager();
        DeduplicationKey deduplicationKey = new DeduplicationKey();
        deduplicationKey.setMcrId(mcrId);
        deduplicationKey.setDeduplicationType(type);
        deduplicationKey.setDeduplicationKey(key);
        em.persist(deduplicationKey);
    }

    public List<PossibleDuplicate> getDuplicates(SortOrder idSort, SortOrder typeSort, String duplicationTypeFilter) {
        EntityManager em = MCREntityManagerProvider.getCurrentEntityManager();

        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Object[]> query = cb.createQuery(Object[].class);

        Root<DeduplicationKey> dk1 = query.from(DeduplicationKey.class);
        Root<DeduplicationKey> dk2 = query.from(DeduplicationKey.class);

        Subquery<DeduplicationNoDuplicate> subquery = query.subquery(DeduplicationNoDuplicate.class);
        Root<DeduplicationNoDuplicate> dfp = subquery.from(DeduplicationNoDuplicate.class);
        Predicate falsePositiveCondition = cb.or(
                cb.and(
                        cb.equal(dfp.get(DeduplicationNoDuplicate_.mcrId1), dk1.get(DeduplicationKey_.mcrId)),
                        cb.equal(dfp.get(DeduplicationNoDuplicate_.mcrId2), dk2.get(DeduplicationKey_.mcrId))
                ),
                cb.and(
                        cb.equal(dfp.get(DeduplicationNoDuplicate_.mcrId1), dk2.get(DeduplicationKey_.mcrId)),
                        cb.equal(dfp.get(DeduplicationNoDuplicate_.mcrId2), dk1.get(DeduplicationKey_.mcrId))
                )
        );
        subquery.select(dfp).where(falsePositiveCondition);

        List<Predicate> predicates = new ArrayList<>();

        predicates.add(cb.equal(dk1.get(DeduplicationKey_.deduplicationKey), dk2.get(DeduplicationKey_.deduplicationKey)));
        predicates.add(cb.equal(dk1.get(DeduplicationKey_.deduplicationType), dk2.get(DeduplicationKey_.deduplicationType)));
        predicates.add(cb.notEqual(dk1.get(DeduplicationKey_.mcrId), dk2.get(DeduplicationKey_.mcrId)));
        predicates.add(cb.lessThan(dk1.get(DeduplicationKey_.mcrId), dk2.get(DeduplicationKey_.mcrId)));
        predicates.add(cb.not(cb.exists(subquery)));

        if (duplicationTypeFilter != null) {
            predicates.add(cb.equal(dk1.get(DeduplicationKey_.deduplicationType), duplicationTypeFilter));
        }


        Predicate duplicateCondition = cb.and(predicates.toArray(new Predicate[0]));

        CriteriaQuery<Object[]> where = query.multiselect(
                dk1.get(DeduplicationKey_.mcrId).alias("MCR_ID_1"),
                dk2.get(DeduplicationKey_.mcrId).alias("MCR_ID_2"),
                dk1.get(DeduplicationKey_.deduplicationType),
                dk1.get(DeduplicationKey_.deduplicationKey)
        ).where(duplicateCondition);


        List<Order> orders = new ArrayList<>();

        if (typeSort != SortOrder.NONE) {
            if (typeSort.equals(SortOrder.ASC)) {
                orders.add(cb.asc(dk1.get(DeduplicationKey_.deduplicationType)));
            } else {
                orders.add(cb.desc(dk1.get(DeduplicationKey_.deduplicationType)));
            }
        }

        if (idSort != SortOrder.NONE) {
            if (idSort.equals(SortOrder.ASC)) {
                orders.add(cb.asc(dk1.get(DeduplicationKey_.mcrId)));
            } else {
                orders.add(cb.desc(dk2.get(DeduplicationKey_.mcrId)));
            }
        }

        if (!orders.isEmpty()) {
            query = where.orderBy(orders);
        }

        List<Object[]> resultList = em.createQuery(query).getResultList();

        return resultList.stream().map(o -> new PossibleDuplicate((String) o[0], (String) o[1], (String) o[2], (String) o[3])).toList();
    }

    public List<DeduplicationKey> getDuplicates(String mcrId, String type, String key) {
        EntityManager em = MCREntityManagerProvider.getCurrentEntityManager();

        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<DeduplicationKey> query = cb.createQuery(DeduplicationKey.class);

        Root<DeduplicationKey> dk1 = query.from(DeduplicationKey.class);

        Subquery<String> subquery1 = query.subquery(String.class);
        Root<DeduplicationNoDuplicate> dfp1 = subquery1.from(DeduplicationNoDuplicate.class);
        Predicate falsePositiveCondition1 = cb.and(
                cb.equal(dfp1.get(DeduplicationNoDuplicate_.mcrId1), mcrId),
                cb.equal(dfp1.get(DeduplicationNoDuplicate_.mcrId2), dk1.get(DeduplicationKey_.mcrId))
        );
        subquery1.select(dfp1.get(DeduplicationNoDuplicate_.mcrId2)).where(falsePositiveCondition1);

        Subquery<String> subquery2 = query.subquery(String.class);
        Root<DeduplicationNoDuplicate> dfp2 = subquery2.from(DeduplicationNoDuplicate.class);
        Predicate falsePositiveCondition2 = cb.and(
                cb.equal(dfp2.get(DeduplicationNoDuplicate_.mcrId2), mcrId),
                cb.equal(dfp2.get(DeduplicationNoDuplicate_.mcrId1), dk1.get(DeduplicationKey_.mcrId))
        );
        subquery2.select(dfp2.get(DeduplicationNoDuplicate_.mcrId1)).where(falsePositiveCondition2);

        Predicate duplicateCondition = cb.and(
                cb.equal(dk1.get(DeduplicationKey_.deduplicationKey), key),
                cb.equal(dk1.get(DeduplicationKey_.deduplicationType), type),
                cb.notEqual(dk1.get(DeduplicationKey_.mcrId), mcrId),
                cb.not(dk1.get(DeduplicationKey_.mcrId).in(subquery1)),
                cb.not(dk1.get(DeduplicationKey_.mcrId).in(subquery2))
        );

        query.select(dk1).where(duplicateCondition);
        return em.createQuery(query).getResultList();
    }

    public List<DeduplicationKey> getDuplicates(String... keys) {
        EntityManager em = MCREntityManagerProvider.getCurrentEntityManager();

        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<DeduplicationKey> query = cb.createQuery(DeduplicationKey.class);

        Root<DeduplicationKey> from = query.from(DeduplicationKey.class);
        List<Predicate> list = Stream.of(keys).map(k -> cb.equal(from.get(DeduplicationKey_.deduplicationKey), k)).toList();
        CriteriaQuery<DeduplicationKey> select = query.select(from).where(cb.or(list.toArray(new Predicate[0])));
        return em.createQuery(select).getResultList();
    }

    public void addNoDuplicate(String id, String duplicateOf, String creator, Date date) {
        EntityManager em = MCREntityManagerProvider.getCurrentEntityManager();
        DeduplicationNoDuplicate noDuplicate = new DeduplicationNoDuplicate();
        noDuplicate.setMcrId1(id);
        noDuplicate.setMcrId2(duplicateOf);
        noDuplicate.setCreator(creator);
        noDuplicate.setCreationDate(date);
        em.persist(noDuplicate);
    }

    public void removeNoDuplicate(int id) {
        EntityManager em = MCREntityManagerProvider.getCurrentEntityManager();
        DeduplicationNoDuplicate noDuplicate = em.find(DeduplicationNoDuplicate.class, id);
        em.remove(noDuplicate);
    }

    public List<DeduplicationNoDuplicate> getNoDuplicates(SortOrder order, DeduplicationNoDuplicateOrderFields by) {
        EntityManager em = MCREntityManagerProvider.getCurrentEntityManager();
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<DeduplicationNoDuplicate> query = cb.createQuery(DeduplicationNoDuplicate.class);
        Root<DeduplicationNoDuplicate> from = query.from(DeduplicationNoDuplicate.class);
        List<Order> orders = new ArrayList<>();
        if (order != SortOrder.NONE) {
            SingularAttribute orderField = getOrderColumn(by);
            if (order.equals(SortOrder.ASC)) {
                orders.add(cb.asc(from.get(orderField)));
            } else {
                orders.add(cb.desc(from.get(orderField)));
            }
        }
        if (!orders.isEmpty()) {
            query = query.orderBy(orders);
        }
        return em.createQuery(query).getResultList();
    }

    public enum SortOrder {
        ASC, DESC, NONE
    }

    public enum DeduplicationNoDuplicateOrderFields {
        MCR_ID_1, MCR_ID_2, CREATOR, DATE;
    }

}
