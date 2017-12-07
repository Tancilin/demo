package com.jusfoun.core.main.etl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.pentaho.di.job.entries.ftpput.JobEntryFTPPUT;
import org.pentaho.di.trans.steps.calculator.CalculatorMetaFunction;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;

import com.jusfoun.core.main.etl.bean.DataColumnDefine;
import com.jusfoun.core.main.etl.bean.DataResource;
import com.jusfoun.core.main.etl.bean.DataResourceType;
import com.jusfoun.core.main.etl.bean.DcExecSQL;
import com.jusfoun.core.main.etl.bean.DcFilterRows;
import com.jusfoun.core.main.etl.bean.DcReplaceString;
import com.jusfoun.core.main.etl.bean.DcSortRows;
import com.jusfoun.core.main.etl.bean.DcStringOperations;
import com.jusfoun.core.main.etl.bean.DcTableInput;
import com.jusfoun.core.main.etl.bean.DcTableOutput;
import com.jusfoun.core.main.etl.bean.DcTransformation;
import com.jusfoun.core.main.etl.bean.DcUniqueRows;
import com.jusfoun.core.main.etl.bean.KettlerHandler;
import com.jusfoun.core.main.util.PropertiesUtils;

/**
 * json解析类/应用启动入口
 * 
 * @author mengshanfeng
 * @date 20160803
 */
@EnableAutoConfiguration
@ComponentScan
@EnableAsync
public class KettleStarter {

	private static final Logger LOGGER = Logger.getLogger(KettleStarter.class);
	
	private static String ftpRootPath = "";
	
	private static DataResource logDB = new DataResource();
	private static DataResource filterDB = new DataResource();
	private static DataResource landDB = new DataResource();
	
	private static void configFtp() {
		
		//配置ftp服务器根目录
		ftpRootPath=PropertiesUtils.getFtpRootPath();
	}
	
	private static void configDB() {
		
		// 设置log数据库信息（指前置机上的ETL数据库）
		logDB=PropertiesUtils.getDataBaseInfo("kettleLog");
		logDB.setResourceName("log");
//		DataResourceType type1 = new DataResourceType();
//		type1.setTypeName("Oracle");
//		logDB.setDcResourceType(type1);
		
		// 设置问题库数据库信息（指中心上的问题库）
		filterDB=PropertiesUtils.getDataBaseInfo("filter");
		filterDB.setResourceName("filter");
//		DataResourceType type2 = new DataResourceType();
//		type2.setTypeName("Oracle");
//		filterDB.setDcResourceType(type2);
		
		// 设置落地库数据库信息（指中心上的落地库）
		landDB=PropertiesUtils.getDataBaseInfo("transLanding");
		landDB.setResourceName("land");
//		DataResourceType type3 = new DataResourceType();
//		type3.setTypeName("Oracle");
//		landDB.setDcResourceType(type3);
	}

	public static boolean createJob(String json) {
		configDB();
		String subPath ="";
		String configJson = json;
		KettleUtil ku = new KettleUtil();
		List<String> transNameList = new ArrayList<>();
		try {
			JSONObject jsonObject = new JSONObject(configJson);
			String job = jsonObject.getString("job");
			String jobIncr = jsonObject.getString("jobIncr");
			JSONArray transNameArr = jsonObject.getJSONArray("transNameArr");
			
			for (int i = 0; i < transNameArr.length(); i++) {
				transNameList.add(transNameArr.getString(i));
			}

			
			if(jsonObject.has("subPath")){
				subPath = jsonObject.getString("subPath");
				if(subPath.equals("null")){
					subPath = "";
				}
			}
			
			// 设置log数据库
			ku.setJobLogDB(KettlerHandler.getDataResource(logDB));
			// 生成job文件
			ku.createJob(subPath,job, transNameList, jobIncr);

		} catch (JSONException e) {
			LOGGER.error("解析JSON参数发生异常！", e);
			return false;
		}
		return true;
	}

