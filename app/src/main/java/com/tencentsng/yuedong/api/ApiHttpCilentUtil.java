package com.tencentsng.yuedong.api;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;

import org.apache.http.Header;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.PersistentCookieStore;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.SyncHttpClient;
import com.tencentsng.yuedong.AppConfig;
import com.tencentsng.yuedong.AppException;
import com.tencentsng.yuedong.bean.Result;
import com.tencentsng.yuedong.bean.URLs;
import com.tencentsng.yuedong.common.StringUtils;
import com.tencentsng.yuedong.common.UIHelper;

/**
 * httpclient 工具
 * 
 * @author cliffyan
 * @version 1.0
 * @created 2016-6-16
 */
public class ApiHttpCilentUtil {

	private static final String TAG = "ApiHttpCilentUtil";

	private static AsyncHttpClient client;

	private static SyncHttpClient sClient;
	
	/**
	 * 同步一下cookie
	 */
	public static void synCookies(Context context, String cookies, String url) {
		CookieSyncManager.createInstance(context);
		CookieManager cookieManager = CookieManager.getInstance();
		cookieManager.setAcceptCookie(true);
		cookieManager.removeSessionCookie();// 移除
		cookieManager.setCookie(url, cookies);// cookies是在HttpClient中获得的cookie
		CookieSyncManager.getInstance().sync();
	}

	/**
	 * 获取异步httpclient单例，自动处理cookies
	 * 
	 * @param context
	 * @return
	 */
	public static synchronized AsyncHttpClient getAsyncInstance(Context context) {
		if (client == null) {
			client = new AsyncHttpClient();
			PersistentCookieStore myCookieStore = new PersistentCookieStore(
					context);
			client.setCookieStore(myCookieStore);
		}
		return client;
	}

	/**
	 * 获取同步httpclient单例，自动处理cookies
	 * 
	 * @param context
	 * @return
	 */
	public static synchronized SyncHttpClient getSyncInstance(Context context) {
		if (sClient == null) {
			sClient = new SyncHttpClient();
			PersistentCookieStore myCookieStore = new PersistentCookieStore(
					context);
			sClient.setCookieStore(myCookieStore);
			Log.i(TAG, "SyncHttpClient-PersistentCookieStore");
		}
		return sClient;
	}

	/**
	 * 登录验证，自动处理cookies,用户名，密码
	 * 
	 * @param context
	 * @param phone
	 *            电话号码
	 * @param pwd
	 *            密码
	 * @param remberPwd
	 *            是否记住密码
	 * @param antoLogin
	 *            是否自动登录
	 */
	public static void login(final Context context, final String phone,
			final String pwd, final String remberPwd, final String antoLogin) {

		String url = URLs.LOGIN_VALIDATE_HTTP;
		AsyncHttpClient client = getAsyncInstance(context);
		RequestParams params = new RequestParams();
		params.put("telNo", phone);
		params.put("userPasswd", pwd);

		client.post(url, params, new AsyncHttpResponseHandler() {

			@Override
			public void onSuccess(int arg0, Header[] arg1, byte[] arg2) {
				Log.i(TAG, "login-onSuccess");
				try {
					Result result = Result.parse(arg2);
					Log.i(TAG, "login-onSuccess-Result.parse");
					Log.i(TAG, "result.OK():" + result.OK());
					if (result.OK()) {
						Log.i(TAG, "login-onSuccess-result.OK()");
						for (Header h : arg1) {
							if (h.toString().contains("Set-Cookie")) {
								String cookies = h.toString().substring(
										"Set-Cookie:".length());
								Log.i(TAG, cookies);

								AppConfig.getAppConfig(context).cleanCookie();
								AppConfig.getAppConfig(context).setCookie(
										cookies);
								AppConfig.getAppConfig(context).setPhone(phone);
								AppConfig.getAppConfig(context).setPwd(pwd);
								AppConfig.getAppConfig(context).setRemPwd(
										remberPwd);
								AppConfig.getAppConfig(context).setAutoLogin(
										antoLogin);

								Log.i(TAG, AppConfig.getAppConfig(context)
										.getCookie());

								if (!StringUtils.isEmpty(AppConfig
										.getAppConfig(context).getCookie())) {

									UIHelper.showHome(context);
									UIHelper.ToastMessage(context, "登录成功");

								}

							}
						}
					} else {
						UIHelper.ToastMessage(context, "用户名或密码错误");

					}

				} catch (Exception e) {
					AppException.run(e);
					Log.i(TAG, e.getMessage());
				}

			}

			@Override
			public void onFailure(int arg0, Header[] arg1, byte[] arg2,
					Throwable arg3) {
				try {
					Log.i(TAG, "login-onFailure");
					AppConfig.getAppConfig(context).cleanCookie();

				} catch (Exception e) {
					AppException.run(e);
				}

			}

		});
	}

