package com.dongkuksystems.dbox.models.type.agent;

import java.util.HashMap;
import java.util.Map;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AgentDoc {

	@ApiModelProperty(value = "Key")
	@Builder.Default
	private String rid = "";
	@ApiModelProperty(value = "문서 확장자")
	@Builder.Default
	private String dos_ext = "";
	@ApiModelProperty(value = "문서 버전")
	@Builder.Default
	private String r_version_label = "";
	@ApiModelProperty(value = "문서 상태 CR : 잠금 아닐 시, CL : 문서 잠금 시")
	@Builder.Default
	private String r_state = "";
	@ApiModelProperty(value = "보안등급 코드")
	@Builder.Default
	private String r_security_level = "";
	@ApiModelProperty(value = "문서 보안 등급")
	@Builder.Default
	private String r_secu_level = "";
	@ApiModelProperty(value = "문서 ID")
	@Builder.Default
	private String r_object_id = "";
	@ApiModelProperty(value = "최초 등록된 문서 ID")
	@Builder.Default
	private String i_chronicle_id = "";
	@ApiModelProperty(value = "문서명")
	@Builder.Default
	private String object_name = "";
	@ApiModelProperty(value = "문서 소유자 명")
	@Builder.Default
	private String owner_name = "";
	@ApiModelProperty(value = "문서 소유자 ID")
	@Builder.Default
	private String own_userid = "";
	@ApiModelProperty(value = "문서 크기")
	@Builder.Default
	private String r_content_size = "";
	@ApiModelProperty(value = "문서 생성일 : 2021-08-04 11:10:32")
	@Builder.Default
	private String r_creation_date = "";
	@ApiModelProperty(value = "문서 수정일")
	@Builder.Default
	private String r_modify_date = "";
	@ApiModelProperty(value = "문서 수정자 명")
	@Builder.Default
	private String r_modifier = "";
	@ApiModelProperty(value = "문서 타입 : text/plain")
	@Builder.Default
	private String a_content_type = "";
	@ApiModelProperty(value = "문서 번호")
	@Builder.Default
	private String dm_rnum = "";
	@ApiModelProperty(value = "문서 생성자 명")
	@Builder.Default
	private String creator_emp_nm = "";
	@ApiModelProperty(value = "문서 잠금 여부 0 : 잠금 아닐 시, 1 : 잠금")
	@Builder.Default
	private String lock = "";
	@ApiModelProperty(value = "0 : 잠금 아닐 시 1 : 잠금")
	@Builder.Default
	private String r_lock_type = "";
	@ApiModelProperty(value = "잠금자 ID")
	@Builder.Default
	private String r_lock_owner = "";
	@ApiModelProperty(value = "잠금자 명")
	@Builder.Default
	private String r_lock_owner_name = "";
	@ApiModelProperty(value = "문서 위치")
	@Builder.Default
	private String doc_folderPath = "";
	@ApiModelProperty(value = "권한명")
	@Builder.Default
	private String permit_name = "";
	@ApiModelProperty(value = "권한명")
	@Builder.Default
	private String r_category = "";
	@ApiModelProperty(value = "문서 카테고리 명")
	@Builder.Default
	private String r_category_nm = "";
	@ApiModelProperty(value = "문서 삭제 날짜 (휴지통)")
	@Builder.Default
	private String delete_date = "";
	@ApiModelProperty(value = "부서명")
	@Builder.Default
	private String dept_name = "";
	@ApiModelProperty(value = "제한문서 첨부 같이 첨부 할수 없는 ( 개인정보 : 5, 첨부제한 : 9, 개인정보 + 첨부제한 : 13 ) ")
	@Builder.Default
	private String online_status = "";
	
	/**
	 * Default 문서 생성
	 * @param doc
	 * @return
	 */
	public static Map<String, Object> toMap(AgentDoc doc) {
		
		Map<String, Object> map = new HashMap<>();
		
		map.put("dos_ext"			, doc.getDos_ext()); 			//	문서 확장자
		map.put("r_version_label"	, doc.getR_version_label());	//	문서 버전
		map.put("r_state"			, doc.getR_state()); 			//	문서 상태
		map.put("r_security_level"	, doc.getR_security_level());	//	보안등급 코드
		map.put("r_secu_level"		, doc.getR_secu_level()); 		//	문서 보안 등급	"확인필요......"
		map.put("r_object_id"		, doc.getR_object_id()); 		//	문서 ID
		map.put("i_chronicle_id"	, doc.getI_chronicle_id()); 	//	최초 등록된 문서 ID
		map.put("object_name"		, doc.getObject_name()); 		//	문서명
		map.put("owner_name"		, doc.getOwner_name()); 		//	문서 소유자 명
		map.put("own_userid"		, doc.getOwner_name()); 		//	문서 소유자 ID			"추가필요..."
		map.put("r_content_size"	, doc.getR_content_size()); 	//	문서 크기
		map.put("r_creation_date"	, doc.getR_creation_date());	//	문서 생성일				"추가필요..."
		map.put("r_modify_date"		, doc.getR_modify_date());		//	문서 수정일				"추가필요..."
		map.put("r_modifier"		, doc.getR_modifier());			//	문서 수정자 명			"추가필요..."
		map.put("a_content_type"	, doc.getA_content_type());		//	문서 타입				"추가필요..."
		map.put("dm_rnum"			, doc.getDm_rnum()); 			//	문서 번호				"확인필요......."
		map.put("creator_emp_nm"	, doc.getCreator_emp_nm());		//	문서 생성자 명			"추가필요..."
		map.put("lock"				, doc.getLock()); 				//	문서 잠금 여부 0 : 잠금 아닐 시, 1 : 잠금		"추가필요..."
		map.put("r_lock_type"		, doc.getR_lock_type());		//	0 : 잠금 아닐 시 1 : 잠금					"추가필요..."
		map.put("r_lock_owner"		, doc.getR_lock_owner()); 		//	잠금자 ID				"추가필요..."
		map.put("r_lock_owner_name"	, doc.getR_lock_owner_name());	//	잠금자 명				"추가필요..."
		map.put("doc_folderPath"	, doc.getDoc_folderPath()); 	//	문서 위치				"추가필요..."
		map.put("permit_name"		, doc.getPermit_name()); 		//	권한명				"확인필요......."
		map.put("r_category"		, doc.getR_category()); 		//	문서 카테고리			"확인필요......."
		map.put("r_category_nm"		, doc.getR_category_nm()); 		//	문서 카테고리 명			"확인필요......."
		map.put("delete_date"		, doc.getDelete_date()); 		//	문서 삭제 날짜 (휴지통)	"확인필요......."
		map.put("dept_name"			, doc.getDept_name());			//	
		map.put("online_status"		, doc.getOnline_status());		//  첨부불가  : 5 첨부불가   (  제한문서 및   개인정보 포함시 5 리턴함 )
		
		return map;
	}
	
	
}
