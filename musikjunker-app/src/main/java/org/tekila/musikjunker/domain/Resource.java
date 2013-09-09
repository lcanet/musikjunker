package org.tekila.musikjunker.domain;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import lombok.Data;

import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;

@Data
@Entity
@JsonSerialize(include = Inclusion.NON_NULL)
public class Resource {


	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;

	@Enumerated(EnumType.STRING)
	private TypeResource type;
	
	@Column
	@Temporal(TemporalType.TIMESTAMP)
	private Date fileDate;
	
	@Column
	@Temporal(TemporalType.TIMESTAMP)
	private Date addedDate;
	
	@Column(length = 1024)
	private String path;

	@Column(length = 255)
	private String fileName;

	@Column
	private Integer duration;

	@Embedded
	private ID3 metadata = new ID3();
	
	@Column
	private int stars = 0;


}
