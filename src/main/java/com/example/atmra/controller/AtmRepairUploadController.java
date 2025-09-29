package com.example.atmra.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.example.atmra.service.AtmRepairFileService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Map;

/**
 * Контроллер REST сервисоа загрузки данных из XLS файла в таблицу ремонтов.
 */
@RestController
@RequestMapping("/api/incidents")
@Slf4j
@RequiredArgsConstructor
public class AtmRepairUploadController {

    private final AtmRepairFileService atmRepairFileService;

    /**
     * Загружает данные из XLS файла в таблицу ремонтов.
     * 
     * @param file XLS файл
     * @return результат выполнения
     */
    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(createResponse("Файл не выбран", -1, false));
        }

        String originalFilename = file.getOriginalFilename();
        if (!originalFilename.endsWith(".xls") && !originalFilename.endsWith(".xlsx")) {
            return ResponseEntity.badRequest()
                    .body(createResponse("Поддерживаются только файлы .xls и .xlsx", -1, false));
        }

        try {
            int size = atmRepairFileService.saveData(file);
            return ResponseEntity.ok(createResponse("Файл успешно обработан", size, true));
        } catch (IOException e) {
            log.error("Ошибка при обработке файла", e);
            return ResponseEntity.internalServerError()
                    .body(createResponse("Ошибка при обработке файла: " + e.getMessage(), -1,
                            false));
        }
    }

    /**
     * Создает таблицу с результатом загрузки файла.
     * 
     * @param message сррбщение
     * @param recordsCount количество загруженных записей
     * @param success признак удачной загрузки
     * @return таблицу с результатом загрузки файла
     */
    private Map<String, Object> createResponse(String message, int recordsCount, boolean success) {
        return Map.of(
                "message", message,
                "recordsCount", recordsCount,
                "success", success);
    }

}
