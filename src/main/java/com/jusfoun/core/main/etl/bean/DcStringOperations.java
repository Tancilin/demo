package com.jusfoun.core.main.etl.bean;

/**
 * 字符串操作（包括去空格、大小写转换等）
 * 
 * @author xuxiangfu
 * @date 20160803
 */
public class DcStringOperations {
	// 操作列
	private String operateColumns;
	
	// 去空格的类型：none/left/right/both
	private String trimTypes;

	// 大小写转换：none/lower/upper
	private String lowerUppers;

	public String getOperateColumns() {
		return operateColumns;
	}

	public void setOperateColumns(String operateColumns) {
		this.operateColumns = operateColumns;
	}

	public String getTrimTypes() {
		return trimTypes;
	}

	public void setTrimTypes(String trimTypes) {
		this.trimTypes = trimTypes;
	}

	public String getLowerUppers() {
		return lowerUppers;
	}

	public void setLowerUppers(String lowerUppers) {
		this.lowerUppers = lowerUppers;
	}
	
}