	/**
	 * 获取短信验证码
	 * 
	 * @param context
	 * @param phone
	 *            电话号码
	 */
	public static void getSms(final Context context, final String phone) {

		String url = URLs.SMS_GET_HTTP;
		AsyncHttpClient client = getAsyncInstance(context);
		RequestParams params = new RequestParams();
		params.put("telNo", phone);
		Log.i(TAG, "getSms:" + url + ":" + phone);

		client.get(url, params, new AsyncHttpResponseHandler() {

			@Override
			public void onSuccess(int arg0, Header[] arg1, byte[] arg2) {
				try {
					String msg = new String(arg2, "utf-8");
					Log.i(TAG, "getSms-onSuccess:" + msg);
				} catch (UnsupportedEncodingException e) {
					AppException.encode(e);
				}
				UIHelper.ToastMessage(context, "短信发送成功");
			}

			@Override
			public void onFailure(int arg0, Header[] arg1, byte[] arg2,
					Throwable arg3) {
				UIHelper.ToastMessage(context, "短信发送失败");

			}

		});
	}

	/**
	 * 验证码验证
	 * 
	 * @param context
	 * @param code 收到的验证码
	 */
	public static void validSms(final Context context, final String phone,
			final String code) {

		String url = URLs.SMS_VALIDATE_HTTP;
		AsyncHttpClient client = getAsyncInstance(context);
		RequestParams params = new RequestParams();
		params.put("telNo", phone);
		params.put("inputCode", code);

		client.post(url, params, new AsyncHttpResponseHandler() {

			@Override
			public void onSuccess(int arg0, Header[] arg1, byte[] arg2) {
				try {
					String msg = new String(arg2, "utf-8");
					Log.i(TAG, "validSms-onSuccess:" + msg);
					JSONObject jsonObject = new JSONObject(msg);
					if (jsonObject.getString("result").equals("success")) {

					}
				} catch (Exception e) {
					AppException.run(e);
				}

			}

			@Override
			public void onFailure(int arg0, Header[] arg1, byte[] arg2,
					Throwable arg3) {
				Log.i(TAG, "validSms-onFailure");

			}

		});
	}

	/**
	 * 上传用户图片
	 * 
	 * @param context
	 * @param picName  图片路径信息，/nmt/scard/以后的
	 * @throws FileNotFoundException
	 */
	public static void uploadHead(final Context context, final File file)
			throws FileNotFoundException {

		String url = URLs.PIC_UPLOAD;
		AsyncHttpClient client = getAsyncInstance(context);
		RequestParams params = new RequestParams();
		params.put("portrait", file);

		client.post(url, params, new AsyncHttpResponseHandler() {

			@Override
			public void onSuccess(int arg0, Header[] arg1, byte[] arg2) {
				try {
					String msg = new String(arg2, "utf-8");
					Log.i(TAG, "uploadHead-onSuccess:" + msg);

					JSONObject jsonObject = new JSONObject(msg);
					if (jsonObject.getBoolean("result") == true) {
						String small = jsonObject.getString("small");
						String origin = jsonObject.getString("origin");
						Log.i(TAG, small+":"+origin);
					}

				} catch (Exception e) {
					AppException.run(e);
				}

			}

			@Override
			public void onFailure(int arg0, Header[] arg1, byte[] arg2,
					Throwable arg3) {
				Log.i(TAG, "uploadHead-onFailure");

			}

		});
	}

