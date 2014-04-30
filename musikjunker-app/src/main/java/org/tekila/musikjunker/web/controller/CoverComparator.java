package org.tekila.musikjunker.web.controller;

import java.util.Comparator;

import org.tekila.musikjunker.domain.Resource;

class CoverComparator implements Comparator<Resource>  {
	@Override
	public int compare(Resource o1, Resource o2) {
		String f1 = o1.getFileName().toLowerCase();
		String f2 = o2.getFileName().toLowerCase();
		
		if (f1.contains("cover") || f1.contains("front")) {
			return -1;
		}
		if (f2.contains("cover") || f2.contains("front")) {
			return 1;
		}
		return f1.compareTo(f2);
	}
}