package com.het.net;

import java.io.IOException;
import java.net.*;

public class UdpUnicast {
    private static final int BUFFER_SIZE = 2048;
    private byte[] buffer = new byte[BUFFER_SIZE];
    private String ip;
    private int port = 49999;
    private DatagramSocket socket;
    private DatagramPacket packetToSend;
    private InetAddress inetAddress;
    private ReceiveData receiveData;
    private UdpUnicastListener listener;

    public UdpUnicast(String ip, int port) {
        super();
        this.ip = ip;
        this.port = port;
    }

    public UdpUnicast() {
        super();
    }

    /**
     * @return the ip
     */
    public String getIp() {
        return ip;
    }

    /**
     * @param ip the ip to set
     */
    public void setIp(String ip) {
        this.ip = ip;
    }

    /**
     * @return the port
     */
    public int getPort() {
        return port;
    }

    /**
     * @param port the port to set
     */
    public void setPort(int port) {
        this.port = port;
    }

    /**
     * @return the listener
     */
    public UdpUnicastListener getListener() {
        return listener;
    }

    /**
     * @param listener the listener to set
     */
    public void setListener(UdpUnicastListener listener) {
        this.listener = listener;
    }

    /**
     * Open udp socket
     */
    public synchronized boolean open() {

        try {
            inetAddress = InetAddress.getByName(ip);
        } catch (UnknownHostException e) {
            e.printStackTrace();
            return false;
        }

        try {
            socket = new DatagramSocket(port);
        } catch (SocketException e) {
            e.printStackTrace();
            return false;
        }

        //receive response
        receiveData = new ReceiveData();
        receiveData.start();
        return true;
    }

    /**
     * Close udp socket
     */
    public synchronized void close() {
        stopReceive();
        if (socket != null) {
            socket.close();
        }
    }

    /**
     * send message
     *
     * @param nPacket the message to broadcast
     */
    public synchronized boolean send(byte[] nPacket) {
        if (socket == null) {
            return false;
        }

        if (nPacket == null) {
            return true;
        }

        packetToSend = new DatagramPacket(nPacket, nPacket.length, inetAddress, port);

        //send data
        try {
            socket.send(packetToSend);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * send message
     *
     * @param text the message to broadcast
     */
    public synchronized boolean send(String text) {
        if (socket == null) {
            return false;
        }

        if (text == null) {
            return true;
        }

        packetToSend = new DatagramPacket(
                text.getBytes(), text.getBytes().length, inetAddress, port);

        //send data
        try {
            socket.send(packetToSend);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * send message
     *
     * @param text the message to broadcast
     */
    public synchronized boolean send(String text, String ip, int port) {
        if (socket == null) {
            return false;
        }

        if (text == null) {
            return true;
        }

        try {
            inetAddress = InetAddress.getByName(ip);
        } catch (UnknownHostException e) {
            e.printStackTrace();
            return false;
        }

        packetToSend = new DatagramPacket(
                text.getBytes(), text.getBytes().length, inetAddress, port);

        //send data
        try {
            socket.send(packetToSend);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Stop to receive
     */
    public void stopReceive() {
        if (receiveData != null && !receiveData.isStoped()) {
            receiveData.stop();
        }
    }

    public void onReceive(byte[] buffer, int length) {
        if (listener != null) {
            listener.onReceived(buffer, length);
        }
    }

    public interface UdpUnicastListener {
        public void onReceived(byte[] data, int length);
    }

    private class ReceiveData implements Runnable {

        private boolean stop;
        private Thread thread;

        private ReceiveData() {
            thread = new Thread(this);
        }

        @Override
        public void run() {

            while (!stop) {
                try {
                    DatagramPacket packetToReceive = new DatagramPacket(buffer, BUFFER_SIZE);
                    socket.receive(packetToReceive);
                    onReceive(buffer, packetToReceive.getLength());
                } catch (SocketTimeoutException e) {
                    System.out.println("Receive packet timeout!");
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }

        void start() {
            thread.start();
        }

        void stop() {
            stop = true;
        }

        boolean isStoped() {
            return stop;
        }
    }
}
