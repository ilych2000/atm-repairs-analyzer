package com.example.atmra.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.atmra.service.AtmRepairConfiguration;
import lombok.RequiredArgsConstructor;

/**
 * Контроллер конфигурации.
 */
@RestController
@RequiredArgsConstructor
public class AtmRepairConfigController {

    private final AtmRepairConfiguration atmRepairConfiguration;

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
                atmRepairConfiguration.getCountTopMostCommonCauses(),
                atmRepairConfiguration.getCountTopLongestRepairTimes(),
                atmRepairConfiguration.getCountCauseFailureRecurred());
    }

}
