package com.dongkuksystems.dbox.services.agent;

import java.io.InputStream;
import java.util.HashMap;

import org.json.simple.JSONObject;

public interface AgentService {

	/**
	 * 사용자 정책 조회
	 * @param user_id	: 사용자 ID
	 * @return
	 * @throws Exception
	 */
	JSONObject callGetPolicy(String user_id) throws Exception;
	
	/**
	 * 폴더리스트 조회
	 * @param user_id			: 사용자 ID
	 * @param rid				: 상위 폴더 ID
	 * @return
	 * @throws Exception
	 */
	JSONObject callFolderList(String user_id, String rid) throws Exception;
	
	/**
	 * 문서리스트 조회
	 * @param user_id			: 사용자 ID
	 * @param r_folder_id		: 상위 폴더 ID
	 * @param _pageNumber		: 페이지 번호 ( 사용안함 )
	 * @param _pageLineCount	: 페이지 건수 ( 사용안함 )
	 * @return
	 * @throws Exception
	 */
	JSONObject callFileList(String user_id, String r_folder_id, String _pageNumber, String _pageLineCount) throws Exception;
	
	// syspath 있을경우.
	// JSONObject callFileList(String user_id, String r_folder_id, String _pageNumber, String _pageLineCount, String syspath) throws Exception;
	
	/**
	 * 편집여부 체크
	 * @param user_id			: 사용자 ID
	 * @param r_object_id		: 문서 ID
	 * @return
	 * @throws Exception
	 */
	JSONObject isCheckOut(String user_id, String r_object_id) throws Exception;
	
	/**
	 * 파일 CheckOut 처리
	 * 	docCheckOut 이전에 실행됨.
	 * 
	 * @param user_id			: 사용자 ID
	 * @param r_object_id		: 문서 ID
	 * @param ip				: IP 정보
	 * @return
	 * @throws Exception
	 */
	JSONObject docCheckOut(String user_id, String r_object_id, String ip) throws Exception;
	
	/**
	 * 파일다운로드 ( 첨부파일다운, 보기시 다운, 편집시 다운 )
	 * 
	 * @param user_id			: 사용자 ID
	 * @param r_object_id		: 문서 ID
	 * @param flag				: 4일경우 보기  ( 첨부, 편집은 null )
	 * @param syspath			: 첨부정책ID 	 ( mail.naver.com ), 보기 편집시 값 없음.
	 * @param ip				: IP 정보
	 * @return
	 * @throws Exception
	 */
	JSONObject checkOut(String user_id, String r_object_id, String flag, String syspath, String ip) throws Exception;
	
	/**
	 * Cache 최신파일 여부 확인
	 * 
	 * @param user_id			: 사용자 ID	
	 * @param r_object_id		: 문서 ID
	 * @param file_version		: 파일 버전 ( 사용안함 r_object_id 를 이용해서 찾음 )
	 * @return
	 * @throws Exception
	 */
	JSONObject checkCacheFile(String user_id, String r_object_id, String file_version) throws Exception;
	
	/**
	 * 편집완료
	 * 
	 * @param user_id			: 사용자 ID
	 * @param r_object_id		: 문서 ID
	 * @param inputStream		: 편집완료시 파일 Stream 정보
	 * @param path				: 사용안함 ( Agent jar 에서 임시 값 으로 던짐 )
	 * @param realFileName		: PC 로컬 파일명
	 * @param ip				: IP 정보
	 * @return
	 * @throws Exception
	 */
	JSONObject doCheckIn(String user_id, String r_object_id, InputStream inputStream, String path, String realFileName, String ip, String checkin_flag) throws Exception;
	
	/**
	 * 편집완료후 더이상 수정 안할경우 최종 파일 cancelCheckout 처리
	 * 
	 * @param user_id			: 사용자 ID
	 * @param r_object_id		: 문서 ID
	 * @param ip				: IP 정보
	 * @return
	 * @throws Exception
	 */
	JSONObject docCheckOutCancel(String user_id, String r_object_id, String ip) throws Exception;
	
