package com.dongkuksystems.dbox.models.dto.etc;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder 
public class DrmCompanyDto {
  private String companyId;
  private String companyName;
}
