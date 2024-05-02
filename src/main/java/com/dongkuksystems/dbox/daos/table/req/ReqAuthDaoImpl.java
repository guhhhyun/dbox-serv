package com.dongkuksystems.dbox.daos.table.req;

import java.util.List;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import com.dongkuksystems.dbox.models.type.request.ReqAuth;


@Primary
@Repository
public class ReqAuthDaoImpl implements ReqAuthDao{

	private ReqAuthMapper reqAuthMapper;
	
	public ReqAuthDaoImpl(ReqAuthMapper reqAuthMapper) {
		this.reqAuthMapper = reqAuthMapper;
	}
	
	
	@Override
	public List<ReqAuth> reqAuthAll() {
		
		return reqAuthMapper.reqAuthAll();
		
	}


	@Override
	public ReqAuth dataByObjId(String rObjectId) {
		
		return reqAuthMapper.dataByObjId(rObjectId);
	}


	@Override
	public List<ReqAuth> reqAuthDetailAll() {
		
		return reqAuthMapper.reqAuthDetailAll();
	}
	
	
}
