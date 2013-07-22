package org.tekila.musikjunker.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class PingController {

	@ResponseBody
	@RequestMapping(value="/ping", produces="text/plain", method=RequestMethod.GET)
	public String ping() {
		return "pong";
	}
}
