package com.ksyun.campus.client.domain;

import lombok.*;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class DataServerMsg{
    private String host;
    private int port;
    private int fileTotal;
    private int capacity;
    private int useCapacity;

    @Override
    public String toString() {
        return "DataServerMsg{" +
                "host='" + host + '\'' +
                ", port=" + port +
                ", fileTotal=" + fileTotal +
                ", capacity=" + capacity +
                ", useCapacity=" + useCapacity +
                '}';
    }
}
