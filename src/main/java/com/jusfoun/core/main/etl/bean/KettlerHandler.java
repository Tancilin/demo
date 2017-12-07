package com.jusfoun.core.main.etl.bean;

import java.util.Collection;
import java.util.Date;

import org.apache.log4j.Logger;
/**
 * 将DC类转换成util中使用的类型
 * 
 * @author 付永波
 *
 */
public class KettlerHandler {

	private static final Logger LOGGER = Logger.getLogger(KettlerHandler.class);

	/**
	 * DataResource转换
	 * 
	 * @param obj
	 * @return
	 */
	public static DataResource getDataResource(Object obj) {
		DataResource dr = new DataResource();
		Class<? extends Object> objClass = obj.getClass();
		try {
			dr.setAcount((String) objClass.getDeclaredMethod("getAcount").invoke(obj));
			dr.setDatabaseName((String) objClass.getDeclaredMethod("getDatabaseName").invoke(obj));
			dr.setPasword((String) objClass.getDeclaredMethod("getPasword").invoke(obj));
			dr.setPort((Integer) objClass.getDeclaredMethod("getPort").invoke(obj));
			dr.setResourceAddr((String) objClass.getDeclaredMethod("getResourceAddr").invoke(obj));
			dr.setResourceDesc((String) objClass.getDeclaredMethod("getResourceDesc").invoke(obj));
			dr.setResourceId((Integer) objClass.getDeclaredMethod("getResourceId").invoke(obj));
			dr.setResourceName((String) objClass.getDeclaredMethod("getResourceName").invoke(obj));
			dr.setResourceTypeId((Integer) objClass.getDeclaredMethod("getResourceTypeId").invoke(obj));
			dr.setDcResourceType((DataResourceType) objClass.getDeclaredMethod("getDcResourceType").invoke(obj));
		} catch (Exception e) {
			LOGGER.error("DataResource转换失败{}", e);
		}
		return dr;
	}

	/**
	 * DataColumnDefine转换
	 * 
	 * @param obj
	 * @return
	 */
	public static DataColumnDefine getDataColumnDefine(Object obj) {
		DataColumnDefine dcd = new DataColumnDefine();
		Class<? extends Object> objClass = obj.getClass();
		try {
			dcd.setColumnCnName((String) objClass.getDeclaredMethod("getColumnCnName").invoke(obj));
			dcd.setColumnDesc((String) objClass.getDeclaredMethod("getColumnDesc").invoke(obj));
			dcd.setColumnId((Integer) objClass.getDeclaredMethod("getColumnId").invoke(obj));
			dcd.setColumnName((String) objClass.getDeclaredMethod("getColumnName").invoke(obj));
			dcd.setColumnPrecision((Integer) objClass.getDeclaredMethod("getColumnPrecision").invoke(obj));
			dcd.setColumnSize((Integer) objClass.getDeclaredMethod("getColumnSize").invoke(obj));
			dcd.setColumnType((String) objClass.getDeclaredMethod("getColumnType").invoke(obj));
			dcd.setResourceId((Integer) objClass.getDeclaredMethod("getResourceId").invoke(obj));
			dcd.setTableId((Integer) objClass.getDeclaredMethod("getTableId").invoke(obj));
		} catch (Exception e) {
			LOGGER.error("DataColumnDefine转换失败{}", e);
		}
		return dcd;
	}

	/**
	 * DataResourceTable转换
	 * 
	 * @param obj
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static DataResourceTable getDataResourceTable(Object obj) {
		DataResourceTable drt = new DataResourceTable();
		Class<? extends Object> objClass = obj.getClass();
		try {
			drt.setFileSplit((String) objClass.getDeclaredMethod("getFileSplit").invoke(obj));
			drt.setOperateDate((Date) objClass.getDeclaredMethod("getOperateDate").invoke(obj));
			drt.setResourceId((Integer) objClass.getDeclaredMethod("getResourceId").invoke(obj));
			drt.setTableCnName((String) objClass.getDeclaredMethod("getTableCnName").invoke(obj));
			drt.setTableDesc((String) objClass.getDeclaredMethod("getTableDesc").invoke(obj));
			drt.setTableId((Integer) objClass.getDeclaredMethod("getTableId").invoke(obj));
			drt.setTableName((String) objClass.getDeclaredMethod("getTableName").invoke(obj));
			drt.setDccolumndefines(
					(Collection<DataColumnDefine>) objClass.getDeclaredMethod("getDccolumndefines").invoke(obj));
		} catch (Exception e) {
			LOGGER.error("DataResourceTable转换失败{}", e);
		}
		return drt;
	}

	/**
	 * DataResourceType转化
	 * 
	 * @param obj
	 * @return
	 */
	public static DataResourceType getDataResourceType(Object obj) {
		DataResourceType drt = new DataResourceType();
		Class<? extends Object> objClass = obj.getClass();
		try {
			drt.setDescription((String) objClass.getDeclaredMethod("getDescription").invoke(obj));
			drt.setParentId((Integer) objClass.getDeclaredMethod("getParentId").invoke(obj));
			drt.setResourceTypeId((Integer) objClass.getDeclaredMethod("getResourceTypeId").invoke(obj));
			drt.setTypeName((String) objClass.getDeclaredMethod("getTypeName").invoke(obj));
		} catch (Exception e) {
			LOGGER.error("DataResourceType转换失败{}", e);
		}
		return drt;
	}

}
