package com.dongkuksystems.dbox.daos.type.user.preset;

import java.util.List;
import java.util.Optional;

import com.dongkuksystems.dbox.models.dto.type.user.UserPresetFilterDto;
import com.dongkuksystems.dbox.models.dto.type.user.UserPresetRepeatingDto;
import com.dongkuksystems.dbox.models.type.user.UserPreset;
import com.dongkuksystems.dbox.models.type.user.UserPresetDetail;
import com.dongkuksystems.dbox.models.type.user.UserPresetRepeating; 


public interface UserPresetDao {
	public Optional<UserPreset> selectOneByUserId(String uUserId); 
  public List<UserPreset> selectOneByFilter(UserPresetFilterDto userPresetFilterDto); 
  public List<UserPresetDetail> selectDetailedOneByFilter(UserPresetFilterDto userPresetFilterDto); 
	public List<UserPreset> selectList(UserPresetFilterDto userPresetFilterDto);
	public List<UserPresetRepeating> selectDetail(String rObjectId);
	public int selectUserPresetCount(String rObjectId, UserPresetRepeatingDto dto);
	public List<UserPresetRepeating> selectRepeatingList(UserPresetFilterDto userPresetFilterDto);
	public List<UserPresetRepeating> selectRepeatingDetailList(UserPresetFilterDto userPresetFilterDto);
}
