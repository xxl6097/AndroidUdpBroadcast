package com.het.net;

import com.het.net.SmartLinkManipualtor.ConnectCallBack;


public class UdpTransmission {
    private UdpUnicast udpUnicast;
    private String ip = "192.168.1.255";
    private int braodPort = 49999;
    private int transPort = 48899;

    public void start(ConnectCallBack c) {
        if (init(c)) {
            udpUnicast.open();
        }
    }

    public boolean init(final ConnectCallBack c) {
        udpUnicast = new UdpUnicast(ip, transPort);
        udpUnicast.setListener(new UdpUnicast.UdpUnicastListener() {
            @Override
            public void onReceived(byte[] data, int length) {
                String receiveStr = new String(data, 0, length);
                System.out.println("handleData:" + receiveStr);
                c.onShowMsg(receiveStr);
            }
        });

        return true;
    }


    public void sendFindCmd() {
        String command = "smartlinkfind";
        udpUnicast.send(command, ip, transPort);
    }

    public void sendHFA11Cmd() {
        String command = "HF-A11ASSISTHREAD";
        udpUnicast.send(command, ip, transPort);
    }

    public void send(byte[] data) {
        udpUnicast.send(data);
    }
}
