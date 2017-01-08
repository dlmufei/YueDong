package com.tencentsng.yuedong.ui;

import com.tencentsng.yuedong.AppConfig;
import com.tencentsng.yuedong.R;
import com.tencentsng.yuedong.api.ApiHttpCilentUtil;
import com.tencentsng.yuedong.bean.URLs;
import com.tencentsng.yuedong.common.UIHelper;
import com.tencentsng.yuedong.service.IMessageService;
import com.tencentsng.yuedong.service.MessageService;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.JsResult;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;

public class Main extends BaseActivity {

	private static final String TAG = Main.class.getName();
	private static final int FILECHOOSER_RESULTCODE = 0;
	private ValueCallback<Uri> mUploadMessage;

	private WebView wv;
	private LinearLayout ll;

	// 消息服务
	private IMessageService iMessageService = null;
	// 获取中间人对象
	private MyConn myConn = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		String url = URLs.MAIN;

		ll = (LinearLayout) findViewById(R.id.ll);
		wv = new WebView(this);
		ll.addView(wv);

		wv.setLayoutParams(new android.widget.LinearLayout.LayoutParams(
				android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
				android.widget.LinearLayout.LayoutParams.MATCH_PARENT));

		WebSettings setting = wv.getSettings();
		setting.setJavaScriptEnabled(true);
		setting.setBuiltInZoomControls(false);
		setting.setAppCacheEnabled(true);
		setting.setUseWideViewPort(true);
		setting.setLoadWithOverviewMode(true);

		String cookies = AppConfig.getAppConfig(getApplicationContext())
				.getCookie();
		ApiHttpCilentUtil.synCookies(getApplicationContext(), cookies, url);

		Log.i(TAG, "cookies:" + cookies);

		if (null != savedInstanceState) {
			wv.restoreState(savedInstanceState);
			Log.i(TAG, "restore state");
		} else {

			wv.loadUrl(url);
		}

		wv.setWebViewClient(new WebViewClient() {

			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon) {
				// TODO Auto-generated method stub
				super.onPageStarted(view, url, favicon);
			}

			@Override
			public void onPageFinished(WebView view, String url) {
				// TODO Auto-generated method stub
				super.onPageFinished(view, url);
			}

		});

		wv.setWebChromeClient(new WebChromeClient() {
			@Override
			public boolean onJsAlert(WebView view, String url, String message,
					JsResult result) {
				// TODO Auto-generated method stub
				return super.onJsAlert(view, url, message, result);
			}

			@Override
			public boolean onJsConfirm(WebView view, String url,
					String message, JsResult result) {
				// TODO Auto-generated method stub
				return super.onJsConfirm(view, url, message, result);
			}

			// webView中支持input的file现选择
			// For Android 3.0+
			public void openFileChooser(ValueCallback<Uri> uploadMsg,
					String acceptType) {

				if (mUploadMessage != null)
					return;
				mUploadMessage = uploadMsg;
				Intent i = new Intent(Intent.ACTION_GET_CONTENT);
				i.addCategory(Intent.CATEGORY_OPENABLE);
				i.setType("*/*");
				startActivityForResult(Intent.createChooser(i, "File Chooser"),
						FILECHOOSER_RESULTCODE);
			}

			// For Android < 3.0
			public void openFileChooser(ValueCallback<Uri> uploadMsg) {
				openFileChooser(uploadMsg, "");
			}

			// For Android > 4.1.1
			public void openFileChooser(ValueCallback<Uri> uploadMsg,
					String acceptType, String capture) {
				openFileChooser(uploadMsg, acceptType);
			}

		});

		this.foreachUserNotice();

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == FILECHOOSER_RESULTCODE) {
			if (null == mUploadMessage)
				return;
			Uri result = data == null || resultCode != RESULT_OK ? null : data
					.getData();
			mUploadMessage.onReceiveValue(result);
			mUploadMessage = null;

		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// TODO Auto-generated method stub
		super.onSaveInstanceState(outState);
		wv.saveState(outState);
		Log.e(TAG, "save state...");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		Log.i(TAG, "onCreateOptionsMenu");
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_exit:
			UIHelper.Exit(this);
			break;
		case R.id.action_changeuser:
			AppConfig.getAppConfig(getApplicationContext()).cleanLoginInfo();

			Intent intent = new Intent(this, Login.class);
			this.startActivity(intent);
			this.finish();
			break;

		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * 监听返回--是否退出程序
	 */
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		boolean flag = true;
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			Log.i(TAG, "onKeyDown-KeyEvent.KEYCODE_BACK");
			wv.goBack();

		} else if (keyCode == KeyEvent.KEYCODE_MENU) {
			// 展示快捷栏&判断是否登录
			flag = false;

		} else {
			flag = super.onKeyDown(keyCode, event);
		}
		return flag;
	}

	@Override
	protected void onDestroy() {
		ll.removeAllViews();
		wv.stopLoading();
		wv.removeAllViews();
		wv.destroy();
		wv = null;
		ll = null;
		super.onDestroy();
	}

	/**
	 * 启动消息轮询通知
	 */
	private void foreachUserNotice() {
		// 启动后台服务
		Intent intentService = new Intent(getApplicationContext(),
				MessageService.class);
		startService(intentService);
		myConn = new MyConn();
		// 需要先调用 getApplicationContext()获取其所属的Activity的上下文环境才能正常bindService
		bindService(intentService, myConn, BIND_AUTO_CREATE);

	}

	// 自定义连接服务对象
	public class MyConn implements ServiceConnection {

		// 连接成功时调用
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			iMessageService = (IMessageService) service;

		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			// TODO Auto-generated method stub
			iMessageService = null;

		}

	}

}