	/**
	 * 
	 * @param user_id			: 사용자 ID
	 * @param ip				: IP 주소	
	 * @param log_type			: 0 : DEVICE_UNKNOWN(파일전송), 1 : USB 로그, 이하 비고 참조
	 * @param inputStream		: 자산 입력 파일 stream
	 * @param file_size			: size
	 * @param date_time			: 로그 생성 날짜 (연월일시분초, 문자열 타입)		20210730121830
	 * 
	 *  property_file			: log_type이 0일때만 자산 파일 스트림을 보냄
	 * 	action					: 로그 행위 0 : copy, 1: move, 2 : delete
	 * 					
	 * @return
	 * @throws Exception
	 */
	JSONObject sendLog(String user_id, String ip, String log_type, InputStream inputStream, String file_size, String date_time) throws Exception;
	
	/**
	 * USB 로그 입력
	 * @param user_id			: 사용자 ID	
	 * @param ip				: IP 주소
	 * @param log_type			: 0 : DEVICE_UNKNOWN(파일전송), 1 : USB 로그, 이하 비고 참조
	 * @param dest_path			: 로그의 행위가 실행된 파일 경로 (log_type이 1일때만 사용(USB 로그일때만 사용))	
	 * @param src_path			: 로그 발생 프로세스 이름 (log_type이 1일때만 사용(USB 로그일때만 사용))		
	 * @param action			: 로그 행위 0 : copy, 1: move, 2 : delete	
	 * @param file_size			: size
	 * @param date_time			: 로그 생성 날짜 (연월일시분초, 문자열 타입)		20210730121830 
	 * @return
	 * @throws Exception
	 */
	JSONObject sendLog(String user_id, String ip, String log_type, String dest_path, String src_path, String action, String file_size, String date_time) throws Exception;
	
	/**
	 * 다른이름으로저장 임시 저장 ( office 등 문서 에서 저장을 D'box 로 저장 할경우 파일 저장전 임시 값 저장 )
	 * @param user_id
	 * @param r_folder_id
	 * @param realFileName
	 * @param ip
	 * @return
	 * @throws Exception
	 */
	JSONObject saveAs(String user_id, String r_folder_id, String realFileName, String ip) throws Exception;
	
	/**
	 * 버전업 가능여부 ( 추가로 checkin 할경우에도 호출함 )
	 * @param user_id			: 사용자 ID
	 * @param r_object_id		: 문서 ID
	 * @return
	 * @throws Exception
	 */
	JSONObject isCheckOutVersion(String user_id, String r_object_id) throws Exception;
	
	/**
	 * 덮어쓰기 가능여부 ( 추가로 checkin 할경우도 호출함 )
	 * @param user_id
	 * @param r_object_id
	 * @return
	 * @throws Exception
	 */
	JSONObject isCheckOutOverwrite(String user_id, String r_object_id) throws Exception;
	
	/**
	 * 다른이름으로 저장 시 폴더일경우 ( validation 체크 )
	 * @param user_id		- 사용자ID
	 * @param r_ObjectName  - 파일명 ( 사용안함 )
	 * @param r_folder_id	- 폴더 RID
	 * @return
	 * @throws Exception
	 */
	JSONObject isSaveAsNewCheck(String user_id, String r_ObjectName, String r_folder_id) throws Exception;

	/**
	 * URL 복사시 자동권한 추가를 위한 syspath 암호화 형식 리턴
	 * 
	 * @param uDocKey		- U_DOC_KEY
	 * @param syspath		- URL 복사시 DBOX 로 사용. 
	 * @return
	 * @throws Exception
	 */
	String makeSyspath(String uDocKey, String syspath) throws Exception; 


	/**
	 * 사용자 아이디로 agent 업데이트 대상 여부 확인
	 * @param user_id			: 사용자 ID
	 * @return
	 * @throws Exception
	 */
	JSONObject updateIdCheck(String user_id) throws Exception;
		

	/**
	 * Agent 현황을 확인할 수 있는 로그를 전송하는 서비스
	 * @param user_id			: 사용자 ID
	 * @param mac_address	    : MAC adress
	 * @param status	        : Agent 행위 (1 : 설치, 2 : 로그인, 3 : 업데이트, 4 : 삭제 )	
     * @param version	        : Agent 버전	
	 * @return
	 * @throws Exception
	 */
	JSONObject createAgentLog( HashMap<String, Object> argInfoVO) throws Exception;
	
}
