package com.dongkuksystems.dbox.models.dto.etc;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder 
public class DrmUserDto {
  private String userId;
  private String displayName;
  private String empType;
}
