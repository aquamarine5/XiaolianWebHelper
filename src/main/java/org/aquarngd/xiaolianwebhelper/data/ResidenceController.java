package org.aquarngd.xiaolianwebhelper.data;

import com.alibaba.fastjson2.JSONObject;
import org.aquarngd.xiaolianwebhelper.XiaolianWebPortal;
import org.aquarngd.xiaolianwebhelper.XiaolianwebhelperApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;

import java.sql.*;
import java.util.Map;
import java.util.Objects;

public class ResidenceController {
    @Autowired
    JdbcTemplate jdbcTemplate;

    Logger logger;

    XiaolianwebhelperApplication application;
    XiaolianWebPortal webPortal;
    private final Integer[] supportedResidenceBuildingsId = new Integer[]{759014, 759935};

    public ResidenceController(XiaolianwebhelperApplication application) {
        if (!isDatabaseExisted()) createResidenceDatabase();
        logger = LoggerFactory.getLogger(ResidenceController.class);
        webPortal = application.webPortal;
        this.application = application;
    }

    private JSONObject buildIndexResidenceRequest(int buildingId) {
        return new JSONObject(Map.ofEntries(
                Map.entry("_mp", "1_1_2"),
                Map.entry("buildingId", buildingId),
                Map.entry("campusId", 347),
                Map.entry("miniSource", 3),
                Map.entry("schoolId", 219),
                Map.entry("system", 2),
                Map.entry("netType", 1)
        ));
    }

    private JSONObject buildUpdateWasherRequest(int buildingId, int residenceId, int floorId) {
        return new JSONObject(Map.ofEntries(
                Map.entry("residenceId", residenceId),
                Map.entry("floorId", floorId),
                Map.entry("deviceType", 1),
                Map.entry("locationType", 2),
                Map.entry("buildingId", buildingId),
                Map.entry("page", 1),
                Map.entry("size", 1000),
                Map.entry("_mp", "1_1_2"),
                Map.entry("miniSource", 3),
                Map.entry("system", 2)
        ));
    }

    private void createResidenceDatabase() {
        jdbcTemplate.execute("""
                CREATE TABLE xiaolian.residenceIndex (
                residenceId INT PRIMARY KEY,
                floorId INT NOT NULL,
                buildingId INT NOT NULL,
                name TEXT NOT NULL
                """);
        for (int buildingId : supportedResidenceBuildingsId) {
            JSONObject residenceInfo = webPortal.sendPostRequest("https://netapi.xiaolianhb.com/m/net/stu/residence/listDevice",
                    buildIndexResidenceRequest(buildingId),
                    "https://netapi.xiaolianhb.com/2020042916153901/4.9.2.0/m/choose/stu/residence/bathroom/byBuilding")
                    .getJSONObject("data").getJSONArray("residences").getJSONObject(0);
            jdbcTemplate.execute(String.format("INSERT INTO `residenceIndex` (residenceId, floorId, buildingId, name) VALUES (%d, %d, %d, '%s')",
                    residenceInfo.getInteger("id"),
                    residenceInfo.getInteger("parentId"),
                    buildingId,
                    residenceInfo.getString("fullName")));
        }
    }

    private void checkResidenceDatabase(int residenceId){
        jdbcTemplate.execute(String.format("""
                    CREATE TABLE IF NOT EXISTS xiaolian.%d (
                    deviceId INT PRIMARY KEY,
                    location VARCHAR(50) NOT NULL,
                    displayNo INT NOT NULL,
                    status INT NOT NULL,
                    lastUsedTime TIMESTAMP NOT NULL,
                    lastWashTime TIMESTAMP NOT NULL
                    ) CHARACTER SET utf8mb4""",residenceId));
        logger.warn("Created {} database",residenceId);
    }

