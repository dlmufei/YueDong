package com.tencentsng.yuedong;


import com.tencentsng.yuedong.ui.Login;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
/**
 * 启动activity
 * @author cliffyan
 * @version 1.0
 * @created 2016-6-10
 */
public class AppStart extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);		
		final View view = View.inflate(this, R.layout.activity_app_start, null);
		setContentView(view);
        
		//注册App异常崩溃处理器
        Thread.setDefaultUncaughtExceptionHandler(AppException.getAppExceptionHandler());
		
		//渐变展示启动屏
		AlphaAnimation aa = new AlphaAnimation(0.3f,1.0f);
		aa.setDuration(3000);
		view.startAnimation(aa);
		aa.setAnimationListener(new AnimationListener()
		{
			@Override
			public void onAnimationEnd(Animation arg0) {
				Intent intent = new Intent(AppStart.this,Login.class);
				AppStart.this.startActivity(intent);
				AppStart.this.finish();
			}
			@Override
			public void onAnimationRepeat(Animation animation) {}
			@Override
			public void onAnimationStart(Animation animation) {}
			
		});
		
	}

}
