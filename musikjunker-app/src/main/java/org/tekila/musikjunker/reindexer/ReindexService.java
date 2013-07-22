package org.tekila.musikjunker.reindexer;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang.StringUtils;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.AudioHeader;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.reference.GenreTypes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import org.tekila.musikjunker.domain.Resource;
import org.tekila.musikjunker.domain.TypeResource;
import org.tekila.musikjunker.exception.ReindexStateException;
import org.tekila.musikjunker.repository.HibernateRepository;

@Slf4j
@Service
public class ReindexService {

	private static final Pattern NUMERIC_GENRE_PATTERN = Pattern.compile("^\\((\\d+)\\)");
	private static final Pattern NUMERIC_GENRE_PATTERN2 = Pattern.compile("^(\\d+)$");

	@Autowired
	private HibernateRepository hibernateRepository;
	
	@Autowired
	private TransactionTemplate transactionTemplate;
	
	@Autowired
	private Environment environment;

	
	private ReindexState currentState;
	private boolean interrupt;
	
	
	/**
	 * Get current state of reindexation
	 * @return
	 */
	public synchronized ReindexState getCurrentState() {
		return currentState;
	}
	
	public synchronized void stopReindex() {
		if (currentState == null || !currentState.isRunning()) {
			throw new ReindexStateException("Not running");
		}
		interrupt = true;
	}
	

	public synchronized ReindexState startReindex() {
		if (currentState != null && currentState.isRunning()) {
			throw new ReindexStateException("Already running");
		}
		
		// build news state
		currentState = new ReindexState();
		currentState.setLastLaunch(new Date());
		currentState.setRunning(true);
		
		// clear interrupt lock
		interrupt = false;
		
		final File dir = new File(environment.getRequiredProperty("musikjunker.basedir"));
		log.info("Running reindex on {}", dir.getAbsolutePath());
		
		Thread thr = new Thread(new Runnable() {
			
			@Override
			public void run() {
				
				try {
					List<Resource> currentList = new ArrayList<Resource>();
					reindex(currentList, null, dir);
					flush(currentList);
					
					currentState.setOk(true);
				} catch (Exception ex) {
					log.error("Error while reindexing", ex);
					currentState.setOk(false);
					currentState.setMessage(ex.getMessage());
					
				} finally {
					// mark end
					currentState.setRunning(false);
					currentState.setEnd(new Date());
				}

			}
		});
		thr.setName("Musikjunker-reindex");
		thr.start();
		
		return currentState;
	}

	private void flush(final List<Resource> currentList) {
		transactionTemplate.execute(new TransactionCallbackWithoutResult() {
			@Override
			protected void doInTransactionWithoutResult(TransactionStatus arg0) {
				hibernateRepository.saveOrUpdateAll(currentList);
			}
		});
		currentList.clear();
	}

	private void reindex(List<Resource> currentList, String path, File dir) throws InterruptedException {
		log.info("Reindexing dir " + dir.getAbsolutePath());
		checkInterrupt();
		
		File[] contents = dir.listFiles();
		if (contents != null) {
			for (File f : contents) {
				checkInterrupt();
				// FIXME hack
				Thread.sleep(1000L);
				if (f.isDirectory()) {
					processDirectory(currentList, path, f);
					reindex(currentList, path != null ? path + "/" + f.getName() : f.getName(), f); 
				} else if (isAudioFile(f)) {
					processTitle(currentList, path, f);
				} else if (isCoverFile(f)) {
					processCover(currentList, path, f);
				}
			}
		}
		checkFlush(currentList);
	}

	private void checkInterrupt() throws InterruptedException {
		if (interrupt) {
			throw new InterruptedException("Interrupted by user");
		}
		
	}

	private void processDirectory(List<Resource> currentList, String path, File f) {
		if (!existsResource(TypeResource.FOLDER, path, f.getName())) {
			Resource r = buildResource(TypeResource.FOLDER, path, f);
			currentList.add(r);
		}
		
	}

	private Resource buildResource(TypeResource type, String path, File f) {
		Resource r = new Resource();
		r.setAddedDate(new Date());
		r.setFileDate(new Date(f.lastModified()));
		r.setType(type);
		r.setPath(path);
		r.setFileName(f.getName());
		return r;
	}

	private boolean existsResource(TypeResource type, String path, String name) {
		DetachedCriteria crit = DetachedCriteria.forClass(Resource.class);
		crit.add(Restrictions.eq("type", type));
		if (path != null) {
			crit.add(Restrictions.eq("path", path));
		}
		crit.add(Restrictions.eq("fileName", name));
		crit.setProjection(Projections.rowCount());
		return hibernateRepository.findCount(crit) > 0;
	}

	private void checkFlush(List<Resource> currentList) {
		if (currentList.size() > 100) {
			flush(currentList);
		}
		
	}

	private void processCover(List<Resource> currentList, String path, File f) {
		if (!existsResource(TypeResource.COVER, path, f.getName())) {
			Resource r = buildResource(TypeResource.COVER, path, f);
			currentList.add(r);
		}

	}


	private void processTitle(List<Resource> currentList, String path, File f) {
		if (!existsResource(TypeResource.AUDIO, path, f.getName())) {
			Resource t = buildResource(TypeResource.AUDIO, path, f);
			
			// extraction ID3

			AudioFile audio;
			try {
				audio = AudioFileIO.read(f);
				Tag tag = audio.getTag();
				AudioHeader ah = audio.getAudioHeader();
				if (ah != null) {
					t.setDuration(ah.getTrackLength());
				}
				if (tag != null) {
					t.getMetadata().setAlbum(StringUtils.left(tag.getFirst(FieldKey.ALBUM), 255));
					t.getMetadata().setArtist(StringUtils.left(tag.getFirst(FieldKey.ARTIST), 255));
					t.getMetadata().setComment(StringUtils.left(tag.getFirst(FieldKey.COMMENT), 255));
					t.getMetadata().setTitle(StringUtils.left(tag.getFirst(FieldKey.TITLE), 255));
					t.getMetadata().setYear(StringUtils.left(tag.getFirst(FieldKey.YEAR), 32));
					t.getMetadata().setGenre(extractGenre(tag.getFirst(FieldKey.GENRE)));
				}
			} catch (Exception e) {
				log.warn("Impossible de l'ID3 de " + f.getName() + " : " + e.getMessage());
			}

			currentList.add(t);
		}
	}

	private String extractGenre(String s) {
		Matcher numericMatch = NUMERIC_GENRE_PATTERN.matcher(s);
		if (numericMatch.matches()) {
			int val = Integer.valueOf(numericMatch.group(1));
			String ssg = GenreTypes.getInstanceOf().getValueForId(val);
			if (ssg != null) {
				return ssg;
			}
		}
		numericMatch = NUMERIC_GENRE_PATTERN2.matcher(s);
		if (numericMatch.matches()) {
			int val = Integer.valueOf(numericMatch.group(1));
			String ssg = GenreTypes.getInstanceOf().getValueForId(val);
			if (ssg != null) {
				return ssg;
			}
		}
		return StringUtils.left(s, 255);
	}

	private boolean isCoverFile(File f) {
		String ext = StringUtils.substringAfterLast(f.getName(), ".").toLowerCase();
		return "jpg".equals(ext) || "png".equals(ext) || "jpeg".equals(ext)
				|| "gif".equals(ext);
	}

	private boolean isAudioFile(File f) {
		String ext = StringUtils.substringAfterLast(f.getName(), ".").toLowerCase();
		return "ogg".equals(ext) || "mp3".equals(ext);
	}

}
