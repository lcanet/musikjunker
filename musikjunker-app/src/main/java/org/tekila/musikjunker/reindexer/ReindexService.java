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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import org.tekila.musikjunker.domain.Resource;
import org.tekila.musikjunker.domain.TypeResource;
import org.tekila.musikjunker.repository.HibernateRepository;

@Slf4j
@Service
@Scope(value=  ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class ReindexService {

	private static final Pattern NUMERIC_GENRE_PATTERN = Pattern.compile("^\\((\\d+)\\)");
	private static final Pattern NUMERIC_GENRE_PATTERN2 = Pattern.compile("^(\\d+)$");

	@Autowired
	private HibernateRepository hibernateRepository;
	
	@Autowired
	private TransactionTemplate transactionTemplate;

	private List<Object> currentList = new ArrayList<Object>();
	
	public void reindex(String baseDir) {
		File dir = new File(baseDir);
		reindex(null, dir);
		flush();

	}

	private void flush() {
		transactionTemplate.execute(new TransactionCallbackWithoutResult() {
			@Override
			protected void doInTransactionWithoutResult(TransactionStatus arg0) {
				hibernateRepository.saveOrUpdateAll(currentList);
			}
		});
		currentList.clear();
	}

	private void reindex(String path, File dir) {
		log.info("Reindexing dir " + dir.getAbsolutePath());

		File[] contents = dir.listFiles();
		if (contents != null) {
			for (File f : contents) {
				if (f.isDirectory()) {
					processDirectory(path, f);
					reindex(path != null ? path + "/" + f.getName() : f.getName(), f); 
				} else if (isAudioFile(f)) {
					processTitle(path, f);
				} else if (isCoverFile(f)) {
					processCover(path, f);
				}
			}
		}
		checkFlush();
	}

	private void processDirectory(String path, File f) {
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

	private void checkFlush() {
		if (currentList.size() > 100) {
			flush();
		}
		
	}

	private void processCover(String path, File f) {
		if (!existsResource(TypeResource.COVER, path, f.getName())) {
			Resource r = buildResource(TypeResource.COVER, path, f);
			currentList.add(r);
		}

	}


	private void processTitle(String path, File f) {
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
