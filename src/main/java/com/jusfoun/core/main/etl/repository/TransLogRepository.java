package com.jusfoun.core.main.etl.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jusfoun.core.main.etl.bean.TransLog;
public interface TransLogRepository extends JpaRepository<TransLog, Long>{

    TransLog findByChannelId(String channelId);
}
