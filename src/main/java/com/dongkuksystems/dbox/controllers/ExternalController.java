package com.dongkuksystems.dbox.controllers;

import static com.dongkuksystems.dbox.models.api.response.ApiResult.OK;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.documentum.fc.client.IDfDocument;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.common.DfTime;
import com.documentum.fc.common.IDfTime;
import com.dongkuksystems.dbox.constants.ComCodeType;
import com.dongkuksystems.dbox.constants.Commons;
import com.dongkuksystems.dbox.models.api.response.ApiResult;
import com.dongkuksystems.dbox.models.common.Documentum;
import com.dongkuksystems.dbox.models.common.UserSession;
import com.dongkuksystems.dbox.models.dto.etc.CustomInputStreamResource;
import com.dongkuksystems.dbox.models.dto.type.agree.DboxAttaDocDto;
import com.dongkuksystems.dbox.models.dto.type.agree.DboxUserApiDto;
import com.dongkuksystems.dbox.models.dto.type.agree.RegistAgreeDto;
import com.dongkuksystems.dbox.models.dto.type.agree.WfDocDto;
import com.dongkuksystems.dbox.models.dto.type.upload.AttachedFile;
import com.dongkuksystems.dbox.models.dto.type.user.UserDSearchAuthDto;
import com.dongkuksystems.dbox.models.dto.type.user.UserPwUpdateDto;
import com.dongkuksystems.dbox.models.table.etc.VDept;
import com.dongkuksystems.dbox.models.table.etc.VUser;
import com.dongkuksystems.dbox.models.type.doc.WfDoclist;
import com.dongkuksystems.dbox.securities.JwtAuthentication;
import com.dongkuksystems.dbox.services.auth.AuthService;
import com.dongkuksystems.dbox.services.cache.CacheService;
import com.dongkuksystems.dbox.services.code.CodeService;
import com.dongkuksystems.dbox.services.data.DataService;
import com.dongkuksystems.dbox.services.dept.GwDeptService;
import com.dongkuksystems.dbox.services.doc.DocService;
import com.dongkuksystems.dbox.services.external.ExternalService;
import com.dongkuksystems.dbox.services.user.UserService;
import com.dongkuksystems.dbox.utils.RestTemplateUtils;
import com.dongkuksystems.dbox.utils.dctm.DCTMUtils;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@RestController
@Api(tags = "외부 인터페이스 컨트롤러")
public class ExternalController extends AbstractCommonController {
/*	
	@Value("${kupload.base-path}")
	private String basePath;
	@Value("${kupload.temp-path}")
	private String tempPath;
*/
	@Value("${appr.dir}")
	private String tempPath;

  @Value("${niris.url}")
  private String nirisUrl;  //메일발송시 참조하는 URL(application*.yml파일 참조)

  @Value("${jwt.token.header}")
  private String tokenHeader;
	
  private final ExternalService externalService;
  private final DocService  docService;
  private final DataService dataService;
  private final CacheService cacheService;
  private final AuthService authService;
  private final CodeService codeService;
  private final GwDeptService  gwDeptService;
  private final UserService  userService;
  
  private Documentum documentum;
 
  public ExternalController(ExternalService externalService, DocService docService, DataService dataService, UserService userService
		                 ,CacheService cacheService, AuthService authService, CodeService codeService, GwDeptService gwDeptService, Documentum documentum) {
    this.externalService = externalService;
    this.docService = docService;
    this.dataService = dataService;
    this.cacheService = cacheService;
    this.authService = authService;
    this.codeService = codeService;
    this.gwDeptService = gwDeptService;
    this.documentum = documentum;
    this.userService = userService;
  }

  @GetMapping(value = "/external/doc/{docId}/link")
  @ApiOperation(value = "link downLoad api")
  public ResponseEntity<InputStreamResource> downloadLinkfile(@AuthenticationPrincipal JwtAuthentication authentication,
      @ApiParam(value = "doc obj Id", example = "213211") @PathVariable String docId) throws Exception {
    UserSession userSession = (UserSession) getRedisRepository().getObject(authentication.loginId,
        UserSession.class);
    CustomInputStreamResource rst = externalService.createExternalLinkFile(docId);
    HttpHeaders headers = new HttpHeaders();
    headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
    headers.add("Content-Disposition", "attachment; filename=" + rst.getFilename());
    headers.add("Pragma", "no-cache");
    headers.add("Expires", "0");
    return ResponseEntity.ok().headers(headers).contentLength(rst.contentLength())
        .contentType(MediaType.parseMediaType("application/octet-stream"))
        .body(new InputStreamResource(rst.getInputStream()));
  }

  @GetMapping(value = "/external/doc/{docId}/content")
  @ApiOperation(value = "link downLoad api")
  public ApiResult<String> downloadfile(@AuthenticationPrincipal JwtAuthentication authentication,
      HttpServletRequest request, @ApiParam(value = "doc obj Id", example = "213211") @PathVariable String docId)
      throws Exception {
    UserSession userSession = (UserSession) getRedisRepository().getObject(authentication.loginId,
        UserSession.class);
    String userAgent = request.getHeader("User-Agent").toUpperCase();
    if (userAgent.indexOf("MOBILE") > -1) {
      if (userAgent.indexOf("PHONE") == -1)
        userAgent = "PHONE";
      else
        userAgent = "TABLET";
    }
    return OK(userAgent + "__" + docId);
  }

  @GetMapping(value = "/external/cache/session-init")
  @ApiOperation(value = "init session caches")
  public ApiResult<Long> initSessionCache() throws Exception {
    Long dels = 0L;
    Set<String> keys = getRedisRepository().getKeys(Commons.SESSION_PREFIX + "*");
    if (keys != null) {
      if (!keys.isEmpty()) {
        dels = getRedisRepository().deleteByKeyCollection(keys);
      }
    }
    return OK(dels);
  }
  
