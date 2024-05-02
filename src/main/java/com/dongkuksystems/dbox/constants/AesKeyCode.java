package com.dongkuksystems.dbox.constants;

/**
 * AES256 암복호화키 ( 첨부 sysid 암호화에 사용 : 32 자리 )
 * @author admin
 *
 */
public enum AesKeyCode {
  
	// 사용안함............................
	
	KEY("dbox5678901234567890123456789012", "KeyValue"); 

	private String value;
	private String desc;

	private AesKeyCode(String value, String desc) {
		this.value 	= value;
		this.desc 	= desc;
	}

	public String getValue() {
		return value;
	}

	public String getDesc() {
		return desc;
	}
}
