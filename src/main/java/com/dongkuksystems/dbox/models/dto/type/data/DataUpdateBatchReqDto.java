package com.dongkuksystems.dbox.models.dto.type.data;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DataUpdateBatchReqDto {
    private boolean belowDoc;
    private boolean belowFolder;
    
	private List<DataUpdateReqDto> data;
}
