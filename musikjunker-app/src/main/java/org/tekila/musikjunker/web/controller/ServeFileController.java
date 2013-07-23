package org.tekila.musikjunker.web.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.tekila.musikjunker.exception.ResourceNotFoundException;
import org.tekila.musikjunker.utils.Range;
import org.tekila.musikjunker.utils.RangeInputStream;

/**
 * TODO: Return buffered response (with range http header) TODO: apply file
 * filtering (security)
 * 
 * @author lc
 * 
 */
@Slf4j
@Controller
public class ServeFileController {

	private static final MediaType MEDIA_TYPE_OGG = new MediaType("application", "ogg");
	private static final MediaType MEDIA_TYPE_MP3 = new MediaType("audio", "mpeg");

	@Autowired
	private Environment environment;

	@RequestMapping(method = RequestMethod.GET, value = "/file")
	public ResponseEntity<InputStream> getFile(@RequestParam(value = "f") String file, HttpServletRequest request)
			throws IOException {

		File f = new File(environment.getRequiredProperty("musikjunker.basedir"), file);

		log.info("File request for {}", f.getAbsolutePath());

		if (!f.exists()) {
			throw new ResourceNotFoundException();
		}
		long length = f.length();

		Range full = new Range(0, length - 1, length);
		List<Range> ranges = new ArrayList<Range>();

		String range = request.getHeader("Range");
		if (range != null && !range.matches("^bytes=\\d*-\\d*(,\\d*-\\d*)*$")) {
			log.warn("Invalid range header {}", range);
			return rangeError(length);
		}

		// If any valid If-Range header, then process each part of byte range.
		if (range != null) {
			if (ranges.isEmpty()) {
				for (String part : range.substring(6).split(",")) {
					// Assuming a file with length of 100, the following examples
					// returns bytes at:
					// 50-80 (50 to 80), 40- (40 to length=100), -20 (length-20=80
					// to length=100).
					long start = sublong(part, 0, part.indexOf("-"));
					long end = sublong(part, part.indexOf("-") + 1, part.length());

					if (start == -1) {
						start = length - end;
						end = length - 1;
					} else if (end == -1 || end > length - 1) {
						end = length - 1;
					}

					// Check if Range is syntactically valid. If not, then return
					// 416.
					if (start > end) {
						log.warn("Invalide range header {}", range);
						return rangeError(length);
					}

					// Add range.
					ranges.add(new Range(start, end, length));
				}
			}
		}
		
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(findContentType(f));
		HttpStatus status = HttpStatus.OK;
		headers.add("Accept-Ranges", "bytes");
		
		InputStream in = new FileInputStream(f);
		log.debug("Serving ranges {}", ranges.toString());
		
		if (ranges.isEmpty() || ranges.get(0) == full) {

			// Return full file.
			Range r = full;
			headers.add("Content-Range", "bytes " + r.getStart() + "-" + r.getEnd() + "/" + r.getTotal());
			headers.setContentLength(r.getLength());

		} else if (ranges.size() == 1) {

			// Return single part of file.
			Range r = ranges.get(0);
			headers.add("Content-Range", "bytes " + r.getStart() + "-" + r.getEnd() + "/" + r.getTotal());
			headers.setContentLength(r.getLength());
			status = HttpStatus.PARTIAL_CONTENT;
			
			in = new RangeInputStream(in, r);

		}  else {
			
			// not supported
			log.warn("Multiple range request not supported");
			headers.setContentLength(f.length());
		}

		return new ResponseEntity<InputStream>(in, headers, status);
	}


	private ResponseEntity<InputStream> rangeError(long length) {
		HttpHeaders headers = new HttpHeaders();
		headers.add("Content-Range", "bytes */" + length); // Required in
		// 416.
		return new ResponseEntity<InputStream>(headers, HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE);
	}


	private static long sublong(String value, int beginIndex, int endIndex) {
		String substring = value.substring(beginIndex, endIndex);
		return (substring.length() > 0) ? Long.parseLong(substring) : -1;
	}

	
	private MediaType findContentType(File f) {
		String fileExt = StringUtils.substringAfterLast(f.getName(), ".").toLowerCase();
		if ("ogg".equals(fileExt)) {
			return MEDIA_TYPE_OGG;
		} else if ("mp3".equals(fileExt)) {
			return MEDIA_TYPE_MP3;
		} else if ("jpg".equals(fileExt) || "jpeg".equals(fileExt)) {
			return MediaType.IMAGE_JPEG;
		} else if ("png".equals(fileExt)) {
			return MediaType.IMAGE_PNG;
		} else if ("gif".equals(fileExt)) {
			return MediaType.IMAGE_GIF;
		} else {
			return MediaType.APPLICATION_OCTET_STREAM;
		}
	}

}