  @GetMapping(value = "/external/cache/init")
  @ApiOperation(value = "init caches")
  public ApiResult<Boolean> initCache() throws Exception {
    cacheService.initSelectDeptPath();
    cacheService.initSelectDepts();
    cacheService.initSelectDeptTree();
    cacheService.initSelectMangeIdTree();
    cacheService.initSelectDeptChildren();
    cacheService.initSelectDeptByOrgId();
    cacheService.initSelectDeptByOrgId();
    cacheService.initSelectDeptCodeByCabinetcode();
    cacheService.initSelectComCodeByCabinetCode();
    cacheService.initSelectOrgIdByCabinetcode();
    cacheService.initSelectDeptChildrenByOrgId();
    cacheService.initSelectTemplates();
    cacheService.initSelectDmFormats();
    cacheService.initSelectUserListOfPart();
		codeService.initCodesByUCodeType();
		codeService.initSecLevelMap();
		codeService.initFolStatusMap();
		codeService.initDocStatusMap();
		codeService.initNotiItemMap();
		codeService.initComCodeMap();
		codeService.initDocHandleListMap();
		codeService.initConfigDocHandleLimitMap();
		codeService.initCommonCabinetDeptMap();
		codeService.initSpecialUserIdSet();
		codeService.initGetComCodeDetail();
		codeService.initGetDrmFormatCodeMap();
		codeService.initDeniedFormatMap();
		codeService.initClosedFormatMap();
		codeService.initAgentInstallerId();
		codeService.initConjfigMidSaveDeptMap();
		codeService.initConfigTransWfMap();
		codeService.initConfigUsbBasePolicyMap();
		codeService.initConfigVerDelPeriodMap();
		codeService.initConfigDeletePeriodMap();
		
    Set<String> keys = getRedisRepository().getKeys(Commons.SESSION_PREFIX + "*");
    if (keys != null) {
      if (!keys.isEmpty()) {
        getRedisRepository().deleteByKeyCollection(keys);
      }
    }
    return OK(true);
  }
  
  @PostMapping("/external/register/agreem")
  @ApiOperation(value = "동의서전송")
  public ApiResult<String> registerAgree(@AuthenticationPrincipal JwtAuthentication authentication,
			@ApiParam(value = "동의서정보") @RequestBody RegistAgreeDto agreeInfo
		) throws Exception {
		UserSession userSession = (UserSession) getRedisRepository().getObject(authentication.loginId,
				UserSession.class);
		
	    String rst = externalService.registerAgree(userSession, agreeInfo);
	    return OK(rst);
	    
//      JSONObject jsonObject =null;
//		if(rst !="") {
//	    RestTemplateUtils restUtils = new RestTemplateUtils();
//			HttpHeaders headers = new HttpHeaders();
//			headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
//			headers.add("Content-Type", "application/json;utf-8");
//			headers.add("Pragma", "no-cache");
//			headers.add("Expires", "0");
//		    
//			String lsUrl = "http://niris.dongkuk.com/api/insertDboxAgreeApi.json";
//			String lsParameters = "?apiKey=060kkve5kbbiflxia5kgqte47bofag2o&systemId=DBOX_AGREE&typeNo=";
//			lsParameters += agreeInfo.getUAgreeType()=="T"?"1":"2"; //AgreeType: T:부서장동의(자동승인), U:사용자동의(프리패스)') ,  N-Iris쪽 ("1: 자동승인 / 2: 프리패스)
//
//			Map<String, String> body = new HashMap<String, String>();
//
//			body.put("userId", userSession.getDUserId());
//			body.put("agreeYn", agreeInfo.getUAgreeYn());
//      body.put("comCd",  userSession.getUser().getComOrgId());
//			
//			ResponseEntity<String> response =restUtils.post(lsUrl+lsParameters, headers, body, String.class);
//
//			//System.out.println(response.getBody());
//			JSONParser jsonParser = new JSONParser(); 
//			jsonObject = (JSONObject) jsonParser.parse(response.getBody().toString()); 
//		}
		
  }
	

