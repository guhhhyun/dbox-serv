package com.dongkuksystems.dbox.models.common;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


/**
 * 공통 메일 객체
 */
public class Mail implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private static final String EMPTY = "";
	
	public Mail() {
		super();
	}
	
	public Mail(String from) {
		super();
		this.from = from;
	}
	
	public Mail(String from, String fromName) {
		super();
		this.from = from;
		this.fromName = fromName;
	}

	/**
	 * 콘텐츠가 HTML 인지 여부
	 */
	private boolean isHtml = true;
	
	public boolean isHtml() {
		return isHtml;
	}

	public void setHtml(boolean isHtml) {
		this.isHtml = isHtml;
	}
	
	/**
	 * 메일 발송자 mail 주소
	 */
	private String from;
	
	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}
	
	public void setFrom(String from, String fromName) {
		this.from = from;
		this.fromName = fromName;
	}
	
	/**
	 * 메일 발송자 이름
	 */
	private String fromName;
	
	public String getFromName() {
		return fromName;
	}
	
	/**
	 * 받는 사람의 메일 주소
	 */
	private List<String> to = new ArrayList<String>();
	
	public List<String> getTo() {
		return to;
	}
	
	public void setTo(String to) {
		this.to.add(to);
		this.toName.add(EMPTY);
	}
	
	public void setTo(String to, String toName) {
		this.to.add(to);
		this.toName.add(toName);
	}

	public void addTo(String to) {
		this.to.add(to);
		this.toName.add(EMPTY);
	}
	
	public void addTo(String to, String toName) {
		this.to.add(to);
		this.toName.add(toName);
	}
	
	/**
	 * 받는 사람 이름
	 */
	private List<String> toName = new ArrayList<String>();
	
	public String getToName(int index) {
		return toName.get(index);
	}
	
	/**
	 * 참조 메일 주소
	 */
	private List<String> cc = new ArrayList<String>();
	
	public List<String> getCc() {
		return cc;
	}

	public void addCc(String cc) {
		this.cc.add(cc);
		this.ccName.add(EMPTY);
	}
	
	public void addCc(String cc, String ccName) {
		this.cc.add(cc);
		this.ccName.add(ccName);
	}
	
	/**
	 * 참조자 이름
	 */
	private List<String> ccName = new ArrayList<String>();
	
	public String getCcName(int index) {
		return ccName.get(index);
	}
	
	/**
	 * 숨은 참조 메일 주소
	 */
	private List<String> bcc = new ArrayList<String>();
	
	public List<String> getBcc() {
		return bcc;
	}

	public void addBcc(String bcc) {
		this.bcc.add(bcc);
		this.bccName.add(EMPTY);
	}
	
	public void addBcc(String bcc, String bccName) {
		this.bcc.add(bcc);
		this.bccName.add(bccName);
	}
	
	/**
	 * 숨은 참조자 이름
	 */
	private List<String> bccName = new ArrayList<String>();
	
	public String getBccName(int index) {
		return bccName.get(index);
	}
	
	/**
	 * 제목
	 */
	private String subject;
	
	public String getSubject() {
		return subject;
	}
	
	public void setSubject(String subject) {
		this.subject = subject;
	}
	
	/**
	 * 본문 내용
	 */
	private String contents;
	
	public String getContents() {
		return contents;
	}

	public void setContents(String contents) {
		this.contents = contents;
	}
	
	/**
	 * 첨부파일
	 */
	private List<File> attachment = new ArrayList<File>();
	
	public List<File> getAttachment() {
		return attachment;
	}

	public void addAttachment(File attachment) {
		this.attachment.add(attachment);
		this.attachmentName.add(EMPTY);
	}
	
	public void addAttachment(File attachment, String attachmentName) {
		this.attachment.add(attachment);
		this.attachmentName.add(attachmentName);
	}
	
	
	/**
	 * 첨부파일 이름
	 */
	private List<String> attachmentName = new ArrayList<String>();
	
	public String getAttachmentName(int index) {
		return attachmentName.get(index);
	}
	

	/**
	 * 컨텐츠 타입
	 */
	private String contentType;
	
	public String getContentType() {
		return contentType;
	}
	
	public void setContentType(String contentType) {
		this.contentType = contentType;
	}
	
	/**
	 * Mime-Type
	 */
	private String mimeType;

	public String getMimeType() {
		return mimeType;
	}

	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}

	/**
	 * 사이트별 메일 정보를 불러오기 위한 프로퍼티
	 */

	/**
	 * 메일 서버 주소
	 */
	private String host;

	/**
	 * 메일 서버 PORT
	 */
	private int port;

	/**
	 * 메일 관리자 ID
	 */
	private String userName;

	/**
	 * 메일 관리자 비밀번호
	 */
	private String password;

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
}
