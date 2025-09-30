package com.example.atmra.service;

import java.util.List;

import com.example.atmra.dto.AtmRepairDto;

/**
 * Интерфейс сервиса работы с таблицей ремонтов.
 */
public interface IAtmRepairService {

    /**
     * Обновляет запись в таблице ремонтов.
     * 
     * @param dto данные для обновления
     * @return обновленная запись из БД
     */
    AtmRepairDto update(AtmRepairDto dto);

    /**
     * Удаляет все записи в таблице ремонтов.
     * 
     * @return результат выполнения
     */
    void deleteAll();

    /**
     * Возвращает все данные из таблицы ремонтов.
     * 
     * @return набор данных
     */
    List<AtmRepairDto> findAll();

    /**
     * Обновляет или создает запись в таблице ремонтов.
     * 
     * @param dtoList данные для обновления
     */
    void createOrUpdate(List<AtmRepairDto> dtoList);

    /**
     * Возвращает из таблицы ремонтов наиболее часто встречающиеся причины неисправности.
     * 
     * @return данные
     */
    List<Object> findMostCommonCauses();

    /**
     * Возвращает из таблицы ремонтов наиболее долгие ремонты.
     * 
     * @return данные
     */
    List<Object> findLongestRepairTimes();

    /**
     * Возвращает из таблицы ремонтов причина поломки которые повторилась в течение определенного
     * количества дней.
     * 
     * @return данные
     */
    Object findCauseFailureRecurred();

}
