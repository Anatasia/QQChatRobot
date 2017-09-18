package com.anatasia.service;

/**
 * @author:Anatasia
 * 功能：QQ自动对话机器人服务
 * ------2017/2/21------
 * 1）查找指定联系人
 * ------2017/2/24------
 * 1)向好友发送聊天信息
 * ------2017/2/27------
 * 1)查找聊天页面无法输入内容的问题。原因：input不能输入中文以及特殊字符。解决思路：换用5以上的手机，避免使用input
 */
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import com.anatasia.global.GlobalString;
import com.anatasia.global.MyLog;
import com.anatasia.global.StreamGobbler;
import android.R.anim;
import android.R.integer;
import android.accessibilityservice.AccessibilityService;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.IntDef;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;

@SuppressLint("NewApi")
public class ASService extends AccessibilityService {

	public static ASService serviceInstance = null;
	private final String TAG = "ASService";
	private final String qqPackageName = "com.tencent.mobileqq";// qq应用包名
	private final int FRIEND = 1;// 已经添加的qq好友
	private final String descriptionSavePath = Environment.getExternalStorageDirectory().getPath()+"/PaperData/";
	private ArrayList<String> groupNameList = new ArrayList<String>();


    private ArrayList<Integer> qqGroupList = new ArrayList<>(); 
	
	/*
	 * start, can call setServiceInfo() here!
	 */
	protected void onServiceConnected() {
		super.onServiceConnected();
		serviceInstance = this;
		MyLog.i(TAG, "service connected!");
	}

	/*
	 * 功能：qq对话机器人启动入口 1）qq联系人查找 打开qq应用->
	 */
	public void socialTest() {
		// 打开qq
		if (openQQ()) {
			MyLog.i(TAG, "QQ应用打开成功");
			// 睡眠5秒等待qq界面出现
			try {
				Thread.sleep(1000 * 5);
			} catch (InterruptedException e) {
				// TODO: handle exception
				MyLog.i(TAG, e.getStackTrace());
			}
			
			int count = 0;
			
			//1.获得QQ群组信息
			getQQGroupInfo();
			MyLog.i(TAG, "发现群组个数:"+groupNameList.size());
			for(int i=0;i<groupNameList.size();i++){
				boolean isValid = true;
				String name = groupNameList.get(i);
				try{
					int tmp = Integer.parseInt(name);
					count++;
					isValid = false;
				}catch(Exception e){
					MyLog.e(TAG, e);
				}
				//2.导出每个群组的聊天对话流以及群描述信息
				if(isValid){
					MyLog.i(TAG, "当前运行到第"+(i+1)+"个群");
					getDescriptionAndChatStream(name);
				}
			}
			
			MyLog.i(TAG,"*************"+GlobalString.getSystemTime()+"***************" );
			MyLog.i(TAG, "********总群数："+groupNameList.size()+"**失效群组数目："+count+"********");
			
		} else {
			MyLog.i(TAG, "QQ应用打开失败");
		}

	}
	
