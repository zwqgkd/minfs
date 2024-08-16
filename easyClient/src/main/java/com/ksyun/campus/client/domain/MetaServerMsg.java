package com.ksyun.campus.client.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MetaServerMsg{
    private String host;
    private int port;

    @Override
    public String toString() {
        return "MetaServerMsg{" +
                "host='" + host + '\'' +
                ", port=" + port +
                '}';
    }
}
