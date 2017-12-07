package com.jusfoun.core.main.etl.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jusfoun.core.main.etl.bean.JobLog;
public interface JobLogRepository extends JpaRepository<JobLog, Long>{
	
    JobLog findByJobnameAndIdJob(String jobname,Long idJob);

}
