package com.apconfig.demo;


import com.sleepace.sdk.constant.StatusCode;
import com.sleepace.sdk.interfs.IResultCallBack;
import com.sleepace.sdk.manager.CallbackData;
import com.sleepace.sdk.util.LogUtil;
import com.sleepace.sdk.wificonfig.NetUtils;
import com.sleepace.sdk.wificonfig.WiFiConfigHelper;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements OnClickListener{
	private static final String TAG = MainActivity.class.getSimpleName();
	private TextView tvTitle;
	private ImageView ivArrow;
	private EditText etAddress, etPort, etSsid, etPwd;
	private Button btnConfig;
	private WiFiConfigHelper wifiConfigHelper;
	
	public static final short DEVICE_TYPE_Z4TWB = 26;
	public static final short DEVICE_TYPE_Z4TWP = 27;
	private short deviceType = DEVICE_TYPE_Z4TWB;
	
	private static final String SERVER_IP = "120.24.169.204";
	private static final int SERVER_PORT = 9010;
	private static final String HTTP_URL = "https://webapi.test.sleepace.net";
	
	private String address;
	private int port;
	
	private ProgressDialog loadingDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);
		wifiConfigHelper = WiFiConfigHelper.getInstance(this);
		findView();
		initListener();
		initUI();
	}
	
	
	private void findView() {
		// TODO Auto-generated method stub
		tvTitle = (TextView) findViewById(R.id.tv_title);
		ivArrow = (ImageView) findViewById(R.id.iv_arrow);
		etAddress = (EditText) findViewById(R.id.et_address);
		etPort = (EditText) findViewById(R.id.et_port);
		etSsid = (EditText) findViewById(R.id.et_ssid);
		etPwd = (EditText) findViewById(R.id.et_pwd);
		btnConfig = (Button) findViewById(R.id.btn_config);
	}


	private void initListener() {
		// TODO Auto-generated method stub
		tvTitle.setOnClickListener(this);
		btnConfig.setOnClickListener(this);
	}


	private void initUI() {
		// TODO Auto-generated method stub
		initTitle(deviceType, false);
		initConfig(deviceType);
//		etSsid.setText("medica_supper");
//		etPwd.setText("11221122");
		
		loadingDialog = new ProgressDialog(this);
		loadingDialog.setCancelable(false);
		loadingDialog.setCanceledOnTouchOutside(false);
		loadingDialog.setMessage(getString(R.string.loading_pair_wifi));
	}
	
	private void initConfig(short deviceType){
		if(deviceType == DEVICE_TYPE_Z4TWB){
			address = HTTP_URL;
			port = 0;
			etPort.setVisibility(View.GONE);
			etAddress.setHint(R.string.hint_http);
			etAddress.setText(address);
			etAddress.setSelection(etAddress.length());
		}else{
			address = SERVER_IP;
			port = SERVER_PORT;
			etPort.setVisibility(View.VISIBLE);
			etAddress.setHint(R.string.hint_ip);
			etPort.setHint(R.string.hint_port);
			etAddress.setText(SERVER_IP);
			etAddress.setSelection(etAddress.length());
			etPort.setText(String.valueOf(SERVER_PORT));
			etPort.setSelection(etPort.length());
		}
	}
	
	private void initTitle(short deviceType, boolean dialogShow){
		if(deviceType == DEVICE_TYPE_Z4TWB){
			tvTitle.setText(R.string.device_name_z400twb);
		}else{
			tvTitle.setText(R.string.device_name_z400twp);
		}
		
		if(dialogShow){
			ivArrow.setImageResource(R.drawable.sdk_icon_nav_toparrow);
		}else{
			ivArrow.setImageResource(R.drawable.sdk_icon_nav_downarrow);
		}
	}
	
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if(v == tvTitle){
			initTitle(deviceType, true);
			new SelectDeviceDialog(this).show();
		}else if(v == btnConfig){
			address = etAddress.getText().toString();
			String strPort = etPort.getText().toString();
			String ssid = etSsid.getText().toString();
			String pwd = etPwd.getText().toString();
			
			if(TextUtils.isEmpty(address)){
				if(deviceType == DEVICE_TYPE_Z4TWB){
					Toast.makeText(this, R.string.hint_http, Toast.LENGTH_SHORT).show();
				}else {
					Toast.makeText(this, R.string.hint_ip, Toast.LENGTH_SHORT).show();
				}
				return;
			}
			
			if(deviceType == DEVICE_TYPE_Z4TWP){
				if(TextUtils.isEmpty(strPort)){
					Toast.makeText(this, R.string.hint_port, Toast.LENGTH_SHORT).show();
					return;
				}
			}
			
			if(!NetUtils.isWifiConnected(this)){
				//Toast.makeText(this, R.string.reminder_connect_hotspot2, Toast.LENGTH_SHORT).show();
				CallbackData cd = new CallbackData();
				cd.setStatus(StatusCode.STATUS_DISCONNECT);
				showConfigResult(MainActivity.this, cd);
				return;
			}
			
			if(TextUtils.isEmpty(ssid)){
				Toast.makeText(this, R.string.input_wifi_name, Toast.LENGTH_SHORT).show();
				return;
			}
			
			port = Integer.valueOf(strPort);
			loadingDialog.show();
			wifiConfigHelper.apWiFiConfig(deviceType, address, port, ssid, pwd, callback);
		}
	}
	
	private IResultCallBack callback = new IResultCallBack() {
		@Override
		public void onResultCallback(final CallbackData cd) {
			// TODO Auto-generated method stub
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					// TODO Auto-generated method stub
					if(!isActivityAlive(MainActivity.this)){
						return;
					}
					LogUtil.log(TAG+" callback " + cd);
					loadingDialog.dismiss();
					showConfigResult(MainActivity.this, cd);
					/*if(cd.isSuccess()){
						Toast.makeText(MainActivity.this, R.string.reminder_configuration_success, Toast.LENGTH_SHORT).show();
						Device device = (Device) cd.getResult();
						LogUtil.log(TAG+" config success device:" + device);
					}else{
						Toast.makeText(MainActivity.this, R.string.reminder_configuration_fail, Toast.LENGTH_SHORT).show();
					}*/
				}
			});
		}
	};
	
	public boolean isActivityAlive(Activity activity) {
        if (activity == null || activity.isFinishing() || activity.isDestroyed()) {
            return false;
        }
        return true;
    }
	
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
//		if(keyCode == KeyEvent.KEYCODE_BACK){
//			return true;
//		}
		return super.onKeyDown(keyCode, event);
	}
	
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}
	
	class SelectDeviceDialog extends Dialog implements android.view.View.OnClickListener {
		
		private View vTop, vD1, vD2, vBottom;

		public SelectDeviceDialog(Context context) {
			super(context, R.style.selectDeviceDialog);
			// TODO Auto-generated constructor stub
		}
		
		@Override
		protected void onCreate(Bundle savedInstanceState) {
			// TODO Auto-generated method stub
			super.onCreate(savedInstanceState);
			setContentView(R.layout.dialog_select_device);
			
			vTop = findViewById(R.id.top);
			vD1 = findViewById(R.id.tv_d1);
			vD2 = findViewById(R.id.tv_d2);
			vBottom = findViewById(R.id.bottom);
			
			vTop.setOnClickListener(this);
			vD1.setOnClickListener(this);
			vD2.setOnClickListener(this);
			vBottom.setOnClickListener(this);
			
//			Window win = getWindow();
//	        WindowManager.LayoutParams params = win.getAttributes();
//	        win.setGravity(Gravity.TOP);
//	        params.height = LayoutParams.MATCH_PARENT;
//	        params.dimAmount = 0;
//	        win.setAttributes(params);
	        
	        setCancelable(true);
//	        setCanceledOnTouchOutside(true);
		}
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			if(v == vD1){
				deviceType = DEVICE_TYPE_Z4TWB;
			}else if(v == vD2){
				deviceType = DEVICE_TYPE_Z4TWP;
			}
			
			initTitle(deviceType, false);
			initConfig(deviceType);
			dismiss();
		}
		
	}
	
	
	private void showConfigResult(Context context, CallbackData cd) {
        final Dialog dialog = new Dialog(context, R.style.myDialog);
        dialog.setContentView(R.layout.dialog_warn_tips);

        TextView tvTips = (TextView) dialog.findViewById(R.id.warn_tips);
        Button btn = (Button) dialog.findViewById(R.id.warn_bt);
        
        int msgRes = 0;
        if(cd.isSuccess()){
        	msgRes = R.string.reminder_configuration_success;
        }else{
        	if(cd.getStatus() == StatusCode.STATUS_DISCONNECT){
        		msgRes = R.string.reminder_connect_hotspot2;
        	}else{
        		msgRes = R.string.reminder_configuration_fail;
        	}
        }
        
        tvTips.setText(msgRes);
        btn.setText(R.string.btn_ok);

        OnClickListener mListner = new OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        };

        btn.setOnClickListener(mListner);
        Window win = dialog.getWindow();
        WindowManager.LayoutParams lp = win.getAttributes();
        lp.width = WindowManager.LayoutParams.WRAP_CONTENT;
        win.setAttributes(lp);

        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }
	
}








































