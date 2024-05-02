package com.dongkuksystems.dbox.services.external;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.http.MediaType;
import com.documentum.fc.client.DfIdNotFoundException;
import com.documentum.fc.client.DfQuery;
import com.documentum.fc.client.IDfACL;
import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfDocument;
import com.documentum.fc.client.IDfPersistentObject;
import com.documentum.fc.client.IDfQuery;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.common.DfId;
import com.documentum.fc.common.DfTime;
import com.documentum.fc.common.IDfTime;
import com.dongkuksystems.dbox.constants.AclTemplate;
import com.dongkuksystems.dbox.constants.DCTMConstants;
import com.dongkuksystems.dbox.constants.FolderType;
import com.dongkuksystems.dbox.constants.GrantedLevels;
import com.dongkuksystems.dbox.constants.SecLevelCode;
import com.dongkuksystems.dbox.daos.type.manager.preservationperiod.PreservationPeriodDao;
import com.dongkuksystems.dbox.errors.ForbiddenException;
import com.dongkuksystems.dbox.errors.NotFoundException;
import com.dongkuksystems.dbox.models.common.UserSession;
import com.dongkuksystems.dbox.models.dto.etc.CustomInputStreamResource;
import com.dongkuksystems.dbox.models.dto.type.agree.DboxAttaDocDto;
import com.dongkuksystems.dbox.models.dto.type.agree.RegistAgreeDto;
import com.dongkuksystems.dbox.models.dto.type.agree.WfDocDto;
import com.dongkuksystems.dbox.models.dto.type.manager.preservationperiod.PreservationPeriodDto;
import com.dongkuksystems.dbox.models.dto.type.upload.AttachedFile;
import com.dongkuksystems.dbox.models.table.etc.VDept;
import com.dongkuksystems.dbox.models.table.etc.VUser;
import com.dongkuksystems.dbox.models.table.log.LogDoc;
import com.dongkuksystems.dbox.models.type.agree.Agree;
import com.dongkuksystems.dbox.models.type.doc.DocLink;
import com.dongkuksystems.dbox.repositories.redis.RedisRepository;
import com.dongkuksystems.dbox.securities.JWT;
import com.dongkuksystems.dbox.services.AbstractCommonService;
import com.dongkuksystems.dbox.services.agent.AgentService;
import com.dongkuksystems.dbox.services.auth.AuthService;
import com.dongkuksystems.dbox.services.cache.CacheService;
import com.dongkuksystems.dbox.services.code.CodeService;
import com.dongkuksystems.dbox.services.data.DataService;
import com.dongkuksystems.dbox.services.dept.GwDeptService;
import com.dongkuksystems.dbox.services.user.UserService;
import com.dongkuksystems.dbox.utils.RestTemplateUtils;
import com.dongkuksystems.dbox.utils.dctm.DCTMUtils;
import com.dongkuksystems.dbox.daos.type.agree.AgreeDao;

@Service
public class ExternalServiceImpl extends AbstractCommonService implements ExternalService {
  @Value("${jwt.token.header}")
  private String tokenHeader;
  @Value("${linkfile.doc-key}")
  private String docKey;
  @Value("${linkfile.sec-level}")
  private String secLevel;
  @Value("${linkfile.preserv-flag}")
  private String preservFlag;
  @Value("${linkfile.text}")
  private String linkfileText;

  
  
  @Value("${dbox.url}")
  private String dboxUrl;
  
  private final DataService dataService;
  private final AuthService authService;  
  private final RedisRepository redisRepository;
  private final CacheService cacheService;
  private final CodeService codeService;
  private final GwDeptService gwDeptService;
  private final AgentService agentService;
  
  private final AgreeDao agreeDao;
  
  private final JWT jwt;
  
  private final PreservationPeriodDao psrvPeriodDao;
  
  //private final UserService userService;
  
  private Map<String, String> psrvPMap; //보존년한맵
  private Map<String, String> psrvPMapDf; //보존년한맵

  
  public ExternalServiceImpl(DataService dataService, AuthService authService, RedisRepository redisRepository
		  , CacheService cacheService, CodeService codeService, JWT jwt	 , GwDeptService gwDeptService, AgreeDao agreeDao,PreservationPeriodDao psrvPeriodDao
		  , AgentService agentService)
		//  ,  UserService userService) 
       {
	    this.dataService = dataService;
	    this.authService = authService;
	    this.redisRepository = redisRepository;
	    this.cacheService = cacheService;
	    this.codeService = codeService;
	    this.jwt = jwt;
	    this.gwDeptService= gwDeptService;
	    this.agreeDao = agreeDao;
		this.psrvPMap  = new HashMap<String, String>();
		this.psrvPMapDf= new HashMap<String, String>();
	    this.psrvPeriodDao  = psrvPeriodDao;
	    //this.userService   = userService;
	    this.agentService=agentService;
  }

  
  @Override
  public CustomInputStreamResource createExternalLinkFile(String docId) throws Exception {
    String path = linkfileText.replace(docKey, docId);
    InputStream is = new ByteArrayInputStream(path.getBytes(StandardCharsets.UTF_8));
    String tmpFilename = "TestTextFile" + ".html";
    CustomInputStreamResource rst = new CustomInputStreamResource(is, is.available(), tmpFilename);
    return rst;
  }
	    

	@Override
	public String createExternalLinkFileByFind( UserSession userSession, String docId) throws Exception {
	  
	    String apiKey         = null;
	    String loginId        = null;
	    IDfSession idfSession = this.getIdfSession(userSession);
	    
	    CustomInputStreamResource customInputStream = null;
 
	    String  resultString=",";
	    IDfSysObject idf_PObj = null;
	    IDfDocument idf_DObj = null;
	    
	    if (DfId.isObjectId(docId)) {
	    	idf_PObj = (IDfSysObject)idfSession.getObject(new DfId(docId)); 
	        idf_DObj = (IDfDocument)idfSession.getObject(new DfId(docId));
		    try {
	
			        String lsSysPath = agentService.makeSyspath(docId, "dbox");
			        resultString  = linkfileText.replaceAll(docKey,      docId+"?approveid=&syspath="+ lsSysPath);
			        
			        if(null !=idf_DObj.getString("u_preserve_flag")) 
			            resultString  = resultString.replace(preservFlag, idf_DObj.getString("u_preserve_flag")); //보존년한
			        else
			        	resultString  = resultString.replace(preservFlag, "15"); //보존년한(기본)
			        if(null !=idf_DObj.getString("u_sec_level"))
			            resultString  = resultString.replace(secLevel,    idf_DObj.getString("u_sec_level"));     //보안등급
			        else
			        	resultString  = resultString.replace(preservFlag, "T"); //보안등급(default)
	
		    	    String linkFileNm = idf_DObj.getString("object_name");
		    	    String s_Extr     = idf_DObj.getString("u_file_ext");
		    	    linkFileNm  = linkFileNm.replace("."+s_Extr, "");
			        
		    	    String s_Ver      = idf_PObj.getVersionLabel(0);//getVersionLabels().getImplicitVersionLabel();
		    	    
		    	    String s_RegUser  = userSession.getUser().getDisplayName();
			        String s_Cabinet  = userSession.getUser().getOrgNm();
			        
			        s_Cabinet 	= StringUtils.stripToEmpty(s_Cabinet.replaceAll("/", "-"));
/*			        
			        String[] ls_Splash= s_Cabinet.split("/");
			        if(ls_Splash.length > 0) {
			        	for(int isCnt=0; isCnt < ls_Splash.length; isCnt++) {
			        	    s_Cabinet=s_Cabinet.replace("/", "_");
			            }
			        }
*/			        
	/*		        
			        String s_RegUser  = idf_DObj.getString("u_reg_user"); //작성자
			        String s_Cabinet  = idf_DObj.getString("u_cabinet_code"); //작성자
			        Optional<VUser> userOpt = userService.selectOneByUserId( s_RegUser);
			        if(userOpt.isPresent()) {
			        	s_RegUser = userOpt.get().getDisplayName();
			        }
			        String s_OrgId = gwDeptService.selectOrgIdByCabinetcode( s_Cabinet);
			        VDept dept = gwDeptService.selectDeptByOrgId(s_OrgId);
			        if(null !=dept) s_Cabinet = dept.getOrgNm();
	*/		        
			        String  s_Today = new DfTime().toString();
			        //s_Today = s_Today.replace("-", "").replace("-", "");
			        linkFileNm = linkFileNm+"_"+s_Cabinet+"_"+s_Ver+"_"+ s_Today.substring(0,10) +"_"+s_RegUser+"."+s_Extr+".html";
			        resultString +="@JJ@"+ linkFileNm;	// 버전
			        //링크파일 : 파일명_팀명_버전_날짜_작성자.xlsx.html  )
	/*
	 * 최종으로 보낼 내용 
	        <meta http-equiv='refresh' content='0.01;url=http://dbox-test.dongkuk.com/#/download/__DOC_ID__'>
	        <meta name="seclevel" content=__SEC_LEVEL__> <!-- S극비, T부서한, C사내, G그룹사내 -->
	        <meta name="preserveflag" content=__PRESERV_FLAG__>
	 */
			        //resultString= path+lsPreservFlag+ lsSecLevel;
		      } catch (DfIdNotFoundException e) {
			    	resultString="문서 권한 없음[Documentum]";
		      } catch (Exception e) {
		    	     e.printStackTrace();
		    	    resultString="기타 예외사항";
		      } finally {
		        if (idfSession != null) {
		          if (idfSession.isTransactionActive()) {
		              idfSession.abortTrans();
		          }
		          if (idfSession.isConnected()) {
	                  sessionRelease(userSession.getUser().getUserId(), idfSession);
		          }
		        }
		     }
	     }else {
	    	 resultString="문서ID가 적절치 않음["+docId+"]";
	     }
	     return resultString;
	}	    
  
