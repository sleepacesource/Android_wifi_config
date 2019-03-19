package com.bleconfig.demo;


import com.sleepace.sdk.domain.BleDevice;
import com.sleepace.sdk.interfs.IResultCallback;
import com.sleepace.sdk.manager.CallbackData;
import com.sleepace.sdk.util.SdkLog;
import com.sleepace.sdk.wificonfig.WiFiConfigHelper;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
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
	private ImageView ivBack;
	private View vDeviceId;
	private TextView tvTitle, tvDeviceId;
	private EditText etAddress, etPort, etSsid, etPwd;
	private Button btnConfig;
	private WiFiConfigHelper wifiConfigHelper;
	
	public static final String EXTRA_DEVICE = "extra_device";
	private BleDevice device;
	private String ip = "120.24.169.204";
	private int port = 9010;
	
	private ProgressDialog loadingDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);
		SdkLog.setLogEnable(true);
		wifiConfigHelper = WiFiConfigHelper.getInstance(this);
		findView();
		initListener();
		initUI();
	}
	
	
	private void findView() {
		// TODO Auto-generated method stub
		ivBack = (ImageView) findViewById(R.id.iv_back);
		tvTitle = (TextView) findViewById(R.id.tv_title);
		vDeviceId = findViewById(R.id.layout_deviceid);
		tvDeviceId = (TextView) findViewById(R.id.tv_deviceid);
		etAddress = (EditText) findViewById(R.id.et_address);
		etPort = (EditText) findViewById(R.id.et_port);
		etSsid = (EditText) findViewById(R.id.et_ssid);
		etPwd = (EditText) findViewById(R.id.et_pwd);
		btnConfig = (Button) findViewById(R.id.btn_config);
	}


	private void initListener() {
		// TODO Auto-generated method stub
		tvTitle.setOnClickListener(this);
		vDeviceId.setOnClickListener(this);
		btnConfig.setOnClickListener(this);
	}


	private void initUI() {
		// TODO Auto-generated method stub
		ivBack.setVisibility(View.GONE);
		tvTitle.setText(R.string.demo_name_ble_wifi);
		etSsid.setText("medica_1");
		etPwd.setText("11221122");
		
		etAddress.setText(ip);
		etAddress.setSelection(etAddress.length());
		etPort.setText(String.valueOf(port));
		etPort.setSelection(etPort.length());
		
		loadingDialog = new ProgressDialog(this);
		loadingDialog.setCancelable(false);
		loadingDialog.setCanceledOnTouchOutside(false);
		loadingDialog.setMessage(getString(R.string.loading_pair_wifi));
	}
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if(v == vDeviceId){
			Intent intent = new Intent(this, SearchBleDeviceActivity.class);
			startActivityForResult(intent, 100);
		}else if(v == btnConfig){
			
			SdkLog.log(TAG+" onClick dType:" + device.getDeviceType());
			
			if(TextUtils.isEmpty(tvDeviceId.getText())){
				Toast.makeText(this, R.string.select_device, Toast.LENGTH_SHORT).show();
				return;
			}
			
			if(device == null || device.getDeviceType() == null){
				return;
			}
			
			ip = etAddress.getText().toString();
			String strPort = etPort.getText().toString();
			
			if(TextUtils.isEmpty(ip)){
				Toast.makeText(this, R.string.hint_ip, Toast.LENGTH_SHORT).show();
				return;
			}
			
			if(TextUtils.isEmpty(strPort)){
				Toast.makeText(this, R.string.hint_port, Toast.LENGTH_SHORT).show();
				return;
			}
			
			port = Integer.valueOf(strPort);
			String ssid = etSsid.getText().toString();
			String pwd = etPwd.getText().toString();
			if(TextUtils.isEmpty(ssid)){
				Toast.makeText(this, R.string.input_wifi_name, Toast.LENGTH_SHORT).show();
				return;
			}
			
			loadingDialog.show();
			wifiConfigHelper.bleWiFiConfig(device.getDeviceType().getType(), device.getAddress(), ip, port, ssid, pwd, callback);
		}
	}
	
	
	@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK) {
            device = (BleDevice) data.getSerializableExtra(EXTRA_DEVICE);
            tvDeviceId.setText(device.getDeviceName());
        }
        SdkLog.log(TAG+" onActivityResult req:" + requestCode+",res:" + resultCode+",d:" + device);
    }
	
	
	private IResultCallback callback = new IResultCallback() {
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
					SdkLog.log(TAG+" callback " + cd);
					loadingDialog.dismiss();
					showConfigResult(MainActivity.this, cd.isSuccess());
					
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
	
	private void showConfigResult(Context context, boolean result) {
        final Dialog dialog = new Dialog(context, R.style.myDialog);
        dialog.setContentView(R.layout.dialog_warn_tips);

        TextView tvTips = (TextView) dialog.findViewById(R.id.warn_tips);
        Button btn = (Button) dialog.findViewById(R.id.warn_bt);
        
        int msgRes = result ? R.string.reminder_configuration_success : R.string.reminder_configuration_fail;
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








































