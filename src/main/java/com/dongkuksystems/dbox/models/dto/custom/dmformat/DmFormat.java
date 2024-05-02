package com.dongkuksystems.dbox.models.dto.custom.dmformat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder 
public class DmFormat {
  private String objectId;
  private String name;
  private String description;
  private String macCreator;
  private String macType;
  private String dosExtension;
  private String assetClass;
  private String mimeType;
}

