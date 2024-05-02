package com.dongkuksystems.dbox.constants;

public enum SysObjectType {
	FOLDER("edms_folder"),
	DOC("edms_doc"),
	EDMS_NOTI("edms_noti"),
	AUTH_SHARE("edms_auth_share"),
	AUTH_BASE("edms_auth_base"),
	PROJECT("edms_project"),
	RESEARCH("edms_research"),
	DOC_IMP("edms_doc_imp"); // add softm : 2021-09-25
	private String value;

	private SysObjectType(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}
}
