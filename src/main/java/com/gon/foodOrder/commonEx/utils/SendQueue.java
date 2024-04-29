package com.sharp.common.utils;

import java.util.Base64;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.azure.storage.queue.QueueClient;
import com.azure.storage.queue.QueueServiceClient;
import com.azure.storage.queue.QueueServiceClientBuilder;
import com.azure.storage.queue.models.QueueStorageException;


@Component
public class SendQueue {

	private  static Logger LOGGER = LoggerFactory.getLogger(SendQueue.class);
			
	private @Value("${sms.apikey}")			String SMS_APIKEY;
	private @Value("${sms.projectid}")		String SMS_PROJECT_ID;
	private @Value("${sms.senderid}")		String SMS_SENDER_ID;
	private @Value("${sms.title}")			String SMS_TITLE;
	private @Value("${sms.callbacknum}")	String CALLBACK_NUM;
		
	public SendQueue() {
		// TODO Auto-generated constructor stub
	}

	public static void sendUploadQueue(String fileUrl) {
		// TODO Auto-generated method stub
		
		if(fileUrl != null && !"".equals(fileUrl)) {
			// 스토리지 계정명
			String ACCOUNT_NAME = "mhpsstoracc";
			// 스토리지 sas 키
			String SAS_TOKEN = "";
			// Queue 스토리지 연결 URL
			String queueServiceURL = String.format("https://%s.queue.core.windows.net/%s", ACCOUNT_NAME, SAS_TOKEN);
			// Queue 명
			String queueName = "mh-edu-log-queue";
			// File 스토리지에 저장되는 App Log 파일경로(마운트된 Root 디렉토리 제외)
			//String fileUrl = "/edu/app-logs/tttt.json";
						
			// Queue Storage에 보낼 Message 형식
			String messageText = "{'fileUrl':'"+fileUrl+"'}";
						
			// Queue 스토리지에 Message Send 함수 호출
			addQueueMessage(queueServiceURL, queueName, messageText);
		}
	}	
	
	public static void addQueueMessage
    (String queueServiceURL, String queueName, String messageText)
	{
	    try
	    {
	        // Instantiate a QueueClient which will be
	        // used to create and manipulate the queue	  
	    	// Queue 스토리지 접근을 위한 Client 생성
	        QueueServiceClient queueServiceClient = new QueueServiceClientBuilder().endpoint(queueServiceURL).buildClient();
	        QueueClient queueClient = queueServiceClient.getQueueClient(queueName);	 

			//LOGGER.debug("[addQueueMessage] messageText : "+messageText);
			
	        // Add a message to the queue
	        // Queue 스토리지에 보낼 Message Base64 인코딩
	        String message = Base64.getEncoder().encodeToString(messageText.getBytes());
			
	        // Queue 스토리지에 Message Send
	        queueClient.sendMessage(message);
	    }
	    catch (QueueStorageException e)
	    {
	        // Output the exception message and stack trace
	    	LOGGER.error(e.getMessage());
	    }
	}

	/*
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		// 스토리지 계정명
		String ACCOUNT_NAME = "mhpsstoracc";
		// 스토리지 sas 키
		String SAS_TOKEN = "?sv=2019-02-02&si=uracle&sig=yINeeJ5yXSos0zOgbiMkKfZ4i5bJpu4VlGsa37Ez530%3D";
		// Queue 스토리지 연결 URL
		String queueServiceURL = String.format("https://%s.queue.core.windows.net/%s", ACCOUNT_NAME, SAS_TOKEN);
		// Queue 명
		String queueName = "mh-edu-log-queue";
		// File 스토리지에 저장되는 App Log 파일경로(마운트된 Root 디렉토리 제외)
		String fileUrl = "/edu/app-logs/tttt.json";
		// Queue Storage에 보낼 Message 형식
		String messageText = "{'fileUrl':'"+fileUrl+"'}";
		// Queue 스토리지에 Message Send 함수 호출
		addQueueMessage(queueServiceURL, queueName, messageText);
		
	}
	*/


}
