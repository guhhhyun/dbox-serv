package com.dongkuksystems.dbox.models.dto.type.data;

import java.util.List;

import com.dongkuksystems.dbox.models.dto.type.auth.AuthBaseUpdateDto;
import com.dongkuksystems.dbox.models.dto.type.auth.AuthShareUpdateDto;
import com.dongkuksystems.dbox.models.dto.type.doc.DocDetailDto;
import com.dongkuksystems.dbox.models.dto.type.folder.FolderDetailDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DataUpdateReqDto {
	private FolderDetailDto folder;
	private DocDetailDto doc;

	private List<AuthBaseUpdateDto> grantLiveAuths;			// Live 권한부여목록
	private List<AuthBaseUpdateDto> revokeLiveAuths;		// Live 권한제거목록
	private List<AuthBaseUpdateDto> grantClosedAuths;		// Closed 권한부여목록
	private List<AuthBaseUpdateDto> revokeClosedAuths;	// Closed 권한제거목록
	private List<AuthShareUpdateDto> grantShares;				// 공유/협업 추가
	private List<AuthShareUpdateDto> revokeShares;			// 공유/협업 제거
}
