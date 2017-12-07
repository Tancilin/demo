package com.jusfoun.core.main.etl.bean;


public class DataColumnDefine {

	private Integer columnId;

	private Integer tableId;
	
	private Integer resourceId;

	private String columnName;

	private String columnType;

	private Integer columnSize;

	private Integer columnPrecision;

	private String columnDesc;

	private String columnCnName;
	
	

	public DataColumnDefine() {
		super();
	}

	public DataColumnDefine(String columnName) {
		super();
		this.columnName = columnName;
	}

	public Integer getColumnId() {
		return columnId;
	}

	public void setColumnId(Integer columnId) {
		this.columnId = columnId;
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

	public String getColumnName() {
		return columnName;
	}

	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}

	public String getColumnType() {
		return columnType;
	}

	public void setColumnType(String columnType) {
		this.columnType = columnType;
	}

	public Integer getColumnSize() {
		return columnSize;
	}

	public void setColumnSize(Integer columnSize) {
		this.columnSize = columnSize;
	}

	public Integer getColumnPrecision() {
		return columnPrecision;
	}

	public void setColumnPrecision(Integer columnPrecision) {
		this.columnPrecision = columnPrecision;
	}

	public String getColumnDesc() {
		return columnDesc;
	}

	public void setColumnDesc(String columnDesc) {
		this.columnDesc = columnDesc;
	}

	public String getColumnCnName() {
		return columnCnName;
	}

	public void setColumnCnName(String columnCnName) {
		this.columnCnName = columnCnName;
	}

}