    private void updateResidence(JSONObject postBody,int residenceId) {
        JSONObject deviceObject = webPortal.sendPostRequest("https://netapi.xiaolianhb.com/m/net/stu/residence/listDevice", postBody, "https://netapi.xiaolianhb.com/2020042916153901/4.9.2.0/m/net/stu/residence/listDevice");
        WasherStatus deviceStatus=WasherStatus.valueOf(deviceObject.getInteger("deviceStatus"));
        int deviceId=deviceObject.getInteger("deviceId");
        SqlRowSet rs = jdbcTemplate.queryForRowSet("SELECT * FROM `?` WHERE deviceId = ?",
                new Object[]{residenceId,deviceId}, new int[]{Types.INTEGER,Types.INTEGER});
        if (rs.next()) {
            if (WasherStatus.valueOf(rs.getInt("status")) == WasherStatus.NOT_USING &&
                    deviceStatus == WasherStatus.USING) {
                jdbcTemplate.execute(String.format("UPDATE `%d` SET lastUsedTime = NOW() WHERE deviceId = %d",residenceId,deviceId));
                logger.info("sql: run sql update lastUsedTime at residenceId:{}, deviceId:{}",residenceId,deviceId);
            }
            if (WasherStatus.valueOf(rs.getInt("status")) == WasherStatus.USING &&
                    deviceStatus == WasherStatus.NOT_USING) {
                long time = System.currentTimeMillis() - rs.getTimestamp("lastUsedTime").getTime();
                if (time <= 3600000L) {
                    SqlRowSet rrs = jdbcTemplate.queryForRowSet("SELECT * FROM `data`");
                    rrs.next();
                    int count = rrs.getInt("avgWashCount");
                    long newAvgTime = (rrs.getLong("avgWashTime") * count + time) / (count + 1);
                    jdbcTemplate.execute("UPDATE `data` SET avgWashTime = " + newAvgTime);
                    jdbcTemplate.execute("UPDATE `data` SET avgWashCount = avgWashCount + 1");
                    jdbcTemplate.execute(String.format("UPDATE `%d` SET lastWashTime = NOW() WHERE deviceId = %d",residenceId,deviceId));
                    logger.info("sql: update avgWashTime");
                } else {
                    logger.warn("sql: pass time too large: {}", time);
                }
            }
            jdbcTemplate.execute(String.format("UPDATE `%d` SET status = %d WHERE deviceId = %d",residenceId,deviceStatus.value(),deviceId));
        } else {
            jdbcTemplate.execute(String.format("INSERT INTO `%d` (deviceId, location, status, lastUsedTime, displayNo) VALUES (%d, '%s', %d, NOW(), %d)",
                    residenceId,
                    deviceId,
                    deviceObject.getString("location"),
                    deviceStatus.value(),
                    deviceObject.getInteger("dispNo")));
            logger.info("sql: run sql insert into.");
        }
    }

    public void updateAllResidences() {
        SqlRowSet sqlRowSet= jdbcTemplate.queryForRowSet("SELECT * FROM `residenceIndex`");
        while(sqlRowSet.next()){
            updateResidence(buildUpdateWasherRequest(
                    sqlRowSet.getInt("buildingId"), sqlRowSet.getInt("residenceId"),sqlRowSet.getInt("floorId") ), sqlRowSet.getInt("residenceId"));
        }
    }

    private boolean isDatabaseExisted() {
        Connection connection = null;
        ResultSet rs = null;
        try {
            connection = Objects.requireNonNull(jdbcTemplate.getDataSource()).getConnection();
            DatabaseMetaData data = connection.getMetaData();
            String[] types = {"TABLE"};
            rs = data.getTables(null, null, "residenceIndex", types);
            if (rs.next()) return true;
        } catch (SQLException e) {
            logger.error("error sql: {}",e.getMessage());
        } finally {
            try {
                if (rs != null) rs.close();
                if (connection != null) connection.close();
            } catch (SQLException e) {
                logger.error("error when closed sql: {}",e.getMessage());
            }
        }
        return false;
    }
}