	public static void createJobFTP(String json) {
		configFtp();
		configDB();
		String subPath = null;
		String configJson = json;
		KettleUtil ku = new KettleUtil();
		try {
			JSONObject jsonObject = new JSONObject(configJson);
			String job = jsonObject.getString("job");
			String jobIncr = "0";
			if(jsonObject.has("subPath")){
				subPath = jsonObject.getString("subPath");
				if(subPath.equals("null")){
					subPath = "";
				}
			}
			JSONObject entryObject = jsonObject.getJSONObject("entryArr");
			if(entryObject==null){
				LOGGER.error("解析JSON参数发生异常！ JSON对象为空！");
				return;
			}
			JobEntryFTPPUT jobEntryFTPPUT = new JobEntryFTPPUT();
			String entryType = entryObject.getString("entryType");
			if("29".equals(entryType)){//FTP 上传
				jobEntryFTPPUT.setServerName(entryObject.getString("servername"));
				jobEntryFTPPUT.setServerPort(entryObject.getString("serverport"));
				jobEntryFTPPUT.setUserName(entryObject.getString("username"));
				jobEntryFTPPUT.setPassword(entryObject.getString("password"));
				jobEntryFTPPUT.setRemoteDirectory("/receive/"+job);//每个作业对应一个目录
				//create by zhnagcm 2016-12-20  将"源目录路径"拆分为目录路径+文件名(正则表达式)
				String folderPath = null;
				String reg = null;
				String localDirectory = entryObject.getString("localDirectory");
				//非空验证
				if(!"".equals(localDirectory)&&null!=localDirectory){
					//判断文件路径时候包含文件名(文件名中包含符号".")
					if(localDirectory.contains(".")){
						int index = localDirectory.lastIndexOf("\\");
						//文件目录
						folderPath = localDirectory.substring(0,index+1);
						//正则表达式
						reg = localDirectory.substring(index+1,localDirectory.length());
					}else{
						folderPath = localDirectory;
					}
				}
				jobEntryFTPPUT.setLocalDirectory(folderPath);
				jobEntryFTPPUT.setWildcard(reg);
			}	
			//设置是否以二进制流格式传输文件
			jobEntryFTPPUT.setBinaryMode(true);
			//设置默认控制编码
			String controlEncoding = entryObject.getString("controlEncoding");
			jobEntryFTPPUT.setControlEncoding(controlEncoding==null||controlEncoding.isEmpty()?"UTF-8":controlEncoding);
			// 设置log数据库
			ku.setJobLogDB(KettlerHandler.getDataResource(logDB));
			// 生成job文件
			ku.createJobFTP(subPath,job,jobEntryFTPPUT,ftpRootPath,jobIncr);

		} catch (JSONException e) {
			LOGGER.error("解析JSON参数发生异常！", e);
		}
	}
	
