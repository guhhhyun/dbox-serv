package com.dongkuksystems.dbox.daos.type.recycle;

import java.util.List;
import java.util.Optional;

import com.dongkuksystems.dbox.models.dto.type.doc.DocFilterDto;
import com.dongkuksystems.dbox.models.dto.type.recycle.DocRecycleDto;
import com.dongkuksystems.dbox.models.dto.type.recycle.FolRecycleDto;
import com.dongkuksystems.dbox.models.dto.type.recycle.RecycleDetailDto;
import com.dongkuksystems.dbox.models.type.doc.Doc;
import com.dongkuksystems.dbox.models.type.recycle.Recycle;

public interface RecycleDao {
	public List<Recycle> getDeletedDataList();
	public List<Recycle> docAuthorizedDetailList( String userId, int level, String uCabinetCode);
	public List<Recycle> folderAuthorizedDetailList(String dUserId, String uCabinetCode);
	public List<Recycle> oneDocById(String dataId);
	public Recycle oneRecycleById(String dataId);
	public List<Recycle> oneFolById(String dataId);
	public List<Recycle> onePjtById(String dataId);
	public List<Recycle> oneRsrhById(String dataId);
	public List<Recycle> projectList();
	public List<Recycle> researchList();
	public Optional<Recycle> selectRecycleCaCode(String orgId);
}
