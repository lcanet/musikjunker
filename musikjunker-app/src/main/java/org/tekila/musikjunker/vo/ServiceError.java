package org.tekila.musikjunker.vo;

import lombok.Data;

@Data
public class ServiceError {

	private boolean error = true;
	private String message;
	
	public ServiceError(String msg) {
		this.message = msg;
	}
	
	
}
