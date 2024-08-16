package com.ksyun.campus.client;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FSOutputStream extends OutputStream {

    private static final int BUFFER_SIZE = 1024; // 1KB buffer size

    private String path;
    private FileSystem fileSystem;
    private List<Byte> writeBuffer = new ArrayList<>();

    public FSOutputStream(String path, FileSystem fileSystem) {
        this.path = path;
        this.fileSystem = fileSystem;
    }

    public String getPath() {
        return path;
    }

    @Override
    public void write(int b) throws IOException {

    }

    @Override
    public void write(byte[] b) {
        for (byte value : b) {
            writeBuffer.add(value);
        }
        try {
            tryFlushWriteBuffer();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        // super.write(b);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        super.write(b, off, len);
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
            postData.put("data", data);

            String url = FileSystem.zkUtil.getMasterMetaAddress();
            fileSystem.sendPostRequest(url, "write", postData);

            writeBuffer.clear();
        }
    }

    private void tryFlushWriteBuffer() throws Exception {
        if (writeBuffer.size() >= BUFFER_SIZE) {
            flushWriteBuffer();
        }
    }
}
