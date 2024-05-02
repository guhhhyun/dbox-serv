package com.dongkuksystems.dbox.config.support;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.apache.commons.io.IOUtils;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMethod;

public class InputStreamHttpServletRequestWrapper extends HttpServletRequestWrapper {
	private final byte[] streamBody;

	public InputStreamHttpServletRequestWrapper(HttpServletRequest request) throws IOException {
		super(request);
		
    if (Objects.isNull(request.getContentType()) || request.getContentType().contains(MediaType.MULTIPART_FORM_DATA_VALUE)) {
    	streamBody = inputStream2Byte(request.getInputStream());
    } else if (isFormPost(request)) {
    	streamBody = params2Byte(request);
    } else {
    	streamBody = inputStream2Byte(request.getInputStream());
    }
	}

	@Override
	public ServletInputStream getInputStream() throws IOException {
		ByteArrayInputStream inputStream = new ByteArrayInputStream(streamBody);

		return new ServletInputStream() {
			@Override
			public boolean isFinished() {
				return false;
			}

			@Override
			public boolean isReady() {
				return false;
			}

			@Override
			public void setReadListener(ReadListener listener) {

			}

			@Override
			public int read() throws IOException {
				return inputStream.read();
			}
		};
	}

	@Override
	public BufferedReader getReader() throws IOException {
		return new BufferedReader(new InputStreamReader(getInputStream()));
	}

  private boolean isFormPost(HttpServletRequest request) {
      return RequestMethod.POST.name().equals(request.getMethod());
  }

	private byte[] params2Byte(HttpServletRequest request) {
		byte[] bytes = request.getParameterMap().entrySet().stream().map(entry -> {
			String result;
			String[] value = entry.getValue();
			if (value != null && value.length > 1) {
				result = Arrays.stream(value).map(s -> entry.getKey() + "=" + s).collect(Collectors.joining("&"));
			} else {
				result = entry.getKey() + "=" + value[0];
			}

			return result;
		}).collect(Collectors.joining("&")).getBytes();
		return bytes;
	}

	private byte[] inputStream2Byte(InputStream inputStream) throws IOException {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		IOUtils.copy(inputStream, outputStream);

		return outputStream.toByteArray();
	}
}