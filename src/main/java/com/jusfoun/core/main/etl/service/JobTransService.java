package com.jusfoun.core.main.etl.service;


import java.sql.Timestamp;
import java.util.List;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.jusfoun.core.main.etl.KettleUtil;
import com.jusfoun.core.main.etl.bean.JobTransRela;
import com.jusfoun.core.main.etl.repository.JobTransRelaRepository;

/**
 * Job、Trans同步类
 * 
 * @author 孟山峰
 * @date 20160615
 */
@Service
public class JobTransService {
	
	private static final Logger LOGGER = Logger.getLogger(JobTransService.class);
	
	@Autowired
	JobTransRelaRepository jobTransRelaRepository;
	
	private String path="etl/";
	
	/**
	 *  @Description:删除job，包括文件及关系表数据
	 *  @date:2016年06月15日 
	 *  @author:孟山峰
	 *  @param path
	 *  @param jobName
	 * @return 
	 */
	public boolean deleteJob(String json){
		JSONObject jsonObject = null;
		String subPath = "";
		String jobName = null;
		try {
			jsonObject = new JSONObject(json);
			subPath = jsonObject.getString("subPath");
			if(subPath.equals("null")){
				subPath = "";
			}
			jobName = jsonObject.getString("jobName");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		KettleUtil ku = new KettleUtil();
		List<JobTransRela> listJobTransRela = jobTransRelaRepository.findByPathAndJobName(path+subPath, jobName);
		if(listJobTransRela.size()>0&&ku.dropJob(subPath,jobName)){
			for(JobTransRela jobTransRela:listJobTransRela){
				jobTransRelaRepository.delete(jobTransRela);
			}
		}
 		
		return true;
	}

	/**
	 *  @Description:删除trans，仅文件
	 *  @date:2016年06月15日 
	 *  @author:孟山峰
	 *  @param path
	 *  @param transName
	 * @return 
	 */
	public boolean deleteTrans(String json){
		JSONObject jsonObject = null;
		String subPath = "";
		String transName = null;
		try {
			jsonObject = new JSONObject(json);
			subPath = jsonObject.getString("subPath");
			if(subPath.equals("null")){
				subPath = "";
			}
			transName = jsonObject.getString("transName");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		KettleUtil ku = new KettleUtil();
		boolean flag = ku.droptrans(subPath,transName);

		return flag;
	}

	/**
	 *  @Description:插入job&trans关系表
	 *  @date:2016年11月29日 
	 *  @author:孟山峰
	 *  @param json
	 * @return 
	 */
	public boolean insertJobTransRela(String json){
		String subPath ="";
		String configJson = json;
		try {
			JSONObject jsonObject = new JSONObject(configJson);
			String jobName = jsonObject.getString("job");
			JSONArray transNameArr = jsonObject.getJSONArray("transNameArr");
			
			if(jsonObject.has("subPath")){
				subPath = jsonObject.getString("subPath");
				if(subPath.equals("null")){
					subPath = "";
				}
			}
			for (int i = 0; i < transNameArr.length(); i++) {
				JobTransRela jobTransRela = new JobTransRela();
				jobTransRela.setPath(path+subPath);
				jobTransRela.setJobName(jobName);
				jobTransRela.setTransName(transNameArr.getString(i));
				jobTransRela.setDescription("");
				jobTransRela.setCreate_time(new Timestamp(System.currentTimeMillis()));

				jobTransRelaRepository.save(jobTransRela);
			}
		} catch (JSONException e) {
			LOGGER.error("解析JSON参数发生异常！", e);
			return false;
		}
		
		
		return true;
	}
}
