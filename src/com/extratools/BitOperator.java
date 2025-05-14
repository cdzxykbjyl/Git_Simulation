package com.extratools;


import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.List;

/**
 * 二进制转换工具类
 */
@Slf4j
public class BitOperator {

    /**
     * 把一个整数该为byte
     *
     * @param value 需要转换为byte的整数
     * @return 整数转换后的byte
     */
    public static byte integerTo1Byte(int value) {
        return (byte) (value & 0xFF);
    }

    /**
     * 把一个整数转换为1位的byte数组
     *
     * @param value 需要转换成1位byte数组的整数
     * @return 整数转换后的byte数组
     */
    public static byte[] integerTo1Bytes(int value) {
        byte[] result = new byte[1];
        result[0] = (byte) (value & 0xFF);
        return result;
    }

    /**
     * 把一个整数转为2位的byte数组
     *
     * @param value 需要转换成2位byte数组的整数
     * @return 整数转换后的byte数组
     */
    public static byte[] integerTo2Bytes(int value) {
        byte[] result = new byte[2];
        result[0] = (byte) ((value >>> 8) & 0xFF);
        result[1] = (byte) (value & 0xFF);
        return result;
    }

    /**
     * 把一个整数转为3位的byte数组
     *
     * @param value 需要转换成3位byte数组的整数
     * @return 整数转换后的byte数组
     */
    public static byte[] integerTo3Bytes(int value) {
        byte[] result = new byte[3];
        result[0] = (byte) ((value >>> 16) & 0xFF);
        result[1] = (byte) ((value >>> 8) & 0xFF);
        result[2] = (byte) (value & 0xFF);
        return result;
    }

    /**
     * 把一个整数转为4位的byte数组
     *
     * @param value 需要转换成2位byte数组的整数
     * @return 整数转换后的byte数组
     */
    public static byte[] integerTo4Bytes(int value) {
        byte[] result = new byte[4];
        result[0] = (byte) ((value >>> 24) & 0xFF);
        result[1] = (byte) ((value >>> 16) & 0xFF);
        result[2] = (byte) ((value >>> 8) & 0xFF);
        result[3] = (byte) (value & 0xFF);

        return result;
    }

    /**
     * 把byte[]转化为整数
     *
     * @param value 需要转换为整数的byte数组
     * @return byte数组转换后的整数值
     */
    public static short bytesToShort(byte[] value) {
        short result = 0;
        int len = value.length;
        for (int i = 0; i < len; i++) {

            result |= (short) ((value[i] & 0xff) << 8 * (len - 1 - i));
        }

        return result;
    }

    /**
     * 把byte[]转化为整数
     *
     * @param value 需要转换为整数的byte数组
     * @return byte数组转换后的整数值
     */
    public static int bytesToInteger(byte[] value) {
        int result = 0;
        int len = value.length;
        for (int i = 0; i < len; i++) {

            result |= ((value[i] & 0xff) << 8 * (len - 1 - i));
        }

        return result;
    }

    /**
     * 将byte数组转换为float
     *
     * @param bs 需要转换为float的byte数组
     * @return
     */
    public static float bytes2Float(byte[] bs) {
        return Float.intBitsToFloat((((bs[3] & 0xFF) << 24) + ((bs[2] & 0xFF) << 16) + ((bs[1] & 0xFF) << 8) + (bs[0] & 0xFF)));
    }

    /**
     * byte
     *
     * @param bytes
     * @return
     */
    public static float byteBE2Float(byte[] bytes) {
        int l;
        l = bytes[0];
        l &= 0xff;
        l |= ((long) bytes[1] << 8);
        l &= 0xffff;
        l |= ((long) bytes[2] << 16);
        l &= 0xffffff;
        l |= ((long) bytes[3] << 24);
        return Float.intBitsToFloat(l);
    }

