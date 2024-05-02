package com.dongkuksystems.dbox.daos.type.user.preset;

import java.util.List;
import java.util.Optional;

import org.apache.ibatis.annotations.Param;

import com.dongkuksystems.dbox.models.dto.type.user.UserPresetFilterDto;
import com.dongkuksystems.dbox.models.dto.type.user.UserPresetRepeatingDto;
import com.dongkuksystems.dbox.models.type.user.UserPreset;
import com.dongkuksystems.dbox.models.type.user.UserPresetDetail;
import com.dongkuksystems.dbox.models.type.user.UserPresetRepeating; 

public interface UserPresetMapper { 
  public Optional<UserPreset> selectOneByUserId(@Param("uUserId") String uUserId);
  public List<UserPreset> selectOneByFilter(@Param("userPreset") UserPresetFilterDto userPresetFilterDto);
  public List<UserPresetDetail> selectDetailedOneByFilter(@Param("userPreset") UserPresetFilterDto userPresetFilterDto);
  public List<UserPreset> selectList(@Param("userPreset") UserPresetFilterDto userPresetFilterDto);
	public List<UserPresetRepeating> selectDetail(@Param("rObjectId") String rObjectId);
	public int selectUserPresetCount(@Param("rObjectId") String rObjectId, @Param("userPresetRepeating") UserPresetRepeatingDto dto);
	public List<UserPresetRepeating> selectRepeatingList(@Param("userPreset") UserPresetFilterDto userPresetFilterDto);
	public List<UserPresetRepeating> selectRepeatingDetailList(@Param("userPreset") UserPresetFilterDto userPresetFilterDto);
	public List<String> selectLiveReadAuthor(@Param("rObjectId") String rObjectId);
	public List<String> selectLiveDeleteAuthor(@Param("rObjectId") String rObjectId);
	public List<String> selectClosedReadAuthor(@Param("rObjectId") String rObjectId);
}
