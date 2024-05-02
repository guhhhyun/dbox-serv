package com.dongkuksystems.dbox.daos.table.takeout;

import java.util.List;
import java.util.Optional;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import com.dongkuksystems.dbox.models.dto.type.request.ReqTakeoutConfigDto;
import com.dongkuksystems.dbox.models.dto.type.request.ReqTakeoutDetailDto;
import com.dongkuksystems.dbox.models.dto.type.request.ReqTakeoutDto;
import com.dongkuksystems.dbox.models.type.manager.TakeoutConfig;
import com.dongkuksystems.dbox.models.type.manager.TakeoutConfigRepeating;
import com.dongkuksystems.dbox.models.type.request.ReqTakeout;
import com.dongkuksystems.dbox.models.type.request.ReqTakeoutDoc;

@Primary
@Repository
public class ReqTakeoutDaoImpl implements ReqTakeoutDao {
  private ReqTakeoutMapper reqTakeoutMapper;

  public ReqTakeoutDaoImpl(ReqTakeoutMapper reqTakeoutMapper) {
    this.reqTakeoutMapper = reqTakeoutMapper;
  }

  @Override
  public Optional<TakeoutConfig> selectReasonOneByDeptCode(String deptCode) {

    return reqTakeoutMapper.selectReasonOneByDeptCode(deptCode);
  }

  @Override
  public List<ReqTakeoutDoc> selectOneByReqId(String reqId) {

    return reqTakeoutMapper.selectOneByReqId(reqId);
  }

  @Override
  public List<ReqTakeoutConfigDto> nameListByObjId(String objectId) {

    return reqTakeoutMapper.nameListByObjId(objectId);
  }

  @Override
  public List<ReqTakeoutConfigDto> nameListByOrgId(String orgId) {

    return reqTakeoutMapper.nameListByOrgId(orgId);
  }

  @Override
  public Optional<ReqTakeout> selectOneByObjectId(String rObjectId) {

    return reqTakeoutMapper.selectOneByObjectId(rObjectId);
  }

  @Override
  public List<ReqTakeoutDetailDto> reqTakeoutDetailAll() {

    return reqTakeoutMapper.reqTakeoutDetailAll();
  }

  @Override
  public List<ReqTakeoutDetailDto> reqTakeoutListByObjId(String takeoutRequestId) {

    return reqTakeoutMapper.reqTakeoutListByObjId(takeoutRequestId);
  }

  @Override
  public List<ReqTakeoutDetailDto> reqTakeoutAll() {

    return reqTakeoutMapper.reqTakeoutAll();

  }

  @Override
  public List<ReqTakeoutDetailDto> selectReqTakeoutDetails(ReqTakeout takeout) {
    return reqTakeoutMapper.selectReqTakeoutDetails(takeout);
  }

  @Override
  public List<TakeoutConfigRepeating> selectRepeatingList(String rObjectId) {
    return reqTakeoutMapper.selectRepeatingList(rObjectId);
  }
  public int selectCountByReqDocId(String uReqId) {
    return reqTakeoutMapper.selectCountByReqDocId(uReqId);
  }

  @Override
  public List<ReqTakeout> selectListByDeptCode(String deptCode, ReqTakeoutDto dto) {
    return reqTakeoutMapper.selectListByDeptCode(deptCode, dto);
  }

  @Override
  public List<ReqTakeoutDto> selectListByReqId(String reqId) {
    return reqTakeoutMapper.selectListByReqId(reqId);
  }

  @Override
  public List<String> selectAuthNameListByObjId(String objectId) {
    return reqTakeoutMapper.authNameListByObjId(objectId);
  }

  @Override
  public List<String> selectFreeNameListByObjId(String objectId) {
    return reqTakeoutMapper.freeNameListByObjId(objectId);
  }
  
  @Override
  public boolean checkTakeoutDoc(String docId, String approveId) {
    return reqTakeoutMapper.checkTakeoutDoc(docId, approveId);
  }
  
  @Override
  public String selectTakeoutDocIdByDocKey(String docKey) {
    return reqTakeoutMapper.selectTakeoutDocIdByDocKey(docKey);
  }
}