	//导出群描述信息以及群对话流
	public void scrawlData(){
		MyLog.i(TAG, "爬取QQ群样本数据");
		int count = 10;
		
		AccessibilityNodeInfo rootItem = getRootInActiveWindow();
		List<AccessibilityNodeInfo> nodeInfos = rootItem.findAccessibilityNodeInfosByText("联系人");
		
		for (AccessibilityNodeInfo node : nodeInfos) {
			node.getParent().performAction(AccessibilityNodeInfo.ACTION_CLICK);
			MyLog.i(TAG, "click 联系人!");
		}
		
		try {
			Thread.sleep(3*1000);
		} catch (InterruptedException e) {
			// TODO: handle exception
			MyLog.e(TAG, e);
		}
		rootItem = getRootInActiveWindow();
		List<AccessibilityNodeInfo> groups = rootItem.findAccessibilityNodeInfosByText("群");
		
		for (AccessibilityNodeInfo node : nodeInfos) {
			node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
			MyLog.i(TAG, "click 群!");
		}
		
		
	}
	
	
	//获取群描述信息的数据，作为训练样本
	private void getQQGroupDescription(){
		MyLog.i(TAG, "收集群描述数据");
		try {
			Thread.sleep(3*1000);
		} catch (InterruptedException e) {
			// TODO: handle exception
			MyLog.e(TAG, e);
		}
		
		AccessibilityNodeInfo rootItem = getRootInActiveWindow();
		List<AccessibilityNodeInfo> nodeInfos = rootItem.findAccessibilityNodeInfosByText("联系人");
		
		for (AccessibilityNodeInfo node : nodeInfos) {
			node.getParent().performAction(AccessibilityNodeInfo.ACTION_CLICK);
			MyLog.i(TAG, "click 联系人!");
		}
		
		try {
			Thread.sleep(3*1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			MyLog.i(TAG,e);
		}
		
		
		MyLog.i(TAG, "click 添加!");
		rootItem = getRootInActiveWindow();
		List<AccessibilityNodeInfo> addGroupNodes = rootItem.findAccessibilityNodeInfosByViewId("com.tencent.mobileqq:id/ivTitleBtnRightText");
		for(AccessibilityNodeInfo node: addGroupNodes){
			node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
		}
		
		try {
			Thread.sleep(3*1000);
			MyLog.i(TAG, "等待3s");
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			MyLog.i(TAG,e);
		}
		MyLog.i(TAG, "click 找群!");
		rootItem = getRootInActiveWindow();
		List<AccessibilityNodeInfo> findGroupNodes = rootItem.findAccessibilityNodeInfosByText("找群");
		for(AccessibilityNodeInfo node: findGroupNodes){
			MyLog.i(TAG, "click success");
			node.getParent().performAction(AccessibilityNodeInfo.ACTION_CLICK);
		}
		
		
		try {
			Thread.sleep(3*1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			MyLog.i(TAG,e);
		}
		
		
		MyLog.i(TAG, "click 进入群分类");
		rootItem = getRootInActiveWindow();
		List<AccessibilityNodeInfo> findCategoryNodes = rootItem.findAccessibilityNodeInfosByText("生活");
		for(AccessibilityNodeInfo node: findCategoryNodes){
			MyLog.i(TAG, "click success");
			MyLog.i(TAG, node.getText());
			node.getParent().performAction(AccessibilityNodeInfo.ACTION_CLICK);
		}
		
//		List<AccessibilityNodeInfo> nodeInfos = rootItem.findAccessibilityNodeInfosByText("联系人");
//		
//		for (AccessibilityNodeInfo node : nodeInfos) {
//			node.getParent().performAction(AccessibilityNodeInfo.ACTION_CLICK);
//			MyLog.i(TAG, "click 联系人!");
//		}
		
	}

	/*
	 * 功能：发送聊天信息
	 */
	private void sendQQMsg(String content) {
		MyLog.i(TAG, "发送qq信息");
		try {
			Thread.sleep(1000 * 3);
		} catch (InterruptedException e) {
			MyLog.i(TAG, e.getStackTrace());
		}
		AccessibilityNodeInfo rootItem = getRootInActiveWindow();
		List<AccessibilityNodeInfo> nodeInfos = rootItem
				.findAccessibilityNodeInfosByViewId("com.tencent.mobileqq:id/input");
		for (AccessibilityNodeInfo node : nodeInfos) {
			node.performAction(AccessibilityNodeInfo.ACTION_CLICK);// 点击一下输入框
			MyLog.i(TAG, "输入框点击成功");
		}

		try {
			Thread.sleep(1000 * 3);
		} catch (InterruptedException e) {
			MyLog.i(TAG, e.getStackTrace());
		}
		if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			AccessibilityNodeInfo root = getRootInActiveWindow();
			List<AccessibilityNodeInfo> nodes = root
					.findAccessibilityNodeInfosByViewId("com.tencent.mobileqq:id/input");
			Bundle setTextInfo = new Bundle();
			setTextInfo.putString(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, content);
			for (AccessibilityNodeInfo node : nodes) {
				node.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT,
						setTextInfo);
			}
		} else {
			MyLog.i(TAG,"键盘输入");
			inputCharArray(content);// 为了兼容4.4以下的机器,这里采用了模拟键盘输入的方式进行文本输入,记得输入之前一定要点一下输入框啊^_^切记只能支持数字和英文字母!
			MyLog.i(TAG,"内容输入成功");
		}
		// Toast.makeText(this,"action:输入",Toast.LENGTH_SHORT).show();

		try {
			Thread.sleep(1000 * 3);
		} catch (InterruptedException e) {
			MyLog.i(TAG, e.getStackTrace());
		}
		rootItem = getRootInActiveWindow();
		nodeInfos = rootItem
				.findAccessibilityNodeInfosByViewId("com.tencent.mobileqq:id/fun_btn");
		// Toast.makeText(this,"action:点击",Toast.LENGTH_SHORT).show();
		for (AccessibilityNodeInfo node : nodeInfos) {
			MyLog.i(TAG, "发送消息");
			node.performAction(AccessibilityNodeInfo.ACTION_CLICK);
		}
		MyLog.i(TAG, "消息发送成功");
	}

