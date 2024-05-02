package com.dongkuksystems.dbox.models.dto.type.dept;

import java.time.LocalDateTime;

import com.documentum.fc.client.IDfPersistentObject;
import com.dongkuksystems.dbox.constants.DCTMConstants;
import com.dongkuksystems.dbox.utils.DateTimeUtils;
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
public class DeptDto {
  @ApiModelProperty(value = "회사코드", required = true, example="UNC")
  private String uComCode;
  @ApiModelProperty(value = "부서코드", required = true, example="TEST_DEPT_CODE_1")
  private String uDeptCode;
  @ApiModelProperty(value = "상위부서코드", example="")
  private String uUpDeptCode;
  @ApiModelProperty(value = "문서함코드", example="")
  private String uCabinetCode;
  @ApiModelProperty(value = "부서명", example="TEST_DEPT_NAME_1")
  private String uDeptName;
  @ApiModelProperty(value = "부서장 ID", example="")
  private String uDeptChief;
  @ApiModelProperty(value = "정렬키", example="0001")
  private String uSortOrder;
  @ApiModelProperty(value = "사용여부")
  private String uUseYn;
  @JsonSerialize(using = LocalDateTimeSerializer.class)
  @JsonDeserialize(using = LocalDateTimeDeserializer.class)
  @ApiModelProperty(value = "이관시작일")
  private LocalDateTime uTransStartDate;
  @ApiModelProperty(value = "생성자")
  private String uCreateUser;
  @JsonSerialize(using = LocalDateTimeSerializer.class)
  @JsonDeserialize(using = LocalDateTimeDeserializer.class)
  @ApiModelProperty(value = "생성 일시")
  private LocalDateTime uCreateDate;
  @ApiModelProperty(value = "수정자")
  private String uModifyUser;
  @JsonSerialize(using = LocalDateTimeSerializer.class)
  @JsonDeserialize(using = LocalDateTimeDeserializer.class)
  @ApiModelProperty(value = "수정 일시")
  private LocalDateTime uModifyDate;
  
  public static IDfPersistentObject toDcmtObject(IDfPersistentObject obj, DeptDto dto) throws Exception {

    obj.setString("u_com_code"     , dto.getUComCode());
    obj.setString("u_dept_code"      , dto.getUDeptCode());
    obj.setString("u_up_dept_code"  , dto.getUUpDeptCode());
    obj.setString("u_cabinet_code"    , dto.getUCabinetCode());
    obj.setString("u_dept_name"  , dto.getUDeptName());
    obj.setString("u_dept_chief"    , dto.getUDeptChief());
    obj.setString("u_sort_order"      , "0001");
    obj.setString("u_use_yn"    , "Y");
    obj.setString("u_trans_start_date"     , "");
    obj.setString("u_create_user"     , "TEST_DEPT_CODE_1");
    obj.setString("u_create_date"     , DateTimeUtils.now(DCTMConstants.DATE_FORMAT));
    obj.setString("u_modify_user"     , "TEST_DEPT_CODE_1");
    obj.setString("u_modify_date"    , DateTimeUtils.now(DCTMConstants.DATE_FORMAT));
//    obj.setString("u_com_code"     , "UNC");
//    obj.setString("u_dept_code"      , "TEST_DEPT_CODE_1");
//    obj.setString("u_up_dept_code"  , "");
//    obj.setString("u_cabinet_code"    , "");
//    obj.setString("u_dept_name"  , "TEST_DEPT_NAME_1");
//    obj.setString("u_dept_chief"    , "");
//    obj.setString("u_sort_order"      , "0001");
//    obj.setString("u_use_yn"    , "Y");
//    obj.setString("u_trans_start_date"     , "");
//    obj.setString("u_create_user"     , "TEST_DEPT_CODE_1");
//    obj.setString("u_create_date"     , DateTimeUtils.now(DCTMConstants.DATE_FORMAT));
//    obj.setString("u_modify_user"     , "TEST_DEPT_CODE_1");
//    obj.setString("u_modify_date"    , DateTimeUtils.now(DCTMConstants.DATE_FORMAT));
    return obj;
  }
}
