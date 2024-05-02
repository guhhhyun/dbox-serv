package com.dongkuksystems.dbox.daos.type.recycle;

import java.util.List;
import java.util.Optional;

import org.apache.ibatis.annotations.Param;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import com.dongkuksystems.dbox.models.dto.type.doc.DocFilterDto;
import com.dongkuksystems.dbox.models.dto.type.recycle.DocRecycleDto;
import com.dongkuksystems.dbox.models.dto.type.recycle.FolRecycleDto;
import com.dongkuksystems.dbox.models.dto.type.recycle.RecycleDetailDto;
import com.dongkuksystems.dbox.models.type.doc.Doc;
import com.dongkuksystems.dbox.models.type.recycle.Recycle;

@Primary
@Repository
public class RecycleDaoImpl implements RecycleDao {
	private final RecycleMapper recycleMapper;
	
	public RecycleDaoImpl(RecycleMapper recycleMapper) {
		this.recycleMapper = recycleMapper;
	}

	@Override
	public List<Recycle> getDeletedDataList() {
	
		return recycleMapper.getDeletedDataList();
	}
	
	@Override
	  public List<Recycle> docAuthorizedDetailList( String userId, int level, String uCabinetCode) {
			return recycleMapper.docAuthorizedDetailList( userId, level, uCabinetCode);
		}

	@Override
	public List<Recycle> folderAuthorizedDetailList(String userId, String uCabinetCode) {
		
		return recycleMapper.folAuthorizedDetailList(userId, uCabinetCode);
	}

	@Override
	public List<Recycle> oneDocById(String dataId) {
		
		return recycleMapper.oneDocById(dataId);
	}
	
	@Override
  public List<Recycle> onePjtById(String dataId) {

    return recycleMapper.onePjtById(dataId);
  }

  @Override
  public List<Recycle> oneRsrhById(String dataId) {

    return recycleMapper.oneRsrhById(dataId);
  }

  @Override
  public Recycle oneRecycleById(String dataId) {
    
    return recycleMapper.oneRecycleById(dataId);
  }
	
	@Override
  public List<Recycle> oneFolById(String dataId) {
    
    return recycleMapper.oneFolById(dataId);
  }

  @Override
  public List<Recycle> projectList() {

    return recycleMapper.projectList();
  }

  @Override
  public List<Recycle> researchList() {

    return recycleMapper.researchList();
  }

@Override
public Optional<Recycle> selectRecycleCaCode(String orgId) {
	return recycleMapper.selectRecycleCaCode(orgId);
}
	
	
}
