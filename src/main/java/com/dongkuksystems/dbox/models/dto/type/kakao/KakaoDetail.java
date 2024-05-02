package com.dongkuksystems.dbox.models.dto.type.kakao;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder 
public class KakaoDetail {
  private int seqno;
  private String usercode;
  private String intime;
  private String reqname;
  private String deptcode;
  private String biztype;
  private String yellowidKey;
  private String reqphone;
  private String callname;
  private String callphone;
  private String msg;
  private String result;
  private String errcode;
  private String kind;
  private int batchflag;
  private int retry;
  private String resend;
  private String templatecode;
  private String reqtime;
  
}