  @PostMapping("/external/register/dboxUserInfo")
  @ApiOperation(value = "D'box 사용여부 / 문서관리자")
  public ApiResult<String> registerDboxUserInfo(@AuthenticationPrincipal JwtAuthentication authentication,
			@ApiParam(value = "동의서정보") @RequestBody DboxUserApiDto userApiDto
		) throws Exception {
		UserSession userSession = (UserSession) getRedisRepository().getObject(authentication.loginId,
				UserSession.class);
		
	    RestTemplateUtils restUtils = new RestTemplateUtils();
		HttpHeaders headers = new HttpHeaders();
		headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
		headers.add("Content-Type", "application/json;utf-8");
		headers.add("Pragma", "no-cache");
		headers.add("Expires", "0");
	    
		String lsUrl = nirisUrl+"/api/insertDboxUserApi.json";
		String lsParameters = "?apiKey=iud1oz1bh5uk149zebxvpl4bv0c0nyrh&systemId=DBOX_SPECIAL_USER";  

		Map<String, String> body = new HashMap<String, String>();

		body.put("userId", userApiDto.getUUserId());
	    body.put("useYn" , userApiDto.getUUseYn());
		
		ResponseEntity<String> response =restUtils.post(lsUrl+lsParameters, headers, body, String.class);

		JSONParser jsonParser = new JSONParser(); 
		JSONObject jsonObject = (JSONObject) jsonParser.parse(response.getBody().toString()); 
			
		return OK(JSONObject.toJSONString(jsonObject));
  }
  
  
   @PostMapping(value = "/approvalFile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @ApiOperation(value = "결재 첨부문서 등록", notes = "파일 단건 처리", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ApiResult<String> postDataContent(@AuthenticationPrincipal JwtAuthentication authentication,
		    //@ApiParam(value = "결재 정보") @RequestBody WfDocDto doc,
			@ApiParam(value = "결재 정보") @ModelAttribute WfDocDto doc,
			@ApiParam(value = "결재 첨부 파일") @RequestPart(required = true) MultipartFile file,  //N-Iris에서 첨부파일 건별로 계속 호출됨
			HttpServletRequest request) throws Exception {
		UserSession userSession = (UserSession) getRedisRepository().getObject(authentication.loginId,
				UserSession.class);
		
		boolean isMobile = request.getHeader("User-Agent").toUpperCase().indexOf("MOBILE") > -1;	// TODO flutter에서도 모바일 여부 확인 가능한지
		
		HttpHeaders headers = new HttpHeaders();
		JSONObject body = new JSONObject();
		
		String  orgId   = userSession.getUser().getOrgId();
		String  userId   = userSession.getUser().getUserId();

		doc.setURequestIp(getClientIp(request));
		
	    String jobUserType="P";
	    doc.setUJobUserType(jobUserType); //작업자구분(P:개인, D:부서관리자, C:사별관리자, G:그룹관리자, S:시스템)
		
	    IDfTime tt      = new DfTime();
	    String  s_Today = new DfTime().toString();
        
	    IDfSession  idfSession = null;
	    IDfDocument idfDoc     = null;
	    String      dboxFileId = null;
	    
	    //WfDocDto doc = new WfDocDto();

		String  lsStatus="200";
		String  lsStr    ="";
		String  rst=""; //
		ResponseEntity<InputStreamResource> nullRst=null;
	    try {
			//부서함> 전자결재> 년도
	    	
	    	VDept myDept = gwDeptService.selectDeptByOrgId(userSession.getUser().getOrgId());
	    	
	    	//doc.setUCabinetCode(userSession.getUser().getDeptCabinetcode());
	    	doc.setUCabinetCode( myDept.getUCabinetCode());
	    	
	    	doc.setUComOrgCabinetCd("g_"+(gwDeptService.selectDeptByOrgId(myDept.getComOrgId()).getUCabinetCode()));
	    	doc.setUGroupOrgCabinetCd("g_"+ (gwDeptService.selectDeptByOrgId("DKG").getUCabinetCode()));

	    	//doc.setUComOrgCabinetCd("g_"+myDept.getComOrgId().toLowerCase());
	    	//doc.setUGroupOrgCabinetCd("g_dkg");
	    	
	    	//System.out.println( "MOLA: cabinetCode="+myDept.getUCabinetCode());
	    	
	          String approvalLevel = doc.getApprovalLevel();
	          if(null==approvalLevel || approvalLevel.trim().equals("")) {
	        	  if(doc.getApprovalFormName().equals("안전작업허가서")) {
	        		  doc.setApprovalLevel("C");
	        	  }else if(doc.getApprovalFormName().equals("기안(집행품의") || doc.getApprovalFormName().equals("품의서(기본/집행/완료/보고")) {
	        		  doc.setApprovalLevel("T");
	        	  }
	        	  
	        	  
	          }
/*	    	
	    	if(null==approvalLevel || approvalLevel.trim().equals("")) {
					headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
					headers.add("Content-Disposition", "attachment; filename=" + rst);
					headers.add("Pragma", "no-cache");
					headers.add("Expires", "0");
					headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
					lsStatus="500";
	
					body.put("status", lsStatus);
					body.put("message", "보안등급이 지정되지 않았습니다");
					body.put("dboxId",  "");
					body.put("dboxLevel",  "");
					body.put("dboxLink",  "");
					body.put("dboxfilename",  "");
					
					return OK(JSONObject.toJSONString(body));
			}
--niris담당자 요청으로 보류
*/	    	 
	    	String delYn         = doc.getFileDeleteYn();
	    	delYn = DCTMUtils.checkNullStringByObj(delYn)==""?"N":delYn;
	    	doc.setFileDeleteYn(delYn);
	    	

	        //api를 호출한 사용자와 결재자가 다른 경우, 
	        if(!doc.getApprovalWriter().equals(userId)) {
	        	System.out.println(userId+" 가 "+ doc.getApprovalWriter() +" 결재건 호출");
	        }
	    	Map<String,String> approvalMap = doc.getAddGrantedMap();
	    	

	    	JSONParser parser = new JSONParser();
	    	
	        Map< String, String[] > drftMap = new HashMap<>();
	        Map< String, String[] > ccLnMap = new HashMap<>();
	        Map< String, String[] > recvMap = new HashMap<>();

	    	String lsDrftLine=doc.getApprovalDraftLine();
	    	String lsCcLnLine=doc.getApprovalCcLine();
	    	String lsRecvLine=doc.getApprovalReceiveLine();
	    	
	    	JSONObject drftObj = lsDrftLine==null?null:(JSONObject) parser.parse( lsDrftLine );
	    	JSONObject cclnObj = lsCcLnLine==null?null:(JSONObject) parser.parse( lsCcLnLine );
	    	JSONObject recvObj = lsRecvLine==null?null:(JSONObject) parser.parse( lsRecvLine );
	    	if(drftObj !=null) drftMap.putAll(drftObj);
	    	if(cclnObj !=null) ccLnMap.putAll(cclnObj);
	    	if(recvObj !=null) recvMap.putAll(recvObj);
	        
	    	ComCodeType[] s_ComOrgArr = null;//사용하지 않음. cabinet_code로 환원 : ComCodeType.values();
	    	
	    	int li_DraftValidCnt=0;
            //P:사용자id가 아니라 조직:O, 회사: C 인 경우는 edms_dept에서  u_cabinet_code 찾아서 권한부여할 때 사용한다
	    	for (Map.Entry<String, String[]> entry : drftMap.entrySet()) {
	              String keyValue="";
	    		  Object oArrayObj = entry.getValue();

	    		  JSONArray jsonArr = (JSONArray)oArrayObj;
	    		  List<String> oArray = new ArrayList<String>();
	    		  
    		      for(int i=0; i < jsonArr.size(); i++) {
    	    		  keyValue = jsonArr.get(i).toString();//entry.getValue()[i];

    	    		  if(!entry.getKey().equals("P")) { //부서나 회사에 대한 권한 부여일 때
    	    			  try {
    	    				  keyValue=gwDeptService.selectDeptByOrgId(keyValue).getUCabinetCode();
	    				      if(null!=keyValue && !keyValue.equals("") && !keyValue.equals(" "))  {
 	    				         keyValue="g_"+keyValue;
 	    				      }

    	    			  } catch (Exception e) {
    	    				  keyValue="";
    	    			  }
	    	    		  if(null==keyValue || keyValue.equals("")|| keyValue.equals(" ")) {
	    	    			  String vOrgId=jsonArr.get(i).toString();
	    	    			  List<String> pUsrList = gwDeptService.selectUserListOfPart(vOrgId); //파트인 경우
	    	    			  for(int j=0; j < pUsrList.size(); j++) {
	    	    				  if(!pUsrList.get(j).equals(""))
	    	    				      oArray.add(pUsrList.get(j).toString());
	    	    			  }
	    	    		  }else {
	        	    		  oArray.add( keyValue) ;
	    	    		  }
    	    		  }else {
    	    			  List<String> usrList = new ArrayList<>();
    	    			  usrList.add(keyValue);
    	    			  List<VUser> gwUser=userService.selectUserListByUserIds(usrList);
    	    			  if(gwUser.size() > 0 ) {
    	    				  if(!gwUser.get(0).equals("") )  oArray.add( keyValue) ;
    	    			  }else {
    	    				  System.out.println("#MOLA:" +keyValue +" 사용자는 유효하지 않은 사용자ID 입니다");
    	    			  }
    	    		  }
    		      }
    		      if(oArray.size() > 0)  li_DraftValidCnt++;
    		      String[] valueArray = oArray.toArray(new String[0]);
    		      entry.setValue(valueArray);
    		      
		    }
/*	    	
	    	if((doc.getApprovalState().equals("F")|| doc.getApprovalState().equals("W")|| doc.getApprovalState().equals("A")) && li_DraftValidCnt < 1) {
				body.put("status", "500");
				body.put("message", "결재라인이 전달되지 않았습니다");
				body.put("dboxId",  "");
				body.put("dboxLevel",  "");
				body.put("dboxLink",  ""); 
				body.put("dboxfilename",  "");

				return OK(JSONObject.toJSONString(body));	    		
	    	}
*/	    	
	    	doc.setApprovalDraftLineM(drftMap); 

	    	for (Map.Entry<String, String[]> entry : ccLnMap.entrySet()) {
	              String keyValue="";
	    		  Object oArrayObj = entry.getValue();
	    		  JSONArray jsonArr = (JSONArray)oArrayObj;
	    		  List<String> oArray = new ArrayList<String>();
	    		  
    		      for(int i=0; i < jsonArr.size(); i++) {
    	    		  keyValue = jsonArr.get(i).toString();//entry.getValue()[i];
    	    		  if(!entry.getKey().equals("P")) { //부서나 회사에 대한 권한 부여일 때
    	    			  try {
    	    				  keyValue=gwDeptService.selectDeptByOrgId(keyValue).getUCabinetCode();
	    				      if(null!=keyValue && !keyValue.equals("") && !keyValue.equals(" "))  {
 	    				         keyValue="g_"+keyValue;
 	    				      }

    	    			  } catch (Exception e) {
    	    				  keyValue="";
    	    			  }
	    	    		  if(null==keyValue || keyValue.equals("")|| keyValue.equals(" ")) {
	    	    			  String vOrgId=jsonArr.get(i).toString();
	    	    			  List<String> pUsrList = gwDeptService.selectUserListOfPart(vOrgId); //파트인 경우
	    	    			  for(int j=0; j < pUsrList.size(); j++) {
	    	    				  if(!pUsrList.get(j).equals(""))
	    	    				      oArray.add(pUsrList.get(j).toString());
	    	    			  }
	    	    		  }else {
	        	    		  oArray.add( keyValue) ;
	    	    		  }
    	    		  }else {
    	    			  List<String> usrList = new ArrayList<>();
    	    			  usrList.add(keyValue);
    	    			  List<VUser> gwUser=userService.selectUserListByUserIds(usrList);
    	    			  if(gwUser.size() > 0 ) {
    	    				  if(!gwUser.get(0).equals("") )  oArray.add( keyValue) ;
    	    			  }else {
    	    				  System.out.println("#MOLA:" +keyValue +" 사용자는 유효하지 않은 사용자ID 입니다");
    	    			  }
    	    		  }
    		      }
    		      String[] valueArray = oArray.toArray(new String[0]);
    		      entry.setValue(valueArray);
		    }
	    	doc.setApprovalCcLineM(ccLnMap); 

	    	for (Map.Entry<String, String[]> entry : recvMap.entrySet()) {
	              String keyValue="";
	    		  Object oArrayObj = entry.getValue();
	    		  JSONArray jsonArr = (JSONArray)oArrayObj;
	    		  List<String> oArray = new ArrayList<String>();
	    		  
    		      for(int i=0; i < jsonArr.size(); i++) {
    	    		  keyValue = jsonArr.get(i).toString();//entry.getValue()[i];
    	    		  if(!entry.getKey().equals("P")) { //부서나 회사에 대한 권한 부여일 때
    	    			  try {
    	    				  keyValue=gwDeptService.selectDeptByOrgId(keyValue).getUCabinetCode();
	    				      if(null!=keyValue && !keyValue.equals("") && !keyValue.equals(" "))  {
 	    				         keyValue="g_"+keyValue;
 	    				      }

    	    			  } catch (Exception e) {
    	    				  keyValue="";
    	    			  }
	    	    		  if(null==keyValue || keyValue.equals("")|| keyValue.equals(" ")) {
	    	    			  String vOrgId=jsonArr.get(i).toString();
	    	    			  List<String> pUsrList = gwDeptService.selectUserListOfPart(vOrgId); //파트인 경우
	    	    			  for(int j=0; j < pUsrList.size(); j++) {
	    	    				  if(!pUsrList.get(j).equals(""))
	    	    				      oArray.add(pUsrList.get(j).toString());
	    	    			  }
	    	    		  }else {
	        	    		  oArray.add( keyValue) ;
	    	    		  }
    	    		  }else {
    	    			  List<String> usrList = new ArrayList<>();
    	    			  usrList.add(keyValue);
    	    			  List<VUser> gwUser=userService.selectUserListByUserIds(usrList);
    	    			  if(gwUser.size() > 0 ) {
    	    				  if(!gwUser.get(0).equals("") )  oArray.add( keyValue) ;
    	    			  }else {
    	    				  System.out.println("#MOLA:" +keyValue +" 사용자는 유효하지 않은 사용자ID 입니다");
    	    			  }
    	    		  }
    		      }
    		      String[] valueArray = oArray.toArray(new String[0]);
    		      entry.setValue(valueArray);
		    }
	    	doc.setApprovalReceiveLineM(recvMap); 

		    String dBoxId=DCTMUtils.checkNullStringByObj(doc.getDboxId());
		    String approvalState = doc.getApprovalState();
		    String attachType    = doc.getAttachType();
		    
	        String folderName= orgId+"/전자결재/"+  s_Today.substring(0,4)+"/temp";
	        if(approvalState.equals("F")) { //최종승인일 때, 
	        	folderName= orgId+"/전자결재/"+  s_Today.substring(0,4);
	        }
	    	
		    System.out.println("@MOLA: 첨부유형:'"+attachType+"', 요청 마스트:"+ doc.toString());
		    if(file.isEmpty()) {
		    	System.out.println("@MOLA: 첨부파일은 PC파일이 아닌듯, 빈 파일임 (결재상태:"+doc.getApprovalState()+")");
		    }
		    
		    //System.out.println(attachType+"  요청 정보: file Org Name:"+ file.getOriginalFilename()+" fileName:"+  file.getName() +" : fileSize" + file.getSize());
		    if(null==attachType || attachType.trim().equals("")) {
		    	if(!approvalState.equals("W")) { //승인요청단계가 아닐때
		    		//if(file.isEmpty())
		    		//    attachType="dbox";
		    		attachType="";
		    		
		    	}else {
				    if(null==attachType || attachType.trim().equals("")) {
							headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
							headers.add("Content-Disposition", "attachment; filename=" + rst);
							headers.add("Pragma", "no-cache");
							headers.add("Expires", "0");
							headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
							lsStatus="500";
		
							body.put("status", lsStatus);
							body.put("message", "첨부유형 정보가 누락되었습니다");
							body.put("dboxId",  "");
							body.put("dboxLevel",  "");
							body.put("dboxLink",  "");
							body.put("dboxfilename",  "");
							
							return OK(JSONObject.toJSONString(body));
							
							//return OK(body.toString());
				    }
		    	}
		    }

	    	if( attachType.equals("pc") && !approvalState.equals("R")  && !approvalState.equals("X")) { //PC에서 첨부한 파일이며, 회수나 반려가 아닐 때, 
		    	//doc.setAddGrantedMap(??????); //결재자리스트
                if(null==file) {
                	headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
					headers.add("Content-Disposition", "attachment; filename=" + rst);
					headers.add("Pragma", "no-cache");
					headers.add("Expires", "0");
					headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));

					lsStatus="500";
					body.put("status", lsStatus);
					body.put("message", "첨부유형이 전달되지 않았습니다(file is Null)");
					body.put("dboxId",  "");
					body.put("dboxLevel",  "");
					body.put("dboxLink",  ""); 
					body.put("dboxfilename",  "");
					
					return OK(JSONObject.toJSONString(body));
					
					//return OK(body.toString());
                }
                if(approvalState.equals("F")) {//최종결재이면 
                	if(null == dBoxId || dBoxId.equals("")) {
                		 //문서저장경로 : 각 부서의 [ 부서함> 전자결재> 년도(자동생성)> temp ]
                    	
                        String path = request.getSession().getServletContext().getRealPath("");

    				    dboxFileId = externalService.createWfDoc(userSession, null, folderName, authentication.loginId, doc, 
    						      AttachedFile.toAttachedFile(file, path, Commons.DEFAULT_EXTENSION), s_Today.substring(0,4));

    				    if(null == dboxFileId || dboxFileId.equals("")) {
    						
    						headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
    						headers.add("Content-Disposition", "attachment; filename=" + rst);
    						headers.add("Pragma", "no-cache");
    						headers.add("Expires", "0");
    						headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
    						lsStatus="500";
    						body.put("status", lsStatus);
    						body.put("message", "dbox파일 생성중 이상 발생");
    						body.put("dboxId",  "");
    						body.put("dboxLevel",  "");
    						body.put("dboxLink",  "");
    						body.put("dboxfilename",  "");
    						
    						return OK(JSONObject.toJSONString(body));
    					}else {
    	                    System.out.println("dBox 파일 생성 1 =" + dboxFileId);
    	                    rst = externalService.createExternalLinkFileByFind(userSession, dboxFileId);
    	                    String[] linkInfo = rst.split("@JJ@");
    	                    
    	                    String dboxfileName ="";
    	                    if(!linkInfo[1].equals("")) {
    	                        rst = linkInfo[0];
    	                        dboxfileName = linkInfo[1];
    	                    }
    						headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
    						headers.add("Content-Disposition", "attachment; filename=" + rst);
    						headers.add("Pragma", "no-cache");
    						headers.add("Expires", "0");
    						headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
    						lsStatus="200";
    						body.put("status", lsStatus);
    						body.put("message", "");
    						body.put("dboxId",  dboxFileId);
    						body.put("dboxLevel",  approvalLevel);
    						body.put("dboxLink",  rst);
    						body.put("dboxfilename",  dboxfileName);
    						
    						
    						return OK(JSONObject.toJSONString(body));
    					}
                	}else {
	                	dboxFileId = dBoxId ;
		    	    	doc.setDboxId(dboxFileId);
		    	    	
		    	    	String path = request.getSession().getServletContext().getRealPath("");
		    	    	
		    	    	try {
		    	    		lsStr = externalService.updateWfDoc(userSession, null, folderName, authentication.loginId, doc, 
							      AttachedFile.toAttachedFile(file, path, Commons.DEFAULT_EXTENSION), s_Today.substring(0,4));
	
					        //보안등급 부여 룰 확인/적용 필요
		    	    		if(!lsStr.equals("")) {
			    	    		lsStatus="500";
		    	    		}
		    	    	} catch (Exception e) {
		    	    		lsStatus="500";
		    	    		//lsStr   =e.toString();
		    	    		lsStr   ="예외상황 발생";
		    	    		System.out.println("#MOLA:"+ e.getMessage());
		    	    		e.printStackTrace();
		    	    	}
			            //AttachedFile.toAttachedFile(file, MessageFormat.format(Commons.TMP_STORAGE_PATH, authentication.loginId), Commons.DEFAULT_EXTENSION));
					    //부서함/전자결재/년도/ 폴더로 자료 이동
	                	/*
	                        1. edms_folder에서 /년도 인 폴더를 u_fol_type='DWV', 캐비닛코드, u_fol_name='년도' 인 폴더의 폴더id를 찾는다.
	                        2. edms_doc, edms_wf_doclist의 u_fol_id (, u_folder_path) 를 변경한다.  u_sec_level이 변경될 수 있는지 확인, u_doc_staatus='C', u_closed_date=현재일시 로 변경, u_closer=, u_update_date, u_editor_names,
	                         
	                    */
                	}
                	
                }else {                         //최종결재가 아니면 임시폴더에 생성한다.
	    		    //문서저장경로 : 각 부서의 [ 부서함> 전자결재> 년도(자동생성)> temp ]
                    String path = request.getSession().getServletContext().getRealPath("");

				    dboxFileId = externalService.createWfDoc(userSession, null, folderName, authentication.loginId, doc, 
						      AttachedFile.toAttachedFile(file, path, Commons.DEFAULT_EXTENSION), s_Today.substring(0,4));

				    if(null == dboxFileId || dboxFileId.equals("")) {
						
						headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
						headers.add("Content-Disposition", "attachment; filename=" + rst);
						headers.add("Pragma", "no-cache");
						headers.add("Expires", "0");
						headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
						lsStatus="500";
						body.put("status", lsStatus);
						body.put("message", "dbox파일 생성중 이상 발생");
						body.put("dboxId",  "");
						body.put("dboxLevel",  "");
						body.put("dboxLink",  "");
						body.put("dboxfilename",  "");
						
						System.out.println("#MOLA_1:Return:"+ JSONObject.toJSONString(body) );
						
						return OK(JSONObject.toJSONString(body));
					}else {
	                    System.out.println("dBox 파일 생성 2 =" + dboxFileId);
	                    rst = externalService.createExternalLinkFileByFind(userSession, dboxFileId);
	                    String[] linkInfo = rst.split("@JJ@");
	                    
	                    String dboxfileName ="";
	                    if(!linkInfo[1].equals("")) {
	                        rst = linkInfo[0];
	                        dboxfileName = linkInfo[1];
	                    }

						headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
						headers.add("Content-Disposition", "attachment; filename=" + rst);
						headers.add("Pragma", "no-cache");
						headers.add("Expires", "0");
						headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
						lsStatus="200";
						body.put("status", lsStatus);
						body.put("message", "");
						body.put("dboxId",  dboxFileId);
						body.put("dboxLevel",  approvalLevel);
						body.put("dboxLink",  rst);
						body.put("dboxfilename",  dboxfileName);
						
						System.out.println("#MOLA_2:Return:"+ JSONObject.toJSONString(body) );
						return OK(JSONObject.toJSONString(body));
					}
					
                }
    	        //rst = externalService.createExternalLinkFile(dboxFileId);
                //rst = externalService.createExternalLinkFileByFind(userSession, dboxFileId);

	    	}else { //Dbox에서 첨부한 파일일 때, 
	    		dboxFileId = dBoxId ;

	    		if(null != delYn && delYn.equals("Y")) {  //첨부되었던 파일 삭제
	    			/** To Do   dBoxId에 해당하는 첨부파일 삭제처리  ( DataController 참조) **/
                    if(!dBoxId.equals("") &&  null !=dBoxId) {
                    	lsStr = externalService.removeWfDoc(userSession, null, dboxFileId, "F",  attachType, doc.getApprovalId() , doc.getApprovalWriter());//첨부파일 한 개의 삭제  
	    	    		if(!lsStr.equals("")) {
		    	    		lsStatus="500";
	    	    		}
                    }
/* 사용중지. 컨텐츠(밴드게시물, 결재) 가 삭제되어도, 링크나 파일 삭제를 하는 처리는 없음                    
                    else {
                    	dboxFileId = externalService.removeWfDoc(userSession, doc.getApprovalId(), "C" ,attachType);//전자결재문서자체를 삭제
                    }
*/
	    			//삭제는 결재번호로 find해서 삭제처리하는 것으로 ...(오류 예방)
	    		/** 회수나 반려시 파일은 어떻게 할 것인지 확인할 것.
	    		 * 
	    		 * 최종결재 승인시 문서 상태값 closed 처리 & 문서 저장경로 이동
                                 최종결재 반려시 문서 상태값 closed 처리
                                  DataController의 "/data/{dataId}/close" 참조
                                 결재회수시 문서 삭제(전자결재 closed문서가 아닌 경우)
	    		 */
	    	    }else if(null != delYn &&  !delYn.equals("Y")) { //새로 첨부된 D'box 파일일 때 
	    	    	doc.setDboxId(dboxFileId);
	    	    	String path = request.getSession().getServletContext().getRealPath("");
	    	    	try {
	    	    		lsStr = externalService.updateWfDoc(userSession, null, folderName, authentication.loginId, doc, 
							      AttachedFile.toAttachedFile(file, path, Commons.DEFAULT_EXTENSION), s_Today.substring(0,4));
				        //보안등급 부여 룰 확인/적용 필요
	    	    		if(!lsStr.equals("")) {
		    	    		lsStatus="500";
	    	    		}
	    	    	} catch (Exception e) {
	    	    		lsStatus="500";
	    	    		lsStr   ="예외상황 발생";
	    	    		System.out.println("#MOLA:"+ e.getMessage());
	    	    		e.printStackTrace();
	    	    	}
		    	}
	    	}

