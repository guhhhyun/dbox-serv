package com.dongkuksystems.dbox.models.dto.type.folder;

import com.dongkuksystems.dbox.constants.Commons;
import com.dongkuksystems.dbox.models.dto.etc.SimpleDeptDto;
import com.dongkuksystems.dbox.models.dto.etc.SimpleUserDto;
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

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FolderDetailDto {
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
    @ApiModelProperty(value = "수정자")
    private String uUpdateUser;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @ApiModelProperty(value = "수정 일시")
    private LocalDateTime uUpdateDate;
    @ApiModelProperty(value = "프로젝트/연구과제 코드")
    private String uPrCode;
    @ApiModelProperty(value = "프로젝트/연구과제 타입")
    private String uPrType;
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

    @ApiModelProperty(value = "등록자 상세")
    private SimpleUserDto createUserDetail;
    @ApiModelProperty(value = "소유부서 상세")
    private SimpleDeptDto ownDeptDetail;

    @ApiModelProperty(value = "Live 권한")
    private List<AuthBase> liveAuthBases;
    @ApiModelProperty(value = "Closed 권한")
    private List<AuthBase> closedAuthBases;
    @ApiModelProperty(value = "Live 공유/협업")
    private List<AuthShare> liveAuthShares;

    @ApiModelProperty(value = "보존년한")
    private Integer uPreserverFlag;

    public void setUCreateDate(LocalDateTime uCreateDate) {
        if (uCreateDate == null) this.uCreateDate = null;
        else {
            ZonedDateTime zdt = uCreateDate.atZone(ZoneId.of("Asia/Seoul"));
            long milli = zdt.toInstant().toEpochMilli();
            this.uCreateDate = Commons.NULL_DATE == milli ? null : uCreateDate;
        }
    }

}
