package com.het.packets.out;

import com.example.androidudpbroadcast.Utils;
import com.het.HET;
import com.het.packets.OutPacket;
import com.het.packets.PacketParseException;

import java.nio.ByteBuffer;

/**
 * Created by HET on 2014-18-10.
 */
public class BindPacket extends OutPacket {
    public BindPacket() {
    }

    public BindPacket(byte[] mac, byte[] b) {
        super(HET.COMMAND.HET_BIND_CMD, Utils.getPacketTime(), mac, b);
    }

    public BindPacket(ByteBuffer buf) throws PacketParseException {
        super(buf);
    }

    public BindPacket(ByteBuffer buf, int length) throws PacketParseException {
        super(buf, length);
    }
}
