package com.hjkj.pregnancy.service.impl;

import com.hjkj.pregnancy.entity.FetalMovementRecord;
import com.hjkj.pregnancy.model.dto.FetalMovementRequest;
import com.hjkj.pregnancy.repository.FetalMovementRecordRepository;
import com.hjkj.pregnancy.service.FetalMovementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FetalMovementServiceImpl implements FetalMovementService {

    private final FetalMovementRecordRepository repository;

    @Override
    @Transactional
    public FetalMovementRecord saveRecord(String openId, FetalMovementRequest request) {
        FetalMovementRecord record = FetalMovementRecord.builder()
                .openId(openId)
                .startTime(request.getStartTime())
                .durationSeconds(request.getDurationSeconds())
                .count(request.getCount())
                .build();

        return repository.save(record);
    }

    @Override
    public List<FetalMovementRecord> getHistory(String openId) {
        // Just return top 50 for now, or pagination if needed later
        // Currently simple list
        return repository.findByOpenIdOrderByCreatedAtDesc(openId);
    }
}
