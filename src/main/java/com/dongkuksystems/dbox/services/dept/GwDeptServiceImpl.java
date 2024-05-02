package com.dongkuksystems.dbox.services.dept;
 
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang.StringUtils;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.documentum.fc.client.IDfPersistentObject;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfId;
import com.documentum.fc.common.DfTime;
import com.dongkuksystems.dbox.constants.EntCode;
import com.dongkuksystems.dbox.constants.NotiItem;
import com.dongkuksystems.dbox.daos.table.etc.gwaddjob.GwAddJobDao;
import com.dongkuksystems.dbox.daos.table.etc.gwdept.GwDeptDao;
import com.dongkuksystems.dbox.daos.table.etc.gwuser.GwUserDao;
import com.dongkuksystems.dbox.daos.type.manager.deptMgr.DeptMgrDao;
import com.dongkuksystems.dbox.errors.NotFoundException;
import com.dongkuksystems.dbox.models.common.UserSession;
import com.dongkuksystems.dbox.models.dto.table.gwdept.GwDeptChildrenDto;
import com.dongkuksystems.dbox.models.dto.table.gwdept.GwDeptFilterDto;
import com.dongkuksystems.dbox.models.dto.table.gwdept.GwDeptListManagerDto;
import com.dongkuksystems.dbox.models.dto.table.gwdept.GwDeptPathDto;
import com.dongkuksystems.dbox.models.dto.table.gwdept.GwDeptTreeDto;
import com.dongkuksystems.dbox.models.table.etc.GwDept;
import com.dongkuksystems.dbox.models.table.etc.VDept;
import com.dongkuksystems.dbox.models.type.manager.deptMgr.DeptMgrs;
import com.dongkuksystems.dbox.services.AbstractCommonService;
import com.dongkuksystems.dbox.services.notification.NotificationService;
 
@Service
public class GwDeptServiceImpl extends AbstractCommonService implements GwDeptService {
 
  private final GwDeptDao gwDeptDao;
  private final GwUserDao gwUserDao;
  private final GwAddJobDao gwAddJobDao;
  private final DeptMgrDao deptMgrDao;
  private final NotificationService notificationService;
 
  public GwDeptServiceImpl(GwDeptDao gwDeptDao, GwUserDao gwUserDao, GwAddJobDao gwAddJobDao, DeptMgrDao deptMgrDao,
      NotificationService notificationService) {
    this.gwDeptDao = gwDeptDao;
    this.gwUserDao = gwUserDao;
    this.gwAddJobDao = gwAddJobDao;
    this.deptMgrDao = deptMgrDao;
    this.notificationService = notificationService;
  }
 
  @Override
  @Cacheable(value = "selectDepts", key = "#comOrgId.concat('-').concat(#mobileYn)")
  public List<VDept> selectDepts(String comOrgId, String mobileYn) {
    if (EntCode.DKG.name().equals(comOrgId)) {
      return gwDeptDao.selectAll();
    } 
    List<VDept> rst = null;
    if ("Y".equals(mobileYn)) {
      final List<String> excludes = Arrays.asList("DKS", "CHR", "VHR", "UNC", "FEI", "ITG");
      rst = gwDeptDao.selectAll().stream().filter(v -> comOrgId.equals(v.getComOrgId()) && !excludes.contains(v.getOrgId())).collect(Collectors.toList());
    } else {
      rst = gwDeptDao.selectAll().stream().filter(v -> comOrgId.equals(v.getComOrgId())).collect(Collectors.toList());
    }
//        .sorted((t1, t2) -> t1.getUnitFullId().compareToIgnoreCase(t2.getUnitFullId())).collect(Collectors.toList());
    return rst;
  }
  
  @Override
  @Cacheable(value = "selectDepts", key = "#comOrgId")
  public List<GwDept> selectDeptsInGwDept(String comOrgId) {
    if (EntCode.DKG.name().equals(comOrgId)) {
      return gwDeptDao.selectDeptsInGwDept();
    } 
    List<GwDept> rst = gwDeptDao.selectDeptsInGwDept().stream().filter(v -> comOrgId.equals(v.getComOrgId())).collect(Collectors.toList());
    return rst;
  }
 
  @Override
  @Cacheable(value = "selectDeptByOrgId", key = "#orgId")
  public VDept selectDeptByOrgId(String orgId) throws Exception {
    VDept rst = gwDeptDao.selectOneByOrgId(orgId).orElseThrow(() -> new NotFoundException("There is no such dept"));
    if (rst.getComOrgId() != null) {
      rst.setComOrgNm(EntCode.findOrgNmByOrgId(rst.getComOrgId()));
    }
    return rst;
  }
  
