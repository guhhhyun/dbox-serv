package com.dongkuksystems.dbox.services.code;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.dongkuksystems.dbox.constants.EntCode;
import com.dongkuksystems.dbox.models.common.UserSession;
import com.dongkuksystems.dbox.models.dto.type.code.CodeDetailDto;
import com.dongkuksystems.dbox.models.dto.type.code.CodeFilterDto;
import com.dongkuksystems.dbox.models.type.code.Code;
import com.dongkuksystems.dbox.models.type.code.CodeLogviewAuth;

public interface CodeService {
	public List<CodeDetailDto> getCodeList(CodeFilterDto codeFilterDto) throws Exception;

	public Map<String, String> getSecLevelMap() throws Exception;

	public Map<String, String> getFolStatusMap() throws Exception;

	public Map<String, String> getDocStatusMap() throws Exception;

	public Map<String, String> getNotiItemMap() throws Exception;

	public Map<String, String> getComCodeMap() throws Exception;
	
	public Map<String, String> getDeniedFormatCodeMap() throws Exception;

  public Map<String, String> getClosedFormatCodeMap() throws Exception;
  
  public Map<String, String> getDrmFormatCodeMap() throws Exception;

	public Map<String, String> getDocHandleListMap() throws Exception;

	public Map<String, String> getConfigDocHandleLimitMap(EntCode entCode) throws Exception;
	
	public Map<String, Code> getCommonCabinetDeptMap() throws Exception;

  public Map<String, String> getAllAcessUserMap() throws Exception;
  
	public Set<String> getSpecialUserIdSet() throws Exception;
	
	public Map<String, String> getPreserveMap() throws Exception;
	
	public Map<String, String> getConjfigMidSaveDeptMap() throws Exception;
	
	public Map<String, String> getConfigTransWfMap() throws Exception;	
	
	public Map<String, String> getConfigUsbBasePolicyMap() throws Exception;
	
	public Map<String, String> getConfigVerDelPeriodMap() throws Exception;
	
	public Map<String, String> getConfigDeletePeriodMap() throws Exception;
	
	public String getAgentInstallerId() throws Exception;

	public void initAgentInstallerId() throws Exception;

	public void initSecLevelMap() throws Exception;

	public void initDeniedFormatMap() throws Exception;
	
	public void initClosedFormatMap() throws Exception;

	public void initDrmFormatMap() throws Exception;

	public void initFolStatusMap() throws Exception;

	public void initDocStatusMap() throws Exception;

	public void initNotiItemMap() throws Exception;

	public void initComCodeMap() throws Exception;

	public void initDocHandleListMap() throws Exception;

	public void initConfigDocHandleLimitMap() throws Exception;

	public void initCommonCabinetDeptMap() throws Exception;

	public void initSpecialUserIdSet() throws Exception;
	
	public void initGetComCodeDetail() throws Exception;
	
	public void initGetDrmFormatCodeMap() throws Exception;
	
	public void initCodesByUCodeType() throws Exception;

	public CodeDetailDto getComCodeDetail(String uCod0eVal1) throws Exception;

	public List<CodeLogviewAuth> getCodeLogviewAuthList(String codeType, CodeFilterDto filter) throws Exception;

	public void patchCodeLogviewAuth(String rObjectId, UserSession userSession, CodeDetailDto dto) throws Exception;

	public void initPreserveMap() throws Exception;
	
	public void initConjfigMidSaveDeptMap() throws Exception;

	public void initConfigTransWfMap() throws Exception; 
	
	public void initConfigUsbBasePolicyMap() throws Exception;
	
	public void initConfigVerDelPeriodMap() throws Exception;
	
	public void initConfigDeletePeriodMap() throws Exception;
	
	public List<CodeDetailDto> getMenuList(UserSession userSession, CodeFilterDto codeFilterDto) throws Exception;

	public List<CodeDetailDto> getHistoryMenuList(UserSession userSession, CodeFilterDto codeFilterDto) throws Exception;


}