    /**
     * 把byte[]转化为整数
     *
     * @param value 需要转换为整数的byte数组
     * @return byte数组转换后的整数值
     */
    public static double bytesToDouble(byte[] value) {
        long result = 0;
        int len = value.length;
        for (int i = 0; i < len; i++) {
            result |= ((long) (value[i] & 0xff) << 8 * i);
        }

        return Double.longBitsToDouble(result);
    }

    /**
     * 把一个byte转化为整数
     *
     * @param value 需要转换为整数的byte
     * @return byte转换后的整数
     */
    private static int onebytesToInteger(byte value) {
        return (int) value & 0xFF;
    }

    /**
     * 把一个2位的byte数组转化为整数
     *
     * @param value 需要转换为整数的2位的byte数组
     * @return 2位的byte数组转换后的整数
     */
    private static int twoBytesToInteger(byte[] value) {
        int temp0 = value[0] & 0xFF;
        int temp1 = value[1] & 0xFF;

        return ((temp0 << 8) + temp1);
    }

    /**
     * 把一个3位的byte数组转化为整数
     *
     * @param value 需要转换为整数的3位的byte数组
     * @return 3位的byte数组转换后的整数
     */
    private static int threeBytesToInteger(byte[] value) {
        int temp0 = value[0] & 0xFF;
        int temp1 = value[1] & 0xFF;
        int temp2 = value[2] & 0xFF;

        return ((temp0 << 16) + (temp1 << 8) + temp2);
    }

    /**
     * 把一个4位的byte数组转化为整数
     *
     * @param value 需要转换为整数的4位的byte数组
     * @return 4位的byte数组转换后的整数
     */
    private static int fourBytesToInteger(byte[] value) {
        int temp0 = value[0] & 0xFF;
        int temp1 = value[1] & 0xFF;
        int temp2 = value[2] & 0xFF;
        int temp3 = value[3] & 0xFF;

        return ((temp0 << 24) + (temp1 << 16) + (temp2 << 8) + temp3);
    }

    /**
     * 把一个4位的byte数组转化为长整数
     *
     * @param value 需要转换为长整数的4位的byte数组
     * @return 4位的byte数组转换后的长整数
     */
    public static long fourBytesToLong(byte[] value) {
        int temp0 = value[0] & 0xFF;
        int temp1 = value[1] & 0xFF;
        int temp2 = value[2] & 0xFF;
        int temp3 = value[3] & 0xFF;

        return (((long) temp0 << 24) + (temp1 << 16) + (temp2 << 8) + temp3);
    }

    /**
     * 把一个数组转化长整数
     *
     * @param value 需要转换为长整数的的byte数组
     * @return byte数组转换后的长整数
     */
    public static long bytes2Long(byte[] value) {
        long result = 0;
        int len = value.length;
        int temp;
        for (int i = 0; i < len; i++) {
            temp = (len - 1 - i) * 8;
            if (temp == 0) {
                result += (value[i] & 0x0ff);
            } else {
                result += (value[i] & 0x0ff) << temp;
            }
        }

        return result;
    }

    /**
     * 把一个长整数改为byte数组
     *
     * @param value
     * @return
     * @throws Exception
     */
    public static byte[] longToBytes(long value) {
        return longToBytes(value, 8);
    }

    /**
     * 把一个长整数转换为byte数组
     *
     * @param value 需要转换为byte的长整数
     * @return 长整数转换后的byte数组
     */
    public static byte[] longToBytes(long value, int len) {
        byte[] result = new byte[len];
        int temp;
        for (int i = 0; i < len; i++) {
            temp = (len - 1 - i) * 8;
            if (temp == 0) {
                result[i] += (value & 0x0ff);
            } else {
                result[i] += (value >>> temp) & 0x0ff;
            }
        }

        return result;
    }

