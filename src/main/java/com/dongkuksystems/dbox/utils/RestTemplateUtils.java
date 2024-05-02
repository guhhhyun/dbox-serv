package com.dongkuksystems.dbox.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.dongkuksystems.dbox.models.dto.type.data.DataDetailDto;

@Component
public class RestTemplateUtils {
	/**
	 * http 요청
	 */
	public <T, U> ResponseEntity<T> http(String url, String method, HttpHeaders headers, U body, Class<T> clazz) throws Exception {
		RestTemplate restTemplate = new RestTemplate(this.makeHttpRequestFactory());

  	URI uri = new URI(url);
    HttpMethod httpMethod = method != null ? HttpMethod.valueOf(method.toUpperCase()) : HttpMethod.GET;
  	HttpEntity<U> httpEntity = new HttpEntity<>(body, headers);
  	
  	return restTemplate.exchange(uri, httpMethod, httpEntity, clazz);
	}
	
  /**
	 * http 요청
   */
	public <T, U> ResponseEntity<T> http(String url, String method, HttpHeaders headers, U body, ParameterizedTypeReference<T> typeReference) throws Exception {
  	RestTemplate restTemplate = new RestTemplate(this.makeHttpRequestFactory());

  	URI uri = new URI(url);
    HttpMethod httpMethod = method != null ? HttpMethod.valueOf(method.toUpperCase()) : HttpMethod.GET;
  	HttpEntity<U> httpEntity = new HttpEntity<>(body, headers);
  	
  	return restTemplate.exchange(uri, httpMethod, httpEntity, typeReference);
	}

	/**
	 * get 요청
	 */
	public <T, U> ResponseEntity<T> get(String url, HttpHeaders headers, Class<T> clazz) throws Exception {
		return this.http(url, HttpMethod.GET.name(), headers, null, clazz);
	}
	
	/**
	 * get 요청
	 */
	public <T, U> ResponseEntity<T> get(String url, HttpHeaders headers, ParameterizedTypeReference<T> typeReference) throws Exception {
		return this.http(url, HttpMethod.GET.name(), headers, null, typeReference);
	}
	
	/**
	 * post 요청
	 */
	public <T, U> ResponseEntity<T> post(String url, HttpHeaders headers, U body, Class<T> clazz) throws Exception {
		return this.http(url, HttpMethod.POST.name(), headers, body, clazz);
	}
	
	/**
	 * post 요청
	 */
	public <T, U> ResponseEntity<T> post(String url, HttpHeaders headers, U body, ParameterizedTypeReference<T> typeReference) throws Exception {
		return this.http(url, HttpMethod.POST.name(), headers, body, typeReference);
	}
	
	/**
	 * put 요청
	 */
	public <T, U> ResponseEntity<T> put(String url, HttpHeaders headers, U body, Class<T> clazz) throws Exception {
		return this.http(url, HttpMethod.PUT.name(), headers, body, clazz);
	}
	
	/**
	 * put 요청
	 */
	public <T, U> ResponseEntity<T> put(String url, HttpHeaders headers, U body, ParameterizedTypeReference<T> typeReference) throws Exception {
		return this.http(url, HttpMethod.PUT.name(), headers, body, typeReference);
	}
	
	/**
	 * patch 요청
	 */
	public <T, U> ResponseEntity<T> patch(String url, HttpHeaders headers, U body, Class<T> clazz) throws Exception {
		return this.http(url, HttpMethod.PATCH.name(), headers, body, clazz);
	}
	
	/**
	 * patch 요청
	 */
	public <T, U> ResponseEntity<T> patch(String url, HttpHeaders headers, U body, ParameterizedTypeReference<T> typeReference) throws Exception {
		return this.http(url, HttpMethod.PATCH.name(), headers, body, typeReference);
	}
	
	/**
	 * delete 요청
	 */
	public <T, U> ResponseEntity<T> delete(String url, HttpHeaders headers, Class<T> clazz) throws Exception {
		return this.http(url, HttpMethod.DELETE.name(), headers, null, clazz);
	}
	
	/**
	 * delete 요청
	 */
	public <T, U> ResponseEntity<T> delete(String url, HttpHeaders headers, ParameterizedTypeReference<T> typeReference) throws Exception {
		return this.http(url, HttpMethod.DELETE.name(), headers, null, typeReference);
	}
	
	/**
	 * form data 생성
	 */
	public HttpEntity<Resource> makeFormDataHttpEntity(InputStream inputStream, String fileName, String contentType, long contentLength) throws IOException {
		HttpHeaders fileHeaders = new HttpHeaders();
		fileHeaders.setContentType(MediaType.valueOf(contentType));
		
		return new HttpEntity<>(new FileInputStreamResource(inputStream, fileName, contentLength), fileHeaders);
	}
	
	/**
	 * form data 생성
	 */
	public HttpEntity<Resource> makeFormDataHttpEntity(File file) throws IOException {
		return this.makeFormDataHttpEntity(
				new FileInputStream(file), 
				file.getName(), 
				Files.probeContentType(file.toPath()), 
				file.length());
	}
	
	/**
	 * form data 생성
	 */
	public HttpEntity<Resource> makeFormDataHttpEntity(MultipartFile multipartFile) throws IOException {
		return this.makeFormDataHttpEntity(
				multipartFile.getInputStream(), 
				multipartFile.getOriginalFilename(), 
				multipartFile.getContentType(), 
				multipartFile.getSize());
	}
	
	/**
	 * HttpRequestFactory 생성
	 */
	protected HttpComponentsClientHttpRequestFactory makeHttpRequestFactory() {
		HttpComponentsClientHttpRequestFactory httpRequestFactory = new HttpComponentsClientHttpRequestFactory();
    httpRequestFactory.setConnectTimeout(3600000);
    httpRequestFactory.setReadTimeout(3600000);
    return httpRequestFactory;
	}
	
	/**
	 * 파일명을 포함한 InputStreamResource
	 */
	protected static class FileInputStreamResource extends InputStreamResource {
		private final String fileName;
		private final long contentLength;
		
		public FileInputStreamResource(InputStream inputStream, String fileName, long contentLength) {
			super(inputStream);
			this.fileName = fileName;
			this.contentLength = contentLength;
		}
		
		@Override
		public String getFilename() {
			return fileName;
		}
		
		@Override
		public long contentLength() throws IOException {
			return contentLength;
		}
	}
	
}
