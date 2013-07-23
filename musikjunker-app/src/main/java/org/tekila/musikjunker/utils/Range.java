package org.tekila.musikjunker.utils;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class Range {
	private final long start;
	private final long end;
	private final long length;
	private final long total;

	public Range(long start, long end, long total) {
		this.start = start;
		this.end = end;
		this.length = end - start + 1;
		this.total = total;
	}

}
