package com.dongkuksystems.dbox.services.code;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.modelmapper.ModelMapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.documentum.fc.client.IDfPersistentObject;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.common.DfId;
import com.dongkuksystems.dbox.constants.CodeSpecialUserType;
import com.dongkuksystems.dbox.constants.CodeType;
import com.dongkuksystems.dbox.constants.EntCode;
import com.dongkuksystems.dbox.daos.table.etc.gwuser.GwUserDao;
import com.dongkuksystems.dbox.daos.type.code.CodeDao;
import com.dongkuksystems.dbox.errors.BadRequestException;
import com.dongkuksystems.dbox.models.common.UserSession;
import com.dongkuksystems.dbox.models.dto.type.code.CodeDetailDto;
import com.dongkuksystems.dbox.models.dto.type.code.CodeFilterDto;
import com.dongkuksystems.dbox.models.dto.type.user.UserPresetFilterDto;
import com.dongkuksystems.dbox.models.type.code.Code;
import com.dongkuksystems.dbox.models.type.code.CodeLogviewAuth;
import com.dongkuksystems.dbox.models.type.user.UserPreset;
import com.dongkuksystems.dbox.services.AbstractCommonService;

@Service
public class CodeServiceImpl extends AbstractCommonService implements CodeService {
	private final CodeDao codeDao;
	private final GwUserDao gwUserDao;

	public CodeServiceImpl(CodeDao codeDao, GwUserDao gwUserDao) {
		this.codeDao = codeDao;
		this.gwUserDao = gwUserDao;
	}

	@Override
	public List<CodeDetailDto> getCodeList(CodeFilterDto codeFilterDto) throws Exception {
		// uCodeType 필수
		String uCodeType = Optional.ofNullable(codeFilterDto.getUCodeType())
				.orElseThrow(() -> new BadRequestException("uCodeType is null"));

		List<CodeDetailDto> codeDetailDtoList = selectCodesByUCodeType(uCodeType);
		List<CodeDetailDto> result = codeDetailDtoList.stream()
				.filter(item -> null == codeFilterDto.getUCodeVal1()
						|| Objects.equals(item.getUCodeVal1(), codeFilterDto.getUCodeVal1()))
				.filter(item -> null == codeFilterDto.getUCodeVal2()
						|| Objects.equals(item.getUCodeVal2(), codeFilterDto.getUCodeVal2()))
				.filter(item -> null == codeFilterDto.getUCodeVal3()
						|| Objects.equals(item.getUCodeVal3(), codeFilterDto.getUCodeVal3()))
				.collect(Collectors.toList());
		return result;
	}

	@Override
	@Cacheable(value = "getSecLevelMap")
	public Map<String, String> getSecLevelMap() throws Exception {
		Map<String, String> codeMap = makeCodeMap(CodeType.SEC_LEVEL, item -> item.getUCodeVal1(),
				item -> item.getUCodeName1());
		return codeMap;
	}

	@Override
	@Cacheable(value = "getFolStatusMap")
	public Map<String, String> getFolStatusMap() throws Exception {
		Map<String, String> codeMap = makeCodeMap(CodeType.FOLDER_STATUS, item -> item.getUCodeVal1(),
				item -> item.getUCodeName1());
		return codeMap;
	}

	@Override
	@Cacheable(value = "getDocStatusMap")
	public Map<String, String> getDocStatusMap() throws Exception {
		Map<String, String> codeMap = makeCodeMap(CodeType.DOCUMENT_STATUS, item -> item.getUCodeVal1(),
				item -> item.getUCodeName1());
		return codeMap;
	}

	@Override
	@Cacheable(value = "getNotiItemMap")
	public Map<String, String> getNotiItemMap() throws Exception {
		Map<String, String> codeMap = makeCodeMap(CodeType.NOTI_ITEM, item -> item.getUCodeVal1(),
				item -> item.getUCodeName1());
		return codeMap;
	}

	@Override
	@Cacheable(value = "getComCodeMap")
	public Map<String, String> getComCodeMap() throws Exception {
		Map<String, String> codeMap = makeCodeMap(CodeType.COM_CODE, item -> item.getUCodeVal1(),
				item -> item.getUCodeName1());
		return codeMap;
	}
	
