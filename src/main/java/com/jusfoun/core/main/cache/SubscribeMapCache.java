package com.jusfoun.core.main.cache;

import java.util.HashMap;
import java.util.Map;

import com.jusfoun.core.main.etl.bean.KettleJob;

public class SubscribeMapCache {

	private static Map<String , KettleJob> mapCache = null;
	private SubscribeMapCache(){
		
	}
	static {
		mapCache = new HashMap<>();
		//这里查询数据库，保存map中
	}
	
	public static void putValue(String name, KettleJob obj){
		mapCache.put(name, obj);
	}
	
	public static KettleJob getValue(String name){
		return mapCache.get(name);
	}
	
	public static void removeValue(String name){
		mapCache.remove(name);
	}
	
	//用于清除缓存信息
	 public static void clearCache() {
		 mapCache.clear();
	 }
	 
	

	public static Map<String, KettleJob> getMapCache() {
		return mapCache;
	}

	
}
