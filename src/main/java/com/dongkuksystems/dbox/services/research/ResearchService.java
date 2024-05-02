package com.dongkuksystems.dbox.services.research;

import java.util.List;
import java.util.Optional;

import com.documentum.fc.client.IDfSession;
import com.dongkuksystems.dbox.models.common.UserSession;
import com.dongkuksystems.dbox.models.dto.type.docbox.research.ResearchCountDto;
import com.dongkuksystems.dbox.models.dto.type.docbox.research.ResearchCreateDto;
import com.dongkuksystems.dbox.models.dto.type.docbox.research.ResearchDetailDto;
import com.dongkuksystems.dbox.models.dto.type.docbox.research.ResearchFilterDto;
import com.dongkuksystems.dbox.models.dto.type.docbox.research.ResearchRepeatDto;
import com.dongkuksystems.dbox.models.dto.type.docbox.research.ResearchUpdateDto;
import com.dongkuksystems.dbox.models.type.docbox.Research;

public interface ResearchService {
	ResearchDetailDto selectResearch(String researchId, String orgId, String userId) throws Exception;
	Optional<Research> selectResearchByURschCode(String uRschCode) throws Exception;

	/**
	 * 연구과제 리스트 조회
	 *  
	 * @param researchFilterDto 검색조건
	 * @param orgId 사용자 부서코드
	 * @param userId 사용자 아이디 (하위 폴더/문서 여부 파악용, null일 경우 조회 안함)
	 * @return 연구과제 리스트
	 */
	List<Research> selectResearchList(ResearchFilterDto researchFilterDto, String orgId, String userId) throws Exception;
	
	/**
	 * 연구과제 개수 조회
	 * 
	 * @param researchFilterDto 검색조건
	 * @param orgId 사용자 부서코드
	 * @return 연구과제 개수 (주관 진행, 주관 완료, 참여 진행, 잠여 완료)
	 */
	ResearchCountDto selectResearchCount(ResearchFilterDto researchFilterDto, String orgId, String userId) throws Exception;

	void makeResearchFinished(UserSession userSession, String researchCode) throws Exception;
	String createResearch(UserSession userSession, ResearchCreateDto dto, IDfSession session) throws Exception;
	String updateResearch(UserSession userSession, ResearchUpdateDto dto, IDfSession session) throws Exception;
	List<ResearchRepeatDto> selectRepeatListByCode(String rschCode) throws Exception;
}
