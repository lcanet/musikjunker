package org.tekila.musikjunker.web.controller.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.tekila.musikjunker.exception.ReindexStateException;
import org.tekila.musikjunker.reindexer.ReindexService;
import org.tekila.musikjunker.reindexer.ReindexState;
import org.tekila.musikjunker.vo.ServiceError;

@Controller
@RequestMapping("/admin/reindex")
public class ReindexController {

	@Autowired
	private ReindexService reindexService;
	
	@ResponseBody
	@ExceptionHandler
	public ServiceError exceptionHandler(ReindexStateException exception) {
		return new ServiceError(exception.getMessage());
	}
	
	@ResponseBody
	@RequestMapping(method = RequestMethod.GET)
	public ReindexState getState() {
		return reindexService.getCurrentState();
	}

	@ResponseBody
	@RequestMapping(method = RequestMethod.POST)
	public ReindexState launch() {
		return reindexService.startReindex();
	}


	@ResponseBody
	@RequestMapping(method = RequestMethod.DELETE)
	public String stop() {
		reindexService.stopReindex();
		return "ok";
	}

}