  @Override
  @Cacheable(value = "selectGwDeptByOrgId", key = "#orgId")
  public GwDept selectGwDeptByOrgId(String orgId) throws Exception {
    GwDept rst = gwDeptDao.selectGwOneByOrgId(orgId).orElseThrow(() -> new NotFoundException("There is no such dept"));
    if (rst.getComOrgId() != null) {
      rst.setComOrgNm(EntCode.findOrgNmByOrgId(rst.getComOrgId()));
    }
    return rst;
  }
 
  @Override
  @Cacheable(value = "selectDeptCodeByCabinetcode", key = "#cabinetcode")
  public String selectDeptCodeByCabinetcode(String cabinetCode) throws Exception {
    return gwDeptDao.selectOrgIdByCabinetcode(cabinetCode);
  }
 
  @Override
  public GwDeptChildrenDto selectDeptChildren(String orgId, boolean userYn, boolean addJobYn) throws Exception {
    return GwDeptChildrenDto.builder().deptList(gwDeptDao.selectListByUpOrgId(orgId, "A", ""))
        .deptUsers(userYn ? gwUserDao.selectListByOrgId(orgId, "A", "") : null)
        .addJob(addJobYn ? gwAddJobDao.selectListByAjId(orgId) : null).build();
  }
 
  @Override
  @Cacheable(value = "selectDeptPath", key = "#orgId")
  public GwDeptPathDto selectDeptPath(String orgId) throws Exception {
    return gwDeptDao.selectDeptPath(orgId);
  }
 
  @Override
  @Cacheable(value = "selectDeptTree", key = "#dto.hashCode()")
  public GwDeptTreeDto selectDeptTree(GwDeptFilterDto dto) throws Exception {
    List<VDept> depts = this.selectDepts(EntCode.DKG.name(), "N");
    EntCode ent = EntCode.getEnt(dto.getDeptId());
    String deptCode = ent != null ? ent.name() : dto.getDeptId();
    GwDeptTreeDto tree = GwDeptTreeDto.builder()
        .dept(this.selectDeptByOrgId(deptCode)) 
        .deptUsers(dto.isUserYn()?gwUserDao.selectListByOrgId(deptCode, dto.getUserStatus(), "") : null)
        .addJob(dto.isAddJobYn()?gwAddJobDao.selectDetailedListByAjId(deptCode) : null)
        .children(this.makeTree(dto, deptCode, depts)).build();
    return tree;
  }
 
  private List<GwDeptTreeDto> makeTree(GwDeptFilterDto dto, String upOrgId, List<VDept> depts) {
    List<GwDeptTreeDto> list = new ArrayList<>();
    for (VDept val : depts) {
      if (upOrgId.equals(val.getUpOrgId())) {
        if (val.getComOrgId() != null) { 
          val.setComOrgNm(EntCode.findOrgNmByOrgId(val.getComOrgId()));
        }
        GwDeptTreeDto tmp = GwDeptTreeDto.builder().dept(val)
            .deptUsers(dto.isUserYn() ? gwUserDao.selectListByOrgId(val.getOrgId(), dto.getUserStatus(), "") : null)
            .addJob(dto.isAddJobYn() ? gwAddJobDao.selectDetailedListByAjId(val.getOrgId()) : null)
            .children(this.makeTree(dto, val.getOrgId(),
                depts.stream().filter(t -> !upOrgId.equals(t.getUpOrgId())).collect(Collectors.toList())))
            .build();
        list.add(tmp);
      }
    }
    return list;
  }
 
  @Override
  @Cacheable(value = "selectDeptChildrenByOrgId", key = "#orgId")
  public List<VDept> selectDeptChildrenByOrgId(String orgId) throws Exception {
    return gwDeptDao.selectDeptChildrenByOrgId(orgId);
  }
 
  @Override
  @Cacheable(value = "selectComCodeByCabinetCode", key = "#cabinetcode")
  public String selectComCodeByCabinetCode(String cabinetcode) throws Exception {
    return gwDeptDao.selectComCodeByCabinetCode(cabinetcode);
  }
 
  @Override
  @Cacheable(value = "selectUserListOfPart", key = "#gwOrgId") // jjg,2021.10.19 Part사용자들 조회용
  public List<String> selectUserListOfPart(String gwOrgId) throws Exception {
    return gwDeptDao.selectUserListOfPart(gwOrgId);
  }
 
  @Override
  public List<GwDeptListManagerDto> selectDeptMemberList(String deptId) {
    return gwDeptDao.selectDeptMemberList(deptId);
 
  }
 