	@Override
	@Cacheable(value = "getDocHandleListMap")
	public Map<String, String> getDocHandleListMap() throws Exception {
		Map<String, String> codeMap = makeCodeMap(CodeType.DOC_HANDLE_LIST, item -> item.getUCodeVal1(),
				item -> item.getUCodeName1());
    return codeMap;
	}
	
  @Override
  @Cacheable(value = "getDeniedFormatCodeMap")
  public Map<String, String> getDeniedFormatCodeMap() throws Exception {
    Map<String, String> codeMap = makeCodeMap(CodeType.DENIED_FILE_FORMAT, item -> item.getUCodeVal1(),
        item -> item.getUCodeName1());
    return codeMap;
  }

  @Override
  @Cacheable(value = "getClosedFormatCodeMap")
  public Map<String, String> getClosedFormatCodeMap() throws Exception {
    Map<String, String> codeMap = makeCodeMap(CodeType.CLOSED_FILE_FORMAT, item -> item.getUCodeVal1(),
        item -> item.getUCodeName1());
    return codeMap;
  }

  @Override
  @Cacheable(value = "getAllAcessUserMap")
  public Map<String, String> getAllAcessUserMap() throws Exception {
    Map<String, String> codeMap = makeCodeMap(CodeType.ALL_ACCESS_USER, item -> item.getUCodeVal1(),
        item -> item.getUCodeName1());
    return codeMap;
  }
  
  @Override
  @Cacheable(value = "getDrmFormatCodeMap")
  public Map<String, String> getDrmFormatCodeMap() throws Exception {
    Map<String, String> codeMap = makeCodeMap(CodeType.DRM_FILE_FORMAT, item -> item.getUCodeVal1(),
        item -> item.getUCodeName1());
    return codeMap;
  }  
  
	@Override
	@Cacheable(value = "getConfigDocHandleLimitMap", key="#entCode.value")
//	@Cacheable(value = "getConfigDocHandleLimitMap", key="#entCode")
	public Map<String, String> getConfigDocHandleLimitMap(EntCode entCode) throws Exception {
		Map<String, List<Code>> codeGroupByMap = makeCodeGroupByMap(CodeType.CONFIG_DOC_HANDLE_LIMIT, item -> item.getUCodeVal1());

		Map<String, String> codeMap = makeCodeMap(codeGroupByMap.get(entCode.name()), item -> item.getUCodeVal2(),
				item -> item.getUCodeVal3());
		
		return codeMap;
	}

	@Override
	@Cacheable(value = "getCommonCabinetDeptMap")
	public Map<String, Code> getCommonCabinetDeptMap() throws Exception {
		Map<String, Code> codeMap = makeCodeMap(CodeType.COMMON_CABINET_DEPT, item -> item.getUCodeVal2(), item -> item);

		return codeMap;
	}

	@Override
	@Cacheable(value = "getSpecialUserIdSet")
	public Set<String> getSpecialUserIdSet() throws Exception {
		Map<String, List<Code>> codeGroupByMap = makeCodeGroupByMap(CodeType.SPECIAL_USER, item -> item.getUCodeVal1());

		// 대상 사용자 아이디 리스트
		List<Code> userTypeList = Optional.ofNullable(codeGroupByMap.get(CodeSpecialUserType.USER.getValue()))
				.orElse(new ArrayList<>());
		List<String> userIdSpecialUserList = userTypeList.stream().map(item -> item.getUCodeVal2())
				.collect(Collectors.toList());

		// 직책 기준 특별사용자 리스트
		List<Code> jobTitleTypeList = Optional.ofNullable(codeGroupByMap.get(CodeSpecialUserType.JOB_TITLE.getValue()))
				.orElse(new ArrayList<>());
		List<String> jobTitleList = jobTitleTypeList.stream().map(item -> item.getUCodeVal2()).collect(Collectors.toList());
		List<String> jobTitleSpecialUserIdList = gwUserDao.selectUserIdListByTitleCodesForSpecialUser(jobTitleList);

		// 전체 리스트 Set으로 합치기
		Set<String> specialUserIdSet = Stream.concat(userIdSpecialUserList.stream(), jobTitleSpecialUserIdList.stream())
				.collect(Collectors.toSet());

		return specialUserIdSet;
	}

