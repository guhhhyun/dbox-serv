package com.dongkuksystems.dbox.models.dto.type.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FolderAuthResult {
  private String uAuthorId;
  private String uPermitType;
}
