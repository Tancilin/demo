package com.jusfoun.core.main.etl.bean;

import java.util.Collection;

public class DcTableOutput {
	private String outputname;
	private String tableName;
	private Collection<DataColumnDefine> sourceColumndefines;
	private Collection<DataColumnDefine> targetColumndefines;

	public String getOutputname() {
		return outputname;
	}

	public void setOutputname(String outputname) {
		this.outputname = outputname;
	}

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public Collection<DataColumnDefine> getSourceColumndefines() {
		return sourceColumndefines;
	}

	public void setSourceColumndefines(Collection<DataColumnDefine> sourceColumndefines) {
		this.sourceColumndefines = sourceColumndefines;
	}

	public Collection<DataColumnDefine> getTargetColumndefines() {
		return targetColumndefines;
	}

	public void setTargetColumndefines(Collection<DataColumnDefine> targetColumndefines) {
		this.targetColumndefines = targetColumndefines;
	}

}
