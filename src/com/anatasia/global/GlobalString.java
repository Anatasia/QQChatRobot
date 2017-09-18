package com.anatasia.global;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class GlobalString {
	private String tag = "GlobalString";
	
	//获取系统当前时间
	public static String getSystemTime(){
		String time = "";
		Long currentTime = System.currentTimeMillis();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		Date date = new Date(currentTime);
		time = sdf.format(date);
		return time;
	}
}
