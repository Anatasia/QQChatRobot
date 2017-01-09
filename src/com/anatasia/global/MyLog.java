package com.anatasia.global;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.os.Environment;
import android.util.Log;

/**
 * 作者：Anatasia
 * 功能：日志管理
 * 1)日志的几个级别定义
 * 2)输出日志到文件中
 */
public class MyLog {
	private final static String TAG = "MyLog";
	private static char MYLOG_TYPE = 'v';//输入日志类型，日志级别从高到低e->w->i->d->v，v代表输出所有信息
	private static boolean MYLOG_WRITE_TO_FILE = true;
	private static String MYLOG_PATH_DIR = Environment.getExternalStorageDirectory().getPath()+"/QQLog/";
	public static String logFileName = "initLog.txt";
	
	public static void w(String tag, Object msg){
		log(tag, msg.toString(),'w');
	}
	
	public static void e(String tag, Object msg){
		log(tag, msg.toString(),'e');
	}
	
	public static void d(String tag, Object msg){
		log(tag, msg.toString(),'d');
	}
	
	public static void i(String tag, Object msg){
		log(tag, msg.toString(),'i');
	}
	
	public static void v(String tag, Object msg){
		log(tag, msg.toString(),'v');
	}
	
	  public static void w(String tag, String text) {
	        log(tag, text, 'w');
	    }

	    public static void e(String tag, String text) {
	        log(tag, text, 'e');
	    }

	    public static void d(String tag, String text) {
	        log(tag, text, 'd');
	    }

	    public static void i(String tag, String text) {
	        log(tag, text, 'i');
	    }

	    public static void v(String tag, String text) {
	        log(tag, text, 'v');
	    }
	    
	    public static void dealException(String tag,String s,Exception e){
	        Log.i("Record",s+":"+Log.getStackTraceString(e));
	        writeLogtoFile("Exception", "Record", s + ":" + Log.getStackTraceString(e));
	    }
	
	/*
	 * 根据tag，msg和日志等级，输出日志
	 */
	private static void log(String tag, String msg, char level){
		if('e' == level && ('e' == MYLOG_TYPE || 'v' == MYLOG_TYPE)){
			Log.e(tag, msg);
		}else if('w' == level && ('w' == MYLOG_TYPE || 'v' == MYLOG_TYPE)){
			Log.w(tag, msg);
		}else if('d' == level && ('d' == MYLOG_TYPE || 'v' == MYLOG_TYPE)){
			Log.d(tag, msg);
		}else if('i' == level && ('i' == MYLOG_TYPE || 'v' == MYLOG_TYPE)){
			Log.w(tag, msg);
		}else{
			Log.v(tag, msg);
		}
		
		if(MYLOG_WRITE_TO_FILE){
			writeLogtoFile(String.valueOf(level), tag, msg);
		}
	}
	
	/*
	 * 打开日志文件，写入日志
	 */
	private static void writeLogtoFile(String myLogType, String tag, String text){
		Date currentDate = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
		String needWriteMsg = sdf.format(currentDate) + "      " + myLogType
				+ "      " + tag + "      " + text;
		File fileDir = new File(MYLOG_PATH_DIR);
		if(!fileDir.exists()){//若日志文件夹不存在，则创建日志文件夹
			fileDir.mkdirs();
		}
		
		File file = new File(MYLOG_PATH_DIR, logFileName);
		if(!file.exists()){
			try {
				file.createNewFile();
				Log.i(TAG, "创建日志文件");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				Log.i(TAG, "Error", e);
			}
		}
		
		try {
			FileWriter fw = new FileWriter(file, true);//第二个参数true表示接着上文件原来的数据，不进行覆盖
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(needWriteMsg);
			bw.close();
			fw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			Log.i(TAG, "日志写入文件出错："+Log.getStackTraceString(e));
		}
	}
}
