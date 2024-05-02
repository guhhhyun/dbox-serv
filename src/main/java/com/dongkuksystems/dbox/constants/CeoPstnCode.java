package com.dongkuksystems.dbox.constants;

public enum CeoPstnCode {
  CHAIRMAN("DKS1110", "회장"), 
  VICE_CHAIRMAN("DKS1120", "부회장"), 
  CEO("DKS1191", "대표이사")
  ;
	
  private String value;
  private String desc;

  private CeoPstnCode(String value, String desc) {
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
