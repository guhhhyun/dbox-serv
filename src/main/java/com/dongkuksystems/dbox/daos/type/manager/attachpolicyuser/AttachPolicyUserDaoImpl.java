package com.dongkuksystems.dbox.daos.type.manager.attachpolicyuser;

import java.util.List;
import java.util.Optional;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import com.dongkuksystems.dbox.models.dto.type.manager.attachpolicy.AttachPolicyUserDto;
import com.dongkuksystems.dbox.models.type.manager.AttachPolicy;
import com.dongkuksystems.dbox.models.type.manager.AttachPolicyUser;

@Primary
@Repository
public class AttachPolicyUserDaoImpl implements AttachPolicyUserDao {

  private AttachPolicyUserMapper attachPolicyUserMapper;

  public AttachPolicyUserDaoImpl(AttachPolicyUserMapper attachPolicyUserMapper) {
    this.attachPolicyUserMapper = attachPolicyUserMapper;
  }

  @Override
  public List<AttachPolicyUser> selectAll(AttachPolicyUserDto dto) {
    return attachPolicyUserMapper.selectAll(dto);
  }

  @Override
  public List<AttachPolicyUser> selectEndAttachUser(String uPolicyId) {
    return attachPolicyUserMapper.selectEndAttachUser(uPolicyId);
  }
  
  @Override
  public List<AttachPolicyUser> selectDeletePolicyUser(String uPolicyId) {
    return attachPolicyUserMapper.selectDeletePolicyUser(uPolicyId);
  }

}
