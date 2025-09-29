package com.example.atmra.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.atmra.dto.AtmRepairDto;
import com.example.atmra.service.AtmRepairService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Контроллер REST сервисов работы с таблицей ремонтов.
 */
@RestController
@RequestMapping("/api/incidents")
@RequiredArgsConstructor
public class AtmRepairController {

    private final AtmRepairService atmRepairService;

    /**
     * Обновляет запись в таблице ремонтов.
     * 
     * @param dto данные для обновления
     * @return обновленная запись из БД
     */
    @PostMapping("/update")
    public AtmRepairDto update(@Valid @RequestBody AtmRepairDto dto) {
        return atmRepairService.update(dto);
    }

    /**
     * Удаляет все записи в таблице ремонтов.
     * 
     * @return результат выполнения
     */
    @GetMapping(path = "/deleteAll")
    public ResponseEntity<?> deleteAll() {
        atmRepairService.deleteAll();
        return ResponseEntity.ok().build();
    }

    /**
     * Возвращает данные из таблицы ремонтов в соотвествии с типом данных.
     * 
     * @param type тип данных <br>
     *            {@code allData} - все данные <br>
     *            {@code mostCommonCauses} - наиболее часто встречающиеся причины неисправности <br>
     *            {@code longestRepairTimes} - наиболее долгих ремонта <br>
     *            {@code causeFailureRecurred} - причина поломки повторилась в течение 15 дней
     * @return набор данных
     */
    @GetMapping("/data/{type}")
    public ResponseEntity<?> getData(@PathVariable String type) {
        return switch (type) {
        case "allData" -> ResponseEntity.ok(atmRepairService.findAll());
        case "mostCommonCauses" -> ResponseEntity.ok(atmRepairService.findMostCommonCauses());
        case "longestRepairTimes" -> ResponseEntity.ok(atmRepairService.findLongestRepairTimes());
        case "causeFailureRecurred" -> ResponseEntity
                .ok(atmRepairService.findCauseFailureRecurred());
        default -> ResponseEntity.badRequest().body(Map.of("message", "Не известный тип: " + type));
        };
    }

}