	        //doc.setStatus("200");
	        //doc.setMessage("");
	    	//doc.setDboxLevel(doc.getApprovalLevel());
	    	//doc.setDboxId(dboxFileId);
	        //doc.setDboxLink(rst);
	        
	        
			headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
			headers.add("Content-Disposition", "attachment; filename=" + rst);
			headers.add("Pragma", "no-cache");
			headers.add("Expires", "0");
			headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));


			body.put("status",  lsStatus);
			body.put("message", lsStr);
			body.put("dboxId",  dboxFileId);
			body.put("dboxfilename",  "");
			body.put("dboxLevel",  approvalLevel);
			body.put("dboxLink",  rst);
			
			return OK(JSONObject.toJSONString(body));
			
			
	    } catch (Exception e) {
	    	e.printStackTrace();
	    	doc.setStatus("500"); //INTERNAL_SERVER_ERROR 서버에러
	    	doc.setMessage(e.getMessage());
	    	
			headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
			headers.add("Content-Disposition", "attachment; filename=");
			headers.add("Pragma", "no-cache");
			headers.add("Expires", "0");
			headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));

			body.put("status", "500");
			
			System.out.println("#MOLA:"+ e.getMessage());
			
			body.put("message", "예외처리 발생");
			body.put("dboxId",  "");
			body.put("dboxLevel",  "");
			body.put("dboxfilename",  "");
			body.put("dboxLink",  rst);
			
            //System.out.println("***************** :" + JSONObject.toJSONString(body));			
			return OK(JSONObject.toJSONString(body));
	
			
			//return OK(body.toString());
	    	
	    }
