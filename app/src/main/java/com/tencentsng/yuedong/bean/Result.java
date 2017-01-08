package com.tencentsng.yuedong.bean;

import java.io.IOException;
import android.util.Log;

import com.tencentsng.yuedong.common.StringUtils;

import org.json.JSONObject;

/**
 * 数据操作结果实体类
 * 
 * @author cliffyan
 * @version 1.0
 * @created 2016-6-10
 */
public class Result {

	private static final String TAG="Result";
	
	private int errorCode;
	private String errorMessage;

	public boolean OK() {
		return errorCode == 1;
	}

	/**
	 * 解析调用结果
	 * @param stream
	 * @return
	 * @throws IOException
	 * @throws Exception
	 */
	public static Result parse(byte[] bytes) throws IOException,
			Exception {
		Log.i(TAG, "parse-start");	
		Result res = null;
		
		// json解析
		try {
			String returnString = new String(bytes, "utf-8");
			if (StringUtils.isEmpty(returnString)) {				
				Log.i(TAG, "从服务器获取消息为空");
				return null;
			}else {
				Log.i(TAG, returnString);		
			}
			
			JSONObject jsonObject =new JSONObject(returnString);

			String message = jsonObject.getString("message");
			Boolean result = jsonObject.getBoolean("result");
			Log.i(TAG, "result:"+result);
			res=new Result();
			if (result.toString().equals("true")) {				
				res.setErrorCode(1);
				Log.i(TAG, "res.setErrorCode(1)");
			}else {
				res.setErrorCode(0);
				Log.i(TAG, "res.setErrorCode(1)");
			}
			res.setErrorMessage(message);
		} catch (Exception e) {
			Log.i(TAG,"parse-"+e.getMessage() );
			return null;
		}

		return res;

	}
	

	public int getErrorCode() {
		return errorCode;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorCode(int errorCode) {
		this.errorCode = errorCode;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	@Override
	public String toString() {
		return String.format("RESULT: CODE:%d,MSG:%s", errorCode, errorMessage);
	}

}