  @Override
  @Cacheable(value = "getAgentInstallerId")
  public String getAgentInstallerId() throws Exception {
    CodeFilterDto codeFilterDto = CodeFilterDto.builder().uCodeType(CodeType.AGENT_INS_ID.getValue()).build();
    List<Code> codeList = codeDao.selectList(codeFilterDto);
    if (codeList.size() == 0 || codeList == null) throw new BadRequestException("installer Id가 없습니다.");
    return codeList.get(0).getUCodeVal1();
  }
  
  @Override
  @Cacheable(value = "getPreserveMap")
  public Map<String, String> getPreserveMap() throws Exception {
    Map<String, String> codeMap = makeCodeMap(CodeType.PRESERVE, item -> item.getUCodeVal1(),
        item -> item.getUCodeName1());
    return codeMap;
  }
  
  @Override
  @Cacheable(value = "getConjfigMidSaveDeptMap")
  public Map<String, String> getConjfigMidSaveDeptMap() throws Exception {
    Map<String, String> codeMap = makeCodeMap(CodeType.CONFIG_MID_SAVE_DEPT, item -> item.getUCodeVal2(),
        item -> item.getUCodeName2());
    return codeMap;
  }
  
  @Override
  @Cacheable(value = "getConfigTransWfMap")
  public Map<String, String> getConfigTransWfMap() throws Exception {
    Map<String, String> codeMap = makeCodeMap(CodeType.CONFIG_TRANS_WF, item -> item.getUCodeVal1(),
        item -> item.getUCodeVal2());
    return codeMap;
  }

  @Override
  @Cacheable(value = "getConfigUsbBasePolicyMap")
  public Map<String, String> getConfigUsbBasePolicyMap() throws Exception {
    Map<String, String> codeMap = makeCodeMap(CodeType.CONFIG_USB_BASE_POLICY, item -> item.getUCodeVal1(),
        item -> item.getUCodeVal2());
    return codeMap;
  }
  
  @Override
  @Cacheable(value = "getConfigVerDelPeriodMap")
  public Map<String, String> getConfigVerDelPeriodMap() throws Exception {
    Map<String, String> codeMap = makeCodeMap(CodeType.CONFIG_VER_DEL_PERIOD, item -> item.getUCodeVal1(),
        item -> item.getUCodeVal2());
    return codeMap;
  }
  
  @Override
  @Cacheable(value = "getConfigDeletePeriodMap")
  public Map<String, String> getConfigDeletePeriodMap() throws Exception {
    Map<String, String> codeMap = makeCodeMap(CodeType.CONFIG_DELETE_PERIOD, item -> item.getUCodeVal1(),
        item -> item.getUCodeVal3());
    return codeMap;
  }
  
  @Override
  @CacheEvict(value = "getAgentInstallerId", allEntries = true)
  public void initAgentInstallerId() throws Exception {
    logger.info("initAgentInstallerId");
  }
  
	@Override
	@CacheEvict(value = "getSecLevelMap", allEntries = true)
	public void initSecLevelMap() throws Exception {
		logger.info("initSecLevelMap");
	}

	@Override
	@CacheEvict(value = "getFolStatusMap", allEntries = true)
	public void initFolStatusMap() throws Exception {
		logger.info("initFolStatusMap");
	}

	@Override
	@CacheEvict(value = "getDocStatusMap", allEntries = true)
	public void initDocStatusMap() throws Exception {
		logger.info("initDocStatusMap");
	}

	@Override
	@CacheEvict(value = "getNotiItemMap", allEntries = true)
	public void initNotiItemMap() throws Exception {
		logger.info("initNotiItemMap");
	}

	@Override
	@CacheEvict(value = "getComCodeMap", allEntries = true)
	public void initComCodeMap() throws Exception {
		logger.info("initComCodeMap");
	}
	
	@Override
	@CacheEvict(value = "getDocHandleListMap", allEntries = true)
	public void initDocHandleListMap() throws Exception {
		logger.info("initDocHandleListMap");
	}

	@Override
	@CacheEvict(value = "getConfigDocHandleLimitMap", allEntries = true)
	public void initConfigDocHandleLimitMap() throws Exception {
		logger.info("initConfigDocHandleLimitMap");
	}

	@Override
	@CacheEvict(value = "getCommonCabinetDeptMap", allEntries = true)
	public void initCommonCabinetDeptMap() throws Exception {
		logger.info("initCommonCabinetDeptMap");
	}

