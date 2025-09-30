package com.example.atmra.service;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

/**
 * Конфигурация.
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "atm-repairs-analizer")
public class AtmRepairConfiguration {

    /** Количество наиболее часто встречающихся причин неисправности */
    private int countTopMostCommonCauses = 3;

    /** Количество наиболее долгих ремонта */
    private int countTopLongestRepairTimes = 3;

    /** Количество дней за которые причина поломки повторилась */
    private int countCauseFailureRecurred = 15;

}
