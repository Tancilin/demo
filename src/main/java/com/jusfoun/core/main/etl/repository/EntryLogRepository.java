package com.jusfoun.core.main.etl.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

import com.jusfoun.core.main.etl.bean.EntryLog;
public interface EntryLogRepository extends JpaRepository<EntryLog,  EntryLog.PK>{
	
    List<EntryLog> findByIdBatch(Long idBatch);

}
