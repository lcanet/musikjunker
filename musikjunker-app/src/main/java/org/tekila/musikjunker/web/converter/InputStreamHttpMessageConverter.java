package org.tekila.musikjunker.web.converter;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.NotImplementedException;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;

public class InputStreamHttpMessageConverter implements HttpMessageConverter<InputStream> {

	@Override
	public boolean canRead(Class<?> clazz, MediaType mediaType) {
		return false;
	}

	@Override
	public boolean canWrite(Class<?> clazz, MediaType mediaType) {
		return InputStream.class.isAssignableFrom(clazz);
	}

	@Override
	public List<MediaType> getSupportedMediaTypes() {
		return Arrays.asList(MediaType.APPLICATION_OCTET_STREAM);
	}

	@Override
	public InputStream read(Class<? extends InputStream> clazz, HttpInputMessage inputMessage) throws IOException,
			HttpMessageNotReadableException {
		throw new NotImplementedException();
	}

	@Override
	public void write(InputStream t, MediaType contentType, HttpOutputMessage outputMessage) throws IOException,
			HttpMessageNotWritableException {
		
		try {
			IOUtils.copy(t, outputMessage.getBody());
		} finally {
			IOUtils.closeQuietly(t);
		}
		
	}

}
