package com.jusfoun.core.main.etl.bean;

public class DataResource {
	private Integer resourceId;

	private String resourceName;

	private Integer resourceTypeId;

	private String resourceAddr;

	private Integer port;

	private String databaseName;

	private String acount;

	private String pasword;

	private String resourceDesc;

	private DataResourceType dcResourceType;

	public Integer getResourceId() {
		return resourceId;
	}

	public void setResourceId(Integer resourceId) {
		this.resourceId = resourceId;
	}

	public String getResourceName() {
		return resourceName;
	}

	public void setResourceName(String resourceName) {
		this.resourceName = resourceName;
	}

	public Integer getResourceTypeId() {
		return resourceTypeId;
	}

	public void setResourceTypeId(Integer resourceTypeId) {
		this.resourceTypeId = resourceTypeId;
	}

	public String getResourceAddr() {
		return resourceAddr;
	}

	public void setResourceAddr(String resourceAddr) {
		this.resourceAddr = resourceAddr;
	}

	public Integer getPort() {
		return port;
	}

	public void setPort(Integer port) {
		this.port = port;
	}

	public String getDatabaseName() {
		return databaseName;
	}

	public void setDatabaseName(String databaseName) {
		this.databaseName = databaseName;
	}

	public String getAcount() {
		return acount;
	}

	public void setAcount(String acount) {
		this.acount = acount;
	}

	public String getPasword() {
		return pasword;
	}

	public void setPasword(String pasword) {
		this.pasword = pasword;
	}

	public String getResourceDesc() {
		return resourceDesc;
	}

	public void setResourceDesc(String resourceDesc) {
		this.resourceDesc = resourceDesc;
	}

	public DataResourceType getDcResourceType() {
		return dcResourceType;
	}

	public void setDcResourceType(DataResourceType dcResourceType) {
		this.dcResourceType = dcResourceType;
	}

}