    // TODO 트랜잭션 처리 어떻게?
    @Override
    public void postDeptManager(String deptId, UserSession userSession, List<GwDeptListManagerDto> members) throws Exception {
 
        List<GwDeptListManagerDto> managers = gwDeptDao.selectDeptManagerList(deptId);
 
        IDfSession idfSession = this.getIdfSession(userSession);
        IDfPersistentObject idf_PObj = null;
        
        idfSession.beginTrans();
        try {

          if (idfSession == null || !idfSession.isConnected()) {
            throw new Exception("DCTM Session 가져오기 실패");
          }
          for (GwDeptListManagerDto member : members) {
            boolean isChecked = isTypeManager(member.getUMgrType());
            boolean isManager = isManager(managers, member.getUserId());
            boolean canSaveManager = isChecked && !isManager;
            if(canSaveManager) {
                saveManager(deptId, userSession.getUser().getUserId(), member, idfSession);
                idf_PObj = (IDfPersistentObject) idfSession.newObject("edms_noti");
                idf_PObj.setString("u_msg_type", "DM");
                idf_PObj.setString("u_sender_id", userSession.getDUserId());
                idf_PObj.setString("u_receiver_id", member.getUserId());
                idf_PObj.setString("u_msg", "부서문서관리자로 지정되었습니다.");
                idf_PObj.setString("u_performer_id", userSession.getDUserId());
                idf_PObj.setString("u_obj_id", member.getRObjectId());
                idf_PObj.setString("u_sent_date", new DfTime().toString());
                idf_PObj.setString("u_action_yn", "N");
                idf_PObj.setString("u_action_need_yn", "N");
                idf_PObj.save();
            } else if (!isChecked && isManager) {
                destroyManagerIfPossible(idfSession, member.getRObjectId());
                idf_PObj = (IDfPersistentObject) idfSession.newObject("edms_noti");
                idf_PObj.setString("u_msg_type", "DM");
                idf_PObj.setString("u_sender_id", userSession.getDUserId());
                idf_PObj.setString("u_receiver_id", member.getUserId());
                idf_PObj.setString("u_msg", "부서문서관리자에서 해제 되었습니다.");
                idf_PObj.setString("u_performer_id", userSession.getDUserId());
                idf_PObj.setString("u_obj_id", member.getRObjectId());
                idf_PObj.setString("u_sent_date", new DfTime().toString());
                idf_PObj.setString("u_action_yn", "N");
                idf_PObj.setString("u_action_need_yn", "N");
                idf_PObj.save();
                
            }          
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
    
       
    }
 
    @Override
    @Cacheable(value = "selectOrgIdByCabinetcode", key = "#cabinetcode")
    public String selectOrgIdByCabinetcode(String cabinetcode) throws Exception {
        return gwDeptDao.selectOrgIdByCabinetcode(cabinetcode);
    }
 
    @Override
    public List<GwDept> selectDeptMng(String managerPerId) {
        return gwDeptDao.selectDeptMng(managerPerId);
    }
 
    private void destroyManagerIfPossible(IDfSession idfSession, String rObjectId) throws DfException {
        if(StringUtils.isNotEmpty(rObjectId)) {
            idfSession.getObject(new DfId(rObjectId)).destroy();
        }
    }
 
    private void saveManager(String deptId, String sessionUserId, GwDeptListManagerDto member, IDfSession idfSession) throws DfException {
      // TODO 건건이 처리 말고 배치로
        IDfPersistentObject idf_PObj = idfSession.newObject("edms_dept_mgr");
        idf_PObj.setString("u_com_code", member.getComOrgId());
        idf_PObj.setString("u_dept_code", deptId);
        idf_PObj.setString("u_user_id", member.getUserId());
        idf_PObj.setString("u_mgr_type", "M");
        idf_PObj.setString("u_assign_user", sessionUserId);
        idf_PObj.setString("u_assign_date", (new DfTime()).toString());
        idf_PObj.setString("u_assign_user_type", "D");
        idf_PObj.save();
    }
 
    private boolean isTypeManager(String mgrType) {
        return "M".equals(mgrType);
    }
 
    private boolean isManager(List<GwDeptListManagerDto> managers, String memberId) {
        GwDeptListManagerDto manager = managers
                .stream()
                .filter(item -> item.getUserId().equals(memberId) && isTypeManager(item.getUMgrType()))
                .findAny().orElse(null);
        return manager != null;
    }
 
    private void sendNotification(String deptId) throws Exception {
        List<DeptMgrs> newManagerList = deptMgrDao.selectByDeptCode(deptId);
        List<String> newManagerDept = newManagerList.stream().map(DeptMgrs::getUDeptCode).collect(Collectors.toList());
        List<String> newManagerId = newManagerList.stream().map(DeptMgrs::getUUserId).collect(Collectors.toList());
        notificationService.sendNotification(Stream.concat(newManagerDept.stream(), newManagerId.stream()).collect(Collectors.toList()), NotiItem.SH, "Dbox", "푸시를 확인해주세요");
    }
}