	/**
	 * 注册
	 * 
	 * @param context
	 * @param code
	 * @throws FileNotFoundException
	 */
	public static void regist1(final Context context, final File file,
			final String nickName, final String pwd, final String phone,
			final String code) throws FileNotFoundException {

		AsyncHttpClient client = getAsyncInstance(context);
		RequestParams params = new RequestParams();
		params.put("portrait", file);

		client.post(URLs.PIC_UPLOAD, params, new AsyncHttpResponseHandler() {

			@Override
			public void onSuccess(int arg0, Header[] arg1, byte[] arg2) {
				try {
					String msg = new String(arg2, "utf-8");
					Log.i(TAG, "regist1-onSuccess:" + msg);

					JSONObject jsonObject = new JSONObject(msg);
					if (jsonObject.getBoolean("result") == true) {
						String small = jsonObject.getString("small");
						String origin = jsonObject.getString("origin");
						Log.i(TAG, small);

						// ---------------------------------------------
						AsyncHttpClient client = getAsyncInstance(context);
						RequestParams params = new RequestParams();
						params.put("userName", nickName);
						params.put("userPasswd", pwd);
						params.put("telNo", phone);
						// params.put("gender", "1");
						params.put("smallImage", small);
						params.put("originImage", origin);

						client.post(URLs.REGISTE, params,
								new AsyncHttpResponseHandler() {

									@Override
									public void onFailure(int arg0,
											Header[] arg1, byte[] arg2,
											Throwable arg3) {
										Log.i(TAG, "regist-onFailure");

									}

									@Override
									public void onSuccess(int arg0,
											Header[] arg1, byte[] arg2) {
										try {
											String msg = new String(arg2,
													"utf-8");
											Log.i(TAG, "regist-onSuccess:"
													+ msg);
											JSONObject jsonObject = new JSONObject(
													msg);
											if (jsonObject.getBoolean("result") == true) {
												UIHelper.ToastMessage(
														context,
														jsonObject
																.getString("message"));
												UIHelper.showLogin(context);

											} else {
												UIHelper.ToastMessage(
														context,
														jsonObject
																.getString("message"));
											}

										} catch (Exception e) {
											Log.i(TAG,
													"regist-onSuccess-Exception");
										}

									}

								});

					}

				} catch (Exception e) {
					AppException.run(e);
				}

			}

			@Override
			public void onFailure(int arg0, Header[] arg1, byte[] arg2,
					Throwable arg3) {
				Log.i(TAG, "regist1-onFailure");

			}

		});
	}

