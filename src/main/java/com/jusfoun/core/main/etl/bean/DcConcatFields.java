package com.jusfoun.core.main.etl.bean;

public class DcConcatFields {
	private String targetFieldName;
	private String separator;
	private DcTextFileField[] dcTextFileField;
	
	public String getTargetFieldName() {
		return targetFieldName;
	}
	public void setTargetFieldName(String targetFieldName) {
		this.targetFieldName = targetFieldName;
	}
	public String getSeparator() {
		return separator;
	}
	public void setSeparator(String separator) {
		this.separator = separator;
	}
	public DcTextFileField[] getDcTextFileField() {
		return dcTextFileField;
	}
	public void setDcTextFileField(DcTextFileField[] dcTextFileField) {
		this.dcTextFileField = dcTextFileField;
	}

	
}
