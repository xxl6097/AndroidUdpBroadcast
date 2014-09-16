package com.het.net;

import java.io.UnsupportedEncodingException;
import java.util.HashSet;
import java.util.Set;

public class SmartLinkManipualtor {
    private static SmartLinkManipualtor instance = null;
    private final String RET_KEY = "smart_config";
    public boolean isConnecting = false;
    private int CONTENT_COUNT = 5;
    private int HEADER_COUNT = 200;
    private int HEADER_CAPACITY = 76;
    private int HEADER_PACKAGE_DELAY_TIME = 10;
    private int CONTENT_GROUP_DELAY_TIME = 500;
    private int CONTENT_PACKAGE_DELAY_TIME = 50;
    private int CONTENT_CHECKSUM_BEFORE_DELAY_TIME = 100;
    private String broadCastIP = "255.255.255.255";
    private String ssid_pwd;
    private Set<String> recvFindMacSet = new HashSet<String>();
    private Set<String> recvConnMacSet = new HashSet<String>();
    private int braodPort = 49999;
    private int transPort = 48899;
    private UdpUnicast udpUnicast;
    private byte receiveByte[] = new byte[512];
    private ConnectCallBack callback;

    private SmartLinkManipualtor(String broadCastIP) {
        this.broadCastIP = broadCastIP;
        init();
    }

    public static SmartLinkManipualtor getInstence(String castIp) {
        if (instance == null) {
            instance = new SmartLinkManipualtor(castIp);
        }
        return instance;
    }

    public boolean init() {
        if (isConnecting) {
            isConnecting = false;
            udpUnicast.stopReceive();
        } else {
            udpUnicast = new UdpUnicast(broadCastIP, braodPort);
            udpUnicast.setListener(new UdpUnicast.UdpUnicastListener() {
                @Override
                public void onReceived(byte[] data, int length) {
                    receive(data, length);
                }
            });
        }
        udpUnicast.open();
        return true;
    }

