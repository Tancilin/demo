package com.jusfoun.core.main.etl;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import org.apache.log4j.Logger;
import org.pentaho.di.core.Condition;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.StepPluginType;
import org.pentaho.di.core.row.ValueMetaAndData;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.di.trans.steps.calculator.CalculatorMeta;
import org.pentaho.di.trans.steps.calculator.CalculatorMetaFunction;
import org.pentaho.di.trans.steps.concatfields.ConcatFieldsMeta;
import org.pentaho.di.trans.steps.dummytrans.DummyTransMeta;
import org.pentaho.di.trans.steps.filterrows.FilterRowsMeta;
import org.pentaho.di.trans.steps.replacestring.ReplaceStringMeta;
import org.pentaho.di.trans.steps.sort.SortRowsMeta;
import org.pentaho.di.trans.steps.sql.ExecSQLMeta;
import org.pentaho.di.trans.steps.stringcut.StringCutMeta;
import org.pentaho.di.trans.steps.stringoperations.StringOperationsMeta;
import org.pentaho.di.trans.steps.systemdata.SystemDataMeta;
import org.pentaho.di.trans.steps.tableinput.TableInputMeta;
import org.pentaho.di.trans.steps.tableoutput.TableOutputMeta;
import org.pentaho.di.trans.steps.textfileoutput.TextFileField;
import org.pentaho.di.trans.steps.uniquerows.UniqueRowsMeta;

import com.jusfoun.core.main.etl.bean.DataColumnDefine;
import com.jusfoun.core.main.etl.bean.DcConcatFields;
import com.jusfoun.core.main.etl.bean.DcFilterRows;
import com.jusfoun.core.main.etl.bean.DcReplaceString;
import com.jusfoun.core.main.etl.bean.DcStringCut;
import com.jusfoun.core.main.etl.bean.DcStringOperations;
import com.jusfoun.core.main.etl.bean.DcTextFileField;
import com.jusfoun.core.main.etl.bean.DcUniqueRows;

/**
 * kettle组件类的调用方法
 * 
 * @author xuxiangfu
 * @date 20160629
 */
public class KettleCompUtil {
	private static final Logger LOGGER = Logger.getLogger(KettleCompUtil.class);

	private PluginRegistry registry;
	
	/**
	 * kettle环境初始化
	 */
	public KettleCompUtil() {
		try {
			KettleEnvironment.init();
		} catch (KettleException e) {
			LOGGER.error("初始化失败！{}", e);
		}
		registry = PluginRegistry.getInstance();
	}

	/**
	 * @Description:执行SQL脚本组件：增量删除目标preSQL
	 * @date:2016年06月20日
	 * @author:孟山峰
	 * @param transMeta
	 * @param targetTable
	 * @param delSQL
	 * @return
	 */
	public StepMeta preDelSql(TransMeta transMeta, String targetResourceName,
			String targetTableName, String delSQL) {
		ExecSQLMeta execSQLMeta = new ExecSQLMeta();
		execSQLMeta.setDefault();
		String execSQLMetaPluginId = registry.getPluginId(StepPluginType.class, execSQLMeta);
		DatabaseMeta database_target = transMeta.findDatabase(targetResourceName);
		execSQLMeta.setDatabaseMeta(database_target);
		execSQLMeta.setSql(delSQL);
		execSQLMeta.setVariableReplacementActive(true);

		StepMeta execSQLMetaStep = new StepMeta(execSQLMetaPluginId, targetTableName + "_del", execSQLMeta);

		return execSQLMetaStep;
	}

