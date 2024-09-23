package org.aquarngd.xiaolianwebhelper;
//Mybatis

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import org.aquarngd.xiaolianwebhelper.data.WasherDeviceRepository;
import org.aquarngd.xiaolianwebhelper.data.WasherStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.sql.*;
import java.util.Objects;

@EnableAsync
@EnableJpaRepositories(basePackages = "org.aquarngd.xiaolianwebhelper.data")
@EntityScan(basePackages = "org.aquarngd.xiaolianwebhelper.data")
@EnableScheduling
@SpringBootApplication
public class XiaolianwebhelperApplication {
    @Autowired
    WasherDeviceRepository repository;

    @Autowired
    JdbcTemplate jdbcTemplate;

    JSONObject postData;
    RestTemplate restTemplate;
    Logger logger;

    HttpHeaders httpHeaders = null;

    public XiaolianwebhelperApplication() {
        logger = LoggerFactory.getLogger(XiaolianwebhelperApplication.class);
        logger.info("XiaolianWebHelper launched.");

        restTemplate = new RestTemplate();
        //restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory());
        postData = new JSONObject();
        postData.put("residenceId", 1215856);
        postData.put("floorId", 759936);
        postData.put("deviceType", 1);
        postData.put("locationType", 2);
        postData.put("buildingId", 759935);
        postData.put("page", 1);
        postData.put("size", 1000);
        postData.put("_mp", "1_1_2");
        postData.put("miniSource", 3);
        postData.put("system", 2);


    }

    public static void main(String[] args) {
        SpringApplication.run(XiaolianwebhelperApplication.class, args);
    }

    @Async
    @Scheduled(cron = "0/10 * 13-23 * * ? ")
    public void refreshWasherDevicesData() {
        if (httpHeaders == null) httpHeaders = getHttpHeaders();
        HttpEntity<JSONObject> httpEntity = new HttpEntity<>(postData, httpHeaders);
        JSONObject body = null;
        try {
            body = restTemplate.postForEntity("https://netapi.xiaolianhb.com/m/net/stu/residence/listDevice", httpEntity, JSONObject.class).getBody();
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatusCode.valueOf(401)) {
                body = loginAndResend();
            }
            logger.info("error: {}", e.getResponseBodyAsString());
        }
        logger.info("post http.");

