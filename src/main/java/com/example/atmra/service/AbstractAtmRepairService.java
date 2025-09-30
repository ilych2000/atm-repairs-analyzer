package com.example.atmra.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.atmra.dto.AtmRepairDto;
import com.example.atmra.entity.AtmRepair;
import com.example.atmra.mapper.AtmRepairMapper;
import com.example.atmra.repository.AtmRepairRepository;

/**
 * Сервис работы с таблицей ремонтов.
 */
@Service
abstract class AbstractAtmRepairService implements IAtmRepairService {

    @Autowired
    protected AtmRepairRepository atmRepairRepository;

    @Autowired
    protected AtmRepairMapper atmRepairMapper;

    @Autowired
    protected AtmRepairConfiguration atmRepairConfiguration;

    @Override
    @Transactional
    public AtmRepairDto update(AtmRepairDto dto) {
        AtmRepair entity = atmRepairRepository.findById(dto.getCaseId()).orElseThrow();
        atmRepairMapper.updateEntity(entity, dto);
        return atmRepairMapper.toDto(atmRepairRepository.save(entity));
    }

    @Override
    @Transactional
    public void deleteAll() {
        atmRepairRepository.deleteAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AtmRepairDto> findAll() {
        return atmRepairMapper.toDtoList(atmRepairRepository.findAll());
    }

    @Override
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

}
