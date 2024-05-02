package com.dongkuksystems.dbox.models.dto.type.folder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import com.documentum.fc.client.IDfPersistentObject;
import com.dongkuksystems.dbox.models.common.UserSession;
import com.dongkuksystems.dbox.models.dto.etc.SimpleUserDto;
import com.dongkuksystems.dbox.models.type.auth.AuthBase;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder 
public class FolderLockDto {
  @ApiModelProperty(value = "문서함 id", required = true)
  private String rObjectId;
	@ApiModelProperty(value = "폴더 이름")
	private String objectName;
  @ApiModelProperty(value = "문서함 코드", required = true)
  private String uCabinetCode;
  @ApiModelProperty(value = "폴더구분", required = true)
  private String uFolType;
  @ApiModelProperty(value = "폴더명")
  private String uFolName;
  @ApiModelProperty(value = "상위폴더 ID")
  private String uUpFolId;
  @ApiModelProperty(value = "보안등급")
  private String uSecLevel;
  @ApiModelProperty(value = "보안등급 이름")
  private String secLevelName;
  @ApiModelProperty(value = "폴더상태")
  private String uFolStatus;
	@ApiModelProperty(value = "폴더상태 이름")
	private String folStatusName;
  @ApiModelProperty(value = "태그")
  private String uFolTag;
  @ApiModelProperty(value = "분류")
  private String uFolClass;
  @ApiModelProperty(value = "수정가능여부")
  private String uEditableFlag;
  @ApiModelProperty(value = "삭제가능여부")
  private String uDeleteStatus;
  @ApiModelProperty(value = "생성자")
  private String uCreateUser;
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  @JsonSerialize(using = LocalDateTimeSerializer.class)
  @JsonDeserialize(using = LocalDateTimeDeserializer.class)
  @ApiModelProperty(value = "생성 일시")
  private LocalDateTime uCreateDate;
  @ApiModelProperty(value = "하위포함 용량")
  private int folSize;
  
  @ApiModelProperty(value = "등록자 상세")
  private SimpleUserDto createUserDetail;

  @ApiModelProperty(value = "Live 권한")
  private List<AuthBase> liveAuthBases;
  @ApiModelProperty(value = "Closed 권한")
  private List<AuthBase> closedAuthBases;

  

  

}
