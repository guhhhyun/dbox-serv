package com.dongkuksystems.dbox.models.dto.type.manager.deletemanage;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DeleteManageDto {
  private String orgId;
  private String uDeleteUser;
  private String objectName;
  private String overStartDate;
  private String overEndDate;
  private String comOrgId;
  private List<String> deptCodeList;
  private String userType;
  private String targetId;
}
