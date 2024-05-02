package com.dongkuksystems.dbox.services.manager.depttransfer;

import com.documentum.fc.client.IDfPersistentObject;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.common.DfId;
import com.documentum.fc.common.DfTime;
import com.dongkuksystems.dbox.daos.type.manager.depttransfer.DeptTransferMapper;
import com.dongkuksystems.dbox.models.common.UserSession;
import com.dongkuksystems.dbox.models.dto.type.manager.depttransfer.DeptTransferDto;
import com.dongkuksystems.dbox.services.AbstractCommonService;
import com.dongkuksystems.dbox.services.manager.common.ManagerCommonService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import java.util.*;

@Slf4j
@Service
public class DeptTransferService extends AbstractCommonService {

    @Autowired
    private DeptTransferMapper deptTransferMapper;

    @Autowired
    private ManagerCommonService managerCommonService;

    private DeptTransferDto deptTransferDto;

    public List<Map<String, Object>> selectDeptTransfers() {
        return deptTransferMapper.selectDeptTransfers();
    }

    // FIXME 성능 확인 후 수정 요망 및 handle StackOverflowError 어떻게?
    public List<Map<String, Object>> selectFoldersAsTree(DeptTransferDto dto) {

        // 임시 할당 및 초기화.
        this.deptTransferDto = dto;

        // 임시 소요시간 확인 용도.
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        List<Map<String, Object>> folders = selectFolders();
        structureFoldersAsTree(folders);

        stopWatch.stop();

        log.info(" ============================================= ");
        log.info(stopWatch.prettyPrint());
        log.info(" ============================================= ");

        //
        this.deptTransferDto = null;

        return folders;
    }

    public List<Map<String, Object>> selectDeptTransfersRequested(String uDeptCode) {
        return deptTransferMapper.selectDeptTransfersRequested(uDeptCode);
    }

    public Map<String, Object> selectAggregateDataToTransfer(List<String> rObjectIds) {
        return deptTransferMapper.selectAggregateDataToTransfer(rObjectIds);
    }

    // TODO Refactoring.
    public void insertTransMgr(DeptTransferDto dto) {
        try {
            UserSession userSession = dto.getUserSession();
            String userId = userSession.getUser().getUserId();
            IDfSession idf_Sess = this.getIdfSession(userSession);
            IDfPersistentObject idf_PObj = idf_Sess.newObject("edms_req_trans_mgr");
            idf_PObj.setString("u_send_dept_code", dto.getUSendDeptCode());
            idf_PObj.setString("u_recv_dept_code", dto.getURecvDeptCode());
            idf_PObj.setString("u_send_cabinet_code", dto.getUSendCabinetCode());
            idf_PObj.setString("u_recv_cabinet_code", dto.getURecvCabinetCode());
            for (String uSendFolId : dto.getUSendFolIds()) {
                idf_PObj.appendString("u_send_fol_id", uSendFolId);
            }
            idf_PObj.setString("u_recv_fol_id", dto.getURecvFolId());
            idf_PObj.setString("u_reg_user", userId);
            idf_PObj.setTime("u_reg_date", new DfTime());
            idf_PObj.setString("u_trans_reason", "");
            idf_PObj.save();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void insertTransReqId(DeptTransferDto dto) {
        try {
            IDfPersistentObject idf_PObj = getIDfPersistentObject(dto);
            for (String uTransReqId : dto.getUTransReqId()) {
                idf_PObj.appendString("u_trans_req_id", uTransReqId);
            }
            idf_PObj.save();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void insertDeptTransfers(DeptTransferDto dto) {
        // TODO 트랜잭션 처리 어떻게?
        insertTransMgr(dto);
        insertTransReqId(dto);
    }

    public void deleteDeptTransfersRequested(UserSession userSession, List<String> rObjectIds) {
        // TODO 쿼리 말고, Documentum 객체 사용해 처리 하도록 수정.
        rObjectIds.forEach(deptTransferMapper::deleteDeptTransfersRequested);
        managerCommonService.deleteObjects(userSession, rObjectIds);
    }

    private IDfPersistentObject getIDfPersistentObject(DeptTransferDto dto) throws Exception {
        return this.getIdfSession(dto.getUserSession()).getObject(new DfId(dto.getRObjectId()));
    }

    private List<Map<String, Object>> selectFolders() {
        if(Objects.isNull(deptTransferDto)) {
            return getEmptyList();
        }
        return deptTransferMapper.selectFolders(deptTransferDto);
    }

    private List<Map<String, Object>> selectFoldersBy(String key) {
        deptTransferDto.setUUpFolId(key);
        return selectFolders();
    }

    private ArrayList<Map<String, Object>> getEmptyList() {
        return new ArrayList<>();
    }

    private void structureFoldersAsTree(List<Map<String, Object>> foldersAsTree) {
        foldersAsTree.forEach(this::structureFolders);
    }

    private void structureFolders(Map<String, Object> folder) {
        String key = String.valueOf(folder.get("key"));
        List<Map<String, Object>> folders = selectFoldersBy(key);
        folder.put("children", folders);
        folder.put("data", toMap(folder, "key", "title"));
        structureFoldersAsTree(folders);
    }

    private HashMap<String, Object> toMap(Map<String, Object> folder, String ...items) {
        return new HashMap<String, Object>() {
            {
                Arrays.stream(items).forEach(value -> put(value, folder.get(value)));
            }
        };
    }
}
