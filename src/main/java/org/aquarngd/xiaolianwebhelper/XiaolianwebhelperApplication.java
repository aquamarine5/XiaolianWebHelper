package org.aquarngd.xiaolianwebhelper;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import org.aquarngd.xiaolianwebhelper.data.WasherDevice;
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
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.Type;
import java.sql.*;
import java.util.ArrayList;
import java.util.Objects;

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

    public XiaolianwebhelperApplication() {
        logger = LoggerFactory.getLogger(XiaolianwebhelperApplication.class);
        logger.info("XiaolianWebHelper launched.");

        restTemplate = new RestTemplate();
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

    @Scheduled(cron = "0/10 * 10-23 * * ? ")
    public void refreshWasherDevicesData() {
        HttpHeaders httpHeaders = getHttpHeaders();
        HttpEntity<JSONObject> httpEntity = new HttpEntity<>(postData, httpHeaders);
        ResponseEntity<JSONObject> response = restTemplate.postForEntity("https://netapi.xiaolianhb.com/m/net/stu/residence/listDevice", httpEntity, JSONObject.class);
        JSONObject body=null;
        if(response.getStatusCode()== HttpStatusCode.valueOf(401)){
            body=loginAndResend();
        }
        else{
            body=response.getBody();
        }
        logger.info("post http.");

        if (body != null) {
            logger.info("data: {}", body.toJSONString());
            JSONArray deviceList = body.getJSONObject("data").getJSONArray("deviceInListInfo");
            for (int i = 0; i < deviceList.size(); i++) {
                JSONObject deviceObject = deviceList.getJSONObject(i);
                updateDeviceData(deviceObject);
            }
        }
    }
    private JSONObject loginAndResend(){
        return null;
    }
    private static HttpHeaders getHttpHeaders() {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set("accessToken", "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiI1NzQ1ODcwIiwib3MiOiIwIiwiaXNzIjoiaHR0cHM6Ly94aWFvbGlhbi5pbyIsImlhdCI6MTcyNjg4NjMzOSwiZXhwIjoxNzI2OTA0MzM5fQ.PT6gZzsPOOxP5Sxz_Jezty1WDJKIL15oivvUCeigT7gwGM28Ubwqso3Eb05PDE1fOIysSTmythpHC5JcWdbPeQ");
        httpHeaders.set("refreshToken", "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiI1NzQ1ODcwIiwib3MiOiIwIiwiaXNzIjoiaHR0cHM6Ly94aWFvbGlhbi5pbyIsImlhdCI6MTcyNjg4NjMzOSwiZXhwIjoxNzI4MTgyMzM5fQ.rMl9qGaoexv3Njg0y59O9nJVlGMJ64IPtvaNAIGLA6EH795ajpajq2JCLKy66aBJ4jxPHAqaOYvdIKZ6r2HJeg");
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
                    lastUsedTime TIMESTAMP NOT NULL
                    ) CHARACTER SET utf8mb4""");
        }
        SqlRowSet rs = jdbcTemplate.queryForRowSet("SELECT * FROM `1215856` WHERE deviceId = ?", new Object[]{deviceObject.getInteger("deviceId")}, new int[]{Types.INTEGER});

        logger.info("run after select deviceId");
        if (rs.next()) {
            if (WasherStatus.valueOf(rs.getInt("status")) == WasherStatus.NOT_USING &&
                    WasherStatus.valueOf(deviceObject.getInteger("deviceStatus")) == WasherStatus.USING) {
                jdbcTemplate.execute("UPDATE `1215856` SET lastUsedTime = NOW() WHERE deviceId = " + deviceObject.getInteger("deviceId"));
                logger.info("run sql update lastUsedTime");
            }
            jdbcTemplate.execute("UPDATE `1215856` SET status = " + deviceObject.getInteger("deviceStatus") + " WHERE deviceId = " + deviceObject.getInteger("deviceId"));
            logger.info("run sql update status");
        } else {
            jdbcTemplate.execute(String.format("INSERT INTO `1215856` (deviceId, location, status, lastUsedTime, displayNo) VALUES (%d, '%s', %d, NOW(), %d)",
                    deviceObject.getInteger("deviceId"),
                    deviceObject.getString("location"),
                    deviceObject.getInteger("deviceStatus"),
                    deviceObject.getInteger("dispNo")));
            logger.info("run sql insert into.");
        }


//            repository.save(new WasherDevice(
//                    deviceObject.getInteger("deviceId"),
//                    deviceObject.getString("location"),
//                    WasherStatus.valueOf(deviceObject.getInteger("deviceStatus")),
//                    new Timestamp(System.currentTimeMillis())
//            ));
//            logger.debug("create {}", deviceObject.getInteger("deviceId"));
//        } else {
//            WasherDevice washerDevice = repository.findById(deviceObject.getInteger("deviceId")).get();
//            WasherStatus nowStatus = WasherStatus.valueOf(deviceObject.getInteger("deviceStatus"));
//            if (nowStatus == WasherStatus.USING && washerDevice.status == WasherStatus.NOT_USING) {
//                washerDevice.lastUsedTime = new Timestamp(System.currentTimeMillis());
//            }
//            washerDevice.status = nowStatus;
//            repository.save(washerDevice);
//        }

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
