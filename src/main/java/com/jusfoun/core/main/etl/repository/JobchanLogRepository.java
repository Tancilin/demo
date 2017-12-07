package com.jusfoun.core.main.etl.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.repository.query.Param;

import com.jusfoun.core.main.etl.bean.JobchanLog;
public interface JobchanLogRepository extends JpaRepository<JobchanLog, JobchanLog.PK>{	

    @Procedure(name = "p_bakuplog")
    String pBakuplog(@Param("flag") String flag);
    
    List<JobchanLog> findByIdBatch(Long idBatch);
	List<JobchanLog> findByRootChannelIdAndLoggingObjectType(String rootChannelId, String loggingObjectType);
	List<JobchanLog> findByParentChannelIdAndLoggingObjectType(String parentChannelId, String loggingObjectType);

}
