package org.aquarngd.xiaolianwebhelper.data;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.io.Serializable;
import java.sql.Timestamp;

@Entity
@Table(name="1215856")
public class WasherDevice implements Serializable {
    @Id
    public int deviceId;
    @Column(name="location")
    public String locationName;
    @Column(name="status")
    public WasherStatus status;
    @Column(name="lastusedtime")
    public Timestamp lastUsedTime;

    public WasherDevice(int deviceId,String locationName,WasherStatus status,Timestamp lastUsedTime){
        this.deviceId=deviceId;
        this.lastUsedTime=lastUsedTime;
        this.locationName=locationName;
        this.status=status;
    }
}
