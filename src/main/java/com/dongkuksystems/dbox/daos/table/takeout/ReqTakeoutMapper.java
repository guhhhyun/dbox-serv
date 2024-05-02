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

public interface ReqTakeoutMapper {
	public List<ReqTakeoutDetailDto> reqTakeoutAll();
    public List<ReqTakeoutDetailDto> reqTakeoutDetailAll();
    public Optional<ReqTakeout> selectOneByObjectId(@Param("rObjectId") String rObjectId);
    public List<ReqTakeoutDetailDto> reqTakeoutListByObjId(@Param("rObjectId") String rObjectId);
    public Optional<TakeoutConfig> selectReasonOneByDeptCode(@Param("uDeptCode") String deptCode);
    public List<ReqTakeoutConfigDto> nameListByObjId(@Param("rObjectId") String rObjectId);
    public List<ReqTakeoutConfigDto> nameListByOrgId(@Param("orgId") String orgId);
    public List<String> authNameListByObjId(@Param("rObjectId") String rObjectId);
    public List<String> freeNameListByObjId(@Param("rObjectId") String rObjectId);
    public List<ReqTakeoutDoc> selectOneByReqId(@Param("uReqId") String reqId);
    public List<ReqTakeoutDetailDto> selectReqTakeoutDetails(@Param("takeout") ReqTakeout takeout);
    public List<ReqTakeoutDetailDto> selectCountByReqDocId(@Param("takeout") ReqTakeout takeout);
    public int selectCountByReqDocId(@Param("reqDocId") String reqDocId);
    public List<TakeoutConfigRepeating> selectRepeatingList(String rObjectId);
    public List<ReqTakeout> selectListByDeptCode(@Param("deptCode") String deptCode, @Param("takeout") ReqTakeoutDto dto);
    public List<ReqTakeoutDto> selectListByReqId(@Param("uReqId") String reqId);
    public boolean checkTakeoutDoc(@Param("docId") String docId, @Param("approveId") String approveId);
    public String selectTakeoutDocIdByDocKey(@Param("docKey") String docKey);
}
