package com.het.packets;

import com.het.HET;

import java.nio.ByteBuffer;

/**
 * Created by HET on 2014-49-10.
 */
public abstract class OutPacket extends Packet {
    protected OutPacket() {
    }

    protected OutPacket(short command, byte[] time, byte[] mac, byte[] b) {
        super(command, time, mac, b);
    }

    protected OutPacket(ByteBuffer buf) throws PacketParseException {
        super(buf);
    }

    protected OutPacket(ByteBuffer buf, int length) throws PacketParseException {
        super(buf, length);
    }

    /**
     * 将整个包转化为字节流, 并写入指定的ByteBuffer对象.
     * 一般而言, 前后分别需要写入包头部和包尾部.
     *
     * @param buf 将包写入的ByteBuffer对象.
     */
    public void fill(ByteBuffer buf) {
        /**填充头部*/
        putHead(buf);
        /** 填充包体*/
        putBody(buf);
        /**填充尾部*/
        putTail(buf);
    }

    @Override
    protected void putHead(ByteBuffer buf) {
        super.putHead(buf);
    }

    @Override
    public int getLength(int bodyLength) {
        //悠视优化_包长度为包头加包体,去掉Header,Tail内容
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
    protected void parseBody(ByteBuffer buf) throws PacketParseException {

    }

    /**
     * 封装包体
     *
     * @param buf ByteBuffer
     */
    @Override
    protected void putBody(ByteBuffer buf) {
        buf.put(body);
    }

    /**
     * 封装包尾
     *
     * @param buf 写入的ByteBuffer对象.
     */
    @Override
    protected void putTail(ByteBuffer buf) {
        buf.put(packetEnd);
    }

    /**
     * 获取包体
     *
     * @param buf    ByteBuffer
     * @param length 包总长度
     * @return
     */
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

    @Override
    protected boolean validateHeader(ByteBuffer buf) {
        if (packetStart == buf.get()) {
            return true;
        }
        return false;
    }
}
