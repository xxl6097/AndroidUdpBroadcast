package com.example.androidudpbroadcast;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.het.net.SmartLinkManipualtor;
import com.het.net.SmartLinkManipualtor.ConnectCallBack;
import com.het.net.SmartLinkManipualtor.ModuleInfo;
import com.het.net.UdpTransmission;
import com.het.packets.PacketParseException;
import com.het.packets.in.RunningPacket;
import com.het.packets.out.BindPacket;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class MainActivity extends Activity {
    private TextView show;
    private SmartLinkManipualtor sm;
    private boolean isconncting = false;
    ConnectCallBack callback = new ConnectCallBack() {

        @Override
        public void onConnectTimeOut() {
            mHander.post(new Runnable() {

                @Override
                public void run() {
                    Toast.makeText(MainActivity.this, "Config TimeOut",
                            Toast.LENGTH_SHORT).show();
                    String t = show.getText().toString();
                    t += "Config TimeOut\n";
                    show.setText(t);
                    mConfigBtn.setText("StartLink");
                    isconncting = false;
                }
            });
        }

        @Override
        public void onConnect(final ModuleInfo mi) {
            mHander.post(new Runnable() {

                @Override
                public void run() {
                    Toast.makeText(
                            MainActivity.this,
                            "Find Device  " + mi.getMid() + "mac" + mi.getMac() + "IP"
                                    + mi.getModuleIP(), Toast.LENGTH_SHORT
                    )
                            .show();
                    String t = show.getText().toString();
                    t += "Find Device  " + mi.getMid() + "mac" + mi.getMac() + "IP"
                            + mi.getModuleIP() + "\n";
                    show.setText(t);
                }
            });
        }

        @Override
        public void onConnectOk() {
            mHander.post(new Runnable() {

                @Override
                public void run() {
                    Toast.makeText(MainActivity.this, "Config Sucessfull",
                            Toast.LENGTH_SHORT).show();
                    mConfigBtn.setText("StartLink");
                    isconncting = false;
                    String t = show.getText().toString();
                    t += "Config Sucessfull " + "\n";
                    show.setText(t);
                }
            });
        }

        @Override
        public void onShowMsg(String m) {
            if (m.length() < 76) {
                Log.i("ooooooooooo", m);
                Message msg = mHander.obtainMessage();
                msg.what = 3;
                msg.obj = m;
                mHander.sendMessage(msg);
            }
        }
    };
    private Button mConfigBtn, mSendBtn;
    private EditText pswd;
    private Handler mHander = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    mConfigBtn.setText("StopLink");
                    break;
                case 2:
                    mConfigBtn.setText("StartLink");
                    break;
                case 3:
                    String tect = show.getText().toString();
                    String recv = (String) msg.obj;
                    tect += recv + "\n";
                    show.setText(tect);
                    Toast.makeText(MainActivity.this, recv, Toast.LENGTH_LONG).show();
                    break;
                default:
                    break;
            }
            super.handleMessage(msg);
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            newOutPacket();
        } catch (Exception e) {
            e.printStackTrace();
        }
        setContentView(R.layout.activity_main);
        show = (TextView) findViewById(R.id.textView1);
        show.setText(Utils.getLocalHostIp());
        mConfigBtn = (Button) findViewById(R.id.button01);
        TextView ssid = (TextView) findViewById(R.id.ssid);
        ssid.setText(Utils.getSSid(this));
        pswd = (EditText) findViewById(R.id.pswd);
        sm = SmartLinkManipualtor.getInstence(Utils.getBroadcastAddress(this));
        mConfigBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                new Thread(new Runnable() {

                    @Override
                    public void run() {
                        if (!isconncting) {
                            isconncting = true;
                            String ss = Utils.getSSid(MainActivity.this);
                            String ps = pswd.getText().toString().trim();
                            mHander.sendEmptyMessage(1);
                            sm.setSsidPwd(ps);
                            sm.startConfig(callback);
                        } else {
                            sm.StopConnection();
                            mHander.sendEmptyMessage(2);
                            isconncting = false;
                        }
                    }
                }).start();
            }
        });
        final UdpTransmission m = new UdpTransmission();
        m.start(callback);
        mSendBtn = (Button) findViewById(R.id.send);
        mSendBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                new Thread(new Runnable() {

                    @Override
                    public void run() {
//                        m.sendHFA11Cmd();
                        try {
                            m.send(getSendBytes());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        });
        show.setOnLongClickListener(new OnLongClickListener() {

            @Override
            public boolean onLongClick(View arg0) {
                show.setText("");
                return false;
            }
        });
    }

    @Override
    public void onBackPressed() {
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(0);
        finish();
        super.onBackPressed();
    }

    private byte[] getSendBytes() throws IOException {
        BindPacket packet = null;
        byte[] bodybyte = Utils.getBodyBytes("203.195.139.126", "30100", "strEcryptZwsUcorZkCrsujLiL6T2vQ==");
        byte[] mac = Utils.macToBytes("008732889182");
        packet = new BindPacket(mac, bodybyte);
        int len = packet.getLength(bodybyte.length);
        ByteBuffer buf = ByteBuffer.allocate(len);
        packet.fill(buf);
        buf.flip();
        return buf.array();
    }


    private void newOutPacket() throws IOException, PacketParseException {
        BindPacket packet = null;
        byte[] bodybyte = Utils.getBodyBytes("203.195.139.126", "30100", "strEcryptZwsUcorZkCrsujLiL6T2vQ==");
        byte[] mac = Utils.macToBytes("008732889182");
        packet = new BindPacket(mac, bodybyte);
        int len = packet.getLength(bodybyte.length);
        ByteBuffer buf = ByteBuffer.allocate(len);
        packet.fill(buf);
        buf.flip();
        System.out.println("SendDatas=" + Arrays.toString(buf.array()));
        RunningPacket run = new RunningPacket(buf);
        System.out.println("parseBody=" + run.toString());
    }
}
