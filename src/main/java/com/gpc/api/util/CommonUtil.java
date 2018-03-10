package com.gpc.api.util;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.google.common.base.Splitter;


/**
 * 
 * @author Arunachalam Govindasamy
 *
 */
public class CommonUtil {
	private static Logger log = Logger.getLogger(CommonUtil.class.getName());
	
	public static Map<String,String> getStringToMap(String str,String keySeparator, String keyValSeparator) {
		Map<String,String> map = null;
		String strArr[] = null;
		String[] strArrValue = null; 
		try {
			strArr = str.split(keySeparator);
			if(null!=strArr && strArr.length>0) {
				map = new HashMap<String,String>();
				for(String s : strArr) {
					if(StringUtils.isNotBlank(s)) {
						strArrValue = s.split(keyValSeparator);
						if(null!=strArrValue && strArrValue.length>1) {
							map.put(strArrValue[0].trim(), strArrValue[1].trim());
						}
					}
				}
			}
			return map;
				//return Splitter.on(delimiter).withKeyValueSeparator(keyDelimiter).split(str);
			
		}catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static void main(String as[]) {
		System.out.println(getStringToMap("produc-product_search","\\|",":"));
	}
	
	

}
