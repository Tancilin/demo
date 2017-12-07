package com.jusfoun.core.main.etl.bean;
import java.io.Serializable;
import java.util.Date;
import javax.persistence.*;
/**
*ENTRY日志表 
*	@AUTO
*/
@IdClass(EntryLog.PK.class)
@Entity(name = "entry_log")
public class EntryLog {

	// 作业批次id
    @Id
    @Column(name="id_batch",updatable=false)
    private Long idBatch;

	// 日志频道id
    @Id
    @Column(name="channel_id")
    private String channelId;

	// 父jobid
    @Column(name="transname")
    private String transname;

	// job名称
    @Column(name="stepname")
    private String stepname;

	// 读取行数
    @Column(name="lines_read")
    private Integer linesRead;

	// 写入行数
    @Column(name="lines_written")
    private Integer linesWritten;

	// 更新行数
    @Column(name="lines_updated")
    private Integer linesUpdated;

	// 输入行数
    @Column(name="lines_input")
    private Integer linesInput;

	// 输出行数
    @Column(name="lines_output")
    private Integer linesOutput;

	// 拒绝行数
    @Column(name="lines_rejected")
    private Integer linesRejected;

	// 错误
    @Column(name="errors")
    private Integer errors;

	// 结果集
    @Column(name="result")
    private String result;

	// 执行结果函数
    @Column(name="nr_result_rows")
    private Integer nrResultRows;

	// 结果执行后文件
    @Column(name="nr_result_files")
    private Integer nrResultFiles;

	// 日志日期
    @Column(name="log_date")
    private Date logDate;


 

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

    public String getStepname() {
        return stepname;
    }

    public void setStepname(String stepname) {
        this.stepname = stepname;
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

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public Integer getNrResultRows() {
        return nrResultRows;
    }

    public void setNrResultRows(Integer nrResultRows) {
        this.nrResultRows = nrResultRows;
    }

    public Integer getNrResultFiles() {
        return nrResultFiles;
    }

    public void setNrResultFiles(Integer nrResultFiles) {
        this.nrResultFiles = nrResultFiles;
    }

    public Date getLogDate() {
        return logDate;
    }

    public void setLogDate(Date logDate) {
        this.logDate = logDate;
    }
    @Embeddable
    public static class PK implements Serializable{
		
        private static final long serialVersionUID = 5896205982831269315L;
		
        @Column(name="id_batch",updatable=false)
        private Long idBatch;
		
        @Column(name="channel_id",updatable=false)
        private String channelId;
	
        public PK(){
             //Entity默认构造函数
        }
		
        public PK(Long idBatch,String channelId ){
            this.idBatch = idBatch;
            this.channelId = channelId;
        }
        
        @Override
        public boolean equals(Object obj) {
            if (obj == null)
                return false;
            if (this.getClass() != obj.getClass())
                return false;
            return idBatch==((EntryLog.PK) obj).idBatch && channelId.equals(((EntryLog.PK) obj).channelId);
        }
		
		@Override
        public int hashCode() {
            return (idBatch.toString() + channelId).hashCode();
        }
    }
 
 
}