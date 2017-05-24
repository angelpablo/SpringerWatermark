/**
 * 
 */
package com.springer.watermark.business;

import java.net.URL;
import java.util.Map;

import com.springer.watermark.TicketException;

/**
 * @author Angel Arenas
 *
 */
public interface WatermarkProcessor {
	// we're doind this since Book and Journal don't share a common ancestry, and
	// also they don't fit the profile of a common "Document" interface either
	
	boolean isDocumentWatermarked(String ticket) throws TicketException;
	
	String watermarkDocument(Map<String,Object> document);
	
	
	URL getWatermarkedDocument(String ticket) throws TicketException;
}
