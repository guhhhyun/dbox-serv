package com.dongkuksystems.dbox.models.dto.type.recycle;

import java.time.LocalDateTime;
import java.util.List;

import com.dongkuksystems.dbox.models.dto.type.folder.FolderDescendantDto;
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
public class FolRecycleDto {
  	private String rObjectId;
  	@ApiModelProperty(value = "문서함유형 D:부서, P:프로젝트, R:연구과제")
	  private String uCabinetType;
	  @ApiModelProperty(value = "문서함코드")
	  private String uCabinetCode;
	  @ApiModelProperty(value = "자료유형 D:문서, F:폴더")
	  private String uObjType;
	  @ApiModelProperty(value = "자료ID")
	  private String uObjId;
	  @ApiModelProperty(value = "삭제자")
	  private String uDeleteUser;
	  @ApiModelProperty(value = "삭제자 이름")
	  private String deleteUserName;
	  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd kk:mm:ss")
	  @JsonSerialize(using = LocalDateTimeSerializer.class)
	  @JsonDeserialize(using = LocalDateTimeDeserializer.class)
	  @ApiModelProperty(value = "삭제일")
	  private LocalDateTime uDeleteDate;
	  @ApiModelProperty(value = "문서이름")
	  private String docName;
	  @ApiModelProperty(value = "작성자")
	  private String writeUser;
	  @ApiModelProperty(value = "작성자 이름")
	  private String writeUserName;
	  private String writeUserJobTitleName;
	  private String writeUserDeptName;
	  @ApiModelProperty(value = "폴더경로")
	  private String folderPath;
	  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd kk:mm:ss")
	  @JsonSerialize(using = LocalDateTimeSerializer.class)
	  @JsonDeserialize(using = LocalDateTimeDeserializer.class)
	  @ApiModelProperty(value = "수정일")
	  private LocalDateTime updateDate;
	  @ApiModelProperty(value = "버전")
	  private String version;
	  @ApiModelProperty(value = "문서크기")
	  private long size;
	  @ApiModelProperty(value = "제한등급")
	  private String secLevel;
	  @ApiModelProperty(value = "제한등급이름")
	  private String secLevelName;
	  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd kk:mm:ss")
	  @JsonSerialize(using = LocalDateTimeSerializer.class)
	  @JsonDeserialize(using = LocalDateTimeDeserializer.class)
	  @ApiModelProperty(value = "등록일")
	  private LocalDateTime uploadDate;
	  @ApiModelProperty(value = "태그")
	  private String tag;
	  @ApiModelProperty(value = "확장자")
	  private String fileExt;
	  @ApiModelProperty(value = "분류")
	  private String boonryu;
	  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd kk:mm:ss")
	  @JsonSerialize(using = LocalDateTimeSerializer.class)
	  @JsonDeserialize(using = LocalDateTimeDeserializer.class)
	  @ApiModelProperty(value = "폐기예상일")
	  private LocalDateTime expectedDeleteDate;
	  private List<FolderDescendantDto> folderDescendantDtoList;
	 
}
