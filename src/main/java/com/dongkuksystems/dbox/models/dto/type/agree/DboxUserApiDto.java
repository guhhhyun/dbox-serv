package com.dongkuksystems.dbox.models.dto.type.agree;

import java.time.LocalDateTime;

import com.documentum.fc.client.IDfPersistentObject;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.common.DfId;
import com.documentum.fc.common.DfTime;
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
public class DboxUserApiDto {

    @ApiModelProperty(value = "Object ID", required = true)
    private String rObjectId             ;

    @ApiModelProperty(value = "D'box User Id", required = true)
    private String uUserId             ;

    @ApiModelProperty(value = "D'box 사용여부", required = true)
    private String uUseYn             ;
    
    @ApiModelProperty(value = "권한코드", required = true)
    private String uMngCd             ;
    
}
