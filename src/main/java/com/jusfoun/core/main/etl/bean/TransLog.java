package com.jusfoun.core.main.etl.bean;
import javax.persistence.Entity;
import java.util.Date;
import javax.persistence.*;
/**
*TRANS日志表 
*	@AUTO
*/
@Entity(name = "trans_log")
public class TransLog {

	// 转换id自增
    @Column(name="id_batch",updatable=false)
    private Long idBatch;

	// 转换频道id
    @Id
    @Column(name="channel_id")
    private String channelId;

	// 转换名称
    @Column(name="transname")
    private String transname;

	// 转换状态启动、停止
    @Column(name="status")
    private String status;

	// 按行读取转换内容
    @Column(name="lines_read")
    private Integer linesRead;

	// 按行写入转换内容
    @Column(name="lines_written")
    private Integer linesWritten;

	// 按行更新转换内容
    @Column(name="lines_updated")
    private Integer linesUpdated;

	// 按行输入转换内容
    @Column(name="lines_input")
    private Integer linesInput;

	// 按行输出转换内容
    @Column(name="lines_output")
    private Integer linesOutput;

	// 错误拒绝行数
    @Column(name="lines_rejected")
    private Integer linesRejected;

	// 错误行数
    @Column(name="errors")
    private Integer errors;

	// 转换开始日期
    @Column(name="startdate")
    private Date startdate;

	// 转换结束日期
    @Column(name="enddate")
    private Date enddate;

	// log日期
    @Column(name="logdate")
    private Date logdate;

	// 依赖日期
    @Column(name="depdate")
    private Date depdate;

	// 重做日期
    @Column(name="replaydate")
    private Date replaydate;


    // 错误的详细信息
    @Column(name="log_field")
    private String logfield;

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

    public String getTransname() {
        return transname;
    }

    public void setTransname(String transname) {
        this.transname = transname;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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

    public Date getStartdate() {
        return startdate;
    }

    public void setStartdate(Date startdate) {
        this.startdate = startdate;
    }

    public Date getEnddate() {
        return enddate;
    }

    public void setEnddate(Date enddate) {
        this.enddate = enddate;
    }

    public Date getLogdate() {
        return logdate;
    }

    public void setLogdate(Date logdate) {
        this.logdate = logdate;
    }

    public Date getDepdate() {
        return depdate;
    }

    public void setDepdate(Date depdate) {
        this.depdate = depdate;
    }

    public Date getReplaydate() {
        return replaydate;
    }

    public void setReplaydate(Date replaydate) {
        this.replaydate = replaydate;
    }

    public String getLogfield() {
		return logfield;
	}

	public void setLogfield(String logfield) {
		this.logfield = logfield;
	}
 
}