/*	    
	    status	text	ex) 200	응답 코드
	    message	text	ex) 성공	응답 메시지
	    dboxId	text	ex) 763819	D'box 문서 ID
	    dboxLevel	text	ex) 40	D'box 문서 보안등급
	    dboxLink	text	ex) http://dbox.dongkuk.com/api/763819….	D'box 문서 링크파일
*/	    
	    //doc 테이블에 결재정보 저장처리하면 됨.
	    
	    //return OK(body.toString());
	    
	    //return OK(doc);
		/*
			status	text	ex) 200	응답 코드
			message	text	ex) 성공	응답 메시지
			dboxId	text	ex) 763819	D'box 문서 ID
			dboxLevel	text	ex) 40	D'box 문서 보안등급
			dboxLink	text	ex) http://dbox.dongkuk.com/api/763819….	D'box 문서 링크파일
		 */
  }	
  
  @PostMapping(value = "/bandAttachDboxFile" )
  @ApiOperation(value = "밴드 문서 권한추가", notes = "Dbox 파일 단건 처리")
  public ApiResult<String> bandAttatchDboxFile(@AuthenticationPrincipal JwtAuthentication authentication,
			@ApiParam(value = "첨부파일권한부여내역") @ModelAttribute DboxAttaDocDto attDoc,
			//@RequestBody DboxAttaDocDto attDoc,
			HttpServletRequest request) throws Exception {
		UserSession userSession = (UserSession) getRedisRepository().getObject(authentication.loginId,
				UserSession.class);
		
		boolean isMobile = request.getHeader("User-Agent").toUpperCase().indexOf("MOBILE") > -1;	// TODO flutter에서도 모바일 여부 확인 가능한지
		
		String orgId=userSession.getUser().getOrgId();
		
		attDoc.setURequestIp(getClientIp(request));
		
	    String jobUserType="P";
	    attDoc.setUJobUserType(jobUserType); //작업자구분(P:개인, D:부서관리자, C:사별관리자, G:그룹관리자, S:시스템)
		
	    IDfTime tt = new DfTime();
	    String s_Today = new DfTime().toString();
        
	    IDfSession idfSession = null;
	    IDfDocument idfDoc = null;
	    
	    //System.out.println("dbox첨부 요청내용 :"+attDoc.toString());
	    
    	String attFileId    = attDoc.getDboxId();
    	attFileId = DCTMUtils.checkNullStringByObj(attFileId)==""?"":attFileId;

		HttpHeaders headers = new HttpHeaders();
		Map<String, String> body = new HashMap<String, String>();
    	
    	if(!attFileId.equals("")) {
		    WfDoclist doc = new WfDoclist();
	
			ResponseEntity<InputStreamResource> nullRst=null;
		    try {
				//부서함> 전자결재> 년도
		    	doc.setUCabinetCode(userSession.getUser().getDeptCabinetcode());
	
		    	JSONParser parser = new JSONParser();
		        Map< String, String[] > arrMap = new HashMap<>();
	
		    	String lsBandRefLine=attDoc.getBandRefLine();
		    	
		    	Object obj = parser.parse( lsBandRefLine );
		    	JSONObject jsonObj = (JSONObject) obj;
		        arrMap.putAll(jsonObj);
		        
		        ComCodeType[] s_ComOrgArr = ComCodeType.values();
		        
	            //P:사용자id가 아니라 조직:O, 회사: C 인 경우는 edms_dept에서  u_cabinet_code 찾아서 권한부여할 때 사용한다
		    	for (Map.Entry<String, String[]> entry : arrMap.entrySet()) {
		              String keyValue="";
		    		  Object oArrayObj = entry.getValue();
		    		  JSONArray jsonArr = (JSONArray)oArrayObj;
		    		  
		    		  List<String> oArray = new ArrayList<String>();
		    		  
	    		      for(int i=0; i < jsonArr.size(); i++) {
	    	    		  keyValue = jsonArr.get(i).toString();//entry.getValue()[i];
	    	    		  if(!entry.getKey().equals("P")) { //부서나 회사에 대한 권한 부여일 때
	    	    			  try {
	    	    				      keyValue=gwDeptService.selectDeptByOrgId(keyValue).getUCabinetCode();
	    	    				      if(null!=keyValue && !keyValue.equals("") && !keyValue.equals(" "))  {
	    	    				         keyValue="g_"+keyValue;
	    	    				      }

	    	    			  } catch (Exception e) {
	    	    				  keyValue="";
	    	    			  }
		    	    		  if(null==keyValue || keyValue.equals("")|| keyValue.equals(" ")) {
		    	    			  String vOrgId=jsonArr.get(i).toString();
		    	    			  List<String> pUsrList = gwDeptService.selectUserListOfPart(vOrgId); //파트인 경우
		    	    			  for(int j=0; j < pUsrList.size(); j++) {
		    	    				  if(!pUsrList.get(j).equals(""))
		    	    				      oArray.add(pUsrList.get(j).toString());
		    	    			  }
		    	    		  }else {
	        	    		      oArray.add( keyValue) ;
		    	    		  }
	    	    		  }else {
	    	    			  List<String> usrList = new ArrayList<>();
	    	    			  usrList.add(keyValue);
	    	    			  List<VUser> gwUser=userService.selectUserListByUserIds(usrList);
	    	    			  if(gwUser.size() > 0 ) {
	    	    				  if(!gwUser.get(0).equals("") )  oArray.add( keyValue) ;
	    	    			  }else {
	    	    				  System.out.println("#MOLA:" +keyValue +" 사용자는 유효하지 않은 사용자ID 입니다");
	    	    			  }
	    	    		  }
	    		      }
	    		      String[] valueArray = oArray.toArray(new String[0]);
	    		      entry.setValue(valueArray);
	    		      
			    }
		    	attDoc.setBandRefLineM(arrMap); 
	
		    	attFileId = externalService.updateBandWfDoc(userSession,null, attDoc); //파일정보 변경
		    } catch (Exception e) {
		    	e.printStackTrace();
		    	
				headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
				headers.add("Content-Disposition", "attachment; filename=");
				headers.add("Pragma", "no-cache");
				headers.add("Expires", "0");
				headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));


				body.put("status", "500");
				body.put("message", e.getMessage());
				return OK(body.toString());
		    	
		    }
		 }
	    	
		 headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
		 headers.add("Content-Disposition", "attachment; filename=" + attFileId);
		 headers.add("Pragma", "no-cache");
		 headers.add("Expires", "0");
		 headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));

         if(!attFileId.equals("")) {
        	if(attDoc.getDboxId().equals(attFileId)) {
            	body.put("status", "200");
				body.put("message", attDoc.getBandRefLine().toString());//테스트를 위해서 받았던 참조라인값을 전달해줌
            }else {
    			body.put("status", "500");
    			body.put("message", "권한부여 실패");
            }
        }else {
			body.put("status", "500");
			body.put("message", "DboxId 값이 Null입니다");
        }
		System.out.println("attatch File id: " + attFileId);
	    return OK(body.toString());
	    
  }	
  
	
  @GetMapping(value = "/external/test")
  @ApiOperation(value = "test")
  public ApiResult<Object> test(@AuthenticationPrincipal JwtAuthentication authentication)
      throws Exception {
    UserSession userSession = (UserSession) getRedisRepository().getObject(authentication.loginId,
        UserSession.class); 
    
    
    return OK(authService.selectDefaultFolderAuth("d00004", "000004d280002597", userSession));
  }
  
  @GetMapping(value = "/external/infs-installer", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
  @ApiOperation(value = "인피니 agent 설치파일 다운로드")
  public ResponseEntity<InputStreamResource> getDataContent(@AuthenticationPrincipal JwtAuthentication authentication) throws Exception {
  //    UserSession userSession = (UserSession) getRedisRepository().getObject(authentication.socialPerId,
  //        UserSession.class);
      CustomInputStreamResource rst = externalService.downloadInfsInstaller();
      HttpHeaders headers = new HttpHeaders();
      headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
      headers.add("Content-Disposition", "attachment; filename=" + rst.getFilename());
      headers.add("Pragma", "no-cache");
      headers.add("Expires", "0");
  //    headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
      return ResponseEntity.ok().headers(headers).contentLength(rst.contentLength())
          .contentType(MediaType.parseMediaType("application/octet-stream"))
          .body(new InputStreamResource(rst.getInputStream()));
  }
	
	@GetMapping(value = "/external/{dataId}/viewer")
	@ApiOperation(value = "자료 뷰어")
	public ApiResult<String> getDataViewer(
			@AuthenticationPrincipal JwtAuthentication authentication,
  		@ApiParam(value = "문서 아이디") @PathVariable String dataId,
  		@ApiParam(value = "인증키 (api_key)") @RequestParam(required = false) String token,
  		HttpServletRequest request
  	) throws Exception {
	  
	  // 퀴리스트링으로 받은 토큰값이 없고 쿠키에 토큰값이 있을 경우 쿠키에서 꺼내서 적용
    if (token == null && request.getCookies() != null) {
      for (Cookie cookie : request.getCookies()) {
        if (tokenHeader.equals(cookie.getName())) token = cookie.getValue();
      }
    }
    
    String result = externalService.makeSynapViewerUrl(dataId, token);

    return OK(result);
	}
  
  @PatchMapping(value = "/external/users/{userId}/password")
  @ApiOperation(value = "사용자 비밀번호 수정")
  public ApiResult<Boolean> patchDataOne(@AuthenticationPrincipal JwtAuthentication authentication,
      @ApiParam(value = "userId", example = "chulsu.kim") @PathVariable String userId,
      @ApiParam(value = "변경 정보") @RequestBody(required = true) UserPwUpdateDto updateDto,
      HttpServletRequest request) throws Exception {
    updateDto.chkValues();
    int rst = userService.updateUserPw(userId, updateDto.getOldPw(), updateDto.getNewPw());
    if (rst == 0) throw new IllegalArgumentException("값이 유효하지 않습니다."); 
    else return OK(true);
  }
  
  @GetMapping(value = "/external/users/{userId}/dsearch-auth")
  @ApiOperation(value = "DSearch에서 그룹/관리자여부 조회")
  public ApiResult<UserDSearchAuthDto> getDSearchAuth(
      @ApiParam(value = "userId", example = "chulsu.kim") @PathVariable String userId,
      HttpServletRequest request) throws Exception {
    UserDSearchAuthDto result = authService.selectDSearchUserAuth(userId);
    return OK(result);
  }
}
