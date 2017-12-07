package com.jusfoun.core.main.etl.bean;

/**
 * 字符串剪切
 * 
 * @author xuxiangfu
 * @date 20160803
 */
public class DcStringCut {
	// 操作的列名
	private String operateColumns;
	
	// 截取字符的起始位置
	private String cutFroms;
	
	// 截取字符的截止位置
	private String cutTos;

	public String getOperateColumns() {
		return operateColumns;
	}

	public void setOperateColumns(String operateColumns) {
		this.operateColumns = operateColumns;
	}

	public String getCutFroms() {
		return cutFroms;
	}

	public void setCutFroms(String cutFroms) {
		this.cutFroms = cutFroms;
	}

	public String getCutTos() {
		return cutTos;
	}

	public void setCutTos(String cutTos) {
		this.cutTos = cutTos;
	}
	
}
