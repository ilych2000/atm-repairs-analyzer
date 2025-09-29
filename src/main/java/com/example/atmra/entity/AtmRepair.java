package com.example.atmra.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Сущность данных ремонта.
 */
@Entity
@Table(name = "repairs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = {"caseId"})
public class AtmRepair {

    /** Идентификатор записи ремонта */
    @Id
    @Column(name = "case_id", columnDefinition = "INTEGER")
    private Long caseId;

    /** Идентификатор АТМ */
    @Column(name = "atm_id", nullable = false, columnDefinition = "TEXT")
    private String atmId;

    /** Причина ремонта */
    @Column(name = "reason", nullable = false, columnDefinition = "TEXT")
    private String reason;

    /** Дата и время начала ремонта */
    @Column(name = "start_time", nullable = false, columnDefinition = "TEXT")
    private LocalDateTime startTime;

    /** Дата и время окончания ремонта */
    @Column(name = "end_time", columnDefinition = "TEXT")
    private LocalDateTime endTime;

    /** Серийный номер АТМ */
    @Column(name = "serial_number", nullable = false, columnDefinition = "TEXT")
    private String serialNumber;

    /** Наименование банка */
    @Column(name = "bank_nm", nullable = false, columnDefinition = "TEXT")
    private String bankName;

    /** Канал */
    @Column(name = "channel", nullable = false, columnDefinition = "TEXT")
    private String channel;

}
