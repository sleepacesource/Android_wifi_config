package com.bleconfig.demo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sleepace.sdk.domain.BleDevice;
import com.sleepace.sdk.interfs.IBleScanCallback;
import com.sleepace.sdk.manager.DeviceType;
import com.sleepace.sdk.manager.ble.BleHelper;
import com.sleepace.sdk.util.SdkLog;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;


public class SearchBleDeviceActivity extends Activity implements OnClickListener {
	private static final String TAG = SearchBleDeviceActivity.class.getSimpleName();
	private ImageView ivBack, ivRefresh;
	private TextView tvTitle, tvRefresh;
    private View vRefresh;
    private ListView listView;
    private LayoutInflater inflater;
    private BleAdapter adapter;
    private RotateAnimation animation;
    private BleHelper bleHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_search_ble);
        bleHelper = BleHelper.getInstance(this);
        findView();
        initListener();
        initUI();
    }


    public void findView() {
    	tvTitle = (TextView) findViewById(R.id.tv_title);
    	ivBack = (ImageView) findViewById(R.id.iv_back);
        vRefresh = findViewById(R.id.layout_refresh);
        tvRefresh = (TextView) findViewById(R.id.tv_refresh);
        ivRefresh = (ImageView) findViewById(R.id.iv_refresh);
        listView = (ListView) findViewById(R.id.list);
    }

    public void initListener() {
    	ivBack.setOnClickListener(this);
        vRefresh.setOnClickListener(this);
        listView.setOnItemClickListener(onItemClickListener);
    }

    public void initUI() {
        inflater = getLayoutInflater();

        animation = new RotateAnimation(0f, 360f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        animation.setDuration(2000);//设置动画持续时间
        animation.setRepeatCount(Animation.INFINITE);
        animation.setInterpolator(new LinearInterpolator());
        
        tvTitle.setText(R.string.search_device);
        adapter = new BleAdapter();
        listView.setAdapter(adapter);
        
        vRefresh.performClick();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }
    
    @Override
    protected void onDestroy() {
    	// TODO Auto-generated method stub
    	super.onDestroy();
    	bleHelper.stopScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == BleHelper.REQCODE_OPEN_BT) {
            
        }
    }

    private OnItemClickListener onItemClickListener = new OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        	bleHelper.stopScan();
        	BleDevice device = adapter.getItem(position);
        	Intent data = new Intent();
        	data.putExtra(MainActivity.EXTRA_DEVICE, device);
        	setResult(RESULT_OK, data);
        	finish();
        }
    };


    private void initRefreshView() {
        tvRefresh.setText(R.string.refresh_deviceid_list);
        ivRefresh.clearAnimation();
        ivRefresh.setImageResource(R.drawable.bg_refresh);
    }

    private void initSearchView() {
        tvRefresh.setText(R.string.refreshing);
        ivRefresh.setImageResource(R.drawable.device_loading);
        ivRefresh.startAnimation(animation);
    }

    @Override
    public void onClick(View v) {
    	if(v == ivBack){
    		finish();
    	}else if (v == vRefresh) {
        	if(bleHelper.isBluetoothOpen()){
        		bleHelper.scanBleDevice(scanCallback);
        	}else{
        		Intent enabler = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
    			startActivityForResult(enabler, BleHelper.REQCODE_OPEN_BT);
        	}
        }
    }
    
    private IBleScanCallback scanCallback = new IBleScanCallback() {
		
		@Override
		public void onStopScan() {
			// TODO Auto-generated method stub
			initRefreshView();
		}
		
		@Override
		public void onStartScan() {
			// TODO Auto-generated method stub
			initSearchView();
    		adapter.clearData();
		}
		
		@Override
		public void onLeScan(final BluetoothDevice device, final int rssi, final byte[] scanRecord) {
			// TODO Auto-generated method stub
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					// TODO Auto-generated method stub
					String modelName = device.getName();
					if(modelName != null){
						modelName = modelName.trim();
					}
		            String deviceName = BleDeviceNameUtil.getBleDeviceName(0xff, scanRecord);
		            if(deviceName != null){
		            	deviceName = deviceName.trim();
		            }
		            
		            SdkLog.log(TAG+" onLeScan modelName:" +modelName+",deviceName:" + deviceName+",scanRecord:" + Arrays.toString(scanRecord));
		            if(/*!TextUtils.isEmpty(deviceName) &&*/ checkDeviceName(deviceName)){
		            	BleDevice ble = new BleDevice();
		            	ble.setModelName(modelName);
		            	ble.setAddress(device.getAddress());
		            	ble.setDeviceName(deviceName);
		            	ble.setDeviceId(deviceName);
		            	ble.setDeviceType(getDeviceTypeByName(deviceName));
		            	adapter.addBleDevice(ble);
		            }
				}
			});
		}
	};
	
	private DeviceType getDeviceTypeByName(String deviceName) {
		if(checkRestOnZ300(deviceName)) {
			return DeviceType.DEVICE_TYPE_Z3;
		}else if(checkEW201W(deviceName)) {
			return DeviceType.DEVICE_TYPE_EW201W;
		}else if(checkEW202W(deviceName)) {
			return DeviceType.DEVICE_TYPE_EW202W;
		}else if(checkNoxSAW(deviceName)) {
			return DeviceType.DEVICE_TYPE_NOX_SAW;
		}else if(checkM600(deviceName)) {
			return DeviceType.DEVICE_TYPE_M600;
		}else if(checkM800(deviceName)) {
			return DeviceType.DEVICE_TYPE_M800;
		}else if(checkBM8701(deviceName)) {
			return DeviceType.DEVICE_TYPE_BM8701;
		}else if(checkBM8701_2(deviceName)) {
			return DeviceType.DEVICE_TYPE_BM8701_2;
		}else if(checkM8701W(deviceName)) {
			return DeviceType.DEVICE_TYPE_M8701W;
		}else if(checkBG001A(deviceName)) {
			return DeviceType.DEVICE_TYPE_BG001A;
		}else if(checkBG002(deviceName)) {
			return DeviceType.DEVICE_TYPE_BG002;
		}else if(checkSN913E(deviceName)) {
			return DeviceType.DEVICE_TYPE_SN913E;
		}else if(checkFH601W(deviceName)) {
			return DeviceType.DEVICE_TYPE_FH601W;
		}else if(checkNox2W(deviceName)) {
			return DeviceType.DEVICE_TYPE_NOX_2W;
		}else if(checkZ400TWP3(deviceName)) {
			return DeviceType.DEVICE_TYPE_Z400TWP_3;
		}else if(checkSM100(deviceName)) {
			return DeviceType.DEVICE_TYPE_SM100;
		}else if(checkSM200(deviceName)) {
			return DeviceType.DEVICE_TYPE_SM200;
		}else if(checkSM300(deviceName)) {
			return DeviceType.DEVICE_TYPE_SM300;
		}else if(checkSDC10(deviceName)) {
			return DeviceType.DEVICE_TYPE_SDC10;
		}else if(checkM901L(deviceName)) {
			return DeviceType.DEVICE_TYPE_M901L;
		}
		return null;
	}
	
	/**
	 * 剔除设备名是乱码的设备
	 * @param deviceName
	 * @return
	 */
	private boolean checkDeviceName(String deviceName) {
        if (deviceName == null) return false;
        Pattern p = Pattern.compile("[0-9a-zA-Z-]+");
        Matcher m = p.matcher(deviceName);
        return m.matches();
    }
	
	private boolean checkRestOnZ300(String deviceName) {
		if (deviceName == null) return false;
		Pattern p = Pattern.compile("^(Z3)[0-9a-zA-Z-]{11}$");
		Matcher m = p.matcher(deviceName);
		return m.matches();
	}
	
	private boolean checkZ400TWP3(String deviceName) {
		if (deviceName == null) return false;
		Pattern p = Pattern.compile("^(ZTW3)[0-9a-zA-Z]{9}$");
		Matcher m = p.matcher(deviceName);
		return m.matches();
	}
	
	private boolean checkBG001A(String deviceName) {
		if (deviceName == null) return false;
		Pattern p1 = Pattern.compile("^(GW001)[0-9a-zA-Z-]{8}$");
		Matcher m1 = p1.matcher(deviceName);
		Pattern p2 = Pattern.compile("^(BG01A)[0-9a-zA-Z-]{8}$");
		Matcher m2 = p2.matcher(deviceName);
		return m1.matches() || m2.matches();
	}
	
	private boolean checkBG002(String deviceName) {
		if (deviceName == null) return false;
		Pattern p1 = Pattern.compile("^(BG02)[0-9a-zA-Z-]{9}$");
		Matcher m1 = p1.matcher(deviceName);
		return m1.matches();
	}
	
	private boolean checkSN913E(String deviceName) {
		if (deviceName == null) return false;
		Pattern p = Pattern.compile("^(SN91E)[0-9a-zA-Z-]{8}$");
		Matcher m = p.matcher(deviceName);
		return m.matches();
	}
	
	private boolean checkM600(String deviceName) {
		if (deviceName == null) return false;
		Pattern p = Pattern.compile("^(M6)[0-9a-zA-Z-]{11}$");
		Matcher m = p.matcher(deviceName);
		return m.matches();
	}
	
	private boolean checkM800(String deviceName) {
		if (deviceName == null) return false;
		Pattern p1 = Pattern.compile("^(M8)[0-9a-zA-Z-]{11}$");
		Matcher m1 = p1.matcher(deviceName);
		return m1.matches();
	}
	
	private boolean checkBM8701(String deviceName) {
		if (deviceName == null) return false;
		Pattern p1 = Pattern.compile("^(BM871)[0-9a-zA-Z-]{8}$");
		Matcher m1 = p1.matcher(deviceName);
		return m1.matches();
	}
	
	private boolean checkBM8701_2(String deviceName) {
		if (deviceName == null) return false;
		Pattern p1 = Pattern.compile("^(BM872)[0-9a-zA-Z-]{8}$");
		Matcher m1 = p1.matcher(deviceName);
		return m1.matches();
	}
	
	private boolean checkM8701W(String deviceName) {
		if (deviceName == null) return false;
		Pattern p1 = Pattern.compile("^(M871W)[0-9a-zA-Z-]{8}$");
		Matcher m1 = p1.matcher(deviceName);
		return m1.matches();
	}
	
	private boolean checkEW201W(String deviceName) {
		if (deviceName == null) return false;
		Pattern p = Pattern.compile("^(EW1W)[0-9a-zA-Z-]{9}$");
		Matcher m = p.matcher(deviceName);
		return m.matches();
	}
	
	private boolean checkEW202W(String deviceName) {
		if (deviceName == null) return false;
		Pattern p = Pattern.compile("^(EW22W)[0-9a-zA-Z-]{8}$");
		Matcher m = p.matcher(deviceName);
		return m.matches();
	}
	
	private static boolean checkNoxSAW(String deviceName) {
        if (deviceName == null) return false;
        Pattern p = Pattern.compile("^(SA11)[0-9a-zA-Z-]{9}$");
        Matcher m = p.matcher(deviceName);
        return m.matches();
    }
	
	private static boolean checkFH601W(String deviceName) {
		if (deviceName == null) return false;
		Pattern p = Pattern.compile("^(FH61W)[0-9a-zA-Z-]{8}$");
		Matcher m = p.matcher(deviceName);
		return m.matches();
	}
    
	public static boolean checkNox2W(String deviceName) {
        if (deviceName == null) return false;
        Pattern p = Pattern.compile("^(SN22)[0-9a-zA-Z-]{9}$");
        Matcher m = p.matcher(deviceName);
        return m.matches();
    }
	
	public static boolean checkSM100(String deviceName) {
		if (deviceName == null) return false;
		Pattern p = Pattern.compile("^(SM100)[0-9a-zA-Z]{8}$");
		Matcher m = p.matcher(deviceName);
		return m.matches();
	}
	
	public static boolean checkSM200(String deviceName) {
		if (deviceName == null) return false;
		Pattern p = Pattern.compile("^(SM200)[0-9a-zA-Z]{8}$");
		Matcher m = p.matcher(deviceName);
		return m.matches();
	}
	
	public static boolean checkSM300(String deviceName) {
		if (deviceName == null) return false;
		Pattern p = Pattern.compile("^(SM300)[0-9a-zA-Z]{8}$");
		Matcher m = p.matcher(deviceName);
		return m.matches();
	}
	
	public static boolean checkSDC10(String deviceName) {
		if (deviceName == null) return false;
		Pattern p = Pattern.compile("^(SDC10)[0-9a-zA-Z]{8}$");
		Matcher m = p.matcher(deviceName);
		return m.matches();
	}
	
	public static boolean checkM901L(String deviceName) {
		if (deviceName == null) return false;
		Pattern p = Pattern.compile("^(M901L)[0-9a-zA-Z]{8}$");
		Matcher m = p.matcher(deviceName);
		return m.matches();
	}
	

    class BleAdapter extends BaseAdapter {
        private List<BleDevice> list = new ArrayList<BleDevice>();
        

        class ViewHolder {
            TextView tvName;
            TextView tvDeviceId;
        }

        @Override
        public int getCount() {

            return list.size();
        }

        @Override
        public BleDevice getItem(int position) {

            return list.get(position);
        }

        @Override
        public long getItemId(int position) {

            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            ViewHolder holder = null;
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.list_device_item, null);
                holder = new ViewHolder();
                holder.tvName = (TextView) convertView.findViewById(R.id.tv_name);
                holder.tvDeviceId = (TextView) convertView.findViewById(R.id.tv_deviceid);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            BleDevice item = getItem(position);
            holder.tvName.setText(item.getModelName());
            holder.tvDeviceId.setText(item.getDeviceName());
            return convertView;
        }

        public void addBleDevice(BleDevice bleDevice) {

            boolean exists = false;
            for (BleDevice d : list) {
                if (d.getAddress().equals(bleDevice.getAddress())) {
                    exists = true;
                    break;
                }
            }

            if (!exists) {
                list.add(bleDevice);
                notifyDataSetChanged();
            }
        }

        public List<BleDevice> getData() {
            return list;
        }

        public void clearData() {
            list.clear();
            notifyDataSetChanged();
        }
    }
}
