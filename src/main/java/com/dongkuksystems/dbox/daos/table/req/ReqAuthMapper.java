package com.dongkuksystems.dbox.daos.table.req;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.dongkuksystems.dbox.models.type.request.ReqAuth;

public interface ReqAuthMapper {
	
	public List<ReqAuth> reqAuthAll();
	public ReqAuth dataByObjId( @Param("rObjectId") String rObjectId);
	public List<ReqAuth> reqAuthDetailAll();
}
