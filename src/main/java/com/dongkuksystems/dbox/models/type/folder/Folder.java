package com.dongkuksystems.dbox.models.type.folder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.dongkuksystems.dbox.constants.FolderStatus;
import com.dongkuksystems.dbox.models.table.etc.VDept;
import com.dongkuksystems.dbox.models.table.etc.VUser;
import com.dongkuksystems.dbox.models.type.auth.AuthBase;
import com.dongkuksystems.dbox.models.type.auth.AuthShare;
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
public class Folder {
  @ApiModelProperty(value = "문서함 id", required = true)
  private String rObjectId;
  @ApiModelProperty(value = "프로젝트/연구과제 코드", required = true)
  private String uPrCode;
  @ApiModelProperty(value = "프로젝트/연구과제 타입", required = true)
  private String uPrType;
  @ApiModelProperty(value = "소속부서코드(테이블 컬럼X)", hidden = true)
  private String uDeptCode;
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
  @ApiModelProperty(value = "폴더상태")
  private String uFolStatus;
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
  @ApiModelProperty(value = "수정자")
  private String uUpdateUser;
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  @JsonSerialize(using = LocalDateTimeSerializer.class)
  @JsonDeserialize(using = LocalDateTimeDeserializer.class)
  @ApiModelProperty(value = "수정 일시")
  private LocalDateTime uUpdateDate;
  @ApiModelProperty(value = "하위포함 용량")
  private long folSize;
  @ApiModelProperty(value = "보유 최대 권한")
  private String maxPermitType;
  @ApiModelProperty(value = "하위 폴더 존재 여부")
  private Boolean hasFolderChildren;
  @ApiModelProperty(value = "하위 문서 존재 여부")
  private Boolean hasDocChildren;
  @ApiModelProperty(value = "하위 프로젝트/투자 존재 여부")
  private Boolean hasProjectChildren;
  @ApiModelProperty(value = "하위 연구과제 존재 여부")
  private Boolean hasResearchChildren;

  @ApiModelProperty(value = "생성자 상세")
  private VUser createUserDetail;
  @ApiModelProperty(value = "소유부서 상세")
  private VDept ownDeptDetail;

  @ApiModelProperty(value = "권한")
  private List<AuthBase> authBases;

  @ApiModelProperty(value = "공유/협업")
  private List<AuthShare> authShares;


  @ApiModelProperty(value = "공유 소속부서 여부")
  private String ownShareYn;


  public static Map<String, String> toMap(Folder folder) {
    Map<String, String> map = new HashMap<>();
    map.put("r_object_id", folder.getRObjectId());
    return map;
  }

  public static List<Map<String, String>> toMapList(List<Folder> folders) {
    List<Map<String, String>> maps = new ArrayList<>();
    for (Folder folder : folders) {
      Map<String, String> map = new HashMap<>();
      map.put("r_object_id", folder.getRObjectId());
      maps.add(map);
    }
    return maps;
  }

  public boolean isLockFolder() {
    if (FolderStatus.LOCK.getValue().equals(this.uFolStatus)) {
      return true;
    }
    return false;
  }
}
