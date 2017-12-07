package com.jusfoun.core.main.etl.web;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.job.Job;
import org.pentaho.di.job.JobMeta;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.jusfoun.core.main.cache.JavaMapCache;
import com.jusfoun.core.main.cache.SubscribeMapCache;
import com.jusfoun.core.main.etl.KettleStarter;
import com.jusfoun.core.main.etl.RunJob;
import com.jusfoun.core.main.etl.bean.KettleJob;
import com.jusfoun.core.main.etl.service.JobTransService;
import com.jusfoun.core.main.etl.service.LogService;

@RestController
@RequestMapping("/kettle")
@SpringApplicationConfiguration(classes = KettleStarter.class)
public class KettleController {

	@Autowired
	private RunJob rj;
	
	@Autowired
	LogService logService;
	
	@Autowired
	JobTransService jobTransService;
	
	
	private static final Logger LOGGER = Logger.getLogger(KettleController.class);
	
	@RequestMapping(value = "/createJobFTP", method = RequestMethod.POST)
	public boolean createJobFTP(@RequestParam("json") String json) {
		LOGGER.info("createJobFTP请求的json："+json);		
		KettleStarter.createJobFTP(json);
		return true;
	}
	
	@RequestMapping(value = "/createJob", method = RequestMethod.POST)
	public boolean createJob(@RequestParam("json") String json) {
		LOGGER.info("createJob请求的json："+json);		
		boolean flag = KettleStarter.createJob(json);
		if(flag==true){
			return jobTransService.insertJobTransRela(json);
		}else{
			return false;
		}
	}

	@RequestMapping(value = "/createTrans", method = RequestMethod.POST)
	public boolean createTrans(@RequestParam("json") String json) {
		LOGGER.info("createTrans请求的json："+json);		
		KettleStarter.createTrans(json);
		return true;
	}

	@RequestMapping(value = "/dropJob", method = RequestMethod.POST)
	public boolean dropJob(@RequestParam("json") String json) {
		LOGGER.info("dropJob请求的json："+json);	
		return jobTransService.deleteJob(json);
	}
	
	@RequestMapping(value = "/dropTrans", method = RequestMethod.POST)
	public boolean dropTrans(@RequestParam("json") String json) {
		LOGGER.info("dropTrans请求的json："+json);	
		return jobTransService.deleteTrans(json);
	}
	
	@RequestMapping(value = "/runJob/{jobName}/{schType}/{beginDate}", method = RequestMethod.POST)
	public boolean runIncrJob(@PathVariable("jobName") String jobName,@PathVariable("schType") String schType,@PathVariable("beginDate") String beginDate) {
		LOGGER.info("runIncrJob请求运行的jobName："+jobName+"；schType:"+schType+"；beginDate:"+beginDate);		
		rj.runJob(jobName,schType,beginDate);
		return true;
	}
	
	@RequestMapping(value = "/runJob/{jobName}/{schType}", method = RequestMethod.POST)
	public boolean runFullJob(@PathVariable("jobName") String jobName,@PathVariable("schType") String schType) {
		LOGGER.info("runFullJob请求运行的jobName："+jobName+"；schType:"+schType);		
		rj.runJob(jobName,schType,null);
		return true;
	}
	
	/**
	 * 运行订阅JOB  szh 20160726
	 * @param jobName
	 * @return
	 */
	@RequestMapping(value = "/runJob", method = RequestMethod.POST)
	public boolean runJob(@RequestParam("jobName") String jobName,@RequestParam("filepath") String filepath) {
		LOGGER.info("runJob请求运行的jobName："+jobName);		
		rj.runJob(jobName,filepath);
		return true;
	}

	@RequestMapping(value = "/stopJob/{jobName}", method = RequestMethod.POST)
	public boolean stopJob(@PathVariable("jobName") String jobName) {
		LOGGER.info("stopJob请求停止的jobName："+jobName);	
		return rj.stopJob(jobName) != 0 ? true : false;
	}
	
