package org.tekila.musikjunker.vo;

import org.tekila.musikjunker.domain.Playlist;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlaylistHeader {

	private String name;
	private Long id;
	
	public PlaylistHeader(Playlist pl) {
		this.name = pl.getName();
		this.id = pl.getId();
	}
}
