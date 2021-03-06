package com.github.sunjx.modbus.sender.util;

import com.github.sunjx.modbus.protocol.ModbusFunction;
import com.github.sunjx.modbus.sender.ChannelSender;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;


public class ModbusChannelSenderUtil {
    protected static Class<ChannelSender> channelSenderClazz = ChannelSender.class;


    public static ModbusFunction sendSyncFunc(ChannelSender sender, String func, int address, int value) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
        /* 33 */
        Method method = channelSenderClazz.getMethod(func, new Class[]{int.class, int.class});
        /* 34 */
        return (ModbusFunction) method.invoke(sender, new Object[]{Integer.valueOf(address), Integer.valueOf(value)});
    }


    public static ModbusFunction sendSyncFunc(ChannelSender sender, String func, int address, boolean value) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
        /* 40 */
        Method method = channelSenderClazz.getMethod(func, new Class[]{int.class, boolean.class});
        /* 41 */
        return (ModbusFunction) method.invoke(sender, new Object[]{Integer.valueOf(address), Boolean.valueOf(value)});
    }


    public static ModbusFunction sendSyncFunc(ChannelSender sender, String func, int address, int[] values) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
        /* 47 */
        Method method = channelSenderClazz.getMethod(func, new Class[]{int.class, int.class, int[].class});
        /* 48 */
        return (ModbusFunction) method.invoke(sender, new Object[]{Integer.valueOf(address), Integer.valueOf(values.length), values});
    }


    public static ModbusFunction sendSyncFunc(ChannelSender sender, String func, int address, boolean[] values) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
        /* 54 */
        Method method = channelSenderClazz.getMethod(func, new Class[]{int.class, int.class, boolean[].class});
        /* 55 */
        return (ModbusFunction) method.invoke(sender, new Object[]{Integer.valueOf(address), Integer.valueOf(values.length), values});
    }


    // *
    public static int sendAsyncFunc(ChannelSender sender, String func, int address, int value) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
        Method method = channelSenderClazz.getMethod(func, int.class, int.class);
        return (Integer) method.invoke(sender, new Object[]{address, value});
    }


    public static int sendAsyncFunc(ChannelSender sender, String func, int address, boolean value) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
        /* 67 */
        Method method = channelSenderClazz.getMethod(func, new Class[]{int.class, boolean.class});
        /* 68 */
        return ((Integer) method.invoke(sender, new Object[]{Integer.valueOf(address), Boolean.valueOf(value)})).intValue();
    }


    public static int sendAsyncFunc(ChannelSender sender, String func, int address, int[] values) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
        /* 74 */
        Method method = channelSenderClazz.getMethod(func, new Class[]{int.class, int.class, int[].class});
        /* 75 */
        return ((Integer) method.invoke(sender, new Object[]{Integer.valueOf(address), Integer.valueOf(values.length), values})).intValue();
    }


    public static int sendAsyncFunc(ChannelSender sender, String func, int address, boolean[] values) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
        /* 81 */
        Method method = channelSenderClazz.getMethod(func, new Class[]{int.class, int.class, boolean[].class});
        /* 82 */
        return ((Integer) method.invoke(sender, new Object[]{Integer.valueOf(address), Integer.valueOf(values.length), values})).intValue();
    }
}
