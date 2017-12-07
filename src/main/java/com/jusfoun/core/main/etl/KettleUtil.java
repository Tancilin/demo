package com.jusfoun.core.main.etl;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettlePluginException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.logging.ChannelLogTable;
import org.pentaho.di.core.logging.JobEntryLogTable;
import org.pentaho.di.core.logging.JobLogTable;
import org.pentaho.di.core.logging.StepLogTable;
import org.pentaho.di.core.logging.TransLogTable;
import org.pentaho.di.core.parameters.DuplicateParamException;
import org.pentaho.di.core.plugins.JobEntryPluginType;
import org.pentaho.di.core.plugins.PluginInterface;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.variables.Variables;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.job.JobHopMeta;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entries.ftpput.JobEntryFTPPUT;
import org.pentaho.di.job.entries.special.JobEntrySpecial;
import org.pentaho.di.job.entries.trans.JobEntryTrans;
import org.pentaho.di.job.entry.JobEntryBase;
import org.pentaho.di.job.entry.JobEntryCopy;
import org.pentaho.di.job.entry.JobEntryInterface;
import org.pentaho.di.trans.TransHopMeta;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.errorhandling.StreamInterface;
import org.pentaho.di.trans.steps.calculator.CalculatorMetaFunction;
import com.jusfoun.core.main.etl.bean.DataColumnDefine;
import com.jusfoun.core.main.etl.bean.DataResource;
import com.jusfoun.core.main.etl.bean.DcConcatFields;
import com.jusfoun.core.main.etl.bean.DcExecSQL;
import com.jusfoun.core.main.etl.bean.DcFilterRows;
import com.jusfoun.core.main.etl.bean.DcReplaceString;
import com.jusfoun.core.main.etl.bean.DcStringCut;
import com.jusfoun.core.main.etl.bean.DcStringOperations;
import com.jusfoun.core.main.etl.bean.DcTableInput;
import com.jusfoun.core.main.etl.bean.DcTableOutput;
import com.jusfoun.core.main.etl.bean.DcTransformation;
import com.jusfoun.core.main.etl.bean.DcUniqueRows;

/**
 * Trans和job文件处理类
 * 
 * @author xuxiangfu
 * @date 20160629
 */
public class KettleUtil {
	
	
	private static final Logger LOGGER = Logger.getLogger(KettleUtil.class);

	private boolean flagJob = false;

	private boolean flagTrans = false;

	private PluginRegistry registry;

	private String[] databasesXML;

	private List<DataResource> resource;

	private String transName;

	private String jobName;

	private String path;
	
	private static final String XMLFORMAT = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><connection>"
			+ "<name>{0}</name><server>{1}</server><type>{2}</type><access>Native</access>"
			+ "<database>{3}</database><port>{4}</port><username>{5}</username>"
			+ "<password>{6}</password></connection>";

	/**
	 * kettle环境初始化
	 */
	public KettleUtil() {
		try {
			KettleEnvironment.init();
		} catch (KettleException e) {
			LOGGER.error("初始化失败！{}", e);
		}
		registry = PluginRegistry.getInstance();
		resource = new ArrayList<DataResource>();
		this.path = "etl/";
	}

	/**
	 * 设置生成文件的路径
	 * 
	 * @param path
	 */
	public void setPath(String path) {
		this.path = path;
	}

	/**
	 * 设置原数据库链接
	 * 
	 * @param originalDB
	 */
	public void setOriginalData(DataResource originalDB) {
		resource.add(0, originalDB);
	}

	/**
	 * 设置存入数据库的链接
	 * 
	 * @param targetDB
	 */
	public void setTargetData(DataResource targetDB) {
		resource.add(1, targetDB);
	}

	/**
	 * 设置存入log数据库的基本信息
	 * 
	 * @param logDB
	 */
	public void setTransLogDB(DataResource logDB) {
		resource.add(2, logDB);
	}
	/**
	 * 设置存入log数据库的基本信息
	 * 
	 * @param logDB
	 */
	public void setJobLogDB(DataResource logDB) {
		resource.add(0, logDB);
	}

	/**
	 * 设置存入问题库的基本信息
	 * 
	 * @param filterDB
	 */
	public void setTransFilterDB(DataResource filterDB) {
		resource.add(3, filterDB);
	}
	
