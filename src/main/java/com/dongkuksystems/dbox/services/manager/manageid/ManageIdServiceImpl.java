package com.dongkuksystems.dbox.services.manager.manageid;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.documentum.fc.client.IDfGroup;
import com.documentum.fc.client.IDfPersistentObject;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfUser;
import com.documentum.fc.common.DfId;
import com.documentum.fc.common.DfTime;
import com.dongkuksystems.dbox.constants.DocLogItem;
import com.dongkuksystems.dbox.constants.EntCode;
import com.dongkuksystems.dbox.daos.table.etc.gwaddjob.GwAddJobDao;
import com.dongkuksystems.dbox.daos.type.manager.gradePreservation.GradePreservationDao;
import com.dongkuksystems.dbox.daos.type.manager.manageid.ManageIdDao;
import com.dongkuksystems.dbox.models.common.UserSession;
import com.dongkuksystems.dbox.models.dto.type.manager.manageid.ManageIdCreateDto;
import com.dongkuksystems.dbox.models.dto.type.manager.manageid.ManageIdDto;
import com.dongkuksystems.dbox.models.dto.type.manager.manageid.ManageIdTreeDto;
import com.dongkuksystems.dbox.models.table.etc.GwDept;
import com.dongkuksystems.dbox.models.table.etc.VDept;
import com.dongkuksystems.dbox.models.table.log.LogDoc;
import com.dongkuksystems.dbox.models.table.log.LogUserLock;
import com.dongkuksystems.dbox.models.type.manager.gradePreservation.GradePreservation;
import com.dongkuksystems.dbox.models.type.manager.manageid.ManageId;
import com.dongkuksystems.dbox.repositories.redis.RedisRepository;
import com.dongkuksystems.dbox.services.AbstractCommonService;
import com.dongkuksystems.dbox.services.code.CodeService;
import com.dongkuksystems.dbox.services.dept.GwDeptService;
import com.dongkuksystems.dbox.utils.RestTemplateUtils;
import com.dongkuksystems.dbox.utils.dctm.DCTMUtils;

@Service
public class ManageIdServiceImpl extends AbstractCommonService implements ManageIdService {

  private final ManageIdDao manageIdDao;
  private final RedisRepository redisRepository;
  private final CodeService codeService;
  private final GwDeptService gwDeptService;
  private final GradePreservationDao gradePreservationDao;
  private final GwAddJobDao gwAddJobDao;

  public ManageIdServiceImpl(ManageIdDao manageIdDao, RedisRepository redisRepository, CodeService codeService, 
      GwDeptService gwDeptService, GradePreservationDao gradePreservationDao, GwAddJobDao gwAddJobDao) {
    this.manageIdDao = manageIdDao;
    this.redisRepository = redisRepository;
    this.codeService = codeService;
    this.gwDeptService = gwDeptService;
    this.gradePreservationDao = gradePreservationDao;
    this.gwAddJobDao = gwAddJobDao;
  }

  @Override
  public List<ManageId> selectUserId(ManageIdDto dto) {
    return manageIdDao.selectUserId(dto);
  }

  @Override
  public List<ManageId> selectUserIdLog(String uUserId, long offset, int limit) {
    return manageIdDao.selectUserIdLog(uUserId,offset, limit);
  }

