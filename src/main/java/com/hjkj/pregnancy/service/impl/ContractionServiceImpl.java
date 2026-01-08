package com.hjkj.pregnancy.service.impl;

import com.hjkj.pregnancy.entity.ContractionRecord;
import com.hjkj.pregnancy.model.dto.ContractionRequest;
import com.hjkj.pregnancy.repository.ContractionRecordRepository;
import com.hjkj.pregnancy.service.ContractionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContractionServiceImpl implements ContractionService {

    private final ContractionRecordRepository repository;

    @Override
    @Transactional
    public ContractionRecord saveRecord(String openId, ContractionRequest request) {
        // 1. Find last record to calculate interval
        ContractionRecord lastRecord = repository.findFirstByOpenIdOrderByStartTimeDesc(openId);

        Integer intervalSeconds = null;
        if (lastRecord != null) {
            // Interval is from Start of last to Start of current
            long seconds = Duration.between(lastRecord.getStartTime(), request.getStartTime()).getSeconds();
            intervalSeconds = (int) seconds;
        }

        ContractionRecord record = ContractionRecord.builder()
                .openId(openId)
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .durationSeconds(request.getDurationSeconds())
                .intervalSeconds(intervalSeconds)
                .painLevel(request.getPainLevel())
                .build();

        return repository.save(record);
    }

    @Override
    public List<ContractionRecord> getHistory(String openId) {
        return repository.findByOpenIdOrderByCreatedAtDesc(openId);
    }
}