    /**
     * 得到一个消息ID
     */
    public static byte[] generateTransactionID() {
        byte[] id = new byte[16];
        System.arraycopy(integerTo2Bytes((int) (Math.random() * 65536)), 0, id, 0, 2);
        System.arraycopy(integerTo2Bytes((int) (Math.random() * 65536)), 0, id, 2, 2);
        System.arraycopy(integerTo2Bytes((int) (Math.random() * 65536)), 0, id, 4, 2);
        System.arraycopy(integerTo2Bytes((int) (Math.random() * 65536)), 0, id, 6, 2);
        System.arraycopy(integerTo2Bytes((int) (Math.random() * 65536)), 0, id, 8, 2);
        System.arraycopy(integerTo2Bytes((int) (Math.random() * 65536)), 0, id, 10, 2);
        System.arraycopy(integerTo2Bytes((int) (Math.random() * 65536)), 0, id, 12, 2);
        System.arraycopy(integerTo2Bytes((int) (Math.random() * 65536)), 0, id, 14, 2);

        return id;
    }

    /**
     * 把IP拆分位int数组
     *
     * @param ip 需要拆分为int数组的IP
     * @return IP拆分位int数组
     * @throws Exception 错误的IP地址异常
     */
    public static int[] getIntIPValue(String ip) throws Exception {
        String[] sip = ip.split("[.]");
        if (sip.length != 4) {
            throw new Exception("error IP Address");
        }
        int[] intIP = {Integer.parseInt(sip[0]), Integer.parseInt(sip[1]), Integer.parseInt(sip[2]), Integer.parseInt(sip[3])};

        return intIP;
    }

    /**
     * 把byte类型IP地址转化为字符串类型的IP
     *
     * @param address byte类型IP地址
     * @return 字符串类型的IP
     */
    public static String getStringIPValue(byte[] address) {
        int first = onebytesToInteger(address[0]);
        int second = onebytesToInteger(address[1]);
        int third = onebytesToInteger(address[2]);
        int fourth = onebytesToInteger(address[3]);

        return first + "." + second + "." + third + "." + fourth;
    }

    /**
     * 合并字节数组
     *
     * @param original   原始的byte数组
     * @param needMerges 任意个...需要被合并的byte数组
     * @return 合并后字节数组
     */
    public static byte[] concatAll(byte[] original, byte[]... needMerges) {
        // 合并后的byte数组长度
        int newLength = original.length;
        for (byte[] array : needMerges) {
            if (array != null) {
                newLength += array.length;
            }
        }

        // 将原始的byte数组拷贝到新的byte数据里面
        byte[] result = Arrays.copyOf(original, newLength);

        // 原始的字符数组的长度
        int offset = original.length;
        for (byte[] array : needMerges) {
            if (array != null) {
                System.arraycopy(array, 0, result, offset, array.length);
                offset += array.length;
            }
        }

        return result;
    }

    /**
     * 合并字节数组
     *
     * @param needMergeList 需要被合并的byte数组集合
     * @return 合并后字节数组
     */
    public static byte[] concatAll(List<byte[]> needMergeList) {
        // 合并后的byte数组长度
        int totalLength = 0;
        for (byte[] array : needMergeList) {
            if (array != null) {
                totalLength += array.length;
            }
        }
        // 创建一个合并后总长度的byte数组
        byte[] result = new byte[totalLength];
        int offset = 0;
        for (byte[] array : needMergeList) {
            if (array != null) {
                System.arraycopy(array, 0, result, offset, array.length);
                offset += array.length;
            }
        }
        return result;
    }

    public static int getBitRange(int number, int start, int end) {
        if (start < 0)
            throw new IndexOutOfBoundsException("min index is 0,but start = " + start);
        if (end >= Integer.SIZE)
            throw new IndexOutOfBoundsException("max index is " + (Integer.SIZE - 1) + ",but end = " + end);

        return (number << Integer.SIZE - (end + 1)) >>> Integer.SIZE - (end - start + 1);
    }

