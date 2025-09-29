package com.example.atmra.service;

import lombok.RequiredArgsConstructor;

import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.atmra.dto.AtmRepairDto;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Сервис загрузки данных из XLS файла в таблицу ремонтов.
 */
@Service
@RequiredArgsConstructor
public class AtmRepairFileService {

    private final AtmRepairService service;

    /**
     * Сохраняет данные о ремонтах из XLS файла и возвращает количество загруженных записей.
     * 
     * @param file XLS файл
     * @return количество загруженных записей
     * @throws IOException в случае ошибки чтения из файла
     */
    public int saveData(MultipartFile file) throws IOException {
        List<AtmRepairDto> data = new ArrayList<>();

        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            for (Row row : sheet) {
                System.out.println(row.getRowNum() + " > " + getCellValue(row.getCell(0)));

                if (row.getRowNum() == 0)
                    continue;
                data.add(AtmRepairDto.builder()
                        .caseId((Long) getCellValue(row.getCell(0)))
                        .atmId(String.valueOf(getCellValue(row.getCell(1))))
                        .reason((String) getCellValue(row.getCell(2)))
                        .startTime((LocalDateTime) getCellValue(row.getCell(3)))
                        .endTime((LocalDateTime) getCellValue(row.getCell(4)))
                        .serialNumber(String.valueOf(getCellValue(row.getCell(5))))
                        .bankName((String) getCellValue(row.getCell(6)))
                        .channel((String) getCellValue(row.getCell(7)))
                        .build());
            }
        }
        service.createOrUpdate(data);
        return data.size();
    }

    /**
     * Возвращает значение ячейки.
     * 
     * @param cell ячейка
     * @return значение ячейки
     */
    private Object getCellValue(Cell cell) {
        switch (cell.getCellType()) {
        case STRING:
            return cell.getStringCellValue();
        case NUMERIC:
            if (DateUtil.isCellDateFormatted(cell)) {
                return cell.getLocalDateTimeCellValue();
            } else {
                return Double.valueOf(cell.getNumericCellValue()).longValue();
            }
        default:
            return null;
        }
    }

}
