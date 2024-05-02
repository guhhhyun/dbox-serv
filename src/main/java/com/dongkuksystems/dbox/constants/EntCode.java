package com.dongkuksystems.dbox.constants;

public enum EntCode {
	DKG("동국제강그룹", "#", "DKG"), 
	DKS("동국제강", "DKG", "DKS"), 
	UNC("동국시스템즈", "DKG", "UNC"), 
	ITG("인터지스", "DKG", "ITG"), 
	FEI("페럼인프라", "DKG", "FEI");

  private String value; 
  private String upOrgId;
  private String orgId;

  private EntCode(String value, String upOrgId, String orgId) {
    this.value = value;
    this.upOrgId = upOrgId;
    this.orgId = orgId;
  }

  public String getValue() {
    return value;
  }
  
  public String getUpOrgId() {
    return upOrgId;
  }
  
  public String getOrgId() {
    return orgId;
  }
  
  public static EntCode getEnt(String name) {
    try {
      if (name == null) {
        return EntCode.DKG;
      }
      return EntCode.valueOf(name);
    } catch (Exception e) {
      return null;
    }
  }
  
  public static EntCode findByOrgId(String orgId) {
    for (EntCode entCode : EntCode.values()) {
      if (entCode.orgId.equals(orgId)) {
        return entCode;
      }
    }
    throw new RuntimeException();
  }
  
  public static String findOrgNmByOrgId(String orgId) {
    for (EntCode entCode : EntCode.values()) {
      if (entCode.orgId.equals(orgId)) {
        return entCode.value;
      }
    }
    throw new RuntimeException();
  }
}
