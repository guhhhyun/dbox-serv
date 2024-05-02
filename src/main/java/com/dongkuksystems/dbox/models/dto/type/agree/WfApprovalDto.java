package com.dongkuksystems.dbox.models.dto.type.agree;

import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

import com.documentum.fc.client.DfQuery;
import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfDocument;
import com.documentum.fc.client.IDfPersistentObject;
import com.documentum.fc.client.IDfQuery;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.common.DfTime;
import com.documentum.fc.common.IDfTime;
import com.dongkuksystems.dbox.constants.AclTemplate;
import com.dongkuksystems.dbox.constants.GrantedLevels;
import com.dongkuksystems.dbox.constants.SecLevelCode;
import com.dongkuksystems.dbox.models.dto.type.upload.AttachedFile;
import com.dongkuksystems.dbox.models.dto.type.upload.AttachedKUploadFile;
import com.dongkuksystems.dbox.models.dto.type.upload.UploadDocDto;
import com.dongkuksystems.dbox.utils.dctm.DCTMUtils;
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
public class WfApprovalDto {
      @ApiModelProperty(value = "IrisId")
      private String IrisId;
	  @ApiModelProperty(value = "reqId")
	  private String reqId;
	  @ApiModelProperty(value = "결재구분")
	  private String guBun;
	  @ApiModelProperty(value = "승인/반려 구분")
	  private String resultGubun;
	   @ApiModelProperty(value = "결재자 ID")
	  private String approvalId;
       @ApiModelProperty(value = "반려사유")
      private String rejectReason;

}
