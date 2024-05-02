package com.dongkuksystems.dbox.constants;

public enum ComCodeType {
  DKS("DKS", "동국제강"), 
  UNC("UNC", "동국시스템즈"),
  FEI("FEI", "페럼인프라"), 
  ITG("ITG", "인터지스");

  private String value;
  private String desc;

  private ComCodeType(String value, String desc) {
    this.value = value;
    this.desc = desc;
  }

  public String getValue() {
    return value;
  }
  
  public String getDesc() {
    return desc;
  }
  
  public static ComCodeType findByValue(String value) {
    for (ComCodeType comCodeType : ComCodeType.values()) {
      if (comCodeType.value.equals(value)) {
        return comCodeType;
      }
    }
    throw new RuntimeException();
  }
}
