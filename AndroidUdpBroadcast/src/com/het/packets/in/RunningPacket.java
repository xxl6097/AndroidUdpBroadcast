package com.het.packets.in;

import com.het.packets.InPacket;
import com.het.packets.PacketParseException;

import java.nio.ByteBuffer;

/**
 * Created by HET on 2014-46-10.
 */
public class RunningPacket extends InPacket {
    private String ip;
    private int port;
    private String key;


    public RunningPacket() {
    }

    public RunningPacket(short command, byte[] time, byte[] mac, byte[] b) {
        super(command, time, mac, b);
    }

    public RunningPacket(ByteBuffer buf) throws PacketParseException {
        super(buf);
    }

    public RunningPacket(ByteBuffer buf, int length) throws PacketParseException {
        super(buf, length);
    }

    @Override
    protected void parseBody(ByteBuffer buf) throws PacketParseException {
        byte[] ip = new byte[4];
        buf.get(ip);
        int n1 = ip[0] & 0XFF;
        int n2 = ip[1] & 0XFF;
        int n3 = ip[2] & 0XFF;
        int n4 = ip[3] & 0XFF;
        this.port = buf.getShort();
        int length = buf.get();
        byte[] key = new byte[length];
        buf.get(key);
        this.key = new String(key);
        this.ip = n1 + "." + n2 + "." + n3 + "." + n4;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    @Override
    public String toString() {
        return "RunningPacket{" +
                "ip='" + ip + '\'' +
                ", port=" + port +
                ", key='" + key + '\'' +
                '}';
    }
}
