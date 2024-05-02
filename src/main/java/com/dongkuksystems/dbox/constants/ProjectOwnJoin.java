package com.dongkuksystems.dbox.constants;

public enum ProjectOwnJoin {
  OWN("O"), 
  JOIN("J");
	
  private String value;

  private ProjectOwnJoin(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }
  
  public static ProjectOwnJoin findByValue(String value) {
    for (ProjectOwnJoin projectOwnJoin : ProjectOwnJoin.values()) {
      if (projectOwnJoin.value.equals(value)) {
        return projectOwnJoin;
      }
    }
    return null;
  }
}
