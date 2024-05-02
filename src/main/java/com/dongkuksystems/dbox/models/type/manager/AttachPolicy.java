package com.dongkuksystems.dbox.models.type.manager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder 
public class AttachPolicy {
	
	@ApiModelProperty(value = "r_object_id")
	private String rObjectId;
	@ApiModelProperty(value = "시스템명")
	private String uSystemName;
	@ApiModelProperty(value = "시스템 구분 코드1")
	private String uSystemKey1;
	@ApiModelProperty(value = "시스템 구분 코드2")
	private String uSystemKey2;
	@ApiModelProperty(value = "시스템 구분 코드3")
	private String uSystemKey3;
	@ApiModelProperty(value = "연동형태(HTML, 원문)")
	private String uAttachType;
	@ApiModelProperty(value = "보안등급구분 (~까지보이기)")
	private String uLimitSecLevel;
	@ApiModelProperty(value = "허용 문서상태")
	private String uDocStatus;
	@ApiModelProperty(value = "정책 비활성화 여부")
	private boolean uInactiveFlag;
	@ApiModelProperty(value = "DRM 적용 여부")
	private boolean uDrmFlag;
	@ApiModelProperty(value = "외부/내부 여부")
	private boolean uExternalFlag;
	@ApiModelProperty(value = "메신저 여부")
	private boolean uMessengerFlag;
	@ApiModelProperty(value = "사용자 여부")
  private boolean uForUserFlag;
	@ApiModelProperty(value = "문서 완료 여부")
  private boolean uDocComplete;
	
	/**
	 * Agent URL 정책 조회 Format 변경
	 * @param attachPolicyList
	 * @return
	 */
	public static List<Map<String, Object>> toMapList(List<AttachPolicy> attachPolicyList) {
		
		List<Map<String, Object>> maps = new ArrayList<>();
		
		for (AttachPolicy attachPolicy : attachPolicyList) {
			
			String sControl = "2";	// 0: 사용안함, 1 : 링크, 2 : 암호문서, 4 : 복호화문서 ==> 최종 다운로드 할때다시 확인 하기때문에 4는 사용안함.
			
			if(StringUtils.isBlank(attachPolicy.getUAttachType()))
			{ 
				sControl	= "2";	// null 이면 암호화.
			}
			else if (attachPolicy.getUAttachType().equals("H")) {
				sControl	= "1";	// 
			}
			else if (attachPolicy.getUAttachType().equals("O")) {
				sControl	= "2";
			}
			else
			{
				sControl	= attachPolicy.getUAttachType();
			}
			
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("ONLINE_NAME"		, attachPolicy.getUSystemKey1());				
			map.put("CONTROL"			, sControl);
			maps.add(map);
		}
		return maps;
  }
}
