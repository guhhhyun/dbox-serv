package com.dongkuksystems.dbox.utils;

import java.util.Date;
import java.util.Properties;

import javax.annotation.PostConstruct;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class MailSenderUtils {
		private String MAIL_TRANSPORT_PROTOCOL;
    private String MAIL_TRANSPORT_PROTOCOL_VALUE;
    private String MAIL_SMTP_HOST;
    private String MAIL_SMTP_HOST_VALUE;
    private String MAIL_SMTP_PORT;
    private String MAIL_SMTP_PORT_VALUE;
    // private String MAIL_SMTP_AUTH;
    // private String MAIL_SMTP_AUTH_VALUE;
    // private String MAIL_SMTP_SSL_ENABLE;
    // private String MAIL_SMTP_SSL_ENABLE_VALUE;
    // private String MAIL_SMTP_SSL_TRUST;
    // private String MAIL_SMTP_SSL_TRUST_VALUE;
    private static final String TEXT_TYPE_HTML								= "html";
//    @Value("${dbox.mail.transport-protocol}")
//    private String MAIL_TRANSPORT_PROTOCOL;
//    @Value("${dbox.mail.transport-protocol-value}")
//    private String MAIL_TRANSPORT_PROTOCOL_VALUE;
//    @Value("${dbox.mail.smtp-host}")
//    private String MAIL_SMTP_HOST;
//    @Value("${dbox.mail.smtp-host-value}")
//    private String MAIL_SMTP_HOST_VALUE;
//    @Value("${dbox.mail.smtp-port}")
//    private String MAIL_SMTP_PORT;
//    @Value("${dbox.mail.smtp-port-value}")
//    private String MAIL_SMTP_PORT_VALUE;
    
    
    public MailSenderUtils(
      @Value("${dbox.mail.transport-protocol}") String MAIL_TRANSPORT_PROTOCOL,
      @Value("${dbox.mail.transport-protocol-value}") String MAIL_TRANSPORT_PROTOCOL_VALUE,
      @Value("${dbox.mail.smtp-host}") String MAIL_SMTP_HOST,
      @Value("${dbox.mail.smtp-host-value}") String MAIL_SMTP_HOST_VALUE,
      @Value("${dbox.mail.smtp-port}") String MAIL_SMTP_PORT,
      @Value("${dbox.mail.smtp-port-value}") String MAIL_SMTP_PORT_VALUE)
      // @Value("${dbox.mail.smtp-auth}") String MAIL_SMTP_AUTH,
      // @Value("${dbox.mail.smtp-auth-value}") String MAIL_SMTP_AUTH_VALUE,
      // @Value("${dbox.mail.smtp-ssl-enable}") String MAIL_SMTP_SSL_ENABLE,
      // @Value("${dbox.mail.smtp-ssl-enable-value}") String MAIL_SMTP_SSL_ENABLE_VALUE,
      // @Value("${dbox.mail.smtp-ssl-trust}") String MAIL_SMTP_SSL_TRUST,
      // @Value("${dbox.mail.smtp-ssl-trust-value}") String MAIL_SMTP_SSL_TRUST_VALUE) 
      {
        this.MAIL_TRANSPORT_PROTOCOL = MAIL_TRANSPORT_PROTOCOL;
        this.MAIL_TRANSPORT_PROTOCOL_VALUE = MAIL_TRANSPORT_PROTOCOL_VALUE;
        this.MAIL_SMTP_HOST = MAIL_SMTP_HOST;
        this.MAIL_SMTP_HOST_VALUE = MAIL_SMTP_HOST_VALUE;
        this.MAIL_SMTP_PORT = MAIL_SMTP_PORT;
        this.MAIL_SMTP_PORT_VALUE = MAIL_SMTP_PORT_VALUE;
        // this.MAIL_SMTP_AUTH = MAIL_SMTP_AUTH;
        // this.MAIL_SMTP_AUTH_VALUE = MAIL_SMTP_AUTH_VALUE;
        // this.MAIL_SMTP_SSL_ENABLE = MAIL_SMTP_SSL_ENABLE;
        // this.MAIL_SMTP_SSL_ENABLE_VALUE = MAIL_SMTP_SSL_ENABLE_VALUE;
        // this.MAIL_SMTP_SSL_TRUST = MAIL_SMTP_SSL_TRUST;
        // this.MAIL_SMTP_SSL_TRUST_VALUE = MAIL_SMTP_SSL_TRUST_VALUE;
      }
    
    @PostConstruct
    public void init() {
      log.info("[SMTP configuration] MAIL_TRANSPORT_PROTOCOL       : " + MAIL_TRANSPORT_PROTOCOL      );
      log.info("[SMTP configuration] MAIL_TRANSPORT_PROTOCOL_VALUE : " + MAIL_TRANSPORT_PROTOCOL_VALUE);
      log.info("[SMTP configuration] MAIL_SMTP_HOST                : " + MAIL_SMTP_HOST               );
      log.info("[SMTP configuration] MAIL_SMTP_HOST_VALUE          : " + MAIL_SMTP_HOST_VALUE         );
      log.info("[SMTP configuration] MAIL_SMTP_PORT                : " + MAIL_SMTP_PORT               );
      log.info("[SMTP configuration] MAIL_SMTP_PORT_VALUE          : " + MAIL_SMTP_PORT_VALUE         );      
      // log.info("[SMTP configuration] MAIL_SMTP_AUTH                : " + MAIL_SMTP_AUTH               );
      // log.info("[SMTP configuration] MAIL_SMTP_AUTH_VALUE          : " + MAIL_SMTP_AUTH_VALUE         );
      // log.info("[SMTP configuration] MAIL_SMTP_SSL_ENABLE          : " + MAIL_SMTP_SSL_ENABLE         );
      // log.info("[SMTP configuration] MAIL_SMTP_SSL_ENABLE_VALUE    : " + MAIL_SMTP_SSL_ENABLE_VALUE   );
      // log.info("[SMTP configuration] MAIL_SMTP_SSL_TRUST           : " + MAIL_SMTP_SSL_TRUST          );
      // log.info("[SMTP configuration] MAIL_SMTP_SSL_TRUST_VALUE     : " + MAIL_SMTP_SSL_TRUST_VALUE    );
    }
	
	/**
    *
    * @param recipients
    *            N명
    *            받는 사람의 이메일 주소
    * @param subject
    *            메일 제목
    * @param bodyHtml
    *            메일 본문(HTML) 메시지
    * @param sender
    *            보내는 사람의 이메일주소
    */
   public void sendMailForHtml(String recipients[], String subject, String bodyHtml, String sender) throws Exception {
     if (recipients == null || recipients.length == 0) {
       throw new RuntimeException("no mail recipents");
     }
     sendMail(TEXT_TYPE_HTML, recipients, subject, bodyHtml, sender);
   }
   
   private void sendMail(String textType, String recipients[], String subject, String message, String sender) throws Exception {
     InternetAddress[] addressTo = new InternetAddress[recipients.length];
     for (int i = 0; i < recipients.length; i++) {
         addressTo[i] = new InternetAddress(recipients[i]);
     }
     sendMail(textType, addressTo, subject, message, new InternetAddress(sender));
   }
   
   private void sendMail(String textType, InternetAddress recipients[], String subject, String message, InternetAddress sender) throws Exception {
     Properties props = new Properties();
     props.put(MAIL_SMTP_HOST, MAIL_SMTP_HOST_VALUE);
     props.put(MAIL_SMTP_PORT, MAIL_SMTP_PORT_VALUE);     
    //  props.put(MAIL_SMTP_AUTH, MAIL_SMTP_AUTH_VALUE);
    //  props.put(MAIL_SMTP_SSL_ENABLE, MAIL_SMTP_SSL_ENABLE_VALUE);
    //  props.put(MAIL_SMTP_SSL_TRUST, MAIL_SMTP_SSL_TRUST_VALUE);
      Session session = Session.getDefaultInstance(props);

    //  Session session = Session.getDefaultInstance(props, new Authenticator() {
    //    protected PasswordAuthentication getPasswordAuthentication() {
    //      try {
    //        String id = "iris.admin@dongkuksystems.com";
    //        String pwd = "initpw1!";
    //        return new PasswordAuthentication(id, pwd);
    //      }catch(Exception e) {
    //        return null;
    //      }
    //    }
    //  });
     
     Message msg = new MimeMessage(session);
    //  InternetAddress senderTest = new InternetAddress("iris.admin@dongkuksystems.com");
    //  msg.setFrom(senderTest);
    msg.setFrom(sender);
     if (recipients.length > 1) {
         msg.setRecipients(Message.RecipientType.TO, recipients);
     } else if (recipients.length == 1) {
         msg.setRecipient(Message.RecipientType.TO, recipients[0]);
     } else {
         // 받는 사람이 존재하지 않습니다...
         throw new Exception("받는 사람이 존재하지 않습니다...");
     }
     msg.setSentDate(new Date());
     msg.setSubject(subject);
     if (TEXT_TYPE_HTML.equals(textType)) {
         msg.setContent(message, "text/html;charset=utf-8");
     } else {
         msg.setContent(message, "text/plain;charset=utf-8");
     }
     Transport.send(msg);
   }
	
//	public static void main(String[] args) {
//		MailSenderUtils mailSender = new MailSenderUtils();
//		mailSender.init();
//    String recipients[] = new String[] { "soik.cha@dongkuk.com"};
//    String subject = "D'box에서 보내는 메일 테스트 입니다.";
//    String message = "<h1>HTML메일보내기 테스트 입니다.</h1><br/><span>테스트~~</span>";
//    String sender = "hojin.kang@dongkuk.com";
//    
//    try {
//      mailSender.sendMailForHtml(recipients, subject, message, sender);
//    } catch (Exception e) {
//    	e.printStackTrace();
//    }
//	}
}
