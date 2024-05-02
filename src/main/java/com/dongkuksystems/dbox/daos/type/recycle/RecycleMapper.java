package com.dongkuksystems.dbox.daos.type.recycle;

import java.util.List;
import java.util.Optional;

import org.apache.ibatis.annotations.Param;

import com.dongkuksystems.dbox.models.dto.type.doc.DocFilterDto;
import com.dongkuksystems.dbox.models.dto.type.recycle.DocRecycleDto;
import com.dongkuksystems.dbox.models.dto.type.recycle.FolRecycleDto;
import com.dongkuksystems.dbox.models.dto.type.recycle.RecycleDetailDto;
import com.dongkuksystems.dbox.models.type.doc.Doc;
import com.dongkuksystems.dbox.models.type.recycle.Recycle;

public interface RecycleMapper {
	
	public List<Recycle> getDeletedDataList();
	public List<Recycle> docAuthorizedDetailList( @Param("userId") String userId, @Param("level") int level, @Param("uCabinetCode") String uCabinetCode);
	public List<Recycle> folAuthorizedDetailList(@Param("userId") String userId, @Param("uCabinetCode") String uCabinetCode);
	public List<Recycle> oneDocById(@Param("dataId") String dataId);
	public Recycle oneRecycleById(@Param("dataId") String dataId);
	public List<Recycle> oneFolById(@Param("dataId") String dataId);
	public List<Recycle> oneRsrhById(@Param("dataId") String dataId);
	public List<Recycle> onePjtById(@Param("dataId") String dataId);
    public List<Recycle> projectList();
    public List<Recycle> researchList();
    public Optional<Recycle> selectRecycleCaCode(@Param("orgId")String orgId);
 
	
}