	/**
	 * 旧数据读取设置
	 * 
	 * @author mengshanfeng
	 * @param resource
	 * @param sourceSQL
	 * @param sourceTable
	 * @param transMeta
	 * @param registry
	 * @return
	 */
	public StepMeta oldTableInput(TransMeta transMeta, String sourceResourceName,
			String sourceTableName, String sourceSQL, String incr) {
		TableInputMeta tableInput = new TableInputMeta();
		String tableInputPluginId = registry.getPluginId(StepPluginType.class, tableInput);
		// 给表输入添加一个DatabaseMeta连接数据库
		DatabaseMeta databaseSource = transMeta.findDatabase(sourceResourceName);
		tableInput.setDatabaseMeta(databaseSource);
		tableInput.setSQL(sourceSQL);

		// 若为增量，需要添加参数替换属性
		if (incr != null && incr.equals("1")) {
			tableInput.setVariableReplacementActive(true);
		}

		// 添加TableInputMeta到转换中
		StepMeta tableInputMetaStep = new StepMeta(tableInputPluginId, sourceTableName + "_in", tableInput);

		return tableInputMetaStep;
	}

	/**
	 * @Description:获取过滤器中的级联条件
	 * @date:2016年10月10日
	 * @author:孟山峰
	 * @param dcFilterRowsList 集合
	 * @param fdcFilterRows 父
	 * @return StepMeta
	 */
	public Condition getChildCondition(List<DcFilterRows> dcFilterRowsList,DcFilterRows fdcFilterRows) {
		Condition conditionz = new Condition();
		
		//递归遍历层级关系
		for (Iterator<DcFilterRows> iterator = dcFilterRowsList.iterator(); iterator.hasNext();) {
			DcFilterRows zDcFilterRows = (DcFilterRows) iterator.next();
			Condition condition = new Condition();
			//若存在子节点，递归遍历
			if (fdcFilterRows.getId().equals(zDcFilterRows.getParentId())) {
				conditionz.setNegated(fdcFilterRows.getNegated());
				condition = getChildCondition(dcFilterRowsList, zDcFilterRows);
				//处理特殊情况：面板的操作符
				if(fdcFilterRows.getOperator()!=0&&(fdcFilterRows.getLeftName()==null||fdcFilterRows.getLeftName().equals("null"))){
					conditionz.setOperator(fdcFilterRows.getOperator());
				}
				conditionz.addCondition(condition);
			}
		}
		
		//若为叶子节点，取出条件
		if(fdcFilterRows.getLeftName()!=null&&!fdcFilterRows.getLeftName().equals("null")){//如果没有下级的话，设置条件
			conditionz.setLeftValuename(fdcFilterRows.getLeftName());
			conditionz.setFunction(Condition.getFunction(fdcFilterRows.getConditionFunction()));
			conditionz.setOperator(fdcFilterRows.getOperator());
			conditionz.setNegated(fdcFilterRows.getNegated());
			String rightName = fdcFilterRows.getRightName();
			String filterValue = fdcFilterRows.getFilterValue();
			if(rightName!=null&&!rightName.equals("null")&&!rightName.equals("")){
				conditionz.setRightValuename(rightName);
			}else if(filterValue!=null&&!filterValue.equals("null")){
				try {
					conditionz.setRightExact(new ValueMetaAndData("constant", filterValue));
				} catch (KettleValueException e) {
					e.printStackTrace();
				}
			}
			
		}
		
		return conditionz;
	}	

	/**
	 * @Description:在“转换”中进行数据过滤
	 * @date:2016年10月10日
	 * @author:孟山峰
	 * @param sourceTableName
	 * @param 
	 * @return StepMeta
	 */
	public StepMeta addFilterRows(String sourceTableName, List<DcFilterRows> dcFilterRowsList,DcFilterRows rootDcFilterRows){
		StepMeta stepMeta = new StepMeta();
		FilterRowsMeta frm = new FilterRowsMeta();
		frm.setCondition(getChildCondition(dcFilterRowsList, rootDcFilterRows));
		String pluginId = registry.getPluginId(StepPluginType.class, frm);
		// 添加TableInputMeta到转换中
		stepMeta = new StepMeta(pluginId, sourceTableName + "_filter", frm);
		return stepMeta;
	}

