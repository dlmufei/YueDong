package com.tencentsng.yuedong.ui;

import com.tencentsng.yuedong.AppConfig;
import com.tencentsng.yuedong.R;

import com.tencentsng.yuedong.api.ApiHttpCilentUtil;
import com.tencentsng.yuedong.common.FileUtils;
import com.tencentsng.yuedong.common.StringUtils;
import com.tencentsng.yuedong.common.UIHelper;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

public class Login extends BaseActivity {

	private static final String TAG = "Login";

	private EditText etPhone;
	private EditText etPwd;
	private CheckBox cbRempwd;
	private CheckBox cbAutoLogin;

	private Boolean bAutoLogin = false;
	private Boolean bRember = false;
	private String strPhone=null;
	private String strPwd=null;
	private String strCookies=null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.acitivity_login);
		initview();
	}

	/**
	 * 初始化控件
	 */
	private void initview() {
		etPhone = (EditText) findViewById(R.id.et_phone);
		etPwd = (EditText) findViewById(R.id.et_pwd);
		cbRempwd = (CheckBox) findViewById(R.id.cb_rempwd);
		cbAutoLogin = (CheckBox) findViewById(R.id.cb_autologin);

		// 初始化变量
		AppConfig config = AppConfig.getAppConfig(getApplicationContext());
		bRember = config.getRemPwd().equals("true") ? true : false;
		Log.i(TAG, bRember.toString());

		if (bRember) {
			strPhone = config.getPhone();
			strPwd = config.getPwd();
			Log.i(TAG, strPhone);

			etPhone.setText(strPhone);
			etPwd.setText(strPwd);

		}
		bAutoLogin = config.getAutoLogin().equals("true") ? true : false;
		Log.i(TAG, bAutoLogin.toString());
		cbRempwd.setChecked(bRember);
		cbAutoLogin.setChecked(bAutoLogin);
		if (bAutoLogin) {
			ApiHttpCilentUtil.login(getApplicationContext(), strPhone, strPwd,
					bRember.toString(), bAutoLogin.toString());
			strCookies = config.getCookie();

		}
	}

	/**
	 * 登录事件
	 * 
	 * @param view
	 */
	public void clickLogin(View view) {
		strPhone = etPhone.getText().toString().trim();
		strPwd = etPwd.getText().toString().trim();
		if (StringUtils.isEmpty(strPhone) || StringUtils.isEmpty(strPwd)) {
			UIHelper.ToastMessage(getApplicationContext(), "输入完整的登录信息");
			return;
		}

		// 防止网络阻塞时，连续点击
		if (UIHelper.isFastDoubleClick(2000)) {
			Toast.makeText(getApplicationContext(), "请勿快速连续点击！",
					Toast.LENGTH_SHORT).show();
			return;
		}

		// 输入控制验证
		ApiHttpCilentUtil.login(getApplicationContext(), strPhone, strPwd,
				bRember.toString(), bAutoLogin.toString());

		Log.i(TAG, strPhone + "：" + strPwd);

	}

	/**
	 * 注册事件
	 * 
	 * @param view
	 */
	public void clickRegist(View view) {
		// 清除缓存的头像文件
		if (FileUtils.deleteFile(Registe.IMAGE_FILE_NAME)) {
			Log.i(TAG, "clickRegist-deleteFile-success");
		} else {
			Log.i(TAG, "clickRegist-deleteFile-failt");
		}
		UIHelper.showRegist(this);
	}

	/**
	 * 自动登录设置
	 * 
	 * @param view
	 */
	public void clickAutoLogin(View view) {
		bAutoLogin = cbAutoLogin.isChecked();
	}

	/**
	 * 记住密码
	 * 
	 * @param view
	 */
	public void clickRemPwd(View view) {
		bRember = cbRempwd.isChecked();
	}

}
