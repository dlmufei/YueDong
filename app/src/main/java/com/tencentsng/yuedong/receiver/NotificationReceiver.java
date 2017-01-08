package com.tencentsng.yuedong.receiver;

import com.tencentsng.yuedong.common.SystemUtils;
import com.tencentsng.yuedong.ui.MesageContent;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class NotificationReceiver extends BroadcastReceiver {

	private static final String TAG = "NotificationReceiver";

	@Override
	public void onReceive(Context context, Intent intent) {
		System.out.println("NotificationReceiver");
		//
		if (SystemUtils.isAppAlive(context, "com.tencentsng.yuedong")) {
			Log.i(TAG, "the app process is alive");		

			// 获取数据intent
			Bundle bundle = intent.getExtras();
			String activityId = bundle.getString("activityId");
		
			Intent detailIntent = new Intent(context,
					MesageContent.class);
			detailIntent.putExtra("activityId", activityId);	
			detailIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			
			context.startActivity(detailIntent);
			
			
			
			
		} else {
			Log.i(TAG, "the app process is dead");
			Intent launchIntent = context.getPackageManager()
					.getLaunchIntentForPackage(
							"com.tencentsng.yuedong");
			launchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
					| Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
			Bundle args = new Bundle(intent.getExtras());
			
			launchIntent.putExtra("EXTRA_BUNDLE", args);
			context.startActivity(launchIntent);
		}

	}

}