	@Override
	@CacheEvict(value = "getSpecialUserIdSet", allEntries = true)
	public void initSpecialUserIdSet() throws Exception {
		logger.info("initSpecialUserIdSet");
	}
	
	@Override
	@CacheEvict(value = "getComCodeDetail", allEntries = true)
	public void initGetComCodeDetail() throws Exception {
	  logger.info("initGetComCodeDetail");
	}
	
	@Override
	@CacheEvict(value = "getDrmFormatCodeMap", allEntries = true)
	public void initGetDrmFormatCodeMap() throws Exception {
	  logger.info("initGetDrmFormatCodeMap");
	}

  @Override
  @CacheEvict(value = "getDeniedFormatCodeMap", allEntries = true)
  public void initDeniedFormatMap() throws Exception {
    logger.info("initDeniedFormatMap");
  }
  
  @Override
  @CacheEvict(value = "getClosedFormatCodeMap", allEntries = true)
  public void initClosedFormatMap() throws Exception {
    logger.info("getClosedFormatCodeMap");
  }
  
  @Override
  @CacheEvict(value = "getDrmFormatCodeMap", allEntries = true)
  public void initDrmFormatMap() throws Exception {
    logger.info("getDrmFormatCodeMap");
  }
  
  @Override
  @CacheEvict(value = "getPreserveMap", allEntries = true)
  public void initPreserveMap() throws Exception {
    logger.info("initPreserveMap");
  }
 
  @Override
  @CacheEvict(value = "getConjfigMidSaveDeptMap", allEntries = true)
  public void initConjfigMidSaveDeptMap() throws Exception {
    logger.info("initConjfigMidSaveDeptMap");
  }
  
  @Override
  @CacheEvict(value = "getConfigTransWfMap", allEntries = true)
  public void initConfigTransWfMap() throws Exception {
    logger.info("initConfigTransWfMap");
  }
  
  @Override
  @CacheEvict(value = "getConfigUsbBasePolicyMap", allEntries = true)
  public void initConfigUsbBasePolicyMap() throws Exception {
    logger.info("initConfigUsbBasePolicyMap");
  }
  
  @Override
  @CacheEvict(value = "getConfigVerDelPeriodMap", allEntries = true)
  public void initConfigVerDelPeriodMap() throws Exception {
    logger.info("initConfigVerDelPeriodMap");
  }
  
  @Override
  @CacheEvict(value = "getConfigDeletePeriodMap", allEntries = true)
  public void initConfigDeletePeriodMap() throws Exception {
    logger.info("initConfigDeletePeriodMap");
  }
  
	@Override
	@Cacheable(value = "getComCodeDetail")
	public CodeDetailDto getComCodeDetail(String uCod0eVal1) throws Exception {
		CodeFilterDto codeFilterDto = CodeFilterDto.builder().uCodeType(CodeType.COM_CODE.getValue()).build();
		List<Code> codeList = codeDao.selectList(codeFilterDto);
		final ModelMapper modelMapper = getModelMapper();
		CodeDetailDto codeDetailDto = codeList.stream().map(item -> modelMapper.map(item, CodeDetailDto.class))
				.filter(item -> item.getUCodeVal1().equals(uCod0eVal1) )
                .findFirst().orElse(new CodeDetailDto());
//		CodeDetailDto codeDetailDto = result.get();
		return codeDetailDto;
	}
	
	
	@Cacheable(value = "selectCodesByUCodeType", key = "#uCodeType")
	private List<CodeDetailDto> selectCodesByUCodeType(String uCodeType) throws Exception {
		CodeFilterDto codeFilterDto = CodeFilterDto.builder().uCodeType(uCodeType).build();
		List<Code> codeList = codeDao.selectList(codeFilterDto);

		final ModelMapper modelMapper = getModelMapper();
		List<CodeDetailDto> result = codeList.stream().map(item -> modelMapper.map(item, CodeDetailDto.class))
				.collect(Collectors.toList());

		return result;
	}

	@Override
	@CacheEvict(value = "selectCodesByUCodeType", allEntries = true)
	public void initCodesByUCodeType() throws Exception {
		logger.info("initCodesByUCodeType");
	}

