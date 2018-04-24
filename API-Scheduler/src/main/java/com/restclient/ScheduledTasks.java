package com.restclient;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;


@Component
public class ScheduledTasks {

	private static final Logger log = LoggerFactory.getLogger(ScheduledTasks.class);
	private static final SimpleDateFormat dateFormat = new SimpleDateFormat(
			"MM/dd/yyyy HH:mm:ss");

	@Value("${daily_upload_url}")
	String daily_upload_url;
	
	
	@Scheduled(cron = "${daily_cron_expression}")
	public void performDailyMigration() throws IOException {

		RestTemplate restTemplate = new RestTemplate();	

		File file;
		try {
			file = ResourceUtils.getFile("classpath:course_api_json.json");
			String content = new String(Files.readAllBytes(file.toPath()));
			
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType( MediaType.APPLICATION_JSON );

			HttpEntity request= new HttpEntity(content, headers);

			try {
				ResponseEntity<String> response = restTemplate.postForEntity(daily_upload_url, request, String.class );
				System.out.println(response.getStatusCode());
//				System.out.println(response);
			} catch (HttpStatusCodeException e) {
				String errorpayload = e.getResponseBodyAsString();
				log.error(errorpayload);
			}
		} catch (FileNotFoundException e1) {
			log.error("Json file not found for daily upload");
//			e1.printStackTrace();
		}

		System.out.println("Current Thread : "+ Thread.currentThread().getName());
		System.out.println("Regular task performed at "
				+ dateFormat.format(new Date()));
		System.out.println("=========================================================");

	}

	@Scheduled(cron = "${monthly_cron_expression}")
	public void performMonthlyMigration() throws IOException {
		if (ScheduledTasks.checkThirdBusinessDay()) {
			
			RestTemplate restTemplate = new RestTemplate();	

			File file;
			try {
				file = ResourceUtils.getFile("classpath:monthly_upload.json");
				String content = new String(Files.readAllBytes(file.toPath()));

				HttpHeaders headers = new HttpHeaders();
				headers.setContentType( MediaType.APPLICATION_JSON );

				HttpEntity request= new HttpEntity(content, headers);

				try {
					ResponseEntity<String> response = restTemplate.postForEntity(daily_upload_url, request, String.class );
					System.out.println(response.getStatusCode());
//					System.out.println(response);
				} catch (HttpStatusCodeException e) {
					String errorpayload = e.getResponseBodyAsString();
					log.error(errorpayload);
				}
			} catch (FileNotFoundException e1) {
				log.error("Json File not found for monthly_upload");
//				e1.printStackTrace();
			}
			
		}

	}

	private static boolean checkThirdBusinessDay() {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.DAY_OF_MONTH, 1);

		List<Date> days = new ArrayList<Date>();
		for (int i=1; i<6; i++) {
			cal.set(Calendar.DATE, i);
			int day = cal.get(Calendar.DAY_OF_WEEK);
			if (day != Calendar.SATURDAY  && day != Calendar.SUNDAY) {
				days.add(cal.getTime());
			}
		}
		
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		Date third = days.get(2);
		String thirdDay = dateFormat.format(third);
		
		Date currentDate = new Date(System.currentTimeMillis());
        String today = dateFormat.format(currentDate);
        
        
        return today.equals(thirdDay);

	}	


}
