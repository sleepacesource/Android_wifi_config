package com.bleconfig.demo;


import java.util.ArrayList;
import java.util.List;

import com.bleconfig.demo_m901l.R;
import com.sleepace.sdk.constant.StatusCode;
import com.sleepace.sdk.domain.BleDevice;
import com.sleepace.sdk.interfs.IResultCallback;
import com.sleepace.sdk.manager.CallbackData;
import com.sleepace.sdk.util.SdkLog;
import com.sleepace.sdk.wificonfig.APNBean;
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
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
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

public class APNActivity extends Activity implements OnClickListener{
	private static final String TAG = APNActivity.class.getSimpleName();
	private ImageView ivBack;
	private View vDeviceId;
	private TextView tvTitle, tvDeviceId;
	private EditText etAPN;
	private Button btnGet, btnSet;
	private WiFiConfigHelper wifiConfigHelper;
	
	public static final String EXTRA_DEVICE = "extra_device";
	private BleDevice device;
	private String apn;
	
	private ProgressDialog loadingDialog;
	
	private final int requestCode = 101;//权限请求码
    private boolean hasPermissionDismiss = false;//有权限没有通过
    private String dismissPermission = "";
    private List<String> unauthoPersssions = new ArrayList<String>();
    private String[] permissions = new String[] {Manifest.permission.ACCESS_FINE_LOCATION};
    private SharedPreferences mSetting;
    private boolean granted = false;
    private Handler handler = new Handler();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_apn);
		mSetting = getSharedPreferences("config", Context.MODE_PRIVATE);
		SdkLog.setLogEnable(true);
		
//		SdkLog.setSaveLog(false);
//		String dir = Environment.getExternalStorageDirectory()+"/BleConfigSdkDemo/Log";
//		SdkLog.setLogDir(dir);
		
		wifiConfigHelper = WiFiConfigHelper.getInstance(this);
		findView();
		initListener();
		initUI();
		
//		checkPermissions();
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		SdkLog.log(TAG+" onResume granted:" + granted);
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
                    if(!ActivityCompat.shouldShowRequestPermissionRationale(APNActivity.this, dismissPermission)) {
                    	
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
		etAPN = (EditText) findViewById(R.id.et_apn);
		btnGet = (Button) findViewById(R.id.btn_get);
		btnSet = (Button) findViewById(R.id.btn_set);
	}


	private void initListener() {
		// TODO Auto-generated method stub
		ivBack.setOnClickListener(this);
		vDeviceId.setOnClickListener(this);
		btnGet.setOnClickListener(this);
		btnSet.setOnClickListener(this);
	}


	private void initUI() {
		// TODO Auto-generated method stub
		tvTitle.setText(R.string.set_apn);
		
//		ip = mSetting.getString("ip", "120.24.68.136");
//		port = mSetting.getInt("port", 3010);
		
		apn = mSetting.getString("apn", "");//
		
		etAPN.setText(apn);
		etAPN.setSelection(etAPN.length());
		
		loadingDialog = new ProgressDialog(this);
		loadingDialog.setCancelable(false);
		loadingDialog.setCanceledOnTouchOutside(false);
		loadingDialog.setMessage(getString(R.string.tips_waiting));
	}
	
	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		Editor editor = mSetting.edit();
		editor.putString("ip", etAPN.getText().toString().trim());
		editor.commit();
	}
	
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if(v == ivBack) {
			finish();
		}else if(v == vDeviceId){
			Intent intent = new Intent(this, SearchBleDeviceActivity.class);
			startActivityForResult(intent, 100);
		}else if(v == btnGet) {
			if(TextUtils.isEmpty(tvDeviceId.getText())){
				Toast.makeText(this, R.string.select_device, Toast.LENGTH_SHORT).show();
				return;
			}
			
			if(device == null || device.getDeviceType() == null){
				SdkLog.log(TAG+" config device null");
				return;
			}
			
			loadingDialog.show();
			wifiConfigHelper.apnGet(device.getDeviceType().getType(), device.getAddress(), getAPNCallback);
		}else if(v == btnSet){
			if(TextUtils.isEmpty(tvDeviceId.getText())){
				Toast.makeText(this, R.string.select_device, Toast.LENGTH_SHORT).show();
				return;
			}
			
			if(device == null || device.getDeviceType() == null){
				SdkLog.log(TAG+" config device null");
				return;
			}
			
			apn = etAPN.getText().toString();
			
//			if(TextUtils.isEmpty(apn)){
//				Toast.makeText(this, R.string.hint_apn, Toast.LENGTH_SHORT).show();
//				return;
//			}
			
			loadingDialog.show();
			wifiConfigHelper.apnSet(device.getDeviceType().getType(), device.getAddress(), apn, setAPNCallback);
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
	
	
	private IResultCallback getAPNCallback = new IResultCallback() {
		@Override
		public void onResultCallback(final CallbackData cd) {
			// TODO Auto-generated method stub
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					// TODO Auto-generated method stub
					if(!isActivityAlive(APNActivity.this)){
						return;
					}
					loadingDialog.dismiss();
					SdkLog.log(TAG+" callback " + cd);
					if(cd.isSuccess()) {
						APNBean bean = (APNBean) cd.getResult();
						apn = bean.getApn();
						etAPN.setText(apn);
						etAPN.setSelection(etAPN.length());
					}
				}
			});
		}
	};
	
	private IResultCallback setAPNCallback = new IResultCallback() {
		@Override
		public void onResultCallback(final CallbackData cd) {
			// TODO Auto-generated method stub
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					// TODO Auto-generated method stub
					if(!isActivityAlive(APNActivity.this)){
						return;
					}
					SdkLog.log(TAG+" callback " + cd);
					loadingDialog.dismiss();
					if(cd.getStatus() == StatusCode.DISCONNECT) {
						showConfigResult(APNActivity.this, false);
					}else {
						if(cd.isSuccess()) {
							Toast.makeText(APNActivity.this, R.string.set_suc, Toast.LENGTH_SHORT).show();
						}else {
							Toast.makeText(APNActivity.this, R.string.set_fail, Toast.LENGTH_SHORT).show();
						}
					}
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

        TextView tvTitle = (TextView) dialog.findViewById(R.id.tv_title);
        TextView tvTips = (TextView) dialog.findViewById(R.id.warn_tips);
        Button btn = (Button) dialog.findViewById(R.id.warn_bt);
        
        tvTitle.setVisibility(View.VISIBLE);
        tvTitle.setText(R.string.connect_device_fail);
        int msgRes = result ? R.string.reminder_connect_ble : R.string.reminder_connect_ble;
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








































