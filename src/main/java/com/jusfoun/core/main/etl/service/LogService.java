package com.jusfoun.core.main.etl.service;


import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jusfoun.core.main.etl.bean.EntryLog;
import com.jusfoun.core.main.etl.bean.JobLog;
import com.jusfoun.core.main.etl.bean.JobchanLog;
import com.jusfoun.core.main.etl.bean.TransLog;
import com.jusfoun.core.main.etl.bean.StepLog;
import com.jusfoun.core.main.etl.repository.EntryLogRepository;
import com.jusfoun.core.main.etl.repository.JobLogRepository;
import com.jusfoun.core.main.etl.repository.JobTransRelaRepository;
import com.jusfoun.core.main.etl.repository.JobchanLogRepository;
import com.jusfoun.core.main.etl.repository.TransLogRepository;
import com.jusfoun.core.main.etl.repository.StepLogRepository;
import com.jusfoun.core.main.util.JSONUtils;


/**
 * Log处理类
 * 
 * @author 孟山峰
 * @date 20160603
 */
@Service
public class LogService {
	
	@Autowired
	EntryLogRepository entryLogRepository;
	
	@Autowired
	JobchanLogRepository jobchanLogRepository;
	
	@Autowired
	JobLogRepository jobLogRepository;
	
	@Autowired
	StepLogRepository stepLogRepository;
	
	@Autowired
	TransLogRepository transLogRepository;
	
	@Autowired
	JobTransRelaRepository jobTransRelaRepository;
	
	
	private static final Logger LOGGER = Logger.getLogger(LogService.class);

	/**
	 *  @Description:获取日志
	 *  @date:2016年06月03日 --->20161214逻辑修改通过jobchanLog表做关联 modified by mengshanfeng
	 *  @author:孟山峰
	 *  @param path
	 *  @param jobName
	 *  @param jobBatchId
	 *  @return 
	 */
	public String getAllLog(String path,String jobName,long jobBatchId){
		
		Map<String , Object>  map  = new HashMap<String, Object>();
		//取出job_log日志
		JobLog jobLog = jobLogRepository.findByJobnameAndIdJob(jobName,jobBatchId);
		
		if(null!=jobLog){
			
			//取出entry_log日志
			List<EntryLog> listEntryLog = entryLogRepository.findByIdBatch(jobBatchId);
			
			//取出jobchan_log日志
			List<JobchanLog> listJobchanLog = jobchanLogRepository.findByIdBatch(jobBatchId);
			
			//取出trans_log日志
			List<TransLog> listTransLog  = new ArrayList<TransLog>();
			List<JobchanLog> jobchanLogs = jobchanLogRepository.findByRootChannelIdAndLoggingObjectType(jobLog.getChannelId(), "TRANS");
			for (JobchanLog jobchanLog : jobchanLogs) {
				TransLog transLog = transLogRepository.findByChannelId(jobchanLog.getChannelId());
				if(transLog != null){
					listTransLog.add(transLog);
				}
			}
			
			//取出step_log日志
			List<StepLog> liststepLog = new ArrayList<>();
			for (TransLog transLog : listTransLog) {
				jobchanLogs = jobchanLogRepository.findByParentChannelIdAndLoggingObjectType(transLog.getChannelId(), "STEP");
				for (JobchanLog jobchanLogStep : jobchanLogs) {
					StepLog stepLog = stepLogRepository.findByChannelId(jobchanLogStep.getChannelId());
					if(stepLog != null){
						liststepLog.add(stepLog);
					}
				}		
			}

	 		map.put("jobLog",jobLog);
	 		map.put("entryLog", listEntryLog);
	 		map.put("jobchanLog", listJobchanLog);
	 		map.put("transLog", listTransLog);
	 		map.put("vstepLog",liststepLog);
	 		
	 		try {
				map.put("nodeHostName", InetAddress.getLocalHost().getHostName());
				map.put("nodeAddress", InetAddress.getLocalHost().getHostAddress());
			} catch (UnknownHostException e) {
				e.printStackTrace();
			}
	 		LOGGER.info("批次ID["+jobBatchId+"]+Job["+path+jobName+".kjb]"+"收集的日志json串："+JSONUtils.toString(map));
		}
		return JSONUtils.toString(map);
	}	

	
	/**
	 *  @Description:备份并清理日志
	 *  @date:2016年07月06日 
	 *  @author:孟山峰
	 *  @param
	 *  @return 
	 */
	public String backupLog(){

		String flag = jobchanLogRepository.pBakuplog("1");
		if("0".equals(flag)){
			LOGGER.info("备份完成！");
		}else if("1".equals(flag)){
			LOGGER.info("已经备份过了！");
		}else if("2".equals(flag)){
			LOGGER.info("备份异常！");
		}
		return flag;
	}
	

}
