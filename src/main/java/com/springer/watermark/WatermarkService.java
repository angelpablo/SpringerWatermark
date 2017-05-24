package com.springer.watermark;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Component;

import com.springer.watermark.business.WatermarkProcessor;

@Component
public class WatermarkService {
	private static final String TICKET = "TICKET";
	private static final String ERROR = "ERROR";
	private static final String STATUS = "STATUS";
	private static final String DOCUMENT_URL = "DOCUMENT_URL";
	
	@Autowired
	private WatermarkProcessor watermarkProcessor;
	
	@JmsListener(destination = "watermark", containerFactory = "myFactory")
	@SendTo("outbound.queue")
	public Map<String, Object> receive(Map<String, Object> request)
	{
		Map<String, Object> result = new HashMap<>();
		
		// 1. Check first if client already has a ticket
		// ticket = [CONTENT_TYPE]_[TIMESTAMP]_[SEQUENCIAL]
		String ticket = (String) request.get(TICKET);
		if(ticket != null && !ticket.isEmpty())
		{
			boolean documentWatermarked;
			try {
				documentWatermarked = watermarkProcessor.isDocumentWatermarked(ticket);
				result.put(TICKET, ticket);
				if(documentWatermarked)
				{
					result.put(STATUS, "watermarked");
					URL documentUrl = watermarkProcessor.getWatermarkedDocument(ticket);
					result.put(DOCUMENT_URL, documentUrl);
				}
				else
				{
					result.put(STATUS, "processing");
				}
				return result;
			} catch (TicketException e) {
				e.printStackTrace();
				result.put(ERROR, e.getMessage());
				return result;
			}
		}
		
		// 2. so client is actually sending document to watermark
		ticket = watermarkProcessor.watermarkDocument(request);
		result.put(TICKET, ticket);
		
		return result;
	}
}
