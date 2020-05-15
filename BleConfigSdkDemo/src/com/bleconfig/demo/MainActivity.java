package com.bleconfig.demo;


import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.sleepace.sdk.domain.BleDevice;
import com.sleepace.sdk.interfs.IResultCallback;
import com.sleepace.sdk.manager.CallbackData;
import com.sleepace.sdk.util.SdkLog;
import com.sleepace.sdk.wificonfig.WiFiConfigHelper;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
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
	private String ip;
	private int port;
	
	private ProgressDialog loadingDialog;
	
	
	private final int requestCode = 101;//权限请求码
    private boolean hasPermissionDismiss = false;//有权限没有通过
    private String dismissPermission = "";
    private List<String> unauthoPersssions = new ArrayList<String>();
    private String[] permissions = new String[] {Manifest.permission.ACCESS_FINE_LOCATION };
    private byte[] ssidRaw;
    private SharedPreferences mSetting;
    private boolean granted = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);
		mSetting = getSharedPreferences("config", Context.MODE_PRIVATE);
		SdkLog.setLogEnable(true);
		
		wifiConfigHelper = WiFiConfigHelper.getInstance(this);
		findView();
		initListener();
		initUI();
		
		checkPermissions();
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		SdkLog.log(TAG+" onResume granted:" + granted);
		if(granted) {
			getSSID();
		}
	}
	
	private void checkPermissions() {
		granted = false;
		if(Build.VERSION.SDK_INT >= 23) {
			unauthoPersssions.clear();
			//逐个判断你要的权限是否已经通过
			for (int i = 0; i < permissions.length; i++) {
				if (ContextCompat.checkSelfPermission(this, permissions[i]) != PackageManager.PERMISSION_GRANTED) {
					unauthoPersssions.add(permissions[i]);//添加还未授予的权限
				}
			}
			//申请权限
			if (unauthoPersssions.size() > 0) {//有权限没有通过，需要申请
				ActivityCompat.requestPermissions(this, new String[]{unauthoPersssions.get(0)}, requestCode);
			}else {
				granted = true;
			}
		}else {
			granted = true;
		}
    }
	
	@Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        hasPermissionDismiss = false;
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (this.requestCode == requestCode) {
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                    hasPermissionDismiss = true;
                    dismissPermission = permissions[i];
                    if(!ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, dismissPermission)) {
                    	
                    }
                    break;
                }
            }

            //如果有权限没有被允许
            if (hasPermissionDismiss) {
            	
            }else{
                checkPermissions();
            }
        }
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
		etSsid.setOnClickListener(this);
	}


	private void initUI() {
		// TODO Auto-generated method stub
		ivBack.setVisibility(View.GONE);
		tvTitle.setText(R.string.demo_name_ble_wifi);
		etPwd.setText("");
		//etPwd.setText("88888888");
		etPwd.setSelection(etPwd.length());
		etPwd.requestFocus();
		
		ip = mSetting.getString("ip", "120.24.68.136");
		port = mSetting.getInt("port", 3010);
		
		etAddress.setText(ip);
		etAddress.setSelection(etAddress.length());
		etPort.setText(String.valueOf(port));
//		etPort.setSelection(etPort.length());
		
		loadingDialog = new ProgressDialog(this);
		loadingDialog.setCancelable(false);
		loadingDialog.setCanceledOnTouchOutside(false);
		loadingDialog.setMessage(getString(R.string.loading_pair_wifi));
	}
	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		Editor editor = mSetting.edit();
		editor.putString("ip", etAddress.getText().toString().trim());
		editor.putInt("port", Integer.valueOf(etPort.getText().toString().trim()));
		editor.commit();
	}
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if(v == etSsid) {
			Intent intent = new Intent();
			intent.setAction("android.net.wifi.PICK_WIFI_NETWORK");
			startActivity(intent);
		}else if(v == vDeviceId){
			Intent intent = new Intent(this, SearchBleDeviceActivity.class);
			startActivityForResult(intent, 100);
		}else if(v == btnConfig){
			if(TextUtils.isEmpty(tvDeviceId.getText())){
				Toast.makeText(this, R.string.select_device, Toast.LENGTH_SHORT).show();
				return;
			}
			
			if(device == null || device.getDeviceType() == null){
				SdkLog.log(TAG+" config device null");
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
			wifiConfigHelper.bleWiFiConfig(device.getDeviceType().getType(), device.getAddress(), ip, port, ssidRaw, pwd, callback);
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
	
	private void getSSID() {
		String ssid = "";
        WifiManager wm = (WifiManager) getSystemService(WIFI_SERVICE);
        if (wm != null) {
            WifiInfo winfo = wm.getConnectionInfo();
            if (winfo != null) {
            	ssidRaw = getWifiSsidRawData(winfo);
                ssid = winfo.getSSID();
//                SdkLog.log(TAG+" getSSID 1:"+ssid);
                if (!TextUtils.isEmpty(ssid)) {
                	if(ssid.length() > 2 && ssid.charAt(0) == '"' && ssid.charAt(ssid.length() - 1) == '"') {
                		ssid = ssid.substring(1, ssid.length() - 1);
                	}
                }else {
                	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
                        if(cm != null){
                            NetworkInfo nInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                            if(nInfo != null){
                                ssid = nInfo.getExtraInfo();
//                                SdkLog.log(TAG+" getSSID 2:"+ssid);
                                if(!TextUtils.isEmpty(ssid) && ssid.charAt(0) == '"' && ssid.charAt(ssid.length() - 1) == '"'){
                                    ssid = ssid.substring(1, ssid.length() - 1);
                                }
                            }
                        }
                    }
                }
            }
        }
        
        SdkLog.log(TAG+" getSSID:"+ssid);
        etSsid.setText(ssid);
    }
	
	private byte[] getWifiSsidRawData(WifiInfo wifiInfo) {
        try {
            Method method = wifiInfo.getClass().getMethod("getWifiSsid");
            method.setAccessible(true);
            Object wifiSsid = method.invoke(wifiInfo);
            SdkLog.log(TAG+" getWifiSsidRawData wifiSsid:"+wifiSsid);
            method = wifiSsid.getClass().getMethod("getOctets");
            method.setAccessible(true);
            byte[] rawSsid = (byte[]) method.invoke(wifiSsid);
            SdkLog.log(TAG+" getWifiSsidRawData rawSsid:"+Arrays.toString(rawSsid));
            return rawSsid;
        } catch (Exception e) {
            e.printStackTrace();
            SdkLog.log(TAG+" getWifiSsidRawData err:"+e.getMessage());
        }
        return null;
    }

}








































