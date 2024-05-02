package com.dongkuksystems.dbox.models.type.request;




import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder 
public class ReqTakeoutDoc {
	  private String rObjectId;
	  @ApiModelProperty(value = "요청ID")
	  private String uReqId;
	  @ApiModelProperty(value = "요청문서 ID")
	  private String uReqDocId;
	  @ApiModelProperty(value = "요청문서 키")
	  private String uReqDocKey;
}