  // edms_user에 사용자 없을 경우 edms_user에 사용자 추가 후 dm_group에도 추가
  @Override
  public void createUserId(ManageIdCreateDto dto, UserSession userSession) throws Exception {
    IDfSession idf_Sess = this.getIdfSession(userSession);
    IDfSession idfAdminSession = DCTMUtils.getAdminSession(); // Superuser
    JSONObject jsonObject = null;
    IDfUser idf_User = (IDfUser) idfAdminSession.newObject("edms_user");
    List<ManageId> userData = manageIdDao.selectGwUserData(dto.getSocialPerId());
    try {
      // 트랜잭션
      idf_Sess.beginTrans();

      if (dto.getUCabinetCode() == null) {
        List<ManageId> deptCodeList = manageIdDao.selectCabinetCode(dto.getOrgId());
        IDfGroup idf_Group = idfAdminSession.getGroup("g_" + deptCodeList.get(0).getUCabinetCode());

        idf_User.setUserName(dto.getSocialPerId());
        idf_User.setUserLoginName(dto.getSocialPerId());
        idf_User.setUserAddress(dto.getEmail());
        idf_User.setString("u_dept_code", deptCodeList.get(0).getUDeptCode());

        idf_Group.addUser(dto.getSocialPerId());

        idf_User.save();
        idf_Group.save();
        
        // iris 연동
        RestTemplateUtils restUtils = new RestTemplateUtils();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
        headers.add("Content-Type", "application/json;utf-8");
        headers.add("Pragma", "no-cache");
        headers.add("Expires", "0");

        String lsUrl = "http://niris.dongkuk.com/api/insertDboxUserApi.json";
        String lsParameters = "?apiKey=iud1oz1bh5uk149zebxvpl4bv0c0nyrh";
        lsParameters += "&systemId=" + "DBOX_SPECIAL_USER";

        Map<String, String> body = new HashMap<String, String>();

        body.put("userId", dto.getSocialPerId());
        body.put("useYn", "Y");

        ResponseEntity<String> response = restUtils.post(lsUrl + lsParameters, headers, body, String.class);

        JSONParser jsonParser = new JSONParser();
        jsonObject = (JSONObject) jsonParser.parse(response.getBody().toString());
        if (Integer.parseInt(response.getStatusCode().toString()) > 201) {
          throw new RuntimeException("그룹웨어에 사용자 정보 수정 실패");
        }        
        
      } else if (dto.getUCabinetCode() != null) {
        IDfGroup idf_Group = idfAdminSession.getGroup("g_" + dto.getUCabinetCode());

        idf_User.setUserName(dto.getSocialPerId());
        idf_User.setUserLoginName(dto.getSocialPerId());
        idf_User.setUserAddress(dto.getEmail());
        idf_User.setString("u_dept_code", dto.getOrgId());

        idf_Group.addUser(dto.getSocialPerId());
        idf_User.save();
        idf_Group.save();
        
        // iris 연동
        RestTemplateUtils restUtils = new RestTemplateUtils();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
        headers.add("Content-Type", "application/json;utf-8");
        headers.add("Pragma", "no-cache");
        headers.add("Expires", "0");

        String lsUrl = "http://niris.dongkuk.com/api/insertDboxUserApi.json";
        String lsParameters = "?apiKey=iud1oz1bh5uk149zebxvpl4bv0c0nyrh";
        lsParameters += "&systemId=" + "DBOX_SPECIAL_USER";

        Map<String, String> body = new HashMap<String, String>();

        body.put("userId", dto.getSocialPerId());
        body.put("useYn", "Y");

        ResponseEntity<String> response = restUtils.post(lsUrl + lsParameters, headers, body, String.class);

        JSONParser jsonParser = new JSONParser();
        jsonObject = (JSONObject) jsonParser.parse(response.getBody().toString());
        if (Integer.parseInt(response.getStatusCode().toString()) > 201) {
          throw new RuntimeException("그룹웨어에 사용자 정보 수정 실패");
        }
      } 
      else {
      }      
      LogUserLock logUserLock = LogUserLock.builder()
          .uComCode(userData.get(0).getComOrgId())
          .uDeptCode(userData.get(0).getOrgId())
          .uUserId(userData.get(0).getSocialPerId())
          .uJobType("U")
          .uJobUser(userSession.getDUserId())
          .uJobDate(LocalDateTime.now())
          .build();
      insertLog(logUserLock);
    } catch (Exception e) {
      System.out.println(e.getMessage());
      throw e;

    } finally {
      if (idf_Sess.isTransactionActive()) {
        idf_Sess.abortTrans();
      }
      if (idfAdminSession != null && idfAdminSession.isConnected()) {
        idfAdminSession.disconnect();
      }
      if (idf_Sess != null && idf_Sess.isConnected()) {
        sessionRelease(userSession.getUser().getUserId(), idf_Sess);
      }
    }
  }

