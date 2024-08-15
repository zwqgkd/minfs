package com.ksyun.campus.metaserver.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DataServerInfo {

    @JsonProperty("ip")
    private String ip;

    @JsonProperty("port")
    private String port;

    @JsonProperty("rack")
    private String rack;

    @JsonProperty("zone")
    private String zone;

    @JsonProperty("capacity")
    private int capacity;
}
