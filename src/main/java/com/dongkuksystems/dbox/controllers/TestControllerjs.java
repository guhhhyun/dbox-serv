package com.dongkuksystems.dbox.controllers;

import static com.dongkuksystems.dbox.models.api.response.ApiResult.OK;

import java.io.File;
import java.io.InputStream;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.codec.binary.Base64;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.MultipartRequest;
import org.springframework.web.util.WebUtils;

import com.dongkuksystems.dbox.constants.Commons;
import com.dongkuksystems.dbox.models.api.response.ApiResult;
import com.dongkuksystems.dbox.models.common.UserSession;
import com.dongkuksystems.dbox.models.dto.type.upload.AttachedFile;
import com.dongkuksystems.dbox.models.dto.type.upload.UploadDocDto;
import com.dongkuksystems.dbox.securities.JwtAuthentication;
import com.dongkuksystems.dbox.services.agent.AgentService;
import com.dongkuksystems.dbox.services.doc.DocService;
import com.dongkuksystems.dbox.utils.dctm.AES256Util;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;


@Controller("TestController")
public class TestControllerjs {
  @Autowired
  private DocService docService;
  
  @Autowired
  private AgentService agentService;
  
  //@Autowired
  //private BCryptPasswordEncoder bcPass;
  
  @RequestMapping(value = {"/pcexplorer/doc/hook/EDMSSaveWW2"}, method = {RequestMethod.POST})
  @ResponseBody
  public JSONObject IFDoCheckIn(HttpServletRequest request) throws Exception {
	  
	  
	  
	  
	  
	  


	  String line = "F| 203| \"d:\\서류 관련\\제출서류\\등본_양병주(서울).pdf\"| Access(Tue Jul 20 15:43:36 2021) | Modification(Tue Jul 20 09:54:19 2021) | Creation(Tue Jul 20 15:43:36 2021) | Size(713 KB)|";
	  
	  String[] asLine 		= line.split("|");
		String sFileName 		= asLine[2].trim();
		String sAccessDate		= asLine[1].trim();
		String sModifyDate		= asLine[1].trim();
		String sCreateDate		= asLine[4].trim();
		String sFileSize		= asLine[4].trim();
		
		int sFileCreateDate1 	= Integer.parseInt(asLine[2].trim().substring(0, 4));
		int sFileCreateDate2 	= Integer.parseInt(asLine[2].trim().substring(4, 6));
		int sFileCreateDate3 	= Integer.parseInt(asLine[2].trim().substring(6, 8));
		
		int sFileModifyDate1	= Integer.parseInt(asLine[3].trim().substring(0, 4));
		int sFileModifyDate2	= Integer.parseInt(asLine[3].trim().substring(4, 6));
		int sFileModifyDate3	= Integer.parseInt(asLine[3].trim().substring(6, 8));
		
		int sFileAccessDate1	= Integer.parseInt(asLine[3].trim().substring(0, 4));
		int sFileAccessDate2	= Integer.parseInt(asLine[3].trim().substring(4, 6));
		int sFileAccessDate3	= Integer.parseInt(asLine[3].trim().substring(6, 8));
	  
	  
		LocalDateTime.parse(sFileSize);
	  
	  
	  
	  
	  
	  // AES256 테스트...
	  /*
	  AES256Util testAes256 = new AES256Util();
	  
	  String sPwd1 = testAes256.encrypt("0900123456789001");
	  String sPwd2 = testAes256.encrypt("0900123456789002");
	  String sPwd3 = testAes256.encrypt("0900123456789003");
	  
	  String sPwdmail = testAes256.encrypt("mail.com");
	  
	  String sPwd4 = testAes256.decrypt(sPwd1);
	  String sPwd5 = testAes256.decrypt(sPwd2);
	  String sPwd6 = testAes256.decrypt(sPwd3);
	  String sPwdmail2 = testAes256.decrypt(sPwdmail);
	  
	  System.out.println(sPwd1);
	  System.out.println(sPwd2);
	  System.out.println(sPwd3);
	  
	  System.out.println(sPwd4);
	  System.out.println(sPwd5);
	  System.out.println(sPwd6);
	  
	  String sPwd7 = URLEncoder.encode(sPwd1, StandardCharsets.UTF_8.toString());
	  String sPwd8 = URLEncoder.encode(sPwd2, StandardCharsets.UTF_8.toString());
	  String sPwd9 = URLEncoder.encode(sPwd3, StandardCharsets.UTF_8.toString());
	  String sPwdmailurl = URLEncoder.encode(sPwdmail, StandardCharsets.UTF_8.toString());
	  
	  
	  
	  
	  
	  String sPwd10 = URLDecoder.decode(sPwd7, StandardCharsets.UTF_8.toString());
	  String sPwd11 = URLDecoder.decode(sPwd8, StandardCharsets.UTF_8.toString());
	  String sPwd12 = URLDecoder.decode(sPwd9, StandardCharsets.UTF_8.toString());
	  
	  String sPwdmailurlde = URLDecoder.decode(sPwdmailurl, StandardCharsets.UTF_8.toString());
	  
	  System.out.println(sPwd7);
	  System.out.println(sPwd8);
	  System.out.println(sPwd9);
	  
	  System.out.println(sPwd10);
	  System.out.println(sPwd11);
	  System.out.println(sPwd12);
	  
	  
	  System.out.println(sPwdmail);
	  System.out.println(sPwdmail2);
	  
	  System.out.println(sPwdmailurl);
	  System.out.println(sPwdmailurlde);
	  */
	  
		/*
		 * String sss1 = bcPass.encode("090012345"+"DBOX"); String sss2 =
		 * bcPass.encode("090012346"+"DBOX");
		 * 
		 * System.out.println(sss1); System.out.println(sss2);
		 * 
		 * 
		 * System.out.println(sss2);
		 */
	  
	  
	  
    JSONObject toAgentReturnJson = new JSONObject();
    byte[] decodedUserId = null;
    String user_id = null;
    String request_user_id = request.getParameter("user_id");
    String r_object_id = request.getParameter("r_object_id");
    String file_name = request.getParameter("file_name");
    String file_path = request.getParameter("file_path");
    if (request_user_id.equals("") || request_user_id == null) {
      toAgentReturnJson.put("errcode", "-1");
      toAgentReturnJson.put("errmsg", "user_id 가누락");
    	      return toAgentReturnJson;
    } 
    if (r_object_id.equals("") || r_object_id == null) {
      toAgentReturnJson.put("errcode", "-1");
      toAgentReturnJson.put("errmsg", "r_object_id누락");
      return toAgentReturnJson;
    } 
    decodedUserId = Base64.decodeBase64(request_user_id);
    user_id = new String(decodedUserId, "UTF-8");
    
    MultipartHttpServletRequest multipartHttpServletRequest = WebUtils.getNativeRequest(request, MultipartHttpServletRequest.class);
    
    // MultipartHttpServletRequest multipartHttpServletRequest = (MultipartHttpServletRequest)request;
    MultipartFile file = multipartHttpServletRequest.getFile("file");
    
    //MultipartFile multipartHttpServletRequest = (MultipartFile)request.mu;
    // multipartHttpServletRequest.
    // MultipartFile file = file.getFile("file");
    
    
    InputStream inputStream = file.getInputStream();
    JSONObject fromDBoxReturnJson = null; // this.docService.doCheckIn(user_id, r_object_id, inputStream, file_path, file_name);
    if (fromDBoxReturnJson.get("return_code").equals("0")) {
      toAgentReturnJson.put("errcode", "0");
      toAgentReturnJson.put("errmsg", fromDBoxReturnJson.get("return_msg"));
    } else if (fromDBoxReturnJson.get("return_code").equals("-1")) {
      toAgentReturnJson.put("errcode", "-1");
      toAgentReturnJson.put("errmsg", fromDBoxReturnJson.get("return_msg"));
    } else {
      toAgentReturnJson.put("errcode", "-1");
      toAgentReturnJson.put("errmsg", "return_code가 누락");
    } 
    return toAgentReturnJson;
  }
  
  
    
