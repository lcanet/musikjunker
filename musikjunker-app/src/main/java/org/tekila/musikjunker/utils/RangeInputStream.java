package org.tekila.musikjunker.utils;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

public class RangeInputStream extends FilterInputStream {
	private long index = 0;
	private final Range range;

	public RangeInputStream(InputStream in, Range range) {
		super(in);
		this.range = range;
	}

	public Range getRange() {
		return range;
	}

	public int read() throws IOException {
		long count;
		long start;

		// skip data before the range
		start = range.getStart();
		while (index < start) {
			// get rid of the data in front
			count = in.skip(start - index);
			index += count;
		}

		// have we reached the end of the range
		if (index > range.getEnd()) {
			return -1;
		}

		int c = in.read();
		if (c == -1) {
			return -1;
		}

		index++;

		return c;
	}
	public int read(byte[] b, int off, int len) throws IOException {
		// from java.io.InputStream implementation
		if (b == null) {
			throw new NullPointerException();
		} else if ((off < 0) || (off > b.length) || (len < 0)
				|| ((off + len) > b.length) || ((off + len) < 0)) {
			throw new IndexOutOfBoundsException();
		} else if (len == 0) {
			return 0;
		}

		long count;
		long start, end;
		int read;

		// skip data before the range
		start = range.getStart();
		while (index < start) {
			// get rid of the data in front
			count = in.skip(start - index);
			index += count;
		}

		// have we reached the end of the range
		end = range.getEnd();
		if (index > end) {
			return -1;
		}

		long available = end - index + 1;

		// only read up to the end of the range
		if (available < len) {
			read = in.read(b, off, (int) available);
		} else {
			read = in.read(b, off, len);
		}

		index += read;

		return read;
	}
}