	/**
	 * 验证码+注册+头像
	 * 
	 * @param context
	 * @param code
	 */
	public static void regist_1(final Context context, final File file,
			final String nickName, final String pwd, final String phone,
			final String code) {

		String url = URLs.SMS_VALIDATE_HTTP;
		AsyncHttpClient client = getAsyncInstance(context);
		RequestParams params = new RequestParams();
		params.put("telNo", phone);
		params.put("inputCode", code);

		client.post(url, params, new AsyncHttpResponseHandler() {

			@Override
			public void onSuccess(int arg0, Header[] arg1, byte[] arg2) {
				try {
					String msg = new String(arg2, "utf-8");
					Log.i(TAG, "validSms-onSuccess:" + msg);
					JSONObject jsonObject = new JSONObject(msg);
					Boolean result = jsonObject.getBoolean("result");
					if (result.toString().equals("true")) {
						Log.i(TAG, "sms-success-next");
						AsyncHttpClient client = getAsyncInstance(context);
						RequestParams params = new RequestParams();
						params.put("portrait", file);

						client.post(URLs.PIC_UPLOAD, params,
								new AsyncHttpResponseHandler() {

									@Override
									public void onSuccess(int arg0,
											Header[] arg1, byte[] arg2) {
										try {
											String msg = new String(arg2,
													"utf-8");
											Log.i(TAG, "regist1-onSuccess:"
													+ msg);

											JSONObject jsonObject = new JSONObject(
													msg);
											if (jsonObject.getBoolean("result") == true) {
												Log.i(TAG, "pic-success-next");
												String small = jsonObject
														.getString("small");
												String origin = jsonObject
														.getString("origin");
												Log.i(TAG, small);

												// ---------------------------------------------
												AsyncHttpClient client = getAsyncInstance(context);
												RequestParams params = new RequestParams();
												params.put("userName", nickName);
												params.put("userPasswd", pwd);
												params.put("telNo", phone);
												// params.put("gender", "1");
												params.put("smallImage", small);
												params.put("originImage",
														origin);

												client.post(
														URLs.REGISTE,
														params,
														new AsyncHttpResponseHandler() {

															@Override
															public void onFailure(
																	int arg0,
																	Header[] arg1,
																	byte[] arg2,
																	Throwable arg3) {
																Log.i(TAG,
																		"regist-onFailure");

															}

															@Override
															public void onSuccess(
																	int arg0,
																	Header[] arg1,
																	byte[] arg2) {
																try {
																	String msg = new String(
																			arg2,
																			"utf-8");
																	Log.i(TAG,
																			"regist-onSuccess:"
																					+ msg);
																	JSONObject jsonObject = new JSONObject(
																			msg);
																	if (jsonObject
																			.getBoolean("result") == true) {
																		UIHelper.ToastMessage(
																				context,
																				jsonObject
																						.getString("message"));
																		UIHelper.showLogin(context);

																	} else {
																		UIHelper.ToastMessage(
																				context,
																				jsonObject
																						.getString("message"));
																	}

																} catch (Exception e) {
																	Log.i(TAG,
																			"regist-onSuccess-Exception");
																}

															}

														});

											}

										} catch (Exception e) {
											AppException.run(e);
										}

									}

									@Override
									public void onFailure(int arg0,
											Header[] arg1, byte[] arg2,
											Throwable arg3) {
										Log.i(TAG, "regist1-onFailure");

									}

								});

					}
				} catch (Exception e) {
					AppException.run(e);
				}

			}

			@Override
			public void onFailure(int arg0, Header[] arg1, byte[] arg2,
					Throwable arg3) {
				Log.i(TAG, "validSms-onFailure");

			}

		});
	}

	/**
	 * 注册（用户没有头像,无SMS）
	 * 
	 * @param context
	 * @param code
	 * @throws FileNotFoundException
	 */
	public static void registNoHead(final Context context,
			final String nickName, final String pwd, final String phone,
			final String code) throws FileNotFoundException {

		AsyncHttpClient client = getAsyncInstance(context);
		RequestParams params = new RequestParams();
		params.put("userName", nickName);
		params.put("userPasswd", pwd);
		params.put("telNo", phone);

		client.post(URLs.REGISTE, params, new AsyncHttpResponseHandler() {

			@Override
			public void onFailure(int arg0, Header[] arg1, byte[] arg2,
					Throwable arg3) {
				Log.i(TAG, "regist-onFailure");

			}

			@Override
			public void onSuccess(int arg0, Header[] arg1, byte[] arg2) {
				try {
					String msg = new String(arg2, "utf-8");
					Log.i(TAG, "regist-onSuccess:" + msg);
					JSONObject jsonObject = new JSONObject(msg);
					if (jsonObject.getBoolean("result") == true) {
						UIHelper.ToastMessage(context,
								jsonObject.getString("message"));

						UIHelper.showLogin(context);

					} else {
						UIHelper.ToastMessage(context,
								jsonObject.getString("message"));
					}

				} catch (Exception e) {
					Log.i(TAG, "regist-onSuccess-Exception");
				}

			}

		});

	}

