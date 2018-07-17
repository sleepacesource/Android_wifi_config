package com.bleconfig.demo;

public class BleDeviceNameUtil {

    public static String getBleDeviceName(int type, byte []record ) {
		byte[] data = null;
		int index = 0;
		while (index < record.length) {
			int len = record[index] & 0xFF;
			int tp = record[index + 1] & 0xFF;
			if (index + len + 1 > 31) {
				break;
			} else if (len == 0) {
				break;
			}
			if (type == tp) {
				data = new byte[len - 1];
				for (int i = 0; i < len - 1; i++) {
					data[i] = record[index + 2 + i];
				}
				break;
			}
			index += (len + 1);
		}

		if(data != null){
			return new String(data);
		}

		return null;
	}
}