	/**
	 * 设置存入落地库的基本信息
	 * 
	 * @param landDB
	 */
	public void setTransLandDB(DataResource landDB) {
		resource.add(4, landDB);
	}
	/**
	 * 创建trans文件
	 * 
	 * @param sourceTable
	 * @param targetTable
	 * @param sourceSql
	 * @param transName
	 * @param incr
	 *            增量：YES，其他为全量
	 * @param delSql
	 *            增量，此参数需要赋值，全量可以传 null
	 */
	 public void createTrans(DcTransformation dcTransformation, Map<String, Object> map) {
		 this.transName = dcTransformation.getTransName();
		// 将数据源基本信息组合成数组
		getDatabasesXml();
		// 设置转化的参数
		TransMeta transMeta = getTransMeta(this.transName);
		// 定义日志
		setTransLogData(transMeta);

		KettleCompUtil kettleCompUtil = new KettleCompUtil();
		List<String> stepMetaList = new ArrayList<String>();
		Map<String,StepMeta> stepMetaMap = new HashMap<String, StepMeta>();
		// 增量/全量
		String incr = null;
		// 源数据库名
		String sourceResourceName = resource.get(0).getResourceName();
		// 目标数据库名
		String targetResourceName = resource.get(1).getResourceName();
		// 问题数据库名
		String filterResourceName = resource.get(3).getResourceName();
		// 落地数据库名
		String landResourceName = resource.get(4).getResourceName();
		// 源表名
		String sourceTableName = null;
		// 目标表名
		String targetTableName = null;
		// 过滤组件的分发流
		List<StreamInterface> targetStreams = null;
		// 计算器组件新增的计算列
		Collection<String> calculatorColumnsTmp = new ArrayList<>();
		// 是否落地
		String landOrNot= null;
		
		//删除目标表
		if(map.containsKey("dcExecSQL")){
			DcExecSQL dcExecSQL = (DcExecSQL) map.get("dcExecSQL");
			String delSql = dcExecSQL.getDeleteSql();
			stepMetaList.add("dcExecSQL");
			stepMetaMap.put("dcExecSQL",kettleCompUtil.preDelSql(transMeta, targetResourceName, "presql", delSql));
		}

		//表输入
		if(map.containsKey("dcTableInput")){
			DcTableInput dcTableInput = (DcTableInput) map.get("dcTableInput");
			String sourceSql = dcTableInput.getTableinputSql();
			sourceTableName = dcTableInput.getTableName();
			incr = dcTableInput.getIncrementType();
			landOrNot = dcTableInput.getLandOrNot();
			stepMetaList.add("dcTableInput");
			stepMetaMap.put("dcTableInput",kettleCompUtil.oldTableInput(transMeta, sourceResourceName, sourceTableName, sourceSql, incr));
		}

		//过滤组件
		if(map.containsKey("dcFilterRows")&&map.containsKey("dcFilterRowsList")){
			DcFilterRows rootDcFilterRows = (DcFilterRows) map.get("dcFilterRows");	
			@SuppressWarnings("unchecked")
			List<DcFilterRows> dcFilterRowsList = (List<DcFilterRows>) map.get("dcFilterRowsList");	
			stepMetaList.add("dcFilterRows");
			StepMeta filterRowsStepMeta = kettleCompUtil.addFilterRows(sourceTableName,dcFilterRowsList,rootDcFilterRows);
			//取出流，为过滤组件的分发规则做准备
			targetStreams = filterRowsStepMeta.getStepMetaInterface().getStepIOMeta().getTargetStreams();
			
			stepMetaMap.put("dcFilterRows",filterRowsStepMeta);
		}		
		
		//去除重复记录
		if(map.containsKey("dcUniqueRows")){
			DcUniqueRows dcUniqueRows = (DcUniqueRows) map.get("dcUniqueRows");
			stepMetaList.add("dcSortRows");
			stepMetaMap.put("dcSortRows",kettleCompUtil.addSortRows(sourceTableName, dcUniqueRows));
			stepMetaList.add("dcUniqueRows");
			stepMetaMap.put("dcUniqueRows",kettleCompUtil.addUniqueRows(sourceTableName, dcUniqueRows));
		}
		
		//字符串替换
		if(map.containsKey("dcReplaceStringList")){
			@SuppressWarnings("unchecked")
			List<DcReplaceString> dcReplaceStringList = (List<DcReplaceString>) map.get("dcReplaceStringList");
			stepMetaList.add("dcReplaceStringList");
			stepMetaMap.put("dcReplaceStringList",kettleCompUtil.addStringReplace(sourceTableName, dcReplaceStringList));
		}
		
		//字符串剪切
		if(map.containsKey("dcStringCut")){
			DcStringCut dcStringCut = (DcStringCut) map.get("dcStringCut");
			dcStringCut.setOperateColumns("table_name,table_desc");
			dcStringCut.setCutFroms("3,3");
			dcStringCut.setCutTos(",5");
			stepMetaList.add("dcStringCut");
			stepMetaMap.put("dcStringCut",kettleCompUtil.addStringCut(sourceTableName, dcStringCut));
		}
		
		//字符串操作（去空格、大小写转换等）
		if(map.containsKey("dcStringOperations")){
			DcStringOperations dcStringOperations = (DcStringOperations) map.get("dcStringOperations");
			stepMetaList.add("dcStringOperations");
			stepMetaMap.put("dcStringOperations",kettleCompUtil.addStringOperations(sourceTableName, dcStringOperations));
		}
		
		//字符串拼接
		if(map.containsKey("dcConcatFields")){
			DcConcatFields dcConcatFields = (DcConcatFields) map.get("dcConcatFields");
			stepMetaList.add("dcConcatFields");
			stepMetaMap.put("dcConcatFields",kettleCompUtil.getConcatFieldsMeta(sourceTableName, dcConcatFields));
		}
		
		//计算器
		if(map.containsKey("functionList")){
			@SuppressWarnings("unchecked")
			List<CalculatorMetaFunction> functionList = (List<CalculatorMetaFunction>) map.get("functionList");
			
			//为问题数据归档做准备，取出计算器中新增的字段列
			//算法为：源选取出来的字段列-计算器中新添加的自段列=问题数据库的归档列
			for(CalculatorMetaFunction function:functionList){
				calculatorColumnsTmp.add(function.getFieldName());
			}
			
			stepMetaList.add("functionList");
			stepMetaMap.put("functionList",kettleCompUtil.calculatorMeta(sourceTableName,functionList));
		}
		
		//空操作
		stepMetaList.add("dummy");
		stepMetaMap.put("dummy",kettleCompUtil.dummyStep());
		
		//表输出步骤
		if(map.containsKey("dcTableOutput")){
			DcTableOutput dcTableOutput = (DcTableOutput) map.get("dcTableOutput");
			Collection<DataColumnDefine> sourceColumns = dcTableOutput.getSourceColumndefines();
			Collection<DataColumnDefine> targetColumns = dcTableOutput.getTargetColumndefines();
			targetTableName = dcTableOutput.getTableName();
			stepMetaList.add("dcTableOutput");
			stepMetaMap.put("dcTableOutput",kettleCompUtil.newTableOutput(transMeta, targetResourceName, sourceColumns, targetColumns, targetTableName,incr));
		}

		int stepSize = stepMetaList.size();
		
		/**
		 *  给最后一个步骤添加在spoon工具中的显示位置
		 */
		stepMetaMap.get(stepMetaList.get(stepSize - 1)).setDraw(true);
		stepMetaMap.get(stepMetaList.get(stepSize - 1)).setLocation(stepSize * 200, 100 + (stepSize - 1) * 50);
		transMeta.addOrReplaceStep(stepMetaMap.get(stepMetaList.get(stepSize - 1)));
		
		/**
		 *  把相邻的两个步骤“连线”
		 */
		for(int idx = 0; idx < stepSize-1; idx++){
			// 给步骤添加在spoon工具中的显示位置
			stepMetaMap.get(stepMetaList.get(idx)).setDraw(true);
			stepMetaMap.get(stepMetaList.get(idx)).setLocation((idx + 1) * 200, 100 + idx * 50);
			transMeta.addOrReplaceStep(stepMetaMap.get(stepMetaList.get(idx)));
			
			//连线
			transMeta.addTransHop(new TransHopMeta(stepMetaMap.get(stepMetaList.get(idx)), stepMetaMap.get(stepMetaList.get(idx+1))));
		}
		
/**落地库逻辑开始*******************************************************************************************************/
		//落地表输出步骤
		if("0".equals(landOrNot)){
			
			//落地库表输出组件
			DcTableOutput dcTableOutput = (DcTableOutput) map.get("dcTableOutput");
			Collection<DataColumnDefine> sourceColumns = dcTableOutput.getSourceColumndefines();
			Collection<DataColumnDefine> targetColumns = dcTableOutput.getTargetColumndefines();
			targetTableName = dcTableOutput.getTableName();
			StepMeta landOutputStepMeta = kettleCompUtil.newTableOutput(transMeta, landResourceName, sourceColumns, targetColumns, targetTableName+"_land",incr);
			landOutputStepMeta.setDraw(true);
			landOutputStepMeta.setLocation(stepSize * 250, 30);
			transMeta.addOrReplaceStep(landOutputStepMeta);
			StepMeta lastStepMeta = stepMetaMap.get(stepMetaList.get(stepSize-2));
			//设为复制分发模式
			lastStepMeta.setDistributes(false);
			transMeta.addTransHop(new TransHopMeta(lastStepMeta, landOutputStepMeta));
			
			//若为增量，添加落地库删除组件，放在第一个组件位置
			if(map.containsKey("dcExecSQL")){
				DcExecSQL dcExecSQL = (DcExecSQL) map.get("dcExecSQL");
				String delSql = dcExecSQL.getDeleteSql();
				delSql=delSql.replace(targetTableName, targetTableName+"_land");
				StepMeta preDelSqlStepMeta = kettleCompUtil.preDelSql(transMeta, landResourceName, "landpresql", delSql);
				preDelSqlStepMeta.setDraw(true);
				preDelSqlStepMeta.setLocation(30, 50);
				transMeta.addOrReplaceStep(preDelSqlStepMeta);
				StepMeta firstStepMeta = stepMetaMap.get(stepMetaList.get(0));
				transMeta.addTransHop(new TransHopMeta(preDelSqlStepMeta, firstStepMeta));
			}
		
		}	
/**问落地库逻辑结束*******************************************************************************************************/	

/**问题库逻辑开始*******************************************************************************************************/
		if(map.containsKey("dcFilterRows")){
			//目标表输出列
			DcTableOutput dcTableOutput = (DcTableOutput) map.get("dcTableOutput");
			Collection<DataColumnDefine> sourceColumns = dcTableOutput.getSourceColumndefines();
			DataColumnDefine sourceIdBatch= new DataColumnDefine();
			sourceIdBatch.setColumnName("IdBatch");
			DataColumnDefine targetIdBatch= new DataColumnDefine();
			targetIdBatch.setColumnName("IdBatch");
			Collection<DataColumnDefine> targetColumns = dcTableOutput.getTargetColumndefines();
			sourceColumns.add(sourceIdBatch);
			targetColumns.add(targetIdBatch);
			
			//把计算器中添加的列剔除掉【备注：这个方式是临时的，正确做法是需要单独获取字段对应关系！特此提示！】
			Iterator<String> itc = calculatorColumnsTmp.iterator();
			Iterator<DataColumnDefine> its = sourceColumns.iterator();
			Iterator<DataColumnDefine> itt = targetColumns.iterator();
			while(itc.hasNext()){
				String calculatorColumnName = itc.next();
				while(its.hasNext()&&itt.hasNext()){
					DataColumnDefine cols = its.next();
					itt.next();
					if(cols.getColumnName().equals(calculatorColumnName)){
						its.remove();
						itt.remove();
					}
				}
			}
						
			//问题库表名称
			String filterTargetTableName = dcTableOutput.getTableName()+"_filter";
			//生成问题库表输出组件
	        StepMeta filterOutputStepMeta = kettleCompUtil.newFilterOutput(transMeta, filterResourceName, sourceColumns, targetColumns, filterTargetTableName);
	        filterOutputStepMeta.setDraw(true);
	        filterOutputStepMeta.setLocation(stepSize * 260, 20);
			transMeta.addOrReplaceStep(filterOutputStepMeta);
			//设置过滤组件的分发规则
			// 获取系统批次ID
 			StepMeta systemDataMeta = kettleCompUtil.addGetSystemData();
 			systemDataMeta.setDraw(true);
 			systemDataMeta.setLocation(stepSize * 200, 20);
 			stepMetaMap.put("systemData",systemDataMeta);
 			transMeta.addOrReplaceStep(systemDataMeta);
			int filterIdx = stepMetaList.indexOf("dcFilterRows");
			targetStreams.get(0).setStepMeta(stepMetaMap.get(stepMetaList.get(filterIdx+1)));
	        targetStreams.get(1).setStepMeta(systemDataMeta);
	      //把过滤组件和获取系统批次ID组件连线	     
	        transMeta.addTransHop(new TransHopMeta(stepMetaMap.get("dcFilterRows"), systemDataMeta));
	      //把过获取系统批次ID组件和问题库表输出组件连线
	        transMeta.addTransHop(new TransHopMeta(systemDataMeta, filterOutputStepMeta));
		}
		
/**问题库逻辑结束*******************************************************************************************************/

		// 输出到xml文件中
		outPutXml("",transMeta);
		
		if (flagTrans) {
			LOGGER.info("TRANS文件"+"["+path+this.transName+"]"+"创建成功!");
		} else {
			LOGGER.info("TRANS文件"+"["+path+this.transName+"]"+"创建失败!");
		}
	}

