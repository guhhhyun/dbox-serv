package com.dongkuksystems.dbox.services.manager.duplication;

import java.util.List;
import java.util.Map;

import com.dongkuksystems.dbox.models.common.UserSession;
import com.dongkuksystems.dbox.models.dto.type.manager.duplication.DuplicationDto;
import com.dongkuksystems.dbox.models.dto.type.manager.duplication.PatchDuplicationDto;
import com.dongkuksystems.dbox.models.type.manager.duplication.Duplication;

public interface DuplicationService {
	
	List<Duplication> selectAll(DuplicationDto dto, long offset, int limit);
	
	List<Duplication> selectList(DuplicationDto dto);
	
	String patchDuplication(String rObjectId, UserSession userSession, PatchDuplicationDto dto) throws Exception;

  Map<String, Integer> sendAllMail(UserSession userSession, DuplicationDto dto) throws Exception;

  int selectAllCount(DuplicationDto dto);
	
}
