package org.tekila.musikjunker.web.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.hibernate.Criteria;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.tekila.musikjunker.domain.Resource;
import org.tekila.musikjunker.domain.TypeResource;
import org.tekila.musikjunker.repository.HibernateRepository;
import org.tekila.musikjunker.vo.SearchResults;

@Controller
public class BrowseController {

	private static final Pattern NUMBERED_DIR_PATTERN = Pattern.compile("CD\\d+", Pattern.CASE_INSENSITIVE);
	
	@Autowired
	private HibernateRepository hibernateRepository;
	

	@ResponseBody
	@RequestMapping(value="/dirs", method=RequestMethod.GET)
	public List<String> getDirs(@RequestParam(value="q", required=false) String q) {
		DetachedCriteria crit = DetachedCriteria.forClass(Resource.class);
		crit.add(Restrictions.eq("type", TypeResource.FOLDER));
		if (StringUtils.isEmpty(q)) {
			crit.add(Restrictions.isNull("path"));
		} else {
			crit.add(Restrictions.eq("path", q));
		}
		crit.setProjection(Projections.property("fileName"));
		crit.addOrder(Order.asc("fileName"));
		
		return hibernateRepository.findByCriteria(crit);
	}

	@ResponseBody
	@RequestMapping("/audios")
	public List<Resource> getAudios(@RequestParam(value="dir", required=true) String q) {
		DetachedCriteria crit = DetachedCriteria.forClass(Resource.class);
		crit.add(Restrictions.eq("type", TypeResource.AUDIO));
		crit.add(Restrictions.eq("path", q));
		crit.addOrder(Order.asc("fileName"));
		
		return hibernateRepository.findByCriteria(crit);
	}

	@ResponseBody
	@RequestMapping("/covers")
	public List<Resource> getCovers(@RequestParam(value="dir", required=true) String q) {
		
		List<Resource> lr = new ArrayList<>();
		lr.addAll(findCovers(q));
		
		String lastDir = StringUtils.substringAfterLast(q, "/");
		if (NUMBERED_DIR_PATTERN.matcher(lastDir).matches()) {
			// try with parent
			lr.addAll(findCovers(StringUtils.substringBeforeLast(q, "/")));
		}
		
		Collections.sort(lr, new CoverComparator());
		return  lr;
	}
	
	private List<Resource> findCovers(String dir) {
		DetachedCriteria crit = DetachedCriteria.forClass(Resource.class);
		crit.add(Restrictions.eq("type", TypeResource.COVER));
		crit.add(Restrictions.eq("path", dir));
		return hibernateRepository.findByCriteria(crit); 
	}
	
	
	@ResponseBody
	@RequestMapping("/search")
	public SearchResults search(@RequestParam(value="q", required=true) String q,
			@RequestParam(value="size", required=false, defaultValue="20") int pageSize, 
			@RequestParam(value="start", required=false, defaultValue="0") int start, 
			@RequestParam(value="o", required=false, defaultValue="") String order 
			) {
		SearchResults res = new SearchResults();

		DetachedCriteria crit = DetachedCriteria.forClass(Resource.class);
		q = q.replace(' ', '%');
		
		crit.add(Restrictions.eq("type", TypeResource.AUDIO));
		crit.add(Restrictions.disjunction()
				.add(Restrictions.ilike("fileName", q, MatchMode.ANYWHERE))
				.add(Restrictions.ilike("metadata.artist", q, MatchMode.ANYWHERE))
				.add(Restrictions.ilike("metadata.album", q, MatchMode.ANYWHERE))
				.add(Restrictions.ilike("metadata.title", q, MatchMode.ANYWHERE))
				);
		
		crit.setProjection(Projections.rowCount());
		res.setSize((int) hibernateRepository.findCount(crit));
		

		crit.setProjection(null);
		crit.setResultTransformer(Criteria.ROOT_ENTITY);

		
		if (StringUtils.isBlank(order)) {
			crit.addOrder(Order.asc("fileName"));
		} else if ("artist".equals(order)) {
			crit.addOrder(Order.asc("metadata.artist"));
		} else if ("album".equals(order)) {
			crit.addOrder(Order.asc("metadata.album"));
		} else if ("title".equals(order)) {
			crit.addOrder(Order.asc("metadata.title"));
		} else if ("genre".equals(order)) {
			crit.addOrder(Order.asc("metadata.genre"));
			
		}
		// keep order
		crit.addOrder(Order.asc("id"));
		
		
		List<Resource> lr = hibernateRepository.findByCriteria(crit, start, pageSize);
		res.setResults(lr);
		res.setIndex(start);
		return res;
	}



	@ResponseBody
	@RequestMapping("/album")
	public List<Resource> getSongsOfAlbum(@RequestParam(value="q", required=true) String q) {
		DetachedCriteria crit = DetachedCriteria.forClass(Resource.class);
		crit.add(Restrictions.eq("type", TypeResource.AUDIO));
		crit.add(Restrictions.eq("metadata.album", q));
		crit.addOrder(Order.asc("fileName"));
		crit.add(Restrictions.eq("type", TypeResource.AUDIO));
		
		return hibernateRepository.findByCriteria(crit);
	}
}
