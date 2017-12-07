package com.jusfoun.core.main.etl.bean;
import javax.persistence.Entity;
import java.util.Date;
import javax.persistence.*;
/**
*STEP日志表 
*	@AUTO
*/
@Entity(name = "step_log")
public class StepLog {
	
	// STEP批次id
    @Column(name="id_batch")
    private Long idBatch;

    @Id
	// 日志评到id
    @Column(name="channel_id",updatable=false)
    private String channelId;

	// log输入日期
    @Column(name="log_date")
    private Date logDate;

	// 转换名称
    @Column(name="transname")
    private String transname;

	// 步骤名称
    @Column(name="stepname")
    private String stepname;

	// 步骤copy序号
    @Column(name="step_copy")
    private Integer stepCopy;

	// 按行读取步骤
    @Column(name="lines_read")
    private Integer linesRead;

	// 按行写入步骤
    @Column(name="lines_written")
    private Integer linesWritten;

	// 按行更新步骤
    @Column(name="lines_updated")
    private Integer linesUpdated;

	// 按行输入步骤
    @Column(name="lines_input")
    private Integer linesInput;

	// 按行输出步骤
    @Column(name="lines_output")
    private Integer linesOutput;

	// 错误拒绝行数
    @Column(name="lines_rejected")
    private Integer linesRejected;

	// 错误的数量
    @Column(name="errors")
    private Integer errors;


	public Long getIdBatch() {
        return idBatch;
    }

    public void setIdBatch(Long idBatch) {
        this.idBatch = idBatch;
    }

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public Date getLogDate() {
        return logDate;
    }

    public void setLogDate(Date logDate) {
        this.logDate = logDate;
    }

    public String getTransname() {
        return transname;
    }

    public void setTransname(String transname) {
        this.transname = transname;
    }

    public String getStepname() {
        return stepname;
    }

    public void setStepname(String stepname) {
        this.stepname = stepname;
    }

    public Integer getStepCopy() {
        return stepCopy;
    }

    public void setStepCopy(Integer stepCopy) {
        this.stepCopy = stepCopy;
    }

    public Integer getLinesRead() {
        return linesRead;
    }

    public void setLinesRead(Integer linesRead) {
        this.linesRead = linesRead;
    }

    public Integer getLinesWritten() {
        return linesWritten;
    }

    public void setLinesWritten(Integer linesWritten) {
        this.linesWritten = linesWritten;
    }

    public Integer getLinesUpdated() {
        return linesUpdated;
    }

    public void setLinesUpdated(Integer linesUpdated) {
        this.linesUpdated = linesUpdated;
    }

    public Integer getLinesInput() {
        return linesInput;
    }

    public void setLinesInput(Integer linesInput) {
        this.linesInput = linesInput;
    }

    public Integer getLinesOutput() {
        return linesOutput;
    }

    public void setLinesOutput(Integer linesOutput) {
        this.linesOutput = linesOutput;
    }

    public Integer getLinesRejected() {
        return linesRejected;
    }

    public void setLinesRejected(Integer linesRejected) {
        this.linesRejected = linesRejected;
    }

    public Integer getErrors() {
        return errors;
    }

    public void setErrors(Integer errors) {
        this.errors = errors;
    }

    public String toString(){
    	return "channelId:" + channelId;
    }
 
}