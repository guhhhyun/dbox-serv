package com.dongkuksystems.dbox.constants;

/**
 * 첨부창 보안등급 제한
 *  그룹사내가 가장 높고,  -> 사내 -> 팀내 -> 제한 
 * @author admin
 *
 */
public enum LimitSecLevelCode {
  
	S(4, "제한"),
	T(3, "팀내"), 
	C(2, "사내"), 
	G(1, "그룹사내");
	
	private int value;
	private String desc;

	private LimitSecLevelCode(int value, String desc) {
		this.value = value;
		this.desc = desc;
	}

	public int getValue() {
		return value;
	}

	public String getDesc() {
		return desc;
	}
  
	public static LimitSecLevelCode getCodeByName(String name) {

		try {
			if (name == null || name.equals("")) {
				return LimitSecLevelCode.G;
			}
			return LimitSecLevelCode.valueOf(name);
		} catch (Exception e) {
			return null;
		}
	}
	
	
}
