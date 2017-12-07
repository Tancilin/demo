package com.jusfoun.core.main.etl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.logging.LoggingHierarchy;
import org.pentaho.di.core.logging.LoggingObjectInterface;
import org.pentaho.di.core.logging.LoggingRegistry;
import org.pentaho.di.core.parameters.UnknownParamException;
import org.pentaho.di.job.Job;
import org.pentaho.di.job.JobMeta;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.jusfoun.core.main.cache.JavaMapCache;
import com.jusfoun.core.main.cache.SubscribeMapCache;
import com.jusfoun.core.main.etl.bean.KettleJob;
import com.jusfoun.core.main.etl.service.LogService;

/**
 * 运行Job
 * 
 * @author 付永波
 * @date 20160529
 */
@Component
@SpringApplicationConfiguration(classes = KettleStarter.class)
public class RunJob {

	private static final Logger LOGGER = Logger.getLogger(RunJob.class);

	private String path = "etl/";
	
	@Autowired
	LogService logService;
	
	public RunJob() {
		try {
			KettleEnvironment.init();
		} catch (KettleException e) {
			LOGGER.error("初始化失败！！{}", e);
		}
	}

	/**
	 * 执行job
	 * @author mengshanfeng
	 * @date 20160626
	 * @param jobName
	 * @return
	 */
	@Async
	public void runJob(String jobName,String schType,String beginDate) {
		JobMeta jobMeta = null;
		try {
			jobMeta = new JobMeta(path + jobName + ".kjb", null);
		} catch (KettleXMLException e) {
			LOGGER.error("{}", e);
		}

		KettleJob kettleJob=new KettleJob();
		Job job =new Job(null, jobMeta) ;
		kettleJob.setJob(job);
		kettleJob.setStartDate(new Date());
		JavaMapCache.putValue(jobName, kettleJob);
		LOGGER.info("Job["+path+jobName+".kjb] 运行开始。。。");
		
		if(!schType.equals("0")){//若为非全量，需要设置时间参数
			try {
				//job.getJobMeta().setParameterValue("v_date", "to_date('"+beginDate+"','yyyymmdd')");
				job.getJobMeta().setParameterValue("v_date", "'"+beginDate+"'");
			} catch (UnknownParamException e1) {
				e1.printStackTrace();
			}
		}
		job.start();
		job.waitUntilFinished();
		
		long minusTime = new Date().getTime() - kettleJob.getStartDate().getTime();
		try {
			if(minusTime >= 0 && minusTime < 1000*60*10){//10分钟内sleep30秒
				Job.sleep(1000*10);
			}else if(minusTime >= 1000*60*10 && minusTime < 1000*60*20){//10-20分钟sleep60秒
				Job.sleep(1000*60);
			}else if(minusTime >= 1000*60*20 && minusTime < 1000*60*30){//20-30分钟sleep120秒
				Job.sleep(1000*60*2);
			}else{
				Job.sleep(1000*60*3);//30分钟以上sleep180秒
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		LOGGER.info("批次ID["+job.getBatchId()+"],Job["+path+jobName+".kjb]运行结束！");
		/////////下面这段代码，解决JOBCHAN_LOG生成数据不全问题///////////////////////////////////////////////////////////
		List<LoggingHierarchy> hierarchy = new ArrayList<LoggingHierarchy>();
		 LoggingRegistry.getInstance().removeIncludingChildren(job.getLogChannelId());
		List<String> childIds = LoggingRegistry.getInstance().getLogChannelChildren(job.getLogChannelId());
		for (String childId : childIds) {
			LoggingObjectInterface loggingObject = LoggingRegistry.getInstance().getLoggingObject(childId);
			if (loggingObject!=null) {
				hierarchy.add(new LoggingHierarchy(job.getLogChannelId(), job.getBatchId(), loggingObject));
			}
		}
		//////////////////////////////////////////////////////////////////////////////////////////////////
		
		
		LOGGER.info("批次ID["+job.getBatchId()+"],Job["+path+jobName+".kjb] 运行日志获取中。。。");
		String logJson = logService.getAllLog(path,jobName,job.getBatchId());
		JavaMapCache.getValue(jobName).setCurrentBatchId(job.getBatchId());
		JavaMapCache.getValue(jobName).setLogMap(logJson);//把日志保存到缓存中，等待被获取
		JavaMapCache.getValue(jobName).setLogFlag(true);//置为可获取日志状态
		if(logJson.equals("{}")){
			LOGGER.info("批次ID["+job.getBatchId()+"],Job["+path+jobName+".kjb] 运行日志未生成！");		
		}else{
			LOGGER.info("批次ID["+job.getBatchId()+"],Job["+path+jobName+".kjb] 运行日志已获取并缓存完毕！");		
		}
	}

	/**
	 * 执行job
	 * @author 盛泽欢
	 * @date 20160725
	 * @param jobName
	 * @param filepath 订阅：subscribeInfo 落地：lands
	 * @return
	 */
	@Async
	public void runJob(String jobName,String filepath) {
		JobMeta jobMeta = null;
		try {
			jobMeta = new JobMeta(path+filepath+"/" + jobName + ".kjb", null);
		} catch (KettleXMLException e) {
			LOGGER.error("解析"+path+filepath+"/" + jobName + ".kjb时出错", e);
		}
		KettleJob kettleJob=new KettleJob();
		Job job =new Job(null, jobMeta) ;
		kettleJob.setJob(job);
		kettleJob.setStartDate(new Date());
		SubscribeMapCache.putValue(jobName, kettleJob);
		LOGGER.info("Job["+path+filepath+"/"+jobName+".kjb] 任务运行开始。。。");	
		job.start();
		job.waitUntilFinished();
		LOGGER.info("Job["+path+filepath+"/"+jobName+".kjb] 任务运行结束！");
		/////////下面这段代码，解决JOBCHAN_LOG生成数据不全问题///////////////////////////////////////////////////////////
		List<LoggingHierarchy> hierarchy = new ArrayList<LoggingHierarchy>();
		LoggingRegistry.getInstance().removeIncludingChildren(job.getLogChannelId());
		List<String> childIds = LoggingRegistry.getInstance().getLogChannelChildren(job.getLogChannelId());
		for (String childId : childIds) {
			LoggingObjectInterface loggingObject = LoggingRegistry.getInstance().getLoggingObject(childId);
			if (loggingObject!=null) {
				hierarchy.add(new LoggingHierarchy(job.getLogChannelId(), job.getBatchId(), loggingObject));
			}
		}
		LOGGER.info("Job["+path+filepath+"/"+jobName+".kjb] 任务运行日志获取中。。。");
		String logJson = logService.getAllLog(path+filepath+"/",jobName,job.getBatchId());
		SubscribeMapCache.getValue(jobName).setCurrentBatchId(job.getBatchId());
		SubscribeMapCache.getValue(jobName).setLogMap(logJson);//把日志保存到缓存中，等待被获取
		SubscribeMapCache.getValue(jobName).setLogFlag(true);//置为可获取日志状态
		LOGGER.info("Job["+path+filepath+"/"+jobName+".kjb] 任务运行日志已获取并缓存完毕！");
	}
	
	/**
	 * 停止job
	 * 
	 * @param jobName
	 * @return
	 */
	public long stopJob(String jobName) {
		if(JavaMapCache.getValue(jobName)!=null){
			Job job = JavaMapCache.getValue(jobName).getJob();
			if (null != job) {
				LOGGER.info("Job["+path+jobName+".kjb] 停止开始。。。");
				job.stopAll();
			}
				LOGGER.info("Job["+path+jobName+".kjb] 已正常停止！");
			return job.getBatchId();
		}else{
			return 0;
		}
	}

	/**
	 * 设置job文件的路径
	 * 
	 * @param path
	 */
	public void setPath(String path) {
		this.path = path;
	}
	
}