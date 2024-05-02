package com.dongkuksystems.dbox.models.dto.type.data;

import com.dongkuksystems.dbox.models.dto.type.doc.DocDetailDto;
import com.dongkuksystems.dbox.models.dto.type.folder.FolderDetailDto;
import com.fasterxml.jackson.annotation.JsonUnwrapped;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DataDetailDto {
	private String dataType;
	@JsonUnwrapped
	private FolderDetailDto folder;
	@JsonUnwrapped
	private DocDetailDto doc;

}

