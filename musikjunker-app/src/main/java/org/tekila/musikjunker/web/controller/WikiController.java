package org.tekila.musikjunker.web.controller;

import java.io.IOException;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.lang.text.StrBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Slf4j
@Controller
public class WikiController {

	private static final String BASE_WIKI_API_URI = "http://en.wikipedia.org/w/api.php?format=json";

	@ResponseBody
	@RequestMapping(value="/wiki", method=RequestMethod.GET)
	public ResponseEntity<byte[]> getDirs(@RequestParam(value="q", required=false) String q) {
		HttpClient client = new HttpClient();
		
		StrBuilder sb = new StrBuilder();
		sb.append(BASE_WIKI_API_URI);
		sb.append("&action=query");
		sb.append("&prop=extracts");
		sb.append("&redirects=1");
		sb.append("&titles=");
		sb.append(q);
		
		log.debug("Requesting wiki content using uri {}", sb.toString());
		
		GetMethod gm = null;
		try {
			gm = new GetMethod(sb.toString());
			int status = client.executeMethod(gm);
			log.info("Getting wiki page for {} status code {}", q, status);
			
			byte[] body = gm.getResponseBody();
			
			if (status != 200) {
				return new ResponseEntity<byte[]>(
						body,
						HttpStatus.INTERNAL_SERVER_ERROR);
			} else {
				return new ResponseEntity<byte[]>(
						body,
						HttpStatus.OK);
			}
			
		} catch (IOException e) {
			log.warn("Cannot request wikipedia", e);
			return new ResponseEntity<byte[]>(
					"Cannot request page".getBytes(),
					HttpStatus.OK);
		} finally {
			if (gm != null) {
				gm.releaseConnection();
			}
		}
		
		
	}
}
