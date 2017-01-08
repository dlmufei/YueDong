package com.tencentsng.yuedong.ui;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;

import com.tencentsng.yuedong.R;
import com.tencentsng.yuedong.api.ApiHttpCilentUtil;
import com.tencentsng.yuedong.common.FileUtils;
import com.tencentsng.yuedong.common.RegValidUtils;
import com.tencentsng.yuedong.common.StringUtils;
import com.tencentsng.yuedong.common.SystemUtils;
import com.tencentsng.yuedong.common.UIHelper;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Toast;

public class Registe extends BaseActivity {

	private static final String TAG = "REGIST";

	private static final int IMAGE_REQUEST_CODE = 0;
	private static final int CAMERA_REQUEST_CODE = 1;
	private static final int RESIZE_REQUEST_CODE = 2;

	public static final String IMAGE_FILE_NAME = "header.jpg";

	private ImageView ivHead;
	private EditText etNikeName;
	private EditText etfirstPwd;
	private EditText etSecondPwd;
	private EditText etPhone;
	private EditText etCode;
	private Button btnSms;

	private String strPhone;
	private String strNikeName;
	private String strFirstPwd;
	private String strSecondPwd;
	private String strCode;

	private PopupWindow menuWindow = null;

	CountDownTimer timer = new CountDownTimer(60000, 1000) {

		@Override
		public void onTick(long millisUntilFinished) {
			btnSms.setText("剩余" + millisUntilFinished / 1000 + "秒");
			btnSms.setEnabled(false);
		}

		@Override
		public void onFinish() {
			btnSms.setEnabled(true);
			btnSms.setText("重发验证码");
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.acitivity_regist);

		initview();

	}

	/**
	 * 初始化控件
	 */
	private void initview() {
		ivHead = (ImageView) findViewById(R.id.iv_head);
		etNikeName = (EditText) findViewById(R.id.et_nickname);
		etfirstPwd = (EditText) findViewById(R.id.et_firstpwd);
		etSecondPwd = (EditText) findViewById(R.id.et_secondpwd);
		etPhone = (EditText) findViewById(R.id.et_phone);
		etCode = (EditText) findViewById(R.id.et_code);
		btnSms = (Button) findViewById(R.id.btn_sms);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode != RESULT_OK) {
			// 删除取消设置的头像图片
			if (FileUtils.deleteFile(IMAGE_FILE_NAME)) {
				Log.i(TAG, "onActivityResult-deleteFile-true");
			} else {
				Log.i(TAG, "onActivityResult-deleteFile-false");
			}
			// 将iv设置为默认图片
			ivHead.setImageResource(R.drawable.head);

			return;
		} else {
			switch (requestCode) {
			case IMAGE_REQUEST_CODE:
				resizeImage(data.getData());

				break;
			case CAMERA_REQUEST_CODE:
				if (SystemUtils.isSdcardExisting()) {
					resizeImage(getImageUri());
				} else {
					UIHelper.ToastMessage(getApplicationContext(),
							"未找到存储卡，无法存储照片！");
				}
				break;

			case RESIZE_REQUEST_CODE:
				if (data != null) {
					showResizeImage(data);
				}
				break;

			}
		}

		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	protected void onDestroy() {
		timer.cancel();
		super.onDestroy();
	}

	/**
	 * 返回
	 * 
	 * @param view
	 */
	public void clickReturn(View view) {
		this.finish();
	}