	private void setTransLogData(TransMeta transMeta) {
		// 定义变量
		VariableSpace space = new Variables();
		// 将日志数据库配置名加入到变量集中
		space.setVariable("transloging", resource.get(2).getResourceName());
		space.initializeVariablesFrom(null);
		// 定义Step日志
		// 声明StepLogTable。space:系统变量;tanMetade:提供数据库连接
		StepLogTable stepLogTable = StepLogTable.getDefault(space, transMeta);
		// StepLogTable使用的数据库连接名（上面配置的变量名）。
		stepLogTable.setConnectionName(resource.get(2).getResourceName());
		// 设置Step日志的表名
		stepLogTable.setTableName("step_log");
		// 设置TransMeta的StepLogTable
		transMeta.setStepLogTable(stepLogTable);
		// 定义Trans日志
		// 声明TransLogTable。space:系统变量;tanMetade:提供数据库连接
		TransLogTable transLogTable = TransLogTable.getDefault(space, transMeta, transMeta.getSteps());
		// TransLogTable使用的数据库连接名（上面配置的变量名）。
		transLogTable.setConnectionName(resource.get(2).getResourceName());
		// 设置Trans日志的表名
		transLogTable.setTableName("trans_log");
		// 设置TransMeta的TransLogTable
		transMeta.setTransLogTable(transLogTable);
		/*
		 * try { LOGGER.info(transMeta.getXML()); } catch (KettleException e) {
		 * LOGGER.error("{}", e); }
		 */
	}

