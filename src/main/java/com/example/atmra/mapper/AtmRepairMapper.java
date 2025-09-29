package com.example.atmra.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;

import com.example.atmra.dto.AtmRepairDto;
import com.example.atmra.entity.AtmRepair;

/**
 * Преобразователь данных для сущности {@link AtmRepair} и DTO {@link AtmRepairDto}.
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface AtmRepairMapper {

    /**
     * Возвращает сушность с данными из DTO.
     * 
     * @param dto DTO {@link AtmRepairDto}
     * @return сущность {@link AtmRepair}
     */
    AtmRepair toEntity(AtmRepairDto dto);

    /**
     * Возвращает DTO с данными из сушности.
     * 
     * @param entity сущность {@link AtmRepair}
     * @return DTO {@link AtmRepairDto}
     */
    AtmRepairDto toDto(AtmRepair entity);

    /**
     * Возвращает список DTO с данными из списка сушностей.
     * 
     * @param entityList список сушностей {@link AtmRepair}
     * @return список DTO
     */
    List<AtmRepairDto> toDtoList(List<AtmRepair> entityList);

    /**
     * Обновляет сушность данными из DTO.
     * 
     * @param entity сущность {@link AtmRepair}
     * @param dto DTO {@link AtmRepairDto}
     */
    void updateEntity(@MappingTarget AtmRepair entity, AtmRepairDto dto);

}
