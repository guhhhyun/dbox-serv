package com.dongkuksystems.dbox.controllers;

import static com.dongkuksystems.dbox.models.api.response.ApiResult.OK;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.documentum.fc.client.DfQuery;
import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfDocument;
import com.documentum.fc.client.IDfPersistentObject;
import com.documentum.fc.client.IDfQuery;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.common.DfId;
import com.dongkuksystems.dbox.constants.EntCode;
import com.dongkuksystems.dbox.constants.HamType;
import com.dongkuksystems.dbox.daos.table.log.LogDao;
import com.dongkuksystems.dbox.daos.type.auth.share.AuthShareDao;
import com.dongkuksystems.dbox.daos.type.docbox.project.ProjectDao;
import com.dongkuksystems.dbox.daos.type.docbox.research.ResearchDao;
import com.dongkuksystems.dbox.daos.type.user.preset.UserPresetDao;
import com.dongkuksystems.dbox.errors.NotFoundException;
import com.dongkuksystems.dbox.models.api.response.ApiResult;
import com.dongkuksystems.dbox.models.common.UserSession;
import com.dongkuksystems.dbox.models.dto.path.DPath;
import com.dongkuksystems.dbox.models.table.etc.VDept;
import com.dongkuksystems.dbox.models.table.etc.VUser;
import com.dongkuksystems.dbox.models.type.auth.AuthShare;
import com.dongkuksystems.dbox.models.type.doc.Doc;
import com.dongkuksystems.dbox.models.type.doc.DocImp;
import com.dongkuksystems.dbox.models.type.docbox.Project;
import com.dongkuksystems.dbox.models.type.docbox.Research;
import com.dongkuksystems.dbox.models.type.folder.Folder;
import com.dongkuksystems.dbox.securities.JwtAuthentication;
import com.dongkuksystems.dbox.services.auth.AuthService;
import com.dongkuksystems.dbox.services.code.CodeService;
import com.dongkuksystems.dbox.services.data.DataPathService;
import com.dongkuksystems.dbox.services.data.DataService;
import com.dongkuksystems.dbox.services.dept.GwDeptService;
import com.dongkuksystems.dbox.services.doc.DocImpService;
import com.dongkuksystems.dbox.services.doc.DocService;
import com.dongkuksystems.dbox.services.folder.FolderService;
import com.dongkuksystems.dbox.services.user.UserService;
import com.dongkuksystems.dbox.utils.dctm.DCTMUtils;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@RestController
@Api(tags = "자료 이동/복사/이관/삭제처리 APIs")
public class DataPathChgController extends AbstractCommonController {
  private final CodeService codeService;

  private final DataService dataService;
  private final FolderService folderService;
  private final DocService docService;
  private final DocImpService docImpService;
  private final DataPathService pathService;
  private final AuthService authService;

  private final GwDeptService gwDeptService;

  private final ResearchDao researchDao;
  private final ProjectDao projectDao;
  private final UserPresetDao userPresetDao;

  private final UserService userService;

  private final AuthShareDao authShareDao;

  private final LogDao logDao;

  public DataPathChgController(DataService dataService, FolderService folderService, DocService docService,
      CodeService codeService, DocImpService docImpService, DataPathService pathService, AuthService authService,
      GwDeptService gwDeptService, ResearchDao researchDao, ProjectDao projectDao, UserPresetDao userPresetDao,
      UserService userService, AuthShareDao authShareDao, LogDao logDao) {
    this.dataService = dataService;
    this.folderService = folderService;
    this.docService = docService;
    this.codeService = codeService;
    this.docImpService = docImpService;
    this.pathService = pathService;
    this.authService = authService;
    this.gwDeptService = gwDeptService;
    this.researchDao = researchDao;
    this.projectDao = projectDao;
    this.userPresetDao = userPresetDao;
    this.userService = userService;
    this.authShareDao = authShareDao;
    this.logDao = logDao;
  }

