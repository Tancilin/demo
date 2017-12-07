package com.jusfoun.core.main.etl.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.jusfoun.core.main.etl.bean.StepLog;

public interface StepLogRepository extends JpaRepository<StepLog, Long>{

    StepLog findByChannelId(String channelId);
}
