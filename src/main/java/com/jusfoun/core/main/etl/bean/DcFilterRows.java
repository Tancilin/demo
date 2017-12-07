package com.jusfoun.core.main.etl.bean;

public class DcFilterRows {
	private String id;
	private String parentId;
	private boolean negated;
	private int operator;
	private String leftName;
	private String conditionFunction;
	private String rightName;
	private String filterValue;
	
	public String getRightName() {
		return rightName;
	}

	public void setRightName(String rightName) {
		this.rightName = rightName;
	}

	public boolean getNegated() {
		return negated;
	}

	public void setNegated(boolean negated) {
		this.negated = negated;
	}

	public int getOperator() {
		return operator;
	}

	public void setOperator(int operator) {
		this.operator = operator;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getParentId() {
		return parentId;
	}

	public void setParentId(String parentId) {
		this.parentId = parentId;
	}

	public String getLeftName() {
		return leftName;
	}

	public void setLeftName(String leftName) {
		this.leftName = leftName;
	}

	public String getConditionFunction() {
		return conditionFunction;
	}

	public void setConditionFunction(String conditionFunction) {
		this.conditionFunction = conditionFunction;
	}

	public String getFilterValue() {
		return filterValue;
	}

	public void setFilterValue(String filterValue) {
		this.filterValue = filterValue;
	}

}
