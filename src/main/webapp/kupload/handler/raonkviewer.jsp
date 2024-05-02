<%@ page contentType="text/html;charset=utf-8"%><%@ page import="com.raonwiz.kupload.RAONKHandler" %><%@page import="com.raonwiz.kupload.util.RAONKParameterVo"%><%
	out.clear();
	
	request.setCharacterEncoding("UTF-8");
	
	RAONKHandler upload = new RAONKHandler();
	
	//디버깅
    //Log Type 설명
    
    //- C : 일반로그 출력(System.out.println 로그 출력)
    //RAONKParameterVo parameterVo = new RAONKParameterVo();
	//parameterVo.setIsDebugMode(true);
    //parameterVo.setLogType("C");
    //parameterVo.setLogLevel("DEBUG");
    //upload.settingVo.setDebugMode(parameterVo);
    
    // - L : 1. Log4j 모듈에 의한 로그 출력(/handler/JAVA 폴더의 log4j.properties 파일을 WEB-INF/classes에 적용)
	//       2. 기존 설정파일이 존재할 경우 /handler/JAVA 폴더의 log4j.properties 파일 내용 중 "# DEXT5 Upload Log" 항목을 기존 설정파일에 적용
	//       3. 기존 설정파일의 위치가 WEB-INF/classes/log4j.properties 경로가 아닐 경우 parameterVo.setLogConfigPath("...")에 해당 경로 설정
	//       4. WEB-INF/lib에 log4j-......jar 파일이 존재하지 않을 경우 DebugMode가 false로 설정됨
    //RAONKParameterVo parameterVo = new RAONKParameterVo();
    //parameterVo.setIsDebugMode(true);
    //parameterVo.setLogType("L");
    //upload.settingVo.setDebugMode(parameterVo);

	//Viewer 페이지의 Encoding을 설정할 때 사용 (기본값 : utf-8)
	//upload.SetEncoding("utf-8");

	String result = upload.Viewer(request, response);

    if(!result.equals("")) {
		out.print(result);
	}
%>