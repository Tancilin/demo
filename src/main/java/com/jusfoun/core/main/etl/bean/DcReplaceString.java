package com.jusfoun.core.main.etl.bean;

/**
 * 字符串替换
 * 
 * @author mengshanfneg
 * @date 20161012
 */
public class DcReplaceString {
	
	// 操作的列，用逗号分隔
	private String operateColumns;
	
	// 替换前的子字符串
	private String replaceStrings;
	
	// 替换后的新字符串
	private String replaceByStrings;
	
	// 是否大小写敏感
	private String caseSensitive;
	
	// 是否设为空字符串
	private String emptyString;

	// 是否使用正则表达式
	private String UseRegExOrNot;
		
	public String getOperateColumns() {
		return operateColumns;
	}

	public void setOperateColumns(String operateColumns) {
		this.operateColumns = operateColumns;
	}

	public String getReplaceStrings() {
		return replaceStrings;
	}

	public void setReplaceStrings(String replaceStrings) {
		this.replaceStrings = replaceStrings;
	}

	public String getReplaceByStrings() {
		return replaceByStrings;
	}

	public void setReplaceByStrings(String replaceByStrings) {
		this.replaceByStrings = replaceByStrings;
	}

	public String getUseRegExOrNot() {
		return UseRegExOrNot;
	}

	public void setUseRegExOrNot(String useRegExOrNot) {
		UseRegExOrNot = useRegExOrNot;
	}

	public String getCaseSensitive() {
		return caseSensitive;
	}

	public void setCaseSensitive(String caseSensitive) {
		this.caseSensitive = caseSensitive;
	}

	public String getEmptyString() {
		return emptyString;
	}

	public void setEmptyString(String emptyString) {
		this.emptyString = emptyString;
	}
	
}