	/**
	 * Create a sort rows step
	 * 
	 * @author xuxiangfu
	 * @param transMeta
	 * @return
	 */
	public StepMeta addSortRows(String sourceTableName, DcUniqueRows dcUniqueRows) {
		String uniqueColumns = dcUniqueRows.getUniqueRows();
		String uniqueCaseI = dcUniqueRows.getIgnoreCase();
		
		String sortRowsStepname = sourceTableName + "_sortRows";
		
		// Columns的个数
		int uniqueColumnsCount = uniqueColumns.split(",").length;
		
		// 是否升序
		boolean[] ascendingArray = new boolean[uniqueColumnsCount];
		// preSorted
		boolean[] preSortedArray = new boolean[uniqueColumnsCount];
		// 是否忽略大小写（0/true：忽略，即 不区分大小写）
		boolean[] uniqueCaseIBoolean = new boolean[uniqueColumnsCount];
		for (int i = 0; i < uniqueColumnsCount; i++) {
			ascendingArray[i] = true;
			preSortedArray[i] = false;
			uniqueCaseIBoolean[i] = "0".equals(uniqueCaseI.split(",")[i]) ? false : true;
		}
		
		SortRowsMeta srm = new SortRowsMeta();
		srm.setFieldName(uniqueColumns.split(","));
		srm.setAscending(ascendingArray);
		srm.setCaseSensitive(uniqueCaseIBoolean);
		srm.setPrefix("SortRowsOut");
		srm.setDirectory(".");
		srm.setPreSortedField(preSortedArray);

		// Set the information of the sort.
		String sortRowsStepPid = registry.getPluginId(StepPluginType.class, srm);
		StepMeta sortRowsStep = new StepMeta(sortRowsStepPid, sortRowsStepname, (StepMetaInterface) srm);

		return sortRowsStep;
	}

	/**
	 * 在“转换”中去除重复记录
	 * 
	 * @author xuxiangfu
	 * @param transMeta
	 * @return
	 */
	public StepMeta addUniqueRows(String sourceTableName, DcUniqueRows dcUniqueRows) {
		String uniqueColumns = dcUniqueRows.getUniqueRows();
		String uniqueCaseI = dcUniqueRows.getIgnoreCase();
		/**
		 * Create a unique rows step
		 */
		String uniqueRowsStepname = sourceTableName + "_unique";
		UniqueRowsMeta uniqueRM = new UniqueRowsMeta();
		// 去重的字段
		uniqueRM.setCompareFields(uniqueColumns.split(","));

		int uniqueCaseICount = uniqueCaseI.split(",").length;
		boolean[] uniqueCaseIBoolean = new boolean[uniqueCaseICount];
		for (int i = 0; i < uniqueCaseICount; i++) {
			//是否忽略大小写（0/true：忽略，即 不区分大小写）
			uniqueCaseIBoolean[i] = "0".equals(uniqueCaseI.split(",")[i]) ? true : false;
		}

		// 是否区分大小写
		uniqueRM.setCaseInsensitive(uniqueCaseIBoolean);

		String uniqueRowsStepPid = registry.getPluginId(StepPluginType.class, uniqueRM);
		StepMeta uniqueRowsStep = new StepMeta(uniqueRowsStepPid, uniqueRowsStepname, (StepMetaInterface) uniqueRM);

		return uniqueRowsStep;
	}


