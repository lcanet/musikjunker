package org.tekila.musikjunker.web.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.tekila.musikjunker.domain.Playlist;
import org.tekila.musikjunker.domain.Resource;
import org.tekila.musikjunker.exception.ResourceNotFoundException;
import org.tekila.musikjunker.repository.HibernateRepository;
import org.tekila.musikjunker.vo.PlaylistCommand;
import org.tekila.musikjunker.vo.PlaylistHeader;

@Controller
public class PlaylistController {

	@Autowired
	private HibernateRepository hibernateRepository;

	@ResponseBody
	@RequestMapping(value="/playlist/{id}", method=RequestMethod.GET)
	public Playlist getContent(@PathVariable(value="id") Long id) {
		
		Playlist p = hibernateRepository.get(Playlist.class, id);
		if (p == null) {
			throw new ResourceNotFoundException();
		}
		return p;
	}

    @ResponseBody
	@RequestMapping(value="/playlists", method=RequestMethod.GET)
	public List<PlaylistHeader> listPlayList() {
		List<Playlist> playlists = hibernateRepository.loadAll(Playlist.class);
		List<PlaylistHeader> headers = new ArrayList<>();
		for (Playlist pl : playlists) {
			headers.add(new PlaylistHeader(pl));
		}
		return headers;
	}

	@Transactional
    @ResponseBody
	@RequestMapping(value="/playlist", method=RequestMethod.POST)
	public Playlist createPlayList(@RequestBody Playlist p) {
		hibernateRepository.persist(p);
		return p;
    }


	@Transactional
    @ResponseBody
	@RequestMapping(value="/playlist/{id}", method=RequestMethod.PUT)
	public Playlist updatePlayList(@PathVariable("id") Long id, @RequestBody PlaylistCommand p) {
    	Playlist dbPlaylist = hibernateRepository.get(Playlist.class, id);
    	if (p.getHeader() != null) {
        	dbPlaylist.setName(p.getHeader().getName());
    	}
    	if (p.getAddResources() != null) {
    		for (Long idResource : p.getAddResources()) {
    			Resource r = hibernateRepository.get(Resource.class, idResource);
    			if (r != null) {
    				dbPlaylist.getSongs().add(r);
    			}
    		}
    	}
    	if (p.getDeleteResources() != null) {
    		for (Long idResource : p.getDeleteResources()) {
    			Resource r = hibernateRepository.get(Resource.class, idResource);
    			if (r != null) {
    				dbPlaylist.getSongs().remove(r);
    			}
    		}
    	}
    	
		return dbPlaylist;
	}
    
    @Transactional
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
	@RequestMapping(value="/playlist/{id}", method=RequestMethod.DELETE)
	public void deletePlayList(@PathVariable("id") Long id) {
		hibernateRepository.delete(Playlist.class, id);
	}

}