	/**
	 * @Description:创建FTP类型Job
	 * @date:2016年07月18日--->20161213修改
	 * @author:孟山峰
	 * @param entry
	 * @return
	 */
	public boolean createJobFTP(String subPath,String jobName,JobEntryFTPPUT jobEntryFTPPUT, String ftpRootPath,String incr) {
		this.jobName = jobName;
		// 将数据源基本信息组合成数组
		getDatabasesXml();
		// 设置转化的参数
		JobMeta jobMeta = getJobMeta(jobName, incr);

		// 定义日志
		setJobLog(jobMeta);

		PluginInterface jobPlugin = registry.findPluginWithId(JobEntryPluginType.class, "SPECIAL");

		JobEntryInterface jeiStart = null;
		try {
			jeiStart = (JobEntryInterface) registry.loadClass(jobPlugin);
		} catch (KettlePluginException e1) {
			LOGGER.error("{}", e1);
			return flagJob;
		}
		JobEntryCopy jgeStart = getJobEntryCopy(jobMeta, jobPlugin, jeiStart);

		JobEntryCopy jge_uftp = getJobEntryCopyFTPPUT(jobEntryFTPPUT);
		jobMeta.addJobEntry(jge_uftp);
		jobMeta.addJobHop(new JobHopMeta(jgeStart, jge_uftp));
		//创建上传文件保存目录
		boolean flag = createDir(ftpRootPath+"/receive/"+jobName);
		if(!flag){
			return false;
		}
		jobFileOutPut(subPath,jobName, jobMeta);
		if (flagJob) {
			LOGGER.info("JOB文件"+"["+path+subPath+jobName+"]"+"创建成功！");
		} else {
			LOGGER.info("JOB文件"+"["+path+subPath+jobName+"]"+"创建失败！");
		}
		return flagJob;
	}	

	
	/**
	 * @Description:FTP 上传组件
	 * @date:2016年07月18日
	 * @author:孟山峰
	 * @param entry
	 * @return
	 */
	private JobEntryCopy getJobEntryCopyFTPPUT(JobEntryFTPPUT entry) {
		PluginInterface jobPlugin = null;
		
		//modify by zhangcm 20170112  原插件名称直接写定中文字符，现修改 为调用kettle国际化配置属性
		String pluginName = BaseMessages.getString( JobEntryBase.class, "JobEntry.FTPPUT.TypeDesc" );
		jobPlugin = PluginRegistry.getInstance().findPluginWithName(JobEntryPluginType.class, pluginName);		

		JobEntryInterface jei_uftp = null;
		try {
			jei_uftp = (JobEntryInterface) registry.loadClass(jobPlugin);
		} catch (KettlePluginException e1) {
			e1.printStackTrace();
		}
		jei_uftp.setPluginId(jobPlugin.getIds()[0]);
		jei_uftp.setName("upload");
		
		JobEntryCopy jge_uftp = new JobEntryCopy();
		jge_uftp.setEntry(jei_uftp);
		jge_uftp.setLocation(600,200);
		jge_uftp.setNr(0);
		jge_uftp.setDrawn(true);
		

		JobEntryFTPPUT uftp  = (JobEntryFTPPUT)jge_uftp.getEntry();
		uftp.setRemoteDirectory(entry.getRemoteDirectory());
		uftp.setPassword(entry.getPassword());
		uftp.setServerPort(entry.getServerPort());
		uftp.setLocalDirectory(entry.getLocalDirectory());//("E:\\workspace4\\kettleAPI\\etl\\tmpfile");
		uftp.setUserName(entry.getUserName());
		uftp.setServerName(entry.getServerName());	
		uftp.setOnlyPuttingNewFiles(true);
		//modify by zhangcm 2016-12-22 添加正则表达式
		uftp.setWildcard(entry.getWildcard());
		//modify by zhangcm 2016-12-28 添加二进制模式
		uftp.setBinaryMode(entry.isBinaryMode());
		//modify by zhangcm 2016-12-28 添加控制编码
		uftp.setControlEncoding(entry.getControlEncoding());
		return jge_uftp;	
	}	
	
	
	private void setJobLog(JobMeta jobMeta) {
		// 定义变量
		VariableSpace space = new Variables();
		// 将日志数据库配置名加入到变量集中
		space.setVariable("jobloging", resource.get(0).getResourceName());
		space.initializeVariablesFrom(null);
		// 定义job日志
		// 声明JobLogTable。space:系统变量;jobMeta:提供数据库连接
		JobLogTable jobLogTable = JobLogTable.getDefault(space, jobMeta);
		// JobLogTable使用的数据库连接名（上面配置的变量名）。
		jobLogTable.setConnectionName(resource.get(0).getResourceName());
		// 设置job日志的表名
		jobLogTable.setTableName("job_log");
		// 设置jobMeta的JobLogTable
		jobMeta.setJobLogTable(jobLogTable);
		// 定义JobEntry日志
		// 声明JobEntryLogTable。space:系统变量;jobMeta:提供数据库连接
		JobEntryLogTable jobEntryLogTable = JobEntryLogTable.getDefault(space, jobMeta);
		// JobEntryLogTable使用的数据库连接名（上面配置的变量名）。
		jobEntryLogTable.setConnectionName(resource.get(0).getResourceName());
		// 设置JobEntry日志的表名
		jobEntryLogTable.setTableName("entry_log");
		// 设置jobMeta的JobEntryLogTable
		jobMeta.setJobEntryLogTable(jobEntryLogTable);
		// 声明ChannelLogTable。space:系统变量;jobMeta:提供数据库连接
		ChannelLogTable channelLogTable = ChannelLogTable.getDefault(space, jobMeta);
		// ChannelLogTable使用的数据库连接名（上面配置的变量名）。
		channelLogTable.setConnectionName(resource.get(0).getResourceName());
		// 设置Channel日志的表名
		channelLogTable.setTableName("jobchan_log");
		// 设置jobMeta的ChannelLogTable
		jobMeta.setChannelLogTable(channelLogTable);
	}

