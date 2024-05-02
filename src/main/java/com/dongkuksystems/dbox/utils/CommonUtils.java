package com.dongkuksystems.dbox.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URLEncoder;

import javax.management.BadAttributeValueExpException;
import javax.servlet.http.HttpServletRequest;

import com.dongkuksystems.dbox.constants.CabinetType;
import com.dongkuksystems.dbox.constants.HamType;
import com.dongkuksystems.dbox.constants.SecLevelCode;

public class CommonUtils {

	/**
	 * @param
	 * @return
	 * @throws IOException
	 */
	public static ByteArrayOutputStream convertByteInputStreamToOut(ByteArrayInputStream fis) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] buffer = new byte[512];
		int readcount = 0;
		while ((readcount = fis.read(buffer)) != -1) {
			baos.write(buffer, 0, readcount);
		}
		return baos;
	}

	public static String getAuthScope(String cabinetType) throws BadAttributeValueExpException {
		if (HamType.DEPT.getValue().equals(cabinetType) || "F".equals(cabinetType)) {
			return HamType.DEPT.getValue();
		} else if (HamType.PROJECT.getValue().equals(cabinetType)) {
			return HamType.PROJECT.getValue();
		} else if (HamType.RESEARCH.getValue().equals(cabinetType)) {
			return HamType.RESEARCH.getValue();
		} else {
			throw new BadAttributeValueExpException(cabinetType);
		}
	}

	@SuppressWarnings("deprecation")
	public static String getFileNm(String browser, String fileNm) {
		String reFileNm = null;
		try {
			if (browser.equals("MSIE") || browser.equals("Trident") || browser.equals("Edge")) {
				reFileNm = java.net.URLEncoder.encode(fileNm, "UTF-8").replaceAll("\\+", "%20");
			} else {
				if (browser.equals("Chrome")) {
					StringBuffer sb = new StringBuffer();
					for (int i = 0; i < fileNm.length(); i++) {
						char c = fileNm.charAt(i);
						if (c > '~') {
							sb.append(java.net.URLEncoder.encode(Character.toString(c), "UTF-8"));
						} else {
							sb.append(c);
						}
					}
					reFileNm = sb.toString();
				} else {
					reFileNm = new String(fileNm.getBytes("UTF-8"), "ISO-8859-1");
				}
				if (browser.equals("Safari") || browser.equals("Firefox"))
					reFileNm = java.net.URLDecoder.decode(reFileNm);
			}
		} catch (Exception e) {
		}
		return reFileNm;
	}

	public static String getBrowser(HttpServletRequest req) {
	    String userAgent = req.getHeader("User-Agent");
	    if (userAgent.indexOf("MSIE") > -1 || userAgent.indexOf("Trident") > -1 || userAgent.indexOf("Edge") > -1) {
	        return "MSIE";
	    } else if (userAgent.indexOf("Chrome") > -1) {
	        return "Chrome";
	    } else if (userAgent.indexOf("Opera") > -1) {
	        return "Opera";
	    } else if (userAgent.indexOf("Safari") > -1) {
	        return "Safari";
	    } else if (userAgent.indexOf("Firefox") > -1) {
	        return "Firefox";
	    } else {
	        return null;
	    }
	}
}
