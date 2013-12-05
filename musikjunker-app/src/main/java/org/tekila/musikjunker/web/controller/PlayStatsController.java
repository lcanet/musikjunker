package org.tekila.musikjunker.web.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.tekila.musikjunker.domain.Resource;
import org.tekila.musikjunker.exception.ResourceNotFoundException;
import org.tekila.musikjunker.repository.HibernateRepository;

@Controller
public class PlayStatsController {

	@Autowired
	private HibernateRepository hibernateRepository;

	@Transactional
	@ResponseBody
	@RequestMapping(value="/song/{id}/incrementPlayStats", method=RequestMethod.POST)
	public String starSong(@PathVariable(value="id") Long id) {
		Resource r = hibernateRepository.get(Resource.class, id);
		if (r == null) {
			throw new ResourceNotFoundException();
		}
		r.incrementPlayStats();
		hibernateRepository.saveOrUpdate(r);
		return "ok";
	}
	
	

}
