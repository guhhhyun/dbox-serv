package com.dongkuksystems.dbox.models.type.manager.stopword;



import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StopWord {
	@ApiModelProperty(value = "불용어")
	private String stopword;
	@ApiModelProperty(value = "검색제외")
	private String blindDeptNm;
	@ApiModelProperty(value = "그룹사코드")
	private String companyCode;
	@ApiModelProperty(value = "그룹사")
	private String companyName;
	@ApiModelProperty(value = "요청ID")
	private String createUserId;
	@ApiModelProperty(value = "삭제요청ID")
	private String deleteUserId;
	
	@ApiModelProperty(value = "설정일")
	private String createTimestamp;
	@ApiModelProperty(value = "해제일")
	private String deleteTimestamp;
	@ApiModelProperty(value = "요청자")
	private String createUserNm;
	@ApiModelProperty(value = "삭제요청자")
	private String deleteUserNm;
	
	
	 

	
	 

}
