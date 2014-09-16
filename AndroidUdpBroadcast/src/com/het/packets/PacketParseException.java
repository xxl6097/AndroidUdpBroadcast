package com.het.packets;


/**
 * 当包解析出错时，抛出这个异常
 */
public class PacketParseException extends Exception {
    private static final long serialVersionUID = 3257284738459775545L;

    public PacketParseException() {
        super();
    }

    public PacketParseException(String arg0) {
        super(arg0);
    }

    public PacketParseException(Throwable arg0) {
        super(arg0);
    }

    public PacketParseException(String arg0, Throwable arg1) {
        super(arg0, arg1);
    }
}
