package com.tencentsng.yuedong;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;

import com.tencentsng.yuedong.common.CyptoUtils;
import com.tencentsng.yuedong.common.StringUtils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * 应用程序配置类：用于保存用户相关信息及设置
 * 
 * @author cliffyan
 * @version 1.0
 * @created 2016-6-10
 */
public class AppConfig {

	private final static String TAG = "APPConfig";
	private final static String APP_CONFIG = "config";

	public final static String CONF_COOKIE = "cookies";
	public final static String CONF_PWD = "pwd";
	public final static String CONF_PHONE = "phone";
	public final static String CONF_REMBER = "rember";
	public final static String CONF_AUTOLOGIN = "autologin";

	private Context mContext;
	private static AppConfig appConfig;

	private AppConfig() {
	}

	/**
	 * 单例实例化
	 * 
	 * @param context
	 * @return
	 */
	public static AppConfig getAppConfig(Context context) {
		if (appConfig == null) {
			appConfig = new AppConfig();
			appConfig.mContext = context;
		}
		return appConfig;
	}

	/**
	 * 获取Preference设置
	 */
	public static SharedPreferences getSharedPreferences(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context);
	}

	/**
	 * Cookies保存
	 * 
	 * @return
	 */
	public String getCookie() {
		Log.i(TAG, "getCookie->start");
		if (StringUtils.isEmpty(get(CONF_COOKIE))) {
			return null;
		}
		return get(CONF_COOKIE);
	}

	/**
	 * 清楚Cookies
	 */
	public void cleanCookie() {
		Log.i(TAG, "cleanCookie->start");

		remove(CONF_COOKIE);
	}

	/**
	 * 设置Cooies
	 * 
	 * @param cookies
	 */
	public void setCookie(String cookies) {
		Log.i(TAG, "setCookie-start-" + cookies);
		set(CONF_COOKIE, cookies);
		Log.i(TAG, "setCookie-end-" + cookies);

	}

	/**
	 * 获取电话号码
	 * 
	 * @return
	 */
	public String getPhone() {
		if (StringUtils.isEmpty(get(CONF_PHONE))) {
			return "";
		}
		return get(CONF_PHONE);
	}

	/**
	 * 清除电话号码
	 */
	public void cleanPhone() {
		remove(CONF_PHONE);
	}

	/**
	 * 设置电话号码
	 * 
	 * @param phone
	 */
	public void setPhone(String phone) {
		set(CONF_PHONE, phone);

	}

	/**
	 * 获取密码，自动处理解密过程
	 * 
	 * @return
	 */
	public String getPwd() {
		if (StringUtils.isEmpty(get(CONF_PWD))) {
			return "";
		}
		return CyptoUtils.decode(get(CONF_PWD));
	}

	/**
	 * 清除保存的密码
	 */
	public void cleanPwd() {
		remove(CONF_PWD);
	}

	/**
	 * 保存密码
	 * 
	 * @param pwd
	 */
	public void setPwd(String pwd) {
		set(CONF_PWD, CyptoUtils.encode(pwd));

	}

	/**
	 * 是否记住密码
	 * 
	 * @return
	 */
	public String getRemPwd() {
		if (StringUtils.isEmpty(get(CONF_REMBER))) {
			return "";
		}
		return get(CONF_REMBER);
	}

	/**
	 * 清楚记住密码配置信息
	 */
	public void cleanRemPwd() {
		remove(CONF_REMBER);
	}

	/**
	 * 设置记住密码
	 * 
	 * @param rem
	 */
	public void setRemPwd(String rem) {
		set(CONF_REMBER, rem);
	}

	/**
	 * 获取自动登录标识
	 * 
	 * @return
	 */
	public String getAutoLogin() {
		if (StringUtils.isEmpty(get(CONF_AUTOLOGIN))) {
			return "";
		}
		return get(CONF_AUTOLOGIN);
	}

	/**
	 * 清除自动登录信息
	 */
	public void cleanAutoLogin() {
		remove(CONF_AUTOLOGIN);
	}

	/**
	 * 保存自动登录信息
	 * 
	 * @param autoLogin
	 */
	public void setAutoLogin(String autoLogin) {
		set(CONF_AUTOLOGIN, autoLogin);
	}

	/**
	 * 清除用户配置的所有信息
	 */
	public void cleanLoginInfo() {
		cleanCookie();
		cleanPhone();
		cleanPwd();
		cleanRemPwd();
		cleanPhone();
	}

	/**
	 * 保存key值
	 * 
	 * @param key
	 * @return 不存在返回null字符串
	 */
	public String get(String key) {
		Properties props = get();
		return (props != null) ? props.getProperty(key) : null;
	}

	/**
	 * 获取全部的配置文件
	 * 
	 * @return Properties文件
	 */
	public Properties get() {
		FileInputStream fis = null;
		Properties props = new Properties();
		try {
			// 读取app_config目录下的config
			File dirConf = mContext.getDir(APP_CONFIG, Context.MODE_PRIVATE);
			fis = new FileInputStream(dirConf.getPath() + File.separator
					+ APP_CONFIG);

			props.load(fis);
		} catch (Exception e) {
		} finally {
			try {
				fis.close();
			} catch (Exception e) {
			}
		}
		return props;
	}

	private void setProps(Properties p) {
		FileOutputStream fos = null;
		try {
			// 把config建在(自定义)app_config的目录下
			File dirConf = mContext.getDir(APP_CONFIG, Context.MODE_PRIVATE);
			File conf = new File(dirConf, APP_CONFIG);
			fos = new FileOutputStream(conf);

			p.store(fos, null);
			fos.flush();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				fos.close();
			} catch (Exception e) {
			}
		}
	}

	public void set(Properties ps) {
		Properties props = get();
		props.putAll(ps);
		setProps(props);
	}

	public void set(String key, String value) {
		Properties props = get();
		props.setProperty(key, value);
		setProps(props);
	}

	public void remove(String... key) {
		Properties props = get();
		for (String k : key)
			props.remove(k);
		setProps(props);
	}

}
