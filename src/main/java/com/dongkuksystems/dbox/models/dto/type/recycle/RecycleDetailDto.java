package com.dongkuksystems.dbox.models.dto.type.recycle;

import java.time.LocalDateTime;

import com.dongkuksystems.dbox.models.dto.type.doc.DocDetailDto;
import com.dongkuksystems.dbox.models.dto.type.folder.FolderDetailDto;
import com.dongkuksystems.dbox.models.type.doc.Doc;
import com.dongkuksystems.dbox.models.type.recycle.Recycle;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
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
public class RecycleDetailDto {
	private String dataType;
	@JsonUnwrapped
	private DocRecycleDto docDto;
	@JsonUnwrapped
	private FolRecycleDto folDto;
	@JsonUnwrapped
  	private ProjectRecycleDto pjtDto;
	@JsonUnwrapped
  	private ResearchRecycleDto rschDto;
	
}
