package org.aquarngd.xiaolianwebhelper;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.HttpHandler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.client.RestTemplate;

@EnableScheduling
@SpringBootApplication
public class XiaolianwebhelperApplication {
	JSONObject postData;
	RestTemplate restTemplate;
	Logger logger;
	public XiaolianwebhelperApplication(){
		logger= LoggerFactory.getLogger(XiaolianwebhelperApplication.class);
		logger.info("XiaolianWebHelper launched.");
		restTemplate=new RestTemplate();
		postData.put("residenceId",1215856);
		postData.put("floorId",759936);
		postData.put("deviceType",1);
		postData.put("locationType",2);
		postData.put("buildingId",759935);
		postData.put("page",1);
		postData.put("size",1000);
		postData.put("_mp","1_1_2");
		postData.put("miniSource",3);
		postData.put("system",2);
	}
	public static void main(String[] args) {
		SpringApplication.run(XiaolianwebhelperApplication.class, args);
	}

	@Scheduled(cron = "0/10 * 13-23 * * ? ")
	public void refreshWashData(){
		HttpHeaders httpHeaders=new HttpHeaders();
		httpHeaders.set("accessToken","");
		httpHeaders.set("refreshToken","");
		httpHeaders.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<JSONObject> httpEntity=new HttpEntity<>(postData,httpHeaders);
		JSONObject body=restTemplate.postForEntity("https://netapi.xiaolianhb.com/m/net/stu/residence/listDevice",httpEntity,JSONObject.class).getBody();

	}

}