    public static int getBitAt(int number, int index) {
        if (index < 0)
            throw new IndexOutOfBoundsException("min index is 0,but " + index);
        if (index >= Integer.SIZE)
            throw new IndexOutOfBoundsException("max index is " + (Integer.SIZE - 1) + ",but " + index);

        return ((1 << index) & number) >> index;
    }

    public static int getBitAtS(int number, int index) {
        String s = Integer.toBinaryString(number);
        return Integer.parseInt(s.charAt(index) + "");
    }

    @Deprecated
    public static int getBitRangeS(int number, int start, int end) {
        String s = Integer.toBinaryString(number);
        StringBuilder sb = new StringBuilder(s);
        while (sb.length() < Integer.SIZE) {
            sb.insert(0, "0");
        }
        String tmp = sb.reverse().substring(start, end + 1);
        sb = new StringBuilder(tmp);
        return Integer.parseInt(sb.reverse().toString(), 2);
    }

    /**
     * 解析数据为int类型（解析失败后返回传入的默认值）
     *
     * @param src        将要解析的数据
     * @param startIndex 解析的起始位置
     * @param length     解析长度
     * @return 解析完成的数据或者解析失败返回defaultVal
     */
    public static short parseShortFromBytes(byte[] src, int startIndex, int length) {
        short i = 0;

        try {
            // 字节数大于4,从起始索引开始向后处理4个字节,其余超出部分丢弃
            final int len = length > 2 ? 2 : length;
            byte[] dest = new byte[len];
            System.arraycopy(src, startIndex, dest, 0, len);

            i = bytesToShort(dest);
        } catch (Exception e) {
            log.error("error", e);
        }

        return i;
    }

    /**
     * 解析数据为int类型（解析失败后返回传入的默认值）
     *
     * @param src        将要解析的数据
     * @param startIndex 解析的起始位置
     * @param length     解析长度
     * @return 解析完成的数据或者解析失败返回defaultVal
     */
    public static int parseIntFromBytes(byte[] src, int startIndex, int length) {
        return parseIntFromBytes(src, startIndex, length, 0);
    }

    /**
     * 解析数据为int类型（解析失败后返回传入的默认值）
     *
     * @param data       将要解析的数据
     * @param startIndex 解析的起始位置
     * @param length     解析长度
     * @return 解析完成的数据或者解析失败返回defaultVal
     */
    private static int parseIntFromBytes(byte[] data, int startIndex, int length, int defaultVal) {
        try {
            // 字节数大于4,从起始索引开始向后处理4个字节,其余超出部分丢弃
            final int len = length > 4 ? 4 : length;
            byte[] tmp = new byte[len];
            System.arraycopy(data, startIndex, tmp, 0, len);
            return bytesToInteger(tmp);
        } catch (Exception e) {
            log.error(BitOperator.bytesToHex(data), e);

            return defaultVal;
        }
    }

    /**
     * 解析数据为int类型（解析失败后返回传入的默认值）
     *
     * @param src        将要解析的数据
     * @param startIndex 解析的起始位置
     * @param length     解析长度
     * @return 解析完成的数据或者解析失败返回defaultVal
     */
    public static float parseFloatFromBytes(byte[] src, int startIndex, int length) {
        float i = 0;

        try {
            // 字节数大于4,从起始索引开始向后处理4个字节,其余超出部分丢弃
            final int len = length > 4 ? 4 : length;
            byte[] dest = new byte[len];
            System.arraycopy(src, startIndex, dest, 0, len);

            i = byteBE2Float(dest);
        } catch (Exception e) {
            log.error("error", e);
        }

        return i;
    }

