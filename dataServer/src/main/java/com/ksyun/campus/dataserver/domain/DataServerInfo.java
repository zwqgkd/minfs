package com.ksyun.campus.dataserver.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;


@AllArgsConstructor
@Data
@Builder
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

    public int getFreeSpace() {
        return capacity-useCapacity;
    }

    public String getId(){
        return zone+"-"+rack;
    }

    public String getDsNode(){
        return ip+":"+port;
    }
}