  	// @PostMapping(value = "/pcexplorer/doc/hook/EDMSSaveWWW", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  	@RequestMapping(value = {"/pcexplorer/doc/hook/EDMSSaveWWW2"}, method = {RequestMethod.POST})
  	@ResponseBody
	public JSONObject postDataContent(
			//MultipartFile file
			MultipartHttpServletRequest request
			) throws Exception {

  		JSONObject toAgentReturnJson = new JSONObject();
		
  		// request.
  		//InputStream inputStream = file.getInputStream();
  		/*
  	    JSONObject fromDBoxReturnJson = this.docService.doCheckIn(user_id, r_object_id, inputStream, file_path, file_name);
  	    if (fromDBoxReturnJson.get("return_code").equals("0")) {
  	      toAgentReturnJson.put("errcode", "0");
  	      toAgentReturnJson.put("errmsg", fromDBoxReturnJson.get("return_msg"));
  	    } else if (fromDBoxReturnJson.get("return_code").equals("-1")) {
  	      toAgentReturnJson.put("errcode", "-1");
  	      toAgentReturnJson.put("errmsg", fromDBoxReturnJson.get("return_msg"));
  	    } else {
  	      toAgentReturnJson.put("errcode", "-1");
  	      toAgentReturnJson.put("errmsg", "return_code가 누락");
  	    } 
  	    */
  	    return toAgentReturnJson;
	}
  
