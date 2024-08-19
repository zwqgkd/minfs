package com.ksyun.campus.client;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.io.OutputStream;
import java.util.*;

public class FSOutputStream extends OutputStream {

    @Getter
    private String path;
    private FileSystem fileSystem;
    private List<Byte> writeBuffer = new ArrayList<>();

    public FSOutputStream(String path, FileSystem fileSystem) {
        this.path = path;
        this.fileSystem = fileSystem;
    }

    @Override
    public void write(int b) throws IOException {
        writeBuffer.add((byte)b);
        try {
            flushWriteBuffer();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void write(byte[] b) {
        for (byte value : b) {
            System.out.println(value);
            writeBuffer.add(value);
        }
        try {
            flushWriteBuffer();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        // super.write(b);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        for (int i = off; i < off + len; i++) {
            writeBuffer.add(b[i]);
        }
        try {
            flushWriteBuffer();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        // super.write(b, off, len);
    }

    @Override
    public void close() throws IOException {
        super.close();
    }

    private void flushWriteBuffer() throws Exception {
        if (!writeBuffer.isEmpty()) {
            int size = writeBuffer.size();
            byte[] data = new byte[size];
            for (int i = 0; i < size; i++) {
                data[i] = writeBuffer.get(i);
            }

            Map<String, Object> postData = new HashMap<>();
            postData.put("path", path);
            // postData.put("data", data);
            // postData.put("offset", off);
            // postData.put("length", len);

            String url = FileSystem.zkUtil.getMasterMetaAddress();
            ResponseEntity<List> response = fileSystem.sendPostRequest(url, "write", postData, List.class);
            List<String> ipList = response.getBody();
            System.out.println(Arrays.toString(data));
            postData.put("data", Arrays.toString(data));

            boolean isSuccess = true;
            long fileSize = 0;

            // Todo: 向dataServer发送写的内容
            for (String ip : ipList) {
                ResponseEntity<Integer> responseFromDataServer = fileSystem.sendPostRequest(ip, "write", postData, Integer.class);
                if (responseFromDataServer.getStatusCode() != HttpStatus.OK) {
                    isSuccess = false;
                }
                fileSize = (long) responseFromDataServer.getBody();
            }

            postData.remove("data");
            postData.put("size", fileSize);

            if (isSuccess) {
                url = FileSystem.zkUtil.getMasterMetaAddress();
                ResponseEntity responseFromMetaServer = fileSystem.sendPostRequest(url, "commitWrite", postData, Void.class);
                System.out.println(responseFromMetaServer.getBody());
            }

            writeBuffer.clear();
        }
    }

//    private void tryFlushWriteBuffer() throws Exception {
//        if (writeBuffer.size() >= BUFFER_SIZE) {
//            flushWriteBuffer();
//        }
//    }
}