  @Override
  public String registerAgree(UserSession userSession, RegistAgreeDto dto) throws Exception {

    IDfSession idfSession = this.getIdfSession(userSession);
    IDfPersistentObject idf_PObj = null;
    JSONObject jsonObject =null;
    try {
      List<Agree> userCheck = agreeDao.selectListUserId(userSession.getUser().getUserId());
   
      idfSession.beginTrans();
       if(!CollectionUtils.isEmpty(userCheck)) {
         if(userCheck.get(0).getUAgreeType().equals("U")) {
           dto.setRObjectId(userCheck.get(0).getRObjectId());
           idf_PObj = RegistAgreeDto.register(idfSession, dto);
         }else {
           dto.setRObjectId(userCheck.get(0).getRObjectId());
         idf_PObj = RegistAgreeDto.register(idfSession, dto);
         }
       }else { 
         idf_PObj = RegistAgreeDto.register(idfSession, dto);
       }
      idf_PObj.save();
      
      RestTemplateUtils restUtils = new RestTemplateUtils();
      HttpHeaders headers = new HttpHeaders();
      headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
      headers.add("Content-Type", "application/json;utf-8");
      headers.add("Pragma", "no-cache");
      headers.add("Expires", "0");
        
      String lsUrl = "http://niris.dongkuk.com/api/insertDboxAgreeApi.json";
      String lsParameters = "?apiKey=060kkve5kbbiflxia5kgqte47bofag2o";
      lsParameters += "&systemId=" + "DBOX_AGREE";
      lsParameters += dto.getUAgreeType().equals("T")?"&typeNo=1":"&typeNo=2"; //AgreeType: T:부서장동의(자동승인), U:사용자동의(프리패스)') ,  N-Iris쪽 ("1: 자동승인 / 2: 프리패스)

      Map<String, String> body = new HashMap<String, String>();

      body.put("userId", userSession.getDUserId());
      body.put("agreeYn", dto.getUAgreeYn());
      body.put("comCd",  userSession.getUser().getComOrgId());
      
      ResponseEntity<String> response =restUtils.post(lsUrl+lsParameters, headers, body, String.class);

      //System.out.println(response.getBody());
      JSONParser jsonParser = new JSONParser(); 
      jsonObject = (JSONObject) jsonParser.parse(response.getBody().toString()); 
      if (Integer.parseInt(response.getStatusCode().toString()) > 201) {
        throw new RuntimeException("그룹웨어에 동의서 정보 등록 실패");
      }
      idfSession.commitTrans();
    } catch (Exception e) {
      throw e;
    } finally {
      if (idfSession != null) {
        if (idfSession.isTransactionActive()) {
          idfSession.abortTrans();
        }
        if (idfSession.isConnected()) {
            sessionRelease(userSession.getUser().getUserId(), idfSession);
        }
      }
    }
    return JSONObject.toJSONString(jsonObject);
  }
  

  
@Override
@Transactional
public String createWfDoc(UserSession userSession, IDfSession idfSess, String path, String socialPerId,
		WfDocDto dto, AttachedFile aFile, String folderName) throws Exception {

   //IDfSession idfSession = idfSess != null ? idfSess : this.getIdfSession(userSession);
   IDfSession idfSession  = DCTMUtils.getAdminSession();	  
  try { 
	  if (!idfSession.isTransactionActive()) {
	      idfSession.beginTrans();
	  }

	  
    String s_ComId   = gwDeptService.selectComCodeByCabinetCode( dto.getUCabinetCode()).toLowerCase();
	PreservationPeriodDto psrvDto = psrvPeriodDao.selectOneByComCode(s_ComId.toUpperCase());
	psrvPMap.put("S", psrvDto.getUSecSYear());
	psrvPMap.put("C", psrvDto.getUSecCYear());
	psrvPMap.put("G", psrvDto.getUSecGYear());
	psrvPMap.put("T", psrvDto.getUSecTYear());
	psrvPMap.put("PJT", psrvDto.getUPjtEverFlag());
	  
	//보존년한 기본
	psrvPMapDf.put("S", "20");
	psrvPMapDf.put("C", "10");
	psrvPMapDf.put("G", "10");
	psrvPMapDf.put("T", "15");
	

    IDfDocument         idfDoc     = null;
    IDfPersistentObject idfDocList = null;
    String              loginId    = null;
  
    String s_CabinetCode  = dto.getUCabinetCode();
    String folderType     = dto.getApprovalState().equals("F")?FolderType.DWY.toString():FolderType.DWT.toString();  //최종승인이 아니면  DWT 전자결재 임시폴더
    String nfolderName  = dto.getApprovalState().equals("F")?folderName:"temp";

    String s_DWFolderId = makeDWVFolder(idfSession, s_CabinetCode, folderType, nfolderName, folderName, userSession.getUser().getUserId());

    String s_DCTMFolderId   = DCTMUtils.makeEDMFolder(idfSession);
    System.out.println("Mola_a=="+aFile.getFileExtention());
    //aFile.setFileExtention(fileExtention);

    aFile.setDcmtContentType(cacheService.selectDmFormats());
    System.out.println("Mola_a=="+aFile.getDContentType());
    
    String lsContentType=aFile.getDContentType();
    dto.setUCreateUser(userSession.getUser().getUserId());    
    
    
	Map<String, String> formatChkMap = null; // drm적용 대상 확장자
    formatChkMap = codeService.getClosedFormatCodeMap();
    String s_Extr=aFile.getFileExtention();

    
    if (formatChkMap.containsKey(s_Extr.toUpperCase())  ) { //최종승인이 아니면 기본적인 ACL부여
        dto.setUDocStatus("C");
    }else {
    	dto.setUDocStatus("L");
    }
    //idfDoc = (IDfDocument) idfSession.newObject("edms_doc");
    idfDoc = dto.createWfDoc(idfSession, dto, aFile);
    
    System.out.println("Mola_a=="+lsContentType);
    
    idfDoc.setContentType(lsContentType);
    if(lsContentType.equals("unknown")) {
    	idfDoc.setContentType(DCTMUtils.getFormatByFileExt(idfSession, DCTMUtils.getFileExtByFileName(idfDoc.getString("title"))));	
    }else {
        idfDoc.setContentType(lsContentType);
    }

    
    // 문서명
    //idfDoc.setObjectName(aFile.get.getOriginalFileName());
    //idfDoc.setTitle(aFile.getOriginalFileName());
    // 파일 set
    idfDoc.setFile(aFile.getTmpFileName());
    // DCTM 폴더(화면에 보이는 부서 폴더 아님)
    
    idfDoc.unlink( idfDoc.getFolderId(idfDoc.getFolderIdCount()-1).toString());
    idfDoc.link(s_DCTMFolderId);

    //idfDoc.setString("u_folder_path", path); //u_fol_id에 해당하는 값을 edms_folder에서 가져다 업데이트해주므로 변경할 필요없음
    idfDoc.setString("u_fol_id",      s_DWFolderId);  // 부서폴더
    idfDoc.setString("u_reg_user",    userSession.getUser().getUserId());

    if (formatChkMap.containsKey(s_Extr.toUpperCase()) || dto.getApprovalState().equals("F") ) { //최종승인이 아니면 기본적인 ACL부여
    	
    	String ls_SecLevel=dto.getApprovalLevel(); //결재 보안등급
        idfDoc.setACLName("a_"+ s_CabinetCode+ "_d_g_"+ ls_SecLevel.toLowerCase()+"_y" );
        idfDoc.setString("u_doc_status"    , "C"); //파일 상태를 closed로 바꿔준다
        idfDoc.setString("u_closed_date"    , (new DfTime()).toString());
        idfDoc.setString("u_closer"    , userSession.getUser().getUserId());        
    } else {
	  //idfDoc.setString("u_doc_status"    , "L");
      if (SecLevelCode.GROUP.getValue().equals(dto.getApprovalLevel())) {
          idfDoc.setACLName(MessageFormat.format(AclTemplate.DEPT_WF_ACL_GROUP.getValue(), s_CabinetCode));
          
      } else if (SecLevelCode.SEC.getValue().equals(dto.getApprovalLevel())) {
          idfDoc.setACLName(MessageFormat.format(AclTemplate.DEPT_WF_ACL_TEAM.getValue(), s_CabinetCode));
      } else if (SecLevelCode.COMPANY.getValue().equals(dto.getApprovalLevel())) {
          idfDoc.setACLName(MessageFormat.format(AclTemplate.DEPT_WF_ACL_COM.getValue(), s_CabinetCode));
          
      } else {
          idfDoc.setACLName(MessageFormat.format(AclTemplate.DEPT_WF_ACL_TEAM.getValue(), s_CabinetCode));
      }
    } 
    
    String s_PreservFlag= psrvPMap.get( idfDoc.getString("u_sec_level"));
	if(null==s_PreservFlag) s_PreservFlag= psrvPMapDf.get(idfDoc.getString("u_sec_level")); //등록된게 없으면 정책서에서 기술한 기본값
	if(dto.getPreserverYear() == dto.getYoungGu()) {
	       s_PreservFlag="0";
	}
	if(null==s_PreservFlag) s_PreservFlag= idfDoc.getString("u_preserve_flag");
	int li_PreservYear=Integer.parseInt(s_PreservFlag)==0?9999:Integer.parseInt(s_PreservFlag);
	idfDoc.setInt("u_preserve_flag", Integer.parseInt( s_PreservFlag) );  //보존연한
	
	if(idfDoc.getString("u_doc_status").equals("C")) {
		//idfDoc.setInt("u_preserve_flag",  dto.getPreserverYear());  //보존연한
		IDfTime startDate = new DfTime() ;
		// Convert the expiration date to a calendar object.
		GregorianCalendar cal = new GregorianCalendar(startDate.getYear(), startDate.getMonth(),   startDate.getDay() );  
		// Add the number of months ­1 (months start counting from 0).
		cal.add(GregorianCalendar.MONTH, (12 * li_PreservYear) -1) ;
		IDfTime expireDate = new DfTime (cal.getTime()) ;
	     if(li_PreservYear==9999)
	    	 idfDoc.setString("u_expired_date",  "9999-12-31 00:00:00");
	     else
	    	 idfDoc.setString("u_expired_date",  expireDate.toString());
	}
      
    idfDoc.save();
    ///////////////////////

    String sOwnSrDeptOrgId = gwDeptService.selectOrgIdByCabinetcode(dto.getUCabinetCode());
	  LogDoc logDoc = LogDoc.builder()
		          .uJobCode( "RE")
		          .uDocId(idfDoc.getString("r_object_id"))
		          .uDocKey(idfDoc.getString("u_doc_key"))
		          .uDocName(idfDoc.getString("title").replaceAll("'", "''"))
		          .uDocVersion(Integer.parseInt(idfDoc.getVersionLabel(0).substring(0, idfDoc.getVersionLabel(0).indexOf(".")))+"")
		          .uFileSize(Long.parseLong(idfDoc.getString("r_content_size")))
		          .uOwnDeptcode(sOwnSrDeptOrgId)
		          .uActDeptCode(userSession.getUser().getOrgId())
		          .uJobUser(userSession.getUser().getUserId())
		          .uJobUserType(dto.getUJobUserType()==null ? "P" : dto.getUJobUserType()) //작업자구분(P:개인, D:부서관리자, C:사별관리자, G:그룹관리자, S:시스템)
		          .uDocStatus(idfDoc.getString("u_doc_status"))
		          .uSecLevel(idfDoc.getString("u_sec_level"))
		          .uCabinetCode(idfDoc.getString("u_cabinet_code"))
		          .uJobGubun("P") //작업구분 (등록=[D:Dbox, P:PC], 수정=[W:덮어쓰기, V:버전생성], 조회=[D:Dbox, L:링크], 첨부=[O:원문, H:HTML], 보안등급 변경=[U:상향, D:하향], 복호화 반출=[S:자가승인, A:자동스인, F:프리패스])
		          .uUserIp(dto.getURequestIp())							// 받아야함.
		          .uAttachSystem("APPR")
		          .build();
		      insertLog(logDoc);
		      
    logDoc = LogDoc.builder()
			          .uJobCode( "AT")
			          .uDocId(idfDoc.getString("r_object_id"))
			          .uDocKey(idfDoc.getString("u_doc_key"))
			          .uDocName(idfDoc.getString("title").replaceAll("'", "''"))
			          .uDocVersion(Integer.parseInt(idfDoc.getVersionLabel(0).substring(0, idfDoc.getVersionLabel(0).indexOf(".")))+"")
			          .uFileSize(Long.parseLong(idfDoc.getString("r_content_size")))
			          .uOwnDeptcode(sOwnSrDeptOrgId)
			          .uActDeptCode(userSession.getUser().getOrgId())
			          .uJobUser(userSession.getUser().getUserId())
			          .uJobUserType(dto.getUJobUserType()==null ? "P" : dto.getUJobUserType()) //작업자구분(P:개인, D:부서관리자, C:사별관리자, G:그룹관리자, S:시스템)
			          .uDocStatus(idfDoc.getString("u_doc_status"))
			          .uSecLevel(idfDoc.getString("u_sec_level"))
			          .uCabinetCode(idfDoc.getString("u_cabinet_code"))
			          .uJobGubun("H") //O:원본, H:html파일
			          .uUserIp(dto.getURequestIp())							// 받아야함.
			          .uAttachSystem("APPR")
			          .build();
			      insertLog(logDoc);    

      logDoc = LogDoc.builder()
				          .uJobCode( "AP")
				          .uDocId(idfDoc.getString("r_object_id"))
				          .uDocKey(idfDoc.getString("u_doc_key"))
				          .uDocName(idfDoc.getString("title").replaceAll("'", "''"))
				          .uDocVersion(Integer.parseInt(idfDoc.getVersionLabel(0).substring(0, idfDoc.getVersionLabel(0).indexOf(".")))+"")
				          .uFileSize(Long.parseLong(idfDoc.getString("r_content_size")))
				          .uOwnDeptcode(sOwnSrDeptOrgId)
				          .uActDeptCode(userSession.getUser().getOrgId())
				          .uJobUser(userSession.getUser().getUserId())
				          .uJobUserType(dto.getUJobUserType()==null ? "P" : dto.getUJobUserType()) //작업자구분(P:개인, D:부서관리자, C:사별관리자, G:그룹관리자, S:시스템)
				          .uDocStatus(idfDoc.getString("u_doc_status"))
				          .uSecLevel(idfDoc.getString("u_sec_level"))
				          .uCabinetCode(idfDoc.getString("u_cabinet_code"))
				          .uJobGubun("H") //O:원본, H:html파일
				          .uUserIp(dto.getURequestIp())							// 받아야함.
				          .uAttachSystem("APPR")
				          .build();
				      insertLog(logDoc);    
			      
    idfSession.commitTrans();
    dto.setDboxId(idfDoc.getObjectId().toString());
    
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

  return dto.getDboxId();
}




@Override
@Transactional
public String updateWfDoc(UserSession userSession, IDfSession idfSess, String path, String socialPerId,
	    WfDocDto dto, AttachedFile aFile, String folderName) throws Exception {

   //IDfSession idfSession = idfSess != null ? idfSess : this.getIdfSession(userSession);
   IDfSession idfSession  = DCTMUtils.getAdminSession();
   IDfCollection idf_Col = null;
   
   String returnMsg="";
   try { 
 	  if (!idfSession.isTransactionActive()) {
 	      idfSession.beginTrans();
 	  }
	
 	  String s_ComId   = gwDeptService.selectComCodeByCabinetCode( dto.getUCabinetCode()).toLowerCase();
 	 s_ComId = s_ComId.toUpperCase();
 	  PreservationPeriodDto psrvDto = psrvPeriodDao.selectOneByComCode(s_ComId);
 	  psrvPMap.put("S", psrvDto.getUSecSYear());
 	  psrvPMap.put("C", psrvDto.getUSecCYear());
 	  psrvPMap.put("G", psrvDto.getUSecGYear());
 	  psrvPMap.put("T", psrvDto.getUSecTYear());
 	  psrvPMap.put("PJT", psrvDto.getUPjtEverFlag());
 		  
      //보존년한 기본
 	  psrvPMapDf.put("S", "20");
 	  psrvPMapDf.put("C", "10");
 	  psrvPMapDf.put("G", "10");
 	  psrvPMapDf.put("T", "15");
 	  
 	  Map<String, Integer> secLevelMap = new HashMap<String, Integer>();
      secLevelMap.put("S", 4);
      secLevelMap.put("T", 3);
      secLevelMap.put("C", 2);
      secLevelMap.put("G", 1);

 		
      IDfPersistentObject idfDocLink= null;

	  String s_CabinetCode  = dto.getUCabinetCode();
	  dto.setUCreateUser(userSession.getUser().getUserId());
	/////////////////////////        
	  String s_ObjId = dto.getDboxId();

	  Map<String, String> formatChkMap = null; // drm적용 대상 확장자
      formatChkMap = codeService.getClosedFormatCodeMap();
	  
	  if(s_ObjId.equals("")) {
		  
		  //해당 결재키의 edms_auth_base 관련 정보 지움
		  //String s_Dql = "select distinct u_obj_id from edms_auth_base where u_ext_key='" + dto.getApprovalId()+"'" ;
		  //String s_Dql = "select distinct r_object_id as u_obj_id from edms_doc_r where u_wf_key='" + dto.getApprovalId()+"'" ;
		  String s_Dql = "select distinct r_object_id as u_obj_id from edms_doc_sp where r_object_id in(select distinct r_object_id from edms_doc_r where u_wf_key='"+ dto.getApprovalId()+"') and i_has_folder=1";
		  
		  idf_Col = DCTMUtils.getCollectionByDQL(idfSession, s_Dql, DfQuery.DF_QUERY);
		  IDfCollection idf_Colb =null;
		  while(idf_Col != null && idf_Col.next()) {
			  
			  s_ObjId=idf_Col.getString("u_obj_id");
			  
			  boolean b_SecLevelChange=true;  //보안레벨을 변경할 수 있는지 확인
			  
			  if (DfId.isObjectId( s_ObjId)) {
				  
				  IDfDocument idfDoc = (IDfDocument)idfSession.getObject(new DfId(s_ObjId));
				  s_ObjId=idfDoc.getString("u_doc_key");
				  
		    	  String applovalLevel         = dto.getApprovalLevel();
			      applovalLevel = DCTMUtils.checkNullStringByObj(applovalLevel)==""? idfDoc.getString("u_sec_level"):applovalLevel;
			      dto.setApprovalLevel(applovalLevel);
				  
				  
				  if(null==dto.getApprovalLevel() || dto.getApprovalLevel().equals("") || dto.getApprovalLevel().equals(" ")) {
					  dto.setApprovalLevel(  idfDoc.getString("u_sec_level") );
					  //if(!dto.getApprovalState().equals("X")) //반려가 아니면
					  //throw new RuntimeException("보안등급항목값이 전달되지 않았습니다");
			          //dto.setApprovalLevel( null==idfDoc.getString("u_sec_level")?"T": (idfDoc.getString("u_sec_level").equals("") || idfDoc.getString("u_sec_level").equals(" ")?"T":idfDoc.getString("u_sec_level")));
				  }
				  if(idfDoc.getString("u_delete_status").equals("D") || idfDoc.getString("u_delete_status").equals("E")) {
					  throw new RuntimeException("문서가 삭제되어첨부할 수 없습니다");
					  //'R:요청중, A:폐기승인, D:삭제(휴지통), E:삭제(보존년한), P:상위폴더 삭제에 의한 삭제, T:임시저장'),
				  }else  if(idfDoc.getString("u_delete_status").equals("P")) 
				  {
					  throw new RuntimeException("문서가 포함된 폴더가 삭제되어 첨부할 수 없습니다");
					  //'R:요청중, A:폐기승인, D:삭제(휴지통), E:삭제(보존년한), P:상위폴더 삭제에 의한 삭제, T:임시저장'),
				  }

				  if(idfDoc.isCheckedOut()) //편집중문서면 편집을 해제하고 결재 프로세스 수행
				  {
					  
					  System.out.println( "#MOLA_LOCK:"+ idfDoc.getLockOwner()+  " 편집중 문서, Lock해제하고 결재진행");
				      idfDoc.cancelCheckout();
				  }
				  
				  idfDoc.setString("u_wf_doc_yn", "Y");
				  
				  String preFolderId = idfDoc.getString("u_fol_id"); //전자결재 폴더로 변경전에 이전 폴더id를 edms_doc_link에 저장해놓기 위한 변수

				  if(null ==dto.getApprovalLevel() || dto.getApprovalLevel().equals("")) {
				      dto.setApprovalLevel(idfDoc.getString("u_sec_level"));
				  }
				  
				  if(idfDoc.getString("u_wf_doc_yn").equals("Y") && idfDoc.getString("u_doc_status").equals("C")){  //결재문서이면서 Closed된 문서인데 
					    String ls_UpFolId = idfDoc.getString("u_fol_id");
					    if(ls_UpFolId.equals("") || ls_UpFolId.equals(" ")) {
					    	b_SecLevelChange=true;
					    }else {
					    	if (DfId.isObjectId( ls_UpFolId)) {
					            IDfPersistentObject idfFol_Obj = idfSession.getObject(new DfId( ls_UpFolId));
					            if(idfFol_Obj.getString("u_fol_type").equals("DWY")) { //결재승인된 문서인 경우 
					            	b_SecLevelChange=false;// 기존에 결재승인된 자료를 재 첨부하는 경우 전자결재의 문서보안등급에 상관없이 최초결재 승인받은 보안등급을 유지한다
					            	dto.setApprovalLevel(idfDoc.getString("u_sec_level"));
					            }
					    	}
					    }
				  }
				  //반려 : closed상태가 아니면 closed처리하면서 close_date, closer등록
				  //     closed상태이면 다음 항목은 건드리지 않는다.(close_date, closer변경하지 않음)
				  idfDoc.setBoolean("u_ver_keep_flag"  , true); //버전유지여부 true
				  s_CabinetCode = idfDoc.getString("u_cabinet_code");
				  dto.setUCabinetCode(s_CabinetCode);
				  
				  String filderType     = dto.getApprovalState().equals("F")?FolderType.DWY.toString():FolderType.DWT.toString();  //최종승인이 아니면  DWT 전자결재 임시폴더
				  String nfolderName  = dto.getApprovalState().equals("F")?folderName:"temp";
				  String s_DWVFolderId = makeDWVFolder(idfSession, s_CabinetCode, filderType, nfolderName, folderName, userSession.getUser().getUserId());
				  
				  //System.out.println("MOLA: CabinetCode="+s_CabinetCode +"  DWVFolderId: "+ s_DWVFolderId+" 상태코드:"+  dto.getApprovalState());
			      if( !dto.getApprovalState().equals("X") ) {
			    	  if(b_SecLevelChange) {
				          idfDoc.setString("u_sec_level",  dto.getApprovalLevel()); //
			    	  }
			      }else {
			    	  if(b_SecLevelChange) {
				          idfDoc.setString("u_sec_level",  dto.getApprovalLevel()); //
			    	  }
			      }
				  
			      String s_Extr = idfDoc.getString("u_file_ext");
			      if (formatChkMap.containsKey(s_Extr.toUpperCase()) || dto.getApprovalState().equals("F")  || dto.getApprovalState().equals("X")) { //최종승인거나 반려이거나 기본 closed포맷 문서이면  ACL변경
			      	
			          if( !dto.getApprovalState().equals("X")) {
			        	  if(b_SecLevelChange) {
				              String ls_SecLevel=dto.getApprovalLevel(); //결재 보안등급
			                  idfDoc.setACLName("a_"+ s_CabinetCode+ "_d_g_"+ ls_SecLevel.toLowerCase()+"_y" );
			        	  }
			          }
			          if( b_SecLevelChange && !idfDoc.getString("u_doc_status").equals("C") ) {
			              idfDoc.setString("u_doc_status"    , "C"); //파일 상태를 closed로 바꿔준다
			              idfDoc.setString("u_closed_date"    , (new DfTime()).toString());
			              idfDoc.setString("u_closer"    , userSession.getUser().getUserId());   
			          }
			      } else {
			  	      //idfDoc.setString("u_doc_status"    , "L");
			    	  if(b_SecLevelChange) {
				          if (SecLevelCode.GROUP.getValue().equals(dto.getApprovalLevel())) {
				              idfDoc.setACLName(MessageFormat.format(AclTemplate.DEPT_WF_ACL_GROUP.getValue(), s_CabinetCode));
				            
				          } else if (SecLevelCode.SEC.getValue().equals(dto.getApprovalLevel())) {
				              idfDoc.setACLName(MessageFormat.format(AclTemplate.DEPT_WF_ACL_TEAM.getValue(), s_CabinetCode));
				              
				          } else if (SecLevelCode.COMPANY.getValue().equals(dto.getApprovalLevel())) {
				              idfDoc.setACLName(MessageFormat.format(AclTemplate.DEPT_WF_ACL_COM.getValue(), s_CabinetCode));
				            
				          } else { 
				              idfDoc.setACLName(MessageFormat.format(AclTemplate.DEPT_WF_ACL_TEAM.getValue(), s_CabinetCode));
				          }
			    	  }
			      } 
				  String s_USecLevel =dto.getApprovalLevel(); //idfDoc.getString("u_sec_level");  //문서의 보안등급(변경전)
				  				  
				  String s_PreservFlag= psrvPMap.get( s_USecLevel );//idfDoc.getString("u_sec_level"));
				  if(null==s_PreservFlag) s_PreservFlag= psrvPMapDf.get(s_USecLevel);//idfDoc.getString("u_sec_level")); //등록된게 없으면 정책서에서 기술한 기본값
				  if(dto.getPreserverYear() == dto.getYoungGu()) {
				         s_PreservFlag="0";
				  }
				  if(null==s_PreservFlag) s_PreservFlag= idfDoc.getString("u_preserve_flag");
				  int li_PreservYear=Integer.parseInt(s_PreservFlag)==0?9999:Integer.parseInt(s_PreservFlag);
				  idfDoc.setInt("u_preserve_flag", Integer.parseInt( s_PreservFlag) );  //보존연한
				  
				  if(idfDoc.getString("u_doc_status").equals("C") ) {
				      //idfDoc.setInt("u_preserve_flag",  dto.getPreserverYear());  //보존연한
				      IDfTime startDate = new DfTime() ;
				      // Convert the expiration date to a calendar object.
				      GregorianCalendar cal = new GregorianCalendar(startDate.getYear(), startDate.getMonth(),   startDate.getDay() );  
				      // Add the number of months ­1 (months start counting from 0).
				      cal.add(GregorianCalendar.MONTH, (12 * li_PreservYear) -1) ;
				      IDfTime expireDate = new DfTime (cal.getTime()) ;
				      if(li_PreservYear==9999)
					   	 idfDoc.setString("u_expired_date",  "9999-12-31 00:00:00");
					  else
					  	 idfDoc.setString("u_expired_date",  expireDate.toString());
				  }
				  
				  if(b_SecLevelChange) {
				      idfDoc.setString("u_fol_id", s_DWVFolderId);
				  }
				  //repeating 항목은 결재Key값으로 찾아서 삭제한 다음, 같은 위치에 insert로 대체한다.

			      int i_ValIdx = idfDoc.findString("u_wf_key", dto.getApprovalId()); //결재 Key값에 해당하는 index값을 찾아서
			
				  if(i_ValIdx < 0){ //최초 등록이면 append해준다
			          idfDoc.appendString("u_wf_key",            dto.getApprovalId());            //결재 ID
				      idfDoc.appendString("u_wf_system",         dto.getApprovalSystem());        //결재 시스템
				      idfDoc.appendString("u_wf_form",           dto.getApprovalFormName());      //결재 양식명
			          idfDoc.appendString("u_wf_title",          dto.getApprovalSubject());       //결재 제목
			          idfDoc.appendString("u_wf_approval_date",  (new DfTime()).toString());
			          idfDoc.appendString("u_wf_link",           dto.getApprovalLink());          //결재화면 URL
			          
			          //int setIdx = idfDoc.findString("u_wf_key", dto.getApprovalId()); //결재 Key값에 해당하는 index값을 찾아서
				      if(!dto.getApprovalState().equals("W"))
				    	  idfDoc.appendString("u_wf_approver",            dto.getApprovalWriter());            //결재 ID
				          //idfDoc.setRepeatingString( "u_wf_approver", setIdx, dto.getApprovalWriter());        //결재자
				      else
				      	  idfDoc.appendString("u_wf_approver",           " ");            //결재 ID
			          
				   }else{
					   
			          idfDoc.remove("u_wf_key",           i_ValIdx); //삭제
			          idfDoc.remove("u_wf_system",        i_ValIdx); //삭제
			          idfDoc.remove("u_wf_form",          i_ValIdx); //삭제
			          idfDoc.remove("u_wf_title",         i_ValIdx); //삭제
			          idfDoc.remove("u_wf_approver",      i_ValIdx); //삭제
			          idfDoc.remove("u_wf_approval_date", i_ValIdx); //삭제
			          idfDoc.remove("u_wf_link",          i_ValIdx); //삭제
			          
			          // 회수가 아닐때만 작동 ( 회수일때는 삭제하고 링크에 있는 원래 폴더로 되돌리므로  )

			          idfDoc.insertString("u_wf_system",         i_ValIdx, dto.getApprovalSystem());        //결재 시스템
			          idfDoc.insertString("u_wf_form",           i_ValIdx, dto.getApprovalFormName());      //결재 양식명
			          idfDoc.insertString("u_wf_title",          i_ValIdx, dto.getApprovalSubject() );       //결재 제목( 필요시 상태(승인요청, 중간결재, 최종결재, 반려를 뒤에 붙일수도 있음)
			          idfDoc.insertString("u_wf_key",            i_ValIdx, dto.getApprovalId());            //결재 ID
			          
				      if(!dto.getApprovalState().equals("W"))
				    	  idfDoc.insertString("u_wf_approver",            i_ValIdx, dto.getApprovalWriter()); 
				      else
				    	  idfDoc.insertString("u_wf_approver",            i_ValIdx, " "); 

				      //if(!dto.getApprovalState().equals("W"))
				      //    idfDoc.appendString("u_wf_approver",  dto.getApprovalWriter());        //결재자
			          idfDoc.insertString("u_wf_approval_date",  i_ValIdx, (new DfTime()).toString());
			          idfDoc.insertString("u_wf_link",           i_ValIdx, dto.getApprovalLink());          //결재화면 URL

				  }
				  idfDoc.setString("u_update_date", (new DfTime()).toString());
				  
				  idfDoc.save();
				  //IDfACL idf_Acl = idfDoc.getACL();
				  //해당 결재키의 edms_auth_base 관련 정보 지움
				  String ls_DocPermitType = idfDoc.getString("u_doc_status").equals("C")?"R":"D";
				  
				  if( !dto.getApprovalState().equals("X")) {
					  s_Dql = "delete edms_auth_base object " +
						  	" where u_add_gubun='W' and u_ext_key = '" + dto.getApprovalId()+ "' and u_obj_id='" +s_ObjId +"'";

			  		  if(s_USecLevel.equals("S")) {
						  s_Dql = "delete edms_auth_base object " +
								  	" where ((u_add_gubun='W' and u_ext_key = '" + dto.getApprovalId()+ "'  and u_obj_id='" +s_ObjId +"')  or  ( u_doc_status='C' and u_obj_id='" +s_ObjId +"' and u_author_type='D' and u_add_gubun='G')) ";
			  		  }
					  idf_Colb = DCTMUtils.getCollectionByDQL(idfSession, s_Dql, DfQuery.DF_QUERY);
					  if(idf_Colb != null) idf_Colb.close();

					  
					  if(dto.getApprovalLevel().equals("G")) {
						  s_Dql = "delete edms_auth_base object " +
								  	" where u_obj_id='" +s_ObjId +"' and  u_doc_status='C' and u_author_id='"+ dto.getUComOrgCabinetCd() +"' and u_add_gubun='G' " ;
					      idf_Colb = DCTMUtils.getCollectionByDQL(idfSession, s_Dql, DfQuery.DF_QUERY);
					      if(idf_Colb != null) idf_Colb.close();
					      
			  		  }else if(dto.getApprovalLevel().equals("C")) {
						  s_Dql = "delete edms_auth_base object " +
								  	" where u_obj_id='" +s_ObjId +"' and  u_doc_status='C' and u_author_id='"+ dto.getUGroupOrgCabinetCd() +"' and u_add_gubun='G' " ;
					      idf_Colb = DCTMUtils.getCollectionByDQL(idfSession, s_Dql, DfQuery.DF_QUERY);
					      if(idf_Colb != null) idf_Colb.close();
			  		  }else if(dto.getApprovalLevel().equals("T")) {
						  s_Dql = "delete edms_auth_base object " +
								  	" where u_obj_id='" +s_ObjId +"' and  u_doc_status='C' and u_author_id in('"+ dto.getUGroupOrgCabinetCd() +"', '"+dto.getUComOrgCabinetCd() +"') and u_add_gubun='G' " ;
					      idf_Colb = DCTMUtils.getCollectionByDQL(idfSession, s_Dql, DfQuery.DF_QUERY);
					      if(idf_Colb != null) idf_Colb.close();
			  		  }
			  		  
					  s_Dql = "select distinct u_author_id, u_author_type, u_permit_type, u_add_gubun, u_doc_status from edms_auth_base where u_obj_id ='"+ idfDoc.getString("u_doc_key") +"' and u_author_type in('D','U','C','G')  and u_add_gubun in('P','W',' ','S', 'J')  and u_author_id !='g_null' " ;
					    
					  IDfQuery 		idf_Qry 	= null;
					  idf_Qry = new DfQuery();
					  idf_Qry.setDQL(s_Dql);
	
				  	  List<String> lDocStatus = new ArrayList<String>();
				  	  lDocStatus.add("C");
				  	  lDocStatus.add("L");

					  try {
						    idf_Colb = idf_Qry.execute(idfSession,DfQuery.QUERY);
					  		
					  		while(idf_Colb != null && idf_Colb.next())
					  		{
					  		    String s_Author=idf_Colb.getString("u_author_id");
					  		    String s_AuthorType=idf_Colb.getString("u_author_type");
					  		    String s_AddGubun  =idf_Colb.getString("u_add_gubun");
					  		  
					  		    if(s_Author.contains("t_y")) continue;
					  		    if(s_Author ==null ||s_Author.equals("") || s_Author.equals(" ") || s_Author.equals("null")) continue;
					  		  
					  		    if(s_AuthorType.equals("D")) if(!s_Author.contains("g_")) s_Author="g_"+ s_Author;
					  		    
					  		    String s_PermitType=idf_Colb.getString("u_permit_type");
					  		    String s_DocStatus =idf_Colb.getString("u_doc_status");
					  		     
					  		    for(int i=0; i< lDocStatus.size(); i++) {
							  		if(! lDocStatus.get(i).equals(s_DocStatus) || (idfDoc.getString("u_doc_status").equals("C") && lDocStatus.get(i).equals("L")))  continue;
						  		    //if(s_AuthorType.equals("D") && idfDoc.getString("u_doc_status").equals("C") && lDocStatus.get(i).equals("L"))  continue;
						  		  
					  		    	
					  		    	//ACL_NAME을 새로 부여해서 grant를 다시 해줌
					  		    	idfDoc.grant(s_Author, GrantedLevels.findByLabel(ls_DocPermitType), "");
					  		    	//idfDoc.grant(s_Author, GrantedLevels.findByLabel(s_PermitType), "");
					  		    	if(s_AuthorType.equals("D")) {
					  		    		//if( !idfDoc.getString("u_sec_level").equals("S") && !idfDoc.getString("u_sec_level").equals("T"))
					  		    		//if( (!idfDoc.getString("u_sec_level").equals("S") && !idfDoc.getString("u_sec_level").equals("T")) || s_AddGubun.equals("P"))
					  		    		if(s_AddGubun.equals("P") || s_AddGubun.equals("W")) {
					  		    		    idfDoc.grant(s_Author+"_sub", GrantedLevels.findByLabel(ls_DocPermitType), "");
					  		    		}
					  		    	}
					  		    	idfDoc.save();
					  		    }
					  	    }
					  		if(idf_Colb != null) idf_Colb.close();
				      }catch (Exception e) {
				 			e.printStackTrace();
					  }
					  
			          String s_AuthStr=  "g_"+dto.getUCabinetCode();

				  	  if( idfDoc.getPermitEx( s_AuthStr) < 7) {
				  		     s_Dql = "delete edms_auth_base object where u_obj_id='" +s_ObjId +"' and u_author_id ='"+ s_AuthStr +"' and  u_doc_status='L' ";
							 idf_Colb = DCTMUtils.getCollectionByDQL(idfSession, s_Dql, DfQuery.DF_QUERY);
							 if(idf_Colb != null) idf_Colb.close();
				  	  }
				  	  int i_AuthorCnt = DCTMUtils.getCountByDQL( idfSession, " edms_auth_base where u_obj_id='"+s_ObjId +"' and u_author_id='"+s_AuthStr +"' and u_doc_status='L' ");
				  	  if(i_AuthorCnt < 1) {

							  IDfPersistentObject idf_PObjD = idfSession.newObject("edms_auth_base");
							  idf_PObjD.setString("u_obj_id"		, idfDoc.getString("u_doc_key"));
							  idf_PObjD.setString("u_obj_type"		, "D");
							  idf_PObjD.setString("u_doc_status"	, "L");
							  idf_PObjD.setString("u_permit_type"	, "D"); //읽기/쓰기/편집
							  idf_PObjD.setString("u_own_dept_yn"	, "Y"); //
							  idf_PObjD.setString("u_author_id"	,  s_AuthStr);
							  idf_PObjD.setString("u_author_type"	,  "D"); //사용자 
							  idf_PObjD.setString("u_create_user"	, dto.getUCreateUser());
							  idf_PObjD.setTime  ("u_create_date"	, new DfTime());
							  idf_PObjD.setString("u_add_gubun"	, "G"); //
							  idf_PObjD.save();
				  	  }
					  if(!dto.getApprovalLevel().equals("S")) {
			              if(dto.getApprovalLevel().equals("G")) s_AuthStr=dto.getUGroupOrgCabinetCd();
			              else if(dto.getApprovalLevel().equals("C")) s_AuthStr = dto.getUComOrgCabinetCd();
						  i_AuthorCnt = DCTMUtils.getCountByDQL( idfSession, " edms_auth_base where u_obj_id='"+s_ObjId +"' and u_author_id='"+s_AuthStr +"' and u_doc_status='C'  ");
						  if(i_AuthorCnt < 1) {
							      IDfPersistentObject idf_PObjD = idfSession.newObject("edms_auth_base");
								  idf_PObjD = idfSession.newObject("edms_auth_base");
							      idf_PObjD.setString("u_obj_id"		, idfDoc.getString("u_doc_key"));
							      idf_PObjD.setString("u_obj_type"		, "D");
							      idf_PObjD.setString("u_doc_status"	, "C");
							      idf_PObjD.setString("u_permit_type"	, "R"); //읽기/쓰기/편집 
							      idf_PObjD.setString("u_own_dept_yn"	, "Y"); //
							      idf_PObjD.setString("u_author_id"	,  s_AuthStr);
							      idf_PObjD.setString("u_author_type"	,  "D"); 
							      idf_PObjD.setString("u_create_user"	, dto.getUCreateUser());
							      idf_PObjD.setTime  ("u_create_date"	, new DfTime());
							      idf_PObjD.setString("u_add_gubun"	, "G"); //
							      idf_PObjD.save();
						   }
						   idfDoc.grant(s_AuthStr, GrantedLevels.findByLabel(ls_DocPermitType), ""); //읽기/쓰기/편집권한 
						   if( !idfDoc.getString("u_sec_level").equals("S") && !idfDoc.getString("u_sec_level").equals("T"))
		  		    	       idfDoc.grant(s_AuthStr+"_sub", GrantedLevels.findByLabel(ls_DocPermitType), "");
				  	  }
					  
					  
					  //System.out.println("MOLA: Session Cabinet Code=" + s_CabinetCode+ " ** Document CabinetCode=" + idfDoc.getString("u_cabinet_code"));
					  String s_OwnCabinetCode= idfDoc.getString("u_cabinet_code"); //문서의 보유 부서 문서함코드
					  
				      for (Map.Entry<String, String[]> entry : dto.getApprovalDraftLineM().entrySet()) {
						  for(int i=0; i < entry.getValue().length; i++) {
							 //if ( authService.checkDocAuth(dto.getDboxId(), userSession.getUser().getUserId(), GrantedLevels.DELETE.getLevel()) ) {
							     
								 if(!entry.getValue()[i].equals("")) {
									
								    String sAuthorId = entry.getValue()[i];
								    String sAuthorType=sAuthorId.contains("g_")?"D":"U";
	
								    idfDoc.grant(sAuthorId, GrantedLevels.findByLabel(ls_DocPermitType), "");
					  		    	if(sAuthorType.equals("D") ) {
					  		    		//if( !idfDoc.getString("u_sec_level").equals("S") && !idfDoc.getString("u_sec_level").equals("T"))
					  		    		    idfDoc.grant(sAuthorId+"_sub", GrantedLevels.findByLabel(ls_DocPermitType), "");
					  		    	}
								    idfDoc.save();
							  		if( idfDoc.getPermitEx( sAuthorId) <= 7) {
							  		     s_Dql = "delete edms_auth_base object where u_obj_id='" +s_ObjId +"' and u_author_id ='"+ sAuthorId +"' ";
										 idf_Colb = DCTMUtils.getCollectionByDQL(idfSession, s_Dql, DfQuery.DF_QUERY);
										 if(idf_Colb != null) idf_Colb.close();
							  		}
								    for(int j=0; j< lDocStatus.size(); j++) {
							  	    	String s_PermitType=lDocStatus.get(j).equals("C")?"R":"D";
									    IDfPersistentObject idf_PObj = idfSession.newObject("edms_auth_base");
						  				idf_PObj.setString("u_obj_id"		, s_ObjId);
						  				idf_PObj.setString("u_obj_type"		, "D");
						  				idf_PObj.setString("u_doc_status"	, lDocStatus.get(j));
						  				
						  				idf_PObj.setString("u_permit_type"	, s_PermitType); //권한
						  				
						  				idf_PObj.setString("u_own_dept_yn"	,  sAuthorId.equals("g_"+s_OwnCabinetCode)?"Y":""); //
						  				idf_PObj.setString("u_author_id"	,  sAuthorId);
						  				idf_PObj.setString("u_author_type"	,  sAuthorType); //사용자 
						  				idf_PObj.setString("u_create_user"	, userSession.getUser().getUserId());
						  				idf_PObj.setTime  ("u_create_date"	, new DfTime());
						  				idf_PObj.setString("u_ext_key"	, dto.getApprovalId()); //
						  				idf_PObj.setString("u_add_gubun"	, "W"); //
						  				
						  				idf_PObj.save();	
								    }
								 }
							 //}
						  }
				      }
				      for (Map.Entry<String, String[]> entry : dto.getApprovalCcLineM().entrySet()) {
						  for(int i=0; i < entry.getValue().length; i++) {
							 //if ( authService.checkDocAuth(dto.getDboxId(), userSession.getUser().getUserId(), GrantedLevels.DELETE.getLevel()) ) {
								 if(!entry.getValue()[i].equals("")) {
									    String sAuthorId = entry.getValue()[i];
									    String sAuthorType=sAuthorId.contains("g_")?"D":"U";
	
								  		if( idfDoc.getPermitEx( sAuthorId) > 3) {
								  			s_Dql = "UPDATE edms_auth_base  OBJECTS SET u_ext_key='" + dto.getApprovalId() + "', SET u_add_gubun='W' " +
													"WHERE u_obj_id='" + s_ObjId + "' AND u_author_id = '" + sAuthorId + "'";
											
											idf_Qry.setDQL(s_Dql);
											idf_Colb = idf_Qry.execute(idfSession, DfQuery.QUERY);
											if (idf_Colb!=null && idf_Colb.next()) idf_Colb.close();
											
										    idfDoc.grant(sAuthorId, GrantedLevels.findByLabel(ls_DocPermitType), "");
							  		    	if(sAuthorType.equals("D") ) {
							  		    		//if( !idfDoc.getString("u_sec_level").equals("S") && !idfDoc.getString("u_sec_level").equals("T"))
							  		    		    idfDoc.grant(sAuthorId+"_sub", GrantedLevels.findByLabel(ls_DocPermitType), "");
							  		    	}
										    idfDoc.save();
											
								  		}else {
										    
								  		    s_Dql = "delete edms_auth_base object where u_obj_id='" +s_ObjId +"' and u_author_id ='"+ sAuthorId +"' ";
											idf_Colb = DCTMUtils.getCollectionByDQL(idfSession, s_Dql, DfQuery.DF_QUERY);
											if(idf_Colb != null) idf_Colb.close();

										    idfDoc.grant(sAuthorId, GrantedLevels.findByLabel("R"), "");
							  		    	if(sAuthorType.equals("D") ) {
							  		    		//if( !idfDoc.getString("u_sec_level").equals("S") && !idfDoc.getString("u_sec_level").equals("T"))
							  		    		    idfDoc.grant(sAuthorId+"_sub", GrantedLevels.findByLabel("R"), "");
							  		    	}
										    
										    idfDoc.save();
											
											for(int j=0; j< lDocStatus.size(); j++) {
											    IDfPersistentObject idf_PObj = idfSession.newObject("edms_auth_base");
								  				idf_PObj.setString("u_obj_id"		, s_ObjId);
								  				idf_PObj.setString("u_obj_type"		, "D");
								  				idf_PObj.setString("u_doc_status"	,  lDocStatus.get(j));
								  				idf_PObj.setString("u_permit_type"	, "R"); //읽기
								  				idf_PObj.setString("u_own_dept_yn"	, sAuthorId.equals("g_"+s_OwnCabinetCode)?"Y":""); //
								  				idf_PObj.setString("u_author_id"	,  sAuthorId);
								  				idf_PObj.setString("u_author_type"	,  sAuthorType); //사용자 
								  				idf_PObj.setString("u_create_user"	, userSession.getUser().getUserId());
								  				idf_PObj.setTime  ("u_create_date"	, new DfTime());
								  				idf_PObj.setString("u_ext_key"	, dto.getApprovalId()); //
								  				idf_PObj.setString("u_add_gubun"	, "W"); //
								  				idf_PObj.save();
										    }
								  		}
							  	    }
						      }
								 
					  }
				      
				      for (Map.Entry<String, String[]> entry : dto.getApprovalReceiveLineM().entrySet()) {
						  for(int i=0; i < entry.getValue().length; i++) {
							 // if ( authService.checkDocAuth(dto.getDboxId(), userSession.getUser().getUserId(), GrantedLevels.DELETE.getLevel()) ) {
								  if(!entry.getValue()[i].equals("")) {
									  String sAuthorId = entry.getValue()[i];
									    String sAuthorType=sAuthorId.contains("g_")?"D":"U";
	
								  		if( idfDoc.getPermitEx( sAuthorId) > 3) {
								  			s_Dql = "UPDATE edms_auth_base OBJECTS SET u_ext_key='" + dto.getApprovalId() + "', SET u_add_gubun='W' " +
													"WHERE u_obj_id='" + s_ObjId + "' AND u_author_id = '" + sAuthorId + "'";
											
											idf_Qry.setDQL(s_Dql);
											idf_Colb = idf_Qry.execute(idfSession, DfQuery.QUERY);
											if (idf_Colb!=null && idf_Colb.next()) idf_Colb.close();
											
										    idfDoc.grant(sAuthorId, GrantedLevels.findByLabel(ls_DocPermitType), "");
							  		    	if(sAuthorType.equals("D") ) {
							  		    		//if( !idfDoc.getString("u_sec_level").equals("S") && !idfDoc.getString("u_sec_level").equals("T"))
							  		    		    idfDoc.grant(sAuthorId+"_sub", GrantedLevels.findByLabel(ls_DocPermitType), "");
							  		    	}
										    idfDoc.save();
											
								  		}else {
								  		    s_Dql = "delete edms_auth_base object where u_obj_id='" +s_ObjId +"' and u_author_id ='"+ sAuthorId +"' ";
											idf_Colb = DCTMUtils.getCollectionByDQL(idfSession, s_Dql, DfQuery.DF_QUERY);
											if(idf_Colb != null) idf_Colb.close();

										    idfDoc.grant(sAuthorId, GrantedLevels.findByLabel("R"), "");
							  		    	if(sAuthorType.equals("D") ) {
							  		    		//if( !idfDoc.getString("u_sec_level").equals("S") && !idfDoc.getString("u_sec_level").equals("T"))
							  		    		    idfDoc.grant(sAuthorId+"_sub", GrantedLevels.findByLabel("R"), "");
							  		    	}
		                                    idfDoc.save();
								  			
											for(int j=0; j< lDocStatus.size(); j++) {
											    IDfPersistentObject idf_PObj = idfSession.newObject("edms_auth_base");
								  				idf_PObj.setString("u_obj_id"		, s_ObjId);
								  				idf_PObj.setString("u_obj_type"		, "D");
								  				idf_PObj.setString("u_doc_status"	, lDocStatus.get(j));
								  				idf_PObj.setString("u_permit_type"	, "R"); //읽기
								  				idf_PObj.setString("u_own_dept_yn"	, sAuthorId.equals("g_"+s_OwnCabinetCode)?"Y":""); //
								  				idf_PObj.setString("u_author_id"	,  sAuthorId);
								  				idf_PObj.setString("u_author_type"	,  sAuthorType); //사용자 
								  				idf_PObj.setString("u_create_user"	, userSession.getUser().getUserId());
								  				idf_PObj.setTime  ("u_create_date"	, new DfTime());
								  				idf_PObj.setString("u_ext_key"	, dto.getApprovalId()); //
								  				idf_PObj.setString("u_add_gubun"	, "W"); //
								  				idf_PObj.save();
										    }
								  		}
							  	    }
						      }
					   }
				      s_AuthStr=  "g_"+dto.getUCabinetCode();
				  	  i_AuthorCnt = DCTMUtils.getCountByDQL( idfSession, " edms_auth_base where u_obj_id='"+s_ObjId +"' and u_author_id='"+s_AuthStr +"' and u_add_gubun !='S' and u_doc_status='L' ");
				  	  if(i_AuthorCnt < 1) {

							  IDfPersistentObject idf_PObjD = idfSession.newObject("edms_auth_base");
							  idf_PObjD.setString("u_obj_id"		, s_ObjId+"");
							  idf_PObjD.setString("u_obj_type"		, "D");
							  idf_PObjD.setString("u_doc_status"	, "L");
							  idf_PObjD.setString("u_permit_type"	, "D"); //읽기/쓰기/편집
							  idf_PObjD.setString("u_own_dept_yn"	, "Y"); //
							  idf_PObjD.setString("u_author_id"	,  s_AuthStr);
							  idf_PObjD.setString("u_author_type"	,  "D"); //사용자 
							  idf_PObjD.setString("u_create_user"	, dto.getUCreateUser());
							  idf_PObjD.setTime  ("u_create_date"	, new DfTime());
							  idf_PObjD.setString("u_add_gubun"	, "G"); //
							  idf_PObjD.save();
				  	  }
/*				  	  
				  	  else {
				  		  if( idfDoc.getPermitEx( s_AuthStr) < 7) {				  		  
				  			  s_Dql = "UPDATE edms_auth_base  OBJECTS SET u_ext_key='" + dto.getApprovalId() + "', SET u_add_gubun='W', SET u_permit_type='D' " +
								  	"WHERE u_obj_id='" + s_ObjId+"" + "' AND u_author_id = '" + s_AuthStr + "'  and u_doc_status='L' ";
				  			  idf_Qry.setDQL(s_Dql);
							  idf_Colb = idf_Qry.execute(idfSession, DfQuery.QUERY);
							  if (idf_Colb!=null && idf_Colb.next()) idf_Colb.close();
							  
							  if(!dto.getApprovalLevel().equals("S")) {//2022.02.23 추가
						          idfDoc.grant(s_AuthStr, GrantedLevels.findByLabel(ls_DocPermitType), "");
							  }
						      idfDoc.save();				  			
				  		  }
				  	  }
*/				  		  
				  	  
					  if(!dto.getApprovalLevel().equals("S")) {
			              if(dto.getApprovalLevel().equals("G")) s_AuthStr=dto.getUGroupOrgCabinetCd();
			              else if(dto.getApprovalLevel().equals("C")) s_AuthStr = dto.getUComOrgCabinetCd();
						  i_AuthorCnt = DCTMUtils.getCountByDQL( idfSession, " edms_auth_base where u_obj_id='"+s_ObjId +"' and u_author_id='"+s_AuthStr +"' and u_add_gubun !='S'  and u_doc_status='C' ");
						  if(i_AuthorCnt < 1) {
							      IDfPersistentObject idf_PObjD = idfSession.newObject("edms_auth_base");
								  idf_PObjD = idfSession.newObject("edms_auth_base");
							      idf_PObjD.setString("u_obj_id"		, idfDoc.getString("u_doc_key"));
							      idf_PObjD.setString("u_obj_type"		, "D");
							      idf_PObjD.setString("u_doc_status"	, "C");
							      idf_PObjD.setString("u_permit_type"	, "R"); //읽기/쓰기/편집 
							      idf_PObjD.setString("u_own_dept_yn"	, "Y"); //
							      idf_PObjD.setString("u_author_id"	,  s_AuthStr);
							      idf_PObjD.setString("u_author_type"	,  "D"); //사용자 
							      idf_PObjD.setString("u_create_user"	, dto.getUCreateUser());
							      idf_PObjD.setTime  ("u_create_date"	, new DfTime());
							      idf_PObjD.setString("u_add_gubun"	, "G"); //
							      idf_PObjD.save();
						   }
						   idfDoc.grant(s_AuthStr, GrantedLevels.findByLabel(ls_DocPermitType), ""); 
				  	  }
				      //if(s_USecLevel.equals("S")) {
					  	    i_AuthorCnt = DCTMUtils.getCountByDQL( idfSession, " edms_auth_base where u_obj_id='"+s_ObjId +"' and u_author_id='"+dto.getApprovalWriter() +"' and u_doc_status='C'  ");
					  	    if(i_AuthorCnt < 1) {
							    IDfPersistentObject idf_PObj = idfSession.newObject("edms_auth_base");
				  				idf_PObj.setString("u_obj_id"		, s_ObjId);
				  				idf_PObj.setString("u_obj_type"		, "D");
				  				idf_PObj.setString("u_doc_status"	, "C");
				  				idf_PObj.setString("u_permit_type"	, "R"); //읽기
				  				idf_PObj.setString("u_own_dept_yn"	,  ""); //
				  				idf_PObj.setString("u_author_id"	,  dto.getApprovalWriter());
				  				idf_PObj.setString("u_author_type"	,  "U"); //사용자 
				  				idf_PObj.setString("u_create_user"	, userSession.getUser().getUserId());
				  				idf_PObj.setTime  ("u_create_date"	, new DfTime());
				  				idf_PObj.setString("u_ext_key"	, dto.getApprovalId()); //
				  				idf_PObj.setString("u_add_gubun"	, "G"); //
				  				idf_PObj.save();
					  	    }
				       //}
					   idfDoc.save();
				      
				      //첨부로그 
				 	  String sOwnSrDeptOrgId = gwDeptService.selectOrgIdByCabinetcode(dto.getUCabinetCode());
					  LogDoc logDoc = LogDoc.builder()
						          .uJobCode( "AP")
						          .uDocId(idfDoc.getString("r_object_id"))
						          .uDocKey(idfDoc.getString("u_doc_key"))
						          .uDocName(idfDoc.getString("title").replaceAll("'", "''"))
						          .uDocVersion(Integer.parseInt(idfDoc.getVersionLabel(0).substring(0, idfDoc.getVersionLabel(0).indexOf(".")))+"")
						          .uFileSize(Long.parseLong(idfDoc.getString("r_content_size")))
						          .uOwnDeptcode(sOwnSrDeptOrgId)
						          .uActDeptCode(userSession.getUser().getOrgId())
						          .uJobUser(userSession.getUser().getUserId())
						          .uJobUserType(dto.getUJobUserType()==null ? "P" : dto.getUJobUserType()) //작업자구분(P:개인, D:부서관리자, C:사별관리자, G:그룹관리자, S:시스템)
						          .uDocStatus(idfDoc.getString("u_doc_status"))
						          .uSecLevel(idfDoc.getString("u_sec_level"))
						          .uCabinetCode(idfDoc.getString("u_cabinet_code"))
						          .uJobGubun("H") //O:원본, H:html파일
						          .uUserIp(dto.getURequestIp())			// 받아야함.
						          .uAttachSystem("APPR")
						          .build();
						      insertLog(logDoc);
				  }

			      //preFolderId 이 전자결재 타입인지 확인 필요
			      if(!preFolderId.equals("")) {
			    	  IDfPersistentObject idFolderInfo = idfSession.getObject(new DfId(preFolderId));
			          
			          String preFolderType=idFolderInfo.getString("u_fol_type");
			          
			          if(preFolderType.equals(FolderType.DWY.toString())  ||  preFolderType.equals(FolderType.DWT.toString())) {
			        	  System.out.println("전자결재함에서 참조된 Dbox 문서로서 Link를 만들지 않음:u_fol_id=" +preFolderId+ ":u_fol_type:"+preFolderType);
			          }else {
			        	  DocLink docLink = new DocLink();
			        	  
			        	  if( !dto.getApprovalState().equals("X")) { //dbox파일을 첨부해서 링크를 생성하는데, 상태가 결재회수는 아닌경우, 결재회수면 이미 만들어진걸 삭제
			        		  dto.setUDocKey(idfDoc.getString("u_doc_key"));
				    	      idfDocLink=docLink.createDocLink(idfSession, dto, preFolderId, "W"); //전자결재는 W로 넘김. 다른 경우는 W가 아닌 값을 사용
				    	      if(idfDocLink !=null)     idfDocLink.save(); //이미 Link파일이 생성된 경우
			        	  }else{ 
			   			      String uFolId=docLink.removeDocLink(idfSession, s_CabinetCode, dto.getApprovalId(), "C", dto.getApprovalWriter()); //문서묶음(Contents)의 삭제( 문서id묶음)
		  			          idfDoc.setString("u_fol_id", uFolId); //링크를 삭제하면 링크가 갖고있던 이전폴더id(u_fol_id)로 edms_doc의 u_fol_id를 변경해준다.(부서함->전자결재/temp 로 왔던것을 다시 부서함 폴더로 되돌림)
			        	  }
			          }
			      }else {
			    	      System.out.println("================="+ DfId.isObjectId(dto.getDboxId()));
			        	  DocLink docLink = new DocLink();
			        	  if( !dto.getApprovalState().equals("X")) { //dbox파일을 첨부해서 링크를 생성하는데, 상태가 결재회수는 아닌경우, 결재회수면 이미 만들어진걸 삭제
			        		  dto.setUDocKey(idfDoc.getString("u_doc_key"));
				    	      idfDocLink=docLink.createDocLink(idfSession, dto, preFolderId, "W"); //전자결재는 W로 넘김. 다른 경우는 W가 아닌 값을 사용
				    	      if(idfDocLink !=null)     idfDocLink.save(); //이미 Link파일이 생성된 경우
			        	  }else{ 
			   			      String uFolId=docLink.removeDocLink(idfSession, s_CabinetCode, dto.getApprovalId(), "C", dto.getApprovalWriter()); //문서묶음(Contents)의 삭제( 문서id묶음)
		  			          idfDoc.setString("u_fol_id", uFolId); //링크를 삭제하면 링크가 갖고있던 이전폴더id(u_fol_id)로 edms_doc의 u_fol_id를 변경해준다.(부서함->전자결재/temp 로 왔던것을 다시 부서함 폴더로 되돌림)
			        	  }
			      }
		       	  //------------------------------------------
				  // 과거 버전이 있는 경우 처리
				  //------------------------------------------
				  if(!idfDoc.getString("u_doc_key").equals(idfDoc.getObjectId().toString()))
				  {
						String s_VerId = "";
						IDfDocument idf_VerDoc = null;
						IDfCollection idf_VerCol = idfDoc.getVersions(null);
						
						while(idf_VerCol != null && idf_VerCol.next())
						{
							s_VerId = idf_VerCol.getString("r_object_id");
							//과거 버전만 처리하기 위해 현재 버전은 skip
							if(s_VerId.equals(idfDoc.getObjectId().toString()))
								continue;
							
							//권한은 현재 버전의 ACL Name을 사용하면 그대로 적용 됨
							idf_VerDoc = (IDfDocument)idfSession.getObject(new DfId(s_VerId));
							idf_VerDoc.setACLDomain(idfSession.getDocbaseOwnerName());
							idf_VerDoc.setACLName(idfDoc.getString("acl_name"));
							
							//위치 변경은 ps_RcevFolId 값이 있는 경우만
							idf_VerDoc.setString("u_fol_id", idfDoc.getString("u_fol_id"));
							idf_VerDoc.setString("u_pr_code", idfDoc.getString("u_pr_code"));
							idf_VerDoc.setString("u_pr_type", idfDoc.getString("u_pr_type"));
							
							idf_VerDoc.setString("u_cabinet_code", idfDoc.getString("u_cabinet_code"));
							idf_VerDoc.save();
						}
						if(idf_VerCol != null) idf_VerCol.close(); 
				  }
			  }
   	      }
		  
          if(idf_Col != null) idf_Col.close();
	      idfSession.commitTrans();	  
	  }else {
		  s_ObjId 	= DCTMUtils.getCurrentObjectID(idfSession, s_ObjId); //문서의 최신버전 r_object_id를 가져온다.	
		  if(s_ObjId.equals("")) {
		        throw new NotFoundException("문서 최신버전이 변경됨[Documentum]");
		  }
		  IDfDocument idfDoc = (IDfDocument)idfSession.getObject(new DfId(s_ObjId));

		  if(idfDoc.getString("u_delete_status").equals("D") || idfDoc.getString("u_delete_status").equals("E")) {
			  throw new RuntimeException("문서가 삭제되어첨부할 수 없습니다");
			  //'R:요청중, A:폐기승인, D:삭제(휴지통), E:삭제(보존년한), P:상위폴더 삭제에 의한 삭제, T:임시저장'),
		  }else  if(idfDoc.getString("u_delete_status").equals("P")) 
		  {
			  throw new RuntimeException("문서가 포함된 폴더가 삭제되어 첨부할 수 없습니다");
			  //'R:요청중, A:폐기승인, D:삭제(휴지통), E:삭제(보존년한), P:상위폴더 삭제에 의한 삭제, T:임시저장'),
		  }
		  
		  if(idfDoc.isCheckedOut()) //편집중문서면 편집을 해제하고 결재 프로세스 수행
		  {
			  System.out.println( "#MOLA_LOCK:"+ idfDoc.getLockOwner()+  " 편집중 문서, Lock해제하고 결재진행");
		      idfDoc.cancelCheckout();
		  }
		  
		  s_ObjId = idfDoc.getString("u_doc_key"); 
		  
		  idfDoc.setString("u_wf_doc_yn", "Y");
		  
		  boolean b_SecLevelChange=true; //문서의 보안등급 변경 가능여부 
		  
		  if(null ==dto.getApprovalLevel() || dto.getApprovalLevel().equals("")) {
		      dto.setApprovalLevel(idfDoc.getString("u_sec_level"));
		  }
		  
		  if(idfDoc.getString("u_wf_doc_yn").equals("Y") && idfDoc.getString("u_doc_status").equals("C")){  //결재문서이면서 Closed된 문서
			    String ls_UpFolId = idfDoc.getString("u_fol_id");
			    if(ls_UpFolId.equals("") || ls_UpFolId.equals(" ")) {
			    	b_SecLevelChange=true;
			    }else {
			    	if (DfId.isObjectId( ls_UpFolId)) {
			            IDfPersistentObject idfFol_Obj = idfSession.getObject(new DfId( ls_UpFolId));
			            if(idfFol_Obj.getString("u_fol_type").equals("DWY")) { //결재승인된 상태
			            	b_SecLevelChange=false;// 기존에 결재승인된 자료를 재 첨부하는 경우 전자결재의 문서보안등급에 상관없이 최초결재 승인받은 보안등급을 유지한다
			            	dto.setApprovalLevel(idfDoc.getString("u_sec_level"));
			            }
			    	}
			    }
		  }
		  
		  s_CabinetCode = idfDoc.getString("u_cabinet_code");
		  dto.setUCabinetCode(s_CabinetCode);
		  
		  String filderType     = dto.getApprovalState().equals("F")?FolderType.DWY.toString():FolderType.DWT.toString();  //최종승인이 아니면  DWT 전자결재 임시폴더
		  String nfolderName  = dto.getApprovalState().equals("F")?folderName:"temp";
		  String s_DWVFolderId = makeDWVFolder(idfSession, s_CabinetCode, filderType, nfolderName, folderName, userSession.getUser().getUserId());
		  
		  //System.out.println("MOLA2: CabinetCode="+s_CabinetCode +"  DWVFolderId: "+ s_DWVFolderId+" 상태코드:"+  dto.getApprovalState());
		  
		  String preFolderId = idfDoc.getString("u_fol_id"); //전자결재 폴더로 변경전에 이전 폴더id를 edms_doc_link에 저장해놓기 위한 변수
		  
		  if(null==dto.getApprovalLevel() || dto.getApprovalLevel().equals("") || dto.getApprovalLevel().equals(" ")) { 
			  dto.setApprovalLevel(  idfDoc.getString("u_sec_level") );
			  //throw new RuntimeException("보안등급항목값이 전달되지 않았습니다");
			  //dto.setApprovalLevel( null==idfDoc.getString("u_sec_level")?"T": (idfDoc.getString("u_sec_level").equals("") || idfDoc.getString("u_sec_level").equals(" ")?"T":idfDoc.getString("u_sec_level")));
		  }
		  if(!dto.getApprovalState().equals("X")) {//반려가 아닐경우,
		      if(b_SecLevelChange) {
		          idfDoc.setString("u_sec_level",  dto.getApprovalLevel()); //
		      }
		  }else{
	    	  if(b_SecLevelChange) {
		          idfDoc.setString("u_sec_level",  dto.getApprovalLevel()); //
	    	  }
		  }
		  String s_USecLevel =dto.getApprovalLevel();//idfDoc.getString("u_sec_level");  //문서의 보안등급(변경전)
		  
		  // 업무 속성 지정
		  if(dto.getApprovalState().equals("F") || dto.getApprovalState().equals("X")) {  //최종결재, 반려시 closed 처리
			  if(b_SecLevelChange && !idfDoc.getString("u_doc_status").equals("C")) {
			      idfDoc.setString("u_doc_status"    , "C");
			      idfDoc.setString("u_closed_date"    , (new DfTime()).toString());
			      idfDoc.setString("u_closer"        , userSession.getUser().getUserId());
			  }
		  
		  }
		  //else if(dto.getApprovalState().equals("W")) { //최초 결재요청인 경우
		  //  idfDoc.setString("u_doc_status"      , "L"); //중간결재
		  //}else {
		  //  idfDoc.setString("u_doc_status"    , "L"); //중간결재
		  //}
		  idfDoc.setBoolean("u_ver_keep_flag"  , true); //버전유지여부 true
		  //회수시 문서 삭제 
	
		  if(b_SecLevelChange) {
		      String uDocStatus   = idfDoc.getString("u_doc_status");
		      boolean uPrivacyFlag = idfDoc.getBoolean("u_privacy_flag");
		      String uSecLevel    = s_USecLevel.toLowerCase();//idfDoc.getString("u_sec_level").toLowerCase();
			  String uPrivacyStr ="g";
			  if(uPrivacyFlag) {    
			    	 uPrivacyStr="p";
			         uSecLevel = uSecLevel.equals("s")?"s":"l";
		     }
			 String s_TypeName = idfDoc.getType().getName(); //edms_doc인지 edms_doc_imp 이거나
		     if(!s_TypeName.equals("edms_doc_imp")) {
		    	 if(!idfDoc.getString("u_pr_code").equals("") && !idfDoc.getString("u_pr_code").equals(" ")) {
		    	     String aclName = "a_"+s_CabinetCode+"_"+idfDoc.getString("u_pr_type").toLowerCase() +"_"+uPrivacyStr+"_"+uSecLevel+"_n";
		    	     idfDoc.setACLName(aclName);
		    	 }else {
		    	     String aclName = "a_"+s_CabinetCode+"_d_"+uPrivacyStr+"_"+uSecLevel+"_n"; //부서함
		    	     idfDoc.setACLName(aclName);
		    	 }
		     }else {
			     String aclName = "a_"+s_CabinetCode+"_s_g_s_n"; //중요문서함
			     idfDoc.setACLName(aclName);
		     }
		     idfDoc.setOwnerName(idfSession.getDocbaseOwnerName());
		  }
		  
		  //if(!preFolderId.equals("DWY"))
		  if(b_SecLevelChange) 
		      idfDoc.setString("u_fol_id", s_DWVFolderId);
		  //repeating 항목은 결재Key값으로 찾아서 삭제한 다음, 같은 위치에 insert로 대체한다.
	      int i_ValIdx = idfDoc.findString("u_wf_key", dto.getApprovalId()); //결재 Key값에 해당하는 index값을 찾아서
	
		  if(i_ValIdx < 0){ //최초 등록이면 append해준다
	          idfDoc.appendString("u_wf_key",            dto.getApprovalId());            //결재 ID
		      idfDoc.appendString("u_wf_system",         dto.getApprovalSystem());        //결재 시스템
		      idfDoc.appendString("u_wf_form",           dto.getApprovalFormName());      //결재 양식명
	          idfDoc.appendString("u_wf_title",          dto.getApprovalSubject());       //결재 제목
	          
	          //int setIdx = idfDoc.findString("u_wf_key", dto.getApprovalId()); //결재 Key값에 해당하는 index값을 찾아서
		      //if(!dto.getApprovalState().equals("W"))
		      //    idfDoc.setRepeatingString( "u_wf_approver", setIdx, dto.getApprovalWriter());        //결재자
		      //else
		      //  idfDoc.setRepeatingString( "u_wf_approver", setIdx, "-");        //결재자
		      if(!dto.getApprovalState().equals("W"))
		    	  idfDoc.appendString("u_wf_approver",            dto.getApprovalWriter());            //결재 ID
		          //idfDoc.setRepeatingString( "u_wf_approver", setIdx, dto.getApprovalWriter());        //결재자
		      else
		    	  idfDoc.appendString("u_wf_approver",           " ");            //결재 ID
	          
	          idfDoc.appendString("u_wf_approval_date",  (new DfTime()).toString());
	          idfDoc.appendString("u_wf_link",           dto.getApprovalLink());          //결재화면 URL
		   }else{
			   
			   //idfDoc.setRepeatingString( "u_wf_system", i_ValIdx, dto.getApprovalWriter());        //결재 시스템
			   //idfDoc.setRepeatingString( "u_wf_form", i_ValIdx, dto.getApprovalWriter());        //결재 양식명
			   //idfDoc.setRepeatingString( "u_wf_title", i_ValIdx, dto.getApprovalWriter());        //결재 제목
			   //idfDoc.setRepeatingString( "u_wf_key", i_ValIdx, dto.getApprovalWriter());        //결재 ID
			   
	          idfDoc.remove("u_wf_key",           i_ValIdx); //삭제
	          idfDoc.remove("u_wf_system",        i_ValIdx); //삭제
	          idfDoc.remove("u_wf_form",          i_ValIdx); //삭제
	          idfDoc.remove("u_wf_title",         i_ValIdx); //삭제
	          idfDoc.remove("u_wf_approver",      i_ValIdx); //삭제
	          idfDoc.remove("u_wf_approval_date", i_ValIdx); //삭제
	          idfDoc.remove("u_wf_link",          i_ValIdx); //삭제
	          
	          // 회수가 아닐때만 작동 ( 회수일때는 삭제하고 링크에 있는 원래 폴더로 되돌리므로  )
	          idfDoc.insertString("u_wf_system",         i_ValIdx, dto.getApprovalSystem());        //결재 시스템
	          idfDoc.insertString("u_wf_form",           i_ValIdx, dto.getApprovalFormName());      //결재 양식명
	          idfDoc.insertString("u_wf_title",          i_ValIdx, dto.getApprovalSubject());       //결재 제목
	          idfDoc.insertString("u_wf_key",            i_ValIdx, dto.getApprovalId());            //결재 ID
	          
		      if(!dto.getApprovalState().equals("W"))
		    	  idfDoc.insertString("u_wf_approver",            i_ValIdx, dto.getApprovalWriter()); 
		      else
		    	  idfDoc.insertString("u_wf_approver",            i_ValIdx, " "); 

	          idfDoc.insertString("u_wf_approval_date",  i_ValIdx, (new DfTime()).toString());
	          idfDoc.insertString("u_wf_link",           i_ValIdx, dto.getApprovalLink());          //결재화면 URL
		   }

		  String s_PreservFlag= psrvPMap.get( s_USecLevel );//idfDoc.getString("u_sec_level"));
	      if(null==s_PreservFlag) s_PreservFlag= psrvPMapDf.get(s_USecLevel);//idfDoc.getString("u_sec_level")); //등록된게 없으면 정책서에서 기술한 기본값
		  if(dto.getPreserverYear() == dto.getYoungGu()) {
		       s_PreservFlag="0";
		  }
		  if(null==s_PreservFlag) s_PreservFlag= idfDoc.getString("u_preserve_flag");
		  int li_PreservYear=Integer.parseInt(s_PreservFlag)==0?9999:Integer.parseInt(s_PreservFlag);
		  idfDoc.setInt("u_preserve_flag", Integer.parseInt( s_PreservFlag) );  //보존연한
		  
		  if(idfDoc.getString("u_doc_status").equals("C")) {
		      //idfDoc.setInt("u_preserve_flag",  dto.getPreserverYear());  //보존연한
		      IDfTime startDate = new DfTime() ;
		      // Convert the expiration date to a calendar object.
		      GregorianCalendar cal = new GregorianCalendar(startDate.getYear(), startDate.getMonth(),   startDate.getDay() );  
		      // Add the number of months ­1 (months start counting from 0).
		      cal.add(GregorianCalendar.MONTH, (12 * li_PreservYear) -1) ;
		      IDfTime expireDate = new DfTime (cal.getTime()) ;
		     if(li_PreservYear==9999)
		    	 idfDoc.setString("u_expired_date",  "9999-12-31 00:00:00");
		     else
		    	 idfDoc.setString("u_expired_date",  expireDate.toString());
		      
		  }
		  
		  
	      IDfTime tt = new DfTime();
		  String s_Today = new DfTime().toString();
		  
		  //IDfACL idf_Acl = idfDoc.getACL();
		  
		  //해당 결재키의 edms_auth_base 관련 정보 지움
		  
		  if( !dto.getApprovalState().equals("X")) {

			  IDfCollection idf_Colb =null;
			  
			  String s_Dql = "delete edms_auth_base object " +
				  	" where u_add_gubun='W' and u_ext_key = '" + dto.getApprovalId()+ "' and u_obj_id='" +s_ObjId +"'";

	  		  if(s_USecLevel.equals("S")) {
				  s_Dql = "delete edms_auth_base object " +
				            //" where ((u_add_gubun='W' and u_ext_key = '" + dto.getApprovalId()+ "' )  or u_author_id ='"+ dto.getUGroupOrgCabinetCd() +"' or u_author_id='"+dto.getUComOrgCabinetCd() +"'  or ( u_doc_status='C' and u_obj_id='" +s_ObjId +"' and u_author_type='D' ) ) ";
				            " where ((u_add_gubun='W' and u_ext_key = '" + dto.getApprovalId()+ "'  and u_obj_id='" +s_ObjId +"')  or  ( u_doc_status='C' and u_obj_id='" +s_ObjId +"' and u_author_type='D' and u_add_gubun='G')) ";
	  		  }
	  		  
			  idf_Colb = DCTMUtils.getCollectionByDQL(idfSession, s_Dql, DfQuery.DF_QUERY);
			  if(idf_Colb != null) idf_Colb.close();

			  
			  if(dto.getApprovalLevel().equals("G")) {
				  s_Dql = "delete edms_auth_base object " +
						  	" where u_obj_id='" +s_ObjId +"' and  u_doc_status='C' and u_author_id='"+ dto.getUComOrgCabinetCd() +"' and u_add_gubun='G' " ;
			      idf_Colb = DCTMUtils.getCollectionByDQL(idfSession, s_Dql, DfQuery.DF_QUERY);
			      if(idf_Colb != null) idf_Colb.close();
			      
	  		  }else if(dto.getApprovalLevel().equals("C")) {
				  s_Dql = "delete edms_auth_base object " +
						  	" where u_obj_id='" +s_ObjId +"' and  u_doc_status='C' and u_author_id='"+ dto.getUGroupOrgCabinetCd() +"' and u_add_gubun='G' " ;
			      idf_Colb = DCTMUtils.getCollectionByDQL(idfSession, s_Dql, DfQuery.DF_QUERY);
			      if(idf_Colb != null) idf_Colb.close();
	  		  }else if(dto.getApprovalLevel().equals("T")) {
				  s_Dql = "delete edms_auth_base object " +
						  	" where u_obj_id='" +s_ObjId +"' and  u_doc_status='C' and u_author_id in('"+ dto.getUGroupOrgCabinetCd() +"', '"+dto.getUComOrgCabinetCd() +"') and u_add_gubun='G' " ;
			      idf_Colb = DCTMUtils.getCollectionByDQL(idfSession, s_Dql, DfQuery.DF_QUERY);
			      if(idf_Colb != null) idf_Colb.close();
	  		  }
	  		  
			  s_Dql = "select distinct u_author_id, u_author_type, u_permit_type, u_add_gubun, u_doc_status from edms_auth_base where u_obj_id ='"+ idfDoc.getString("u_doc_key") +"' and u_author_type in('D','U','S')  and u_add_gubun in('P','W','S',' ','G', 'J')  " ;
				
			    
			  IDfQuery 		idf_Qry 	= null;
			  idf_Qry = new DfQuery();
			  idf_Qry.setDQL(s_Dql);

		  	  List<String> lDocStatus = new ArrayList<String>();
		  	  lDocStatus.add("C");
		  	  lDocStatus.add("L");

		  	  String ls_DocPermitType = idfDoc.getString("u_doc_status").equals("C")?"R":"D";
			  try {
				    idf_Colb = idf_Qry.execute(idfSession,DfQuery.QUERY);
			  		
			  		while(idf_Colb != null && idf_Colb.next())
			  		{
			  		    String s_Author=idf_Colb.getString("u_author_id");
			  		  
			  		    if(s_Author.contains("t_y")) continue;
			  		    if(s_Author ==null ||s_Author.equals("") || s_Author.equals(" ")|| s_Author.equals("null")) continue;

			  		    //if(s_Author.substring(0,1).equals("d")) if( !s_Author.contains("g_")) s_Author="g_"+ s_Author;
			  		    
			  		    String s_AuthorType=idf_Colb.getString("u_author_type");
			  		    String s_PermitType=idf_Colb.getString("u_permit_type");
			  		    String s_AddGubun  =idf_Colb.getString("u_add_gubun");
			  		    String s_DocStatus =idf_Colb.getString("u_doc_status");
			  		     
			  		    for(int i=0; i< lDocStatus.size(); i++) {
						     
			  		    	//S 등급은 기본권한(G)를 Closed 부서 권한에 추가하지 않는다.
			  		    	//if(s_AuthorType.equals("D") && idfDoc.getString("u_doc_status").equals("C") && lDocStatus.equals("L"))  continue;
			  		    	if(! lDocStatus.get(i).equals(s_DocStatus) || (idfDoc.getString("u_doc_status").equals("C") && lDocStatus.get(i).equals("L")))  continue;
				  		    //if(s_USecLevel.equals("S") && s_AuthorType.equals("D") && lDocStatus.get(i).equals("C") && s_AddGubun.equals("G"))  continue;
						    
			  		    	//ACL_NAME을 새로 부여해서 grant를 다시 해줌
			  		    	if(s_AuthorType.equals("S")) continue; //기본권한그룹은 ACL에서 부여된 권한이 있으므로 따로 grant하지 않음
			  		    	idfDoc.grant(s_Author, GrantedLevels.findByLabel(ls_DocPermitType), "");
			  		    	if(s_AuthorType.equals("D")) {
			  		    		//if( (!idfDoc.getString("u_sec_level").equals("S") && !idfDoc.getString("u_sec_level").equals("T")) || s_AddGubun.equals("P"))
			  		    		if( s_AddGubun.equals("P") || s_AddGubun.equals("W"))
			  		    		    idfDoc.grant(s_Author+"_sub", GrantedLevels.findByLabel(ls_DocPermitType), "");
			  		    	}
			  		    	idfDoc.save();
			  		    }
			  	    }
			  		if(idf_Colb != null) idf_Colb.close();
		      }catch (Exception e) {
		 			e.printStackTrace();
			  }	  
			  //System.out.println("MOLA: Session Cabinet Code=" + s_CabinetCode+ " ** Document CabinetCode=" + idfDoc.getString("u_cabinet_code"));
			  String s_OwnCabinetCode= idfDoc.getString("u_cabinet_code"); //문서의 보유 부서 문서함코드
			  
		      for (Map.Entry<String, String[]> entry : dto.getApprovalDraftLineM().entrySet()) {
				  for(int i=0; i < entry.getValue().length; i++) {
					 //if ( authService.checkDocAuth(dto.getDboxId(), userSession.getUser().getUserId(), GrantedLevels.DELETE.getLevel()) ) {
					     
						 if(!entry.getValue()[i].equals("")) {
							
						    String sAuthorId = entry.getValue()[i];
						    String sAuthorType=sAuthorId.contains("g_")?"D":"U";
 
						    idfDoc.grant(sAuthorId, GrantedLevels.findByLabel(ls_DocPermitType), "");
			  		    	if(sAuthorType.equals("D")) {
			  		    		//if( !idfDoc.getString("u_sec_level").equals("S") && !idfDoc.getString("u_sec_level").equals("T"))
			  		    		    idfDoc.grant(sAuthorId+"_sub", GrantedLevels.findByLabel(ls_DocPermitType), "");
			  		    	}
						    
						    idfDoc.save();
					  		if( idfDoc.getPermitEx( sAuthorId) <= 7) {
					  		     s_Dql = "delete edms_auth_base object where u_obj_id='" +s_ObjId +"' and u_author_id ='"+ sAuthorId +"' ";
								 idf_Colb = DCTMUtils.getCollectionByDQL(idfSession, s_Dql, DfQuery.DF_QUERY);
								 if(idf_Colb != null) idf_Colb.close();
					  		}
					  		
					  		int i_AuthorCnt = DCTMUtils.getCountByDQL( idfSession, " edms_auth_base where u_obj_id='"+s_ObjId +"' and u_author_id='"+sAuthorId +"' ");
					  		if(i_AuthorCnt < 1) {
				  			
							    for(int j=0; j< lDocStatus.size(); j++) {
						  	    	String s_PermitType= lDocStatus.get(j).equals("C")?"R":"D";
								    IDfPersistentObject idf_PObj = idfSession.newObject("edms_auth_base");
					  				idf_PObj.setString("u_obj_id"		, s_ObjId);
					  				idf_PObj.setString("u_obj_type"		, "D");
					  				idf_PObj.setString("u_doc_status"	, lDocStatus.get(j));
					  				
					  				idf_PObj.setString("u_permit_type"	, s_PermitType); //권한
					  				
					  				idf_PObj.setString("u_own_dept_yn"	,  sAuthorId.equals("g_"+s_OwnCabinetCode)?"Y":""); //
					  				idf_PObj.setString("u_author_id"	,  sAuthorId);
					  				idf_PObj.setString("u_author_type"	,  sAuthorType); //사용자 
					  				idf_PObj.setString("u_create_user"	, userSession.getUser().getUserId());
					  				idf_PObj.setTime  ("u_create_date"	, new DfTime());
					  				idf_PObj.setString("u_ext_key"	, dto.getApprovalId()); //
					  				idf_PObj.setString("u_add_gubun"	, "W"); //
					  				
					  				idf_PObj.save();	
							    }
					  		}
						 }
					 //}
				  }
		      }
		      for (Map.Entry<String, String[]> entry : dto.getApprovalCcLineM().entrySet()) {
				  for(int i=0; i < entry.getValue().length; i++) {
					 //if ( authService.checkDocAuth(dto.getDboxId(), userSession.getUser().getUserId(), GrantedLevels.DELETE.getLevel()) ) {
						 if(!entry.getValue()[i].equals("")) {
							    String sAuthorId = entry.getValue()[i];
							    String sAuthorType=sAuthorId.contains("g_")?"D":"U";

						  		if( idfDoc.getPermitEx( sAuthorId) > 3) {
						  			s_Dql = "UPDATE edms_auth_base  OBJECTS SET u_ext_key='" + dto.getApprovalId() + "', SET u_add_gubun='W' " +
											"WHERE u_obj_id='" + s_ObjId + "' AND u_author_id = '" + sAuthorId + "'";
									
									idf_Qry.setDQL(s_Dql);
									idf_Colb = idf_Qry.execute(idfSession, DfQuery.QUERY);
									if (idf_Colb!=null && idf_Colb.next()) idf_Colb.close();
									
									idfDoc.grant(sAuthorId, GrantedLevels.findByLabel(ls_DocPermitType), "");
					  		    	if(sAuthorType.equals("D")) {
					  		    		//if( !idfDoc.getString("u_sec_level").equals("S") && !idfDoc.getString("u_sec_level").equals("T"))
					  		    		    idfDoc.grant(sAuthorId+"_sub", GrantedLevels.findByLabel(ls_DocPermitType), "");
					  		    	}
									idfDoc.save();
						  		}else {
								    idfDoc.grant(sAuthorId, GrantedLevels.findByLabel("R"), "");
					  		    	if(sAuthorType.equals("D") ) {
					  		    		//if( !idfDoc.getString("u_sec_level").equals("S") && !idfDoc.getString("u_sec_level").equals("T"))
					  		    		    idfDoc.grant(sAuthorId+"_sub", GrantedLevels.findByLabel("R"), "");
					  		    	}
								    idfDoc.save();

								    s_Dql = "delete edms_auth_base object where u_obj_id='" +s_ObjId +"' and u_author_id ='"+ sAuthorId +"' ";
									idf_Colb = DCTMUtils.getCollectionByDQL(idfSession, s_Dql, DfQuery.DF_QUERY);
									if(idf_Colb != null) idf_Colb.close();
						  			
							  		int i_AuthorCnt = DCTMUtils.getCountByDQL( idfSession, " edms_auth_base where u_obj_id='"+s_ObjId +"' and u_author_id='"+sAuthorId +"' ");
							  		if(i_AuthorCnt < 1) {
										for(int j=0; j< lDocStatus.size(); j++) {
											
										    IDfPersistentObject idf_PObj = idfSession.newObject("edms_auth_base");
							  				idf_PObj.setString("u_obj_id"		, s_ObjId);
							  				idf_PObj.setString("u_obj_type"		, "D");
							  				idf_PObj.setString("u_doc_status"	,  lDocStatus.get(j));
							  				idf_PObj.setString("u_permit_type"	, "R"); //읽기
							  				idf_PObj.setString("u_own_dept_yn"	, sAuthorId.equals("g_"+s_OwnCabinetCode)?"Y":""); //
							  				idf_PObj.setString("u_author_id"	,  sAuthorId);
							  				idf_PObj.setString("u_author_type"	,  sAuthorType); //사용자 
							  				idf_PObj.setString("u_create_user"	, userSession.getUser().getUserId());
							  				idf_PObj.setTime  ("u_create_date"	, new DfTime());
							  				idf_PObj.setString("u_ext_key"	, dto.getApprovalId()); //
							  				idf_PObj.setString("u_add_gubun"	, "W"); //
							  				idf_PObj.save();
									    }
							  		}
						  		}
					  	    }
				      }
						 
			  }
		      
		      for (Map.Entry<String, String[]> entry : dto.getApprovalReceiveLineM().entrySet()) {
				  for(int i=0; i < entry.getValue().length; i++) {
					 // if ( authService.checkDocAuth(dto.getDboxId(), userSession.getUser().getUserId(), GrantedLevels.DELETE.getLevel()) ) {
						  if(!entry.getValue()[i].equals("")) {
							  String sAuthorId = entry.getValue()[i];
							    String sAuthorType=sAuthorId.contains("g_")?"D":"U";

						  		if( idfDoc.getPermitEx( sAuthorId) > 3) {
						  			s_Dql = "UPDATE edms_auth_base OBJECTS SET u_ext_key='" + dto.getApprovalId() + "', SET u_add_gubun='W' " +
											"WHERE u_obj_id='" + s_ObjId + "' AND u_author_id = '" + sAuthorId + "'";
									
									idf_Qry.setDQL(s_Dql);
									idf_Colb = idf_Qry.execute(idfSession, DfQuery.QUERY);
									if (idf_Colb!=null && idf_Colb.next()) idf_Colb.close();
									
								    idfDoc.grant(sAuthorId, GrantedLevels.findByLabel(ls_DocPermitType), "");
					  		    	if(sAuthorType.equals("D") ) {
					  		    		//if( !idfDoc.getString("u_sec_level").equals("S") && !idfDoc.getString("u_sec_level").equals("T"))
					  		    		    idfDoc.grant(sAuthorId+"_sub", GrantedLevels.findByLabel(ls_DocPermitType), "");
					  		    	}
	                                idfDoc.save();
									
						  		}else {
								    idfDoc.grant(sAuthorId, GrantedLevels.findByLabel("R"), "");
					  		    	if(sAuthorType.equals("D")) {
					  		    		//if( !idfDoc.getString("u_sec_level").equals("S") && !idfDoc.getString("u_sec_level").equals("T"))
					  		    		    idfDoc.grant(sAuthorId+"_sub", GrantedLevels.findByLabel("R"), "");
					  		    	}
	                                idfDoc.save();

	                                s_Dql = "delete edms_auth_base object where u_obj_id='" +s_ObjId +"' and u_author_id ='"+ sAuthorId +"' ";
									idf_Colb = DCTMUtils.getCollectionByDQL(idfSession, s_Dql, DfQuery.DF_QUERY);
									if(idf_Colb != null) idf_Colb.close();
						  			
							  		int i_AuthorCnt = DCTMUtils.getCountByDQL( idfSession, " edms_auth_base where u_obj_id='"+s_ObjId +"' and u_author_id='"+sAuthorId +"' and  u_author_type !='S' ");
							  		if(i_AuthorCnt < 1) {

										for(int j=0; j< lDocStatus.size(); j++) {

										    IDfPersistentObject idf_PObj = idfSession.newObject("edms_auth_base");
							  				idf_PObj.setString("u_obj_id"		, s_ObjId);
							  				idf_PObj.setString("u_obj_type"		, "D");
							  				idf_PObj.setString("u_doc_status"	, lDocStatus.get(j));
							  				idf_PObj.setString("u_permit_type"	, "R"); //읽기
							  				idf_PObj.setString("u_own_dept_yn"	, sAuthorId.equals("g_"+s_OwnCabinetCode)?"Y":""); //
							  				idf_PObj.setString("u_author_id"	,  sAuthorId);
							  				idf_PObj.setString("u_author_type"	,  sAuthorType); //사용자 
							  				idf_PObj.setString("u_create_user"	, userSession.getUser().getUserId());
							  				idf_PObj.setTime  ("u_create_date"	, new DfTime());
							  				idf_PObj.setString("u_ext_key"	, dto.getApprovalId()); //
							  				idf_PObj.setString("u_add_gubun"	, "W"); //
							  				idf_PObj.save();
									    }
							  		}
						  		}
					  	    }
				      }
			   }
		      
	          String s_AuthStr=  "g_"+dto.getUCabinetCode();
		  	  int i_AuthorCnt = DCTMUtils.getCountByDQL( idfSession, " edms_auth_base where u_obj_id='"+s_ObjId +"' and u_author_id='"+s_AuthStr +"' and u_add_gubun!='S' and u_doc_status='L' ");
		  	  if(i_AuthorCnt < 1) {
		  		  if(!s_USecLevel.equals("S")) {

					  IDfPersistentObject idf_PObjD = idfSession.newObject("edms_auth_base");
					  idf_PObjD.setString("u_obj_id"		, idfDoc.getString("u_doc_key"));
					  idf_PObjD.setString("u_obj_type"		, "D");
					  idf_PObjD.setString("u_doc_status"	, "L");
					  idf_PObjD.setString("u_permit_type"	, "D"); //읽기/쓰기/편집
					  idf_PObjD.setString("u_own_dept_yn"	, "Y"); //
					  idf_PObjD.setString("u_author_id"	,  s_AuthStr);
					  idf_PObjD.setString("u_author_type"	,  "D"); //사용자 
					  idf_PObjD.setString("u_create_user"	, dto.getUCreateUser());
					  idf_PObjD.setTime  ("u_create_date"	, new DfTime());
					  idf_PObjD.setString("u_add_gubun"	, "G"); //
					  idf_PObjD.save();
					  idfDoc.grant(s_AuthStr, GrantedLevels.findByLabel(ls_DocPermitType), "");
		  		      if(s_AuthStr.contains("g_") ) {
		  		    	  if( !idfDoc.getString("u_sec_level").equals("S") && !idfDoc.getString("u_sec_level").equals("T"))
		  		              idfDoc.grant(s_AuthStr+"_sub", GrantedLevels.findByLabel(ls_DocPermitType), "");
		  		      }
		  		      idfDoc.save();
		  		  }
		  	  }
			  if(!dto.getApprovalLevel().equals("S")) {
	              if(dto.getApprovalLevel().equals("G")) s_AuthStr=dto.getUGroupOrgCabinetCd();
	              else if(dto.getApprovalLevel().equals("C")) s_AuthStr = dto.getUComOrgCabinetCd();
				  i_AuthorCnt = DCTMUtils.getCountByDQL( idfSession, " edms_auth_base where u_obj_id='"+s_ObjId +"' and u_author_id='"+s_AuthStr +"' and u_add_gubun !='S'  and u_doc_status='C'  ");
				  if(i_AuthorCnt < 1) {
					      IDfPersistentObject idf_PObjD = idfSession.newObject("edms_auth_base");
						  idf_PObjD = idfSession.newObject("edms_auth_base");
					      idf_PObjD.setString("u_obj_id"		, idfDoc.getString("u_doc_key"));
					      idf_PObjD.setString("u_obj_type"		, "D");
					      idf_PObjD.setString("u_doc_status"	, "C");
					      idf_PObjD.setString("u_permit_type"	, "R"); //읽기/쓰기/편집 
					      idf_PObjD.setString("u_own_dept_yn"	, "Y"); //
					      idf_PObjD.setString("u_author_id"	,  s_AuthStr);
					      idf_PObjD.setString("u_author_type"	,  "D"); //사용자 
					      idf_PObjD.setString("u_create_user"	, dto.getUCreateUser());
					      idf_PObjD.setTime  ("u_create_date"	, new DfTime());
					      idf_PObjD.setString("u_add_gubun"	, "G"); //
					      idf_PObjD.save();
				   }
				   idfDoc.grant(s_AuthStr, GrantedLevels.findByLabel(ls_DocPermitType), "");
	  		       if(s_AuthStr.contains("g_") ) {
	  		    	   if( !idfDoc.getString("u_sec_level").equals("S") && !idfDoc.getString("u_sec_level").equals("T"))
	  		               idfDoc.grant(s_AuthStr+"_sub", GrantedLevels.findByLabel(ls_DocPermitType), "");
	  		       }
	  		       idfDoc.save();
		  	  }
		      
		       //if(s_USecLevel.equals("S")) {
			  	    i_AuthorCnt = DCTMUtils.getCountByDQL( idfSession, " edms_auth_base where u_obj_id='"+s_ObjId +"' and u_author_id='"+dto.getApprovalWriter() +"'  and u_doc_status='C' ");
			  	    if(i_AuthorCnt < 1) {
					    IDfPersistentObject idf_PObj = idfSession.newObject("edms_auth_base");
		  				idf_PObj.setString("u_obj_id"		, s_ObjId);
		  				idf_PObj.setString("u_obj_type"		, "D");
		  				idf_PObj.setString("u_doc_status"	, "C");
		  				idf_PObj.setString("u_permit_type"	, "R"); //읽기
		  				idf_PObj.setString("u_own_dept_yn"	,  ""); //
		  				idf_PObj.setString("u_author_id"	,  dto.getApprovalWriter());
		  				idf_PObj.setString("u_author_type"	,  "U"); //사용자 
		  				idf_PObj.setString("u_create_user"	, userSession.getUser().getUserId());
		  				idf_PObj.setTime  ("u_create_date"	, new DfTime());
		  				idf_PObj.setString("u_ext_key"	, dto.getApprovalId()); //
		  				idf_PObj.setString("u_add_gubun"	, "G"); //
		  				idf_PObj.save();
					    idfDoc.grant(s_AuthStr, GrantedLevels.findByLabel(ls_DocPermitType), "");
					    idfDoc.save();
			  	    }
		      //}
			  idfDoc.setString("u_update_date", (new DfTime()).toString());
						  	    
		      idfDoc.save();
		      if(idf_Colb != null) idf_Colb.close();
	      //첨부로그 
	 	  String sOwnSrDeptOrgId = gwDeptService.selectOrgIdByCabinetcode(dto.getUCabinetCode());
		  LogDoc logDoc = LogDoc.builder()
			          .uJobCode( "AP")
			          .uDocId(idfDoc.getString("r_object_id"))
			          .uDocKey(idfDoc.getString("u_doc_key"))
			          .uDocName(idfDoc.getString("title").replaceAll("'", "''"))
			          .uDocVersion(Integer.parseInt(idfDoc.getVersionLabel(0).substring(0, idfDoc.getVersionLabel(0).indexOf(".")))+"")
			          .uFileSize(Long.parseLong(idfDoc.getString("r_content_size")))
			          .uOwnDeptcode(sOwnSrDeptOrgId)
			          .uActDeptCode(userSession.getUser().getOrgId())
			          .uJobUser(userSession.getUser().getUserId())
			          .uJobUserType(dto.getUJobUserType()==null ? "P" : dto.getUJobUserType()) //작업자구분(P:개인, D:부서관리자, C:사별관리자, G:그룹관리자, S:시스템)
			          .uDocStatus(idfDoc.getString("u_doc_status"))
			          .uSecLevel(idfDoc.getString("u_sec_level"))
			          .uCabinetCode(idfDoc.getString("u_cabinet_code"))
			          .uJobGubun("H") //O:원본, H:html파일
			          .uUserIp(dto.getURequestIp())			// 받아야함.
			          .uAttachSystem("APPR")
			          .build();
			      insertLog(logDoc);
	      
	      
	       //preFolderId 이 전자결재 타입인지 확인 필요
	       if(!preFolderId.equals("")) {
	    	  IDfPersistentObject idFolderInfo = idfSession.getObject(new DfId(preFolderId));
	          
	          String preFolderType=idFolderInfo.getString("u_fol_type");
	          
	          if(preFolderType.equals(FolderType.DWY.toString())  ||  preFolderType.equals(FolderType.DWT.toString())) {
	        	  System.out.println("전자결재함에서 참조된 Dbox 문서로서 Link를 만들지 않음:u_fol_id=" +preFolderId+ ":u_fol_type:"+preFolderType);
	          }else {
	        	  DocLink docLink = new DocLink();
	        	  
	        	  if( !dto.getApprovalState().equals("X")) { //dbox파일을 첨부해서 링크를 생성하는데, 상태가 결재회수는 아닌경우, 결재회수면 이미 만들어진걸 삭제
	        		  dto.setUDocKey(idfDoc.getString("u_doc_key"));
		    	      idfDocLink=docLink.createDocLink(idfSession, dto, preFolderId, "W"); //전자결재는 W로 넘김. 다른 경우는 W가 아닌 값을 사용
		    	      if(idfDocLink !=null)     idfDocLink.save(); //이미 Link파일이 생성된 경우
	        	  }else{ 
	   			      String uFolId=docLink.removeDocLink(idfSession, s_CabinetCode, dto.getApprovalId(), "C", dto.getApprovalWriter()); //문서묶음(Contents)의 삭제( 문서id묶음)
	  			      idfDoc.setString("u_fol_id", uFolId); //링크를 삭제하면 링크가 갖고있던 이전폴더id(u_fol_id)로 edms_doc의 u_fol_id를 변경해준다.(부서함->전자결재/temp 로 왔던것을 다시 부서함 폴더로 되돌림)        		  
	        	  }
	          }
	       }else { //폴더 루트에서 첨부된 파일
	    	      System.out.println("================="+ DfId.isObjectId(dto.getDboxId()));
	        	  DocLink docLink = new DocLink();
	        	  if( !dto.getApprovalState().equals("X")) { //dbox파일을 첨부해서 링크를 생성하는데, 상태가 결재회수는 아닌경우, 결재회수면 이미 만들어진걸 삭제
	        		  dto.setUDocKey(idfDoc.getString("u_doc_key"));
		    	      idfDocLink=docLink.createDocLink(idfSession, dto, preFolderId, "W"); //전자결재는 W로 넘김. 다른 경우는 W가 아닌 값을 사용
		    	      if(idfDocLink !=null)     idfDocLink.save(); //이미 Link파일이 생성된 경우
	        	  }else{ 
	   			      String uFolId=docLink.removeDocLink(idfSession, s_CabinetCode, dto.getApprovalId(), "C", dto.getApprovalWriter()); //문서묶음(Contents)의 삭제( 문서id묶음)
	  			      idfDoc.setString("u_fol_id", uFolId); //링크를 삭제하면 링크가 갖고있던 이전폴더id(u_fol_id)로 edms_doc의 u_fol_id를 변경해준다.(부서함->전자결재/temp 로 왔던것을 다시 부서함 폴더로 되돌림)        		  
	        	  }
	       }
	       idfDoc.save();

	       //------------------------------------------
		   // 과거 버전이 있는 경우 처리
		   //------------------------------------------
		   if(!idfDoc.getString("u_doc_key").equals(idfDoc.getObjectId().toString()))
		   {
					String s_VerId = "";
					IDfDocument idf_VerDoc = null;
					IDfCollection idf_VerCol = idfDoc.getVersions(null);
					
					while(idf_VerCol != null && idf_VerCol.next())
					{
						s_VerId = idf_VerCol.getString("r_object_id");
						//과거 버전만 처리하기 위해 현재 버전은 skip
						if(s_VerId.equals(idfDoc.getObjectId().toString()))
							continue;
						
						//권한은 현재 버전의 ACL Name을 사용하면 그대로 적용 됨
						idf_VerDoc = (IDfDocument)idfSession.getObject(new DfId(s_VerId));
						idf_VerDoc.setACLDomain(idfSession.getDocbaseOwnerName());
						idf_VerDoc.setACLName(idfDoc.getString("acl_name"));
						
						//위치 변경은 ps_RcevFolId 값이 있는 경우만
						idf_VerDoc.setString("u_fol_id", idfDoc.getString("u_fol_id"));
						idf_VerDoc.setString("u_pr_code", idfDoc.getString("u_pr_code"));
						idf_VerDoc.setString("u_pr_type", idfDoc.getString("u_pr_type"));
						
						idf_VerDoc.setString("u_cabinet_code", idfDoc.getString("u_cabinet_code"));
						idf_VerDoc.save();
					}
					if(idf_VerCol != null) idf_VerCol.close(); 
		   }
	       //idf_Acl.save();
		  ///////////////////////	  
    
	      if(idf_Colb != null) idf_Colb.close();
	      idfSession.commitTrans();
		}
	  }
	} catch (Exception e) {
	      e.printStackTrace();
	      returnMsg=e.getMessage();
	      returnMsg.replaceAll("/", "_");
	} finally {
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
	return returnMsg;
}


//dbox파일을 첨부로 한 경우, 권한만 부여하는 경우( dbox 밴드 첨부파일 )
@Override
@Transactional
public String updateBandWfDoc(UserSession userSession, IDfSession idfSess, DboxAttaDocDto doc) throws Exception {

  IDfSession idfSession = idfSess != null ? idfSess : DCTMUtils.getAdminSession();  //관리자 세션을 가져옴
  //IDfSession idfSession = idfSess != null ? idfSess : this.getIdfSession(userSession);
  
  String s_ObjId = doc.getDboxId();
  IDfSession adSess =null; //admin Session
  IDfCollection idf_Col = null;
  try { 
    if (!idfSession.isTransactionActive()) {
        idfSession.beginTrans();
    }
    s_ObjId 	= DCTMUtils.getCurrentObjectID(idfSession, s_ObjId); //문서의 최신버전 r_object_id를 가져온다.
    if(s_ObjId.equals("")) {
	    throw new NotFoundException("문서 최신버전이 변경됨[Documentum]");
	}
    
    IDfDocument idfDoc = (IDfDocument)idfSession.getObject(new DfId(s_ObjId));
    s_ObjId = idfDoc.getString("u_doc_key");//문서Key

    adSess = DCTMUtils.getAdminSession();
 	if (!adSess.isTransactionActive()) {
 	    adSess.beginTrans();
 	}

    if(idfDoc.getString("u_delete_status").equals("D") || idfDoc.getString("u_delete_status").equals("E")) {
	    throw new RuntimeException("문서가 삭제되어첨부할 수 없습니다");
		  //'R:요청중, A:폐기승인, D:삭제(휴지통), E:삭제(보존년한), P:상위폴더 삭제에 의한 삭제, T:임시저장'),
	 }else  if(idfDoc.getString("u_delete_status").equals("P")) 
	 {
	    throw new RuntimeException("문서가 포함된 폴더가 삭제되어 첨부할 수 없습니다");
		  //'R:요청중, A:폐기승인, D:삭제(휴지통), E:삭제(보존년한), P:상위폴더 삭제에 의한 삭제, T:임시저장'),
	 }else if(!idfDoc.getString("u_delete_status").trim().equals("")) {
		 throw new RuntimeException("문서의 상태가 '"+idfDoc.getString("u_delete_status")+"' 로 변경되었습니다. 시스템관리자에 문의바랍니다");
	 }
	//IDfACL idf_Acl = idfDoc.getACL();
	String s_OwnCabinetCode=idfDoc.getString("u_cabinet_code"); //문서의 보유 부서 문서함코드
	if(idfDoc.isCheckedOut())
	{
	    System.out.println( "#MOLA_LOCK:"+ idfDoc.getLockOwner()+  " 편집중 문서, Lock해제하고 결재진행");
		idfDoc.cancelCheckout();
	}
	
    //해당 결재키의 edms_auth_base 관련 정보 지움
    String s_Dql = "delete edms_auth_base object " +
	    	" where u_add_gubun='W' and u_ext_key = '" + doc.getKeyId()+ "' and u_obj_id='" +s_ObjId +"'";

    idf_Col = DCTMUtils.getCollectionByDQL(idfSession, s_Dql, DfQuery.DF_QUERY);
    if(idf_Col != null) idf_Col.close();
	
    for (Map.Entry<String, String[]> entry : doc.getBandRefLineM().entrySet()) {
        for(int i=0; i < entry.getValue().length; i++)
		    //if ( !authService.checkDocAuth(doc.getDboxId(), userSession.getUser().getUserId(), GrantedLevels.READ.getLevel()) ) {
        	    if(!entry.getValue()[i].equals("")) {
        	    	
				    String sAuthorId = entry.getValue()[i];
				    String sAuthorType="U";
				    if(sAuthorId.contains("g_")) {
				    	sAuthorType="D";
				    }
				    //idf_Acl.grant(sAuthorId, GrantedLevels.findByLabel("R"), "");
				    
				    if( idfDoc.getPermitEx( sAuthorId) < 3) {
				        idfDoc.grant(entry.getValue()[i], GrantedLevels.findByLabel("R"), "");
				    }
  		            if(sAuthorType.equals("D")) {
  		                idfDoc.grant(sAuthorId+"_sub", GrantedLevels.findByLabel("R"), "");
	  		        }
			  	    int i_AuthorCnt = DCTMUtils.getCountByDQL( idfSession, " edms_auth_base where u_obj_id='"+s_ObjId +"' and u_author_id='"+ sAuthorId +"'  and u_doc_status='" +idfDoc.getString("u_doc_status")+"'");
			  	    if(i_AuthorCnt < 1) {
					    IDfPersistentObject idf_PObj = idfSession.newObject("edms_auth_base");
		  				idf_PObj.setString("u_obj_id"		, s_ObjId);
		  				idf_PObj.setString("u_obj_type"		, "D");
		  				idf_PObj.setString("u_doc_status"	, idfDoc.getString("u_doc_status"));
		  				idf_PObj.setString("u_permit_type"	, "R"); //읽기
		  				idf_PObj.setString("u_own_dept_yn"	,  sAuthorId.equals("g_"+s_OwnCabinetCode)?"Y":""); //
		  				idf_PObj.setString("u_author_id"	,  sAuthorId);
		  				idf_PObj.setString("u_author_type"	,  sAuthorType); //사용자 
		  				idf_PObj.setString("u_create_user"	, userSession.getUser().getUserId());
		  				idf_PObj.setTime  ("u_create_date"	, new DfTime());
		  				idf_PObj.setString("u_ext_key"	    ,  doc.getKeyId()); //
		  				idf_PObj.setString("u_add_gubun"	, "W"); //
		  				
		  				idf_PObj.save();
			  	    }
        	    }
    }

	idfDoc.setTime("u_update_date", new DfTime());
	
	String sOwnSrDeptOrgId = gwDeptService.selectOrgIdByCabinetcode( idfDoc.getString("u_cabinet_code"));
	LogDoc logDoc = LogDoc.builder()
	          .uJobCode( "AT")
	          .uDocId(idfDoc.getString("r_object_id"))
	          .uDocKey(idfDoc.getString("u_doc_key"))
	          .uDocName(idfDoc.getString("title").replaceAll("'", "''"))
	          .uDocVersion(Integer.parseInt(idfDoc.getVersionLabel(0).substring(0, idfDoc.getVersionLabel(0).indexOf(".")))+"")
	          .uFileSize(Long.parseLong(idfDoc.getString("r_content_size")))
	          .uOwnDeptcode(sOwnSrDeptOrgId)
	          .uActDeptCode(userSession.getUser().getOrgId())
	          .uJobUser(userSession.getUser().getUserId())
	          .uJobUserType(doc.getUJobUserType()==null ? "P" : doc.getUJobUserType()) //작업자구분(P:개인, D:부서관리자, C:사별관리자, G:그룹관리자, S:시스템)
	          .uDocStatus(idfDoc.getString("u_doc_status"))
	          .uSecLevel(idfDoc.getString("u_sec_level"))
	          .uCabinetCode(idfDoc.getString("u_cabinet_code"))
	          .uJobGubun("H") //O:원본, H:html파일
	          .uUserIp(doc.getURequestIp())			// 받아야함.
	          .uAttachSystem("BAND")
	          .build();
	      insertLog(logDoc);	

    //idfDoc.setString("u_doc_status"    , "C"); //파일 상태를 closed로 바꿔준다
    //idf_Acl.save();
    idfDoc.save();
    //------------------------------------------
    // 과거 버전이 있는 경우 처리
	//------------------------------------------
	if(!idfDoc.getString("u_doc_key").equals(idfDoc.getObjectId().toString()))
	{
				String s_VerId = "";
				IDfDocument idf_VerDoc = null;
				IDfCollection idf_VerCol = idfDoc.getVersions(null);
				
				while(idf_VerCol != null && idf_VerCol.next())
				{
					s_VerId = idf_VerCol.getString("r_object_id");
					//과거 버전만 처리하기 위해 현재 버전은 skip
					if(s_VerId.equals(idfDoc.getObjectId().toString()))
						continue;
					
					//권한은 현재 버전의 ACL Name을 사용하면 그대로 적용 됨
					idf_VerDoc = (IDfDocument)idfSession.getObject(new DfId(s_VerId));
					idf_VerDoc.setACLDomain(idfSession.getDocbaseOwnerName());
					idf_VerDoc.setACLName(idfDoc.getString("acl_name"));
					
					//위치 변경은 ps_RcevFolId 값이 있는 경우만
					idf_VerDoc.setString("u_fol_id", idfDoc.getString("u_fol_id"));
					idf_VerDoc.setString("u_pr_code", idfDoc.getString("u_pr_code"));
					idf_VerDoc.setString("u_pr_type", idfDoc.getString("u_pr_type"));
					
					idf_VerDoc.setString("u_cabinet_code", idfDoc.getString("u_cabinet_code"));
					idf_VerDoc.save();
				}
				if(idf_VerCol != null) idf_VerCol.close(); 
	}	           
	idfSession.commitTrans();
	adSess.commitTrans();
	    
  } catch (Exception e) {
	  e.printStackTrace();
	  s_ObjId="";
  } finally {
	  if(idf_Col != null) idf_Col.close();
	  
	  if (adSess != null) {
	    if (adSess.isTransactionActive()) {
	    	adSess.abortTrans();
	    }
	    if (adSess.isConnected()) {
	    	adSess.disconnect();
	    }
	  }
	  if (idfSession != null) {
	    if (idfSession.isTransactionActive()) {
	      idfSession.abortTrans();
	    }
	    if (idfSession.isConnected()) {
	      this.sessionRelease(userSession.getDUserId(), idfSession);
	    }
	  }
   }
   //System.out.println("");
  return s_ObjId;
}



@Override
@Transactional
public String removeWfDoc(UserSession userSession, IDfSession idfSess, String keyString, String keyGubun , String attachType, String approvalId, String UserId) throws Exception {

	  String cabinetCode ="";//userSession.getUser().getDeptCabinetcode();

	  IDfSession idfSession  = DCTMUtils.getAdminSession();
		  
	  try { 
		    if (!idfSession.isTransactionActive()) {
		        idfSession.beginTrans();
	        }
			IDfDocument         idfDoc = null;
		    IDfPersistentObject idfDocLink= null;

			/////////////////////////        
		    String s_ObjId 	= DCTMUtils.getCurrentObjectID(idfSession, keyString); //문서의 최신버전 r_object_id를 가져온다.
			if(s_ObjId.equals("")) {
			      throw new NotFoundException("문서 최신버전이 변경됨[Documentum]");
			}
			idfDoc = (IDfDocument)idfSession.getObject(new DfId(s_ObjId));
			cabinetCode = idfDoc.getString("u_cabinet_code");
			// 업무 속성 지정
			//repeating 항목은 결재Key값으로 찾아서 삭제한 다음, 같은 위치에 insert로 대체한다.
			//1. edms_doc이나 edms_doc_imp 속성 정리
		    int i_ValIdx = idfDoc.findString("u_wf_key", approvalId); //결재 Key값에 해당하는 index값을 찾아서
			if(i_ValIdx >= 0){ //최초 등록이면 append해준다
		        idfDoc.remove("u_wf_system",        i_ValIdx); //삭제
		        idfDoc.remove("u_wf_form",          i_ValIdx); //삭제
		        idfDoc.remove("u_wf_title",         i_ValIdx); //삭제
		        idfDoc.remove("u_wf_approver",      i_ValIdx); //삭제
		        idfDoc.remove("u_wf_approval_date", i_ValIdx); //삭제
		        idfDoc.remove("u_wf_key",           i_ValIdx); //삭제
		        idfDoc.remove("u_wf_link",          i_ValIdx); //삭제
			}
			// 2.권한 회수
			String s_Dql = "select u_author_id, u_author_type, u_permit_type, u_add_gubun, u_doc_status from edms_auth_base where u_obj_id ='"+ idfDoc.getString("u_doc_key") +"' and u_author_type ='W' and u_ext_key = '" + approvalId+"'" ;
			    
		    IDfQuery 		idf_Qry 	= null;
		    idf_Qry = new DfQuery();
			idf_Qry.setDQL(s_Dql);
			
			//IDfACL idf_Acl = idfDoc.getACL();
			
			try {
				  IDfCollection idf_Col = DCTMUtils.getCollectionByDQL(idfSession, s_Dql, DfQuery.DF_QUERY);
                  idf_Col = idf_Qry.execute(idfSession,DfQuery.QUERY);
			  		
		  		  while(idf_Col != null && idf_Col.next())
			  	  {
			  		    String s_Author=idf_Col.getString("u_author_id");
		  		    	//idf_Acl.revoke(s_Author, null);
			  		    idfDoc.revoke(s_Author, null);
			  	  }
			  	  if(idf_Col != null) idf_Col.close();
		      }catch (Exception e) {
		 			e.printStackTrace();
			  }				
			  
		    //3. 해당 결재키의 edms_auth_base 관련 정보 지움
		    s_Dql = "delete edms_auth_base object " +
			    	" where u_add_gubun='W' and u_ext_key = '" + approvalId+ "' and u_obj_id='" +idfDoc.getString("u_doc_key") +"'";
		    IDfCollection idf_Col = DCTMUtils.getCollectionByDQL(idfSession, s_Dql, DfQuery.DF_QUERY);
		    if(idf_Col != null) idf_Col.close();
			  
			//TO DO : edms_doc에서 work_flow관련 정보를 찾아서 remove
			//edms_doc 을 찾아서 u_fol_id 값을 알아낸 다음,
			//edms_folder에서 폴더타입이 DWY:부서 전자결재 년도, DWT:부서 전자결재 임시  인지 확인
			if(!attachType.equals("pc")) {  //dbox 첨부파일이면 work flow 파일리스트에 있는 링크를 삭제
				DocLink docLink = new DocLink();
			    String uFolId=docLink.removeDocLink(idfSession, cabinetCode, idfDoc.getString("u_doc_key"), keyGubun, UserId );
				
			    idfDoc.setString("u_fol_id", uFolId); //링크를 삭제하면 링크가 갖고있던 이전폴더id(u_fol_id)로 edms_doc의 u_fol_id를 변경해준다.(부서함->전자결재/temp 로 왔던것을 다시 부서함 폴더로 되돌림)
			}
			//idf_Acl.save();
			idfDoc.save();
			idfSession.commitTrans();
			
		} catch (Exception e) {
			System.out.println(e.getMessage());
			throw e;
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
	  
		return "";
 }

public String makeDWVFolder(IDfSession idf_Sess, String cabinetCode, String folderType, String folderName, String yearFolName, String userId) throws Exception
{
		IDfPersistentObject idfFolObj = null;
		String sFolderId = "";
		if(folderType.equals("DWY")) {
		    sFolderId=idf_Sess.getIdByQualification("edms_folder where u_cabinet_code='"+ cabinetCode+"' and u_fol_type='"+ folderType+"'  and u_fol_name='" + folderName +"'").toString();
		}else {
			sFolderId=idf_Sess.getIdByQualification("edms_folder where u_cabinet_code='"+ cabinetCode+"' and u_fol_type='"+ folderType+"'  and u_fol_name='" + folderName +"' and u_up_fol_id=(select r_object_id from edms_folder where u_cabinet_code='"+ cabinetCode+"' and u_fol_type='DWY'  and u_fol_name='" + yearFolName +"') ").toString();
		}
		
		
		String uFolId="";
		String s_ComId   = gwDeptService.selectComCodeByCabinetCode(cabinetCode).toLowerCase();

		if(folderType.equals("DWT")) {
			uFolId = idf_Sess.getIdByQualification("edms_folder where u_cabinet_code='"+ cabinetCode+"' and u_fol_type='DWY'  and u_fol_name='" + yearFolName +"'").toString();
			if(!DfId.isObjectId(uFolId)) {

				idfFolObj = (IDfPersistentObject) idf_Sess.newObject("edms_folder");
	
				idfFolObj.setString("u_cabinet_code",    cabinetCode );
				idfFolObj.setString("u_fol_type",        "DWY" );
				idfFolObj.setString("u_fol_name",        yearFolName );
				idfFolObj.setString("u_sec_level",       "T" );
				idfFolObj.setString("u_fol_status",      "O" );
				idfFolObj.setString("u_editable_flag",   "1" );
				idfFolObj.setString("u_delete_status",   " " );
				idfFolObj.setString("u_create_user",     userId);
				idfFolObj.setString("u_create_date",     (new DfTime()).toString());
				
				idfFolObj.save();
				uFolId = idfFolObj.getObjectId().toString();
				
				String s_Dql_d = "delete edms_auth_base object " +
				        " where u_obj_id = '" + uFolId + "'  and (u_author_type='S'  or u_add_gubun='G')";  // 보안등급기본 제거, 권한이 낮으면 이전에 추가되었던 '속성'추가 항목도 제거
				
				IDfCollection idf_Col= DCTMUtils.getCollectionByDQL(idf_Sess, s_Dql_d, DfQuery.DF_QUERY);
				
		        if(idf_Col != null) idf_Col.close();

				List<String> lst_Author = new ArrayList<String>();
				lst_Author.add("g_chairman");
				lst_Author.add("g_audit_wf");
				lst_Author.add("g_" + s_ComId + "_mgr_g_a");
				lst_Author.add("g_" + s_ComId + "_mgr_g_b");
				
				lst_Author.add("g_" + cabinetCode + "_imwon");
				lst_Author.add("g_" + cabinetCode + "_chief");
				lst_Author.add("g_" + cabinetCode + "_mgr_g_a");
				lst_Author.add("g_" + cabinetCode + "_mgr_g_b");
				lst_Author.add("g_" + cabinetCode + "_old");
				lst_Author.add("g_" + cabinetCode); 
				
				for(String s_Author : lst_Author)
				{
			  		int i_AuthorCnt = DCTMUtils.getCountByDQL( idf_Sess, " edms_auth_base where u_obj_id='"+uFolId +"' and u_author_id='"+s_Author +"' and u_doc_status='L'  ");
			  	    if(i_AuthorCnt < 1) {
					
						IDfPersistentObject idf_PObj = idf_Sess.newObject("edms_auth_base");
						
						idf_PObj.setString("u_obj_id"		, uFolId);
						idf_PObj.setString("u_obj_type"		, "F"); //폴더
						idf_PObj.setString("u_doc_status"	, "L");
						idf_PObj.setString("u_permit_type"	, "D");
						idf_PObj.setString("u_own_dept_yn"	, s_Author.equals("g_" + cabinetCode) ? "Y" : "");
						idf_PObj.setString("u_author_id"	, s_Author);
						idf_PObj.setString("u_author_type"	, s_Author.equals("g_" + cabinetCode) ? "D" : "S");
						idf_PObj.setString("u_create_user"	, userId);
						idf_PObj.setTime  ("u_create_date"	, new DfTime());
						idf_PObj.setString("u_add_gubun"	, "G");
						
						idf_PObj.save(); 
			  	    }
			  	    
			  	    i_AuthorCnt = DCTMUtils.getCountByDQL( idf_Sess, " edms_auth_base where u_obj_id='"+uFolId +"' and u_author_id='"+"g_" + cabinetCode +"' and u_doc_status='C'  ");
			  	    if(i_AuthorCnt < 1) {
			  	        IDfPersistentObject idf_PObj = idf_Sess.newObject("edms_auth_base");
					    if( s_Author.equals("g_" + cabinetCode)) {  //Closed이면서 author_type이 S 인 것이 들어가지 않도록 함
							idf_PObj.setString("u_obj_id"		, uFolId);
							idf_PObj.setString("u_obj_type"		, "F"); //폴더
							idf_PObj.setString("u_doc_status"	, "C");
							idf_PObj.setString("u_permit_type"	, "D");
							idf_PObj.setString("u_own_dept_yn"	, s_Author.equals("g_" + cabinetCode) ? "Y" : "");
							idf_PObj.setString("u_author_id"	, s_Author);
							idf_PObj.setString("u_author_type"	, s_Author.equals("g_" + cabinetCode) ? "D" : "S");
							idf_PObj.setString("u_create_user"	, userId);
							idf_PObj.setTime  ("u_create_date"	, new DfTime());
							idf_PObj.setString("u_add_gubun"	, "G");
						    
							idf_PObj.save();
					    }
			  	    }
			    }							
			}
		}
		
		if(!DfId.isObjectId(sFolderId))
		{
			idfFolObj = (IDfPersistentObject) idf_Sess.newObject("edms_folder");

			idfFolObj.setString("u_up_fol_id",       uFolId);
			idfFolObj.setString("u_cabinet_code",    cabinetCode );
			idfFolObj.setString("u_fol_type",        folderType );
			idfFolObj.setString("u_fol_name",        folderName );
			idfFolObj.setString("u_sec_level",       "T" );
			idfFolObj.setString("u_fol_status",      "O" );
			idfFolObj.setString("u_editable_flag",   "1" );
			idfFolObj.setString("u_delete_status",   " " );
			idfFolObj.setString("u_create_user",     userId);
			idfFolObj.setString("u_create_date",     (new DfTime()).toString());
			
			idfFolObj.save();
			sFolderId = idfFolObj.getObjectId().toString();
			
	
			String s_Dql_d = "delete edms_auth_base object " +
			        " where u_obj_id = '" + sFolderId + "'  and (u_author_type='S'  or u_add_gubun='G')";  // 보안등급기본 제거, 권한이 낮으면 이전에 추가되었던 '속성'추가 항목도 제거
			
			IDfCollection idf_Col= DCTMUtils.getCollectionByDQL(idf_Sess, s_Dql_d, DfQuery.DF_QUERY);
			
	        if(idf_Col != null) idf_Col.close();
	
			
			List<String> lst_Author = new ArrayList<String>();
			lst_Author.add("g_chairman");
			lst_Author.add("g_audit_wf");
			lst_Author.add("g_" + s_ComId + "_mgr_g_a");
			lst_Author.add("g_" + s_ComId + "_mgr_g_b");
			
			lst_Author.add("g_" + cabinetCode + "_imwon");
			lst_Author.add("g_" + cabinetCode + "_chief");
			lst_Author.add("g_" + cabinetCode + "_mgr_g_a");
			lst_Author.add("g_" + cabinetCode + "_mgr_g_b");
			lst_Author.add("g_" + cabinetCode + "_old");
			lst_Author.add("g_" + cabinetCode); 
			
			for(String s_Author : lst_Author)
			{
		  		int i_AuthorCnt = DCTMUtils.getCountByDQL( idf_Sess, " edms_auth_base where u_obj_id='"+sFolderId +"' and u_author_id='"+s_Author +"' and u_doc_status='L'  ");
		  	    if(i_AuthorCnt < 1) {
				
					IDfPersistentObject idf_PObj = idf_Sess.newObject("edms_auth_base");
					
					idf_PObj.setString("u_obj_id"		, sFolderId);
					idf_PObj.setString("u_obj_type"		, "F"); //폴더
					idf_PObj.setString("u_doc_status"	, "L");
					idf_PObj.setString("u_permit_type"	, "D");
					idf_PObj.setString("u_own_dept_yn"	, s_Author.equals("g_" + cabinetCode) ? "Y" : "");
					idf_PObj.setString("u_author_id"	, s_Author);
					idf_PObj.setString("u_author_type"	, s_Author.equals("g_" + cabinetCode) ? "D" : "S");
					idf_PObj.setString("u_create_user"	, userId);
					idf_PObj.setTime  ("u_create_date"	, new DfTime());
					idf_PObj.setString("u_add_gubun"	, "G");
					
					idf_PObj.save(); 
		  	    }
		  	    
		  	    i_AuthorCnt = DCTMUtils.getCountByDQL( idf_Sess, " edms_auth_base where u_obj_id='"+sFolderId +"' and u_author_id='"+"g_" + cabinetCode +"' and u_doc_status='C'  ");
		  	    if(i_AuthorCnt < 1) {
		  	        IDfPersistentObject idf_PObj = idf_Sess.newObject("edms_auth_base");
				    if( s_Author.equals("g_" + cabinetCode)) {  //Closed이면서 author_type이 S 인 것이 들어가지 않도록 함
						idf_PObj.setString("u_obj_id"		, sFolderId);
						idf_PObj.setString("u_obj_type"		, "F"); //폴더
						idf_PObj.setString("u_doc_status"	, "C");
						idf_PObj.setString("u_permit_type"	, "D");
						idf_PObj.setString("u_own_dept_yn"	, s_Author.equals("g_" + cabinetCode) ? "Y" : "");
						idf_PObj.setString("u_author_id"	, s_Author);
						idf_PObj.setString("u_author_type"	, s_Author.equals("g_" + cabinetCode) ? "D" : "S");
						idf_PObj.setString("u_create_user"	, userId);
						idf_PObj.setTime  ("u_create_date"	, new DfTime());
						idf_PObj.setString("u_add_gubun"	, "G");
					    
						idf_PObj.save();
				    }
		  	    }
			}
	  }

	  return sFolderId;
 }

 @Override
  public CustomInputStreamResource downloadInfsInstaller() throws Exception {
    IDfSession idfSession = null;
    IDfDocument idfDoc = null;
    InputStream inputStream = null;
    CustomInputStreamResource customInputStream = null;
    String objId = null;
    try {
      objId = codeService.getAgentInstallerId();
      idfSession = this.getIdfSession(UserSession.builder().docbase(DCTMConstants.DOCBASE).dUserId(DCTMConstants.DCTM_ADMIN_ID).build());
      if (DfId.isObjectId(objId)) {
        idfDoc = (IDfDocument) idfSession.getObject(new DfId(objId));
        String fileName = idfDoc.getString("object_name");
        customInputStream = new CustomInputStreamResource(idfDoc.getContent(), idfDoc.getContentSize(), fileName);
      } else {
        throw new Exception("Object Id is not valid");
      }
    } catch (DfIdNotFoundException e) {
      throw new ForbiddenException("문서 권한 없음[Documentum]");
    } catch (Exception e) {
      throw e;
    } finally {
      if (idfSession != null) {
        if (idfSession.isTransactionActive()) {
          idfSession.abortTrans();
        }
        if (idfSession.isConnected()) {
          sessionRelease(DCTMConstants.DCTM_ADMIN_ID, idfSession);
        }
      }
      if (inputStream != null) {
        inputStream.close();
      }
    }
    return customInputStream;
  }
 
	@Override
	public String makeSynapViewerUrl(String dataId, String token) throws Exception {
		MultiValueMap<String, Object> multipartParams = new LinkedMultiValueMap<>();
		
		// 사이냅뷰어 API 
		// 1. fileType(필수) - URL
		multipartParams.add("fileType", "URL");
		
		// 2. convertType - 0 : HTML, 1 : Image, 2 : PDF, -1 : DEFAULT
		multipartParams.add("convertType", "-1");
		
		// 3. filePath(필수)		
		String path = dboxUrl + "/api/data/" + dataId + "/content/?api_key=" + token;
		multipartParams.add("filePath", path);
		
		// 4. fid(필수) - uuid 지정
		multipartParams.add("fid", dataId);
		
		// 5. sync - true : 변환 후 뷰어로 자동 전환 / false : json반환
		multipartParams.add("sync", "true");
		
		// 6. force - true : 기존 변환 결과를 사용하지 않고 재변환
		multipartParams.add("force", "true");
		
		// 기타정보
		multipartParams.add("accessCookieData", "");
		multipartParams.add("convertLocale", "");
		multipartParams.add("urlEncoding", "UTF-8");
		multipartParams.add("refererUrl", "");
		multipartParams.add("downloadUrl", "");
		multipartParams.add("title", "");
		multipartParams.add("single", "false");
		multipartParams.add("fitSheet", "false");
		multipartParams.add("openPassword", "");
		multipartParams.add("permissionPassword", "");
		
		String url = "http://125.60.95.32:8080/SynapDocViewServer/job";

		RestTemplateUtils restTemplate = new RestTemplateUtils();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
  		
		ResponseEntity<HashMap<String, Object>> returnValue = restTemplate.post(url, headers, multipartParams, new ParameterizedTypeReference<HashMap<String,Object>>() {});
		
		String result = returnValue.getHeaders().getLocation().toString();
		
		return result;
	}
}
