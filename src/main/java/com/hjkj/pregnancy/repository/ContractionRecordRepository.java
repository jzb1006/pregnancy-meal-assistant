package com.hjkj.pregnancy.repository;

import com.hjkj.pregnancy.entity.ContractionRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContractionRecordRepository extends JpaRepository<ContractionRecord, Long> {

    List<ContractionRecord> findByOpenIdOrderByCreatedAtDesc(String openId);

    // Find the latest record to calculate interval
    ContractionRecord findFirstByOpenIdOrderByStartTimeDesc(String openId);
}
