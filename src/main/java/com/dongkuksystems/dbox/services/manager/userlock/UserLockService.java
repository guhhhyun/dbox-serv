package com.dongkuksystems.dbox.services.manager.userlock;

import java.util.List;
import java.util.Optional;

import com.dongkuksystems.dbox.models.common.UserSession;
import com.dongkuksystems.dbox.models.dto.type.code.CodeDetailDto;
import com.dongkuksystems.dbox.models.dto.type.manager.deptinformconfig.DeptInformConfigDto;
import com.dongkuksystems.dbox.models.dto.type.user.LockUserDto;
import com.dongkuksystems.dbox.models.dto.type.user.UserLockFilterDto;
import com.dongkuksystems.dbox.models.type.manager.deptinformconfig.DeptInformConfig;
import com.dongkuksystems.dbox.models.type.user.UserLock;

public interface UserLockService {
	List<UserLock> selectUserLocks(UserLockFilterDto dto);
	void patchUserLock(String rObjectId, String userObjectId, UserSession userSession, LockUserDto dto) throws Exception;
	void registUserLock(UserSession userSession, LockUserDto dto) throws Exception;
	Optional<DeptInformConfig> selectListByOrgId(String uComCode, String uDeptCode);
	void patchCode(String uCodeType, UserSession userSession, CodeDetailDto dto) throws Exception;
	void patchDeptInform(UserSession userSession, DeptInformConfigDto dto) throws Exception;
	List<DeptInformConfigDto> selectListDept(String uCodeCode);


}