	/**
	 * 设置转化的参数
	 * 
	 * @param transname
	 * @return
	 */
	private JobMeta getJobMeta(String jobname, String incr) {
		JobMeta jobMeta = new JobMeta();
		// 设置转化的名称
		jobMeta.setName(jobname);

		// 若为增量，需要添加增量参数
		if (incr != null && incr.equals("1")) {

			try {
				//jobMeta.addParameterDefinition("v_date", "to_date('19000101','yyyymmdd')", "增量时间");
				jobMeta.addParameterDefinition("v_date", "'19000101000000'", "增量时间");
			} catch (DuplicateParamException e) {
				e.printStackTrace();
			}
		}

		// job只添加日志数据库连接
		DatabaseMeta databaseMeta = null;
		try {
			databaseMeta = new DatabaseMeta(databasesXML[0]);
		} catch (KettleXMLException e) {
			LOGGER.error("{}", e);
		}
		jobMeta.addDatabase(databaseMeta);

		return jobMeta;
	}

	/**
	 * 生成job对应多个trans文件
	 * 
	 * @param jobName
	 * @param transNameList
	 * @return
	 */
	public boolean createJob(String subPath,String jobName, List<String> transNameList, String incr) {
		this.jobName = jobName;
		// 将数据源基本信息组合成数组
		getDatabasesXml();
		// 设置转化的参数
		JobMeta jobMeta = getJobMeta(jobName, incr);

		// 定义日志
		setJobLog(jobMeta);

		PluginInterface jobPlugin = registry.findPluginWithId(JobEntryPluginType.class, "SPECIAL");

		JobEntryInterface jeiStart = null;
		try {
			jeiStart = (JobEntryInterface) registry.loadClass(jobPlugin);
		} catch (KettlePluginException e1) {
			LOGGER.error("{}", e1);
			return flagJob;
		}
		JobEntryCopy jgeStart = getJobEntryCopy(jobMeta, jobPlugin, jeiStart);
		if (transNameList.size() > 1) {
			jgeStart.setLaunchingInParallel(true);
		}
		jobPlugin = PluginRegistry.getInstance().findPluginWithId(JobEntryPluginType.class, "TRANS");

		for (int i = 0; i < transNameList.size(); i++) {
			File file = new File(path +subPath+ transNameList.get(i) + ".ktr");
			if (!file.exists()) {
				LOGGER.error(path+subPath+transNameList.get(i) + ".ktr文件不存在！！");
				return false;
			}
			transInJob(transNameList.get(i), jobMeta, jobPlugin, jgeStart, i);

		}
		jobFileOutPut(subPath,jobName, jobMeta);

		if (flagJob) {
			LOGGER.info("JOB文件"+"["+path+subPath+jobName+"]"+"创建成功！");
		} else {
			LOGGER.info("JOB文件"+"["+path+subPath+jobName+"]"+"创建失败！");
		}
		return flagJob;
	}


	private JobEntryCopy getJobEntryCopy(JobMeta jobMeta, PluginInterface jobPlugin, JobEntryInterface jeiStart) {
		jeiStart.setPluginId(jobPlugin.getIds()[0]);

		((JobEntrySpecial) jeiStart).setStart(true);
		jeiStart.setName(JobMeta.STRING_SPECIAL_START);
		JobEntryCopy jgeStart = new JobEntryCopy();
		jgeStart.setEntry(jeiStart);
		jgeStart.setLocation(100, 100);
		jgeStart.setNr(0);
		jgeStart.setDrawn(true);
		jobMeta.addJobEntry(jgeStart);
		return jgeStart;
	}

