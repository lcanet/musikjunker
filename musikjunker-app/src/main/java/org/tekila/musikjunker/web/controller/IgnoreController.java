package org.tekila.musikjunker.web.controller;

import java.util.List;

import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.tekila.musikjunker.domain.Resource;
import org.tekila.musikjunker.domain.TypeResource;
import org.tekila.musikjunker.exception.ResourceNotFoundException;
import org.tekila.musikjunker.repository.HibernateRepository;

@Controller
public class IgnoreController {

	@Autowired
	private HibernateRepository hibernateRepository;

	@Transactional
	@ResponseBody
	@RequestMapping(value="/song/{id}/ignore", method=RequestMethod.POST)
	public String ignoreSong(@PathVariable(value="id") Long id) {
		Resource r = hibernateRepository.get(Resource.class, id);
		if (r == null) {
			throw new ResourceNotFoundException();
		}
		r.setIgnoreShuffle(true);
		hibernateRepository.saveOrUpdate(r);
		return "ok";
	}
	

	@Transactional
	@ResponseBody
	@RequestMapping(value="/song/{id}/unignore", method=RequestMethod.POST)
	public String unignoreSong(@PathVariable(value="id") Long id) {
		Resource r = hibernateRepository.get(Resource.class, id);
		if (r == null) {
			throw new ResourceNotFoundException();
		}
		r.setIgnoreShuffle(false);
		hibernateRepository.saveOrUpdate(r);
		return "ok";
	}

	@Transactional
	@ResponseBody
	@RequestMapping(value="/dir/unignore", method=RequestMethod.POST)
	public String unignoreDir(@RequestParam(value="dir",required=true) String dir) {
		DetachedCriteria crit = DetachedCriteria.forClass(Resource.class);
		crit.add(Restrictions.eq("type", TypeResource.AUDIO));
		crit.add(Restrictions.like("path", dir, MatchMode.START));
		
		List<Resource> lr = hibernateRepository.findByCriteria(crit);
		for (Resource r : lr) {
			r.setIgnoreShuffle(false);
			hibernateRepository.saveOrUpdate(r);
		}
		return "ok";
	}
	@Transactional
	@ResponseBody
	@RequestMapping(value="/dir/ignore", method=RequestMethod.POST)
	public String ignoreDir(@RequestParam(value="dir",required=true) String dir) {
		DetachedCriteria crit = DetachedCriteria.forClass(Resource.class);
		crit.add(Restrictions.eq("type", TypeResource.AUDIO));
		crit.add(Restrictions.like("path", dir, MatchMode.START));
		
		List<Resource> lr = hibernateRepository.findByCriteria(crit);
		for (Resource r : lr) {
			r.setIgnoreShuffle(true);
			hibernateRepository.saveOrUpdate(r);
		}
		return "ok";
	}



}
