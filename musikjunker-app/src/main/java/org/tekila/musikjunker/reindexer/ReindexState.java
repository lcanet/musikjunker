package org.tekila.musikjunker.reindexer;

import java.util.Date;

import lombok.Data;

@Data
public class ReindexState {

	private Date lastLaunch;
	private Date end;
	private boolean ok;
	private boolean running;
	private String message;
	
	
}
