package com.dongkuksystems.dbox.models.dto.type.manager.stopword;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StopWordDto {
	private String uStopWord;
	private String uBlindDept;
	private String uComCode;
	private String uComCodeNm;
	private String uCreateId;
	private String uDeleteId;
	private String uCreateTmp;
	private String uDeleteTmp;
	private String uCreateNm;
	private String uDeleteNm;
}

