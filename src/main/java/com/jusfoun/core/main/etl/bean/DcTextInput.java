package com.jusfoun.core.main.etl.bean;

/**
 * text文件数据源
 * 
 * @author xuxiangfu
 * @date 20160803
 */
public class DcTextInput {
	// 文件名
	private String fileNames;
	//文本数据源类型 added by mengshanfeng 20160808
	private String inputStyle;
	// 头部行数量
	private int nrHeaderLines;
	
	// 分隔符
	private String separator;
	
	// 列名，多列用逗号分隔
	private String columnNames;
	
	/*
	 *  列的数据类型，用逗号分隔
	 * 		1:ValueMetaInterface.TYPE_NUMBER
	 * 		2:ValueMetaInterface.TYPE_STRING
	 * 		5:ValueMetaInterface.TYPE_INTEGER
	 */
	private String columnTypes;
	
	// 浮点型长度，多列用逗号分隔
	private String columnLengths;
	
	// 浮点型精度，多列用逗号分隔
	private String columnPrecisions;
	
	public String getFileNames() {
		return fileNames;
	}
	public void setFileNames(String fileNames) {
		this.fileNames = fileNames;
	}
	
	public String getInputStyle() {
		return inputStyle;
	}
	public void setInputStyle(String inputStyle) {
		this.inputStyle = inputStyle;
	}
	public int getNrHeaderLines() {
		return nrHeaderLines;
	}
	public void setNrHeaderLines(int nrHeaderLines) {
		this.nrHeaderLines = nrHeaderLines;
	}
	public String getSeparator() {
		return separator;
	}
	public void setSeparator(String separator) {
		this.separator = separator;
	}
	public String getColumnNames() {
		return columnNames;
	}
	public void setColumnNames(String columnNames) {
		this.columnNames = columnNames;
	}
	public String getColumnTypes() {
		return columnTypes;
	}
	public void setColumnTypes(String columnTypes) {
		this.columnTypes = columnTypes;
	}
	public String getColumnLengths() {
		return columnLengths;
	}
	public void setColumnLengths(String columnLengths) {
		this.columnLengths = columnLengths;
	}
	public String getColumnPrecisions() {
		return columnPrecisions;
	}
	public void setColumnPrecisions(String columnPrecisions) {
		this.columnPrecisions = columnPrecisions;
	}

}