  /**
   * Agent 문서 보기 정보 확인
   * 
   * @param authentication
   * @param dataViewCheckoutDto
   * @param dataId
   * @return
   * @throws Exception
   */
  @PostMapping(value = "/data/path/chg")
  @ApiOperation(value = "자료 이동/이관/복사/삭제처리")
  public ApiResult<Object> getViewCheck(@AuthenticationPrincipal JwtAuthentication authentication,
      @ApiParam(value = "자료이동이관복사삭제요청") @RequestBody DPath dto, HttpServletRequest request) throws Exception {

    UserSession userSession = (UserSession) getRedisRepository().getObject(authentication.loginId, UserSession.class);
    boolean isMobile = request.getHeader("User-Agent").toUpperCase().indexOf("MOBILE") > -1; // TODO flutter에서도 모바일
    // 여부 확인 가능한지
    final String userId = userSession.getUser().getUserId();

    dto.setReqUser(userId);
    dto.setTargetSecLevel("T");// 기본 부서로 지정

    if ((null == dto.getSourceFolders() || dto.getSourceFolders().size() < 1)
        && (null == dto.getSourceFiles() || dto.getSourceFiles().size() < 1)
        && (null == dto.getSourcePjts() || dto.getSourcePjts().size() < 1)
        && (null == dto.getSourceRscs() || dto.getSourceRscs().size() < 1)
        && (null == dto.getSourceTFiles() || dto.getSourceTFiles().size() < 1)
        && (null == dto.getSourceTIds() || dto.getSourceTIds().size() < 1) // &&
    // (null == dto.getSrcFolId())

    )
      throw new RuntimeException("\u00A0[ 문서가 선택되지 않았습니다\u00A0 "
          + "\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0"
          + "\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0"
          + "\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0"
          + " 선택한 폴더나 문서가 파랗게 표시된 후 처리해 주시기 바랍니다.]");

    Map<String, String> gotoMap = new HashMap<String, String>();
    Map<String, String> pjtMap = new HashMap<String, String>();
    gotoMap.put("DPC", "DFO");// 부서문서함
    gotoMap.put("DIM", "DIF");// 중요문서함

    gotoMap.put("PJT", "PFO");// 타겟으로 프로젝트/투자 카테고리를 선택했을 때, 일반 폴더는 PFO타입으로 생성한다
    gotoMap.put("POW", "PFO");// 타겟으로 프로젝트/투자 카테고리하단의 주관 프로젝트/투자를 선택했을 때, 일반 폴더는 PFO타입으로 생성한다
    gotoMap.put("PFN", "PFO");// 타겟으로 프로젝트/투자 카테고리하단의 주관 프로젝트/투자밑의 완료함을 선택했을 때, 일반 폴더는 PFO타입으로 생성한다
    gotoMap.put("PCL", "PFO");// 타겟으로 프로젝트/투자 의 '분류폴더를' 선택했을 때, 일반 폴더는 PFO타입으로 생성한다( PCL은 edms_folder에서 조회가능)
    gotoMap.put("PJC", "PFO");// PJC. edms_project에 정보 추가/삭제 필요

    gotoMap.put("PIN", "PIC");// 참여프로젝트
    gotoMap.put("PIF", "PIC");// 참여프로젝트 완료함
    gotoMap.put("RSC", "RFO");// 연구과제
    gotoMap.put("ROW", "RFO"); // 주관 연구과제
    gotoMap.put("RFN", "RFO"); // 연구과제 완료함
    gotoMap.put("RCL", "RFO"); // 연구과제 정리용 폴더(edms_folder에 존재)
    gotoMap.put("RIN", "RIC"); // 참여 연구과제
    gotoMap.put("RIF", "RIC"); // 참여 프로젝트 완료함
    gotoMap.put("SHR", "SFO"); // 참여프로젝트 문서함 (프로젝트)

    gotoMap.put("CPC", "CFO"); // 사별문서함
    gotoMap.put("MPC", "MFO"); // 사별(관리직)문서함

    // 프로젝트(edms_project) 나 연구과제 (edms_research)에 정보생성이나 변경을 해야 하는 경우
    // 이동, 복사시에는 폴더명으로 생성한다.
    //
    pjtMap.put("PJT", "PJC");// 주관프로젝트
    pjtMap.put("POW", "PJC");
    pjtMap.put("PFN", "PJC");
    pjtMap.put("PCL", "PJC");

    pjtMap.put("PIN", "PIC");// 참여프로젝트
    pjtMap.put("PIF", "PIC");

    pjtMap.put("RSC", "RSC");// 주관연구과제
    pjtMap.put("ROW", "RSC");
    pjtMap.put("RFN", "RSC");
    pjtMap.put("RCL", "RSC");

    pjtMap.put("RIN", "RIC");// 참여 연구과제
    pjtMap.put("RIF", "RIC");

    // TODO:(2022.02.23. 복사/이동/삭제 인수인계 정리)
    // body : return data
    // body.flag: T가 아니면 화면에서 refresh
    // : T나 공백은 : 정상처리, refresh
    // : MK : 예외처리 메세지 출력과 화면을 refresh안하기 위한 것
    // : MC : move인데 copy처리하는 case - 화면 refresh
    // : MP : move -> project - 화면 refresh
    // : MR : move -> research - 화면 refresh
    Map<String, String> body = new HashMap<String, String>();
    String lsFlag = "S";

    String uptPthGbn = dto.getUptPthGbn(); // 처리구분(C:복사,M:이동,T:이관,D:삭제

    // 테스트
    // dto.setSrcFolId(" ");

    // if(dto.getSrcFolId().equals(" ")) {
    // System.out.println("#MOLA:루트폴더에서 선택됨");
    // }else {
    // System.out.println("#MOLA:선택폴더:" + dto.getSrcFolId());
    /*
     * Optional<Folder> optFolder = folderService.selectOne(dto.getSrcFolId());
     * if(optFolder.isPresent()) { String sourceGubun =
     * optFolder.get().getUFolType(); String sourceCabCd =
     * optFolder.get().getUCabinetCode(); dto.setSrcCabinetcode(sourceCabCd); }
     */
    // }

    /*** 반출함 삭제건인지 체크 **/
    boolean bTakeout = false;
    int iTakeOutDel = 0;
    if (uptPthGbn.equals("D") && null != dto.getSourceTIds() && null != dto.getSourceTFiles()) {
      if (dto.getSourceTIds().size() != dto.getSourceTFiles().size()) {
        body.put("flag", "MK");
        body.put("status", "200");
        body.put("message", "반출함 삭제정보 생성오류(요청id:" + dto.getSourceTIds().size() + " 건 ,파일:"
            + dto.getSourceTFiles().size() + " 건). 관리자에 문의바랍니다");
        return OK(body);
      }

      if (dto.getSourceTIds().size() > 0 && dto.getSourceTIds().size() == dto.getSourceTFiles().size()) {
        List<String> sTIdArray = dto.getSourceTIds(); // edms_req_takeout_doc 에 등록된 요청ID (u_req_id)
        List<String> sTFilArray = dto.getSourceTFiles(); // edms_req_takeout_doc 에 등록된 요청문서ID( u_req_id)
        // 화면에서 file id와 요청id가 세트로 넘어와서 같은 i 번째를 처리함
        for (int i = 0; i < sTIdArray.size(); i++) {

          String lsReqRObjectId = sTIdArray.get(i);// pathService.selectTakeoutDocsRobjectIdByDocId(s_ObjId);
          if (null != lsReqRObjectId && !lsReqRObjectId.equals("") && !lsReqRObjectId.equals(" ")) {

            bTakeout = true;
            // System.out.println("반출요청 rObjectId=" + lsReqRObjectId+" 요청 문서id: "+
            // sTFilArray.get(i));
            IDfCollection idf_Col = null;
            IDfSession idfSession = DCTMUtils.getAdminSession();
            try {
              String s_Dql = "UPDATE edms_req_takeout_doc  OBJECTS SET u_status='D' " + "WHERE u_req_id='"
                  + lsReqRObjectId + "' and u_req_doc_id='" + sTFilArray.get(i) + "' ";

              IDfQuery idf_Qry = null;
              idf_Qry = new DfQuery();
              idf_Qry.setDQL(s_Dql);
              idf_Col = idf_Qry.execute(idfSession, DfQuery.QUERY);
              if (idf_Col != null && idf_Col.next())
                idf_Col.close();

            } catch (Exception e) {
              // e.printStackTrace();
              throw new RuntimeException("반출요청 파일 삭제중 예외사항 발생(" + e.getMessage() + ")");
            } finally {
              idfSession.disconnect();
              if (idf_Col != null)
                idf_Col.close();
            }
            iTakeOutDel++;
          }
        }
      }
    }

    // TODO:(2022.02.23. 복사/이동/삭제 인수인계 정리)
    // 반출함 삭제일 경우 바로 return
    if (bTakeout) {
      if (iTakeOutDel > 0) {
        body.put("flag", "MK");
        body.put("status", "200");
        body.put("message", "반출함의 문서 " + iTakeOutDel + " 건을 삭제하였습니다");
        return OK(body);
      }
    }

    // TODO:(2022.02.23. 복사/이동/삭제 인수인계 정리)
    // 복사라도 이동으로 됨
    /******* 이동 복사시 타겟의 정보 조회해서 DTO에 세팅 (프로젝트, 연구과제, 프로젝트나 연구과제 완료-분류폴더이동처리 ******/
    if (uptPthGbn.equals("C") || uptPthGbn.equals("M")) {
      /*****************************************/
      // 프로젝트나 연구과제 완료함에서 완료함내 분류폴더로 이동/복사하는 경우에 대한 처리 : 상위폴더를 분류폴더로 업데이트하는 처리
      /*****************************************/
      if (dto.getTargetGubun().equals("PCL") || dto.getTargetGubun().equals("RCL")) {
        Optional<Folder> optFolder = folderService.selectOne(dto.getTargetDboxId()); // 여기로 오는게 PCL이나 RCL같은 분류폴더 ID가
                                                                                     // TargetDboxId로 넘어온다.
        if (optFolder.isPresent()) {
          if (userSession.getUser().getDeptCabinetcode().equals(optFolder.get().getUCabinetCode())) {

            IDfCollection idf_Col = null;

            if (dto.getTargetGubun().equals("PCL")) {

              if (dto.getSourcePjts().size() > 0) {
                IDfSession idfSession = DCTMUtils.getAdminSession();
                try {
                  for (int li = 0; li < dto.getSourcePjts().size(); li++) {
                    String ls_PrCode = dto.getSourcePjts().get(li);
                    String s_Dql = "";
                    if (ls_PrCode.length() < 2)
                      continue;

                    LocalDate now = LocalDate.now();
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
                    String formatedNow = now.format(formatter);
                    LocalTime time = LocalTime.now();
                    DateTimeFormatter timeformat = DateTimeFormatter.ofPattern("HH:mm:ss");
                    String timeformatNow = time.format(timeformat);

                    String ls_UpdateDate = formatedNow + " " + timeformatNow;

                    // TODO:(2022.02.23. 복사/이동/삭제 인수인계 정리)
                    // update날짜 수정은 통합검색에서 인식할 수 있기 위함
                    if (ls_PrCode.substring(0, 1).equals("p")) {
                      s_Dql = "UPDATE edms_project  OBJECTS SET u_fol_id='" + dto.getTargetDboxId()
                          + "',  SET u_finish_yn='Y', SET u_update_date=DATE('" + ls_UpdateDate
                          + "','mm/dd/yyyy hh:mm:ss'),  SET u_update_user='" + userId + "' " + "WHERE u_cabinet_code='"
                          + optFolder.get().getUCabinetCode() + "' and u_pjt_code='" + dto.getSourcePjts().get(li)
                          + "' ";
                    } else {
                      s_Dql = "UPDATE edms_project  OBJECTS SET u_fol_id='" + dto.getTargetDboxId()
                          + "',  SET u_finish_yn='Y', SET u_update_date=DATE('" + ls_UpdateDate
                          + "','mm/dd/yyyy hh:mm:ss'),  SET u_update_user='" + userId + "' " + "WHERE u_cabinet_code='"
                          + optFolder.get().getUCabinetCode() + "' and r_object_id='" + dto.getSourcePjts().get(li)
                          + "' ";

                    }
                    IDfQuery idf_Qry = null;
                    idf_Qry = new DfQuery();
                    idf_Qry.setDQL(s_Dql);
                    idf_Col = idf_Qry.execute(idfSession, DfQuery.QUERY);
                    if (idf_Col != null && idf_Col.next())
                      idf_Col.close();

                  }
                } catch (Exception e) {
                  // e.printStackTrace();
                  throw new RuntimeException("분류폴더 이동중 예외상황이 발생하였습니다\n시스템관리자에 문의하여 주시기 바랍니다");
                } finally {
                  idfSession.disconnect();
                  if (idf_Col != null)
                    idf_Col.close();
                  body.put("flag", lsFlag);
                  body.put("status", "200");
                  body.put("message", "프로젝트 분류폴더로 이동하였습니다");
                  return OK(body);
                }
              } else {
                throw new RuntimeException("프로젝트 완료-분류폴더로 이동할 프로젝트를 선택하셔야 합니다");
                /*
                 * body.put("flag", lsFlag); body.put("status", "200"); body.put("message",
                 * "프로젝트 완료-분류폴더로 이동할 프로젝트를 선택하셔야 합니다"); return OK(body);
                 */
              }

            } else if (dto.getTargetGubun().equals("RCL")) {
              if (dto.getSourceRscs().size() > 0) {
                IDfSession idfSession = DCTMUtils.getAdminSession();
                try {
                  LocalDate now = LocalDate.now();
                  DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
                  String formatedNow = now.format(formatter);
                  LocalTime time = LocalTime.now();
                  DateTimeFormatter timeformat = DateTimeFormatter.ofPattern("HH:mm:ss");
                  String timeformatNow = time.format(timeformat);

                  String ls_UpdateDate = formatedNow + " " + timeformatNow;

                  for (int li = 0; li < dto.getSourceRscs().size(); li++) {

                    String ls_PrCode = dto.getSourceRscs().get(li);
                    String s_Dql = "";
                    if (ls_PrCode.length() < 2)
                      continue;
                    if (ls_PrCode.substring(0, 1).equals("r")) {
                      s_Dql = "UPDATE edms_research  OBJECTS SET u_fol_id='" + dto.getTargetDboxId()
                          + "',  SET u_finish_yn='Y', SET u_update_date=DATE('" + ls_UpdateDate
                          + "','mm/dd/yyyy hh:mm:ss'),  SET u_update_user='" + userId + "' " + "WHERE u_cabinet_code='"
                          + optFolder.get().getUCabinetCode() + "' and u_rsch_code='" + dto.getSourceRscs().get(li)
                          + "' ";
                    } else {
                      s_Dql = "UPDATE edms_research  OBJECTS SET u_fol_id='" + dto.getTargetDboxId()
                          + "',  SET u_finish_yn='Y', SET u_update_date=DATE('" + ls_UpdateDate
                          + "','mm/dd/yyyy hh:mm:ss'),  SET u_update_user='" + userId + "' " + "WHERE u_cabinet_code='"
                          + optFolder.get().getUCabinetCode() + "' and r_object_id='" + dto.getSourceRscs().get(li)
                          + "' ";

                    }
                    IDfQuery idf_Qry = null;
                    idf_Qry = new DfQuery();
                    idf_Qry.setDQL(s_Dql);
                    idf_Col = idf_Qry.execute(idfSession, DfQuery.QUERY);
                    if (idf_Col != null && idf_Col.next())
                      idf_Col.close();
                  }
                } catch (Exception e) {
                  // e.printStackTrace();
                  throw new RuntimeException("분류폴더 이동중 예외상황이 발생하였습니다\n시스템관리자에 문의하여 주시기 바랍니다");
                } finally {
                  idfSession.disconnect();
                  if (idf_Col != null)
                    idf_Col.close();
                  body.put("flag", lsFlag);
                  body.put("status", "200");
                  body.put("message", "연구과제 분류폴더로 이동하였습니다");
                  return OK(body);
                }
              } else {
                throw new RuntimeException("연구과제 완로-분류폴더로 이동할 프로젝트를 선택하셔야 합니다");
              }
            }

          } else {
            throw new RuntimeException("타 부서의 분류함입니다");
          }
        } else {
          throw new RuntimeException("존재하지 않는 분류폴더정보입니다(" + dto.getTargetDboxId() + ")");
        }
      }
      /*****************************************/
      // 프로젝트나 연구과제 완료함으로 이동/복사하는 경우에 대한 처리 : 프로젝트나 연구과제의 완료여부를 Y 로 변경해준다(현재 : 프로젝트
      // Context메뉴에서 처리함) -> 화면에서 막혀있음
      /*****************************************/
      if (dto.getTargetGubun().equals("PFN") || dto.getTargetGubun().equals("RFN")) { // targetDboxId로 타겟의 cabinet_code가
                                                                                      // 넘어옴
        if (dto.getTargetGubun().equals("PFN") && dto.getSourcePjts().size() > 0) {

          Optional<Project> pjtOpt = null;
          if (!dto.getSourcePjts().get(0).equals("")) {

            if (dto.getSourcePjts().get(0).substring(0, 1).equals("p"))
              pjtOpt = projectDao.selectOneByUPjtCode(dto.getSourcePjts().get(0));
            else
              pjtOpt = projectDao.selectOne(dto.getSourcePjts().get(0));

            if (pjtOpt.isPresent()) {

              if (null == dto.getSrcCabinetcode() || dto.getSrcCabinetcode().equals(""))
                dto.setSrcCabinetcode(pjtOpt.get().getUCabinetCode());
            }
          }
        } else if (dto.getTargetGubun().equals("RFN") && dto.getSourceRscs().size() > 0) {
          if (!dto.getSourceRscs().get(0).equals("")) {
            Optional<Research> rcsOpt = null;
            if (dto.getSourceRscs().get(0).substring(0, 1).equals("r"))
              rcsOpt = researchDao.selectOneByURschCode(dto.getSourceRscs().get(0));
            else
              rcsOpt = researchDao.selectOne(dto.getSourceRscs().get(0));

            if (rcsOpt.isPresent()) {

              if (null == dto.getSrcCabinetcode() || dto.getSrcCabinetcode().equals(""))
                dto.setSrcCabinetcode(rcsOpt.get().getUCabinetCode());
            }
          }
        } else {
          // 파일이나 폴더를 프로젝트/연구과제 완료함으로 이동/복사하는 경우 (2022.01.14 추가)
          // => dto.getSrcCabinetcode() 값이 세팅되어있지 않아서 바로 아래서 null Exception발생 방지
          throw new RuntimeException("붙여넣기할 수 없는 경로입니다");

        }
        if (dto.getSrcCabinetcode().equals(dto.getTargetDboxId())) { // 같은 부서 cabinet_code인지 확인
          IDfCollection idf_Col = null;

          if (dto.getTargetGubun().equals("PFN") && dto.getSourcePjts().size() > 0) {

            if (dto.getSourcePjts().size() > 0) {
              IDfSession idfSession = DCTMUtils.getAdminSession();
              try {
                LocalDate now = LocalDate.now();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
                String formatedNow = now.format(formatter);
                LocalTime time = LocalTime.now();
                DateTimeFormatter timeformat = DateTimeFormatter.ofPattern("HH:mm:ss");
                String timeformatNow = time.format(timeformat);

                String ls_UpdateDate = formatedNow + " " + timeformatNow;

                for (int li = 0; li < dto.getSourcePjts().size(); li++) {
                  String ls_PrCode = dto.getSourcePjts().get(li);
                  String s_Dql = "";
                  if (ls_PrCode.length() < 2)
                    continue;
                  if (ls_PrCode.substring(0, 1).equals("p")) {

                    s_Dql = "UPDATE edms_project  OBJECTS SET u_fol_id=' ', SET u_finish_yn='Y', SET u_update_date=DATE('"
                        + ls_UpdateDate + "','mm/dd/yyyy hh:mm:ss'), SET u_update_user='" + userId + "'"
                        + "WHERE u_cabinet_code='" + dto.getTargetDboxId() + "' and u_pjt_code='" + ls_PrCode + "' ";
                  } else {
                    s_Dql = "UPDATE edms_project  OBJECTS SET u_fol_id=' ',  SET u_finish_yn='Y', SET u_update_date=DATE('"
                        + ls_UpdateDate + "','mm/dd/yyyy hh:mm:ss'),  SET u_update_user='" + userId + "'"
                        + "WHERE u_cabinet_code='" + dto.getTargetDboxId() + "' and r_object_id='" + ls_PrCode + "' ";

                  }

                  IDfQuery idf_Qry = null;
                  idf_Qry = new DfQuery();
                  idf_Qry.setDQL(s_Dql);
                  idf_Col = idf_Qry.execute(idfSession, DfQuery.QUERY);
                  if (idf_Col != null && idf_Col.next())
                    idf_Col.close();
                }
              } catch (Exception e) {
                // e.printStackTrace();
                throw new RuntimeException("완료폴더 이동중 예외상황이 발생하였습니다\n시스템관리자에 문의하여 주시기 바랍니다");
              } finally {
                idfSession.disconnect();
                if (idf_Col != null)
                  idf_Col.close();
                body.put("flag", lsFlag);
                body.put("status", "200");
                body.put("message", "프로젝트 완료폴더로 이동하였습니다");
                return OK(body);
              }
            } else {
              throw new RuntimeException("프로젝트 완로-분류폴더로 이동할 프로젝트를 선택하셔야 합니다");
            }

          } else if (dto.getTargetGubun().equals("RFN") && dto.getSourceRscs().size() > 0) {
            if (dto.getSourceRscs().size() > 0) {
              IDfSession idfSession = DCTMUtils.getAdminSession();
              try {
                LocalDate now = LocalDate.now();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
                String formatedNow = now.format(formatter);
                LocalTime time = LocalTime.now();
                DateTimeFormatter timeformat = DateTimeFormatter.ofPattern("HH:mm:ss");
                String timeformatNow = time.format(timeformat);

                String ls_UpdateDate = formatedNow + " " + timeformatNow;

                for (int li = 0; li < dto.getSourceRscs().size(); li++) {
                  String ls_PrCode = dto.getSourceRscs().get(li);
                  String s_Dql = "";
                  if (ls_PrCode.length() < 2)
                    continue;
                  if (ls_PrCode.substring(0, 1).equals("r")) {
                    s_Dql = "UPDATE edms_research  OBJECTS SET u_finish_yn='Y', SET u_fol_id=' ', SET u_update_date=DATE('"
                        + ls_UpdateDate + "','mm/dd/yyyy hh:mm:ss'), SET u_update_user='" + userId + "'"
                        + "WHERE u_cabinet_code='" + dto.getTargetDboxId() + "' and u_rsch_code='"
                        + dto.getSourceRscs().get(li) + "' ";
                  } else {
                    s_Dql = "UPDATE edms_research  OBJECTS SET u_finish_yn='Y', SET u_fol_id=' ', SET u_update_date=DATE('"
                        + ls_UpdateDate + "','mm/dd/yyyy hh:mm:ss'), SET u_update_user='" + userId + "'"
                        + "WHERE u_cabinet_code='" + dto.getTargetDboxId() + "' and r_object_id='"
                        + dto.getSourceRscs().get(li) + "' ";
                  }
                  IDfQuery idf_Qry = null;
                  idf_Qry = new DfQuery();
                  idf_Qry.setDQL(s_Dql);
                  idf_Col = idf_Qry.execute(idfSession, DfQuery.QUERY);
                  if (idf_Col != null && idf_Col.next())
                    idf_Col.close();
                }
              } catch (Exception e) {
                // e.printStackTrace();
                throw new RuntimeException("완료폴더 이동중 예외상황이 발생하였습니다\n시스템관리자에 문의하여 주시기 바랍니다");
              } finally {
                if (idf_Col != null)
                  idf_Col.close();
                idfSession.disconnect();
                body.put("flag", lsFlag);
                body.put("status", "200");
                body.put("message", "연구과제 완료폴더로 이동하였습니다");
                return OK(body);
              }
            } else {
              throw new RuntimeException("연구과제 완료폴더로 이동할 프로젝트를 선택하셔야 합니다");
            }
          }
        } else {
          throw new RuntimeException("타 부서의 분류함입니다");
        }
      }
      
      /*****************************************/
      // 이동처리가 불가한 경로에 대한 요청이 온 경우에 대한 예외처리 ( 화면에서 예외로 걸러내기는 하지만, 예외처리로 잘못된 데이터 처리 예방)
      /*****************************************/
      if (uptPthGbn.equals("M")) {
        if (dto.getTargetGubun().equals("PIN") || dto.getTargetGubun().equals("PIF")
            || dto.getTargetGubun().equals("RIN") || dto.getTargetGubun().equals("RIF")) {
          throw new RuntimeException("붙여넣기할 수 없는 경로입니다");
        }
      }
      if (dto.getSourcePjts().size() > 0) {
        if (!dto.getSourcePjts().get(0).equals(" ")) {
          if (uptPthGbn.equals("M") || uptPthGbn.equals("M")) {
            throw new RuntimeException("프로젝트단위 이동복사는 허용되지 않습니다");
          }
          if (dto.getTargetGubun().equals("PJT") || dto.getTargetGubun().equals("POW")) {
            throw new RuntimeException("붙여넣기할 수 없는 경로입니다");
          }
          if (!dto.getTargetGubun().equals("PCL") && !uptPthGbn.equals("M")) {
            throw new RuntimeException("프로젝트단위 이동복사는 허용되지 않습니다");
          }
        }
      }
      if (dto.getSourceRscs().size() > 0) {
        if (!dto.getSourceRscs().get(0).equals(" ")) {
          if (dto.getTargetGubun().equals("RSC") || dto.getTargetGubun().equals("ROW")) {
            throw new RuntimeException("붙여넣기할 수 없는 경로입니다");
          }
          if (!dto.getTargetGubun().equals("RCL") && !uptPthGbn.equals("M")) {
            throw new RuntimeException("연구/과제 단위 복사는 허용되지 않습니다");
          }
        }
      }
      if (dto.getSourceFiles().size() > 0) {
        if (!dto.getSourceFiles().get(0).equals(" ")) {
          if (dto.getTargetGubun().equals("PJT") || dto.getTargetGubun().equals("POW")
              || dto.getTargetGubun().equals("PFN") || dto.getTargetGubun().equals("PIN")
              || dto.getTargetGubun().equals("PIF") || dto.getTargetGubun().equals("RSC")
              || dto.getTargetGubun().equals("ROW") || dto.getTargetGubun().equals("RFN")
              || dto.getTargetGubun().equals("RIN") || dto.getTargetGubun().equals("RIF")) {
            throw new RuntimeException("붙여넣기할 수 없는 경로입니다");
          }
        }
      }
      
      //TODO:(2022.02.23. 복사/이동/삭제 인수인계 정리)
      //중요문서함용으로 추가했는데 현재 사용 안함
      if (null != gotoMap.get(dto.getTargetGubun())) {
        if (dto.getSourceFiles().size() > 0 && null != dto.getSourceFiles()) {
          if (!dto.getSourceFiles().get(0).equals("")) {
            // System.out.println(" 이동-복사 불가한 경우");
            // if(uptPthGbn.equals("M") && !dto.getTargetGubun().equals("PJC") &&
            // !dto.getTargetGubun().equals("RSC"))
            // throw new RuntimeException("파일이 이동할 수 없는 경로입니다");
          }
        }
      }
    }

    /** 타겟이 지정되었을 때, 해당 타겟이 카테고리타입일 경우 소스의 폴더타입을 타겟의 폴더타입으로 변겨해줄 때 사용 **/
    //TODO:(2022.02.23. 복사/이동/삭제 인수인계 정리)
    //알림을 위해 데이터 미리 세팅
    VDept myDept = gwDeptService.selectDeptByOrgId(userSession.getUser().getOrgId());

    dto.setUComOrgCabinetCd(gwDeptService.selectDeptByOrgId(myDept.getComOrgId()).getUCabinetCode());
    dto.setUGroupOrgCabinetCd(gwDeptService.selectDeptByOrgId("DKG").getUCabinetCode());
    if (null != myDept.getManagerPerId()) {
      dto.setUApprover(myDept.getManagerPerId()); // 승인자 세팅
      VUser reqMgr = userService.selectOneByUserId(dto.getUApprover()).orElse(new VUser());
      dto.setUApproverEmail(reqMgr.getEmail());
      dto.setUApproverPhoneNo(reqMgr.getMobileTel());
      dto.setUApproverNm(reqMgr.getDisplayName());
    } else {
      String ls_ApproverId = null;// pathService.selectTemporaryRelManagerId(userSession.getUser().getOrgId());
      if (null != ls_ApproverId) {
        VUser reqMgr = userService.selectOneByUserId(ls_ApproverId).orElse(new VUser());
        dto.setUApproverEmail(reqMgr.getEmail());
        dto.setUApproverPhoneNo(reqMgr.getMobileTel());
        dto.setUApproverNm(reqMgr.getDisplayName());
      } else {
        if (uptPthGbn.equals("D"))
          throw new RuntimeException("삭제/폐기 결재승인자가 미지정상태입니다");
      }
    }

    /*****************************************/
    // 이동복사시 이후 처리에 필요한 DTO 변수값의 세팅 1차
    /*****************************************/
    //TODO:(2022.02.23. 복사/이동/삭제 인수인계 정리)
    // 사용 이유 확인해야함..
    if (uptPthGbn.equals("C") || uptPthGbn.equals("M")) {
      if (null != dto.getSrcCabinetcode() && null != dto.getTgCabinetcode()) {
        if (!dto.getSrcCabinetcode().equals(dto.getTgCabinetcode())) {
          String sTgOrgId = gwDeptService.selectOrgIdByCabinetcode(dto.getTgCabinetcode());
          dto.setTgOrgId(sTgOrgId);

          //TODO:(2022.02.23. 복사/이동/삭제 인수인계 정리) : 위에서 중복 호출인듯 삭제 해도 되는지 체크
          myDept = gwDeptService.selectDeptByOrgId(userSession.getUser().getOrgId());
          dto.setUComOrgCabinetCd(gwDeptService.selectDeptByOrgId(myDept.getComOrgId()).getUCabinetCode());
          dto.setUGroupOrgCabinetCd(gwDeptService.selectDeptByOrgId("DKG").getUCabinetCode());
          dto.setUApprover(myDept.getManagerPerId()); // 승인자 세팅
          if (null != myDept.getManagerPerId()) {
            VUser reqMgr = userService.selectOneByUserId(dto.getUApprover()).orElse(new VUser());
            dto.setUApproverEmail(reqMgr.getEmail());
            dto.setUApproverPhoneNo(reqMgr.getMobileTel());
            dto.setUApproverNm(reqMgr.getDisplayName());
          } else {
            String ls_ApproverId = null;// pathService.selectTemporaryRelManagerId(userSession.getUser().getOrgId());
            if (null != ls_ApproverId) {
              VUser reqMgr = userService.selectOneByUserId(ls_ApproverId).orElse(new VUser());
              dto.setUApproverEmail(reqMgr.getEmail());
              dto.setUApproverPhoneNo(reqMgr.getMobileTel());
              dto.setUApproverNm(reqMgr.getDisplayName());
            } else {
              if (uptPthGbn.equals("D"))
                throw new RuntimeException("삭제/폐기 결재승인자가 미지정상태입니다");
            }
          }
          if (uptPthGbn.equals("M")) {
            uptPthGbn = "C";
            dto.setUptPthGbn(uptPthGbn);
            lsFlag = "MC";
          }
        }
      }
    }

    /*
     * Map<String, String> uGubunMap = new HashMap<String, String>();
     * uGubunMap.put("D", "부서함"); uGubunMap.put("DI", "중요부서함"); uGubunMap.put("P",
     * "프로젝트/투자"); uGubunMap.put("R", "연구과제"); uGubunMap.put("S", "공유/협업");
     * uGubunMap.put("C", "조직함(회사)"); uGubunMap.put("M", "조직함(관리직)");
     * uGubunMap.put("DW", "전자결재");
     */
    //TODO:(2022.02.23. 복사/이동/삭제 인수인계 정리)
    if (uptPthGbn.equals("D")) {
      dto.setTargetDboxId("RCY"); // 삭제처리면 타겟박스아이디가 안넘어옴, 후속로직을 위해 강제 세팅
      dto.setTargetGubun("RCY"); // 삭제처리면 타겟박스아이디가 안넘어옴, 배치작업등록시에 사용하므로 세팅
    }
    /*****************************************/
    // 타겟 Dbox id에는 프로젝트코드, 프로젝트id(r_object_id), doc(id, key), folder id, 대상cabinet
    // code, 빈값(삭제 - RCY로 앞에서 세팅)
    // 이 넘어올수 있어서 여기서 체크해서 null값이면 화면에서 어떤 예외상황이 발생한 것이라 다시 되돌려 보냄
    /*****************************************/
    if (null == dto.getTargetDboxId()) {
      // System.out.println("#MOLA# 전달된 DTO값 =" + dto.toString()+"#");
      throw new RuntimeException("붙여넣기 대상정보가 정확히 전달되지 않았습니다. 붙여넣기 경로를 다시 선택해주세요");
    }

    String targetDboxId = dto.getTargetDboxId();
    boolean isShare = false;

    if (DfId.isObjectId(targetDboxId)) {
      //TODO:(2022.02.23. 복사/이동/삭제 인수인계 정리) 현재 사용안하고 밑에서 처리
      if (dto.getTargetGubun().equals("DFO")) {
        List<AuthShare> lstAuthShare = authShareDao.selectList(dto.getTargetDboxId()); // 공유협업 폴더인지확인하는 용도
        if (lstAuthShare.size() > 0) {
          // 공유협업폴더로 이동하는 경우
          System.out.println("공유 협업폴더로 이동하는 경우");
          isShare = true;
        }
      }
    }
    if (targetDboxId.substring(0, 1).equals("d")) { // ??
      dto.setTgCabinetcode(targetDboxId);
      targetDboxId = gwDeptService.selectOrgIdByCabinetcode(targetDboxId);
    }

    String ps_Del = " ";// uptPthGbn.equals("C")?" ":null; //복사일 때는 삭제되지 않은것만 대상으로 복사해가자
    String targetGubun = dto.getTargetGubun(); // 타겟의 노드구분 ex) DPC(문서함: 카테고리), DFO(문서함 폴더)
    String s_TargetFolType = gotoMap.get(targetGubun); // 복사대상폴더구분(edms_folder의 u_fol_type)
    String tgCabinetCode = "";

    boolean gwonhan = false; // 권한체크
//이동,복사할 타겟쪽 권한 체크

    /*****************************************/
    // 타겟에 대한 정보를 DTO에 담아서, 후속 처리시 판단의 근거로 삼는다
    // ( 프로젝트코드나 프로젝트id, 연구과제코드, 연구과제id가 넘어올 수도 있고,
    // 문서함, 프로젝트함, 연구과제함의경우 타겟이 root인 경우 보안레벨을 T 기본값으로 지정해서
    // 권한비교/관리에 이상이 없도록 세팅해준다)
    /*****************************************/
    if (uptPthGbn.equals("C") || uptPthGbn.equals("M")) { // 이동, 복사시에는 만들어질 타겟쪽 폴더타입을 미리 결정해준다.

      if (null != s_TargetFolType) { // 타겟으로 카테고리나 edms_project, edms_research항목이 선택되었을 경우

        dto.setTargetFolType(s_TargetFolType); // 카테고리 폴더이면 edms_folder타입으로 변환해준다
        if (targetGubun.equals("PJC") || targetGubun.equals("PIF")) {// 주관프로젝트함내 프로젝트가 타겟이거나, 완료함내 프로젝트가 타겟인
                                                                     // 경우(edms_project)
          // 프로젝트 정보 조회
          Optional<Project> pjtOpt = null;
          if (targetDboxId.substring(0, 1).equals("p"))
            pjtOpt = projectDao.selectOneByUPjtCode(targetDboxId);
          else
            pjtOpt = projectDao.selectOne(targetDboxId);
          if (pjtOpt.isPresent()) {
            String sTgOrgId = gwDeptService.selectOrgIdByCabinetcode(pjtOpt.get().getUCabinetCode());
            dto.setTgOrgId(sTgOrgId);
            dto.setPrCode(pjtOpt.get().getUPjtCode());
            dto.setPrType("P");
            gwonhan = authService.isRootAuthenticated(HamType.findByValue("P"), pjtOpt.get().getUPjtCode(), userId); // 타겟
                                                                                                                     // 부서에
                                                                                                                     // 대한
                                                                                                                     // 프로젝트
                                                                                                                     // 생성권한이
                                                                                                                     // 있는지
                                                                                                                     // 확인
            dto.setTgCabinetcode(pjtOpt.get().getUCabinetCode()); // 타겟 부서함코드
            dto.setTargetSecLevel(pjtOpt.get().getUSecLevel()); // 보안등급
          } else {
            String sTgOrgId = gwDeptService.selectOrgIdByCabinetcode(dto.getTgCabinetcode());
            dto.setTgOrgId(sTgOrgId);
            gwonhan = authService.isRootAuthenticated(HamType.findByValue("M"), sTgOrgId, userId); // 타겟 부서에 대한 연구과제
                                                                                                   // 생성권한이 있는지 확인

            dto.setTargetSecLevel("T"); // 보안등급
          }
        } else if (targetGubun.equals("RSC") || targetGubun.equals("RIC")) {// 주관연구과제 연구과제나 참여연구과제 연구과제가 타겟인
                                                                            // 경우(edms_research)
          // 연구/투자 정보 조회

          Optional<Research> researchOpt = null;
          if (targetDboxId.substring(0, 1).equals("r"))
            researchOpt = researchDao.selectOneByURschCode(targetDboxId);
          else
            researchOpt = researchDao.selectOne(targetDboxId);

          if (researchOpt.isPresent()) {
            String sTgOrgId = gwDeptService.selectOrgIdByCabinetcode(researchOpt.get().getUCabinetCode());
            dto.setTgOrgId(sTgOrgId);
            gwonhan = authService.isRootAuthenticated(HamType.findByValue("R"), researchOpt.get().getURschCode(),
                userId); // 타겟 부서에 대한 연구과제 생성권한이 있는지 확인

            dto.setPrCode(researchOpt.get().getURschCode());
            dto.setPrType("R");

            dto.setTgCabinetcode(researchOpt.get().getUCabinetCode()); // 타겟 부서함코드
            dto.setTargetSecLevel(researchOpt.get().getUSecLevel()); // 보안등급
          } else {
            String sTgOrgId = gwDeptService.selectOrgIdByCabinetcode(dto.getTgCabinetcode());
            dto.setTgOrgId(sTgOrgId);
            gwonhan = authService.isRootAuthenticated(HamType.findByValue("M"), sTgOrgId, userId); // 타겟 부서에 대한 연구과제
                                                                                                   // 생성권한이 있는지 확인

            dto.setTargetSecLevel("T"); // 보안등급
          }

        } else {

          if (null != s_TargetFolType)
            gwonhan = authService.isRootAuthenticated(HamType.findByValue("M"), targetDboxId, userId); // DPC, PJT나 PIN,
                                                                                                       // RSC 같은 최상위
                                                                                                       // 카테고리를 선택했을 때,
          else
            gwonhan = authService.checkFolderAuth(targetDboxId, userId, "D");// GrantedLevels.WRITE.getLabel());

          VDept dept = gwDeptService.selectDeptByOrgId(targetDboxId);
          tgCabinetCode = dept.getUCabinetCode();
          dto.setTgCabinetcode(tgCabinetCode);
        }
        if (!gwonhan) { // 이동시 대상 폴더에 쓰기권한이 없는 경우
          // return OK("권한이 부족합니다");
          throw new RuntimeException("권한이 부족합니다");
        }
      }
      // 타겟으로 edms_folder에서 관리하는 폴더를 선택한 경우,
      else {
        s_TargetFolType = targetGubun;
        dto.setTargetFolType(s_TargetFolType);

        Optional<Folder> optFolder = folderService.selectOne(targetDboxId);
        if (optFolder.isPresent()) {
          dto.setTgCabinetcode(optFolder.get().getUCabinetCode()); // 타겟 부서함코드
          dto.setTargetSecLevel(optFolder.get().getUSecLevel()); // 보안등급

          if (optFolder.get().getUFolStatus().equals("C")) {
            System.out.println(" 잠긴 폴더('" + optFolder.get().getUFolName() + "') 로는 붙여넣기 할 수 없습니다");
            throw new RuntimeException(" 잠긴 폴더('" + optFolder.get().getUFolName() + "') 로는 붙여넣기 할 수 없습니다");
          }

          if (targetGubun.equals(optFolder.get().getUFolType())) { // 확인중.....
            gwonhan = authService.checkFolderAuth(targetDboxId, userId, "D");// GrantedLevels.WRITE.getLabel());
            if (!gwonhan) {
              // return OK("권한이 부족합니다");
              throw new RuntimeException("권한이 부족합니다");
            }
          } else {
            throw new RuntimeException("타겟 폴더유형이 요청한 내용과 일치하지 않습니다");// 화면에서 넘어온 복사대상폴더구분과 현재 확인한 폴더 유형이 다를때
          }
          dto.setPrCode(optFolder.get().getUPrCode()); // 타겟의 프로젝트/연구과제 코드
          dto.setPrType(optFolder.get().getUPrType()); // 타겟의 프로젝트/연구과제 타입

        } else {
          throw new RuntimeException("옮겨갈 대상의 정보가 변경되었습니다");
        }
      }
//이관할 타겟쪽 권한 체크
    } else if (uptPthGbn.equals("T")) {// 이관시 옮겨갈 부서에 대한 권한 체크
      // 이관 부서 권한 체크시에는 부서-부서코드,사용자id를 넘김
      gwonhan = authService.isRootAuthenticated(HamType.findByValue("M"), targetDboxId, userId); // DPC, PJT나 PIN, RSC
                                                                                                 // 같은 최상위 카테고리를 선택했을 때,
      if (!gwonhan) {
        // return OK("이관 부서에 대한 폴더 생성 권한이 없습니다\n권한을 받으신 후 재작업 해주세요");
        throw new RuntimeException("이관 부서에 대한 폴더 생성 권한이 없습니다\\n권한을 받으신 후 재작업 해주세요");
      } else {
        Optional<Research> researchOpt = researchDao.selectOne(targetDboxId);
        if (researchOpt.isPresent()) {
          dto.setTgCabinetcode(researchOpt.get().getUCabinetCode()); // 타겟 부서함코드
          dto.setTargetSecLevel(researchOpt.get().getUSecLevel()); // 보안등급
        }
      }
    }

    String sourceGubun = null;

    ////////////////////////////////////////////// 선택된 ....
    List<String> sFolArray = dto.getSourceFolders(); // edms_folder에서 관리되는 대상 폴더들
    List<String> sFilArray = dto.getSourceFiles(); // edms_doc에 존재하는 대상 파일들
    List<String> sPjtArray = dto.getSourcePjts(); // edms_project에 존재하는 대상 프로젝트들
    List<String> sRscsArray = dto.getSourceRscs(); // edms_research에 존재하는 대상 연구과제들

    ////////////////////////////////////////////// end
    String comOrgId = userSession.getUser().getComOrgId();
    EntCode entCode = EntCode.valueOf(comOrgId);
    Map<String, String> limitMap = codeService.getConfigDocHandleLimitMap(entCode);
    /*
     * -전자결재,프로젝트/투자, 참여, 연구과제,공유/협업,반출함,휴지통,조직 : 불가능, dialog [''로는 이동할 수 없습니다.]
     * -부서함, 주관, 완료함: OK 부서함:이동 주관:프로젝트 등록 완료함:프로젝트 완료(해당 폴더가 완료함 하위로 이동(그 외 속성 유지))
     */

    /** 복사/이동/이관/삭제 구분별 제한 건수 조회 */
    /********************************************************************************************************************************/
    String limitCnt = uptPthGbn.equals("C") ? limitMap.get("COPY").toString()
        : (uptPthGbn.equals("M") ? limitMap.get("MOVE").toString()
            : (uptPthGbn.equals("D") ? limitMap.get("DEL").toString()
                : (uptPthGbn.equals("T") ? limitMap.get("TRANS").toString() : "")));
    /********************************************************************************************************************************/

    // Mola.1 .
    // limitCnt="5"; //배치작업 등록 테스트시 사용할 인자값
    // 테스트목적으로 제한: limitCnt=1+"";

    boolean isDocImp = false; // 즁요문서
    boolean isShareDoc = false; // 공유협업
    boolean isOrgDoc = false; // 조직함문서

    String resultMsg = "";// 결과 메시지 (case: 체크결과나 Exception, Batch등록 메시지, 정상처리결과 )

    // 하위에 폴더/문서 전체 건수 확인
    String returnMsg = "";
    int totPjtCnt = 0;
    int totRscCnt = 0;
    int totFolCnt = 0;
    int totFilCnt = 0;
    int totFilLStatusCnt = 0;
    int totFilCStatusCnt = 0;
    int totFilKStatusCnt = 0;

    String authExclusive = "NO"; // 권한 있는 내역을 제외하지 않음

    List<String> folList = new ArrayList<String>();
    Map<String, Map<String, List<DPath>>> uFolMapTot = new HashMap<String, Map<String, List<DPath>>>();
    Map<String, Map<String, List<DPath>>> uFolMapCopyTot = new HashMap<String, Map<String, List<DPath>>>();

    Map<String, List<DPath>> uFolMap = new HashMap<String, List<DPath>>();
    Map<String, List<DPath>> uFolMapCopy = new HashMap<String, List<DPath>>(); // 조직함으로 이동시 live문서들은 복사처리를 먼저 수행한 이후에
                                                                               // uFolMap으로 이동처리를 수행한다.
                                                                               // 같은 폴더명인 경우, 폴더함침으로 처리되므로 이동시 소스쪽 폴더가
                                                                               // 무너지지 않게됨
    List<DPath> docList = new ArrayList<DPath>();

    dto.setReqStatus(dto.getUptPthGbn().equals("T") ? "S" : "R"); // R:요청중, S:승인없이 이관'

    boolean isChkDocLive = false;

    if (dto.getUptPthGbn().equals("M")) {
      if (s_TargetFolType.equals("CFO") || s_TargetFolType.equals("MFO")) {
        // 조직함으로의 이동은 처리는 복사 -> 이동 순서로 처리한다.
        isChkDocLive = true;
      }
    }

    if (sPjtArray.size() > 0) {
      String s_FolId = null;
      String s_ProCode = null;

      if ((dto.getTargetGubun().equals("DFO") || dto.getTargetGubun().equals("DPC"))
          && (dto.getUptPthGbn().equals("M") || dto.getUptPthGbn().equals("C"))) {
        throw new RuntimeException("프로젝트단위로 부서함으로의 이동/복사는 허용되지 않습니다");
      }
      if (dto.getTargetGubun().substring(0, 1).equals("R")
          && (dto.getUptPthGbn().equals("M") || dto.getUptPthGbn().equals("C"))) {
        throw new RuntimeException("프로젝트단위로 연구과제로의 이동/복사는 허용되지 않습니다");
      }
      try {
        for (int i = 0; i < sPjtArray.size(); i++) {
          if (sPjtArray.get(i).equals(""))
            continue;

          if (dto.getUptPthGbn().equals("M") && sPjtArray.get(i).equals(dto.getTargetDboxId())) {
            throw new RuntimeException("이동경로로 같은 프로젝트를 지정할 수 없습니다");
          }

          dto.setSourceGubun("PJT");
          Optional<Project> pjtOpt = projectDao.selectOneByUPjtCode(sPjtArray.get(i));
          if (pjtOpt.isPresent()) {

            if (null == dto.getSrcCabinetcode() || dto.getSrcCabinetcode().equals("")) {
              dto.setSrcCabinetcode(pjtOpt.get().getUCabinetCode());
            }
            if (dto.getUptPthGbn().equals("D")) {
              final boolean isOwnDept = userSession.getUser().getDeptCabinetcode().equals(dto.getSrcCabinetcode());
              final boolean isChief = Objects.equals(userId, pjtOpt.get().getUChief());
              // 권한 확인
              if (!isOwnDept && !isChief)
                throw new RuntimeException("프로젝트에 대한 삭제권한이 없습니다.");
            }

            if (isChkDocLive) {
              List<DPath> NAChkList = pathService.selectNAList(null, pjtOpt.get().getUPjtCode(), userId, authExclusive,
                  ps_Del, "L", dto.getUptPthGbn()); // Live문서(복사처리용)
              int nCnt = 0;
              for (int k = 0; k < NAChkList.size(); k++) {
                if (NAChkList.get(k).getUSecLevel().equals("G") && !NAChkList.get(k).getUSecLevel().equals("C"))
                  nCnt++;
              }
              if (nCnt > 0) {
                resultMsg += " 이동하려는 프로젝트내에 사내,그룹사내 둥급이 아닌 문서가 포함되어있습니다";
                break;
              }

              uFolMap = NAChkList.stream().collect(Collectors.groupingBy(item -> item.getUFolId())); // 업폴더ID, 하위내역으로 정리
              totPjtCnt++;
              for (int li = 0; li < NAChkList.size(); li++) {
                if (NAChkList.get(li).getListType().equals("FOL"))
                  totFolCnt++;
                else if (NAChkList.get(li).getListType().equals("DOC"))
                  totFilCnt++;
                if (NAChkList.get(li).getUObjStatus().equals("L"))
                  totFilLStatusCnt++;
                if (NAChkList.get(li).getUObjStatus().equals("C"))
                  totFilCStatusCnt++;
                if (NAChkList.get(li).getUObjStatus().equals("K"))
                  totFilKStatusCnt++;

              }

              List<DPath> NAChkListCopy = pathService.selectNAList(null, pjtOpt.get().getUPjtCode(), userId,
                  authExclusive, ps_Del, "C", dto.getUptPthGbn());
              int docCnt = 0;
              for (int k = 0; k < NAChkListCopy.size(); k++) {
                if (NAChkListCopy.get(k).getListType().equals("DOC"))
                  docCnt++;
                else {
                  if (null != uFolMap.get(NAChkListCopy.get(k).getRObjectId()))
                    totPjtCnt--; // 폴더는 중복되므로 count에서 제외
                }
              }
              if (docCnt > 0) {
                uFolMapCopy = NAChkListCopy.stream().collect(Collectors.groupingBy(item -> item.getUFolId())); // 업폴더ID,
                                                                                                               // 하위내역으로
                                                                                                               // 정리
                uFolMapCopyTot.put("P" + sPjtArray.get(i), uFolMapCopy);
              }
              // totPjtCnt += NAChkListCopy.size();

            } else {
              totPjtCnt++;
              List<DPath> NAChkList = pathService.selectNAList(null, pjtOpt.get().getUPjtCode(), userId, authExclusive,
                  ps_Del, null, dto.getUptPthGbn());
              uFolMap = NAChkList.stream().collect(Collectors.groupingBy(item -> item.getUFolId())); // 업폴더ID, 하위내역으로 정리

              uFolMapTot.put("P" + sPjtArray.get(i), uFolMap);
              for (int li = 0; li < NAChkList.size(); li++) {
                if (NAChkList.get(li).getListType().equals("FOL"))
                  totFolCnt++;
                else if (NAChkList.get(li).getListType().equals("DOC"))
                  totFilCnt++;
                if (NAChkList.get(li).getUObjStatus().equals("L"))
                  totFilLStatusCnt++;
                if (NAChkList.get(li).getUObjStatus().equals("C"))
                  totFilCStatusCnt++;
                if (NAChkList.get(li).getUObjStatus().equals("K"))
                  totFilKStatusCnt++;

              }
              // totPjtCnt += NAChkList.size();
            }
            // if( !dto.getUptPthGbn().equals("D") && !dto.getUptPthGbn().equals("C")) {
            dto = pathService.getCheckList(userSession, uptPthGbn, null, sPjtArray.get(i), isMobile, dto);
            List<String> result = dto.getReturnStr();
            // 0:결과코드, 1:건수, 3:에러문자열 순서의 리스트
            if (result.get(0).equals("500")) {
              resultMsg += result.get(2);
            } // 결과 메시지를 합칩니다
            // }
          } else {
            throw new RuntimeException(" 유효하지 않은 프로젝트id입니다(" + sPjtArray.get(i) + ")");
          }
        }
      } catch (RuntimeException e) {
        // e.printStackTrace();
        throw new RuntimeException(e.getMessage());
      }
    }
    if (sRscsArray.size() > 0) {
      try {
        if ((dto.getTargetGubun().equals("DFO") || dto.getTargetGubun().equals("DPC"))
            && (dto.getUptPthGbn().equals("M") || dto.getUptPthGbn().equals("C"))) {
          throw new RuntimeException("연구투자단위로 부서함으로의 이동/복사는 허용되지 않습니다");
        }

        if (dto.getTargetGubun().substring(0, 1).equals("P")
            && (dto.getUptPthGbn().equals("M") || dto.getUptPthGbn().equals("C"))) {
          throw new RuntimeException("연구투자단위로 프로젝트로의 이동/복사는 허용되지 않습니다");
        }

        String s_FolId = null;
        String s_RscsCode = null;
        for (int i = 0; i < sRscsArray.size(); i++) {
          if (sRscsArray.get(i).equals(""))
            continue;

          if (dto.getUptPthGbn().equals("M") && sRscsArray.get(i).equals(dto.getTargetDboxId())) {
            throw new RuntimeException("이동경로로 같은 연구과제를 지정할 수 없습니다");
          }

          dto.setSourceGubun("RSC");
          Optional<Research> researchOpt = researchDao.selectOneByURschCode(sRscsArray.get(i));
          // Optional<Research> researchOpt = researchDao.selectOne(sRscsArray.get(i));
          if (researchOpt.isPresent()) {

            if (null == dto.getSrcCabinetcode() || dto.getSrcCabinetcode().equals("")) {
              dto.setSrcCabinetcode(researchOpt.get().getUCabinetCode());
            }

            if (dto.getUptPthGbn().equals("D")) {
              final boolean isOwnDept = userSession.getUser().getDeptCabinetcode().equals(dto.getSrcCabinetcode());
              final boolean isChief = Objects.equals(userId, researchOpt.get().getUChief());
              // 권한 확인
              if (!isOwnDept && !isChief)
                throw new RuntimeException("연구과제에 대한 삭제권한이 없습니다.");
            }

            if (isChkDocLive) {
              List<DPath> NAChkList = pathService.selectNAList(null, researchOpt.get().getURschCode(), userId,
                  authExclusive, ps_Del, "L", dto.getUptPthGbn()); // Live문서(복사처리용)
              int nCnt = 0;
              for (int k = 0; k < NAChkList.size(); k++) {
                if (NAChkList.get(k).getUSecLevel().equals("G") && !NAChkList.get(k).getUSecLevel().equals("C"))
                  nCnt++;
              }
              if (nCnt > 0) {
                resultMsg += " 이동하려는 연구과제내에 사내,그룹사내 등급이 아닌 문서가 포함되어있습니다";
                break;
              }
              uFolMap = NAChkList.stream().collect(Collectors.groupingBy(item -> item.getUFolId())); // 업폴더ID, 하위내역으로 정리
              totPjtCnt++;// = NAChkList.size();
              for (int li = 0; li < NAChkList.size(); li++) {
                if (NAChkList.get(li).getListType().equals("FOL"))
                  totFolCnt++;
                else if (NAChkList.get(li).getListType().equals("DOC"))
                  totFilCnt++;
                if (NAChkList.get(li).getUObjStatus().equals("L"))
                  totFilLStatusCnt++;
                if (NAChkList.get(li).getUObjStatus().equals("C"))
                  totFilCStatusCnt++;
                if (NAChkList.get(li).getUObjStatus().equals("K"))
                  totFilKStatusCnt++;

              }

              List<DPath> NAChkListCopy = pathService.selectNAList(null, researchOpt.get().getURschCode(), userId,
                  authExclusive, ps_Del, "C", dto.getUptPthGbn());
              int docCnt = 0;
              for (int k = 0; k < NAChkListCopy.size(); k++) {
                if (NAChkListCopy.get(k).getListType().equals("DOC"))
                  docCnt++;
                else {
                  if (null != uFolMap.get(NAChkListCopy.get(k).getRObjectId()))
                    totPjtCnt--; // 폴더는 중복되므로 count에서 제외
                }
              }
              if (docCnt > 0) {
                uFolMapCopy = NAChkListCopy.stream().collect(Collectors.groupingBy(item -> item.getUFolId())); // 업폴더ID,
                                                                                                               // 하위내역으로
                                                                                                               // 정리
                uFolMapCopyTot.put("R" + sRscsArray.get(i), uFolMapCopy);
              }
              // totPjtCnt += NAChkListCopy.size();

            } else {
              List<DPath> NAChkList = pathService.selectNAList(null, researchOpt.get().getURschCode(), userId,
                  authExclusive, ps_Del, null, dto.getUptPthGbn());
              uFolMap = NAChkList.stream().collect(Collectors.groupingBy(item -> item.getUFolId())); // 업폴더ID, 하위내역으로 정리
              uFolMapTot.put("R" + sRscsArray.get(i), uFolMap);
              totRscCnt++;// = NAChkList.size();
              for (int li = 0; li < NAChkList.size(); li++) {
                if (NAChkList.get(li).getListType().equals("FOL"))
                  totFolCnt++;
                else if (NAChkList.get(li).getListType().equals("DOC"))
                  totFilCnt++;
                if (NAChkList.get(li).getUObjStatus().equals("L"))
                  totFilLStatusCnt++;
                if (NAChkList.get(li).getUObjStatus().equals("C"))
                  totFilCStatusCnt++;
                if (NAChkList.get(li).getUObjStatus().equals("K"))
                  totFilKStatusCnt++;

              }

            }
            // if(!dto.getUptPthGbn().equals("D") && !dto.getUptPthGbn().equals("C")) {
            dto = pathService.getCheckList(userSession, uptPthGbn, null, sRscsArray.get(i), isMobile, dto);
            List<String> result = dto.getReturnStr();
            // 0:결과코드, 1:건수, 3:에러문자열 순서의 리스트
            if (result.get(0).equals("500")) {
              resultMsg += result.get(2);
            } // 결과 메시지를 합칩니다
            // }
          } else {
            throw new RuntimeException(" 유효하지 않은 연구과제 id입니다(" + sRscsArray.get(i) + ")");
          }
        }
      } catch (RuntimeException e) {
        // e.printStackTrace();
        throw new RuntimeException(e.getMessage());
      }
    }
    // resultMsg="test";
    // totRscCnt
    boolean isFolder = false;
    boolean isPCLRCLDel = false; // PCL, RCL분류폴더 삭제여부

    if (sFolArray.size() > 0) {
      String s_FolId = null;
      String s_ProCode = null;
      try {
        for (int i = 0; i < sFolArray.size(); i++) {
          if (sFolArray.get(i).equals(""))
            continue;

          if (dto.getUptPthGbn().equals("M") && sFolArray.get(i).equals(dto.getTargetDboxId())) {
            throw new RuntimeException("이동경로로 같은 폴더를 지정할 수 없습니다");
          }
          Optional<Folder> optFolder = folderService.selectOne(sFolArray.get(i));
          if (optFolder.isPresent()) {
            //TODO:(2022.02.23. 복사/이동/삭제 인수인계 정리) 분류함은 권한체크 안함
            // if(dto.getUptPthGbn().equals("D")) {
            // boolean b_Auth = authService.checkFolderAuth(sFolArray.get(i), userId,
            // "D");//GrantedLevels.WRITE.getLabel());
            // if(!b_Auth) throw new RuntimeException( "'"+ optFolder.get().getUFolName()+"'
            // 는 삭제권한이 없는 폴더입니다");
            // }
            sourceGubun = optFolder.get().getUFolType();

            boolean b_Auth = authService.checkFolderAuth(sFolArray.get(i), userId, "D");
            if (!dto.getUptPthGbn().equals("C") && !b_Auth) {
              if (!sourceGubun.equals("PCL") && !sourceGubun.equals("RCL"))
                throw new RuntimeException("쓰기권한이 없는 폴더입니다");
            }

            isFolder = true;

            // sourceGubun = optFolder.get().getUFolType();
            // 분류폴더 삭제건
            if (uptPthGbn.equals("D") && sourceGubun.equals("PCL")) {
              if (userSession.getUser().getDeptCabinetcode().equals(optFolder.get().getUCabinetCode())) {
                //
                IDfSession idfSession = DCTMUtils.getAdminSession();
                try {
                  int i_Cnt = DCTMUtils.getCountByDQL(idfSession,
                      " edms_project where u_fol_id='" + sFolArray.get(i) + "' and u_finish_yn='Y' ");
                  int i_FolCnt = DCTMUtils.getCountByDQL(idfSession,
                      " edms_folder where u_up_fol_id='" + sFolArray.get(i) + "' ");

                  if (i_Cnt > 0 || i_FolCnt > 0) {
                    throw new RuntimeException("빈 분류함이어야 삭제가능합니다");
                  } else {
                    IDfPersistentObject idfObj = (IDfPersistentObject) idfSession.getObject(new DfId(sFolArray.get(i)));
                    idfObj.destroy(); // edms_doc, edms_doc_imp간 이동시 기존 문서를 삭제처리한다
                    isPCLRCLDel = true;
                  }
                } catch (Exception e) {
                  // e.printStackTrace();
                  throw new RuntimeException(e.getMessage());
                } finally {
                  idfSession.disconnect();
                }
              }
            } else if (uptPthGbn.equals("D") && sourceGubun.equals("RCL")) {
              if (userSession.getUser().getDeptCabinetcode().equals(optFolder.get().getUCabinetCode())) {
                IDfSession idfSession = DCTMUtils.getAdminSession();
                try {

                  int i_Cnt = DCTMUtils.getCountByDQL(idfSession,
                      " edms_research where u_fol_id='" + sFolArray.get(i) + "' and u_finish_yn='Y' ");
                  int i_FolCnt = DCTMUtils.getCountByDQL(idfSession,
                      " edms_folder where u_up_fol_id='" + sFolArray.get(i) + "' ");

                  if (i_Cnt > 0 || i_FolCnt > 0) {
                    throw new RuntimeException("빈 분류함이어야 삭제가능합니다");
                  } else {
                    IDfPersistentObject idfObj = (IDfPersistentObject) idfSession.getObject(new DfId(sFolArray.get(i)));
                    idfObj.destroy(); // edms_doc, edms_doc_imp간 이동시 기존 문서를 삭제처리한다
                    isPCLRCLDel = true;
                  }
                } catch (Exception e) {
                  // e.printStackTrace();
                  throw new RuntimeException(e.getMessage());
                } finally {
                  idfSession.disconnect();
                }
              }
            }
            //TODO:(2022.02.23. 복사/이동/삭제 인수인계 정리) 아래 경우는 예외처리 시작
            if (sourceGubun.equals("PCL") && dto.getTargetGubun().substring(0, 1).equals("D")
                && (dto.getUptPthGbn().equals("M") || dto.getUptPthGbn().equals("C"))) {
              throw new RuntimeException("프로젝트 분류폴더를 부서함으로 이동-복사할 수 없습니다");
            }
            if (sourceGubun.equals("PCL") && dto.getTargetGubun().substring(0, 1).equals("R")
                && (dto.getUptPthGbn().equals("M") || dto.getUptPthGbn().equals("C"))) {
              throw new RuntimeException("프로젝트 분류폴더를 연구과제로 이동-복사할 수 없습니다");
            }
            if (sourceGubun.equals("RCL") && dto.getTargetGubun().substring(0, 1).equals("D")
                && (dto.getUptPthGbn().equals("M") || dto.getUptPthGbn().equals("C"))) {
              throw new RuntimeException("연구투자 분류폴더를 부서함으로 이동-복사할 수 없습니다");
            }
            if (sourceGubun.equals("RCL") && dto.getTargetGubun().substring(0, 1).equals("P")
                && (dto.getUptPthGbn().equals("M") || dto.getUptPthGbn().equals("C"))) {
              throw new RuntimeException("연구과자 분류폴더를 프로젝트로 이동-복사할 수 없습니다");
            }
            if (null != optFolder.get().getUPrCode() && !optFolder.get().getUPrCode().equals("")
                && !optFolder.get().getUPrCode().equals(" ")) {
              dto.setSourcePjtCode(optFolder.get().getUPrCode()); // 소스폴더가 프로젝트 폴더일 때, 타겟이 다른 프로젝트 코드이면 복사로 처리하기 위해 세팅
            }

            if (!dto.getUptPthGbn().equals("C")) { // '잠금'처리된 폴더 내 폴더 및 문서는 '잠근 해제' 되어있는 폴더로 복사 가능하다
              if (optFolder.get().getUFolStatus().equals("C")) {
                System.out.println("'" + optFolder.get().getUFolName() + "' 은 잠긴 폴더입니다");
                throw new RuntimeException("'" + optFolder.get().getUFolName() + "' 은 잠긴 폴더입니다");
              }
            }
            if (sourceGubun.substring(0, 1).equals("D")
                && (dto.getTargetGubun().equals("PCL") || dto.getTargetGubun().equals("RCL"))
                && (dto.getUptPthGbn().equals("M") || dto.getUptPthGbn().equals("C"))) {
              throw new RuntimeException("부서함폴더를 분류폴더로 이동-복사할 수 없습니다");
            }

            dto.setSourceGubun(sourceGubun);

            if (!dto.getUptPthGbn().equals("D") && null != optFolder.get().getUPrCode()
                && !optFolder.get().getUPrCode().equals("") && !optFolder.get().getUPrCode().equals(" ")
                && (optFolder.get().getUUpFolId().equals("") || optFolder.get().getUUpFolId().equals(" "))) {
              if (optFolder.get().getUPrCode().equals(dto.getPrCode())
                  && (!dto.getTargetGubun().equals("PFO") && !dto.getTargetGubun().equals("RFO"))
                  && dto.getUptPthGbn().equals("M")) { // 이동의 경우 제한, 프로젝트루트에서 복붙할 때 방지
                throw new RuntimeException("이동이 제한된 경로입니다");
              }
            }
            if (!dto.getUptPthGbn().equals("D")
                && (null == optFolder.get().getUPrCode() || optFolder.get().getUPrCode().equals("")
                    || optFolder.get().getUPrCode().equals(" "))
                && (optFolder.get().getUUpFolId().equals("") || optFolder.get().getUUpFolId().equals(" "))) {
              if ((optFolder.get().getUPrCode().equals("") || optFolder.get().getUPrCode().equals(" "))
                  && (null == dto.getPrCode() || dto.getPrCode().equals("")) && (!dto.getTargetGubun().equals("POW")
                      && !dto.getTargetGubun().equals("ROW") && !dto.getTargetGubun().equals("DPC"))) {
                throw new RuntimeException("이동이 제한된 경로입니다");
              }
            }
            if (null == dto.getSrcCabinetcode() || dto.getSrcCabinetcode().equals("")) {
              dto.setSrcCabinetcode(optFolder.get().getUCabinetCode());
            }

            if (sourceGubun.substring(0, 1).equals("P") || sourceGubun.substring(0, 1).equals("R")) { // 프로젝트나 연구투자 폴더인
                                                                                                      // 경우

              List<DPath> NAChkList = pathService.selectNAList(sFolArray.get(i), null, userId, authExclusive, ps_Del,
                  null, dto.getUptPthGbn());
              uFolMap = NAChkList.stream().collect(Collectors.groupingBy(item -> item.getUFolId())); // 업폴더ID, 하위내역으로 정리
              uFolMapTot.put(sFolArray.get(i), uFolMap);

              for (int li = 0; li < NAChkList.size(); li++) {
                if (NAChkList.get(li).getListType().equals("FOL"))
                  totFolCnt++;
                else if (NAChkList.get(li).getListType().equals("DOC"))
                  totFilCnt++;
                if (NAChkList.get(li).getUObjStatus().equals("L"))
                  totFilLStatusCnt++;
                if (NAChkList.get(li).getUObjStatus().equals("C"))
                  totFilCStatusCnt++;
                if (NAChkList.get(li).getUObjStatus().equals("K"))
                  totFilKStatusCnt++;

              }

              // if(!dto.getUptPthGbn().equals("D") && !dto.getUptPthGbn().equals("C")) {
              dto = pathService.getCheckList(userSession, uptPthGbn, sFolArray.get(i), null, isMobile, dto);
              List<String> result = dto.getReturnStr();
              // 0:결과코드, 1:건수, 3:에러문자열 순서의 리스트
              if (result.get(0).equals("500")) {
                resultMsg += result.get(2);
              } // 결과 메시지를 합칩니다
              // }
            } else {
              totFolCnt++;

              //TODO:(2022.02.23. 복사/이동/삭제 인수인계 정리) 아래 구문은 현재 안쓰임..
              if (sourceGubun.substring(0, 1).equals("S")) {
                if (uptPthGbn.equals("C") || uptPthGbn.equals("M")) {
                  throw new RuntimeException(" 공유/협업 폴더는 이동/복사가 제한됩니다");
                }
              }
              
              isShareDoc = sourceGubun.substring(0, 1).equals("S") ? true : false; // SHR, SFO(공유협업, 공유폴더..)
              isOrgDoc = sourceGubun.substring(0, 1).equals("C") ? true
                  : (sourceGubun.substring(0, 1).equals("M") ? true : false); // (회사)CPC, CFO, (관리직)MPC, MFO...

              if (isShareDoc == true && (uptPthGbn.equals("C") || uptPthGbn.equals("M"))) {
                resultMsg += " 공유/협업 폴더는 이동/복사가 제한됩니다";
                throw new RuntimeException(" 공유/협업 폴더는 이동/복사가 제한됩니다");
                // break;
              }

              if (isChkDocLive) {
                List<DPath> NAChkList = pathService.selectNAList(null, sFolArray.get(i), userId, authExclusive, ps_Del,
                    "L", dto.getUptPthGbn()); // Live문서(복사처리용)
                int nCnt = 0;
                for (int k = 0; k < NAChkList.size(); k++) {
                  if (NAChkList.get(k).getUSecLevel().equals("G") && !NAChkList.get(k).getUSecLevel().equals("C"))
                    nCnt++;
                }
                if (nCnt > 0) {
                  resultMsg += " 이동하려는 폴더내에 사내,그룹사내 둥굽이 아닌 문서가 포함되어있습니다";
                  throw new RuntimeException(" 이동하려는 폴더내에 사내,그룹사내 둥굽이 아닌 문서가 포함되어있습니다");
//							    	break;
                }
                uFolMap = NAChkList.stream().collect(Collectors.groupingBy(item -> item.getUFolId())); // 업폴더ID, 하위내역으로
                                                                                                       // 정리
                totPjtCnt += NAChkList.size();

                List<DPath> NAChkListCopy = pathService.selectNAList(null, sFolArray.get(i), userId, authExclusive,
                    ps_Del, "C", dto.getUptPthGbn());
                int docCnt = 0;
                for (int k = 0; k < NAChkListCopy.size(); k++) {
                  if (NAChkListCopy.get(k).getListType().equals("DOC"))
                    docCnt++;
                  else {
                    if (null != uFolMap.get(NAChkListCopy.get(k).getRObjectId()))
                      totPjtCnt--; // 폴더는 중복되므로 count에서 제외
                  }
                }
                if (docCnt > 0) {
                  uFolMapCopy = NAChkListCopy.stream().collect(Collectors.groupingBy(item -> item.getUFolId())); // 업폴더ID,
                                                                                                                 // 하위내역으로
                                                                                                                 // 정리
                  uFolMapCopyTot.put(sFolArray.get(i), uFolMapCopy);
                }
                totPjtCnt += NAChkListCopy.size();
              } else {

                List<DPath> NAChkList = pathService.selectNAList(sFolArray.get(i), null, userId, authExclusive, ps_Del,
                    null, dto.getUptPthGbn()); // 하위(전체)에 권한 모든 폴더나 문서 리스트를 조회
                uFolMap = NAChkList.stream().collect(Collectors.groupingBy(item -> item.getUFolId())); // 업폴더ID, 하위내역으로
                                                                                                       // 정리
                uFolMapTot.put(sFolArray.get(i), uFolMap);

                for (int li = 0; li < NAChkList.size(); li++) {
                  if (NAChkList.get(li).getListType().equals("FOL"))
                    totFolCnt++;
                  else if (NAChkList.get(li).getListType().equals("DOC"))
                    totFilCnt++;
                  if (NAChkList.get(li).getUObjStatus().equals("L"))
                    totFilLStatusCnt++;
                  if (NAChkList.get(li).getUObjStatus().equals("C"))
                    totFilCStatusCnt++;
                  if (NAChkList.get(li).getUObjStatus().equals("K"))
                    totFilKStatusCnt++;

                }
                // totFolCnt += NAChkList.size();
              }
              // if(!dto.getUptPthGbn().equals("D") && !dto.getUptPthGbn().equals("C")) {
              dto = pathService.getCheckList(userSession, uptPthGbn, sFolArray.get(i), null, isMobile, dto);
              List<String> result = dto.getReturnStr();

              // 0:결과코드, 1:건수, 3:에러문자열 순서의 리스트
              if (result.get(0).equals("500")) {
                resultMsg += result.get(2);
              } // 결과 메시지를 합칩니다
              // }
            }
          } else {
            throw new RuntimeException(" 유효하지 않은 폴더 id입니다");
          }
        }
      } catch (RuntimeException e) {
        // e.printStackTrace();
        throw new RuntimeException(e.getMessage());
      } finally {

      }
    }

    if (isPCLRCLDel) {
      body.put("flag", lsFlag);
      body.put("status", "200");
      body.put("message", "분류폴더를 삭제하였습니다");
      return OK(body);
    }

    System.out.println("==" + resultMsg);
    if (null != dto.getSourceGubun() && dto.getUptPthGbn().equals("M")) {
      if (null != dto.getPrCode() && !dto.getPrCode().equals("") && !dto.getPrCode().equals(" ")
          && null != dto.getSourcePjtCode() && !dto.getSourcePjtCode().equals("")
          && !dto.getSourcePjtCode().equals(" ")) {
        if (dto.getSourceGubun().equals("PFO") && dto.getTargetFolType().equals("PFO")
            && dto.getUptPthGbn().equals("M")) {
          if (!dto.getPrCode().equals(dto.getSourcePjtCode())) {
            dto.setUptPthGbn("C");
            uptPthGbn = "C";
            lsFlag = "MP";
          }
        } else if (dto.getSourceGubun().equals("RFO") && dto.getTargetFolType().equals("RFO")
            && dto.getUptPthGbn().equals("M")) {
          if (!dto.getPrCode().equals(dto.getSourcePjtCode())) {
            dto.setUptPthGbn("C");
            uptPthGbn = "C";
            lsFlag = "MR";
          }
        }
      }
    }

    String frontStr = "),";

    List<String> sLiveFilArray = Arrays.asList(""); // 조직함으로 보낼 때
    List<String> sClosFilArray = Arrays.asList(""); // 조직함으로 보낼 때

    if (sFilArray.size() > 0) {

      IDfSession idfSession = DCTMUtils.getAdminSession();

      if (null != dto.getTargetGubun()) {
        if ((dto.getTargetGubun().equals("PCL") || dto.getTargetGubun().equals("RCL"))
            && (dto.getUptPthGbn().equals("M") || dto.getUptPthGbn().equals("C"))) {
          throw new RuntimeException("문서를 분류폴더로 이동-복사할 수 없습니다");
        }
      }
      try {
        for (int i = 0; i < sFilArray.size(); i++) {
          if (sFilArray.get(i).equals(""))
            continue;

          totFilCnt++;
          String s_ObjId = sFilArray.get(i);//
          s_ObjId = DCTMUtils.getCurrentObjectID(idfSession, s_ObjId); // 문서의 최신버전 r_object_id를 가져온다.
          if (s_ObjId.equals("")) {
            throw new NotFoundException("문서 최신버전이 변경됨[Documentum]");
          }
          IDfDocument idfDocObj = (IDfDocument) idfSession.getObject(new DfId(s_ObjId));

          if (uptPthGbn.equals("D")) { // 삭제때만 체크
            List<DPath> DTChkList = pathService.selectDTList(idfDocObj.getString("u_doc_key")); // 문서관점 : 타시스템 첨부한 문서
            if (DTChkList.size() > 0) {
              throw new RuntimeException("타시스템에 첨부된 문서입니다");

            }
            // 속도이슈일 때는 삭제해도 될것이라 생각됨. 반출함 문서여부 체크
            // String ls_TakeoutId=pathService.selectTakeoutDocsRobjectIdByDocId(s_ObjId);
            // if(null !=ls_TakeoutId && !ls_TakeoutId.equals("") && !ls_TakeoutId.equals("
            // ")) throw new RuntimeException("반출함 문서라 문서 삭제대상이 아닙니다");
          }
          if (idfDocObj.isCheckedOut()) {
            if (uptPthGbn.equals("M")) {
              throw new RuntimeException(idfDocObj.getLockOwner() + " 가 편집중인 문서입니다");
            } else {
              if (sFilArray.size() == 1 && uptPthGbn.equals("D")) {

                VUser reqMgr = userService.selectOneByUserId(idfDocObj.getLockOwner()).orElse(new VUser());
                throw new RuntimeException(reqMgr.getOrgNm() + "  " + reqMgr.getDisplayName() + " "
                    + reqMgr.getPstnName() + " 편집중 문서입니다(삭제불가)");
              }
            }
          }

          if (!isShareDoc) {

            Optional<DocImp> optDocImp = docImpService.selectOne(s_ObjId);

            int secLevel = uptPthGbn.equals("C") ? 3
                : (uptPthGbn.equals("M") ? 7 : (uptPthGbn.equals("D") ? 7 : (uptPthGbn.equals("T") ? 7 : 99999)));
            if (optDocImp.isPresent()) {

              if (dto.getUptPthGbn().equals("D")) {
                boolean b_Auth = false;
                if (optDocImp.get().getUDocStatus().equals("L"))
                  b_Auth = authService.checkDocAuth(s_ObjId, userId, 7);// GrantedLevels.WRITE.getLabel());
                else
                  b_Auth = authService.checkDocAuth(s_ObjId, userId, 3);// GrantedLevels.WRITE.getLabel());
                if (!b_Auth)
                  throw new RuntimeException("'" + optDocImp.get().getObjectName() + "' 는 삭제권한이 없는 파일입니다");
              }

              if (null == dto.getSourcePjtCode() || dto.getSourcePjtCode().equals("")
                  || dto.getSourcePjtCode().equals(" ")) {
                dto.setSourcePjtCode(optDocImp.get().getUPrCode());
                if (optDocImp.get().getUPrType().equals("P"))
                  dto.setSourceGubun("PFO");
                else if (optDocImp.get().getUPrType().equals("R"))
                  dto.setSourceGubun("RFO");

                if (null != dto.getSourceGubun() && dto.getUptPthGbn().equals("M")) {
                  if (null != dto.getPrCode() && !dto.getPrCode().equals("") && !dto.getPrCode().equals(" ")
                      && null != dto.getSourcePjtCode() && !dto.getSourcePjtCode().equals("")
                      && !dto.getSourcePjtCode().equals(" ")) {
                    if (dto.getSourceGubun().equals("PFO") && dto.getTargetFolType().equals("PFO")
                        && dto.getUptPthGbn().equals("M")) {
                      if (!dto.getPrCode().equals(dto.getSourcePjtCode())) {
                        dto.setUptPthGbn("C");
                        uptPthGbn = "C";
                        lsFlag = "MP";
                      }
                    } else if (dto.getSourceGubun().equals("RFO") && dto.getTargetFolType().equals("RFO")
                        && dto.getUptPthGbn().equals("M")) {
                      if (!dto.getPrCode().equals(dto.getSourcePjtCode())) {
                        dto.setUptPthGbn("C");
                        uptPthGbn = "C";
                        lsFlag = "MR";
                      }
                    }
                  }
                }
              }

              if (!optDocImp.get().getUDeleteStatus().equals(" ")) {// 삭제된 문서인지 체크
                resultMsg += " " + optDocImp.get().getObjectName() + " 은 삭제된 상태입니다";
              }
              if (null == dto.getSrcCabinetcode() || dto.getSrcCabinetcode().equals("")) {
                dto.setSrcCabinetcode(optDocImp.get().getUCabinetCode());
              }

              if (!dto.getUptPthGbn().equals("D") && null != optDocImp.get().getUPrCode()
                  && !optDocImp.get().getUPrCode().equals("") && !optDocImp.get().getUPrCode().equals(" ")
                  && (optDocImp.get().getUFolId().equals("") || optDocImp.get().getUFolId().equals(" "))) {
                if (optDocImp.get().getUPrCode().equals(dto.getPrCode())) {
                  throw new RuntimeException("이동이 제한된 경로입니다");
                }
              }

              if (!dto.getUptPthGbn().equals("D")
                  && (null == optDocImp.get().getUPrCode() || optDocImp.get().getUPrCode().equals("")
                      || optDocImp.get().getUPrCode().equals(" "))
                  && (optDocImp.get().getUFolId().equals("") || optDocImp.get().getUFolId().equals(" "))) {
                if ((optDocImp.get().getUPrCode().equals("") || optDocImp.get().getUPrCode().equals(" "))
                    && (null == dto.getPrCode() || dto.getPrCode().equals("") && !dto.getTargetGubun().equals("DPC"))) {
                  throw new RuntimeException("이동이 제한된 경로입니다");
                }
              }
              if (null == sourceGubun || sourceGubun.equals("")) {
                String srcFolId = optDocImp.get().getUFolId();
                if (srcFolId.trim().equals("")) {
                  if (!optDocImp.get().getUPrType().trim().equals("")) {
                    sourceGubun = optDocImp.get().getUPrType() + "FO";
                  } else {
                    sourceGubun = optDocImp.get().getUPrType() + "DFO";
                  }
                  dto.setSourceGubun(sourceGubun);
                } else {

                  Optional<Folder> optFolder = folderService.selectOne(srcFolId);
                  String srcFolType = "";
                  if (optFolder.isPresent()) {
                    // System.out.println(optFolder.get().getUFolStatus());
                    if (optFolder.get().getUFolStatus().equals("C")) {// 잠금처리된 폴더
                      if (!dto.getUptPthGbn().equals("C")) { // '잠금'처리된 폴더 내 폴더 및 문서는 '잠근 해제' 되어있는 폴더로 복사 가능하다 (복사일 때는
                                                             // 허용)
                        resultMsg += "  '" + optDocImp.get().getObjectName() + "' 은 잠긴 폴더에 있는 문서입니다";
                        throw new RuntimeException(resultMsg);
                      }
                    }
                    if (null == dto.getSourceGubun() || dto.getSourceGubun().equals("")) {
                      srcFolType = optFolder.get().getUFolType();
                      if (null != optDocImp.get().getUDeleteStatus() && !optDocImp.get().getUDeleteStatus().equals(" "))
                        srcFolType = "RCY";// 휴지통
                      dto.setSourceGubun(srcFolType);
                      sourceGubun = srcFolType;
                    } else {
                      sourceGubun = dto.getSourceGubun();
                    }
                    isDocImp = sourceGubun.equals("DIP") ? true : (sourceGubun.equals("DIM") ? true : false);
                  }
                }
              }
              isShareDoc = sourceGubun.substring(0, 1).equals("S") ? true : false; // SHR, SFO(공유협업, 공유폴더..)
              isOrgDoc = sourceGubun.substring(0, 1).equals("C") ? true
                  : (sourceGubun.substring(0, 1).equals("M") ? true : false); // (회사)CPC, CFO, (관리직)MPC, MFO...

              if (isShareDoc == true && (uptPthGbn.equals("C") || uptPthGbn.equals("M"))) {
                resultMsg += (resultMsg.equals("") ? "" : frontStr) + " 공유/협업 폴더는 이동/복사가 제한됩니다";
                break;
              }

              if (targetDboxId.substring(0, 1).equals("C") || targetDboxId.substring(0, 1).equals("M")) { // 조직함
                if (optDocImp.get().getUDocStatus().equals("C")) { // Closed문서인 경우
                  if (!optDocImp.get().getUSecLevel().equals("G") && !optDocImp.get().getUSecLevel().equals("C")) {
                    resultMsg += (resultMsg.equals("") ? "" : frontStr) + "Closed문서는 사내,그룹사내 문서만 이동할 수 있습니다";
                  }
                } else {
                  if (uptPthGbn.equals("M")) {
                    uptPthGbn = "C";
                    dto.setUptPthGbn(uptPthGbn);
                    lsFlag = "MC";
                  }
                }
              }
              if (!uptPthGbn.equals("D")) { // 삭제시에는 체크하지 않음
                List<DPath> authList = pathService.selectDocAuthCheck(s_ObjId, userId, secLevel); // secLevel 권한이상
                // 허용된 문서인지 확인
                if (authList.size() < 1) {
                  resultMsg += (resultMsg.equals("") ? "" : frontStr) + optDocImp.get().getObjectName()
                      + " 파일에 대한 권한이 없습니다 ";
                }
              }

              if (isOrgDoc && uptPthGbn.equals("M")) {
                if (optDocImp.get().getUDocStatus().equals("L"))
                  sLiveFilArray.add(s_ObjId);
                else {
                  if (optDocImp.get().getUSecLevel().equals("G") || optDocImp.get().getUSecLevel().equals("C"))
                    sClosFilArray.add(s_ObjId);
                  else
                    resultMsg += " Closed문서는 사내,그룹사내 문서만 이동가능합니다";
                }
              }
            } else {
              Optional<Doc> optDoc = docService.selectOne(s_ObjId);

              // 복사(C)일때는 3(읽기)권한 이상이면 복사가 가능하지만, 이동,삭제,이관할때는 읽기쓰기권한(7)이 필요
              secLevel = uptPthGbn.equals("C") ? 3
                  : (uptPthGbn.equals("M") ? 7 : (uptPthGbn.equals("D") ? 7 : (uptPthGbn.equals("T") ? 7 : 99999)));
              if (optDoc.isPresent()) {

                s_ObjId = DCTMUtils.getCurrentObjectID(idfSession, s_ObjId); // 문서의 최신버전 r_object_id를 가져온다.
                if (s_ObjId.equals("")) {
                  throw new NotFoundException("문서 최신버전이 변경됨[Documentum]");
                }

                if (dto.getUptPthGbn().equals("D")) {
                  boolean b_Auth = false;
                  if (optDoc.get().getUDocStatus().equals("L"))
                    b_Auth = authService.checkDocAuth(s_ObjId, userId, 7);// GrantedLevels.WRITE.getLabel());
                  else
                    b_Auth = authService.checkDocAuth(s_ObjId, userId, 3);// GrantedLevels.WRITE.getLabel());

                  if (!b_Auth) {
                    throw new RuntimeException("'" + optDoc.get().getObjectName() + "' 는 삭제권한이 없는 파일입니다");
                  }
                }
                if (null == dto.getSourcePjtCode() || dto.getSourcePjtCode().equals("")
                    || dto.getSourcePjtCode().equals(" ")) {
                  dto.setSourcePjtCode(optDoc.get().getUPrCode());
                  if (optDoc.get().getUPrType().equals("P"))
                    dto.setSourceGubun("PFO");
                  else if (optDoc.get().getUPrType().equals("R"))
                    dto.setSourceGubun("RFO");

                  if (null != dto.getSourceGubun() && dto.getUptPthGbn().equals("M")) {
                    if (null != dto.getPrCode() && !dto.getPrCode().equals("") && !dto.getPrCode().equals(" ")
                        && null != dto.getSourcePjtCode() && !dto.getSourcePjtCode().equals("")
                        && !dto.getSourcePjtCode().equals(" ")) {
                      if (dto.getSourceGubun().equals("PFO") && dto.getTargetFolType().equals("PFO")
                          && dto.getUptPthGbn().equals("M")) {
                        if (!dto.getPrCode().equals(dto.getSourcePjtCode())) {
                          dto.setUptPthGbn("C");
                          uptPthGbn = "C";
                          lsFlag = "MP";
                        }
                      } else if (dto.getSourceGubun().equals("RFO") && dto.getTargetFolType().equals("RFO")
                          && dto.getUptPthGbn().equals("M")) {
                        if (!dto.getPrCode().equals(dto.getSourcePjtCode())) {
                          dto.setUptPthGbn("C");
                          uptPthGbn = "C";
                          lsFlag = "MR";
                        }
                      }
                    }
                  }
                }
                if (!optDoc.get().getUDeleteStatus().equals(" ")) {// 삭제된 문서인지 체크
                  resultMsg += (resultMsg.equals("") ? "" : frontStr) + optDoc.get().getObjectName() + " 은 삭제된 상태입니다\n";
                }
                if (!dto.getUptPthGbn().equals("D") && null != optDoc.get().getUPrCode()
                    && !optDoc.get().getUPrCode().equals("") && !optDoc.get().getUPrCode().equals(" ")
                    && (optDoc.get().getUFolId().equals("") || optDoc.get().getUFolId().equals(" "))) {
                  if (optDoc.get().getUPrCode().equals(dto.getPrCode())
                      && (!dto.getTargetGubun().equals("PFO") && !dto.getTargetGubun().equals("RFO"))
                      && dto.getUptPthGbn().equals("M")) { // 이동의 경우 제한, 프로젝트루트에서 복붙할 때 방지
                    throw new RuntimeException("이동이 제한된 경로입니다");
                  }
                }
                if (!dto.getUptPthGbn().equals("D")
                    && (null == optDoc.get().getUPrCode() || optDoc.get().getUPrCode().equals("")
                        || optDoc.get().getUPrCode().equals(" "))
                    && (optDoc.get().getUFolId().equals("") || optDoc.get().getUFolId().equals(" "))) {
                  if ((optDoc.get().getUPrCode().equals("") || optDoc.get().getUPrCode().equals(" "))
                      && (null == dto.getPrCode() || dto.getPrCode().equals(""))
                      && !dto.getTargetGubun().equals("DPC")) {
                    throw new RuntimeException("이동이 제한된 경로입니다");
                  }
                }

                if (null == dto.getSrcCabinetcode() || dto.getSrcCabinetcode().equals("")) {
                  dto.setSrcCabinetcode(optDoc.get().getUCabinetCode());
                }
                if (null == sourceGubun || sourceGubun.equals("")) {
                  String srcFolId = optDoc.get().getUFolId();
                  if (srcFolId.trim().equals("")) {
                    if (!optDoc.get().getUPrType().trim().equals("")) {
                      sourceGubun = optDoc.get().getUPrType() + "FO";
                    } else {
                      sourceGubun = optDoc.get().getUPrType() + "DFO";
                    }
                    dto.setSourceGubun(sourceGubun);
                  } else {
                    Optional<Folder> optFolder = folderService.selectOne(srcFolId);
                    String srcFolType = "";
                    if (optFolder.isPresent()) {
                      if (optFolder.get().getUFolStatus().equals("C")) {// 잠금처리된 폴더
                        if (!dto.getUptPthGbn().equals("C")) { // '잠금'처리된 폴더 내 폴더 및 문서는 '잠근 해제' 되어있는 폴더로 복사 가능하다
                          resultMsg += "  '" + optDoc.get().getObjectName() + "' 은 잠긴 폴더에 있는 문서입니다";
                          throw new RuntimeException(resultMsg);
                        }
                      }
                      if (null == dto.getSourceGubun() || dto.getSourceGubun().equals("")) {
                        srcFolType = optFolder.get().getUFolType();
                        if (null != optDoc.get().getUDeleteStatus() && !optDoc.get().getUDeleteStatus().equals(" "))
                          srcFolType = "RCY";// 휴지통
                        dto.setSourceGubun(srcFolType);
                        sourceGubun = srcFolType;
                      } else {
                        sourceGubun = dto.getSourceGubun();
                      }
                      isDocImp = sourceGubun.equals("DIP") ? true : (sourceGubun.equals("DIM") ? true : false);
                    }
                  }
                }
                if (isShareDoc == true && (uptPthGbn.equals("C") || uptPthGbn.equals("M"))) {
                  resultMsg += (resultMsg.equals("") ? "" : frontStr) + "공유/협업 폴더는 이동/복사가 제한됩니다\n";
                  break;
                }
                if (!uptPthGbn.equals("D")) { // 삭제시에는 체크하지 않음
                  List<DPath> authList = pathService.selectDocAuthCheck(s_ObjId, userId, secLevel); // secLevel 권한이상
                  // 허용된 문서인지 확인
                  if (authList.size() < 1) {
                    resultMsg += (resultMsg.equals("") ? "" : frontStr) + optDoc.get().getObjectName()
                        + " 파일에 대한 권한이 없습니다";
                  }
                }
                if (null != sourceGubun) {
                  isOrgDoc = sourceGubun.substring(0, 1).equals("C") ? true
                      : (sourceGubun.substring(0, 1).equals("M") ? true : false); // (회사)CPC, CFO, (관리직)MPC, MFO...

                  String srcFolId = optDoc.get().getUFolId();
                  Optional<Folder> optFolder = folderService.selectOne(srcFolId);
                  String srcFolType = "";
                  if (optFolder.isPresent()) {
                    if (null == dto.getSourceGubun() || dto.getSourceGubun().equals("")) {
                      if (!optFolder.get().getUFolStatus().equals("C")) {// 잠금처리된 폴더
                        if (!dto.getUptPthGbn().equals("C")) { // '잠금'처리된 폴더 내 폴더 및 문서는 '잠근 해제' 되어있는 폴더로 복사 가능하다
                          resultMsg += "  '" + optDocImp.get().getObjectName() + "' 은 잠긴 폴더에 있는 문서입니다";
                          throw new RuntimeException(resultMsg);
                        }
                      }
                      srcFolType = optFolder.get().getUFolType();
                      if (null == optDoc.get().getUDeleteStatus()) {
                        ;
                      } else {
                        sourceGubun = "RCY";
                      }
                      ;// 휴지통
                      dto.setSourceGubun(srcFolType);
                      sourceGubun = srcFolType;
                    }
                    isDocImp = sourceGubun.equals("DIP") ? true : (sourceGubun.equals("DIM") ? true : false);
                  }
                  if (isOrgDoc && uptPthGbn.equals("M")) {
                    if (optDocImp.get().getUDocStatus().equals("L"))
                      sLiveFilArray.add(s_ObjId);
                    else {
                      if (optDocImp.get().getUSecLevel().equals("G") || optDocImp.get().getUSecLevel().equals("C"))
                        sClosFilArray.add(s_ObjId);
                      else
                        resultMsg += " Closed문서는 사내,그룹사내 문서만 이동가능합니다";
                    }
                  }
                }
              } else { // edms_doc_imp에도 edms_doc에도 없는 문서인 경우
                throw new RuntimeException(" 유효하지 않은 문서 id입니다. F5 키를 누르신 후 다시 시도해보시기 바랍니다(" + s_ObjId + ")");
              }
            }
          }
        }
      } catch (RuntimeException e) {
        // e.printStackTrace();
        throw new RuntimeException(e.getMessage());
      } finally {
        idfSession.disconnect();
      }
    }

    /*
     * if(uptPthGbn.equals("T")){ //이관일 때, 중요함에서 옮기려는 경우
     * if(sourceGubun.substring(0,2).equals("DI")) { //타겟CabinetCode로 중요문서함이 있는지 확인
     * List<DPath> diList=pathService.selectFolTypeList( dto.getTgCabinetcode(),
     * "DIM");//해당 cabinet으로 중요문서함(DIM, DIF) 폴더가 있는지 확인 if(diList.size() < 1) {
     * resultMsg+="대상 부서에 중요함 권한이 없습니다.\n이관하려는 부서에 중요함 권한을 부여받은 이후에 이관하시기 바랍니다.";
     * }else { dto.setTargetDboxId(diList.get(0).getRObjectId()); //해당부서의
     * '중요문서함(DIM타입)을 수신함코드로 지정 } //targetGubun="DIF" 로 폴더 업데이트 } }
     */
    if (null != dto.getSourceGubun()) {
      if (dto.getSourceGubun().equals("EXP")) {
        resultMsg += (resultMsg.equals("") ? "" : frontStr) + "반출함에서 잘못 선택되었습니다";
      } else if (dto.getSourceGubun().equals("RCY")) {
        resultMsg += (resultMsg.equals("") ? "" : frontStr) + "휴지통에서 잘못 선택되었습니다";
      }
    }

    if (uptPthGbn.equals("C") || uptPthGbn.equals("M")) {
      if (null != dto.getSrcCabinetcode() && null != dto.getTgCabinetcode()) {
        if (!dto.getSrcCabinetcode().equals(dto.getTgCabinetcode())) {
          String sTgOrgId = gwDeptService.selectOrgIdByCabinetcode(dto.getTgCabinetcode());
          dto.setTgOrgId(sTgOrgId);

          myDept = gwDeptService.selectDeptByOrgId(userSession.getUser().getOrgId());
          dto.setUComOrgCabinetCd(gwDeptService.selectDeptByOrgId(myDept.getComOrgId()).getUCabinetCode());
          dto.setUGroupOrgCabinetCd(gwDeptService.selectDeptByOrgId("DKG").getUCabinetCode());
          dto.setUApprover(myDept.getManagerPerId()); // 승인자 세팅

          if (uptPthGbn.equals("M")) {
            uptPthGbn = "C";
            dto.setUptPthGbn(uptPthGbn);
            lsFlag = "MC";
          }
        }
      }
    }

    String s_PrCodeCheck = pjtMap.get(targetGubun);
    /** 프로젝트나 연구과제를 생성해야하는 대상인지 여부 */
    if ((dto.getSourceGubun().equals("DFO") || dto.getSourceGubun().equals("PFO") || dto.getSourceGubun().equals("RFO"))
        && (dto.getSourceGubun().equals("DFO") || dto.getSourceGubun().equals("PFO")
            || dto.getSourceGubun().equals("RFO"))
        && (!dto.getTargetGubun().equals("POW") && !dto.getTargetGubun().equals("ROW"))) {
      ;
    } else {
      dto.setUPrCodeCheck(s_PrCodeCheck);
    }

    String jobStr = dto.getUptPthGbn().equals("M") ? "이동"
        : (dto.getUptPthGbn().equals("C") ? "복사" : (dto.getUptPthGbn().equals("T") ? "이관" : "삭제"));

    if (resultMsg.equals("")) {
      if (Integer.parseInt(limitCnt) < (totPjtCnt + totRscCnt + totFolCnt + totFilCnt)) { // 제한건수를 초과한 경우
        if (isDocImp) {
          resultMsg = "대상 건수가  폴더:" + totFolCnt + " 건 \n " + " 파일 :" + totFilCnt + " 건 입니다 \n 기준값 이하로 나누어 처리하시기 바랍니다.";
        } else {
          /**
           * TO_Do - 폐기요청 : edms_req_delete (삭제처리문서중 closed 문서 ) - 이관 : 이관:
           * edms_req_trans_user, edms_req_trans_user_doc 이동,복사,삭제 : edms_batch_mcd,
           * edms_batch_mcd_objs
           **/
          if (dto.getUptPthGbn().equals("T")) {
            pathService.addTransBatchObjects(userSession, dto);

          } else {

            String sCabinetType = "";
            for (int i = 0; i < sPjtArray.size(); i++) {
              pathService.updateDeleteStatus(userSession, sPjtArray.get(i), "Y"); // 프로젝트(edms_project) 나
                                                                                  // 연구/투자(edms_research) 의
                                                                                  // u_delete_status값을 업데이트
            }
            for (int i = 0; i < sRscsArray.size(); i++) {
              pathService.updateDeleteStatus(userSession, sRscsArray.get(i), "Y"); // 프로젝트(edms_project) 나
                                                                                   // 연구/투자(edms_research) 의
                                                                                   // u_delete_status값을 업데이트
            }

            pathService.addMcdBatchObjects(userSession, dto);
          }
          // MC :이동인데 복사로 처리된 건
          // B :배치작업등록 건
          // S :정상건
          // 나머지는 RuntimeException
          lsFlag = "B";
          resultMsg = jobStr + "작업  대상   폴더:" + totFolCnt + " 건 \n " + " 파일 :" + totFilCnt + " 건 \n(Live: "
              + totFilLStatusCnt + " 건),  \n(Close:" + totFilCStatusCnt + " 건), \n(Link: " + totFilKStatusCnt
              + " 건 입니다 \n 배치작업에 등록되었습니다.";
        }
      }
    }
    /*
     * "- 수신부서가 본인이 속한 부서일 경우 이동으로 처리 [이동 시 권한처리에 관한 규칙]을 따름 - 수신부서가 본인이 속한 부서가 아닐
     * 경우 복사로 처리 이동 대상의 소유부서와 권한을 수신부서 기준으로 수정하여 붙여넣기 대상에 생성"
     */
    String ls_SuccessMsg = "";
    if (uptPthGbn.equals("D")) {
      ls_SuccessMsg = " 총 건수 ===프로젝트:" + totPjtCnt + "건,  연구과제 :" + totRscCnt + " 건,  폴더 :" + totFolCnt + " 건,"
          + "전체파일건수 :" + totFilCnt + " 건" + "(Live: " + totFilLStatusCnt + " 건)" + "(Close:" + totFilCStatusCnt + " 건)"
          + "(Link: " + totFilKStatusCnt + " 건 (실제 삭제건은 권한에 따라 차이날 수 있습니다)";
      System.out.println(ls_SuccessMsg);

    } else {
      ls_SuccessMsg = " 대상 건수 ===프로젝트:" + totPjtCnt + "건,  연구과제 :" + totRscCnt + " 건,  폴더 :" + totFolCnt + " 건,"
          + "전체파일건수 :" + totFilCnt + " 건" + "(Live: " + totFilLStatusCnt + " 건)" + "(Close:" + totFilCStatusCnt + " 건)"
          + "(Link: " + totFilKStatusCnt + " 건 ";
      System.out.println(ls_SuccessMsg);
    }
    if ((totPjtCnt + totRscCnt + totFolCnt + totFilCnt) < 1) {
      ;// resultMsg="작업대상 하위에 아무것도 존재하지 않습니다";
    }
    /*************************************************************************
     * request에 대한 정합성 체크 End
     ******/

    // 사용자 프리셋 추가 Start. 2021.11.19 **********************************
    dto.setReqUserIp(request.getRemoteAddr()); // 요청자 IP 지정
    /*
     * List<UserPreset> userPresetList =
     * userPresetDao.selectOneByFilter(UserPresetFilterDto.builder()
     * .uUserId(userSession.getUser().getUserId()) .uRegBaseFlag(true).build());
     * 
     * UserPreset userPreset = userPresetList.get(0); dto.setUserPreset(userPreset);
     */
    // 사용자 프리셋 추가 End. 2021.11.19 ************************************
    String transCause = "";
    if (!resultMsg.equals("")) {
      // break;
//			return OK(resultMsg);
      if (!lsFlag.equals("B")) {
        throw new RuntimeException(resultMsg);
      } else {
        body.put("flag", lsFlag);
        body.put("status", "200");
        body.put("message", resultMsg);
        return OK(body);
      }

    } else {
      if (uptPthGbn.equals("C") || uptPthGbn.equals("M"))
        if (!dto.getTargetGubun().equals(""))
          if (dto.getTargetGubun().substring(0, 1).equals("S")) {
            if (isFolder) {
              resultMsg = pathService.shareFol(userSession, dto, sFolArray);
              /*
               * for(int i=0; i < sFolArray.size(); i++) { // 공유/협업 권한 해제
               * 
               * for (String item : sFolArray) { IDfPersistentObject idf_PObj =
               * idfSession.getObject(new DfId(item.getRObjectId())); idf_PObj.destroy(); }
               * 
               * // 공유/협업 권한 부여
               * 
               * for (String item : sFolArray) { IDfPersistentObject idf_PObj =
               * RegistAuthShareDto.toIDfPersistentObject( idfSession,
               * RegistAuthShareDto.builder() .uObjId(dataId) .uAuthorId(item.getTargetId())
               * .uAuthorType(item.getType()) .uPermitType(item.getPermitType()) .build() );
               * 
               * idf_PObj.save(); } }
               */
            }

          }
      if (null == dto.getSourceGubun()) {
        ;
      } else if (dto.getSourceGubun().equals("DWF") || dto.getSourceGubun().equals("DWY")
          || dto.getSourceGubun().equals("DWT")) {
        // if(uptPthGbn.equals("C") || uptPthGbn.equals("M")) {
        if (uptPthGbn.equals("M")) {
          // return OK("전자결재문서는 복사/이동할 수 없습니다");

          if (uptPthGbn.equals("M")) {
            List<String> sFilArrayLnk = dto.getSourceFiles(); // edms_doc에 존재하는 대상 파일들

            IDfSession idfSession = DCTMUtils.getAdminSession();
            IDfCollection idf_Col = null;
            IDfCollection idf_Col_U = null;
            try {
              for (int ic = 0; ic < sFilArrayLnk.size(); ic++) {
                if (sFilArray.get(ic).equals(""))
                  continue;

                String s_ObjId = sFilArray.get(ic);//
                s_ObjId = DCTMUtils.getCurrentObjectID(idfSession, s_ObjId); // 문서의 최신버전 r_object_id를 가져온다.
                if (s_ObjId.equals("")) {
                  throw new NotFoundException("문서 최신버전이 변경됨[Documentum]");
                }
                IDfDocument idfDocObj = (IDfDocument) idfSession.getObject(new DfId(s_ObjId));

                s_ObjId = idfDocObj.getString("u_doc_key");

                int lCnt = 0;
                // String s_Dql = "select * from edms_doc_link where u_doc_key = '" + s_ObjId +
                // "' and u_fol_id='"+ dto.getSrcFolId()+"'" ;
                String s_Dql = "select * from edms_doc_link  where u_doc_key = '" + s_ObjId + "' ";
                idf_Col = DCTMUtils.getCollectionByDQL(idfSession, s_Dql, DfQuery.DF_QUERY);
                while (idf_Col != null && idf_Col.next()) {
                  System.out.println("전 :" + dto.getSourceFiles().toString());
                  dto.getSourceFiles().remove(sFilArray.get(ic)); //
                  System.out.println("후:" + dto.getSourceFiles().toString());

                  System.out.println(dto.getTargetDboxId());
                  String ls_FolId = dto.getTargetDboxId();

                  if (ls_FolId.equals(dto.getSrcCabinetcode()) || ls_FolId.equals(dto.getTgCabinetcode()))
                    ls_FolId = " ";

                  String s_UptDql = "UPDATE edms_doc_link  OBJECTS SET u_fol_id='" + ls_FolId + "' "
                      + "WHERE u_doc_key='" + s_ObjId + "' and r_object_id='" + idf_Col.getString("r_object_id") + "' ";

                  IDfQuery idf_Qry = null;
                  idf_Qry = new DfQuery();
                  idf_Qry.setDQL(s_UptDql);
                  idf_Col_U = idf_Qry.execute(idfSession, DfQuery.QUERY);
                  if (idf_Col_U != null && idf_Col_U.next())
                    idf_Col_U.close();

                  String sOwnSrDeptOrgId = gwDeptService.selectOrgIdByCabinetcode(dto.getSrcCabinetcode());
                  /*
                   * LogDoc logDoc = LogDoc.builder() .uJobCode( "ED") //자료수정
                   * .uDocId(idf_Col.getString("r_object_id"))
                   * .uDocKey(idfDocObj.getString("u_doc_key"))
                   * .uDocName(idfDocObj.getString("object_name").replaceAll("'", "''"))
                   * .uDocVersion(Integer.parseInt(idfDocObj.getVersionLabel(0).substring(0,
                   * idfDocObj.getVersionLabel(0).indexOf(".")))+"")
                   * .uFileSize(Long.parseLong(idfDocObj.getString("r_content_size")))
                   * .uOwnDeptcode(sOwnSrDeptOrgId)
                   * .uActDeptCode(userSession.getUser().getOrgId()) .uJobUser(userId)
                   * .uJobUserType(dto.getUJobUserType()==null ? "P" : dto.getUJobUserType())
                   * .uDocStatus(idfDocObj.getString("u_doc_status"))
                   * .uSecLevel(idfDocObj.getString("u_sec_level"))
                   * .uCabinetCode(idfDocObj.getString("u_cabinet_code")) .uJobGubun("")
                   * .uUserIp(dto.getReqUserIp()) // 받아야함. .uAttachSystem("") .build();
                   * logDao.insertLog(logDoc);
                   */
                  lCnt++;
                }
                if (idf_Col != null)
                  idf_Col.close();
                if (idf_Col_U != null)
                  idf_Col_U.close();
              }
            } catch (Exception e) {
              e.printStackTrace();
            } finally {
              idfSession.disconnect();
              if (idf_Col != null)
                idf_Col.close();
              if (idf_Col_U != null)
                idf_Col_U.close();
            }
            if (sFilArrayLnk.size() < 1) {
              body.put("flag", "T");
              body.put("status", "200");
              body.put("message", "링크파일을 이동하였습니다");
              return OK(body);

            }
          }
          //TODO:(2022.02.23. 복사/이동/삭제 인수인계 정리) 링크복사 막
          /*
           * else if(uptPthGbn.equals("C")) { //복사 List<String> sFilArrayLnk =
           * dto.getSourceFiles(); //edms_doc에 존재하는 대상 파일들
           * 
           * IDfSession idfSession = DCTMUtils.getAdminSession(); IDfCollection idf_Col =
           * null; IDfCollection idf_Col_U = null; try { for(int ic=0; ic <
           * sFilArrayLnk.size(); ic++) { if(sFilArray.get(ic).equals("")) continue;
           * 
           * String s_ObjId = sFilArray.get(ic);// s_ObjId =
           * DCTMUtils.getCurrentObjectID(idfSession, s_ObjId); //문서의 최신버전 r_object_id를
           * 가져온다. if(s_ObjId.equals("")) { throw new
           * NotFoundException("문서 최신버전이 변경됨[Documentum]"); } IDfDocument idfDocObj =
           * (IDfDocument)idfSession.getObject(new DfId(s_ObjId)); s_ObjId =
           * idfDocObj.getString("u_doc_key");
           * 
           * int lCnt=0; String s_Dql = "select * from edms_doc_link  where u_doc_key = '"
           * + s_ObjId + "' and u_fol_id='"+ dto.getSrcFolId()+"'" ; idf_Col =
           * DCTMUtils.getCollectionByDQL(idfSession, s_Dql, DfQuery.DF_QUERY);
           * while(idf_Col != null && idf_Col.next()) { System.out.println("전 Cp:"+
           * dto.getSourceFiles().toString());
           * dto.getSourceFiles().remove(sFilArray.get(ic)); //
           * System.out.println("후 Cp:"+ dto.getSourceFiles().toString());
           * 
           * System.out.println(dto.getTargetDboxId()); String
           * ls_FolId=dto.getTargetDboxId();
           * 
           * if(ls_FolId.equals(dto.getSrcCabinetcode()) ||
           * ls_FolId.equals(dto.getTgCabinetcode())) ls_FolId=" ";
           * 
           * DocLink docLink = new DocLink();
           * 
           * IDfPersistentObject idf_LObj = null; idf_LObj = (IDfPersistentObject)
           * idfSession.newObject( "edms_doc_link" ); idf_LObj.setString("u_doc_id",
           * idfDocObj.getString("r_object_id") ); //r_object_id,
           * document.getChronicleId() idf_LObj.setString("u_doc_key", s_ObjId ); //문서번호
           * idf_LObj.setString("u_cabinet_code", idfDocObj.getString("u_cabinet_code") );
           * //문서함코드
           * 
           * idf_LObj.setString("u_fol_id", ls_FolId ); //변경전 폴더 id, saveWfList이후에
           * 전자결재폴더id로 바꿔서 edms_doc에 저장( link테이블에 있는 u_fol_id가 이전폴더id가 됨 )
           * idf_LObj.setString("u_link_type", idf_Col.getString("u_link_type") );
           * //링크종류', SET COMMENT_TEXT='W:결재')
           * 
           * idf_LObj.setString("u_create_user", userId);
           * idf_LObj.setString("u_create_date", (new DfTime()).toString());
           * 
           * idf_LObj.save();
           * 
           * lCnt++; } if(idf_Col != null) idf_Col.close(); } }catch( Exception e) {
           * e.printStackTrace(); } finally { idfSession.disconnect(); if(idf_Col != null)
           * idf_Col.close(); } if(sFilArrayLnk.size() < 1) { body.put("flag", "T");
           * body.put("status", "200"); body.put("message", "링크파일을 복사하였습니다"); return
           * OK(body);
           * 
           * } }
           */
          // if(dto.getSourceFiles().size() > 0) throw new RuntimeException("전자결재문서는
          // 복사/이동할 수 없습니다");
          if (dto.getSourceFiles().size() > 0)
            throw new RuntimeException("전자결재문서는 이동할 수 없습니다");
        }
      }

      // 대상과 타겟의 부서문서함코드가 다르면 복사로 처리한다.
      // if(isShare) {
      if ((null != dto.getSrcCabinetcode() && !dto.getSrcCabinetcode().equals("")
          && !dto.getSrcCabinetcode().equals(" "))
          && (null != dto.getTgCabinetcode() && !dto.getTgCabinetcode().equals("")
              && !dto.getTgCabinetcode().equals(" "))) {
        if (!dto.getSrcCabinetcode().equals(dto.getTgCabinetcode())) {
          if (uptPthGbn.equals("M")) {
            uptPthGbn = "C";
            dto.setUptPthGbn(uptPthGbn);
            lsFlag = "MC";
          }
        }
      }
      // }
      // 로직 미정
      String groupCode = userSession.getUser().getMgr().getGroupComCode();
      String comCode = userSession.getUser().getMgr().getCompanyComCode();
      List<String> depts = userSession.getUser().getMgr().getCompanyDeptCode();
      String jobUserType = null;
      if (depts != null) {
        for (String dept : depts) {
          if (dto.getSrcCabinetcode().equals(dto.getTgCabinetcode())) {
            jobUserType = "D";
            break;
          }
        }
      }
      jobUserType = groupCode == null ? (comCode == null ? (jobUserType == null ? "P" : jobUserType) : "C") : "G";
      dto.setUJobUserType(jobUserType); // 작업자구분(P:개인, D:부서관리자, C:사별관리자, G:그룹관리자, S:시스템)

      switch (uptPthGbn) {
      case "C": // 복사
        transCause = "";
        resultMsg = pathService.copyFolderAndFiles(userSession, null, dto, uFolMapTot, isMobile);
        if (resultMsg.equals("")) {
          body.put("status", "200");
          if (lsFlag.equals("S")) {
            if (dto.getSourceFolders().size() > 0 && dto.getTargetGubun().equals("POW")) {
              body.put("flag", "T");
              body.put("message", "프로젝트로 등록되었습니다");
            } else if (dto.getSourceFolders().size() > 0 && dto.getTargetGubun().equals("ROW")) {
              body.put("flag", "T");
              body.put("message", "연구과제로 등록되었습니다");
            } else {
              body.put("message", "정상처리되었습니다");
            }
          } else {
            if (lsFlag.equals("MC")) {
              body.put("flag", lsFlag);
              body.put("message", "소유부서 또는 주관부서가 아닌 곳으로 이동요청이 문서관리정책에 따라 복사로 처리되었습니다");
            } else if (lsFlag.equals("MP")) {
              body.put("flag", lsFlag);
              body.put("message", "프로젝트간 이동요청이  문서관리정책에 따라 복사로 처리되었습니다");
            } else if (lsFlag.equals("MR")) {
              body.put("flag", lsFlag);
              body.put("message", "연구과제간 이동요청이  문서관리정책에 따라 복사로 처리되었습니다");
            }
          }
          return OK(body);
        } else
          throw new RuntimeException(resultMsg);

      case "M": // 이동
        transCause = "";
        // updateData 참조
        String sTargetGubun = dto.getTargetGubun().substring(0, 1);

        resultMsg = pathService.moveFolderAndFiles(userSession, null, dto, uFolMapTot, isMobile);

        if (resultMsg.equals("")) {
          body.put("status", "200");
          if (lsFlag.equals("S")) {
            if (dto.getSourceFolders().size() > 0 && dto.getTargetGubun().equals("POW")) {
              body.put("flag", "T");
              body.put("message", "프로젝트로 등록되었습니다");
            } else if (dto.getSourceFolders().size() > 0 && dto.getTargetGubun().equals("ROW")) {
              body.put("flag", "T");
              body.put("message", "연구과제로 등록되었습니다");
            } else {
              body.put("flag", "T");
              body.put("message", "정상처리되었습니다");
            }
          }
          return OK(body);
        } else
          throw new RuntimeException(resultMsg);

      case "D": // 삭제
        transCause = "";
        DPath dParam = pathService.selectAlermType("DR", dto.getSrcCabinetcode()); // DR :폐기이벤트
        dto.setUAlarmYn(dParam.getUAlarmYn()); // 알람 발송여부
        dto.setUMmsYn(dParam.getUMmsYn()); // MMS 발송대상 여부
        dto.setUEmailYn(dParam.getUEmailYn()); // 이메일 발송대상 여부

        resultMsg = pathService.deleteFolderAndFiles(userSession, null, dto, uFolMapTot, isMobile);

        if ((dto.getReCycleCnt() + dto.getReqDelCnt() + dto.getLinkDelCnt()) < 1) {
          resultMsg = "삭제처리되지 않았습니다";
        }
        if (resultMsg.equals("")) {
          body.put("flag", "T");
          body.put("status", "200");
          body.put("message", "정상처리되었습니다");
          return OK(body);
        } else
          throw new RuntimeException(resultMsg);

      case "T": // 이관
        resultMsg = pathService.transFolderAndFiles(userSession, null, dto, uFolMapTot, isMobile);
        pathService.addTransBatchObjects(userSession, dto);
        if (resultMsg.equals("")) {
          body.put("flag", "S");
          body.put("status", "200");
          body.put("message", "정상처리되었습니다");
          return OK(body);
        } else {
          throw new RuntimeException(resultMsg);
        }

      default:
        if (resultMsg.equals("")) {
          body.put("flag", "S");
          body.put("status", "200");
          body.put("message", "정상처리되었습니다");
          return OK(body);
        } else
          throw new RuntimeException(resultMsg);
      }
    }
  }
}