    /**
     * 处理接受UDP数据包
     *
     * @param data
     * @param len
     */
    private void receive(byte[] data, int len) {
        if (len > 0) {
            try {
                String receiveStr = new String(data, 0, len, "UTF-8");
                callback.onShowMsg(receiveStr);
                if (receiveStr.contains(RET_KEY)) {// 接收设备发送的smart_config广播
                    recvDeviceBackConfigCmd(receiveStr);
                } else {
                    recvDeviceBackData(receiveStr);
                }
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 收到设备返回smart_config数据包
     */
    private void recvDeviceBackConfigCmd(String receiveStr) {
        System.out.println(receiveStr);
        String mac = receiveStr.replace(RET_KEY, "").trim();
        if (!recvFindMacSet.contains(mac)) {
            recvFindMacSet.add(mac);
            try {
                for (int i = 0; i < 5; i++) {// 收到设备的smart_config广播，然后发送HF-A11ASSISTHREAD广播给设备，让其返回设备config
                    sendHFA11Cmd();
                    Thread.sleep(1000);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 收到设备返回数据包
     *
     * @param receiveStr
     */
    private void recvDeviceBackData(String receiveStr) {
        String[] info = receiveStr.split(",");
        if (info.length == 3) {
            System.out.println("get Module IP=" + receiveStr);
            ModuleInfo mi = new ModuleInfo();
            mi.setMac(info[1]);
            mi.setModuleIP(info[0]);
            mi.setMid(info[2]);
            if (recvFindMacSet.contains(mi.getMac())) {
                if (!recvConnMacSet.contains(mi.getMac())) {
                    callback.onConnect(mi);
                    recvConnMacSet.add(mi.getMac());
                }
            }
        }
    }

    /**
     * 设置路由器密码
     *
     * @param password
     */
    public void setSsidPwd(String password) {
        System.out.println("SSID.pwd=" + password);
        this.ssid_pwd = password;
    }

    ;

    /**
     * 开始配置
     */
    public void startConfig(ConnectCallBack c) {
        this.callback = c;
        isConnecting = true;
        recvFindMacSet.clear();
        recvConnMacSet.clear();
        new Thread(new Runnable() {
            @Override
            public void run() {
                // TODO Auto-generated method stub
                while (isConnecting) {
                    connect();
                }
            }
        }).start();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(10 * 1000);
                } catch (InterruptedException e) {
                }
                //向48899端口发送20次“smartlinkfind”UDP广播，间隔1s
                for (int i = 0; i < 20 && isConnecting; i++) {
                    sendFindCmd();
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                if (isConnecting) {
                    if (recvFindMacSet.size() <= 0)
                        callback.onConnectTimeOut();
                    else if (recvFindMacSet.size() > 0)
                        callback.onConnectOk();
                }
                StopConnection();
            }
        }).start();
    }

    public void sendFindCmd() {
        String command = "smartlinkfind";
        udpUnicast.send(command, broadCastIP, transPort);
    }

    public void sendHFA11Cmd() {
        String command = "HF-A11ASSISTHREAD";
        udpUnicast.send(command, broadCastIP, transPort);
    }

    public void StopConnection() {
        if (instance != null) {
            isConnecting = false;
            udpUnicast.stopReceive();
            udpUnicast.close();
            instance = null;
        }
    }

    // 发送200次76
    // for(此过程执行5次)
    // {
    // 1.头发三次(89)+body发一次(pwd)+尾发三次(86)
    // 2.此操作将 pwd长度+256+76 发送出去，也发3次，扰乱接受者
    // }
    // 大概耗时2000+7000=9000ms，第二个线程等10s，足以让此线程执行完毕后再顺序执行第二线程
    private void connect() {
        System.out.println("connect");
        int count = 1;
        byte[] header = this.getBytes(HEADER_CAPACITY);
        // 大概耗时200*10=2000ms
        while (count <= HEADER_COUNT && isConnecting) {
            udpUnicast.send(header);
            try {
                Thread.sleep(HEADER_PACKAGE_DELAY_TIME);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            count++;
        }
        String pwd = ssid_pwd;
        int[] content = new int[pwd.length() + 2];

        content[0] = 89;
        int j = 1;
        for (int i = 0; i < pwd.length(); i++) {
            content[j] = pwd.charAt(i) + 76;
            j++;
        }
        content[content.length - 1] = 86;

        count = 1;
        // for(此过程执行5次)
        // {
        // 1.头发三次+body发一次+尾发三次
        // 2.此操作将 密码长度+256+76 发送出去，也发3次，扰乱接受者
        // }
        // 大概耗时650+100+150+500=1400*5=7000ms
        while (count <= CONTENT_COUNT && isConnecting) {
            // 此for循环工作：头发三次+body发一次+尾发三次
            // 如果密码为6位，大概耗时50*3*2+50*6+50=650ms
            for (int i = 0; i < content.length; i++) {
                // JCTIP ver2 start end checksum send 3 time;
                int _count = 1;
                // 头和尾是标记，分别发三次广播。此作用就是扰乱接受者
                if (i == 0 || i == content.length - 1) {
                    _count = 3;
                }
                int t = 1;
                // 大概耗时50ms
                while (t <= _count && isConnecting) {
                    udpUnicast.send(getBytes(content[i]));
                    if (i != content.length) {
                        try {
                            Thread.sleep(CONTENT_PACKAGE_DELAY_TIME);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    t++;
                }
                // mConfigBroadUdp.send(getBytes(content[i]));

                if (i != content.length) {
                    try {
                        Thread.sleep(CONTENT_PACKAGE_DELAY_TIME);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            // 耗时100ms
            try {
                Thread.sleep(CONTENT_CHECKSUM_BEFORE_DELAY_TIME);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // int checkLength = pwd.length() * 30 + 76;
            // JCTIP ver1
            int checkLength = pwd.length() + 256 + 76;

            // JCTIP ver2 此操作将 密码长度+256+76 发送出去，也发3次，扰乱接受者
            int t = 1;
            // 耗时50*3=150ms
            while (t <= 3 && isConnecting) {
                udpUnicast.send(getBytes(checkLength));
                if (t < 3) {
                    try {
                        Thread.sleep(CONTENT_PACKAGE_DELAY_TIME);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                t++;
            }
            // mConfigBroadUdp.send(getBytes(checkLength));

            // 耗时500ms
            try {
                Thread.sleep(CONTENT_GROUP_DELAY_TIME);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            count++;
        }
        System.out.println("connect END");
    }

    private byte[] getBytes(int capacity) {
        byte[] data = new byte[capacity];
        for (int i = 0; i < capacity; i++) {
            data[i] = 5;
        }
        return data;
    }


    public interface ConnectCallBack {
        void onConnect(ModuleInfo mi);

        void onConnectTimeOut();

        void onConnectOk();

        void onShowMsg(String msg);
    }

    public class ModuleInfo {
        private String mac;
        private String ModuleIP;
        private String mid;

        public String getMac() {
            return this.mac;
        }

        public void setMac(String mac) {
            this.mac = mac;
        }

        public String getModuleIP() {
            return this.ModuleIP;
        }

        public void setModuleIP(String moduleIP) {
            this.ModuleIP = moduleIP;
        }

        public String getMid() {
            return this.mid;
        }

        public void setMid(String string) {
            this.mid = string;
        }
    }

}