	/**
	 * @Description:字符串替换组件
	 * @date:2016年09月28日
	 * @author:孟山峰
	 * @param sourceTableName
	 * @param dcReplaceStringList
	 * @return StepMeta
	 */
	public StepMeta addStringReplace(String sourceTableName, List<DcReplaceString> dcReplaceStringList) {
		
		StepMeta stepMeta = new StepMeta();
		ReplaceStringMeta replaceStringMeta = new ReplaceStringMeta();
		
		// 替换字段数
		int cnt = dcReplaceStringList.size();
		replaceStringMeta.allocate(cnt);
		
		for(int i = 0; i < cnt; i++){
			// 操作的列名
			replaceStringMeta.getFieldInStream()[i] = dcReplaceStringList.get(i).getOperateColumns();
			// 旧字符
			replaceStringMeta.getReplaceString()[i] = dcReplaceStringList.get(i).getReplaceStrings();
			// 新字符
			replaceStringMeta.getReplaceByString()[i] = dcReplaceStringList.get(i).getReplaceByStrings();	
			// 是否使用正则表达式
			replaceStringMeta.getUseRegEx()[i] = Integer.parseInt(dcReplaceStringList.get(i).getUseRegExOrNot());
		}

		String StepPid = registry.getPluginId(StepPluginType.class, replaceStringMeta);
		stepMeta = new StepMeta(StepPid, sourceTableName + "_ReplaceString", (StepMetaInterface) replaceStringMeta);
		
		return stepMeta;
	}

	
	/**
	 * 字符串剪切
	 * 2016/07/29
	 * 
	 * @author xuxiangfu
	 * @return StepMeta
	 */
	public StepMeta addStringCut(String sourceTableName, DcStringCut dcStringCut) {
		StepMeta stepMeta = new StepMeta();
		StringCutMeta stringCutM = new StringCutMeta();
		String Stepname = sourceTableName + "_StringCut";
		
		String operateColumns = dcStringCut.getOperateColumns();
		String cutFroms = dcStringCut.getCutFroms();
		String cutTos = dcStringCut.getCutTos();

		String keyStream[] = operateColumns.split(",");
		String cutFrom[] = cutFroms.split(",");
		String cutTo[] = cutTos.split(",");
		
		stringCutM.setFieldInStream(keyStream);
		stringCutM.setFieldOutStream(keyStream);
		stringCutM.setCutFrom(cutFrom);
		stringCutM.setCutTo(cutTo);
		
		String StepPid = registry.getPluginId(StepPluginType.class, stringCutM);
		stepMeta = new StepMeta(StepPid, Stepname, (StepMetaInterface) stringCutM);

		return stepMeta;
	}

	/**
	 * 字符串操作（包括去空格、大小写转换等）
	 * 2016/07/29
	 * 
	 * @author xuxiangfu
	 * @return StepMeta
	 */
	public StepMeta addStringOperations(String sourceTableName, DcStringOperations dcStringOps) {
		StepMeta stepMeta = new StepMeta();
		StringOperationsMeta stringOM = new StringOperationsMeta();
		String Stepname = sourceTableName + "_StringOperations";
		String operateColumns = dcStringOps.getOperateColumns();
		String trimTypes = dcStringOps.getTrimTypes();
		String lowerUppers = dcStringOps.getLowerUppers();
		
		String keyStream[] = operateColumns.split(",");
		int operateLength = keyStream.length;
		stringOM.allocate(operateLength);// 初始化 added by mengshanfeng20160815
		// 去空类型：none/left/right/both
		String trimTypeString[] = trimTypes.split(",");
		int[] trimTypeInt = new int[operateLength];

		// 大小写转换：none/lower/upper
		String[] lowerUpperString = lowerUppers.split(",");
		int[] lowerUpperint = new int[operateLength];
		
		for(int idx=0; idx < operateLength; idx++){
			if(!"".equals(trimTypeString[idx]) && trimTypeString[idx] != null){
				trimTypeInt[idx] = Integer.parseInt(trimTypeString[idx]);
			}
			
			if(!"".equals(lowerUpperString[idx]) && lowerUpperString[idx] != null){
				lowerUpperint[idx] = Integer.parseInt(lowerUpperString[idx]);
			}
		}
		
		stringOM.setFieldInStream(keyStream);
		stringOM.setTrimType(trimTypeInt);
		stringOM.setLowerUpper(lowerUpperint);
		// padding 填充(填充位置，填充字符，填充长度)
		// initcap 首字母大写
		// escape 对字符进行编码（%xx格式）
		// digits 
		// remove special character 删除特殊字符
		
		String StepPid = registry.getPluginId(StepPluginType.class, stringOM);
		stepMeta = new StepMeta(StepPid, Stepname, (StepMetaInterface) stringOM);

		return stepMeta;
	}
	
