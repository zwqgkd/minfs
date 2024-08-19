package com.ksyun.campus.metaserver.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DataServerInfo {

    @JsonProperty("ip")
    private String ip;

    @JsonProperty("port")
    private int port;

    @JsonProperty("rack")
    private String rack;

    @JsonProperty("zone")
    private String zone;

    @JsonProperty("capacity")
    private int capacity;

    @JsonProperty("fileTotal")
    private int fileTotal;

    @JsonProperty("useCapacity")
    private int useCapacity;

    @JsonIgnore
    public int getFreeSpace() {
        return capacity-useCapacity;
    }

    @JsonIgnore
    public String getId(){
        return rack+"-"+zone;
    }

    @JsonIgnore
    public String getDsNode(){
        return ip+":"+port;
    }
}
