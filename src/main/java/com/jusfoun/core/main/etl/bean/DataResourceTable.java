package com.jusfoun.core.main.etl.bean;

import java.util.Collection;
import java.util.Date;


public class DataResourceTable {
	private Integer tableId;

	private Integer resourceId;

	private String tableName;

	private String fileSplit;

	private String tableDesc;

	private Date operateDate;

	private String tableCnName;
	
	private Collection<DataColumnDefine> dccolumndefines;
	
	public Collection<DataColumnDefine> getDccolumndefines() {
		return dccolumndefines;
	}

	public void setDccolumndefines(Collection<DataColumnDefine> dccolumndefines) {
		this.dccolumndefines = dccolumndefines;
	}

	public Integer getTableId() {
		return tableId;
	}

	public void setTableId(Integer tableId) {
		this.tableId = tableId;
	}

	public Integer getResourceId() {
		return resourceId;
	}

	public void setResourceId(Integer resourceId) {
		this.resourceId = resourceId;
	}

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public String getFileSplit() {
		return fileSplit;
	}

	public void setFileSplit(String fileSplit) {
		this.fileSplit = fileSplit;
	}

	public String getTableDesc() {
		return tableDesc;
	}

	public void setTableDesc(String tableDesc) {
		this.tableDesc = tableDesc;
	}

	public Date getOperateDate() {
		return operateDate;
	}

	public void setOperateDate(Date operateDate) {
		this.operateDate = operateDate;
	}

	public String getTableCnName() {
		return tableCnName;
	}

	public void setTableCnName(String tableCnName) {
		this.tableCnName = tableCnName;
	}

}
