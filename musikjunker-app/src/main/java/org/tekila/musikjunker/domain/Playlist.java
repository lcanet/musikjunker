package org.tekila.musikjunker.domain;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;

import lombok.Data;

import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;

@Data
@Entity
@JsonSerialize(include = Inclusion.NON_NULL)
public class Playlist {


	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;

	@Column(length = 255)
	private String name;
	
	@ManyToMany(fetch = FetchType.LAZY)
	private List<Resource> songs = new ArrayList<>();

}