  	@RequestMapping({"/pcexplorer/doc/common/checkout22"})
    @ResponseBody
    public JSONObject IFDoDownload(HttpServletRequest request) throws Exception {
      JSONObject toAgentReturnJson = new JSONObject();
      byte[] decodedUserId = null;
      String user_id = null;
      String request_user_id = request.getParameter("user_id");
      String r_object_id = request.getParameter("r_object_id");
      String flag = request.getParameter("flag");
      String syspath = request.getParameter("syspath");
      if (r_object_id.equals("")) {
        toAgentReturnJson.put("file_path", "");
        toAgentReturnJson.put("file_name", "");
        toAgentReturnJson.put("errcode", "-1");
        toAgentReturnJson.put("errmsg", "누락1");
        return toAgentReturnJson;
      } 
      if (request_user_id.equals("") || request_user_id == null) {
        toAgentReturnJson.put("file_path", "");
        toAgentReturnJson.put("file_name", "");
        toAgentReturnJson.put("errcode", "-1");
        toAgentReturnJson.put("errmsg", "누락2");
        return toAgentReturnJson;
      } 
      
      // request_user_id = Base64.encodeBase64String(request_user_id.getBytes());
      
      decodedUserId = Base64.decodeBase64(request_user_id);
      user_id = new String(decodedUserId, "UTF-8");
      JSONObject fromDBoxReturnJson = this.agentService.checkOut(user_id, r_object_id, flag, syspath, "");
      if (fromDBoxReturnJson.get("return_code").equals("0")) {
        toAgentReturnJson.put("file_path", fromDBoxReturnJson.get("file_path"));
        toAgentReturnJson.put("file_name", fromDBoxReturnJson.get("file_name"));
        toAgentReturnJson.put("errcode", "0");
        toAgentReturnJson.put("errmsg", fromDBoxReturnJson.get("return_msg"));
      } else if (fromDBoxReturnJson.get("return_code").equals("-1")) {
        toAgentReturnJson.put("file_path", "");
        toAgentReturnJson.put("file_name", "");
        toAgentReturnJson.put("errcode", "-1");
        toAgentReturnJson.put("errmsg", fromDBoxReturnJson.get("return_msg"));
      } else {
        toAgentReturnJson.put("file_path", "");
        toAgentReturnJson.put("file_name", "");
        toAgentReturnJson.put("errcode", "-1");
        toAgentReturnJson.put("errmsg", "누락3");
      } 
      return toAgentReturnJson;
    }
  	
