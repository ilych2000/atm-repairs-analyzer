package com.example.atmra.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.atmra.entity.AtmRepair;

/**
 * Репозиторий для данных ремонтов.
 */
public interface AtmRepairRepository extends JpaRepository<AtmRepair, Long> {

    /**
     * Возвращает список ремонтов по причине.
     * 
     * @param reason причина ремонта
     * @return список ремонтов
     */
    List<AtmRepair> findRepairsByReason(String reason);

}
