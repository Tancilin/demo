package com.jusfoun.core.main.etl.bean;
import javax.persistence.Entity;
import java.util.Date;
import javax.persistence.*;
/**
*JOB日志表 
*	@AUTO
*/
@Entity(name = "job_log")
public class JobLog {

	// 作业id每次自增（批次id）
    @Id
    @Column(name="id_job",updatable=false)
    private Long idJob;

	// log通道id
    @Column(name="channel_id")
    private String channelId;

	// job名字
    @Column(name="jobname")
    private String jobname;

	// job状态分为启动、停止
    @Column(name="status")
    private String status;

	// 转换中最后读取的行数
    @Column(name="lines_read")
    private Integer linesRead;

	// 转换中最后写入的行数
    @Column(name="lines_written")
    private Integer linesWritten;

	// 转换中最后跟新行数
    @Column(name="lines_updated")
    private Integer linesUpdated;

	// 转换中通过网络写入磁盘的行数
    @Column(name="lines_input")
    private Integer linesInput;

	// 转换中通过网络读入磁盘的行数
    @Column(name="lines_output")
    private Integer linesOutput;

	// 作业中由于错误拒绝的行数
    @Column(name="lines_rejected")
    private Integer linesRejected;

	// 错误行数
    @Column(name="errors")
    private Integer errors;

	// 增量开始日期
    @Column(name="startdate")
    private Date startdate;

	// 增量结束日期
    @Column(name="enddate")
    private Date enddate;

	// 日志记录更新时间
    @Column(name="logdate")
    private Date logdate;

	// 计算更新时间
    @Column(name="depdate")
    private Date depdate;

	// 计算重做时间
    @Column(name="replaydate")
    private Date replaydate;


    // 错误的详细信息
    @Column(name="log_field")
    private String logfield; 

    public Long getIdJob() {
        return idJob;
    }

    public void setIdJob(Long idJob) {
        this.idJob = idJob;
    }

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public String getJobname() {
        return jobname;
    }

    public void setJobname(String jobname) {
        this.jobname = jobname;
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