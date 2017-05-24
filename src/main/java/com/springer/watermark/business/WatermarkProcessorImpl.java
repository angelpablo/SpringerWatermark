/**
 * 
 */
package com.springer.watermark.business;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.springer.watermark.ContentType;
import com.springer.watermark.TicketException;
import com.springer.watermark.model.Book;
import com.springer.watermark.model.BookRepository;
import com.springer.watermark.model.Journal;
import com.springer.watermark.model.JournalRepository;

/**
 * @author Angel Arenas
 *
 */
public class WatermarkProcessorImpl implements WatermarkProcessor {
	private static final String TICKET = "TICKET";
	private static final String AUTHOR = "AUTHOR";
	private static final String TITLE = "TITLE";
	private static final String TOPIC = "TOPIC";
	private static final String URL = "URL";
	private static final String CONTENT = "CONTENT";
	
	private static final Logger log = LoggerFactory.getLogger(WatermarkProcessorImpl.class);

	
	@Autowired
	private BookRepository bookRepository;
	
	@Autowired
	private JournalRepository JournalRepository;

	private static final AtomicInteger sequential = new AtomicInteger(1);
	
	/* (non-Javadoc)
	 * @see com.springer.watermark.business.WatermarkProcessor#watermarkBook(com.springer.watermark.model.Book)
	 */
	private void watermarkBook(Book book) {
		// this is just a simulation of watermarking process
		Map<String, String> watermarkMap = new LinkedHashMap<>();
		watermarkMap.put("content", "book");
		watermarkMap.put("title", book.getTitle());
		watermarkMap.put("author", book.getAuthor());
		watermarkMap.put("topic", book.getTopic());
		ObjectMapper mapper = new ObjectMapper();
		String watermark = null;
		try {
			watermark = mapper.writeValueAsString(watermarkMap);
		} catch (JsonProcessingException e) {
			// we're not doing anything hese as it's not decided yet the behaviour in this case
			e.printStackTrace();
		}
		
		// here we're just setting the watermark property to indicate watermarking is complete.
		// In real life app, probably the watermarking would be a BPM solution, or automated using
		// iText for example. 
		book.setWatermarked(true);
		bookRepository.save(book);
		
		log.info(watermark);
	}

	/* (non-Javadoc)
	 * @see com.springer.watermark.business.WatermarkProcessor#watermakJournal(com.springer.watermark.model.Journal)
	 */
	private void watermakJournal(Journal journal) {
		// this is just a simulation of watermarking process
		Map<String, String> watermarkMap = new LinkedHashMap<>();
		watermarkMap.put("content", "journal");
		watermarkMap.put("title", journal.getTitle());
		watermarkMap.put("author", journal.getAuthor());
		ObjectMapper mapper = new ObjectMapper();
		String watermark = null;
		try {
			watermark = mapper.writeValueAsString(watermarkMap);
		} catch (JsonProcessingException e) {
			// we're not doing anything hese as it's not decided yet the behaviour in this case
			e.printStackTrace();
		}
		
		// here we're just setting the watermark property to indicate watermarking is complete.
		// In real life app, probably the watermarking would be a BPM solution, or automated using
		// iText for example. 
		journal.setWatermarked(true);
		JournalRepository.save(journal);
		
		log.info(watermark);
	}

	private ContentType getContentType(String ticket) throws TicketException
	{
		String[] ticketFields = ticket.split("_");
		if(ticketFields.length != 3)
		{
			throw new TicketException("Ticket not recognized.");
		}
		
		ContentType contentType = ContentType.valueOf(ticketFields[0]);
		return contentType;
	}
	
	@Override
	public boolean isDocumentWatermarked(String ticket) throws TicketException {
		ContentType contentType = getContentType(ticket);
		switch (contentType)
		{
			case BOOK:
				Book book = bookRepository.findOne(ticket);
				if(book == null)
				{
					throw new TicketException("Book ticket not recognized.");
				}
				if(book.isWatermarked())
				{
					return true;
				}
				//here's where we simulate a call to BPM, or something that will delegate 
			    // the actual watermarking. 		
				watermarkBook(book);  //get the book ready for the next call
				return false;
			case JOURNAL:
				Journal journal = JournalRepository.findOne(ticket);
				if(journal == null)
				{
					throw new TicketException("Journal ticket not recognized.");
				}
				if(journal.isWatermarked())
				{
					return true;
				}
				//here's where we simulate a call to BPM, or something that will delegate 
			    // the actual watermarking. 		
				watermakJournal(journal);  //simulating getting ready for next call
				return false;
			// so here we can expand onto more type of documents	
			default:
				throw new TicketException("Content type not processable yet");
		}

	}

	@Override
	public URL getWatermarkedDocument(String ticket) throws TicketException {
		ContentType contentType = getContentType(ticket);
		
		// TODO This is missing all the null checking, on a real app we should take care of it  :)
		switch(contentType)
		{
			case BOOK:
				Book book = bookRepository.findOne(ticket);
				if(book.isWatermarked())
				{
					return book.getDocumentLocation();
				}
				return null;
			case JOURNAL:
				Journal journal = JournalRepository.findOne(ticket);
				if(journal.isWatermarked())
				{
					return journal.getDocumentLocation();
				}
				return null;
		}
		throw new TicketException("Ticket not recognized");
	}

	@Override
	public String watermarkDocument(Map<String, Object> document) {
		// in a real life app we should be validating all these entries
		String content = (String) document.get(CONTENT);
		String author = (String) document.get(AUTHOR);
		String title = (String) document.get(TITLE);
		URL url = null;
		try {
			url = new URL((String)document.get(URL));
		} catch (MalformedURLException e) {
			// TODO deal with this in real life app
			e.printStackTrace();
		}
		String topic = (String) document.get(TOPIC);
		Date today = GregorianCalendar.getInstance().getTime();
		Integer suffix = sequential.getAndIncrement();
		ContentType contentType = ContentType.valueOf(content);
		String ticket = null;
		
		switch(contentType)
		{  // this will only represent the beginning of the processing, not the actual watermarking
			case BOOK:
				ticket = ContentType.BOOK + "_" + today.getTime() + "_" + suffix;
				Book book = new Book(ticket, url, title, author, topic);
				bookRepository.save(book);
				break;
			case JOURNAL:
				ticket = ContentType.JOURNAL + "_" + today.getTime() + "_" + suffix;
				Journal journal = new Journal(ticket, url, title, author);
				JournalRepository.save(journal);
		}
		return ticket;
	}

}
