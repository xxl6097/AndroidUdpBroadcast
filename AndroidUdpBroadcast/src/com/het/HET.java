package com.het;

/**
 * Created by HET on 2014-24-10.
 */
public class HET {
    /**
     * 输出包的包头长度
     */
    public static final int HET_LENGTH_BASIC_OUT_HEADER = 27;
    /**
     * 包尾长度
     */
    public static final int HET_LENGTH_BASIC_TAIL = 1;

    /**
     * 包长度为包头加包体,去掉Header,Tail内容*
     */

    public final class COMMAND {
        /**
         * 绑定模式命令字*
         */
        public static final byte HET_BIND_CMD = 0x0051;
        /**
         * 设备发送命令字*
         */
        public static final byte HET_SEND_TO_SEVER_CMD = 0x0053;
        /**
         * 设备或者服务器有设置参数更改*
         */
        public static final byte HET_RECV_FORM_SERVER_CMD = 0x0056;
        /**
         * 设备向APP定时返回运行参数*
         */
        public static final byte HET_TIMER_TO_SERVER_CMD = 0x0052;
    }
}