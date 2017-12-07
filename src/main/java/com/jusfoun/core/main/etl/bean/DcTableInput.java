package com.jusfoun.core.main.etl.bean;

public class DcTableInput {
	private String inputname;
	private String tableName;
	private String tableinputSql;
	private String incrementType;
	private String landOrNot;
	public String getInputname() {
		return inputname;
	}

	public void setInputname(String inputname) {
		this.inputname = inputname;
	}

	public String getTableName() {
		return tableName;
	}

	public String getLandOrNot() {
		return landOrNot;
	}

	public void setLandOrNot(String landOrNot) {
		this.landOrNot = landOrNot;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public String getTableinputSql() {
		return tableinputSql;
	}

	public void setTableinputSql(String tableinputSql) {
		this.tableinputSql = tableinputSql;
	}

	public String getIncrementType() {
		return incrementType;
	}

	public void setIncrementType(String incrementType) {
		this.incrementType = incrementType;
	}

}
