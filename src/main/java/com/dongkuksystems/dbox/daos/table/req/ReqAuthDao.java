package com.dongkuksystems.dbox.daos.table.req;

import java.util.List;

import com.dongkuksystems.dbox.models.type.request.ReqAuth;

public interface ReqAuthDao {
	
	public List<ReqAuth> reqAuthAll();
	public ReqAuth dataByObjId(String rObjectId);
	public List<ReqAuth> reqAuthDetailAll();
	
}
