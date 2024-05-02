package com.dongkuksystems.dbox.constants;

public enum CabinetType {
  DEPT("D", "dept"), 
//  PROJECT("P", "project"),
//  RESEARCH("R", "연구과제"), 
  SIGNIFICANCE("S", "사별"), 
  COMPANY("C", "사별"), 
  MANAGE_DKS("M", "동국제강-관리"), 
  MANAGE_ITS("I", "인터지스-관리"); 

  private String value;
  private String desc;

  private CabinetType(String value, String desc) {
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
