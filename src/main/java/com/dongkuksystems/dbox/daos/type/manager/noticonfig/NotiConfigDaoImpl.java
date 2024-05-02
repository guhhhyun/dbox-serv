package com.dongkuksystems.dbox.daos.type.manager.noticonfig;

import java.util.List;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import com.dongkuksystems.dbox.models.type.manager.noticonfig.NotiConfig;

@Primary
@Repository
public class NotiConfigDaoImpl implements NotiConfigDao{
	private NotiConfigMapper alarmMapper;
	
	public NotiConfigDaoImpl(NotiConfigMapper alarmMapper) {
		this.alarmMapper = alarmMapper;
	}
		
	  @Override
	  public List<NotiConfig> selectAll(String uComCode) {
	    return alarmMapper.selectAll(uComCode);
	  }

    @Override
    public NotiConfig selectOneByCodes(String uComCode, String uEventCode) {
      return alarmMapper.selectOneByCodes(uComCode, uEventCode);
    }
	  
	

}
