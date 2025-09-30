package com.example.atmra.service;

import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.atmra.dto.AtmRepairGroupDto;
import com.example.atmra.entity.AtmRepair;
import lombok.RequiredArgsConstructor;

/**
 * Сервис работы с таблицей ремонтов.
 */
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "atm-service.type", havingValue = "code")
public class AtmRepairService extends AbstractAtmRepairService {

    @Override
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
                .limit(atmRepairConfiguration.getCountTopMostCommonCauses())
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

    @Override
    @Transactional(readOnly = true)
    public List<Object> findLongestRepairTimes() {
        List<Object> result = atmRepairRepository.findAll()
                .stream()
                .sorted(Comparator.comparingInt(repair -> ((int) ChronoUnit.HOURS
                        .between(repair.getEndTime(), repair.getStartTime()))))
                .limit(atmRepairConfiguration.getCountTopLongestRepairTimes())
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

    @Override
    @Transactional(readOnly = true)
    public Object findCauseFailureRecurred() {
        Map<String, List<AtmRepair>> repairsByReason =
                atmRepairRepository.findAll()
                        .stream()
                        .collect(Collectors.groupingBy(
                                repair -> "АТМ: %s. %s".formatted(repair.getAtmId(),
                                        repair.getReason()),
                                TreeMap::new,
                                Collectors.mapping(Function.identity(),
                                        Collectors.toList())));
        List<Object> ret = new ArrayList<>();
        repairsByReason.forEach((key, list) -> {
            if (list.size() > 1) {
                List<AtmRepair> atm = new ArrayList<>();
                list.sort(Comparator.comparing(AtmRepair::getStartTime));
                var pred = list.get(0);
                int countCauseFailureRecurred =
                        atmRepairConfiguration.getCountCauseFailureRecurred();
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