	//为拖拽做准备，重写为按组件解析 modified by mengshanfeng 20160802
	public static void createTrans(String json) {
		configDB();
		
		String configJson = json;
		KettleUtil ku = new KettleUtil();
		//存储解析对象
		Map<String, Object> map = new HashMap<>(); 
		//转换基本信息
		DcTransformation dcTransformation = new DcTransformation();
		//数据源配置信息
		DataResource originalData = new DataResource();
		DataResource targetData = new DataResource();
		//表列对应关系
		List<DataColumnDefine> inputCol = new ArrayList<DataColumnDefine>();
		List<DataColumnDefine> outputCol = new ArrayList<DataColumnDefine>();
		//组件信息
		DcTableInput dcTableInput = null;
		DcTableOutput dcTableOutput = null;
		DcExecSQL dcExecSQL = null;	
		DcFilterRows rootDcFilterRows = null;
		List<DcFilterRows> dcFilterRowsList = null;
		DcSortRows dcSortRows = null;
		DcUniqueRows dcUniqueRows = null;
		List<CalculatorMetaFunction> functionList = null;
		List<DcReplaceString> dcReplaceStringList = null;

		try {
			JSONObject transObject = new JSONObject(configJson);
				
				if(transObject.has("subPath")){
					String subPath = transObject.getString("subPath");
					if(!subPath.equals("null")){
						dcTransformation.setPath(subPath);
					}else{
						dcTransformation.setPath(null);
					}
					
				}
				
				JSONObject tran = transObject.getJSONObject("tran");
				JSONObject fromDb = transObject.getJSONObject("fromDb");
				String fromDbType = transObject.getString("fromDbType");
				JSONObject fromTab = transObject.getJSONObject("fromTab");
				JSONObject toDb = transObject.getJSONObject("toDb");
				String toDbType = transObject.getString("toDbType");
				JSONObject toTab = transObject.getJSONObject("toTab");

				dcTransformation.setTransName(tran.getString("transformationName"));
				dcTransformation.setTransDescription(tran.getString("description"));
				
				// 原数据库设置
				originalData.setResourceName(fromDb.getString("resourceName"));
				originalData.setResourceAddr(fromDb.getString("resourceAddr"));
				DataResourceType type = new DataResourceType();
				type.setTypeName(fromDbType);
				originalData.setDcResourceType(type);
				originalData.setDatabaseName(fromDb.getString("databaseName"));
				originalData.setPort(Integer.parseInt(fromDb.getString("port")));
				originalData.setAcount(fromDb.getString("acount"));
				originalData.setPasword(fromDb.getString("pasword"));
				
				// 目标数据库设置
				targetData.setResourceName(toDb.getString("resourceName"));
				targetData.setResourceAddr(toDb.getString("resourceAddr"));
				DataResourceType type1 = new DataResourceType();
				type1.setTypeName(toDbType);
				targetData.setDcResourceType(type1);
				targetData.setDatabaseName(toDb.getString("databaseName"));
				targetData.setPort(Integer.parseInt(toDb.getString("port")));
				targetData.setAcount(toDb.getString("acount"));
				targetData.setPasword(toDb.getString("pasword"));
				
				//表输入组件
				if(transObject.has("tableinput")){
					JSONObject inputObject = transObject.getJSONObject("tableinput");
					dcTableInput = new DcTableInput();
					dcTableInput.setTableinputSql(inputObject.getString("tableinputSql0"));
					dcTableInput.setInputname(inputObject.getString("name"));
					dcTableInput.setIncrementType(inputObject.getString("incrementType0"));
					dcTableInput.setLandOrNot(inputObject.getString("isLanding0"));
					dcTableInput.setTableName(fromTab.getString("tableName"));
					map.put("dcTableInput", dcTableInput);
				}
				
				//表输出组件
				if(transObject.has("tableoutput")){
					JSONObject outputObject = transObject.getJSONObject("tableoutput");
					dcTableOutput = new DcTableOutput();
					dcTableOutput.setOutputname(outputObject.getString("name"));
					
					int cnt = outputObject.getInt("columnCount0");
					for (int i = 0; i < cnt; i++) {
						inputCol.add(new DataColumnDefine(outputObject.getString("sourceColumnName"+i)));
						outputCol.add(new DataColumnDefine(outputObject.getString("targetColumnName"+i)));
					}

					dcTableOutput.setSourceColumndefines(inputCol);
					dcTableOutput.setTargetColumndefines(outputCol);
					dcTableOutput.setTableName(toTab.getString("tableName"));	
					
					//临时使用，在做拖拽时会独立出来
					if(!"".equals(outputObject.getString("deleteSql0"))){
						dcExecSQL = new DcExecSQL();
						dcExecSQL.setDeleteSql(outputObject.getString("deleteSql0"));
						map.put("dcExecSQL", dcExecSQL);
					}
					map.put("dcTableOutput", dcTableOutput);
					
				}
				
				//计算器组件
				if(transObject.has("calculator")){
					JSONObject calculatorObject = transObject.getJSONObject("calculator");
					Integer calculatorCount=calculatorObject.getInt("calculatorCount0");
					functionList = new ArrayList<>();
					for(int i=0 ;i<calculatorCount;i++){
						CalculatorMetaFunction function = new CalculatorMetaFunction();
						function.setFieldName(calculatorObject.getString("fieldName"+i));
						function.setCalcType(calculatorObject.getInt("calcType"+i));
						function.setFieldA(calculatorObject.getString("fieldA"+i));
						function.setFieldB(calculatorObject.getString("fieldB"+i));
						function.setFieldC(calculatorObject.getString("fieldC"+i));
						function.setValueType(calculatorObject.getInt("valueType"+i));
						function.setRemovedFromResult(false);
						function.setConversionMask("#");
						functionList.add(function);
					}
					map.put("functionList", functionList);
				}
				
				//过滤组件
				if(transObject.has("conditions")&&transObject.has("filterrows")){
					JSONArray conditionsArr = transObject.getJSONArray("conditions");
					rootDcFilterRows = new DcFilterRows();	
					dcFilterRowsList = new ArrayList<>();
					for (int i = 0; i < conditionsArr.length(); i++) {
						JSONObject conditionsObject = (JSONObject) conditionsArr.get(i);
						
						DcFilterRows dcFilterRows = new DcFilterRows();
						dcFilterRows.setId(conditionsObject.getString("idCondition"));
						dcFilterRows.setParentId(conditionsObject.getString("idConditionParent"));
						dcFilterRows.setNegated(conditionsObject.getString("negated").equals("1")?true:false);
						String operator = conditionsObject.getString("operator");
						if(!operator.equals("null")&&!operator.equals("")&&operator!=null){
							if(operator.equals("OR")){
								operator = "1";
							}else if(operator.equals("AND")){
								operator = "2";
							}else if(operator.equals("NOT")){
								operator = "3";
							}else if(operator.equals("OR_NOT")){
								operator = "4";
							}else if(operator.equals("AND_NOT")){
								operator = "5";
							}else if(operator.equals("XOR")){
								operator = "6";
							}
							dcFilterRows.setOperator(Integer.valueOf(operator).intValue());	
						}						
						dcFilterRows.setLeftName(conditionsObject.getString("leftName"));
						dcFilterRows.setConditionFunction(conditionsObject.getString("conditionFunction"));
						dcFilterRows.setRightName(conditionsObject.getString("rightName"));
						
						Object dcValueObject = conditionsObject.get("dcValue");
						if(!dcValueObject.equals("null")&&dcValueObject instanceof JSONObject){
							dcFilterRows.setFilterValue(((JSONObject)dcValueObject).getString("valueStr"));	
						}

						if(i==0){
							dcFilterRows.setNegated(false);
							rootDcFilterRows = dcFilterRows;
						}
						dcFilterRowsList.add(dcFilterRows);
					}
					map.put("dcFilterRows", rootDcFilterRows);
					map.put("dcFilterRowsList", dcFilterRowsList);
				}
				
				//排序组件
				if(transObject.has("sortRows")){
					JSONObject sortObject = transObject.getJSONObject("sortRows");
					dcSortRows = new DcSortRows();
					// 去重的列
					dcSortRows.setUniqueRows(sortObject.getString("uniqueRows"));
					// 去重的列是否忽略大小写
					dcSortRows.setIgnoreCase(sortObject.getString("ignoreCase"));
					map.put("dcSortRows", dcSortRows);
				}
				
				//去重组件
				if(transObject.has("uniquerows")){
					JSONObject uniqueObject = transObject.getJSONObject("uniquerows");
					dcUniqueRows = new DcUniqueRows();
					// 去重的列
					dcUniqueRows.setUniqueRows(uniqueObject.getString("uniqueRows0"));
					// 去重的列是否忽略大小写
					dcUniqueRows.setIgnoreCase(uniqueObject.getString("ignoreCase0"));
					map.put("dcUniqueRows", dcUniqueRows);
				}
				
				//字符串操作组件
				if(transObject.has("stringoperation")){
					JSONObject stringObject = transObject.getJSONObject("stringoperation");
					DcStringOperations dcStringOperations = new DcStringOperations();
					dcStringOperations.setOperateColumns(stringObject.getString("operateColumns0"));
					dcStringOperations.setTrimTypes(stringObject.getString("trimTypes0"));
					dcStringOperations.setLowerUppers(stringObject.getString("lowerUppers0"));
					map.put("dcStringOperations", dcStringOperations);
				}

				//字符串替换
				if(transObject.has("stringreplace")){
					JSONObject stringReplaceObject = transObject.getJSONObject("stringreplace");
					Integer cnt=stringReplaceObject.getInt("stringReplaceCount0");
					dcReplaceStringList = new ArrayList<>();
					for(int i=0 ;i<cnt;i++){
						DcReplaceString dcReplaceString = new DcReplaceString();
						dcReplaceString.setOperateColumns(stringReplaceObject.getString("replaceOperateColumns"+i));
						dcReplaceString.setReplaceStrings(stringReplaceObject.getString("replaceStrings"+i));
						dcReplaceString.setReplaceByStrings(stringReplaceObject.getString("replaceByStrings"+i));
						dcReplaceString.setUseRegExOrNot(stringReplaceObject.getString("UseRegExOrNot"+i));
						
						dcReplaceStringList.add(dcReplaceString);
					}
					map.put("dcReplaceStringList", dcReplaceStringList);
				}
				
				//新组件在此罗列添加。。。。。
				
				// 设置源数据库
				ku.setOriginalData(KettlerHandler.getDataResource(originalData));
				// 设置目标数据库
				ku.setTargetData(KettlerHandler.getDataResource(targetData));
				// 设置log数据库
				ku.setTransLogDB(KettlerHandler.getDataResource(logDB));
				// 设置问题数据库
				ku.setTransFilterDB(KettlerHandler.getDataResource(filterDB));
				// 设置落地数据库
				ku.setTransLandDB(KettlerHandler.getDataResource(landDB));
				
				ku.createTrans(dcTransformation,map);
		} catch (JSONException e) {
			LOGGER.error("解析JSON参数发生异常！", e);
		}
	}

