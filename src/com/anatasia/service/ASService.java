package com.anatasia.service;
/**
 * @author:Anatasia
 * 功能：QQ自动对话机器人服务
 * 1）查找指定联系人
 */
import com.anatasia.global.MyLog;

import android.accessibilityservice.AccessibilityService;
import android.view.accessibility.AccessibilityEvent;


public class ASService extends AccessibilityService{

	public static ASService serviceInstance = null;
	private final String TAG = "ASService";
	
	/*
	 * start, can call setServiceInfo() here!
	 */
	protected void onServiceConnected() {
		super.onServiceConnected();
		serviceInstance = this;
		MyLog.i(TAG,"service connected!");
	}
	
	public static ASService getInstance(){
		return serviceInstance;
	}
	
	/*running,this method maybe called many times over the lifecycle of service
	 *监听窗口变化的回调
	 */
	@Override
	public void onAccessibilityEvent(AccessibilityEvent event) {
		// TODO Auto-generated method stub
		
	}
	
	/*
	 * 中断服务的回调
	 * @see android.accessibilityservice.AccessibilityService#onInterrupt()
	 */
	@Override
	public void onInterrupt() {
		// TODO Auto-generated method stub
		
	}

	//shut down
	public void onUnbind(){
		
	}
}
