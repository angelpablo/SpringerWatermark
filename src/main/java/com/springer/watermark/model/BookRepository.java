/**
 * 
 */
package com.springer.watermark.model;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author Angel Arenas
 *
 */
public interface BookRepository extends JpaRepository<Book, String> {

}