	/**
	 * @Description:创建简单转换 表到表(仅用在订阅)
	 * @date:2016年08月03日
	 * @author:szh
	 * @param json
	 */
	public static void createTransSimple(String json) {	
		configDB();		
		String configJson = json;
		KettleUtil ku = new KettleUtil();
		//存储解析对象
		//转换基本信息
		DcTransformation dcTransformation = new DcTransformation();
		//数据源配置信息
		DataResource originalData = new DataResource();
		DataResource targetData = new DataResource();
		//表列对应关系
		List<DataColumnDefine> inputCol = new ArrayList<DataColumnDefine>();
		List<DataColumnDefine> outputCol = new ArrayList<DataColumnDefine>();
		//组件信息
		DcTableInput dcTableInput = null;
		DcTableOutput dcTableOutput = null;
		DcExecSQL dcExecSQL = null;		
		
		try {		
			JSONObject jsonObject = new JSONObject(configJson);
			
			if(jsonObject.has("subPath")){
				String subPath = jsonObject.getString("subPath");
				if(!subPath.equals("null")){
					dcTransformation.setPath(subPath);
				}else{
					dcTransformation.setPath(null);
				}
				
			}
			
			JSONObject fromDb = jsonObject.getJSONObject("fromDb");
			String fromDbType = jsonObject.getString("fromDbType");
			JSONObject fromTab = jsonObject.getJSONObject("fromTab");
			JSONObject toDb = jsonObject.getJSONObject("toDb");
			String toDbType = jsonObject.getString("toDbType");
			JSONObject toTab = jsonObject.getJSONObject("toTab");			
			JSONObject tran = jsonObject.getJSONObject("tran");
			dcTransformation.setTransName(tran.getString("transformationName"));
			dcTransformation.setTransDescription(tran.getString("description"));
			
			// 原数据库设置
			originalData.setResourceName(fromDb.getString("resourceName"));
			originalData.setResourceAddr(fromDb.getString("resourceAddr"));
			DataResourceType type = new DataResourceType();
			type.setTypeName(fromDbType);
			originalData.setDcResourceType(type);
			originalData.setDatabaseName(fromDb.getString("databaseName"));
			originalData.setPort(Integer.parseInt(fromDb.getString("port")));
			originalData.setAcount(fromDb.getString("acount"));
			originalData.setPasword(fromDb.getString("pasword"));
			// 目标数据库设置

			targetData.setResourceName(toDb.getString("resourceName"));
			targetData.setResourceAddr(toDb.getString("resourceAddr"));
			DataResourceType type1 = new DataResourceType();
			type1.setTypeName(toDbType);
			targetData.setDcResourceType(type1);
			targetData.setDatabaseName(toDb.getString("databaseName"));
			targetData.setPort(Integer.parseInt(toDb.getString("port")));
			targetData.setAcount(toDb.getString("acount"));
			targetData.setPasword(toDb.getString("pasword"));
			
			//表输入组件			
			
			dcTableInput = new DcTableInput();
			dcTableInput.setTableinputSql(jsonObject.getString("tableinputSql"));
			dcTableInput.setInputname(fromTab.getString("tableName")+"表输入");
			if(jsonObject.has("incrementType")){				
				dcTableInput.setIncrementType(jsonObject.getString("incrementType"));
			}			
			dcTableInput.setTableName(fromTab.getString("tableName"));
			//表输出组件
			dcTableOutput = new DcTableOutput();
			dcTableOutput.setOutputname(toTab.getString("tableName")+"表输出");		
			JSONArray ColumnArr = toTab.getJSONArray("dccolumndefines");
			for (int i = 0; i < ColumnArr.length(); i++) {
				inputCol.add(new DataColumnDefine(ColumnArr.getJSONObject(i).getString("columnName")));
				outputCol.add(new DataColumnDefine(ColumnArr.getJSONObject(i).getString("columnName")));
			}
			dcTableOutput.setSourceColumndefines(inputCol);
			dcTableOutput.setTargetColumndefines(outputCol);
			dcTableOutput.setTableName(toTab.getString("tableName"));
			if(jsonObject.has("deleteSql")){				
				dcExecSQL = new DcExecSQL();
				dcExecSQL.setDeleteSql(jsonObject.getString("deleteSql"));
			}	
			// 设置源数据库
			ku.setOriginalData(KettlerHandler.getDataResource(originalData));
			// 设置目标数据库
			ku.setTargetData(KettlerHandler.getDataResource(targetData));
			// 设置log数据库
			ku.setTransLogDB(KettlerHandler.getDataResource(logDB));
			// 生成trans文件：【转换，				   表输入，		         表输出，	      执行sql 】	
			ku.createTranSimple(dcTransformation, dcTableInput, dcTableOutput, dcExecSQL);

		} catch (JSONException e) {
			LOGGER.error("解析JSON参数发生异常！", e);
		}
	}
	
