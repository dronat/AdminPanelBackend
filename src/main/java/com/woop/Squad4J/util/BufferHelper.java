package com.woop.Squad4J.util;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;

public class BufferHelper {

    private BufferHelper(){
        throw new IllegalStateException("You cannot instantiate a utility class.");
    }
    /**
     * <a href="https://github.com/roengle/squadQuery/blob/main/src/util/BufferHelper.java">squadQuery implementation</a>
     *
     * Method to retrieve a string from a buffer, where the buffer's <b>current position</b> is at the
     * beginning of the string. The string MUST be null-terminated.
     *
     * @param buffer the buffer to get the string from
     * @return the string from the buffer
     */
    public static String getStringFromBuffer(ByteBuffer buffer){
        List<Byte> bytes = new ArrayList<>();
        byte bt;
        while((bt = buffer.get()) != (byte)0){
            bytes.add(bt);
        }
        //return new String(Arrays.stream(bytes.stream().toArray()).map(elem -> Byteelem), UTF_8);
        byte[] bytesArr = new byte[bytes.size()];
        for (int i = 0; i < bytes.size(); i++) {
            bytesArr[i] = bytes.get(i);
        }
        return new String(bytesArr, UTF_8);
    }
}