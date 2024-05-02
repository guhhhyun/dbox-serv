package com.dongkuksystems.dbox.services.drm;

import java.io.File;
import java.io.InputStream;
import java.util.List;

import com.dongkuksystems.dbox.constants.DrmCompanyId;
import com.dongkuksystems.dbox.constants.DrmSecLevelType;
import com.dongkuksystems.dbox.models.dto.etc.DrmCompanyDto;
import com.dongkuksystems.dbox.models.dto.etc.DrmDeptDto;
import com.dongkuksystems.dbox.models.dto.etc.DrmUserDto;

public interface DrmService {
	public File encrypt(InputStream inputStream, DrmSecLevelType secLevelType, DrmCompanyDto company,
			List<DrmDeptDto> authDepts, List<DrmUserDto> authUsers, String docId, String docKey,  String fileName, long fileSize, String loginId,
			String userName, String deptCode, String deptName, String entCode, String entName, boolean disableSaveEdit, String ip) throws Exception;

	public File decrypt(InputStream inputStream, String fileName, long fileSize) throws Exception;

	public boolean check(InputStream inputStream, String fileName, long fileSize) throws Exception;
}
