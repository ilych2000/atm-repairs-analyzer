package com.example.atmra.service;

import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.atmra.dto.AtmRepairDto;
import com.example.atmra.dto.AtmRepairGroupDto;
import com.example.atmra.entity.AtmRepair;
import com.example.atmra.mapper.AtmRepairMapper;
import com.example.atmra.repository.AtmRepairRepository;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

/**
 * Сервис работы с таблицей ремонтов.
 */
@Service
@RequiredArgsConstructor
public class AtmRepairService {

    /** Количество наиболее часто встречающихся причин неисправности */
    @Getter
    @Setter(onMethod_ = {@Value("${atm-repairs-analizer.count-top-most-common-causes}")})
    private int countTopMostCommonCauses;

    /** Количество наиболее долгих ремонта */
    @Getter
    @Setter(onMethod_ = {@Value("${atm-repairs-analizer.count-longest-repair-times}")})
    private int countTopLongestRepairTimes;

    /** Количество дней за которые причина поломки повторилась */
    @Getter
    @Setter(onMethod_ = {@Value("${atm-repairs-analizer.count-cause-failure-recurred}")})
    private int countCauseFailureRecurred;

    private final AtmRepairRepository atmRepairRepository;

    private final AtmRepairMapper atmRepairMapper;

    /**
     * Обновляет запись в таблице ремонтов.
     * 
     * @param dto данные для обновления
     * @return обновленная запись из БД
     */
    @Transactional
    public AtmRepairDto update(AtmRepairDto dto) {
        AtmRepair entity = atmRepairRepository.findById(dto.getCaseId()).orElseThrow();
        atmRepairMapper.updateEntity(entity, dto);
        return atmRepairMapper.toDto(atmRepairRepository.save(entity));
    }

    /**
     * Удаляет все записи в таблице ремонтов.
     * 
     * @return результат выполнения
     */
    @Transactional
    public void deleteAll() {
        atmRepairRepository.deleteAll();
    }

    /**
     * Возвращает все данные из таблицы ремонтов.
     * 
     * @return набор данных
     */
    @Transactional(readOnly = true)
    public List<AtmRepairDto> findAll() {
        return atmRepairMapper.toDtoList(atmRepairRepository.findAll());
    }

    /**
     * Обновляет или создает запись в таблице ремонтов.
     * 
     * @param dtoList данные для обновления
     */
    @Transactional
    public void createOrUpdate(List<AtmRepairDto> dtoList) {
        for (AtmRepairDto atmRepairDto : dtoList) {
            AtmRepair e = atmRepairRepository.findById(atmRepairDto.getCaseId()).orElse(null);
            if (e == null) {
                e = atmRepairMapper.toEntity(atmRepairDto);
                atmRepairRepository.save(e);
            } else {
                atmRepairMapper.updateEntity(e, atmRepairDto);
            }
        }
    }

    /**
     * Возвращает из таблицы ремонтов наиболее часто встречающиеся причины неисправности.
     * 
     * @return данные
     */
    @Transactional(readOnly = true)
    public List<Object> findMostCommonCauses() {
        List<Object> ret = new ArrayList<>();
        Map<String, List<AtmRepair>> repairsByReason =
                atmRepairRepository.findAll()
                        .stream()
                        .collect(Collectors.groupingBy(AtmRepair::getReason,
                                Collectors.mapping(Function.identity(), Collectors.toList())));
        List<String> topReasons = repairsByReason.entrySet()
                .stream()
                .sorted(Comparator.comparingInt(entry -> -entry.getValue().size()))
                .limit(countTopMostCommonCauses)
                .map(Entry::getKey)
                .toList();
        for (String reason : topReasons) {
            ret.add(AtmRepairGroupDto.builder()
                    .groupTitle("%s (Всего: %s)"
                            .formatted(reason, repairsByReason.get(reason).size()))
                    .build());
            ret.addAll(atmRepairMapper.toDtoList(repairsByReason.get(reason)));
        }
        return ret;
    }

    /**
     * Возвращает из таблицы ремонтов наиболее долгие ремонты.
     * 
     * @return данные
     */
    @Transactional(readOnly = true)
    public List<Object> findLongestRepairTimes() {
        List<Object> result = atmRepairRepository.findAll()
                .stream()
                .sorted(Comparator.comparingInt(repair -> ((int) ChronoUnit.HOURS
                        .between(repair.getEndTime(), repair.getStartTime()))))
                .limit(countTopLongestRepairTimes)
                .flatMap(repair -> Stream.of(
                        AtmRepairGroupDto.builder()
                                .groupTitle("%s. Время ремонта %d часов".formatted(
                                        repair.getReason(),
                                        ChronoUnit.HOURS.between(repair.getStartTime(),
                                                repair.getEndTime())))
                                .build(),
                        atmRepairMapper.toDto(repair)))
                .toList();
        return result;
    }

    /**
     * Возвращает из таблицы ремонтов причина поломки которые повторилась в течение определенного
     * количества дней.
     * 
     * @return данные
     */
    @Transactional(readOnly = true)
    public Object findCauseFailureRecurred() {
        Map<String, List<AtmRepair>> repairsByReason =
                atmRepairRepository.findAll()
                        .stream()
                        .collect(Collectors.groupingBy(
                                repair -> "АТМ: %s. %s".formatted(repair.getAtmId(),
                                        repair.getReason()),
                                Collectors.mapping(Function.identity(),
                                        Collectors.toList())));
        List<Object> ret = new ArrayList<>();
        repairsByReason.forEach((key, list) -> {
            if (list.size() > 1) {
                List<AtmRepair> atm = new ArrayList<>();
                list.sort(Comparator.comparing(AtmRepair::getStartTime));
                var pred = list.get(0);
                for (int i = 1; i < list.size(); i++) {
                    var next = list.get(i);
                    if (ChronoUnit.DAYS.between(pred.getStartTime(),
                            next.getStartTime()) <= countCauseFailureRecurred) {
                        if (atm.isEmpty() || atm.getLast() != pred) {
                            atm.add(pred);
                        }
                        atm.add(next);
                        pred = next;
                    }
                }
                if (!atm.isEmpty()) {
                    ret.add(AtmRepairGroupDto.builder()
                            .groupTitle(key)
                            .build());
                    ret.addAll(atmRepairMapper.toDtoList(atm));
                }
            }
        });
        return ret;
    }

}