	/**
	 * 注册
	 * 
	 * @param view
	 */
	public void clickregist(View view) {
		strNikeName = etNikeName.getText().toString();
		strPhone = etPhone.getText().toString();
		strFirstPwd = etfirstPwd.getText().toString();
		strSecondPwd = etSecondPwd.getText().toString();
		strCode = etCode.getText().toString();
		if (StringUtils.isEmpty(strNikeName) || StringUtils.isEmpty(strPhone)
				|| StringUtils.isEmpty(strFirstPwd)
				|| StringUtils.isEmpty(strSecondPwd)
				|| StringUtils.isEmpty(strCode)) {
			UIHelper.ToastMessage(getApplicationContext(), "请输入完整的注册信息");
			return;
		}

		if (!RegValidUtils.IsMobilephone(strPhone)) {
			UIHelper.ToastMessage(getApplicationContext(), "电话信息不正确");
			return;
		}

		if (!strFirstPwd.equals(strSecondPwd)) {
			UIHelper.ToastMessage(getApplicationContext(), "两次输入密码不一样");
			return;
		}
		if (UIHelper.isFastDoubleClick(2000)) {
			Toast.makeText(getApplicationContext(), "请勿快速连续点击！",
					Toast.LENGTH_SHORT).show();
			return;
		}

		try {

			if (FileUtils.checkFileExists(IMAGE_FILE_NAME)) {
				Log.i(TAG, "clickregist-自定义头像");
				ApiHttpCilentUtil.regist_1(getApplicationContext(), new File(
						Environment.getExternalStorageDirectory()
								.getAbsolutePath(), IMAGE_FILE_NAME),
						strNikeName, strFirstPwd, strPhone, strCode);
			} else {
				Log.i(TAG, "clickregist-默认头像");
				ApiHttpCilentUtil.registNoHead_1(getApplicationContext(),
						strNikeName, strFirstPwd, strPhone, strCode);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * 点击用户头像
	 * 
	 * @param view
	 */
	public void clickhead(View view) {
		View v = View.inflate(getApplicationContext(), R.layout.pop_headimage,
				null);

		menuWindow = new PopupWindow(v, LinearLayout.LayoutParams.MATCH_PARENT,
				LinearLayout.LayoutParams.WRAP_CONTENT, true);
		menuWindow.showAtLocation(findViewById(R.id.ll_registmain),
				Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
		Button btnPickPhoto = (Button) v.findViewById(R.id.btnPickPhoto);
		Button btnTakePhoto = (Button) v.findViewById(R.id.btnTakePhoto);
		Button btnPhotoCancel = (Button) v.findViewById(R.id.btnPhoteCancel);
		btnPhotoCancel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				menuWindow.dismiss();
				Log.i(TAG, "取消");

			}
		});

		btnTakePhoto.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				Log.i(TAG, "拍照");
				Intent cameraIntent = new Intent(
						"android.media.action.IMAGE_CAPTURE");
				cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, getImageUri());
				cameraIntent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 0);
				startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE);

				if (menuWindow != null) {
					menuWindow.dismiss();
				}

			}
		});

		btnPickPhoto.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Log.i(TAG, "相册");

				if (SystemUtils.isSdcardExisting()) {

					Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
					galleryIntent.addCategory(Intent.CATEGORY_OPENABLE);
					galleryIntent.setType("image/*");
					startActivityForResult(galleryIntent, IMAGE_REQUEST_CODE);

					if (menuWindow != null) {
						menuWindow.dismiss();
					}

				} else {
					UIHelper.ToastMessage(getApplicationContext(), "请插入sd卡");
				}

			}
		});

	}

	/**
	 * 获取图片的URI地址
	 * @return
	 */
	private Uri getImageUri() {
		return Uri.fromFile(new File(Environment.getExternalStorageDirectory()
				.getAbsolutePath(), IMAGE_FILE_NAME));
	}

	/**
	 * 调用
	 * @param uri
	 */
	public void resizeImage(Uri uri) {
		Intent intent = new Intent("com.android.camera.action.CROP");
		intent.setDataAndType(uri, "image/*");
		intent.putExtra("crop", "true");
		intent.putExtra("aspectX", 1);
		intent.putExtra("aspectY", 1);
		intent.putExtra("outputX", 150);
		intent.putExtra("outputY", 150);
		intent.putExtra("return-data", true);
		startActivityForResult(intent, RESIZE_REQUEST_CODE);
	}

	boolean saveBitmap2file(Bitmap bmp, String filename) {
		CompressFormat format = Bitmap.CompressFormat.PNG;
		int quality = 100;
		OutputStream stream = null;
		try {
			Log.i(TAG, "saveBitmap2file:"
					+ Environment.getExternalStorageDirectory()
							.getAbsolutePath() + "/" + filename);
			stream = new FileOutputStream(Environment
					.getExternalStorageDirectory().getAbsolutePath()
					+ "/"
					+ filename);
			Log.i(TAG, "saveBitmap2file:"
					+ Environment.getExternalStorageDirectory()
							.getAbsolutePath() + "/" + filename);
		} catch (FileNotFoundException e) {
			Log.i(TAG, "saveBitmap2file-FileNotFoundException");
			e.printStackTrace();
		}
		return bmp.compress(format, quality, stream);
	}

	/**
	 * 
	 * @param data
	 */
	private void showResizeImage(Intent data) {
		Bundle extras = data.getExtras();
		if (extras != null) {
			Bitmap photo = extras.getParcelable("data");
			saveBitmap2file(photo, IMAGE_FILE_NAME);
			Drawable drawable = new BitmapDrawable(photo);
			ivHead.setImageDrawable(drawable);
		}
	}

	/**
	 * 获取短信验证码事件
	 * 
	 * @param view
	 */
	public void clickGetSms(View view) {
		strPhone = etPhone.getText().toString();
		if (StringUtils.isEmpty(strPhone)) {
			UIHelper.ToastMessage(getApplicationContext(), "请先填写电话信息");
			return;
		}
		if (!RegValidUtils.IsMobilephone(strPhone)) {
			UIHelper.ToastMessage(getApplicationContext(), "电话信息不正确");
			return;
		}

		ApiHttpCilentUtil.getSms(getApplicationContext(), strPhone);
		timer.start();
	}

}
