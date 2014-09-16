package com.het.packets;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

/**
 * Created by HET on 2014-50-10.
 */
public abstract class Packet {
    /**
     * 报文起始
     */
    public static final byte packetStart = (byte) 0XF2;
    /**
     * 报文结束
     */
    public static final byte packetEnd = (byte) 0XF3;
    /**
     * 终端软件版本字节�?
     */
    protected byte[] ctrbhVersion = new byte[]{0x01, 0x01, 0x01};
    /**
     * 协议版本 byte[]{0x11,0x11}
     */
    protected short protocolVersion = 4369;
    /**
     * 加密类型
     */
    protected byte encryptType = 0x00;
    /**
     * 数据上下行分类(00-设备终端发服务器或手机发服务器,01-服务器发设备终端)
     */
    protected byte packetType = 0x00;
    /**
     * 上行主体(00-设备终端,01-其他为手机中转上行,02-为用户请求)
     */
    protected byte uploadItem = 0x00;
    /**
     * 命令字
     */
    protected short command;
    /**
     * 上行账号字节组（设备MAC�?
     */
    protected byte[] macAddr = new byte[6];
    /**
     * 协议时间
     */
    protected byte timeZone = (byte) 0XFF;
    /**
     * 协议时间
     */
    protected byte[] time = new byte[7];
    /**
     * 数据长度
     */
    protected short dataLen;
    /**
     * 包文体
     */
    protected byte[] body;

    /**
     * 构造一个包对象，什么字段也不填，仅限于子类使用
     */
    protected Packet() {

    }

    protected Packet(short command, byte[] time, byte[] mac, byte[] b) {
        this.command = command;
        this.time = time;
        this.macAddr = mac;
        this.dataLen = (short) b.length;
        this.body = b;
    }

    /**
     * 从buf中构造一个OutPacket，用于调试。这个buf里面可能包含了抓包软件抓来的数据
     *
     * @param buf ByteBuffer
     * @throws PacketParseException 解析出错
     */
    protected Packet(ByteBuffer buf) throws PacketParseException {
        this(buf, buf.limit() - buf.position());
    }

    /**
     * 从buf中构造一个OutPacket，用于调试。这个buf里面可能包含了抓包软件抓来的数据
     *
     * @param buf    ByteBuffer
     * @param length 要解析的内容长度
     * @throws PacketParseException 如果解析出错
     */
    protected Packet(ByteBuffer buf, int length)
            throws PacketParseException {
        if (!validateHeader(buf)) {
            throw new PacketParseException("包头有误，抛弃该包: " + toString());
        }
        parseHeader(buf);
        /**得到包体*/
        byte[] body = getBodyBytes(buf, length);

        ByteBuffer tempBuf = ByteBuffer.wrap(body);
        try {
            /**解析包体*/
            parseBody(tempBuf);
        } catch (BufferUnderflowException e) {
            throw new PacketParseException(e.getMessage());
        }
        parseTail(buf);
    }

    /**
     * 将包头部转化为字节流, 写入指定的ByteBuffer对象.
     *
     * @param buf 写入的ByteBuffer对象.
     */
    protected void putHead(ByteBuffer buf) {
        buf.put(packetStart);
        buf.put(ctrbhVersion);
        buf.putShort(protocolVersion);
        buf.put(encryptType);
        buf.put(packetType);
        buf.put(uploadItem);
        buf.putShort(command);
        buf.put(macAddr);
        buf.put(timeZone);
        buf.put(time);
        buf.putShort(dataLen);
    }

    public byte[] getCtrbhVersion() {
        return ctrbhVersion;
    }

    public void setCtrbhVersion(byte[] ctrbhVersion) {
        this.ctrbhVersion = ctrbhVersion;
    }

    public short getProtocolVersion() {
        return protocolVersion;
    }

    public void setProtocolVersion(short protocolVersion) {
        this.protocolVersion = protocolVersion;
    }

    public byte getEncryptType() {
        return encryptType;
    }

    public void setEncryptType(byte encryptType) {
        this.encryptType = encryptType;
    }

    public byte getPacketType() {
        return packetType;
    }

    public void setPacketType(byte packetType) {
        this.packetType = packetType;
    }

    public byte getUploadItem() {
        return uploadItem;
    }

    public void setUploadItem(byte uploadItem) {
        this.uploadItem = uploadItem;
    }

    public short getCommand() {
        return command;
    }

    public void setCommand(short command) {
        this.command = command;
    }

    public byte[] getMacAddr() {
        return macAddr;
    }

    public void setMacAddr(byte[] macAddr) {
        this.macAddr = macAddr;
    }

    public byte getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(byte timeZone) {
        this.timeZone = timeZone;
    }

    public byte[] getTime() {
        return time;
    }

    public void setTime(byte[] time) {
        this.time = time;
    }

    public short getDataLen() {
        return dataLen;
    }

    public void setDataLen(short dataLen) {
        this.dataLen = dataLen;
    }

    public byte[] getBody() {
        return body;
    }

    public void setBody(byte[] body) {
        this.body = body;
    }

    /**
     * 校验头部
     *
     * @return true表示头部有效
     */
    protected abstract boolean validateHeader(ByteBuffer buf);

    /**
     * 得到UDP形式包的总长度，不考虑TCP形式
     *
     * @param bodyLength 包体长度
     * @return 包长度
     */
    public abstract int getLength(int bodyLength);

    /**
     * @return 包头长度
     */
    protected abstract int getHeadLength();

    /**
     * @return 包尾长度
     */
    protected abstract int getTailLength();

    /**
     * 将包头部转化为字节流, 写入指定的ByteBuffer对象.
     *
     * @param buf 写入的ByteBuffer对象.
     */
//    protected abstract void putHead(ByteBuffer buf);

    /**
     * 初始化包体
     *
     * @param buf ByteBuffer
     */
    protected abstract void putBody(ByteBuffer buf);

    /**
     * 得到包体的字节数组
     *
     * @param buf    ByteBuffer
     * @param length 包总长度
     * @return 包体字节数组
     */
    protected abstract byte[] getBodyBytes(ByteBuffer buf, int length);

    /**
     * 将包尾部转化为字节流, 写入指定的ByteBuffer对象.
     *
     * @param buf 写入的ByteBuffer对象.
     */
    protected abstract void putTail(ByteBuffer buf);

    /**
     * 解析包体，从buf的开头位置解析起
     *
     * @param buf ByteBuffer
     * @throws PacketParseException 如果解析出错
     */
    protected abstract void parseBody(ByteBuffer buf)
            throws PacketParseException;


    /**
     * 从buf的当前位置解析包头
     *
     * @param buf ByteBuffer
     * @throws PacketParseException 如果解析出错
     */
    protected abstract void parseHeader(ByteBuffer buf)
            throws PacketParseException;

    /**
     * 从buf的当前未知解析包尾
     *
     * @param buf ByteBuffer
     * @throws PacketParseException 如果解析出错
     */
    protected abstract void parseTail(ByteBuffer buf)
            throws PacketParseException;

}
