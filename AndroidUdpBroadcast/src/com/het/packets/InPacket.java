package com.het.packets;

import com.het.HET;

import java.nio.ByteBuffer;

/**
 * Created by HET on 2014-43-10.
 */
public abstract class InPacket extends Packet {

    public InPacket() {
        super();
    }

    protected InPacket(short command, byte[] time, byte[] mac, byte[] b) {
        super(command, time, mac, b);
    }

    protected InPacket(ByteBuffer buf) throws PacketParseException {
        super(buf);
    }

    protected InPacket(ByteBuffer buf, int length) throws PacketParseException {
        super(buf, length);
    }

    @Override
    protected boolean validateHeader(ByteBuffer buf) {
        if (packetStart == buf.get()) {
            return true;
        }
        return false;
    }

    @Override
    public int getLength(int bodyLength) {
        return HET.HET_LENGTH_BASIC_OUT_HEADER + HET.HET_LENGTH_BASIC_TAIL + bodyLength;
    }

    @Override
    protected int getHeadLength() {
        return HET.HET_LENGTH_BASIC_OUT_HEADER;
    }

    @Override
    protected int getTailLength() {
        return HET.HET_LENGTH_BASIC_TAIL;
    }

    @Override
    protected void putHead(ByteBuffer buf) {
        super.putHead(buf);
    }

    @Override
    protected void putBody(ByteBuffer buf) {
        buf.put(body);
    }

    @Override
    protected byte[] getBodyBytes(ByteBuffer buf, int length) {
        /**得到包体长度*/
        int bodyLen = length - HET.HET_LENGTH_BASIC_OUT_HEADER - HET.HET_LENGTH_BASIC_TAIL;
        /**得到包体内容*/
        byte[] body = new byte[bodyLen];
        buf.get(body);
        return body;
    }

    @Override
    protected void putTail(ByteBuffer buf) {
        buf.put(packetEnd);
    }

    @Override
    protected void parseHeader(ByteBuffer buf) throws PacketParseException {
        buf.get(ctrbhVersion);
        protocolVersion = buf.getShort();
        encryptType = buf.get();
        packetType = buf.get();
        uploadItem = buf.get();
        command = buf.getShort();
        buf.get(macAddr);
        timeZone = buf.get();
        buf.get(time);
        dataLen = buf.getShort();
    }

    @Override
    protected void parseTail(ByteBuffer buf) throws PacketParseException {
        buf.get();
    }

}
