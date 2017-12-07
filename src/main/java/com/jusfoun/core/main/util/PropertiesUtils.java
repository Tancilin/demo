package com.jusfoun.core.main.util;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import com.jusfoun.core.main.etl.bean.DataResource;
import com.jusfoun.core.main.etl.bean.DataResourceType;

public class PropertiesUtils {
	/**
	 * 获取数据库连接信息
	 * @param propertiesType 数据库连接属性配置类别
	 * cbLanding：编目落地数据库 kettleLog：KETTLE日志数据库 transLanding转换落地数据库 filter：问题库
	 * @return
	 */
	public static Map<String, String> getCenterProperties(String propertiesType){
		Map<String,String> map = new HashMap<>();
		Resource rs = new ClassPathResource("centerServer.properties");
		Properties properties=new Properties();
		try {
			properties.load(rs.getInputStream());
			String hostKey=propertiesType+".host";
			String dbNameKey=propertiesType+".dbName";
			String portKey=propertiesType+".port";
			String usernameKey=propertiesType+".username";
			String passwordKey=propertiesType+".password";
			String resourceTypeKey="dataplatfrm.databasetype";
			String host = properties.getProperty(hostKey);
			String dbName = properties.getProperty(dbNameKey);
			String port = properties.getProperty(portKey);
			String username = properties.getProperty(usernameKey);
			String password = properties.getProperty(passwordKey);
			String resourceType=properties.getProperty(resourceTypeKey);
			map.put(hostKey, host);
			map.put(dbNameKey, dbName);
			map.put(portKey, port);
			map.put(usernameKey, username);
			map.put(passwordKey, password);
			map.put(resourceTypeKey, resourceType);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return map;
	}
	/**
	 * 获取数据库连接信息
	 * @param propertiesType 数据库连接属性配置类别
	 * cbLanding：编目落地数据库 kettleLog：KETTLE日志数据库 transLanding转换落地数据库 filter：问题库
	 * @return
	 */
	public static DataResource getDataBaseInfo(String propertiesType) {
		DataResource db=new DataResource();
		Map<String, String> dbMap = PropertiesUtils.getCenterProperties(propertiesType);
		String hostKey = propertiesType + ".host";
		String dbNameKey = propertiesType + ".dbName";
		String portKey = propertiesType + ".port";
		String usernameKey = propertiesType + ".username";
		String passwordKey = propertiesType + ".password";
		String resourceTypeKey="dataplatfrm.databasetype";		
		db.setResourceAddr(dbMap.get(hostKey));
		db.setDatabaseName(dbMap.get(dbNameKey));
		db.setPort(Integer.parseInt(dbMap.get(portKey)));
		db.setAcount(dbMap.get(usernameKey));
		db.setPasword(dbMap.get(passwordKey));
		DataResourceType type = new DataResourceType();
		type.setTypeName(dbMap.get(resourceTypeKey));
		db.setDcResourceType(type);
		return db;
	}
	
	//读取配置的ftp服务器根目录  added by mengshanfeng 20161213
	public static String getFtpRootPath() {
		Resource rs = new ClassPathResource("centerServer.properties");
		Properties properties=new Properties();
		try {
			properties.load(rs.getInputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}
		String ftpRootPath = properties.getProperty("ftp.rootpath");
		return ftpRootPath;
	}
	
	public static void main(String[] args) {
		PropertiesUtils.getCenterProperties("kettleLog");
	}
	
}
