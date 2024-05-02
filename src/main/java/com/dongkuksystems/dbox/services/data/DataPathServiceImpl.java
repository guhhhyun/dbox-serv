package com.dongkuksystems.dbox.services.data;
import static com.dongkuksystems.dbox.models.api.response.ApiResult.OK;

import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Map.Entry;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.documentum.fc.client.DfQuery;
import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfDocument;
import com.documentum.fc.client.IDfGroup;
import com.documentum.fc.client.IDfPersistentObject;
import com.documentum.fc.client.IDfQuery;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.common.DfId;
import com.documentum.fc.common.DfTime;
import com.documentum.fc.common.IDfId;
import com.documentum.fc.common.IDfTime;
import com.dongkuksystems.dbox.constants.AuthorType;
import com.dongkuksystems.dbox.constants.ComCodeType;
import com.dongkuksystems.dbox.constants.DCTMConstants;
import com.dongkuksystems.dbox.constants.DocLogItem;
import com.dongkuksystems.dbox.constants.DocStatus;
import com.dongkuksystems.dbox.constants.FolderStatus;
import com.dongkuksystems.dbox.constants.GrantedLevels;
import com.dongkuksystems.dbox.constants.HamType;
import com.dongkuksystems.dbox.constants.SysObjectType;
import com.dongkuksystems.dbox.daos.table.kakao.KakaoDao;
import com.dongkuksystems.dbox.daos.type.docbox.project.ProjectDao;
import com.dongkuksystems.dbox.daos.type.docbox.research.ResearchDao;
import com.dongkuksystems.dbox.daos.type.manager.preservationperiod.PreservationPeriodDao;
import com.dongkuksystems.dbox.daos.type.path.PathDao;
import com.dongkuksystems.dbox.errors.BadRequestException;
import com.dongkuksystems.dbox.errors.NotFoundException;
import com.dongkuksystems.dbox.models.common.UserSession;
import com.dongkuksystems.dbox.models.dto.path.DPath;
import com.dongkuksystems.dbox.models.dto.type.auth.CheckAuthParam;
import com.dongkuksystems.dbox.models.dto.type.auth.FolderAuthDto;
import com.dongkuksystems.dbox.models.dto.type.auth.HamInfoResult;
import com.dongkuksystems.dbox.models.dto.type.auth.RegistAuthShareDto;
import com.dongkuksystems.dbox.models.dto.type.docbox.project.ProjectCreateDto;
import com.dongkuksystems.dbox.models.dto.type.docbox.research.ResearchCreateDto;
import com.dongkuksystems.dbox.models.dto.type.folder.RegistFolderDto;
import com.dongkuksystems.dbox.models.dto.type.kakao.KakaoDetail;
import com.dongkuksystems.dbox.models.dto.type.manager.preservationperiod.PreservationPeriodDto;
import com.dongkuksystems.dbox.models.table.etc.VDept;
import com.dongkuksystems.dbox.models.table.etc.VUser;
import com.dongkuksystems.dbox.models.table.log.LogDoc;
import com.dongkuksystems.dbox.models.type.auth.AuthBase;
import com.dongkuksystems.dbox.models.type.docbox.Project;
import com.dongkuksystems.dbox.models.type.docbox.Research;
import com.dongkuksystems.dbox.models.type.request.ReqUseUsb;
import com.dongkuksystems.dbox.models.type.user.UserPreset;
import com.dongkuksystems.dbox.repositories.redis.RedisRepository;
import com.dongkuksystems.dbox.securities.JWT;
import com.dongkuksystems.dbox.services.AbstractCommonService;
import com.dongkuksystems.dbox.services.auth.AuthService;
import com.dongkuksystems.dbox.services.cache.CacheService;
import com.dongkuksystems.dbox.services.code.CodeService;
import com.dongkuksystems.dbox.services.dept.GwDeptService;
import com.dongkuksystems.dbox.services.folder.FolderService;
import com.dongkuksystems.dbox.services.notification.NotificationService;
import com.dongkuksystems.dbox.services.project.ProjectService;
import com.dongkuksystems.dbox.services.research.ResearchService;
import com.dongkuksystems.dbox.utils.CommonUtils;
import com.dongkuksystems.dbox.utils.dctm.DCTMUtils;


@Service
public class DataPathServiceImpl extends AbstractCommonService implements DataPathService {
  @Value("${jwt.token.header}")
  private String tokenHeader;
  
  @Value("${dbox.url}")
  private String dboxUrl;  //메일발송시 참조하는 URL(application*.yml파일 참조)
  
  
  private final FolderService folderService ;
  private final DataService dataService;
  private final AuthService authService;  
  private final CodeService codeService;
  private final RedisRepository redisRepository;
  private final CacheService cacheService;
  private final GwDeptService gwDeptService;
  private final JWT jwt;
  private final PathDao pathDao;
  private final ProjectDao projectDao;
  private final ResearchDao researchDao;
  
  private final ProjectService projectService;
  private final ResearchService researchService;
  
  private Map<String, Integer> secLevelMap;
  private Map<String, String> uSrcMap;
  private Map<String, String> uTgtMap;
  private Map<String, String> psrvPMap; //보존년한맵
  private Map<String, String> psrvPMapDf; //보존년한맵(기본값)
  
  private final NotificationService notificationService;
  
  private final PreservationPeriodDao psrvPeriodDao;
  
  private final KakaoDao kakaoDao;


  public DataPathServiceImpl(DataService dataService, AuthService authService, CodeService codeService
		     , RedisRepository redisRepository, CacheService cacheService, JWT jwt, PathDao pathDao
		     , FolderService folderService,GwDeptService gwDeptService
		     , ProjectService projectService, ResearchService researchService
		     , ProjectDao projectDao, ResearchDao researchDao
		     , NotificationService notificationService
		     , PreservationPeriodDao psrvPeriodDao
		     , KakaoDao kakaoDao
		     ) {
	this.dataService     = dataService;
	this.authService     = authService;
    this.redisRepository = redisRepository;
    this.cacheService    = cacheService;
    this.codeService     = codeService;
    this.jwt = jwt;
    this.pathDao = pathDao   		;
    this.folderService=folderService;
    this.gwDeptService=gwDeptService;
    this.projectService = projectService;
    this.researchService = researchService;
    this.projectDao=projectDao;
    this.researchDao=researchDao;
    this.notificationService = notificationService;
    this.psrvPeriodDao = psrvPeriodDao;
    this.kakaoDao = kakaoDao;

    this.secLevelMap = new HashMap<String, Integer>();
	this.uSrcMap  = new HashMap<String, String>();
	this.uTgtMap  = new HashMap<String, String>();
	
	this.psrvPMap  = new HashMap<String, String>();
	this.psrvPMapDf= new HashMap<String, String>();
    
    
  }

  //리스트 정렬용
  class uFolderAscending implements Comparator<String>{
	    public int compare(String a, String b)
		{
			return a.compareTo(b);
		}
  }

  @Override
  public List<DPath> selectDTList(String ps_DocKey ) throws Exception {
	  return  pathDao.selectDTList( ps_DocKey); // 하위(전체)에 권한 (없는) 폴더나 문서가 있는지 체크
  }
  
  @Override
  public List<DPath> selectNAList(String ps_FolId,  String s_PrCode, String ps_UserId, String ps_AuthExclusive, String ps_Del, String psStatus, String ps_JobGubun) throws Exception {
	  return  pathDao.selectNAList( ps_FolId,  s_PrCode,  ps_UserId,  ps_AuthExclusive, ps_Del, psStatus,  ps_JobGubun); // 하위(전체)에 권한 (없는) 폴더나 문서가 있는지 체크
  }

  @Override
  public List<DPath> selectDocAuthCheck(String docKey,  String ps_UserId, int pi_AuthLevel) throws Exception {
	  return  pathDao.selectDocAuthCheck( docKey,   ps_UserId, pi_AuthLevel); // 파일에 대한 권한이 있는지
  }
  
  /**-- 중요문서함(폴더타입 DI*) 이 있는지 확인 **/
  @Override 
  public List<DPath> selectFolTypeList(String psCabinetcode,String psFolType) throws Exception{
	  return  pathDao.selectFolTypeList( psCabinetcode,  psFolType); // 파일에 대한 권한이 있는지
  }
  
  /**-- 이벤트별로 알림유형 조회하는 처리**/
  @Override 
  public DPath selectAlermType(String psEventCode,  String psCabinet) throws Exception{
	  return  pathDao.selectAlermType( psEventCode,  psCabinet); 
  }
  
  /**-- 반출함 반출요청id 조회**/
  @Override 
  public String selectTakeoutDocsRobjectIdByDocId(String psDocId) throws Exception{
	  return  pathDao.selectTakeoutDocsRobjectIdByDocId( psDocId); 
  }
  
  /**-- 승인자ID 검색 **/
  @Override
  public String selectTemporaryRelManagerId(String psOrgId)  throws Exception{
     return pathDao.selectTemporaryRelManagerId( psOrgId);
  }

  void initMapInfo() {
	    //보안등급 비교용 Map
		this.secLevelMap.put("S", 4);
		this.secLevelMap.put("T", 3);
		this.secLevelMap.put("C", 2);
		this.secLevelMap.put("G", 1);
		this.secLevelMap.put(null,  0);
		this.secLevelMap.put("",  0);
		
  	
		//RE:생성이력, DEL:삭제이력
		this.uSrcMap.put("D", "DEL") ;// 부서함
		this.uSrcMap.put("P", "DEL") ;// 프로젝트/투자
		this.uSrcMap.put("R", "DEL") ;// 연구과제
		this.uSrcMap.put("M", "DEL") ;// 연구과제
		
		this.uSrcMap.put("DI", "RE"); //중요문서함

		
		//RE:생성이력, DEL:삭제이력
		this.uTgtMap.put("D", "RE") ;// 부서함
		this.uTgtMap.put("S", "RE") ;// 공유/협업
		this.uTgtMap.put("P", "RE") ;// 프로젝트/투자
		this.uTgtMap.put("R", "RE") ;// 연구과제
		this.uTgtMap.put("C",  "RE") ;// 조직함(회사)
		this.uTgtMap.put("M",  "RE") ;// 조직함(관리팀)
		
		this.uTgtMap.put("DI", "DEL"); //중요문서함
		
		//보존년한 기본
		psrvPMapDf.put("S", "20");
		psrvPMapDf.put("C", "10");
		psrvPMapDf.put("G", "10");
		psrvPMapDf.put("T", "15");
		

  }
  @Override
  public DPath  getCheckList(UserSession userSession, String gubun, String ps_FolId, String ps_PrCode, boolean isMobile, DPath dto) throws Exception {
        // dataId 무결성 확인
    	String rtnString="";
	    List<String> chkTotList = new ArrayList<>();

        // if (DfId.isObjectId(ps_FolId)) {
    	// 현재 사용자 아이디
    	final String userId = userSession.getUser().getUserId();

    	boolean		b_IsSubExist	= false;
    	//boolean      b_IsUpLockFol  = false;
		boolean		b_IsSubLockFol	= false;
		boolean		b_IsSubLockDoc	= false;
		boolean		b_IsSubNoAuth	= false;
		boolean		b_IsSubDelDoc	= false;
		boolean		b_IsSubAttach	= false;
        boolean     b_IsSubClosedDoc= false;
	
    	List<DPath>  EOChkList = pathDao.selectEOList(ps_FolId, ps_PrCode);         // 하위에 폴더나 문서가 있는지 체크
    	if(!dto.getUptPthGbn().equals("C")) { //'잠금'처리된 폴더 내 폴더 및 문서는 '잠금 해제' 되어있는 폴더로 복사 가능하다
    	    List<DPath>  LFChkList = pathDao.selectLFList(ps_FolId, ps_PrCode);         // 하위(전체)에 잠긴 폴더가 있는지 체크
        	b_IsSubLockFol	= LFChkList.size() > 0?true:false;
    	}
    	//List<DPath>  LFUpChkList = pathDao.selectUpLFList(ps_FolId, ps_PrCode);     // 상위(전체)에 잠긴 폴더가 있는지 체크
    	
    	if(dto.getUptPthGbn().equals("M")) { //이동시에만 체크
    	    List<DPath>  LDChkList = pathDao.selectLDList(ps_FolId, ps_PrCode);         // (하위에)잠긴(편집중인) 문서가 있는지 체크
        	b_IsSubLockDoc	= LDChkList.size() > 0?true:false;
    	}
    	
    	//if(!gubun.equals("C")){  //복사가 아닐때는 모두 체크
    	    //List<DPath>  DDChkList = pathDao.selectDDList(ps_FolId,ps_PrCode);      // 하위(전체)에 삭제 상태(문서는 요청중 포함)인 폴더나 문서가 있는지 체크
    	    //b_IsSubDelDoc	= DDChkList.size() > 0?true:false;
        //}
    	if(gubun.equals("D")){  //삭제일 때 체크
        	List<DPath>  ATChkList = pathDao.selectATList(ps_FolId,ps_PrCode);      // 하위(전체)에 타시스템 첨부(원문)한 문서가 있는지 체크
        	b_IsSubAttach	= ATChkList.size() > 0?true:false;
    	}
    	
    	String authExclusive=null;
    	//List<DPath>  NAChkList = pathDao.selectNAList(ps_FolId, ps_PrCode,userId, authExclusive, null, null, dto.getUptPthGbn()); // 하위(전체)에 권한 (없는) 폴더나 문서가 있는지 체크
    	
    	//삭제구분자에 " "를 넘겨서 삭제되지 않은 것만 대상으로 이동,복사,삭제처리되도록 함
    	
    	int iNaChkCnt=0;
    	if(dto.getUptPthGbn().equals("M")) {  //이동시에만 체크
    	    List<DPath>  NAChkList = pathDao.selectNAList(ps_FolId, ps_PrCode,userId, authExclusive, " ", null, dto.getUptPthGbn()); // 하위(전체)에 권한 (없는) 폴더나 문서가 있는지 체크
    	    b_IsSubNoAuth	= NAChkList.size() > 0?true:false;
    	    iNaChkCnt =NAChkList.size();
    	}

    	b_IsSubExist	= EOChkList.size() > 0?true:false;
    	//b_IsUpLockFol	= LFUpChkList.size() > 0?true:false;
    	
    	if(gubun.equals("M")) { //이동요청일 때, Closed문서가 있는데 다른 조직함으로의 이동일 경우
        	List<DPath>  CDChkList = pathDao.selectCDList(ps_FolId,ps_PrCode);     // (하위에)Closed 문서가 있는지 체크
        	b_IsSubClosedDoc= CDChkList.size() > 0?true:false;
        	
    		if(!b_IsSubClosedDoc) {
    			
    			if(dto.getTargetDboxId().substring(0,1).equals("C") || dto.getTargetDboxId().substring(0,1).equals("M")) { //조직함    			
	    			//G나 C 가 아닌 파일이 있는지 확인
    				rtnString += b_IsSubClosedDoc ? ((rtnString.equals("") ? "" : ",") + "Closed문서는 사내,그룹사내 문서만 이동할 수 있습니다"           ) : "";
    			}
    		}else {
    			if(dto.getTargetDboxId().substring(0,1).equals("C") || dto.getTargetDboxId().substring(0,1).equals("M")) { //조직함
	    			if(dto.getSrcCabinetcode() != dto.getTgCabinetcode()) {
	    				dto.setUptPthGbn("C");//Live문서인 경우 복사로 처리하도록 함
	    			}
    			}
    		}
    	}
    	
    	if(!dto.getUptPthGbn().equals("D")) { //이동,복사
    		b_IsSubAttach=false;
    	}else if(dto.getUptPthGbn().equals("C") || dto.getUptPthGbn().equals("D")) {
    		b_IsSubLockDoc=false; //편집중인 문서(편집중인 문서는 삭제할 때 pass한다
    	}

		//if(!b_IsUpLockFol && !b_IsSubLockFol && !b_IsSubLockDoc && !b_IsSubDelDoc && !b_IsSubNoAuth && !b_IsSubAttach)
	    if(!b_IsSubLockFol && !b_IsSubLockDoc && !b_IsSubDelDoc && !b_IsSubNoAuth && !b_IsSubAttach)
		{
			chkTotList.add("200");    //정상처리코드
			chkTotList.add(iNaChkCnt+"");//건수
			chkTotList.add("");       //메시지
			
		}else{
			//rtnString += b_IsUpLockFol ? ((rtnString.equals("") ? "" : ",") + "상위 폴더에 잠김 폴더 있음"           ) : "";
			if(!dto.getUptPthGbn().equals("C")) { //'잠금'처리된 폴더 내 폴더 및 문서는 '잠근 해제' 되어있는 폴더로 복사 가능하다
			    rtnString += b_IsSubLockFol ? ((rtnString.equals("") ? "" : ",") + "하위 폴더에 잠김 폴더 있음"           ) : "";
			}
			rtnString += b_IsSubLockDoc ? ((rtnString.equals("") ? "" : ",") + "편집중 문서 있음"         ) : "";
		    //rtnString += b_IsSubDelDoc  ? ((rtnString.equals("") ? "" : ",") + "기삭제된 문서 있음"       ) : "";
			rtnString += b_IsSubNoAuth  ? ((rtnString.equals("") ? "" : ",") + "권한 없는 문서 있음"      ) : "";
			rtnString += b_IsSubAttach  ? ((rtnString.equals("") ? "" : ",") + "타시스템 첨부한 문서 있음") : "";
			
			chkTotList.add("500");
			chkTotList.add(EOChkList.size()+"");
			chkTotList.add(rtnString);
		}
		dto.setReturnStr(chkTotList);
		
    	return dto;
  }
  
  
  /** 폴더/파일 복사처리
   * jjg
   * **/
  
  @Override
  public String copyFolderAndFiles(UserSession userSession, IDfSession idfSess, DPath dto, Map<String, Map<String, List<DPath>>>  folMapTot, boolean isMobile) throws Exception {

		String rtnMsg=""; 
		//IDfSession idfSession = idfSess != null ? idfSess : this.getIdfSession(userSession);
		IDfSession idfSession = idfSess != null ? idfSess :DCTMUtils.getAdminSession();
		
		initMapInfo();
		
	    String sOwnSrDeptOrgId = gwDeptService.selectOrgIdByCabinetcode( dto.getSrcCabinetcode());
		String sOwnTgDeptOrgId = gwDeptService.selectOrgIdByCabinetcode( dto.getTgCabinetcode());

		String s_CabinetCode = dto.getTgCabinetcode();
		String sTgOrgId = gwDeptService.selectOrgIdByCabinetcode( s_CabinetCode);
		dto.setTgOrgId(sTgOrgId);
		HamInfoResult hamInfo = authService.selectDeptHamInfo(dto.getTgOrgId()).orElse(
	              authService.selectHamInfo(dto.getTgOrgId()).orElseThrow(() -> new NotFoundException(CheckAuthParam.class,  dto.getTgOrgId() )));
		dto.setOwnSrDeptOrgId(sOwnSrDeptOrgId);
		dto.setOwnTgDeptOrgId(sOwnTgDeptOrgId);
		dto.setHamInfo(hamInfo);
		
		String s_ComId   = gwDeptService.selectComCodeByCabinetCode(s_CabinetCode).toLowerCase();
		dto.setTgComId(s_ComId);
		PreservationPeriodDto psrvDto = psrvPeriodDao.selectOneByComCode(s_ComId.toUpperCase());
		psrvPMap.put("S", psrvDto.getUSecSYear());
		psrvPMap.put("C", psrvDto.getUSecCYear());
		psrvPMap.put("G", psrvDto.getUSecGYear());
		psrvPMap.put("T", psrvDto.getUSecTYear());
		
		psrvPMap.put("PJT", psrvDto.getUPjtEverFlag());

	    //private String uSecSYear;
	    //private String uSecTYear;
	    //private String uSecCYear;
	    //private String uSecGYear;
		
		try {
			if(folMapTot.size() > 0) {

	        	IDfPersistentObject idfTg_PObj=null;
				int iCnt=0;
			    for (Entry<String, Map<String, List<DPath>>> entry : folMapTot.entrySet()) {
			        Map<String, List<DPath>> folMap = folMapTot.get(entry.getKey());

					List<String> folList = new ArrayList<String>();
			        for (Map.Entry<String, List<DPath>> folEntry : folMap.entrySet()) {
				    	folList.add(folEntry.getKey());
				    }

				 	if (!idfSession.isTransactionActive()) {
				 	    idfSession.beginTrans();
				 	}
			        
		        	String keyStr=entry.getKey();
		            if( !keyStr.substring(0,1).equals("P") && !keyStr.substring(0,1).equals("R")) {
			        		rtnMsg=copyFolderAndFile(userSession, idfSession, dto, folList, folMap, isMobile, keyStr, iCnt);
			       	}else{
			       		if(keyStr.substring(0,1).equals("P")) {  //프로젝트 코드 
			        			dto.setPrCode(keyStr);
			        			dto.setPrType("P");
				        		keyStr=keyStr.substring(1);
			        			rtnMsg=copyFolderAndFile(userSession, idfSession, dto, folList, folMap, isMobile, keyStr, iCnt);
			       		}else if(keyStr.substring(0,1).equals("R")) { //연구투자 코드
			        			dto.setPrCode(keyStr);
			        			dto.setPrType("R");
				        		keyStr=keyStr.substring(1);
			        			rtnMsg=copyFolderAndFile(userSession, idfSession, dto, folList, folMap, isMobile,keyStr, iCnt);
			       		}
			       	}
		  		    idfSession.commitTrans();
			       	iCnt++;
			    }
			}else {
			 	if (!idfSession.isTransactionActive()) {
			 	    idfSession.beginTrans();
			 	}
				List<String> folList = new ArrayList<String>();
				Map<String, List<DPath>> folMap = new HashMap<String, List<DPath>>();
   			    rtnMsg=copyFolderAndFile(userSession, idfSession, dto, folList, folMap, isMobile,"6", 0);

   			    idfSession.commitTrans();
			}

		} catch (Exception e) {
			e.printStackTrace();
		    rtnMsg=e.toString();
        } finally {
		    if (idfSession != null) {
		      if (idfSession.isTransactionActive()) {
		        idfSession.abortTrans();
		      }
		      if (idfSession.isConnected()) {
		    	  idfSession.disconnect();
		      }
		    }
		 }
		return rtnMsg;
		 
      }
  
  /** 폴더/파일 복사처리
   * jjg
   * **/
  
  @Override
  public String copyFolderAndFile(UserSession userSession, IDfSession idfSession, DPath dto, List<String> folList,  Map<String, List<DPath>>  folMap, boolean isMobile, String keyStr, int iCnt) throws Exception {

			//폴더 생성
			String newFolderName = null;             //......................폴더함침이 가능한 경우, 타겟쪽 폴더id가 리턴되며 새로 만들어야 하는 경우 (1)로 번호가 붙어서 사용됨
	
		 	if(null !=folList && folList.size() > 0)
		        Collections.sort(folList, new uFolderAscending());
	
		    String topFolId = dto.getTargetDboxId(); //옮기려는 최상위 폴더 ID
		    String sUprCodeCheck=dto.getUPrCodeCheck()==null?"9":dto.getUPrCodeCheck();
		    if(null==dto.getPrCode() || dto.getPrCode().equals("") || dto.getPrCode().equals(" ") || dto.getTargetFolType().equals("DFO")) {
		        dto.setPrCode(" ");
		        dto.setPrType(" ");
            }

		    IDfPersistentObject idfTg_PObj=null;
	    	String targetGubun = dto.getTargetGubun();

	    	/////////////////////////////////////프로젝트, 연구과제쪽 '폴더' 관련 세팅
	    	String sFinishYn="N";
	    	String sPjtUFolId="";
		    if(targetGubun.equals("PCL") || targetGubun.equals("RCL")) {
		    	idfTg_PObj = idfSession.getObject(new DfId(topFolId));
		    	String objTypeNm= idfTg_PObj.getType().getName();
		    	if(objTypeNm.equals("edms_folder") && targetGubun.equals("PCL"))	    sPjtUFolId=idfTg_PObj.getString("u_fol_id");
		    	else if(objTypeNm.equals("edms_folder") && targetGubun.equals("RCL"))   sPjtUFolId=idfTg_PObj.getString("u_fol_id"); 

		    	sFinishYn="Y";
    		    topFolId=sPjtUFolId;
	    	}else if(dto.getTargetGubun().equals("PFN") || dto.getTargetGubun().equals("RFN") || dto.getTargetGubun().equals("PIF")) {
	    		sFinishYn="Y" ;
	    		
	    	}else if(dto.getTargetGubun().equals("PJC") ||dto.getTargetGubun().equals("POW")|| dto.getTargetGubun().equals("PIC") || dto.getTargetGubun().equals("RSC")|| dto.getTargetGubun().equals("RIC")|| dto.getTargetGubun().equals("ROW")) { //프로젝트, 연구과제
	    		if (!DfId.isObjectId(topFolId)) topFolId="";
	    		;
	    	}else if(dto.getTargetGubun().substring(0,1).equals("D")  && ( keyStr.substring(0,1).equals("p")|| keyStr.substring(0,1).equals("r"))) { //올기려는건 프로젝트나 연구과제인데
	    		if(keyStr.substring(0,1).equals("p") || keyStr.substring(0,1).equals("r")) {
	    		    if(!dto.getTargetGubun().equals("DWY") && !dto.getTargetGubun().equals("DIF")) topFolId="";
	    		}
	    	}else {
	    		if (!DfId.isObjectId(topFolId)) topFolId="";
	    	}
		    /////////////////////////////////////////////////////////////////
//복사처리
		 	/** 복사대상 기존 폴더 id, 새로만든 폴더 id 맵 : 하위 폴더-파일 생성시에 기존폴더를 이미 만들었는지 확인하고 파일의 u_fol_id 지정을 위해서 사용 */
		 	Map<String, String> folChgMap = new HashMap<String,String>();
		 	Map<String, String> folHapMap = new HashMap<String,String>();
		 	Map<String, String> folChgPrjMap = new HashMap<String,String>(); //폴더로 프로젝트를 만들었을 때,		 			 	
	    	

	    	/** 소스쪽 프로젝트 코드가 있는 경우 */
		    if(sUprCodeCheck.substring(0,1).equals("P") || sUprCodeCheck.substring(0,1).equals("R")) {
		    	if(keyStr.substring(0,1).equals("p")) {
                	Optional<Project> pjtOpt = projectDao.selectOne(keyStr);
                	if(pjtOpt.isPresent()) {
        		    	newFolderName = pjtOpt.get().getUPjtName();//소스쪽 프로젝트명
                	}else {
                		newFolderName ="새폴더";
                	}
		    	}else
		    	if(keyStr.substring(0,1).equals("r")) {
		    		Optional<Research> researchOpt = researchDao.selectOne(keyStr);
                	if(researchOpt.isPresent()) {
        		    	newFolderName = researchOpt.get().getURschName();//소스쪽 프로젝트명
                	}else {
                		newFolderName ="새폴더";
                	}
		    	}
            	newFolderName = getChekedFolderName( idfSession, dto.getTgCabinetcode(), topFolId, newFolderName, dto, "");

		    }else if((sUprCodeCheck.equals("9") || sUprCodeCheck ==null) &&  ( keyStr.substring(0,1).equals("p")|| keyStr.substring(0,1).equals("r"))) { //프로젝트나 연구투자건데, 부서함으로 가는 경우
		    	 if( keyStr.substring(0,1).equals("p")|| keyStr.substring(0,1).equals("r")) {
			        //프로젝트나 폴더를 만들고 그 id가 topFolId가 됨
			    	if(keyStr.substring(0,1).equals("p")) {
	                	Optional<Project> pjtOpt = projectDao.selectOne(keyStr);
	                	if(pjtOpt.isPresent()) {
	        		    	newFolderName = pjtOpt.get().getUPjtName();//소스쪽 프로젝트명
	                	}else {
	                		newFolderName ="새폴더";
	                	}
			    	}
			    	if(keyStr.substring(0,1).equals("r")) {
			    		Optional<Research> researchOpt = researchDao.selectOne(keyStr);
	                	if(researchOpt.isPresent()) {
	        		    	newFolderName = researchOpt.get().getURschName();//소스쪽 프로젝트명
	                	}else {
	                		newFolderName ="새폴더";
	                	}
			    	}
			    	
			    	/** 같은 이름의 폴더가 있으면 
			    	 *  1) 쓰기권한이 있으면  동일한 이름의 해당 폴더id를 받아서 사용하게 됨
			    	 *  2) 쓰기권한이 없으면 (숫자)폴더를 만들어서 사용(현재방식)
			    	 */
			    	
			    	
			    	newFolderName = getChekedFolderName( idfSession, dto.getTgCabinetcode(), topFolId, newFolderName, dto,"");
			    	//if(DfId.isObjectId(newFolderName)) {
				 	//       topFolId = newFolderName ;
				 	//       folChgMap.put(keyStr, topFolId) ;//(기존폴더ID, 변경된폴더ID)
				 	//       folHapMap.put(keyStr, topFolId) ;//(기존폴더ID, 합쳐진폴더ID)
			    	//}else {
		    			//프로젝트나 연구과제명으로 폴더 생성 
			    		IDfPersistentObject idfSc_PObj = topFolId.equals("")?null:idfSession.getObject(new DfId(topFolId));

			            RegistFolderDto fto = RegistFolderDto.builder().uUpFolId(topFolId).uFolName(  newFolderName)
				            .uCabinetCode(dto.getTgCabinetcode()).uFolType(dto.getTargetFolType())
				            .uSecLevel( null !=idfSc_PObj ?idfSc_PObj.getString("u_sec_level"):(null==dto.getTargetSecLevel()? "T":dto.getTargetSecLevel()))
				            .uFolStatus(FolderStatus.ORDINARY.getValue())
				            .uCreateUser(dto.getReqUser())
				            .uPrCode(dto.getPrCode())
				            .uPrType(dto.getPrType())
				            .uDeleteStatus("")
				            .build();
			                   //dto.setPreFolderId("");
					 	       topFolId = folderService.createFolder(idfSession, fto);
					 	       folChgMap.put(keyStr, topFolId) ;//(기존폴더ID, 신규폴더ID)
					 	       addMoveAuthBase( userSession, idfSession, topFolId, dto.getTgCabinetcode(), "F", dto, null); //새로만든 폴더에 권한 부여		    	
			    	//}
		    	 }
		    }

		    if(sUprCodeCheck !=null && !sUprCodeCheck.equals("") && ( keyStr.substring(0,1).equals("p")|| keyStr.substring(0,1).equals("r"))) {//프로젝트나 연구과제로 프로젝트나 연구과제를 생성해야 하는 경우
		    	if(keyStr.substring(0,1).equals("p")|| keyStr.substring(0,1).equals("r")) {
		    		
			    	if(keyStr.substring(0,1).equals("p")) {
	                	Optional<Project> pjtOpt = projectDao.selectOne(keyStr);
	                	if(pjtOpt.isPresent()) {
	        		    	newFolderName = pjtOpt.get().getUPjtName();//소스쪽 프로젝트명
	                	}else {
	                		newFolderName ="새폴더";
	                	}
			    	}
			    	if(keyStr.substring(0,1).equals("r")) {
			    		Optional<Research> researchOpt = researchDao.selectOne(keyStr);
	                	if(researchOpt.isPresent()) {
	        		    	newFolderName = researchOpt.get().getURschName();//소스쪽 프로젝트명
	                	}else {
	                		newFolderName ="새폴더";
	                	}
			    	}
				    if(sUprCodeCheck.substring(0,1).equals("P")) {
				    	
				    	newFolderName = getChekedPjtName(idfSession, dto.getTgCabinetcode(), sFinishYn, newFolderName, dto);
/*				        
				    	if(!DfId.isObjectId(newFolderName)) {
				    		String sFolId = newFolderName;
				    		
				    		idfTg_PObj = idfSession.getObject(new DfId(sFolId));
				    		sFolId = idfTg_PObj.getString("u_pjt_code");

				            dto.setPrCode(sFolId);
						    dto.setPrType("P");//프로젝트
				    	}else {	
*/
				    	    String sFolId = projectService.createProject(userSession, ProjectCreateDto.builder().uPjtName(newFolderName).uListOpenYn("Y").uOwnDept(userSession.getUser().getOrgId()).uFinishYn(sFinishYn).uFolId(topFolId).uSecLevel(dto.getTargetSecLevel()).uCreateUser(dto.getReqUser()).build(), idfSession);
				            dto.setPrCode(sFolId);
						    dto.setPrType("P");//프로젝트
//				    	}
				    }
				    else if(sUprCodeCheck.substring(0,1).equals("R")) {
				    	newFolderName = getChekedRscsName(idfSession, dto.getTgCabinetcode(), sFinishYn, newFolderName, dto);
/*				    	
				    	if(DfId.isObjectId(newFolderName)) {
			    		    idfTg_PObj = idfSession.getObject(new DfId(newFolderName));
			    		    String sFolId = idfTg_PObj.getString("u_rsch_code");

				            dto.setPrCode(sFolId);
						    dto.setPrType("R");//연구및과제
				    	}else {				    	
*/				    	
				            String sFolId = researchService.createResearch(userSession, ResearchCreateDto.builder().uRschName(newFolderName).uListOpenYn("Y").uOwnDept(userSession.getUser().getOrgId()).uFinishYn(sFinishYn).uFolId(topFolId).uSecLevel(dto.getTargetSecLevel()).uCreateUser(dto.getReqUser()).build(), idfSession);
				            dto.setPrCode(sFolId);
						    dto.setPrType("R");//연구및과제
//				    	}
				    }
		    	}
            }else if( sUprCodeCheck.equals("9")){
            	if(DfId.isObjectId(topFolId)) {
    		        idfTg_PObj = idfSession.getObject(new DfId(topFolId));
	    		    if(idfTg_PObj !=null) {
	    		    	String objTypeNm= idfTg_PObj.getType().getName();    		    	
		    		    if(objTypeNm.equals("edms_doc") || objTypeNm.equals("edms_folder")) {
				            dto.setPrCode(idfTg_PObj.getString("u_pr_code"));
						    dto.setPrType(idfTg_PObj.getString("u_pr_type"));
						    dto.setTargetSecLevel(idfTg_PObj.getString("u_sec_level"));
		    		    }else if(objTypeNm.equals("edms_project")) {
				            dto.setPrCode(idfTg_PObj.getString("u_pjt_code"));
				            dto.setPrType("P");
						    dto.setTargetSecLevel(idfTg_PObj.getString("u_sec_level"));//프로젝트
		    		    }else if(objTypeNm.equals("edms_research")) {
				            dto.setPrCode(idfTg_PObj.getString("u_rsch_code"));
				            dto.setPrType("R");
						    dto.setTargetSecLevel(idfTg_PObj.getString("u_sec_level"));//연구및과제
		    		    }
	    		    }
            	}
            }
		    //String sOwnSrDeptOrgId = gwDeptService.selectOrgIdByCabinetcode( dto.getSrcCabinetcode());
			//String sOwnTgDeptOrgId = gwDeptService.selectOrgIdByCabinetcode( dto.getTgCabinetcode());
	
		 	String uFolId ="" ;//원래 상위폴더 ID
		 	String nFolId ="";
		 	
			String targetBoxGubun=dto.getTargetGubun().substring(0,2);
			String targetGubunCp = uTgtMap.get(targetBoxGubun)==null? uTgtMap.get(targetBoxGubun.substring(0,1)):uTgtMap.get(targetBoxGubun);
			
			String lsJobCode="DP";
			String lsJobGubun="";
	
			String s_CabinetCode = dto.getTgCabinetcode();

			boolean isLog=true;//false; //2022.01.12 모든 경우 로그 남기도록 함(이은주 차장 요청, 중요문서함 도입후 다시 고민해보기로)
			boolean isSameType=true;
			
			String s_FolType = dto.getTargetFolType(); //controller에서 변환
            
			List<String> sFolArray=Arrays.asList("");
			List<String> sFilArray=Arrays.asList("");
			
			if( keyStr.equals("6")||!keyStr.substring(0,1).equals("p") && !keyStr.substring(0,1).equals("r")) {
			    sFolArray = dto.getSourceFolders();  //edms_folder   에서 관리되는 대상 폴더들 
			    sFilArray = dto.getSourceFiles();    //edms_doc      에 존재하는 대상 파일들
			}
   
			IDfPersistentObject cpTObj = null;
			if(DfId.isObjectId(topFolId))
			    cpTObj = idfSession.getObject(new DfId(topFolId));

		 	if(sFolArray.size() > 0 && sFolArray.get(0) !="") {
			 	for(int c=0; c < sFolArray.size(); c++) {
	    		 	uFolId =sFolArray.get(c);
	    		 	
	    		 	if(!uFolId.equals(keyStr)) continue;
	    		 	
	    		 	IDfPersistentObject cpFoObj = idfSession.getObject(new DfId(uFolId));
	    		 	String s_FolName = cpFoObj.getString("u_fol_name");
                    
	    		    String sourceBoxGubun=  cpFoObj.getString("u_fol_type").substring(0,2);
	    			lsJobGubun = uSrcMap.get(sourceBoxGubun)==null? uSrcMap.get(sourceBoxGubun.substring(0,1)):uSrcMap.get(sourceBoxGubun);
	    			//dto.setSourceGubun(sourceGubun);

	    			if(dto.getSrcCabinetcode().equals(dto.getTgCabinetcode()) && (!dto.getTargetGubun().equals("POW") && !dto.getTargetGubun().equals("ROW")&& !dto.getTargetGubun().equals("DPC")))
	    			    dto.setTargetSecLevel(cpFoObj.getString("u_sec_level"));//폴더의 보안등급과 같게 프로젝트를 만들예정
	    			
	    			if(targetGubunCp.equals(lsJobGubun)) {
	    				isLog=true; //로그기록대상
	    				//lsJobGubun =sourceGubun;
	    				lsJobCode=lsJobGubun;
	    			}
	    			isSameType=true;
	    			
	    			//소스와 타겟 어느 한쪽이 중요문서함인 경우, edms_doc이나 edms_doc_imp에 새로운 파일을 만들어야 함.
	    			if(!sourceBoxGubun.equals(targetBoxGubun) && (sourceBoxGubun.equals("DI") || sourceBoxGubun.equals("DI")))  {
	    				isSameType=false;
	    			}
	    		 	
				    if(sUprCodeCheck.substring(0,1).equals("P")) {//프로젝트로 복사할 때
				    	//uFolId =
				    	newFolderName = getChekedPjtName(idfSession, dto.getTgCabinetcode(), sFinishYn, s_FolName, dto);
				    	//String sFolId = newFolderName;
				    	//if(!DfId.isObjectId(newFolderName)) {
				    	String sFolId = projectService.createProject(userSession, ProjectCreateDto.builder().uPjtName(newFolderName).uListOpenYn("Y").uFinishYn(sFinishYn).uOwnDept(userSession.getUser().getOrgId()).uFolId(topFolId).uSecLevel(dto.getTargetSecLevel()).uCreateUser(dto.getReqUser()).build(), idfSession);
				    	//}else {
				    	//	idfTg_PObj = idfSession.getObject(new DfId(sFolId));
				    	//	sFolId = idfTg_PObj.getString("u_pjt_code");
				    	//}
				    	folChgPrjMap.put( uFolId  , sFolId);
				    	//topFolId = uFolId;
				    }
				    else if(sUprCodeCheck.substring(0,1).equals("R")) { //연구과제로 복사할 때
				    	newFolderName = getChekedRscsName(idfSession, dto.getTgCabinetcode(), sFinishYn, s_FolName, dto);
				    	//String sFolId = newFolderName;
				    	//if(!DfId.isObjectId(newFolderName)) {
				        String sFolId = researchService.createResearch(userSession, ResearchCreateDto.builder().uRschName(newFolderName).uListOpenYn("Y").uFinishYn(sFinishYn).uOwnDept(userSession.getUser().getOrgId()).uCreateUser(dto.getReqUser()).uFolId(topFolId).uSecLevel(dto.getTargetSecLevel()).build(), idfSession);
				    	//}else {
			    		//    idfTg_PObj = idfSession.getObject(new DfId(newFolderName));
			    		//    sFolId = idfTg_PObj.getString("u_rsch_code");
				    	//}
				    	folChgPrjMap.put( uFolId  , sFolId);
				    }else {
		    		 	
		    			//중복된 폴더명 확인 후 최종 폴더명 반환(root용), 옮기려는 폴더(topFolId)하위에 s_FolName과 같은 이름이 있으면 (1), (2)...로 붙임
				    	
		    			//중복된 폴더명 확인 후 최종 폴더명 반환(root용), 옮기려는 폴더(topFolId)하위에 s_FolName과 같은 이름이 있으면 (1), (2)...로 붙임
				    	if(cpTObj !=null) {
				    	    if( !cpTObj.getType().getName().equals("edms_folder") ) 
		    		 	    {
		    		 	        topFolId = folChgMap.get(uFolId);
		    		 	    }
				    	}
			  			
		    			s_FolName = getChekedFolderName( idfSession, s_CabinetCode, topFolId, s_FolName, dto,"");
		    			
		    			//중복된 명칭의 폴더가 존재하는데, 권한이 있는 경우 해당폴더 id를 반환하며, 권한이 없으면 (1),(2)이런 폴더명이므로
				    	//if(DfId.isObjectId(s_FolName)) { //중복된 폴더가 존재하는데, 쓰기권한이 있는 경우  
				    	//	String nTopFolId = s_FolName ;
				    	//	folChgMap.put(uFolId, nTopFolId) ;//(기존폴더ID, 신규폴더ID)
				    	//	folHapMap.put(uFolId, nTopFolId) ;//(기존폴더ID, 합쳐진폴더ID)
				    	//}else {
			    			//sFolArray에 있는 폴더를 생성 
				            RegistFolderDto fto = RegistFolderDto.builder().uUpFolId(topFolId).uFolName(  s_FolName )
					            .uCabinetCode(s_CabinetCode).uFolType(s_FolType)
					            //.uSecLevel(cpFoObj.getString("u_sec_level"))
					            .uSecLevel(null!=cpFoObj? cpFoObj.getString("u_sec_level"):dto.getTargetSecLevel())
					            .uFolStatus(FolderStatus.ORDINARY.getValue())
					            .uPrCode(dto.getPrCode())
					            .uPrType(dto.getPrType())
					            .uCreateUser(dto.getReqUser())  //생성자
					            .uDeleteStatus("")
					            .build();
				                 
				                 dto.setPreFolderId(uFolId); //직전 폴더 추가권한 상속용
				                 String nTopFolId = folderService.createFolder(idfSession, fto);
						 	     folChgMap.put(uFolId, nTopFolId) ;//(기존폴더ID, 신규폴더ID)
						 	     addMoveAuthBase(userSession, idfSession, nTopFolId, s_CabinetCode, "F", dto, null); //새로만든 폴더에 권한 부여
				    	//}
				    }
			 	}
		 	}

			//파일은 바로 처리 (u_fol_di값으로 topFolId)
		    //  파일생성
			 Map<String, String> formatChkMap = null; // drm적용 대상 확장자
	         formatChkMap = codeService.getClosedFormatCodeMap();
		 	
		 	if(sFilArray.size()  > 0 && sFilArray.get(0) !=""  && iCnt < 1) {  //첫 차수에만 실행한다
			 	for(int c=0; c < sFilArray.size(); c++) {
				     String ls_RObjectId 	= DCTMUtils.getCurrentObjectID(idfSession, sFilArray.get(c)); //문서의 최신버전 r_object_id를 가져온다.
     				 if(ls_RObjectId.equals("")) {
    				     throw new NotFoundException("문서 최신버전이 변경됨[Documentum]");
    				 }
				     IDfDocument idfObj = (IDfDocument)idfSession.getObject(new DfId(ls_RObjectId));
	    		 	 String s_TypeName = idfObj.getType().getName(); //edms_doc인지 edms_doc_imp 이거나		 	

	    		 	 String s_DocName = idfObj.getString("object_name");
				     String s_Extr    = idfObj.getString("u_file_ext");
				     String s_uFolId  = idfObj.getString("u_fol_id");
				     //NONE(1, "N"), BROWSE(2, "B"), READ(3, "R"), RELATE(4, "RELATE"), VERSION(5, "V"), WRITE(6, "W"), DELETE(7, "D");
				     
				     //if( idfObj.getPermit() < 6) continue; //쓰기권한이상이 있으면 복사할 수 있도록  
				     if(null==folChgMap.get( s_uFolId)) {
				         uFolId = topFolId;  //옮기려는 폴더에 같은 이름이 있는지 체크하는 용도
				     }else {
				    	 uFolId = folChgMap.get( s_uFolId);  //옮기려는 폴더에 같은 이름이 있는지 체크하는 용도
				     }
				     if(null !=uFolId  && !uFolId.equals("") ) {				     
					     IDfPersistentObject typeObj = idfSession.getObject(new DfId(uFolId));
					     if(typeObj.getType().getName().equals("edms_project") || typeObj.getType().getName().equals("edms_research")) {//프로젝트나 연구과제로 파일을 직접 복사할 경우
					    	 uFolId = "";
					    	 //s_DocName=getChekedDocNameScnd(idfSession, s_CabinetCode, uFolId, s_DocName, dto);
					     }else {
					    	 //s_DocName=getChekedDocNameScnd(idfSession, s_CabinetCode, uFolId, s_DocName, dto);
					     }
				     }else {
				    	 uFolId = "";
				     }

				     IDfDocument idfNewDoc = null;
				     if(!isSameType) {
				    	 idfNewDoc = s_TypeName.equals("edms_doc")?saveAsNewFiles(idfSession, idfObj, dto):saveAsNewImpFiles(idfSession, idfObj, dto);
				     }else {
				    	 idfNewDoc = saveAsNewFiles(idfSession, idfObj, dto);
				     }
				     String s_NDocName=getChekedDocNameScnd(idfSession, s_CabinetCode, uFolId, s_DocName, dto, s_DocName, s_Extr);
	
				     if (!DfId.isObjectId(s_NDocName)) {
	    				 s_DocName =  s_NDocName ;//s_DocName+"[복사본]";
				     }
                     //생성하자마자 Closed문서가 되는 경우 
    		         if (formatChkMap.containsKey(s_Extr.toUpperCase())) {
    		        	 idfNewDoc.setString("u_doc_status","C");
    		        	 idfNewDoc.setString("u_closer", dto.getReqUser());
    		        	 idfNewDoc.setTime("u_closed_date", new DfTime());
    		         }else {
        				 idfNewDoc.setString("u_doc_status", "L"); // live	(복사시에는 상태를 Live로 . 무조건.)
        	        	 idfNewDoc.setString("u_closer", "");
        	        	 idfNewDoc.setString("u_closed_date", null);
        	        	 idfNewDoc.setString("u_expired_date", null);
    		         }
    		         idfNewDoc.setString("u_takeout_flag" , "0");//반출여부 초기화
    		         idfNewDoc.setString("u_ver_keep_flag", "0");//버전유지여부 초기화
    		         idfNewDoc.setString("u_recycle_date" , "");
    		         idfNewDoc.setString("u_last_editor"  , dto.getReqUser()); 
    		         
	    			//중복된 파일명 확인 후 최종 폴더명 반환(root용)
					 idfNewDoc.setTitle(s_DocName + "."+s_Extr);
	    			 idfNewDoc.setObjectName(s_DocName);
	    			 
				     String registObjId = idfNewDoc.getObjectId().toString();
				     
				     String uDocStatus   = idfNewDoc.getString("u_doc_status");
				     boolean uPrivacyFlag = idfNewDoc.getBoolean("u_privacy_flag");
				     String uSecLevel    = idfNewDoc.getString("u_sec_level").toLowerCase();
                     if (secLevelMap.get(idfNewDoc.getString("u_sec_level")) < secLevelMap.get(dto.getTargetSecLevel())) { //상위폴더의 보안등급이 더 높은 경우
                    	 uSecLevel= dto.getTargetSecLevel().toLowerCase();
                     }
				     
				     String uPrivacyStr ="g";
				     if(uPrivacyFlag) {    
				    	 uPrivacyStr="p";
				         uSecLevel = uSecLevel.equals("s")?"s":"l";
				     }
				     
				     if(!targetBoxGubun.equals("DI")) {
				    	 if(!dto.getPrCode().equals("") && !dto.getPrCode().equals(" ")) {
				    	     String aclName = "a_"+s_CabinetCode+"_"+dto.getPrType().toLowerCase() +"_"+uPrivacyStr+"_"+uSecLevel+"_n";
				    	     idfNewDoc.setACLName(aclName);
				    	 }else {
				    	     String aclName = "a_"+s_CabinetCode+"_d_"+uPrivacyStr+"_"+uSecLevel+"_n"; //부서함
				             idfNewDoc.setACLName(aclName);
				    	 }
				     }else {
			    	     String aclName = "a_"+s_CabinetCode+"_s_g_s_n"; //중요문서함
			    	     idfNewDoc.setACLName(aclName);
				     }
				     idfNewDoc.setOwnerName(idfSession.getDocbaseOwnerName());
				     
				     idfNewDoc.setString("u_pr_code", dto.getPrCode());
				     idfNewDoc.setString("u_pr_type", dto.getPrType());
				     
				     idfNewDoc.setString("u_fol_id", uFolId); //타겟 폴더 ID
				     idfNewDoc.setString("u_doc_key", "" + idfNewDoc.getChronicleId()); // 문서 키
				     idfNewDoc.setString("u_cabinet_code", s_CabinetCode);
				     idfNewDoc.setString("u_reg_source", "D");
				     // 추가 권한자 지정 : 기본 권한이나 공유/협업 대상자 등

				     String s_PreservFlag= psrvPMap.get( idfNewDoc.getString("u_sec_level"));
				     if(null==s_PreservFlag) s_PreservFlag= psrvPMapDf.get(idfNewDoc.getString("u_sec_level")); //등록된게 없으면 정책서에서 기술한 기본값
				     if(dto.getPrType().equals("P") || dto.getPrType().equals("R")) {
				    	 s_PreservFlag= psrvPMap.get( "PJT");
				    	 if(null !=s_PreservFlag) {
				    	     if(s_PreservFlag.equals("1")) 
				    	         s_PreservFlag="0";
				    	 }
				    	 
				     }
				     if(null==s_PreservFlag) s_PreservFlag= idfNewDoc.getString("u_preserve_flag");
				     int li_PreservYear=Integer.parseInt(s_PreservFlag) ;//==0?9999:Integer.parseInt(s_PreservFlag);
				     idfNewDoc.setInt("u_preserve_flag", li_PreservYear );  //보존연한
				     
				     if (idfNewDoc.getString("u_doc_status").equals("C") ) { //Closed문서인 경우에만 만료일자 계산
							 IDfTime startDate = new DfTime() ;
							 // Convert the expiration date to a calendar object.
							 GregorianCalendar cal = new GregorianCalendar(startDate.getYear(), startDate.getMonth(),   startDate.getDay() );  
							 // Add the number of months ­1 (months start counting from 0).
							 cal.add(GregorianCalendar.MONTH, (12 * li_PreservYear) -1) ;
							 IDfTime expireDate = new DfTime (cal.getTime()) ;
						     if(li_PreservYear==0)
						    	 idfNewDoc.setString("u_expired_date",  "9999-12-31 00:00:00");
						     else
						         idfNewDoc.setString("u_expired_date",  expireDate.toString());
				     }
				     if(idfNewDoc.isCheckedOut()) {
				    	 idfNewDoc.cancelCheckout();
				     }
/*				     
				     
			         idfNewDoc.setInt("u_preserve_flag",  dto.getUserPreset().getUPreserveFlag()); // 보존년한
			         if (dto.getUserPreset().getUOpenFlag() != null) {
			           idfNewDoc.setString("u_open_flag", dto.getUserPreset().getUOpenFlag());
			         }
			         if (dto.getUserPreset().getUMailPermitFlag() != null) {
			           idfNewDoc.setBoolean("u_auto_auth_mail_flag", "Y".equals(dto.getUserPreset().getUMailPermitFlag())?true:false);
			         }
*/		         
				     //idfNewDoc.link(dto.getTargetDboxId()); //타켓 폴더와 연결
				     //dto.setPreFolderId(" ");
				     idfNewDoc.save();
				     addMoveAuthBase( userSession,idfSession, idfNewDoc.getChronicleId().getId(), s_CabinetCode, "D", dto, idfNewDoc);
				     //setDocAuthBase( idfSession, mDocKey, idfNewDoc,  s_CabinetCode,userSession.getUser().getUserId());
//복사처리				     
				     
				     if(isLog) {
							if( lsJobGubun.equals("DEL")) lsJobGubun =uDocStatus.equals("L") ? "LD" : "DR";
							else lsJobGubun ="DP";
					    	 
	  	    			 LogDoc logDoc = LogDoc.builder()
	 	    			          .uJobCode( lsJobCode)
	 	    			          .uDocId(idfObj.getString("r_object_id"))
	 	    			          .uDocKey(idfObj.getString("u_doc_key"))
	 	    			          .uDocName(idfObj.getString("title").replaceAll("'", "''"))
	 	    			          .uDocVersion(Integer.parseInt(idfObj.getVersionLabel(0).substring(0, idfObj.getVersionLabel(0).indexOf(".")))+"")
	 	    			          .uFileSize(Long.parseLong(idfObj.getString("r_content_size")))
	 	    			          .uOwnDeptcode(dto.getOwnSrDeptOrgId())
	 	    			          .uActDeptCode(userSession.getUser().getOrgId())
	 	    			          .uJobUser(dto.getReqUser())
	 	    			          .uJobUserType(dto.getUJobUserType()==null ? "P" : dto.getUJobUserType()) //작업자구분(P:개인, D:부서관리자, C:사별관리자, G:그룹관리자, S:시스템)
	 	    			          .uDocStatus(idfObj.getString("u_doc_status"))
	 	    			          .uSecLevel(idfObj.getString("u_sec_level"))
	 	    			          .uCabinetCode(idfObj.getString("u_cabinet_code"))
	 	    			          //.uJobGubun(lsJobGubun)
	 	    			          .uUserIp(dto.getReqUserIp())							// 받아야함.
	 	    			          .uAttachSystem("")
	 	    			          .build();
	 	    			      insertLog(logDoc);
					     }    				     
			 	}
		 	}
//복사처리		 	
	    	for(int i=0; i < folList.size(); i++) {

	    		uFolId = folList.get(i);//상위폴더 ID
	    		if(uFolId.equals(" ")) continue;
	    		if(null==uFolId) continue;
	    		String projCd = folChgPrjMap.get(uFolId);

	    	    if(null !=projCd ) {
	    			dto.setPrCode(projCd);
	    			dto.setPrType(dto.getTargetGubun().substring(0,1));
	    		}else{
		    		nFolId = folChgMap.get(uFolId);
		    		if(nFolId==null || nFolId=="") {
		    			IDfPersistentObject cpFoObj = idfSession.getObject(new DfId(uFolId));
		    			String upFolId = cpFoObj.getString("u_up_fol_id");
		    			String newUFolId = folChgMap.get(upFolId);
                        if( keyStr.substring(0,1).equals("p")|| keyStr.substring(0,1).equals("r")) {
		    			   if(null==newUFolId ) {
		    				   newUFolId = folChgMap.get(keyStr);
		    			   }
		    		    }
                        String s_FolName = cpFoObj.getString("u_fol_name");
		    			s_FolName = getChekedFolderName( idfSession, s_CabinetCode, newUFolId, s_FolName, dto,"");
		    			
		    			//중복된 명칭의 폴더가 존재하는데, 권한이 있는 경우 해당폴더 id를 반환하며, 권한이 없으면 (1),(2)이런 폴더명이므로
				    	//if(DfId.isObjectId(s_FolName)) { //중복된 폴더가 존재하는데, 쓰기권한이 있는 경우  
				    	//	String nTopFolId = s_FolName ;
				    	//	folChgMap.put(uFolId, nTopFolId) ;//(기존폴더ID, 신규폴더ID)
				    	//	folHapMap.put(uFolId, nTopFolId) ;//(기존폴더ID, 합쳐진폴더ID)
				    	//}else {
                        
						 	//sFolArray에 있는 폴더를 생성
				            RegistFolderDto fto = RegistFolderDto.builder().uUpFolId(newUFolId).uFolName(  cpFoObj.getString("u_fol_name"))
					            .uCabinetCode(s_CabinetCode).uFolType( s_FolType)
					            .uSecLevel(null!=cpFoObj? cpFoObj.getString("u_sec_level"):dto.getTargetSecLevel())
					            .uFolStatus(FolderStatus.ORDINARY.getValue())
					            .uCreateUser(dto.getReqUser())
					            .uPrCode(dto.getPrCode())
					            .uPrType(dto.getPrType())
					            .uDeleteStatus("")
					            .build();
				                   dto.setPreFolderId(uFolId); //직전 폴더 추가권한 상속용
						 	       nFolId = folderService.createFolder(idfSession, fto);
						 	       folChgMap.put(uFolId, nFolId) ;//(기존폴더ID, 신규폴더ID)
						 	       //setFolAuthBase( idfSession, upFolId,  dto.getTgCabinetcode(), dto.getReqUser());
						 	       //setFolAuthBase(idfSession, topFolId, nFolId, s_CabinetCode, userSession.getUser().getUserId());
						 	       addMoveAuthBase( userSession,idfSession, nFolId, s_CabinetCode,  "F", dto, null);
				    	//}
		    		}	
	    		}
	    		List<DPath> folBList = folMap.get(uFolId);
    			
	    		if(null!=folBList ) {
		    		for(int j=0; j < folBList.size(); j++) {
		    			DPath dData = folBList.get(j);
		    			if(dData.getListType().equals("FOL")) {//폴더("FOL" 인 경우)
		    				String subFolId = dData.getRObjectId();
		    	    		
		    				projCd = folChgPrjMap.get(subFolId);
		    				nFolId = folChgMap.get(subFolId);
		    	    		if(null !=projCd) {
		    	    			dto.setPrCode(projCd);
		    	    			dto.setPrType(dto.getTargetGubun().substring(0,1));
		    	    		}else {
			    				if(nFolId==null || nFolId=="") {
			    	    			IDfPersistentObject cpFoObj = idfSession.getObject(new DfId(subFolId));
			    	    			String upFolId = cpFoObj.getString("u_up_fol_id");
			    	    			String newUFolId = folChgMap.get(upFolId); //상위폴더의 새로만들어진 id
			
			                        String s_FolName = cpFoObj.getString("u_fol_name");
					    			s_FolName = getChekedFolderName( idfSession, s_CabinetCode, newUFolId, s_FolName, dto,"");
					    			
					    			//중복된 명칭의 폴더가 존재하는데, 권한이 있는 경우 해당폴더 id를 반환하며, 권한이 없으면 (1),(2)이런 폴더명이므로
							    	if(DfId.isObjectId(s_FolName)) { //중복된 폴더가 존재하는데, 쓰기권한이 있는 경우  
							    		String nTopFolId = s_FolName ;
							    		folChgMap.put(subFolId, nTopFolId) ;//(기존폴더ID, 신규폴더ID)
							    		folHapMap.put(subFolId, nTopFolId) ;//(기존폴더ID, 합쳐진폴더ID)

							    	}else {
				    				 	//sFolArray에 있는 폴더를 생성
				    		            RegistFolderDto fto = RegistFolderDto.builder().uUpFolId(newUFolId).uFolName(  cpFoObj.getString("u_fol_name"))
				    			            .uCabinetCode(s_CabinetCode).uFolType(s_FolType)
				    			            //.uSecLevel(null==dto.getTargetSecLevel()? cpFoObj.getString("u_sec_level"):dto.getTargetSecLevel())
				    			            .uSecLevel(null!=cpFoObj? cpFoObj.getString("u_sec_level"):dto.getTargetSecLevel())
				    			            .uFolStatus(FolderStatus.ORDINARY.getValue())
				    			            .uCreateUser(userSession.getUser().getUserId())
				    			            .uPrCode(dto.getPrCode())
				    			            .uPrType(dto.getPrType())
				    			            .uDeleteStatus("")
				    			            .build();
				    		                   dto.setPreFolderId(subFolId); //직전 폴더 추가권한 상속용
				    				 	       nFolId = folderService.createFolder(idfSession, fto);
				    				 	       folChgMap.put(subFolId, nFolId) ;//(기존폴더ID, 신규폴더ID)
				    				 	      //setFolAuthBase(idfSession, topFolId, nFolId, s_CabinetCode, userSession.getUser().getUserId());
				    				 	      addMoveAuthBase( userSession,idfSession, nFolId, s_CabinetCode, "F", dto, null);
							    	}
			    				}
		    	    		}
		    			}else if(dData.getListType().equals("LNK")) {//링크파일인 경우
		    				
		    				setDocLink(userSession, idfSession, nFolId, dData.getRObjectId(), dto ,isSameType, folChgMap, folHapMap ) ; //링크파일은 복사시 원본파일을 복사해줌. (상무님. 11.29 회의시)
		    				
		    			}else { //문서("DOC" 인 경우)
		    			    // nFileId = createCopyDoc(...);
		    			    //U_FOL_ID 값에 nFolId인자값을 넣어줌
						     String ls_RObjectId 	= DCTMUtils.getCurrentObjectID(idfSession, dData.getRObjectId()); //문서의 최신버전 r_object_id를 가져온다.
		        			 if(ls_RObjectId.equals("")) {
		        			    throw new NotFoundException("문서 최신버전이 변경됨[Documentum]");
		        			 }
						     
		        			 ls_RObjectId=ls_RObjectId==""?dData.getRObjectId():ls_RObjectId;
						     IDfDocument idfObj = (IDfDocument)idfSession.getObject(new DfId(ls_RObjectId));
						     String s_TypeName = idfObj.getType().getName(); //edms_doc인지 edms_doc_imp 이거나
						     
			    			 IDfDocument idfNewDoc = null;
						     if(!isSameType) {
						    	 idfNewDoc = s_TypeName.equals("edms_doc")?saveAsNewFiles(idfSession, idfObj, dto):saveAsNewImpFiles(idfSession, idfObj, dto);
						     }else {
						    	 idfNewDoc = saveAsNewFiles(idfSession, idfObj, dto);
						     }
						     
						     String registObjId = idfNewDoc.getObjectId().toString();
						     String s_DocName = idfObj.getString("object_name");
		    				 String s_Extr    = idfNewDoc.getString("u_file_ext");
			    			 
						     if( null!=folHapMap.get(uFolId)) {
						         String s_NDocName=getChekedDocNameScnd(idfSession, s_CabinetCode, uFolId, s_DocName, dto, s_DocName, s_Extr);

				    		     if (!DfId.isObjectId(s_NDocName)) {				    				 
				    				 s_DocName = s_NDocName ;//idfObj.getString("object_name")+"[복사본]";
				    			 }
						     }
		                     //생성하자마자 Closed문서가 되는 경우 
		    		         if (formatChkMap.containsKey(s_Extr.toUpperCase())) {
		    		        	 idfNewDoc.setString("u_doc_status","C");
		    		        	 idfNewDoc.setString("u_closer", dto.getReqUser());
		    		        	 idfNewDoc.setTime("u_closed_date", new DfTime());
		    		         }else {
			    				 idfNewDoc.setString("u_doc_status", "L"); // live
		    		        	 idfNewDoc.setString("u_closer", "");
		    		        	 idfNewDoc.setString("u_closed_date", null);
		    		        	 idfNewDoc.setString("u_expired_date", null);
		    		         }
			    			//중복된 파일명 확인 후 최종 폴더명 반환(root용)
			    			 idfNewDoc.setObjectName (s_DocName);
			    			 idfNewDoc.setTitle (s_DocName+"." + s_Extr);
						     
						     String uDocStatus   = idfNewDoc.getString("u_doc_status");
						     boolean uPrivacyFlag = idfNewDoc.getBoolean("u_privacy_flag");
						     String uSecLevel    = idfNewDoc.getString("u_sec_level").toLowerCase();

						     if (secLevelMap.get(idfNewDoc.getString("u_sec_level")) < secLevelMap.get(dto.getTargetSecLevel())) { //상위폴더의 보안등급이 더 높은 경우
						    	 uSecLevel=  dto.getTargetSecLevel().toLowerCase();
		                     }
						     
						     String uPrivacyStr ="g";
						     if(uPrivacyFlag) {    
						    	 uPrivacyStr="p";
						         uSecLevel = uSecLevel.equals("s")?"s":"l";
						     }
						     
						     if(!targetBoxGubun.equals("DI")) {
						    	 if(!dto.getPrCode().equals("") && !dto.getPrCode().equals(" ")) {
						    	     String aclName = "a_"+s_CabinetCode+"_"+dto.getPrType().toLowerCase() +"_"+uPrivacyStr+"_"+uSecLevel+"_n";
						    	     idfNewDoc.setACLName(aclName);
						    	 }else {
						    	     String aclName = "a_"+s_CabinetCode+"_d_"+uPrivacyStr+"_"+uSecLevel+"_n"; //부서함
						             idfNewDoc.setACLName(aclName);
						    	 }
						     }else {
					    	     String aclName = "a_"+s_CabinetCode+"_s_g_s_n"; //중요문서함
					    	     idfNewDoc.setACLName(aclName);
						     }


						     nFolId = folChgMap.get(idfObj.getString("u_fol_id"));
						     idfNewDoc.setOwnerName(idfSession.getDocbaseOwnerName());
						     idfNewDoc.setString("u_fol_id", nFolId); //타겟 폴더 ID
						     idfNewDoc.setString("u_doc_key", "" + idfNewDoc.getChronicleId()); // 문서 키
						     idfNewDoc.setString("u_reg_source", "D");
						     idfNewDoc.setString("u_last_editor"  , dto.getReqUser());
//복사처리						     
						     String s_PreservFlag= psrvPMap.get( idfNewDoc.getString("u_sec_level"));
						     if(null==s_PreservFlag) s_PreservFlag= psrvPMapDf.get(idfNewDoc.getString("u_sec_level")); //등록된게 없으면 정책서에서 기술한 기본값
						     if(dto.getPrType().equals("P") || dto.getPrType().equals("R")) {
						    	 s_PreservFlag= psrvPMap.get( "PJT");
						    	 if(null !=s_PreservFlag) {
						    		 if(s_PreservFlag.equals("1"))
						    	         s_PreservFlag="0";
						    	 }
						     }
						     if(null==s_PreservFlag) s_PreservFlag= idfNewDoc.getString("u_preserve_flag");
						     int li_PreservYear=Integer.parseInt(s_PreservFlag) ;//==0?9999:Integer.parseInt(s_PreservFlag);
						     idfNewDoc.setInt("u_preserve_flag", li_PreservYear );  //보존연한

						     // 추가 권한자 지정 : 기본 권한이나 공유/협업 대상자 등
						     if (idfNewDoc.getString("u_doc_status").equals("C") ) { //Closed문서인 경우에만 만료일자 계산  
								     IDfTime startDate = new DfTime() ;
  								     // Convert the expiration date to a calendar object.
								     GregorianCalendar cal = new GregorianCalendar(startDate.getYear(), startDate.getMonth(),   startDate.getDay() );  
								     // Add the number of months ­1 (months start counting from 0).
								     cal.add(GregorianCalendar.MONTH, (12 * li_PreservYear) -1) ;
								     IDfTime expireDate = new DfTime (cal.getTime()) ;
								     if(li_PreservYear==0)
								    	 idfNewDoc.setString("u_expired_date",  "9999-12-31 00:00:00");
								     else
								         idfNewDoc.setString("u_expired_date",  expireDate.toString());
								     
						     }
/*						     
					         idfNewDoc.setInt("u_preserve_flag",  dto.getUserPreset().getUPreserveFlag()); // 보존년한
					         if (dto.getUserPreset().getUOpenFlag() != null) {
					           idfNewDoc.setString("u_open_flag", dto.getUserPreset().getUOpenFlag());
					         }
					         if (dto.getUserPreset().getUMailPermitFlag() != null) {
					           idfNewDoc.setBoolean("u_auto_auth_mail_flag", "Y".equals(dto.getUserPreset().getUMailPermitFlag())?true:false);
					         }
*/						     
						     idfNewDoc.setString("u_pr_code", dto.getPrCode());
						     idfNewDoc.setString("u_pr_type", dto.getPrType());
						     
						     idfNewDoc.setString("u_cabinet_code", s_CabinetCode);
						     if(idfNewDoc.isCheckedOut()) {
						    	 idfNewDoc.cancelCheckout();
						     }
						     
						     idfNewDoc.save();
						     
			    		     String s_DCTMFolderId = DCTMUtils.makeEDMFolder(idfSession);
						     
						     //setDocAuthBase( idfSession, sDocKey, idfNewDoc,  s_CabinetCode, userSession.getUser().getUserId());
						     dto.setPreFolderId(  idfNewDoc.getString("u_copy_org_id"));
						     addMoveAuthBase( userSession,idfSession, idfNewDoc.getChronicleId().getId(), s_CabinetCode, "D", dto, idfNewDoc);
						     
		  				     if(isLog) {
		   						if( lsJobGubun.equals("DEL")) lsJobGubun =uDocStatus.equals("L") ? "LD" : "DR";
		   						else lsJobGubun ="DP";
		   				    	 
		 	  	    			 LogDoc logDoc = LogDoc.builder()
		 	 	    			          .uJobCode( lsJobCode)
		 	 	    			          .uDocId(idfObj.getString("r_object_id"))
		 	 	    			          .uDocKey(idfObj.getString("u_doc_key"))
		 	 	    			          .uDocName(idfObj.getString("title").replaceAll("'", "''"))
		 	 	    			          .uDocVersion(Integer.parseInt(idfObj.getVersionLabel(0).substring(0, idfObj.getVersionLabel(0).indexOf(".")))+"")
		 	 	    			          .uFileSize(Long.parseLong(idfObj.getString("r_content_size")))
		 	 	    			          .uOwnDeptcode(dto.getOwnSrDeptOrgId())
		 	 	    			          .uActDeptCode(userSession.getUser().getOrgId())
		 	 	    			          .uJobUser(dto.getReqUser())
		 	 	    			          .uJobUserType(dto.getUJobUserType()==null ? "P" : dto.getUJobUserType()) //작업자구분(P:개인, D:부서관리자, C:사별관리자, G:그룹관리자, S:시스템)
		 	 	    			          .uDocStatus(idfObj.getString("u_doc_status"))
		 	 	    			          .uSecLevel(idfObj.getString("u_sec_level"))
		 	 	    			          .uCabinetCode(idfObj.getString("u_cabinet_code"))
		 	 	    			          //.uJobGubun(lsJobGubun)
		 	 	    			          .uUserIp(dto.getReqUserIp())							// 받아야함.
		 	 	    			          .uAttachSystem("")
		 	 	    			          .build();
		 	 	    			      insertLog(logDoc);
		   				     }
		    			}		    			
		    		}
	    		}
	    	}
	
			return "";
    }
  
    /**
     * 파일 복사
     * */
    @Override
    public IDfDocument saveAsNewFiles(IDfSession idfSession, IDfDocument idfDoc, DPath dto) throws Exception{

    	IDfId idfId = idfDoc.saveAsNew(true);
    	String ls_RobjectId = idfId.getId();
    	
    	IDfDocument oNewSO = (IDfDocument)idfSession.getObject(new DfId(ls_RobjectId));
    	

    	oNewSO.setString("u_last_editor", ""); //마지막 편집자 삭제
		
		//String s_MDepts = idf_PjtObj.getAllRepeatingStrings("u_own_dept", ",");
		
	    oNewSO.unlink( oNewSO.getFolderId(oNewSO.getFolderIdCount()-1).toString());
 		//---------------------------------------------
	    String s_DCTMFolderId = DCTMUtils.makeEDMFolder(idfSession);
		oNewSO.link(s_DCTMFolderId);
		
        if(dto.getUptPthGbn().equals("C")) {
    	    oNewSO.setString("u_copy_org_id", idfDoc.getChronicleId()+""); //복사의 경우 원본파일 id를 기록해준다
        }
        
        oNewSO.truncate("u_wf_key",           1);    //최초 작성자를 유지하고 나머지 사용자들은 truncate한다
        
        int i_ValIdx = oNewSO.findString("u_editor", dto.getReqUser());
		if(i_ValIdx < 0){ //기존 편집자에 없으면 append해준다
	        oNewSO.appendString("u_editor"  , dto.getReqUser());
		}
		oNewSO.setString("r_creator_name", dto.getReqUser());
        oNewSO.setString("u_reg_user", dto.getReqUser());
        oNewSO.setString("u_reg_date", (new DfTime()).toString());
        oNewSO.setString("u_update_date", (new DfTime()).toString());
       
        oNewSO.setString("u_wf_doc_yn", "N");
        oNewSO.truncate("u_wf_key",           0);    //결재 ID
	    oNewSO.truncate("u_wf_system",        0);    //결재 시스템
	    oNewSO.truncate("u_wf_form",          0);    //결재 양식명
        oNewSO.truncate("u_wf_title",         0);    //결재 제목
        oNewSO.truncate("u_wf_approver",      0);    //결재자
        oNewSO.truncate("u_wf_approval_date", 0); 
        oNewSO.truncate("u_wf_link",          0);    //결재화면 URL
        
        oNewSO.setBoolean("u_privacy_flag",      false);   //개인정보포함여부 unChk
        oNewSO.setBoolean("u_auto_auth_mail_flag",true);    //메일자동권한부여 Chk


        if(!idfDoc.getString("u_cabinet_code").equals(dto.getTgCabinetcode()))
            oNewSO.setString("u_cabinet_code", dto.getTgCabinetcode()); // 문서함코드(다른 부서 복사시)
	    
	    //edms_auth_base의 소유부서(기본부서)인 것을 지움
		//기존 권한 유지를 위해 권한자 목록 가져와서 ACL 부여
    	
    	return oNewSO;

    }
    
    /**
     * 파일 복사(중요문서함으로) : edms_doc이나 edms_doc_imp 서로 문서내용을 복사해서 새 문서를 만듬 
     * */
    @Override
    public IDfDocument saveAsNewImpFiles(IDfSession idfSession, IDfDocument idfDoc, DPath dto) throws Exception{

    	String targetBoxGubun=dto.getTargetGubun().substring(0,2);
    	String typeStr = targetBoxGubun.equals("DI")?SysObjectType.DOC_IMP.getValue():SysObjectType.DOC.getValue();

    	IDfDocument oNewSO = (IDfDocument) idfSession.newObject( typeStr);
        if(dto.getUptPthGbn().equals("C")) {
        	oNewSO.setString("u_copy_org_id", idfDoc.getChronicleId()+""); //복사의 경우 원본파일 id를 기록해준다
        }
        oNewSO.setString("u_last_editor", ""); //마지막 편집자 삭제
        // 파일 set
//        idfNewDoc.setFile(idfDoc.get);

        oNewSO.setContentType(idfDoc.getContentType());
        oNewSO.setContent(CommonUtils.convertByteInputStreamToOut(idfDoc.getContent()));

        // 포맷
        oNewSO.setContentType(idfDoc.getContentType());
        // 소유자 지정 : Docbase Owner로 지정
        oNewSO.setOwnerName(idfSession.getDocbaseOwnerName());
        // 업무 속성 지정
        oNewSO.setString("u_pr_code", idfDoc.getString("u_pr_code")); 
        oNewSO.setString("u_pr_type", idfDoc.getString("u_pr_type")); 
        
        if(!idfDoc.getString("u_cabinet_code").equals(dto.getTgCabinetcode()))
            oNewSO.setString("u_cabinet_code", dto.getTgCabinetcode()); // 문서함코드(다른 부서 복사시)
        
        
        oNewSO.setString("u_doc_key", "" + oNewSO.getChronicleId()); // 문서 키
        oNewSO.setString("u_doc_flag", "S"); // 일반문서, M:마이그레이션문서
        
        oNewSO.setString("u_wf_doc_yn", "N");
        oNewSO.truncate("u_wf_key",           0);    //결재 ID
	    oNewSO.truncate("u_wf_system",        0);    //결재 시스템
	    oNewSO.truncate("u_wf_form",          0);    //결재 양식명
        oNewSO.truncate("u_wf_title",         0);    //결재 제목
        oNewSO.truncate("u_wf_approver",      0);    //결재자
        oNewSO.truncate("u_wf_approval_date", 0); 
        oNewSO.truncate("u_wf_link",          0);    //결재화면 URL
        
        oNewSO.truncate("u_wf_key",           1);    //최초 작성자를 유지하고 나머지 사용자들은 truncate한다
        int i_ValIdx = oNewSO.findString("u_editor", dto.getReqUser());
		if(i_ValIdx < 0){ //기존 편집자에 없으면 append해준다
	        oNewSO.appendString("u_editor"  , dto.getReqUser());
		}
        
        
        oNewSO.setBoolean("u_privacy_flag",      false);   //개인정보포함여부 unChk
        oNewSO.setBoolean("u_auto_auth_mail_flag",true);   //메일자동권한부여 Chk
        
        oNewSO.setString("u_sec_level", targetBoxGubun.equals("DI")?"S":idfDoc.getString("u_sec_level")); // 보안등급:제한문서
        oNewSO.setString("u_doc_status", DocStatus.LIVE.getValue()); // live
        oNewSO.setString("r_creator_name", dto.getReqUser());
        oNewSO.setString("u_reg_user", dto.getReqUser());
        oNewSO.setString("u_reg_date", (new DfTime()).toString());
        oNewSO.setString("u_update_date", (new DfTime()).toString());
        
	    String s_DCTMFolderId = DCTMUtils.makeEDMFolder(idfSession);
	    //oNewSO.unlink(oNewSO.getString("i_folder_id"));
	    oNewSO.link(s_DCTMFolderId);
        oNewSO.setString("u_fol_id",   dto.getTargetDboxId()); // 부서폴더
        
    	return oNewSO;

    }

    private void setFolAuthBase(IDfSession idfSession, String topFolId, String newFolId, String ps_RcevCab, String crUser) throws Exception{
    	try {
/*    		
	    	IDfCollection idf_Col=null;
			String s_Dql = "select * from edms_auth_base " +
						" where u_obj_id = '" + topFolId + "' " ;
				
			idf_Col = DCTMUtils.getCollectionByDQL(idfSession, s_Dql, DfQuery.DF_QUERY);
			
			IDfPersistentObject	idf_PObj	= null;
			while(idf_Col != null && idf_Col.next())
			{
	  			//--------------------------------------------------
	  			// 문서권한 기본(edms_auth_base) 정보 생성
	  			//--------------------------------------------------
  				idf_PObj = idfSession.newObject("edms_auth_base");
  				
  				//if(idf_PObj.getString("u_doc_status").equals("C") && idf_PObj.getString("u_author_type").equals("S")) continue;
  				
  				idf_PObj.setString("u_obj_id"		, newFolId);
  				idf_PObj.setString("u_obj_type"		, idf_Col.getString("u_obj_type"));
  				idf_PObj.setString("u_doc_status"	, idf_Col.getString("u_doc_status"));
  				idf_PObj.setString("u_permit_type"	, idf_Col.getString("u_permit_type"));
  				idf_PObj.setString("u_own_dept_yn"	, idf_Col.getString("u_own_dept_yn"));
  				idf_PObj.setString("u_author_id"	, idf_Col.getString("u_author_id"));
  				idf_PObj.setString("u_author_type"	, idf_Col.getString("u_author_type"));
  				idf_PObj.setString("u_create_user"	, crUser);
  				idf_PObj.setTime  ("u_create_date"	, new DfTime());
  				idf_PObj.save();
			}
			if(idf_Col != null) idf_Col.close();
*/			
    	} catch (Exception e) {
			e.printStackTrace();
	    }    	
    }
    
    private void setDocLink(UserSession userSession, IDfSession idfSession, String uFolId, String rObjectId, DPath dto, boolean isSameType, Map<String, String> folChgMap,Map<String, String> folHapMap) throws Exception{
    	try {

			//IDfDocument idfDoc = (IDfDocument)idfSession.getObject(new DfId(rObjectId));
			IDfPersistentObject idf_PObj = idfSession.getObject(new DfId(rObjectId)); 
/*
			//--------------------------------------------------
  			// 문서권한 기본(edms_doc_link) 정보 생성
  			//--------------------------------------------------
			IDfPersistentObject	idf_PObj	= null;
			idf_PObj = idfSession.newObject("edms_doc_link");

			idf_PObj.setString("u_doc_key",          idfDoc.getString("u_doc_key")); //문서번호
			idf_PObj.setString("u_cabinet_code",     dto.getTgCabinetcode() ); //문서함코드
				
			idf_PObj.setString("u_fol_id",           uFolId           ); //변경전 폴더 id, saveWfList이후에  전자결재폴더id로 바꿔서 edms_doc에 저장( link테이블에 있는 u_fol_id가 이전폴더id가 됨 )
			idf_PObj.setString("u_link_type",        null             ); //링크종류', SET COMMENT_TEXT='W:결재')
				
			idf_PObj.setString("u_create_user",      dto.getReqUser());
			idf_PObj.setString("u_create_date",      (new DfTime()).toString());

			idf_PObj.save();
*/
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			
			 Map<String, String> formatChkMap = null; // drm적용 대상 확장자
	         formatChkMap = codeService.getClosedFormatCodeMap();
			
			String targetBoxGubun=dto.getTargetGubun().substring(0,2);
			String ls_RObjectId 	= DCTMUtils.getCurrentObjectID(idfSession, idf_PObj.getString("u_doc_key")); //문서의 최신버전 r_object_id를 가져온다.
			 if(ls_RObjectId.equals("")) {
			    throw new NotFoundException("문서 최신버전이 변경됨[Documentum]");
			 }

		     IDfDocument idfObj = (IDfDocument)idfSession.getObject(new DfId(ls_RObjectId));
		     String s_TypeName = idfObj.getType().getName(); //edms_doc인지 edms_doc_imp 이거나
		     
			 IDfDocument idfNewDoc = null;
		     if(!isSameType) {
		    	 idfNewDoc = s_TypeName.equals("edms_doc")?saveAsNewFiles(idfSession, idfObj, dto):saveAsNewImpFiles(idfSession, idfObj, dto);
		     }else {
		    	 idfNewDoc = saveAsNewFiles(idfSession, idfObj, dto);
		     }
		     
		     String registObjId = idfNewDoc.getObjectId().toString();
		     String s_DocName = idfObj.getString("object_name");
			 String s_Extr    = idfNewDoc.getString("u_file_ext");
			 String s_CabinetCode = dto.getTgCabinetcode();
					 
		     if( null!=folHapMap.get(uFolId)) {
		         String s_NDocName=getChekedDocNameScnd(idfSession, s_CabinetCode, uFolId, s_DocName, dto, s_DocName, s_Extr);

	   			 //if (!s_DocName.equals(s_NDocName)) {
	   			 if (!DfId.isObjectId(s_NDocName)) {
	   				 s_DocName = s_NDocName ;//idfObj.getString("object_name")+"[복사본]";
	   			 }
		     }
			//중복된 파일명 확인 후 최종 폴더명 반환(root용)
			 idfNewDoc.setObjectName (s_DocName);
			 idfNewDoc.setTitle (s_DocName+"." + s_Extr);
			 
		     String uDocStatus   = idfNewDoc.getString("u_doc_status");
		     boolean uPrivacyFlag = idfNewDoc.getBoolean("u_privacy_flag");
		     String uSecLevel    = idfNewDoc.getString("u_sec_level").toLowerCase();
	         
		     if (secLevelMap.get(idfNewDoc.getString("u_sec_level")) < secLevelMap.get(dto.getTargetSecLevel())) { //상위폴더의 보안등급이 더 높은 경우
	            	uSecLevel=  dto.getTargetSecLevel().toLowerCase();
	         }
		     String nFolId = folChgMap.get(idfObj.getString("u_fol_id"));
		     idfNewDoc.setOwnerName(idfSession.getDocbaseOwnerName());
		     idfNewDoc.setString("u_fol_id", nFolId); //타겟 폴더 ID
		     idfNewDoc.setString("u_doc_key", "" + idfNewDoc.getChronicleId()); // 문서 키
		     idfNewDoc.setString("u_reg_source", "D");
		     
             //생성하자마자 Closed문서가 되는 경우 
	         if (formatChkMap.containsKey(s_Extr.toUpperCase())) {
	        	 idfNewDoc.setString("u_doc_status","C");
	        	 idfNewDoc.setString("u_closer", dto.getReqUser());
	        	 idfNewDoc.setTime("u_closed_date", new DfTime());
	         }else {
				 idfNewDoc.setString("u_doc_status", "L"); // live
	        	 idfNewDoc.setString("u_closer", "");
	        	 idfNewDoc.setString("u_closed_date", null);
	        	 idfNewDoc.setString("u_expired_date", null);
				 
	         }
	         idfNewDoc.setString("u_takeout_flag" , "0");//반출여부 초기화
	         idfNewDoc.setString("u_ver_keep_flag", "0");//버전유지여부 초기화
	         idfNewDoc.setString("u_recycle_date" , "");
	         idfNewDoc.setString("u_last_editor"  , dto.getReqUser());		     
	         
		     String uPrivacyStr ="g";
		     if(uPrivacyFlag) {    
		    	 uPrivacyStr="p";
		         uSecLevel = uSecLevel.equals("s")?"s":"l";
		     }
		     
		     if(!targetBoxGubun.equals("DI")) {
		    	 if(!dto.getPrCode().equals("") && !dto.getPrCode().equals(" ")) {
		    	     String aclName = "a_"+s_CabinetCode+"_"+dto.getPrType().toLowerCase() +"_"+uPrivacyStr+"_"+uSecLevel+"_n";
		    	     idfNewDoc.setACLName(aclName);
		    	 }else {
		    	     String aclName = "a_"+s_CabinetCode+"_d_"+uPrivacyStr+"_"+uSecLevel+"_n"; //부서함
		             idfNewDoc.setACLName(aclName);
		    	 }
		     }else {
	    	     String aclName = "a_"+s_CabinetCode+"_s_g_s_n"; //중요문서함
	    	     idfNewDoc.setACLName(aclName);
		     }
		     
//복사처리						     
		     String s_PreservFlag= psrvPMap.get( idfNewDoc.getString("u_sec_level"));
		     if(null==s_PreservFlag) s_PreservFlag= psrvPMapDf.get(idfNewDoc.getString("u_sec_level")); //등록된게 없으면 정책서에서 기술한 기본값
		     if(dto.getPrType().equals("P") || dto.getPrType().equals("R")) {
		    	 s_PreservFlag= psrvPMap.get( "PJT");
		    	 if(null !=s_PreservFlag) {
		    	     if(s_PreservFlag.equals("1")) 
		    	    	 s_PreservFlag="0";
		    	 }
		     }
		     if(null==s_PreservFlag) s_PreservFlag= idfNewDoc.getString("u_preserve_flag");
		     int li_PreservYear=Integer.parseInt(s_PreservFlag) ;//==0?9999:Integer.parseInt(s_PreservFlag);
		     idfNewDoc.setInt("u_preserve_flag", li_PreservYear );  //보존연한
		     // 추가 권한자 지정 : 기본 권한이나 공유/협업 대상자 등
		     if (idfNewDoc.getString("u_doc_status").equals("C") ) { //Closed문서인 경우에만 만료일자 계산  
				     IDfTime startDate = new DfTime() ;
				     // Convert the expiration date to a calendar object.
				     GregorianCalendar cal = new GregorianCalendar(startDate.getYear(), startDate.getMonth(),   startDate.getDay() );  
				     // Add the number of months ­1 (months start counting from 0).
				     cal.add(GregorianCalendar.MONTH, (12 * li_PreservYear) -1) ;
				     IDfTime expireDate = new DfTime (cal.getTime()) ;
				     if(li_PreservYear==0)
				    	 idfNewDoc.setString("u_expired_date",  "9999-12-31 00:00:00");
				     else
				         idfNewDoc.setString("u_expired_date",  expireDate.toString());
		     }
/*		     
	         idfNewDoc.setInt("u_preserve_flag",  dto.getUserPreset().getUPreserveFlag()); // 보존년한
	         if (dto.getUserPreset().getUOpenFlag() != null) {
	           idfNewDoc.setString("u_open_flag", dto.getUserPreset().getUOpenFlag());
	         }
	         if (dto.getUserPreset().getUMailPermitFlag() != null) {
	           idfNewDoc.setBoolean("u_auto_auth_mail_flag", "Y".equals(dto.getUserPreset().getUMailPermitFlag())?true:false);
	         }
*/		     
		     idfNewDoc.setString("u_pr_code", dto.getPrCode());
		     idfNewDoc.setString("u_pr_type", dto.getPrType());
		     
		     idfNewDoc.setString("u_cabinet_code", s_CabinetCode);
		     
		     idfNewDoc.save();
		     dto.setPreFolderId(  idfNewDoc.getString("u_copy_org_id"));
		     addMoveAuthBase( userSession,idfSession, idfNewDoc.getChronicleId().getId(), s_CabinetCode, "D", dto, idfNewDoc);

    	} catch (Exception e) {
			e.printStackTrace();
	    }    	
    }
    
    
    private void setDocAuthBase(IDfSession idfSession, String lsDocKey, IDfDocument oNewSO, String ps_RcevCab, String crUser) throws Exception{
    	IDfCollection idf_Col=null;
    	try {
			String s_Dql = "select * from edms_auth_base " +
						" where u_obj_id = '" + lsDocKey + "' " +
						"   and u_obj_type = 'D' " ;
				
			idf_Col = DCTMUtils.getCollectionByDQL(idfSession, s_Dql, DfQuery.DF_QUERY);
			
			IDfPersistentObject	idf_PObj	= null;
			while(idf_Col != null && idf_Col.next()) {	
				idf_PObj = idfSession.newObject("edms_auth_base");
				idf_PObj.setString("u_obj_id"		, oNewSO.getString("u_doc_key"));
				idf_PObj.setString("u_obj_type"		, idf_Col.getString("u_obj_type"));
				idf_PObj.setString("u_doc_status"	, idf_Col.getString("u_doc_status"));
				idf_PObj.setString("u_permit_type"	, idf_Col.getString("u_permit_type"));
				idf_PObj.setString("u_own_dept_yn"	, idf_Col.getString("u_own_dept_yn"));
				idf_PObj.setString("u_author_id"	, "g_" + ps_RcevCab);
				idf_PObj.setString("u_author_type"	, idf_Col.getString("u_author_type"));
				idf_PObj.setString("u_create_user"	, crUser);
				idf_PObj.setTime  ("u_create_date"	, new DfTime());
				idf_PObj.save();
	
    	    }
			if(idf_Col != null) idf_Col.close();
    	} catch (Exception e) {
			e.printStackTrace();
	    } finally {
	    	if(idf_Col != null) idf_Col.close();
	    }
    }
    
    
    //이동시 대상 폴더의 상위폴더와 cabinet 변경  (edms_folder)
    private void uptFolderPath(IDfSession idfSession, String uUpFolId, String rObjectId, String ps_RcevCab, String crUser, DPath dto) throws Exception{

    	try {
    		
    		IDfPersistentObject idf_PObj = idfSession.getObject(new DfId(rObjectId));
    		
    		//---------------------------------------------------------------
    		// 속성 값 수정
    		//---------------------------------------------------------------
    		if(uUpFolId !=null && null ==dto.getPrCode())
    			 idf_PObj.setString("u_up_fol_id"    ,  uUpFolId);
    		else
    			idf_PObj.setString("u_up_fol_id"    ,  uUpFolId);

    		idf_PObj.setString("u_cabinet_code"	,  ps_RcevCab);
    		idf_PObj.setString("u_fol_status", "O");

    		
    		
    		idf_PObj.setString("u_pr_code", dto.getPrCode());
    		idf_PObj.setString("u_pr_type", dto.getPrType());

    		idf_PObj.setString("u_fol_type", dto.getTargetFolType())  ;
    		
    		//if (secLevelMap.get(idf_PObj.getString("u_sec_level")) < secLevelMap.get(dto.getTargetSecLevel())) {
    		//	idf_PObj.setString("u_sec_level", dto.getTargetSecLevel() );
    		//}

			idf_PObj.save();
    		//---------------------------------------------------------------
    	} catch (Exception e) {
			e.printStackTrace();
	    }
    }    
    
    
    //이동시 링크파일의 상퓌 폴더 변경  (edms_doc_link)
    private void uptDocLink(IDfSession idfSession, String uUpFolId, String rObjectId, String ps_RcevCab, String crUser, DPath dto) throws Exception{
    	try {
    		
    		IDfPersistentObject idf_PObj = idfSession.getObject(new DfId(rObjectId));
    		
    		//---------------------------------------------------------------
    		// 속성 값 수정
    		//---------------------------------------------------------------
    		idf_PObj.setString("u_fol_id"	    ,  uUpFolId);
    		idf_PObj.setString("u_cabinet_code"	,  ps_RcevCab);
    		//idf_PObj.setString("u_author_id"	, "g_" + ps_RcevCab);
			idf_PObj.setString("u_create_user"	, crUser);
			idf_PObj.setTime  ("u_create_date"	, new DfTime());

    		//if (secLevelMap.get(idf_PObj.getString("u_sec_level")) < secLevelMap.get(dto.getTargetSecLevel())) {
    		//	idf_PObj.setString("u_sec_level", dto.getTargetSecLevel() );
    		//}
			
			idf_PObj.save();
    		//---------------------------------------------------------------
    	} catch (Exception e) {
			e.printStackTrace();
	    }    	
    }    
    
    //이동처리시에 동일한 이름의 폴더가 있어서 폴더함침시 소스쪽 폴더정보를 삭제처리
    private void delHapFolderAuthBase(UserSession userSession, IDfSession idfSession, String rObjectId) throws Exception{
    	IDfCollection idf_Col=null;
    	try {  
    		
				//edms_auth_base 관련 정보 지움
				String s_Dql = "delete edms_auth_base object " +
						" where u_obj_id = '" + rObjectId + "' and u_add_gubun='G' ";
				
				idf_Col = DCTMUtils.getCollectionByDQL(idfSession, s_Dql, DfQuery.DF_QUERY);
				if(idf_Col != null) idf_Col.close();

				s_Dql = "delete edms_auth_share object " +
						" where u_obj_id = '" + rObjectId + "'";
				
				idf_Col = DCTMUtils.getCollectionByDQL(idfSession, s_Dql, DfQuery.DF_QUERY);
				if(idf_Col != null) idf_Col.close();
				
    	}catch (Exception e) {
			e.printStackTrace();
	    }finally {
	    	if(idf_Col != null) idf_Col.close();
	    }
    }    
    
    //폴더의 기본 auth 리스트 반환
    private List<String> getFolderAuthorList(UserSession userSession, IDfSession idfSession, String rObjectId, DPath dto ) throws Exception{
    	
		    if( (!dto.getSourceGubun().equals(dto.getTargetFolType()) ) ){  //&& dto.getUptPthGbn().equals("C")) || dto.getUptPthGbn().equals("M")) {
			    String s_Dql_d = "delete edms_auth_base object " +
				         " where u_obj_id = '" + rObjectId + "' and ( u_add_gubun='S' )";  //속성에서 추가되거나 다른 경로로 추가된 것들은 그대로 유지하기 우해서 u_add_gubun 조건 사용
		        IDfCollection idf_Col_D = DCTMUtils.getCollectionByDQL(idfSession, s_Dql_d, DfQuery.DF_QUERY);
		        if(idf_Col_D != null) idf_Col_D.close();
			}
		
			
			String s_ComId    = dto.getTgComId();
			String ps_RcevCab = dto.getTgCabinetcode();

			Map<String, Integer> map_Author =new HashMap<String,Integer>();	        	
		
			if(dto.getPrType().equals("") || dto.getPrType().equals(" ")) { //부서함이거나 사별함
				if(dto.getTargetFolType().substring(0,1).equals("C")) {  //사별함
			    	  map_Author = DCTMConstants.COM_FOLDER_HAM_AUTH_MAP;
		
				}else if(dto.getTargetFolType().substring(0,1).equals("M")) { //공용문서함
		    	      switch(ComCodeType.findByValue(s_ComId)) {      
		    	      case DKS:
		    	    	map_Author = DCTMConstants.DEFAULT_FOLDER_COM_DKS_HAM_MAP;
		    	        break;
		    	      case ITG:
		    	    	map_Author = DCTMConstants.DEFAULT_FOLDER_COM_ITG_HAM_MAP;
		    	        break;
		    	      case FEI:
		    	    	map_Author = DCTMConstants.COM_FOLDER_HAM_AUTH_MAP;
		    	        break;
		    	      default:
		    	    	break;
		    	      }
				}else if(dto.getTargetFolType().substring(0,1).equals("D")) {  //부서
					
					if(!dto.getTargetGubun().substring(0,2).equals("DI")) {
					    map_Author = DCTMConstants.TEAM_FOLDER_AUTH_MAP;     //부서일반
				    }else {
					    map_Author = DCTMConstants.TEAM_FOLDER_IMP_AUTH_MAP; //중요문서
				    }
				}
			}else { //프로젝트,연구과제
			    if(dto.getPrType().equals("P")) {
			    	map_Author = DCTMConstants.PJT_FOLDER_AUTH_MAP;
		
			    }else if(dto.getPrType().equals("R")) {
			    	map_Author = DCTMConstants.RSCH_FOLDER_AUTH_MAP;
			    }
			}
			List<String> lst_Author = new ArrayList<String>();
			for (Entry<String, Integer> entry : map_Author.entrySet()) {
				String s_AuthorId= entry.getKey();
				s_AuthorId =s_AuthorId.replace( DCTMConstants.COM_CODE_STR, s_ComId).replace( DCTMConstants.CABINET_CODE_STR, ps_RcevCab).replace( DCTMConstants.DEFAULT_CABINET_STR, ps_RcevCab);
				s_AuthorId =s_AuthorId.replace( DCTMConstants.PJT_CODE_STR, dto.getPrCode()).replace( DCTMConstants.RSCH_CODE_STR, dto.getPrCode());
				s_AuthorId =s_AuthorId.replace( DCTMConstants.PJT_CODE_STR, dto.getPrCode()).replace( DCTMConstants.RSCH_CODE_STR, dto.getPrCode());
				
				lst_Author.add(s_AuthorId);
			}
			return lst_Author;
    }
    
    //폴더의 기본 auth Share 리스트 반환
    private boolean setFolderAuthShareList(UserSession userSession, IDfSession idfSession, String uFolId, String rObjectId , DPath dto, String p_ObjType ) throws Exception{
    	

	    String s_Dql = "select * from edms_auth_share where u_obj_id ='"+ uFolId +"'" ;
		
  		IDfQuery 		idf_Qry 	= null;
  		idf_Qry = new DfQuery();
  		idf_Qry.setDQL(s_Dql);
  			
  		IDfCollection idf_Col=null;
  		try {
  			    idf_Col = idf_Qry.execute(idfSession,DfQuery.QUERY);
  		} catch (Exception e) {
  			e.printStackTrace();
  			return false;
  		}
  		try {
		    
	  		String lDocStatus[] = {"L","C"};
	  		IDfDocument idfDoc_Obj =null;
  		    if(p_ObjType.equals("D")){//문서
  		    	idfDoc_Obj =(IDfDocument)idfSession.getObject(new DfId(rObjectId));
  		    }

  		    //권한자를 READ, DELETE 두 군데 모두 넣은 경우, Closed권한에 넣을때는 한 번만 넣도록 한다
  		    Map<String,String> imiMap= new HashMap<String,String>();

	  		while(idf_Col != null && idf_Col.next())
	  		{
	  		    String s_Author=idf_Col.getString("u_author_id");
	  		    if(null== s_Author || s_Author.equals("g_null")|| s_Author.equals("") || s_Author.equals(" ")) continue;

	  		    String s_AuthorType=idf_Col.getString("u_author_type");
	  		    String s_PermitType=idf_Col.getString("u_permit_type");
	  		    
	  		    //IDfPersistentObject idf_PObj=null;

	  		    if(p_ObjType.equals("F")){  //폴더인 경우
	  		    	IDfPersistentObject idf_PObj = idfSession.newObject("edms_auth_share");
		  			idf_PObj.setString("u_obj_id"		, rObjectId);
		  			idf_PObj.setString("u_author_id"		, s_Author);
		  			idf_PObj.setString("u_author_type"		, s_AuthorType);
		  			idf_PObj.setString("u_permit_type"		, s_PermitType);
		  			idf_PObj.setString("u_create_user"		, dto.getReqUser());
		  			idf_PObj.setTime  ("u_create_date"	, new DfTime());
		  			idf_PObj.save();
	  		    }else if(p_ObjType.equals("D")){
	  			
	  		    	//rObjectId 	= DCTMUtils.getCurrentObjectID(idfSession, rObjectId); //문서의 최신버전 r_object_id를 가져온다.
	  		    	String ls_UDocKey = idfDoc_Obj.getString("u_doc_key");
	  		    	
		  		    if(s_AuthorType.equals("D") ) s_Author="g_"+ s_Author;
		  		  
		  		    for(int i=0; i< lDocStatus.length; i++) { 
		  		    	
		  		    	//System.out.println(lDocStatus[i]+ " : "+ s_Author+" : "+ s_AuthorType +" : "+ ls_UDocKey);
						//if(s_Author.equals("g_" + dto.getTgCabinetcode())) continue; //복사자부서, 
						//if(s_Author.equals(dto.getReqUser())) continue;              //복사자는 ... auth_base 추가시에 'D'삭제권한으로 추가해주므로 중복되지 않게 제거 
						
						if(lDocStatus[i].equals("C")) {
							if(null !=imiMap.get(s_Author))
							    imiMap.put(s_Author,"1");
							else
								continue;
							s_PermitType="R";
							//if(idfDoc_Obj.getString("u_sec_level").equals("S") && idfDoc_Obj.getString("u_doc_status").equals("C")) continue;  //closed의 제한등급이면 pass
						}
						//System.out.println("========"+lDocStatus[i]+ " : "+ s_Author+" : "+ s_AuthorType +" : "+ ls_UDocKey+" : "+ s_PermitType);
//if(s_Author.equals("jungkyo.jung")) s_Author="changmyoun.ji";
				  	    int i_AuthorCnt = DCTMUtils.getCountByDQL( idfSession, " edms_auth_base where u_obj_id='"+rObjectId +"' and u_author_id='"+s_Author +"' and u_doc_status='"+lDocStatus[i]+"'  and u_author_type ='S' ");
				  	    if(i_AuthorCnt < 1) {

				  	    	IDfPersistentObject idf_PObj = idfSession.newObject("edms_auth_base");

				  			idf_PObj.setString("u_obj_id"		, ls_UDocKey);  //문서는 문서Key를 objId에 넣도록 한다(2022.01.06 확인)
				  			idf_PObj.setString("u_obj_type"		, p_ObjType);    //폴더, 문서 
				  			idf_PObj.setString("u_doc_status"	, lDocStatus[i]);
				  			idf_PObj.setString("u_permit_type"	, s_PermitType);
							idf_PObj.setString("u_own_dept_yn"	, s_Author.equals("g_" + dto.getTgCabinetcode()) ? "Y" : "");
							idf_PObj.setString("u_author_id"	, s_Author);
							idf_PObj.setString("u_author_type"	, s_AuthorType.equals("S")?"U":s_AuthorType);
				  			
				  			idf_PObj.setString("u_create_user"	, dto.getReqUser());
				  			idf_PObj.setTime  ("u_create_date"	, new DfTime());
				  			idf_PObj.setString("u_add_gubun"	, "S");  //공유-협업그룹
				  			
				  			idf_PObj.save();
				  	    }
		  		    }
		  		    
		  		    if(p_ObjType.equals("D") && null !=idfDoc_Obj){//문서
					//if(s_Author.equals("g_" + dto.getTgCabinetcode())) continue; //복사자부서, 
					//if(s_Author.equals(dto.getReqUser())) continue;              //복사자는 ... auth_base 추가시에 'D'삭제권한으로 추가해주므로 중복되지 않게 제거 
	  		    	
	  		    	  if( idfDoc_Obj.getPermitEx( s_Author) != GrantedLevels.findByLabel(idf_Col.getString("u_permit_type"))) {
  		    		    
	  		    		idfDoc_Obj.grant(s_Author, GrantedLevels.findByLabel(idf_Col.getString("u_permit_type")), ""); //삭제가능 grant 권한 부여
 		    		    
	  		    		//if(s_AuthorType.equals("D")) {
	  		    		    //if(!idfDoc_Obj.getString("u_sec_level").equals("S") && !idfDoc_Obj.getString("u_sec_level").equals("T")) {
	  		    		    //idfDoc_Obj.grant(s_Author+"_sub", GrantedLevels.findByLabel(idf_Col.getString("u_permit_type")), ""); //삭제가능 grant 권한 부여
	  		    		    //}
	  		    		//    idfDoc_Obj.save();
	  		    	    //}
	  		    	  }
				    }
	  		    }
	  	    }
	  		if(idf_Col != null) idf_Col.close();
  		}catch (Exception e) {
   			e.printStackTrace();
   			return false;
	    }finally {
	    	if(idf_Col != null) idf_Col.close();
	    }
  		return true ;
    }
	
    
    
    //프로젝트 하위로 들어가게 되면 , 프로젝트의 참여부서(읽기, 쓰기)들을 auth_share 사용자로 등록한다.
    private boolean setProjectJoinDeptToAuthbase(UserSession userSession, IDfSession idfSession, String rObjectId , DPath dto, String p_ObjType ) throws Exception{

	    String s_Dql = "select * from edms_project where u_pjt_code ='"+ dto.getPrCode()+"'" ;
	    if(dto.getPrType().equals("R"))
	    	s_Dql = "select * from edms_research where u_rsch_code ='"+ dto.getPrCode()+"'" ;
		
  		IDfQuery 		idf_Qry 	= null;
  		idf_Qry = new DfQuery();
  		idf_Qry.setDQL(s_Dql);
  			
  		IDfCollection idf_Col=null;
  		try {
  			    idf_Col = idf_Qry.execute(idfSession,DfQuery.QUERY);
  		} catch (Exception e) {
  			e.printStackTrace();
  			return false;
  		}
  		try {
		    
	  		IDfDocument         idf_DocObj =null;
	  		IDfPersistentObject idfFol_Obj =null;
	  		
	  		String s_USecLevel="";
	  		if(p_ObjType.equals("D")) {
		        idf_DocObj =(IDfDocument)idfSession.getObject(new DfId(rObjectId));
		        rObjectId =idf_DocObj.getString("u_doc_key"); //u_doc_key로 변경
		        s_USecLevel=idf_DocObj.getString("u_sec_level");
	  		}else if(p_ObjType.equals("F")) {
  		    	idfFol_Obj =idfSession.getObject(new DfId(rObjectId));
  		    	s_USecLevel=idfFol_Obj.getString("u_sec_level");
  		    }


	  		String s_AddGubun="J";//프로젝트 참여부서들은 u_add_gubun값  J 로 등록한다(*2021.12.16 soik.cha 공지.메신져)
	  		List<String> lDocStatus = new ArrayList<String>();
	  		if(!s_USecLevel.equals("S")) lDocStatus.add("C");
	  		lDocStatus.add("L");
	  		
	  		while(idf_Col != null && idf_Col.next())
	  		{
	  			IDfPersistentObject idf_PjtObj = idfSession.getObject(new DfId(idf_Col.getString("r_object_id")));
	  			
	  			String s_MDepts = idf_PjtObj.getAllRepeatingStrings("u_own_dept", ",");
	  			
	  			String s_RDepts = idf_PjtObj.getAllRepeatingStrings("u_join_dept_read", ",");
	  			String s_DDepts = idf_PjtObj.getAllRepeatingStrings("u_join_dept_del", ",");
	  			
	  			String s_ChiefId = idf_PjtObj.getString("u_chief_id"); //책임자ID
	  			
	  			if(p_ObjType.equals("D") ) {//문서
	  				String ls_MaxPermit="D";
	  				if(idf_DocObj.getString("u_doc_status").equals("C")) ls_MaxPermit="R";
	  				
	  			    if( idf_DocObj.getPermitEx( s_ChiefId) != GrantedLevels.findByLabel(ls_MaxPermit)) {
		    	        idf_DocObj.grant(s_ChiefId, GrantedLevels.findByLabel(ls_MaxPermit), "");
	  		            idf_DocObj.save();
	  			    }
	  			}
	  			
	  			String[] s_MDeptsArr = s_MDepts.split(",");
	  			String[] s_RDeptsArr = s_RDepts.split(",");
	  			String[] s_DDeptsArr = s_DDepts.split(",");
	  			
	  		    for(int ai=0; ai < s_MDeptsArr.length; ai++) {  //주관부서 
  		    		String s_Author=s_MDeptsArr[ai];
  		    		String s_AuthorType="D";
  		  		    VDept myDept = gwDeptService.selectDeptByOrgId(s_Author);
  		  		    if(null != myDept) {
  		  		    	s_Author = myDept.getUCabinetCode();
  		  		        s_Author = "g_"+s_Author;
  		  		    }else {
  		    			System.out.println("#MOLA#유효하지 않은 부서코드" + s_MDeptsArr[ai]);
  		    			continue;
  		    		}
  		  		    
                    String s_PermitType="D";
  		  		    if(p_ObjType.equals("D")) {//문서
  		  		        if(idf_DocObj.getString("u_doc_status").equals("C")) s_PermitType="R"; //closed문서는 읽기권한
	  		  		    if( idf_DocObj.getPermitEx( s_Author) != GrantedLevels.findByLabel(s_PermitType)) {
		  		    	    idf_DocObj.grant(s_Author, GrantedLevels.findByLabel(s_PermitType), "");
		  		    	    if(s_AuthorType.equals("D")) {
		  		    	        idf_DocObj.grant(s_Author+"_sub", GrantedLevels.findByLabel(s_PermitType), "");
		  		    	    }    
			  		        idf_DocObj.save();
		  		    	}
  		  		    }
  		  		    for(int i=0; i< lDocStatus.size(); i++) {
		  		    	
				  	    int i_AuthorCnt = DCTMUtils.getCountByDQL( idfSession, " edms_auth_base where u_obj_id='"+rObjectId +"' and u_author_id='"+s_Author +"' and u_doc_status='"+lDocStatus.get(i)+"'  and u_author_type !='S' ");
				  	    if(i_AuthorCnt < 1) {
		  		    	    s_PermitType="D";
							IDfPersistentObject idf_PObj = idfSession.newObject("edms_auth_base");
							if(lDocStatus.get(i).equals("C")) s_PermitType="R"; //Closed권한은 조회/다운로드권한
							
							//if(s_Author.equals("g_" + dto.getTgCabinetcode())) continue; //복사자부서, 
							
				  			idf_PObj.setString("u_obj_id"		, rObjectId);
				  			idf_PObj.setString("u_obj_type"		, p_ObjType);    //폴더, 문서 
				  			idf_PObj.setString("u_doc_status"	, lDocStatus.get(i));
				  			idf_PObj.setString("u_permit_type"	, s_PermitType);
							idf_PObj.setString("u_own_dept_yn"	, s_Author.equals("g_" + dto.getTgCabinetcode()) ? "Y" : "");
							idf_PObj.setString("u_author_id"	, s_Author);
						    idf_PObj.setString("u_author_type"	, "D"); //부서  
				  			idf_PObj.setString("u_create_user"	, dto.getReqUser());
				  			idf_PObj.setTime  ("u_create_date"	, new DfTime());
				  			idf_PObj.setString("u_add_gubun"	, s_AddGubun);  //속성추가
				  			idf_PObj.save();
				  	    }
		  		    }
	  		    }	  			
	  		    for(int ai=0; ai < s_RDeptsArr.length; ai++) {
  		    		String s_Author=s_RDeptsArr[ai];
  		    		if(null==s_Author ||s_Author.equals("")) break;
  		  		    VDept myDept = gwDeptService.selectDeptByOrgId(s_Author);
  		  		    if(null != myDept) {
  		  		    	s_Author = myDept.getUCabinetCode();
  		  		        s_Author = "g_"+s_Author;
  		  		    }else {
  		    			System.out.println("#MOLA#유효하지 않은 부서코드" + s_MDeptsArr[ai]);
  		    			continue;
  		    		}
                    String s_PermitType="R";
  		  		    if(p_ObjType.equals("D")) {//문서
	  		  		    if( idf_DocObj.getPermitEx( s_Author) != GrantedLevels.findByLabel(s_PermitType)) {
		  		    	    idf_DocObj.grant(s_Author, GrantedLevels.findByLabel(s_PermitType), "");
		  		    	    idf_DocObj.grant(s_Author+"_sub", GrantedLevels.findByLabel(s_PermitType), "");
			  		        idf_DocObj.save();
		  		    	}
  		  		    }
  		  		    for(int i=0; i< lDocStatus.size(); i++) {
				  	    int i_AuthorCnt = DCTMUtils.getCountByDQL( idfSession, " edms_auth_base where u_obj_id='"+rObjectId +"' and u_author_id='"+s_Author +"' and u_doc_status='"+lDocStatus.get(i)+"' and u_author_type !='S'");
				  	    if(i_AuthorCnt < 1) {
							IDfPersistentObject idf_PObj = idfSession.newObject("edms_auth_base");
							
							//if(s_Author.equals("g_" + dto.getTgCabinetcode())) continue; //복사자부서, 
							
				  			idf_PObj.setString("u_obj_id"		, rObjectId);
				  			idf_PObj.setString("u_obj_type"		, p_ObjType);    //폴더, 문서 
				  			idf_PObj.setString("u_doc_status"	, lDocStatus.get(i));
				  			idf_PObj.setString("u_permit_type"	, s_PermitType);
							idf_PObj.setString("u_own_dept_yn"	, s_Author.equals("g_" + dto.getTgCabinetcode()) ? "Y" : "");
							idf_PObj.setString("u_author_id"	, s_Author);
						    idf_PObj.setString("u_author_type"	, "D");  //참여부서들이라 부서로 등록
				  			idf_PObj.setString("u_create_user"	, dto.getReqUser());
				  			idf_PObj.setTime  ("u_create_date"	, new DfTime());
				  			idf_PObj.setString("u_add_gubun"	, s_AddGubun);  //속성추가
				  			idf_PObj.save();
				  	    }
		  		    }
	  		    }
	  		    for(int ai=0; ai < s_DDeptsArr.length; ai++) {
  		    		String s_Author=s_DDeptsArr[ai];
  		    		if(null==s_Author ||s_Author.equals("")) break;
  		  		    VDept myDept = gwDeptService.selectDeptByOrgId(s_Author);
  		  		    if(null != myDept) {
  		  		    	s_Author = myDept.getUCabinetCode();
  		  		        s_Author = "g_"+s_Author;
  		  		    }else {
  		    			System.out.println("#MOLA#유효하지 않은 부서코드" + s_MDeptsArr[ai]);
  		    			continue;
  		    		}
                    String s_PermitType="D";
  		  		    if(p_ObjType.equals("D")) {//문서
  		  		        if(idf_DocObj.getString("u_doc_status").equals("C")) s_PermitType="R";
	  		  		    if( idf_DocObj.getPermitEx( s_Author) != GrantedLevels.findByLabel(s_PermitType)) {
		  		    	    idf_DocObj.grant(s_Author, GrantedLevels.findByLabel(s_PermitType), "");
		  		    	    idf_DocObj.grant(s_Author+"_sub", GrantedLevels.findByLabel(s_PermitType), "");
			  		        idf_DocObj.save();
		  		    	}
  		  		    }
		  		    for(int i=0; i< lDocStatus.size(); i++) {

				  	    int i_AuthorCnt = DCTMUtils.getCountByDQL( idfSession, " edms_auth_base where u_obj_id='"+rObjectId +"' and u_author_id='"+s_Author +"' and u_doc_status='"+lDocStatus.get(i)+"'  and u_author_type !='S'");
				  	    if(i_AuthorCnt < 1) {
		  		    	    s_PermitType="D";  //삭제권한
							IDfPersistentObject idf_PObj = idfSession.newObject("edms_auth_base");
							if(lDocStatus.get(i).equals("C")) s_PermitType="R"; //Closed권한은 조회/다운로드권한
							
							//if(s_Author.equals("g_" + dto.getTgCabinetcode())) continue; //복사자부서,
				  			idf_PObj.setString("u_obj_id"		, rObjectId);
				  			idf_PObj.setString("u_obj_type"		, p_ObjType);    //폴더, 문서 
				  			idf_PObj.setString("u_doc_status"	, lDocStatus.get(i));
				  			idf_PObj.setString("u_permit_type"	, s_PermitType);
							idf_PObj.setString("u_own_dept_yn"	, s_Author.equals("g_" + dto.getTgCabinetcode()) ? "Y" : "");
							idf_PObj.setString("u_author_id"	, s_Author);
							idf_PObj.setString("u_author_type"	, "D");  //참여부서들이라 부서로 등록
				  			idf_PObj.setString("u_create_user"	, dto.getReqUser());
				  			idf_PObj.setTime  ("u_create_date"	, new DfTime());
				  			idf_PObj.setString("u_add_gubun"	, s_AddGubun);  //속성추가
				  			idf_PObj.save();
				  	    }
		  		    }
	  		    }
	  	    }
	  		if(idf_Col != null) idf_Col.close();
  		}catch (Exception e) {
   			e.printStackTrace();
   			return false;
	    }finally {
	    	if(idf_Col != null) idf_Col.close();
	    }
  		return true ;
    }    
    
    //상위 폴더의 기본 auth base 리스트 반환
    private boolean setFolderAuthBaseList(UserSession userSession, IDfSession idfSession, String ps_UObjId, String rObjectId , DPath dto, String p_ObjType ) throws Exception{
    	
    	
	    String s_Dql = "select distinct u_author_id, u_author_type, u_permit_type, u_add_gubun, u_doc_status from edms_auth_base where u_obj_id ='"+ ps_UObjId +"' and u_author_type in('D','U','C','G')  and u_add_gubun in('P','W', 'G',' ', 'J')  and u_author_id !='g_null'" ;
	    if(p_ObjType.equals("D")){//문서
    	    //s_Dql = "select distinct u_author_id, u_author_type, u_permit_type, u_add_gubun, u_doc_status from edms_auth_base where u_obj_id ='"+ ps_UObjId +"' and u_author_type in('D','U','C','G')  and u_add_gubun in('P','W', 'G',' ','S', 'J')  and u_author_id !='g_null'" ;
	    	s_Dql = "select distinct u_author_id, u_author_type, u_permit_type, u_add_gubun, u_doc_status from edms_auth_base where u_obj_id ='"+ ps_UObjId +"' and u_author_type in('D','U','C','G')  and u_add_gubun in('P','W', 'G',' ', 'J')  and u_author_id !='g_null'" ;
	    }
	    
	    //if(!dto.getPreFolderId().equals("") && !dto.getPreFolderId().equals(ps_UObjId)) {
	    //	s_Dql = "select distinct u_author_id, u_author_type, u_permit_type, u_add_gubun, u_doc_status from edms_auth_base where u_obj_id in('"+ ps_UObjId +"', '"+ dto.getPreFolderId()+"') and u_author_type in('D','U','C','G')  and u_add_gubun in('P','W','S',' ')  " ;
	    //}
	    
  		IDfQuery 		idf_Qry 	= null;
  		idf_Qry = new DfQuery();
  		idf_Qry.setDQL(s_Dql);
  			
  		IDfCollection idf_Col=null;
  		IDfCollection idf_Colb=null;
  		try {
  			    idf_Col = idf_Qry.execute(idfSession,DfQuery.QUERY);
  		} catch (Exception e) {
  			e.printStackTrace();
  			return false;
  		}
  		try {
		    
	  		String lDocStatus[] = {"C", "L"};
	  		IDfDocument         idfDoc_Obj =null;
	  		IDfPersistentObject idfFol_Obj =null;
	  		int liDocMaxPermit =3;
  		    if(p_ObjType.equals("D")){//문서
  		    	idfDoc_Obj =(IDfDocument)idfSession.getObject(new DfId(rObjectId));
  		    	if(null !=idfDoc_Obj && idfDoc_Obj.getString("u_doc_status").equals("L")) liDocMaxPermit=7;
  		    	rObjectId = idfDoc_Obj.getString("u_doc_key");
  		    	
  		    }else if(p_ObjType.equals("F")) {//폴더
  		    	idfFol_Obj =idfSession.getObject(new DfId(rObjectId));
  		    }
  		    
  		    Map<String,String> delMap= new HashMap<String,String>();

	  		while(idf_Col != null && idf_Col.next())
	  		{
	  		    String s_Author=idf_Col.getString("u_author_id");
	  		    
	  		    if(null== s_Author || s_Author.equals("g_null")|| s_Author.equals("") || s_Author.equals(" ")) continue;
	  		    
	  		    String s_AuthorType=idf_Col.getString("u_author_type");
	  		    String s_PermitType=idf_Col.getString("u_permit_type");
	  		    String s_AddGubun  =idf_Col.getString("u_add_gubun");
	  		    String s_DocStatus =idf_Col.getString("u_doc_status");
	  		    
	  		    if(p_ObjType.equals("D")){//문서
	  		    //    if(null !=idfDoc_Obj && idfDoc_Obj.getString("u_sec_level").equals("S") && !s_AddGubun.equals("P")) liDocMaxPermit=3;
		  		    if(null !=idfDoc_Obj && idfDoc_Obj.getString("u_sec_level").equals("S") ) {
		  		        if(s_AuthorType.equals("U") && s_Author.equals(dto.getReqUser()))  s_AddGubun="G";//제한등급일 때, 복사,이동하는 사용자와 같은 ID에 대해서는 G로 추가
		  		        if(s_AuthorType.equals("U") && !s_Author.equals(dto.getReqUser())) s_AddGubun="P";//제한등급일 때, 복사,이동하는 사용자와 다른 사용자ID는 P 로 추가하거나 변경
		  		    }	  		    	
	  		    }
	  		    	  		    
	  		    if(p_ObjType.equals("D") && s_Author.contains("_sub") && (s_AddGubun.equals("P") || s_AddGubun.equals("S"))) continue;
	  		    
	  		    if( p_ObjType.equals("D") && idfDoc_Obj.getString("u_doc_status").equals("L") && s_PermitType.equals("D") ) delMap.put(s_Author, s_AuthorType); 
	  		     
	  		    for(int i=0; i< lDocStatus.length; i++) {
		  		    if(!s_DocStatus.equals(lDocStatus[i])) continue;

			  	    int i_AuthorCnt = DCTMUtils.getCountByDQL( idfSession, " edms_auth_base where u_obj_id='"+rObjectId +"' and u_author_id='"+s_Author +"' and u_doc_status='"+lDocStatus[i]+"' and u_author_type !='S' ");
			  	    if(i_AuthorCnt < 1) {
	  		    	
						IDfPersistentObject idf_PObj = idfSession.newObject("edms_auth_base");
						if(lDocStatus[i].equals("C")) {
							s_PermitType="R"; //Closed권한은 조회/다운로드권한
							
							if(p_ObjType.equals("D")) {
								if(liDocMaxPermit > 3 
								  && s_Author.equals("g_" + dto.getTgCabinetcode().toLowerCase()) 
								    && dto.getSrcCabinetcode().equals(dto.getTgCabinetcode()) )  s_PermitType="D";
								//if(idfDoc_Obj.getString("u_sec_level").equals("S")) continue;
								//if(idfDoc_Obj.getString("u_sec_level").equals("G") &&  (!s_Author.equals( dto.getUGroupOrgCabinetCd()) )) continue;
								//else if(idfDoc_Obj.getString("u_sec_level").equals("C") &&  (!s_Author.equals( dto.getUComOrgCabinetCd())   )) continue;
								//else if(idfDoc_Obj.getString("u_sec_level").equals("T") &&  !s_Author.equals( "g_"+dto.getTgCabinetcode()   )) continue;
							}
							else if(p_ObjType.equals("F") && idfFol_Obj.getString("u_sec_level").equals("S") && !s_AddGubun.equals("G")) continue;
						}else {
							s_PermitType=idf_Col.getString("u_permit_type");
						}
						if(lDocStatus[i].equals(s_DocStatus)) { 
						
							 if(p_ObjType.equals("D")) {
									if(liDocMaxPermit > 3 
									  && s_Author.equals("g_" + dto.getTgCabinetcode().toLowerCase()) 
									    && dto.getSrcCabinetcode().equals(dto.getTgCabinetcode()) 
									      && s_DocStatus.equals("L"))  s_PermitType="D";
							 }
							 if(lDocStatus[i].equals("C")) s_PermitType="R";

							//if(s_Author.equals("g_" + dto.getTgCabinetcode())) continue; //복사자부서, 
							//if(s_Author.equals(dto.getReqUser()) && !s_AddGubun.equals("G")) continue;              //복사자는 ... auth_base 추가시에 'D'삭제권한으로 추가해주므로 중복되지 않게 제거 
				  			idf_PObj.setString("u_obj_id"		, rObjectId);
				  			idf_PObj.setString("u_obj_type"		, p_ObjType);    //폴더, 문서 
				  			idf_PObj.setString("u_doc_status"	, lDocStatus[i]);
				  			idf_PObj.setString("u_permit_type"	, s_PermitType);
							idf_PObj.setString("u_own_dept_yn"	, s_Author.equals("g_" + dto.getTgCabinetcode()) ? "Y" : "");
							idf_PObj.setString("u_author_id"	, s_Author);
							//idf_PObj.setString("u_author_type"	, s_AuthorType.equals("S")?"U":s_AuthorType);
							idf_PObj.setString("u_author_type"	, s_AuthorType);
				  			
				  			idf_PObj.setString("u_create_user"	, dto.getReqUser());
				  			idf_PObj.setTime  ("u_create_date"	, new DfTime());
				  			idf_PObj.setString("u_add_gubun"	, s_AddGubun);  //속성추가
				  			idf_PObj.save();
				  			if(p_ObjType.equals("D")) {
				  				if(idfDoc_Obj.getString("u_doc_status").equals(s_DocStatus)) { //문서가 Live이면 Live권한대상자권한부여, 아니면 Closed권한 부여
				  		    		idfDoc_Obj.grant(s_Author, GrantedLevels.findByLabel(s_PermitType), ""); //삭제가능 grant 권한 부여
				  		    		if(s_AuthorType.equals("D")) {//
				  		    			if(s_AddGubun.equals("P") ||s_AddGubun.equals("W")) {// && (!idfDoc_Obj.getString("u_sec_level").equals("S") && !idfDoc_Obj.getString("u_sec_level").equals("T"))) {//
				  		            	    idfDoc_Obj.grant(s_Author+"_sub", GrantedLevels.findByLabel(s_PermitType), ""); //삭제가능 grant 권한 부여
				  		    			}
					  		        }
				  		    		idfDoc_Obj.save();
				  				}
				  			}				  			
						}
			  	    }else {
			  	    	  if(p_ObjType.equals("D") && null !=idfDoc_Obj){//문서
			  	    		  
					  		  if( idfDoc_Obj.getPermitEx( s_Author) != GrantedLevels.findByLabel(s_PermitType)) {
								 if(p_ObjType.equals("D")) {
										if(liDocMaxPermit > 3 
										  && s_Author.equals("g_" + dto.getTgCabinetcode().toLowerCase()) 
										    && dto.getSrcCabinetcode().equals(dto.getTgCabinetcode()) 
										     && s_DocStatus.equals("L"))  s_PermitType="D";
								 }
					  			  
					  			  s_Dql = "UPDATE edms_auth_base  OBJECTS SET u_permit_type='"+ s_PermitType+"' " +
									  	"WHERE u_obj_id='" + rObjectId+"" + "' AND u_author_id = '" + s_Author + "'  and u_add_gubun='"+ s_AddGubun+"' and u_doc_status='"+ s_DocStatus+"'";
					  			  idf_Qry.setDQL(s_Dql);
								  idf_Colb = idf_Qry.execute(idfSession, DfQuery.QUERY);
								  if (idf_Colb!=null && idf_Colb.next()) idf_Colb.close();
								
								  if(null !=delMap.get(s_Author) && (!delMap.get(s_Author).equals("D") && s_PermitType.equals("R")) || s_PermitType.equals("D")) {
                                      if(idfDoc_Obj.getString("u_doc_status").equals(lDocStatus[i] )) {
										  idfDoc_Obj.grant(s_Author, GrantedLevels.findByLabel(s_PermitType), "");
										  //문서권한 줄 때 기본권한(G)는 사내나 그룹사 문서가 아니면 _sub권한(하위 모두 볼 수 있는 권한) 을 주지 않는다. 
						  		          if(s_AuthorType.equals("D") ){//
						  		        	  //if(!(s_AddGubun.equals("G") && (idfDoc_Obj.getString("u_sec_level").equals("S") || idfDoc_Obj.getString("u_sec_level").equals("T")))) {//
					  		        		  if(!idfDoc_Obj.getString("u_doc_status").equals(s_DocStatus)) continue;
	
						  		        	  if(s_AddGubun.equals("P") ||s_AddGubun.equals("W")) {// && (!idfDoc_Obj.getString("u_sec_level").equals("S") && !idfDoc_Obj.getString("u_sec_level").equals("T"))) {
					  		                      idfDoc_Obj.grant(s_Author+"_sub", GrantedLevels.findByLabel(s_PermitType), ""); //삭제가능 grant 권한 부여
						  		        	  }
							  		      }
										  idfDoc_Obj.save();				  			
                                      }
								  }
					  		  }
			  	    	  }
			  	    }
			  	    if(p_ObjType.equals("F") && lDocStatus[i].equals("L") && s_AddGubun.equals("P") && s_AuthorType.equals("D")) {  //폴더 라이브이면서, P로 추가된 조직은 _sub를 author_type='S' 로 추가해준다.
				  	    i_AuthorCnt = DCTMUtils.getCountByDQL( idfSession, " edms_auth_base where u_obj_id='"+rObjectId +"' and u_author_id='"+s_Author +"' and u_doc_status='L' and u_author_type ='S' and u_add_gubun='P' ");
				  	    if(i_AuthorCnt < 1) {
				  	    	IDfPersistentObject idf_PObj = idfSession.newObject("edms_auth_base");
				  	    	idf_PObj.setString("u_obj_id"		, rObjectId);
				  			idf_PObj.setString("u_obj_type"		, p_ObjType);    //폴더, 문서 
				  			idf_PObj.setString("u_doc_status"	, "L");
				  			idf_PObj.setString("u_permit_type"	, s_PermitType);
							idf_PObj.setString("u_own_dept_yn"	, s_Author.equals("g_" + dto.getTgCabinetcode()) ? "Y" : "");
							idf_PObj.setString("u_author_id"	, s_Author+"_sub");
							idf_PObj.setString("u_author_type"	, "S"); //기본권한그룹
				  			idf_PObj.setString("u_create_user"	, dto.getReqUser());
				  			idf_PObj.setTime  ("u_create_date"	, new DfTime());
				  			idf_PObj.setString("u_add_gubun"	, s_AddGubun);  //속성추가
				  			idf_PObj.save();				  	    	
				  	    }
			  	    }
			  	    	
			  	    //if(i==0 ) {  //최초 1회( C, L 두 번 루프돌 경우)
			  		    if(p_ObjType.equals("D") && null !=idfDoc_Obj){//문서
	
			  		    	if((null !=delMap.get(s_Author) && !delMap.get(s_Author).equals("D") && s_PermitType.equals("R")) || s_PermitType.equals("D")) {
	  		        		    if(!idfDoc_Obj.getString("u_doc_status").equals(s_DocStatus)) continue;
				  		    	//if( idfDoc_Obj.getPermitEx( s_Author) != GrantedLevels.findByLabel(s_PermitType) ) {
				  		    		idfDoc_Obj.grant(s_Author, GrantedLevels.findByLabel(s_PermitType), ""); //삭제가능 grant 권한 부여
				  		    		if(s_AuthorType.equals("D")) {//
				  		    			if(s_AddGubun.equals("P") ||s_AddGubun.equals("W")) {// && (!idfDoc_Obj.getString("u_sec_level").equals("S") && !idfDoc_Obj.getString("u_sec_level").equals("T"))) {//
				  		            	    idfDoc_Obj.grant(s_Author+"_sub", GrantedLevels.findByLabel(s_PermitType), ""); //삭제가능 grant 권한 부여
				  		    			}
					  		        }
						  	    	idfDoc_Obj.save();
							    //}
				  		   }
			  		    }
			  	    //}
	  		    }
	  		    
	  	    }
	  		if(idf_Col != null) idf_Col.close();
	  		if(idf_Colb != null) idf_Colb.close();
  		}catch (Exception e) {
   			e.printStackTrace();
   			return false;
	    }finally {
	    	if(idf_Col != null) idf_Col.close();
	    	if(idf_Colb != null) idf_Colb.close();
	    }
  		return true ;
    }
    
    @Override
    public void addMoveAuthBase(UserSession userSession, IDfSession idfSession, String rObjectId, String ps_RcevCab, String objType, DPath dto, IDfDocument idfNewDoc) throws Exception{
    	
    	String jobGubun=dto.getUptPthGbn(); //C :복사, M :이동
    	
    	if(null==psrvPMapDf.get("S"))   initMapInfo();
    	
    	List<String> lDocStatus = new ArrayList<String>();
    	IDfCollection idf_Col=null;  		
    	
    	try {  
/*
 * 복사시 
    -> 부서,등급 변경 여부
    -> link파일은 원본을 복사 (추가 )
    
    - acl_name 지정
    - 상위폴더 auth_share 여부: 대상폴더의 auth_share가져다가 S구분 사용자로 등록하면서, grant
    - 복사자id와  g_문서함코드를 auth_base에 등록, 복사자 id에  grant로 삭제권한 부여
*/    

		boolean bWork=false;
        //author_type:D, add_gubun='P' ; 프로젝트 루트로 갈 경우 참여부서 추가시
		
		String ls_DocPermitType = "R";
		int liDocMaxPermit =3;
		if(null !=idfNewDoc && objType.equals("D")) {  //문서의 경우, 파라메터로 넘어오는 rObjectId값에 u_doc_key( 복사는 신규문서라 r_object_id=u_doc_key, 이동은 u_doc_key)
			ls_DocPermitType =idfNewDoc.getString("u_doc_status").equals("C")?"R":"D";
			liDocMaxPermit =GrantedLevels.findByLabel(ls_DocPermitType);			
		}
		
		boolean bInitialize=false;
		boolean bFolAuthChg=true;
		if( (null != dto.getPrCode() && !dto.getPrCode().equals("") && !dto.getPrCode().equals(" ")) 
			|| (dto.getSourceGubun().equals("DFO") && (dto.getTargetGubun().equals("PJC")))
			|| (dto.getSourceGubun().equals("DFO") && (dto.getTargetGubun().equals("RJC")))
			|| (dto.getSourceGubun().equals("DFO") && (dto.getTargetGubun().equals("PFO") || dto.getTargetGubun().equals("RFO")))
			|| (dto.getTargetGubun().equals("DFO") && (dto.getSourceGubun().equals("PFO") || dto.getSourceGubun().equals("RFO")|| dto.getSourceGubun().equals("DEL")))
			|| dto.getTargetDboxId().equals(dto.getTgCabinetcode())
			|| !dto.getSrcCabinetcode().equals(dto.getTgCabinetcode())  //부서가 바뀌는 경우
			) { //프로젝트를 생성해서 가거나, 프로젝트폴더밑으로 가는 경우 
				//함간 이동복사시  공유협업권한 초기화
			/*
			    if( dto.getTargetDboxId().equals(dto.getTgCabinetcode()) && dto.getSourceGubun().equals("DFO") && dto.getTargetGubun().equals("DPC") && dto.getSrcCabinetcode().equals(dto.getTgCabinetcode())){ //부서폴더에서 부서함 Root로 가는 경우
			    	if(dto.getUptPthGbn().equals("C")) { //복사의 경우 기존권한을 유지한다.
			    		bFolAuthChg=false;
			    	}else { //이동의 경우 기존 권한 유지 
			    		bFolAuthChg=false;
			    	}
			    	
			    }else {
            */			    
					String s_Dql_d = "delete edms_auth_share object  where u_obj_id = '" + rObjectId + "' ";
		            idf_Col = DCTMUtils.getCollectionByDQL(idfSession, s_Dql_d, DfQuery.DF_QUERY);
		            if(idf_Col != null) idf_Col.close();
	
		            //함간 이동복사시  기본권한 초기화
					s_Dql_d = "delete edms_auth_base object  where u_obj_id = '" + rObjectId + "' ";
		            idf_Col = DCTMUtils.getCollectionByDQL(idfSession, s_Dql_d, DfQuery.DF_QUERY);
		            if(idf_Col != null) idf_Col.close();
	
		            //if( (dto.getSourceGubun().equals("DFO") && (dto.getTargetGubun().equals("PFO") || dto.getTargetGubun().equals("RFO")))
					//   || (dto.getTargetGubun().equals("DFO") && (dto.getSourceGubun().equals("PFO") || dto.getSourceGubun().equals("RFO"))))
		            //    bInitialize=false;  //폴더간 복사때는 상위폴더의 공유협업을 상속받아 사용한다
		            //else
		            bInitialize=true;  //루트로 이동/복사한 경우 프로젝트나 연구과제는 참여부서권한을 추가한다
			  //  }
		}

        if(jobGubun.equals("C")) {
        	
			if(objType.equals("F")) { //폴더

		    	IDfPersistentObject idfFol_Obj = idfSession.getObject(new DfId(rObjectId));
		    	String s_UFolId = idfFol_Obj.getString("u_up_fol_id");
		    	IDfPersistentObject idfUpFol_Obj =null;
		    	if (DfId.isObjectId(s_UFolId)) {
		    		idfUpFol_Obj = idfSession.getObject(new DfId(s_UFolId));

                    //“잠금” 처리된 폴더 내 폴더 및 문서의 생성/수정/삭제/이동/이관은 불가하다. ※ “잠금” 처리된 폴더 자체의 이관은 가능함.(복사 가능 !, 단 잠금처리되지 않은 폴더로)
                    //잠금폴더내 폴더나 문서를 잠금처리되지 않은 폴더로 복사하면 상위폴더가 잠금상태가 아니므로 잠금 상태를 상속받아 사용
		    		idfFol_Obj.setString("u_fol_status",  idfUpFol_Obj.getString("u_fol_status"));
		    		// 기타 상속 속성 확인필요
			    	idfFol_Obj.save();
		    	}else {
		    	    if(s_UFolId.equals("") || s_UFolId.equals(" ")) {

		    	    	if(!idfFol_Obj.getString("u_pr_code").equals("") && !idfFol_Obj.getString("u_pr_code").equals(" ") && idfFol_Obj.getString("u_pr_type").equals("P")) {
		    	      	 	String s_Dql = "select u_sec_level from edms_project where u_cabinet_code = '" + idfFol_Obj.getString("u_cabinet_code") + "' and u_pjt_code='"+idfFol_Obj.getString("u_pr_code") +"'";
			    	  		IDfQuery 		idf_Qry 	= null;
			    	  		idf_Qry = new DfQuery();
			    	  		idf_Qry.setDQL(s_Dql);
			    	  		try {
			    	  			idf_Col = idf_Qry.execute(idfSession,DfQuery.QUERY);
			    		  		while(idf_Col != null && idf_Col.next()) dto.setTargetSecLevel(idf_Col.getString("u_sec_level"));
			    	  		} catch (Exception e) {
			    				e.printStackTrace();
			    		    }finally {
			    		    	if(idf_Col !=null) idf_Col.close();
			    		    }
	
			    	    }else if(!idfFol_Obj.getString("u_pr_code").equals("") && !idfFol_Obj.getString("u_pr_code").equals(" ") && idfFol_Obj.getString("u_pr_type").equals("R")) { 
		    	      	 	String s_Dql = "select u_sec_level from edms_research where u_cabinet_code = '" + idfFol_Obj.getString("u_cabinet_code") + "' and u_rsch_code='"+idfFol_Obj.getString("u_pr_code") +"'";
			    	  		IDfQuery 		idf_Qry 	= null;
			    	  		idf_Qry = new DfQuery();
			    	  		idf_Qry.setDQL(s_Dql);
			    	  		try {
			    	  			idf_Col = idf_Qry.execute(idfSession,DfQuery.QUERY);
			    		  		while(idf_Col != null && idf_Col.next()) dto.setTargetSecLevel(idf_Col.getString("u_sec_level"));
			    	  		} catch (Exception e) {
			    				e.printStackTrace();
			    		    }finally {
			    		    	if(idf_Col !=null) idf_Col.close();
			    		    }
			    	    }
		    	    }
		    	}

//복사-폴더		    
		    	String ls_BeforeSecLevel = idfFol_Obj.getString("u_sec_level");// 직전 보안 등급, 
		    	
				if (bFolAuthChg && secLevelMap.get( ls_BeforeSecLevel) < secLevelMap.get( idfUpFol_Obj==null?dto.getTargetSecLevel():idfUpFol_Obj.getString("u_sec_level"))) {
					idfFol_Obj.setString("u_sec_level", idfUpFol_Obj==null?dto.getTargetSecLevel():idfUpFol_Obj.getString("u_sec_level"));
					idfFol_Obj.save();
				}					

		    	List<String> lst_Author =getFolderAuthorList(userSession, idfSession, rObjectId, dto );
				for(String s_Author : lst_Author)
				{
						IDfPersistentObject idf_PObj = idfSession.newObject("edms_auth_base");
				  		int i_AuthorCnt = DCTMUtils.getCountByDQL( idfSession, " edms_auth_base where u_obj_id='"+rObjectId +"' and u_author_id='"+s_Author +"' and u_doc_status='L'  ");
				  	    if(i_AuthorCnt < 1) {
	
							idf_PObj.setString("u_obj_id"		, rObjectId);
							idf_PObj.setString("u_obj_type"		, objType);
							idf_PObj.setString("u_doc_status"	, "L");
							idf_PObj.setString("u_permit_type"	, "D");
							idf_PObj.setString("u_own_dept_yn"	, s_Author.equals("g_" + ps_RcevCab) ? "Y" : "");
							idf_PObj.setString("u_author_id"	, s_Author);
							idf_PObj.setString("u_author_type"	, s_Author.equals("g_" + ps_RcevCab) ? "D" : "S");
							idf_PObj.setString("u_create_user"	, dto.getReqUser());
							idf_PObj.setTime  ("u_create_date"	, new DfTime());
							idf_PObj.setString("u_add_gubun"	, "G");
				  	    }
						
						idf_PObj.save(); 
						if(!idfFol_Obj.getString("u_sec_level").equals("S")) {
							if( s_Author.equals("g_" + ps_RcevCab)) {  //Closed이면서 author_type이 S 인 것이 들어가지 않도록 함
								idf_PObj = idfSession.newObject("edms_auth_base");
								idf_PObj.setString("u_obj_id"		, rObjectId);
								idf_PObj.setString("u_obj_type"		, objType);
								idf_PObj.setString("u_doc_status"	, "C");
								idf_PObj.setString("u_permit_type"	, "R");//Closed는 조회/다운로드
								idf_PObj.setString("u_own_dept_yn"	, s_Author.equals("g_" + ps_RcevCab) ? "Y" : "");
								idf_PObj.setString("u_author_id"	, s_Author);
								idf_PObj.setString("u_author_type"	, s_Author.equals("g_" + ps_RcevCab) ? "D" : "S");
								idf_PObj.setString("u_create_user"	, dto.getReqUser());
								idf_PObj.setTime  ("u_create_date"	, new DfTime());
								idf_PObj.setString("u_add_gubun"	, "G");
								idf_PObj.save();
							}
						}
				}
//복사-폴더			
				if( !bInitialize) { 
				    bWork= setFolderAuthShareList(userSession, idfSession, s_UFolId, rObjectId , dto, objType) ;//공유그룹 권한자
					if(null !=dto.getPreFolderId()) { //직전폴더
					    idfFol_Obj = idfSession.getObject(new DfId(dto.getPreFolderId()));
					}
					if( dto.getTargetFolType().equals("PFO") || dto.getTargetFolType().equals("RFO")) { //프로젝트나 연구과제용 폴더생성 작업인 경우
						bWork=setProjectJoinDeptToAuthbase( userSession, idfSession, rObjectId , dto, "F" ) ;
					}
					
					if (bFolAuthChg && secLevelMap.get(ls_BeforeSecLevel) < secLevelMap.get( idfUpFol_Obj==null?dto.getTargetSecLevel():idfUpFol_Obj.getString("u_sec_level"))) {
					}else {
						bWork= setFolderAuthBaseList(userSession, idfSession, dto.getPreFolderId(), rObjectId , dto, objType)  ;//직전폴더 속성 상속
					}
				}else {
					if( dto.getTargetFolType().equals("PFO") || dto.getTargetFolType().equals("RFO")) { //프로젝트나 연구과제용 폴더생성 작업인 경우
						bWork=setProjectJoinDeptToAuthbase( userSession, idfSession, rObjectId , dto, "F" ) ;
					}
					if( !s_UFolId.equals("") && !s_UFolId.equals(" ")) {
					    bWork= setFolderAuthShareList(userSession, idfSession, s_UFolId, rObjectId , dto, objType) ;//공유그룹 권한자
					    if (secLevelMap.get(ls_BeforeSecLevel) < secLevelMap.get( idfUpFol_Obj==null?dto.getTargetSecLevel():idfUpFol_Obj.getString("u_sec_level"))) {
					        bWork= setFolderAuthBaseList(userSession, idfSession, s_UFolId, rObjectId , dto, objType)  ;//상위폴더 속성 상속
					    }
					}
				}
				
				lDocStatus.add("C");
				lDocStatus.add("L");
				//복사자 추가
		  		String s_Author = dto.getReqUser(); // 복사자id : grant함
				for(int i=0; i< lDocStatus.size(); i++) {
			  	    int i_AuthorCnt = DCTMUtils.getCountByDQL( idfSession, " edms_auth_base where u_obj_id='"+rObjectId +"' and u_author_id='"+s_Author +"' and u_doc_status='"+lDocStatus.get(i)+"'");
			  	    if(i_AuthorCnt < 1) {
				  	    IDfPersistentObject idf_PObj = idfSession.newObject("edms_auth_base");
				       
				        idf_PObj = idfSession.newObject("edms_auth_base");
				  		idf_PObj.setString("u_obj_id"		, rObjectId);
				  		idf_PObj.setString("u_obj_type"		, objType);
				  		idf_PObj.setString("u_doc_status"	, lDocStatus.get(i));
			  			if(lDocStatus.get(i).equals("C"))
			  				idf_PObj.setString("u_permit_type"	, "R");
			  			else
				  		    idf_PObj.setString("u_permit_type"	, "D");
						idf_PObj.setString("u_own_dept_yn"	, " ");
						idf_PObj.setString("u_author_id"	, s_Author);
						idf_PObj.setString("u_author_type"	, "U");  //개인
				  		idf_PObj.setString("u_create_user"	, dto.getReqUser());
				  		idf_PObj.setTime  ("u_create_date"	, new DfTime());
				  		
				  		if(idfFol_Obj.getString("u_sec_level").equals("S") && lDocStatus.get(i).equals("C"))
				  		    idf_PObj.setString("u_add_gubun"	, "G");
				  		else
				  		    idf_PObj.setString("u_add_gubun"	, "P");
				  		idf_PObj.save();
			  	    }
		  	    }
//복사-문서				
			}else if(objType.equals("D")) { //문서
				
		  		    String s_DocStatus=idfNewDoc.getString("u_doc_status");
		  		    if(!s_DocStatus.equals("C") &&  !idfNewDoc.getString("u_sec_level").equals("S")) lDocStatus.add("L");
					lDocStatus.add("C");
				
			    	String s_UFolId = idfNewDoc.getString("u_fol_id");
			    	IDfPersistentObject idfUpFol_Obj =null;  //보안등급 비교용 상위 폴더 
			    	if (DfId.isObjectId(s_UFolId)) {
			    		idfUpFol_Obj = idfSession.getObject(new DfId(s_UFolId));
			    	}else {
			    	    if(s_UFolId.equals("") || s_UFolId.equals(" ")) {
			    	    	
			    	    	if(!idfNewDoc.getString("u_pr_code").equals("") && !idfNewDoc.getString("u_pr_code").equals(" ") && idfNewDoc.getString("u_pr_type").equals("P")) {
			    	      	 	String s_Dql = "select u_sec_level from edms_project where u_cabinet_code = '" + idfNewDoc.getString("u_cabinet_code") + "' and u_pjt_code='"+idfNewDoc.getString("u_pr_code") +"'";
				    	  		IDfQuery 		idf_Qry 	= null;
				    	  		idf_Qry = new DfQuery();
				    	  		idf_Qry.setDQL(s_Dql);
				    	  		try {
				    	  			idf_Col = idf_Qry.execute(idfSession,DfQuery.QUERY);
				    		  		while(idf_Col != null && idf_Col.next()) dto.setTargetSecLevel(idf_Col.getString("u_sec_level"));
				    	  		} catch (Exception e) {
				    				e.printStackTrace();
				    		    }finally {
				    		    	if(idf_Col !=null) idf_Col.close();
				    		    }
		
				    	    }else if(!idfNewDoc.getString("u_pr_code").equals("") && !idfNewDoc.getString("u_pr_code").equals(" ") && idfNewDoc.getString("u_pr_type").equals("R")) { 
			    	      	 	String s_Dql = "select u_sec_level from edms_research where u_cabinet_code = '" + idfNewDoc.getString("u_cabinet_code") + "' and u_rsch_code='"+idfNewDoc.getString("u_pr_code") +"'";
				    	  		IDfQuery 		idf_Qry 	= null;
				    	  		idf_Qry = new DfQuery();
				    	  		idf_Qry.setDQL(s_Dql);
				    	  		try {
				    	  			idf_Col = idf_Qry.execute(idfSession,DfQuery.QUERY);
				    		  		while(idf_Col != null && idf_Col.next()) dto.setTargetSecLevel(idf_Col.getString("u_sec_level"));
				    	  		} catch (Exception e) {
				    				e.printStackTrace();
				    		    }finally {
				    		    	if(idf_Col !=null) idf_Col.close();
				    		    }
				    	    }
			    	    }
			    	}
			    	String ls_BeforeSecLevel = idfNewDoc.getString("u_sec_level");
			  		if (bFolAuthChg && secLevelMap.get( ls_BeforeSecLevel) < secLevelMap.get( null==idfUpFol_Obj?dto.getTargetSecLevel():idfUpFol_Obj.getString("u_sec_level"))) { 
			  			idfNewDoc.setString("u_sec_level", idfUpFol_Obj==null?dto.getTargetSecLevel():idfUpFol_Obj.getString("u_sec_level"));
			  		    idfNewDoc.save();
			  		}
/* r_object_id가 새 것이라 삭제할 게 없음
			    	if( !bInitialize) {
		                ////////////////////////2. 직전 auth_base 정리 (보안등급 변경없으면, 속성추가항목 유지, 그렇지 않으면 삭제대상에 추가(P, ' ')///
						String s_Dql_d = "delete edms_auth_base object " +
							        " where u_obj_id = '" + rObjectId + "'  and (u_add_gubun='S' or u_add_gubun='P' or u_add_gubun=' ' or u_add_gubun='J') ";  // 보안등급기본 제거, 권한이 낮으면 이전에 추가되었던 '속성'추가 항목도 제거
			            idf_Col = DCTMUtils.getCollectionByDQL(idfSession, s_Dql_d, DfQuery.DF_QUERY);
			            if(idf_Col != null) idf_Col.close();
			    	}
*/
			  		//프로젝트에서 상속받는 권한이 있는 경우
			  		if( dto.getTargetFolType().equals("PFO") || dto.getTargetFolType().equals("RFO")) { //프로젝트나 연구과제용 폴더생성 작업인 경우
						bWork=setProjectJoinDeptToAuthbase( userSession, idfSession, idfNewDoc.getString("r_object_id") , dto, "D" ) ;
					}
//복사-문서처리			    	
			  		if(!s_UFolId.equals("") && !s_UFolId.equals(" ")) {
			    	    bWork= setFolderAuthShareList(userSession, idfSession, s_UFolId, idfNewDoc.getString("r_object_id") , dto, objType) ;//문서
			  		}
					
			  		String s_TargetSecLevel="T";

			  		if( !bInitialize) {
				  		if (bFolAuthChg && secLevelMap.get(ls_BeforeSecLevel) < secLevelMap.get( null==idfUpFol_Obj?dto.getTargetSecLevel():idfUpFol_Obj.getString("u_sec_level"))) { 
						    bWork= setFolderAuthBaseList(userSession, idfSession, s_UFolId, idfNewDoc.getString("r_object_id") , dto, objType) ;
				  		}else {
							bWork= setFolderAuthBaseList(userSession, idfSession, idfNewDoc.getString("u_copy_org_id"), idfNewDoc.getString("r_object_id") , dto, objType)  ;//속성추가 권한자
				  		}
			  		}else {
			  			if (secLevelMap.get( ls_BeforeSecLevel) < secLevelMap.get( null==idfUpFol_Obj?dto.getTargetSecLevel():idfUpFol_Obj.getString("u_sec_level"))) {
		  			        bWork= setFolderAuthBaseList(userSession, idfSession, s_UFolId, idfNewDoc.getString("r_object_id") , dto, objType) ;//폴더
			  			}
			  		}
//복사-문서처리		
			  		if(!s_DocStatus.equals("C")) lDocStatus.add("L");
			  		for(int i=0; i< lDocStatus.size(); i++) {
				  	    String s_Author = "g_" + ps_RcevCab;  //g_부서 : grant는 하지 않음
						if (secLevelMap.get(ls_BeforeSecLevel) < secLevelMap.get(dto.getTargetSecLevel())) {
						    if(idfNewDoc.getString("u_sec_level").equals("G"))      s_Author =  "g_"+dto.getUGroupOrgCabinetCd();
						    else if(idfNewDoc.getString("u_sec_level").equals("C")) s_Author =  "g_"+dto.getUComOrgCabinetCd();
						}
				  	    
				  	    int i_AuthorCnt=0;
				  	  
				  	    if(idfNewDoc.getString("u_sec_level").equals("S")) {  //제한등급의 closed권한에는 추가하지 않는다
				  	    	if(!lDocStatus.get(i).equals("C")) {
					  	    	liDocMaxPermit=7;
					  	    	i_AuthorCnt = DCTMUtils.getCountByDQL( idfSession, " edms_auth_base where u_obj_id='"+rObjectId +"' and u_author_id='"+s_Author +"' and u_doc_status='"+lDocStatus.get(i)+"'");
						  	    if(i_AuthorCnt < 1) {
							  	    IDfPersistentObject idf_PObj = idfSession.newObject("edms_auth_base");
							        idf_PObj = idfSession.newObject("edms_auth_base");
							  		idf_PObj.setString("u_obj_id"		, rObjectId);
							  		idf_PObj.setString("u_obj_type"		, objType);
							  		idf_PObj.setString("u_doc_status"	, lDocStatus.get(i));
					  				idf_PObj.setString("u_permit_type"	, "D");
									idf_PObj.setString("u_own_dept_yn"	, "Y");
									idf_PObj.setString("u_author_id"	, s_Author);
									idf_PObj.setString("u_author_type"	,"D"); //부서
							  		idf_PObj.setString("u_create_user"	, dto.getReqUser());
							  		idf_PObj.setTime  ("u_create_date"	, new DfTime());
							  		idf_PObj.setString("u_add_gubun"	, "G");
							  		idf_PObj.save();
						  	    }
				  	    	}
                                   ;//제한등급에 복사자를 지정하기 위해 pass
				  	    }else {
					  	    i_AuthorCnt = DCTMUtils.getCountByDQL( idfSession, " edms_auth_base where u_obj_id='"+rObjectId +"' and u_author_id='"+s_Author +"' and u_doc_status='"+lDocStatus.get(i)+"'");
					  	    if(i_AuthorCnt < 1) {
						  	    IDfPersistentObject idf_PObj = idfSession.newObject("edms_auth_base");
						        idf_PObj = idfSession.newObject("edms_auth_base");
						  		idf_PObj.setString("u_obj_id"		, rObjectId);
						  		idf_PObj.setString("u_obj_type"		, objType);
						  		idf_PObj.setString("u_doc_status"	, lDocStatus.get(i));
					  			if(lDocStatus.get(i).equals("C"))
					  				idf_PObj.setString("u_permit_type"	, "R");
					  			else
						  		    idf_PObj.setString("u_permit_type"	, "D");
								idf_PObj.setString("u_own_dept_yn"	, "Y");
								idf_PObj.setString("u_author_id"	, s_Author);
								idf_PObj.setString("u_author_type"	,"D"); //부서
						  		idf_PObj.setString("u_create_user"	, dto.getReqUser());
						  		idf_PObj.setTime  ("u_create_date"	, new DfTime());
						  		idf_PObj.setString("u_add_gubun"	, "G");
						  		idf_PObj.save();
					  	    }	  	    
				  	    }
				  	    if(i==0  && s_DocStatus.equals("L")) {//&& idfNewDoc.getPermitEx( s_Author) != liDocMaxPermit) {
				  	        idfNewDoc.grant(s_Author, liDocMaxPermit,"");
				  	        if((!idfNewDoc.getString("u_sec_level").equals("S") && !idfNewDoc.getString("u_sec_level").equals("T"))) {//
				  	            idfNewDoc.grant(s_Author+"_sub", liDocMaxPermit,"");
				  	        }
				  		    idfNewDoc.save();
				  	    }						  	    
				  	    
//복사-문서처리
				  		s_Author = dto.getReqUser(); // 복사자id : grant함
				  		liDocMaxPermit=7;
				  		i_AuthorCnt = DCTMUtils.getCountByDQL( idfSession, " edms_auth_base where u_obj_id='"+rObjectId +"' and u_author_id='"+s_Author +"' and u_doc_status='"+lDocStatus.get(i)+"'");
				  	    if(i_AuthorCnt < 1) {
					  	    IDfPersistentObject idf_PObj = idfSession.newObject("edms_auth_base");
					        idf_PObj = idfSession.newObject("edms_auth_base");
					  		idf_PObj.setString("u_obj_id"		, rObjectId);
					  		idf_PObj.setString("u_obj_type"		, objType);
					  		idf_PObj.setString("u_doc_status"	, lDocStatus.get(i));
				  			if(lDocStatus.get(i).equals("C"))
				  				idf_PObj.setString("u_permit_type"	, "R");
				  			else
					  		    idf_PObj.setString("u_permit_type"	, "D");
							idf_PObj.setString("u_own_dept_yn"	, " ");
							idf_PObj.setString("u_author_id"	, s_Author);
							idf_PObj.setString("u_author_type"	, "U");  //개인
					  		idf_PObj.setString("u_create_user"	, dto.getReqUser());
					  		idf_PObj.setTime  ("u_create_date"	, new DfTime());

					  		if(idfNewDoc.getString("u_sec_level").equals("S"))   //제한등급의 closed권한에는 추가하지 않는다
					  		    idf_PObj.setString("u_add_gubun"	, "G");
					  		else
					  			idf_PObj.setString("u_add_gubun"	, "P");
					  		idf_PObj.save();
				  	    }
				  	    
				  	    if(i==0 ) {//&& idfNewDoc.getPermitEx( s_Author) != liDocMaxPermit) {
				  	        idfNewDoc.grant(s_Author, liDocMaxPermit,"");
				  		    idfNewDoc.save();
				  	    }				  	    
		  		    }
					
			}
///  이동 처리에서 사용하는 부분 ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////			
///  이동 처리에서 사용하는 부분 ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////			
		}else if( jobGubun.equals("M")) {  //이동처리
			/* 이동시
			  -> 문서함,등급 변경여부 
			    - acl_name 지정
			    - auth_base 원본의 W인 auth_base사용자들 가져다가 grant 
			    : 부서,등급 변경이 없으면 acl_name그대로 사용   
			      : 변경이 있으면 원본에서 W 인 auth_base사용자들만 상속받고 grant


			     대상폴더의 auth_share가져다가 S구분 사용자로 등록하면서, grant
			    대상폴더의 보안등급이 더 높을때는 auth_base에 속성으로 추가한 사용자들을 상속한다. 		
			 */
			
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////				
//이동 -폴더-권한처리 Start
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			if(objType.equals("F")) { //폴더
//이동-폴더
				IDfPersistentObject idfFol_Obj = idfSession.getObject(new DfId(rObjectId));
		    	String s_UFolId = idfFol_Obj.getString("u_up_fol_id");
		    	IDfPersistentObject idfUpFol_Obj =null;
		    	if (DfId.isObjectId(s_UFolId)) {
		    		idfUpFol_Obj = idfSession.getObject(new DfId(s_UFolId));

                    //“잠금” 처리된 폴더 내 폴더 및 문서의 생성/수정/삭제/이동/이관은 불가하다. ※ “잠금” 처리된 폴더 자체의 이관은 가능함.(복사 가능 !, 단 잠금처리되지 않은 폴더로)
                    //잠금폴더내 폴더나 문서를 잠금처리되지 않은 폴더로 복사하면 상위폴더가 잠금상태가 아니므로 잠금 상태를 상속받아 사용
		    		idfFol_Obj.setString("u_fol_status",  idfUpFol_Obj.getString("u_fol_status"));
		    		// 기타 상속 속성 확인필요
			    	idfFol_Obj.save();
		    	}		    	    
		    	if(s_UFolId.equals("") || s_UFolId.equals(" ")) {

	    	    	if(!idfFol_Obj.getString("u_pr_code").equals("") && !idfFol_Obj.getString("u_pr_code").equals(" ") && idfFol_Obj.getString("u_pr_type").equals("P")) {
	    	      	 	String s_Dql = "select u_sec_level from edms_project where u_cabinet_code = '" + idfFol_Obj.getString("u_cabinet_code") + "' and u_pjt_code='"+idfFol_Obj.getString("u_pr_code") +"'";
		    	  		IDfQuery 		idf_Qry 	= null;
		    	  		idf_Qry = new DfQuery();
		    	  		idf_Qry.setDQL(s_Dql);
		    	  		try {
		    	  			idf_Col = idf_Qry.execute(idfSession,DfQuery.QUERY);
		    		  		while(idf_Col != null && idf_Col.next()) dto.setTargetSecLevel(idf_Col.getString("u_sec_level"));
		    	  		} catch (Exception e) {
		    				e.printStackTrace();
		    		    }finally {
		    		    	if(idf_Col !=null) idf_Col.close();
		    		    }

		    	    }else if(!idfFol_Obj.getString("u_pr_code").equals("") && !idfFol_Obj.getString("u_pr_code").equals(" ") && idfFol_Obj.getString("u_pr_type").equals("R")) { 
	    	      	 	String s_Dql = "select u_sec_level from edms_research where u_cabinet_code = '" + idfFol_Obj.getString("u_cabinet_code") + "' and u_rsch_code='"+idfFol_Obj.getString("u_pr_code") +"'";
		    	  		IDfQuery 		idf_Qry 	= null;
		    	  		idf_Qry = new DfQuery();
		    	  		idf_Qry.setDQL(s_Dql);
		    	  		try {
		    	  			idf_Col = idf_Qry.execute(idfSession,DfQuery.QUERY);
		    		  		while(idf_Col != null && idf_Col.next()) dto.setTargetSecLevel(idf_Col.getString("u_sec_level"));
		    	  		} catch (Exception e) {
		    				e.printStackTrace();
		    		    }finally {
		    		    	if(idf_Col !=null) idf_Col.close();
		    		    }
		    	    }
	    	    }		    	
//이동-폴더				
		        String ls_BeforeSecLevel = idfFol_Obj.getString("u_sec_level");
		        
		        if(!bFolAuthChg) dto.setTargetSecLevel(ls_BeforeSecLevel);
		        
		    	if(! bInitialize) {
	                ////////////////////////1. 직전 공유그룹 권한리스트 정리 //////////////////////////////////////////////////////////
				    String s_Dql_d = "delete edms_auth_share object  where u_obj_id = '" + rObjectId + "'"; 
			        idf_Col = DCTMUtils.getCollectionByDQL(idfSession, s_Dql_d, DfQuery.DF_QUERY);
			        if(idf_Col != null) idf_Col.close();
	
	                ////////////////////////2. 직전 auth_base 정리 (보안등급 변경없으면, 속성추가항목 유지, 그렇지 않으면 삭제대상에 추가(P, ' ')///
			    	if (bFolAuthChg && secLevelMap.get(ls_BeforeSecLevel) < secLevelMap.get( null==idfUpFol_Obj?dto.getTargetSecLevel():idfUpFol_Obj.getString("u_sec_level"))) {
	
			    		idfFol_Obj.setString("u_sec_level", idfUpFol_Obj==null?dto.getTargetSecLevel():idfUpFol_Obj.getString("u_sec_level"));
						idfFol_Obj.save();
	                    
						if(idfFol_Obj.getString("u_sec_level").equals("S")) { //제한등급일 때는 G인것도 삭제하고, 상속받는다.
					        s_Dql_d = "delete edms_auth_base object " +
					                " where u_obj_id = '" + rObjectId + "'  and (u_add_gubun in('G', 'S', 'P',' ', 'J' ) or u_author_type='S') ";  // 보안등급기본 제거, 권한이 낮으면 이전에 추가되었던 '속성'추가 항목도 제거
						}else {
					        s_Dql_d = "delete edms_auth_base object " +
					                " where u_obj_id = '" + rObjectId + "'  and (u_add_gubun in('S', 'P',' ', 'J' ) or u_author_type='S') ";  // 보안등급기본 제거, 권한이 낮으면 이전에 추가되었던 '속성'추가 항목도 제거
						}
					    idf_Col = DCTMUtils.getCollectionByDQL(idfSession, s_Dql_d, DfQuery.DF_QUERY);
					    if(idf_Col != null) idf_Col.close();
					    bWork= setFolderAuthBaseList(userSession, idfSession, s_UFolId, rObjectId , dto, objType) ;//폴더
				    }else {
						s_Dql_d = "delete edms_auth_base object " +
						        " where u_obj_id = '" + rObjectId +
						        "'  and (u_add_gubun='S' or u_author_type='S'  or u_add_gubun='J' or u_author_id='"+ "g_"+ ps_RcevCab+"' or u_author_id='"+ "g_"+ dto.getUComOrgCabinetCd()+"'  or u_author_id='"+ "g_"+ dto.getUComOrgCabinetCd()+"')  ";  // 보안등급기본, 공유협업 제거
						
						idf_Col = DCTMUtils.getCollectionByDQL(idfSession, s_Dql_d, DfQuery.DF_QUERY);
						if(idf_Col != null) idf_Col.close();
				    }
		    	}else {
		    		if (secLevelMap.get(ls_BeforeSecLevel) < secLevelMap.get(dto.getTargetSecLevel())) {
		    		    idfFol_Obj.setString("u_sec_level", idfUpFol_Obj==null?dto.getTargetSecLevel():idfUpFol_Obj.getString("u_sec_level"));
					    idfFol_Obj.save();
		    		}
		    	}
	            //idf_Col = DCTMUtils.getCollectionByDQL(idfSession, s_Dql_d, DfQuery.DF_QUERY);
//이동-폴더		        
	            ///////////////////////3.폴더속성에 따른 기본권한 추가 /////////////////////////////////////////////////////////
		    	List<String> lst_Author =getFolderAuthorList(userSession, idfSession, rObjectId, dto );
				for(String s_Author : lst_Author)
				{
						IDfPersistentObject idf_PObj = idfSession.newObject("edms_auth_base");

				  		int i_AuthorCnt = DCTMUtils.getCountByDQL( idfSession, " edms_auth_base where u_obj_id='"+rObjectId +"' and u_author_id='"+s_Author +"' and u_doc_status='L'  ");
				  	    if(i_AuthorCnt < 1) {
							idf_PObj.setString("u_obj_id"		, rObjectId);
							idf_PObj.setString("u_obj_type"		, objType);
							idf_PObj.setString("u_doc_status"	, "L");
							idf_PObj.setString("u_permit_type"	, "D");
							idf_PObj.setString("u_own_dept_yn"	, s_Author.equals("g_" + ps_RcevCab) ? "Y" : "");
							idf_PObj.setString("u_author_id"	, s_Author);
							idf_PObj.setString("u_author_type"	, s_Author.equals("g_" + ps_RcevCab) ? "D" : "S");
							idf_PObj.setString("u_create_user"	, dto.getReqUser());
							idf_PObj.setTime  ("u_create_date"	, new DfTime());
							idf_PObj.setString("u_add_gubun"	, "G");
							
							idf_PObj.save(); 
				  	    }
						
						if( s_Author.equals("g_" + ps_RcevCab) ) {  //Closed이면서 author_type이 S 인 것이 들어가지 않도록 함
							if(idfFol_Obj.getString("u_sec_level").equals("S")) s_Author = dto.getReqUser();
					  		i_AuthorCnt = DCTMUtils.getCountByDQL( idfSession, " edms_auth_base where u_obj_id='"+rObjectId +"' and u_author_id='"+s_Author +"' and u_doc_status='C'  ");
					  	    if(i_AuthorCnt < 1) {
							
								idf_PObj = idfSession.newObject("edms_auth_base");
								idf_PObj.setString("u_obj_id"		, rObjectId);
								idf_PObj.setString("u_obj_type"		, objType);
								idf_PObj.setString("u_doc_status"	, "C");
								idf_PObj.setString("u_permit_type"	, "R");
								idf_PObj.setString("u_own_dept_yn"	, s_Author.equals("g_" + ps_RcevCab) ? "Y" : "");
								idf_PObj.setString("u_author_id"	, s_Author);
								idf_PObj.setString("u_author_type"	, s_Author.equals("g_" + ps_RcevCab) ? "D" : "U");
								idf_PObj.setString("u_create_user"	, dto.getReqUser());
								idf_PObj.setTime  ("u_create_date"	, new DfTime());
								idf_PObj.setString("u_add_gubun"	, "G");
								idf_PObj.save();
					  	    }
						}
				}
//이동-폴더		
				if(! bInitialize) {
                    ///////////////////////3.상위폴더 공유속성 상속여부 체크 / 상속처리/////////////////////////////////////////////////////////
					if( bFolAuthChg)
				        bWork= setFolderAuthShareList(userSession, idfSession, s_UFolId, rObjectId , dto, objType) ;//폴더
				    if( dto.getTargetFolType().equals("PFO") || dto.getTargetFolType().equals("RFO")) { //프로젝트나 연구과제용 폴더생성 작업인 경우
						bWork=setProjectJoinDeptToAuthbase( userSession, idfSession, rObjectId , dto, "F" ) ;
					}				    
				}else {
					if( dto.getTargetFolType().equals("PFO") || dto.getTargetFolType().equals("RFO")) { //프로젝트나 연구과제용 폴더생성 작업인 경우
						bWork=setProjectJoinDeptToAuthbase( userSession, idfSession, rObjectId , dto, "F" ) ;
					}

					if( !s_UFolId.equals("") && !s_UFolId.equals(" ")) {
					    bWork= setFolderAuthShareList(userSession, idfSession, s_UFolId, rObjectId , dto, objType) ;//공유그룹 권한자
					    if (secLevelMap.get(ls_BeforeSecLevel) < secLevelMap.get(dto.getTargetSecLevel())) {
						    bWork= setFolderAuthBaseList(userSession, idfSession, s_UFolId, rObjectId , dto, objType) ;//폴더
					    }
					}
					if(!idfFol_Obj.getString("u_sec_level").equals("S")) {
						
						String s_Author = dto.getReqUser();
						if (secLevelMap.get(ls_BeforeSecLevel) < secLevelMap.get(dto.getTargetSecLevel())) {
						    if(idfFol_Obj.getString("u_sec_level").equals("G"))      s_Author =  "g_"+dto.getUGroupOrgCabinetCd();
						    else if(idfFol_Obj.getString("u_sec_level").equals("C")) s_Author =  "g_"+dto.getUComOrgCabinetCd();
						    else if(idfFol_Obj.getString("u_sec_level").equals("T")) s_Author =  "g_"+dto.getTgCabinetcode();
						}
				  		int i_AuthorCnt = DCTMUtils.getCountByDQL( idfSession, " edms_auth_base where u_obj_id='"+rObjectId +"' and u_author_id='"+s_Author +"' and u_doc_status='C' and u_add_gubun in('G','P','J', ' ') ");
				  	    if(i_AuthorCnt < 1) {
					  	    IDfPersistentObject idf_PObj = idfSession.newObject("edms_auth_base");
					        idf_PObj = idfSession.newObject("edms_auth_base");
					  		idf_PObj.setString("u_obj_id"		, rObjectId);
					  		idf_PObj.setString("u_obj_type"		, objType);
					  		idf_PObj.setString("u_doc_status"	, "C");
			  				idf_PObj.setString("u_permit_type"	, "R");
							idf_PObj.setString("u_own_dept_yn"	, " ");
							idf_PObj.setString("u_author_id"	, s_Author);
							idf_PObj.setString("u_author_type"	, "U");  //개인
					  		idf_PObj.setString("u_create_user"	, dto.getReqUser());
					  		idf_PObj.setTime  ("u_create_date"	, new DfTime());

					  		if(idfFol_Obj.getString("u_sec_level").equals("S"))   //제한등급의 closed권한에는 추가하지 않는다
					  		    idf_PObj.setString("u_add_gubun"	, "G");
					  		else
					  			idf_PObj.setString("u_add_gubun"	, "G");
					  		idf_PObj.save();
				  	    }			  	    
					}
				}
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////				
// 이동 -문서-권한처리 Start
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////				
			}else if(objType.equals("D")) { //문서
//이동-문서
		  		    String s_DocStatus=idfNewDoc.getString("u_doc_status");
					lDocStatus.add("C");

		  		    String ls_BeforeSecLevel = idfNewDoc.getString("u_sec_level");
		  		    
			    	String s_UFolId = idfNewDoc.getString("u_fol_id");
			    	IDfPersistentObject idfUpFol_Obj =null;  //보안등급 비교용 상위 폴더 
			    	if (DfId.isObjectId(s_UFolId)) {
			    		idfUpFol_Obj = idfSession.getObject(new DfId(s_UFolId));
			    	}
			    	if(s_UFolId.equals("") || s_UFolId.equals(" ")) {
		    	    	if(!idfNewDoc.getString("u_pr_code").equals("") && !idfNewDoc.getString("u_pr_code").equals(" ") && idfNewDoc.getString("u_pr_type").equals("P")) {
		    	      	 	String s_Dql = "select u_sec_level from edms_project where u_cabinet_code = '" + idfNewDoc.getString("u_cabinet_code") + "' and u_pjt_code='"+idfNewDoc.getString("u_pr_code") +"'";
			    	  		IDfQuery 		idf_Qry 	= null;
			    	  		idf_Qry = new DfQuery();
			    	  		idf_Qry.setDQL(s_Dql);
			    	  		try {
			    	  			idf_Col = idf_Qry.execute(idfSession,DfQuery.QUERY);
			    		  		while(idf_Col != null && idf_Col.next()) dto.setTargetSecLevel(idf_Col.getString("u_sec_level"));
			    	  		} catch (Exception e) {
			    				e.printStackTrace();
			    		    }finally {
			    		    	if(idf_Col !=null) idf_Col.close();
			    		    }
	
			    	    }else if(!idfNewDoc.getString("u_pr_code").equals("") && !idfNewDoc.getString("u_pr_code").equals(" ") && idfNewDoc.getString("u_pr_type").equals("R")) { 
		    	      	 	String s_Dql = "select u_sec_level from edms_research where u_cabinet_code = '" + idfNewDoc.getString("u_cabinet_code") + "' and u_rsch_code='"+idfNewDoc.getString("u_pr_code") +"'";
			    	  		IDfQuery 		idf_Qry 	= null;
			    	  		idf_Qry = new DfQuery();
			    	  		idf_Qry.setDQL(s_Dql);
			    	  		try {
			    	  			idf_Col = idf_Qry.execute(idfSession,DfQuery.QUERY);
			    		  		while(idf_Col != null && idf_Col.next()) dto.setTargetSecLevel(idf_Col.getString("u_sec_level"));
			    	  		} catch (Exception e) {
			    				e.printStackTrace();
			    		    }finally {
			    		    	if(idf_Col !=null) idf_Col.close();
			    		    }
			    	    }
		    	    }
			    	if(! bInitialize) {
			    		if(!s_DocStatus.equals("C") && !idfNewDoc.getString("u_sec_level").equals("S")) lDocStatus.add("L");
////////////////////////1. 직존 공유그룹 권한리스트 정리 //////////////////////////////////////////////////////////
			            String s_Dql_d = "delete edms_auth_share object  where u_obj_id = '" + rObjectId + "'"; 
		                idf_Col = DCTMUtils.getCollectionByDQL(idfSession, s_Dql_d, DfQuery.DF_QUERY);
		                if(idf_Col != null) idf_Col.close();
		                //////////////////////// 1. 기존 auth_base 정리 //////////////////////////////////////////////////////////		
						s_Dql_d = "delete edms_auth_base object " +
						        " where u_obj_id = '" + rObjectId + "'  and (u_add_gubun='S'  or u_add_gubun='J' or u_author_type='S' or u_author_id='"+ "g_"+ ps_RcevCab+"') ";  //공유협업에서 추가된 권한, 보안등급기본추가권한 제거

						if (secLevelMap.get(ls_BeforeSecLevel) < secLevelMap.get(null==idfUpFol_Obj?"T":idfUpFol_Obj.getString("u_sec_level"))) {
							if(idfUpFol_Obj.getString("u_sec_level").equals("S")) //제한등급이면 기존문서의 u_add_gubun 이 G인 것도 삭제
								s_Dql_d = "delete edms_auth_base object " +
								        " where u_obj_id = '" + rObjectId + "'  and u_add_gubun in('G', 'S', 'P',' ', 'J' ) ";  //공유협업에서 추가된 권한, 보안등급기본추가권한 제거, 보안등급 상향되면 '속성추가' 권한도 제거
							else
								s_Dql_d = "delete edms_auth_base object " +
								        " where u_obj_id = '" + rObjectId + "'  and u_add_gubun in('S', 'P',' ', 'J' ) ";  //공유협업에서 추가된 권한, 보안등급기본추가권한 제거, 보안등급 상향되면 '속성추가' 권한도 제거
						}
		
			            idf_Col = DCTMUtils.getCollectionByDQL(idfSession, s_Dql_d, DfQuery.DF_QUERY);
				  		if(idf_Col != null) idf_Col.close();
			    	}
//이동 - 문서 			    	
			    	if(bInitialize) {
						if (secLevelMap.get(ls_BeforeSecLevel) < secLevelMap.get(dto.getTargetSecLevel())) {
							idfNewDoc.setString("u_sec_level", idfUpFol_Obj==null?dto.getTargetSecLevel():idfUpFol_Obj.getString("u_sec_level"));
							idfNewDoc.save();
						}
			  		    //if(!s_DocStatus.equals("C") && !idfNewDoc.getString("u_sec_level").equals("S")) lDocStatus.add("L");
						if(!s_DocStatus.equals("C") ) lDocStatus.add("L");
						
						if( dto.getTargetFolType().equals("PFO") || dto.getTargetFolType().equals("RFO")) { //프로젝트나 연구과제용 폴더생성 작업인 경우
							bWork=setProjectJoinDeptToAuthbase( userSession, idfSession, idfNewDoc.getString("r_object_id") , dto, "D" ) ;
						}
						if(null !=idfUpFol_Obj) { //프로젝트 root로 바로 가는 경우 상위폴더id가 없음
				  		    //2. 상위폴더 공유그룹 체크/권한 추가
				  		    bWork= setFolderAuthShareList(userSession, idfSession, s_UFolId, idfNewDoc.getString("r_object_id") , dto, objType) ;//문서
				  		    if (secLevelMap.get(ls_BeforeSecLevel) < secLevelMap.get(dto.getTargetSecLevel())) {
				  		        bWork= setFolderAuthBaseList(userSession, idfSession, s_UFolId, idfNewDoc.getString("r_object_id") , dto, objType) ;//폴더
				  		    }
				  		    if(!idfUpFol_Obj.getString("u_sec_level").equals("S")) { //제한등급이 아닐때 팀을 u_add_gubun='G' 로 추가
								for(int i=0; i< lDocStatus.size(); i++) {
							  	    String s_Author = "g_" + ps_RcevCab;  //g_부서 : grant는 하지 않음
									if (secLevelMap.get(ls_BeforeSecLevel) < secLevelMap.get(dto.getTargetSecLevel())) {
									    if(idfUpFol_Obj.getString("u_sec_level").equals("G"))      s_Author =  "g_"+dto.getUGroupOrgCabinetCd();
									    else if(idfUpFol_Obj.getString("u_sec_level").equals("C")) s_Author =  "g_"+dto.getUComOrgCabinetCd();
									}
							  	    
							  	    int i_AuthorCnt = DCTMUtils.getCountByDQL( idfSession, " edms_auth_base where u_obj_id='"+rObjectId +"' and u_author_id='"+s_Author +"' and u_doc_status='"+lDocStatus.get(i)+"'");
							  	    if(i_AuthorCnt < 1) {
			
								  	    IDfPersistentObject idf_PObj = idfSession.newObject("edms_auth_base");
								        idf_PObj = idfSession.newObject("edms_auth_base");
								  		idf_PObj.setString("u_obj_id"		, rObjectId);
								  		idf_PObj.setString("u_obj_type"		, objType);
								  		idf_PObj.setString("u_doc_status"	, lDocStatus.get(i));
							  			if(lDocStatus.get(i).equals("C"))
							  				idf_PObj.setString("u_permit_type"	, "R");
							  			else
								  		    idf_PObj.setString("u_permit_type"	, "D");
										idf_PObj.setString("u_own_dept_yn"	, "Y");
										idf_PObj.setString("u_author_id"	, s_Author);
										idf_PObj.setString("u_author_type"	,"D"); //부서
								  		idf_PObj.setString("u_create_user"	, dto.getReqUser());
								  		idf_PObj.setTime  ("u_create_date"	, new DfTime());
								  		idf_PObj.setString("u_add_gubun"	, "G");
								  		idf_PObj.save();  		
							  	    }
							  	    if(i==0 && s_DocStatus.equals("L")) {//&& idfNewDoc.getPermitEx( s_Author) != liDocMaxPermit) {
							  	        idfNewDoc.grant(s_Author, liDocMaxPermit,"");
							  	    	if((!idfNewDoc.getString("u_sec_level").equals("S") && !idfNewDoc.getString("u_sec_level").equals("T"))) {//
							  	            idfNewDoc.grant(s_Author+"_sub", liDocMaxPermit,"");
							  	    	}
							  		    idfNewDoc.save();
							  	    }	
					  		    }
							  								
							}				  		    
						}else {
							for(int i=0; i< lDocStatus.size(); i++) {
						  	    String s_Author = "g_" + ps_RcevCab;  //g_부서 : grant는 하지 않음
								if (secLevelMap.get(ls_BeforeSecLevel) < secLevelMap.get(dto.getTargetSecLevel())) {
								    if(dto.getTargetSecLevel().equals("G"))      s_Author =  "g_"+dto.getUGroupOrgCabinetCd();
								    else if(dto.getTargetSecLevel().equals("C")) s_Author =  "g_"+dto.getUComOrgCabinetCd();
								}
						  	    
						  	    int i_AuthorCnt = DCTMUtils.getCountByDQL( idfSession, " edms_auth_base where u_obj_id='"+rObjectId +"' and u_author_id='"+s_Author +"' and u_doc_status='"+lDocStatus.get(i)+"'");
						  	    
						  	    if(idfNewDoc.getString("u_sec_level").equals("S")) {
						  	    	if(idfNewDoc.getString("u_doc_status").equals("C")) liDocMaxPermit=3;
						  	    	else liDocMaxPermit=7; //제한문서는 부서에 읽기권한 부여
						  	    }
						  	    
						  	    if(i_AuthorCnt < 1) {
						  	    	if(idfNewDoc.getString("u_sec_level").equals("S") && lDocStatus.get(i).equals("C")) {
						  	    		s_Author = dto.getReqUser();  //이동권한자
								  	    i_AuthorCnt = DCTMUtils.getCountByDQL( idfSession, " edms_auth_base where u_obj_id='"+rObjectId +"' and u_author_id='"+s_Author +"' and u_doc_status='C'");
								  	    if(i_AuthorCnt < 1) {
									  	    IDfPersistentObject idf_PObj = idfSession.newObject("edms_auth_base");
									  	    
									        idf_PObj = idfSession.newObject("edms_auth_base");
									  		idf_PObj.setString("u_obj_id"		, rObjectId);
									  		idf_PObj.setString("u_obj_type"		, objType);
									  		idf_PObj.setString("u_doc_status"	, "C");
							  		        idf_PObj.setString("u_permit_type"	, "R");
											idf_PObj.setString("u_own_dept_yn"	, " ");
											idf_PObj.setString("u_author_id"	, s_Author);
											idf_PObj.setString("u_author_type"	,"U");
									  		idf_PObj.setString("u_create_user"	, dto.getReqUser());
									  		idf_PObj.setTime  ("u_create_date"	, new DfTime());
									  		idf_PObj.setString("u_add_gubun"	, "G");
									  		idf_PObj.save();  
						  	    	    }
						  	    	}else {
								  	    IDfPersistentObject idf_PObj = idfSession.newObject("edms_auth_base");
								        idf_PObj = idfSession.newObject("edms_auth_base");
								  		idf_PObj.setString("u_obj_id"		, rObjectId);
								  		idf_PObj.setString("u_obj_type"		, objType);
								  		idf_PObj.setString("u_doc_status"	, lDocStatus.get(i));
							  			if(liDocMaxPermit==3 || lDocStatus.get(i).equals("C"))
							  				idf_PObj.setString("u_permit_type"	, "R");
							  			else if(liDocMaxPermit==7)
								  		    idf_PObj.setString("u_permit_type"	, "D");
										idf_PObj.setString("u_own_dept_yn"	, "Y");
										idf_PObj.setString("u_author_id"	, s_Author);
										idf_PObj.setString("u_author_type"	,"D"); //부서
								  		idf_PObj.setString("u_create_user"	, dto.getReqUser());
								  		idf_PObj.setTime  ("u_create_date"	, new DfTime());
								  		idf_PObj.setString("u_add_gubun"	, "G");
								  		idf_PObj.save();  		
						  	    	}
						  	    }
						  			  
						  	    if(i==0 && s_DocStatus.equals("L")) {//&& idfNewDoc.getPermitEx( s_Author) != liDocMaxPermit) {
						  	        idfNewDoc.grant(s_Author, liDocMaxPermit,"");
						  	    	if((!idfNewDoc.getString("u_sec_level").equals("S") && !idfNewDoc.getString("u_sec_level").equals("T"))) {//
						  	            idfNewDoc.grant(s_Author+"_sub", liDocMaxPermit,"");
						  	    	}
						  		    idfNewDoc.save();
						  	    }    
					  		    s_Author = dto.getReqUser();  //이동권한자
						  	    if(idfNewDoc.getPermitEx( s_Author) != 7 && idfNewDoc.getString("u_doc_status").equals("L")) {
						  	        idfNewDoc.grant(s_Author, 7,"");
						  		    idfNewDoc.save();
						  	    }
						  	    s_Author = dto.getReqUser();  //g_부서 : grant는 하지 않음
						  	    //if(idfNewDoc.getPermitEx( s_Author) != liDocMaxPermit) {
						  	        idfNewDoc.grant(s_Author, liDocMaxPermit,"");
						  		    idfNewDoc.save();
						  	    //}
				  		    }
						}
//이동--문서						
			    	}else {
						if (secLevelMap.get(ls_BeforeSecLevel) < secLevelMap.get(dto.getTargetSecLevel())) {
							
							idfNewDoc.setString("u_sec_level", idfUpFol_Obj==null?dto.getTargetSecLevel():idfUpFol_Obj.getString("u_sec_level"));
							idfNewDoc.save();

				  		    if(!s_DocStatus.equals("C") && !idfNewDoc.getString("u_sec_level").equals("S")) lDocStatus.add("L");
							
					  		bWork= setFolderAuthShareList(userSession, idfSession, s_UFolId, idfNewDoc.getString("r_object_id") , dto, objType) ;//문서
					  		
					  		if( dto.getTargetFolType().equals("PFO") || dto.getTargetFolType().equals("RFO")) { //프로젝트나 연구과제용 폴더생성 작업인 경우
								bWork=setProjectJoinDeptToAuthbase( userSession, idfSession, idfNewDoc.getString("r_object_id") , dto, "D" ) ;
							}
				  		    //bWork= setFolderAuthBaseList(userSession, idfSession, s_UFolId, idfNewDoc.getString("r_object_id") , dto, objType) ;
					  		//if(dto.getTargetSecLevel().equals("S")) {
					  		String s_Author="";
							if(idfNewDoc.getString("u_doc_status").equals("L") ) {
									if(!idfNewDoc.getString("u_sec_level").equals("S")) {
										s_Author =  "g_"+ps_RcevCab;
										if(idfNewDoc.getString("u_sec_level").equals("G"))       s_Author =  "g_"+dto.getUGroupOrgCabinetCd();
									    else if(idfNewDoc.getString("u_sec_level").equals("C"))  s_Author =  "g_"+dto.getUComOrgCabinetCd();
													
								  	    int i_AuthorCnt = DCTMUtils.getCountByDQL( idfSession, " edms_auth_base where u_obj_id='"+rObjectId +"' and u_author_id='"+s_Author +"' and u_doc_status='L'");
								  	    int i_AuthorCntClose = DCTMUtils.getCountByDQL( idfSession, " edms_auth_base where u_obj_id='"+rObjectId +"' and u_author_id='"+s_Author +"' and u_doc_status='C' ");
								  	    
								  	    if(i_AuthorCntClose > 0) {
								  	    	String s_Dql_d = "delete edms_auth_base object " +
											        " where u_obj_id = '" + rObjectId + "'  and u_author_id= '"+s_Author +"' and u_doc_status='C' and u_add_gubun='G' "; 
								            idf_Col = DCTMUtils.getCollectionByDQL(idfSession, s_Dql_d, DfQuery.DF_QUERY);
									  		if(idf_Col != null) idf_Col.close();							  	    	
								  	    }
								  	    bWork= setFolderAuthShareList(userSession, idfSession, s_UFolId, idfNewDoc.getString("r_object_id") , dto, objType) ;//문서
								  	    bWork= setFolderAuthBaseList(userSession, idfSession, s_UFolId, idfNewDoc.getString("r_object_id") , dto, objType) ;
								  	    
								  	    if(i_AuthorCnt < 1) {
									  	    IDfPersistentObject idf_PObj = idfSession.newObject("edms_auth_base");

									        idf_PObj = idfSession.newObject("edms_auth_base");
									  		idf_PObj.setString("u_obj_id"		, rObjectId);
									  		idf_PObj.setString("u_obj_type"		, objType);
									  		idf_PObj.setString("u_doc_status"	, "L");
								  		    idf_PObj.setString("u_permit_type"	, "D");
											idf_PObj.setString("u_own_dept_yn"	, "Y");
											idf_PObj.setString("u_author_id"	, s_Author);
											idf_PObj.setString("u_author_type"	,"D"); //부서
									  		idf_PObj.setString("u_create_user"	, dto.getReqUser());
									  		idf_PObj.setTime  ("u_create_date"	, new DfTime());
									  		idf_PObj.setString("u_add_gubun"	, "G");
									  		idf_PObj.save();  
								  	    }
								  	    if( s_DocStatus.equals("L")) {
								  	    //if(idfNewDoc.getPermitEx( s_Author) != liDocMaxPermit) {
								  	        idfNewDoc.grant(s_Author, liDocMaxPermit,"");
								  	        if((!idfNewDoc.getString("u_sec_level").equals("S") && !idfNewDoc.getString("u_sec_level").equals("T"))) {//
								  	            idfNewDoc.grant(s_Author+"_sub", liDocMaxPermit,"");
								  	        }
								  		    idfNewDoc.save();
								  	    //}
								  	    }
									}else {
										s_Author =  "g_"+ps_RcevCab;
								  	    int i_AuthorCnt = DCTMUtils.getCountByDQL( idfSession, " edms_auth_base where u_obj_id='"+rObjectId +"' and u_author_id='"+s_Author +"' and u_doc_status='L'");
								  	    if(i_AuthorCnt < 1) {
									  	    IDfPersistentObject idf_PObj = idfSession.newObject("edms_auth_base");

									        idf_PObj = idfSession.newObject("edms_auth_base");
									  		idf_PObj.setString("u_obj_id"		, rObjectId);
									  		idf_PObj.setString("u_obj_type"		, objType);
									  		idf_PObj.setString("u_doc_status"	, "L");
								  		    idf_PObj.setString("u_permit_type"	, "D");
											idf_PObj.setString("u_own_dept_yn"	, "Y");
											idf_PObj.setString("u_author_id"	, s_Author);
											idf_PObj.setString("u_author_type"	,"D"); //부서
									  		idf_PObj.setString("u_create_user"	, dto.getReqUser());
									  		idf_PObj.setTime  ("u_create_date"	, new DfTime());
									  		idf_PObj.setString("u_add_gubun"	, "G");
									  		idf_PObj.save();  
								  	    }
								  	    if( s_DocStatus.equals("L")) {
								  	    //if(idfNewDoc.getPermitEx( s_Author) != liDocMaxPermit) {
								  	        idfNewDoc.grant(s_Author, liDocMaxPermit,"");
								  		    idfNewDoc.save();
								  	    //}
								  	    }
									}
					  	    		
							  	    s_Author = dto.getReqUser();  //g_부서 : grant는 하지 않음
							  	    int i_AuthorCnt = DCTMUtils.getCountByDQL( idfSession, " edms_auth_base where u_obj_id='"+rObjectId +"' and u_author_id='"+s_Author +"' and u_doc_status='L' and u_add_gubun='G' ");
							  	    if(i_AuthorCnt > 0) {
							  	    	String s_Dql_d = "delete edms_auth_base object " +
										        " where u_obj_id = '" + rObjectId + "'  and u_author_id= '"+s_Author +"' and u_doc_status='L' and u_add_gubun='G' "; 
							            idf_Col = DCTMUtils.getCollectionByDQL(idfSession, s_Dql_d, DfQuery.DF_QUERY);
								  		if(idf_Col != null) idf_Col.close();							  	    	
							  	    }

							  	    //if(idfNewDoc.getPermitEx( s_Author) != liDocMaxPermit) {
							  	        idfNewDoc.grant(s_Author, liDocMaxPermit,"");
							  		    idfNewDoc.save();
							  	    //}				  	    
							  	  
							  	     i_AuthorCnt = DCTMUtils.getCountByDQL( idfSession, " edms_auth_base where u_obj_id='"+rObjectId +"' and u_author_id='"+s_Author +"' and u_doc_status='L' ");
							  	    if(i_AuthorCnt < 1) {
								  	    IDfPersistentObject idf_PObj = idfSession.newObject("edms_auth_base");
								  		idf_PObj.setString("u_obj_id"		, rObjectId);
								  		idf_PObj.setString("u_obj_type"		, objType);
								  		idf_PObj.setString("u_doc_status"	, "L");
							  		    idf_PObj.setString("u_permit_type"	, "D");
										idf_PObj.setString("u_own_dept_yn"	, " ");
										idf_PObj.setString("u_author_id"	, s_Author);
										idf_PObj.setString("u_author_type"	,"U");
								  		idf_PObj.setString("u_create_user"	, dto.getReqUser());
								  		idf_PObj.setTime  ("u_create_date"	, new DfTime());
								  		idf_PObj.setString("u_add_gubun"	, "P");
								  		idf_PObj.save();  
							  	    }
							  	    i_AuthorCnt = DCTMUtils.getCountByDQL( idfSession, " edms_auth_base where u_obj_id='"+rObjectId +"' and u_author_id='"+s_Author +"' and u_doc_status='C' and u_add_gubun='P' ");
							  	    if(i_AuthorCnt < 1) {
							  	    	IDfPersistentObject idf_PObj = idfSession.newObject("edms_auth_base");
								  		idf_PObj.setString("u_obj_id"		, rObjectId);
								  		idf_PObj.setString("u_obj_type"		, objType);
								  		idf_PObj.setString("u_doc_status"	, "C");
							  		    idf_PObj.setString("u_permit_type"	, "R");
										idf_PObj.setString("u_own_dept_yn"	, " ");
										idf_PObj.setString("u_author_id"	, s_Author);
										idf_PObj.setString("u_author_type"	,"U");
								  		idf_PObj.setString("u_create_user"	, dto.getReqUser());
								  		idf_PObj.setTime  ("u_create_date"	, new DfTime());
								  		idf_PObj.setString("u_add_gubun"	, "P");
								  		idf_PObj.save();  
							  	    }
//이동-문서							  		
					  	   	}else {
									bWork= setFolderAuthBaseList(userSession, idfSession, s_UFolId, rObjectId , dto, objType) ;
                                    if(!idfNewDoc.getString("u_sec_level").equals("S")) {
                                    	s_Author =  "g_"+ps_RcevCab;
										if(idfNewDoc.getString("u_sec_level").equals("G"))       s_Author =  "g_"+dto.getUGroupOrgCabinetCd();
									    else if(idfNewDoc.getString("u_sec_level").equals("C"))  s_Author =  "g_"+dto.getUComOrgCabinetCd();
													
								  	    int i_AuthorCnt = DCTMUtils.getCountByDQL( idfSession, " edms_auth_base where u_obj_id='"+rObjectId +"' and u_author_id='"+s_Author +"' and u_doc_status='L'");
								  	    int i_AuthorCntClose = DCTMUtils.getCountByDQL( idfSession, " edms_auth_base where u_obj_id='"+rObjectId +"' and u_author_id='"+s_Author +"' and u_doc_status='C' ");
								  	    
								  	    if(i_AuthorCntClose > 0) {
								  	    	String s_Dql_d = "delete edms_auth_base object " +
											        " where u_obj_id = '" + rObjectId + "'  and u_author_id= '"+s_Author +"' and u_doc_status='C' and u_add_gubun='G' "; 
								            idf_Col = DCTMUtils.getCollectionByDQL(idfSession, s_Dql_d, DfQuery.DF_QUERY);
									  		if(idf_Col != null) idf_Col.close();							  	    	
								  	    }
								  	    bWork= setFolderAuthShareList(userSession, idfSession, s_UFolId, idfNewDoc.getString("r_object_id") , dto, objType) ;//문서
								  	    bWork= setFolderAuthBaseList(userSession, idfSession, s_UFolId, idfNewDoc.getString("r_object_id") , dto, objType) ;//문서
								  	    
								  	    if( s_DocStatus.equals("L")) {
								  	    //if(idfNewDoc.getPermitEx( s_Author) != liDocMaxPermit) {
								  	        idfNewDoc.grant(s_Author, liDocMaxPermit,"");
								  	        if((!idfNewDoc.getString("u_sec_level").equals("S") && !idfNewDoc.getString("u_sec_level").equals("T"))) {//
								  	            idfNewDoc.grant(s_Author+"_sub", liDocMaxPermit,"");
								  	        }
								  		    idfNewDoc.save();
								  	    //}
								  	    }
								  	    if(i_AuthorCnt < 1) {
									  	    IDfPersistentObject idf_PObj = idfSession.newObject("edms_auth_base");

									        idf_PObj = idfSession.newObject("edms_auth_base");
									  		idf_PObj.setString("u_obj_id"		, rObjectId);
									  		idf_PObj.setString("u_obj_type"		, objType);
									  		idf_PObj.setString("u_doc_status"	, "L");
								  		    idf_PObj.setString("u_permit_type"	, "D");
											idf_PObj.setString("u_own_dept_yn"	, "Y");
											idf_PObj.setString("u_author_id"	, s_Author);
											idf_PObj.setString("u_author_type"	,"D"); //부서
									  		idf_PObj.setString("u_create_user"	, dto.getReqUser());
									  		idf_PObj.setTime  ("u_create_date"	, new DfTime());
									  		idf_PObj.setString("u_add_gubun"	, "G");
									  		idf_PObj.save();  
								  	    }
									}
							  	    s_Author = dto.getReqUser();  //g_부서 : grant는 하지 않음
							  	    //if(idfNewDoc.getPermitEx( s_Author) != liDocMaxPermit) {
							  	        idfNewDoc.grant(s_Author, liDocMaxPermit,"");
							  		    idfNewDoc.save();
							  	    //}				  	    
							  	    int i_AuthorCnt = DCTMUtils.getCountByDQL( idfSession, " edms_auth_base where u_obj_id='"+rObjectId +"' and u_author_id='"+s_Author +"' and u_doc_status='L' and u_add_gubun='P' ");
							  	    if(i_AuthorCnt < 1) {
								  	    IDfPersistentObject idf_PObj = idfSession.newObject("edms_auth_base");
								  	    
								        idf_PObj = idfSession.newObject("edms_auth_base");
								  		idf_PObj.setString("u_obj_id"		, rObjectId);
								  		idf_PObj.setString("u_obj_type"		, objType);
								  		idf_PObj.setString("u_doc_status"	, "L");
							  		    idf_PObj.setString("u_permit_type"	, "D");
										idf_PObj.setString("u_own_dept_yn"	, " ");
										idf_PObj.setString("u_author_id"	, s_Author);
										idf_PObj.setString("u_author_type"	,"U"); //부서
								  		idf_PObj.setString("u_create_user"	, dto.getReqUser());
								  		idf_PObj.setTime  ("u_create_date"	, new DfTime());
								  		idf_PObj.setString("u_add_gubun"	, "P");
								  		idf_PObj.save();  
					  	    	    }
					      	}
					  		//}
//이동--문서
						}else {//문서의 보안등급이 이동하려는 폴더같은 타겟쪽 보안등급보다 더 높거나 같은 경우 
					  		bWork= setFolderAuthShareList(userSession, idfSession, s_UFolId, idfNewDoc.getString("r_object_id") , dto, objType) ;//문서
					  		//문서의 기존 권한체계를 가져감
					  		bWork= setFolderAuthBaseList(userSession, idfSession, idfNewDoc.getString("r_object_id"), idfNewDoc.getString("r_object_id") , dto, objType) ;

							if(!ls_BeforeSecLevel.equals("S")) {
								for(int i=0; i< lDocStatus.size(); i++) {
							  	    String s_Author = "g_" + ps_RcevCab;
							  	    if(lDocStatus.get(i).equals("C")) {
								        if(ls_BeforeSecLevel.equals("G"))      s_Author =  "g_"+dto.getUGroupOrgCabinetCd();
								        else if(ls_BeforeSecLevel.equals("C")) s_Author =  "g_"+dto.getUComOrgCabinetCd();
								    }
							  	    //if(i==0 && idfNewDoc.getPermitEx( s_Author) != liDocMaxPermit) {
							  	    if( s_DocStatus.equals("L")) {
							  	        idfNewDoc.grant(s_Author, liDocMaxPermit,"");
							  	        if((!idfNewDoc.getString("u_sec_level").equals("S") && !idfNewDoc.getString("u_sec_level").equals("T"))) {//
							  	            idfNewDoc.grant(s_Author+"_sub", liDocMaxPermit,"");
							  	        }
							  		    idfNewDoc.save();
							  	    //}
							  	    }
							  	    int i_AuthorCnt = DCTMUtils.getCountByDQL( idfSession, " edms_auth_base where u_obj_id='"+rObjectId +"' and u_author_id='"+s_Author +"' and u_doc_status='"+lDocStatus.get(i)+"'");
							  	    if(i_AuthorCnt < 1) {
								  	    IDfPersistentObject idf_PObj = idfSession.newObject("edms_auth_base");
								  		idf_PObj.setString("u_obj_id"		, rObjectId);
								  		idf_PObj.setString("u_obj_type"		, objType);
								  		idf_PObj.setString("u_doc_status"	, lDocStatus.get(i));
							  			if(lDocStatus.get(i).equals("C"))
							  				idf_PObj.setString("u_permit_type"	, "R");
							  			else
								  		    idf_PObj.setString("u_permit_type"	, "D");
										idf_PObj.setString("u_own_dept_yn"	, "Y");
										idf_PObj.setString("u_author_id"	, s_Author);
										idf_PObj.setString("u_author_type"	,"D"); //부서
								  		idf_PObj.setString("u_create_user"	, dto.getReqUser());
								  		idf_PObj.setTime  ("u_create_date"	, new DfTime());
								  		idf_PObj.setString("u_add_gubun"	, "G");
								  		idf_PObj.save();
							  	    }
					  		    }
							}else {  //제한등급 문서의 경우 
								for(int i=0; i< lDocStatus.size(); i++) {
							  	    String s_Author = dto.getReqUser();  //g_부서 : grant는 하지 않음
							  	    //if(i==0 && idfNewDoc.getPermitEx( s_Author) != liDocMaxPermit) {
							  	    if(i==0) {
							  	        idfNewDoc.grant(s_Author, liDocMaxPermit,"");
							  		    idfNewDoc.save();
								    }
							  	    //}
							  	    
							  	    int i_AuthorCnt = DCTMUtils.getCountByDQL( idfSession, " edms_auth_base where u_obj_id='"+rObjectId +"' and u_author_id='"+s_Author +"' and u_doc_status='"+lDocStatus.get(i)+"'");
							  	    if(i_AuthorCnt < 1) {
								  	    IDfPersistentObject idf_PObj = idfSession.newObject("edms_auth_base");
								  	    
								        idf_PObj = idfSession.newObject("edms_auth_base");
								  		idf_PObj.setString("u_obj_id"		, rObjectId);
								  		idf_PObj.setString("u_obj_type"		, objType);
								  		idf_PObj.setString("u_doc_status"	, lDocStatus.get(i));
							  			if(lDocStatus.get(i).equals("C"))
							  				idf_PObj.setString("u_permit_type"	, "R");
							  			else
								  		    idf_PObj.setString("u_permit_type"	, "D");
										idf_PObj.setString("u_own_dept_yn"	, " ");
										idf_PObj.setString("u_author_id"	, s_Author);
										idf_PObj.setString("u_author_type"	,"D"); //부서
								  		idf_PObj.setString("u_create_user"	, dto.getReqUser());
								  		idf_PObj.setTime  ("u_create_date"	, new DfTime());
								  		if((idfNewDoc.getString("u_sec_level").equals("S"))) {
								  		    idf_PObj.setString("u_add_gubun"	, "G");
								  		}else {
								  			idf_PObj.setString("u_add_gubun"	, "P");
								  		}
								  		idf_PObj.save();  
							  	    }
							  	    
					  		    }
								String s_Author="";
								if(idfNewDoc.getString("u_doc_status").equals("L") ) {
							  	    s_Author = "g_" + ps_RcevCab;
							  	    
							  	    liDocMaxPermit=3;//제한문서는 부서에 Read권한만 

							  	    //if(idfNewDoc.getPermitEx( s_Author) != liDocMaxPermit) {
							  	    if(s_DocStatus.equals("L")) {
							  	        idfNewDoc.grant(s_Author, liDocMaxPermit,"");
							  	        if((!idfNewDoc.getString("u_sec_level").equals("S") && !idfNewDoc.getString("u_sec_level").equals("T"))) {//
							  	            idfNewDoc.grant(s_Author+"_sub", liDocMaxPermit,"");
							  	        }
							  		    idfNewDoc.save();
							  	    //}
							  	    }
							  	    int i_AuthorCnt = DCTMUtils.getCountByDQL( idfSession, " edms_auth_base where u_obj_id='"+rObjectId +"' and u_author_id='"+s_Author +"' and u_doc_status='L'");
							  	    if(i_AuthorCnt < 1) {
								  	    IDfPersistentObject idf_PObj = idfSession.newObject("edms_auth_base");
								  	    
								        idf_PObj = idfSession.newObject("edms_auth_base");
								  		idf_PObj.setString("u_obj_id"		, rObjectId);
								  		idf_PObj.setString("u_obj_type"		, objType);
								  		idf_PObj.setString("u_doc_status"	, "L");
							  		    idf_PObj.setString("u_permit_type"	, "D");
										idf_PObj.setString("u_own_dept_yn"	, "Y");
										idf_PObj.setString("u_author_id"	, s_Author);
										idf_PObj.setString("u_author_type"	,"D"); //부서
								  		idf_PObj.setString("u_create_user"	, dto.getReqUser());
								  		idf_PObj.setTime  ("u_create_date"	, new DfTime());
								  		idf_PObj.setString("u_add_gubun"	, "G");
								  		idf_PObj.save();  
							  	    }
							  	    liDocMaxPermit=7;//이동을 할 수 있는 작업자는 7 권한   
							  	    s_Author = dto.getReqUser();  //g_부서 : grant는 하지 않음
							  	    //if(idfNewDoc.getPermitEx( s_Author) != liDocMaxPermit) {
							  	        idfNewDoc.grant(s_Author, liDocMaxPermit,"");
							  		    idfNewDoc.save();
							  	    //}
/*							  		    
							  	    i_AuthorCnt = DCTMUtils.getCountByDQL( idfSession, " edms_auth_base where u_obj_id='"+rObjectId +"' and u_author_id='"+s_Author +"' and u_doc_status='L' ");
							  	    if(i_AuthorCnt > 0) {
							  	    	String s_Dql_d = "delete edms_auth_base object " +
										        " where u_obj_id = '" + rObjectId + "'  and u_author_id= '"+s_Author +"' and u_doc_status='L'  and u_add_gubun not in('S','J','W','P') "; 
							            idf_Col = DCTMUtils.getCollectionByDQL(idfSession, s_Dql_d, DfQuery.DF_QUERY);
								  		if(idf_Col != null) idf_Col.close();							  	    	
							  	    }

							  	    IDfPersistentObject idf_PObj = idfSession.newObject("edms_auth_base");
							        idf_PObj = idfSession.newObject("edms_auth_base");
							  		idf_PObj.setString("u_obj_id"		, rObjectId);
							  		idf_PObj.setString("u_obj_type"		, objType);
							  		idf_PObj.setString("u_doc_status"	, "L");
						  		    idf_PObj.setString("u_permit_type"	, "D");
									idf_PObj.setString("u_own_dept_yn"	, "Y");
									idf_PObj.setString("u_author_id"	, s_Author);
									idf_PObj.setString("u_author_type"	,"U"); //부서
							  		idf_PObj.setString("u_create_user"	, dto.getReqUser());
							  		idf_PObj.setTime  ("u_create_date"	, new DfTime());
							  		if((idfNewDoc.getString("u_sec_level").equals("S"))) {
							  		    idf_PObj.setString("u_add_gubun"	, "G");
							  		}else {
							  			idf_PObj.setString("u_add_gubun"	, "P");
							  		}
							  		idf_PObj.save();  
*/							  		
					  	    	}else {
					  	    		
							  	    s_Author = "g_" + ps_RcevCab;
							  	    int i_AuthorCnt = DCTMUtils.getCountByDQL( idfSession, " edms_auth_base where u_obj_id='"+rObjectId +"' and u_author_id='"+s_Author +"' and u_doc_status='L'");
							  	    if(i_AuthorCnt < 1) {
								  	    IDfPersistentObject idf_PObj = idfSession.newObject("edms_auth_base");
								  	    
								        idf_PObj = idfSession.newObject("edms_auth_base");
								  		idf_PObj.setString("u_obj_id"		, rObjectId);
								  		idf_PObj.setString("u_obj_type"		, objType);
								  		idf_PObj.setString("u_doc_status"	, "L");
							  		    idf_PObj.setString("u_permit_type"	, "D");
										idf_PObj.setString("u_own_dept_yn"	, "Y");
										idf_PObj.setString("u_author_id"	, s_Author);
										idf_PObj.setString("u_author_type"	,"D"); //부서
								  		idf_PObj.setString("u_create_user"	, dto.getReqUser());
								  		idf_PObj.setTime  ("u_create_date"	, new DfTime());
								  		idf_PObj.setString("u_add_gubun"	, "G");
								  		idf_PObj.save();  
							  	    }

							  	    
							  	    s_Author = dto.getReqUser();  //g_부서 : grant는 하지 않음
							  	    //if(idfNewDoc.getPermitEx( s_Author) != liDocMaxPermit) {
							  	        idfNewDoc.grant(s_Author, liDocMaxPermit,"");
							  		    idfNewDoc.save();
							  	    //}				  	    
							  	    
							  	    i_AuthorCnt = DCTMUtils.getCountByDQL( idfSession, " edms_auth_base where u_obj_id='"+rObjectId +"' and u_author_id='"+s_Author +"' and u_doc_status='L' ");
							  	    if(i_AuthorCnt < 1) {
								  	    IDfPersistentObject idf_PObj = idfSession.newObject("edms_auth_base");
								  	    
								        idf_PObj = idfSession.newObject("edms_auth_base");
								  		idf_PObj.setString("u_obj_id"		, rObjectId);
								  		idf_PObj.setString("u_obj_type"		, objType);
								  		idf_PObj.setString("u_doc_status"	, "L");
							  		    idf_PObj.setString("u_permit_type"	, "D");
										idf_PObj.setString("u_own_dept_yn"	, "Y");
										idf_PObj.setString("u_author_id"	, s_Author);
										idf_PObj.setString("u_author_type"	,"U"); //사용자
								  		idf_PObj.setString("u_create_user"	, dto.getReqUser());
								  		idf_PObj.setTime  ("u_create_date"	, new DfTime());
								  		idf_PObj.setString("u_add_gubun"	, "P");
								  		idf_PObj.save();  
					  	    	    }
					  	    	}								
							}
							
						}
						if(null !=idfUpFol_Obj) {
							if(!idfUpFol_Obj.getString("u_sec_level").equals("S") && !ls_BeforeSecLevel.equals("S")) { //제한등급이 아닐때 팀을 u_add_gubun='G' 로 추가
								
								if(s_DocStatus.equals("C")) {
								    if(lDocStatus.size() < 2 ) lDocStatus.add("L");
								}
								
								for(int i=0; i< lDocStatus.size(); i++) {
							  	    String s_Author = "g_" + ps_RcevCab;  //g_부서 : grant는 하지 않음
							  	    
									if (secLevelMap.get(idfUpFol_Obj.getString("u_sec_level")) < secLevelMap.get(dto.getTargetSecLevel())) {
									    if(idfUpFol_Obj.getString("u_sec_level").equals("G"))      s_Author =  "g_"+dto.getUGroupOrgCabinetCd();
									    else if(idfUpFol_Obj.getString("u_sec_level").equals("C")) s_Author =  "g_"+dto.getUComOrgCabinetCd();
									    else if(idfUpFol_Obj.getString("u_sec_level").equals("T")) s_Author =  "g_"+dto.getTgCabinetcode();
									}
									if( lDocStatus.get(i).equals(s_DocStatus)) {
								  	    //if(i==0 && idfNewDoc.getPermitEx( s_Author) != liDocMaxPermit) {
										if( s_DocStatus.equals("L")) {
								  	        idfNewDoc.grant(s_Author, liDocMaxPermit,"");
								  	        if((!idfNewDoc.getString("u_sec_level").equals("S") && !idfNewDoc.getString("u_sec_level").equals("T"))) {//
								  	            idfNewDoc.grant(s_Author+"_sub", liDocMaxPermit,"");
								  	        }
								  		    idfNewDoc.save();
								  	    //}
										}
									}
							  	    int i_AuthorCnt = DCTMUtils.getCountByDQL( idfSession, " edms_auth_base where u_obj_id='"+rObjectId +"' and u_author_id='"+s_Author +"' and u_doc_status='"+lDocStatus.get(i)+"'");
							  	    if(i_AuthorCnt < 1) {
			
								  	    IDfPersistentObject idf_PObj = idfSession.newObject("edms_auth_base");
								        idf_PObj = idfSession.newObject("edms_auth_base");
								  		idf_PObj.setString("u_obj_id"		, rObjectId);
								  		idf_PObj.setString("u_obj_type"		, objType);
								  		idf_PObj.setString("u_doc_status"	, lDocStatus.get(i));
							  			if(lDocStatus.get(i).equals("C"))
							  				idf_PObj.setString("u_permit_type"	, "R");
							  			else
								  		    idf_PObj.setString("u_permit_type"	, "D");
										idf_PObj.setString("u_own_dept_yn"	, "Y");
										idf_PObj.setString("u_author_id"	, s_Author);
										idf_PObj.setString("u_author_type"	,"D"); //부서
								  		idf_PObj.setString("u_create_user"	, dto.getReqUser());
								  		idf_PObj.setTime  ("u_create_date"	, new DfTime());
								  		idf_PObj.setString("u_add_gubun"	, "G");
								  		idf_PObj.save();  		
							  	    }
					  		    }
							}else {
								if(s_DocStatus.equals("C")) {
								    if(lDocStatus.size() < 2 ) lDocStatus.add("L");
								}else if(s_DocStatus.equals("L")) {
									if(lDocStatus.size() < 2 ) lDocStatus.add("L");
								}
						  	    String s_Author = "g_" + ps_RcevCab;  //g_부서 : grant는 하지 않음
								if (secLevelMap.get(idfUpFol_Obj.getString("u_sec_level")) < secLevelMap.get(dto.getTargetSecLevel())) {
								    if(idfUpFol_Obj.getString("u_sec_level").equals("G"))      s_Author =  "g_"+dto.getUGroupOrgCabinetCd();
								    else if(idfUpFol_Obj.getString("u_sec_level").equals("C")) s_Author =  "g_"+dto.getUComOrgCabinetCd();
								    else if(idfUpFol_Obj.getString("u_sec_level").equals("T")) s_Author =  "g_"+dto.getTgCabinetcode();
								}
								for(int i=0; i< lDocStatus.size(); i++) {
									if( lDocStatus.get(i).equals(s_DocStatus)) {
										if( s_DocStatus.equals("L")) {
								  	        idfNewDoc.grant(s_Author, liDocMaxPermit,"");
								  	        if((!idfNewDoc.getString("u_sec_level").equals("S") && !idfNewDoc.getString("u_sec_level").equals("T"))) {//
								  	            idfNewDoc.grant(s_Author+"_sub", liDocMaxPermit,"");
								  	        }
								  		    idfNewDoc.save();
										}
									}
								}
								
							}
						}
					}
			    }
			}	
		    

    	} catch (Exception e) {
			e.printStackTrace();
	    }finally {
	    	if(idf_Col !=null) idf_Col.close();
	    }
    }
    
    /*
  	//중복된 폴더명 확인 후 최종 폴더명 반환(root용)
  	 * 
  	 */
      @Override
  	public String getChekedPjtName(IDfSession  idf_Sess, String ps_CabinetCode, String ps_FinishYn, String s_PjtName, DPath dto) throws Exception{
      	
  	 	Map<String, String> exFolMap = new HashMap<String,String>(); //타겟폴더 root에 존재하는 폴더들
  	 	Map<String, String> exPjtMap = new HashMap<String,String>(); //r_object_id, u_pjt_code맵
  	 	//폴더 중복체크
  	 	if(null !=s_PjtName)
	 		if(s_PjtName.contains("'"))
	 			s_PjtName = s_PjtName.replace("'","''");
  	 		
  	 	String s_Dql = "select r_object_id, u_pjt_code, u_pjt_name from edms_project where u_cabinet_code = '" + ps_CabinetCode + "' " +
  			          "and u_finish_yn ='"+ ps_FinishYn+"' " +
  				      "and u_pjt_name like '" + s_PjtName + "%'";
  		IDfQuery 		idf_Qry 	= null;
  		idf_Qry = new DfQuery();
  		idf_Qry.setDQL(s_Dql);

  		if(null !=s_PjtName) {
 		    if(s_PjtName.contains("''"))
 			    s_PjtName = s_PjtName.replace("''","'");
        }
  		
  		IDfCollection idf_Col=null;
  		try {
  			idf_Col = idf_Qry.execute(idf_Sess,DfQuery.QUERY);

	  		//edms_auth_base에서 찾아서 revoke하고
	  		while(idf_Col != null && idf_Col.next())
	  		{
	  			String exFolName =idf_Col.getString("u_pjt_name");
	  	 		if(exFolName.contains("''"))
	  	 			exFolName = exFolName.replace("''","'");
	  			
	  			String s_RObjectId =idf_Col.getString("r_object_id");
	  			String s_UPjtCode  =idf_Col.getString("u_pjt_code");
	  			
	  			exFolMap.put(exFolName, s_RObjectId);
	  			exPjtMap.put(s_RObjectId, s_UPjtCode);
	  		}
	  		String orgPjtName=s_PjtName;
	  		
	  		if(!exFolMap.isEmpty()) {
				String s_RObjectId =s_PjtName;
				int k=3;
				//for(int i=0; i < k ; i++) {
				for(int i=1; i < k ; i++) {
					if( null !=exFolMap.get(s_PjtName)) {
						s_RObjectId = exFolMap.get(s_PjtName);

						s_PjtName=orgPjtName+" ("+ i +")" ;
						k++;
					}else {
					    break;
					}
				}
			    //if(orgPjtName.equals( s_PjtName))s_PjtName=s_RObjectId;
	  		}
/*				
	  		if(!exFolMap.isEmpty()) {
	  			
	  			if( exFolMap.get(s_PjtName) !=null) {
	
					String s_RObjectId = exFolMap.get(s_PjtName);
	
					boolean gwonhan = authService.isRootAuthenticated(HamType.findByValue( "P"), exPjtMap.get(s_RObjectId), dto.getReqUser()); //해당 프로젝트에 대한 쓰기권한확인
					if(gwonhan) {
						return s_RObjectId;	//권한이 있으면 폴더를 새로 만들지 않고 기존폴더에 복사한다			
					}else {
						s_PjtName = orgPjtName+"(1)";
					}
	  			    
	  			    String tmpS_FolName = s_PjtName;
	  	  			for(int k=1;  k < 10000 ; k++) {
	  	  				if( exFolMap.get(tmpS_FolName)==null ) {
	  	  				    s_PjtName=tmpS_FolName;
	  	  					break;
	  	  				}else {
	  	  					tmpS_FolName = orgPjtName + "("+ k+")";
	  	  				}
	  			    }
	  			}
	  		}
*/	  		
  		} catch (Exception e) {
		    e.printStackTrace();
		}finally {
            if(idf_Col !=null ) idf_Col.close();			
		}
      	
      	return s_PjtName;
      }    
      /*
    	//중복된 연구과제명 확인 후 최종 폴더명 반환(root용)
    	 * 
    	 */
        @Override
    	public String getChekedRscsName(IDfSession  idf_Sess, String ps_CabinetCode, String ps_FinishYn, String s_PjtName, DPath dto) throws Exception{
        	
    	 	Map<String, String> exFolMap = new HashMap<String,String>(); //타겟폴더 root에 존재하는 폴더들
    	 	Map<String, String> exRscMap = new HashMap<String,String>(); //r_object_id, u_rsch_code 맵
    	 	//폴더 중복체크
    	 	if(null !=s_PjtName)
		 		if(s_PjtName.contains("'"))
		 			s_PjtName = s_PjtName.replace("'","''");
    	 	
    	 	String s_Dql = "select r_object_id, u_rsch_code, u_rsch_name from edms_research where u_cabinet_code = '" + ps_CabinetCode + "' " +
    			          "and u_finish_yn ='"+ ps_FinishYn+"' " +
    				      "and u_rsch_name like '" + s_PjtName + "%'";
    		IDfQuery 		idf_Qry 	= null;
    		idf_Qry = new DfQuery();
    		idf_Qry.setDQL(s_Dql);
    		
    		if(null !=s_PjtName)
	 		    if(s_PjtName.contains("''"))
	 			    s_PjtName = s_PjtName.replace("''","'");
    		
    		IDfCollection idf_Col=null;
    		try {
    			idf_Col = idf_Qry.execute(idf_Sess,DfQuery.QUERY);
	    		//edms_auth_base에서 찾아서 revoke하고
	    		while(idf_Col != null && idf_Col.next())
	    		{
	    			String s_RObjectId = idf_Col.getString("r_object_id");//("u_rsch_code");
	      			String s_RschCode  =idf_Col.getString("u_rsch_code");
	      			String s_UPjtCode  =idf_Col.getString("u_pjt_code");
	      			
	    			String exFolName =idf_Col.getString("u_pjt_name");
	    	 		if(exFolName.contains("''"))
	    	 			exFolName = exFolName.replace("''","'");
	    			
	    			exFolMap.put(exFolName, s_RObjectId);
	      			exRscMap.put(s_RObjectId, s_RschCode);
	    		}
	    		String orgPjtName=s_PjtName;
	    		if(!exFolMap.isEmpty()) {
					String s_RObjectId =s_PjtName;
					int k=3;
					//for(int i=0; i < k ; i++) {
					for(int i=1; i < k ; i++) {
						if( null !=exFolMap.get(s_PjtName)) {
							s_RObjectId = exFolMap.get(s_PjtName);

							s_PjtName=orgPjtName+" ("+ i +")" ;
							k++;
						}else {
						    break;
						}
					}
				    //if(orgPjtName.equals( s_PjtName))s_PjtName=s_RObjectId;
		  		}
/*	    		
	    		if(!exFolMap.isEmpty()) {
	
	    			if( exFolMap.get(s_PjtName) !=null) {
					    String s_RObjectId = exFolMap.get(s_PjtName);
					    boolean gwonhan = authService.isRootAuthenticated(HamType.findByValue( "R"), exRscMap.get(s_RObjectId), dto.getReqUser()); //해당 연구과제에 대한 쓰기권한확인
					    if(gwonhan) {
						    return s_RObjectId;	//권한이 있으면 폴더를 새로 만들지 않고 기존폴더에 복사한다			
					    }else {
						    s_PjtName = orgPjtName+"(1)";
					    }
	    			    String tmpS_FolName = s_PjtName;
	    	  			for(int k=1;  k < 10000 ; k++) {
	    	  				if( exFolMap.get(tmpS_FolName)==null ) {
	    	  				    s_PjtName=tmpS_FolName;
	    	  					break;
	    	  				}else {
	    	  					tmpS_FolName = orgPjtName + "("+ k+")";
	    	  				}
	    			    }
	    			}
	    		}
*/	    		
    		} catch (Exception e) {
				e.printStackTrace();
			}finally {
				if(idf_Col !=null ) idf_Col.close();
			}
        	
        	return s_PjtName;
        }   
    /*
	//중복된 폴더명 확인 후 최종 폴더명 반환(root용)
	 * 
	 */
    @Override
	public String getChekedFolderName(IDfSession  idf_Sess, String ps_CabinetCode, String ps_upFolId, String s_FolName, DPath dto, String s_FolId) throws Exception{
    	
    	//String targetGubun=dto.getTargetGubun();
    	//if(targetGubun.substring(0,1).equals("P") || targetGubun.substring(0,1).equals("R")) {
    	//	
    	//}

	 	Map<String, String> exFolMap = new HashMap<String,String>(); //타겟폴더 root에 존재하는 폴더들
	 	//폴더 중복체크
	 	if(null==ps_upFolId || ps_upFolId.equals("")) ps_upFolId=" ";
	 	if(null !=s_FolName)
	 		if(s_FolName.contains("'"))
	 	        s_FolName = s_FolName.replace("'","''");
	 	
        String ls_PrCode = dto.getPrCode();
        if(null !=ls_PrCode && ls_PrCode.equals("")) ls_PrCode=" ";
	 	
		String s_Dql = "select r_object_id, u_fol_name from edms_folder where u_cabinet_code = '" + ps_CabinetCode + "' " +
			          "and u_up_fol_id ='"+ ps_upFolId+"'  and u_delete_status=' ' " +
			          "and u_pr_code ='"+ ls_PrCode+"'" +
				      "and u_fol_name like '" + s_FolName + "%'";

 		if(null !=s_FolName) {
 			if(s_FolName.contains("''"))
 	            s_FolName = s_FolName.replace("''","'");
 		}
	 	
		IDfQuery 		idf_Qry 	= null;
		idf_Qry = new DfQuery();
		idf_Qry.setDQL(s_Dql);
			
		IDfCollection idf_Col=null;
		try {
		    idf_Col = idf_Qry.execute(idf_Sess,DfQuery.QUERY);
			//edms_auth_base에서 찾아서 revoke하고
			while(idf_Col != null && idf_Col.next())
			{
				String exFolId   =idf_Col.getString("r_object_id");
				String exFolName =idf_Col.getString("u_fol_name");
		 		if(exFolName.contains("''"))
		 			exFolName = exFolName.replace("''","'");
				
				exFolMap.put(exFolName, exFolId);
			}
			String orgDocName=s_FolName;
			String ls_FindFolId="";
			if(!exFolMap.isEmpty()) {
				
				if( exFolMap.get(s_FolName) !=null) {
					//권한체크해서
/*					
					if(dto.getUptPthGbn().equals("C")) {
					    String s_RObjectId = exFolMap.get(s_FolName);
					    boolean gwonhan = authService.checkFolderAuth(s_RObjectId, dto.getReqUser(), "D");//GrantedLevels.WRITE.getLabel());
					    if(gwonhan) {
						    return s_RObjectId;	//권한이 있으면 폴더를 새로 만들지 않고 기존폴더에 복사한다			
					    }else {
					        s_FolName = orgDocName+"(1)";
					    }
					}else {
*/					if(!s_FolId.equals("")) {
					    if( dto.getUptPthGbn().equals("M") 
					    		&& exFolMap.get(s_FolName).equals(s_FolId)) {
					    	s_FolName = orgDocName;

					    	return s_FolName;
					    }
						s_FolName = orgDocName+" (1)";
				    }else {
				    	s_FolName = orgDocName+" (1)";
				    }
//					}
				    String tmpS_FolName = s_FolName;
				    int l=3;
		  			for(int k=1;  k < l ; k++) {
		  				if( exFolMap.get(tmpS_FolName)==null ) {
		  					s_FolName=tmpS_FolName;
		  					break;
		  				}else {
		  					tmpS_FolName = orgDocName + " ("+ k+")";
		  				}
		  				l++;
				    }
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			if(idf_Col !=null) idf_Col.close();
		}

    	
    	return s_FolName;
    }
    
    /**
  	//중복된 파일명 확인 후 최종 파일명 반환(root용)
  	 * ex) 같은 폴더에  테스트.ppt가 있는데 테스트.ppt를 하나 더 복사하면 테스트(1).ppt 라는 이름 부여
  	 */
      @Override
  	public String getChekedDocName(IDfSession  idf_Sess, String ps_CabinetCode, String s_UFolId, String ps_DocName, DPath dto) throws Exception{

      	String s_TypeNm="edms_doc";
      	
      	if(s_UFolId.equals("") || s_UFolId.equals(" ")) { //프로젝트 root로 바로 가는 경우 상위폴더id가 없음
      		dto.setUfolStatus("L"); //상위폴더값이 ""나 " " 인 경우
      		return ps_DocName;
      	}else {

  	    	IDfPersistentObject cpTObj = idf_Sess.getObject(new DfId(s_UFolId));
  	    	dto.setUfolStatus("L"); //기본값으로 지정(복사고려)
  	    	
  	    	if(null !=cpTObj) {
  	    		if(cpTObj.getType().getName().equals("edms_folder")) {
  	    			if(cpTObj.getString("u_fol_type").equals("DIF")) s_TypeNm="edms_doc_imp";
  	    		}
  	    		dto.setUfolStatus( cpTObj.getString("u_fol_status")); //상위폴더의 폴더 상태 상속시 이 값을 참조
  	    	}
  	    	dto.setTargetSecLevel(cpTObj.getString("u_sec_level"));  //상위폴더의 보안등급
      	}
      	
      	Map<String, String> exDocMap = new HashMap<String,String>(); //타겟폴더 root에 존재하는 폴더들
  	 	//Document 중복체크
      	//String extStr = DCTMUtils.getFileExtByFileName(ps_DocName);
      	//int i_Pos = ps_DocName.lastIndexOf(".");
  		//ps_DocName = (i_Pos > 0) ? ps_DocName.substring(0, i_Pos ) : "";
  		String orgDocName=ps_DocName;
      	
  		ps_DocName = ps_DocName.replace("'","''");
  	 	String s_Dql = "select u_doc_key,title, object_name from "+ s_TypeNm +" where u_cabinet_code = '" + ps_CabinetCode + "' " +
  	 			     "and u_fol_id = '" + s_UFolId + "'  " +
  				     "and object_name like '" + ps_DocName + "%'  ";
  		IDfQuery 		idf_Qry 	= null;
  		idf_Qry = new DfQuery();
  		idf_Qry.setDQL(s_Dql);
  			
  		IDfCollection idf_Col=null;
  		try {
  			idf_Col = idf_Qry.execute(idf_Sess,DfQuery.QUERY); 
  		
	  		while(idf_Col != null && idf_Col.next())
	  		{
	  			String exDocNm =idf_Col.getString("object_name");
	  			exDocNm = exDocNm.replace("''","'");

	  			String exDocKey =idf_Col.getString("u_doc_key");
	  			exDocMap.put(exDocNm, exDocKey);
	  		}
	  		if(!exDocMap.isEmpty()) {
	  			String s_RObjectId =ps_DocName;
	  			int k=3;
	  			for(int i=2; i < k ; i++) {
	  				if( null !=exDocMap.get(ps_DocName)) {
	  					s_RObjectId = exDocMap.get(ps_DocName);
	  					ps_DocName=orgDocName+" - 복사본("+ (i<1?"":i+"") +")";
	  					k++;
	  				}else {
	  				    break;
	  				}
	  			}
	  			if(orgDocName.equals( ps_DocName))ps_DocName=s_RObjectId;
	  			
	  		}
  		} catch (Exception e) {
				e.printStackTrace();
		}finally {
  		    if(idf_Col != null) idf_Col.close();
		}
  		return ps_DocName;
  	}

      
    /**
	//중복된 파일명 확인 후 최종 파일명 반환(root용)
	 * ex) 같은 폴더에  테스트.ppt가 있는데 테스트.ppt를 하나 더 복사하면 테스트(1).ppt 라는 이름 부여
	 */
    @Override
	public String getChekedDocNameScnd(IDfSession  idf_Sess, String ps_CabinetCode, String s_UFolId, String ps_DocName, DPath dto, String ps_DocId, String s_Extr) throws Exception{

    	String s_TypeNm="edms_doc";
    	
    	if(s_UFolId.equals("") || s_UFolId.equals(" ")) { //프로젝트 root로 바로 가는 경우 상위폴더id가 없음
    		dto.setUfolStatus("L"); //상위폴더값이 ""나 " " 인 경우
    		s_UFolId=" ";
    		//return ps_DocName;
    		dto.setTargetSecLevel("T"); //부서권한 기본
    	}else {

	    	IDfPersistentObject cpTObj = idf_Sess.getObject(new DfId(s_UFolId));
	    	dto.setUfolStatus("L"); //기본값으로 지정(복사고려)
	    	
	    	if(null !=cpTObj) {
	    		if(cpTObj.getType().getName().equals("edms_folder")) {
	    			if(cpTObj.getString("u_fol_type").equals("DIF")) s_TypeNm="edms_doc_imp";
	    		}
	    		dto.setUfolStatus( cpTObj.getString("u_fol_status")); //상위폴더의 폴더 상태 상속시 이 값을 참조
	    	}
	    	dto.setTargetSecLevel(cpTObj.getString("u_sec_level"));  //상위폴더의 보안등급
    	}
    	
    	Map<String, String> exDocMap = new HashMap<String,String>(); //타겟폴더 root에 존재하는 폴더들
	 	//Document 중복체크
    	//String extStr = DCTMUtils.getFileExtByFileName(ps_DocName);
    	//int i_Pos = ps_DocName.lastIndexOf(".");
		//ps_DocName = (i_Pos > 0) ? ps_DocName.substring(0, i_Pos ) : "";
		String orgDocName=ps_DocName;
		
		ps_DocName = ps_DocName.replace("'","''");
        String ls_PrCode = dto.getPrCode();
        if(null !=ls_PrCode && ls_PrCode.equals("")) ls_PrCode=" ";
        
	 	//if(null !=dto.getPrCode() && !dto.getPrCode().equals(" ") && !dto.getPrCode().equals("")) {
		String s_Dql = "select u_doc_key,title, object_name from "+ s_TypeNm +" where u_cabinet_code = '" + ps_CabinetCode + "' " +
	 			     "and u_fol_id = '" + s_UFolId + "'  " +
	 			     "and u_pr_code = '" + ls_PrCode + "'  " +
				     "and object_name like '" + ps_DocName + "%'  and u_delete_status=' ' and u_file_ext='"+s_Extr +"'";
	 	//}
	 	if(ps_DocName.contains("''"))
	 	    ps_DocName = ps_DocName.replace("''","'");
		IDfQuery 		idf_Qry 	= null;
		idf_Qry = new DfQuery();
		idf_Qry.setDQL(s_Dql);
			
		IDfCollection idf_Col=null;
		try {
			idf_Col = idf_Qry.execute(idf_Sess,DfQuery.QUERY);

			while(idf_Col != null && idf_Col.next())
			{
				String exDocNm =idf_Col.getString("object_name");
			 	if(exDocNm.contains("''"))
			 		exDocNm = exDocNm.replace("''","'");
				
				String exDocKey =idf_Col.getString("u_doc_key");
				exDocMap.put(exDocNm, exDocKey);
			}
			if(!exDocMap.isEmpty()) {
				String s_RObjectId =ps_DocName;
				int k=3;
				//for(int i=0; i < k ; i++) {
				for(int i=1; i < k ; i++) {
					if( null !=exDocMap.get(ps_DocName)) {
						s_RObjectId = exDocMap.get(ps_DocName);
	
						if(s_RObjectId.equals(ps_DocId)) { ps_DocName=s_RObjectId; break;}
	
						ps_DocName=orgDocName+" ("+ i +")" ;
						//ps_DocName=orgDocName+" - 복사본"+ (i<1?"":"("+(i+1)+")") ;
						//i= i==1?i++:i;
	                    //k=i+2;
						k++;
					}else {
					    break;
					}
				}
				if(orgDocName.equals( ps_DocName))ps_DocName=s_RObjectId;
				
			}
		} catch (Exception e) {
			e.printStackTrace();
	    } finally {
		    if(idf_Col != null) idf_Col.close();
	    }
		return ps_DocName;
	}

    
    /** 폴더/파일 이동처리
     * jjg
     * **/
    
    @Override
    public String moveFolderAndFiles(UserSession userSession, IDfSession idfSess, DPath dto, Map<String, Map<String,List<DPath>>> folMapTot, boolean isMobile) throws Exception {
    	//폴더 생성
		String rtnMsg=""; 
		//다른 프로젝트로 이동복사하는 경우에 대한 처리....
		//IDfSession idfSession = idfSess != null ? idfSess : this.getIdfSession(userSession);
		IDfSession idfSession = idfSess != null ? idfSess :DCTMUtils.getAdminSession();

		initMapInfo();
		
	    String sOwnSrDeptOrgId = gwDeptService.selectOrgIdByCabinetcode( dto.getSrcCabinetcode());
		String sOwnTgDeptOrgId = gwDeptService.selectOrgIdByCabinetcode( dto.getTgCabinetcode());
		
		String s_CabinetCode = dto.getTgCabinetcode();
		String sTgOrgId = gwDeptService.selectOrgIdByCabinetcode( s_CabinetCode);
		dto.setTgOrgId(sTgOrgId);
		HamInfoResult hamInfo = authService.selectDeptHamInfo(dto.getTgOrgId()).orElse(
	              authService.selectHamInfo(dto.getTgOrgId()).orElseThrow(() -> new NotFoundException(CheckAuthParam.class,  dto.getTgOrgId() )));
		dto.setOwnSrDeptOrgId(sOwnSrDeptOrgId);
		dto.setOwnTgDeptOrgId(sOwnTgDeptOrgId);
		dto.setHamInfo(hamInfo);
		
		String s_ComId   = gwDeptService.selectComCodeByCabinetCode(s_CabinetCode).toLowerCase();
		dto.setTgComId(s_ComId);
		PreservationPeriodDto psrvDto = psrvPeriodDao.selectOneByComCode(s_ComId.toUpperCase());
		psrvPMap.put("S", psrvDto.getUSecSYear());
		psrvPMap.put("C", psrvDto.getUSecCYear());
		psrvPMap.put("G", psrvDto.getUSecGYear());
		psrvPMap.put("T", psrvDto.getUSecTYear());
		
		psrvPMap.put("PJT", psrvDto.getUPjtEverFlag());
		

		try {
		 	if (!idfSession.isTransactionActive()) {
		 	    idfSession.beginTrans();
		 	}
			if(folMapTot.size() > 0) {
			
				int iCnt=0;
	        	IDfPersistentObject idfTg_PObj=null;

			    for (Entry<String, Map<String, List<DPath>>> entry : folMapTot.entrySet()) {
			        Map<String, List<DPath>> folMap = folMapTot.get(entry.getKey());

					List<String> folList = new ArrayList<String>();
			        for (Map.Entry<String, List<DPath>> folEntry : folMap.entrySet()) {
				    	folList.add(folEntry.getKey());
				    }
			        
		        	String keyStr=entry.getKey();
		        	if( !keyStr.substring(0,1).equals("P") && !keyStr.substring(0,1).equals("R")) {
			        		rtnMsg=moveFolderAndFile(userSession, idfSession, dto, folList, folMap, isMobile,  keyStr, iCnt);
		        	}else{
		        		if(keyStr.substring(0,1).equals("P")) {  //프로젝트 코드
		        			dto.setPrCode(keyStr);
		        			dto.setPrType("P");
			        		keyStr=keyStr.substring(1);
		        			rtnMsg=moveFolderAndFile(userSession, idfSession, dto, folList, folMap, isMobile, keyStr, iCnt);
		        		}else if(keyStr.substring(0,1).equals("R")) { //연구투자 코드
		        			dto.setPrCode(keyStr);
		        			dto.setPrType("R");
			        		keyStr=keyStr.substring(1);
		        			rtnMsg=moveFolderAndFile(userSession, idfSession, dto, folList, folMap, isMobile,keyStr, iCnt);
		        		}
		        	}
		        	iCnt++;
			    }
			}else {
				List<String> folList = new ArrayList<String>();
				Map<String, List<DPath>> folMap = new HashMap<String, List<DPath>>();
   			    rtnMsg=moveFolderAndFile(userSession, idfSession, dto, folList, folMap, isMobile,"6", 0);
			}
			if (idfSession.isTransactionActive()) {
  		        idfSession.commitTrans();
			}

		} catch (Exception e) {
		    e.printStackTrace();
		    rtnMsg=e.getMessage();
        } finally {
		    if (idfSession != null) {
		      if (idfSession.isTransactionActive()) {
		        idfSession.abortTrans();
		      }
		      if (idfSession.isConnected()) {
		    	  idfSession.disconnect();
		      }
		    }
		 }
		return rtnMsg;
    }
    
    
    @Override
    public String moveFolderAndFile(UserSession userSession, IDfSession idfSession, DPath dto, List<String> folList, Map<String,List<DPath>> folMap, boolean isMobile, String keyStr, int iCnt) throws Exception {

    	try {
			String newFolderName = null;
			/*
			   u_job_code  : RE(등록)
			   u_job_gubun : MOVE
			        중요문서함(DI) -> 부서함  (D)                  : 이동 대상에 대한 생성 이력 생성  
			        중요문서함(DI) -> 공유/협업   (S)               : 이동 대상에 대한 생성 이력 생성
			        중요문서함(DI) -> 프로젝트/투자/연구과제 (P, R)    : 이동 대상에 대한 생성 이력 생성
	
			   u_job_code  : DEL(삭제) => LD 또는 DR (  u_doc_status가 L 이면 LD, C 이면 DR
			   u_job_gubun : MOVE
			        부서함(D)                      -> 중요문서함 (DI)  : 이동 대상에 대한 삭제 이력 생성
			        프로젝트/투자/연구과제 (P, R)      -> 중요문서함 (DI)  : 이동 대상에 대한 삭제 이력 생성
			        조직함 C, M)                   -> 중요문서함 (DI)  : 이동 대상에 대한 삭제 이력 생성
	
			 */		
			//다른 프로젝트로 이동복사하는 경우에 대한 처리....
			//IDfSession idfSession = idfSess != null ? idfSess : this.getIdfSession(userSession);
			
	//		    folList.forEach(System.out::println); //대상 폴더 리스트 (정렬 확인)
		 	if(null !=folList && folList.size() > 0)
		        Collections.sort(folList, new uFolderAscending());
	//		    folList.forEach(System.out::println); //대상 폴더 리스트 (정렬 확인)
	
		    String topFolId = dto.getTargetDboxId(); //옮기려는 최상위 폴더 ID
		    String sUprCodeCheck=dto.getUPrCodeCheck()==null?"9":dto.getUPrCodeCheck();
		    
		    if(null==dto.getPrCode() || dto.getPrCode().equals("") || dto.getPrCode().equals(" ") || dto.getTargetFolType().equals("DFO")) {
		        dto.setPrCode(" ");
		        dto.setPrType(" ");
            }
	
		    IDfPersistentObject idfTg_PObj=null;
	    	String targetGubun = dto.getTargetGubun();
			String s_CabinetCode = dto.getTgCabinetcode();

	
	    	/////////////////////////////////////프로젝트, 연구과제쪽 '폴더' 관련 세팅
	    	String sFinishYn="N";
	    	String sPjtUFolId="";
		    if(targetGubun.equals("PCL") || targetGubun.equals("RCL")) {
		    	idfTg_PObj = idfSession.getObject(new DfId(topFolId));
		    	String objTypeNm= idfTg_PObj.getType().getName();
		    	if(objTypeNm.equals("edms_folder") && targetGubun.equals("PCL"))	    sPjtUFolId=idfTg_PObj.getString("u_fol_id");
		    	else if(objTypeNm.equals("edms_folder") && targetGubun.equals("RCL"))   sPjtUFolId=idfTg_PObj.getString("u_fol_id"); 
	
		    	sFinishYn="Y";
			    topFolId=sPjtUFolId;
	    	}else if(targetGubun.equals("PFN") || targetGubun.equals("RFN") || targetGubun.equals("PIF")) {
	    		sFinishYn="Y" ;
	    		
	    	}else if(targetGubun.equals("PJC") || targetGubun.equals("PIC") || targetGubun.equals("RSC")|| targetGubun.equals("RIC")) { //프로젝트, 연구과제
	    		if (!DfId.isObjectId(topFolId)) topFolId="";
	    		;
	    	}else if(targetGubun.substring(0,1).equals("D")  && ( keyStr.substring(0,1).equals("p")|| keyStr.substring(0,1).equals("r"))) { //올기려는건 프로젝트나 연구과제인데
	    		if( keyStr.substring(0,1).equals("p")|| keyStr.substring(0,1).equals("r")) {
	    		    if(!targetGubun.equals("DWY") && !targetGubun.equals("DIF")) topFolId=""; //타겟이 카테고리여라...아래서 폴더먼저 만들고 하위폴더-문서작업 들어가요
	    		}
	    	}else {
	    		if (!DfId.isObjectId(topFolId)) topFolId="";
	    	}
		    /////////////////////////////////////////////////////////////////
//이동처리
	    	IDfPersistentObject idfSc_PObj = null;
		    
		 	/** 복사대상 기존 폴더 id, 새로만든 폴더 id 맵 : 하위 폴더-파일 생성시에 기존폴더를 이미 만들었는지 확인하고 파일의 u_fol_id 지정을 위해서 사용 */
		 	Map<String, String> folChgMap = new HashMap<String,String>();
		 	Map<String, String> folHapMap = new HashMap<String,String>();
		 	Map<String, String> folChgPrjMap = new HashMap<String,String>(); //폴더로 프로젝트를 만들었을 때,
		 	
		 	Map<String, String> folPathMap = new HashMap<String,String>(); //폴더id, 폴더경로
	
	    	/** 소스쪽 프로젝트 코드가 있는 경우 */
		    if(sUprCodeCheck.substring(0,1).equals("P") || sUprCodeCheck.substring(0,1).equals("R")) {
		        //프로젝트나 폴더를 만들고 그 id가 topFolId가 됨
		    	if(keyStr.substring(0,1).equals("p")) {
                	Optional<Project> pjtOpt = projectDao.selectOne(keyStr);
                	if(pjtOpt.isPresent()) {
        		    	newFolderName = pjtOpt.get().getUPjtName();//소스쪽 프로젝트명
                	}else {
                		newFolderName ="새폴더";
                	}
		    	}
		    	if(keyStr.substring(0,1).equals("r")) {
		    		Optional<Research> researchOpt = researchDao.selectOne(keyStr);
                	if(researchOpt.isPresent()) {
        		    	newFolderName = researchOpt.get().getURschName();//소스쪽 프로젝트명
                	}else {
                		newFolderName ="새폴더";
                	}
		    	}
		    }else if((sUprCodeCheck.equals("9") || sUprCodeCheck ==null) && ( keyStr.substring(0,1).equals("p")|| keyStr.substring(0,1).equals("r"))) { //프로젝트나 연구투자건 통째로, 부서함으로 이동하는 경우 
		        //프로젝트나 폴더를 만들고 그 id가 topFolId가 됨
		    	if(keyStr.substring(0,1).equals("p")) {
                	Optional<Project> pjtOpt = projectDao.selectOne(keyStr);
                	if(pjtOpt.isPresent()) {
        		    	newFolderName = pjtOpt.get().getUPjtName();//소스쪽 프로젝트명
                	}else {
                		newFolderName ="새폴더";
                	}
		    	}
		    	if(keyStr.substring(0,1).equals("r")) {
		    		Optional<Research> researchOpt = researchDao.selectOne(keyStr);
                	if(researchOpt.isPresent()) {
        		    	newFolderName = researchOpt.get().getURschName();//소스쪽 프로젝트명
                	}else {
                		newFolderName ="새폴더";
                	}
		    	}
/*		    	
		    	idfSc_PObj =idfSession.getObject(new DfId(keyStr));
		    	String objTypeNm= idfSc_PObj.getType().getName();
	             
		    	newFolderName = objTypeNm.equals("edms_project")?idfSc_PObj.getString("u_pjt_name"):idfSc_PObj.getString("u_rsch_name");//소스쪽 프로젝트명, 연구과제명
*/		    	
		    	newFolderName = getChekedFolderName( idfSession, dto.getTgCabinetcode(), topFolId, newFolderName, dto, "");
		    	if(DfId.isObjectId(newFolderName)) {
			 	       topFolId = newFolderName;
			 	       folChgMap.put(keyStr, topFolId) ;//(기존폴더ID, 신규폴더ID)
		    		
		    	}else {
					//프로젝트나 연구과제명으로 폴더 생성 
		            RegistFolderDto fto = RegistFolderDto.builder().uUpFolId(topFolId).uFolName(  newFolderName)
			            .uCabinetCode(dto.getTgCabinetcode()).uFolType(dto.getTargetFolType())
			            .uSecLevel(null!=idfSc_PObj?idfSc_PObj.getString("u_sec_level"):dto.getTargetSecLevel())
			            .uFolStatus(FolderStatus.ORDINARY.getValue())
			            .uCreateUser(userSession.getUser().getUserId())
			            .uPrCode(dto.getPrCode())
			            .uPrType(dto.getPrType())
			            .uDeleteStatus("")
			            .build();
				 	       topFolId = folderService.createFolder(idfSession, fto);
				 	       folChgMap.put(keyStr, topFolId) ;//(기존폴더ID, 신규폴더ID)
				 	       
				 	   // 공유/협업 권한 해제
				 	       if(dto.getTargetGubun().substring(0,1).equals("S")) {
	/*						
						  	  	IDfPersistentObject idf_PObj = RegistAuthShareDto.toIDfPersistentObject(
							    		idfSession, 
							    		RegistAuthShareDto.builder()
							    			.uObjId(dataId)
								    		.uAuthorId(item.getTargetId())
								    		.uAuthorType(item.getType())
								    		.uPermitType(item.getPermitType())
								    		.build()
							    	);
	*/						    	
							}
				 	       //dto.setPreFolderId( "");
				 	       addMoveAuthBase( userSession,idfSession, topFolId, dto.getTgCabinetcode(), "F", dto, null); //새로만든 폴더에 권한 부여	(dto는 PreSet 전달용)	    	
		    	}
		    }
	//이동처리
		    if(sUprCodeCheck !=null && !sUprCodeCheck.equals("9")&& !sUprCodeCheck.equals("") && ( keyStr.substring(0,1).equals("p")|| keyStr.substring(0,1).equals("r"))) {//프로젝트나 연구과제로 프로젝트나 연구과제를 생성해야 하는 경우
		    	
		    	String rObjectId=keyStr;
		    	if(keyStr.substring(0,1).equals("p")) {
                	Optional<Project> pjtOpt = projectDao.selectOne(keyStr);
                	if(pjtOpt.isPresent()) {
                		rObjectId = pjtOpt.get().getRObjectId();//소스쪽 프로젝트명
                	}
		    	}
		    	if(keyStr.substring(0,1).equals("r")) {
		    		Optional<Research> researchOpt = researchDao.selectOne(keyStr);
                	if(researchOpt.isPresent()) {
                		rObjectId = researchOpt.get().getRObjectId();//소스쪽 프로젝트명
                	}
		    	}
		    	
		    	idfTg_PObj = idfSession.getObject(new DfId(rObjectId));
		    	String s_TypeName = idfTg_PObj.getType().getName();
		    	
			    if(sUprCodeCheck.substring(0,1).equals("P")) {
			    	if(s_TypeName.equals("edms_project") ){
				    	newFolderName = getChekedPjtName(idfSession, dto.getTgCabinetcode(), sFinishYn, newFolderName, dto);
				    	idfTg_PObj.setString("u_finish_yn", sFinishYn);
				    	idfTg_PObj.setString("u_fol_id", topFolId);
				    	
				    	String sFolId= idfTg_PObj.getString("u_pjt_code");
				    	if(DfId.isObjectId(newFolderName)) {
			    		    idfTg_PObj = idfSession.getObject(new DfId(newFolderName));
			    		    sFolId = idfTg_PObj.getString("u_pjt_code");
				    	}
				    	//String sFolId = idfTg_PObj.getString("u_pjt_code");
				        dto.setPrCode( sFolId );
						dto.setPrType("P");//프로젝트
	
						// 책임자 그룹 생성
					    String s_GroupCode = "g_"+ sFolId+"_pjtmgr";
				        IDfGroup idf_GroupObj = (IDfGroup)idfSession.getObjectByQualification("dm_group where group_name = '"+ s_GroupCode +"'");
			            if(idf_GroupObj == null) {
							idf_GroupObj = (IDfGroup)idfSession.newObject("dm_group");
							idf_GroupObj.setGroupName(s_GroupCode);
						}
						idf_GroupObj.removeAllUsers();
						idf_GroupObj.addUser(userSession.getUser().getUserId());
						idf_GroupObj.save();
							
						//권한작업 필요
						idfTg_PObj.save();
						
	
			    	}else { //프로젝트에서 연구과제로 오는 경우(새로 만들어줌)
				    	newFolderName = getChekedRscsName(idfSession, dto.getTgCabinetcode(), sFinishYn, newFolderName, dto);
				    	String sFolId = newFolderName;
				    	if(!DfId.isObjectId(newFolderName)) {
				            sFolId = researchService.createResearch(userSession, ResearchCreateDto.builder().uRschName(newFolderName).uListOpenYn("Y").uFinishYn(sFinishYn).uOwnDept(userSession.getUser().getOrgId()).uSecLevel(dto.getTargetSecLevel()).uCreateUser(dto.getReqUser()).uFolId(topFolId).build(), idfSession);
				    	}else {
			    		    idfTg_PObj = idfSession.getObject(new DfId(newFolderName));
			    		    sFolId = idfTg_PObj.getString("u_pjt_code");
				    	}
				        dto.setPrCode(sFolId);
						dto.setPrType("R");//연구및과제
			    	}
			    }
			    else if(sUprCodeCheck.substring(0,1).equals("R") ) {
			    	if(s_TypeName.equals("edms_research")){
				    	newFolderName = getChekedRscsName(idfSession, dto.getTgCabinetcode(), sFinishYn, newFolderName, dto);
				    	idfTg_PObj.setString("u_finish_yn", sFinishYn);
				    	idfTg_PObj.setString("u_fol_id", topFolId);
				    	
				    	String sFolId = idfTg_PObj.getString("u_rsch_code");
				    	if(DfId.isObjectId(newFolderName)) {
			    		    idfTg_PObj = idfSession.getObject(new DfId(newFolderName));
			    		    sFolId = idfTg_PObj.getString("u_rsch_code");
				    	}				    	
				        dto.setPrCode(sFolId);
						dto.setPrType("R");//연구및과제
	                    
						// 책임자 그룹 생성
					    String s_GroupCode = "g_"+ sFolId+"_rschmgr";
				        IDfGroup idf_GroupObj = (IDfGroup)idfSession.getObjectByQualification("dm_group where group_name = '"+ s_GroupCode +"'");
			            if(idf_GroupObj == null) {
							idf_GroupObj = (IDfGroup)idfSession.newObject("dm_group");
							idf_GroupObj.setGroupName(s_GroupCode);
						}
						idf_GroupObj.removeAllUsers();
						idf_GroupObj.addUser(userSession.getUser().getUserId());
						idf_GroupObj.save();
						
						//권한작업 필요
						idfTg_PObj.save();
	
			    	}else {//연구과제에서 프로젝트로 오는 경우(새로 만들어줌)
				    	newFolderName = getChekedPjtName(idfSession, dto.getTgCabinetcode(), sFinishYn, newFolderName, dto);
				    	//String sFolId = newFolderName;
				    	//if(!DfId.isObjectId(newFolderName)) {
				    	String    sFolId = projectService.createProject(userSession, ProjectCreateDto.builder().uPjtName(newFolderName).uListOpenYn("Y").uFinishYn(sFinishYn).uOwnDept(userSession.getUser().getOrgId()).uFolId(topFolId).uSecLevel(dto.getTargetSecLevel()).uCreateUser(dto.getReqUser()).build(), idfSession);
				    	//}else {
				    	//	idfTg_PObj = idfSession.getObject(new DfId(sFolId));
				    	//	sFolId = idfTg_PObj.getString("u_pjt_code");
				    	//}
				        dto.setPrCode(sFolId);
						dto.setPrType("P");//프로젝트			         
			    	}				
			    }
	        }else if( sUprCodeCheck.equals("9")){
	        	if(DfId.isObjectId(topFolId)) {
				    idfTg_PObj = idfSession.getObject(new DfId(topFolId));
				    if(idfTg_PObj !=null) {
				    	String objTypeNm= idfTg_PObj.getType().getName();    		    	
		    		    if(objTypeNm.equals("edms_doc") || objTypeNm.equals("edms_folder")) {
				            dto.setPrCode(idfTg_PObj.getString("u_pr_code"));
						    dto.setPrType(idfTg_PObj.getString("u_pr_type"));
						    dto.setTargetSecLevel(idfTg_PObj.getString("u_sec_level"));
		    		    }else if(objTypeNm.equals("edms_project")) {
				            dto.setPrCode(idfTg_PObj.getString("u_pjt_code"));
				            dto.setPrType("P");
						    dto.setTargetSecLevel(idfTg_PObj.getString("u_sec_level"));//프로젝트
		    		    }else if(objTypeNm.equals("edms_research")) {
				            dto.setPrCode(idfTg_PObj.getString("u_rsch_code"));
				            dto.setPrType("R");
						    dto.setTargetSecLevel(idfTg_PObj.getString("u_sec_level"));//연구및과제
		    		    }
				    }
	        	}
	        }
	
		 	String uFolId ="" ;//원래 상위폴더 ID
		 	String nFolId ="";
	        
		 	
			String targetBoxGubun=dto.getTargetGubun().substring(0,2);
			String targetGubunCp = uTgtMap.get(targetBoxGubun)==null? uTgtMap.get(targetBoxGubun.substring(0,1)):uTgtMap.get(targetBoxGubun);
			
			String lsJobCode="DM";
			String lsJobGubun="DM";

			boolean isLog=true;//false; //2022.01.12 모든 경우 로그 남기도록 함(이은주 차장 요청, 중요문서함 도입후 다시 고민해보기로)
			
			boolean isSameType=true;
			
			String s_FolType = dto.getTargetFolType(); //controller에서 변환
	
			List<String> sFolArray=Arrays.asList("");
			List<String> sFilArray=Arrays.asList("");
			
			if( keyStr.equals("6") || (!keyStr.substring(0,1).equals("p") && !keyStr.substring(0,1).equals("r"))) {
			    sFolArray = dto.getSourceFolders();  //edms_folder   에서 관리되는 대상 폴더들 
			    sFilArray = dto.getSourceFiles();    //edms_doc      에 존재하는 대상 파일들
			}
	
			IDfPersistentObject cpTObj = null;
			if(DfId.isObjectId(topFolId))
			    cpTObj =idfSession.getObject(new DfId(topFolId));
			
		 	if(sFolArray.size() > 0 && sFolArray.get(0) !="" ) {
			 	for(int c=0; c < sFolArray.size(); c++) {
	    		 	uFolId =sFolArray.get(c);
	    		 	if(!uFolId.equals(keyStr)) continue;

	    		 	IDfPersistentObject cpFoObj = idfSession.getObject(new DfId(uFolId));
	    		 	String s_FolName = cpFoObj.getString("u_fol_name");
	    		 	
	    			if(dto.getSrcCabinetcode().equals(dto.getTgCabinetcode()) && (!dto.getTargetGubun().equals("POW") && !dto.getTargetGubun().equals("ROW")&& !dto.getTargetGubun().equals("DPC")))
	    				if(null ==dto.getTargetSecLevel() || dto.getTargetSecLevel().equals("")|| dto.getTargetSecLevel().equals(" "))
	    			        dto.setTargetSecLevel(cpFoObj.getString("u_sec_level"));//폴더의 보안등급과 같게 프로젝트를 만들예정
	    		 	
	    		    String sourceBoxGubun=  cpFoObj.getString("u_fol_type").substring(0,2);
	    			String sourceGubun = uSrcMap.get(sourceBoxGubun)==null? uSrcMap.get(sourceBoxGubun.substring(0,1)):uSrcMap.get(sourceBoxGubun);
	    			//dto.setSourceGubun(sourceGubun);
	    			if(targetGubunCp.equals(sourceGubun)) {
	    				isLog=true; //로그기록대상
	    				lsJobGubun =sourceGubun;
	    				lsJobCode=lsJobGubun;
	    			}
	    			isSameType=true;
	    			
		    		cpFoObj.setString("u_pr_code",  dto.getPrCode());
		    		cpFoObj.setString("u_pr_type",  dto.getPrType());

		    		
	    			//소스와 타겟 어느 한쪽이 중요문서함인 경우, edms_doc이나 edms_doc_imp에 새로운 파일을 만들어야 함.
	    			if(!sourceBoxGubun.equals(targetBoxGubun) && (sourceBoxGubun.equals("DI") || sourceBoxGubun.equals("DI")))  {
	    				isSameType=false;
	    			}
	    		 	
				    if(sUprCodeCheck.substring(0,1).equals("P")) {//프로젝트로 이동할 때
				    	newFolderName = getChekedPjtName(idfSession, dto.getTgCabinetcode(), sFinishYn, s_FolName, dto);
				    	String sFolId = newFolderName;
				    	//if(!DfId.isObjectId(newFolderName)) {

				    	    sFolId = projectService.createProject(userSession, ProjectCreateDto.builder().uPjtName(newFolderName).uListOpenYn("Y").uOwnDept(userSession.getUser().getOrgId()).uFinishYn(sFinishYn).uFolId(topFolId).uSecLevel(dto.getTargetSecLevel()).uCreateUser(dto.getReqUser()).build(), idfSession);
				    	    String ls_FolId = cpFoObj.getString("r_object_id");
				    	    //auth_share내역 조회해서 D권한이면 u_join_dept_del, 
				    	    //                   R권한이면 u_join_dept_read 에 추가
				    	    
				    	    idfSession = idfSession != null ? idfSession :DCTMUtils.getAdminSession();
				    	    dto.setPrCode(sFolId);
				    	    dto.setPrType("P");
				    	    
 	                        delHapFolderAuthBase(userSession , idfSession, uFolId);
				    	    
				    		cpFoObj.setString("u_up_fol_id", "");
				    		cpFoObj.setString("u_pr_code", sFolId);
				    		cpFoObj.setString("u_pr_type", "P");
				    		
				    		cpFoObj.save();
/*				    	    
				    	}else {
				    		idfTg_PObj = idfSession.getObject(new DfId(sFolId));
				    		sFolId = idfTg_PObj.getString("u_pjt_code");

				    		cpFoObj.setString("u_up_fol_id", "");
				    		cpFoObj.setString("u_pr_code", sFolId);
				    		cpFoObj.setString("u_pr_type", "P");
				    		cpFoObj.save();
				    		
				    		dto.setPrCode(sFolId);
				    	    dto.setPrType("P");
				    	}
*/				    	
				    	folChgMap.put(uFolId, "");
				    	folChgPrjMap.put( uFolId  , sFolId);
				    }
				    else if(sUprCodeCheck.substring(0,1).equals("R")) { //연구과제로 이동할 때
				    	newFolderName = getChekedRscsName(idfSession, dto.getTgCabinetcode(), sFinishYn, s_FolName, dto);
				    	String sFolId = newFolderName;
				    	//if(!DfId.isObjectId(newFolderName)) {
				            sFolId = researchService.createResearch(userSession, ResearchCreateDto.builder().uRschName(newFolderName).uListOpenYn("Y").uOwnDept(userSession.getUser().getOrgId()).uFinishYn(sFinishYn).uFolId(topFolId).uSecLevel(dto.getTargetSecLevel()).uCreateUser(dto.getReqUser()).build(), idfSession);
				            idfSession = idfSession != null ? idfSession :DCTMUtils.getAdminSession();
				    	//}else {
			    		//    idfTg_PObj = idfSession.getObject(new DfId(newFolderName));
			    		//    sFolId = idfTg_PObj.getString("u_rsch_code");
			    		    
				    		cpFoObj.setString("u_up_fol_id", "");
				    		cpFoObj.setString("u_pr_code", sFolId);
				    		cpFoObj.setString("u_pr_type", "P");
				    		cpFoObj.save();
			    		    
				    	    dto.setPrCode(sFolId);
				    	    dto.setPrType("R");
				    	//}				    	
				    	folChgPrjMap.put( uFolId  , sFolId);
				    	folChgMap.put(uFolId, "");
				    }else {
				    	
		    			//중복된 폴더명 확인 후 최종 폴더명 반환(root용), 옮기려는 폴더(topFolId)하위에 s_FolName과 같은 이름이 있으면 (1), (2)...로 붙임
				    	if(cpTObj !=null) {
				    	    if( !cpTObj.getType().getName().equals("edms_folder") ) 
		    		 	    {
		    		 	        topFolId = folChgMap.get(uFolId);
		    		 	    }
				    	}
		    			s_FolName = getChekedFolderName( idfSession, s_CabinetCode, topFolId, s_FolName, dto, uFolId);
				    	//if(DfId.isObjectId(s_FolName)) {
					 	       //folHapMap.put(uFolId, s_FolName) ;//(기존폴더ID, 합쳐진폴더ID)
					 	       //delHapFolderAuthBase(userSession , idfSession, uFolId);
					 	       //cpFoObj.destroy();  //이동하려는 레벨에 같은 폴더이름이 있으면 기존 폴더정보는 없애면서 auth_base정보도 없애야 한다.
				    	//}else {
			    			/** topFolId 값이 3종류 
			    			   1.프로젝트나 연구과제가 이동하면서 새로 생성된 폴더 : 위에서 새로 만들어진 폴더의 id
			    			   2.부서함에서 프로젝트-연구과제로나  프로젝트-연구과제함간에 폴더가 아닌 프로젝트나 연구과제단위 이동시 만들어진 프로젝트나 연구과제는 "" 빈값  
			    			   3.폴더에서 폴더로 변경되는 경우, targetBoxId로 오는 값을 사용
			    			   
			    			   folChgMap : 폴더가 소스폴더용으로 새로 만들어진 경우
			    			   folHamMap : 이동하려는 폴더에 같은 이름이 있어서 기존폴더를 destroy하는 경우
			    			 * **/
				    		cpFoObj.setString("u_fol_name", s_FolName);
				    		cpFoObj.save();

				    		if(null == folChgPrjMap.get( uFolId)) {
				    		    uptFolderPath(idfSession, topFolId, uFolId, s_CabinetCode, userSession.getUser().getUserId(), dto) ;
		  	    			    addMoveAuthBase( userSession, idfSession, uFolId, s_CabinetCode, "F", dto, null);

		  	    			    folChgMap.put(sFolArray.get(c), uFolId);//최상위로 자신을 지정		  	    			    
				    		}
		  	    			
		  	    			if(topFolId !=null && (null ==dto.getPrCode() || dto.getPrCode().equals("")) && (topFolId !=null && !topFolId.equals(dto.getTargetDboxId())))
		  	    			    folChgMap.put(sFolArray.get(c), topFolId);
		  	    			//if(topFolId !=null ) {
		  	    				//if(topFolId.equals(dto.getTargetDboxId()))
		  	    				   //folChgMap.put(sFolArray.get(c), topFolId);
		  	    			//}
				    	//}
	
				    }
			 	}
		 	}
	//이동처리	 	
			//파일은 바로 처리 (u_fol_di값으로 topFolId)
		    //  파일생성
		 	if(sFilArray.size()  > 0 && sFilArray.get(0) !="" && iCnt < 1) {
			 	for(int c=0; c < sFilArray.size(); c++) {
				     String ls_RobjectId 	= DCTMUtils.getCurrentObjectID(idfSession, sFilArray.get(c)); //문서의 최신버전 r_object_id를 가져온다.
        			 if(ls_RobjectId.equals("")) {
	        			throw new NotFoundException("문서 최신버전이 변경됨[Documentum]");
        			 }
					 IDfDocument idfNewDoc =(IDfDocument)idfSession.getObject(new DfId(ls_RobjectId)); 
					 String sDocKey = idfNewDoc.getString("u_doc_key");
				     String s_TypeName = idfNewDoc.getType().getName(); //edms_doc인지 edms_doc_imp 이거나
				     
				     String s_DocName = idfNewDoc.getString("object_name");

				     String s_Extr    = idfNewDoc.getString("u_file_ext");
				     String s_uFolId  = idfNewDoc.getString("u_fol_id");
				     //NONE(1, "N"), BROWSE(2, "B"), READ(3, "R"), RELATE(4, "RELATE"), VERSION(5, "V"), WRITE(6, "W"), DELETE(7, "D");
				     
				     //if( idfObj.getPermit() < 6) continue; //쓰기권한이상이 있으면 복사할 수 있도록  
				     if(null==folChgMap.get( s_uFolId)) {
				         uFolId = topFolId;  //옮기려는 폴더에 같은 이름이 있는지 체크하는 용도
				     }else {
				    	 uFolId = folChgMap.get( s_uFolId);  //옮기려는 폴더에 같은 이름이 있는지 체크하는 용도
				     }
				     //uFolId 타입이 edms_project나 edms_research이면 s_uFolId로 비교하고 uFolId="";
				     
				     String ls_TypeNm="";
				     if(uFolId.equals("") || uFolId.equals(" ")) {
				    	 
				     }else {
					     IDfPersistentObject typeObj = idfSession.getObject(new DfId(uFolId));
					     if(typeObj.getType().getName().equals("edms_folder")){
		    		         String sourceBoxGubun=  typeObj.getString("u_fol_type").substring(0,2);
		    			     String sourceGubun = uSrcMap.get(sourceBoxGubun)==null? uSrcMap.get(sourceBoxGubun.substring(0,1)):uSrcMap.get(sourceBoxGubun);
			    			 if(targetGubunCp.equals(sourceGubun)) {
			    				isLog=true; //로그기록대상
			    				lsJobGubun =sourceGubun;
			    				lsJobCode=lsJobGubun;
			    			 }
				 	     }
					     ls_TypeNm=typeObj.getType().getName();
				     }
	    			 String s_NDocName= s_DocName;
				     if(ls_TypeNm.equals("edms_project") || ls_TypeNm.equals("edms_research")) {//프로젝트나 연구과제로 파일을 직접 복사할 경우
				    	 uFolId = "";
				     }else {
				    	 s_NDocName=getChekedDocNameScnd(idfSession, s_CabinetCode, uFolId, s_DocName, dto, sDocKey, s_Extr);
				     }
				     if(!isSameType) {
				    	 idfNewDoc = s_TypeName.equals("edms_doc")?saveAsNewFiles(idfSession, idfNewDoc, dto):saveAsNewImpFiles(idfSession, idfNewDoc, dto);
				     }
	    			 if (!DfId.isObjectId(s_DocName) && !s_NDocName.equals( sDocKey)) {  //붙여넣기를 다시 했을때, 같은파일이면 파일명을 바꾸지 않는다.
	    				 s_DocName = s_NDocName ;//s_DocName+"[복사본]";
	    			 }
	    			 if(s_NDocName.equals( sDocKey)) continue;
//이동처리

	    			//중복된 파일명 확인 후 최종 폴더명 반환(root용)
					 idfNewDoc.setTitle(s_DocName + "."+s_Extr);
	    			 idfNewDoc.setObjectName(s_DocName);

				     String registObjId = idfNewDoc.getObjectId().toString();
				     
				     idfNewDoc.setBoolean("u_privacy_flag",      false);   //개인정보포함여부 unChk
				     
				     String uDocStatus   = idfNewDoc.getString("u_doc_status");
				     boolean uPrivacyFlag = idfNewDoc.getBoolean("u_privacy_flag");
				     String uSecLevel    = idfNewDoc.getString("u_sec_level").toLowerCase();

				     if (secLevelMap.get(idfNewDoc.getString("u_sec_level")) < secLevelMap.get(dto.getTargetSecLevel())) { //상위폴더의 보안등급이 더 높은 경우
				    	 uSecLevel=  dto.getTargetSecLevel().toLowerCase();
                     }
				     
				     String uPrivacyStr ="g";
				     if(uPrivacyFlag) {    
				    	 uPrivacyStr="p";
				         uSecLevel = uSecLevel.equals("s")?"s":"l";
				     }
				     
				     if(!targetBoxGubun.equals("DI")) {
				    	 if(!dto.getPrCode().equals("") && !dto.getPrCode().equals(" ")) {
				    	     String aclName = "a_"+s_CabinetCode+"_"+dto.getPrType().toLowerCase() +"_"+uPrivacyStr+"_"+uSecLevel+"_n";
				    	     idfNewDoc.setACLName(aclName);
				    	 }else {
				    	     String aclName = "a_"+s_CabinetCode+"_d_"+uPrivacyStr+"_"+uSecLevel+"_n"; //부서함
				             idfNewDoc.setACLName(aclName);
				    	 }
				     }else {
			    	     String aclName = "a_"+s_CabinetCode+"_s_g_s_n"; //중요문서함
			    	     idfNewDoc.setACLName(aclName);
				     }

	//이동처리
				     //idfNewDoc.setACLDomain(idfSession.getDocbaseOwnerName());
				     idfNewDoc.setString("u_pr_code", dto.getPrCode());
				     idfNewDoc.setString("u_pr_type", dto.getPrType());

				     idfNewDoc.setString("u_fol_id", uFolId); //타겟 폴더 ID
				     idfNewDoc.setString("u_doc_key", "" + idfNewDoc.getChronicleId()); // 문서 키
				     idfNewDoc.setString("u_cabinet_code", s_CabinetCode);

				     String s_PreservFlag= psrvPMap.get( dto.getTargetSecLevel());
				     if(null==s_PreservFlag) s_PreservFlag= psrvPMapDf.get( dto.getTargetSecLevel()); //등록된게 없으면 정책서에서 기술한 기본값
				     if(dto.getPrType().equals("P") || dto.getPrType().equals("R")) {
				    	 s_PreservFlag= psrvPMap.get( "PJT");
				    	 if(null !=s_PreservFlag) {
				    		 if(s_PreservFlag.equals("1")) 
				    	         s_PreservFlag="0";
				    	 }
				     }
				     if(null==s_PreservFlag) s_PreservFlag= idfNewDoc.getString("u_preserve_flag");
				     int li_PreservYear=Integer.parseInt(s_PreservFlag);//==0?9999:Integer.parseInt(s_PreservFlag);
				     idfNewDoc.setInt("u_preserve_flag", li_PreservYear );  //보존연한
				     
				     // 추가 권한자 지정 : 기본 권한이나 공유/협업 대상자 등
				     if (idfNewDoc.getString("u_doc_status").equals("C") ) { //Closed문서인 경우에만 만료일자 계산  
						     IDfTime startDate = new DfTime() ;
  						     // Convert the expiration date to a calendar object.
						     GregorianCalendar cal = new GregorianCalendar(startDate.getYear(), startDate.getMonth(),   startDate.getDay() );  
						     // Add the number of months ­1 (months start counting from 0).
						     cal.add(GregorianCalendar.MONTH, (12 * li_PreservYear) -1) ;
						     IDfTime expireDate = new DfTime (cal.getTime()) ;
						     if(li_PreservYear==0)
						    	 idfNewDoc.setString("u_expired_date",  "9999-12-31 00:00:00");
						     else
						         idfNewDoc.setString("u_expired_date",  expireDate.toString());
				     }
/*				     
 * 
			         idfNewDoc.setInt("u_preserve_flag",  dto.getUserPreset().getUPreserveFlag()); // 보존년한
			         if (dto.getUserPreset().getUOpenFlag() != null) {
			           idfNewDoc.setString("u_open_flag", dto.getUserPreset().getUOpenFlag());
			         }
			         if (dto.getUserPreset().getUMailPermitFlag() != null) {
			           idfNewDoc.setBoolean("u_auto_auth_mail_flag", "Y".equals(dto.getUserPreset().getUMailPermitFlag())?true:false);
			         }
*/
				     idfNewDoc.setString("u_update_date", (new DfTime()).toString());
			         if(!isSameType) {
				    	 IDfDocument idfObj =(IDfDocument)idfSession.getObject(new DfId(ls_RobjectId));
				    	 idfObj.destroy(); //edms_doc, edms_doc_imp간 이동시 기존 문서를 삭제처리한다
				     }else {
					     idfNewDoc.save();
				     }
//이동처리	
			         //dto.setPreFolderId("");
				     addMoveAuthBase(userSession, idfSession, idfNewDoc.getString("u_doc_key"), s_CabinetCode,  "D", dto, idfNewDoc);
	
					//------------------------------------------
					// 과거 버전이 있는 경우 처리
					//------------------------------------------
					if(!idfNewDoc.getString("u_doc_key").equals(idfNewDoc.getObjectId().toString()))
					{
						String s_VerId = "";
						IDfDocument idf_VerDoc = null;
						IDfCollection idf_VerCol = idfNewDoc.getVersions(null);
						
						while(idf_VerCol != null && idf_VerCol.next())
						{
							s_VerId = idf_VerCol.getString("r_object_id");
							//과거 버전만 처리하기 위해 현재 버전은 skip
							if(s_VerId.equals(idfNewDoc.getObjectId().toString()))
								continue;
							
							//권한은 현재 버전의 ACL Name을 사용하면 그대로 적용 됨
							idf_VerDoc = (IDfDocument)idfSession.getObject(new DfId(s_VerId));
							idf_VerDoc.setACLDomain(idfSession.getDocbaseOwnerName());
							idf_VerDoc.setACLName(idfNewDoc.getString("acl_name"));
							
							//위치 변경은 ps_RcevFolId 값이 있는 경우만
							idf_VerDoc.setString("u_fol_id", idfNewDoc.getString("u_fol_id"));
							idf_VerDoc.setString("u_pr_code", idfNewDoc.getString("u_pr_code"));
							idf_VerDoc.setString("u_pr_type", idfNewDoc.getString("u_pr_type"));
							
							idf_VerDoc.setString("u_cabinet_code", idfNewDoc.getString("u_cabinet_code"));
							idf_VerDoc.save();
						}
						if(idf_VerCol != null) idf_VerCol.close(); 
					}
				     
				     if(isLog) {
						 if( lsJobGubun.equals("DEL")) lsJobGubun =uDocStatus.equals("L") ? "LD" : "DR";
						 else lsJobGubun ="DM";
				    	 
				    	 
						 String s_BeforeChangeVal = folPathMap.get(s_uFolId);
						 if(null==s_BeforeChangeVal) {
							 s_BeforeChangeVal=pathDao.selectFolderPath(s_uFolId);
							 folPathMap.put(s_uFolId, s_BeforeChangeVal);
						 }

						 String s_AfterChangeVal = folPathMap.get(uFolId);
						 if(null==s_AfterChangeVal)  {
							 //s_AfterChangeVal=pathDao.selectFolderPath(nFolId);
							 s_AfterChangeVal= GetFolderPathFromDCTM(userSession, uFolId, true) ;//pathDao.selectFolderPath(uFolId);
							 folPathMap.put(uFolId, s_AfterChangeVal);
						 }
						 
	  	    			 LogDoc logDoc = LogDoc.builder()
	 	    			          .uJobCode( lsJobCode)
	 	    			          .uDocId(idfNewDoc.getString("r_object_id"))
	 	    			          .uDocKey(idfNewDoc.getString("u_doc_key"))
	 	    			          .uDocName(idfNewDoc.getString("title").replaceAll("'", "''"))
	 	    			          .uDocVersion(Integer.parseInt(idfNewDoc.getVersionLabel(0).substring(0, idfNewDoc.getVersionLabel(0).indexOf(".")))+"")
	 	    			          .uFileSize(Long.parseLong(idfNewDoc.getString("r_content_size")))
	 	    			          .uOwnDeptcode(dto.getOwnSrDeptOrgId())
	 	    			          .uActDeptCode(userSession.getUser().getOrgId())
	 	    			          .uJobUser(dto.getReqUser())
	 	    			          .uJobUserType(dto.getUJobUserType()==null ? "P" : dto.getUJobUserType()) //작업자구분(P:개인, D:부서관리자, C:사별관리자, G:그룹관리자, S:시스템)
	 	    			          .uDocStatus(idfNewDoc.getString("u_doc_status"))
	 	    			          .uSecLevel(idfNewDoc.getString("u_sec_level"))
	 	    			          .uCabinetCode(idfNewDoc.getString("u_cabinet_code"))
	 	    			          //.uJobGubun(lsJobGubun) 
	 	    			          .uBeforeChangeVal(s_BeforeChangeVal)
	 	    			          .uAfterChangeVal( s_AfterChangeVal)
	 	    			          
	 	    			          .uUserIp(dto.getReqUserIp())							// 받아야함.
	 	    			          .uAttachSystem("")
	 	    			          .build();
	 	    			      insertLog(logDoc);
					 }
			 	}
		 	}
	//이동처리	 	
	    	for(int i=0; i < folList.size(); i++) {
	    		
	    		uFolId = folList.get(i);//상위폴더 ID
	    		
	    		if(uFolId.equals(" ") && ( !keyStr.substring(0,1).equals("p")&& !keyStr.substring(0,1).equals("r"))) continue;
	    		if(uFolId.equals("") && ( keyStr.substring(0,1).equals("p")|| keyStr.substring(0,1).equals("r"))) uFolId=keyStr ;

	    		//if(null !=folHapMap.get(uFolId)) continue;

	    		if(null==uFolId) continue;
	    	    if(null ==dto.getPrCode() || dto.getPrCode().equals("")|| dto.getPrCode().equals(" ")) { //프로젝트나 연구과제로의 이동이 아닌경우
	    			if(uFolId.equals(" ") || uFolId.equals(""))
	    				nFolId = folChgMap.get(keyStr);
	    			else
		    		    nFolId = folChgMap.get(uFolId);
		    		
		    		if(null != nFolId) { //합폴더가 아닌 경우, 
		    			String newUFolId =nFolId;
		    			boolean isUpt=true;
		    			if(!uFolId.equals(keyStr)) {

		    			    IDfPersistentObject cpFoObj = idfSession.getObject(new DfId(uFolId));
		    			    String upFolId = cpFoObj.getString("u_up_fol_id");
		    			    //newUFolId=folHapMap.get(upFolId);
		    			    //if(null== newUFolId) {
		    			        newUFolId = folChgMap.get(upFolId);
		    			    //}
		    			    
		    			    String s_FolName= cpFoObj.getString("u_fol_name");
			    			s_FolName = getChekedFolderName( idfSession, s_CabinetCode, newUFolId, s_FolName, dto, uFolId);
					    	if(DfId.isObjectId(s_FolName)) {
					    		newUFolId=s_FolName;
						 	    folHapMap.put(upFolId, s_FolName) ;//(기존폴더ID, 합쳐진폴더ID)
						 	    //delHapFolderAuthBase(userSession , idfSession, uFolId);
						 	    //cpFoObj.destroy();
						 	    isUpt=false;
					    	}

		                    if( keyStr.substring(0,1).equals("p")|| keyStr.substring(0,1).equals("r")) {
			    			   if(null==newUFolId ) {
			    				   newUFolId = folChgMap.get(keyStr);
			    			   }
			    		    } 
		    			}
			    		if( null !=folChgMap.get(uFolId) &&  folChgMap.get(uFolId).equals(uFolId)) {
			    			;
			    		}else {
		    				dto.setPreFolderId(uFolId );
			    			if(isUpt) {
		  	    			    uptFolderPath(idfSession, newUFolId, uFolId, s_CabinetCode, userSession.getUser().getUserId(), dto) ;
			    			}
	  	    			    addMoveAuthBase( userSession,idfSession, uFolId, s_CabinetCode, "F", dto, null);
			    		}
	  	    			//folChgMap.put(sFolArray.get(i), newUFolId);
		    		}else {
	    			    IDfPersistentObject cpFoObj = idfSession.getObject(new DfId(uFolId));
	    			    String upFolId = cpFoObj.getString("u_up_fol_id");
	    			    dto.setPreFolderId(uFolId );
		    			uptFolderPath(idfSession, upFolId, uFolId, s_CabinetCode, userSession.getUser().getUserId(), dto) ;
		    			addMoveAuthBase( userSession,idfSession, uFolId, s_CabinetCode, "F", dto, null);
		    		}
	    		}else {
					if(uFolId.equals(keyStr)) {

			    		if( null !=folChgMap.get(uFolId) &&  folChgMap.get(uFolId).equals(uFolId)) {
			    			;
			    		}else {
							
					    	IDfPersistentObject cpFoObj = idfSession.getObject(new DfId(uFolId));
					    	if(!cpFoObj.getType().getName().equals("edms_folder")) {
								if(null==folChgMap.get(uFolId))
							        cpFoObj.setString("u_up_fol_id", uFolId);
					    	    else
					    	    	cpFoObj.setString("u_up_fol_id", folChgMap.get(uFolId));
	
				    		    cpFoObj.setString("u_pr_code", dto.getPrCode());
				    		    cpFoObj.setString("u_pr_type", dto.getPrType());
					    		cpFoObj.setString("u_fol_type", dto.getTargetFolType());

				    		    cpFoObj.save();
					    	}else {
					    		if(null !=folChgPrjMap.get(uFolId)) {
					    			//System.out.println(uFolId+"#MOLA:" + folChgPrjMap.get(uFolId));
					    			;
					    		}
								if(null!=folChgPrjMap.get(uFolId) ) 
							        cpFoObj.setString("u_up_fol_id", folChgPrjMap.get(uFolId));
								if(null!=folChgMap.get(uFolId))  
								    cpFoObj.setString("u_up_fol_id", folChgMap.get(uFolId));
					    		    
							    cpFoObj.setString("u_pr_code", dto.getPrCode());
				    		    cpFoObj.setString("u_pr_type", dto.getPrType());
					    		cpFoObj.setString("u_fol_type", dto.getTargetFolType());
				    		    cpFoObj.save();
	
					    		
					    	}
			    		}
	    			    //folHapMap.put(uFolId, dto.getPrCode()) ;//(기존폴더ID, 프로젝트ID)
				    }else {
				    	IDfPersistentObject cpFoObj = idfSession.getObject(new DfId(uFolId));
    	    			String upFolId = cpFoObj.getString("u_up_fol_id");
				    	if(null !=folChgPrjMap.get(upFolId)) {
				    		cpFoObj.setString("u_up_fol_id", "");
				    		cpFoObj.setString("u_pr_code", dto.getPrCode());
				    		cpFoObj.setString("u_pr_type", dto.getPrType());
				    		cpFoObj.setString("u_fol_type", dto.getTargetFolType());
				    		
				    		cpFoObj.save();
				    	}else {
				    		cpFoObj.setString("u_pr_code", dto.getPrCode());
				    		cpFoObj.setString("u_pr_type", dto.getPrType());
				    		cpFoObj.setString("u_fol_type", dto.getTargetFolType());
				    		
				    		cpFoObj.save();
				    	}
				    	
				    }
    			    IDfPersistentObject cpFoObj = idfSession.getObject(new DfId(uFolId));
    			    String upFolId = cpFoObj.getString("u_up_fol_id");
    			    dto.setPreFolderId(uFolId );
	    			uptFolderPath(idfSession, upFolId, uFolId, s_CabinetCode, userSession.getUser().getUserId(), dto) ;
	    			addMoveAuthBase( userSession,idfSession, uFolId, s_CabinetCode, "F", dto, null);
	    		}
	    		List<DPath> folBList = folMap.get(uFolId);

	    		if(null!=folBList ) {
		    		for(int j=0; j < folBList.size(); j++) {
		    			
		    			DPath dData = folBList.get(j);
	//이동처리	    	
		    			uFolId = dData.getRObjectId();
		    			if(dData.getListType().equals("FOL")) {//폴더("FOL" 인 경우)

		    				nFolId = folChgMap.get(uFolId);

		    				if(null !=dto.getPrCode()  && !dto.getPrCode().equals(" ") && !dto.getPrCode().equals("")) {
		    					if(uFolId.equals(keyStr)) {
		    	    			    //folHapMap.put(uFolId, dto.getPrCode()) ;//(기존폴더ID, 프로젝트ID)
		    				    }else {
		    				    	IDfPersistentObject cpFoObj = idfSession.getObject(new DfId(uFolId));
			    	    			String upFolId = cpFoObj.getString("u_up_fol_id");
			    	    			
		      	    			    uptFolderPath(idfSession, upFolId, uFolId, s_CabinetCode, userSession.getUser().getUserId(), dto) ;
		    	    				dto.setPreFolderId(uFolId );
		    	    			    addMoveAuthBase( userSession, idfSession, uFolId, s_CabinetCode, "F", dto, null);
		    				    }
		    	    		}else {
			    				if(nFolId==null || nFolId==""|| nFolId==" ") {
			    					
			    					if(uFolId.equals(keyStr)) continue;
			    					
			    	    			IDfPersistentObject cpFoObj = idfSession.getObject(new DfId(uFolId));
			    	    			String upFolId = cpFoObj.getString("u_up_fol_id");
			    	    			String newUFolId=folChgMap.get(upFolId);
			    	    			boolean isUpt=true;
			    	    			if(null==newUFolId) {
			    	    				
			    	    				//newUFolId=folHapMap.get(upFolId);
			    	    				//if(null== newUFolId) {
			    	    			    //    newUFolId = upFolId; //기존 폴더그대로 유지
			    	    				//}
				    	    			if(dto.getPrCode().equals(newUFolId)) {
				    	    				isUpt=false;
				    	    				dto.setPreFolderId(uFolId );
			      	    			        uptFolderPath(idfSession, "", uFolId, s_CabinetCode, userSession.getUser().getUserId(), dto) ;
			      	    			        addMoveAuthBase( userSession, idfSession, uFolId, s_CabinetCode, "F", dto, null);
				    	    			}else {
				    	    				//isUpt=false;
						    			    String s_FolName= cpFoObj.getString("u_fol_name");
							    			s_FolName = getChekedFolderName( idfSession, s_CabinetCode, newUFolId, s_FolName, dto, uFolId);
									    	//if(DfId.isObjectId(s_FolName)) {
									    	//	isUpt=false;
									    		
									    	//	newUFolId=s_FolName;
										 	//    folHapMap.put(uFolId, s_FolName) ;//(기존폴더ID, 합쳐진폴더ID)
									 	        //delHapFolderAuthBase(userSession , idfSession, uFolId);
									 	        //cpFoObj.destroy();  //이동하려는 레벨에 같은 폴더이름이 있으면 기존 폴더정보는 없애면서 auth_base정보도 없애야 한다.
									    	//}
				    	    			}
			    	    			}
			    	    			if(isUpt) {
			    	    				newUFolId=upFolId;
			      	    			    uptFolderPath(idfSession, newUFolId, uFolId, s_CabinetCode, userSession.getUser().getUserId(), dto) ;
			      	    			    upFolId=newUFolId;
			    				    }
		    	    				dto.setPreFolderId(upFolId );
		      	    			    addMoveAuthBase( userSession, idfSession, uFolId, s_CabinetCode, "F", dto, null);
			    				}
		    	    		}
		    			}else if(dData.getListType().equals("LNK")) {//링크파일인 경우
	
		    				uptDocLink(idfSession,  uFolId,  dData.getRObjectId(), s_CabinetCode, userSession.getUser().getUserId(), dto);
		    				
		    			}else { //문서("DOC" 인 경우)
		    			    // nFileId = createCopyDoc(...);
		    			    //U_FOL_ID 값에 nFolId인자값을 넣어줌
						     String ls_RobjectId 	= DCTMUtils.getCurrentObjectID(idfSession, dData.getRObjectId()); //문서의 최신버전 r_object_id를 가져온다.
		        			 if(ls_RobjectId.equals("")) {
			        			    throw new NotFoundException("문서 최신버전이 변경됨[Documentum]");
		        			 }
		        			 ls_RobjectId=ls_RobjectId==""?dData.getRObjectId():ls_RobjectId;
						     IDfDocument idfNewDoc = (IDfDocument)idfSession.getObject(new DfId(ls_RobjectId));
						     String sDocKey = idfNewDoc.getString("u_doc_key");
						     String s_TypeName = idfNewDoc.getType().getName();  //edms_doc 문서인지 edms_doc_imp 문서인지
						     //if( idfObj.getPermit() < 6) continue; //쓰기권한이상이 있으면 복사할 수 있도록
	//이동처리					 
						     /* 이동처리************************ 파일 복사 *****************************/
						     if(!isSameType) {
						    	 idfNewDoc = s_TypeName.equals("edms_doc")?saveAsNewFiles(idfSession, idfNewDoc, dto):saveAsNewImpFiles(idfSession, idfNewDoc, dto);
						     }
						     uFolId = idfNewDoc.getString("u_fol_id");
						     String nFoldId = folChgMap.get(uFolId);
						     if(null==nFoldId)  nFoldId= folChgPrjMap.get(uFolId);
						     
						     if(null !=folChgPrjMap.get(uFolId)) nFoldId= folChgPrjMap.get(uFolId);
						     if(keyStr.equals(uFolId) &&  null != folChgMap.get(uFolId) && folChgMap.get(uFolId).equals("") && folChgMap.get(uFolId).equals(" ")) {
						    	 uFolId = idfNewDoc.getString("u_fol_id");
						    	 nFoldId = uFolId;
						     }
						     
						     //if(null==nFoldId)  nFoldId= folHapMap.get(uFolId);
						     
						     String registObjId = idfNewDoc.getObjectId().toString(); //nFoldId 확인지점
						     String s_DocName = idfNewDoc.getString("object_name");
						     
						     String s_Extr    = idfNewDoc.getString("u_file_ext");
						     if(null !=nFoldId ) {
					             String s_NDocName = s_DocName;
					             if(!dto.getPrCode().equals(nFoldId)) {
			    			         s_NDocName=getChekedDocNameScnd(idfSession, s_CabinetCode, nFoldId, s_DocName, dto, sDocKey, s_Extr);
					             }else {
					            	 nFoldId="";
					             }
				    		     if (!DfId.isObjectId(s_NDocName) && !s_NDocName.equals( sDocKey)) {  //붙여넣기를 다시 했을때, 같은파일이면 파일명을 바꾸지 않는다.
				    				 //idfNewDoc.setString("u_doc_status", "L"); // live //함침처리때 룰이라 제거			    				 
				    				 s_DocName =  s_NDocName ;//s_DocName+"[복사본]";
							     }
				    		     if(null == nFoldId) nFoldId=uFolId;

					             idfNewDoc.setString("u_fol_id", nFoldId); //타겟 폴더 ID
			    			     idfNewDoc.setTitle(s_DocName + "."+s_Extr);
				    			 idfNewDoc.setObjectName (s_DocName);
				    			 
						     }
			    			//중복된 파일명 확인 후 최종 폴더명 반환(root용)
						     String uDocStatus   = idfNewDoc.getString("u_doc_status");
						     
						     idfNewDoc.setBoolean("u_privacy_flag",      false);   //개인정보포함여부 unChk
						     
						     boolean uPrivacyFlag = idfNewDoc.getBoolean("u_privacy_flag");
						     String uSecLevel    = idfNewDoc.getString("u_sec_level").toLowerCase();
						     if (secLevelMap.get(idfNewDoc.getString("u_sec_level")) < secLevelMap.get(dto.getTargetSecLevel())) {
				    		     //상위폴더의 보안등급이 더 높은 경우
						    	 uSecLevel=  dto.getTargetSecLevel().toLowerCase();
				    		 }						     
						     String uPrivacyStr ="g";
						     if(uPrivacyFlag) {    
						    	 uPrivacyStr="p";
						         uSecLevel = uSecLevel.equals("s")?"s":"l";
						     }
						     
						     if(!targetBoxGubun.equals("DI")) {
						    	 if(!dto.getPrCode().equals("") && !dto.getPrCode().equals(" ")) {
						    	     String aclName = "a_"+s_CabinetCode+"_"+dto.getPrType().toLowerCase() +"_"+uPrivacyStr+"_"+uSecLevel+"_n";
						    	     idfNewDoc.setACLName(aclName);
						    	 }else {
						    	     String aclName = "a_"+s_CabinetCode+"_d_"+uPrivacyStr+"_"+uSecLevel+"_n"; //부서함
						             idfNewDoc.setACLName(aclName);
						    	 }
						     }else {
					    	     String aclName = "a_"+s_CabinetCode+"_s_g_s_n"; //중요문서함
					    	     idfNewDoc.setACLName(aclName);
						     }

						     idfNewDoc.setOwnerName(idfSession.getDocbaseOwnerName());
						     
						     idfNewDoc.setString("u_doc_key", "" + idfNewDoc.getChronicleId()); // 문서 키
						     String s_PreservFlag= psrvPMap.get( dto.getTargetSecLevel());
						     if(null==s_PreservFlag) s_PreservFlag= psrvPMapDf.get(dto.getTargetSecLevel()); //등록된게 없으면 정책서에서 기술한 기본값
						     if(dto.getPrType().equals("P") || dto.getPrType().equals("R")) {
					    	     s_PreservFlag= psrvPMap.get( "PJT");
					    	     if(null !=s_PreservFlag) {
					    	    	 if(s_PreservFlag.equals("1")) 
					    	             s_PreservFlag="0";
					    	     }
					    	 }
						     if(null==s_PreservFlag) s_PreservFlag= idfNewDoc.getString("u_preserve_flag");
						     int li_PreservYear=Integer.parseInt(s_PreservFlag) ;//==0?9999:Integer.parseInt(s_PreservFlag);
						     idfNewDoc.setInt("u_preserve_flag", li_PreservYear );  //보존연한
						     
						     // 추가 권한자 지정 : 기본 권한이나 공유/협업 대상자 등
						     if (idfNewDoc.getString("u_doc_status").equals("C") ) { //Closed문서인 경우에만 만료일자 계산  
								     IDfTime startDate = new DfTime() ;
								     // Convert the expiration date to a calendar object.
								     GregorianCalendar cal = new GregorianCalendar(startDate.getYear(), startDate.getMonth(),   startDate.getDay() );  
								     // Add the number of months ­1 (months start counting from 0).
								     cal.add(GregorianCalendar.MONTH, (12 * li_PreservYear) -1) ;
								     IDfTime expireDate = new DfTime (cal.getTime()) ;
								     if(li_PreservYear==0)
								    	 idfNewDoc.setString("u_expired_date",  "9999-12-31 00:00:00");
								     else
								         idfNewDoc.setString("u_expired_date",  expireDate.toString());
						     }
/*							  
					         idfNewDoc.setInt("u_preserve_flag",  dto.getUserPreset().getUPreserveFlag()); // 보존년한
					         if (dto.getUserPreset().getUOpenFlag() != null) {
					           idfNewDoc.setString("u_open_flag", dto.getUserPreset().getUOpenFlag());
					         }
					         if (dto.getUserPreset().getUMailPermitFlag() != null) {
					           idfNewDoc.setBoolean("u_auto_auth_mail_flag", "Y".equals(dto.getUserPreset().getUMailPermitFlag())?true:false);
					         }
*/						     
						     idfNewDoc.setString("u_pr_code", dto.getPrCode());
						     idfNewDoc.setString("u_pr_type", dto.getPrType());
						     
						     idfNewDoc.setString("u_cabinet_code", s_CabinetCode);
						     idfNewDoc.setString("u_update_date", (new DfTime()).toString());
						     
						     idfNewDoc.save();
						     dto.setPreFolderId(  idfNewDoc.getString("u_copy_org_id"));
						     addMoveAuthBase( userSession,idfSession, idfNewDoc.getString("u_doc_key"), s_CabinetCode, "D", dto, idfNewDoc);
						     
						     //------------------------------------------
							// 과거 버전이 있는 경우 처리
							//------------------------------------------
						    if(!idfNewDoc.getString("u_doc_key").equals(idfNewDoc.getObjectId().toString()))
							{
								String s_VerId = "";
								IDfDocument idf_VerDoc = null;
								IDfCollection idf_VerCol = idfNewDoc.getVersions(null);
								
								while(idf_VerCol != null && idf_VerCol.next())
								{
									s_VerId = idf_VerCol.getString("r_object_id");
									//과거 버전만 처리하기 위해 현재 버전은 skip
									if(s_VerId.equals(idfNewDoc.getObjectId().toString()))
										continue;
									
									//권한은 현재 버전의 ACL Name을 사용하면 그대로 적용 됨
									idf_VerDoc = (IDfDocument)idfSession.getObject(new DfId(s_VerId));
									idf_VerDoc.setACLDomain(idfSession.getDocbaseOwnerName());
									idf_VerDoc.setACLName(idfNewDoc.getString("acl_name"));
									
									//위치 변경은 ps_RcevFolId 값이 있는 경우만
									idf_VerDoc.setString("u_fol_id", idfNewDoc.getString("u_fol_id"));
									idf_VerDoc.setString("u_pr_code", idfNewDoc.getString("u_pr_code"));
									idf_VerDoc.setString("u_pr_type", idfNewDoc.getString("u_pr_type"));
									
									idf_VerDoc.setString("u_cabinet_code", idfNewDoc.getString("u_cabinet_code"));
									idf_VerDoc.save();
								}
								if(idf_VerCol != null) idf_VerCol.close(); 
							}						     
		  				     if(isLog) {
								 if( lsJobGubun.equals("DEL")) lsJobGubun =uDocStatus.equals("L") ? "LD" : "DR";
								 else lsJobGubun ="DM";
		   				    	 
								 String s_BeforeChangeVal = folPathMap.get(uFolId);
								 if(null==s_BeforeChangeVal) {
									 s_BeforeChangeVal=pathDao.selectFolderPath(uFolId);
									 folPathMap.put(uFolId, s_BeforeChangeVal);
								 }

								 String s_AfterChangeVal =s_BeforeChangeVal;
								 if(null !=nFoldId) {
									 s_AfterChangeVal = folPathMap.get(nFolId);
									 if(null==s_AfterChangeVal)  {
										 //s_AfterChangeVal=pathDao.selectFolderPath(nFolId);
										 s_AfterChangeVal= GetFolderPathFromDCTM(userSession, uFolId, true) ;//pathDao.selectFolderPath(uFolId);
										 folPathMap.put(nFolId, s_AfterChangeVal);
									 }
		  				         }
		   						
		 	  	    			 LogDoc logDoc = LogDoc.builder()
		 	 	    			          .uJobCode( lsJobCode)
		 	 	    			          .uDocId(idfNewDoc.getString("r_object_id"))
		 	 	    			          .uDocKey(idfNewDoc.getString("u_doc_key"))
		 	 	    			          .uDocName(idfNewDoc.getString("title").replaceAll("'", "''"))
		 	 	    			          .uDocVersion(Integer.parseInt(idfNewDoc.getVersionLabel(0).substring(0, idfNewDoc.getVersionLabel(0).indexOf(".")))+"")
		 	 	    			          .uFileSize(Long.parseLong(idfNewDoc.getString("r_content_size")))
		 	 	    			          .uOwnDeptcode(dto.getOwnSrDeptOrgId())
		 	 	    			          .uActDeptCode(userSession.getUser().getOrgId())
		 	 	    			          .uJobUser(dto.getReqUser())
		 	 	    			          .uJobUserType(dto.getUJobUserType()==null ? "P" : dto.getUJobUserType()) //작업자구분(P:개인, D:부서관리자, C:사별관리자, G:그룹관리자, S:시스템)
		 	 	    			          .uDocStatus(idfNewDoc.getString("u_doc_status"))
		 	 	    			          .uSecLevel(idfNewDoc.getString("u_sec_level"))
		 	 	    			          .uCabinetCode(idfNewDoc.getString("u_cabinet_code"))		 	 	    			          
		 	 	    			          //.uJobGubun(lsJobGubun) //설정필요없음
		 	 	    			          .uBeforeChangeVal(s_BeforeChangeVal)
				    			          .uAfterChangeVal( s_AfterChangeVal)
		 	 	    			          
		 	 	    			          .uUserIp(dto.getReqUserIp())							// 받아야함.
		 	 	    			          .uAttachSystem("")
		 	 	    			          .build();
		 	 	    			      insertLog(logDoc);
		   				   }
		  				   if(!isSameType) {
							   IDfDocument idfObj = (IDfDocument)idfSession.getObject(new DfId(ls_RobjectId));
		  				       idfObj.destroy(); //중요함-일반문서함간 이동이면 원본을 삭제처리한다
		  				   }
		    			}		    			
		    		}
	    		}
	    	}
/*	    	
	    	for (Entry<String,String> entry : folHapMap.entrySet()) {
	    		
	    		if(entry.getKey().equals(entry.getValue())) continue;
	    		if(DfId.isObjectId(entry.getKey())) {
	    			IDfPersistentObject idfObj = (IDfPersistentObject)idfSession.getObject(new DfId(entry.getKey()));
		 	        delHapFolderAuthBase(userSession , idfSession, entry.getKey());
		 	        idfObj.destroy();  //이동하려는 레벨에 같은 폴더이름이 있으면 기존 폴더정보는 없애면서 auth_base정보도 없애야 한다.
	    		}
	    	}
*/
	    	if(null !=dto.getPrCode() && !dto.getPrCode().equals(" ") && !dto.getPrCode().equals("")) {
	    		if(sFolArray.size() > 0 && sFolArray.get(0) !="" ) {
	    			if (DfId.isObjectId(keyStr)) {
	    			    IDfPersistentObject idfObj = (IDfPersistentObject)idfSession.getObject(new DfId(keyStr));
	    			    
	    			    if(null !=idfObj.getString("u_pr_code") && !idfObj.getString("u_pr_code").equals(" ") && !idfObj.getString("u_pr_code").equals("")) {
	    			    	
	    			        if(idfObj.getString("u_pr_code").equals(dto.getPrCode()) && ( dto.getTargetGubun().equals("POW") || dto.getTargetGubun().equals("ROW"))) {
		 	                        delHapFolderAuthBase(userSession , idfSession, keyStr);
		 	                        idfObj.destroy();  //이동하려는 레벨에 같은 폴더이름이 있으면 기존 폴더정보는 없애면서 auth_base정보도 없애야 한다.
	    			        }
	    			    }else if( idfObj.getString("u_pr_code").equals("") && (idfObj.getString("u_fol_type").equals("RFO") || idfObj.getString("u_fol_type").equals("PFO") || dto.getTargetGubun().equals("DPC"))) {
	 	                    delHapFolderAuthBase(userSession , idfSession, keyStr);
	 	                    idfObj.destroy();  //이동하려는 레벨에 같은 폴더이름이 있으면 기존 폴더정보는 없애면서 auth_base정보도 없애야 한다.
	    			    }
	    			}
	    		}

	    	}

    	}catch (Exception e) {
		    e.printStackTrace();
		    return e.getMessage();
        }
		return "";
      }
    

    /** 폴더/파일 삭제처리
     * jjg
     * **/
    @Override
    public String deleteFolderAndFiles(UserSession userSession, IDfSession idfSess, DPath dto, Map<String, Map<String, List<DPath>>>  folMapTot, boolean isMobile) throws Exception {
		String rtnMsg=""; 
		//다른 프로젝트로 이동복사하는 경우에 대한 처리....
		//IDfSession idfSession = idfSess != null ? idfSess : this.getIdfSession(userSession);
		IDfSession idfSession = idfSess != null ? idfSess :DCTMUtils.getAdminSession();
		
		initMapInfo();
		
	    String sOwnSrDeptOrgId = gwDeptService.selectOrgIdByCabinetcode( dto.getSrcCabinetcode());
		String sOwnTgDeptOrgId = gwDeptService.selectOrgIdByCabinetcode( dto.getSrcCabinetcode());

		String s_CabinetCode = dto.getSrcCabinetcode();
		String sTgOrgId = gwDeptService.selectOrgIdByCabinetcode( s_CabinetCode);
		dto.setTgOrgId(sTgOrgId);
		HamInfoResult hamInfo = authService.selectDeptHamInfo(dto.getTgOrgId()).orElse(
	              authService.selectHamInfo(dto.getTgOrgId()).orElseThrow(() -> new NotFoundException(CheckAuthParam.class,  dto.getTgOrgId() )));
		dto.setOwnSrDeptOrgId(sOwnSrDeptOrgId);
		dto.setOwnTgDeptOrgId(sOwnTgDeptOrgId);
		dto.setHamInfo(hamInfo);
		
		dto.setReqDelStr("");
		dto.setReqDelCnt(0);//삭제건수 +1

		dto.setReCycleStr("");
		dto.setReCycleCnt(0);//삭제건수 +1
		
		
		try {
		 	if (!idfSession.isTransactionActive()) {
		 	    idfSession.beginTrans();
		 	}
			if(folMapTot.size() > 0) {

	        	IDfPersistentObject idfTg_PObj=null;
				int iCnt=0;
			    for (Entry<String, Map<String, List<DPath>>> entry : folMapTot.entrySet()) {
			        Map<String, List<DPath>> folMap = folMapTot.get(entry.getKey());

					List<String> folList = new ArrayList<String>();
			        for (Map.Entry<String, List<DPath>> folEntry : folMap.entrySet()) {
				    	folList.add(folEntry.getKey());
				    }
			        
		        	String keyStr=entry.getKey();
		        	    if( !keyStr.substring(0,1).equals("P") && !keyStr.substring(0,1).equals("R")) {
			        		rtnMsg=deleteFolderAndFile(userSession, idfSession, dto, folList, folMap, isMobile, keyStr, iCnt);
			        	}else{
			        		if(keyStr.substring(0,1).equals("P")) {  //프로젝트 코드 
			        			dto.setPrCode(keyStr);
			        			dto.setPrType("P");
				        		keyStr=keyStr.substring(1);
			        			rtnMsg=deleteFolderAndFile(userSession, idfSession, dto, folList, folMap, isMobile, keyStr, iCnt);
			        		}else if(keyStr.substring(0,1).equals("R")) { //연구투자 코드
			        			dto.setPrCode(keyStr);
			        			dto.setPrType("R");
				        		keyStr=keyStr.substring(1);
			        			rtnMsg=deleteFolderAndFile(userSession, idfSession, dto, folList, folMap, isMobile,keyStr, iCnt);
			        		}
			        	}
			        	iCnt++;
			        //}
			    }
			}else {
				List<String> folList = new ArrayList<String>();
				Map<String, List<DPath>> folMap = new HashMap<String, List<DPath>>();
   			    rtnMsg=deleteFolderAndFile(userSession, idfSession, dto, folList, folMap, isMobile,"6", 0);
			}
  		    idfSession.commitTrans();

		} catch (Exception e) {
		    e.printStackTrace();
		    rtnMsg=e.getMessage();
        } finally {
		    if (idfSession != null) {
		      if (idfSession.isTransactionActive()) {
		        idfSession.abortTrans();
		      }
		      if (idfSession.isConnected()) {
		    	  idfSession.disconnect();
		      }
		    }
		 }
		return "";//rtnMsg;
		
    }
    /** 폴더/파일 삭제처리
     * jjg
     * **/
    @Override
    public String deleteFolderAndFile(UserSession userSession, IDfSession idfSession, DPath dto, List<String> folList, Map<String, List<DPath>>  folMap, boolean isMobile, String keyStr, int iCnt) throws Exception {

  		 	final String userId = userSession.getUser().getUserId();
  		 	
  		    //folList.forEach(System.out::println); //대상 폴더 리스트 (정렬 확인)
  		    Collections.sort(folList, new uFolderAscending());
  		    //folList.forEach(System.out::println); //대상 폴더 리스트 (정렬 확인)

  		 	/***************************************************************************
  		     *  keyStr 1) 6(파일들만 선택된 경우)
  		     *         2) 첫자리가 p(프로젝트코드), r(연구과제코드)인 경우로서 프로젝트나 연구과제를 삭제대상으로 선택했는데 그 루트에 파일만 있는 경우
  		     *            하위에 폴더가 없는 경우 아래쪽  folList값이 비어있음.
  		     ***************************************************************************
				sFolArray : 선택한 폴더들
			    sFilArray : 선택한 파일들
  		                 
  		        folList   : 선택한 폴더를 포함한 '상위폴더' 리스트
  		        folBList  : '상위폴더' 바로 하위에 있는 폴더(FOL) 나 파일(DOC)
  			**************************************************************************/
			String authExclusive=null;
			
			boolean bPjtDel=false;
			boolean bRscDel=false;
			boolean bFolDel=false;
			boolean bFilDel=false;
  		    
			Map<String, String> dObjMap = new HashMap<String,String>();

			
			if(iCnt==0) { //1. 처음 1회때 처리함 (파일만 선택했건, 폴더랑 파일이 선택되었건, 폴더,파일,프로젝트,연구과제 모두 선택되었건 1회에 선택된 파일들 처리를 마무리한다
				List<String> PjtList = dto.getSourcePjts(); 
				
                if(PjtList.size() > 0) {  //프로젝트
                	
                	for(int i=0 ; i < PjtList.size(); i++) {
                		if(PjtList.get(i).equals("")) continue;
                	    Optional<Project> pjtOpt = projectDao.selectOneByUPjtCode(PjtList.get(i));            	
                	    if(pjtOpt.isPresent()) {
                	    	IDfPersistentObject dObj = (IDfPersistentObject)idfSession.getObject(new DfId( pjtOpt.get().getRObjectId()));
                	        boolean b_IsSubNoAuth=false;
                	        if(null !=dObj) {
                	        	List<DPath>  NAChkList = pathDao.selectNAList(null, PjtList.get(i),userId, authExclusive, " ", null, "D"); // 하위(전체)에 권한 (없는) 폴더나 문서가 있는지 체크
                        	    b_IsSubNoAuth	= NAChkList.size() > 0?true:false;
                	        }
                	        if(!b_IsSubNoAuth) {
                	        	bPjtDel=DeleteFolObject(idfSession, dObj, userId, pjtOpt.get().getRObjectId(), "D", dto);// D(개별 삭제), P(대상의 상위가 삭제 됨으로 인한 삭제)
                	        	if(bFolDel)
                	        	    dObjMap.put( PjtList.get(i), "D");
                	        }
                	    }
                	}
                	dto.setSourceGubun("PJC");
    			}

				List<String> RscsList = dto.getSourceRscs(); 
                if(RscsList.size() > 0) {  //프로젝트
                	for(int i=0 ; i < RscsList.size(); i++) {
                		if(RscsList.get(i).equals("")) continue;
                		Optional<Research> researchOpt = researchDao.selectOneByURschCode(RscsList.get(i));            	
                	    if(researchOpt.isPresent()) {
                	    	IDfPersistentObject dObj = (IDfPersistentObject)idfSession.getObject(new DfId( researchOpt.get().getRObjectId()));
                	        boolean b_IsSubNoAuth=false;
                	        if(null !=dObj) {
                	        	List<DPath>  NAChkList = pathDao.selectNAList(null, RscsList.get(i), userId, authExclusive, " ", null, "D"); // 하위(전체)에 권한 (없는) 폴더나 문서가 있는지 체크
                        	    b_IsSubNoAuth	= NAChkList.size() > 0?true:false;
                	        }
                	        if(!b_IsSubNoAuth) {
                	        	bRscDel=DeleteFolObject(idfSession, dObj, userId, researchOpt.get().getRObjectId(), "D", dto);// D(개별 삭제), P(대상의 상위가 삭제 됨으로 인한 삭제)
                	        	if(bFolDel)
                	        	    dObjMap.put( RscsList.get(i), "D");                	        	
                	        }
                	    }
                	}
                	dto.setSourceGubun("RSC");
    			}
                
				List<String> sFolArray = dto.getSourceFolders(); 
				
                if(sFolArray.size() > 0) {
                    	for(int i=0 ; i < sFolArray.size(); i++) {
                    		if(sFolArray.get(i).equals("")) continue;
                    		String ps_FolId= sFolArray.get(i);
                	        IDfPersistentObject dObj = (IDfPersistentObject)idfSession.getObject(new DfId(sFolArray.get(i)));

                	        boolean b_IsSubNoAuth=false;
                	        boolean b_IsSubLockDoc=false;
                	        if(null !=dObj) {
                        	    String ls_PrCode= dObj.getString("u_pr_code");
                        	    if(null != ls_PrCode) {
                        	        if(ls_PrCode.equals("") || ls_PrCode.equals(" ")) {
                        	    	    ls_PrCode=null;
                        	        }
                        	    }
                	        	List<DPath>  NAChkList = pathDao.selectNAList(ps_FolId, ls_PrCode,userId, authExclusive, " ", null, "D"); // 하위(전체)에 권한 (없는) 폴더나 문서가 있는지 체크
                        	    b_IsSubNoAuth	= NAChkList.size() > 0?true:false;
                        	    
                        	    List<DPath>  LDChkList = pathDao.selectLDList(ps_FolId, null);     // (하위에)잠긴(편집중인) 문서가 있는지 체크
                        	    b_IsSubLockDoc  = LDChkList.size() > 0?true:false;
                        	    
                	        }
                	        if(!b_IsSubNoAuth && !b_IsSubLockDoc) {  //권한없는 문서도 없고, 편집중인 문서(pass)도 없으면 폴더가 휴지통에 보이도록 D 로 삭제
           					    boolean b_Auth = authService.checkFolderAuth(ps_FolId, userId, "D");//GrantedLevels.WRITE.getLabel());
           					    if(!b_Auth &&  (!dObj.getString("u_fol_type").equals("PCL") && !dObj.getString("u_fol_type").equals("RCL"))) { //분류함이 아닌 경우
            					    System.out.println( "'"+ dObj.getString("u_fol_name")+"' 는 삭제권한이 없는 폴더입니다");
           					    }else {
	                	        	bFolDel=DeleteFolObject(idfSession, dObj, userId, ps_FolId, "D", dto);// D(개별 삭제), P(대상의 상위가 삭제 됨으로 인한 삭제)
	                	        	if(bFolDel)
	                	        	    dObjMap.put( ps_FolId, "D");
           					    }
                	        }
                    	}
    			}
                
				List<String> sFilArray=Arrays.asList("");
			    sFilArray = dto.getSourceFiles();    //edms_doc      에 존재하는 대상 파일들
			    
				if(sFilArray.size() > 0 ) {
		  			for(int c=0; c < sFilArray.size(); c++) {
		  				if(sFilArray.get(c).equals("")) continue;
		  				String ls_RObjectId 	= DCTMUtils.getCurrentObjectID(idfSession,sFilArray.get(c)); //문서의 최신버전 r_object_id를 가져온다.
		  				
						//List<DPath> authList = pathDao.selectDocAuthCheck(ls_RObjectId, userId,  7); // secLevel 권한이상
						
						if(ls_RObjectId.equals("")) {
						     System.out.println("문서 최신버전이 변경됨[Documentum]");
						     continue;
						}
						IDfSession idfUsrSession = this.getIdfSession(userSession);
						try {
							IDfDocument idfDocObj = (IDfDocument)idfUsrSession.getObject(new DfId(ls_RObjectId));
							if(!idfDocObj.getString("u_delete_status").trim().equals("")) continue; 
							//System.out.println(ls_RObjectId+"==1=="+ idfDocObj.getString("u_delete_status"));
							
							int     iPermit		= idfDocObj.getPermit();    // 보유권한
							boolean bIsCheckout	= idfDocObj.isCheckedOut();	// 편집중상태확인
	
			        	    if(bIsCheckout) {
							     System.out.println("편집중 문서 [" + ls_RObjectId +"]");
		        	    	    continue;
			        	    }
					      // 허용된 문서인지 확인
							//if (authList.size() < 1) {
			        	    if( iPermit < (idfDocObj.getString("u_doc_status").equals("L")?7:3 )) { 
							   ;
							   //System.out.println("#권한없는문서#" + idfObj.getString("object_name"));
							   //권한이 없는 문서는 삭제하지 않고 pass
							}else {
		  			            bFilDel=DeleteDocObject(userSession, idfSession, userId, ls_RObjectId, "D", dto);  // D(개별 삭제), P(대상의 상위가 삭제 됨으로 인한 삭제)
		  			        
		  			            dObjMap.put( ls_RObjectId, "D");
		  			           String lsJobGubun="";
							   
		  			           IDfDocument idfObj = (IDfDocument)idfSession.getObject(new DfId(ls_RObjectId));
					           idfObj.setTime  ("u_recycle_date"	, new DfTime()); //휴지통으로 삭제한 일자 
					           idfObj.save();
							}
						}catch(Exception e){
							e.printStackTrace();
						}finally {
		    		      if (idfUsrSession.isConnected()) {
		    		    	  idfUsrSession.disconnect();
			    		   }
						}
		  			}
				}
			}
			
			boolean isLog=false;
			if(!dto.getSourceGubun().substring(0,2).equals("DI")) {  //중요문서함이 아닌 경우
				isLog=true;
			}
			List<DPath>  NAChkList=null;
			/**파일만 선택된 경우가 아닐 때 , **/
			
			
			boolean isDflag=false;
			String ls_DelStr="P";//대상의 상위가 삭제 됨으로 인한 삭제
			if(!bPjtDel &&  !bRscDel && !bFolDel) {
				ls_DelStr="D";
				isDflag=true;
			}
			
  		    if(!keyStr.equals("6")) {
				
	  			boolean bFileDel=true;  //파일처리중 오류발생 시 break 용도
	    		IDfPersistentObject	idfDObj=null;
	    		IDfPersistentObject	idfUpObj=null;
	    		
	    		System.out.println("size="+ folList.size() );
	    		
	  			if(folList.size() ==1 && (folList.get(0).equals("") || folList.get(0).equals(" "))) {
	  				List<DPath> folAList = folMap.get(folList.get(0));
	  				for(int ri=0; ri < folAList.size(); ri++) {
	  					folList.add(folAList.get(ri).getRObjectId());
	  				}
	  			}
	  			
		    	for(int i=0; i < folList.size(); i++) {
		    		String ls_DelStrSub =ls_DelStr;
		    				
		    		String uFolId = folList.get(i);//폴더 ID
		    		if(uFolId.equals("") || uFolId.equals(" ")) continue;
		    		
		    		if (DfId.isObjectId(uFolId)) {  //유효한 폴더id인 경우이다.

		    			idfDObj	= idfSession.getObject(new DfId(uFolId));
		    			if(null !=idfDObj) {
		    				//System.out.println(idfDObj.getType().getName());
		    				
		    				String lsUUpFolID = idfDObj.getType().getName().equals("edms_folder")?idfDObj.getString("u_up_fol_id"):idfDObj.getString("u_fol_id");
		    				
			    		    if(!lsUUpFolID.equals(" ") && !lsUUpFolID.equals("") && ls_DelStr.equals("D")) {  //상위폴더ID값이 비어있지 않은 경우, (프로젝트나 부서함 루트폴더가 아님), 구분자가 D(개별삭제인 경우)

			    		    	if (DfId.isObjectId(idfDObj.getString("u_up_fol_id"))) {
			    		            idfUpObj	= idfSession.getObject(new DfId( idfDObj.getString("u_up_fol_id")));
			    		            if(null !=idfUpObj) {//상위폴더 Object를 잘 가져온 경우

				    		            if( null != idfUpObj.getString("u_delete_status") && !idfUpObj.getString("u_delete_status").equals("") && !idfUpObj.getString("u_delete_status").equals(" ")) {
			    		            	    if( null !=dObjMap.get(idfUpObj.getString("u_up_fol_id")) && dObjMap.get(idfUpObj.getString("u_up_fol_id")).equals("D") ) ls_DelStrSub = "P";  //상위폴더가 개별삭제건이면 하위폴더는 P로 지정한다
			    		                    else if( null !=dObjMap.get(idfUpObj.getString("u_up_fol_id")) &&dObjMap.get(idfUpObj.getString("u_up_fol_id")).equals("P") ) ls_DelStrSub = "P";  //상위폴더가 개별삭제건이 아니면 하위폴더는 P로 지정한다
				    		            }
			    		            }else{
			    		            	throw new RuntimeException(idfDObj.getString("u_up_fol_id") +" 폴더를 찾을수 없습니다.");
			    		            }			    		            	
			    		    	}
			    		    }
			    		    if( null ==dObjMap.get(uFolId)) {  //이번 처리에서 삭제된 적이 없는 폴더이다
			  		 	    	NAChkList = pathDao.selectNAList(uFolId, null ,userId, authExclusive, " ", null, "D"); // 하위(전체)에 권한 (없는) 폴더나 문서가 있는지 체크
                        	    List<DPath>  LDChkList = pathDao.selectLDList(uFolId, null);                            // (하위에)잠긴(편집중인) 문서가 있는지 체크
			  		 	        if( NAChkList.size() == 0 && LDChkList.size() == 0) {
		  		 	                if(isDflag) {
		  		 	                	boolean b_Auth = authService.checkFolderAuth(uFolId, userId, "D");//GrantedLevels.WRITE.getLabel());
		  		 	                	if(b_Auth) {
					                        bFileDel=DeleteFolObject(idfSession, idfDObj, userId, uFolId,  ls_DelStrSub , dto);  //상위폴더의 삭제구분자가 지정되지 않은 경우
					                        dObjMap.put( uFolId, ls_DelStrSub);
					                        ls_DelStrSub="P";
		  		 	                	}else {
		  		 	                		System.out.println( "'"+ idfDObj.getString("u_fol_name")+"' 는 삭제권한이 없거나 편집중인 문서가 있는 폴더입니다");
		  		 	                	}
		  		 	                }else {    	  		 	        	
		  		 	                	boolean b_Auth = authService.checkFolderAuth(uFolId, userId, "D");//GrantedLevels.WRITE.getLabel());
		  		 	                	if(b_Auth) {
			  		 	            	    bFileDel=DeleteFolObject(idfSession, idfDObj, userId, uFolId,  "P" , dto);  //D(개별 삭제), P(대상의 상위가 삭제 됨으로 인한 삭제)
			  		 	            	    dObjMap.put( uFolId, "P");
					                        ls_DelStrSub="P";
		  		 	                	}else {
		  		 	                		System.out.println( "'"+ idfDObj.getString("u_fol_name")+"' 는 삭제권한이 없거나 편집중인 문서가 있는 폴더입니다");
		  		 	                	}
		  		 	                }
		   	  		 	       }else {
		   	  		 	        	//권한없거나편집중인 문서가 있으면 삭제하지 않는다. 하위폴더나 문서 삭제시에 권한있는 건이 있으면, 개별삭제건이 나올 수 있다. 
		   	  		 	       }
			    		    }else {
			    		    	//
			    		    	System.out.println(uFolId +" 선행작업에서 삭제처리됨 ");
			    		    }
		    			}else {
		    				continue;//유효하지 않은 폴더 
		    			}
		    		}else {
		    			continue;  //유효하지 않은 폴더ID이면 skip 한다
		    		}

  		    		if(!bFileDel) break; //폴더삭제가 정상적이지 않았으면 중단한다
  		    		
  		    		List<DPath> folBList = folMap.get(uFolId);
  		    		if(null !=folBList) {
	  		    		for(int j=0; j < folBList.size(); j++) {
	  		    			DPath dData = folBList.get(j);
	  		    			
	  		    			if(dData.getListType().equals("FOL")) {
	  		    				if( null != dObjMap.get(dData.getRObjectId())) continue;  //삭제처리한 폴더이면 pass
	  		    				if (!DfId.isObjectId(dData.getRObjectId())) {
	  		    					throw new RuntimeException(idfDObj.getString("u_up_fol_id") +" 폴더를 찾을수 없습니다.");
	  		    					//continue;  //유효하지 않은 ID 이면 pass
	  		    				}
	
	  		    	  		 	idfDObj	= idfSession.getObject(new DfId(dData.getRObjectId()));
	  		    	  		    if(!idfDObj.getString("u_up_fol_id").equals(" ") && !idfDObj.getString("u_up_fol_id").equals("")) {
	  		    	  		 	    idfUpObj	= idfSession.getObject(new DfId( idfDObj.getString("u_up_fol_id")));
	  		    	  		    }
	           	        	    
	  		    	  		    if( null != idfUpObj) {
	  		    	  		 	    if(!idfDObj.getString("u_delete_status").equals("P")  && !idfDObj.getString("u_delete_status").equals("D")) {
	  		    	  		 		  if(null !=dObjMap.get(idfUpObj.getString("u_up_fol_id")))
	  		    	  		 		      if( dObjMap.get(idfUpObj.getString("u_up_fol_id")).equals("D") ) ls_DelStrSub = "P";  //상위폴더가 개별삭제건이면 하위폴더는 P로 지정한다
	  		    	  		 		}
	  		    	  		 	    NAChkList = pathDao.selectNAList(uFolId, null ,userId, authExclusive, " ", null, "D"); // 하위(전체)에 권한 (없는) 폴더나 문서가 있는지 체크
	                        	    List<DPath>  LDChkList = pathDao.selectLDList(uFolId, null);                            // (하위에)잠긴(편집중인) 문서가 있는지 체크
				  		 	        if( NAChkList.size() == 0 && LDChkList.size() == 0) {
		  		 	                	boolean b_Auth = authService.checkFolderAuth(uFolId, userId, "D");//GrantedLevels.WRITE.getLabel());
		  		 	                	if(b_Auth) {
	  		    				            bFileDel=DeleteFolObject(idfSession, idfDObj, userId, dData.getRObjectId(), ls_DelStrSub, dto);  //D(개별 삭제), P(대상의 상위가 삭제 됨으로 인한 삭제)
	  		    				            dObjMap.put( dData.getRObjectId(), ls_DelStrSub);
		  		 	                	}else {
		  		 	                		System.out.println( "'"+ idfDObj.getString("u_fol_name")+"' 는 삭제권한이 없거나 편집중인 문서가 있는 폴더입니다");
		  		 	                	}
	  		    	  		 	    }
	  		    	  		 	}
	                            
			    			}else if(dData.getListType().equals("LNK")) { 
			    				idfDObj = idfSession.getObject(new DfId(dData.getRObjectId()));
			    				String objTypeNm= idfDObj.getType().getName();
			    				if(!objTypeNm.equals("edms_doc") && !objTypeNm.equals("edms_doc_imp"))
			    				    idfDObj.destroy();
			    				else {
			    					IDfCollection		idf_Col1		= null;
			    	    			String s_Dql1 = "select * from edms_doc_link " +
			    							" where u_doc_key = '" + dData.getRObjectId() + "' " ;
			    				    idf_Col1 = DCTMUtils.getCollectionByDQL(idfSession, s_Dql1, DfQuery.DF_QUERY);
			    				    if(idf_Col1 != null) idf_Col1.close();
			    				    
			    	  				//링크 삭제이력
  									IDfPersistentObject idf_LinkDel = idfSession.newObject("edms_doc_link_del");
  									idf_LinkDel.setString("u_doc_id",           idf_Col1.getString("u_doc_id")  ); //r_object_id, document.getChronicleId()
  									idf_LinkDel.setString("u_doc_key",          idf_Col1.getString("u_doc_key")  ); //문서번호
  									idf_LinkDel.setString("u_cabinet_code",     idf_Col1.getString("u_cabinet_code")); //문서함코드
  									idf_LinkDel.setString("u_fol_id",           idf_Col1.getString("u_fol_id")  ); //변경전 폴더 id, saveWfList이후에  전자결재폴더id로 바꿔서 edms_doc에 저장( link테이블에 있는 u_fol_id가 이전폴더id가 됨 )
  									idf_LinkDel.setString("u_link_type",        idf_Col1.getString("u_link_type")  ); //링크종류', SET COMMENT_TEXT='W:결재')
  									idf_LinkDel.setString("u_create_user",      dto.getReqUser()); //삭제 작업자
  									idf_LinkDel.setString("u_create_date",   (new DfTime()).toString());
  									idf_LinkDel.save();

			    				    IDfCollection		idf_Col		= null;
			    	  				String s_Dql = "delete edms_doc_link object " +
			    	  						" where u_cabinet_code='"+dto.getSrcCabinetcode()+"' and u_doc_key = '" + dData.getRObjectId() + "' " ;
	
			    	  				idf_Col = DCTMUtils.getCollectionByDQL(idfSession, s_Dql, DfQuery.DF_QUERY);
			    	  				if(idf_Col != null) idf_Col.close();
  									
			    				}		    					
			    					
	
	  		    			}else { //문서("DOC" 인 경우)
	  		    				String ls_RobjectId 	= DCTMUtils.getCurrentObjectID(idfSession,dData.getRObjectId()); //문서의 최신버전 r_object_id를 가져온다.
								if(ls_RobjectId.equals("")) {
								     System.out.println("문서 최신버전이 변경됨[Documentum]");
								     continue;
								}
								IDfSession idfUsrSession = this.getIdfSession(userSession);
								try {
									IDfDocument idfObj = (IDfDocument)idfUsrSession.getObject(new DfId(ls_RobjectId));
									
									if(!idfObj.getString("u_delete_status").equals("") ) continue;
									
									int     iPermit		= idfObj.getPermit();    // 보유권한
									boolean bIsCheckout	= idfObj.isCheckedOut();	// 편집중상태확인
								    
					        	    if(bIsCheckout) {
									     System.out.println("편집중 문서 [" + ls_RobjectId +"]");
				        	    	    continue;
					        	    }
							      // 허용된 문서인지 확인
									//if (authList.size() < 1) {
									if ( iPermit <  (idfObj.getString("u_doc_status").equals("L")?7:3 )) {
									   ;
									//List<DPath> authList = pathDao.selectDocAuthCheck(ls_RobjectId, userId,  7); // secLevel 권한이상
								      // 허용된 문서인지 확인
									//if (authList.size() < 1) {
									   ;
									   //System.out.println("#권한없는문서#" + idfObj.getString("object_name"));
									   //권한이 없는 문서는 삭제하지 않고 pass
									}else {
									    if(idfObj.getString("u_fol_id").equals(" ") && idfObj.getString("u_fol_id").equals(" ")) {
									    	bFileDel=DeleteDocObject(userSession, idfSession, userId, ls_RobjectId,  ls_DelStr , dto);//D(개별 삭제), P(대상의 상위가 삭제 됨으로 인한 삭제)
									    }else {
		
									    	if( null !=dObjMap.get(idfObj.getString("u_fol_id"))) {
		 		    				            bFileDel=DeleteDocObject(userSession, idfSession, userId, ls_RobjectId, "P", dto);//D(개별 삭제), P(대상의 상위가 삭제 됨으로 인한 삭제)
									    	}else {
									    		bFileDel=DeleteDocObject(userSession, idfSession, userId, ls_RobjectId,  ls_DelStrSub, dto);//D(개별 삭제), P(대상의 상위가 삭제 됨으로 인한 삭제)
									    	}
									        //idfObj.setTime  ("u_recycle_date"	, new DfTime()); //휴지통으로 삭제한 일자 
									        //idfObj.save();
									    }
									}
								}catch(Exception e) {
									e.printStackTrace();
								}finally {
				    		      if (idfUsrSession.isConnected()) {
				    		    	  idfUsrSession.disconnect();
					    		   }
								}
	
	  		    			}
	  		    		}
  		    		}
  		    	}
  		  }
  		  if(!dto.getReqDelStr().equals("") && dto.getReqDelCnt() > 1) {
  		      dto.setReqDelStr( dto.getReqDelStr()+" 외 " + (dto.getReqDelCnt()-1) + " 건") ;
  		      
  		  }else if(!dto.getReqDelStr().equals("") && dto.getReqDelCnt() ==1) {
  			  dto.setReqDelStr( dto.getReqDelStr());
  			  
  		  }
		  /* 폐기요청 등록 건 **/
		  if(dto.getUAlarmYn().equals("Y") && !dto.getReqDelStr().equals("")) {//알람 발송
			  IDfPersistentObject idf_NotiObj = idfSession.newObject("edms_noti");
			  idf_NotiObj.setString("u_msg_type", "DR");
			  idf_NotiObj.setString("u_sender_id"  , dto.getReqUser());
			  idf_NotiObj.setString("u_receiver_id" , dto.getUApprover());
			  idf_NotiObj.setString("u_msg" , "'" + dto.getReqDelStr() + "'" + " 문서의 폐기요청이 등록되었습니다.");
			  
			  if( dto.getReqDelCnt() > 1) {
			      idf_NotiObj.setString("u_obj_id"       , "");					
			      idf_NotiObj.setString("u_performer_id" , "");
			  }else {
			      idf_NotiObj.setString("u_obj_id"       , dto.getReqDelObjId());					
			      idf_NotiObj.setString("u_performer_id" , dto.getReqDelDocKey());
			  }
			  idf_NotiObj.setTime("u_sent_date"    , new DfTime());
					
			  idf_NotiObj.setString("u_action_need_yn"    , dto.getReqDelCnt() > 1?"N":"Y"); //승인,반려버튼을 보일지 말지 (단건이 아닌 경우  바로가기 버튼만 보이도록 한다) 
			  idf_NotiObj.setInt("u_group_key", 1);
				
			  idf_NotiObj.save();
		  }
  		  
  		  if(dto.getUEmailYn().equals("Y") && !dto.getReqDelStr().equals("")) {//이메일 발송대상 여부
  			  if(null==dto.getUApproverEmail() && dto.getUApproverEmail().equals("")) {
  				  System.out.println( dto.getUApprover()==null?"-null-":dto.getUApprover()+" : 결재자 E-Mail이 존재하지 않습니다");
  			  }else {
			      List<String> mgrsEmail = new ArrayList<>();
		          mgrsEmail.add( dto.getUApproverEmail() );
		        
	              StringBuffer content = new StringBuffer();       
	              content.append("<html> ");   
	              content.append("<body>");           
	              content.append("                                 <font face='굴림' size=3>      &nbsp; D'Box에서 알려드립니다.</font>");           
	              content.append("<br><br>                         <font face='굴림' size=3>      &nbsp; "+dto.getReqDelStr()+" 문서의 폐기요청이 등록되었습니다.</font>");
	              content.append("<br><br>                         <font face='굴림' size=3>     <a href='"+  dboxUrl  +"/#/manager/dept/document-status?tab=2'>승인화면 바로가기</a></font>");
	              content.append("<br><br>                         <font face='굴림' size=3>      &nbsp; 감사합니다.</font>"); 
	              content.append(" </body></html>");          
	              notificationService.sendMail(userSession.getUser().getEmail(), mgrsEmail, "'" + dto.getReqDelStr() + "'" + " 문서의 폐기요청이 등록되었습니다.", new String(content));            
  			  }
          }  		  
		  if(dto.getUMmsYn().equals("Y") && !dto.getReqDelStr().equals("")) {//MMS  발송대상 여부
				if( null !=dto.getUApproverPhoneNo() && !dto.getUApproverPhoneNo().equals("")) {
					
	                String callphone=dto.getUApproverPhoneNo().replace("-", "");
	                
					LocalDate now = LocalDate.now();
				    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
				    String formatedNow = now.format(formatter);
				    LocalTime time = LocalTime.now();
				    DateTimeFormatter timeformat = DateTimeFormatter.ofPattern("HHmmss");
				    String timeformatNow = time.format(timeformat);
				    
				      KakaoDetail kakaoData = KakaoDetail.builder()
				                              .usercode("dbox")
				                              .biztype("at")
				                              .yellowidKey("92fb64a3500ed9b9d0e0fd2a2638d2b6e43c80ae")
				                              .reqname("suerM")
				                              .reqphone("15884640")  //'#{문서명.확장자 외 *건} 문서의 폐기요청이 등록되었습니다
				                              .msg(dto.getReqDelStr()+" 문서의 폐기요청이 등록되었습니다.")
				                              .reqtime("00000000000000")
				                              .result("0")
				                              .kind("T")
				                              .resend("N")
				                              .templatecode("dbox_alarm_004")
				                              .callphone(callphone)
				                              .deptcode("T9-YC2-2V")
				                              .intime(formatedNow+timeformatNow)
				                              .callname( dto.getUApproverNm())
				                              .build();
				      
				       kakaoDao.insertKakao(kakaoData);	
			    }    				
		  }  		    
  		  return "";
      }
    
    
    /** 폴더/파일 삭제처리
     * jjg
     * **/
    @Override
    public String transFolderAndFiles(UserSession userSession, IDfSession idfSess, DPath dto, Map<String, Map<String, List<DPath>>>  folMapTot, boolean isMobile) throws Exception {
       	//폴더 생성
    		String rtnMsg=""; 
    		//다른 프로젝트로 이동복사하는 경우에 대한 처리....
    		IDfSession idfSession = idfSess != null ? idfSess : this.getIdfSession(userSession);
    		
    		initMapInfo();
    		
    		try {
    		 	if (!idfSession.isTransactionActive()) {
    		 	    idfSession.beginTrans();
    		 	}
    			if(folMapTot.size() > 0) {
    				int iCnt=0;
    				IDfPersistentObject idfTg_PObj=null;
    			    for (Entry<String, Map<String, List<DPath>>> entry : folMapTot.entrySet()) {
    			        Map<String, List<DPath>> folMap = folMapTot.get(entry.getKey());

    					List<String> folList = new ArrayList<String>();
    			        for (Map.Entry<String, List<DPath>> folEntry : folMap.entrySet()) {
    				    	folList.add(folEntry.getKey());
    				    }
    			        
    			        String keyStr=entry.getKey();

		        	    if( !keyStr.substring(0,1).equals("P") && !keyStr.substring(0,1).equals("R")) {
		        	    	
			        		rtnMsg=transFolderAndFile(userSession, idfSession, dto, folList, folMap, isMobile, keyStr, iCnt);
			        	}else{
			        		if(keyStr.substring(0,1).equals("P")) {  //프로젝트 코드 
			        			dto.setPrCode(keyStr);
			        			dto.setPrType("P");
				        		keyStr=keyStr.substring(1);
			        			rtnMsg=transFolderAndFile(userSession, idfSession, dto, folList, folMap, isMobile, keyStr, iCnt);
			        		}else if(keyStr.substring(0,1).equals("R")) { //연구투자 코드
			        			dto.setPrCode(keyStr);
			        			dto.setPrType("R");
				        		keyStr=keyStr.substring(1);
			        			rtnMsg=transFolderAndFile(userSession, idfSession, dto, folList, folMap, isMobile,keyStr, iCnt);
			        		}
			        	}
			        	iCnt++;
			        //}
    			    }
    			}else {
    				List<String> folList = new ArrayList<String>();
    				Map<String, List<DPath>> folMap = new HashMap<String, List<DPath>>();
       			    rtnMsg=transFolderAndFile(userSession, idfSession, dto, folList, folMap, isMobile,"", 0);
    			}
      		    idfSession.commitTrans();

    		} catch (Exception e) {
    		    e.printStackTrace();
            } finally {
    		    if (idfSession != null) {
    		      if (idfSession.isTransactionActive()) {
    		        idfSession.abortTrans();
    		      }
    		      if (idfSession.isConnected()) {
    		    	  idfSession.disconnect();
    		      }
    		    }
    		 }
    		return rtnMsg;
    }    
      /** 폴더/파일 이관 처리
       * jjg
       * **/
      @Override
      public String transFolderAndFile(UserSession userSession, IDfSession idfSession, DPath dto, List<String> folList, Map<String, List<DPath>>  folMap, boolean isMobile, String keyStr, int iCnt) throws Exception
      {
    		String newFolderName = null;

    		IDfPersistentObject	idf_PObj	= null;
			IDfDocument			idf_Doc		= null;
			IDfCollection		idf_Col		= null;
		    String              resultStr="";

			String			s_ObjType	= "";
			boolean			b_Privacy	= false;
			
			String sourceGubun   = "";
			
			String returnMsg="";
    			  
		 	IDfDocument         idfDoc     = null;
		 	final String userId = userSession.getUser().getUserId();
		 	
		    folList.forEach(System.out::println); //대상 폴더 리스트 (정렬 확인)
		    Collections.sort(folList, new uFolderAscending());
		    folList.forEach(System.out::println); //대상 폴더 리스트 (정렬 확인)

		    
			List<String> sFolArray=Arrays.asList("");
			List<String> sFilArray=Arrays.asList("");
			
			if( iCnt < 1) {
			    sFolArray = dto.getSourceFolders();  //edms_folder   에서 관리되는 대상 폴더들 
			    sFilArray = dto.getSourceFiles();    //edms_doc      에 존재하는 대상 파일들
			}

			String s_SDeptName = pathDao.selectOrgNmbyCabinetCode( dto.getSrcCabinetcode());//송신측 부서명
			String s_RDeptName = pathDao.selectOrgNmbyCabinetCode( dto.getTgCabinetcode()); //수신측 부서명
			
			String	s_RcevFolId	= "";
            /***********************************************************************************************
             *  [이관함]폴더를 만듬 (프로젝트, 연구과제, 중요문서함, 일반부서함별 별도 생성) Start  ***************************
             ***********************************************************************************************/			
			if(( keyStr.substring(0,1).equals("p")      //ex) p00001  (프로젝트 코드 ) : edms_project -> PFO
			   || keyStr.substring(0,1).equals("r"))){  //ex) r00001 (연구과제 코드)  : edms_research -> RFO

				IDfPersistentObject	idfSc_PObj =idfSession.getObject(new DfId(keyStr));
				String objTypeNm= idfSc_PObj.getType().getName();
		    	sourceGubun = objTypeNm.equals("edms_project")?"PFO":"RFO";//소스쪽 프로젝트명, 연구과제명
		    	
		    	dto.setTargetFolType(sourceGubun);
		    	String s_PrCode="";
		    	String newPrName ="[이관프로젝트]"+s_SDeptName;
		    	if(keyStr.substring(0,1).equals("p")) {
		    		s_PrCode = projectService.createProject(userSession, ProjectCreateDto.builder().uPjtName(newPrName).uListOpenYn("Y").uFinishYn("N").uOwnDept(userSession.getUser().getOrgId()).uFolId(dto.getTgCabinetcode()).uSecLevel(dto.getTargetSecLevel()).uCreateUser(dto.getReqUser()).build(), idfSession);
		    	}else if(keyStr.substring(0,1).equals("r")) {
		    		s_PrCode = researchService.createResearch(userSession, ResearchCreateDto.builder().uRschName(newPrName).uListOpenYn("Y").uFinishYn("N").uOwnDept(userSession.getUser().getOrgId()).uFolId(dto.getTgCabinetcode()).uSecLevel(dto.getTargetSecLevel()).uCreateUser(dto.getReqUser()).build(), idfSession);
		    	}
                dto.setPrCode(s_PrCode);
		    	dto.setPrType(keyStr.substring(0,1).toUpperCase());

    			//받는 cabinet에 보내는 부서명으로 이관함 존재하는지 체크후 없으면 생성
    			//s_RcevFolId=CreateTransFolder(idfSession, dto.getSrcCabinetcode(), s_SDeptName, dto);
		    	
    			addMoveAuthBase( userSession, idfSession, s_RcevFolId, dto.getTgCabinetcode(), "F", dto, null); //새로만든 폴더에 권한 부여
				
			}else { //부서함(일반(DFO),중요문서함(DIF))
				if(!sFolArray.get(0).equals("")) {
				    IDfPersistentObject	idfSc_FObj =idfSession.getObject(new DfId(sFolArray.get(0)));
				    sourceGubun = idfSc_FObj.getString("u_fol_type");
				}else if(!sFilArray.get(0).equals("")) {
				    IDfPersistentObject	idfSc_DObj =idfSession.getObject(new DfId(sFilArray.get(0)));
			    	String objTypeNm= idfSc_DObj.getType().getName();
			    	sourceGubun = objTypeNm.equals("edms_doc")?"DFO":"DIF";//edms_doc이면 부서함폴더(DFO), edms_doc_imp이면 중요문서함폴더(DIF)
				}
                dto.setPrCode(" ");
		    	dto.setPrType(" ");
				dto.setTargetFolType(sourceGubun);
				
				s_RcevFolId=CreateTransFolder(idfSession, dto.getSrcCabinetcode(), s_SDeptName, dto);
			    //addMoveAuthBase( userSession, idfSession, s_RcevFolId, dto.getTgCabinetcode(), "F", dto, null); //새로만든 폴더에 권한 부여
			}
            /*******************************************************************************************************
            ****[이관함]폴더를 만듬 (프로젝트, 연구과제, 중요문서함, 일반부서함별 별도 생성) End **************************************  
            *******************************************************************************************************/
			
			if(null !=s_RcevFolId && s_RcevFolId !="") 
			{
				returnMsg= "수신 부서에 '이관함' 폴더 생성 실패 : " +
			                  "요청 ID   = " + keyStr + ", " + 
						      "요청자 ID  = " + dto.getReqUser() + ", " +
						      "발신부서 = " + s_SDeptName + "(" + dto.getSrcCabinetcode() + "), " +
						      "수신부서 = " + s_RDeptName + "(" + dto.getSrcCabinetcode() + ")\n";

				return returnMsg;
			}
			
			String  s_ProjectCode = dto.getPrCode();
			if(null ==s_ProjectCode || s_ProjectCode.equals("")) {
			    IDfPersistentObject idfTg_PObj = idfSession.getObject(new DfId(s_RcevFolId));
				
    			//개별 파일 이관
    			for(int c=0; c < sFilArray.size(); c++) {
    				if(sFilArray.get(c) !="") {
   				        String ls_RobjectId 	= DCTMUtils.getCurrentObjectID(idfSession, sFilArray.get(c)); //문서의 최신버전 r_object_id를 가져온다.
	        			if(ls_RobjectId.equals("")) {
		        		    throw new NotFoundException("문서 최신버전이 변경됨[Documentum]");
		        		}   				        
   				        String s_Dql = "edms_doc where r_object_id='" + ls_RobjectId + "'"; 
   				        if(sourceGubun.equals("DI")) {
   				        	s_Dql = "edms_doc_imp where r_object_id='" + ls_RobjectId + "'";  //중요문서함에서 찾을것
   				        }    				    
    				    idf_Doc = (IDfDocument)idfSession.getObjectByQualification(s_Dql);
    				    resultStr=TransDoc(idfSession, idf_Doc, s_RcevFolId, dto.getSrcCabinetcode(), dto);
    				    //addMoveAuthBase( userSession, idfSession, s_RcevFolId, dto.getTgCabinetcode(), "D", dto, null); //새로만든 폴더에 권한 부여
    			    }
    			}
    			
    			if(!resultStr.equals("")) return resultStr;
    			
    		 	/** 최상위 폴더( 개별삭제인지, 상위 삭제인지 구분하기 위해서 사용 */
    		 	Map<String, Integer> topFolMap = new HashMap<String,Integer>();
  			
    			for(int c=0; c < sFolArray.size(); c++) {
    				if(sFolArray.get(c) !="") {
    				    TransFol(idfSession, dto.getReqUser(), sFolArray.get(c), dto.getSrcCabinetcode(), dto.getSrcCabinetcode(), s_RcevFolId, dto);
    				    //addMoveAuthBase( userSession, idfSession, sFolArray.get(c), dto.getTgCabinetcode(), "F", dto, null); //새로만든 폴더에 권한 부여
    				    topFolMap.put(sFolArray.get(c), 1);
    			    }
    			}

    			for(int i=0; i < folList.size(); i++) {
		    		String uFolId = folList.get(i);//폴더 ID
		    		
		    		if(uFolId !="" ) {
		    			  int topFolCnt=-1;
		    			  if(topFolMap.get(uFolId) !=null) {
	  		    	           topFolCnt = topFolMap.get(uFolId);
		    		      }
	  		    		  if(topFolCnt > 0) {
	  		    			  continue;
	  		    		  } //최상위로 선택된 폴더들이 아닌 경우 처리함

	    		    	  List<DPath> folBList = folMap.get(uFolId);
	    		    		
	    		    	  for(int j=0; j < folBList.size(); j++) {

	    		    		    s_RcevFolId	= ""; //빈값을 줘야 edms_doc의 u_fol_id를 변경하지 않고 현재 그대로 사용
	
	    		    			DPath dData = folBList.get(j);
	    						String s_ObjId = dData.getRObjectId();
	    						
	    						idf_PObj = idfSession.getObject(new DfId(s_ObjId));
	    						String s_ObjIds = idf_PObj.getAllRepeatingStrings("u_obj_id", ",");
	    						String[] sa_ObjId = s_ObjIds.split("[,]");
	    						
	    		    			if(dData.getListType().equals("FOL")) {
	    		    				resultStr=TransFol(idfSession, dto.getReqUser(), s_ObjId, dto.getSrcCabinetcode(), dto.getSrcCabinetcode(), s_RcevFolId, dto);
	    		    				if(!resultStr.equals("")) return resultStr;
	    		    				//addMoveAuthBase( userSession, idfSession, s_ObjId, dto.getTgCabinetcode(), "F", dto, null); //새로만든 폴더에 권한 부여

	    		    			}else if(dData.getListType().equals("LNK")) {
	    							idf_PObj = idfSession.getObject(new DfId(s_ObjId));
	    		    			    idf_PObj.destroy();
                                    //링크파일은 삭제
	    		    			}else { //문서("DOC" 인 경우)
	    		    				//편집중 문서는 강제 해제 함
	   		    				    String ls_RobjectId 	= DCTMUtils.getCurrentObjectID(idfSession, s_ObjId); //문서의 최신버전 r_object_id를 가져온다.
	   		        			    if(ls_RobjectId.equals("")) {
	 		        			        throw new NotFoundException("문서 최신버전이 변경됨[Documentum]");
	 		        			    }	   		    				    
	    		    				String s_Dql = "edms_doc where r_object_id='" + ls_RobjectId + "'";
	    		    				idf_Doc = (IDfDocument)idfSession.getObjectByQualification(s_Dql);
	    		    				resultStr=TransDoc(idfSession, idf_Doc, s_RcevFolId, dto.getSrcCabinetcode(), dto);  //파일에 넘길때는 값이 비어있으므로, u_fol_id를 기존구조 그대로 사용함
	    		    				if(!resultStr.equals("")) return resultStr;
	    		    				//addMoveAuthBase( userSession, idfSession, idf_Doc.getChronicleId()+"", dto.getTgCabinetcode(), "D", dto, null); //새로만든 폴더에 권한 부여
	    		    			}		    			
	    		    		}
	    		    	}
	    		    }
			    }else {
			    	  //resultStr=TransDoc(idfSession, idf_Doc, s_RcevFolId, dto.getSrcCabinetcode(), dto);  //파일에 넘길때는 값이 비어있으므로, u_fol_id를 기존구조 그대로 사용함
				    	IDfCollection idf_Col_b=null;
						String s_Dql = "select * from edms_folder " +
									" where u_pr_code = '" + s_ProjectCode + "' " ;
	      				
						idf_Col_b = DCTMUtils.getCollectionByDQL(idfSession, s_Dql, DfQuery.DF_QUERY);
		  				while(idf_Col_b != null && idf_Col_b.next())
		  				{
		  					String s_ObjId = idf_Col.getString("r_object_id");
		  					TransFol(idfSession, dto.getReqUser(), s_ObjId, dto.getSrcCabinetcode(), dto.getSrcCabinetcode(), null, dto);
		  				    //addMoveAuthBase( userSession, idfSession, s_ObjId, dto.getTgCabinetcode(), "F", dto, null); //새로만든 폴더에 권한 부여
		  				}
	      				if(idf_Col_b != null) idf_Col_b.close();
	    			  
						s_Dql = "select * from edms_doc " +
								" where u_pr_code = '" + s_ProjectCode + "' " ;
					    idf_Col_b = DCTMUtils.getCollectionByDQL(idfSession, s_Dql, DfQuery.DF_QUERY);
	  				    while(idf_Col_b != null && idf_Col_b.next())
	  				    {
	  					    String s_ObjId = idf_Col.getString("u_doc_key");
	    				    String ls_RobjectId 	= DCTMUtils.getCurrentObjectID(idfSession, s_ObjId); //문서의 최신버전 r_object_id를 가져온다.
		        			if(ls_RobjectId.equals("")) {
			        		    throw new NotFoundException("문서 최신버전이 변경됨[Documentum]");
			        		}	    				    
	  					    resultStr=TransDoc(idfSession, idf_Doc, null, dto.getSrcCabinetcode(), dto);  //파일에 넘길때는 값이 비어있으므로, u_fol_id를 기존구조 그대로 사용함
	  					     //addMoveAuthBase( userSession, idfSession, idf_Doc.getChronicleId()+"", dto.getTgCabinetcode(), "D", dto, null); //새로만든 폴더에 권한 부여
	  				    }
      				    if(idf_Col_b != null) idf_Col_b.close();

						
   				        IDfDocument idfObj = (IDfDocument)idfSession.getObject(new DfId(s_ProjectCode));
   						String sOwnSrDeptOrgId = gwDeptService.selectOrgIdByCabinetcode( dto.getSrcCabinetcode());
      				    idfObj.setString("u_cabinet_code", dto.getSrcCabinetcode());
      				    idfObj.setString("u_own_dept", sOwnSrDeptOrgId);
      				    
      			        int i_ValIdx = idfObj.findString("u_join_dept_read",sOwnSrDeptOrgId);
	      				if(i_ValIdx < 0){ //최초 등록이면 append해준다
	      					idfObj.appendString("u_join_dept_read",            sOwnSrDeptOrgId); 
      				    }
      			        i_ValIdx = idfObj.findString("u_join_dept_del",sOwnSrDeptOrgId);
	      				if(i_ValIdx < 0){ //최초 등록이면 append해준다
	      					idfObj.appendString("u_join_dept_del",            sOwnSrDeptOrgId); 
      				    }//idfObj.setString("u_chief_id", );  //책임자 
	      				
      				    idfObj.save();
			    }
	    		return resultStr;
        }
      
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //이관 처리 (상세)
    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////      
    @Override
  	public String TransDoc(IDfSession idfSession, IDfDocument idf_Doc, String ps_RcevFolId, String ps_RcevCab, DPath dto) throws Exception
  	{
  		String				s_Status	= "";
  		String				s_AclName	= "";
  		String				s_FolPath	= "";
  		String				s_DocSec	= "";
  		String				s_CabCode	= "";
  		String				s_PrType	= "";
  		String				s_WfYn		= "";
  		String				s_Dql		= "";
  		String				s_Author	= "";
  		String				s_PerType	= "";
  		boolean				b_Privacy	= false;
  		IDfCollection		idf_Col		= null;
  		IDfPersistentObject	idf_PObj	= null;

	    String ps_DocKey = idf_Doc.getString("u_doc_key"); 

  		try
  		{
  			//다큐멘텀 세션 트랜잭션 시작
  			
  			s_DocSec	= idf_Doc.getString("u_sec_level");
  			s_Status	= idf_Doc.getString("u_doc_status");
  			s_PrType	= idf_Doc.getString("u_pr_type");
  			b_Privacy	= idf_Doc.getBoolean("u_privacy_flag");
  			s_WfYn		= idf_Doc.getString("u_wf_doc_yn");
  			s_FolPath	= idf_Doc.getString("u_folder_path");
  			

  			//편집중 문서는 강제 해제 함
  			if(idf_Doc.isCheckedOut())
  			{
  				idf_Doc.cancelCheckout();
  			}
  			
  			//------------------------------------------------------------------------------------
  			// 권한 수정 (정책서 기준)
  			// 1. 제한문서는 기존 권한에 수신부서의 팀장, 직속임원 Line이 자동 추가됨
  			// 2. 그 외 등급은 수신부서의 팀내 등급으로 변경
  			// => 결론적으로, 수신부서 기본 ACL을 지정하면 수신부서의 팀장, 직속임원 Line이 
  			//    포함되어 있기 때문에, 문서 속성의 추가 권한자(edms_auth_base)만 유지하면 됨
  			//------------------------------------------------------------------------------------
  			//먼저 수신 부서 기본 ACL 지정
  			s_AclName = "a_" + ps_RcevCab + "_" +										//문서함코드
  					(s_PrType.equals("") ? "d" : s_PrType).toLowerCase() + "_" + 		//d(부서),p(프로젝트),r(연구과제)
  					(b_Privacy ? "p" : "g") + "_" +										//g(일반),p(개인정보포함)
  					(s_Status.equals("L") ? "l" : s_DocSec).toLowerCase() + "_" +		//보안등급(l,s,t,c,g)
  					(s_WfYn.equals("Y") ? "y" : "n");									//n(일반),y(결재문서)
  			
  			idf_Doc.setACLDomain(idfSession.getDocbaseOwnerName());
  			idf_Doc.setACLName(s_AclName);

  			//제한 등급이면 기존 소유부서의 기본권한 정보 지우고, 그 외 추가 권한들 유지 및 ACL 부여
  			if(s_DocSec.equals("S"))
  			{
  				//edms_auth_base의 소유부서(기본부서)인 것을 지움
  				s_Dql = "delete edms_auth_base object " +
  						" where u_obj_id = '" + ps_DocKey + "' " +
  						"   and u_own_dept_yn = 'Y' ";
  				
  				idf_Col = DCTMUtils.getCollectionByDQL(idfSession, s_Dql, DfQuery.DF_QUERY);
  				if(idf_Col != null) idf_Col.close();
  				
  				//기존 권한 유지를 위해 권한자 목록 가져와서 ACL 부여
  				s_Dql = "select u_permit_type, u_author_id from edms_auth_base " +
  						" where u_obj_id = '" + ps_DocKey + "' " +
  						"   and u_obj_type = 'D' " +
  						"   and u_doc_status = '" + s_Status + "' ";
  				
  				idf_Col = DCTMUtils.getCollectionByDQL(idfSession, s_Dql, DfQuery.DF_QUERY);
  				while(idf_Col != null && idf_Col.next())
  				{
  					s_PerType = idf_Col.getString("u_permit_type");
  					s_Author  = idf_Col.getString("u_author_id");
  					
  					idf_Doc.grant(s_Author, (s_PerType.equals("R") ? 3 : 7),"");
  				}
  				if(idf_Col != null) idf_Col.close();

  			}
  			//제한이 아니면 기본 ACL 지정
  			else
  			{
  				//edms_auth_base의 소유부서(기본부서)인 것을 지움
  				s_Dql = "delete edms_auth_base object " +
  						" where u_obj_id = '" + ps_DocKey + "' ";
  				
  				idf_Col = DCTMUtils.getCollectionByDQL(idfSession, s_Dql, DfQuery.DF_QUERY);
  				if(idf_Col != null) idf_Col.close();
  				
  				idf_Doc.setString("u_sec_level", "T");	
  			}

  			//--------------------------------------------------
  			// 문서권한 기본(edms_auth_base) 정보 생성
  			// : Live 용, Closed 용, 2개
  			//--------------------------------------------------
  			List<String> lst_Status = new ArrayList<String>();
  			lst_Status.add("C");
  			lst_Status.add("L");
  			
  			for(String s_LStatus : lst_Status)
  			{
  				idf_PObj = idfSession.newObject("edms_auth_base");
  				idf_PObj.setString("u_obj_id"		, ps_DocKey);
  				idf_PObj.setString("u_obj_type"		, "D");
  				idf_PObj.setString("u_doc_status"	, s_LStatus);
  				idf_PObj.setString("u_permit_type"	, "D");
  				idf_PObj.setString("u_own_dept_yn"	, "Y");
  				idf_PObj.setString("u_author_id"	, "g_" + ps_RcevCab);
  				idf_PObj.setString("u_author_type"	, "D");
  				idf_PObj.setString("u_create_user"	, "SYSTEM");
  				idf_PObj.setTime  ("u_create_date"	, new DfTime());
  				idf_PObj.save();
  			}
  			
  			//위치 변경은 ps_RcevFolId 값이 있는 경우만
  			if(null !=ps_RcevFolId && ps_RcevFolId !="")
  				idf_Doc.setString("u_fol_id", ps_RcevFolId);
  			
  			idf_Doc.setString("u_pr_code", dto.getPrCode());
  			idf_Doc.setString("u_pr_type", dto.getPrType());
  			
  			idf_Doc.setString("u_cabinet_code", ps_RcevCab);
  			idf_Doc.save();

  			//------------------------------------------
  			// 과거 버전이 있는 경우 처리
  			//------------------------------------------
  			if(!ps_DocKey.equals(idf_Doc.getObjectId().toString()))
  			{
  				String s_VerId = "";
  				IDfDocument idf_VerDoc = null;
  				IDfCollection idf_VerCol = idf_Doc.getVersions(null);
  				
  				while(idf_VerCol != null && idf_VerCol.next())
  				{
  					s_VerId = idf_VerCol.getString("r_object_id");
  					//과거 버전만 처리하기 위해 현재 버전은 skip
  					if(s_VerId.equals(idf_Doc.getObjectId().toString()))
  						continue;
  					
  					//권한은 현재 버전의 ACL Name을 사용하면 그대로 적용 됨
  					idf_VerDoc = (IDfDocument)idfSession.getObject(new DfId(s_VerId));
  					idf_VerDoc.setACLDomain(idfSession.getDocbaseOwnerName());
  					idf_VerDoc.setACLName(idf_Doc.getString("acl_name"));
  					
  		    		if (secLevelMap.get(idf_VerDoc.getString("u_sec_level")) < secLevelMap.get(dto.getTargetSecLevel())) {
  		    			idf_VerDoc.setString("u_sec_level", dto.getTargetSecLevel() );
  		    		}
  					
  					//위치 변경은 ps_RcevFolId 값이 있는 경우만
  					if(null !=ps_RcevFolId && ps_RcevFolId !="") 
  						idf_VerDoc.setString("u_fol_id", ps_RcevFolId);
 					
  					idf_VerDoc.setString("u_cabinet_code", ps_RcevCab);
  					idf_VerDoc.save();
  				}
  				if(idf_VerCol != null) idf_VerCol.close(); 
  			}
  		    System.out.println("문서 처리 완료 : (Doc Key = " + ps_DocKey + ", Path : " + idf_Doc.getString("u_folder_path") + "/" + idf_Doc.getObjectName() + ")");
  		}
  		catch(Exception e)
  		{
  			e.printStackTrace();
  			return "문서 권한 정보 변경 오류 발생 : (Doc Key = " + ps_DocKey + ")\n";
  		}
  		return "";
  	}

  	//폴더 오브젝트 처리
  	@Override
  	public String TransFol(IDfSession idfSession,
  			              String ps_ReqUser, 
  			              String ps_FolId,  
  			              String ps_SendCab, 
  			              String ps_RcevCab, 
  			              String ps_RcevFolId,
  			              DPath dto
  			             ) throws Exception
  	{
  		Statement			sm_EDM		= null;
  		ResultSet			rs_EDM		= null;
  		String				s_Dql		= "";
  		String				s_Sql		= "";
  		String				s_LType		= "";
  		String				s_ObjId		= "";
  		String				s_ComId		= "";
  		IDfCollection		idf_Col		= null;
  		IDfPersistentObject	idf_PObj	= null;
  		
  		String s_ProjectCode = dto.getPrCode();

  		try
  		{
  			//---------------------------------------------------------------
  			// 해당 폴더 권한 수정(위치 변경은 ps_RcevFolId 값이 있는 경우만) 
  			// => 기본 권한 정책 기준으로 처리 
  			//---------------------------------------------------------------
  			idf_PObj = idfSession.getObject(new DfId(ps_FolId));
  			
  			if(null !=ps_RcevFolId && ps_RcevFolId !="") 
  				idf_PObj.setString("u_up_fol_id", ps_RcevFolId);

  			idf_PObj.setString("u_cabinet_code", ps_RcevCab);
  			idf_PObj.setString("u_fol_status", "O");
  			idf_PObj.setString("u_pr_code", dto.getPrCode());
  			idf_PObj.setString("u_pr_type", dto.getPrType());
  			idf_PObj.setString("u_delete_status", "");

    		if (secLevelMap.get(idf_PObj.getString("u_sec_level")) < secLevelMap.get(dto.getTargetSecLevel())) {
    			idf_PObj.setString("u_sec_level", dto.getTargetSecLevel() );
		    }
  			
  			idf_PObj.save();
  			
  			//edms_auth_base 관련 정보 지움
  			s_Dql = "delete edms_auth_base object " +
  					" where u_obj_id = '" + ps_FolId + "' ";
  			
  			idf_Col = DCTMUtils.getCollectionByDQL(idfSession, s_Dql, DfQuery.DF_QUERY);
  			if(idf_Col != null) idf_Col.close();

  			// 폴더의 기본권한 그룹 생성
  			//String s_ComCode = gwDeptService.selectComCodeByCabinetCode(ps_RcevCab);;
  			String s_ComCode = dto.getTgComId();//gwDeptService.selectComCodeByCabinetCode(ps_RcevCab).toLowerCase();
  			s_ComId = gwDeptService.selectComCodeByCabinetCode(ps_RcevCab).toLowerCase(); 
  			
  			List<String> lst_Author = new ArrayList<String>();
  			lst_Author.add("g_chairman");
  			lst_Author.add("g_" + s_ComId + "_mgr_g_a");
  			lst_Author.add("g_" + s_ComId + "_mgr_g_b");
  			lst_Author.add("g_" + ps_RcevCab + "_imwon");
  			lst_Author.add("g_" + ps_RcevCab + "_chief");
  			lst_Author.add("g_" + ps_RcevCab + "_mgr_g_a");
  			lst_Author.add("g_" + ps_RcevCab + "_mgr_g_b");
  			lst_Author.add("g_" + ps_RcevCab + "_old");
  			lst_Author.add("g_" + ps_RcevCab);
  			
  			//폴더 기본 권한 정보 - Live 
  			for(String s_Author : lst_Author)
  			{
  				idf_PObj = idfSession.newObject("edms_auth_base");
  				idf_PObj.setString("u_obj_id"		, ps_FolId);
  				idf_PObj.setString("u_obj_type"		, "F");
  				idf_PObj.setString("u_doc_status"	, "L");
  				idf_PObj.setString("u_permit_type"	, "D");
  				idf_PObj.setString("u_own_dept_yn"	, s_Author.equals("g_" + ps_RcevCab) ? "Y" : "");
  				idf_PObj.setString("u_author_id"	, s_Author);
  				idf_PObj.setString("u_author_type"	, s_Author.equals("g_" + ps_RcevCab) ? "D" : "S");
  				idf_PObj.setString("u_create_user"	, "SYSTEM");
  				idf_PObj.setTime  ("u_create_date"	, new DfTime());
  				idf_PObj.save();
  			}
  			
  			//문서 용 권한 - Closed 
  			idf_PObj = idfSession.newObject("edms_auth_base");
  			idf_PObj.setString("u_obj_id"		, ps_FolId);
  			idf_PObj.setString("u_obj_type"		, "F");
  			idf_PObj.setString("u_doc_status"	, "C");
  			idf_PObj.setString("u_permit_type"	, "D");
  			idf_PObj.setString("u_own_dept_yn"	, "Y");
  			idf_PObj.setString("u_author_id"	, "g_" + ps_RcevCab);
  			idf_PObj.setString("u_author_type"	, "D");
  			idf_PObj.setString("u_create_user"	, "SYSTEM");
  			idf_PObj.setTime  ("u_create_date"	, new DfTime());
  			idf_PObj.save();
  		}
  		catch(Exception e)
  		{
  			return "폴더(하위 오브젝트) 권한 정보 변경 오류 발생 : " + ps_FolId + "\n" + e.toString();
  		}
  		finally
  		{
  			if(rs_EDM != null) try { rs_EDM.close(); } catch(Exception e2) {};
  			if(sm_EDM != null) try { sm_EDM.close(); } catch(Exception e2) {};
  		}
  		return "";
  	}
  	
  	@Override
  	public String CreateTransFolder(IDfSession  idfSession, String ps_CabCode, String ps_DeptName, DPath dto) throws Exception
  	{
  		String	s_Dql		= "";
  		String	s_UpFolId	= "";
  		String	s_FolId		= "";
  		String	s_FolName	= "[이관함]";

  		try
  		{
  			String s_ComCode = gwDeptService.selectComCodeByCabinetCode(ps_CabCode);
  			
  			//------------------------------------------------
  			// 폴더의 기본권한 그룹 목록 준비
  			//------------------------------------------------
  			List<String> lst_Author = new ArrayList<String>();
  			lst_Author.add("g_chairman");
  			lst_Author.add("g_" + s_ComCode + "_mgr_g_a");
  			lst_Author.add("g_" + s_ComCode + "_mgr_g_b");
  			lst_Author.add("g_" + ps_CabCode + "_imwon");
  			lst_Author.add("g_" + ps_CabCode + "_chief");
  			lst_Author.add("g_" + ps_CabCode + "_mgr_g_a");
  			lst_Author.add("g_" + ps_CabCode + "_mgr_g_b");
  			lst_Author.add("g_" + ps_CabCode + "_old");
  			if(dto.getTargetFolType().equals("PFO")) {
  				lst_Author.add("g_" + s_ComCode + "_pjtmgr");
  			}else if(dto.getTargetFolType().equals("PFO")) {
  				lst_Author.add("g_" + s_ComCode + "_rschmgr");
  			}

  			
  			lst_Author.add("g_" + ps_CabCode);

  			s_Dql = "edms_folder where u_cabinet_code = '" + ps_CabCode + "' " +
  			        "and u_fol_type ='"+ dto.getTargetFolType()+"'" +
  				    "and u_fol_name = '" + s_FolName + "'  and u_up_fol_id is nullstring ";
  				
  			//있는지 체크
  			s_UpFolId = idfSession.getIdByQualification(s_Dql).toString();
  			
  			//없으면 생성
  			if(!DfId.isObjectId(s_UpFolId))
  			{
  				IDfPersistentObject idf_PObj = (IDfPersistentObject)idfSession.newObject("edms_folder");
  				idf_PObj.setString("u_sec_level"	, dto.getTargetSecLevel());
  				idf_PObj.setString("u_fol_status"	, "O");
  				idf_PObj.setString("u_delete_status", "");
  				
  				idf_PObj.setString("u_pr_code"	, dto.getPrCode());
  				idf_PObj.setString("u_pr_type"	, dto.getPrType());

  				
  				idf_PObj.setString("u_create_user"	, dto.getReqUser());
  				idf_PObj.setTime  ("u_create_date"	, new DfTime());
  				idf_PObj.save();
  				
  				s_UpFolId = idf_PObj.getObjectId().getId();
  				
  				for(String s_Author : lst_Author)
  				{
  					idf_PObj = idfSession.newObject("edms_auth_base");
  					idf_PObj.setString("u_obj_id"		, s_UpFolId);
  					idf_PObj.setString("u_obj_type"		, "F");
  					idf_PObj.setString("u_doc_status"	, "L");	//폴더 자체의 권한은 'L'로 설정 된 것
  					idf_PObj.setString("u_permit_type"	, "D");
  					idf_PObj.setString("u_own_dept_yn"	, s_Author.equals("g_" + ps_CabCode) ? "Y" : "");
  					idf_PObj.setString("u_author_id"	, s_Author);
  					idf_PObj.setString("u_author_type"	, s_Author.equals("g_" + ps_CabCode) ? "D" : "S");
  					idf_PObj.setString("u_create_user"	, dto.getReqUser());
  					idf_PObj.setTime  ("u_create_date"	, new DfTime());
  					idf_PObj.save();
  				}
  			}
  			
  			//'이관함'폴더 하위에 송신부서 팀명 폴더 체크 후 생성
  			s_Dql = "edms_folder " + 
  					"where u_cabinet_code = '" + ps_CabCode + "' " +
  					"  and u_fol_type  = '" + dto.getTargetFolType() + "' " +
  					"  and u_fol_name  = '" + ps_DeptName + "' " +
  					"  and u_up_fol_id = '" + s_UpFolId + "' ";
  			
  			//있는지 체크
  			s_FolId = idfSession.getIdByQualification(s_Dql).toString();
  			
  			//없으면 생성
  			if(!DfId.isObjectId(s_FolId))
  			{
  				IDfPersistentObject idf_PObj = (IDfPersistentObject)idfSession.newObject("edms_folder");
  				idf_PObj.setString("u_sec_level"	, dto.getTargetSecLevel());
  				idf_PObj.setString("u_fol_status"	, "O");
  				idf_PObj.setString("u_delete_status", "");
  				idf_PObj.setString("u_create_user"	, dto.getReqUser());
  				idf_PObj.setTime  ("u_create_date"	, new DfTime());
  				idf_PObj.save();
  				
  				s_FolId = idf_PObj.getObjectId().getId();
  				
  				for(String s_Author : lst_Author)
  				{
  					idf_PObj = idfSession.newObject("edms_auth_base");
  					idf_PObj.setString("u_obj_id"		, s_FolId);
  					idf_PObj.setString("u_obj_type"		, "F");
  					idf_PObj.setString("u_doc_status"	, "L");	//폴더 자체의 권한은 'L'로 설정 된 것
  					idf_PObj.setString("u_permit_type"	, "D");
  					idf_PObj.setString("u_own_dept_yn"	, s_Author.equals("g_" + ps_CabCode) ? "Y" : "");
  					idf_PObj.setString("u_author_id"	, s_Author);
  					idf_PObj.setString("u_author_type"	, s_Author.equals("g_" + ps_CabCode) ? "D" : "S");
  					idf_PObj.setString("u_create_user"	, dto.getReqUser());
  					idf_PObj.setTime  ("u_create_date"	, new DfTime());
  					idf_PObj.save();
  				}
  			}
  		}
  		catch(Exception e)
  		{
  			//Util.PrintLog(Util.GetStackTraceString(e));
  		}
  		return s_FolId;
  	}
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////      
//                     삭제 처리 (상세)
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////        
    	//폴더 오브젝트 삭제 처리 
    	//ps_DelFlag : D(개별 삭제), P(대상의 상위가 삭제 됨으로 인한 삭제)
        @Override
   	    public boolean DeleteFolObject(IDfSession idfSession, IDfPersistentObject idfObj, String ps_ReqUser, String ps_FolId, String ps_DelFlag, DPath dto) throws Exception
    	{
    		boolean li_FolSuccess=false;
    		try
    		{
    			//폴더 오브젝트 삭제
    			if(idfObj != null) 
    			{
    				if(idfObj.getString("u_delete_status").equals(" ") || idfObj.getString("u_delete_status").equals(""))
    				{
    					//휴지통 등록. flag 값이 'D' 인 경우 통과
						String s_Type = idfObj.getType().getName();
						String s_FolType="";
						String s_Objtype="F";

						if(s_Type.equals("edms_folder"))       {s_FolType= idfObj.getString("u_pr_type").equals(" ")|| idfObj.getString("u_pr_type").equals("")?"D":idfObj.getString("u_pr_type"); s_Objtype="F" ;}  //폴더 삭제
						else if(s_Type.equals("edms_project")) {s_FolType="P"; s_Objtype="P" ;}  //프로젝트 삭제
						else if(s_Type.equals("edms_research")){s_FolType="R"; s_Objtype="R" ;}  //연구과제 삭제
						
    					if("D".equals(ps_DelFlag))  //상위삭제에서 s_Objtype이 바뀔수도 있음(프로젝트나 연구과제 삭제시)
    					{
    						IDfPersistentObject idf_RecycleObj = idfSession.newObject("edms_recycle");
    						idf_RecycleObj.setString("u_cabinet_code", idfObj.getString("u_cabinet_code"));
    						idf_RecycleObj.setString("u_cabinet_type", s_FolType);
    						idf_RecycleObj.setString("u_obj_type"    , s_Objtype);
    						idf_RecycleObj.setString("u_obj_id"      , ps_FolId);
    						idf_RecycleObj.setString("u_delete_user" , ps_ReqUser);
    						idf_RecycleObj.setTime  ("u_delete_date" , new DfTime());
    						idf_RecycleObj.save();
    						
    					}
    					idfObj.setString("u_delete_status", s_Objtype.equals("F")?ps_DelFlag:"Y"); //폴더이면 파라메터로 넘어온 ps_DelFlag, 프로젝트나 연구과제삭제플래그는 Y 
    					idfObj.save();
    					dto.setReCycleCnt(dto.getReCycleCnt() + 1);//휴지통 삭제건수 +1
    					li_FolSuccess=true;
    				}else {
    					li_FolSuccess=true;
    				}
    			}
/*    			
    			IDfCollection idf_Col=null;
  				//edms_auth_base 에서 Live한 권한 삭제 (휴지통)
  				String s_Dql = "delete edms_auth_base object " +
  						" where u_obj_id = '" + ps_FolId + "' " +
  						"   and u_doc_status = 'L' ";
  				
  				idf_Col = DCTMUtils.getCollectionByDQL(idfSession, s_Dql, DfQuery.DF_QUERY);
  				if(idf_Col != null) idf_Col.close();
  				li_FolSuccess=true;
*/  				
    			
    		}
    		catch(Exception e)
    		{
    			li_FolSuccess=false;
    			throw e;
    		}
    		return li_FolSuccess;
    	}
    	
      //프로젝트나 연구과제 삭제건수가 많아서 배치등록할 때 u_delete_status는 먼저 Y로 바꿔놓을때 사용한다.(컨트롤로에서 호출해서 사용)
        @Override
        public boolean updateDeleteStatus(UserSession userSession, String ps_ObjId, String ps_Status ) throws Exception
        {
        	IDfSession idfSession = null;
        	try
    		{
        		idfSession = this.getIdfSession(userSession);
    		 	if (!idfSession.isTransactionActive()) {
    		 	    idfSession.beginTrans();
    		 	}
    		 	String s_RobjectId="";
    		 	if(ps_ObjId.substring(0,1).equals("p"))
    		 	    s_RobjectId = idfSession.getIdByQualification("edms_project where u_pjt_code='" + ps_ObjId + "'").toString();
    		 	else
    		 		s_RobjectId = idfSession.getIdByQualification("edms_research where u_rsch_code='" + ps_ObjId + "'").toString();
    		 	
    			//Document 오브젝트 삭제
        		IDfPersistentObject idf_Obj = (IDfPersistentObject)idfSession.getObject(new DfId(ps_ObjId));
        		idf_Obj.setString("u_delete_status", ps_Status);
        		idf_Obj.setTime("u_update_date", new DfTime());
        		idf_Obj.setString("u_update_user", userSession.getUser().getUserId());
 	
    		}catch(Exception e)
    		{
    			return false;
    		}finally {
    			if (idfSession != null) {
	      		      if (idfSession.isTransactionActive()) {
	      		        idfSession.abortTrans();
	      		      }
	      		      if (idfSession.isConnected()) {
	    		    	  idfSession.disconnect();
	      		      }
      		    }    			
    		}
    		return true;        	
        }
        
        
    	//문서 오브젝트 삭제 처리 
    	//ps_DelFlag : D(개별 삭제), P(대상의 상위가 삭제 됨으로 인한 삭제)
    	//다음에 해당하면 삭제 불가
    	//1. 편집중 문서
    	//2. 삭제 상태(요청중 포함)인 문서
    	//3. 권한 없는 문서
    	//4. 타시스템 첨부
        @Override
    	public boolean DeleteDocObject(UserSession userSession,IDfSession  idfSession, String ps_ReqUser, String ps_DocId, String ps_DelFlag, DPath dto) 
    	{
    		String	s_Msg		= "";
    		String	s_DocStatus	= "";
    		String	s_DelStatus	= "";
    		String	s_FolType	= "";
    		String	s_FolPath	= "";
    		String	s_DocKey	= "";
    		String	s_FolId		= "";
    		String  s_DocId     = "";
    		
    		String s_CloseDate ="";
    		String s_Closer="";
    		
    		try
    		{
    			//Document 오브젝트 삭제
    			IDfDocument idf_Doc = (IDfDocument)idfSession.getObject(new DfId(ps_DocId));

    			IDfCollection		idf_Col		= null;

    			String s_Dql = "select * from edms_doc_link " +
						" where u_doc_key = '" + idf_Doc.getString("u_doc_key") + "' " ;
			    idf_Col = DCTMUtils.getCollectionByDQL(idfSession, s_Dql, DfQuery.DF_QUERY);
			    int linkDelCnt=0;
  				while(idf_Col != null && idf_Col.next())
    			{
	  				//링크 삭제이력
					IDfPersistentObject idf_LinkDel = idfSession.newObject("edms_doc_link_del");
					idf_LinkDel.setString("u_doc_id",           idf_Col.getString("u_doc_id")  ); //r_object_id, document.getChronicleId()
					idf_LinkDel.setString("u_doc_key",          idf_Col.getString("u_doc_key")  ); //문서번호
					idf_LinkDel.setString("u_cabinet_code",     idf_Col.getString("u_cabinet_code")); //문서함코드
					idf_LinkDel.setString("u_fol_id",           idf_Col.getString("u_fol_id")  ); //변경전 폴더 id, saveWfList이후에  전자결재폴더id로 바꿔서 edms_doc에 저장( link테이블에 있는 u_fol_id가 이전폴더id가 됨 )
					idf_LinkDel.setString("u_link_type",        idf_Col.getString("u_link_type")  ); //링크종류', SET COMMENT_TEXT='W:결재')
					idf_LinkDel.setString("u_create_user",      dto.getReqUser()); //삭제 작업자
					idf_LinkDel.setString("u_create_date",   (new DfTime()).toString());
					idf_LinkDel.save();

  					IDfCollection		idf_Colb		= null;
	  				s_Dql = "delete edms_doc_link object " +
	  						" where u_cabinet_code='"+idf_Col.getString("u_cabinet_code")+"' and u_doc_key = '" + idf_Doc.getString("u_doc_key") + "' " ;

	  				idf_Colb = DCTMUtils.getCollectionByDQL(idfSession, s_Dql, DfQuery.DF_QUERY);
	  				if(idf_Colb != null) idf_Colb.close();
	  				dto.setLinkDelCnt(dto.getLinkDelCnt() + 1);
	  				linkDelCnt++;
					
	  				
				}
  				if(idf_Col != null) idf_Col.close();
  				
  				if(linkDelCnt > 0) {
  	  				System.out.println("링크 삭제 ");
  					return true;
  				}
  				

				
    			if(idf_Doc != null) 
    			{
    				s_DocId     = idf_Doc.getString("r_object_id");
    				s_DocKey	= idf_Doc.getString("u_doc_key");
    				s_DocStatus	= idf_Doc.getString("u_doc_status");
    				s_FolId		= idf_Doc.getString("u_fol_id");
    				s_FolPath	= idf_Doc.getString("u_folder_path");
    				s_CloseDate = idf_Doc.getString("u_closed_date");
    				s_Closer    = idf_Doc.getString("u_closer");
    				
    				//if( null !=idf_Doc.getString("u_delete_status")&& !idf_Doc.getString("u_delete_status").equals("") || !idf_Doc.getString("u_delete_status").equals(" ")) {
    				//	System.out.println("##MOLA:삭제/폐기 요청된 문서: "+ (idf_Doc.getString("u_delete_status").equals("R")?"-폐기요청-":"-삭제요청-") + idf_Doc.getString("title") );
    				//}
    				
    				//Live문서는 휴지통으로 이동
    				if(s_DocStatus.equals("L")  && s_CloseDate.trim().equals("nulldate")  &&  s_Closer.trim().equals(""))
    				{
    					idf_Doc.setString("u_delete_status", ps_DelFlag);
    					idf_Doc.setString("u_update_date", (new DfTime()).toString());
    					idf_Doc.save();
    					
    					//휴지통 등록. flag 값이 'D' 인 경우만
    					if("D".equals(ps_DelFlag))
    					{
    						//IDfPersistentObject idf_FolObj = idfSession.getObject(new DfId(s_FolId));
    						//s_FolType = idf_FolObj.getString("u_fol_type");
    						s_FolType=idf_Doc.getString("u_pr_type");
    						
    						IDfPersistentObject idf_RecycleObj = idfSession.newObject("edms_recycle");
    						idf_RecycleObj.setString("u_cabinet_code", idf_Doc.getString("u_cabinet_code"));
    						idf_RecycleObj.setString("u_cabinet_type", s_FolType.equals("") ? "D" : s_FolType );
    						idf_RecycleObj.setString("u_obj_type"    , "D");
    						idf_RecycleObj.setString("u_obj_id"      , s_DocId);
    						idf_RecycleObj.setString("u_delete_user" , ps_ReqUser);
    						idf_RecycleObj.setTime  ("u_delete_date" , new DfTime());
    						idf_RecycleObj.save();
    					}
    					if(dto.getReCycleStr().equals("")) dto.setReCycleStr( idf_Doc.getObjectName());
    					dto.setReCycleCnt(dto.getReCycleCnt() + 1);//휴지통 삭제건수 +1
    				}
    				//Closed 문서는 폐기 요청 (Closed상태이거나, 라이브문서인데  closed일자나 Closer항목이 비어있지 않으면 폐기대상)
    				else if(s_DocStatus.equals("C") 
    						|| (s_DocStatus.equals("L")  &&  (!s_CloseDate.trim().equals("nulldate")  ||  !s_Closer.trim().equals("")) ) )
    				{
    					idf_Doc.setString("u_delete_status", "R");
    					idf_Doc.setString("u_update_date", (new DfTime()).toString());
    					idf_Doc.save();
                        System.out.println("MM:####:" +s_DocStatus +"  s_CloseDate:"+ s_CloseDate + "   s_CloseDate:"+s_Closer + "..s_DocId:"+s_DocId );    					
    					IDfPersistentObject idf_RecycleObj = idfSession.newObject("edms_req_delete");
    					idf_RecycleObj.setString("u_cabinet_code", idf_Doc.getString("u_cabinet_code"));
    					idf_RecycleObj.setString("u_req_doc_id"  , s_DocId);
    					idf_RecycleObj.setString("u_req_doc_key" , s_DocKey);
    					idf_RecycleObj.setString("u_req_type"    , "D");
    					idf_RecycleObj.setString("u_req_status"  , "R");
    					
    					//idf_RecycleObj.setString("u_approver"  , dto.getUApprover()); //승인자
    					
    					idf_RecycleObj.setString("u_req_user"    , ps_ReqUser);
    					idf_RecycleObj.setTime  ("u_req_date"    , new DfTime());
    					
    					idf_RecycleObj.setString("u_doc_name"    , idf_Doc.getString("title"));
    					idf_RecycleObj.setString("u_sec_level"   , idf_Doc.getString("u_sec_level"));
    					idf_RecycleObj.setString("u_create_year" , idf_Doc.getString("u_reg_date").substring(0,4));
    					
    					
    					idf_RecycleObj.setTime  ("u_expired_date"    , idf_Doc.getTime("u_expired_date"));//문서의 보존연한 마감일자 가져다가 세팅
    					idf_RecycleObj.setString("u_req_reason"  , "화면에서 선택하여 처리됨");
    					idf_RecycleObj.save();

    					if(dto.getReqDelStr().equals("")) dto.setReqDelStr( idf_Doc.getObjectName()+"."+idf_Doc.getString("u_file_ext"));
    					dto.setReqDelCnt(dto.getReqDelCnt() + 1);//삭제건수 +1
    					if(dto.getReqDelCnt()==1) {
    					    dto.setReqDelObjId(idf_RecycleObj.getObjectId()+"");
    					    dto.setReqDelDocKey(s_DocKey);
    					}
    					
    				}
    				
    				/////////////////////////// 반출함 문서가 있으면 상태를 D로 업데이트 처리한다.
    				int i_TakeOutCnt = DCTMUtils.getCountByDQL( idfSession, " edms_req_takeout_doc where u_req_doc_id='"+idf_Doc.getString("r_object_id") +"' and u_req_doc_key='"+idf_Doc.getString("u_doc_key") +"' and u_status !='D' ");
    				if(i_TakeOutCnt > 0) {
    					
    	    			s_Dql = "select distinct u_req_id from edms_req_takeout_doc " +
        						" where u_req_doc_id='"+idf_Doc.getString("r_object_id") +"' and u_req_doc_key='"+idf_Doc.getString("u_doc_key") +"' and u_status !='D' ";
        			    idf_Col = DCTMUtils.getCollectionByDQL(idfSession, s_Dql, DfQuery.DF_QUERY);
        			    
        			    /**************************************/
	  			        String s_DqlDoc = "UPDATE edms_req_takeout_doc  OBJECTS SET u_status='D' "  
						          +"WHERE u_req_doc_id='" + idf_Doc.getString("r_object_id")+ "'  and u_req_doc_key='" + idf_Doc.getString("u_doc_key")+"'" ;
						IDfQuery 		idf_Qry 	= null;
						idf_Qry = new DfQuery();
						idf_Qry.setDQL(s_DqlDoc);
						IDfCollection idf_ColDoc = idf_Qry.execute(idfSession, DfQuery.QUERY);
						if (idf_ColDoc!=null && idf_ColDoc.next()) idf_ColDoc.close();
        			    
          				while(idf_Col != null && idf_Col.next())
            			{
          					int i_RemainCnt = DCTMUtils.getCountByDQL( idfSession, " edms_req_takeout_doc where u_req_id='"+idf_Col.getString("u_req_id") +"' and u_status !='D' ");
          					if(i_RemainCnt < 1) {
			  			        String s_DqlUpt = "UPDATE edms_req_takeout  OBJECTS SET u_limit_date=DATE(TODAY) "  
								          +"WHERE r_object_id='" + idf_Col.getString("u_req_id")+ "' " ;

								IDfQuery 		idf_Qry2 	= null;
								idf_Qry2 = new DfQuery();
								idf_Qry2.setDQL(s_DqlUpt);
								IDfCollection idf_ColM = idf_Qry2.execute(idfSession, DfQuery.QUERY);
								if (idf_ColM!=null && idf_ColM.next()) idf_ColM.close();
          					}
            			}
          				if (idf_Col!=null && idf_Col.next()) idf_Col.close();
    				}
                    /////////////////////////// 반출함 문서가 있으면 상태를 D로 업데이트 처리한다.
						
    				//로그 기록
    				String sOwnSrDeptOrgId = dto.getOwnSrDeptOrgId();
    				
    				LogDoc logDoc = LogDoc.builder()
   				          .uJobCode( (s_DocStatus.equals("L") ? "LD" : "DR"))	//LD(Live 문서 삭제), DR(폐기 요청)
   				          .uDocId(s_DocId)
   				          .uDocKey(idf_Doc.getString("u_doc_key"))
   				          .uDocName(idf_Doc.getString("title").replaceAll("'", "''"))
   				          .uDocVersion(Integer.parseInt(idf_Doc.getVersionLabel(0).substring(0, idf_Doc.getVersionLabel(0).indexOf(".")))+"")
   				          .uFileSize(Long.parseLong(idf_Doc.getString("r_content_size")))
    			          .uOwnDeptcode(sOwnSrDeptOrgId)
   				          .uActDeptCode(userSession.getUser().getOrgId())
   				          .uJobUser(ps_ReqUser)
   				          .uJobUserType(dto.getUJobUserType()==null ? "P" : dto.getUJobUserType()) //작업자구분(P:개인, D:부서관리자, C:사별관리자, G:그룹관리자, S:시스템)
   				          .uDocStatus(idf_Doc.getString("u_doc_status"))
   				          .uSecLevel(idf_Doc.getString("u_sec_level"))
   				          .uCabinetCode(idf_Doc.getString("u_cabinet_code"))
   				          //.uJobGubun("DEL")  //삭제처리
   				          .uUserIp(dto.getReqUserIp())							// 받아야함.
   				          .uAttachSystem("")
   				          .build();
   				      insertLog(logDoc);
    				return true;
    			}
    		}
    		catch(Exception e)
    		{
//    			s_Result += "다음 오브젝트(문서) 삭제 시 오류 발생 : " + ps_DocId + "<br>" + Util.GetStackTraceString(e);
    			//Util.PrintLog("다음 오브젝트(문서) 삭제 시 오류 발생 : " + ps_DocId + "\n" + Util.GetStackTraceString(e));
    			return false;
    		}
    		finally
    		{
    			return true;	
    		}
    		
    	}
        
        //제한건수가 넘는 요청건에 대해서 Batch 작업을 요청한다(edms_req_trans_user, edms_req_trans_user_doc )
        @Override
        public void addTransBatchObjects(UserSession userSession,  DPath dto ) throws Exception{
        	
    		//////////////////////////////////////////////  선택된 ....
    		List<String> FolList = dto.getSourceFolders();  //edms_folder에서 관리되는 대상 폴더들 
    		List<String> DocList = dto.getSourceFiles();    //edms_doc에 존재하는 대상 파일들
    		List<String> PjtList = dto.getSourcePjts();     //edms_project에 존재하는 대상 프로젝트들 
    		List<String> RscList = dto.getSourceRscs();    //edms_research에 존재하는 대상 연구과제들
    		//////////////////////////////////////////////  end
        	
        	IDfSession idfSession =null;
        	try {
        		idfSession = this.getIdfSession(userSession);
    		 	if (!idfSession.isTransactionActive()) {
    		 	    idfSession.beginTrans();
    		 	}
        		
    		    String reqTitle = "";
    		    
    		    int totCnt = PjtList.size()+ RscList.size()+ FolList.size() + DocList.size() ;
    		    
   		    	if(PjtList.size() > 0) {  //프로젝트
    				IDfDocument dObj = (IDfDocument)idfSession.getObject(new DfId(PjtList.get(0)));
				    reqTitle="프로젝트 : "+dObj.getString("u_pjt_name");
		            reqTitle += " 외 "+(totCnt-1)+" 건";
    			}
				if(reqTitle.equals("")) {
    		    	if(RscList.size() > 0) { //연구과제
	    				IDfDocument dObj = (IDfDocument)idfSession.getObject(new DfId(RscList.get(0)));
	    				if(reqTitle.equals("")) {
	    				    reqTitle="연구과제 :"+dObj.getString("u_rsch_name");
					        if(totCnt > 0)
					            reqTitle += " 외 "+(totCnt-1)+" 건";
	    				}
    		    	}
    			}    		    
				if(reqTitle.equals("")) {
    		    	if(FolList.size() > 0) {
					    IDfPersistentObject fObj = idfSession.getObject(new DfId(FolList.get(0)));
					    if(reqTitle.equals("")) {
					        reqTitle="폴더 :"+fObj.getString("u_fol_name");
					        if(totCnt > 0)
					        	reqTitle += " 외 "+(totCnt-1)+" 건";
					    }
    		    	}
				}

				if(reqTitle.equals("")) {
    		    	if(DocList.size() > 0) {
        				String ls_RobjectId	= DCTMUtils.getCurrentObjectID(idfSession, DocList.get(0)); //문서의 최신버전 r_object_id를 가져온다.
        				if(ls_RobjectId.equals("")) {
        				    throw new NotFoundException("문서 최신버전이 변경됨[Documentum]");
        				}
        				IDfDocument dObj = (IDfDocument)idfSession.getObject(new DfId(ls_RobjectId));
    		    		
					    if(reqTitle.equals("")) {
					        reqTitle="문서 :"+dObj.getString("object_name");
					        if(totCnt > 0)
					        	reqTitle += " 외 "+(totCnt-1)+" 건";
					    }
    		    	}
				}
				
	  			//--------------------------------------------------
	  			// 문서권한 기본(edms_req_trans_user) 정보 생성
	  			//--------------------------------------------------
    			IDfPersistentObject idf_PObj = idfSession.newObject("edms_req_trans_user");
  				
  				idf_PObj.setString("u_req_title"	 , reqTitle);
  				idf_PObj.setString("u_req_user"	     , dto.getReqUser());
  				idf_PObj.setTime  ("u_req_date"	     , new DfTime());
  				idf_PObj.setString("u_req_status"    , dto.getReqStatus());
  				idf_PObj.setString("u_req_reason"    , dto.getTransCause());

    			for(int i=0; i < PjtList.size(); i++)
    			{
    				if(!PjtList.get(i).equals(""))   idf_PObj.appendString("u_obj_id"        , PjtList.get(i));
    			}

    			for(int i=0; i < RscList.size(); i++)
    			{
    				if(!RscList.get(i).equals(""))   idf_PObj.appendString("u_obj_id"        , RscList.get(i));
    			}
    			
    			for(int i=0; i < FolList.size(); i++)
    			{
    				if(!FolList.get(i).equals(""))   idf_PObj.appendString("u_obj_id"        , FolList.get(i));
    			}
    			for(int i=0; i < DocList.size(); i++)
    			{
    				if(!DocList.get(i).equals(""))   idf_PObj.appendString("u_obj_id"        , DocList.get(i));
    			}
  				
  				idf_PObj.setString("u_send_cab_code"    , dto.getSrcCabinetcode());
  				idf_PObj.setString("u_rcev_cab_code"    , dto.getTgCabinetcode());
  				if(dto.getReqStatus().equals("S")) {
  				    idf_PObj.setString("u_approver"     , dto.getReqUser());
  				    idf_PObj.setTime  ("u_approve_date" , new DfTime());
  				    idf_PObj.setTime  ("u_trans_date"	, new DfTime());
  				}
  				
  				idf_PObj.save();
  				String sReqId = idf_PObj.getObjectId().getId();

  				
    			for(int i=0; i < DocList.size(); i++)
    			{
    	  			//--------------------------------------------------
    	  			// 문서권한 기본(edms_req_trans_user_doc) 정보 생성 : 문서
    	  			//--------------------------------------------------
    				IDfPersistentObject idf_DObj = idfSession.newObject("edms_req_trans_user_doc");

    				String ls_RobjectId 	= DCTMUtils.getCurrentObjectID(idfSession, DocList.get(0)); //문서의 최신버전 r_object_id를 가져온다.
    				if(ls_RobjectId.equals("")) {
    				    throw new NotFoundException("문서 최신버전이 변경됨[Documentum]");
    				}
    				IDfDocument dObj = (IDfDocument)idfSession.getObject(new DfId(ls_RobjectId));

    				idf_DObj.setString("u_req_id"	, sReqId);
    				idf_DObj.setString("u_doc_id"	, dObj.getString("r_object_id"));
    				idf_DObj.setString("u_doc_key"  , dObj.getString("u_doc_key"));
    				idf_DObj.setString("u_org_path" , dObj.getString("u_folder_path")); //이관 전 경로
      				
      				idf_PObj.save();
    			}

        	} catch (Exception e) {
    		    e.printStackTrace();
    		  } finally {
    		    if (idfSession != null) {
    		      if (idfSession.isTransactionActive()) {
    		        idfSession.abortTrans();
    		      }
    		      if (idfSession.isConnected()) {
    		    	  idfSession.disconnect();
    		      }
    		    }
    		  }	
        }        
        
        //제한건수가 넘는 요청건에 대해서 Batch 작업을 요청한다
        @Override
        public void addMcdBatchObjects(UserSession userSession, DPath dto ) throws Exception{
        	IDfSession idfSession = null;
        	
    		//////////////////////////////////////////////  선택된 ....
    		List<String> FolList = dto.getSourceFolders();  //edms_folder에서 관리되는 대상 폴더들 
    		List<String> DocList = dto.getSourceFiles();    //edms_doc에 존재하는 대상 파일들
    		List<String> PjtList = dto.getSourcePjts();     //edms_project에 존재하는 대상 프로젝트들 
    		List<String> RscList = dto.getSourceRscs();    //edms_research에 존재하는 대상 연구과제들
    		//////////////////////////////////////////////  end
        	
        	try {
        		idfSession = this.getIdfSession(userSession);
    		 	if (!idfSession.isTransactionActive()) {
    		 	    idfSession.beginTrans();
    		 	}
    		 	
    		 	Map<String, String> typeMap=new HashMap<String,String>();
    		 	//D:부서함, W:결재폴더, I:중요문서함, P:프로젝트, R:연구과제, S:공유/협업, O:조직함'),
    		 	
    		 	typeMap.put("C", "O");//조직함(회사)
    		 	typeMap.put("M", "O");//조직함(관리)
    		 	typeMap.put("D", "D");//부서함
    		 	typeMap.put("DI", "I");//중요부서함
    		 	typeMap.put("P", "P");//프로젝트함
    		 	typeMap.put("R", "R");//연구과제
    		 	typeMap.put("S", "S");//연구과제
    		 	
    			IDfPersistentObject	idf_PObj	= null;
    			
    			String sReqId="";

	  			//--------------------------------------------------
	  			// 문서권한 기본(edms_batch_mcd) 정보 생성
	  			//--------------------------------------------------
  				idf_PObj = idfSession.newObject("edms_batch_mcd");
  				
  				String srcGubun=dto.getSourceGubun().substring(0,2);
  				srcGubun = typeMap.get(srcGubun)==null? typeMap.get(srcGubun.substring(0,1)):typeMap.get(srcGubun);

  				String tgtGubun=dto.getTargetGubun().substring(0,2);
  				tgtGubun = typeMap.get(tgtGubun)==null? typeMap.get(tgtGubun.substring(0,1)):typeMap.get(tgtGubun);
  				
  				idf_PObj.setString("u_req_user"	     , dto.getReqUser());
  				idf_PObj.setString("u_req_type"	     , dto.getUptPthGbn());
  				idf_PObj.setString("u_source_type"   , srcGubun);
  				idf_PObj.setString("u_send_cab_code" , dto.getSrcCabinetcode());
  				
  				if(!dto.getUptPthGbn().equals("D")) {  //삭제가 아닐때만 세팅하는 항목들
  				    idf_PObj.setString("u_target_type"	 , tgtGubun);
  				    idf_PObj.setString("u_rcev_cab_code" , dto.getTgCabinetcode());
  				    
  				    if(dto.getTgCabinetcode().equals( dto.getTargetDboxId())) {
  				    	idf_PObj.setString("u_target_fol_id" , " ");
  				    }else {
  				    	String s_RobjectId=dto.getTargetDboxId();

  				    	if(!s_RobjectId.equals("")) {
  				    		if(s_RobjectId.substring(0,1).equals("p"))
  				    			s_RobjectId = idfSession.getIdByQualification("edms_project where u_pjt_code='" + s_RobjectId + "'").toString();
  				    		else if(s_RobjectId.substring(0,1).equals("r"))
  				    			s_RobjectId = idfSession.getIdByQualification("edms_research where u_rsch_code='" + s_RobjectId+ "'").toString();
  				    	}
  				        idf_PObj.setString("u_target_fol_id" , s_RobjectId);
  				    }
  				}

  				idf_PObj.setTime  ("u_req_date"      , new DfTime());
  				if(dto.getReqStatus()=="S") {//R:요청중, S:승인없이 이관'
  				    idf_PObj.setTime  ("u_job_date"	     , new DfTime());
  				}
  				idf_PObj.save();
  				sReqId = idf_PObj.getObjectId().getId();
                /** 상세 요청내역 등록  //////////////////////////////////////////////////////  Start*/
    			for(int i=0; i < PjtList.size(); i++)
    			{
    				if(PjtList.get(i).equals("")) continue;
    	  			//--------------------------------------------------
    	  			// 문서권한 기본(edms_batch_mcd) 정보 생성 :폴더
    	  			//--------------------------------------------------
      				idf_PObj = idfSession.newObject("edms_batch_mcd_objs");
      				
      				String s_RobjectId = idfSession.getIdByQualification("edms_project where u_pjt_code='" + PjtList.get(i) + "'").toString();
      				idf_PObj.setString("u_req_id"   , sReqId    );
      				idf_PObj.setString("u_obj_type"	, "P"       );  //프로젝트
      				idf_PObj.setString("u_obj_id"   , s_RobjectId );//PjtList.get(i));
      				idf_PObj.save();
    			}
    			for(int i=0; i < RscList.size(); i++)
    			{
    				if(RscList.get(i).equals("")) continue;
    	  			//--------------------------------------------------
    	  			// 문서권한 기본(edms_batch_mcd) 정보 생성 :폴더
    	  			//--------------------------------------------------
      				idf_PObj = idfSession.newObject("edms_batch_mcd_objs");
      				String s_RobjectId = idfSession.getIdByQualification("edms_research where u_rsch_code='" + RscList.get(i) + "'").toString();
      				
      				idf_PObj.setString("u_req_id"   , sReqId    );
      				idf_PObj.setString("u_obj_type"	, "R"       );  //연구과제
      				idf_PObj.setString("u_obj_id"   , s_RobjectId );//RscList.get(i));
      				idf_PObj.save();
    			}
    			for(int i=0; i < FolList.size(); i++)
    			{
    				if(FolList.get(i).equals("")) continue;
    	  			//--------------------------------------------------
    	  			// 문서권한 기본(edms_batch_mcd) 정보 생성 :폴더
    	  			//--------------------------------------------------
      				idf_PObj = idfSession.newObject("edms_batch_mcd_objs");
      				
      				idf_PObj.setString("u_req_id"   , sReqId    );
      				idf_PObj.setString("u_obj_type"	, "F"       );  //폴더
      				idf_PObj.setString("u_obj_id"   , FolList.get(i));
      				idf_PObj.save();
    			}
    			for(int i=0; i < DocList.size(); i++)
    			{
    				if(DocList.get(i).equals("")) continue;
    	  			//--------------------------------------------------
    	  			// 문서권한 기본(edms_batch_mcd) 정보 생성 : 문서
    	  			//--------------------------------------------------
      				idf_PObj = idfSession.newObject("edms_batch_mcd_objs");
      				idf_PObj.setString("u_req_id"   , sReqId    );
      				idf_PObj.setString("u_obj_type"	, "D");  //문서
      				String ls_RobjectId 	= DCTMUtils.getCurrentObjectID(idfSession, DocList.get(i)); //문서의 최신버전 r_object_id를 가져온다.
      				
      				IDfDocument idfObj = (IDfDocument)idfSession.getObject(new DfId(ls_RobjectId));
      				
      				idf_PObj.setString("u_obj_id"   , idfObj.getString("u_doc_key"));//DocList.get(i));  //문서 Key값을 입력
      				
      				idf_PObj.save();
    			}
    			/** 상세 요청내역 등록  //////////////////////////////////////////////////////  End */
    		    idfSession.commitTrans();
    		    
    		  } catch (Exception e) {
    		    e.printStackTrace();
    		  } finally {
    		    if (idfSession != null) {
    		      if (idfSession.isTransactionActive()) {
    		        idfSession.abortTrans();
    		      }
    		      if (idfSession.isConnected()) {
    		    	  idfSession.disconnect();
    		      }
    		    }
    		  }
    			
        }
        
      	//폴더 오브젝트 처리
      	@Override
      	public String shareFol(UserSession userSession,
      			              DPath dto,
      			             List<String> sFolArray
      			             ) throws Exception
      	{ 
            IDfSession idfSession = null;
            String ps_FolId="";

            String				s_Dql		= ""; 
     		IDfCollection		idf_Col		= null;
      		IDfPersistentObject	idf_PObj	= null;
        	
        	try {
        		idfSession = this.getIdfSession(userSession);
    		 	if (!idfSession.isTransactionActive()) {
    		 	    idfSession.beginTrans();
    		 	}      		
	      		
	      		//공유폴더에 권한을 가진 사용자리스트를 조회해서 
    			s_Dql = "select * from edms_auth_share " +
    						" where u_obj_id = '" + dto.getTargetDboxId() + "' " ;
    				
    			idf_Col = DCTMUtils.getCollectionByDQL(idfSession, s_Dql, DfQuery.DF_QUERY);
      	 
      			for (String sFolId : sFolArray) {//공유하려는 폴더에 권한으로 추가한다
      				ps_FolId=sFolId;
      			
      				while(idf_Col != null && idf_Col.next())
        			{
        	  			//--------------------------------------------------
        	  			// 문서권한 기본(edms_auth_base) 정보 생성
        	  			//--------------------------------------------------
      					
	      			    //edms_auth_base_share 관련 정보 지움
	          			idf_PObj = RegistAuthShareDto.toIDfPersistentObject(
	        		    		idfSession, 
	        		    		RegistAuthShareDto.builder()
	        		    			.uObjId(ps_FolId)
	        		    			
	        			    		.uAuthorId(idf_Col.getString("u_author_id"))
	        			    		
	        			    		.uAuthorType(idf_Col.getString("u_author_type"))
	        			    		.uPermitType("R")  //읽기권한
	        			    		.build()
	        		    );	        	  	  			
	        	  		idf_PObj.save();  
        			 }
			      }

    			   if(idf_Col != null) idf_Col.close(); 
        	    }catch(Exception e)
	      		{
	      			return "폴더(하위 오브젝트) 권한 정보 변경 오류 발생 : " + ps_FolId + "\n" + e.toString();
	      		}
	      		finally
	      		{
	    			if(idf_Col != null) idf_Col.close(); 
	    			
	      			if (idfSession != null) {
	      		      if (idfSession.isTransactionActive()) {
	      		        idfSession.abortTrans();
	      		      }
	      		      if (idfSession.isConnected()) {
	    		    	  idfSession.disconnect();
	      		      }
	      		    }
	      		}
	      		return "";
      	}

      	/**
    	 * 특정 폴더의 경로 조회 : DCTM 사용 
    	 * @param ps_FolId : 폴더아이디
    	 * @param pb_First : 재귀호출 대비 최초 호출 여부
    	 * @return 폴더 경로
    	 */
      	@Override
    	public String GetFolderPathFromDCTM(UserSession userSession, String ps_FolId, boolean pb_First) throws Exception
    	{
    		String	s_Path	= "";
    		String	s_UpId	= "";
    		String	s_FolName	= "";
    		IDfPersistentObject idf_PObj = null;
    		
    		if(ps_FolId.equals("")) return "/";
    		
    		try
    		{
    			IDfSession idfSession = this.getIdfSession(userSession);
    			
    			idf_PObj = idfSession.getObject(new DfId(ps_FolId));
    			s_UpId = idf_PObj.getString("u_up_fol_id");
    			s_FolName = idf_PObj.getString("u_fol_name");
    			
    			if(s_UpId.equals(""))
    				s_Path = s_FolName;
    			else
    				s_Path = GetFolderPathFromDCTM(userSession, s_UpId, false) + "/" + s_FolName;
    				
    		}
    		catch(Exception e)
    		{
    			e.printStackTrace();
    		}
    		finally
    		{
    		}
    		return (pb_First ? "/" : "") + s_Path;
    	}      	
}