  // edms_user에 사용자 있을 경우 user_state 값만 수정
  @Override
  public void updateIdStatus(ManageIdCreateDto dto, UserSession userSession) throws Exception {
    IDfSession idf_Sess = this.getIdfSession(userSession);
    IDfSession idfAdminSession = DCTMUtils.getAdminSession(); // Superuser
    List<ManageId> rObjectIdList = manageIdDao.selectRObjectId(dto.getSocialPerId());
    IDfPersistentObject idf_PObj = idfAdminSession.getObject(new DfId(rObjectIdList.get(0).getRObjectId()));
    JSONObject jsonObject = null;
    List<ManageId> userData = manageIdDao.selectGwUserData(dto.getSocialPerId());
    try {      
      
      // 사용->미사용 처리
      if (dto.getUserState().equals("0")) {
        idf_PObj.setString("user_state", "1");
        idf_PObj.save();

        // iris 연동
        RestTemplateUtils restUtils = new RestTemplateUtils();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
        headers.add("Content-Type", "application/json;utf-8");
        headers.add("Pragma", "no-cache");
        headers.add("Expires", "0");

        String lsUrl = "http://niris.dongkuk.com/api/insertDboxUserApi.json";
        String lsParameters = "?apiKey=iud1oz1bh5uk149zebxvpl4bv0c0nyrh";
        lsParameters += "&systemId=" + "DBOX_SPECIAL_USER";

        Map<String, String> body = new HashMap<String, String>();

        body.put("userId", dto.getSocialPerId());
        body.put("useYn", "N");

        ResponseEntity<String> response = restUtils.post(lsUrl + lsParameters, headers, body, String.class);

        JSONParser jsonParser = new JSONParser();
        jsonObject = (JSONObject) jsonParser.parse(response.getBody().toString());
        if (Integer.parseInt(response.getStatusCode().toString()) > 201) {
          throw new RuntimeException("그룹웨어에 사용자 정보 수정 실패");
        }
        
        LogUserLock logUserLock = LogUserLock.builder()
            .uComCode(userData.get(0).getComOrgId())
            .uDeptCode(userData.get(0).getOrgId())
            .uUserId(userData.get(0).getSocialPerId())
            .uJobType("L")
            .uJobUser(userSession.getDUserId())
            .uJobDate(LocalDateTime.now())
            .build();
        insertLog(logUserLock);

        // TODO: dooyeon.yoo 추가 잠금 사용자 redis추가
//        redisRepository.put(Commons.LOCK_PREFIX + socialPerId, "", null);
        
      } else if (dto.getUserState().equals("1")) { // 미사용->사용 처리        
        idf_PObj.setString("user_state", "0");
        idf_PObj.save();

        // iris 연동
        RestTemplateUtils restUtils = new RestTemplateUtils();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
        headers.add("Content-Type", "application/json;utf-8");
        headers.add("Pragma", "no-cache");
        headers.add("Expires", "0");

        String lsUrl = "http://niris.dongkuk.com/api/insertDboxUserApi.json";
        String lsParameters = "?apiKey=iud1oz1bh5uk149zebxvpl4bv0c0nyrh";
        lsParameters += "&systemId=" + "DBOX_SPECIAL_USER";

        Map<String, String> body = new HashMap<String, String>();

        body.put("userId", dto.getSocialPerId());
        body.put("useYn", "Y");

        ResponseEntity<String> response = restUtils.post(lsUrl + lsParameters, headers, body, String.class);

        JSONParser jsonParser = new JSONParser();
        jsonObject = (JSONObject) jsonParser.parse(response.getBody().toString());
        if (Integer.parseInt(response.getStatusCode().toString()) > 201) {
          throw new RuntimeException("그룹웨어에 사용자 정보 수정 실패");
        }        
        LogUserLock logUserLock = LogUserLock.builder()
            .uComCode(userData.get(0).getComOrgId())
            .uDeptCode(userData.get(0).getOrgId())
            .uUserId(userData.get(0).getSocialPerId())
            .uJobType("U")
            .uJobUser(userSession.getDUserId())
            .uJobDate(LocalDateTime.now())
            .build();
        insertLog(logUserLock);

        // TODO: dooyeon.yoo 추가 잠금 사용자 redis 제거
//        redisRepository.delete(Commons.LOCK_PREFIX + socialPerId);
      }
    } catch (Exception e) {
      throw e;
    } finally {
      if (idfAdminSession != null && idfAdminSession.isConnected()) {
        idfAdminSession.disconnect();
      }
      if (idf_Sess != null && idf_Sess.isConnected()) {
        sessionRelease(userSession.getUser().getUserId(), idf_Sess);
      }
    }
  }
  
