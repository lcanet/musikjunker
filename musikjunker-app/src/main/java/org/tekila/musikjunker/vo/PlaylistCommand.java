package org.tekila.musikjunker.vo;

import lombok.Data;

/**
 * Playlist modification command
 *
 */
@Data
public class PlaylistCommand {

	private PlaylistHeader header;
	private Long[] addResources;
	private Long[] deleteResources;
	
}