	/**
	 * codeType에 해당하는 코드의 map 생성
	 */
	private <T> Map<String, T> makeCodeMap(CodeType codeType, Function<Code, String> keyMapper,
			Function<Code, T> valueMapper) throws Exception {
		CodeFilterDto codeFilterDto = CodeFilterDto.builder().uCodeType(codeType.getValue()).build();
		List<Code> codeList = codeDao.selectList(codeFilterDto);

		Map<String, T> codeMap = makeCodeMap(codeList, keyMapper, valueMapper);

		return codeMap;
	}

	/**
	 * codeType에 해당하는 코드의 map 생성
	 */
	private <T> Map<String, T> makeCodeMap(List<Code> codeList, Function<Code, String> keyMapper,
			Function<Code, T> valueMapper) throws Exception {
		Map<String, T> codeMap = codeList.stream().collect(Collectors.toMap(keyMapper, valueMapper));

		return codeMap;
	}

	/**
	 * codeType에 해당하는 코드를 group by 하여 나눈 map 생성
	 */
	private Map<String, List<Code>> makeCodeGroupByMap(CodeType codeType, Function<Code, String> classifier)
			throws Exception {
		CodeFilterDto codeFilterDto = CodeFilterDto.builder().uCodeType(codeType.getValue()).build();
		List<Code> codeList = codeDao.selectList(codeFilterDto);
		Map<String, List<Code>> codeGroupByMap = makeCodeGroupByMap(codeList, classifier);

		return codeGroupByMap;
	}

	/**
	 * codeType에 해당하는 코드를 group by 하여 나눈 map 생성
	 */
	private Map<String, List<Code>> makeCodeGroupByMap(List<Code> codeList, Function<Code, String> classifier)
			throws Exception {
		Map<String, List<Code>> codeGroupByMap = codeList.stream().collect(Collectors.groupingBy((classifier)));

		return codeGroupByMap;
	}


	@Override
	public List<CodeLogviewAuth> getCodeLogviewAuthList(String codeType, CodeFilterDto codeFilterDto) throws Exception {
		// uCodeType 필수
		String uCodeType = Optional.ofNullable(codeFilterDto.getUCodeType())
				.orElseThrow(() -> new BadRequestException("uCodeType is null"));

		List<CodeLogviewAuth> codeDetailDtoList = codeDao.selectLogview(codeType, codeFilterDto);

		return codeDetailDtoList;
	}
	

	@Override
	public void patchCodeLogviewAuth(String rObjectId, UserSession userSession, CodeDetailDto dto) throws Exception {
		// TODO Auto-generated method stub
		IDfSession idfSession = this.getIdfSession(userSession);

		IDfPersistentObject idf_PObj = idfSession.getObject(new DfId(rObjectId));

		try {		
			idf_PObj.setString("u_code_val3", dto.getUCodeVal3());
			idf_PObj.setString("u_code_name1", dto.getUCodeName1());
			idf_PObj.setString("u_code_name2", dto.getUCodeName2());
			idf_PObj.setString("u_code_name3", dto.getUCodeName3());		
		
			idf_PObj.save();  

		} catch(Exception e) {
			e.printStackTrace();
		} finally {
		  if (idfSession != null  && idfSession.isConnected()) {
			  idfSession.disconnect();
	        }
		}
		
	}
	
	
	 @Override
	  public List<CodeDetailDto> getMenuList(UserSession userSession, CodeFilterDto codeFilterDto) throws Exception {
	    String uUserId = userSession.getUser().getUserId(); 
	    codeFilterDto.setUCodeType(CodeType.MENU_CATEGORY.getValue());
	    codeFilterDto.setUCodeVal3("Y"); //사용여부
	    
	    List<CodeDetailDto> result = codeDao.selectMenuList(uUserId, codeFilterDto);
	    return result;
	  }
	
	@Override
	public List<CodeDetailDto> getHistoryMenuList(UserSession userSession, CodeFilterDto codeFilterDto) throws Exception {
	    String uUserId 		= userSession.getUser().getUserId();
	    String uUserOrgId	= userSession.getUser().getComOrgId();
	    codeFilterDto.setUCodeType(CodeType.CONFIG_LOGVIEW_AUTH.getValue());
	    
	    List<CodeDetailDto> result = codeDao.getHistoryMenuList(uUserId, uUserOrgId, codeFilterDto);
	    return result;
	}


}