	private void transInJob(String transName, JobMeta jobMeta, PluginInterface jobPlugin, JobEntryCopy jgeStart,
			int i) {
		JobEntryInterface jeiTrans = null;
		try {
			jeiTrans = (JobEntryInterface) registry.loadClass(jobPlugin);
		} catch (KettlePluginException e1) {
			LOGGER.error("{}", e1);
		}
		jeiTrans.setPluginId(jobPlugin.getIds()[0]);
		jeiTrans.setName("trans_" + transName);

		JobEntryCopy jgeTrans = new JobEntryCopy();
		jgeTrans.setEntry(jeiTrans);
		jgeTrans.setLocation(250, 100 + 100 * i);
		jgeTrans.setNr(0);
		jgeTrans.setDrawn(true);
		jobMeta.addJobEntry(jgeTrans);
		jobMeta.addJobHop(new JobHopMeta(jgeStart, jgeTrans));
		JobEntryInterface entry = jgeTrans.getEntry();
		JobEntryTrans trans = (JobEntryTrans) entry;
		trans.setFileName("${Internal.Job.Filename.Directory}/" + transName + ".ktr");
	}

	private void jobFileOutPut(String subPath,String jobName, JobMeta jobMeta) {
		String jobXml = jobMeta.getXML();
		File file = new File(path + (subPath==null?"":subPath) + jobName + ".kjb");
		try {
			FileUtils.writeStringToFile(file, jobXml, "UTF-8");
			flagJob = true;
		} catch (IOException e) {
			LOGGER.error("{}", e);
		}
	}

	/**
	 * 输出生成的Xml文件
	 * 
	 * @param transname
	 * @param transMeta
	 */
	private void outPutXml(String subPath,TransMeta transMeta) {
		String transXml = null;
		try {
			transXml = transMeta.getXML();
		} catch (KettleException e) {
			LOGGER.error("{}", e);
		}
		File file = new File(path+subPath + transName + ".ktr");
		try {
			FileUtils.writeStringToFile(file, transXml, "UTF-8");
			flagTrans = true;
		} catch (IOException e) {
			LOGGER.error("{}", e);
		}
	}
	
	/**
	 * 设置转化的参数
	 * 
	 * @param transname
	 * @return
	 */
	private TransMeta getTransMeta(String transname) {
		TransMeta transMeta = new TransMeta();
		// 设置转化的名称
		transMeta.setName(transname);
		// 添加转换的数据库连接
		for (int i = 0; i < databasesXML.length; i++) {
			DatabaseMeta databaseMeta = null;
			try {
				databaseMeta = new DatabaseMeta(databasesXML[i]);
			} catch (KettleXMLException e) {
				LOGGER.error("{}", e);
			}
			transMeta.addOrReplaceDatabase(databaseMeta);
		}
		return transMeta;
	}

	/**
	 * 将数据源基本信息组合成数组
	 * 
	 * @param resource
	 */
	private void getDatabasesXml() {
		databasesXML = new String[resource.size()];
		for (int i = 0; i < resource.size(); i++) {
			databasesXML[i] = MessageFormat.format(XMLFORMAT, resource.get(i).getResourceName(),
					resource.get(i).getResourceAddr(), resource.get(i).getDcResourceType().getTypeName(),
					resource.get(i).getDatabaseName(), resource.get(i).getPort() + "", resource.get(i).getAcount(),
					resource.get(i).getPasword());
			
			//modify by zhangcm 2016-11-15 将转换流程引用到的所有数据源，添加上连接池配置
			
			StringBuffer retval = new StringBuffer( databasesXML[i].replaceAll("</connection>", "  ") );
			retval.append("\r").append("<attributes>").append("\r");
			//强制标志符使用小写
			retval.append( "	<attribute><code>FORCE_IDENTIFIERS_TO_LOWERCASE</code><attribute>N</attribute></attribute>").append("\r");
			//强制标志符使用大写
			retval.append( "	<attribute><code>FORCE_IDENTIFIERS_TO_UPPERCASE</code><attribute>N</attribute></attribute>" ).append("\r");
			//初始连接数
			retval.append( "	<attribute><code>INITIAL_POOL_SIZE</code><attribute>5</attribute></attribute>" ).append("\r");
			//最大连接数
			retval.append( "	<attribute><code>MAXIMUM_POOL_SIZE</code><attribute>40</attribute></attribute>" ).append("\r");
			//连接超时时间
			retval.append( "	<attribute><code>POOLING_maxWait</code><attribute>30000</attribute></attribute>" ).append("\r");
			//重试延迟
			retval.append( "	<attribute><code>CONNECTION_RETRY_DELAY</code><attribute>2000</attribute></attribute>").append("\r");
			//重试时间
			retval.append( "	<attribute><code>CONNECTION_RETRY_TIMES</code><attribute>10</attribute></attribute>").append("\r");
			//允许最大空闲连接数
			retval.append( "	<attribute><code>POOLING_maxIdle</code><attribute>8</attribute></attribute>").append("\r");
			//当连接空闲时，是否执行连接测试
			retval.append( "    <attribute><code>POOLING_testOnBorrow</code><attribute>true</attribute></attribute>" ).append("\r");
			retval.append( "    <attribute><code>POOLING_testWhileIdle</code><attribute>true</attribute></attribute>" ).append("\r");
			retval.append( "    <attribute><code>POOLING_timeBetweenEvictionRunsMillis</code><attribute>30000</attribute></attribute>" ).append("\r");
			//失效连接回收时间
			retval.append( "    <attribute><code>POOLING_removeAbandonedTimeout</code><attribute>300</attribute></attribute>" ).append("\r");
			retval.append( "    <attribute><code>PORT_NUMBER</code><attribute>" + resource.get(i).getPort()+ "</attribute></attribute>" ).append("\r");
			retval.append( "    <attribute><code>QUOTE_ALL_FIELDS</code><attribute>N</attribute></attribute>" ).append("\r");
			//支持boolean数据类型
			retval.append( "    <attribute><code>SUPPORTS_BOOLEAN_DATA_TYPE</code><attribute>Y</attribute></attribute>" ).append("\r");
			//是否使用连接池
			retval.append( "	<attribute><code>USE_POOLING</code><attribute>Y</attribute></attribute>" ).append("\r");
			retval.append("</attributes>").append("\r");
			retval.append("</connection>");
			databasesXML[i] = retval.toString();
			
	
		}
	}

