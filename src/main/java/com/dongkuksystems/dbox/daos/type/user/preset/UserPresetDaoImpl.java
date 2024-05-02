package com.dongkuksystems.dbox.daos.type.user.preset;

import java.util.List;
import java.util.Optional;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import com.dongkuksystems.dbox.models.dto.type.user.UserPresetFilterDto;
import com.dongkuksystems.dbox.models.dto.type.user.UserPresetRepeatingDto;
import com.dongkuksystems.dbox.models.type.user.UserPreset;
import com.dongkuksystems.dbox.models.type.user.UserPresetDetail;
import com.dongkuksystems.dbox.models.type.user.UserPresetRepeating; 

@Primary
@Repository
public class UserPresetDaoImpl implements UserPresetDao {
  private UserPresetMapper userPresetMapper;

  public UserPresetDaoImpl(UserPresetMapper userPresetMapper) {
    this.userPresetMapper = userPresetMapper;
  }

  @Override
  public Optional<UserPreset> selectOneByUserId(String uUserId) {	  
	  return userPresetMapper.selectOneByUserId(uUserId);
  }
  
  @Override
  public List<UserPreset> selectOneByFilter(UserPresetFilterDto userPresetFilterDto) {	  
    return userPresetMapper.selectOneByFilter(userPresetFilterDto);
  }
    
  @Override
  public List<UserPreset> selectList(UserPresetFilterDto userPresetFilterDto) {
    return userPresetMapper.selectList(userPresetFilterDto);
  }
  
  @Override
  public List<UserPresetRepeating> selectDetail(String rObjectId) {
    return userPresetMapper.selectDetail(rObjectId);
  }
  
  @Override
  public int selectUserPresetCount(String rObjectId, UserPresetRepeatingDto dto) {
    return userPresetMapper.selectUserPresetCount(rObjectId, dto);
  }
  
  @Override
  public List<UserPresetRepeating> selectRepeatingList(UserPresetFilterDto userPresetFilterDto) {
  	return userPresetMapper.selectRepeatingList(userPresetFilterDto);
  }

  @Override
  public List<UserPresetRepeating> selectRepeatingDetailList(UserPresetFilterDto userPresetFilterDto) {
  	return userPresetMapper.selectRepeatingDetailList(userPresetFilterDto);
  }

  @Override
  public List<UserPresetDetail> selectDetailedOneByFilter(UserPresetFilterDto userPresetFilterDto) {
    return userPresetMapper.selectDetailedOneByFilter(userPresetFilterDto);
  }
}
