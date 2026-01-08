package com.hjkj.pregnancy.repository;

import com.hjkj.pregnancy.entity.FetalMovementRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface FetalMovementRecordRepository extends JpaRepository<FetalMovementRecord, Long> {

    // Find records by openId, ordered by create time desc
    List<FetalMovementRecord> findByOpenIdOrderByCreatedAtDesc(String openId);

    // Find records by openId and time range
    List<FetalMovementRecord> findByOpenIdAndStartTimeBetweenOrderByStartTimeDesc(
            String openId, LocalDateTime start, LocalDateTime end);
}