	/**
	 * @Description:创建简单转换 表到表并添加批次号
	 * @date:2017年09月06日
	 * @author:szh
	 * @param json
	 */
	public static void createTranOneTouch(String json) {	
		configDB();		
		String configJson = json;
		KettleUtil ku = new KettleUtil();
		//存储解析对象
		Map<String, Object> map = new HashMap<>(); 
		//转换基本信息
		DcTransformation dcTransformation = new DcTransformation();
		//数据源配置信息
		DataResource originalData = new DataResource();
		DataResource targetData = new DataResource();
		//表列对应关系
		List<DataColumnDefine> inputCol = new ArrayList<DataColumnDefine>();
		List<DataColumnDefine> outputCol = new ArrayList<DataColumnDefine>();
		//组件信息
		DcTableInput dcTableInput = null;
		DcTableOutput dcTableOutput = null;
		DcExecSQL dcExecSQL = null;
		try {
			JSONObject transObject = new JSONObject(configJson);
				
			if(transObject.has("subPath")){
				String subPath = transObject.getString("subPath");
				if(!subPath.equals("null")){
					dcTransformation.setPath(subPath);
				}else{
					dcTransformation.setPath(null);
				}
				
			}
			
			JSONObject tran = transObject.getJSONObject("tran");
			JSONObject fromDb = transObject.getJSONObject("fromDb");
			String fromDbType = transObject.getString("fromDbType");
			JSONObject fromTab = transObject.getJSONObject("fromTab");
			JSONObject toDb = transObject.getJSONObject("toDb");
			String toDbType = transObject.getString("toDbType");
			JSONObject toTab = transObject.getJSONObject("toTab");

			dcTransformation.setTransName(tran.getString("transformationName"));
			dcTransformation.setTransDescription(tran.getString("description"));
			
			// 原数据库设置
			originalData.setResourceName(fromDb.getString("resourceName"));
			originalData.setResourceAddr(fromDb.getString("resourceAddr"));
			DataResourceType type = new DataResourceType();
			type.setTypeName(fromDbType);
			originalData.setDcResourceType(type);
			originalData.setDatabaseName(fromDb.getString("databaseName"));
			originalData.setPort(Integer.parseInt(fromDb.getString("port")));
			originalData.setAcount(fromDb.getString("acount"));
			originalData.setPasword(fromDb.getString("pasword"));
			
			// 目标数据库设置
			targetData.setResourceName(toDb.getString("resourceName"));
			targetData.setResourceAddr(toDb.getString("resourceAddr"));
			DataResourceType type1 = new DataResourceType();
			type1.setTypeName(toDbType);
			targetData.setDcResourceType(type1);
			targetData.setDatabaseName(toDb.getString("databaseName"));
			targetData.setPort(Integer.parseInt(toDb.getString("port")));
			targetData.setAcount(toDb.getString("acount"));
			targetData.setPasword(toDb.getString("pasword"));
			
			//表输入组件
			if(transObject.has("tableinput")){
				JSONObject inputObject = transObject.getJSONObject("tableinput");
				dcTableInput = new DcTableInput();
				dcTableInput.setTableinputSql(inputObject.getString("tableinputSql0"));
				dcTableInput.setInputname(inputObject.getString("name"));
				dcTableInput.setIncrementType(inputObject.getString("incrementType0"));
				dcTableInput.setLandOrNot(inputObject.getString("isLanding0"));
				dcTableInput.setTableName(fromTab.getString("tableName"));
				map.put("dcTableInput", dcTableInput);
			}
			
			//表输出组件
			if(transObject.has("tableoutput")){
				JSONObject outputObject = transObject.getJSONObject("tableoutput");
				dcTableOutput = new DcTableOutput();
				dcTableOutput.setOutputname(outputObject.getString("name"));
				
				int cnt = outputObject.getInt("columnCount0");
				for (int i = 0; i < cnt; i++) {
					inputCol.add(new DataColumnDefine(outputObject.getString("sourceColumnName"+i)));
					outputCol.add(new DataColumnDefine(outputObject.getString("targetColumnName"+i)));
				}

				dcTableOutput.setSourceColumndefines(inputCol);
				dcTableOutput.setTargetColumndefines(outputCol);
				dcTableOutput.setTableName(toTab.getString("tableName"));	
				
				//临时使用，在做拖拽时会独立出来
				if(!"".equals(outputObject.getString("deleteSql0"))){
					dcExecSQL = new DcExecSQL();
					dcExecSQL.setDeleteSql(outputObject.getString("deleteSql0"));
					map.put("dcExecSQL", dcExecSQL);
				}
				map.put("dcTableOutput", dcTableOutput);
				
			}
			
			// 设置源数据库
			ku.setOriginalData(KettlerHandler.getDataResource(originalData));
			// 设置目标数据库
			ku.setTargetData(KettlerHandler.getDataResource(targetData));
			// 设置log数据库
			ku.setTransLogDB(KettlerHandler.getDataResource(logDB));
			
			ku.createTranOneTouch(dcTransformation,map);
		} catch (JSONException e) {
			LOGGER.error("解析JSON参数发生异常！", e);
		}
	}
	public static void main(String[] args) {
		SpringApplication.run(KettleStarter.class, args);
		
	}

}