  @Override
  public String createNewUserPreset(String userId, String comCode, String orgId, UserSession userSession) throws Exception {
    IDfSession idfSession = this.getIdfSession(userSession);    
    
    // 초기값 설정
    String type = "D";    
    List<String> regBaseFlagList = Arrays.asList("F","T","F","F");
    List<String> secBaseFlagList = Arrays.asList("T","T","T","T");       
    List<String> configNameList = Arrays.asList("Preset(제한)","Preset(팀내)","Preset(사내)","Preset(그룹사내)");
    List<ManageId> deptCodeList = manageIdDao.selectCabinetCode(orgId);
    List<ManageId> comCabinetCodeList = manageIdDao.selectComCabinet(comCode);
    List<GradePreservation> selectPreserve = gradePreservationDao.selectGradePreservation(comCode);
    ArrayList<String> preserveFlagList = new ArrayList<>();
    preserveFlagList.add(selectPreserve.get(0).getUSecSYear());
    preserveFlagList.add(selectPreserve.get(0).getUSecTYear());
    preserveFlagList.add(selectPreserve.get(0).getUSecCYear());
    preserveFlagList.add(selectPreserve.get(0).getUSecGYear());
    List<String> secLevelList = Arrays.asList("S","T","C","G");
    String deptCabinetCode = "g_" + deptCodeList.get(0).getUCabinetCode();
    String comCabinetCode = "g_" + comCabinetCodeList.get(0).getUCabinetCode();
        
    try {
      for(int i = 0; i<configNameList.size(); i++) {
      IDfPersistentObject idf_PObj = (IDfPersistentObject) idfSession.newObject("edms_user_preset");
      idf_PObj.setString("u_user_id", userId);
      idf_PObj.setString("u_reg_base_flag", regBaseFlagList.get(i));   
      idf_PObj.setString("u_sec_base_flag", secBaseFlagList.get(i));
      idf_PObj.setString("u_config_name", configNameList.get(i));
      idf_PObj.setString("u_config_type", type);
      idf_PObj.setString("u_open_flag", "T");
      idf_PObj.setString("u_preserve_flag", preserveFlagList.get(i));
      idf_PObj.setString("u_sec_level", secLevelList.get(i));
      idf_PObj.setString("u_pc_reg_flag", "C");
      idf_PObj.setString("u_copy_flag", "K");
      idf_PObj.setString("u_edit_save_flag", "V");
      idf_PObj.setString("u_mail_permit_flag", "Y");
      idf_PObj.setString("u_create_user", idfSession.getLoginUserName());
      idf_PObj.setString("u_create_date", (new DfTime()).toString()); 
      
      if(secLevelList.get(i).equals("S")) {
        idf_PObj.appendString("u_live_delete_author", deptCabinetCode);
        idf_PObj.appendString("u_closed_read_author", userId);      
      }
      else if(secLevelList.get(i).equals("T")) {
        idf_PObj.appendString("u_live_delete_author", deptCabinetCode);
        idf_PObj.appendString("u_closed_read_author", deptCabinetCode);
      }
      else if(secLevelList.get(i).equals("C")) {
        idf_PObj.appendString("u_live_delete_author", deptCabinetCode);
        idf_PObj.appendString("u_closed_read_author", comCabinetCode);
      }
      else {
        idf_PObj.appendString("u_live_delete_author", deptCabinetCode);
        idf_PObj.appendString("u_closed_read_author", "g_d00201");
      }
      
      idf_PObj.save();
      }
          
    } catch(Exception e) {
      e.printStackTrace();
    } finally {
      if (idfSession != null  && idfSession.isConnected()) {
        sessionRelease(userSession.getUser().getUserId(), idfSession);
      }
    }        
    return null;
  }

  @Override
  @Cacheable(value = "selectManageIdTree", key = "#comOrgId")
  public ManageIdTreeDto selectMangeIdTree(String comOrgId) throws Exception {
    List<GwDept> depts = gwDeptService.selectDeptsInGwDept(comOrgId);
    List<ManageId> userIdList =  manageIdDao.selectUserId(ManageIdDto.builder().build());
    EntCode ent = EntCode.getEnt(comOrgId);
    String deptCode = ent.name();
    ManageIdTreeDto tree = ManageIdTreeDto.builder()
        .dept(gwDeptService.selectGwDeptByOrgId(deptCode)) 
        .deptUsers(manageIdDao.selectUserIdByDeptCode(deptCode))
        .addJob(gwAddJobDao.selectDetailedListByAjId(deptCode))
        .children(this.makeTree(deptCode, depts)).build();
    return tree;
  }
  
  private List<ManageIdTreeDto> makeTree(String upOrgId, List<GwDept> depts) {
    List<ManageIdTreeDto> list = new ArrayList<>();
    for (GwDept val : depts) {
      if (upOrgId.equals(val.getUpOrgId())) {
        if (val.getComOrgId() != null) { 
          val.setComOrgNm(EntCode.findOrgNmByOrgId(val.getComOrgId()));
        }
        ManageIdTreeDto tmp = ManageIdTreeDto.builder()
            .dept(val)
            .deptUsers(manageIdDao.selectUserIdByDeptCode(val.getOrgId()))
            .addJob(gwAddJobDao.selectDetailedListByAjId(val.getOrgId()))
            .children(this.makeTree(val.getOrgId(),
                depts.stream().filter(t -> !upOrgId.equals(t.getUpOrgId())).collect(Collectors.toList())))
            .build();
        list.add(tmp);
      }
    }
    return list;
  }
}
