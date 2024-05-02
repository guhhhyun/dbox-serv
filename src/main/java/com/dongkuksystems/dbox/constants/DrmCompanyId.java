package com.dongkuksystems.dbox.constants;

public enum DrmCompanyId {
	DONGKUK("DKS", "DONGKUK-E486-7361-4154", "동국제강"),
	INTERGIS("ITG", "INTERGIS-BE94-8DEA-4EC2", "인터지스"),
	DKSYSTEMS("UNC","DKSYSTEMS-7550-3A9B-461D", "동국시스템즈"),
	FERRUMINF("FEI","FERRUMINF-F330-7398-4AF9", "페럼인프라");

  private String orgId;
  private String value;
  private String comName;

	private DrmCompanyId(String orgId, String value, String comName) {
	  this.orgId = orgId;
		this.value = value;
		this.comName = comName;
	}

  public String getOrgId() {
    return orgId;
  }
  public String getValue() {
    return value;
  }
  public String getComName() {
    return comName;
  }
  
  public static DrmCompanyId findByOrgId(String orgId) {
    for (DrmCompanyId type : DrmCompanyId.values()) {
      if (type.orgId.equals(orgId)) {
        return type;
      }
    }
    throw new RuntimeException();
  }
}
