package com.dongkuksystems.dbox.models.dto.type.docbox.research;

import static com.google.common.base.Preconditions.checkArgument;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.documentum.fc.client.IDfPersistentObject;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.common.DfId;
import com.documentum.fc.common.DfTime;
import com.dongkuksystems.dbox.constants.DCTMConstants;
import com.dongkuksystems.dbox.constants.DboxObjectType;
import com.dongkuksystems.dbox.constants.SysObjectType;
import com.dongkuksystems.dbox.errors.upload.UploadNameLengthException;
import com.dongkuksystems.dbox.utils.DboxStringUtils;
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
public class ResearchCreateDto {

	@ApiModelProperty(value = "ObjectId")
	private String rObjectId;

	@ApiModelProperty(value = "문서함코드")
	private String uCabinetCode;
	@ApiModelProperty(value = "연구과제 코드")
	private String uRschCode;
	@ApiModelProperty(value = "연구과제 명")
	private String uRschName;
  @ApiModelProperty(value = "주관부서")
  private String uOwnDept;
	@ApiModelProperty(value = "책임자")
	private String uChiefId;
	@ApiModelProperty(value = "보안등급")
	private String uSecLevel;
	@ApiModelProperty(value = "완료여부")
	private String uFinishYn;
	@ApiModelProperty(value = "시행년도")
	private String uStartYear;
	@ApiModelProperty(value = "분류폴더 ID")
	private String uFolId;
	@ApiModelProperty(value = "목록보기 활성화 여부")
	private String uListOpenYn;
	@ApiModelProperty(value = "생성자")
	private String uCreateUser;

	@JsonSerialize(using = LocalDateTimeSerializer.class)
	@JsonDeserialize(using = LocalDateTimeDeserializer.class)
	@ApiModelProperty(value = "생성 일시")
	private LocalDateTime uCreateDate;

	@ApiModelProperty(value = "변경자")
	private String uUpdateUser;

	@JsonSerialize(using = LocalDateTimeSerializer.class)
	@JsonDeserialize(using = LocalDateTimeDeserializer.class)
	@ApiModelProperty(value = "변경 일시")
	private LocalDateTime uUpdateDate;

  @ApiModelProperty(value = "참여부서(조회/다운로드) 리스트")
  private List<String> uJoinDeptReads;	
  @ApiModelProperty(value = "참여부서(편집/삭제) 리스트")
  private List<String> uJoinDeptDels;	

	public static IDfPersistentObject createResearch(IDfSession idfSession, ResearchCreateDto dto) throws Exception {

    if (dto.getURschName().getBytes().length >= 240) {
      throw new UploadNameLengthException("연구과제 명은 영문 240, 한글 80자 이내여야 합니다.");
    }
    
		IDfPersistentObject idf_PObj = null;
		String s_ObjId = dto.getRObjectId();
		boolean isNew = (DCTMConstants.DCTM_BLANK.equals(s_ObjId) || null == s_ObjId);
		if(!isNew) 
		    idf_PObj = idfSession.getObject(new DfId(s_ObjId));
		else {
		  idf_PObj = (IDfPersistentObject) idfSession.newObject(SysObjectType.RESEARCH.getValue());
//		  idf_PObj.setString("u_rsch_code", dto.getURschCode());  // 서비스에서 신규 채번해서 넘겨줌 -> objectId 뒤 5글자 추출해서 입력 2022-01-25수정
      idf_PObj.setString("u_rsch_code", DboxStringUtils.getPrCodeFromObjId(DboxObjectType.RESEARCH.getValue(), idf_PObj.getObjectId().getId())); 
		}
		if (dto.getUCabinetCode() != null) idf_PObj.setString("u_cabinet_code", dto.getUCabinetCode());
		if (dto.getURschName() != null) idf_PObj.setString("u_rsch_name", dto.getURschName());
		if (dto.getUOwnDept() != null) idf_PObj.setString("u_own_dept",     dto.getUOwnDept() );

		idf_PObj.getAllRepeatingStrings("", ",");
		if (!isNew) {
		  idf_PObj.truncate("u_join_dept_read", 0);
		  idf_PObj.truncate("u_join_dept_del", 0);
		}
		List<String> joinDeptReadList = Optional.ofNullable(dto.getUJoinDeptReads()).orElse(new ArrayList<String>());
		for(int i=0; i < joinDeptReadList.size(); i++) {
			idf_PObj.appendString("u_join_dept_read", joinDeptReadList.get(i));
		}
		List<String> joinDeptDelList = Optional.ofNullable(dto.getUJoinDeptDels()).orElse(new ArrayList<String>());
		for(int i=0; i < joinDeptDelList.size(); i++) {
			idf_PObj.appendString("u_join_dept_del", joinDeptDelList.get(i));
		}
		
		if (dto.getUChiefId() != null) idf_PObj.setString("u_chief_id", dto.getUChiefId());
		if (dto.getUSecLevel() != null) idf_PObj.setString("u_sec_level", dto.getUSecLevel());
		if (dto.getUFinishYn() != null) idf_PObj.setString("u_finish_yn", dto.getUFinishYn());
		if (dto.getUStartYear() != null) idf_PObj.setString("u_start_year", dto.getUStartYear());
		if (dto.getUFolId() != null) idf_PObj.setString("u_fol_id", dto.getUFolId());
		if (dto.getUListOpenYn() != null) idf_PObj.setString("u_list_open_yn", dto.getUListOpenYn());
		
    if(isNew )  { //신규생성일 때
  	    idf_PObj.setString("u_create_user", idfSession.getLoginUserName()  );
  	    idf_PObj.setString("u_create_date", (new DfTime()).toString());
  	    idf_PObj.setString("u_update_user", idfSession.getLoginUserName()  );
    } else {
      idf_PObj.setString("u_update_user", dto.getUUpdateUser() );
    }
		idf_PObj.setString("u_update_date", (new DfTime()).toString());

		return idf_PObj;

	}

}