	/*
	 * 功能：搜索联系人
	 */
	private void searchContacts(String contact, int type) {

		try {
			Thread.sleep(1000*3);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			MyLog.i(TAG, e1.getStackTrace());
		}
		AccessibilityNodeInfo rootItem = getRootInActiveWindow();
		// 找到qq搜索框
		List<AccessibilityNodeInfo> nodeInfos = rootItem
				.findAccessibilityNodeInfosByViewId("com.tencent.mobileqq:id/et_search_keyword");
		for (AccessibilityNodeInfo nodeInfo : nodeInfos) {
			nodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK);// 点击搜索框
			MyLog.i(TAG, "搜索框点击成功");
		}
		MyLog.i(TAG, "搜索关键词：" + contact);
		// 休息3秒，等待搜索框点击后的页面出现
		try {
			Thread.sleep(1000 * 3);
		} catch (InterruptedException e) {
			MyLog.i(TAG, e.getStackTrace());
		}
		if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			MyLog.i(TAG, "安卓版本：" + android.os.Build.VERSION.SDK_INT);
			rootItem = getRootInActiveWindow();
			nodeInfos = rootItem
					.findAccessibilityNodeInfosByViewId("com.tencent.mobileqq:id/et_search_keyword");
			Bundle setTextInfo = new Bundle();
			setTextInfo
					.putString(
							AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,
							contact);// 在搜索框中输入搜索关键词
			for (AccessibilityNodeInfo node : nodeInfos) {
				node.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT,
						setTextInfo);
			}
		} else {
			MyLog.i(TAG, "模拟键盘方式进行文本输入");
			inputCharArray(contact);// 为了兼容4.4以下的机器,这里采用了模拟键盘输入的方式进行文本输入,记得输入之前一定要点一下输入框啊^_^
			MyLog.i(TAG, "键盘输入成功");
		}

		// 等待下一页面出现
		try {
			Thread.sleep(1000 * 3);
		} catch (InterruptedException e) {
			MyLog.i(TAG, e.getStackTrace());
		}
		rootItem = getRootInActiveWindow();
		switch (type) {
		case FRIEND:
			nodeInfos = rootItem
					.findAccessibilityNodeInfosByViewId("com.tencent.mobileqq:id/result_layout");
			for (AccessibilityNodeInfo node : nodeInfos) {
				node.getChild(0).getChild(1).getChild(0)
						.performAction(AccessibilityNodeInfo.ACTION_CLICK);// 点击搜索结果的第一条记录
			}

			break;

		default:
			break;
		}
		MyLog.i(TAG, "searchContacts is over!");

	}

	/*
	 * 功能：模拟键盘方式进行文本输入
	 */
	 private void inputCharArray(String str){
	        try {
	            Runtime runtime = Runtime.getRuntime();
	            Process proc = runtime.exec("su \n");
	            DataOutputStream os = new DataOutputStream(proc.getOutputStream());
	            StreamGobbler errorGobbler = new StreamGobbler(proc.getErrorStream(), "ERROR");
	            StreamGobbler outputGobbler = new StreamGobbler(proc.getInputStream(), "OUTPUT");
	            errorGobbler.start();
	            outputGobbler.start();

	            inputStringByText(str, os);
	            Thread.sleep(5*1000);
	            int ev = proc.waitFor();
	            MyLog.i(TAG, "输入完成，进程结束，ev="+ev);

	        }catch (Exception e){
	            MyLog.dealException(TAG,"get runtime error:",e);
	        }
	    }

	    private void inputStringByText(String str,DataOutputStream os){
	        try
	        {
	            String[] strArray=str.split(",");
	            for (int i = 0; i < strArray.length; i++) {
	                try {
	                    Thread.sleep(3000);
	                } catch (InterruptedException e) {
	                    e.printStackTrace();
	                }
	                String  item =  strArray[i];
	                MyLog.i(TAG, "send key code : " + item);
	                inputOneCharByText(item,os);
	                if(i<strArray.length-1){
	                    inputOneCharByText(",",os);
	                }
	            }
	        }
	        catch (Exception e)
	        {
	            MyLog.dealException(TAG, "input keycode error", e);
	        }
	    }

	    private void inputOneChar(final int KeyCode,DataOutputStream os){
	        try
	        {
	            MyLog.i(TAG, "send key code : " + KeyCode);
	            String keyCommand = "input keyevent " + KeyCode;
	            os.writeBytes(keyCommand + "\n");
	        }
	        catch (IOException e)
	        {
	            MyLog.dealException(TAG, "input keycode error", e);
	        }
	    }

	    private void inputOneCharByText(String chr,DataOutputStream os){
	        try
	        {
	            MyLog.i(TAG, "send key char : " + chr);
	            String keyCommand = "input text '" + chr+ "'";
	            os.writeBytes(keyCommand + "\n");
				os.flush(); 
				os.close();
	        }
	        catch (IOException e)
	        {
	            MyLog.dealException(TAG, "input text error", e);
	        }
	    }

	/*
	 * 功能：打开qq应用
	 */
	private boolean openQQ() {
		MyLog.i(TAG, "准备开启应用：com.tencent.mobileqq");

		HashMap<Integer, String> appHashMap = new HashMap<Integer, String>();// 终端应用map
		HashMap<Integer, String> packageHashMap = new HashMap<Integer, String>();// 终端应用包名map
		HashMap<Integer, String> activityHashMap = new HashMap<Integer, String>();// 应用启动activity
																					// map

		List<PackageInfo> packages = getPackageManager()
				.getInstalledPackages(0);// 获取所有授权应用信息
		// 获取应用名及对应的应用包名；
		int flag = 0;
		for (int i = 0; i < packages.size(); i++) {
			PackageInfo packageInfo = packages.get(i);
			String appName = packageInfo.applicationInfo.loadLabel(
					getPackageManager()).toString();
			String packageName = packageInfo.packageName;
			appHashMap.put(flag, appName);
			packageHashMap.put(flag, packageName);
			flag++;
		}

		int size = appHashMap.size();// 终端应用总数
		PackageManager pm = this.getPackageManager(); // 获得PackageManager对象
		Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
		mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

		// 通过查询，获得所有ResolveInfo对象.
		List<ResolveInfo> resolveInfos = pm.queryIntentActivities(mainIntent,
				PackageManager.GET_ACTIVITIES);
		// 调用系统排序 ， 根据name排序
		// 该排序很重要，否则只能显示系统应用，而不能列出第三方应用程序
		Collections.sort(resolveInfos,
				new ResolveInfo.DisplayNameComparator(pm));

		for (ResolveInfo reInfo : resolveInfos) {
			String pkgName = reInfo.activityInfo.packageName; // 获得应用程序的包名
			String activityName = reInfo.activityInfo.name; // 获得该应用程序的启动Activity的name
			for (int i = 0; i < size; i++) {
				if (pkgName.equals(packageHashMap.get(i))) {
					activityHashMap.put(i, activityName);
					break;
				}
			}
		}

		// 找到qq应用启动主activity
		String qqStartActivity = "";
		for (int i = 0; i < appHashMap.size(); i++) {
			if (qqPackageName.equals(packageHashMap.get(i))) {
				qqStartActivity = activityHashMap.get(i);
				break;
			}
		}

		try {
			Intent intent = new Intent();
			intent.setClassName(qqPackageName, qqStartActivity);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(intent);

			return true;
		} catch (Exception e) {
			MyLog.i(TAG, "应用程序启动失败：" + e.getMessage());
			Toast.makeText(this, "APP启动失败", Toast.LENGTH_SHORT).show();

		}
		return false;
	}

	public static ASService getInstance() {
		return serviceInstance;
	}

	@Override
	public void onInterrupt() {
		// TODO Auto-generated method stub
		
	}

	
	
	/*
	 * 功能：返回事件名称
	 */
	 private String getEventTypeText(int eventType){
	        String eventText = "";
	        switch (eventType) {
	            case AccessibilityEvent.TYPE_VIEW_CLICKED:
	                eventText = "TYPE_VIEW_CLICKED";
	                break;
	            case AccessibilityEvent.TYPE_VIEW_FOCUSED:
	                eventText = "TYPE_VIEW_FOCUSED";
	                break;
	            case AccessibilityEvent.TYPE_VIEW_LONG_CLICKED:
	                eventText = "TYPE_VIEW_LONG_CLICKED";
	                break;
	            case AccessibilityEvent.TYPE_VIEW_SELECTED:
	                eventText = "TYPE_VIEW_SELECTED";
	                break;
	            case AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED:
	                eventText = "TYPE_VIEW_TEXT_CHANGED";
	                break;
	            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
	                eventText = "TYPE_WINDOW_STATE_CHANGED";
	                break;
	            case AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED:
	                eventText = "TYPE_NOTIFICATION_STATE_CHANGED";
	                break;
	            case AccessibilityEvent.TYPE_TOUCH_EXPLORATION_GESTURE_END:
	                eventText = "TYPE_TOUCH_EXPLORATION_GESTURE_END";
	                break;
	            case AccessibilityEvent.TYPE_ANNOUNCEMENT:
	                eventText = "TYPE_ANNOUNCEMENT";
	                break;
	            case AccessibilityEvent.TYPE_TOUCH_EXPLORATION_GESTURE_START:
	                eventText = "TYPE_TOUCH_EXPLORATION_GESTURE_START";
	                break;
	            case AccessibilityEvent.TYPE_GESTURE_DETECTION_START:
	                eventText = "TYPE_GESTURE_DETECTION_START";
	                break;
	            case AccessibilityEvent.TYPE_GESTURE_DETECTION_END:
	                eventText = "TYPE_GESTURE_DETECTION_END";
	                break;
	            case AccessibilityEvent.TYPE_TOUCH_INTERACTION_START:
	                eventText = "TYPE_TOUCH_INTERACTION_START";
	                break;
	            case AccessibilityEvent.TYPE_TOUCH_INTERACTION_END:
	                eventText = "TYPE_TOUCH_INTERACTION_END";
	                break;
	            case AccessibilityEvent.TYPE_VIEW_HOVER_ENTER:
	                eventText = "TYPE_VIEW_HOVER_ENTER";
	                break;
	            case AccessibilityEvent.TYPE_VIEW_HOVER_EXIT:
	                eventText = "TYPE_VIEW_HOVER_EXIT";
	                break;
	            case AccessibilityEvent.TYPE_VIEW_SCROLLED:
	                eventText = "TYPE_VIEW_SCROLLED";
	                break;
	            case AccessibilityEvent.TYPE_VIEW_TEXT_SELECTION_CHANGED:
	                eventText = "TYPE_VIEW_TEXT_SELECTION_CHANGED";
	                break;
	            case AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED:
	                eventText = "TYPE_WINDOW_CONTENT_CHANGED";
	                break;
	        }
	        return eventText;
	    }
	 /*
	 * running,this method maybe called many times over the lifecycle of service
	 * 监听窗口变化的回调
	 */
	@Override
	public void onAccessibilityEvent(AccessibilityEvent event) {
//		AccessibilityNodeInfo rootNodeInfo = getRootInActiveWindow();
//		if(rootNodeInfo==null) return;
//		List<AccessibilityNodeInfo> groupList = rootNodeInfo.findAccessibilityNodeInfosByViewId("com.tencent.mobileqq:id/qb_troop_list_view");
//		//当前在群联系人页面
//		if(groupList.size()!=0){
//			getGroupNameInCurrentPage(rootNodeInfo);
//		}
	}

	//找出群列表
	public void getQQGroupInfo(){
		
		AccessibilityNodeInfo rootItem = getRootInActiveWindow();
		List<AccessibilityNodeInfo> nodeInfos = rootItem.findAccessibilityNodeInfosByText("联系人");
		
		for (AccessibilityNodeInfo node : nodeInfos) {
			node.getParent().performAction(AccessibilityNodeInfo.ACTION_CLICK);
			MyLog.i(TAG, "click 联系人!");
		}
		
		try {
			Thread.sleep(3*1000);
		} catch (InterruptedException e) {
			// TODO: handle exception
			MyLog.e(TAG, e);
		}
		rootItem = getRootInActiveWindow();
		List<AccessibilityNodeInfo> groups = rootItem.findAccessibilityNodeInfosByText("群");
		if(groups.size()>0){
			groups.get(0).performAction(AccessibilityNodeInfo.ACTION_CLICK);
			MyLog.i(TAG, "click 群!");
		}
		try {
			Thread.sleep(3*1000);
		} catch (InterruptedException e) {
			// TODO: handle exception
			MyLog.e(TAG, e);
		}
		int i=0;
		rootItem = getRootInActiveWindow();
		List<AccessibilityNodeInfo> last=null;
		List<AccessibilityNodeInfo> groupList = rootItem.findAccessibilityNodeInfosByViewId("com.tencent.mobileqq:id/qb_troop_list_view");
		while(groupList.size()>0){
			i++;
			if(groupList.size()>0){
				AccessibilityNodeInfo groupInfo = groupList.get(0);
				
				List<AccessibilityNodeInfo> detailGroup = groupInfo.findAccessibilityNodeInfosByViewId("com.tencent.mobileqq:id/text1");
				if(last!=null&&last.equals(detailGroup)){
					MyLog.i(TAG, "到达页面底部");
					break;
				}
				//System.out.println("找到qq群个数:"+detailGroup.size());
				for(AccessibilityNodeInfo node: detailGroup){
					String name = node.getText().toString();
					if(!groupNameList.contains(name)){
						groupNameList.add(name);
					}
				}
			
				groupInfo.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD);
				last=detailGroup;
				try {
					Thread.sleep(1*1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			if(i>50) break;
			rootItem = getRootInActiveWindow();
			groupList = rootItem.findAccessibilityNodeInfosByViewId("com.tencent.mobileqq:id/qb_troop_list_view");
			
		}
		
	}
	
	//导出对话流和群描述信息
	private void getDescriptionAndChatStream(String contact){
		AccessibilityNodeInfo rootItem = getRootInActiveWindow();
		List<AccessibilityNodeInfo> searchBoxNode = rootItem.findAccessibilityNodeInfosByViewId("com.tencent.mobileqq:id/et_search_keyword");
		if(searchBoxNode.size()>0){
			MyLog.i(TAG, "点击搜索框");
			searchBoxNode.get(0).performAction(AccessibilityNodeInfo.ACTION_CLICK);
		}
		
		// 休息3秒，等待搜索框点击后的页面出现
		try {
			Thread.sleep(1000 * 4);
		} catch (InterruptedException e) {
			MyLog.i(TAG, e.getStackTrace());
		}
			MyLog.i(TAG, "安卓版本：" + android.os.Build.VERSION.SDK_INT);
			rootItem = getRootInActiveWindow();
			searchBoxNode = rootItem
					.findAccessibilityNodeInfosByViewId("com.tencent.mobileqq:id/et_search_keyword");
			if(searchBoxNode.size()>0){
				MyLog.i(TAG, "设置输入文本");
				ClipboardManager clipboard = (ClipboardManager)this.getSystemService(Context.CLIPBOARD_SERVICE);  
				ClipData clip = ClipData.newPlainText("text", contact);  
				clipboard.setPrimaryClip(clip);  
				try {
					Thread.sleep(1*1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				//焦点（n是AccessibilityNodeInfo对象）  
				searchBoxNode.get(0).performAction(AccessibilityNodeInfo.ACTION_FOCUS);  
				////粘贴进入内容  
				searchBoxNode.get(0).performAction(AccessibilityNodeInfo.ACTION_PASTE);  
			}

		// 等待下一页面出现
		try {
			Thread.sleep(1000 * 3);
		} catch (InterruptedException e) {
			MyLog.i(TAG, e.getStackTrace());
		}
		rootItem = getRootInActiveWindow();
		
		List<AccessibilityNodeInfo> nodeInfos = rootItem
					.findAccessibilityNodeInfosByViewId("com.tencent.mobileqq:id/result_layout");
		if (nodeInfos.size()>0) {
			nodeInfos.get(0).getChild(0).getChild(1).getChild(0)
					.performAction(AccessibilityNodeInfo.ACTION_CLICK);// 点击搜索结果的第一条记录
		}

		// 等待下一页面出现
		try {
			Thread.sleep(1000 * 3);
		} catch (InterruptedException e) {
			MyLog.i(TAG, e.getStackTrace());
		}
		
		rootItem = getRootInActiveWindow();
		while(rootItem==null){
			rootItem = getRootInActiveWindow();
			try {
				Thread.sleep(1*1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		nodeInfos = rootItem.findAccessibilityNodeInfosByViewId("com.tencent.mobileqq:id/ivTitleBtnRightImage");
		if(nodeInfos.size()>0){
			nodeInfos.get(0).performAction(AccessibilityNodeInfo.ACTION_CLICK);
		}
		
		try {
			Thread.sleep(1000 * 3);
		} catch (InterruptedException e) {
			MyLog.i(TAG, e.getStackTrace());
		}
		
	
		rootItem = getRootInActiveWindow();
		
		//导出群对话流
		nodeInfos = rootItem.findAccessibilityNodeInfosByText("聊天记录");
		if(nodeInfos.size()>0){
			MyLog.i(TAG, "找到聊天记录入口");
			nodeInfos.get(0).getParent().performAction(AccessibilityNodeInfo.ACTION_CLICK);
		}
		try {
			Thread.sleep(3*1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		rootItem = getRootInActiveWindow();
		nodeInfos = rootItem.findAccessibilityNodeInfosByText("导出");
		if(nodeInfos.size()>0){
			MyLog.i(TAG, "找到导出聊天对话流");
			nodeInfos.get(0).performAction(AccessibilityNodeInfo.ACTION_CLICK);
		}
		
		try {
			Thread.sleep(3*1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		rootItem = getRootInActiveWindow();
		nodeInfos = rootItem.findAccessibilityNodeInfosByViewId("com.tencent.mobileqq:id/dialogRightBtn");
		if(nodeInfos.size()>0){
			MyLog.i(TAG, "点击确认导出聊天记录按钮");
			nodeInfos.get(0).performAction(AccessibilityNodeInfo.ACTION_CLICK);
		}
		try {
			Thread.sleep(1*1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		rootItem = getRootInActiveWindow();
		nodeInfos = rootItem.findAccessibilityNodeInfosByViewId("com.tencent.mobileqq:id/ivTitleBtnLeft");
		if(nodeInfos.size()>0){
			nodeInfos.get(0).performAction(AccessibilityNodeInfo.ACTION_CLICK);
		}
		try {
			Thread.sleep(5*1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//采集描述信息
	
		String description = contact+"\n";
		String fileName = descriptionSavePath+contact+".txt";
		getDescriptionInfo(description, fileName);
		
		//返回联系人界面
		rootItem = getRootInActiveWindow();
		List<AccessibilityNodeInfo> goBackButton = rootItem.findAccessibilityNodeInfosByViewId("com.tencent.mobileqq:id/ivTitleBtnLeft");
		if(goBackButton.size()>0){
			goBackButton.get(0).performAction(AccessibilityNodeInfo.ACTION_CLICK);
		}
		
		try {
			Thread.sleep(2*1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		rootItem = getRootInActiveWindow();
		goBackButton = rootItem.findAccessibilityNodeInfosByViewId("com.tencent.mobileqq:id/rlCommenTitle");
		if(goBackButton.size()>0){
			goBackButton.get(0).getChild(0).performAction(AccessibilityNodeInfo.ACTION_CLICK);
		}
		
		try{
			Thread.sleep(3*100);
		}catch(InterruptedException e){
			e.printStackTrace();
		}
		
	}
	
	//通过adb执行窗口滑动命令
	public void execSU(String command) {
        DataOutputStream dataOutputStream = null;
        BufferedReader errorStream = null;
        Process process = null;
        try {
            process = Runtime.getRuntime().exec("su");
            dataOutputStream = new DataOutputStream(process.getOutputStream());
            dataOutputStream.write(command.getBytes(Charset.forName("UTF-8")));
            dataOutputStream.writeBytes("exit\n");
            dataOutputStream.flush();
            process.waitFor();
            errorStream = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            StringBuilder errorMsg = new StringBuilder();
            String line;
            while ((line = errorStream.readLine()) != null) {
                errorMsg.append(line);
            }
            if (errorMsg.toString().length() > 0) {
                Log.i(TAG, "errorMessage:" + errorMsg);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (process != null) {
                    process.destroy();
                }
                if (dataOutputStream != null) {
                    dataOutputStream.close();
                }
                if (errorStream != null) {
                    errorStream.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
	
	public void getDescriptionInfo(String description, String fileName) {

		String cmd = "input swipe 521 1500 520 600 \n";
		
		boolean isTagFound = false;
		boolean isDescriptionFound = false;
		int i=0;
		
		while(!isTagFound&&!isDescriptionFound){
			i++;
			AccessibilityNodeInfo rootItem = getRootInActiveWindow();
			List<AccessibilityNodeInfo> nodeInfos = rootItem.findAccessibilityNodeInfosByText("群介绍");
			if(nodeInfos.size()>0){
				MyLog.i(TAG, "找到群介绍");
				AccessibilityNodeInfo parent = nodeInfos.get(0).getParent();
				if(parent!=null){
					MyLog.i(TAG, "获取群介绍信息");
					if(parent.getChild(3)!=null&&parent.getChild(3).getText()!=null){
						description += parent.getChild(3).getText().toString()+"\n";
						isDescriptionFound = true;
					}
				}
				
				MyLog.i(TAG, "获取群标签信息");
				AccessibilityNodeInfo topParent = parent.getParent();
				//MyLog.i(TAG, "TOP:"+topParent.getChildCount());
				int start = -1;
				int end = -1;
				for(int j=0;j<topParent.getChildCount()&&(start==-1||end==-1);j++){
					AccessibilityNodeInfo tmp = topParent.getChild(j);
					for(int k=0;k<tmp.getChildCount()&&(start==-1||end==-1);k++){
						if(tmp.getChild(k).getText()!=null){
							//MyLog.i(TAG, tmp.getChild(k).getText());
							if(tmp.getChild(k).getText().toString().equals( "群介绍")){
								
								end=j;  
							}
							
							if(tmp.getChild(k).getText().toString().equals("聊天背景")){
							
								start=j;
							}
						}
					}
				}
				MyLog.i(TAG, "start="+start+"   end="+end);
				if(start!=-1&&end!=-1){
					for(int j = start+1;j<end;j++){
						AccessibilityNodeInfo tmp = topParent.getChild(j);
						for(int k =0;k<tmp.getChildCount();k++){
							if(tmp.getChild(k).getText()!=null){
								MyLog.i(TAG,tmp.getChild(k).getText().toString());
								description += tmp.getChild(k).getText().toString()+"\n";
							}
						}
					}
					isTagFound = true;
				}
				
			}
		
			
			if(i>5){
				MyLog.i(TAG, "跳出循环");
				break;
			}
		
			execSU(cmd);

			try {
				Thread.sleep(1*1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		MyLog.i(TAG, description);
		
		File file = new File(fileName+".txt");
		if(!file.exists()){
			try {
				file.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		FileWriter fw = null;
		BufferedWriter bw = null;
		try {
			fw = new FileWriter(file);
			 bw = new BufferedWriter(fw);
			bw.write(description);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			if(bw!=null){
				try {
					bw.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if(fw!=null){
				try {
					fw.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
		try {
			Thread.sleep(2*1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
