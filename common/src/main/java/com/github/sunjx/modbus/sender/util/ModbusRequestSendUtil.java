
package com.github.sunjx.modbus.sender.util;


import com.github.sunjx.modbus.common.util.IntArrayUtil;
import com.github.sunjx.modbus.common.util.ParseStringUtil;
import com.github.sunjx.modbus.protocol.ModbusFunction;
import com.github.sunjx.modbus.sender.ChannelSender;
import com.github.sunjx.modbus.sender.ChannelSenderFactory;
import io.netty.channel.Channel;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.List;


public class ModbusRequestSendUtil {
    public enum PriorityStrategy {
        Channel, Req;
    }


    private static final InternalLogger log = InternalLoggerFactory.getInstance(ModbusRequestSendUtil.class);


    public static void sendRequests(Collection<Channel> channels, List<String> reqs, boolean isAllUseAsync, int fixedDelay, PriorityStrategy strategy) {
        switch (strategy) {
            case Channel:
                sendRequestsByChannelFirst(channels, reqs, isAllUseAsync, fixedDelay);
            case Req:
                sendRequestsByReqFirst(channels, reqs, isAllUseAsync, fixedDelay);
                break;
            default:
                break;
        }

    }

    /**
     * todo
     *
     * @param channels
     * @param reqs
     * @param isAllUseAsync
     * @param fixedDelay
     */
    private static void sendRequestsByChannelFirst(Collection<Channel> channels, List<String> reqs, boolean isAllUseAsync, int fixedDelay) {
        for (Channel channel : channels) {
            if (channel == null || !channel.isActive() || !channel.isOpen() || !channel.isWritable()) {
                continue;
            }
            ChannelSender sender = ChannelSenderFactory.getInstance().get(channel);
            for (String str : reqs) {
                try {
                    long startTime = System.currentTimeMillis();
                    String[] args = str.split("[;|]");
                    if (args.length >= 3) {
                        String funcString = args[0];
                        // 寄存器地址起
                        String addressString = args[1];
                        String value = args[2];
                        if (isAllUseAsync && !funcString.endsWith("Async")) {
                            funcString = funcString + "Async";
                        }
                        if (!funcString.endsWith("Async")) {
                            sendSyncFunc(sender, funcString, addressString, value);
                        } else {
                            sendAsyncFunc(sender, funcString, addressString, value);
                        }
                        long endTime = System.currentTimeMillis();
                        long span = endTime - startTime;
                        if (fixedDelay - span > 0L) {
                            Thread.sleep(fixedDelay - span);
                        }
                    }
                } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException | IllegalArgumentException | SecurityException | InterruptedException ex) {
                    log.error("sendRequestsByChannelFirst", ex);
                }

            }

        }

    }


    private static void sendRequestsByReqFirst(Collection<Channel> channels, List<String> reqs, boolean isAllUseAsync, int fixedDelay) {
        for (String str : reqs) {
            long startTime = System.currentTimeMillis();
            for (Channel channel : channels) {

                try {
                    if (channel == null || !channel.isActive() || !channel.isOpen() || !channel.isWritable()) {
                        continue;
                    }
                    ChannelSender sender = ChannelSenderFactory.getInstance().get(channel);
                    String[] args = str.split("[;|]");
                    if (args.length >= 3) {
                        // todo
                        // readHoldingRegistersAsync
                        String funcString = args[0];

                        // 1026 寄存器地址
                        String addressString = args[1];

                        // 1 类型
                        String value = args[2];

                        if (isAllUseAsync && !funcString.endsWith("Async")) {
                            funcString = funcString + "Async";
                        }
                        if (!funcString.endsWith("Async")) {
                            sendSyncFunc(sender, funcString, addressString, value);
                            continue;

                        }
                        sendAsyncFunc(sender, funcString, addressString, value);
                    }
                } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException | IllegalArgumentException | SecurityException ex) {
                    log.error("sendRequestsByReqFirst", ex);
                }
            }
            /* 112 */
            long endTime = System.currentTimeMillis();
            /* 113 */
            long span = endTime - startTime;
            /* 114 */
            if (span < fixedDelay) {

                try {
                    /* 116 */
                    Thread.sleep(fixedDelay - span);
                    /* 117 */
                } catch (InterruptedException ex) {
                    /* 118 */
                    log.error("sendRequestsByReqFirst", ex);

                }

            }

        }

    }


    public static ModbusFunction sendSyncFunc(ChannelSender sender, String funcString, String addressStr, String value) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
        /* 126 */
        ModbusFunction respFunction = null;
        /* 127 */
        if (!funcString.startsWith("#")) {
            /* 128 */
            Integer address = Integer.valueOf(addressStr);
            /* 129 */
            if ("TF".contains(value)) {
                /* 130 */
                respFunction = ModbusChannelSenderUtil.sendSyncFunc(sender, funcString, address.intValue(), value.equalsIgnoreCase("T"));
                /* 131 */
            } else if (value.contains(",")) {
                /* 132 */
                String[] values = value.split("[,]");
                /* 133 */
                if (value.contains("T") || value.contains("F")) {
                    /* 134 */
                    respFunction = ModbusChannelSenderUtil.sendSyncFunc(sender, funcString, address.intValue(), ParseStringUtil.parseBooleanArray(values));

                } else {
                    /* 136 */
                    respFunction = ModbusChannelSenderUtil.sendSyncFunc(sender, funcString, address.intValue(), IntArrayUtil.toIntArray(values));

                }

            } else {
                // 目前使用这个方法
                respFunction = ModbusChannelSenderUtil.sendSyncFunc(sender, funcString, address.intValue(), Integer.valueOf(value).intValue());

            }

        }
        /* 142 */
        return respFunction;

    }


    public static int sendAsyncFunc(ChannelSender sender, String funcString, String addressStr, String value) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
        int transactionId = 0;
        if (!funcString.startsWith("#")) {
            Integer address = Integer.valueOf(addressStr);
            if (!funcString.endsWith("Async")) {
                funcString = funcString + "Async";
            }
            if ("TF".contains(value)) {
                transactionId = ModbusChannelSenderUtil.sendAsyncFunc(sender, funcString, address, value.equalsIgnoreCase("T"));
            } else if (value.contains(",")) {
                String[] values = value.split("[,]");
                if (value.contains("T") || value.contains("F")) {
                    transactionId = ModbusChannelSenderUtil.sendAsyncFunc(sender, funcString, address, ParseStringUtil.parseBooleanArray(values));
                } else {
                    transactionId = ModbusChannelSenderUtil.sendAsyncFunc(sender, funcString, address, IntArrayUtil.toIntArray(values));
                }
            } else {
                transactionId = ModbusChannelSenderUtil.sendAsyncFunc(sender, funcString, address, Integer.valueOf(value));
            }
        }
        return transactionId;

    }

}


/* Location:              D:\logs\easymodbus4j-0.0.5.jar!\com\github\zengfr\easymodbus4j\sende\\util\ModbusRequestSendUtil.class
 * Java compiler version: 8 (52.0)
 * JD-Core Version:       1.1.2
 */