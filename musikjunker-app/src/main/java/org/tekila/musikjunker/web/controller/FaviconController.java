package org.tekila.musikjunker.web.controller;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;

import javax.imageio.ImageIO;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.io.IOUtils;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.tekila.musikjunker.domain.Resource;
import org.tekila.musikjunker.domain.TypeResource;
import org.tekila.musikjunker.exception.ResourceNotFoundException;
import org.tekila.musikjunker.repository.HibernateRepository;

@Controller
@Slf4j
public class FaviconController {

	@Autowired
	private HibernateRepository hibernateRepository;

	@Autowired
	private Environment environment;


	@ResponseBody
	@RequestMapping(value="/song/{id}/favicon", method=RequestMethod.GET)
	public ResponseEntity<byte[]> getFavicon(@PathVariable(value="id") Long id)  {
		Resource r = hibernateRepository.get(Resource.class, id);
		if (r == null) {
			throw new ResourceNotFoundException();
		}
		
		// find all covers
		DetachedCriteria crit = DetachedCriteria.forClass(Resource.class);
		crit.add(Restrictions.eq("type", TypeResource.COVER));
		crit.add(Restrictions.eq("path", r.getPath()));
		
		List<Resource> lr = hibernateRepository.findByCriteria(crit);
		if (lr.isEmpty()) {
			return getDefaultFavicon();
			
		} else {
			Collections.sort(lr, new CoverComparator());
			return buildFavicon(lr.get(0));			
		}
	}


	private ResponseEntity<byte[]> buildFavicon(Resource resource) {

		File rootDir = new File(environment.getRequiredProperty("musikjunker.basedir"));
		File f = new File(rootDir, resource.getPath() + File.separator + resource.getFileName());
		
		log.info("Building favicon of file {}", f.getAbsolutePath());

		try {
			BufferedImage bi = ImageIO.read(f);
			BufferedImage favicon = new BufferedImage(16, 16, BufferedImage.TYPE_INT_RGB);
			Graphics graphivs = favicon.getGraphics();
			graphivs.drawImage(bi, 0, 0, 16, 16, 0, 0, bi.getWidth(), bi.getHeight(), null);
			
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ImageIO.write(favicon, "PNG", bos);
			
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.IMAGE_PNG);
			return new ResponseEntity<byte[]>(bos.toByteArray(), headers, HttpStatus.OK);

			
		} catch (IOException e) {
			log.warn("Cannot build favicon, returning default", e);
			return getDefaultFavicon();
		}
	}


	private ResponseEntity<byte[]> getDefaultFavicon()  {
		byte[] defaultFavicon;
		InputStream in = null;
		try  {
			in = getClass().getResourceAsStream("/favicon.png");
			if (in == null) {
				throw new IOException("Cannot get default favicon!");
			}
			defaultFavicon = IOUtils.toByteArray(in);
		} catch (IOException e) {
			log.error("Cannot read default favicon", e);
			throw new ResourceNotFoundException();
		} finally {
			IOUtils.closeQuietly(in);
		}
		
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.IMAGE_PNG);
		return new ResponseEntity<byte[]>(defaultFavicon, headers, HttpStatus.OK);
	}
	

}
