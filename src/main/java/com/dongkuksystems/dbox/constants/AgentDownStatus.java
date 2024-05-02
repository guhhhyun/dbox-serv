package com.dongkuksystems.dbox.constants;

/**
 * 첨부정책 : 
 * @author admin
 *
 */
public enum AgentDownStatus {
  

	ATTACH_HTML("ATTACH_HTML"				, "HTML 첨부"), 
	ATTACH_ORIGINAL("ATTACH_ORIGINAL"		, "원문첨부 반출함에서만 사용"),
	ATTACH_DRM("ATTACH_DRM"					, "DRM 첨부"),
	DOWNLOAD_ORIGINAL("DOWNLOAD_ORIGINAL"	, "복호화 다운로드 정책 반출함 HTML문서"),
	DOWNLOAD_DRM("DOWNLOAD_DRM"				, "DRM  다운로드 정책");
  
	private String value;
	private String desc;

	private AgentDownStatus(String value, String desc) {
		this.value = value;
		this.desc = desc;
	}

	public String getValue() {
		return value;
	}

	public String getDesc() {
		return desc;
	}
}
