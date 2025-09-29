package com.example.atmra.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.atmra.entity.AtmRepair;

/**
 * Репозиторий для данных ремонтов.
 */
public interface AtmRepairRepository extends JpaRepository<AtmRepair, Long> {
}
