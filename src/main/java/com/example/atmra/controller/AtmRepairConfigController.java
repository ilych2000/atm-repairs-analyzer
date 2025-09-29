package com.example.atmra.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.atmra.service.AtmRepairService;

import lombok.RequiredArgsConstructor;

/**
 * Контроллер конфигурации.
 */
@RestController
@RequiredArgsConstructor
public class AtmRepairConfigController {

    private final AtmRepairService atmRepairService;

    /**
     * Возвращает скрипт с конфигурацией.
     * 
     * @return javascript с конфигурацией
     */
    @GetMapping(value = "/config.js", produces = "application/javascript")
    public String getConfig() {
        return """
                const CONFIG = {
                    countTopMostCommonCauses : %d,
                    countTopLongestRepairTimes : %d,
                    countCauseFailureRecurred : %d,
                }
                """.formatted(
                atmRepairService.getCountTopMostCommonCauses(),
                atmRepairService.getCountTopLongestRepairTimes(),
                atmRepairService.getCountCauseFailureRecurred());
    }

}
