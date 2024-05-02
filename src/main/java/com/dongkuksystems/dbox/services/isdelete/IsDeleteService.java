package com.dongkuksystems.dbox.services.isdelete;

public interface IsDeleteService {

  Boolean isDelete(String docId) throws Exception;

  Boolean isDeleteFol(String folId) throws Exception;

}
