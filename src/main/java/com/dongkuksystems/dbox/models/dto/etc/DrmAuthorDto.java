package com.dongkuksystems.dbox.models.dto.etc;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder 
public class DrmAuthorDto {
  private String authorType;
  private String id;
  private String name;
}
