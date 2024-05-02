package com.dongkuksystems.dbox.services.isdelete;

import org.springframework.stereotype.Service;

import com.dongkuksystems.dbox.daos.type.doc.DocDao;
import com.dongkuksystems.dbox.daos.type.folder.FolderDao;
import com.dongkuksystems.dbox.daos.type.path.PathDao;
import com.dongkuksystems.dbox.errors.BadRequestException;
import com.dongkuksystems.dbox.models.type.doc.Doc;
import com.dongkuksystems.dbox.models.type.docbox.Project;
import com.dongkuksystems.dbox.models.type.docbox.Research;
import com.dongkuksystems.dbox.models.type.folder.Folder;
import com.dongkuksystems.dbox.services.AbstractCommonService;
import com.dongkuksystems.dbox.services.project.ProjectService;
import com.dongkuksystems.dbox.services.research.ResearchService;

@Service
public class IsDeleteServiceImpl  extends AbstractCommonService implements IsDeleteService {
  private final DocDao docDao;
  private final FolderDao folderDao;
  private final PathDao pathDao;
  private final ProjectService projectService;
  private final ResearchService researchService;
  
  public IsDeleteServiceImpl(DocDao docDao, FolderDao folderDao, PathDao pathDao, ProjectService projectService,
      ResearchService researchService) {
    this.docDao = docDao;
    this.folderDao = folderDao;
    this.pathDao = pathDao;
    this.projectService = projectService;
    this.researchService = researchService;
  }


  @Override
  public Boolean isDelete(String docId) throws Exception {
    Doc docData = docDao.selectOne(docId).orElse(new Doc());
    Folder folData = folderDao.selectOne(docData.getUFolId()).orElse(new Folder());
    Project project = projectService.selectProjectByUPjtCode(docData.getUPrCode()).orElse(new Project());
    Research research = researchService.selectResearchByURschCode(docData.getUPrCode()).orElse(new Research());
    if(!(" ".equals(docData.getUFolId()))) {
      if("D".equals(folData.getUDeleteStatus()) || "P".equals(folData.getUDeleteStatus()) || folData.getRObjectId() == null
          || !(docData.getUPrCode().equals(folData.getUPrCode())) || !(docData.getUCabinetCode().equals(folData.getUCabinetCode()))) {
        return true;
      }else {
        return false;
      }
    }else {
      if("P".equals(docData.getUPrType())) {
        if("Y".equals(project.getUDeleteStatus()) || project.getRObjectId() == null) {         
          return true;
        }else {
          return false;
        }
      }else if("R".equals(docData.getUPrType())) {
        if("Y".equals(research.getUDeleteStatus()) || research.getRObjectId() == null) {          
          return true;
        }else {
          return false;
        }
      }else {
        return false;
      }
    }
  }


  @Override
  public Boolean isDeleteFol(String folId) throws Exception{
    Folder folData = folderDao.selectOne(folId).orElse(new Folder());
    String folderPath = pathDao.selectFolderPath(folData.getRObjectId());
    String realFolPath = folderPath.replace("/", "");
    Project project = projectService.selectProjectByUPjtCode(folData.getUPrCode()).orElse(new Project());
    Research research = researchService.selectResearchByURschCode(folData.getUPrCode()).orElse(new Research());
    if(!(" ".equals(folData.getUUpFolId()))) {
      Folder upfolData = folderDao.selectOne(folData.getUUpFolId()).orElse(new Folder());
      if("D".equals(upfolData.getUDeleteStatus()) || "P".equals(upfolData.getUDeleteStatus()) || upfolData.getRObjectId() == null
          || !(folData.getUPrCode().equals(upfolData.getUPrCode())) || !(folData.getUCabinetCode().equals(upfolData.getUCabinetCode()))) {
          return true;
      }else {
        return false;
      }
    }else {
      if("P".equals(folData.getUPrType())) {
        if("Y".equals(project.getUDeleteStatus()) || project.getRObjectId() == null) {         
          return true;
        }else {
          return false;
        }
      }else if("R".equals(folData.getUPrType())) {
        if("Y".equals(research.getUDeleteStatus()) || research.getRObjectId() == null) {          
          return true;
        }else {
          return false;
        }
      }else {
        return false;
      }
    }
  }
  
  
}