	/**
	 * 验证码验证
	 * 
	 * @param context
	 * @param code
	 */
	public static void registNoHead_1(final Context context,
			final String nickName, final String pwd, final String phone,
			final String code) {

		String url = URLs.SMS_VALIDATE_HTTP;
		AsyncHttpClient client = getAsyncInstance(context);
		RequestParams params = new RequestParams();
		params.put("telNo", phone);
		params.put("inputCode", code);

		client.post(url, params, new AsyncHttpResponseHandler() {

			@Override
			public void onSuccess(int arg0, Header[] arg1, byte[] arg2) {
				try {
					String msg = new String(arg2, "utf-8");
					Log.i(TAG, "validSms-onSuccess:" + msg);
					JSONObject jsonObject = new JSONObject(msg);
					Boolean result = jsonObject.getBoolean("result");
					Log.i(TAG, "sms-success-result" + result);
					if (result.toString().equals("true")) {

						Log.i(TAG, "sms-success-next");
						AsyncHttpClient client = getAsyncInstance(context);
						RequestParams params = new RequestParams();
						params.put("userName", nickName);
						params.put("userPasswd", pwd);
						params.put("telNo", phone);

						client.post(URLs.REGISTE, params,
								new AsyncHttpResponseHandler() {

									@Override
									public void onFailure(int arg0,
											Header[] arg1, byte[] arg2,
											Throwable arg3) {
										Log.i(TAG, "regist-onFailure");

									}

									@Override
									public void onSuccess(int arg0,
											Header[] arg1, byte[] arg2) {
										try {
											String msg = new String(arg2,
													"utf-8");
											Log.i(TAG, "regist-onSuccess:"
													+ msg);
											JSONObject jsonObject = new JSONObject(
													msg);
											if (jsonObject.getBoolean("result") == true) {
												UIHelper.ToastMessage(
														context,
														jsonObject
																.getString("message"));

												UIHelper.showLogin(context);

											} else {
												UIHelper.ToastMessage(
														context,
														jsonObject
																.getString("message"));
											}

										} catch (Exception e) {
											Log.i(TAG,
													"regist-onSuccess-Exception"
															+ e.getMessage());
										}

									}

								});

					} else {
						Log.i(TAG, "sms-success-not-next");
					}
				} catch (Exception e) {
					Log.i(TAG, "sms-Exception：" + e.getMessage());
					AppException.run(e);
				}

			}

			@Override
			public void onFailure(int arg0, Header[] arg1, byte[] arg2,
					Throwable arg3) {
				Log.i(TAG, "validSms-onFailure");

			}

		});
	}

	/**
	 * 注册(只注册)
	 * 
	 * @param context
	 * @param code
	 * @throws FileNotFoundException
	 */
	public static void regist2(final Context context, final File file,
			final String nickName, final String pwd, final String phone,
			final String code) throws FileNotFoundException {

		AsyncHttpClient client = getAsyncInstance(context);
		RequestParams params = new RequestParams();
		params.put("userName", nickName);
		params.put("userPasswd", phone);
		params.put("telNo", pwd);
		params.put("gender", "1");
		params.put("smallImage", "smallImage-test");
		params.put("originImage", "originImage-test");

		client.post(URLs.REGISTE, new AsyncHttpResponseHandler() {

			@Override
			public void onFailure(int arg0, Header[] arg1, byte[] arg2,
					Throwable arg3) {
				Log.i(TAG, "regist-onFailure");

			}

			@Override
			public void onSuccess(int arg0, Header[] arg1, byte[] arg2) {
				try {
					String msg = new String(arg2, "utf-8");
					Log.i(TAG, "regist-onSuccess:" + msg);
					JSONObject jsonObject = new JSONObject(msg);
					if (jsonObject.getBoolean("result") == true) {
						UIHelper.ToastMessage(context,
								jsonObject.getString("message"));
						UIHelper.showHome(context);

					} else {
						UIHelper.ToastMessage(context,
								jsonObject.getString("message"));
					}

				} catch (Exception e) {
					Log.i(TAG, "regist-onSuccess-Exception");
				}

			}

		});

	}

}