        if (body != null) {
            //logger.info("data: {}", body.toJSONString());
            JSONArray deviceList = body.getJSONObject("data").getJSONArray("deviceInListInfo");
            for (int i = 0; i < deviceList.size(); i++) {
                JSONObject deviceObject = deviceList.getJSONObject(i);
                updateDeviceData(deviceObject);
            }
        }
    }

    private void createDataDatabase() {

        jdbcTemplate.execute("""
                CREATE TABLE xiaolian.data (
                accessToken TEXT NOT NULL,
                refreshToken TEXT NOT NULL,
                avgWashCount INT DEFAULT 0,
                avgWashTime BIGINT DEFAULT 0,
                requestTimes INT DEFAULT 0
                ) CHARACTER SET utf8mb4""");
    }

    private JSONObject loginAndResend() {
        if (!isDatabaseExisted("data")) createDataDatabase();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        //logger.info("post data: {}",getLoginJsonObject().toJSONString());
        HttpEntity<JSONObject> httpEntity = new HttpEntity<>(getLoginJsonObject(), headers);
        ResponseEntity<JSONObject> responseEntity = restTemplate.postForEntity("https://mapi.xiaolianhb.com/mp/login", httpEntity, JSONObject.class);
        logger.info("try re-login.");
        JSONObject response = responseEntity.getBody();
        if (response != null) {
            logger.info("sql: login response: {}", response.toJSONString());
            String accessToken = response.getJSONObject("data").getString("accessToken");
            String refreshToken = response.getJSONObject("data").getString("refreshToken");
            jdbcTemplate.execute("UPDATE `data` SET accessToken = '" + accessToken + "'");
            jdbcTemplate.execute("UPDATE `data` SET refreshToken = '" + refreshToken + "'");
            httpHeaders.set("accessToken", accessToken);
            httpHeaders.set("refreshToken", refreshToken);
        }
        HttpEntity<JSONObject> httpPostEntity = new HttpEntity<>(postData, httpHeaders);
        ResponseEntity<JSONObject> postResponse = restTemplate.postForEntity("https://netapi.xiaolianhb.com/m/net/stu/residence/listDevice", httpPostEntity, JSONObject.class);
        return postResponse.getBody();
    }

    private static JSONObject getLoginJsonObject() {
        JSONObject loginData = new JSONObject();
        loginData.put("appList", new JSONArray());
        loginData.put("mobile", "19030827318");
        loginData.put("password", "070304syz");
        loginData.put("appVersion", "1.4.8");
        loginData.put("system", "2");
        loginData.put("model", "DCO-AL00");
        loginData.put("lon", "");
        loginData.put("appSource", "1");
        loginData.put("systemVersion", "12");
        loginData.put("brand", "HUAWEI");
        loginData.put("uniqueId", "8c185942d3b9cf82");
        loginData.put("lat", "");
        return loginData;
    }

    private HttpHeaders getHttpHeaders() {
        if (!isDatabaseExisted("data")) {
            createDataDatabase();
            logger.debug("sql: create data table");
            jdbcTemplate.execute(String.format("INSERT INTO `data` (accessToken,refreshToken) VALUES ('%s', '%s')",
                    "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiI1NzQ1ODcwIiwib3MiOiIwIiwiaXNzIjoiaHR0cHM6Ly94aWFvbGlhbi5pbyIsImlhdCI6MTcyNjkwODcyOSwiZXhwIjoxNzI2OTI2NzI5fQ.WK85D2fwxnd6SsWs8KKBKhm75nBki8C7q_Cs331iazIFo6Ji5JxW7lC2wVkBHcd4XoAnFMI9tUWpOI5rXBMVpg",
                    "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiI1NzQ1ODcwIiwib3MiOiIwIiwiaXNzIjoiaHR0cHM6Ly94aWFvbGlhbi5pbyIsImlhdCI6MTcyNjkwODcyOSwiZXhwIjoxNzI4MjA0NzI5fQ.Bv2wkS8m7O2gHOfDjxFPRHpBgis4KbXO1R-_Kp0ly3ohPjL9hDQWev66_XjGU0DrnS59B5ZWG0MSh7aPi86SBg"));

        }
        SqlRowSet rs = jdbcTemplate.queryForRowSet("SELECT * FROM `data`");
        HttpHeaders httpHeaders = new HttpHeaders();
        if (rs.next()) {
            httpHeaders.set("accessToken", rs.getString("accessToken"));
            httpHeaders.set("refreshToken", rs.getString("refreshToken"));
        }
        httpHeaders.set("referer", "https://netapi.xiaolianhb.com/2020042916153901/4.9.2.0/m/net/stu/residence/listDevice");
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        return httpHeaders;
    }

    private void updateDeviceData(JSONObject deviceObject) {
        if (!isDatabaseExisted("1215856")) {
            jdbcTemplate.execute("""
                    CREATE TABLE xiaolian.1215856 (
                    deviceId INT PRIMARY KEY,
                    location VARCHAR(50) NOT NULL,
                    displayNo INT NOT NULL,
                    status INT NOT NULL,
                    lastUsedTime TIMESTAMP NOT NULL,
                    lastWashTime TIMESTAMP NOT NULL
                    ) CHARACTER SET utf8mb4""");
            logger.info("sql: create xiaolian table");
        }
        SqlRowSet rs = jdbcTemplate.queryForRowSet("SELECT * FROM `1215856` WHERE deviceId = ?", new Object[]{deviceObject.getInteger("deviceId")}, new int[]{Types.INTEGER});
        if (rs.next()) {
            if (WasherStatus.valueOf(rs.getInt("status")) == WasherStatus.NOT_USING &&
                    WasherStatus.valueOf(deviceObject.getInteger("deviceStatus")) == WasherStatus.USING) {
                jdbcTemplate.execute("UPDATE `1215856` SET lastUsedTime = NOW() WHERE deviceId = " + deviceObject.getInteger("deviceId"));
                logger.info("sql: run sql update lastUsedTime");
            }
            if (WasherStatus.valueOf(rs.getInt("status")) == WasherStatus.USING &&
                    WasherStatus.valueOf(deviceObject.getInteger("deviceStatus")) == WasherStatus.NOT_USING){
                long time = System.currentTimeMillis() - rs.getTimestamp("lastUsedTime").getTime();
                if(time<=3600000L){
                    SqlRowSet rrs = jdbcTemplate.queryForRowSet("SELECT * FROM `data`");
                    rrs.next();
                    int count=rrs.getInt("avgWashCount");
                    long newAvgTime = (rrs.getLong("avgWashTime")*count + time) / (count + 1);
                    jdbcTemplate.execute("UPDATE `data` SET avgWashTime = " + newAvgTime);
                    jdbcTemplate.execute("UPDATE `data` SET avgWashCount = avgWashCount + 1");
                    jdbcTemplate.execute("UPDATE `1215856` SET lastWashTime = NOW() WHERE deviceId = "+deviceObject.getInteger("deviceId"));
                    logger.info("sql: update avgWashTime");
                }
                else{
                    logger.warn("sql: pass time too large: {}",time);
                }
            }
            jdbcTemplate.execute("UPDATE `1215856` SET status = " + deviceObject.getInteger("deviceStatus") + " WHERE deviceId = " + deviceObject.getInteger("deviceId"));
        } else {
            jdbcTemplate.execute(String.format("INSERT INTO `1215856` (deviceId, location, status, lastUsedTime, displayNo) VALUES (%d, '%s', %d, NOW(), %d)",
                    deviceObject.getInteger("deviceId"),
                    deviceObject.getString("location"),
                    deviceObject.getInteger("deviceStatus"),
                    deviceObject.getInteger("dispNo")));
            logger.info("sql: run sql insert into.");
        }
    }

    private boolean isDatabaseExisted(String id) {
        Connection connection = null;
        ResultSet rs = null;
        try {
            connection = Objects.requireNonNull(jdbcTemplate.getDataSource()).getConnection();
            DatabaseMetaData data = connection.getMetaData();
            String[] types = {"TABLE"};
            rs = data.getTables(null, null, id, types);
            if (rs.next()) return true;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) rs.close();
                if (connection != null) connection.close();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
        return false;
    }

}
