package org.aquarngd.xiaolianwebhelper.controller;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import org.aquarngd.xiaolianwebhelper.data.WasherDeviceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WasherHelper {

    @Autowired
    WasherDeviceRepository repository;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @RequestMapping("/wash")
    public String GetWash(){
        JSONObject jsonObject=new JSONObject();
        SqlRowSet rs =jdbcTemplate.queryForRowSet("SELECT * FROM `1215856`");
        JSONArray devicesList=jsonObject.putArray("devices");
        while(rs.next()){
            JSONObject device=new JSONObject();
            device.put("id",rs.getInt("displayNo"));
            device.put("status",rs.getInt("status"));
            device.put("name",rs.getString("location"));
            device.put("time",rs.getTimestamp("lastUsedTime").getTime());
            devicesList.add(device);
        }
        return jsonObject.toJSONString();
    }
}
