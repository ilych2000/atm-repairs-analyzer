package com.example.atmra.service;

import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.atmra.dto.AtmRepairGroupDto;
import com.example.atmra.entity.AtmRepair;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

/**
 * Сервис работы с таблицей ремонтов посредством Сriteria API.
 */
@Service
@ConditionalOnProperty(name = "atm-service.type", havingValue = "cb")
public class AtmRepairCBService extends AbstractAtmRepairService {

    @Autowired
    private EntityManager entityManager;

    @Override
    @Transactional(readOnly = true)
    public List<Object> findMostCommonCauses() {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Object[]> query = cb.createQuery(Object[].class);
        Root<AtmRepair> root = query.from(AtmRepair.class);

        query.multiselect(
                root.get("reason"),
                cb.count(root));
        query.groupBy(root.get("reason"));
        query.orderBy(cb.desc(cb.count(root)));

        List<Object[]> results = entityManager.createQuery(query)
                .setMaxResults(atmRepairConfiguration.getCountTopMostCommonCauses())
                .getResultList();

        List<Object> result = new ArrayList<>();
        for (Object[] row : results) {
            String reason = (String) row[0];
            Long count = (Long) row[1];

            result.add(AtmRepairGroupDto.builder()
                    .groupTitle("%s (Всего: %s)".formatted(reason, count))
                    .build());

            List<AtmRepair> repairs = atmRepairRepository.findRepairsByReason(reason);
            result.addAll(atmRepairMapper.toDtoList(repairs));
        }
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Object> findLongestRepairTimes() {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<AtmRepair> query = cb.createQuery(AtmRepair.class);
        Root<AtmRepair> root = query.from(AtmRepair.class);

        Expression<Long> durationInHours = cb.diff(
                cb.function("julianday", Long.class, root.get("endTime")),
                cb.function("julianday", Long.class, root.get("startTime")));
        query.select(root);
        query.orderBy(cb.desc(durationInHours));

        List<AtmRepair> longestRepairs = entityManager.createQuery(query)
                .setMaxResults(atmRepairConfiguration.getCountTopLongestRepairTimes())
                .getResultList();

        List<Object> result = new ArrayList<>();
        for (AtmRepair repair : longestRepairs) {
            long hours = ChronoUnit.HOURS.between(repair.getStartTime(), repair.getEndTime());

            result.add(AtmRepairGroupDto.builder()
                    .groupTitle("%s. Время ремонта %d часов".formatted(repair.getReason(), hours))
                    .build());
            result.add(atmRepairMapper.toDto(repair));
        }
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Object> findCauseFailureRecurred() {
        // Сначала получаем все комбинации ATM+причина, которые повторяются
        List<Object[]> recurringCombinations = findRecurringAtmReasonCombinations();

        List<Object> result = new ArrayList<>();
        for (Object[] combination : recurringCombinations) {
            String atmId = (String) combination[0];
            String reason = (String) combination[1];

            List<AtmRepair> repairs = findRepairsByAtmAndReasonWithRecurrence(atmId, reason);
            if (!repairs.isEmpty()) {
                result.add(AtmRepairGroupDto.builder()
                        .groupTitle("АТМ: %s. %s".formatted(atmId, reason))
                        .build());
                result.addAll(atmRepairMapper.toDtoList(repairs));
            }
        }
        return result;
    }

    /**
     * Находит комбинации ATM + причина, которые повторяются
     *
     * @return список комбинации ATM + причина, которые повторяются
     */
    private List<Object[]> findRecurringAtmReasonCombinations() {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Object[]> query = cb.createQuery(Object[].class);
        Root<AtmRepair> root = query.from(AtmRepair.class);
        query.multiselect(
                root.get("atmId"),
                root.get("reason"),
                cb.count(root));
        query.groupBy(root.get("atmId"), root.get("reason"));
        query.having(cb.gt(cb.count(root), 1));
        return entityManager.createQuery(query).getResultList();
    }

    /**
     * Находит ремонты по ATM и причине с проверкой повторяемости
     *
     * @param atmId идентификато АТМ
     * @param reason причина ремонта
     * @return список ремонтов
     */
    private List<AtmRepair> findRepairsByAtmAndReasonWithRecurrence(String atmId, String reason) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<AtmRepair> query = cb.createQuery(AtmRepair.class);
        Root<AtmRepair> root = query.from(AtmRepair.class);

        Predicate conditions = cb.and(
                cb.equal(root.get("atmId"), atmId),
                cb.equal(root.get("reason"), reason));

        query.select(root).where(conditions);
        query.orderBy(cb.asc(root.get("startTime")));

        List<AtmRepair> allRepairs = entityManager.createQuery(query).getResultList();
        return filterRepairsByRecurrence(allRepairs);
    }

    /**
     * Фильтрует ремонты по критерию повторяемости
     *
     * @param repairs список ремонтов
     * @return отфильтрованный список ремонтов
     */
    private List<AtmRepair> filterRepairsByRecurrence(List<AtmRepair> repairs) {
        List<AtmRepair> recurring = new ArrayList<>();
        int countCauseFailureRecurred = atmRepairConfiguration.getCountCauseFailureRecurred();
        int size = repairs.size();
        for (int i = 1; i < size; i++) {
            AtmRepair previous = repairs.get(i - 1);
            AtmRepair current = repairs.get(i);
            long daysBetween = ChronoUnit.DAYS.between(
                    previous.getStartTime(),
                    current.getStartTime());

            if (daysBetween <= countCauseFailureRecurred) {
                if (recurring.isEmpty() || (recurring.getLast() != previous)) {
                    recurring.add(previous);
                }
                recurring.add(current);
            }
        }
        return recurring;
    }

}
