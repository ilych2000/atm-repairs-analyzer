package com.example.atmra.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * DTO данных ремонта.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "caseId")
public class AtmRepairDto {

    /** Идентификатор записи ремонта */
    @NotNull
    private Long caseId;

    /** Идентификатор АТМ */
    @NotNull
    private String atmId;

    /** Причина ремонта */
    @NotBlank
    private String reason;

    /** Дата и время начала ремонта */
    @NotNull
    private LocalDateTime startTime;

    /** Дата и время окончания ремонта */
    private LocalDateTime endTime;

    /** Серийный номер АТМ */
    @NotBlank
    private String serialNumber;

    /** Наименование банка */
    @NotBlank
    private String bankName;

    /** Канал */
    @NotBlank
    private String channel;

}
