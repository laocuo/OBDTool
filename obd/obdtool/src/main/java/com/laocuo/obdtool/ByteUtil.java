package com.laocuo.obdtool;

public class ByteUtil {

    public static String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder("");
        if (src == null || src.length <= 0) {
            return null;
        }
        for (int i = 0; i < src.length; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
            stringBuilder.append(" ");
        }
        return stringBuilder.toString();
    }

    /**
     * 将16进制字符串转换为byte[]
     * "66 45 23 12"
     *
     * @param str
     * @return
     */
    public static byte[] hexStrtoBytes(String str) {
        if (str == null || str.trim().equals("")) {
            return new byte[0];
        }
        String[] dataArray = str.split(" ");
        byte[] bytes = new byte[dataArray.length];

        for (int i = 0; i < dataArray.length; i++) {
            String subStr = dataArray[i];
            bytes[i] = (byte) Integer.parseInt(subStr, 16);
        }
        return bytes;
    }
}