	/**
	 *  @Description:扫描job的状态，如果为"无误完成"且"日志已缓存"，则返回状态及日志，否则只返回状态 
	 *  @param jobName
	 *  @return 
	 */
	@RequestMapping(value="/getJobStatus/{jobName}", method = RequestMethod.POST)
	public String getJobStatus(@PathVariable String jobName){
		LOGGER.info("getJobStatus请求状态的jobName："+jobName);
		KettleJob kettleJob=JavaMapCache.getValue(jobName);
		SimpleDateFormat format=new SimpleDateFormat("yy-MM-dd HH:mm:ss");
		if(kettleJob==null) return "Waiting, ";
		Job job=kettleJob.getJob();
		
		if(job.getStatus().equals("Waiting")) return "Waiting, ";
		if(job.getStatus().equals("Running")) return "Running, "+format.format(kettleJob.getStartDate());
		if(job.getStatus().equals("Halting")) return "Halting, "+format.format(kettleJob.getStartDate());
		if(job.getStatus().equals("Finished")&&!kettleJob.isLogFlag()) return "Finished (log not prepared), "+format.format(kettleJob.getStartDate());
		if(job.getStatus().equals("Finished")&&kettleJob.isLogFlag()) return "Finished, "+format.format(kettleJob.getStartDate())+","+(kettleJob.isLogFlag()?kettleJob.getLogMap():"");
		if(job.getStatus().equals("Finished (with errors)")&&!kettleJob.isLogFlag()) return "Finished (with errors)(log not prepared), "+format.format(kettleJob.getStartDate());
		if(job.getStatus().equals("Finished (with errors)")&&kettleJob.isLogFlag()) return "Finished (with errors), "+format.format(kettleJob.getStartDate())+","+(kettleJob.isLogFlag()?kettleJob.getLogMap():"");
		if(job.getStatus().equals("Stopped")) return "Stopped, "+format.format(kettleJob.getStartDate());
		if(job.getStatus().equals("Stopped (with errors)")) return "Stopped (with errors), "+format.format(kettleJob.getStartDate());

		return job.getStatus();
	}
	
	/**
	 *  @Description:扫描job的状态，如果为"无误完成"且"日志已缓存"，则返回状态及日志，否则只返回状态 
	 *  @param jobName   盛泽换（仅用在订阅）
	 *  @return 
	 */
	@RequestMapping(value="/getSubscribeJobStatus/{jobName}", method = RequestMethod.POST)
	public String getSubscribeJobStatus(@PathVariable String jobName){
		LOGGER.info("getJobStatus请求状态的jobName："+jobName);
		KettleJob kettleJob=SubscribeMapCache.getValue(jobName);
		SimpleDateFormat format=new SimpleDateFormat("yy-MM-dd HH:mm:ss");
		if(kettleJob==null) return "Waiting, ";
		Job job=kettleJob.getJob();
		
		if(job.getStatus().equals("Waiting")) return "Waiting, ";
		if(job.getStatus().equals("Running")) return "Running, "+format.format(kettleJob.getStartDate());
		if(job.getStatus().equals("Halting")) return "Halting, "+format.format(kettleJob.getStartDate());
		if(job.getStatus().equals("Finished")&&!kettleJob.isLogFlag()) return "Finished (log not prepared), "+format.format(kettleJob.getStartDate());
		if(job.getStatus().equals("Finished")&&kettleJob.isLogFlag()) return "Finished, "+format.format(kettleJob.getStartDate())+","+(kettleJob.isLogFlag()?kettleJob.getLogMap():"");
		if(job.getStatus().equals("Finished (with errors)")&&!kettleJob.isLogFlag()) return "Finished (with errors)(log not prepared), "+format.format(kettleJob.getStartDate());
		if(job.getStatus().equals("Finished (with errors)")&&kettleJob.isLogFlag()) return "Finished (with errors), "+format.format(kettleJob.getStartDate())+","+(kettleJob.isLogFlag()?kettleJob.getLogMap():"");
		if(job.getStatus().equals("Stopped")) return "Stopped, "+format.format(kettleJob.getStartDate());
		if(job.getStatus().equals("Stopped (with errors)")) return "Stopped (with errors), "+format.format(kettleJob.getStartDate());

		return job.getStatus();
	}
	
	@RequestMapping(value = "/backupLog", method = RequestMethod.POST)
	public String backupLog() {
		LOGGER.info("backupLog请求备份日志...");
		return logService.backupLog();
	}
	
