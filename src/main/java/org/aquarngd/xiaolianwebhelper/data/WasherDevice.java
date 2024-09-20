package org.aquarngd.xiaolianwebhelper.data;

import java.io.Serializable;
import java.sql.Timestamp;


public class WasherDevice implements Serializable {
    public int deviceId;
    public String locationName;
    public WasherStatus status;
    public Timestamp lastUsedTime;

    public WasherDevice(int deviceId,String locationName,WasherStatus status,Timestamp lastUsedTime){
        this.deviceId=deviceId;
        this.lastUsedTime=lastUsedTime;
        this.locationName=locationName;
        this.status=status;
    }
}
