package com.anatasia.service;
/*
 * @author:Anatasia
 * 功能：QQ自动对话机器人
 * 1）查找指定联系人
 */
import android.accessibilityservice.AccessibilityService;
import android.view.accessibility.AccessibilityEvent;


public class ASService extends AccessibilityService{

	public static ASService serviceInstance = null;
	//start, can call setServiceInfo() here!
	public void onServiceConnected() {
		super.onServiceConnected();
		serviceInstance = this;
	}
	
	public ASService getService(){
		return serviceInstance;
	}
	
	//running,this method maybe called many times over the lifecycle of service
	@Override
	public void onAccessibilityEvent(AccessibilityEvent event) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void onInterrupt() {
		// TODO Auto-generated method stub
		
	}

	//shut down
	public void onUnbind(){
		
	}
}
