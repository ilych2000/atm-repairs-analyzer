package com.example.atmra.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO заголовка группы данных ремонтов.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AtmRepairGroupDto {

    /** Текст заголовка группы данных ремонтов */
    @NotBlank
    private String groupTitle;

}
