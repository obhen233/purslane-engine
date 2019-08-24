package com.sunbox.util;

import java.security.MessageDigest;
import java.util.UUID;

public class CodingUtil {
	
	public static String MD5(String src){
		 char hexDigits[] = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
	        try {
	            byte[] btInput = src.getBytes("UTF-8");
	            MessageDigest mdInst = MessageDigest.getInstance("MD5");
	            mdInst.update(btInput);
	            byte[] md = mdInst.digest();
	            int j = md.length;
	            char str[] = new char[j * 2];
	            int k = 0;
	            for (int i = 0; i < j; i++) {
	                byte byte0 = md[i];
	                str[k++] = hexDigits[byte0 >>> 4 & 0xf];
	                str[k++] = hexDigits[byte0 & 0xf];
	            }
	            return new String(str).toUpperCase();
	        } catch (Exception e) {
	            return "";
	        }
	}


	public static String getShortUuid() {
		String[] chars = new String[] {
				"a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z",
				"0", "1", "2", "3", "4", "5", "6", "7", "8", "9",
				"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V","W", "X", "Y", "Z"
		};
		StringBuffer stringBuffer = new StringBuffer();
		String uuid = UUID.randomUUID().toString().replace("-", "");
		for (int i = 0; i < 8; i++) {
			String str = uuid.substring(i * 4, i * 4 + 4);
			int strInteger = Integer.parseInt(str, 16);
			stringBuffer.append(chars[strInteger % 0x3E]);
		}
		return stringBuffer.toString();
	}


	public static String getUUID() {
		UUID uuid = UUID.randomUUID();
		String str = uuid.toString();
		String temp = str.substring(0, 8) + str.substring(9, 13) +
				str.substring(14, 18) + str.substring(19, 23) + str.substring(24);
		return temp;
	}
}
