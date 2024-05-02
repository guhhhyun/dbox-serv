package com.dongkuksystems.dbox.services.agent;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.documentum.fc.client.IDfPersistentObject;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.client.IDfVersionPolicy;
import com.documentum.fc.common.DfId;
import com.documentum.fc.common.DfTime;
import com.documentum.fc.common.IDfId;
import com.dongkuksystems.dbox.constants.AgentDownStatus;
import com.dongkuksystems.dbox.constants.CodeType;
import com.dongkuksystems.dbox.constants.Commons;
import com.dongkuksystems.dbox.constants.DCTMConstants;
import com.dongkuksystems.dbox.constants.DeleteStatus;
import com.dongkuksystems.dbox.constants.DocLogItem;
import com.dongkuksystems.dbox.constants.DocStatus;
import com.dongkuksystems.dbox.constants.DrmSecLevelType;
import com.dongkuksystems.dbox.constants.GrantedLevels;
import com.dongkuksystems.dbox.constants.HamType;
import com.dongkuksystems.dbox.constants.LimitSecLevelCode;
import com.dongkuksystems.dbox.constants.ProjectType;
import com.dongkuksystems.dbox.constants.SecLevelCode;
import com.dongkuksystems.dbox.daos.table.etc.gwdept.GwDeptDao;
import com.dongkuksystems.dbox.daos.type.auth.CommonAuthDao;
import com.dongkuksystems.dbox.daos.type.code.CodeDao;
import com.dongkuksystems.dbox.daos.type.doc.DocDao;
import com.dongkuksystems.dbox.daos.type.manager.agentpolicy.AgentDao;
import com.dongkuksystems.dbox.daos.type.manager.attachpolicy.AttachPolicyDao;
import com.dongkuksystems.dbox.daos.type.manager.preservationperiod.PreservationPeriodDao;
import com.dongkuksystems.dbox.daos.type.user.preset.UserPresetDao;
import com.dongkuksystems.dbox.errors.BadRequestException;
import com.dongkuksystems.dbox.errors.NotFoundException;
import com.dongkuksystems.dbox.models.common.UserSession;
import com.dongkuksystems.dbox.models.dto.etc.CustomInputStreamResource;
import com.dongkuksystems.dbox.models.dto.type.auth.DrmAuthDto;
import com.dongkuksystems.dbox.models.dto.type.auth.HamInfoResult;
import com.dongkuksystems.dbox.models.dto.type.code.CodeDetailDto;
import com.dongkuksystems.dbox.models.dto.type.code.CodeFilterDto;
import com.dongkuksystems.dbox.models.dto.type.data.DataDetailDto;
import com.dongkuksystems.dbox.models.dto.type.doc.DocDetailDto;
import com.dongkuksystems.dbox.models.dto.type.doc.DocFilterDto;
import com.dongkuksystems.dbox.models.dto.type.docbox.project.ProjectFilterDto;
import com.dongkuksystems.dbox.models.dto.type.docbox.research.ResearchFilterDto;
import com.dongkuksystems.dbox.models.dto.type.folder.FolderDetailDto;
import com.dongkuksystems.dbox.models.dto.type.folder.FolderFilterDto;
import com.dongkuksystems.dbox.models.dto.type.request.ReqTakeoutDetailDto;
import com.dongkuksystems.dbox.models.dto.type.upload.AttachedKUploadFile;
import com.dongkuksystems.dbox.models.dto.type.upload.CheckinDocDto;
import com.dongkuksystems.dbox.models.dto.type.upload.UploadDocDto;
import com.dongkuksystems.dbox.models.dto.type.user.UserPresetFilterDto;
import com.dongkuksystems.dbox.models.table.etc.GwUser;
import com.dongkuksystems.dbox.models.table.etc.VDept;
import com.dongkuksystems.dbox.models.table.etc.VUser;
import com.dongkuksystems.dbox.models.table.log.LogDoc;
import com.dongkuksystems.dbox.models.table.log.LogPcDocs;
import com.dongkuksystems.dbox.models.table.log.LogUsb;
import com.dongkuksystems.dbox.models.type.agent.AgentDoc;
import com.dongkuksystems.dbox.models.type.agent.AgentFolder;
import com.dongkuksystems.dbox.models.type.code.Code;
import com.dongkuksystems.dbox.models.type.doc.Doc;
import com.dongkuksystems.dbox.models.type.doc.DocImp;
import com.dongkuksystems.dbox.models.type.docbox.Project;
import com.dongkuksystems.dbox.models.type.docbox.Research;
import com.dongkuksystems.dbox.models.type.folder.Folder;
import com.dongkuksystems.dbox.models.type.manager.AttachPolicy;
import com.dongkuksystems.dbox.models.type.manager.UsbPolicy;
import com.dongkuksystems.dbox.models.type.request.ReqTakeout;
import com.dongkuksystems.dbox.models.type.user.UserPreset;
import com.dongkuksystems.dbox.services.AbstractCommonService;
import com.dongkuksystems.dbox.services.auth.AuthService;
import com.dongkuksystems.dbox.services.code.CodeService;
import com.dongkuksystems.dbox.services.data.DataPathService;
import com.dongkuksystems.dbox.services.data.DataService;
import com.dongkuksystems.dbox.services.dept.GwDeptService;
import com.dongkuksystems.dbox.services.doc.DocImpService;
import com.dongkuksystems.dbox.services.doc.DocService;
import com.dongkuksystems.dbox.services.drm.DrmService;
import com.dongkuksystems.dbox.services.folder.FolderService;
import com.dongkuksystems.dbox.services.login.LoginService;
import com.dongkuksystems.dbox.services.project.ProjectService;
import com.dongkuksystems.dbox.services.research.ResearchService;
import com.dongkuksystems.dbox.services.takeout.TakeoutReqService;
import com.dongkuksystems.dbox.services.user.UserService;
import com.dongkuksystems.dbox.utils.dctm.AES256Util;
import com.dongkuksystems.dbox.utils.dctm.DCTMUtils;
import com.google.common.collect.Lists;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class AgentServiceImpl extends AbstractCommonService implements AgentService {
	
	// @Slf4j 로 사용함.
	// private static final Logger logger = LoggerFactory.getLogger(AgentServiceImpl.class);

	@Value("${drm.dir}")
	private String drmDir;
	
	@Value("${linkfile.doc-key}")
	private String docKey;

	@Value("${linkfile.sec-level}")
	private String secLevel;

	@Value("${linkfile.preserv-flag}")
	private String preservFlag;

	@Value("${linkfile.text}")
	private String linkfileText;
	
	private final AttachPolicyDao 		attachPolicyDao;
	private final AgentDao 				agentDao;
	private final GwDeptDao 			gwDeptDao;
	private final UserPresetDao 		userPresetDao;
	private final CommonAuthDao 		commonAuthDao;
	private final CodeDao 				codeDao;
	private final DocDao 				docDao;
	private final PreservationPeriodDao psrvPeriodDao;
	private final FolderService 		folderService;
	private final UserService 			userSevice;
	private final DocService 			docService;
	private final CodeService 			codeService;
	private final DrmService 			drmService;
	private final ProjectService 		projectService;
	private final ResearchService 		researchService;
	private final ProjectService 		projService;
	private final DataService 			dataService;
	private final TakeoutReqService 	takeoutReqService;
	private final AuthService 			authService;
	private final GwDeptService 		gwDeptService;
	private final DataPathService 		pathService;
	private final DocImpService 		docImpService;
	private final UserService 			userService;
	private final LoginService 			loginService;
	
	public AgentServiceImpl(AttachPolicyDao attachPolicyDao, AgentDao agentDao, GwDeptDao gwDeptDao,
								FolderService folderService, UserService userSevice, DocService docService, CodeService codeService, DrmService drmService, UserPresetDao userPresetDao, CommonAuthDao commonAuthDao, ResearchService researchService, ProjectService projectService, ProjectService projService, DataService dataService, CodeDao codeDao, TakeoutReqService takeoutReqService, AuthService authService, GwDeptService gwDeptService, DataPathService pathService, DocDao docDao, DocImpService docImpService, PreservationPeriodDao psrvPeriodDao, UserService userService, LoginService loginService) {
		
		this.attachPolicyDao 	= attachPolicyDao;
		this.agentDao 			= agentDao;
		this.gwDeptDao 			= gwDeptDao;
		this.userPresetDao 		= userPresetDao;
		this.commonAuthDao 		= commonAuthDao;
		this.codeDao 			= codeDao;
		this.docDao 			= docDao;
		this.psrvPeriodDao 		= psrvPeriodDao;
		this.folderService 		= folderService;
		this.userSevice 		= userSevice;
		this.docService 		= docService;
		this.codeService 		= codeService;
		this.drmService 		= drmService;
		this.projectService 	= projectService;
		this.researchService 	= researchService;
		this.projService 		= projService;
		this.dataService 		= dataService;
		this.takeoutReqService 	= takeoutReqService;
		this.authService 		= authService;
		this.gwDeptService 		= gwDeptService;
		this.pathService 		= pathService;
		this.docImpService 		= docImpService;
		this.userService 		= userService;
		this.loginService 		= loginService;
		
	}

	@Override
	public JSONObject callFolderList(String user_id, String rid) throws Exception {
		
		log.info("[callFolderList] START : " + user_id);
		log.info("[callFolderList] START : " + rid);
		
		//==============================================
		// 폴더 리스트 조회 ( 1레벨 메뉴, 문서함, 폴더 리스트 )
		//==============================================
		// 호출.
		//		- 메뉴, 문서함, 폴더 리스트 조회
		//		- rid : 호출된 Key 값  최초 : ROOT
		// Return
		//		- list
		//			rid					: 폴더 코드
		//			text				: 폴더 이름
		//			r_folder_security	: 폴더 보안 등급
		//			r_link_type			: 0 : root 폴더, 1 : root 폴더 외
		//			folder_type			: 폴더 타입 user : 개인문서함, dept : 부서문서함, project : 공용문서함, CommonBox : 공통문서함, trash : 휴지통
		//			r_object_type		: dm_user_root : 개인업무함, dm_dept_root : 부서업무함, dm_group_root : 공용업무함, dm_common_root : 공통업무함, dm_folder : 폴더, dm_trash : 휴지통
		//		- totalCount
		// 		- return_code
		//		- return_msg
		//
		// 하위 종류
		// 1. ROOT 메뉴
		// 2. 1레벨 부서함, 프로젝트, 연구과제, 공유, 반춤함  5개 메뉴
		// 4.   하위 폴더 ( 전자결재 년도폴더,  프로젝트 완료함 분류폴더, 연구과제 완료함 분류폴더 )
		// 5.   하위 문서함, 프로젝트등..
		//==============================================
		
		
		// 리턴 기본정보
		JSONObject rtnJsonObject	= new JSONObject();	
		String	sReturnCode			= "0";					// RETURN CODE
		String	sReturnMsg			= "SUCCESS";			// RETURN MESSAGE

		// 
		boolean bIsHaveFolder		= false;	// 고정 폴더 필요 한지 여부
		boolean bIsSearchFolder 	= false;	// 폴더 조회후 추가해야될경우 
		boolean bIsSearchCabinet 	= false;	// 문서함 조회후 추가해야될경우
		boolean bIsSearchDoc	 	= false;	// 문서 조회후 추가해야될경우
		
		String  sCategoryId			= "";		// CATEGORY 선태된 폴더 ID
		String	sCategory			= "";		// CATEGORY 처리
	
		// 리턴 폴더
		List<Map<String, Object>> lReturnFolderList	= new ArrayList<Map<String,Object>>();
		
		try {

			// 사용자 정보 조회
			VUser vUser	 			= userSevice.selectOneByUserId( user_id ).orElseThrow(() -> new NotFoundException(VUser.class, "NO USER"));
			UserSession userSession	= UserSession.builder().dUserId(user_id).docbase(DCTMConstants.DOCBASE).user(vUser).build();
			
			
			// 수동 생성 폴더 확인 ( 부서함 전자결재 DWY, 프로젝트 완료함 분류폴더 PCL, 연구과제 완료함 분류폴더 RCL)
			String[] asLine 		= rid.split("_");
			if(asLine.length == 2)
			{		
				sCategoryId		= asLine[0];
				sCategory 		= asLine[1];
			}
			
			//======================================
			// 1. ROOT 	
			// 2. ROOT 1레벨 고정
			// 3. ROOT 1레벨 고정 > 2레벨 고정
			// 4. ROOT 1레벨 고정 > 2레벨 고정 > 3레벨 고정 
			//======================================
			
			// "TOP_ROOT_ID"
			if(rid.toUpperCase().equals("TOP_ROOT_ID"))
			{
				
				//======================================
				// 1. ROOT 
				//	고정메뉴
				// 		부서(문서함)
				// 		프로젝트/투자(문서함)
				// 		연구과제(문서함)
				//		공유/협업
				//		반출함
				//
				//	하위리스트조회
				// 		없음
				//======================================
				
				// DEPT		, edms_dept			부서(문서함)
				// PROJECT	, edms_project		프로젝트/투자(문서함)
				// RESEARCH	, edms_research		연구과제(문서함)
				// SHARE	, dm_share_root		공유/협업
				// TAKEOUT	, dm_takeout_root	반출함
				
				// 1레벨 생성.
				lReturnFolderList.add(AgentFolder.toMap(AgentFolder.builder().order_code("1").rid("DEPT"	).text("부서함"			).r_folder_security("").r_link_type("0").folder_type("dept"		).r_object_type("dm_dept_root").build()));
				lReturnFolderList.add(AgentFolder.toMap(AgentFolder.builder().order_code("2").rid("PROJECT"	).text("프로젝트/투자"	).r_folder_security("").r_link_type("0").folder_type("project"	).r_object_type("dm_project_root").build()));
				
				// 동국제강만 연구과제 보임.
				if(vUser.getComOrgId().equals("DKS"))
				{
					lReturnFolderList.add(AgentFolder.toMap(AgentFolder.builder().order_code("3").rid("RESEARCH").text("연구과제"		).r_folder_security("").r_link_type("0").folder_type("research"	).r_object_type("dm_research_root").build()));
				}
				
				lReturnFolderList.add(AgentFolder.toMap(AgentFolder.builder().order_code("4").rid("SHARE"	).text("공유/협업"		).r_folder_security("").r_link_type("0").folder_type("share"	).r_object_type("dm_share_root").build()));
				lReturnFolderList.add(AgentFolder.toMap(AgentFolder.builder().order_code("5").rid("TAKEOUT"	).text("반출함"		).r_folder_security("").r_link_type("0").folder_type("takeout"	).r_object_type("dm_takeout_root").build()));
				
			}
			else if(rid.equals("DEPT"))
			{
				
				//======================================
				// 2. ROOT 1레벨 고정
				//	고정메뉴
				// 		부서 하위 		: 전자결재
				// 		프로젝트 하위 	: 주관, 참여
				// 		연구과제 하이 	: 주관, 참여
				//
				//	하위리스트조회
				// 		부서함내 	폴더 조회
				// 		공유협업내 	폴더	죄회
				//======================================
				
				// 부서함 			: DEPT 					-> DPC
				// 부서함 전자결개 		: APPROVAL 				-> DWF
				
				// 프로젝트 주관 		: PROJECTMAIN			-> POW
				// 프로젝트 주관 완료 	: PROJECTMAINCOMPLETE	-> PFN
				// 프로젝트 참여 		: PROJECTJOIN			-> PIN
				// 프로젝트 참여 완료	: PROJECTJOINCOMPLETE	-> PIF
				
				// 연구과제 주관 		: RESEARCHMAIN			-> ROW
				// 연구과제 주관 완료	: RESEARCHMAINCOMPLETE	-> RFN
				// 연구과제 참여 		: RESEARCHJOIN			-> RIN
				// 연구과제 참여 완료	: RESEARCHJOINCOMPLETE	-> PIF
				
				
				
				// 고정메뉴 추가
				lReturnFolderList.add(AgentFolder.toMap(AgentFolder.builder().order_code("1").rid("DWF").text("전자결재").r_folder_security("").r_link_type("1").folder_type("dept").r_object_type("dm_dept_root").build()));
				
				bIsSearchFolder 	= true;		// 폴더  검색
				bIsSearchCabinet 	= false;	// 문서함 검색
				
				
				// 폴더 추가
				String dataId 				= vUser.getOrgId();
				String hamType 				= "D";							// 이부분 있으면 문서함조회.
				String folderType			= null;							// 20211118 기존 "DFO"; 에서 수정함
				boolean withDoc 			= false;
				boolean isMobile 			= false;
				boolean checkHasChildren	= false;					// 20211118 기존 true; 에서 수정함
				
				// 부서 하위 폴더 조회.
				List<DataDetailDto> result = dataService.getDataChildren(userSession, dataId, hamType, folderType, null, withDoc, checkHasChildren, isMobile);
				
				
				int iRow = 11;	// 일반 폴서 순서는 ( 고정 관여 안하게 11부터 시작 ) 
				for (DataDetailDto folderParent : result) {
					
					FolderDetailDto folder = folderParent.getFolder();
					
					Map<String, Object> mChildMap = new HashMap<String, Object>();
					
					mChildMap.put("order_code"			, Integer.toString(++iRow));  					// 순번.
					mChildMap.put("rid"					, folder.getRObjectId()+"_"+rid+"_FOLDER");  	// GATEGORY 정보 추가함.
					mChildMap.put("text"				, folder.getUFolName());
					mChildMap.put("r_folder_security"	, folder.getUSecLevel());
					mChildMap.put("r_link_type"			, "1");
					mChildMap.put("folder_type"			, folder.getUFolType()); 						// 폴더 타입 user : 개인문서함, dept : 부서문서함 ....
					mChildMap.put("r_object_type"		, folder.getUFolType());						// dm_user_root : 개인업무함 dm_dept_root : 부서업무함, dm_group_root : 공용업무함...
					
					lReturnFolderList.add(mChildMap);
				}
				
			}
			else if(rid.equals("PROJECT"))
			{
				lReturnFolderList.add(AgentFolder.toMap(AgentFolder.builder().order_code("1").rid("POW").text("주관").r_folder_security("").r_link_type("1").folder_type("project").r_object_type("dm_project_root").build()));
				lReturnFolderList.add(AgentFolder.toMap(AgentFolder.builder().order_code("2").rid("PIN").text("참여").r_folder_security("").r_link_type("1").folder_type("project").r_object_type("dm_project_root").build()));
			}
			else if(rid.equals("RESEARCH"))
			{
				lReturnFolderList.add(AgentFolder.toMap(AgentFolder.builder().order_code("1").rid("ROW").text("주관").r_folder_security("").r_link_type("1").folder_type("research").r_object_type("dm_research_root").build()));
				lReturnFolderList.add(AgentFolder.toMap(AgentFolder.builder().order_code("2").rid("RIN").text("참여").r_folder_security("").r_link_type("1").folder_type("research").r_object_type("dm_research_root").build()));
			}
			else if(rid.equals("SHARE"))
			{
				// http://dbox-test.dongkuk.com/api/share-folder

				List<DataDetailDto> list = folderService.selectShareFolderList(FolderFilterDto.builder().uFolName(null).build(), user_id);
				
				int iRow = 11;	// 일반 폴서 순서는 ( 고정 관여 안하게 11부터 시작 ) 
				for (DataDetailDto folderParent : list) {
					
					FolderDetailDto folder = folderParent.getFolder();
					
					Map<String, Object> mChildMap = new HashMap<String, Object>();
					
					mChildMap.put("order_code"			, Integer.toString(++iRow));  					// 순번.
					mChildMap.put("rid"					, folder.getRObjectId()+"_"+rid+"_FOLDER");  	// GATEGORY 정보 추가함.
					mChildMap.put("text"				, folder.getUFolName());
					mChildMap.put("r_folder_security"	, folder.getUSecLevel());
					mChildMap.put("r_link_type"			, "1");
					mChildMap.put("folder_type"			, folder.getUFolType()); 						// 폴더 타입 user : 개인문서함, dept : 부서문서함 ....
					mChildMap.put("r_object_type"		, folder.getUFolType());						// dm_user_root : 개인업무함 dm_dept_root : 부서업무함, dm_group_root : 공용업무함...
					
					lReturnFolderList.add(mChildMap);
				}
				
			}
			else if(rid.equals("TAKEOUT"))
			{
				// 반출함은 폴더 없음.
			}
			else if( rid.equals("DWF")) 
			{
				// 부서함 -> 전자결재
				// 폴더 조회 DWY
				// 폴더 추가
				String dataId 				= vUser.getOrgId();
				String hamType 				= "D";							// 이부분 있으면 문서함조회.
				String folderType			= "DWY";							// 20211118 기존 "DFO"; 에서 수정함
				boolean withDoc 			= false;
				boolean isMobile 			= false;
				boolean checkHasChildren	= false;					// 20211118 기존 true; 에서 수정함
				
				// 부서 하위 폴더 조회.
				List<DataDetailDto> result = dataService.getDataChildren(userSession, dataId, hamType, folderType, null, withDoc, checkHasChildren, isMobile);
				
				
				int iRow = 11;	// 일반 폴서 순서는 ( 고정 관여 안하게 11부터 시작 ) 
				for (DataDetailDto folderParent : result) {
					
					FolderDetailDto folder = folderParent.getFolder();
					
					Map<String, Object> mChildMap = new HashMap<String, Object>();
					
					mChildMap.put("order_code"			, Integer.toString(++iRow));  					// 순번.
					mChildMap.put("rid"					, folder.getRObjectId()+"_"+rid+"_FOLDER");  	// GATEGORY 정보 추가함.
					mChildMap.put("text"				, folder.getUFolName());
					mChildMap.put("r_folder_security"	, folder.getUSecLevel());
					mChildMap.put("r_link_type"			, "1");
					mChildMap.put("folder_type"			, folder.getUFolType()); 						// 폴더 타입 user : 개인문서함, dept : 부서문서함 ....
					mChildMap.put("r_object_type"		, folder.getUFolType());						// dm_user_root : 개인업무함 dm_dept_root : 부서업무함, dm_group_root : 공용업무함...
					
					lReturnFolderList.add(mChildMap);
				}
				
				
				
			}
			else if(sCategory.equals("DWY")	) 
			{
				
				//===============================================
				// 이부분 호출 안함
				// DWY 부분 부터는 폴더로 간주함.   ( sCategory는 2자리 일경우만 실행 )
				// 
				// 		DWF 에서 실행 하면 DWY 는 3자리로 호출함  
				// 		반출함 하위 년도 폴더 에는 하위 폴더 없음.
				//  	"folder_type":"DWY","r_object_type":"DWY","r_folder_security":"T","r_link_type":"1","text":"2021","rid":"0003383f80027ea3_DWF_FOLDER"}]
				// 
				// 구조 :: 
				// 	전자결재
				//		2022
				//			temp	--> DWT 로 사용 하고 있음  ::{~"folder_type":"DWT","r_object_type":"DWT","r_folder_security":"T","r_link_type":"1","text":"temp","rid":"0003383f8002713f_DWF_FOLDER"}
				//		2023
				//
				//	DWT 하위에 폴더는 없기 때문에 DWF 그대로 사용함 ( 0003383f8002713f_DWF_FOLDER ) 
				//===============================================
				
			}
			else if(rid.equals("POW") || rid.equals("PIN") || rid.equals("ROW") || rid.equals("RIN") ) 
			{
				
				//======================================
				// 3. ROOT 1레벨 고정 > 2레벨 고정
				//
				//	프로젝트, 연구과제 진행 문서함 조회
				//	
				//	고정메뉴
				//		프로젝트 투자 주관	: 완료함
				//		프로젝트 참여 참여	: 완료함
				//		연구과제     주관	: 완료함
				//		연구과제     참여	: 완료함
				//
				//	하위리스트조회
				// 		전자결재			: 년도폴더
				//		프로젝트 투자 주관	: 진행프로젝트
				//		프로젝트 참여 참여	: 진행프로젝트
				//		연구과제     주관	: 진행프로젝트
				//		연구과제     참여	: 진행프로젝트
				//======================================
				
				// 하위리스트 조회
				// TODO :: 하위 폴더 id 에 category 추가 할지 확인 필요함.  ==> 필요 할거 같음.. 나중 조회 때도 사용하자... 
				
				// @@부분 무조건 추가해야함..
				// 1. DWF	: 전자결재			-> 년도폴더 조회됨
				// 2. POW	: 프로젝트 투자 주관	-> @@완료함폴더(고정), 진행프로젝트 조회됨, 폴더 없음.  :: http://dbox-dev.dongkuk.com:19736/api/projects?ownJoin=O&uFinishYn=N
				// 3. PIN	: 프로젝트 참여 참여 -> @@완료함폴더(고정), 진행프로젝트 조회됨, 폴더 없음.	:: http://dbox-dev.dongkuk.com:19736/api/projects?ownJoin=J&uFinishYn=N
				// 4. ROW	: 연구과제     주관 -> @@완료함폴더(고정), 진행프로젝트 조회됨, 폴더 없음.	:: http://dbox-dev.dongkuk.com:19736/api/researchs?ownJoin=O&uFinishYn=N
				// 4. RIN	: 연구과제     참여 -> @@완료함폴더(고정), 진행프로젝트 조회됨, 폴더 없음.	:: http://dbox-dev.dongkuk.com:19736/api/researchs?ownJoin=J&uFinishYn=N
				
				
				
				String orgId 		= vUser.getOrgId();
				String sOwnJoin 	= "";
				String sFinishYn	= "N";
				
				// 고정 메뉴
				if(rid.equals("POW"))
				{
					lReturnFolderList.add(AgentFolder.toMap(AgentFolder.builder().order_code("1").rid("PFN").text("완료함").r_folder_security("").r_link_type("1").folder_type("project").r_object_type("dm_project_root").build()));
					sOwnJoin= "O";
				}
				else if(rid.equals("PIN"))
				{
					lReturnFolderList.add(AgentFolder.toMap(AgentFolder.builder().order_code("1").rid("PIF").text("완료함").r_folder_security("").r_link_type("1").folder_type("project").r_object_type("dm_project_root").build()));
					sOwnJoin= "J";
				}
				else if(rid.equals("ROW"))
				{
					lReturnFolderList.add(AgentFolder.toMap(AgentFolder.builder().order_code("1").rid("RFN").text("완료함").r_folder_security("").r_link_type("1").folder_type("research").r_object_type("dm_research_root").build()));
					sOwnJoin= "O";
				}
				else if(rid.equals("RIN"))
				{
					lReturnFolderList.add(AgentFolder.toMap(AgentFolder.builder().order_code("1").rid("RIF").text("완료함").r_folder_security("").r_link_type("1").folder_type("research").r_object_type("dm_research_root").build()));
					sOwnJoin= "J";
				}
				
				// 프로젝트, 연구과제 진행 문서함 조회
				if(rid.equals("POW") || rid.equals("PIN"))
				{
					// 프로젝트 진행 리스트
					
					ProjectFilterDto projectFilterDto = ProjectFilterDto.builder().ownJoin(sOwnJoin).uFinishYn(sFinishYn).rDeptCode(orgId).build();
					List<Project> result = projService.selectProjectList(projectFilterDto, orgId, user_id);
					
					
					
					int iRow = 11; 
					for (Project folder : result) {
						
						Map<String, Object> mChildMap = new HashMap<String, Object>();
						
						mChildMap.put("order_code"			, Integer.toString(++iRow));  	  // 순번.
						mChildMap.put("rid"					, folder.getUPjtCode()+"_"+rid);  // 키를 프로젝트코드로... GATEGORY 정보 추가함.
						mChildMap.put("text"				, folder.getUPjtName());
						
						mChildMap.put("r_folder_security"	, "");
						mChildMap.put("r_link_type"			, "1");
						mChildMap.put("folder_type"			, ""); 	// 폴더 타입 user : 개인문서함, dept : 부서문서함 ....
						mChildMap.put("r_object_type"		, "");	// dm_user_root : 개인업무함 dm_dept_root : 부서업무함, dm_group_root : 공용업무함...
						
						lReturnFolderList.add(mChildMap);
					}
					
				}
				else if(rid.equals("ROW") || rid.equals("RIN"))
				{
					
					// 연구과제 진행 리스트
					
					ResearchFilterDto researchFilterDto = ResearchFilterDto.builder().ownJoin(sOwnJoin).uFinishYn(sFinishYn).rDeptCode(orgId).build();
					List<Research> result = researchService.selectResearchList(researchFilterDto, orgId, user_id);
					
					int iRow = 11; 
					for (Research folder : result) {
						
						Map<String, Object> mChildMap = new HashMap<String, Object>();
						
						mChildMap.put("order_code"			, Integer.toString(++iRow));  			// 순번.
						//mChildMap.put("rid"					, folder.getRObjectId()+"_"+rid);  // GATEGORY 정보 추가함.
						mChildMap.put("rid"					, folder.getURschCode()+"_"+rid);  // GATEGORY 정보 추가함.
						mChildMap.put("text"				, folder.getURschName());
						
						mChildMap.put("r_folder_security"	, "");
						mChildMap.put("r_link_type"			, "1");
						mChildMap.put("folder_type"			, ""); 	// 폴더 타입 user : 개인문서함, dept : 부서문서함 ....
						mChildMap.put("r_object_type"		, "");	// dm_user_root : 개인업무함 dm_dept_root : 부서업무함, dm_group_root : 공용업무함...
						
						lReturnFolderList.add(mChildMap);
					}
					
				}
				
			}
			
			else if( rid.equals("PFN") || rid.equals("PIF") || rid.equals("RFN")|| rid.equals("RIF") 
					|| sCategory.equals("PCL") || sCategory.equals("RCL") ) 
			{
				
				
				//======================================
				// 4. ROOT 1레벨 고정 > 2레벨 고정 > 3레벨 고정
				//
				//	 - 프로젝트, 연구과제 완료함 하위 문서함, 폴더 조회
				//		주관일경우만 폴더 조회함.
				//
				//	 - 프로젝트, 연구과제 완료함 하위 폴더 에 하위 폴더 정보
				//
				//	고정메뉴
				//		없음
				//
				//
				//	uFinishYn = Y 가 다른 부분임.
				//  
				//	하위리스트조회
				//		프로젝트 투자 주관 완료함 : 진행프로젝트  :: http://dbox-dev.dongkuk.com:19736/api/projects?ownJoin=O&uFinishYn=Y
				//		프로젝트 참여 참여 완료함 : 진행프로젝트	:: http://dbox-dev.dongkuk.com:19736/api/projects?ownJoin=J&uFinishYn=Y
				//		연구과제     주관 완료함 : 진행프로젝트	:: http://dbox-dev.dongkuk.com:19736/api/researchs?ownJoin=J&uFinishYn=Y
				//		연구과제     참여 완료함 : 진행프로젝트	:: http://dbox-dev.dongkuk.com:19736/api/researchs?ownJoin=J&uFinishYn=Y
				//
				// 
				//		PFN : 주관 프로젝트/투자 완료함	// 수동으로 생성한 분류폴더 와 프로젝트 조회됨
				// 		PIF : 참여 프로젝트/투자 완료함	// 프로젝트만 조회됨
				// 		RFN : 주관 연구과제 완료함		// 수동으로 생성한 분류폴더 와 프로젝트 조회됨
				// 		RIF : 참여 연구과완료함			// 
				//======================================
				String orgId 		= vUser.getOrgId();
				String sOwnJoin 	= "";
				String sFinishYn	= "Y";
				
				
				String dataId	    = vUser.getOrgId();
				String hamType 		= null;		// 이부분 있으면 문서함조회.
				String folderType	= "";
				boolean withDoc 	= false;
				boolean isMobile 	= false;
				
				String sAppendCategory = "";
				
				// 고정 메뉴
				if(rid.equals("PFN"))
				{
					sOwnJoin		= "O";				// O : 주관, J : 참여
					hamType			= "D";				// D : ROOT, null : ROOT 아닌 폴더만 조회 필요 
					folderType 		= "PCL";			// 각 메뉴
					sAppendCategory = "_" + folderType;	// 하위 폴더 메뉴
					
				}
				else if(rid.equals("PIF"))
				{
					sOwnJoin= "J";
				}
				else if(rid.equals("RFN"))
				{
					sOwnJoin		= "O";
					hamType			= "D";
					folderType 		= "RCL";
					sAppendCategory = "_" + folderType;
				}
				else if(rid.equals("RIF"))
				{
					sOwnJoin		= "J";
				}
				else if(sCategory.equals("PCL"))
				{
					// 프로젝트 완료함 분류 폴더 하위 폴더
					sOwnJoin		= "O";
					hamType			= null; 
					folderType 		= "PCL";
					sAppendCategory = "_" + folderType;
					
					dataId			= sCategoryId;			// 하위는 현재 속성과 같음
				}
				else if(sCategory.equals("RCL"))
				{
					sOwnJoin		= "O";	// 주관, 참여
					hamType			= null;	// 하위 폴더임 
					folderType 		= "RCL";
					sAppendCategory = "_" + folderType;
					
					dataId			= sCategoryId;			// 하위는 현재 속성과 같음
				}
				
				
				int iRow = 11;	// 일반 폴더 순서는 ( 고정 관여 안하게 11부터 시작 ) 
				
				// 완료함 하위 분류폴더 폴더 조회
				if(rid.equals("PFN") || rid.equals("RFN") || sCategory.equals("PCL") || sCategory.equals("RCL"))
				{
					 
					boolean checkHasChildren	= true;
					List<DataDetailDto> result = dataService.getDataChildren(userSession, dataId, hamType, folderType, null, withDoc, checkHasChildren, isMobile);
					
					
					for (DataDetailDto folderParent : result) {
						
						FolderDetailDto folder = folderParent.getFolder();
						
						Map<String, Object> mChildMap = new HashMap<String, Object>();
						
						mChildMap.put("order_code"			, Integer.toString(++iRow));  				// 순번.
						mChildMap.put("rid"					, folder.getRObjectId() + sAppendCategory);	// ID, CATEGORY 포함.
						mChildMap.put("text"				, folder.getUFolName());
						mChildMap.put("r_folder_security"	, folder.getUSecLevel());
						mChildMap.put("r_link_type"			, "1");
						mChildMap.put("folder_type"			, folder.getUFolType()); 	// 폴더 타입 user : 개인문서함, dept : 부서문서함 ....
						mChildMap.put("r_object_type"		, folder.getUFolType());	// dm_user_root : 개인업무함 dm_dept_root : 부서업무함, dm_group_root : 공용업무함...
						
						lReturnFolderList.add(mChildMap);
						
					}
				}
				
				//==================================
				// 프로젝트 완료함 하위 문서함 조회
				// 연구과제 완료함 하위 문서함 조회
				// 
				// 추가로 프로젝트 완료함 분류 체계 폴더 내 검색시
				//==================================
				if(rid.equals("PFN") || rid.equals("PIF") || sCategory.equals("PCL"))
				{
					
					ProjectFilterDto projectFilterDto = ProjectFilterDto.builder().ownJoin(sOwnJoin).uFinishYn(sFinishYn).rDeptCode(orgId).build();
					
					// 프로젝트 완료함 분류 체계 폴더 추가
					if(sCategory.equals("PCL"))
					{
						projectFilterDto.setUFolId(sCategoryId);
					}
					
					List<Project> result = projService.selectProjectList(projectFilterDto, orgId, user_id);
					
					for (Project folder : result) {
						
						Map<String, Object> mChildMap = new HashMap<String, Object>();
						
						mChildMap.put("order_code"			, Integer.toString(++iRow));  		// 순번.
						mChildMap.put("rid"					, folder.getUPjtCode()+"_"+rid);  	// 키를 프로젝트코드로... GATEGORY 정보 추가함.
						mChildMap.put("text"				, folder.getUPjtName());
						
						mChildMap.put("r_folder_security"	, "");
						mChildMap.put("r_link_type"			, "1");
						mChildMap.put("folder_type"			, ""); 	// 폴더 타입 user : 개인문서함, dept : 부서문서함 ....
						mChildMap.put("r_object_type"		, "");	// dm_user_root : 개인업무함 dm_dept_root : 부서업무함, dm_group_root : 공용업무함...
						
						lReturnFolderList.add(mChildMap);
					}
					
				}
				else if(rid.equals("RFN") || rid.equals("RIF") || sCategory.equals("RCL"))
				{
					
					ResearchFilterDto researchFilterDto = ResearchFilterDto.builder().ownJoin(sOwnJoin).uFinishYn(sFinishYn).rDeptCode(orgId).build();
					
					// 프로젝트 완료함 분류 체계 폴더 추가
					if(sCategory.equals("RCL"))
					{
						researchFilterDto.setUFolId(sCategoryId);
					}
					
					List<Research> result = researchService.selectResearchList(researchFilterDto, orgId, user_id);
					
					//int iRow = 11; 
					for (Research folder : result) {
						
						Map<String, Object> mChildMap = new HashMap<String, Object>();
						
						mChildMap.put("order_code"			, Integer.toString(++iRow));  			// 순번.
						//mChildMap.put("rid"					, folder.getRObjectId()+"_"+rid);  // GATEGORY 정보 추가함.
						mChildMap.put("rid"					, folder.getURschCode()+"_"+rid);  // GATEGORY 정보 추가함.
						mChildMap.put("text"				, folder.getURschName());
						
						mChildMap.put("r_folder_security"	, "");
						mChildMap.put("r_link_type"			, "1");
						mChildMap.put("folder_type"			, ""); 	// 폴더 타입 user : 개인문서함, dept : 부서문서함 ....
						mChildMap.put("r_object_type"		, "");	// dm_user_root : 개인업무함 dm_dept_root : 부서업무함, dm_group_root : 공용업무함...
						
						lReturnFolderList.add(mChildMap);
					}
					
				}
				
			}
			else
			{
				//===============================================================
				
				// 1. 문서함 내 폴더 선택했을경우 실행됨			: 000004d2800040ca_DEPT_FOLDER    :: 부서함은 윗부분 DEPT 에서 처리함
				// 2. 프로젝트문서함 -> 주관 -> 프로젝트 선택시  	: p00002_POW
				// 3. 프로젝트문서함 -> 주관 -> 프로젝트 -> 폴더  : p00002_POW_FOLDER
				// 4. 프로젝트문서함 -> 주관 -> 완료함 -> 문서함 :  p00002_PFN
				//
				// 고정 메뉴 이외의 정보 조회
				//
				//	프로젝트 또는 완료함 하위 분류 폴더 일경우 : 2자리 -> 다음 폴더도 2자리 로 처리
				//	프로젝트 또는 완료함 하위 분류 폴더 일경우 : 2자리 -> 문서함 하위는 3자리로 처리함 
				//===================================================
				// 사용안함
				//  - (사용안함) 문서함 선택 했을경우.			: 000004d2800040ca_DEPT :: 여기서 사용안함
				//	- (사용안함) 프로젝트완료함 하위, 연구과제 완료함 하위는 다른 타입에서 조회 해야 해서 여기서 사용안함
				
				String hamType 		= null;		// 이부분 있으면 문서함조회.  Root 가 됨..
				String folderType	= "DFO";
				boolean withDoc 	= false;
				boolean isMobile 	= false;
				

				String  dataId			= rid;
				String 	sAppendCategory = "";

				if(asLine.length == 2)
				{
					// 문서함
					hamType 		= "D";							// 이부분 있으면 문서함조회.
					
					// 연구과제 분리함.
					if(sCategory.equals("ROW") || sCategory.equals("RIN")
							|| sCategory.equals("RFN") || sCategory.equals("RIF") || sCategory.equals("RCL"))
					{
						hamType 	= "R";
						folderType = null;
					}
					
					dataId 			= asLine[0];
					sCategory 		= asLine[1];
					sAppendCategory = "_" + sCategory + "_FOLDER";	// 하위는 폴더임.
					
				}
				if(asLine.length == 3)
				{
					// 폴더
					dataId 			= asLine[0];
					sCategory 		= asLine[1];
					sAppendCategory = "_" + sCategory + "_FOLDER";
				}
				
				boolean checkHasChildren	= true;
				List<DataDetailDto> result = dataService.getDataChildren(userSession, dataId, hamType, folderType, null, withDoc, checkHasChildren, isMobile);
					
				int iRow = 11;	// 일반 폴더 순서는 ( 고정 관여 안하게 11부터 시작 ) 
				for (DataDetailDto folderParent : result) {
					
					FolderDetailDto folder = folderParent.getFolder();
					
					Map<String, Object> mChildMap = new HashMap<String, Object>();
					
					mChildMap.put("order_code"			, Integer.toString(++iRow));  				// 순번.
					mChildMap.put("rid"					, folder.getRObjectId() + sAppendCategory);	// ID, CATEGORY 포함.
					mChildMap.put("text"				, folder.getUFolName());
					mChildMap.put("r_folder_security"	, folder.getUSecLevel());
					mChildMap.put("r_link_type"			, "1");
					mChildMap.put("folder_type"			, folder.getUFolType()); 	// 폴더 타입 user : 개인문서함, dept : 부서문서함 ....
					mChildMap.put("r_object_type"		, folder.getUFolType());	// dm_user_root : 개인업무함 dm_dept_root : 부서업무함, dm_group_root : 공용업무함...
					
					lReturnFolderList.add(mChildMap);
					
				}
				
				// 리턴할 자료 리스트에 추가
				//dataList.addAll(folderDetailDtoList.stream()
				//		.map(item -> DataDetailDto.builder().dataType(DboxObjectType.FOLDER.getValue()).folder(item).build())
				//		.collect(Collectors.toList()));
				
				
			}

			sReturnCode = "0";
			sReturnMsg 	= "SUCCESS";
			
		} catch (Exception e) {

			sReturnCode = "-1";
			sReturnMsg	= "폴더 정보 조회에 실패 하였습니다.";
			
			log.info("[callFolderList] ERROR :: " + user_id);
			log.info("[callFolderList] ERROR :: " + e.toString() + e.getMessage());
			

		} finally {

		}

		// Agent 리턴 정보.
		rtnJsonObject.put("list"		, lReturnFolderList);
		rtnJsonObject.put("totalCount"	, lReturnFolderList.size());
		rtnJsonObject.put("return_code"	, sReturnCode);	
		rtnJsonObject.put("return_msg"	, sReturnMsg);
		
		log.info("[callFolderList] END : " + sReturnCode);
		log.info("[callFolderList] END : " + sReturnMsg);
		// log.info("[callFolderList] END : " + rtnJsonObject.toString());
		
		return rtnJsonObject;
	}

	@Override
	public JSONObject callFileList(String user_id, String r_folder_id, String _pageNumber, String _pageLineCount)
			throws Exception {
		
		log.info("[callFileList] START : " + user_id);
		log.info("[callFileList] START : " + r_folder_id);
		
		//==============================================
		// 문서 리스트
		//==============================================
		// 호출.
		//		- 메뉴, 문서함, 폴더 리스트 조회
		//		- rid : 호출된 Key 값  최초 : ROOT
		// Return
		//		- list
		//			rid					: 폴더 코드
		//			text				: 폴더 이름
		//			r_folder_security	: 폴더 보안 등급
		//			r_link_type			: 0 : root 폴더, 1 : root 폴더 외
		//			folder_type			: 폴더 타입 user : 개인문서함, dept : 부서문서함, project : 공용문서함, CommonBox : 공통문서함, trash : 휴지통
		//			r_object_type		: dm_user_root : 개인업무함, dm_dept_root : 부서업무함, dm_group_root : 공용업무함, dm_common_root : 공통업무함, dm_folder : 폴더, dm_trash : 휴지통
		//		- totalCount
		// 		- return_code
		//		- return_msg
		//==============================================
		
		// 리턴 기본정보
		JSONObject rtnJsonObject	= new JSONObject();	
		String	sReturnCode			= "0";					// RETURN CODE
		String	sReturnMsg			= "SUCCESS";			// RETURN MESSAGE

		//==============================================
		// 메뉴 변경
		// dept_doc			: DEPT		--> DPC
		// public_doc		: PROJECT	--> PJT
		// common_doc 		: SHARE		--> SHR
		// customize_doc 	: RESEARCH	--> RSC
		// customize_doc2 	: TAKEOUT	--> EXP
		//==============================================
		if(r_folder_id.equals("DEPT") || r_folder_id.equals("dept_doc")){
			r_folder_id= "DPC";
		}
		else if(r_folder_id.equals("PROJECT") || r_folder_id.equals("public_doc")){
			r_folder_id= "PJT";
		}
		else if(r_folder_id.equals("SHARE") || r_folder_id.equals("common_doc") || r_folder_id.equals("personal_doc") ){
			r_folder_id= "SHR";
		}
		else if(r_folder_id.equals("RESEARCH") || r_folder_id.equals("customize_doc")){
			r_folder_id= "RSC";
		}
		else if(r_folder_id.equals("TAKEOUT") || r_folder_id.equals("customize_doc2")){
			r_folder_id= "EXP";
		}

		boolean isRoot 				= false;
		boolean bIsSearchDoc	 	= true;	// 문서 조회 여부 확인
		
		// 리턴 문서
		List<Map<String, Object>> lReturnDocList	= new ArrayList<Map<String,Object>>();
		
		String sSelFolderId		= r_folder_id; // 복호화를 위함 반출함 메뉴 확인용.
		String sSelCategory 	= "";
		
		try 
		{

			// 사용자 정보 조회
			VUser vUser = userSevice.selectOneByUserId( user_id ).orElseThrow(() -> new NotFoundException(VUser.class, "NO USER"));
			String  dataId			= "";
			
			// 1. 고정 메뉴  ( 하위에도 고정 메뉴만 나타남 )
			// 2. 고정 메뉴중 하위에 문서도 나타나는 경우 ( 하위에 폴더 도 나타날수 있음 )
			// 3. 일반 폴더 ( DB 에서 조회된 폴더 : 하위에 문서만 나타남 )
			// 4. 공유 협업 하위 에 문서 없음 폴더만 있음
			if(r_folder_id.toUpperCase().equals("TOP_ROOT_ID")
					 || r_folder_id.equals("PJT")	|| r_folder_id.equals("RSC") 
					 || r_folder_id.equals("POW")	|| r_folder_id.equals("PIN") 
					 || r_folder_id.equals("ROW")	|| r_folder_id.equals("RIN") 
					 || r_folder_id.equals("PFN") 	|| r_folder_id.equals("PIF") 
					 || r_folder_id.equals("RFN")	|| r_folder_id.equals("PIF")
					 || r_folder_id.equals("SHR") )
			{
	
				// 1. 고정 메뉴  ( 하위에도 고정 메뉴만 나타남 )
				// "문서함만 있음");
				bIsSearchDoc = false;
				
			}
			else if( r_folder_id.equals("DPC") )
			{
				
				// 2. 고정 메뉴중 하위에 문서도 나타나는 경우 ( 하위에 폴더 도 나타날수 있음 )
				
				// TODO :: DEPT 문서함 내 폴더 없이 결재링크 파일 나타남.   링크 파일도 첨부가 가능 한지는 확인 필요함
				// TODO :: DWF 전자결재에 문서 리스트 확인필요함
				// TODO :: 조회 하는 방식 확인 필요함.

				bIsSearchDoc 	= true;
				isRoot		 	= true;
				dataId 			= vUser.getOrgId();
				
			}
			else if( r_folder_id.equals("EXP") )
			{
				// 반출함.
				bIsSearchDoc = false;
				
				ReqTakeout takeout = ReqTakeout.builder().uReqUser(user_id).build();
				List<ReqTakeoutDetailDto> listTakeout = takeoutReqService.takeoutDetailList(takeout);
				
				// FOLDER_ID, CATEGORY 는 모두 EXP 로 사용함.
				// edms_req_takeout, edms_req_takeout_doc
				// html 생성시 u_doc_key 와 edms_req_takeout 의 r_object_id 정보 로 생성
				// 이후 URL 보기시 edms_req_takeout_doc 에서 u_req_doc_id 과 u_req_doc_key 를 이용 해서 -> u_req_id 를 찾는 방법.
				
				// List<DocDetailDto> to MAP 형식으로 변경.
				lReturnDocList.addAll(listTakeout.stream()
						.filter(item -> StringUtils.isNotBlank(item.getRObjectId()) &&  StringUtils.isNotBlank(item.getUReqDocKey()) )
						.map(item -> 
									AgentDoc.toMap(AgentDoc.builder()
											.r_object_id(item.getUReqDocId() + "_" + item.getUReqDocKey() + "_" + item.getRObjectId()+ "_EXP_EXP")
											.i_chronicle_id(item.getUReqDocKey())
											.object_name(item.getUReqDocTitle())
											.dos_ext(item.getUReqDocExt())
											.r_content_size(item.getUDocSize())
											.creator_emp_nm(item.getDocEditorName() != null ? item.getDocEditorName() : " " )
											.dept_name(item.getDocOrgNm().replaceAll("/", "-"))
											.r_version_label( item.getUVersionLabel().contains(".") ? item.getUVersionLabel().substring(0, item.getUVersionLabel().indexOf(".")) : item.getUVersionLabel() )
											.r_modify_date(item.getULimitDate() != null 	? item.getULimitDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : "")
											.r_secu_level(item.getUSecLevel().equals("S") 	? "제한" : item.getUSecLevel().equals("T") ? "팀내" : item.getUSecLevel().equals("C") ? "사내" : "그룹사내" )	// 보안등급
											.permit_name(item.getUApprType().equals("P") 	? "사전승인" : item.getUSecLevel().equals("A") ? "자동승인" : "프리패스")
											.online_status("")
										.build()))
						.collect(Collectors.toList()));
				
			}
			
			else if( r_folder_id.equals("DWF"))
			{
				// 전자결재 하위는 년도만 있고 문서 없음.
				bIsSearchDoc 	= false;
			}
			else if(r_folder_id.equals("DWY") )
			{
				
				// ## 이부분 호출 안함 => DWF 로 사용하고 folder_id 를 그대로 사용함.
				//		년도 폴더 : [0000000000000001_DWF_FOLDER]
				//		형식 :   folder.getRObjectId()+"_"+rid+"_FOLDER"
				//		타입 : DWY ( 타입 사용안함 )
				//				
				// 문서함 전자 결재
				
				// 2. 고정 메뉴중 하위에 문서도 나타나는 경우 ( 하위에 폴더 도 나타날수 있음 )
				
				// :: DEPT 문서함 내 폴더 없이 결재링크 파일 나타남.   링크 파일도 첨부가 가능 한지는 확인 필요함
				// :: DWF 전자결재에 문서 리스트 확인필요함
				// :: 조회 하는 방식 확인 필요함.
				// :: DWY 로 처리 필요함.
				// TEST 문서 삭제 필요
				
				// 사용안함.
				bIsSearchDoc 	= true;
				isRoot		 	= true;
				dataId			= vUser.getOrgId();
				sSelFolderId	= r_folder_id;
				
			}
			else
			{
				
				// p00021_POW
				
				// 폴더 ID 와 Category 분리.
				String[] saFolderInfo 	= r_folder_id.split("_");
				dataId			= saFolderInfo[0];
				
				
				sSelFolderId	= dataId;
				sSelCategory 	= saFolderInfo.length > 1 ? saFolderInfo[1] : "";
				
				
				// 문서함ID_메뉴  일경우  부서및 문서함 1레벨 문서 조회
				if(saFolderInfo.length == 2)
				{
					// 문서함
					isRoot 		= true;
				}
				
				
			}
				
			
			// 문서 검색 메뉴 일경우 검색
			if(bIsSearchDoc)
			{
				//======================================================
				// 3. 일반 폴더 ( DB 에서 조회된 폴더 : 하위에 문서만 나타남 )
				// 폴더 정보 조회 순간 부터 r_object_id 에 필요한 정보를 담음 : 부서문서함, 프로젝트 문서함 연구과제 문서함,    폴더 구분 어떻게 할건지 확인 필요함....
				// 검색 필터 설정
				String dataIdTemp = dataId;
				
		        DocFilterDto docFilterDto = null;
		        if (isRoot) {
		        	// 프로젝트/투자, 연구과제, 부서 여부 확인
		        	HamInfoResult hamInfo = commonAuthDao.selectHamInfo(dataId).orElseThrow(() -> new BadRequestException("존재하지 않는 dataId 입니다. (dataId: " + dataIdTemp + ")"));
		        	HamType type = HamType.findByValue(hamInfo.getHamType());
		        	switch (type) {
		        		// 부서함일 경우
		        		case DEPT:
		        		case COMPANY:
		        		case COMPANY_M:
		              VDept dept = gwDeptDao.selectOneByOrgId(dataId).orElseThrow(() -> new BadRequestException("존재하지 않는 부서코드입니다."));
		              docFilterDto = DocFilterDto.builder()
		              		.uCabinetCode(dept.getUCabinetCode())
		              		.uFolId(DCTMConstants.DCTM_BLANK)
		              		.uPrCode(DCTMConstants.DCTM_BLANK)
		              		.build();
		        			break;
		        		// 프로젝트/투자일 경우
		        		case PROJECT:
		        			Project project = projectService.selectProjectByUPjtCode(dataId).orElseThrow(() -> new BadRequestException("존재하지 않는 프로젝트 코드입니다."));
		              docFilterDto = DocFilterDto.builder()
		              		.uCabinetCode(project.getUCabinetCode())
		              		.uFolId(DCTMConstants.DCTM_BLANK)
		              		.uPrCode(dataId)
		              		.uPrType(ProjectType.PROJECT.getValue())
		              		.build();
		        			break;
		        		// 연구과제일 경우
		        		case RESEARCH:
		        			Research research = researchService.selectResearchByURschCode(dataId).orElseThrow(() -> new BadRequestException("존재하지 않는 연구과제 코드입니다."));
		              docFilterDto = DocFilterDto.builder()
		              		.uCabinetCode(research.getUCabinetCode())
		              		.uFolId(DCTMConstants.DCTM_BLANK)
		              		.uPrCode(dataId)
		              		.uPrType(ProjectType.RESEARCH.getValue())
		              		.build();
		        			break;
		      			default:
		        	}
		        } else {
		          docFilterDto = DocFilterDto.builder().uFolId(dataId).build();
		        }
				
				
				// 일반 dbox 는 검색(2) 이상의 권한을 가진 문서 리스트 조회
		        // 첨부창은 3이상의 권한을 가진 문서 리스트 조회 ( 2021-12-02)
		        List<DocDetailDto> docDetailDtoList = docService.selectAuthorizedDetailList(docFilterDto, user_id, GrantedLevels.READ.getLevel());
				
		        
		        String sAppendFolderId 	= sSelFolderId;
		        String sAppendCategory	= sSelCategory;
		        String sAppendApproveId	= "";
		        
		        //======================================================
		        // 조건 검색
		        //======================================================
		        
//				String sAttachDocStatusTemp = "A"; 	// L:Live, C:Closed, A 전체.
//				String sAttachLimitSecTemp	= "S";	// S:제한,T:팀내,C사내:,G:그룹사내
//				
//				// 테스트 syspath = "nmail.dongkuk.com";
//				
//				if(syspath != null && !syspath.equals(""))
//				{
//					Optional<AttachPolicy> opAttachPolicy = attachPolicyDao.selectOneBySystemKey(syspath);
//					if(opAttachPolicy.isPresent())
//					{
//						sAttachDocStatusTemp 		= opAttachPolicy.get().getUDocStatus();		// L:Live, C:Closed, A 전체.
//						sAttachLimitSecTemp  		= opAttachPolicy.get().getULimitSecLevel();	// S:제한,T:팀내,C사내:,G:그룹사내		   
//					}
//					
//				}
//				
//				String sAttachDocStatus 	= sAttachDocStatusTemp;		// L:Live, C:Closed, A 전체.
//				String sAttachLimitSec  	= sAttachLimitSecTemp;
//				
				
				//======================================================
		        
		        // 1	creator_emp_nm	소유자	키값으로, 이 값이 비어있으면 나머지 속성 표시 안됨
		        // 2	object_name	이름	
		        // 3	r_version_label	버전	
		        // 4	r_modify_date	수정일	
		        // 5	r_secu_level	보안등급	
		        // 6	permit_name		권한			--> Dbox 에서 사용하는 부분 없어서 상태 체크로 사용함 Live, Closed
		        // 7	r_lock_owner	잠금자	
		        // 8	r_content_size	크기	
		        // 9	r_lock_type	"0 : 잠금 아님 1 : 잠금 상태"	파일이 잠겨있을 경우 아이콘에 잠김 표시 생김
		        // 10   online_status  : 5 : 첨부불가  (  제한 및 개인정보포함여부 일경우 5 를 리턴 하여   첨부 안되게함 ) ==> 2022.02.09 제한 문서도 첨부 가능 하게 하면서 사용안함.
		        //			이전 정보 : // .online_status(  ( item.getUSecLevel().equals("S") && item.getUPrivacyFlag() ) ? "13" : item.getUSecLevel().equals("S") ? "9" : item.getUPrivacyFlag() ? "5" : "" )
		        
				// List<DocDetailDto> to MAP 형식으로 변경.
				lReturnDocList.addAll(docDetailDtoList.stream()
//							.filter(item -> { if(sAttachDocStatus.equals("A") ) return true; 
//												else if(sAttachDocStatus.equals(item.getUDocStatus())) return true; 
//												else return false; })
//							.filter(item -> { if(sAttachLimitSec.equals("S")) return true; 
//													else if(sAttachLimitSec.equals("T") && item.getUSecLevel().equals("T") ) return true;
//													else if(sAttachLimitSec.equals("T") && item.getUSecLevel().equals("C") ) return true;
//													else if(sAttachLimitSec.equals("T") && item.getUSecLevel().equals("G") ) return true;
//													else if(sAttachLimitSec.equals("C") && item.getUSecLevel().equals("C") ) return true;
//													else if(sAttachLimitSec.equals("C") && item.getUSecLevel().equals("G") ) return true;
//													else if(sAttachLimitSec.equals("G") && item.getUSecLevel().equals("G") ) return true;
//													else return false; })
							.map(item -> 
										AgentDoc.toMap(AgentDoc.builder()
												.r_object_id(item.getRObjectId() + "_" + item.getUDocKey() + "_" + sAppendApproveId + "_" + sAppendFolderId + "_" + sAppendCategory)
												.i_chronicle_id(item.getUDocKey())
												.creator_emp_nm(item.getRegUserDetail() != null ? item.getRegUserDetail().getDisplayName() : " "  )
												.dept_name(item.getOwnDeptDetail().getOrgNm().replaceAll("/", "-"))
												.object_name(item.getTitle().replaceAll("/", "-"))
												.r_version_label( item.getLastVersion().contains(".") ? item.getLastVersion().substring(0, item.getLastVersion().indexOf(".")) : item.getLastVersion() )
												.r_modify_date(item.getUUpdateDate() != null ? item.getUUpdateDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : "")
												.r_secu_level(item.getSecLevelName())	// 보안등급
												.permit_name( item.getUDocStatus().equals("L") ? "Live" : "Closed")
												.r_lock_owner_name(item.getUEditorNames())
												.r_content_size(item.getRContentSize())
												.r_lock_type(StringUtils.isBlank(item.getUEditorNames()) ? "0" : "1")
												.online_status("")
											.build()))
							.collect(Collectors.toList()));
			}
			
			// Return
			sReturnCode 		= "0";			// 정상처리
			sReturnMsg			= "SUCCESS";	// MSG
			
		} catch (Exception e) {

			sReturnCode = "-1";
			sReturnMsg	= "파일 정보 조회에 실패 하였습니다.";
			
			log.info("[callFileList] ERROR :: " + user_id);
			log.info("[callFileList] ERROR :: " + e.toString() + e.getMessage());
			
		} finally {

		}
		
		// Agent 리턴 정보.
		rtnJsonObject.put("list"		, lReturnDocList);
		rtnJsonObject.put("totalCount"	, lReturnDocList.size());	  
		rtnJsonObject.put("return_code"	, sReturnCode);	
		rtnJsonObject.put("return_msg"	, sReturnMsg);	
		
		log.info("[callFileList] END : " + sReturnCode);
		log.info("[callFileList] END : " + sReturnMsg);
		// log.info("[callFileList] END : " + rtnJsonObject.toString());
		
		return rtnJsonObject;
	}

	@Override
	public JSONObject isCheckOut(String user_id, String r_object_id) throws Exception {
		
		log.info("[isCheckOut] START : " + user_id);
		log.info("[isCheckOut] START : " + r_object_id);
		
		//==============================================
		// 문서 편집중 상태 확인
		//		( 보기, 편집시 호출됨 ) 
		//		isCheckOut -> checkCacheFile -> checkOut
		//==============================================
		// 호출.
		//		- WEB 에서 문서 편집 호출후 Agent 에서 문서 편집 상태 확인하는 부분
		//
		//	r_object_id 형식
		//		편집 : 090004d280006366
		//		보기 : 090004d280006366__testapp_1zK24M3d5Y9KbPY3YbWuZhl7jt6Fg%2F7Cl2vS06FX%2BTU%3D
		//
		// Return
		//		- check_out			: 0 : 일반, 1 : 편집중
		// 		- return_code
		//		- return_msg
		//
		//
		//	기타
		//		메일 자동권한 추가 시 권한이 없을수 있어서 DB 로 조회함 ( idfSession.getObject 하면 에러 남 )
		//==============================================
		
		JSONObject rtnJsonObject = new JSONObject();
		
		String 	sReturnCheckOut 	= "0";
		String	sReturnCode 		= "0";
		String	sReturnMsg			= "";

		try {
 			
			// 문서 ID
			if(r_object_id.length() > 16 )
			{
				r_object_id = r_object_id.substring(0, 16);
			}
			
			
			// 선택된 문서 정보
			// 최신 문서 ID 조회후 편집 중 상태 확인
	    	Optional<Doc> optDoc 		= docDao.selectDetailOne(r_object_id, user_id, false);	    	
			if (optDoc.isPresent()) 
			{
				
				Doc 	doc 	= optDoc.get();
				String 	sDocKey = doc.getUDocKey();
				
				// 최신문서 정보 조회 ( DOC_KEY 를 이용 )
				Optional<Doc> optDocCurrent = docDao.selectDetailOne(sDocKey, user_id, true);
				if (optDocCurrent.isPresent()) 
				{
					
					Doc 	docCurrent 	= optDocCurrent.get();
					
					if(StringUtils.isNotBlank(docCurrent.getRLockOwner()))
					{
						sReturnCheckOut = "1";
					}
				}
			}
			else
			{
				// 문서가 없을경우 중요문서함에서 한번더 확인
				// 중요문서함여부 확인
				// TODO :: 버전 정보 가 있을경우 추가 필요
				Optional<DocImp> optDocImp= docImpService.selectOne(r_object_id);
				if (optDocImp.isPresent()) 
				{
					
					DocImp 	docImp 	= optDocImp.get();
					if(StringUtils.isNotBlank(docImp.getRLockOwner()))
					{
						sReturnCheckOut = "1";
					}
				}
			}
			
			
			// Return
			sReturnCode 		= "0";			// 정상처리
			sReturnMsg			= "SUCCESS";	// MSG

		} catch (Exception e) {

			sReturnCode = "-1";
			sReturnMsg	= "편집중 체크에 실패 하였습니다.";

			log.info("[isCheckOut] ERROR :: " + user_id);
			log.info("[isCheckOut] ERROR :: " + e.toString() + e.getMessage());
			
		} finally {

		}

		// Agent 리턴 정보.
		rtnJsonObject.put("check_out"	, sReturnCheckOut);
		rtnJsonObject.put("return_code"	, sReturnCode);
		rtnJsonObject.put("return_msg"	, sReturnMsg);
		
		log.info("[isCheckOut] END : " + rtnJsonObject.toString());
		
		return rtnJsonObject;
	}

	@Override
	public JSONObject checkOut(String user_id, String r_object_id, String flag, String syspath, String ip) throws Exception {
		
		log.info("[checkOut] START : " + user_id);
		log.info("[checkOut] START : " + r_object_id);
		log.info("[checkOut] START : " + flag);
		log.info("[checkOut] START : " + syspath);
		log.info("[checkOut] START : " + ip);
		
		//==============================================
		// 파일 다운로드 ( 보기, 편집, 원문다운로드, 첨부 )
		//==============================================
		// 호출.
		//		- 다운로드 ( 편집 은 다른 Service 에서 처리됨 )
		// 			- 문서보기 시 다운로드( 최신보기일경우 UI 단에서 최신 ID 조회해서 던짐 ) : URL 문서 보기도 해당 ( APPROVEID 포함될수 있음.)
		//			- 문서편집 시 다운로드
		//			- 문서첨부 시 다운로드
		//	
		//		- user_id		: 사용자 ID	
		//		- r_object_id	: 문서 고유 ID	
		//							첨부시	:: r_object_id|doc_id|approveid|folderid|category 
		//							보기시 	:: r_object_id|doc_id|approveid|sysid
		//
		//							*** 추가로 파라미터 던져줄수 없다고 하여 r_object_id 에 정보를 구분자로 추가함.
		//							*** 첨부 파일 리스트 조회 할때 r_object_id|doc_id|approveid|folderid|category  으로 만들어서 리턴 필요함...
		//							*** 보기시는 websocket 호출 하는 부분에서 r_object_id 던질 필요 있음.
		//
		//
		//		- flag			: 1 : 링크, 2 : 원문, 3 : DRM, 4 : 읽기  --> view 일때만 4던짐... 
		//		- syspath		: URL or PROCESS
		//
		// 		첨부시 flag : 1,2,3 ==> flag 사용안하고 syspath 에서 DB 정책 조회 한후 사용함 ->  H, D ( 반출함은복호화 파일 )
		//		보기시 flag : 4
		//		편집시 syspath null, flag null
		//
		// Return
		//		1. file_path	: 다운로드 WAS 파일 경로		
		//		2. file_name	: 다운로드 WAS 파일 명
		// 		3. return_code
		//		4. return_msg
		//==============================================
		JSONObject rtnJsonObject 	= new JSONObject();
		String 	sReturnFilePath 	= "";
		String 	sReturnFileName 	= "";
		String	sReturnCode 		= "0";
		String	sReturnMsg			= "";

		// 기본 정보.
		String 	sViewEditAttach		= "VIEW"; 		// VIEW/EDIT/ATTACH		
		String	sAutoPermitSystemYn	= "N";			// 보기 자동권한 필요한지 여부.
		boolean bIsAutoPermitDoc 	= false;		// 현재 문서가 권한이 없고 ( 자동권한 부여해야 되는 시스템일경우 )
		
		boolean bIsViewDocId		= false;		// 반출함 용량큰 HTML
		boolean bIsViewApprove		= false;		// 반출함 용량큰 HTML
		boolean bIsViewSysId		= false;		// 반출함 용량큰 HTML
		
		String 	sViewDocId			= "";			// 문서 DOC_ID
		String  sViewApproveId		= "";			// 반출함 ApproveId	( 첨부파일 HTML 생성시 사용함 )
		String  sViewSysId			= "";			// 첨부 SYSID ( 문서 보기시 사용함 )
		
		 
		// 반출함일경우만 원문 첨부 가능함.
		// 사용안함.  ( 추후에 필요시 사용 하자 )
		boolean bIsExpFile			= false;	// 첨부 문서의 FOLDER_ID	사용유무		// 현재 사용안함 ( 추후에 필요시 사용 하자 )
		boolean bIsAttachFolderId	= false;	// 첨부 문서의 FOLDER_ID	사용유무		// 현재 사용안함 ( 추후에 필요시 사용 하자 )
		boolean bIsAttachCategory	= false;	// 첨부 문서의 CATEGORY		사용유무		// 현재 사용안함 ( 추후에 필요시 사용 하자 )
		String  sAttachFolderId		= "";			// 첨부 문서의 FOLDER_ID				// 현재 사용안함 ( 추후에 필요시 사용 하자 )
		String  sAttachCategory		= "";			// 첨부 문서의 CATEGORY					// 현재 사용안함 ( 추후에 필요시 사용 하자 )
		
		
		boolean isCEO    = false; // CEO
		boolean isDocImp = false; // 즁요문서
		
		String	sDownloadGubun 		= "";	// HTML, DRM, ORIGINAL 중 다운로드 구분 처리.
		
		
		// TODO :: isCEO 확인해보자
		
		//===========================================================================
		//	기능 확인한부분
		// 		DRM 처리
		//      원문 다운로드시 용량 큰 부분 html 로 다운로드 필요함
		//      DRM 다운로드시 ( 첨부, 보기, 편집 구분 필요함 :: 로그 입력을 위해서 )
		//      원문 다운로드 로직 확인필요.
		//      로그 추가 필요.
		//      superSession 처리 필요??  idf_SuperSess.
		//      첨부 정책 받아야함 	--> Agent 에서 정보값 못 던진다고 함 ( 최종 파일 다운 로드 할때 정책 확인 필요 )   --> SYDID 받음
		//      [인피니 기능] 첨부 정책 통해서 	--> 보안등급 구분 필요함.  ( 권한이 안맞으면 MSG 처리 필요 )		
		//      문서 상태 구분은 어떻게 할건가?		:: 현재 까지 따로 구분 없어서 모든 조건 입력 해서 처리함.
		//      권한 높은지 비교는 어떻게 할건가?	:: 현재 까지 따로 구분 없어서 모든 조건 입력 해서 처리함.
		//      파일명 보기, 편집시 실제 파일명과 똑같이 리턴 하자		
		//      반출함 은 agent 통해서 다운로드 안함
		// 	    	복호화 원문 다운로드시 object_id  와복호화 결재 r_object_id 와 매칭이 맞는지 확인 필요함
		// 	    		==> 이때 web 에서는 approve r_object_id 를 어떻게 던져 야 될지 생각 필요함...
		//      			==> 보기 한다고 건수 관련 처리 없음 ::: 원문 다운로드시 원문 다운로드 하고 로그 입력 하면됨.
		//      문서보기 작업구분 :, 조회=[D:Dbox, L:링크], 첨부=[O:원문, H:HTML], 복호화 반출=[S:"
		//===========================================================================
		
		//Documentum
		IDfSysObject idf_sObj       = null;   	//대상 문서
		
		String s_ObjectName			= ""; 		// 문서명
		String s_CurrVersion 		= ""; 		// 문서버전
		Long   l_ContentSize		= (long) 0;	// Size
		boolean b_IsCheckout		= false;	// Checkout 유무
		String s_DocStatus			= "";
		String s_DocKey				  = ""; 		// u_doc_key
		String s_DocExt				  = ""; 		// u_file_ext 
		String s_SecLevel       = "";
		boolean b_PrivacyFlag   = false;
		
		AttachPolicy attachPolicy	= null;
		String sAttachSystemName	= "";
		String sAttachType			= "";
		String sAttachDocStatus 	= "";
		String sAttachLimitSec  	= "";
		boolean bDrmFlag			= true;
		boolean bExternalFlag		= false;	// 첨부시 외부/내부 여부
		boolean bMessengerFlag		= false;	// 첨부시 메신저 여부
		boolean bDocCompleteFlag	= false;	// 첨부시 메신저 여부
		
		// 보기시 HTML 파일 OPEN 시 첨부 시스템 확인
		String	sSysIdSystemKey		= "";		// 로그 입력시 사용 ( 보기 편집시 로그 입력 안하면서 사용안함 )
		
		String s_WasFileName		= ""; // Was File Name
		String s_WasDownLoadPath	= ""; // Was DownloadPath
		String s_ClientFileName		= ""; // PC 에 저장시 사용될 파일명.
		
		// 사용자 정보 & 세션.
		VUser 		user 			= null;
		UserSession userSession 	= null;
		IDfSession idfSession		= null;
		IDfSession idfSession_bak	= null;
		IDfSession idfAdminSession	= null;
		
		//=================================
		//	기능구별
		// 		1. 호출 구분 ( 첨부, 보기, 편집 )
		// 		2. 예외 정책 조회 ( 보기 자동권한, 보기 반출함, 첨부 시 예외 )
		// 		3. 다운로드
		//=================================
		try {
			
			//==============================================
			// 0. 필수 정보 확인
			//==============================================
			if(StringUtils.isBlank(r_object_id))
			{
				String sMsg = "문서 ID 정보에 값이 없습니다.(호출된 정보가 없습니다)";
				rtnJsonObject.put("file_path", 		"");
				rtnJsonObject.put("file_name", 		"");
				rtnJsonObject.put("return_code", 	"-1");
				rtnJsonObject.put("return_msg", 	sMsg);
				return rtnJsonObject;
				
			}
			else if(StringUtils.isBlank(user_id))
			{
				String sMsg = "사용자 ID 정보에 값이 없습니다.(호출된 정보가 없습니다)";
				rtnJsonObject.put("file_path", 		"");
				rtnJsonObject.put("file_name", 		"");
				rtnJsonObject.put("return_code", 	"-1");
				rtnJsonObject.put("return_msg", 	sMsg);
				return rtnJsonObject;
				
			}
			
			// 사용자 정보 & 세션.  조회.
			user 			= userSevice.selectOneByUserId( user_id ).orElseThrow(() -> new NotFoundException(VUser.class, "NO USER"));
			userSession 	= UserSession.builder().dUserId(user_id).docbase(DCTMConstants.DOCBASE).dPw(DCTMConstants.DCTM_GLOBAL_PW).user(user).build();
			idfSession 		= this.getIdfSession(userSession);
			
			//=================================
			// Null 체크
			// 	syspath 는 모두 소문자로처리
			//=================================
			if(StringUtils.isBlank(syspath)){ syspath	= "";}
			if(StringUtils.isBlank(flag))	{ flag 		= "";}
			
			// 소문자로 변경.
			syspath = syspath.toLowerCase();
			
			//=================================
			// 1. 첨부, 보기, 편집 구분
			//	ATTACH, VIEW, EDIT
			//=================================
			if(!syspath.equals("")) {
				
				
				//==================================
				// 첨부 기본 다운로드 정책
				// 	첨부도 아니고, 보기도 아닐경우 편집..
				//==================================
				sViewEditAttach = "ATTACH";
				flag 			= "";
				sDownloadGubun 	= AgentDownStatus.ATTACH_DRM.getValue();
				
				
				//========================================================================
				// 첨부시도 R_OBJECT_ID|U_DOC_KEY|APPROVE_ID|FOLDER_ID|CATEGORY 로 사용함.
				//	SYSPATH 는 문서 정보에 포함 하지 않고 자체 적으로 따로 받음.
				//========================================================================
				String[] saObjectIdInfo	= r_object_id.split("_");
				r_object_id 			= saObjectIdInfo[0];
				
				if(saObjectIdInfo.length > 1)
				{
					bIsViewDocId	= true;
					sViewDocId 		= saObjectIdInfo[1];
				}
				
				if(saObjectIdInfo.length > 2)
				{
					bIsViewApprove	= true;
					sViewApproveId 	= saObjectIdInfo[2];
				}
				
				//=========================================
				// 반출함 폴더 일경우만 사용함  ( 추후에 필요시 사용 하자 )
				//  나머진 1자리 or 2자리 , 3자리만 사용
				//  반출함만 : 0900303980002113_0900303980002113_0000303980005913_EXP_EXP
				//=========================================
				if(saObjectIdInfo.length > 3)
				{
					bIsAttachFolderId	= true;
					sAttachFolderId 	=  saObjectIdInfo[3];
				}
				
				if(saObjectIdInfo.length > 4)
				{
					bIsAttachCategory	= true;
					sAttachCategory		=  saObjectIdInfo[4];
				}
				
				//=========================================
				// SYSID 는 자체적으로 받음 : syspath
				//=========================================
				
			}
			else if(syspath.equals("") && (flag.equals("4") || r_object_id.length() > 16))
			{
				
				//==================================
				// 보기 기본 다운로드 정책
				//==================================
				sViewEditAttach = "VIEW";
				sDownloadGubun 	= AgentDownStatus.DOWNLOAD_DRM.getValue();

				
				//=================================================================
				// 일반 WEB 에서 보기 , 첨부 문서 (sysid) 포함된 html 문서 보기 로 구분함
				//
				// 	[1] 일반 Dbox : 0900123456789001
				// 				09000000_2F71uV9BED92Irc46h5QfZLs%3D|8TyZgptnyAVfDNO6bC8%3D
				//		- 일반 WEB 은 r_object_id 1개로만 사용처리 가능함
				// 
				//  [2] 첨부 문서  : currentid + "_" + 암호화(docid + syspath 의 attach 타입 r_object_id )   : 자리수 120 자리 이내로 하기 위함 
				//		- 그외 html 문서 보기등 .. R_OBJECT_ID_R_OBJECT_ID+syspath의 r_object_id   
				//		암호화 : Aes256 -> Base64.encodeBase64URLSafeString
				//
				//  ** 변경 반출함 보기는 없기 때문에 approveId 는 사라짐          // dockey, approveid, viewsyspath,  
				//  ** U_DOC_KEY 도 R_OBJECT_ID 에서 확인 하면 되어서 필요 없음
				//=================================================================
				
				String[] saObjectIdInfo	= r_object_id.split("_");
				r_object_id 			= saObjectIdInfo[0];
				
				// 보기시 syspath 가 있을경우 objectid 와 비교 해서 같은지 확인. ( 같을경우 자동권한 시스템이면 -> 자동보기 처리 )
				if(saObjectIdInfo.length > 1)
				{
					bIsViewSysId	= true;
					sViewSysId 		=  saObjectIdInfo[1];
					
					try {
						
						// BASE64 DECODE
						byte[] byteObjSyspath		= sViewSysId.getBytes("UTF-8");
						byte[] byteDescObjSyspath	= Base64.decodeBase64(byteObjSyspath);
						String sDecBase64			= new String(byteDescObjSyspath, "UTF-8");
						
						// AES256 복호화
						AES256Util aesUtil			= new AES256Util();
						String sDecSysPath			= aesUtil.decrypt(sDecBase64);
						
						String sSysIdDocKeyId			= sDecSysPath.substring(0, 16);	// 문서의 U_DOC_KEY  : (사용안함 )문서의 R_OBJECT_ID
						String sSysIdAttachRObjectId	= sDecSysPath.substring(16);	// EDMS_ATTACH_POLICY 의 R_OBJECT_ID
						sSysIdSystemKey 				= "";							// EDMS_ATTACH_POLICY 의 U_SYSTEM_KEY1
						
						if(sSysIdAttachRObjectId.equals("dbox"))
						{
							sSysIdSystemKey = "dbox";
						}
						else
						{
							// 첨부시스템 r_object_id
							Optional<AttachPolicy> opAttachPolicy = attachPolicyDao.selectOneByObjectId(sSysIdAttachRObjectId);
							if(opAttachPolicy.isPresent())
							{
								sSysIdSystemKey = opAttachPolicy.get().getUSystemKey1();
							}
							else
							{
								sSysIdSystemKey = "dbox";
							}
						}
						
						// 암호화 된부분과 비교시 DOC_KEY 로 함.
				    	String sDocKey = "";
				    	Optional<Doc> optDocCheck =  docDao.selectDetailOne(r_object_id, user_id, false);
						
						if (optDocCheck.isPresent()) {
							
							Doc doc = optDocCheck.get();
							sDocKey	= doc.getUDocKey();
						}
						
						// syspath 에 입력되어 있는 dockey 와 실제 문서의 dockey 가 같을경우 자동권한 부여 여부 확인함.
						if(sDocKey.equals(sSysIdDocKeyId))
						{
							sAutoPermitSystemYn = "Y";
							
							// 2021-12-30 :: 모든 HTML 은 자동권한 추가임
//							CodeFilterDto codeFilterDto = CodeFilterDto.builder()
//																	.uCodeType(CodeType.ATTACH_MAIL_SYSTEM.getValue())
//																	.uCodeVal1(sSysIdSystemKey)
//																	.build();
//							    List<Code> codeList = codeDao.selectList(codeFilterDto);
//							    if (codeList != null && codeList.size() > 0)
//							    {
//							    	sAutoPermitSystemYn = "Y";
//							    }
//							
						}
						
						
						//===================================================
						// HTML 자동권한 시스템일경우 ( 문서 권한 없는지 확인 필요 )
						// 보기일경우(html 실행) 자동권한 시스템 이면서 문서ID 복호화후 변경이 없는게 확인될 경우 :: 권한 없으면 관리자 세션으로 변경해서 다운로드 가능하게함.
						//===================================================
						if(sAutoPermitSystemYn.equals("Y"))
						{
							
							//================================
							// 첨부정책 조회해서 자동권한 시스템인지 확인 필요함.
							// 자동권한 정책은 어떤 table or 코드 에서 확인 가능 한가?
							// 문서 속성 메일 자동권한 부여 flag 처리 도 활용 해야 되는가?
							//================================
							
							// 자동권한 시스템일경우 ( HTML or URL 로 접근 할때만 실행됨 : 권한 없으면 관리자 권한으로 다운로드함. )
							// 문서 확인 ( 선택된 문서가 없으면 dockey 정보로 확인 )
					    	Optional<Doc> optDoc = docDao.selectDetailOne(r_object_id, userSession.getDUserId(), false);
					    	
							// 문서 권한 확인
							if (optDoc.isPresent()) {
								
								Doc doc = optDoc.get();
								if (doc.getMaxLevel() < GrantedLevels.READ.getLevel() && doc.isUAutoAuthMailFlag())
						        {
									bIsAutoPermitDoc = true;
						        }
							}
						}
						
					} catch (Exception e) {
						// TODO: handle exception
					}
					
				}
							
			}
			else if(syspath.equals("") && !flag.equals("4"))
			{

				//==================================
				// 편집 기본 다운로드 정책
				// 	첨부도 아니고, 보기도 아닐경우 편집..
				//==================================
				sViewEditAttach = "EDIT";	
				sDownloadGubun 	= AgentDownStatus.DOWNLOAD_DRM.getValue();
				

				if(r_object_id.length() > 16 )
				{
					r_object_id = r_object_id.substring(0, 16);
				}
				
			}
			
			
			//===============================================
			// 보기시 예외 사항		
			//		- 반출함 문서 복호화 처리 		확인
			//		- 자동권한(메일) 시스템인지	확인
			//		- 반출함, 자동권한일경우 권한 없으면 관리자 세션 으로 처리
			
			
			// 역할자 추가 필요
			// 	문서가 편집중일경우는 dmadmin 으로 로그인해서 다운로드만
			//	이후 다시 html 호출시 편집 아닐경우만 권한 추가. 
			//===============================================
			if(bIsAutoPermitDoc)
			{
				
				// 권한 추가 , BASE 에도 추가필요.
				idfAdminSession = DCTMUtils.getAdminSession();
				idf_sObj 		= (IDfSysObject)idfAdminSession.getObject(new DfId(r_object_id));
				
				int iUserPermit = idf_sObj.getPermitEx(userSession.getDUserId());
				
				if(iUserPermit < 3 && !idf_sObj.isCheckedOut())
				{
					
					try
					{
						
						// 권한 추가
						idf_sObj.grant(userSession.getDUserId(), GrantedLevels.findByLabel("R"), "");
						idf_sObj.save();
						
						
//						IDfACL idfAcl = idf_sObj.getACL();
//						if(idfAcl.getObjectName().contains("dm_45"))
//						{
//							idfAcl.grant(userSession.getDUserId(), GrantedLevels.findByLabel("R"), "");
//							idfAcl.save();
//						}
						
						// 전체권한 변경.
						DCTMUtils.setAllDocACL(idfAdminSession, r_object_id);
						
						// 역할자 있는지 여부 체크
						IDfPersistentObject idf_AuthBase = idfSession.getObjectByQualification("edms_auth_base where u_obj_id='"+ idf_sObj.getChronicleId() +"'"
																											+	" and u_obj_type='D'"
																											+	" and u_doc_status='" + idf_sObj.getString("u_doc_status") +"'"
																											+	" and u_permit_type='R'"
																											+	" and u_author_id='" + userSession.getDUserId() +"'");
						// 값이 없으면 추가 필요.
						if(idf_AuthBase == null) 
						{
							
							// 역할자 추
							IDfPersistentObject idf_PObj = idfSession.newObject("edms_auth_base");
							
							idf_PObj.setString("u_obj_id"		, idf_sObj.getChronicleId()+"");
							idf_PObj.setString("u_obj_type"		, "D");
							idf_PObj.setString("u_doc_status"	, idf_sObj.getString("u_doc_status"));
							idf_PObj.setString("u_permit_type"	, "R"); //읽기
							
							if(user.getDeptCabinetcode().equals(idf_sObj.getString("u_cabinet_code")))
							{
								idf_PObj.setString("u_own_dept_yn", "Y");
							}
							else
							{
								idf_PObj.setString("u_own_dept_yn", "N");
							}
							
							idf_PObj.setString("u_author_id"	, userSession.getDUserId());
							idf_PObj.setString("u_author_type"	, "U"); //사용자 
							idf_PObj.setString("u_create_user"	, userSession.getDUserId());
							idf_PObj.setTime  ("u_create_date"	, new DfTime());
							idf_PObj.setString("u_ext_key"		, "");            	//결재 ID
							idf_PObj.setString("u_add_gubun"	, ""); 				//
							idf_PObj.save();
						}
						
					}
					catch (Exception e) {
						log.info("[checkOut] ROLE ERROR :: " + e.toString() + e.getMessage());
					}

				}
				
				idfSession_bak	= idfSession;
				idfSession		= idfAdminSession;
				
				// 이전 정보.
				/*
				idfAdminSession = DCTMUtils.getAdminSession();
				idf_sObj 		= (IDfSysObject)idfAdminSession.getObject(new DfId(r_object_id));
				
				int iUserPermit = idf_sObj.getPermitEx(userSession.getDUserId());
				
				if(iUserPermit < 3)
				{
					idfSession_bak	= idfSession;
					idfSession		= idfAdminSession;
				}
				*/
				
				// 권한 추가 , BASE 에도 추가필요.
				
				
				
			}
			// 보기 예외 완료.
			
			
			//=========================================================================
			// 2. 문서정보 조회. 
			// 		UI 에서 보기 편집시 (선택된 ID or 최신 ID 조회후 ) 실행 하기때문에 최신 ID 조회 안함. :: 편집일경우 최신 ID 다운로드 필요.
			// 		문서 정보 조회 시작.
			//=========================================================================
			idf_sObj 	= (IDfSysObject)idfSession.getObject(new DfId(r_object_id));
		  
			s_CurrVersion 	= idf_sObj.getVersionLabel(0);										// idf_sObj.getVersionLabels().getImplicitVersionLabel() 같음
			l_ContentSize	= idf_sObj.getContentSize();
			s_ObjectName	= idf_sObj.getObjectName().replaceAll("/", "-");
			b_IsCheckout	= idf_sObj.isCheckedOut();
			// s_Title			= com.google.common.io.Files.getNameWithoutExtension(s_ObjectName); // Title 사용안함.. idf_sObj.getTitle();  
			// s_File_Ext		= com.google.common.io.Files.getFileExtension(s_ObjectName);  		// DCTMCommonUtil.getDocExtension(idf_sObj.getObjectName());
			
			// 권한, 상태 정보
			s_SecLevel		= idf_sObj.getString("u_sec_level");
			s_DocStatus		= idf_sObj.getString("u_doc_status");
			s_DocKey		= idf_sObj.getString("u_doc_key");
			s_DocExt      	= idf_sObj.getString("u_file_ext");
			b_PrivacyFlag 	= idf_sObj.getBoolean("u_privacy_flag");
			
			//=============================================================
			// 파일다운로드 포맷
			//	파일명 + 팀명 + 버전 + 날짜 + 작성자
			// 예) 전략실 연간업무계획 보고서_경영전략팀_1_2021-07-20_홍길동.PPTX 
			//	
			// 2022-01-11 정책 변경
			//	파일명 + 문서소속부서 팀명 + 버전 + 날짜 + 수정자
			//	예) 테스트파일_지식자산팀_1_2022-01-11_차소익.txt
			//	** u_update_date 가 없으면 u_update_date 로 사용.
			//=============================================================
			String sOrgName		= userSession.getUser().getOrgNm().replaceAll("/", "-");
			// String sVersion     = idf_sObj.getVersionLabel(0).substring(0, idf_sObj.getVersionLabel(0).indexOf("."));
			String sVersion     = idf_sObj.getVersionLabel(0).contains(".") ? idf_sObj.getVersionLabel(0).substring(0, idf_sObj.getVersionLabel(0).indexOf(".")) : idf_sObj.getVersionLabel(0);
			
			
			String sModifyDate	= ""; // idf_sObj.getString("u_update_date").substring(0, 10);
			if(StringUtils.isNotBlank(idf_sObj.getString("u_update_date")) && !idf_sObj.getString("u_update_date").equals("nulldate"))
			{
				sModifyDate	= idf_sObj.getString("u_update_date").substring(0, 10);
			}
			else
			{
				sModifyDate	= idf_sObj.getString("r_modify_date").substring(0, 10);
			}
			
			String sUserName	= userSession.getUser().getDisplayName();
			String sFileFormat	= idf_sObj.getString("u_file_ext");
			
			// 최종 수정자
			if(StringUtils.isNotBlank(idf_sObj.getString("u_last_editor")))
			{
				VUser userData = userService.selectOneByUserId(idf_sObj.getString("u_last_editor")).orElse(new VUser());
				sUserName	= userData.getDisplayName();
			}
			
			// 문서 소유 부서의 팀
			if(StringUtils.isNotBlank(idf_sObj.getString("u_cabinet_code")))
			{
				
				VDept vDept = gwDeptDao.selectOneByCabinetCode(idf_sObj.getString("u_cabinet_code")).orElse(new VDept());
				sOrgName 	= StringUtils.stripToEmpty(vDept.getOrgNm()).replaceAll("/", "-");
			}
			
			
			String sRtnFileName		= s_ObjectName + "_" + sOrgName + "_" + sVersion + "_" + sModifyDate + "_" + sUserName + "." + sFileFormat;
			String sRtnHtmlFileName	= s_ObjectName + "_" + sOrgName + "_" + sVersion + "_" + sModifyDate + "_" + sUserName + "." + "html";
			
			//========================
			// 첨부 일경우 확인필요 권한 체크
			//========================
			if(sViewEditAttach.equals("ATTACH"))
			{
				
				// r_object_id	==> 문서 고유 ID
				// bIsAttachFolderId	: 첨부 문서의 FOLDER_ID	사용유무
				// bIsAttachCategory	= false;	// 첨부 문서의 CATEGORY		사용유무		// 현재 사용안함 ( 추후에 필요시 사용 하자 )
				// sAttachFolderId		= "";			// 첨부 문서의 FOLDER_ID				// 현재 사용안함 ( 추후에 필요시 사용 하자 )
				// sAttachCategory		= "";			// 첨부 문서의 CATEGORY					// 현재 사용안함 ( 추후에 필요시 사용 하자 )
				// bIsViewApprove		= ""; 			// 반출함 ID 유무
				// sViewApproveId		= ""; 			// 반출함 ApproveId
				// flag					=> 1 : 링크, 2 : 원문, 3 : DRM, 4 : 읽기	

				// 반출함 여부 체크
				if(bIsAttachFolderId && bIsAttachCategory && ( sAttachFolderId.equals("EXP") || sAttachCategory.equals("EXP") ) )
				{
					bIsExpFile = true; // 반출함 파일.
					
					// TODO :: sViewApproveId : 반출함 ID DB 에 포함되어 있는지 여부 확인 필요함.
					// TODO :: sViewApproveId : 반출함 ID DB 에 포함되어 있는지 여부 확인 필요함. -> 로그 입력 필요.
					
				}
				
				// 첨부 정책 확인
				attachPolicy 		= attachPolicyDao.selectOneBySystemKey(syspath).orElse(AttachPolicy.builder().build());
				
				sAttachSystemName	= attachPolicy.getUSystemName();	// 시스템명 ( 추후 로그 입력시 사용함 )
				sAttachType			= attachPolicy.getUAttachType();	// H:HTML, O:원문					--> 
				sAttachDocStatus 	= attachPolicy.getUDocStatus();		// L:Live, C:Closed, A 전체.
				sAttachLimitSec  	= attachPolicy.getULimitSecLevel();	// S:제한,T:팀내,C사내:,G:그룹사내
				bDrmFlag			= attachPolicy.isUDrmFlag();		// true : 암호화, false : 복호화
				bExternalFlag		= attachPolicy.isUExternalFlag();	// 첨부시 외부/내부 여부
				bMessengerFlag		= attachPolicy.isUMessengerFlag();	// 첨부시 메신저 여부
				bDocCompleteFlag	= attachPolicy.isUDocComplete();
				
				if(bIsExpFile)
				{
					// 반출함문서 는 복호화.
					sDownloadGubun = AgentDownStatus.ATTACH_ORIGINAL.getValue();
					
					// 반출함이지만 용량 20M 넘으면 HTML 다운로드
					// - 복호화 원문 다운로드시 20M 이상일경우 HTMl 다운로드 형식으로 변경 필요.  ::: 1 : 링크, 2 : 원문, 3 : DRM
					if(l_ContentSize > 20 * 1024 * 1024 )
					{
						sDownloadGubun = AgentDownStatus.ATTACH_HTML.getValue();
					}
					
				}
				else if(sAttachType.equals("O") && !bDrmFlag)
				{
					// 원문첨부 + 복호화 일경우  DRM 해제후 다운로드
					sDownloadGubun = AgentDownStatus.ATTACH_ORIGINAL.getValue();
				}
				else if(sAttachType.equals("H"))
				{
					sDownloadGubun = AgentDownStatus.ATTACH_HTML.getValue();
				}
				else if(sAttachType.equals("O"))
				{					
					sDownloadGubun = AgentDownStatus.ATTACH_DRM.getValue();
				}
				else
				{
					// 이부분 사용안함..
					flag	= sAttachType;	// H:HTML, O:원문  
				}
				
				log.info("[docCheckOut] attach sAttachSystemName 	:: " + sAttachSystemName);
				log.info("[docCheckOut] attach sAttachType 			:: " + sAttachType);
				log.info("[docCheckOut] attach sAttachDocStatus		:: " + sAttachDocStatus);
				log.info("[docCheckOut] attach sAttachLimitSec 		:: " + sAttachLimitSec);
				log.info("[docCheckOut] attach s_DocStatus 			:: " + s_DocStatus);
				log.info("[docCheckOut] attach sDownloadGubun		:: " + sDownloadGubun);
				log.info("[docCheckOut] attach s_CurrVersion 		:: " + s_CurrVersion);
				log.info("[docCheckOut] attach l_ContentSize		:: " + l_ContentSize);
				log.info("[docCheckOut] attach flag 				:: " + flag);

				//==========================================
				// 정책 체크 필요.
				//	Agent 에서 Msg 처리 필요함..
				//		추가 2022.02.09 : 개인정보일경우 첨부 할수 없음
				//		추가 2022.02.09 : LIVE 는 보안등급 체크 안함  CLOSE 일경우만 체크함.
				//==========================================
				
				// 문서 상태 체크 ( 전체 아닐경우 상태 비교 )
				if (!bIsExpFile && !sAttachDocStatus.equals("A") && !sAttachDocStatus.equals(s_DocStatus) )
				{
			
					String sMsg = sAttachDocStatus.equals("L") ? "Live 문서만 첨부가능합니다." : "Closed 문서만 첨부가능합니다.";
					rtnJsonObject.put("file_path", 		"");
					rtnJsonObject.put("file_name", 		"");
					rtnJsonObject.put("return_code", 	"-1");
					rtnJsonObject.put("return_msg", 	sMsg);
					return rtnJsonObject;
				}
				
				// 개인정보 문서 첨부 안됨 ( LIVE, CLOSE 전부 )
				if(!bIsExpFile && b_PrivacyFlag)
				{
					rtnJsonObject.put("file_path", 		"");
					rtnJsonObject.put("file_name", 		"");
					rtnJsonObject.put("return_code", 	"-1");
					rtnJsonObject.put("return_msg", 	" 개인 정보 문서는 첨부할 수 없습니다.");
					return rtnJsonObject;
				}
				
				// 문서 보안등급 체크 ( 정첵 보다 낮을경우 처리 ) LIVE 는 보안등급 체크 안함 , LIVE 는 등급이 없음.
				if(!bIsExpFile 
						&& DocStatus.CLOSED.getValue().equals( s_DocStatus )
						&& LimitSecLevelCode.getCodeByName(s_SecLevel).getValue() > LimitSecLevelCode.getCodeByName(sAttachLimitSec).getValue())
				{
					
					rtnJsonObject.put("file_path", 		"");
					rtnJsonObject.put("file_name", 		"");
					rtnJsonObject.put("return_code", 	"-1");
					rtnJsonObject.put("return_msg", 	LimitSecLevelCode.valueOf(sAttachLimitSec).getDesc() + " 보안등급까지 첨부가 가능합니다.");
					return rtnJsonObject;
				}
				
				// 2022-01-03 CLOSE 처리시 조건에 맞는 부분이 아닌 U_DOC_COMPLETE 에 맞으면 처리 함
				// Close 처리 조건일때 문서 편집중은 첨부 안되게함
				if( !bIsExpFile && bDocCompleteFlag
						&& DocStatus.LIVE.getValue().equals( s_DocStatus ) && b_IsCheckout) 
				{
					
					rtnJsonObject.put("file_path"	,	"");
					rtnJsonObject.put("file_name"	,	"");
					rtnJsonObject.put("return_code"	, 	"-1");
					rtnJsonObject.put("return_msg"	,	"편집중 문서는 첨부 할수 없습니다.");
					return rtnJsonObject;
				}
				
				// 제한문서 전자결재 에 첨부 불가능 ( 2021-12-22 ) ==> 이부분은 첨부 정책에서 제한 이하로 설정 하는걸로함
						
				//==========================================
				// 첨부시 Close 처리 필요
				//	내부, 메신저 제외, 원문첨부
				// 	추가로 : unidos 는 URL 첨부면서 완료 처리 함
				//	syspath.equals("unidos.dongkuk.com" 최종 한번더 확인하자
				// 
				// 2022-01-03 CLOSE 처리시 조건에 맞는 부분이 아닌 U_DOC_COMPLETE 에 맞으면 처리 함
				//==========================================
				//if(!bExternalFlag && !bMessengerFlag && sAttachType.equals("O") 
				if(!bIsExpFile && bDocCompleteFlag
						&& DocStatus.LIVE.getValue().equals( s_DocStatus ) && !b_IsCheckout )
				{
					// 공통 모듈 호출함
					try
					{
						String result = dataService.patchDocClosed(r_object_id, userSession, ip );						
					}
					catch (Exception e) {
						rtnJsonObject.put("file_path"	, 	"");
						rtnJsonObject.put("file_name"	, 	"");
						rtnJsonObject.put("return_code"	, 	"-1");
						rtnJsonObject.put("return_msg"	,	"첨부시 완료처리 가 실패 되었습니다.");
						return rtnJsonObject;
					}
				}
				
				
				
			}
			
			//=======================================
			// 3. 다운로드 처리
			// 		HTML, ORIGINAL, DRM 구분해서 다운로드 처리
			//=======================================
			if(sDownloadGubun.equals(AgentDownStatus.ATTACH_HTML.getValue()))
			{
				
				//======================================================
				// 1. 링크 다운로드 ( 첨부 HTML 파일 )
				//=========================
				// DownLoad 파일명
				//	s_WasFileName = s_Target_Id + "_V" + s_CurrVersion + "_" + user_id + "_" + DCTMUtils.getCurrentTime("") + "_R.html";
				//
				// WAS 다운로드 정보
				//	- s_WasFileName 	: WAS 임시 다운로드 파일명
				//	- s_WasDownLoadPath	: WAS Download Path 설정 ( DrmService 와 맞춤 ) -> 최종 html 만들고 이변수에 경로+ 파일명함.
				//	- WAS 최종 다운로드 경로 폴더 생성 ( makeHtmlDoc 에서 다시 한번 리턴함 )
				//======================================================
				s_WasFileName 		= UUID.randomUUID().toString()+ "." + Commons.DEFAULT_EXTENSION;				
				s_WasDownLoadPath 	= drmDir + File.separator;
				File encrypted 		= new File(drmDir + File.separator + s_WasFileName);
				FileUtils.forceMkdirParent(encrypted);
				
				//======================================================
				// URL 파라미터 암호처리
				//		- 자동권한 첨부시스템 구별을 위해서 syspth 를 암호화 함.
				//		- 파라미터 : r_object_id, sViewDocId, sViewApproveId, syspath, r_object_id+syspath
				//	
				//	순서	
				//		1. AES256 처리
				//		2. BASE64 처리 ( 파라미터 URL 형식에 사용될수 있게 맞춤 )
				//
				//
				//	HTML 실행시 U_DOC_KEY 를 활용 해서 처리 되기 때문에 암호화시 DOC_KEY 정보를 활용
				//		1. viewcheck 에서 활용
				//		2. agnet 에서 호출시 checkOut 에서 보기시 활용.
				//======================================================
				AES256Util aesUtil			= new AES256Util();
				String sEncSysPath			= aesUtil.encrypt(sViewDocId + syspath);
				String sUrlEncodeSysPath 	= Base64.encodeBase64URLSafeString(sEncSysPath.getBytes());
				
				
				// Download
				s_WasDownLoadPath = this.makeHtmlDoc(r_object_id, sViewDocId, sViewApproveId, syspath, sUrlEncodeSysPath,
															s_WasDownLoadPath, s_WasFileName, idfSession);
				
				s_ClientFileName	= sRtnHtmlFileName;
				
			} 
			else if (sDownloadGubun.equals(AgentDownStatus.ATTACH_ORIGINAL.getValue()) ) 
			{
				
				//====================================
				// 2. 원문 다운로드 ( 첨부 복호화 필요 )
				//		첨부 반출함 원문 		:: sViewEditAttach.equals("ATTACH") && sDownloadGubun.equals(AgentDownStatus.ATTACH_ORIGINAL.getValue())
				//		보기 html 반출함 원문 	:: AgentDownStatus.DOWNLOAD_ORIGINAL.getValue()
				//
				// WEB 에서 복호화 다운로드는 없음 : kupload 에서 처리함.
				//	최종 :  ==> 서버에 복호화 된 파일이 올라가 있기때문에 다운로드만 처리함
				//====================================
				CustomInputStreamResource rst = docService.downloadDoc(userSession, idf_sObj.getObjectId().getId());
				
				File orgFile = new File(drmDir + File.separator + UUID.randomUUID());
				FileUtils.forceMkdirParent(orgFile);
				FileUtils.copyInputStreamToFile(rst.getInputStream(), orgFile);
				
				// 파일명, 경로
				s_WasDownLoadPath	= orgFile.getAbsolutePath();
				s_ClientFileName	= sRtnFileName;

			}
			else if(sDownloadGubun.equals(AgentDownStatus.ATTACH_DRM.getValue()) 
					|| sDownloadGubun.equals(AgentDownStatus.DOWNLOAD_DRM.getValue())  )
			{
				
				//======================================================
				// 3. DRM 파일 다운로드 ( 암호화 해서 다운 )
				//======================================================
				// 
				// 첨부
				// 		3 : 첨부 DRM 파일 다운로드	: 		ATTACH_DRM
				// 보기/편집
				// 		사용안함 :: 2 : HTML 문서보기 시 반출출함일경우 첨부 DRM
				// 		3 : 편집							DOWNLOAD_DRM
				// 		4 : 보기							DOWNLOAD_DRM
				//
				//
				// 첨부 		: 사내한 DRM
				// 보기,편집 	: 개인한 DRM
				//======================================================
				
				// DRM 파일 로 처리. ( DataService 참고 )
				 // 특별사용자 리스트 (회장/부회장/각 회사 대표)
				String currentUserId = user.getUserId();
				
				Set<String> specialUserIdSet = codeService.getSpecialUserIdSet();
				isCEO = specialUserIdSet.contains(currentUserId);
				
				// CustomInputStreamResource rst = docService.downloadDoc(userSession, idf_sObj.getObjectId().getId());
				CustomInputStreamResource rst = null;
				if(bIsAutoPermitDoc)
				{
					// 관리자 세션으로 파일 다운로드함.
					rst = docService.downloadDoc(idfSession, idf_sObj.getObjectId().getId());
				}
				else
				{
					// 일반 세션으로 다운로드.
					rst = docService.downloadDoc(userSession, idf_sObj.getObjectId().getId());
				}
				
				// DataService downloadData 참고함.
				File drmFile  = null;
				String entCode = user.getComOrgId();
				String entName = codeService.getComCodeDetail(entCode).getUCodeName1();
				
				// 해당하는 형식이 아닐경우 drm실행 X. 반출함일경우도 false로 바꿈
				boolean 			doDRM   	 	= true ; 							// 기본이 DRM 실행
				Map<String, String> formatChkMap 	= null; 			// drm적용 대상 확장자
				formatChkMap 						= codeService.getDrmFormatCodeMap();
				if (!formatChkMap.containsKey(
						!DCTMConstants.DCTM_BLANK.equals(s_DocExt.toUpperCase())? 
								s_DocExt.toUpperCase() : FilenameUtils.getExtension(idf_sObj.getTitle()).toUpperCase())) { 
					doDRM = false;
				}
				
				// 첨부시와, (보기 편집) 시 DRM 구별함.
				if(sDownloadGubun.equals(AgentDownStatus.ATTACH_DRM.getValue()))
				{
					//===================
					// 첨부시 사내한.
					//===================

					if(doDRM)
					{

		        DrmAuthDto drmAuthorDto = authService.getAuthorsForDrm(user, entCode, entName, s_DocKey, rst.getDocStatus(), DrmSecLevelType.COMPANY.getCode());
            log.info("[----agent-drm----] INFO :: " + DrmSecLevelType.COMPANY.name() + "__secLevel__ : " + rst.getFileSecLevel());
            
            boolean isClosedSec = false;
            if (DocStatus.CLOSED.getValue().equals(s_SecLevel)) {
              if (SecLevelCode.SEC.getValue().equals(s_DocStatus)) {
                isClosedSec = true;
              }
            }
            
            boolean disableSaveEdit = isClosedSec || b_PrivacyFlag;
            
		        // 사내한 권한
						drmFile = drmService.encrypt(rst.getInputStream(), DrmSecLevelType.COMPANY, drmAuthorDto.getCompany(), 
						    drmAuthorDto.getAuthDeptList(), drmAuthorDto.getAuthUserList(),
								idf_sObj.getObjectId().getId(), idf_sObj.getString("u_doc_key"),
								rst.getFilename(), rst.contentLength(), currentUserId, user.getDisplayName(), user.getOrgId(),
								user.getOrgNm(), entCode, entName, disableSaveEdit, ip);
				
					}
					else
					{
						// 암호화 하지 않음
						drmFile = new File(drmDir + File.separator + UUID.randomUUID());
						FileUtils.forceMkdirParent(drmFile);
						FileUtils.copyInputStreamToFile(rst.getInputStream(), drmFile);
					}
					
				}
				else
				{
					
					//===================
					// 보기, 편집시 개인한.
					//===================
					
					
					// DRM 사용(확장자), DRM 사용불가 확장자 파일 구분
					if(doDRM)
					{
            boolean isClosedSec = false;
            if (DocStatus.CLOSED.getValue().equals(s_DocStatus)) {
              if (SecLevelCode.SEC.getValue().equals(s_SecLevel)) {
                isClosedSec = true;
              }
            }

            boolean disableSaveEdit = isClosedSec || b_PrivacyFlag;
						
						// 첨부시 개인 DRM
            DrmAuthDto drmAuthorDto = authService.getAuthorsForDrm(user, entCode, entName, s_DocKey, rst.getDocStatus(), rst.getFileSecLevel());
            log.info("[----agent-drm----] INFO :: " + drmAuthorDto.getSecLevelType() + "__secLevel__ : " + rst.getFileSecLevel());
            drmFile = drmService.encrypt(rst.getInputStream(), drmAuthorDto.getSecLevelType(), drmAuthorDto.getCompany(), 
                drmAuthorDto.getAuthDeptList(), drmAuthorDto.getAuthUserList(),
								idf_sObj.getObjectId().getId(), idf_sObj.getString("u_doc_key"),
								rst.getFilename(), rst.contentLength(), currentUserId, user.getDisplayName(), user.getOrgId(),
								user.getOrgNm(), entCode, entName, disableSaveEdit, ip);
						
						
					}
					else
					{

						// 암호화 하지 않음
						drmFile = new File(drmDir + File.separator + UUID.randomUUID());
						FileUtils.forceMkdirParent(drmFile);
						FileUtils.copyInputStreamToFile(rst.getInputStream(), drmFile);
						
					}
					
				}
				
				// 파일명 처리.
				s_WasDownLoadPath 	= drmFile.getAbsolutePath();
				s_ClientFileName	= sRtnFileName;
				
			}
			
			sReturnFilePath 	= s_WasDownLoadPath;
			sReturnFileName 	= s_ClientFileName;
			
			
			//===============================
			// 4. 로그 입력
			// 		** 반출함 일경우 추가로 첨부시 완료 날짜 변경함 ( 최종 파일도 리턴 해야 해서 마지막에 추가함 )
			//		보기, 편집은 ui 에서 보기, 편집 호출 한 이후 추가로 로그 입력 Service 호출해서 입력 하기로함.
			//===============================
			if(sViewEditAttach.equals("ATTACH"))
			{
				
				Double 	dSelVersion		=  Double.parseDouble(s_CurrVersion);
				int 	iSelVersion		= dSelVersion.intValue();
				String  sVersionValue 	= String.valueOf(iSelVersion); 
				
				String sDocStatus 		= idf_sObj.getString("u_doc_status");	// L, C
				String sDocSecLevel		= idf_sObj.getString("u_sec_level");	// T, L
				String sDocCabinetCode  = idf_sObj.getString("u_cabinet_code");
				String sJobGubun 		= sDownloadGubun.equals(AgentDownStatus.ATTACH_HTML.getValue()) ? "H" : "O"; 	// 작업구분 ( 수정=[W:덮어쓰기, V:버전생성], 조회=[D:Dbox, L:링크], 첨부=[O:원문, H:HTML], 복호화 반출=[S:자가승인, A:자동스인, F:프리패스]) 
				String sOwnSrDeptOrgId 	= gwDeptService.selectOrgIdByCabinetcode( sDocCabinetCode );
				
				LogDoc logDoc = LogDoc.builder()
				          .uJobCode(DocLogItem.AT.getValue())
				          .uDocId(idf_sObj.getObjectId().getId())
				          .uDocKey(idf_sObj.getString("u_doc_key"))
				          .uDocName(idf_sObj.getTitle().replaceAll("'", "''"))
				          .uDocVersion(sVersionValue)
				          .uFileSize(idf_sObj.getContentSize())
				          .uOwnDeptcode(sOwnSrDeptOrgId)
				          .uActDeptCode(userSession.getUser().getOrgId())
				          .uJobUser(userSession.getUser().getUserId())
				          .uJobUserType("P") //작업자구분(P:개인, D:부서관리자, C:사별관리자, G:그룹관리자, S:시스템)
				          .uDocStatus(sDocStatus)
				          .uSecLevel(sDocSecLevel)
				          .uCabinetCode(sDocCabinetCode)
				          .uJobGubun(sJobGubun)
				          .uUserIp(ip)
				          .uAttachSystem(syspath)
				          .build();
				      insertLog(logDoc);
				      
				//===========================================
				// 반출함 관련 M 일경우 sysdate 로 변경 필요
				// 반출함이면서 반출함 정보가 첨부시 반출함 목록에서 삭제 처리
				// LIMIT_DATE 변경하면 리스트에 안나타남.
				//===========================================
				if(bIsExpFile)
				{
					idfSession.beginTrans();
					 
					DCTMUtils.updateTakeOutAttachLimitDate(idfSession, r_object_id, sViewApproveId);
					 
					if ( idfSession!=null && idfSession.isConnected() && idfSession.isTransactionActive() ) 
					{
						idfSession.commitTrans();
					}
					 
				 }
				
			}
			else if(sViewEditAttach.equals("VIEW") || sViewEditAttach.equals("EDIT"))
			{
				
				// 2022-01-26 : 보기도 WEB 에서 편집도 EO 로 WEB 에서 로그 입력 하기로 해서  사용안함 
				
				// 2022-01-13 : 보기는 WEB 에서 보기 한후 보기 로그 Service 호출 하는 방식으로 변경됨 ( 캐시 파일 보기시 로그가 안남기 때문 )
				// 보기, 편집시 보기 로그 입력함.
				
				// 2022-01-12 : 편집 -> 편집취소 시 아무런 로그 가 안남음
				//  편집 완료 하고 -> cancelcheckout 되는 구조여서   ( 편집 취소 로그를 남길수 가 없음 )
				//  그래서 편집시에도 보기 로기 입력 하기로함 
				
				/*
				Double 	dSelVersion		=  Double.parseDouble(s_CurrVersion);
				int 	iSelVersion		= dSelVersion.intValue();
				String  sVersionValue 	= String.valueOf(iSelVersion); 
				
				String sDocStatus 		= idf_sObj.getString("u_doc_status");	// L, C
				String sDocSecLevel		= idf_sObj.getString("u_sec_level");	// T, L
				String sDocCabinetCode  = idf_sObj.getString("u_cabinet_code");
				String sJobGubun 		= bIsViewSysId == true ? "L" : "D"; 	// 작업구분 ( 수정=[W:덮어쓰기, V:버전생성], 조회=[D:Dbox, L:링크], 첨부=[O:원문, H:HTML], 복호화 반출=[S:자가승인, A:자동스인, F:프리패스]) 
				String sOwnSrDeptOrgId 	= gwDeptService.selectOrgIdByCabinetcode( sDocCabinetCode );
				
				LogDoc logDoc = LogDoc.builder()
				          .uJobCode(DocLogItem.VE.getValue())
				          .uDocId(idf_sObj.getObjectId().getId())
				          .uDocKey(idf_sObj.getString("u_doc_key"))
				          .uDocName(idf_sObj.getTitle().replaceAll("'", "''"))
				          .uDocVersion(sVersionValue)
				          .uFileSize(idf_sObj.getContentSize())
				          .uOwnDeptcode(sOwnSrDeptOrgId)
				          .uActDeptCode(userSession.getUser().getOrgId())
				          .uJobUser(userSession.getUser().getUserId())
				          .uJobUserType("P") //작업자구분(P:개인, D:부서관리자, C:사별관리자, G:그룹관리자, S:시스템)
				          .uDocStatus(sDocStatus)
				          .uSecLevel(sDocSecLevel)
				          .uCabinetCode(sDocCabinetCode)
				          .uJobGubun(sJobGubun)
				          .uUserIp(ip)
				          .uAttachSystem(sSysIdSystemKey)
				          .build();
				      insertLog(logDoc);
				*/
			}
			
			
			
//			else if(sViewEditAttach.equals("EDIT"))
//			{
//				//====================================
//				// 편집 완료시만 로그 입력함.
//				// ischeckout -> docheckout -> checkout 순이어서  이부분 로그 입력 필요함.
//				// 이부분은 CHECKOUT 부분에서 사용필요.
//				// 
//				// 결론 ::: CHECKOUT 시 로그 입력 안함
//				//====================================
//			}
			
			// Return
			sReturnCode 		= "0";			// 정상처리
			sReturnMsg			= "SUCCESS";	// MSG
			
		} catch (Exception e) {
	
			sReturnCode = "-1";
			sReturnMsg	= "파일 다운로드에 실패하였습니다."; // FAIL
			
			log.info("[checkOut] ERROR :: " + user_id);
			log.info("[checkOut] ERROR :: " + e.toString() + e.getMessage());
			
			if ( idfSession!=null && idfSession.isConnected() && idfSession.isTransactionActive() ) 
			{
				idfSession.abortTrans();
			}
			
		} finally {
	
			// 관리자 세션 종료.
			if (idfAdminSession != null  && idfAdminSession.isConnected()) {
				
				idfAdminSession.disconnect();
				
				idfSession = idfSession_bak;			// 일반 사용자 세션 으로 되돌림. ( Release 처리 위함 )(					
				
			}
			
			// 사용자 세션 종료.
			if(userSession != null)
			{
				this.sessionRelease(userSession.getDUserId(), idfSession);				
			}
			
			
		}

		// Agent 리턴 정보.
		rtnJsonObject.put("file_path", 		sReturnFilePath);
		rtnJsonObject.put("file_name", 		sReturnFileName);
		rtnJsonObject.put("return_code", 	sReturnCode);
		rtnJsonObject.put("return_msg", 	sReturnMsg);
		
		log.info("[checkOut] END : " + rtnJsonObject.toString());
		
		return rtnJsonObject;
	}

	@Override
	public JSONObject docCheckOut(String user_id, String r_object_id, String ip) throws Exception {
		
		log.info("[docCheckOut] START : " + user_id);
		log.info("[docCheckOut] START : " + r_object_id);
		log.info("[docCheckOut] START : " + ip);
		
		//==============================================
		// 편집
		//==============================================
		// 호출.
		// 		- doDownload 이후 Agent 에서 편집 호출함
		//
		// Return
		// 		- return_code
		//		- return_msg
		//
		// 	** 편집 완료시만 로그 입력함.
		// 	** 결론 ::: CHECKOUT 시 로그 입력 안함
		//==============================================
		
		// 리턴 기본정보
		JSONObject rtnJsonObject 				= new JSONObject();
		String	sReturnCode 					= "0";				// RETURN CODE
		String	sReturnMsg						= "SUCCESS";		// RETURN MESSAGE
		
		// 사용자 정보 조회
		VUser 		vUser 		= null;
		UserSession userSession = null;
				
		//Documentum
		IDfSession 		idfSession	= null;
		IDfSysObject 	idf_sObj	= null;   //대상 문서
		
		try {
			
			vUser 		= userSevice.selectOneByUserId( user_id ).orElseThrow(() -> new NotFoundException(VUser.class, "NO USER"));
			userSession = UserSession.builder().dUserId(user_id).docbase(DCTMConstants.DOCBASE).build();
			
			
			idfSession = this.getIdfSession(userSession);
	
			if(r_object_id.length() > 16 )
			{
				r_object_id = r_object_id.substring(0, 16);
			}
			
			idf_sObj 				= (IDfSysObject)idfSession.getObject(new DfId(r_object_id));
			
			// 최신 ID :: // 최신 Object 확인
			String sChronicleId		= idf_sObj.getChronicleId().toString();
			String sCurrentObjId 	= DCTMUtils.getCurrentObjectID(idfSession, sChronicleId);
			idf_sObj 				= (IDfSysObject)idfSession.getObject(new DfId(sCurrentObjId));
			
			idf_sObj.checkout();
			
			// Return
			sReturnCode 		= "0";			// 정상처리
			sReturnMsg			= "SUCCESS";	// MSG
			
		} catch (Exception e) {

			sReturnCode = "-1";
			sReturnMsg	= "편집에 실패 하였습니다.";
			
			log.info("[doCheckOut] ERROR :: " + user_id);
			log.info("[doCheckOut] ERROR :: " + e.toString() + e.getMessage());
			
		} finally {

			// 사용자 세션 종료.
			if(userSession != null)
			{
				this.sessionRelease(userSession.getDUserId(), idfSession);							
			}
		}
		
		// Return 입력
		rtnJsonObject.put("return_code"		, sReturnCode);	
		rtnJsonObject.put("return_msg"		, sReturnMsg);
		
		log.info("[doCheckOut] END : " + rtnJsonObject.toString());
		
		return rtnJsonObject;
	}

	@Override
	public JSONObject checkCacheFile(String user_id, String r_object_id, String file_version) throws Exception {
		
		log.info("[checkCacheFile] START : " + user_id);
		log.info("[checkCacheFile] START : " + r_object_id);
		log.info("[checkCacheFile] START : " + file_version);
		
		//==============================================
		// 문서 버전 상태 확인용
		//==============================================
		// 호출.
		//		- 캐싱 파일 변경 이력 정보
		// 		- 캐싱된 파일의 변경 이력을 체크. 서버에 저장된 버전과 캐싱된 파일의 버전을 비교
		//
		// Return
		//		- retinfo			: 0 : 변경됨, 1 : 변경안됨
		// 		- return_code
		//		- return_msg
		//==============================================
		
		// 리턴 기본정보
		JSONObject rtnJsonObject	= new JSONObject();
		String 	sRetinfo			= "1";	
		String	sReturnCode			= "0";					// RETURN CODE
		String	sReturnMsg			= "SUCCESS";			// RETURN MESSAGE
		
		try {

			if(r_object_id.length() > 16 )
			{
				r_object_id = r_object_id.substring(0, 16);
			}
			
			// 선택된 문서 정보
			// 최신 문서 ID 조회후 편집 중 상태 확인
	    	Optional<Doc> optDoc 		= docDao.selectDetailOne(r_object_id, user_id, false);	    	
			if (optDoc.isPresent()) 
			{
				
				Doc 	doc 	= optDoc.get();
				String 	sDocKey = doc.getUDocKey();
				
				// 최신문서 정보 조회 ( DOC_KEY 를 이용 )
				Optional<Doc> optDocCurrent = docDao.selectDetailOne(sDocKey, user_id, true);
				if (optDocCurrent.isPresent()) 
				{
					
					Doc 	docCurrent 		= optDocCurrent.get();
					String sCurrentObjId	= docCurrent.getRObjectId();
					
					// 버전 변경 여부 확인  ( 최신 OBJECT_ID 비교 )
					if(!r_object_id.equals(sCurrentObjId)) {
						sRetinfo = "0";
					}
					else if(docCurrent.getMaxLevel() < GrantedLevels.READ.getLevel() )
					{
						// 권한이 없을경우  파일 다시 받게 처리함 
						// 로컬에 파일이있는 상태에서 URL 복사 해서 붙여 넣기 할경우 캐시가 있다고 생각 해서 ( 파일 다운로드 하면서 권한 추가 안함 )
						sRetinfo = "0";	
					}
				}
			}
			else
			{
				// 문서가 없을경우 중요문서함에서 한번더 확인
				// 중요문서함여부 확인
				// TODO :: 버전 정보 가 있을경우 추가 필요
				Optional<DocImp> optDocImp= docImpService.selectOne(r_object_id);
				if (optDocImp.isPresent()) 
				{
					
					DocImp	docImp 			= optDocImp.get();
					String sCurrentObjId	= docImp.getRObjectId();
					
					// 버전 변경 여부 확인  ( 최신 OBJECT_ID 비교 )
					if(!r_object_id.equals(sCurrentObjId)) {
						sRetinfo = "0";
					}
				}
			}
			
			// Return
			sReturnCode 		= "0";			// 정상처리
			sReturnMsg			= "SUCCESS";	// MSG
			
		} catch (Exception e) {

			sReturnCode = "-1";
			sReturnMsg	= "캐시 확인에 실패하였습니다.";
			
			log.info("[checkCacheFile] ERROR :: " + user_id);
			log.info("[checkCacheFile] ERROR :: " + e.toString() + e.getMessage());
			
		} finally {

		}
		
		// Return 입력
		rtnJsonObject.put("retinfo"			, sRetinfo);
		rtnJsonObject.put("return_code"		, sReturnCode);	
		rtnJsonObject.put("return_msg"		, sReturnMsg);
		
		log.info("[checkCacheFile] END : " + rtnJsonObject.toString());
		
		return rtnJsonObject;
		
	}

	
	
	@Override
	public JSONObject doCheckIn(String user_id, String r_object_id, InputStream inputStream, String path,
			String realFileName, String ip, String checkin_flag) throws Exception {
		
		log.info("[doCheckIn] START : " + user_id);
		log.info("[doCheckIn] START : " + r_object_id);
		log.info("[doCheckIn] START : " + path);
		log.info("[doCheckIn] START : " + realFileName);
		log.info("[doCheckIn] START : " + ip);
		log.info("[doCheckIn] START : " + checkin_flag);
		
		//==============================================
		// 파일변경
		//	- 편집 완료 기능을 ( 파일 변경, 편집 취소로 ) 2개로 나눠짐
		//==============================================
		// 호출.
		//			user_id		사용자 ID	
		//			r_object_id	문서 고유 ID	
		//			file		파일 스트림	     : stream 을 받아서 처리 필요함.
		//			file_path	파일 업로드 경로	 : 이부분은 로컬 파일 경로임....
		//			file_name	파일 명		 : 이부분은 로컬 파일 명임.
		//
		// 		- 편집완료	:: 사용자별로 정책에 의해 나눠짐
		//		- 중간저장 :: 사용자별로 정책에 의해 나눠짐 
		//
		// 				- doDownload 이후 Agent 에서 편집 호출함
		//				- 더이상 편집 안하는 문서 일경우 호출 되는 방식임 : 실제 편집 취소 아님..
		//
		//				- 편집 완료시 		--> doCheckin 호출후 doCancelCheckOut 호출
		//				- 편집 내용 없을경우 --> doCancelCheckOut 호출됨.
		// 
		// 버전정책 : 기본적으로 버전업
		//
		// 버전업 예)   1.0, 2.0, 3.0 문서, 과거버전 유무는 Agent 에더 던진 obj 가 현재 최신 obj id 인지 비교 필요.
		// 	- 최신버전 편집 (버전업) 		- 3.0 일경우 (docheckout 에서 3.0 편집): 3.0 편집 완료하면서 4.0 생성 -> 4.0 파일 변경(save) -> 4.0 checkout
		//	- 최신버전 편집 (현재버전) 	- 3.0 일경우 (docheckout 에서 3.0 편집): 3.0 파일 변경(save) 		-> 3.0 checkout
		//	- 과거버전 편집 (버전업) 		- 2.0 일경우 (docheckout 에서 3.0 편집): 3.0 편집 완료하면서 4.0 생성 -> 4.0 파일 변경(save) -> 4.0 checkout
		//	- 과거버전 편집 (현재버전) 	- 2.0 일경우 (docheckout 에서 3.0 편집): 3.0 편집 완료하면서 4.0 생성 -> 4.0 파일 변경(save) -> 4.0 checkout
		//
		//	버전업 : 선택된 문서에 대해 Check-In 호출 (호출 조건 : 내가 Lock Owner인 경우이거나, 또는 편집중이 아닌 상태)
		//
		//	drmService.check : 실제 파일이 암호화 되어 있는지 체크 하는 부분.
		//
		//----------------------------------------------------------------------
		//	기능 추가 ( 버전업, 복사본 생성, 오픈 , 다른이름으로 저장)
		//		조건 :
		//				버전업 : 선택된 문서에 대해 Check-In 호출 (호출 조건 : 내가 Lock Owner인 경우이거나, 또는 편집중이 아닌 상태)
		//					-> Check-In 시, flag값 필요
		//		복사본 생성 : 복사본 생성 서비스 호출(신규)
		//		오픈으로 연 경우에는, 서버에 저장될 수 있는 조건에서만 서버에 저장됨
		//		해당 문서의 Lock Owner인 경우에만 서버에서 저장 처리함.
		//		그 외의 경우에는 서버에서 “저장할 수 없다”는 Messsage를 리턴하고,
		// 
		//		Agent는 해당 문서에 대해 지우지 말고, 사용자에게 Return 받은 메시지를 출력함.
		//		이 경우에는 사용자에게 Job List의 버전업 이나 복사본 생성을 통해 처리하도록 유도
		//
		//		2022-01-13 : 실시간 저장 부서는 문서는 다른 사용자가 편집 완료 가능함.
		//	
		//		다른이름으로 저장
		//			- doSaveAsTemp 에서 TEMP 저장이후 doCheckin 호출함 (flag 추가됨)
		//			- 이후 기존 대로 doCheckout 호출 하고 편집중 상태로 변경됨.
		//----------------------------------------------------------------------
		//
		// Return
		// 		- return_code		"0 : 성공	-1 : 실패
		//		- return_msg		메시지 입력
		//==============================================
		// 리턴 기본정보
		JSONObject rtnJsonObject	= new JSONObject();
		String	sReturnCode 		= "0";					// RETURN CODE
		String	sReturnMsg			= "SUCCESS";			// RETURN MESSAGE
	
		String sTrayProcessGubun	= "";					// V: 버전업, W : 덮어쓰기, N : 등록 (TEMP 호출 이후 바로 호출시 ), G 일반 편집 완료시
		if(StringUtils.isNotBlank(checkin_flag))
		{
			sTrayProcessGubun	= checkin_flag;
		}
		
		IDfSysObject idfObj 		= null;		// 호출된 OBJ
		IDfSysObject idfNewObj 		= null;		// 호출후 NEWVerssion OBJ
		
		boolean bIsVersionUp		= false;	// 버전업 할지 여부
		boolean	bIsCurrent			= true;		// 선택된 버전이 최신 문서 인지 여부체크
		String 	sSavePolicy			= "NEW_VERSION"; // NEW_VERSION : 새로운버전, CURRENT_VERSION : 현재버전   ( 기본정책은 새로운 버전 )
		String 	sChronicleId		= "";
		String 	sCurrentObjId		= "";
		
		
		VUser 		user 			= null;
		UserSession userSession 	= null;
		IDfSession idfSession 		= null;
		IDfSession idfAdminSession	= null;
		boolean bIsMideSaveDept 	= false;	// 실시간 저장 문서
		boolean bIsOtherSaveUser 	= false;	// 실시간 저장 문서 이면서 편집자 와 현재 편집 완료자가 다를 경우
		
		String sCurrVersion			= "";	// 편집 이전 버전 정보
		String sAfterVersion		= "";	// 편집 이후 버전 정보
		String sWasTempName			= "";	// WAS TempFile
		
		String s_DocExt				= "";	// 파일 확장자.
		String s_SecLevel			= "";	// 보안등급
		boolean	doDRM   	 		= false;
		
		try {
			
			user 			= userSevice.selectOneByUserId( user_id ).orElseThrow(() -> new NotFoundException(VUser.class, "NO USER"));
			userSession 	= UserSession.builder().dUserId(user_id).docbase(DCTMConstants.DOCBASE).user(user).build();
			
			
			if(r_object_id.length() > 16 )
			{
				r_object_id = r_object_id.substring(0, 16);
			}
			
			// 자신이 체크아웃 여부
			idfSession 		= this.getIdfSession(userSession);
			idfObj 			= (IDfSysObject) idfSession.getObject(new DfId(r_object_id));
			s_DocExt		= idfObj.getString("u_file_ext");
			s_SecLevel		= idfObj.getString("u_sec_level");
			
			//============================================================
			// [1]. DRM 복호화
			//============================================================
			// 해당하는 형식이 아닐경우 drm실행 X. 반출함일경우도 false로 바꿈
			File file 							= null;
			
			Map<String, String> formatChkMap 	= null; // drm적용 대상 확장자
			formatChkMap 						= codeService.getDrmFormatCodeMap();
			
			// 파일 Stream 정보
			File fileTemp = new File(drmDir + File.separator + UUID.randomUUID());
			FileUtils.forceMkdirParent(fileTemp);
			FileUtils.copyInputStreamToFile(inputStream, fileTemp);
			
			// 파일 복호화시 사용할 파일명
			String sFileUUIDName = UUID.randomUUID().toString() + "." + s_DocExt;	// 예 ) a74eb09d30964059bb86b2536f741509.xlsx, sFileUUIDName: 5e1bfa25-ea30-4443-9e65-86bf74d94392.txt
			log.info("[doCheckIn] sFileUUIDName : " + sFileUUIDName);
			
			formatChkMap = codeService.getDrmFormatCodeMap();
	    	if (formatChkMap.containsKey(s_DocExt.toUpperCase())) {
				if (drmService.check(new FileInputStream(fileTemp), sFileUUIDName, fileTemp.length() )) {
					file 	= drmService.decrypt(new FileInputStream(fileTemp), sFileUUIDName, fileTemp.length() );
					doDRM 	= true;
		    	 }
	    	}
	    	
	    	// DRM 사용안할경우 처리함.
			if(!doDRM)
			{
				file = fileTemp;
				
				// 암호화 하지 않음
				//file = new File(drmDir + File.separator + UUID.randomUUID());
				//FileUtils.forceMkdirParent(file);
				//FileUtils.copyInputStreamToFile(inputStream, file);
			}

			// 파일명, 경로
			sWasTempName	= file.getAbsolutePath();
			
			
			//============================================================
			// [2]. 문서 정보 체크
			//		선택된 문서 존재/최신버전 여부 체크
			//
			// 	1. 호출된 OBJECT_ID 가 최신 ID 인지 여부 파악 필요 ( 과거 버전 편집 or 최신버전 편집 구별 필요함 )
			// 	2. 사용자 정책 조회필요 ( 부서별 정책임 ) : 버전업 , 현재버전
			// 	3. 파일변경 처리 필요 ( 버전업 or 현재 버전 처리 필요 )
			//============================================================
			r_object_id 	= DCTMUtils.checkNullStringByObj(r_object_id);
			sCurrentObjId	= r_object_id;
			
			// 문서 존재 여부 체크 ( false 는 파일 있을경우 체크 )
			if (!DCTMUtils.isValidObjectByID(idfSession, r_object_id, false)) {
	
				rtnJsonObject.put("return_code", 	"-1");
				rtnJsonObject.put("return_msg", 	"문서가 없거나 최신 문서 아닙니다.");
				return rtnJsonObject;

			}
			
			// 최신 OBJECT 가 아닐경우 최신으로 변경 ( Checkout 은 최신 ID 에서 처리됨 )
			if(!idfObj.getBoolean("i_has_folder")){
				
				bIsCurrent		= false;	// 최신 ID 여부
				
				// 최신 Obj 변경
				sChronicleId	= idfObj.getChronicleId().toString();
				sCurrentObjId	= DCTMUtils.getCurrentObjectID(idfSession, sChronicleId);
				idfObj 			= (IDfSysObject) idfSession.getObject(new DfId(sCurrentObjId));
				
			}
			
			
			//============================================================
			// [3]. Validation 체크
			// 		트레이 버전업, 트레이 현재버전 쓰기, 일반
			//
			//
			//		추가기능 버전업   : LockOwner 또는 편집중이 아닌 상태인경우만 처리
			// 		추가기능 오픈    : LockOwner 인 경우만 편집 처리.
			// 		1. 현재 편집중 인가??  편집 완료인가 여부 파악 필요.
			// 		2. 권한 확인 파악 필요??
			// 		3. 문서 버전업만 처리 필요.
			//============================================================
			boolean bIsCheckedOutUser = idfObj.isCheckedOutBy(idfSession.getLoginUserName());
			boolean bIsCheckOut		  = idfObj.isCheckedOut();
			
			if(sTrayProcessGubun.equals("V")) 
			{

				// 실제 편집 완료 할때는 사용안함  ==> checkin 하기전 호출함 
				/*
				// check : 0 	: 정상 1: 오류
				// return_code 	: 0 정상 : -1 에러발생 
				// return_msg	: MSG 처리
				JSONObject jsonCheck =  this.isCheckOutVersion(user_id, sCurrentObjId);
				String sCheck 		= (String) jsonCheck.get("check");
				String sCheckMsg 	= (String) jsonCheck.get("return_msg");
				
				if(sCheck.equals("1")) 
				{
					rtnJsonObject.put("return_code", 	"-1");
					rtnJsonObject.put("return_msg", 	sCheckMsg);
					return rtnJsonObject;
				}
				*/
				
			}
			else if(sTrayProcessGubun.equals("W")) 
			{
				
				// 실제 편집 완료 할때는 사용안함  ==> checkin 하기전 호출함 
				/*
				
				// 현재버전 쓰기
				// isOverWriteCheck 와 같음
				JSONObject jsonCheck =  this.isCheckOutOverwrite(user_id, sCurrentObjId);
				String sCheck 		= (String) jsonCheck.get("check");
				String sCheckMsg 	= (String) jsonCheck.get("return_msg");
				
				if(sCheck.equals("1")) 
				{
					rtnJsonObject.put("return_code", 	"-1");
					rtnJsonObject.put("return_msg", 	sCheckMsg);
					return rtnJsonObject;
				}
				*/
				
			}
			else if(sTrayProcessGubun.equals("N")) 
			{
				// 다른이름으로 저장 TEMP 이후 호출됨.
				// 특정 처리 안함.
			}
			else
			{
				

				
				// 문서함 정보
				String sDocCabinetCode	= idfObj.getString("u_cabinet_code");
				String sDocComOrgId		= gwDeptService.selectComCodeByCabinetCode(sDocCabinetCode);
				String sDocDeptOrgId 	= gwDeptService.selectOrgIdByCabinetcode( sDocCabinetCode );
				
				// 편집중 중간 저장 여부 구별  .getDeptCabinetcode() 에서 수정함.
				CodeFilterDto codeFilterDto = CodeFilterDto.builder()
						.uCodeType(CodeType.CONFIG_MID_SAVE_DEPT.getValue())
						.uCodeVal1(sDocComOrgId)
						.uCodeVal2(sDocDeptOrgId)
						.build();
				List<Code> codeList = codeDao.selectList(codeFilterDto);
				if (codeList != null && codeList.size() > 0)
				{
					bIsMideSaveDept = true;
				}
				if (bIsMideSaveDept && !idfObj.isCheckedOutBy(idfSession.getLoginUserName()))
				{			
					// 실시간 저장 부서이면서 편집자가 다를경우 ( 관리자 세션으로 편집 완료 처리 함 )
					bIsOtherSaveUser = true;
					idfAdminSession = DCTMUtils.getAdminSession();
				}
				
				
				// 일반
				if(!idfObj.isCheckedOut()) 
				{
					// 일반 편집 처리.
					rtnJsonObject.put("return_code", 	"-1");
					rtnJsonObject.put("return_msg", 	"편집중 문서가 아닙니다.");
					return rtnJsonObject;
					
				}
				else if (!bIsMideSaveDept && !idfObj.isCheckedOutBy(idfSession.getLoginUserName())) 
				//else if (!idfObj.isCheckedOutBy(idfSession.getLoginUserName()))
				{
					
					// 일반 편집 처리.
					rtnJsonObject.put("return_code", 	"-1");
					rtnJsonObject.put("return_msg", 	"선택하신 대상에 대한 편집완료 권한이 없습니다. ( 다른 사용자가 편집중 문서입니다 )");
					return rtnJsonObject;
				}
				
			}
			
			
			//============================================================
			// [4] 사용자 편집 완료 정책 조회
			//		트레이 버전업, 트레이 현재 버전 저장, 일반 편집 완료시 ( 현재버전 NEW 버전 체크 )
			//=========================================================
			//	[1] 트레이 버전업 : 버전업
			//	[2] 트레이 현재버전 : 현재 버전 쓰기
			// 	[3] 일반 : 사용자 정책 조회  ( 버전업 or 현재버전 )  :: 편집완료, 중간 저장 flag 값 parameter 로 추가로 받아야함. :: edms_user_preset
			// 		V:자동 버전업, U:직전 수정자가 다를 경우에만 버전업, O:덮어쓰기
			//
			//
			//	트레이 JOB 리스트 기능 추가 ( V, W, N )
			//============================================================
			sSavePolicy = "NEW_VERSION";	// 기본 정책 버전업.
			
			if(sTrayProcessGubun.equals("V")) 
			{
				sSavePolicy = "NEW_VERSION";
			}
			else if(sTrayProcessGubun.equals("W")) 
			{
				bIsCurrent	= true;	
				r_object_id	= sCurrentObjId;		// 최신 OBJECT_ID 정보로 대체함.  ( 이후 setfile 최신 id 다 생각 하고 r_object_id 활용하기 때문 )
				sSavePolicy = "CURRENT_VERSION";
			}
			else if(sTrayProcessGubun.equals("N")) 
			{
				bIsCurrent	= true;	
				r_object_id	= sCurrentObjId;		// 최신 OBJECT_ID 정보로 대체함.  ( 이후 setfile 최신 id 다 생각 하고 r_object_id 활용하기 때문 )
				sSavePolicy = "CURRENT_VERSION";
			}
			else
			{
				// 현재 사용자에 대한 UserPreset 조회 ( 사용자 기본 값 조회 )
				// 수정 2021-12-01 문서 권한에 맞는 기본값 확인.
				UserPresetFilterDto userPresetFilterDto = UserPresetFilterDto.builder().uUserId(user_id).uSecBaseFlag(true).uSecLevel(s_SecLevel).build();
				List<UserPreset> userPresetList 		= userPresetDao.selectList(userPresetFilterDto);
				
				if(userPresetList.size() > 0)
				{
					String sUserPresetFlag = userPresetList.get(0).getUEditSaveFlag();	
					
					if(sUserPresetFlag.equals("O"))
					{
						sSavePolicy = "CURRENT_VERSION";
					}
					else if(sUserPresetFlag.equals("U"))
					{
						
						String sLastEditor	= idfObj.getString("u_last_editor");	// 최종 파일 수정자 확인.
						if(StringUtils.isNotBlank(sLastEditor))
						{ 
							if(user_id.equals(sLastEditor))
							{
								sSavePolicy = "CURRENT_VERSION";
							}
						}
					}
				}
				
			}
			
			//============================================================
			// [5] 최신버전 현재 버전 저장과  
			//============================================================
			if(bIsCurrent && sSavePolicy.equals("CURRENT_VERSION")) {
			
				// 현재 버전 저장
				bIsVersionUp = false;
				
				System.out.println("doCheckIn CURRENT_VERSION save :: " + r_object_id);
				
				// ( 과거 버전 현재 버전 저장은 이부분 실행 안됨 ) -> 과거 버전 편집시 무조건 버전업 처리됨
				
				
				if(bIsOtherSaveUser)
				{
					
					// 이전 사용자 CHECKOUT 상태를 관리자 권한을 이용하여 cancelCheckout 처리
					// 편집 취소 호출 함.
					// 실시간 저장 다른 사용자 편집 완료 필요할경우.
					// 바로 편집 완료 안됨  ( 편집 취소후 처리함 )
					idfObj 			= (IDfSysObject) idfAdminSession.getObject(new DfId(r_object_id));
					idfObj.cancelCheckout();
					
					
					
					//============================
					// 새로운 사용자로 편집 후 저장 처리
					// 	이후 사용자로 CHECKOUT 처리
					//============================
					idfObj 			= (IDfSysObject) idfSession.getObject(new DfId(r_object_id));
					idfObj.checkout();
					
					
					/* 문서 중간저장. */
					CheckinDocDto 	dto 	= CheckinDocDto.builder().sObjectId(r_object_id).sFilePath(sWasTempName).build();
					IDfSysObject 	idfDoc	= CheckinDocDto.checkinIDfDocument(idfSession, dto);
					
					// 파일수정자 정보 변경
					idfDoc.setString("u_last_editor"	, userSession.getDUserId());
					idfDoc.setTime("u_update_date"		, new DfTime());
					
					// 관리자 권한으로 save
					idfDoc.save();
					
					// 편집자 세션으로 checkout 처리   ( idfDoc 아닌 idfObj )
					idfObj.checkout();

				}
				else
				{
					
					
					// 버전업, 덮어 쓰기일경우는 현재 편집완료된 상태 이기 때문에 checkout 처리 필요함.
					if(( sTrayProcessGubun.equals("V") || sTrayProcessGubun.equals("W") ) && !idfObj.isCheckedOut()) 
					{
						idfObj.checkout();
					}
					
					/* 문서 중간저장. */
					CheckinDocDto 	dto 	= CheckinDocDto.builder().sObjectId(r_object_id).sFilePath(sWasTempName).build();
					IDfSysObject 	idfDoc	= CheckinDocDto.checkinIDfDocument(idfSession, dto);
					
					// 파일수정자 정보 변경
					idfDoc.setString("u_last_editor"	, userSession.getDUserId());
					idfDoc.setTime("u_update_date"		, new DfTime());
					
					// 임시저장일경우 ( TEMP 상태 -> 일반으로 변경 ) 편집 이 아니어서 그대로 save 함.
					if(sTrayProcessGubun.equals("N")) 
					{
						// TEMP 저장후 ( TEMP 파일 교체 만 처리 함 ) 
						idfDoc.setString("u_delete_status", "");
						idfDoc.save();
					}
					else
					{
						
						// 일반저장
						idfDoc.save();
						idfDoc.checkout();
						
						
						
						
						// saveLock 은 속성값 변경 하지 않음.
						// The CHECKOUT action sets the attributes r_lock_owner, r_lock_date, r_lock_machine, i_vstamp. The attributes r_modify_date and r_modifier are not modified.
						// The lock and unlock mechanism don’t modify any attribute of document:
						
						// 편집중이면 편집중 유지,  TEMP 일경우는 그냥 일반 유지됨 saveLock 시
						//idfDoc.saveLock();   // 기존 사용.
						// idfDoc.save();
						// idfDoc.checkout();
						
					}
					
				}
				
				
			}
			else
			{
				
				// 버전업 저장.
				bIsVersionUp = true;
				
				/* 새로운 버전 저장. */
				System.out.println("doCheckIn NEW_VERSION save :: " + r_object_id);
				
				// Version 정책 ( Minor )  Version 정책 확인.
				IDfId 				idf_newId 		= null;
				IDfVersionPolicy 	idf_VerPolcy 	= null; 
				idf_VerPolcy 						= idfObj.getVersionPolicy();
				String s_NextVersion 				= idf_VerPolcy.getNextMajorLabel();
				
				
				// 버전업, 덮어 쓰기일경우는 현재 편집완료된 상태 이기 때문에 checkout 처리 필요함.
				if(( sTrayProcessGubun.equals("V") || sTrayProcessGubun.equals("W") ) && !idfObj.isCheckedOut()) 
				{
					idfObj.checkout();
				}
				
				
				if(bIsOtherSaveUser)
				{
					// 실시간 저장 다른 사용자 편집 완료 필요할경우.
					// 바로 편집 완료 안됨  ( 편집 취소후 처리함 )
					idfObj 			= (IDfSysObject) idfAdminSession.getObject(new DfId(r_object_id));
					idfObj.cancelCheckout();
					
					
					// 이후 사용자로 CHECKOUT 처리
					idfObj 			= (IDfSysObject) idfSession.getObject(new DfId(sCurrentObjId));
					idfObj.checkout();
					
					// Checkin ( 버전 정책 checkout 한 사용자로 새로 조회 필요함 ) 
					idf_VerPolcy 			    = idfObj.getVersionPolicy();
					s_NextVersion 				= idf_VerPolcy.getNextMajorLabel();
					
					idf_newId = idfObj.checkin(false, s_NextVersion + ",CURRENT");	
					
					
					
				}
				else
				{
					// Checkin
					idf_newId = idfObj.checkin(false, s_NextVersion + ",CURRENT");					
				}
				
				
				//=====================
				// 신규버전의 수정일 변경
				//=====================
				idfNewObj = (IDfSysObject) idfSession.getObject(idf_newId);
				idfNewObj.setFile(sWasTempName);

				// New 버전 정보입력.
				idfNewObj.setString("u_last_editor"		, userSession.getDUserId());
				idfNewObj.setTime("u_update_date"		, new DfTime());
				
				// 초기화.
				idfNewObj.setBoolean ("u_takeout_flag"	, false);	// 반출 여부 초기화
				idfNewObj.setBoolean ("u_ver_keep_flag"	, false);	// 버전 유지 여부 초기화
				
				idfNewObj.save();
				
				// 다시 편집중 상태로 변경함    ( 이후 cancelCheckout 에서 최종 처리됨 )
				idfNewObj.checkout();
				
			}
			
			//========================
			// 로그 입력
			//========================
			
			// 속성(버전)
			String sLogRObjectId = "";
			long sLogContentSize = 0;
			if(!bIsVersionUp)
			{
				// 현재 버전 저장
				sLogRObjectId	= idfObj.getObjectId().getId();
				sLogContentSize = idfObj.getContentSize();
				
				sCurrVersion	= idfObj.getVersionLabel(0);
				sAfterVersion	= sCurrVersion;
			}
			else
			{
				// 신규 버전 저장
				sLogRObjectId	= idfNewObj.getObjectId().getId();
				sLogContentSize = idfNewObj.getContentSize();
				
				sCurrVersion	= idfObj.getVersionLabel(0);
				sAfterVersion	= idfNewObj.getVersionLabel(0);
			}
			Double 	dSelVersion		= Double.parseDouble(sCurrVersion);
			int 	iSelVersion		= dSelVersion.intValue();
			String  sVersionValue 	= String.valueOf(iSelVersion); 
			
			Double 	dNewVersion		= Double.parseDouble(sAfterVersion);
			int 	iNewVersion		= dNewVersion.intValue();
			String  sNewVersionValue= String.valueOf(iNewVersion);
			
			
			// 속성(상태)
			String sDocStatus 		= idfObj.getString("u_doc_status");		// L, C
			String sDocSecLevel		= idfObj.getString("u_sec_level");		// T, L
			String sDocCabinetCode  = idfObj.getString("u_cabinet_code");
			String sJobGubun 		= bIsVersionUp == true ? "V" : "W"; 	// 작업구분 ( 수정=[W:덮어쓰기, V:버전생성], 조회=[D:Dbox, L:링크], 첨부=[O:원문, H:HTML], 복호화 반출=[S:자가승인, A:자동스인, F:프리패스]) 
			String sOwnSrDeptOrgId 	= gwDeptService.selectOrgIdByCabinetcode( sDocCabinetCode );
			
				
			LogDoc logDoc = LogDoc.builder()
			          .uJobCode(DocLogItem.ED.getValue())
			          .uDocId(sLogRObjectId)
			          .uDocKey(idfObj.getString("u_doc_key"))
			          .uDocName(idfObj.getTitle().replaceAll("'", "''"))
			          .uDocVersion(sNewVersionValue)
			          .uFileSize(sLogContentSize)
			          .uOwnDeptcode(sOwnSrDeptOrgId)
			          .uActDeptCode(userSession.getUser().getOrgId())
			          .uJobUser(userSession.getUser().getUserId())
			          .uJobUserType("P") //작업자구분(P:개인, D:부서관리자, C:사별관리자, G:그룹관리자, S:시스템)
			          .uDocStatus(sDocStatus)
			          .uSecLevel(sDocSecLevel)
			          .uCabinetCode(sDocCabinetCode)
			          .uJobGubun(sJobGubun)
			          .uUserIp(ip)							// 받아야함.
			          .uAttachSystem("")
			          .uBeforeChangeVal(sVersionValue)		// 버전정보
			          .uAfterChangeVal(sNewVersionValue)	// 버전정보
			          .build();
			      insertLog(logDoc);
			        
			// Return
			sReturnCode 		= "0";			// 정상처리
			sReturnMsg			= "SUCCESS";	// MSG
	
		} catch (Exception e) {
	
			sReturnCode = "-1";
			sReturnMsg = "문서가 없거나 최신 문서 아닙니다.";
			
			log.info("[doCheckIn] ERROR :: " + user_id);
			log.info("[doCheckIn] ERROR :: " + e.toString() + e.getMessage());

		} finally {
	
			
			log.info("doCheckIn finally :: " + r_object_id);
			log.info("doCheckIn finally sWasTempName :: " + sWasTempName);
			
			try {
				if (inputStream != null)
					inputStream.close();
			} catch (IOException e) {
			}
			
			// WAS 업로드파일 삭제
			DCTMUtils.deleteWASContent(sWasTempName);
	
			// 사용자 세션 종료.
			if(userSession != null)
			{
				this.sessionRelease(userSession.getDUserId(), idfSession);
			}
			
			// 관리자 세션 종료.
			if (idfAdminSession != null  && idfAdminSession.isConnected()) {
				idfAdminSession.disconnect();					
			}
			
			
		}
		
		// Agent 리턴 정보.
		rtnJsonObject.put("return_code", 	sReturnCode);	
		rtnJsonObject.put("return_msg", 	sReturnMsg);
		
		log.info("[doCheckin] END : " + rtnJsonObject.toString());
		
		return rtnJsonObject;
	}

	@Override
	public JSONObject docCheckOutCancel(String user_id, String r_object_id, String ip) throws Exception {
		
		log.info("[docCheckOutCancel] START : " + user_id);
		log.info("[docCheckOutCancel] START : " + r_object_id);
		
		//==============================================
		// 편집취소
		//==============================================
		// 호출.
		// 		- doDownload 이후 Agent 에서 편집 호출함
		//		- 더이상 편집 안하는 문서 일경우 호출 되는 방식임 : 실제 편집 취소 아님..
		//
		//		- 편집 완료시 		--> doCheckin 호출후 doCancelCheckOut 호출
		//		- 편집 내용 없을경우 --> doCancelCheckOut 호출됨.
		//
		//	기타 : 로그 입력 안함	
		// 
		// Return
		// 		- return_code
		//		- return_msg
		//==============================================
		
		// 리턴 기본정보
		JSONObject rtnJsonObject 				= new JSONObject();
		String	sReturnCode 					= "0";					// RETURN CODE
		String	sReturnMsg						= "SUCCESS";			// RETURN MESSAGE
		
		// 사용자 정보 조회
		// VUser vUser 			= null;
		UserSession userSession = null;
		
		//Documentum
		IDfSession 		idfSession	= null;
		IDfSysObject 	idf_sObj	= null;
		
		try {
			
			// vUser 		= userSevice.selectOneByUserId( user_id ).orElseThrow(() -> new NotFoundException(VUser.class, "NO USER"));
			userSession = UserSession.builder().dUserId(user_id).docbase(DCTMConstants.DOCBASE).build();
			
			
			idfSession = this.getIdfSession(userSession);
	
			if(r_object_id.length() > 16 )
			{
				r_object_id = r_object_id.substring(0, 16);
			}
			
			// 선택 Obj 확인
			idf_sObj 				= (IDfSysObject)idfSession.getObject(new DfId(r_object_id));
			
			// 최신 ID
			String sChronicleId		= idf_sObj.getChronicleId().toString();
			String sCurrentObjId 	= DCTMUtils.getCurrentObjectID(idfSession, sChronicleId);
			
			// 최신 Object 확인
			idf_sObj 				= (IDfSysObject)idfSession.getObject(new DfId(sCurrentObjId));
			
			// Checkout
			idf_sObj.cancelCheckout();
			
			// Return
			sReturnCode 		= "0";			// 정상처리
			sReturnMsg			= "SUCCESS";	// MSG
			
		} catch (Exception e) {

			sReturnCode = "-1";
			sReturnMsg	= "편집취소에 실패하였습니다.";	// MSG
			
			log.info("[doCheckOutCancel] ERROR :: " + user_id);
			log.info("[doCheckOutCancel] ERROR :: " + e.toString() + e.getMessage());
			
		} finally {

			if(userSession != null)
			{
				this.sessionRelease(userSession.getDUserId(), idfSession);				
			}
		}
		
		// Return 입력
		rtnJsonObject.put("return_code"		, sReturnCode);	
		rtnJsonObject.put("return_msg"		, sReturnMsg);
		
		log.info("[doCheckOutCancel] END : " + rtnJsonObject.toString());
		
		return rtnJsonObject;
		
	}

	@Override
	public JSONObject callGetPolicy(String user_id) throws Exception {

		log.info("[callGetPolicy] START : " + user_id);
		
		//==============================================
		// 정책 조회
		//	Dbox 사용자가 아닐경우 : 첨부 정책 조회 필요 없음.
		//	2021-10-18 일 기준  : 정책 조회 5분 단위로 실행됨
		// 
		//	추가사항 : Dbox 사용자가 아니면 DLP 정책만 조회하고, 첨부 정책은 조회 안함.
		//	추가사항 : USB ( 1. 회사별, 2. (부서 사용자 기간), 3. USB 권한 결재 기간 ) 조회 필요.
		//==============================================
		// 호출.
		// 		- 로그인시 호출됨 
		// 		- 몇분에 한번씩 호출됨
		// 		- 관리자 화면 사용자 설정 변경시 js 파일에서 호출
		//
		// Return
		//			
		// 		1. USB_CONTROL 		: 부서별 USB 권한 USB 제어 ( 0 : 허용, 1 : 차단, 2 : 읽기전용, 3 : 로그 )
		//										쓰기 + 로그 : 3,  읽기 + 로그 : 5
		// 
		// 		2. SCHEDULE_POLICY	: 사용자별 USB 권한		:: USB 정책이 차단일경우 -> 스케쥴 USB 정책의 기간 동안은 사용한다.
		//			4.1 USE				- 기간반출 사용 여부 (0 : 사용안함 1 : 사용 )
		//			4.2 START_DATE		- 파일 기간 반출 시작일 (연월일)  ex) 2021092810
		//			4.3 END_DATE		- 파일 기간 반출 종료일 (연월일)
		//			4.4 USB_CONTROL		- 스케줄 정책 기간 동안의 USB 제어 정책 USB 제어 ( 0 : 허용, 1 :  차단, 2 : 읽기전용, 3 : 로그 )
		//
		// 		3. CACHING_POLICY	: 사용안함 :: 사용안함.     :: Agent 파일 로컬에 남겨두는 부분.
		//			5.1 USE				- 캐싱 기능 사용 여부 ( 0 : 사용안함 1 : 사용 )
		//			5.2 DATE			- 캐싱 기간 (일 단위)
		//			5.3 SIZE			- 캐싱 용량 (MB 단위)
		//			5.4 NUM_FILE		- 캐싱 파일 개수
		//
		// 		4. ONLINE_URL  		: URL 정책
		//			1.1 ONLINE_NAME		- URL 명
		//			1.2 CONTROL			- 중복시 합산 ( 0 : 사용안함, 1 : 링크, 2 : 암호문서, 4 : 복호화문서)
		//
		// 		5. ONLINE_PROCESS	: PROCESS 정책
		//			2.1 ONLINE_NAME		- 프로세스 명
		//			2.2 CONTROL			- 중복시 합산 ( 0 : 사용안함, 1 : 링크, 2 : 암호문서 4 : 복호화문서 )
		//
		//		- return_code		: RETURN CODE
		//		- return_msg		: RETURN MESSAGE
		//==============================================
		
		// 리턴 기본정보
		JSONObject rtnJsonObject 				= new JSONObject();
		String	sReturnCode 					= "0";
		String	sReturnMsg						= "SUCCESS";
		
		// 리턴 추가정보
		String sUsbControl										= "1";			// 쓰기 + 로그 : 3,  읽기 + 로그 : 5
		List<Map<String, Object>> lReturnAttachPolicyUrl		= new ArrayList<Map<String,Object>>();
		List<Map<String, Object>> lReturnAttachPolicyProcess	= new ArrayList<Map<String,Object>>();
		Map<String, Object> mSchedulePolicy 					= new HashMap<String, Object>();	// 스케쥴러정책 ( 사용자별 USB )
		Map<String, Object> mCachingPolicy 						= new HashMap<String, Object>();	// 사용안함 빈값 리턴.
		
		// 조회용
		boolean	bIsDboxUser = false;												// Dbox 사용자 확인용 ( Dbox 사용자일경우만 첨부창에 첨부 정책 보여줌 )
		List<AttachPolicy> lAttachPolicyUrl		= new ArrayList<AttachPolicy>();	// URL리스트
		List<AttachPolicy> lAttachPolicyProcess	= new ArrayList<AttachPolicy>();	// PROCESS  :: 
		
		try {

			String sUserComCode		= "";
			String sUserDeptCode	= "";

			Optional<VUser>	vuser	= userSevice.selectOneByUserId( user_id );
			
			// Dbox 사용자가 아닐경우 ( 그룹웨어에서 사용자 한번더 조회함 )
			if(vuser.isPresent())
			{
				// Dbox 사용자.
				bIsDboxUser		= true;
				sUserComCode	= vuser.get().getComOrgId();
				sUserDeptCode	= vuser.get().getOrgId();			// 부서코드로 사용함.  vuser.get().getDeptCabinetcode();
			}
			else
			{
				// 그룹웨어 사용자.
				Optional<GwUser> gwUser	= userSevice.selectOtherGwUserOneByUserId( user_id );
				
				if(gwUser.isPresent())
				{
					sUserComCode	= gwUser.get().getComOrgId();
					sUserDeptCode	= gwUser.get().getOrgId();		// 부서코드로 사용함.  gwUser.get().getDeptCabinetcode();					
				}
			}
			
			//====================================================================================
			// 1. USB 회사 정책 조회.
			//====================================================================================
			CodeFilterDto 		filter = CodeFilterDto.builder().uCodeType("CONFIG_USB_BASE_POLICY").uCodeVal1(sUserComCode).build();
			List<CodeDetailDto> result = codeService.getCodeList(filter);
			
			if(result.stream().findFirst().isPresent())
			{
				String sCodeValue2 = result.stream().findFirst().get().getUCodeVal2();  // RW, RO
				
				if(sCodeValue2.equals("RW"))
				{
					sUsbControl = "3";
				}
				if(sCodeValue2.equals("RO"))
				{
					sUsbControl = "5";
				}
			}

			//====================================================================================
			// 2. 개인, 부서별 USB_CONTROL 정책
			//====================================================================================
			
			// 기본 정책.
			String sScheduleUse					= "0";  // 기간반출 사용 여부 			0 : 사용안함		1 : 사용
			String sScheduleStartDate			= "";   // 파일 기간 반출 시작일 (연월일)
			String sScheduleEndDate				= "";   // 파일 기간 반출 종료일 (연월일)
			String sScheduleUsbControl			= "0";  // USB 제어 	 				0 : 허용, 1 :  차단, 2 : 읽기전용, 3 : 로그
			
			Optional<UsbPolicy> userUsbPolicy = agentDao.selectUserDeptUsbPolicy(user_id, sUserDeptCode);

			if(userUsbPolicy.isPresent())
			{
				
				sScheduleUse 				= "1";
				sScheduleStartDate			= userUsbPolicy.get().getU_start_date().format(DateTimeFormatter.ofPattern("yyyyMMddHHmm"));  	// 파일 기간 반출 시작일 (연월일)   202201201530
				sScheduleEndDate			= userUsbPolicy.get().getU_end_date().format(DateTimeFormatter.ofPattern("yyyyMMddHHmm"));		// 파일 기간 반출 종료일 (연월일)
				
				// USB 제어  0 : 허용, 1 :  차단, 2 : 읽기전용, 3 : 로그	
				String userPolicy = userUsbPolicy.get().getU_policy();
				if(userPolicy.equals("RW"))
				{
					sScheduleUsbControl = "3";
				}
				if(userPolicy.equals("RO"))
				{
					sScheduleUsbControl = "5";
				}
			
			}
				
			// USB 정책이 차단일경우 -> 스케쥴 USB 정책의 기간 동안은 사용한다.
			mSchedulePolicy.put("USE"			, sScheduleUse);			// "1";
			mSchedulePolicy.put("START_DATE"	, sScheduleStartDate);		// "2021092810";
			mSchedulePolicy.put("END_DATE"		, sScheduleEndDate);		// "2021092815";
			mSchedulePolicy.put("USB_CONTROL"	, sScheduleUsbControl);		// "1";
			
			
			
			//====================================================================================
			// 3. 캐싱 정책
			// 	CONFIG_DOC_HANDLE_LIMIT	문서 처리 제한 값	ITG	TEMREG		: PC임시공간			: 150
			// 	CONFIG_DOC_HANDLE_LIMIT	문서 처리 제한 값	ITG	TEMSIZE		: PC임시공간 제한 용량	: 500
			// 	CONFIG_DOC_HANDLE_LIMIT	문서 처리 제한 값	ITG	TEMTERM		: PC임시공간 보관 기간	: 7
			//====================================================================================
			
			// 기본값
			mCachingPolicy.put("USE"			, "0");		// 캐싱 기능 사용 여부 	0 		: 사용안함, 1 : 사용
			mCachingPolicy.put("DATE"			, "0");		// 캐싱 기간 (일 단위)   2		:: 2일 지나면 삭제
			mCachingPolicy.put("SIZE"			, "0");		// 캐싱 용량 (MB 단위)	1024	:: 용량 넘어서면 이전 파일 삭제
			mCachingPolicy.put("NUM_FILE"		, "0");		// 캐싱 파일 개수		10		:: 11개 째는 가장 오래된 파일 삭제
			
			// 캐싱 조회
			CodeFilterDto 		cachingRegFilter	= CodeFilterDto.builder().uCodeType("CONFIG_DOC_HANDLE_LIMIT").uCodeVal1(sUserComCode).uCodeVal2("TEMREG").build();
			CodeFilterDto 		cachingSizeFilter	= CodeFilterDto.builder().uCodeType("CONFIG_DOC_HANDLE_LIMIT").uCodeVal1(sUserComCode).uCodeVal2("TEMSIZE").build();
			CodeFilterDto 		cachingTermFilter	= CodeFilterDto.builder().uCodeType("CONFIG_DOC_HANDLE_LIMIT").uCodeVal1(sUserComCode).uCodeVal2("TEMTERM").build();
			
			List<CodeDetailDto> resultRegCaching 	= codeService.getCodeList(cachingRegFilter);
			List<CodeDetailDto> resultSizeCaching 	= codeService.getCodeList(cachingSizeFilter);
			List<CodeDetailDto> resultTermCaching	= codeService.getCodeList(cachingTermFilter);
			
			if(resultRegCaching.stream().findFirst().isPresent() 
					&& resultSizeCaching.stream().findFirst().isPresent()
					&& resultTermCaching.stream().findFirst().isPresent())
			{
				
				String sCachingRegValue 	= resultRegCaching.stream().findFirst().get().getUCodeVal3();	
				String sCachingSizeValue 	= resultSizeCaching.stream().findFirst().get().getUCodeVal3();
				String sCachingTermValue 	= resultTermCaching.stream().findFirst().get().getUCodeVal3();
				
				mCachingPolicy.put("USE"			, "1");						// 캐싱 기능 사용 여부 	0 		: 사용안함, 1 : 사용
				mCachingPolicy.put("DATE"			, sCachingTermValue);		// 캐싱 기간 (일 단위)   2		:: 2일 지나면 삭제
				mCachingPolicy.put("SIZE"			, sCachingSizeValue);		// 캐싱 용량 (MB 단위)	1024	:: 용량 넘어서면 이전 파일 삭제
				mCachingPolicy.put("NUM_FILE"		, sCachingRegValue);		// 캐싱 파일 개수		10		:: 11개 째는 가장 오래된 파일 삭제
				
			}
			
			//====================================================================================
			// 4. Dbox 사용자일경우만 첨부정책 조회함.
			//====================================================================================
			if(bIsDboxUser)
			{
				// 2. URL_ATTACH 조회
				lAttachPolicyUrl		= attachPolicyDao.selectAllAgentPolicy(user_id);
				lReturnAttachPolicyUrl 	= AttachPolicy.toMapList(lAttachPolicyUrl);
				
				// 3. PROCESS_ATTACH 조회
				lAttachPolicyProcess 		= attachPolicyDao.selectAllAgentPolicy(user_id);
				lReturnAttachPolicyProcess	= AttachPolicy.toMapList(lAttachPolicyProcess);
				
			}
			
			
			// Return
			sReturnCode 		= "0";			// 정상처리
			sReturnMsg			= "SUCCESS";	// MSG
			
		} catch (Exception e) {
			
			sReturnCode = "-1";
			sReturnMsg	= "정책조회에 실패하였습니다.";
			
			log.info("[callGetPolicy] ERROR :: " + user_id);
			log.info("[callGetPolicy] ERROR :: " + e.toString() + e.getMessage());
			
		} finally {
			
		}
		
		// Return 입력
		rtnJsonObject.put("USB_CONTROL"		, sUsbControl);		
		rtnJsonObject.put("ONLINE_URL"		, lReturnAttachPolicyUrl);
		rtnJsonObject.put("ONLINE_PROCESS"	, lReturnAttachPolicyProcess);
		rtnJsonObject.put("SCHEDULE_POLICY"	, mSchedulePolicy);
		rtnJsonObject.put("CACHING_POLICY"	, mCachingPolicy);
		
		rtnJsonObject.put("return_code"		, sReturnCode);	
		rtnJsonObject.put("return_msg"		, sReturnMsg);
		
		log.info("[callGetPolicy] END :: " + user_id);
		// log.info("[callGetPolicy] END :: " + rtnJsonObject.toString());
		
		return rtnJsonObject;
	}

	@Override
	public JSONObject sendLog(String user_id, String ip, String log_type, InputStream inputStream, String file_size,
			String date_time) throws Exception {

		log.info("[sendLogFile] START 			:: " + user_id);
		log.info("[sendLogFile] 자산 user_id 		:: " + user_id);
		log.info("[sendLogFile] 자산 ip 			:: " + ip);
		log.info("[sendLogFile] 자산 log_type 	:: " + log_type);
		log.info("[sendLogFile] 자산 file_size 	:: " + file_size);
		log.info("[sendLogFile] 자산 date_time 	:: " + date_time);
		
		//==============================================
		// 자산이력 입력
		//==============================================
		// 호출.
		// 		- USB 사용시 로그 정보 입력
		//			입력 : u_file_name
		//					u_file_ext
		//					u_file_size
		//					u_job_user
		//					u_dept_code
		//					u_com_code
		//					u_job_date
		//					u_user_ip
		// Return
		// 		- return_code
		//		- return_msg
		//==============================================
		
		JSONObject 	rtnJsonObject 	= new JSONObject();
		String		sReturnCode		= "0";
		String		sReturnMsg		= "";
		
		
		InputStreamReader 	isr = null; // new InputStreamReader(inputStream);
		BufferedReader 		br 	= null; // new BufferedReader(isr);
		
		try {
			
			String sUserComCode		= "";
			String sUserDeptCode	= "";
			
			// 사용자 정보 조회 ( 사용자가 없을경우도 있어서 한번더 검색하고 파라미터 user_id 바로 입력 )
			//VUser vUser = userSevice.selectOneByUserId( user_id ).orElseThrow(() -> new NotFoundException(VUser.class, user_id));
			Optional<VUser>	vUser	= userSevice.selectOneByUserId( user_id );
			
			// Dbox 사용자가 아닐경우 ( 그룹웨어에서 사용자 한번더 조회함 )
			if(vUser.isPresent())
			{
				// Dbox 사용자.
				sUserComCode	= vUser.get().getComOrgId();
				sUserDeptCode	= vUser.get().getOrgId();			// 부서코드로 사용함.  vuser.get().getDeptCabinetcode();
			}
			else
			{
				// 그룹웨어 사용자.
				Optional<GwUser> gwUser	= userSevice.selectOtherGwUserOneByUserId( user_id );
				
				if(gwUser.isPresent())
				{
					sUserComCode	= gwUser.get().getComOrgId();
					sUserDeptCode	= gwUser.get().getOrgId();		// 부서코드로 사용함.  gwUser.get().getDeptCabinetcode();					
				}
			}
			
			
			
			
			isr = new InputStreamReader(inputStream);
			br 	= new BufferedReader(isr);
			
			// LocalDateTime now 			= LocalDateTime.now();
			// LocalDateTime localJobDate	= LocalDateTime.of(now.getYear(), now.getMonth(), now.getDayOfMonth(), 0, 0, 0);
			
			// 파일별 JOB 시간을 모두 같게 처리함
			LocalDateTime localJobDate	= LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
			
			// 정보 리스트 담기
			List<LogPcDocs> 	liLogPcDoc 		= new ArrayList<LogPcDocs>();
			
			DateTimeFormatter format = DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss yyyy", Locale.US);
			
			//===================================================
			//	-- 아직 어떻게 전송 되는 지 확인 안됨.... 파악 필요함.
			//===================================================
			String line = null;
			while( (line = br.readLine() ) != null)
			{
				
				String[] asLine 		= line.split("[|]");
				String sFileName 		= asLine[2].trim();
				String sAccessDate		= asLine[3].trim();
				String sModifyDate		= asLine[4].trim();
				String sCreateDate		= asLine[5].trim();
				String sFileSize		= asLine[6].trim();
				
				// sFileName :: "c:\Users\byung\Desktop\현재 작업 폴더\01.Project\동국\20210830회의\D`Box_&_IFNS_PKG_인터페이스정의서_v1.6.xlsx"
				//	 sAccessDate :: Access(Tue Aug 31 15:01:50 2021)
				//	 sModifyDate :: Modification(Tue Aug 31 15:01:50 2021)
				//	 sCreateDate :: Creation(Tue Aug 31 09:06:53 2021)
				//	 sFileSize :: Size(73 KB)

				/*
				System.out.println("line       :: " + line);
				System.out.println(" sFileName :: " + sFileName);
				System.out.println(" sAccessDate :: " + sAccessDate);
				System.out.println(" sModifyDate :: " + sModifyDate);
				System.out.println(" sCreateDate :: " + sCreateDate);
				System.out.println(" sFileSize :: " + sFileSize);
				*/
				
				sFileName 				 = sFileName.substring( sFileName.indexOf("\"") + 1, 	sFileName.lastIndexOf("\"")   );
				String sAccessDateFormat = sAccessDate.substring( sAccessDate.indexOf("(") + 1, sAccessDate.lastIndexOf(")")   );
				String sModifyDateFormat = sModifyDate.substring( sModifyDate.indexOf("(") + 1, sModifyDate.lastIndexOf(")")   );
				String sCreateDateFormat = sCreateDate.substring( sCreateDate.indexOf("(") + 1, sCreateDate.lastIndexOf(")")   );
				sFileSize   			 = sFileSize.substring( sFileSize.indexOf("Size(") + 5, sFileSize.lastIndexOf(" KB)")   );
				
				// TODO 날짜 포맷 안맞으면 특정 날짜로 바꾸자.
				LocalDateTime ldtAccessDate = LocalDateTime.parse(sAccessDateFormat, format);
				LocalDateTime ldtModifyDate = LocalDateTime.parse(sModifyDateFormat, format);
				LocalDateTime ldtCreateDate = LocalDateTime.parse(sCreateDateFormat, format);
				
				// 로그 입력.
				LogPcDocs logPcDoc = LogPcDocs.builder()
						.uUserId(user_id)
						.uComCode(sUserComCode)
						.uDeptCode(sUserDeptCode)
						.uFileName(sFileName)
						.uFileSize(Integer.parseInt(sFileSize))
						.uCreateDate(ldtCreateDate)
						.uModifyDate(ldtModifyDate)
						.uAccessDate(ldtAccessDate)
						.uLogDate(localJobDate)
						.build();
	
					liLogPcDoc.add(logPcDoc);
				
			}

			// 기존 자산 삭제처리.
			this.deleteLogPcDocsList(user_id);
			
			// 자산정보가 있을경우 Map 에 리스트 등록후 일괄 insert 문 실행함.
 			if(liLogPcDoc.size() > 0)
			{
 				
 				//==========================================
 				// 3천건 정상 등록됨, 4천건 Error 발생함.
 				//==========================================
 				// 전체 입력시 ( 이부분은 오류 발생 해서 나눠서 
 				// this.insertLogPcDocsList(liLogPcDoc);
 				//==========================================
 				
 				// 최종 :: 2000 씩 나눠서 실행.
				List<List<LogPcDocs>> lists = Lists.partition(liLogPcDoc, 2000);
				
				for (List<LogPcDocs> list : lists) {
					this.insertLogPcDocsList(list);					
				}
				
			}
			
			// Return
			sReturnCode 		= "0";			// 정상처리
			sReturnMsg			= "SUCCESS";	// MSG

		} catch (Exception e) {

			sReturnCode = "-1";
			sReturnMsg	= "FAIL";
			
			log.info("[sendLogFile] ERROR :: " + user_id);
			log.info("[sendLogFile] ERROR :: " + e.toString() + e.getMessage());
			
		} finally {
			
			// Close
			try {
				if (isr != null)
					isr.close();
			} catch (IOException e) {
			}

			try {
				if (br != null)
					br.close();
			} catch (IOException e) {
			}

			try {
				if (inputStream != null)
					inputStream.close();
			} catch (IOException e) {
			}
			
		}

		// Return
		rtnJsonObject.put("return_code"	, sReturnCode);
		rtnJsonObject.put("return_msg"	, sReturnMsg);
		
		log.info("[sendLogFile] END :: " + user_id);
		log.info("[sendLogFile] END :: " + rtnJsonObject.toString());
		
		return rtnJsonObject;
		
		
	}

	@Override
	public JSONObject sendLog(String user_id, String ip, String log_type, String dest_path, String src_path,
			String action, String file_size, String date_time) throws Exception {
		
		log.info("[sendLogUsb]  USB user_id 	:: " + user_id);
		log.info("[sendLogUsb]  USB ip 			:: " + ip);
		log.info("[sendLogUsb]  USB log_type 	:: " + log_type);
		log.info("[sendLogUsb]  USB dest_path 	:: " + dest_path);
		log.info("[sendLogUsb]  USB src_path	:: " + src_path);
		log.info("[sendLogUsb]  USB action 		:: " + action);
		log.info("[sendLogUsb]  USB file_size 	:: " + file_size);
		log.info("[sendLogUsb]  USB date_time 	:: " + date_time);
		
		//==============================================
		// USB 로그 입력
		//==============================================
		// 호출.
		// 		- USB 사용시 로그 정보 입력
		//			입력 : u_file_name
		//					u_file_ext
		//					u_file_size
		//					u_job_user
		//					u_dept_code
		//					u_com_code
		//					u_job_date
		//					u_user_ip
		// Return
		// 		- return_code
		//		- return_msg
		//
		//	2022-02-11 확장자 없을경우 등록 안함. 				ex ) E:\\23.수출 컨테이너 보관\\인터지스\\A9E43000
		//	2022-02-11 확장자 형식 16자리 이상일경우 등록 안함    ex ) 수출 컨테이너 보관\\인터지스\\A9E43000     ( 로컬에서는 "" 이지만 서버에서는 . 이후 모두 붙음 )
		// 
		//	2022-02-11 최종 제외 형식
		//		1. 확장자가 없는 경우 "" 제외		:: \Device\Harddisk1\DR2	
		//		2. 확장자 5자리 이상 제외 		:: E:\23.수출 컨테이너 보관\인터지스\ -> 수출 컨테이너 보관\인터지스\
		//		3. 마지막에 \ 문자가 있는 제외 	:: E:\23.수출 컨테이너 보관\인터지스\
		//==============================================
		
		JSONObject 	rtnJsonObject 	= new JSONObject();
		String		sReturnCode		= "0";
		String		sReturnMsg		= "";

		try {

			String sUserComCode		= "";
			String sUserDeptCode	= "";
			
			// 사용자 정보 조회 ( 사용자가 없을경우도 있어서 한번더 검색하고 파라미터 user_id 바로 입력 )
			//VUser vUser = userSevice.selectOneByUserId( user_id ).orElseThrow(() -> new NotFoundException(VUser.class, user_id));
			Optional<VUser>	vUser	= userSevice.selectOneByUserId( user_id );
			
			// Dbox 사용자가 아닐경우 ( 그룹웨어에서 사용자 한번더 조회함 )
			if(vUser.isPresent())
			{
				// Dbox 사용자.
				sUserComCode	= vUser.get().getComOrgId();
				sUserDeptCode	= vUser.get().getOrgId();			// 부서코드로 사용함.  vuser.get().getDeptCabinetcode();
			}
			else
			{
				// 그룹웨어 사용자.
				Optional<GwUser> gwUser	= userSevice.selectOtherGwUserOneByUserId( user_id );
				
				if(gwUser.isPresent())
				{
					sUserComCode	= gwUser.get().getComOrgId();
					sUserDeptCode	= gwUser.get().getOrgId();		// 부서코드로 사용함.  gwUser.get().getDeptCabinetcode();					
				}
			}
			
			
			// 이전 형식 ( 로컬에서는 확장자 "" 이지만 서버에서는   수출 컨테이너 보관\\인터지스\\A9E43000 로 나타남 )
			// Path targetPath 	= Paths.get(dest_path);
			// String sFileExt		= com.google.common.io.Files.getFileExtension(targetPath.getFileName().toString());
			
			// 파일명. 처리
			String sFileExt = FilenameUtils.getExtension(dest_path);
			
			// 확장자가 있고, size 16자리 이상 아닐경우 등록 처리함.
			if( StringUtils.isBlank(sFileExt) )
			{
				sReturnCode 		= "0";
				sReturnMsg			= "EXT BLANK";
			}
			else if(sFileExt.length() >= 5 )
			{
				sReturnCode 		= "0";
				sReturnMsg			= "EXT value to long";
			}
			else if(dest_path != null && dest_path.endsWith("\\") )
			{
				sReturnCode 		= "0";
				sReturnMsg			= "EXT contains \\ ";
			}
			else
			{
				// 로그 형식 				
				LogUsb logUsb = LogUsb.builder().uFileName(dest_path)
						.uFileExt(sFileExt)
						.uFileSize(Integer.parseInt(file_size))
						.uJobUser(user_id)
						.uComCode(sUserComCode)
						.uDeptCode(sUserDeptCode)
						.uUserIp(ip)
						.build();
				
				// 로그입력
				this.insertLog(logUsb);
				
				
				// Return
				sReturnCode 		= "0";			// 정상처리
				sReturnMsg			= "SUCCESS";	// MSG
			}
			
			
			
			
		} catch (Exception e) {
			
			// 실패
			sReturnCode		= "-1";
			sReturnMsg		= "FAIL";
			
			log.info("[sendLogUsb] ERROR :: " + user_id);
			log.info("[sendLogUsb] ERROR :: " + e.toString() + e.getMessage());
		}

		// Return
		rtnJsonObject.put("return_code"	, sReturnCode);
		rtnJsonObject.put("return_msg"	, sReturnMsg);
		
		log.info("[sendLogUsb] END :: " + user_id);
		log.info("[sendLogUsb] END :: " + rtnJsonObject.toString());
		
		return rtnJsonObject;
	}
	
	/**
	 *  
	 * html 파일 생성
	 * @param ps_docId  문서 ID
	 * @throws Exception 
	 */
	private String makeHtmlDoc(String ps_objectId ,
								String sViewDocId, String sViewApproveId, 
								String syspath, String sUrlEncodeSysPath,
								String ps_path,String ps_WasFileName, IDfSession idf_Sess ) throws Exception{
		
		//=========================================================
		// 필요할것 같은부분
		//	최종 적으로 DOC_KEY 입력으로 변경됨.
		// 
		// 방식 r_object_id|doc_id|approveid|folderid|category
		//		- r_object_id 	: 문서 ID
		//		- doc_id 		: 문서 KEY ID
		//		- approveid 	: 반출함 r_object_id
		//		- folderid 		: 폴더 ID
		//		- category		: 폴더 CATEGORY
		//=========================================================
		
		FileOutputStream 	fos 		= null;
		StringBuffer 		htmlDoc 	= new StringBuffer();
		String 				s_fileName 	= "";
		String 				s_htmlPath 	= "";
		
		IDfSysObject  idf_Sys = null;

		try {
			
			idf_Sys = (IDfSysObject) idf_Sess.getObject(new DfId(ps_objectId));
			
			// 
			String sChronicleId = idf_Sys.getChronicleId().toString();
			
			String 				s_AppendParameter 	= "?approveid="+sViewApproveId+"&syspath="+sUrlEncodeSysPath;
			
			String linkNewText = linkfileText;
			
			linkNewText	= linkNewText.replaceAll(docKey		, sChronicleId + s_AppendParameter);
			linkNewText	= linkNewText.replace(preservFlag	, idf_Sys.getString("u_preserve_flag")); //보존년한
	        linkNewText	= linkNewText.replace(secLevel		, idf_Sys.getString("u_sec_level")); 		//보안등급
	        
	        htmlDoc.append(linkNewText);
			/*
			 * <meta http-equiv='refresh' content='0.01;url=http://dbox-dev.dongkuk.com:8080/#/download/090004d280007522?approveid=&syspath=eU40UFFmQVJLeVVMYW83by9icDRBMW1HL2pHMnRUT2RkRmxDR2lTYTVIMD0'> 
			 * <metaname="seclevel" content=T> <!-- S극비, T부서한, C사내, G그룹사내 --> 
			 * <metaname="preserveflag" content=0>
			 * <body>
          	 * <button type='button' onclick="location.href='http://dbox-dev.dongkuk.com/#/download/__DOC_ID__'">Link</button>
        	 * </body>
			 */
			

			// 파일명("메일id"_"문서id".html)
			s_fileName =  ps_WasFileName;

			s_htmlPath = ps_path + s_fileName;
			fos = new FileOutputStream(s_htmlPath);
			fos.write(htmlDoc.toString().getBytes());
			fos.close();
			
		} catch (FileNotFoundException fnfe) {
			// log.error(fnfe);
			fnfe.printStackTrace();
		} catch (IOException ie) {
			 //log.error(ie);
			ie.printStackTrace();
		} finally {
			try {
				if (fos != null){
					fos.close();
				}
			}catch (IOException ioe) {
				//log.error(ioe);
				ioe.printStackTrace();
			}
		}
		// html 생성코드 작성
		return s_htmlPath;
	}

	@Override
	public JSONObject saveAs(String user_id, String r_folder_id, String realFileName, String ip) throws Exception {
		
		
		log.info("[saveAs] START : " + user_id);
		log.info("[saveAs] START : " + r_folder_id);
		log.info("[saveAs] START : " + realFileName);
		log.info("[saveAs] START : " + ip);
		
		//==============================================
		// 다른이름으로 저장 TEMP 저장.
		//	- Office 에서 다른 이름 저장시 D'box 폴더 선택시
		//
		// 흐름
		//	다른이름 저장시
		//		saveAs
		//		doCheckIn     ( FLAG 실시간 저장 포함 ) : 파일 업로드
		//		isCheckOut							- 기존 기능들 호출함
		//		cacheCheckFile						- 기존 기능들 호출함
		//		doCheckOut							- 기존 기능들 호출함
		// 
		//	파일 수정 이후
		//		checkout
		//		Office 최종 수정 완료
		//		cancelCheckout
		//==============================================
		// 리턴 기본정보
		//		return_code
		//		return_msg
		//
		//==============================================
		
		JSONObject rtnJsonObject	= new JSONObject();
		String	sReturnCode 		= "0";					// RETURN CODE
		String	sReturnMsg			= "SUCCESS";			// RETURN MESSAGE
		String 	sReturnObjId		= ""; 
		
		VUser 		user 			= null;
		UserSession userSession 	= null;
		IDfSession idfSession 		= null;
		
		String sWasTempName			= "";	// WAS TempFile
		String s_DocExt				= "";	// 파일 확장자.
		boolean	doDRM   	 		= false;
		
		IDfSysObject 	idf_sObj	= null;
		
		String sFolderFullId		= "";
		String sFolderType			= "F";	// D : 부서, P : 프로젝트, R : 연구과제, F 폴더
		
		try {
			
			user 			= userSevice.selectOneByUserId( user_id ).orElseThrow(() -> new NotFoundException(VUser.class, "NO USER"));
			userSession 	= UserSession.builder().dUserId(user_id).docbase(DCTMConstants.DOCBASE).user(user).build();
			
			if(r_folder_id.length() > 16 )
			{
				sFolderFullId 	= r_folder_id.substring(0, 16);
			}
			
			//============================================================
			// [0]. 세션 연결
			//============================================================
			idfSession 		= this.getIdfSession(userSession);
			
			//============================================================
			// [1]. DRM 복호화
			//============================================================
			String sUUID_DRM	= UUID.randomUUID().toString();		// DRM 처리할 경로 ( rankupload 는 파일 서버 전송시 다른 폴더에 있기 때문에 파일명 같아도 됨 )
			String sUUID  		= UUID.randomUUID().toString();		
			String sExt			= com.google.common.io.Files.getFileExtension(realFileName);
			
			File fileTemp 		= new File(drmDir + File.separator + sUUID + "." + sExt);
			FileUtils.forceMkdirParent(fileTemp);
			
			// Stream 없이 TEMP 생성.
			fileTemp = File.createTempFile("temp_", ".tmp", new File(drmDir));
            
			// 파일명, 경로
			sWasTempName	= fileTemp.getAbsolutePath();
			
			
			//============================================================
			// 폴더 정보 조회
			//	1. 부서 ROOT	(D)	
			//	2. 프로젝트 ROOT (P)		: p00061_POW
			//	3. 연구과제 ROOT (R)		: r00002_ROW
			//	4. 부서 폴더, 프로젝트폴더, 공유협업 폴더 ( 001_DEPT_FOLDER, 002_PROJECT_FOLDER, 003_SHARE_FOLDER )
			//	5. 그외 다른 메뉴 및 폴더 ( 등록 가능 한지 체크함 )
			//============================================================
			String[] asCategory		= r_folder_id.split("_");
			String   sCategoryId	= "";
			String 	 sCategory		= "";
			if(asCategory.length == 2)
			{		
				sCategoryId		= asCategory[0];
				sCategory 		= asCategory[1];
			}
			
			if(r_folder_id.equals("DEPT") || r_folder_id.equals("dept_doc"))
			{
				sFolderFullId	= user.getOrgId();
				sFolderType		= "D";
			}
			else if(sCategory.equals("POW") || sCategory.equals("PIN"))
			{
				sFolderFullId	= sCategoryId;
				sFolderType		= "P";
			}
			else if(sCategory.equals("ROW") || sCategory.equals("RIN"))
			{
				sFolderFullId	= sCategoryId;
				sFolderType		= "R";
			}
			else
			{
				
				// 일반 폴더일경우 체크.
				
				//============================================================
				// [3]. Validation 체크
				//============================================================
				JSONObject jsonCheck =  this.isSaveAsNewCheck(user_id, realFileName, r_folder_id);
				String sCheckCode	= (String) jsonCheck.get("return_code");
				String sCheckMsg 	= (String) jsonCheck.get("return_msg");
				
				if(sCheckCode.equals("-1")) 
				{
					rtnJsonObject.put("return_code", 	"-1");
					rtnJsonObject.put("return_msg", 	sCheckMsg);
					return rtnJsonObject;
				}
			}
			
			
			
			
			//============================================================
			// [2]. 파일 등록 
			//		KUploadController ->   raonkhandler(/kupload/raonkhandler)
			//			-> dataService.uploadDoc(userSession, dto, aFile, ip, null);
			//
			//============================================================
			
			String strOriginalFileName 			= realFileName; 				// "테스트 2K.xlsx"; 																							// eventVo.getOriginalFileName(); //Original File Name
	        String strNewFileLocation 			= fileTemp.getAbsolutePath();	// "C:/workspace/dbox-serv/src/main/webapp/raonkuploaddata/2022/01/61a46e992cfd44ebbea77eea077ee442.xlsx"; 	// eventVo.getNewFileLocation(); //NewFileLocation Value
	        String strResponseFileServerPath 	= ""; 							// js : AttachedKUploadFile 에서 사용안함으로 보임 "/raonkuploaddata/2022/01/61a46e992cfd44ebbea77eea077ee442.xlsx"; 										// eventVo.getResponseFileServerPath(); //ResponseFileServerPath Value
	        String strFileIndex 				= "0z"; 						// 1건 1건씩 하기때문에 마지막 0z 함																									//	eventVo.getFileIndex(); //FileIndex Value - 마지막 파일은 index 뒤에 z가 붙습니다.
	        String strGuid 						= sUUID_DRM;					// "61a46e992cfd44ebbea77eea077ee442"; 	-- 5dd5e159-8e5c-4595-92e4-6600b4a962d6																// eventVo.getGuid(); //Guid
	        
	        String prType 						= null ; 						// Objects.isNull(eventVo.getUpload().getParameterValue("prType"))?null:eventVo.getUpload().getParameterValue("prType")[0];
	        
	        // 부서면 D, 폴더면 F,  프로젝트 ROOT P 및 1레벨 안됨 ,  프로젝트 폴더 F,
	        // 반출함 안됨
	        // 공유협업 1레벨 안됨 , 폴더 가능 :F
	        String hamType 						= sFolderType; 							// Objects.isNull(prType)? eventVo.getUpload().getParameterValue("hamType")[0] : "D";
	        
	        // 부서일때 : UNC50014030,  폴더면 0000~~
	        String upObjectId 					= sFolderFullId;				// 우선 문서 id 대신 처리함 "000030398000433a"; 			// Objects.isNull(prType)? eventVo.getUpload().getParameterValue("upObjectId")[0]: userSession.getUser().getOrgId();
	        String uploadFlag 					= "S"; 							// 건너띄기:S, 버전갱신:V, 복사본 추가:C		// eventVo.getUpload().getParameterValue("uploadFlag")[0];

	        // 파라미터 받아서 우선 주석 :: path, ip
	        // (사용안함) String path 						= ""; // eventVo.getObjectKeyFolderPath();
	        // String ip = "127.0.0.1"; // getClientIp(request);

	        AttachedKUploadFile aFile;
	        aFile = new AttachedKUploadFile(
	        		strNewFileLocation,
	        		strResponseFileServerPath,
	            strOriginalFileName,
	            strFileIndex,
	            strGuid
	            );
	        
	        // TEMP 저장 구분 추가함.
	        UploadDocDto dto 	= UploadDocDto.builder().hamType(hamType).upObjectId(upObjectId).uploadFlag(uploadFlag).prType(prType).deleteStatus(DeleteStatus.TEMP.getValue()).build();
	        sReturnObjId 		= dataService.uploadDoc(userSession, dto, aFile, ip, null);
	        
	        //=====================================
	        // 문서 CHECKOUT ( Agent 에서 또다시 checkout Service 호출해서 여기서 checkout 할필요 없음
	        //=====================================
	        // idf_sObj = (IDfSysObject)idfSession.getObject(new DfId(sCreateDocObjId));
			// idf_sObj.checkout();
	        
			
		} catch (Exception e) {
	
			sReturnCode = "-1";
			sReturnMsg 	= "문서 등록에 실패 하였습니다.";
			
			log.info("[saveAs] ERROR :: " + user_id);
			log.info("[saveAs] ERROR :: " + e.toString() + e.getMessage());
			
			
		} finally {
	
			
			log.info("saveAs finally 				:: " + r_folder_id);
			log.info("saveAs finally sWasTempName 	:: " + sWasTempName);
			
			// WAS 업로드파일 삭제
			DCTMUtils.deleteWASContent(sWasTempName);
	
			if(userSession != null)
			{
				this.sessionRelease(userSession.getDUserId(), idfSession);				
			}
			
		}
		
		// Agent 리턴 정보.
		rtnJsonObject.put("r_object_id"		,	sReturnObjId);
		rtnJsonObject.put("i_chronicle_id"	, 	sReturnObjId);
		rtnJsonObject.put("return_code"		, 	sReturnCode);	
		rtnJsonObject.put("return_msg"		, 	sReturnMsg);
		
		log.info("[saveAs] END : " + rtnJsonObject.toString());
		
		return rtnJsonObject;
	}
	
	
	@Override
	public JSONObject isCheckOutVersion(String user_id, String r_object_id) throws Exception {
		
		log.info("[isCheckOutVersion] START : " + user_id);
		log.info("[isCheckOutVersion] START : " + r_object_id);
		
		//==============================================
		// 버전업 가능 여부 체크
		// 
		//==============================================
		// 호출.
		//		- 트레이 문서 선택후 버전업  ( 버전업 , 덮어쓰기, 복사, 새로 생성 )
		//
		//	r_object_id 형식
		//		편집 : 090004d280006366
		//		보기 : 090004d280006366__testapp_1zK24M3d5Y9KbPY3YbWuZhl7jt6Fg%2F7Cl2vS06FX%2BTU%3D
		//
		//	r_folder_id 형식
		//		ROOT : PFN
		//		폴더  : r_object_id_카테고리_FOLDER
		// 
		// Return
		//		- check			: 0 : 버전업가능, 1 : 버전업불가
		// 		- return_code	: 0 : 정상	 , -1 : 오류
		//		- return_msg	: 리턴 MSG 처리
		//
		//	기타
		//		버전업 안되는 조건
		//			( 편집중, 삭제문서, 권한, CLOSE 문서, 잠금처리 폴더문서 )
		//==============================================
		
		JSONObject rtnJsonObject = new JSONObject();
		
		String 	sReturnCheckOut 	= "0";
		String	sReturnCode 		= "0";	
		String	sReturnMsg			= "SUCCESS";	
		
		String  sFolderId			= "";

		try {
 			
			// 문서 ID
			if(r_object_id.length() > 16 )
			{
				r_object_id = r_object_id.substring(0, 16);
			}
			
			// 선택된 문서 정보
			// 최신 문서 ID 조회후 편집 중 상태 확인
	    	Optional<Doc> optDoc 		= docDao.selectDetailOne(r_object_id, user_id, false);	    	
			if (optDoc.isPresent()) 
			{
				
				Doc 	doc 	= optDoc.get();
				String 	sDocKey = doc.getUDocKey();
				
				// 최신문서 정보 조회 ( DOC_KEY 를 이용 )
				Optional<Doc> optDocCurrent = docDao.selectDetailOne(sDocKey, user_id, true);
				if (optDocCurrent.isPresent()) 
				{
					Doc 	docCurrent 	= optDocCurrent.get();
						
					//=================
					// 문서 정보 확인
					//=================
					String sCheckOutStatus  	= "0";
					String sCheckOutUserCheck 	= "0";	// 0 : 같은사용자, 1: 다른 사용자.
					String sDeleteStatus  		= "0";
					String 	uDocStatus 			= doc.getUDocStatus();
					boolean bIsReadOnly			= false;	// 잠금처리 폴더 확인
					
					int 	maxLevel		= doc.getMaxLevel();
					boolean isLive 			= DocStatus.LIVE.getValue().equals(uDocStatus);
					boolean isClosed 		= DocStatus.CLOSED.getValue().equals(uDocStatus);
					
					if(StringUtils.isNotBlank(docCurrent.getRLockOwner()) )
					{
						sCheckOutStatus = "1";
						
						// 다른 사용자가 편집중인지 체크
						if(!user_id.equals(docCurrent.getRLockOwner()) )
						{
							sCheckOutUserCheck = "1";
						}
						
					}
					
					if(StringUtils.isNotBlank(docCurrent.getUDeleteStatus()))
					{
						sDeleteStatus = "1";
					}
					
					// 잠금처리 폴더 확인
					String sDocFolderId		= doc.getUFolId();
					Optional<Folder> optFolder = folderService.selectOne(sDocFolderId);
					if(optFolder.isPresent()) {
						if( optFolder.get().getUFolStatus().equals("C") ) {//잠금처리된 폴더
							bIsReadOnly = true;
						}
					}
					
					//=================
					// Validation 체크
					//=================
					if(sCheckOutStatus.equals("1") && sCheckOutUserCheck.equals("1")) 
					{
						sReturnCheckOut = "1";
						sReturnMsg		= doc.getTitle() +" 은 편집중 상태입니다";
					}
					else if(sDeleteStatus.equals("1")) 
					{
						sReturnCheckOut = "1";
						sReturnMsg		= doc.getTitle() +" 은 삭제된 상태입니다";
					}
					else if(isLive && maxLevel < GrantedLevels.DELETE.getLevel()) 
					{
						sReturnCheckOut = "1";
						sReturnMsg		= doc.getTitle() +" 버전업 권한이 없습니다.";	// canUpdateDoc 로직 활용
					}
					else if(isClosed) 
					{
						sReturnCheckOut = "1";
						sReturnMsg		= doc.getTitle() +" 완료(CLOSE) 문서입니다.";
					}
					else if(bIsReadOnly)
					{
						sReturnCheckOut = "1";
						sReturnMsg		= doc.getTitle() +" 잠금처리된 폴더 문서입니다.";
					}
					
				}
				else
				{
					sReturnCheckOut = "1";
					sReturnMsg		= "문서 정보가 없습니다.";
				}
			}
			else
			{
				// 문서가 없을경우 중요문서함에서 한번더 확인
				// 중요문서함여부 확인
				// TODO :: 버전 정보 가 있을경우 추가 필요
				Optional<DocImp> optDocImp= docImpService.selectOne(r_object_id);
				if (optDocImp.isPresent()) 
				{
					
					DocImp 	docImp 	= optDocImp.get();
					if(StringUtils.isNotBlank(docImp.getRLockOwner()))
					{
						// sReturnCheck = "1";
					}
				}
				else
				{
					sReturnCheckOut = "1";
					sReturnMsg		= "문서 정보가 없습니다.";
				}
			}
			
			// Return
			sReturnCode 		= "0";			// 정상처리

		} catch (Exception e) {

			sReturnCheckOut = "1";
			sReturnCode 	= "-1";
			sReturnMsg		= "버전업 체크에 실패하였습니다.";

			log.info("[isCheckOutVersion] ERROR :: " + user_id);
			log.info("[isCheckOutVersion] ERROR :: " + e.toString() + e.getMessage());
			
		} finally {

		}

		// Agent 리턴 정보.
		rtnJsonObject.put("check_out"	, sReturnCheckOut);
		rtnJsonObject.put("return_code"	, sReturnCode);
		rtnJsonObject.put("return_msg"	, sReturnMsg);
		
		log.info("[isCheckOutVersion] END : " + rtnJsonObject.toString());
		
		return rtnJsonObject;
	}
	
	public JSONObject isCheckOutOverwrite(String user_id, String r_object_id) throws Exception {
		
		log.info("[isOverWriteCheck] START : " + user_id);
		log.info("[isOverWriteCheck] START : " + r_object_id);
		
		//==============================================
		// 덮어쓰기 가능 여부 체크
		// 
		//==============================================
		// 호출.
		//		- 트레이 문서 선택후 버전업  ( 버전업 , 덮어쓰기, 복사, 새로 생성 )
		//
		//	r_object_id 형식
		//		- 090004d280006366
		//		- 090004d280006366__testapp_1zK24M3d5Y9KbPY3YbWuZhl7jt6Fg%2F7Cl2vS06FX%2BTU%3D
		//
		//	r_folder_id 형식
		//		ROOT : PFN, ... , ...
		//		폴더  : r_object_id_카테고리_FOLDER
		// 
		// Return
		//		- check_out		: 0 : 버전업가능, 1 : 버전업불가   ( Agent 명칭을 isCheckOut 과 같이 check_out 를 사용한다고함 )
		// 		- return_code	: 0 : 정상	 , -1 : 오류
		//		- return_msg	: 리턴 MSG 처리
		//
		//	기타
		//		덮어쓰기 안되는 조건
		//			( 편집중, 삭제문서, 권한, CLOSE 문서, 잠금처리된 폴더, 마지막 파일 편집자가 )
		//==============================================
		
		JSONObject rtnJsonObject = new JSONObject();
		
		String 	sReturnCheckOut 	= "0";
		String	sReturnCode 		= "0";	
		String	sReturnMsg			= "SUCCESS";	
		
		String  sFolderId			= "";	// - 사용안함.

		try {
 			
			// 문서 ID
			if(r_object_id.length() > 16 )
			{
				r_object_id = r_object_id.substring(0, 16);
			}
			
			// 선택된 문서 정보
			// 최신 문서 ID 조회후 편집 중 상태 확인
	    	Optional<Doc> optDoc 		= docDao.selectDetailOne(r_object_id, user_id, false);	    	
			if (optDoc.isPresent()) 
			{
				
				Doc 	doc 	= optDoc.get();
				String 	sDocKey = doc.getUDocKey();
				
				// 최신문서 정보 조회 ( DOC_KEY 를 이용 )
				Optional<Doc> optDocCurrent = docDao.selectDetailOne(sDocKey, user_id, true);
				if (optDocCurrent.isPresent()) 
				{
					Doc 	docCurrent 	= optDocCurrent.get();
						
					//=================
					// 문서 정보 확인
					//=================
					String sCheckOutStatus  	= "0";
					String sCheckOutUserCheck 	= "0";	// 0 : 같은사용자, 1: 다른 사용자.
					String sDeleteStatus  		= "0";
					String 	uDocStatus 			= doc.getUDocStatus();
					// String 	uDocStatus 		= doc.getl();
					boolean bIsReadOnly		= false;	// 잠금처리 폴더 확인
					String sLastEditor		= StringUtils.isBlank(doc.getULastEditor()) ? "" : doc.getULastEditor();
					
					int 	maxLevel		= doc.getMaxLevel();
					boolean isLive 			= DocStatus.LIVE.getValue().equals(uDocStatus);
					boolean isClosed 		= DocStatus.CLOSED.getValue().equals(uDocStatus);
					
					if(StringUtils.isNotBlank(docCurrent.getRLockOwner()) )
					{
						sCheckOutStatus = "1";
						
						// 다른 사용자가 편집중인지 체크
						if(!user_id.equals(docCurrent.getRLockOwner()) )
						{
							sCheckOutUserCheck = "1";
						}
					}
					
					if(StringUtils.isNotBlank(docCurrent.getUDeleteStatus()))
					{
						sDeleteStatus = "1";
					}
					
					// 잠금처리 폴더 확인
					String sDocFolderId		= doc.getUFolId();
					Optional<Folder> optFolder = folderService.selectOne(sDocFolderId);
					if(optFolder.isPresent()) {
						if( optFolder.get().getUFolStatus().equals("C") ) {//잠금처리된 폴더
							bIsReadOnly = true;
						}
					}
					
					//=================
					// Validation 체크
					//=================
					if(sCheckOutStatus.equals("1") && sCheckOutUserCheck.equals("1"))  
					{
						sReturnCheckOut = "1";
						sReturnMsg		= doc.getTitle() +" 은 편집중 상태입니다";
					}
					else if(sDeleteStatus.equals("1")) 
					{
						sReturnCheckOut = "1";
						sReturnMsg		= doc.getTitle() +" 은 삭제된 상태입니다";
					}
					else if(isLive && maxLevel < GrantedLevels.DELETE.getLevel()) 
					{
						sReturnCheckOut = "1";
						sReturnMsg		= doc.getTitle() +" 덮어쓰기 권한이 없습니다.";	// canUpdateDoc 로직 활용
					}
					else if(isClosed) 
					{
						sReturnCheckOut = "1";
						sReturnMsg		= doc.getTitle() +" 완료(CLOSE) 문서입니다.";
					}
					else if(bIsReadOnly)
					{
						sReturnCheckOut = "1";
						sReturnMsg		= doc.getTitle() +" 잠금처리된 폴더 문서입니다.";
					}
					else if(!sLastEditor.equals(user_id))
					{
						sReturnCheckOut = "1";
						sReturnMsg		= doc.getTitle() +" 마지막 파일 편집자가 아닙니다.";
					}
					
				}
				else
				{
					sReturnCheckOut = "1";
					sReturnMsg		= "문서 정보가 없습니다.";
				}
			}
			else
			{
				// 문서가 없을경우 중요문서함에서 한번더 확인
				// 중요문서함여부 확인
				// TODO :: 버전 정보 가 있을경우 추가 필요
				Optional<DocImp> optDocImp= docImpService.selectOne(r_object_id);
				if (optDocImp.isPresent()) 
				{
					
					DocImp 	docImp 	= optDocImp.get();
					if(StringUtils.isNotBlank(docImp.getRLockOwner()))
					{
						// sReturnCheck = "1";
					}
				}
				else
				{
					sReturnCheckOut = "1";
					sReturnMsg		= "문서 정보가 없습니다.";
				}
			}
			
			// Return
			sReturnCode 		= "0";			// 정상처리

		} catch (Exception e) {

			sReturnCheckOut = "1";
			sReturnCode 	= "-1";
			sReturnMsg		= "덮어쓰기 체크에 실패하였습니다.";

			log.info("[isCheckOutOverwrite] ERROR :: " + user_id);
			log.info("[isCheckOutOverwrite] ERROR :: " + e.toString() + e.getMessage());
			
		} finally {

		}

		// Agent 리턴 정보.
		rtnJsonObject.put("check_out"	, sReturnCheckOut);
		rtnJsonObject.put("return_code"	, sReturnCode);
		rtnJsonObject.put("return_msg"	, sReturnMsg);
		
		log.info("[isCheckOutOverwrite] END : " + rtnJsonObject.toString());
		
		return rtnJsonObject;
	}
	
	// 다른이름으로 저장 전 체크
	public JSONObject isSaveAsNewCheck(String user_id, String r_ObjectName, String r_folder_id) throws Exception {
		
		log.info("[isSaveAsNewCheck] START : " + user_id);
		log.info("[isSaveAsNewCheck] START : " + r_ObjectName);
		log.info("[isSaveAsNewCheck] START : " + r_folder_id);
		
		//==============================================
		// 다른이름으로 저장 가능 여부 체크
		// 
		//==============================================
		// 호출.
		//		- 트레이 문서 선택후 버전업  ( 버전업 , 덮어쓰기, 복사, 새로 생성 )
		//
		//	r_ObjectName	- 현재 사용안함
		//
		//	r_folder_id 형식
		//		ROOT : PFN, ... , ...
		//		폴더  : r_object_id_카테고리_FOLDER
		// 
		// Return
		//		- check			: 0 : 버전업가능, 1 : 버전업불가
		// 		- return_code	: 0 : 정상	 , -1 : 오류
		//		- return_msg	: 리턴 MSG 처리
		//
		//	기타
		//		SaveAsNew 체크
		//			( 잠금처리된 폴더, 실시간 저장 체크 )
		//==============================================
		
		JSONObject rtnJsonObject = new JSONObject();
		
		String	sReturnCode 		= "0";	
		String	sReturnMsg			= "SUCCESS";	
		
		String  sFolderId			= "";		// 폴더의 실제 R_OBJECT_ID 만 사용
		String  sFolderCabinetCode	= "";		// 폴더 문서함 ID
		
		// 사용안함.
		// String  sReturnAction		= "2";		// 1 : 덮어쓰기 2 : 버전갱신 9 : 중간저장 + 덮어쓰기 10: 중간저장 + 버전갱신

		try {
 			
			// 문서 ID
			if(r_folder_id.length() > 16 )
			{
				sFolderId = r_folder_id.substring(0, 16);
			}
			else
			{
				sFolderId	= r_folder_id;
			}
			
			boolean bIsExistFolder	= true;		// 폴더 존재 여부
			boolean bIsReadOnly		= false;	// 잠금처리 폴더 확인
			
			// 잠금처리 폴더 확인
			Optional<Folder> optFolder = folderService.selectOne(sFolderId);
			if(optFolder.isPresent()) {
				
				sFolderCabinetCode = optFolder.get().getUCabinetCode();
				
				if( optFolder.get().getUFolStatus().equals("C") ) {//잠금처리된 폴더
					bIsReadOnly = true;
				}
			}
			else
			{
				bIsExistFolder = false;
			}
			
			//=================
			// Validation 체크
			//=================
			if(bIsReadOnly)
			{
				sReturnCode 	= "-1";
				sReturnMsg		= " 잠금처리된 폴더 문서입니다.";
			}
			else if(!bIsExistFolder)
			{
				sReturnCode 	= "-1";
				sReturnMsg		= " 폴더 정보가 없습니다.";
			}
			
			if( !(r_folder_id.contains("_DEPT_FOLDER") 
					|| r_folder_id.contains("_POW_FOLDER")
					|| r_folder_id.contains("_PIN_FOLDER")
					|| r_folder_id.contains("_ROW_FOLDER")
					|| r_folder_id.contains("_RIN_FOLDER")
					|| r_folder_id.contains("_SHARE_FOLDER")) )
			{
				sReturnCode 	= "-1";
				sReturnMsg		= " 업로드 불가능한 폴더입니다.";
			}
			
			
			// 복사시 편집 없이 완료만 처리됨. --> 이부분 첨부 정책에도 없기 때문에 (회의후 필요 없음 ) 
//			formatChkMap = codeService.getClosedFormatCodeMap();
//		    
//		    idfNewDoc.setString("u_doc_status", "L"); // live	(복사시에는 상태를 Live로 . 무조건.)
//            //생성하자마자 Closed문서가 되는 경우 
//	        if (formatChkMap.containsKey(s_Extr.toUpperCase())) {
//	        	 idfNewDoc.setString("u_doc_status"	,"C");
//	        	 idfNewDoc.setString("u_closer"		, dto.getReqUser());
//	        	 idfNewDoc.setTime("u_closed_date"	, new DfTime());
//	        }
			
			//=========================================
			//	편집 실시간 저장 여부.
			//	 다른 이름 저장시 실시간 저장 안됨
			//=========================================
			// sReturnAction = this.getCheckoutActionFlag(user_id, sFolderCabinetCode);
			

		} catch (Exception e) {

			sReturnCode = "-1";
			sReturnMsg	= "다른이름으로 저장 체크에 실패하였습니다.";

			log.info("[isSaveAsNewCheck] ERROR :: " + user_id);
			log.info("[isSaveAsNewCheck] ERROR :: " + e.toString() + e.getMessage());
			
		} finally {

		}

		// Agent 리턴 정보.
		rtnJsonObject.put("return_code"	, sReturnCode);
		rtnJsonObject.put("return_msg"	, sReturnMsg);
		
		log.info("[isSaveAsNewCheck] END : " + rtnJsonObject.toString());
		
		return rtnJsonObject;
	}
	
	
	@Override
	public String makeSyspath(String uDocKey, String syspath) throws Exception {
    AES256Util aesUtil = new AES256Util();
    String sEncSysPath      = aesUtil.encrypt(uDocKey + syspath);
    String sUrlEncodeSysPath  = Base64.encodeBase64URLSafeString(sEncSysPath.getBytes());
    
    return sUrlEncodeSysPath;
	}

	// 사용안함
	// OFFICE TEMP 저장시 실시간 저장 여부 체크 하려고 했지만 ( Agent 에 기능 이 없어서 처리 안하기로함 ) 
	/**
	 * 편집 실시간 저장 여부.
	 * @param ps_UserId			- 사용자 ID
	 * @param ps_CabinetCode	- CABINT_CODE
	 * @return
	 * @throws Exception
	 */
	private String getCheckoutActionFlag(String ps_UserId, String ps_CabinetCode) throws Exception {
		
		String  sReturnAction		= "2";		// 1 : 덮어쓰기 2 : 버전갱신 9 : 중간저장 + 덮어쓰기 10: 중간저장 + 버전갱신
		
		try {
 			
			// 편집 정책 조회 ( 1 : 덮어쓰기 2 : 버전갱신 9 : 중간저장 + 덮어쓰기 10: 중간저장 + 버전갱신 )
			int iOverWriteOrNew		= 2;	// DEFAULT
			int iMiddleSave 		= 0;	// DEFAULT
			
			// 버전업 or 덮어쓰기 구별
			Optional<UserPreset> userPresetPolicy = userPresetDao.selectOneByUserId(ps_UserId);
			if(userPresetPolicy.isPresent())
			{
				String sUserPresetFlag = userPresetPolicy.get().getUEditSaveFlag();				
				if(sUserPresetFlag.equals("O"))
				{
					iOverWriteOrNew = 1;
				}
			}
			
			// 문서함 정보
			String sDocCabinetCode	= ps_CabinetCode; // 문서정보가 아닌 폴더 정보에서 조회된 cabinet_code 로 확인   idf_CurrentObj.getString("u_cabinet_code");
			String sDocComOrgId		= gwDeptService.selectComCodeByCabinetCode(sDocCabinetCode);
			String sDocDeptOrgId 	= gwDeptService.selectOrgIdByCabinetcode( sDocCabinetCode );
			
			// 편집중 중간 저장 여부 구별  .getDeptCabinetcode() 에서 수정함.
			CodeFilterDto codeFilterDto = CodeFilterDto.builder()
					.uCodeType(CodeType.CONFIG_MID_SAVE_DEPT.getValue())
					.uCodeVal1(sDocComOrgId)   		// userSession.getUser().getComOrgId()
					.uCodeVal2(sDocDeptOrgId)		// userSession.getUser().getOrgId()
					.build();
			List<Code> codeList = codeDao.selectList(codeFilterDto);
			if (codeList != null && codeList.size() > 0)
			{
				iMiddleSave = 8;
			}
			
			int iSaveOption		= iOverWriteOrNew + iMiddleSave;
			sReturnAction 		= Integer.toString(iSaveOption);

		} catch (Exception e) {

			// Exception 시 일반 편집
			sReturnAction		= "2";

			log.info("[getCheckoutActionFlag] ERROR :: " + e.toString() + e.getMessage());
			
		}
		
		return sReturnAction;
	}
	
}