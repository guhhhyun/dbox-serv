package com.dongkuksystems.dbox.daos.type.manager.hisviewuser;

import java.util.List;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import com.dongkuksystems.dbox.models.dto.type.manager.hisviewuser.HisViewUserFilterDto;
import com.dongkuksystems.dbox.models.type.manager.hisviewuser.HisViewUser; 

@Primary
@Repository
public class HisViewUserDaoImpl implements HisViewUserDao {
  private HisViewUserMapper hisViewUserMapper;

  public HisViewUserDaoImpl(HisViewUserMapper hisViewUserMapper) {
    this.hisViewUserMapper = hisViewUserMapper;
  }

  @Override
  public List<HisViewUser> selectAll(HisViewUserFilterDto hisViewUserFilterDto) {
    return hisViewUserMapper.selectAll(hisViewUserFilterDto);
  }

  
}
