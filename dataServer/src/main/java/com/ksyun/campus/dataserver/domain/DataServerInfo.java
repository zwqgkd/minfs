package com.ksyun.campus.dataserver.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;


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

    public DataServerInfo(String ip, int port, String rack, String zone, int capacity, int fileTotal, int useCapacity) {
        this.ip = ip;
        this.port = port;
        this.rack = rack;
        this.zone = zone;
        this.capacity = capacity;
        this.fileTotal = fileTotal;
        this.useCapacity = useCapacity;
    }

    public String getId(){
        return zone + '-' + rack;
    }
}

