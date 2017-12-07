package com.jusfoun.core.main.etl.bean;
import java.io.Serializable;
import java.util.Date;
import javax.persistence.*;
/**
*JOBCHAN日志表 
*	@AUTO
*/
@IdClass(JobchanLog.PK.class)
@Entity(name = "jobchan_log")
	@NamedStoredProcedureQuery(name = "p_bakuplog", procedureName = "p_bakuplog", parameters = {
			@StoredProcedureParameter(mode = ParameterMode.INOUT, name = "flag", type = String.class)
	})
public class JobchanLog {

	// 作业批次id
    @Id
    @Column(name="id_batch",updatable=false)
    private Long idBatch;
    
	// 日志频道id
    @Id
    @Column(name="channel_id")
    private String channelId;

	// 父jobid
    @Column(name="log_date")
    private Date logDate;

	// job名称
    @Column(name="logging_object_type")
    private String loggingObjectType;

	// 读取行数
    @Column(name="object_name")
    private String objectName;

	// 写入行数
    @Column(name="object_copy")
    private String objectCopy;

	// 更新行数
    @Column(name="repository_directory")
    private String repositoryDirectory;

	// 输入行数
    @Column(name="filename")
    private String filename;

	// 输出行数
    @Column(name="object_id")
    private String objectId;

	// 拒绝行数
    @Column(name="object_revision")
    private String objectRevision;

	// 错误
    @Column(name="parent_channel_id")
    private String parentChannelId;

	// 结果集
    @Column(name="root_channel_id")
    private String rootChannelId;


 

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

    public String getLoggingObjectType() {
        return loggingObjectType;
    }

    public void setLoggingObjectType(String loggingObjectType) {
        this.loggingObjectType = loggingObjectType;
    }

    public String getObjectName() {
        return objectName;
    }

    public void setObjectName(String objectName) {
        this.objectName = objectName;
    }

    public String getObjectCopy() {
        return objectCopy;
    }

    public void setObjectCopy(String objectCopy) {
        this.objectCopy = objectCopy;
    }

    public String getRepositoryDirectory() {
        return repositoryDirectory;
    }

    public void setRepositoryDirectory(String repositoryDirectory) {
        this.repositoryDirectory = repositoryDirectory;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    public String getObjectRevision() {
        return objectRevision;
    }

    public void setObjectRevision(String objectRevision) {
        this.objectRevision = objectRevision;
    }

    public String getParentChannelId() {
        return parentChannelId;
    }

    public void setParentChannelId(String parentChannelId) {
        this.parentChannelId = parentChannelId;
    }

    public String getRootChannelId() {
        return rootChannelId;
    }

    public void setRootChannelId(String rootChannelId) {
        this.rootChannelId = rootChannelId;
    }

    
    @Embeddable
    public static class PK implements Serializable{
		
        private static final long serialVersionUID = 5896205982831169395L;
		
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
            return idBatch==((JobchanLog.PK) obj).idBatch && channelId.equals(((JobchanLog.PK) obj).channelId);
        }
		
		@Override
        public int hashCode() {
            return (idBatch.toString() + channelId).hashCode();
        }
    }
 
}