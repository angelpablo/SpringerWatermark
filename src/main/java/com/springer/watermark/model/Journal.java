/**
 * 
 */
package com.springer.watermark.model;

import java.net.URL;

import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * @author Angel Arenas
 *
 */
@Entity
public final class Journal {
	@Id
	private String ticket;
	private URL documentLocation;
	
	private String title;
	private String author;
	
	private boolean watermarked;

	protected Journal() {}
	
	// In a real app we should use the Builder pattern to avoid this clumsy constructor
	public Journal(String ticket, URL documentLocation, String title, String author) {
		super();
		this.ticket = ticket;
		this.documentLocation = documentLocation;
		this.title = title;
		this.author = author;
	}

	public boolean isWatermarked() {
		return watermarked;
	}

	public void setWatermarked(boolean watermarked) {
		this.watermarked = watermarked;
	}

	public String getTicket() {
		return ticket;
	}

	public URL getDocumentLocation() {
		return documentLocation;
	}

	public String getTitle() {
		return title;
	}

	public String getAuthor() {
		return author;
	}

	
	
}