	/**
	 * @Description:计算器组件
	 * @date:2016年07月28日
	 * @author:孟山峰
	 * @param transMeta
	 * @param list
	 * @return
	 */
	public StepMeta calculatorMeta(String sourceTableName,List<CalculatorMetaFunction> list) {

		CalculatorMeta calculatorMeta = new CalculatorMeta();
		String calculatorMetaPluginId = registry.getPluginId(StepPluginType.class, calculatorMeta);			
		
		calculatorMeta.setCalculation((CalculatorMetaFunction[]) list.toArray(new CalculatorMetaFunction[list.size()]));
		StepMeta calculatorMetaStep = new StepMeta(calculatorMetaPluginId,sourceTableName+"_cal", calculatorMeta);

		return calculatorMetaStep;
	}
	
	/**
	 * @Description:字符串拼接组件
	 * @date:2016年08月15日
	 * @author:孟山峰
	 * @param sourceTableName
	 * @param dcConcatFields
	 * @return
	 */
	public StepMeta getConcatFieldsMeta(String sourceTableName,DcConcatFields dcConcatFields) {

		ConcatFieldsMeta concatFieldsMeta = new ConcatFieldsMeta();
		String concatFieldsMetaPluginId = registry.getPluginId(StepPluginType.class, concatFieldsMeta);
		concatFieldsMeta.setDefault();//初始化
		concatFieldsMeta.setTargetFieldName(dcConcatFields.getTargetFieldName());
		concatFieldsMeta.setSeparator(dcConcatFields.getSeparator());
		//设置需要拼接的字段
		DcTextFileField[] dcTextFileFields = dcConcatFields.getDcTextFileField();
		TextFileField[] textFileFields = new TextFileField[dcTextFileFields.length];
		for(int i=0;i<dcTextFileFields.length;i++){
			TextFileField textFileField = new TextFileField();
			textFileField.setName(dcTextFileFields[i].getName());
			textFileField.setType(dcTextFileFields[i].getType());
			textFileField.setTrimType(dcTextFileFields[i].getTrimType());
			textFileFields[i]=textFileField;
		}
		concatFieldsMeta.setOutputFields(textFileFields);
		StepMeta concatFieldsMetaStep = new StepMeta(concatFieldsMetaPluginId,sourceTableName+"_concat", concatFieldsMeta);
		
		return concatFieldsMetaStep;
	}
	
