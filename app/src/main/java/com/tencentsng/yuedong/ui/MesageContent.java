package com.tencentsng.yuedong.ui;

import com.tencentsng.yuedong.AppConfig;
import com.tencentsng.yuedong.R;
import com.tencentsng.yuedong.api.ApiHttpCilentUtil;
import com.tencentsng.yuedong.bean.URLs;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.LinearLayout;

public class MesageContent extends BaseActivity {
	
	private static final String TAG = "MesageContent";
	private WebView wv;
	private LinearLayout ll;
	
	@SuppressLint("SetJavaScriptEnabled")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_msg_content);
		
		Bundle bundle = getIntent().getExtras();
		String activityId = bundle.getString("activityId");
		
		String url=URLs.DETAIL+activityId;
		
		Log.i(TAG, "url:"+url);
		
		ll = (LinearLayout) findViewById(R.id.ll_msgcontent);
		wv = new WebView(getApplicationContext());
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
		wv.requestFocus();
		wv.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);//取消滚动条

		String cookies = AppConfig.getAppConfig(getApplicationContext())
				.getCookie();
		ApiHttpCilentUtil.synCookies(getApplicationContext(), cookies, url);
		wv.loadUrl(url);
	}

}
