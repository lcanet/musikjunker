package org.tekila.musikjunker.domain;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import lombok.Data;

@Embeddable
@Data
public class ID3 {

	@Column(length = 255)
	private String title;
	@Column(length = 255)
	private String artist;
	@Column(length = 255)
	private String album;
	@Column(length = 32)
	private String year;
	@Column(length = 255)
	private String comment;
	@Column(length = 255)
	private String genre;
	
	
}
