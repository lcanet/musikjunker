package org.tekila.musikjunker.vo;

import java.util.List;

import lombok.Data;

import org.tekila.musikjunker.domain.Resource;

@Data
public class SearchResults {

	private int index;
	private int size;
	private List<Resource> results;
}
