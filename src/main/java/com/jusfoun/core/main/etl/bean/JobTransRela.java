package com.jusfoun.core.main.etl.bean;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;

@Entity(name = "job_trans_rela")
public class JobTransRela {
	@Id
    @Column(name="id",updatable=false)
    @GeneratedValue(strategy=GenerationType.SEQUENCE,generator="job_trans_rela_seq")
    @SequenceGenerator(name="job_trans_rela_seq",sequenceName="job_trans_rela_seq",allocationSize=1)
	private Integer id;
	
	@Column(name="path")
	private String path;
	
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	@Column(name="job_name")
	private String jobName;
	
	@Column(name="trans_name")
	private String transName;
	
	@Column(name="description")
	private String description;
	
	@Column(name="create_time")
	private Date create_time;

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getJobName() {
		return jobName;
	}

	public void setJobName(String jobName) {
		this.jobName = jobName;
	}

	public String getTransName() {
		return transName;
	}

	public void setTransName(String transName) {
		this.transName = transName;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Date getCreate_time() {
		return create_time;
	}

	public void setCreate_time(Date create_time) {
		this.create_time = create_time;
	}
}
