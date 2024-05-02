package com.dongkuksystems.dbox.constants;

public class Commons {
  public static final String LOGOUT_PREFIX = "__LOGOUT__";
  public static final String LOCK_PREFIX = "LOCK_";
  public static final String SESSION_PREFIX = "SESSION_";
  //임시파일 생성 경로
  public static final String TMP_STORAGE_PATH = "C:\\dboxTmp/{0}/";
  public static final String DEFAULT_EXTENSION = "tmp";

  public static final long NULL_DATE = -6847835272000L;
  
  //redis 저장 시간
  public static final Long REDIS_TIMEOUT_SEC = (long) (60 * 60 * 24);
  //documentum session mng 저장 개수
  public static final Long DCTM_SESSION_MAXIMUM = (long) 5000;
  //documentum session mng 저장 시간
  public static final Long DCTM_SESSION_DURATION_HOUR = (long) (24);
  public static final Long DCTM_SESSION_DURATION_MIN = (long) (60*DCTM_SESSION_DURATION_HOUR);
}
