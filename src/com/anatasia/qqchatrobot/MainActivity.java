package com.anatasia.qqchatrobot;
import com.anatasia.global.MyLog;
import com.anatasia.service.ASService;

import android.R.integer;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
/**
 *author:Anatasia
 *功能:qq对话机器人初始化，服务启动等
 *1）判断对话机器人服务是否启动，若未启动则进行启动
 *2）
 */
public class MainActivity extends Activity {

	private final String TAG = "MainActivity";
	private ASService serviceInstance = null; 
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		//进行qq测试
		Button qqTestButton = (Button)findViewById(R.id.testQQ);
		
		
		
		qqTestButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				//获取服务实例
				ASService currentServiceInstance = getServiceInstance();
				if(currentServiceInstance!=null){
					currentServiceInstance.socialTest();
				}
			}
		});
	}
	
	/*
	 * 功能：获取对话机器人服务实例
	 * 1）判断辅助功能是否开启，若未开启则弹出辅助功能开启框
	 */
	
	private ASService getServiceInstance(){
		if(!isAccessibilitySettingOn(getApplicationContext())){//辅助功能未开启
			Toast.makeText(MainActivity.this, "尚未开启辅助功能，请在新弹出的对话框中开启QQChatRobot辅助功能!", Toast.LENGTH_SHORT).show();
			MyLog.i(TAG, "辅助功能开启失败");
			startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
			return null;
		}else{
			serviceInstance = ASService.getInstance();
			MyLog.i(TAG, "辅助功能开启成功,serviceInstance = "+serviceInstance);
		}
		
		return serviceInstance;
	}
	
	/*
	 *功能： 判断qq自动对话机器人辅助服务是否打开
	 * 1)判断辅助功能设置是否开启
	 * 2）判断已开启的服务
	 * 3）找到对话机器人服务，返回true，若对话机器人服务未开启返回false;
	 */
	public boolean isAccessibilitySettingOn(Context mContext) {
		int accessibilityEnabled = 0;
		final String service = "com.anatasia.qqchatrobot/com.anatasia.service.ASService";
		boolean accessibilityFound = false;//标志对话机器人服务是否开启
		try {//判断辅助功能设置是否开启
			accessibilityEnabled = Settings.Secure.getInt(mContext
					.getApplicationContext().getContentResolver(),
					Settings.Secure.ACCESSIBILITY_ENABLED);
			MyLog.i(TAG, "accessibilityEnabled = " + accessibilityEnabled);
		} catch (SettingNotFoundException e) {
			// TODO Auto-generated catch block
			MyLog.i(TAG, "Error:"+e.getStackTrace().toString());
		}
		
		if(accessibilityEnabled == 1){//如果辅助功能设置打开
			MyLog.i(TAG, "======ACCESSIBILITY IS ENABLED=======");
			String settingValue = Settings.Secure.getString(mContext.getApplicationContext().getContentResolver(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
			if(settingValue != null){//存在开启的服务
				TextUtils.SimpleStringSplitter simpleStringSplitter = new TextUtils.SimpleStringSplitter(':');
				simpleStringSplitter.setString(settingValue);
				while(simpleStringSplitter.hasNext()){
					String accessibilityService = simpleStringSplitter.next();
					MyLog.i(TAG, "------>已开启服务：" + accessibilityService);
					if(accessibilityService.equalsIgnoreCase(service)){//如果对话机器人服务开启
						MyLog.i(TAG, "找到对话机器人服务，且该服务已经开启");
						accessibilityFound = true;
						return true;
					}
					
				}
			}
		}else {
			MyLog.i(TAG, "======辅助功能未开启======");
		}
		return accessibilityFound;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
