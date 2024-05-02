package com.dongkuksystems.dbox.services.cache;

import java.util.List;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.dongkuksystems.dbox.daos.custom.dmformat.DmFormatDao;
import com.dongkuksystems.dbox.models.dto.custom.dmformat.DmFormat;
import com.dongkuksystems.dbox.services.AbstractCommonService;
import com.dongkuksystems.dbox.utils.CommonUtils;

@Service
public class CacheServiceImpl extends AbstractCommonService implements CacheService {
  private final DmFormatDao dmFormatDao;
  private final CacheManager cacheManager;

  public CacheServiceImpl(DmFormatDao dmFormatDao, CacheManager cacheManager) {
    this.dmFormatDao = dmFormatDao;
    this.cacheManager = cacheManager;
  }

  @Override
  @Cacheable(value = "selectDmFormats")
  public List<DmFormat> selectDmFormats() {
    return dmFormatDao.selectAll();
  }

  /**
   * @apiNote 캐시초기화
   * @param CacheManager cacheManager
   * @return void
   */
  @Override
  public void clearCaches() {
//    CommonUtils.evictAllCaches(cacheManager);
//    this.initSelectDmFormats();
  }

  @Override
  @CacheEvict(value = "selectDmFormats", allEntries = true)
  public void initSelectDmFormats() {
    logger.info("selectDmFormats");
  }

  @Override
  @CacheEvict(value = "selectDepts", allEntries = true)
  public void initSelectDepts() {
    logger.info("initSelectDepts");
  }

  @Override
  @CacheEvict(value = "selectDeptTree", allEntries = true)
  public void initSelectDeptTree() {
    logger.info("initSelectDeptTree");
  }
  
  @Override
  @CacheEvict(value = "selectManageIdTree", allEntries = true)
  public void initSelectMangeIdTree() {
    logger.info("initSelectMangeIdTree");
  }

  @Override
  @CacheEvict(value = "selectDeptPath", allEntries = true)
  public void initSelectDeptPath() {
    logger.info("initSelectDeptPath");
  }

  @Override
  @CacheEvict(value = "selectDeptChildren", allEntries = true)
  public void initSelectDeptChildren() {
    logger.info("initSelectDeptChildren");
  }

  @Override
  @CacheEvict(value = "selectDeptByOrgId", allEntries = true)
  public void initSelectDeptByOrgId() {
    logger.info("initSelectDeptByOrgId");
  }
  
  @Override
  @CacheEvict(value = "selectGwDeptByOrgId", allEntries = true)
  public void initSelectGwDeptByOrgId() {
    logger.info("initSelectGwDeptByOrgId");
  }

  @Override
  @CacheEvict(value = "selectDeptCodeByCabinetcode", allEntries = true)
  public void initSelectDeptCodeByCabinetcode() {
    logger.info("initSelectDeptCodeByCabinetcode");
  }
  
  @Override
  @CacheEvict(value = "selectDeptChildrenByOrgId", allEntries = true)
  public void initSelectDeptChildrenByOrgId() {
    logger.info("initSelectDeptChildrenByOrgId");
  }

  @Override
  @CacheEvict(value = "selectComCodeByCabinetCode", allEntries = true)
  public void initSelectComCodeByCabinetCode() {
    logger.info("initSelectComCodeByCabinetCode");
  }

  @Override
  @CacheEvict(value = "selectOrgIdByCabinetcode", allEntries = true)
  public void initSelectOrgIdByCabinetcode() {
    logger.info("initSelectOrgIdByCabinetcode");
  }

  @Override
  @CacheEvict(value = "selectUserListOfPart", allEntries = true)
  public void initSelectUserListOfPart() {
    logger.info("initSelectUserListOfPart");
  }
  
  @Override
  @CacheEvict(value = "selectTemplates", allEntries = true)
  public void initSelectTemplates() {
    logger.info("initSelectTemplates");
  }
}