	/**
	 * 新数据生成设置
	 * 
	 * @param resource
	 * @param sourceTable
	 * @param targetTable
	 * @param registry
	 * @return
	 */
	public StepMeta newTableOutput(TransMeta transMeta, String targetResourceName, 
			Collection<DataColumnDefine> sourceColumns, Collection<DataColumnDefine> targetColumns,
			String targetTableName ,String incr) {
		TableOutputMeta tableoutput = new TableOutputMeta();
		String tableOutputPluginId = registry.getPluginId(StepPluginType.class, tableoutput);
		DatabaseMeta database_target = transMeta.findDatabase(targetResourceName);
		tableoutput.setDatabaseMeta(database_target);
		tableoutput.setTableName(targetTableName);
		tableoutput.setSpecifyFields(true);
		tableoutput.setCommitSize(50000);
		// 若为全量，需要添加清空目标表属性
		if (incr != null && !incr.equals("1")) {
			tableoutput.setTruncateTable(true);
		}

		int sourceLength = sourceColumns.size();
		String[] columnSource = new String[sourceLength];
		for (int i = 0; i < sourceLength; i++) {
			columnSource[i] = ((DataColumnDefine) sourceColumns.toArray()[i]).getColumnName();
		}
		int targetLength = targetColumns.size();
		String[] columnTarget = new String[targetLength];
		for (int i = 0; i < targetLength; i++) {
			columnTarget[i] = ((DataColumnDefine) targetColumns.toArray()[i]).getColumnName();
		}
		tableoutput.setFieldDatabase(columnTarget);
		tableoutput.setFieldStream(columnSource);
		StepMeta tableOutputMetaStep = new StepMeta(tableOutputPluginId, targetTableName + "_out",
				tableoutput);

		return tableOutputMetaStep;
	}
	/**
	 * @Description:过滤数据归档表
	 * @date:2016年08月18日
	 * @author:孟山峰 
	 * @param transMeta
	 * @param filterResourceName
	 * @param sourceColumns
	 * @param filterTargetColumns
	 * @param filterTargetTableName
	 * @return
	 */
	public StepMeta newFilterOutput(TransMeta transMeta, String filterResourceName, 
			Collection<DataColumnDefine> sourceColumns, Collection<DataColumnDefine> filterTargetColumns,
			String filterTargetTableName) {
		TableOutputMeta tableoutput = new TableOutputMeta();
		String tableOutputPluginId = registry.getPluginId(StepPluginType.class, tableoutput);
		DatabaseMeta database_target = transMeta.findDatabase(filterResourceName);
		tableoutput.setDatabaseMeta(database_target);
		tableoutput.setTableName(filterTargetTableName);
		tableoutput.setSpecifyFields(true);
		tableoutput.setCommitSize(50000);

		int sourceLength = sourceColumns.size();
		String[] columnSource = new String[sourceLength];
		for (int i = 0; i < sourceLength; i++) {
			columnSource[i] = ((DataColumnDefine) sourceColumns.toArray()[i]).getColumnName();
		}
		int targetLength = filterTargetColumns.size();
		String[] columnTarget = new String[targetLength];
		for (int i = 0; i < targetLength; i++) {
			columnTarget[i] = ((DataColumnDefine) filterTargetColumns.toArray()[i]).getColumnName();
		}
		tableoutput.setFieldDatabase(columnTarget);
		tableoutput.setFieldStream(columnSource);
		StepMeta tableOutputMetaStep = new StepMeta(tableOutputPluginId, filterTargetTableName + "_filterOut",
				tableoutput);

		return tableOutputMetaStep;
	}
	
	/**
	 * @Description:空操作组件
	 * @date:2016年08月30日
	 * @author:孟山峰 
	 * @return
	 */
	public StepMeta dummyStep() {
		DummyTransMeta dummyTransMeta = new DummyTransMeta();
		String dummyTransMetaPluginId = registry.getPluginId(StepPluginType.class, dummyTransMeta);
		StepMeta dummyTransMetaStep = new StepMeta(dummyTransMetaPluginId, "dummy", dummyTransMeta);

		return dummyTransMetaStep;
	}	
	/**
	 * @Description:获取系统信息组件
	 * @date:2016年10月28日
	 * @author: 盛泽欢
	 * @return
	 */
	public StepMeta addGetSystemData() {
		SystemDataMeta systemDataMeta = new SystemDataMeta();
		String systemDataMetaPluginId = registry.getPluginId(StepPluginType.class, systemDataMeta);
		StepMeta systemDataMetaStep = new StepMeta(systemDataMetaPluginId, "systemData", systemDataMeta);
		systemDataMeta.setDefault();
		String[] fieldNames = {"IdBatch","execTime"};
		systemDataMeta.setFieldName(fieldNames);
		int[] fieldTypes={SystemDataMeta.TYPE_SYSTEM_INFO_TRANS_BATCH_ID,SystemDataMeta.TYPE_SYSTEM_INFO_SYSTEM_DATE};
		systemDataMeta.setFieldType(fieldTypes);
		return systemDataMetaStep;
	}
}