  	@RequestMapping(value = {"/pcexplorer/doc/hook/EDMSSaveW22"}, method = {RequestMethod.POST})
    @ResponseBody
    public JSONObject IFDoCheckIn2(HttpServletRequest request) throws Exception {
      JSONObject toAgentReturnJson = new JSONObject();
      String user_id = request.getParameter("user_id");
      String r_object_id = request.getParameter("r_object_id");
      if (r_object_id.equals("") || r_object_id == null) {
        toAgentReturnJson.put("errcode", "-1");
        toAgentReturnJson.put("errmsg", "누락1");
        return toAgentReturnJson;
      } 
      MultipartHttpServletRequest multipartHttpServletRequest = (MultipartHttpServletRequest)request;
      MultipartFile file = multipartHttpServletRequest.getFile("file");
      InputStream inputStream = file.getInputStream();
      String path = "C:/work/tmp/ECM/file";
      String fileName = file.getOriginalFilename();
      String tmpFileName = "";
      String tmpFileExt = "";
      int i = fileName.lastIndexOf(".");
      if (i <= 0) {
        tmpFileName = fileName;
        tmpFileExt = "";
      } else {
        tmpFileName = fileName.substring(0, i);
        tmpFileExt = fileName.substring(i + 1, fileName.length());
      } 
      String nowDate = (new SimpleDateFormat("yyyyMMddHHmmss")).format(new Date());
      String realFileName = tmpFileName + "_" + nowDate + "." + tmpFileExt;
      //File tmpFile = new File(path + "/" + realFileName);
      //file.transferTo(tmpFile);
      JSONObject fromDBoxReturnJson = this.agentService.doCheckIn(user_id, r_object_id, inputStream, path, realFileName, "", "");
      if (fromDBoxReturnJson.get("return_code").equals("0")) {
        toAgentReturnJson.put("errcode", "0");
        toAgentReturnJson.put("errmsg", fromDBoxReturnJson.get("return_msg"));
      } else if (fromDBoxReturnJson.get("return_code").equals("-1")) {
        toAgentReturnJson.put("errcode", "-1");
        toAgentReturnJson.put("errmsg", fromDBoxReturnJson.get("return_msg"));
      } else {
        toAgentReturnJson.put("errcode", "-1");
        toAgentReturnJson.put("errmsg", "누락2");
      } 
      return toAgentReturnJson;
    }
  	
  	@RequestMapping(value = {"/pcexplorer/doc/hook/sendLog22222"}, method = {RequestMethod.POST})
    @ResponseBody
    public JSONObject IFSendLog(HttpServletRequest request) throws Exception {
  		
      JSONObject toAgentReturnJson = new JSONObject();
      JSONObject fromDBoxReturnJson = new JSONObject();
      byte[] decodedUserId = null;
      String user_id = null;
      decodedUserId = Base64.decodeBase64(request.getParameter("user_id"));
      user_id = new String(decodedUserId, "UTF-8");
      String ip = request.getParameter("ip");
      String log_type = request.getParameter("log_type");
      String date_time = request.getParameter("date_time");
      String file_size = request.getParameter("file_size");
      if (log_type.equals("0")) {
        MultipartHttpServletRequest multipartHttpServletRequest = (MultipartHttpServletRequest)request;
        MultipartFile file = multipartHttpServletRequest.getFile("file");
        InputStream inputStream = file.getInputStream();
        fromDBoxReturnJson = this.agentService.sendLog(user_id, ip, log_type, inputStream, file_size, date_time);
      } else if (log_type.equals("1")) {
        String dest_path = request.getParameter("dest_path");
        String src_path = request.getParameter("src_path");
        String action = request.getParameter("action");
        fromDBoxReturnJson = this.agentService.sendLog(user_id, ip, log_type, dest_path, src_path, action, file_size, date_time);
      } 
      if (fromDBoxReturnJson.get("return_code").equals("-1")) {
        toAgentReturnJson.put("errcode", "-1");
        toAgentReturnJson.put("errmsg", fromDBoxReturnJson.get("return_msg"));
      } else if (fromDBoxReturnJson.get("return_code").equals("0")) {
        toAgentReturnJson.put("errcode", "0");
        toAgentReturnJson.put("errmsg", fromDBoxReturnJson.get("return_msg"));
      } else {
        toAgentReturnJson.put("errcode", "-1");
        toAgentReturnJson.put("errmsg", "return_code");
      } 
      return toAgentReturnJson;
    }
  
}
