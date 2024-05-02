package com.dongkuksystems.dbox.daos.table.takeout;

import java.util.List;
import java.util.Optional;

import org.apache.ibatis.annotations.Param;

import com.dongkuksystems.dbox.models.dto.type.request.ReqTakeoutConfigDto;
import com.dongkuksystems.dbox.models.dto.type.request.ReqTakeoutDetailDto;
import com.dongkuksystems.dbox.models.dto.type.request.ReqTakeoutDto;
import com.dongkuksystems.dbox.models.type.manager.TakeoutConfig;
import com.dongkuksystems.dbox.models.type.manager.TakeoutConfigRepeating;
import com.dongkuksystems.dbox.models.type.request.ReqTakeout;
import com.dongkuksystems.dbox.models.type.request.ReqTakeoutDoc;

public interface ReqTakeoutDao {

  public List<ReqTakeoutConfigDto> nameListByObjId(String objectId);

  public List<ReqTakeoutConfigDto> nameListByOrgId(String orgId);

  public Optional<ReqTakeout> selectOneByObjectId(String rObjectId);

  public List<ReqTakeoutDetailDto> reqTakeoutAll();

  public List<ReqTakeoutDetailDto> reqTakeoutDetailAll();

  public List<ReqTakeoutDetailDto> reqTakeoutListByObjId(String docId);

  public Optional<TakeoutConfig> selectReasonOneByDeptCode(String deptCode);

  public List<ReqTakeoutDoc> selectOneByReqId(String reqId);

  public List<ReqTakeoutDetailDto> selectReqTakeoutDetails(ReqTakeout takeout);
  
  public List<TakeoutConfigRepeating> selectRepeatingList(String rObjectId);

  public int selectCountByReqDocId(String uReqId);

  public List<ReqTakeout> selectListByDeptCode(String deptCode, ReqTakeoutDto dto);

  public List<ReqTakeoutDto> selectListByReqId(String reqId);

  public List<String> selectAuthNameListByObjId(String objectId);
  
  public List<String> selectFreeNameListByObjId(String objectId);

  public boolean checkTakeoutDoc(String docId, String approveId);
  
  public String selectTakeoutDocIdByDocKey(String docKey);
}
