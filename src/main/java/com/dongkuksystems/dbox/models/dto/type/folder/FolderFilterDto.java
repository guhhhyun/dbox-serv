package com.dongkuksystems.dbox.models.dto.type.folder;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder 
public class FolderFilterDto {
  private String rObjectId;
  private String uCabinetCode;
  private String uFolType;
  private String uFolName;
  private String uUpFolId;
  private String uSecLevel;
  private String uFolStatus;
  private String uFolClass;
  private String uPrCode;
  private String uPrType;
  private String uFolTag;
  private String uDeleteStatus;
}
