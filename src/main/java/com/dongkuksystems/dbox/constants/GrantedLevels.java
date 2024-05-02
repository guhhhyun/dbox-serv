package com.dongkuksystems.dbox.constants;

public enum GrantedLevels {
  NONE(1, "N"), BROWSE(2, "B"), READ(3, "R"), RELATE(4, "RELATE"), VERSION(5, "V"), WRITE(6, "W"), DELETE(7, "D");

  private int level;
  private String label;

  GrantedLevels(int level, String label) {
    this.level = level;
    this.label = label;
  }

  public int getLevel() {
    return this.level;
  }
  public String getLabel() {
    return this.label;
  }
  
  public String getLevelCode() {
    return DELETE.level == this.level ? "D" : READ.level == this.level ? "R" : "B";
  }
  
  public boolean checkAuthentication(int target) {
    return this.level<=target;
  }

  public static boolean checkLockAuth(int target) {
    return READ.level >= target;
  }

  public static boolean isFirstBiggerOrSame(String a, String b) {
    int from = GrantedLevels.findByLabel(a);
    int to   = GrantedLevels.findByLabel(b);
    
    if (from >= to) return true;
    else return false;
  }
  public static boolean isFirstBigger(String a, String b) {
    int from = GrantedLevels.findByLabel(a);
    int to   = GrantedLevels.findByLabel(b);
    
    if (from > to) return true;
    else return false;
  }
  
  public static String findLabelByLevel(int level) {
	  for (GrantedLevels gl : GrantedLevels.values()) {
	    if (gl.getLevel() == level) {
	      return gl.label;
	    }
	  }
	  throw new RuntimeException();
  }
  
  public static int findByLabel(String target) {
    for (GrantedLevels gl : GrantedLevels.values()) {
      if (gl.getLabel().equals(target)) {
        return gl.level;
      }
    }
    throw new RuntimeException();
  }

  public static String findLabelByLevel(String target) {
    for (GrantedLevels gl : GrantedLevels.values()) {
      if (gl.name().equals(target)) {
        return gl.label;
      }
    }
    for (GrantedLevels gl : GrantedLevels.values()) {
      if (gl.label.equals(target)) {
        return gl.label;
      }
    }
    throw new RuntimeException();
  }
  
  public static int findByLevel(String target) {
    for (GrantedLevels gl : GrantedLevels.values()) {
      if (gl.name().equals(target)) {
        return gl.level;
      }
    }
    for (GrantedLevels gl : GrantedLevels.values()) {
      if (gl.label.equals(target)) {
        return gl.level;
      }
    }
    throw new RuntimeException();
  }
}
