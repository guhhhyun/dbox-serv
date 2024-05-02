package com.dongkuksystems.dbox.models.dto.type.auth;

import com.dongkuksystems.dbox.constants.HamType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class HamInfoResult {
  private String hamType;
  private String comOrgId;
  private String uCabinetCode;
  private String myCode;
  private String cabinetOrgId;

  public String getHamCodeForAuth() {
    switch (HamType.findByValue(this.hamType)) {
    case DEPT:
    case COMPANY:
    case COMPANY_M:
      return this.cabinetOrgId.toUpperCase();
//    case PROJECT:
//    case RESEARCH:
//      return this.myCode.toUpperCase();
//    }
    default:
      return this.myCode.toUpperCase();
    }
  }
}
