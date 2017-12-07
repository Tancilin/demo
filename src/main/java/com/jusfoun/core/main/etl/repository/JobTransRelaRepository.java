package com.jusfoun.core.main.etl.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.jusfoun.core.main.etl.bean.JobTransRela;

public interface JobTransRelaRepository extends JpaRepository<JobTransRela, Integer>{
	 List<JobTransRela> findByPathAndJobName(String path,String jobName);
}
