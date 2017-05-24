package com.springer.watermark;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class SpringerWatermarkApplicationTests {
	private static final Logger log = LoggerFactory.getLogger(SpringerWatermarkApplicationTests.class);
	@Autowired
	private JmsTemplate jmsTemplate;
	
/*	@Test
	public void contextLoads() {
	}
*/	
	@Test
	public void ticketChecks(){
		Map<String, String> document = new HashMap<>();
		document.put("TICKET", "hello world");
		//JmsTemplate jmsTemplate = context.getBean(JmsTemplate.class);
		jmsTemplate.convertAndSend("watermark", document);
		
		// This code is synchronous, but just for testing as the service is actually asynchronous
		Map received = (Map) jmsTemplate.receiveAndConvert("outbound.queue");
		String errorMessage = (String) received.get("ERROR");
		assertThat(errorMessage).isEqualTo("Ticket not recognized.");
	}

	@Test
	public void bookWatermarking()
	{
		Map<String, String> document = new HashMap<>();		
		document.put("AUTHOR", "Ian Fleming");
		document.put("TITLE", "James Bond");
		document.put("TOPIC", "FICTION");
		document.put("URL","file://home/incoming/books");
		document.put("CONTENT","BOOK");
		jmsTemplate.convertAndSend("watermark", document);
		
		// This code is synchronous, but just for testing as the service is actually asynchronous
		Map received = (Map) jmsTemplate.receiveAndConvert("outbound.queue");
		String ticket = (String) received.get("TICKET");
		
		// with this call we'll receive the status as "processing"
		document.clear();
		document.put("TICKET", ticket);
		jmsTemplate.convertAndSend("watermark", document);
		// This code is synchronous, but just for testing as the service is actually asynchronous
		received = (Map) jmsTemplate.receiveAndConvert("outbound.queue");
		log.info("response " + received);

		// with this call we'll get the content
		jmsTemplate.convertAndSend("watermark", document);
		// This code is synchronous, but just for testing as the service is actually asynchronous
		received = (Map) jmsTemplate.receiveAndConvert("outbound.queue");
		String status = (String) received.get("STATUS");
		log.info("response " + received);
		assertThat(status).isEqualTo("watermarked");
		
	}
}