	/**
	 * 删除job文件
	 * 
	 * @param jobName
	 */
	public boolean dropJob(String subPath,String jobName) {
		File jobFile = new File(path + subPath+ jobName + ".kjb");
		boolean flag = jobFile.delete();
		return flag;
	}

	/**
	 * 删除trans文件
	 * 
	 * @param transName
	 */
	public boolean droptrans(String subPath,String transName) {

		File transFile = new File(path +subPath+ transName + ".ktr");
		boolean flag = transFile.delete();
		return flag;
	}

	/**
	 * 创建目录
	 * 
	 * @param name
	 */
	public boolean createDir(String name) {

		File dir = new File(name);
		if(dir.exists()){
			return true;
		}else{
			return dir.mkdirs();
		}
	}

	/**
	 *  @Description:得到文件大小（字节）
	 *  @date:2016年07月21日 
	 *  @author:孟山峰
	 *  @param file
	 *  @return 
	 */
	public long getFileSize(File file){

	        long  size =  0 ;  
	        File flist[] = file.listFiles();  
	        for  ( int  i =  0 ; i < flist.length; i++)  
	        {  
	            if  (flist[i].isDirectory())  
	            {  
	                size = size + getFileSize(flist[i]);  
	            } else   
	            {  
	                size = size + flist[i].length();  
	            }  
	        }  
		
		return size;
	}
	
	/**
	 *  @Description:移动文件夹
	 *  @date:2016年08月04日 
	 *  @author:孟山峰
	 *  @param sourcepath
	 *  @param targetpath
	 *  @return 
	 */
	public boolean moveFiles(String sourcepath,String targetpath){

		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        File sourcedir = new File(sourcepath);
		// 文件列表
		File[] files = sourcedir.listFiles();
		if (files == null)
			return true;
		// 目标
		File targetdir = new File(targetpath);
		if (!targetdir.exists()) {
			targetdir.mkdirs();
		}
		// 文件移动
		for (int i = 0; i < files.length; i++) {
			if (files[i].isDirectory()) {
				moveFiles(files[i].getPath(), targetpath + "\\" + files[i].getName());
				// 成功，删除原文件
				files[i].delete();
			}
			File targetFile = new File(targetdir.getPath() + "\\" + files[i].getName()+sdf.format(new Date())+".bak");
			// 目标文件夹下存在的话，删除
			if (targetFile.exists()) {
				targetFile.delete();
			}
			files[i].renameTo(targetFile);
		}
		return true;
	}
	
	/**
	 * @Description:创建  简单转换 表到表(仅用在订阅)
	 * @date:2016年08月03日
	 * @author:szh
	 * @param dcTransformation
	 * @param dcTableInput
	 * @param dcTableOutput
	 * @param dcExecSQL
	 */
	 public void createTranSimple(DcTransformation dcTransformation, DcTableInput dcTableInput, DcTableOutput dcTableOutput,DcExecSQL dcExecSQL) {
		 this.transName = dcTransformation.getTransName();
		// 将数据源基本信息组合成数组
		getDatabasesXml();
		// 设置转化的参数
		TransMeta transMeta = getTransMeta(this.transName);
		// 定义日志
		setTransLogData(transMeta);

		KettleCompUtil kettleCompUtil = new KettleCompUtil();
		List<StepMeta> stepMetaList = new ArrayList<StepMeta>();
		// 增量/全量
		String incr = dcTableInput.getIncrementType();
		// 源数据库名
		String sourceResourceName = resource.get(0).getResourceName();
		// 目标数据库名
		String targetResourceName = resource.get(1).getResourceName();
		// 源表名
		String sourceTableName = dcTableInput.getTableName();
		// 目标表名
		String targetTableName = dcTableOutput.getTableName();
		int stepSize;
		
		/**
		 *  若为增量，则需要添加删除目标表数据组件
		 */
		if (null != incr&& "1".equals(incr)) {
			String delSql = dcExecSQL.getDeleteSql();
			stepMetaList.add(kettleCompUtil.preDelSql(transMeta, targetResourceName, 
					targetTableName, delSql));
		}
		String sourceSql = dcTableInput.getTableinputSql();
		stepMetaList.add(kettleCompUtil.oldTableInput(transMeta, sourceResourceName, 
				sourceTableName, sourceSql, incr));
	
		/**
		 *  表输出步骤(TableOutputMeta)
		 */
		Collection<DataColumnDefine> sourceColumns = dcTableOutput.getSourceColumndefines();
		Collection<DataColumnDefine> targetColumns = dcTableOutput.getTargetColumndefines();
		stepMetaList.add(kettleCompUtil.newTableOutput(transMeta, targetResourceName, 
				sourceColumns, targetColumns, targetTableName,incr));

		stepSize = stepMetaList.size();
		
		/**
		 *  给最后一个步骤添加在spoon工具中的显示位置
		 */
		stepMetaList.get(stepSize - 1).setDraw(true);
		stepMetaList.get(stepSize - 1).setLocation(stepSize * 200, 100 + (stepSize - 1) * 50);
		transMeta.addOrReplaceStep(stepMetaList.get(stepSize - 1));
		
		/**
		 *  把相邻的两个步骤“连线”
		 */
		for(int idx = 0; idx < stepSize-1; idx++){
			// 给步骤添加在spoon工具中的显示位置
			stepMetaList.get(idx).setDraw(true);
			stepMetaList.get(idx).setLocation((idx + 1) * 200, 100 + idx * 50);
			transMeta.addOrReplaceStep(stepMetaList.get(idx));
			
			//连线
			transMeta.addTransHop(new TransHopMeta(stepMetaList.get(idx), stepMetaList.get(idx+1)));
		}

		// 输出到xml文件中
		outPutXml(dcTransformation.getPath(),transMeta);
		
		if (flagTrans) {
			LOGGER.info("TRANS文件创建成功");
		} else {
			LOGGER.info("TRANS文件创建失败");
		}
	}
	 
