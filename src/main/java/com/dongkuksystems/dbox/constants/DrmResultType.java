package com.dongkuksystems.dbox.constants;

public enum DrmResultType {
	SUCCESS("00000"), 
	PLAIN_FILE("90005");
	
  private String value;

  private DrmResultType(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }
}