    /**
     * 解析数据为int类型（解析失败后返回传入的默认值）
     *
     * @param src        将要解析的数据
     * @param startIndex 解析的起始位置
     * @param length     解析长度
     * @return 解析完成的数据或者解析失败返回defaultVal
     */
    public static double parseDoubleFromBytes(byte[] src, int startIndex, int length) {
        double i = 0;

        try {
            // 字节数大于4,从起始索引开始向后处理4个字节,其余超出部分丢弃
            final int len = length > 8 ? 8 : length;
            byte[] dest = new byte[len];
            System.arraycopy(src, startIndex, dest, 0, len);

            i = bytesToDouble(dest);
        } catch (Exception e) {
            log.error("error", e);
        }

        return i;
    }

    /**
     * 解析数据为int类型（解析失败后返回传入的默认值）
     *
     * @param src        将要解析的数据
     * @param startIndex 解析的起始位置
     * @param length     解析长度
     * @return 解析完成的数据或者解析失败返回defaultVal
     */
    public static String parseCharsFromBytes(byte[] src, int startIndex, int length) {
        String result = "";

        try {
            for (int i = startIndex; i <= length; i++) {
                char tempChar = (char) src[i];
                result += tempChar;
            }
        } catch (Exception e) {
            log.error("error", e);
        }

        return result;
    }

    /**
     * 字节数组转16进制
     *
     * @param bytes 需要转换的byte数组
     * @return 转换后的Hex字符串
     */
    public static String bytesToHex(byte[] bytes) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < bytes.length; i++) {
            String hex = Integer.toHexString(bytes[i] & 0xFF);
            if (hex.length() < 2) {
                hex = "0"+hex;
            }

            if(i==0) {
                sb.append(hex);
            } else {
                sb.append(" ").append(hex);
            }
        }
        return sb.toString().toUpperCase();
    }

    /**
     * 字节数组转16进制
     *
     * @param number 需要转换的byte
     * @return 转换后的Hex字符串
     */
    public static String byteToHex(int number) {
        StringBuffer sb = new StringBuffer();
        String hex = Integer.toHexString(number & 0xFF);
        if (hex.length() < 2) {
            sb.append(0);
        }
        sb.append(hex);
        return sb.toString().toUpperCase();
    }

    public static String parseStringFromBytes(byte[] data, int startIndex, int lenth) {
        return parseStringFromBytes(data, startIndex, lenth, null);
    }

    public static String parseStringFromBytes(byte[] data, int startIndex, int lenth, String defaultVal) {
        try {
            byte[] tmp = new byte[lenth];
            System.arraycopy(data, startIndex, tmp, 0, lenth);
            return new String(tmp, "UTF-8");
        } catch (Exception e) {
            log.error("解析字符串出错", e);
            return defaultVal;
        }
    }

    public static String parseHexStringFromBytes(byte[] data, int startIndex, int lenth, String defaultVal) {
        try {
            byte[] tmp = new byte[lenth];
            System.arraycopy(data, startIndex, tmp, 0, lenth);
            return BitOperator.bytesToHex(tmp).toUpperCase();
        } catch (Exception e) {
            log.error("解析字符串出错", e);
            return defaultVal;
        }
    }

    public static String parseBcdStringFromBytes(byte[] data, int startIndex, int lenth) {
        return parseBcdStringFromBytes(data, startIndex, lenth, null);
    }

    public static String parseBcdStringFromBytes(byte[] data, int startIndex, int lenth, String defaultVal) {
        try {
            byte[] tmp = new byte[lenth];
            System.arraycopy(data, startIndex, tmp, 0, lenth);
            return BCD8421Operater.bcd2String(tmp);
        } catch (Exception e) {
            log.error("解析BCD(8421码)出错", e);
            return defaultVal;
        }
    }

    /**
     * 获取高四位
     *
     * @param data
     * @return
     */
    public static int getHeight4(byte data) {//
        int height;
        height = ((data & 0xf0) >> 4);
        return height;
    }

    /**
     * 获取低四位
     *
     * @param data
     * @return
     */
    public static int getLow4(byte data) {
        int low;
        low = (data & 0x0f);
        return low;
    }
}
