package com.dongkuksystems.dbox.constants;

public enum CodeType {
	SEC_LEVEL("SEC_LEVEL"),
	FOLDER_STATUS("FOLDER_STATUS"),
	DOCUMENT_STATUS("DOCUMENT_STATUS"),
	ALL_ACCESS_USER("ALL_ACCESS_USER"),
	SPECIAL_USER("SPECIAL_USER"),
	NOTI_ITEM("NOTI_ITEM"),
	AGENT_INS_ID("AGENT_INS_ID"),
	COM_CODE("COM_CODE"),
	VALIDATION_VALUE("VALIDATION_VALUE"),
	DENIED_FILE_FORMAT("DENIED_FILE_FORMAT"),
	CLOSED_FILE_FORMAT("CLOSED_FILE_FORMAT"),
	DRM_FILE_FORMAT("DRM_FILE_FORMAT"),
	DOC_HANDLE_LIST("DOC_HANDLE_LIST"),
	CONFIG_DOC_HANDLE_LIMIT("CONFIG_DOC_HANDLE_LIMIT"),
	COMMON_CABINET_DEPT("COMMON_CABINET_DEPT"),
	ATTACH_MAIL_SYSTEM("ATTACH_MAIL_SYSTEM"),
	CONFIG_MID_SAVE_DEPT("CONFIG_MID_SAVE_DEPT"),
	PRESERVE("PRESERVE_VALUE"),
	MENU_CATEGORY("MENU_CATEGORY"),
	CONFIG_TRANS_WF("CONFIG_TRANS_WF"),
	CONFIG_USB_BASE_POLICY("CONFIG_USB_BASE_POLICY"),
	CONFIG_VER_DEL_PERIOD("CONFIG_VER_DEL_PERIOD"),
	CONFIG_DELETE_PERIOD("CONFIG_DELETE_PERIOD"),
	CONFIG_LOGVIEW_AUTH("CONFIG_LOGVIEW_AUTH");
	
  private String value;

  private CodeType(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }
}