 	/**
	 * @Description:创建  简单转换 表到表并添加批次号
	 * @date:2017年09月06日
	 * @author:szh
	 * @param dcTransformation
	 * @param dcTableInput
	 * @param dcTableOutput
	 * @param dcExecSQL
	 */
	public void createTranOneTouch(DcTransformation dcTransformation, Map<String, Object> map) {
		this.transName = dcTransformation.getTransName();
		// 将数据源基本信息组合成数组
		getDatabasesXml();
		// 设置转化的参数
		TransMeta transMeta = getTransMeta(this.transName);
		// 定义日志
		setTransLogData(transMeta);

		KettleCompUtil kettleCompUtil = new KettleCompUtil();
		List<String> stepMetaList = new ArrayList<String>();
		Map<String,StepMeta> stepMetaMap = new HashMap<String, StepMeta>();
		// 增量/全量
		String incr = null;
		// 源数据库名
		String sourceResourceName = resource.get(0).getResourceName();
		// 目标数据库名
		String targetResourceName = resource.get(1).getResourceName();			
		// 源表名
		String sourceTableName = null;
		// 目标表名
		String targetTableName = null;			
		
		//删除目标表
		if(map.containsKey("dcExecSQL")){
			DcExecSQL dcExecSQL = (DcExecSQL) map.get("dcExecSQL");
			String delSql = dcExecSQL.getDeleteSql();
			stepMetaList.add("dcExecSQL");
			stepMetaMap.put("dcExecSQL",kettleCompUtil.preDelSql(transMeta, targetResourceName, "presql", delSql));
		}

		//表输入
		if(map.containsKey("dcTableInput")){
			DcTableInput dcTableInput = (DcTableInput) map.get("dcTableInput");
			String sourceSql = dcTableInput.getTableinputSql();
			sourceTableName = dcTableInput.getTableName();
			incr = dcTableInput.getIncrementType();
			stepMetaList.add("dcTableInput");
			stepMetaMap.put("dcTableInput",kettleCompUtil.oldTableInput(transMeta, sourceResourceName, sourceTableName, sourceSql, incr));
		}
		
		// 获取系统批次ID
		StepMeta systemDataMeta = kettleCompUtil.addGetSystemData();
		systemDataMeta.setDraw(true);
		stepMetaList.add("systemData");
		stepMetaMap.put("systemData",systemDataMeta);
//		transMeta.addOrReplaceStep(systemDataMeta);
		
		//表输出步骤
		if(map.containsKey("dcTableOutput")){
			DcTableOutput dcTableOutput = (DcTableOutput) map.get("dcTableOutput");
			Collection<DataColumnDefine> sourceColumns = dcTableOutput.getSourceColumndefines();
			Collection<DataColumnDefine> targetColumns = dcTableOutput.getTargetColumndefines();
			DataColumnDefine sourceIdBatch= new DataColumnDefine();
			DataColumnDefine sourceExecTime= new DataColumnDefine();			
			DataColumnDefine targetIdBatch= new DataColumnDefine();
			DataColumnDefine targetExecTime= new DataColumnDefine();
			sourceIdBatch.setColumnName("IdBatch");
			sourceExecTime.setColumnName("ExecTime");
			targetIdBatch.setColumnName("IdBatch");
			targetExecTime.setColumnName("ExecTime");
			sourceColumns.add(sourceIdBatch);
			sourceColumns.add(sourceExecTime);
			targetColumns.add(targetIdBatch);
			targetColumns.add(targetExecTime);
			targetTableName = dcTableOutput.getTableName();
			stepMetaList.add("dcTableOutput");
			stepMetaMap.put("dcTableOutput",kettleCompUtil.newTableOutput(transMeta, targetResourceName, sourceColumns, targetColumns, targetTableName,"1"));
		}

		int stepSize = stepMetaList.size();
		
		/**
		 *  给最后一个步骤添加在spoon工具中的显示位置
		 */
		stepMetaMap.get(stepMetaList.get(stepSize - 1)).setDraw(true);
		stepMetaMap.get(stepMetaList.get(stepSize - 1)).setLocation(stepSize * 200, 100 + (stepSize - 1) * 50);
		transMeta.addOrReplaceStep(stepMetaMap.get(stepMetaList.get(stepSize - 1)));
		
		/**
		 *  把相邻的两个步骤“连线”
		 */
		for(int idx = 0; idx < stepSize-1; idx++){
			// 给步骤添加在spoon工具中的显示位置
			stepMetaMap.get(stepMetaList.get(idx)).setDraw(true);
			stepMetaMap.get(stepMetaList.get(idx)).setLocation((idx + 1) * 200, 100 + idx * 50);
			transMeta.addOrReplaceStep(stepMetaMap.get(stepMetaList.get(idx)));
			
			//连线
			transMeta.addTransHop(new TransHopMeta(stepMetaMap.get(stepMetaList.get(idx)), stepMetaMap.get(stepMetaList.get(idx+1))));
		}

		// 输出到xml文件中
		outPutXml("",transMeta);
		
		if (flagTrans) {
			LOGGER.info("TRANS文件"+"["+path+this.transName+"]"+"创建成功!");
		} else {
			LOGGER.info("TRANS文件"+"["+path+this.transName+"]"+"创建失败!");
		}
	}
}