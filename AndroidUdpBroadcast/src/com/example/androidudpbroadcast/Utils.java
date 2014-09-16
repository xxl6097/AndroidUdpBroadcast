package com.example.androidudpbroadcast;

import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import org.apache.http.conn.util.InetAddressUtils;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;

public class Utils {
    public static final String YY_MM_DD_DD_HH_MM_SS = "yyyy-MM-dd-hh-mm-ss";
    private static Context mContext;

    public static byte[] getBodyBytes(String ip, String port, String key) throws NumberFormatException, IOException {

        String[] ipArr = ip.split("\\.");
        byte[] ipByte = new byte[4];
        ipByte[0] = (byte) (Integer.parseInt(ipArr[0]) & 0xFF);
        ipByte[1] = (byte) (Integer.parseInt(ipArr[1]) & 0xFF);
        ipByte[2] = (byte) (Integer.parseInt(ipArr[2]) & 0xFF);
        ipByte[3] = (byte) (Integer.parseInt(ipArr[3]) & 0xFF);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        dos.write(ipByte);
        dos.writeShort(Short.parseShort(port));
        dos.writeByte(key.getBytes().length);
        dos.write(key.getBytes());
        byte[] bs = baos.toByteArray();
        baos.close();
        dos.close();

        return bs;
    }

    public static byte[] macToBytes(String mac) {
        byte[] resBytes = new byte[6];
        resBytes[0] = (byte) Integer.parseInt(mac.substring(0, 2), 16);
        resBytes[1] = (byte) Integer.parseInt(mac.substring(2, 4), 16);
        resBytes[2] = (byte) Integer.parseInt(mac.substring(4, 6), 16);
        resBytes[3] = (byte) Integer.parseInt(mac.substring(6, 8), 16);
        resBytes[4] = (byte) Integer.parseInt(mac.substring(8, 10), 16);
        resBytes[5] = (byte) Integer.parseInt(mac.substring(10, 12), 16);
        return resBytes;
    }

    public static byte[] getPacketTime() {
        byte[] bytePacketTime = new byte[7];
        String stime = GetNowDate(YY_MM_DD_DD_HH_MM_SS);
        String[] sarr = stime.split("-");
        String a = sarr[0].substring(0, 2);
        bytePacketTime[0] = (byte) Integer.parseInt(sarr[0].substring(0, 2));
        bytePacketTime[1] = (byte) Integer.parseInt(sarr[0].substring(2, 4));
        bytePacketTime[2] = (byte) Integer.parseInt(sarr[1]);
        bytePacketTime[3] = (byte) Integer.parseInt(sarr[2]);
        bytePacketTime[4] = (byte) Integer.parseInt(sarr[3]);
        bytePacketTime[5] = (byte) Integer.parseInt(sarr[4]);
        bytePacketTime[6] = (byte) Integer.parseInt(sarr[5]);
        return bytePacketTime;
    }

    public static String GetNowDate(String format) {
        String temp_str = "";
        Date dt = new Date();
        //�?��的aa表示“上午�?或�?下午�?   HH表示24小时�?   如果换成hh表示12小时�?
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        temp_str = sdf.format(dt);
        return temp_str;
    }


    public static String getLocalHostIp() {
        String ipaddress = "";
        try {
            Enumeration<NetworkInterface> en = NetworkInterface
                    .getNetworkInterfaces();
            // �������õ�����ӿ�?
            while (en.hasMoreElements()) {
                NetworkInterface nif = en.nextElement();// �õ�ÿһ������ӿڰ󶨵�����ip
                Enumeration<InetAddress> inet = nif.getInetAddresses();
                // ����ÿһ���ӿڰ󶨵�����ip
                while (inet.hasMoreElements()) {
                    InetAddress ip = inet.nextElement();
                    if (!ip.isLoopbackAddress()
                            && InetAddressUtils.isIPv4Address(ip
                            .getHostAddress())) {
                        return ip.getHostAddress();
                    }
                }

            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return ipaddress;

    }

    public static String getSSid(Context ctx) {
        WifiManager wm = (WifiManager) ctx.getSystemService(Context.WIFI_SERVICE);
        if (wm != null) {
            WifiInfo wi = wm.getConnectionInfo();
            if (wi != null) {
                String s = wi.getSSID();
                if (s.length() > 2 && s.charAt(0) == '"'
                        && s.charAt(s.length() - 1) == '"') {
                    return s.substring(0, s.length() - 1);
                }
            }
        }
        return "";
    }

    public static String getBroadcastAddress(Context ctx) {
        WifiManager cm = (WifiManager) ctx
                .getSystemService(Context.WIFI_SERVICE);
        DhcpInfo myDhcpInfo = cm.getDhcpInfo();
        if (myDhcpInfo == null) {
            return "255.255.255.255";
        }
        // int broadcast = (myDhcpInfo.ipAddress & myDhcpInfo.netmask)
        // | ~myDhcpInfo.netmask;
        int broadcast = (myDhcpInfo.ipAddress & myDhcpInfo.netmask)
                | ~myDhcpInfo.netmask;
        byte[] quads = new byte[4];
        for (int k = 0; k < 4; k++)
            quads[k] = (byte) ((broadcast >> k * 8) & 0xFF);
        try {
            return InetAddress.getByAddress(quads).getHostAddress();
        } catch (Exception e) {
            return "255.255.255.255";
        }
    }
}
