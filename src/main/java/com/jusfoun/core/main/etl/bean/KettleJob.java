package com.jusfoun.core.main.etl.bean;

import java.util.Date;

import org.pentaho.di.job.Job;
/**
 * 
 * @author 杨光跃
 */
public class KettleJob {
	private String logMap;
	private Job job;
	long currentBatchId;
	private boolean logFlag;
	private Date startDate;
	
	
	

	public long getCurrentBatchId() {
		return currentBatchId;
	}
	public void setCurrentBatchId(long currentBatchId) {
		this.currentBatchId = currentBatchId;
	}
	public String getLogMap() {
		return logMap;
	}
	public void setLogMap(String logMap) {
		this.logMap = logMap;
	}
	public Job getJob() {
		return job;
	}
	public void setJob(Job job) {
		this.job = job;
	}
	public boolean isLogFlag() {
		return logFlag;
	}
	public void setLogFlag(boolean logFlag) {
		this.logFlag = logFlag;
	}
	public Date getStartDate() {
		return startDate;
	}
	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}
}
