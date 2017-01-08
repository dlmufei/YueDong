package com.tencentsng.yuedong.bean;


/**
 * 接口URL实体类
 * @author cliffyan
 * @version 1.0
 * @created 2016-6-10
 */
public class URLs{
	
	//119.29.201.102:8000/user/login/
	public final static String HOST = "119.29.201.102";//119.29.201.102//www.sng.seanxp.com
	public final static String HTTP = "http://";
	public final static String PORT = "8000";
	
	
	private final static String URL_API_HOST = HTTP + HOST+":"+PORT + "/";
	//用户登录
	public final static String MAIN = URL_API_HOST + "activity/find/";
	//用户登录
	public final static String LOGIN_VALIDATE_HTTP = URL_API_HOST + "user/login/";	
	//获取sms｛sms/get/？phone=18941134883｝
	//http://119.29.201.102:8001/sms/get/?telNo=18941134883
	public final static String SMS_GET_HTTP = URL_API_HOST + "sms/get/";
	//验证sms｛sms/valid/？code=1234｝->json{"result":success/faild}
	public final static String SMS_VALIDATE_HTTP = URL_API_HOST + "sms/valid/";	
	//推送消息
	///activity/push/
	public static final String USER_NOTICE_HTTP = URL_API_HOST + "activity/push/";
	//app更新信息
	public final static String UPDATE_VERSION = URL_API_HOST+"MobileAppVersion.json";
	//注册头像
	public final static String PIC_UPLOAD = URL_API_HOST+"picture/upload/";
	//注册
	public final static String REGISTE = URL_API_HOST+"user/register/";
	//详情
	public final static String DETAIL = URL_API_HOST+"activity/detail/?activityId=";
	
}
