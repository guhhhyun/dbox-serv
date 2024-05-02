package com.dongkuksystems.dbox.utils.certifications;

import java.security.MessageDigest;
import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.Base64.Encoder;

import org.springframework.stereotype.Component;

@Component
public class CertifiUtils {
	/**
	 * BASE64 인코딩
	 */
	public String encodeBase64(String ticket) {
		String encodedString = "";
		try {
			byte[] targetBytes = ticket.getBytes("UTF-8"); 
      Encoder encoder = Base64.getEncoder();
      encodedString = encoder.encodeToString(targetBytes); 
		} catch (Exception e) {
			e.printStackTrace(); 
		} 
		
		return encodedString;
	}

	/**
	 * BASE64 디코딩
	 */
	public String decodeBase64(String ticket) {
		String decodedString = "";
		try {
			byte[] targetBytes = ticket.getBytes("UTF-8"); 
      Decoder decoder = Base64.getDecoder(); 
      byte[] decodedBytes = decoder.decode(targetBytes);
      decodedString = new String(decodedBytes, "UTF-8");
		} catch (Exception e) {
			e.printStackTrace(); 
		} 
		
		return decodedString;
	}

	/**
	 * SHA256으로 변환
	 */
  public String encryptSHA256(String value) throws Exception {
      MessageDigest md = MessageDigest.getInstance("SHA-256");
      byte[] hashedValue = value.getBytes();
      md.update(hashedValue);
      return bytesToHex(md.digest());
  }
  
  /**
   * 바이트어레이를 HEX값으로 변환
   */
  private static String bytesToHex(byte[] b) {
    char hexDigit[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };
    StringBuffer buf = new StringBuffer();
    for (int j = 0; j < b.length; j++) {
        buf.append(hexDigit[(b[j] >> 4) & 0x0f]);
        buf.append(hexDigit[b[j] & 0x0f]);
    }
    
    return buf.toString();
  }
  
  /**
   * 3DES 암호화
   */
  public String get3DESEncrypt(String key, String value, String charSet) throws Exception {
      TripleDESCrypt tdc = new TripleDESCrypt(key, charSet);
      return tdc.hexEncrypt(value, charSet);
  }
  
  /**
   * 3DES 복호화
   */
  public String get3DESDecrypt(String key, String value, String charSet) throws Exception {
      TripleDESCrypt tdc = new TripleDESCrypt(key, charSet);
      return tdc.hexDecrypt(value, charSet);
  }
}
