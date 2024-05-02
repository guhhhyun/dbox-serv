package com.dongkuksystems.dbox.utils;

import org.springframework.stereotype.Component;

@Component
public class PushUtils {
//	final static int     RETRY        = 3;                                  // 메시지 전송실패시 재시도 횟수
//	final static int     TTL          = 3600;                               // 기기가 비활성화 상태일때 GCM가 메시지를 유효화하는 시간
//
//	private FirebaseApp firebaseApp;
//	
//	@PostConstruct
//	public void init() {
//		// TODO 모바일 에서 푸시 개발 후 적용
////		FileInputStream serviceAccount;
////		try {
////			serviceAccount = new FileInputStream("/opt/keystore/kjj-niris-firebase-adminsdk-zrzw6-c97241006f.json");
////
////			FirebaseOptions options = FirebaseOptions.builder()
////			  .setCredentials(GoogleCredentials.fromStream(serviceAccount))
////			  .build();
////			firebaseApp = FirebaseApp.initializeApp(options);
////		} catch (FileNotFoundException e) {
////			e.printStackTrace();
////		} catch (IOException e) {
////			e.printStackTrace();
////		}
//	}
//
//	@Test
//	public void testFnc() {
//		try{
//			init();
//			sendFCM("cq8f2HEEj5M:APA91bFYBe6AVGyvw7vb81cN-H7frA3sR1iaH_voIhPy42-CpR-LAXqSboy8xzteBfS8Jv_XqDzTLnU1kHyKcxvYBwKTON2gZql3jXBwTnuFB1ITqHs-NKLPpLc-mE7T5CkpMiL7W-DA", "테스트", "테스트.\nasdfasdfasfdasdfasdf\nasfasfasfaeffawf",  "1");	// 차소익
//
//		} catch(Exception e) {
//			System.out.println(e.toString());
//		}
//
//		System.out.println("Test End");
//	}
//
//	public boolean sendFCM(String pushKey, String strTitle, String strMsg,  String pushType){
//		try {
//			AndroidConfig androidConfig = AndroidConfig.builder()
//					.setTtl(TTL)
//					.putData("title", strTitle)
//					.putData("message", strMsg.replace("\n", "<br/>"))
//					.putData("pushType", pushType)
//					.putData("notId", Integer.toString(new Random().nextInt()))
//					.build();
//
//			ApnsConfig apnsConfig = ApnsConfig.builder()
//					.setAps(Aps.builder()
//							.setSound("default")
//							.setBadge(0)
//							.setContentAvailable(true)
//							.setAlert(ApsAlert.builder()
//									.setTitle(strTitle)
//									.setBody(strMsg)
//									.build())
//							.putCustomData("pushType", pushType)
//							.putCustomData("notId", Integer.toString(new Random().nextInt()))
//							.build())
//					.build();
//
//			Message message = Message.builder()
//					.setAndroidConfig(androidConfig)
//					.setApnsConfig(apnsConfig)
//					.setToken(pushKey)
//			    	.build();
//
//			FirebaseMessaging.getInstance(firebaseApp).send(message);
//			return true;
//		} catch (FirebaseMessagingException e) {
//			e.printStackTrace();
//		}
//		return false;
//	}

}
