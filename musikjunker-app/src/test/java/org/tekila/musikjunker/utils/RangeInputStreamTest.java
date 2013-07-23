package org.tekila.musikjunker.utils;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;

public class RangeInputStreamTest {
	
	private byte[] dataSet;

	@Before
	public void setupDataset() {
		
		dataSet = new byte[255];
		for (int i = 0; i < dataSet.length; i++) {
			dataSet[i] = (byte) (i & 0xff);
		}
		
	}
	
	@Test
	public void subRangeStart() throws IOException {
			
		// start
		Range r = new Range(0, 9, 255);
		RangeInputStream rin = new RangeInputStream(new ByteArrayInputStream(dataSet), r);
		byte[] b = IOUtils.toByteArray(rin);
		assertEquals(10, b.length);
		assertEquals(0, b[0] & 0xff);
		assertEquals(9, b[9] & 0xff);
		
	}

	@Test
	public void subRangeMiddle() throws IOException {
			
		// start
		Range r = new Range(10, 19, 255);
		RangeInputStream rin = new RangeInputStream(new ByteArrayInputStream(dataSet), r);
		byte[] b = IOUtils.toByteArray(rin);
		assertEquals(10, b.length);
		assertEquals(10, b[0] & 0xff);
		assertEquals(19, b[9] & 0xff);
		
	}
	

	@Test
	public void subRangeFull() throws IOException {
			
		// start
		Range r = new Range(0, 255, 255);
		RangeInputStream rin = new RangeInputStream(new ByteArrayInputStream(dataSet), r);
		byte[] b = IOUtils.toByteArray(rin);
		assertEquals(255, b.length);
		for (int i = 0; i < b.length; i++) {
			assertEquals(b[i], dataSet[i]);
		}
		
	}


}