	/**
	 * @Description:创建简单转换 表到表(仅用在订阅)
	 * @date:2016年08月03日
	 * @author:szh
	 * @param json
	 * @return
	 */
	@RequestMapping(value = "/createTransSimple", method = RequestMethod.POST)
	public boolean createTransSimple(@RequestParam("json") String json) {
		LOGGER.info("createTransSimple请求的json："+json);		
		KettleStarter.createTransSimple(json);
		return true;
	}
	
	/**
	 * @Description:获取交换平台服务器状态
	 * @date:2016年10月21日
	 * @author:szh
	 * @param json
	 * @return
	 */
	@RequestMapping(value = "/getStatus", method = RequestMethod.POST)
	public boolean getStatus(@RequestParam("json") String json) {
		LOGGER.info("getStatus请求的json："+json);			
		return true;
	}
	
	//压力测试用 --mengshanfeng20170321
	@RequestMapping(value = "/getMapStatus/{jobName}", method = RequestMethod.POST)
	public String getMapStatus(@PathVariable("jobName") String jobName) {
		LOGGER.info("getMapStatus请求的job："+jobName);	
		
		KettleJob kettleJob=JavaMapCache.getValue(jobName);
		if(kettleJob == null){
			return "此作业无缓存数据！";
		}else{
			return kettleJob.getJob().getStatus()+"-"+jobName+"-"+kettleJob.isLogFlag()+"-"+kettleJob.getLogMap();
		}
		
	}
	
	//压力测试用 --mengshanfeng20170321
	@RequestMapping(value = "/reloadLog/{jobName}/{batchId}", method = RequestMethod.POST)
	public String reloadLog(@PathVariable("jobName") String jobName,@PathVariable("batchId") long batchId) {
		LOGGER.info("reloadLog请求的job："+jobName+"，批次为："+batchId);	
		
		KettleJob kettleJob=JavaMapCache.getValue(jobName);
		if(kettleJob == null){
			
			if(batchId==0){
				return "此作业无缓存数据，请指定加载日志的任务批次号<非0>！";
			}
			
			JobMeta jobMeta = null;
			try {
				jobMeta = new JobMeta("etl/" + jobName + ".kjb", null);
			} catch (KettleXMLException e) {
				LOGGER.error("{}", e);
			}

			kettleJob=new KettleJob();
			Job job =new Job(null, jobMeta) ;
			kettleJob.setJob(job);
			kettleJob.setStartDate(new Date());//////////////////。。。。。。。。
			JavaMapCache.putValue(jobName, kettleJob);
			String logJson = logService.getAllLog("etl/", jobName, batchId);
			JavaMapCache.getValue(jobName).setCurrentBatchId(batchId);
			JavaMapCache.getValue(jobName).setLogMap(logJson);//把日志加载到缓存中
			JavaMapCache.getValue(jobName).setLogFlag(true);//置为可获取日志状态
			
			String str = kettleJob.getJob().getStatus()+"-"+jobName+"-"+kettleJob.isLogFlag()+"-"+kettleJob.getLogMap();
			
			return "此作业缓存日志为首次载入："+str;
		}else{
			String str1 = kettleJob.getJob().getStatus()+"-"+jobName+"-"+kettleJob.isLogFlag()+"-"+kettleJob.getLogMap();
			if(batchId==0){
				batchId = kettleJob.getCurrentBatchId();
			}

			String logJson = logService.getAllLog("etl/", jobName, batchId);
			JavaMapCache.getValue(jobName).setLogMap(logJson);//把日志重新刷新到缓存中
			
			String str2 = kettleJob.getJob().getStatus()+"-"+jobName+"-"+kettleJob.isLogFlag()+"-"+kettleJob.getLogMap();
			return "重新加载前日志状态为："+str1+"*********重新加载后日志状态更新为："+str2;
		}
		
	}
	
	/**
	 * @Description:创建简单转换 表到表并添加批次号
	 * @date:2017年09月06日
	 * @author:szh
	 * @param json
	 * @return
	 */
	@RequestMapping(value = "/createTranOneTouch", method = RequestMethod.POST)
	public boolean createTranOneTouch(@RequestParam("json") String json) {
		LOGGER.info("createTranOneTouch 请求的json："+json);		
		KettleStarter.createTranOneTouch(json);
		return true;
	}
}
