package com.dongkuksystems.dbox.models.common;

import com.dongkuksystems.dbox.models.table.etc.VUser;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserSession {
  private Long id;
  private String docbase;
  private String dUserId;
  private String dPw;
  private String dctmSessionId;	
  private String socialPerId;
  private String token;
  private VUser user;
//  private String deptCabinetCode;
}
