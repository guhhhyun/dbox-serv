package com.dongkuksystems.dbox.models.dto.type.manager.disposalrequest;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DisposalRequestDto {
  private String uReqStatus;
  private String orgId;
  private String displayName;
  private String uDocName;
  private String overStartDate;
  private String overEndDate;
  private String comOrgId;
  private String uReqType;
  private List<String> deptCodeList;
}
