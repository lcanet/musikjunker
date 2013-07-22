package org.tekila.musikjunker.web.controller;

import java.io.File;
import java.io.IOException;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.io.FileUtils;
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
import org.springframework.web.bind.annotation.ResponseBody;
import org.tekila.musikjunker.exception.ResourceNotFoundException;

/**
 * TODO: Return buffered response (with range http header)
 * TODO: apply file filtering (security)
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
	@ResponseBody
	public ResponseEntity<byte[]> getFile(@RequestParam(value="f") String file) throws IOException {
		
		File f = new File(environment.getRequiredProperty("musikjunker.basedir"),
				file);
		log.info("File request for {}", f.getAbsolutePath());
		if (f.exists()) {
			byte[] fileData = FileUtils.readFileToByteArray(f);
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(findContentType(f));
			headers.setContentLength(fileData.length);
			ResponseEntity<byte[]> re = new ResponseEntity<byte[]>(fileData, headers, HttpStatus.OK);
			
			return re;
			
		} else {
			throw new ResourceNotFoundException();
		}
		
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
