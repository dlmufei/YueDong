package com.tencentsng.yuedong.service;

import org.apache.http.Header;
import org.json.JSONObject;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.SyncHttpClient;
import com.tencentsng.yuedong.AppException;
import com.tencentsng.yuedong.R;
import com.tencentsng.yuedong.api.ApiHttpCilentUtil;
import com.tencentsng.yuedong.bean.URLs;
import com.tencentsng.yuedong.receiver.NotificationReceiver;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

public class MessageService extends Service {

	private static final String TAG = "MessageService";

	private MyThread myThread;
	private NotificationManager manager;
	private Notification notification;
	private PendingIntent pi;
	private SyncHttpClient client;
	// private AsyncHttpClient client;
	private boolean flag = true;

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onCreate() {
		Log.i(TAG, "MessageService-onCreate");
		// this.client =
		// AsyncHttpCilentUtil.getInstance(getApplicationContext());
		this.client = ApiHttpCilentUtil
				.getSyncInstance(getApplicationContext());
		this.myThread = new MyThread();
		this.myThread.start();
		// messageReceiveStart();
		super.onCreate();
	}

	@Override
	public void onDestroy() {
		this.flag = false;
		myThread.stop();
		super.onDestroy();
	}

	/**
	 * notice
	 * 
	 * @param message
	 *            信息
	 * @param activityId
	 *            msgid
	 * @param activityTitle
	 *            标题
	 */
	private void notification(String message, String activityId,
			String activityTitle) {
		// 获取系统的通知管理器
		manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		notification = new Notification(R.drawable.ic_launcher, message,
				System.currentTimeMillis());
		notification.defaults = Notification.DEFAULT_ALL; // 使用默认设置，比如铃声、震动、闪灯
		notification.flags = Notification.FLAG_AUTO_CANCEL; // 但用户点击消息后，消息自动在通知栏自动消失
		notification.flags |= Notification.FLAG_NO_CLEAR;// 点击通知栏的删除，消息不会依然不会被删除

		// 创建对应的处理事件
		// 设置点击通知栏的动作为启动另外一个广播
		Intent broadcastIntent = new Intent(getApplicationContext(),
				NotificationReceiver.class);

		broadcastIntent.putExtra("activityId", activityId);

		PendingIntent pendingIntent = PendingIntent.getBroadcast(
				getApplicationContext(), 0, broadcastIntent,
				PendingIntent.FLAG_UPDATE_CURRENT);
		notification.setLatestEventInfo(getApplicationContext(), activityTitle,
				message, pendingIntent);

		manager.notify(10, notification);

		Log.i(TAG, "activityId:" + activityId);
	}

	/**
	 * 服务线程
	 * @author FEI
	 *
	 */
	private class MyThread extends Thread {
		private static final long INTERVAL = 5000;

		@Override
		public void run() {

			String url = URLs.USER_NOTICE_HTTP;
			while (flag) {
				Log.i(TAG, "发送请求:" + url);
				try {
					// 每个10秒向服务器发送一次请求
					Thread.sleep(INTERVAL);
				} catch (InterruptedException e) {
					AppException.run(e);
				}
				client.post(url, new AsyncHttpResponseHandler() {
					@Override
					public void onSuccess(int statusCode, Header[] headers,
							byte[] responseBody) {
						Log.i(TAG, "onSuccess");
						try {

							String strMsg = new String(responseBody, "utf-8");
							Log.i(TAG, "strMsg:" + strMsg);
							JSONObject jsonObject = new JSONObject(strMsg);

							Boolean resultFlag = jsonObject
									.getBoolean("result");
							//
							if (resultFlag.toString().equals("true")) {

								String message = jsonObject
										.getString("message");
								String activityTitle = jsonObject
										.getString("activityTitle");
								String activityId = jsonObject
										.getString("activityId");

								notification(message, activityId, activityTitle);
							}

						} catch (Exception e) {
							Log.i(TAG, "onSuccess-Exception:" + e.getMessage());
							AppException.run(e);
						}
					}

					@Override
					public void onFailure(int statusCode, Header[] headers,
							byte[] responseBody, Throwable error) {
						Log.i(TAG, "onFailure-数据请求失败");
					}
				});
			}
		}
	}

	public class MyBinder extends Binder implements IMessageService {

		@Override
		public void startReceiveMessage() {
			// TODO Auto-generated method stub
			flag = false;

		}

		@Override
		public void stopReceiveMessage() {
			// TODO Auto-generated method stub
			flag = true;

		}

	}

}
