package com.chapter3decode;

public class EncodeDemo {

	//查看编码结果
	public static void main(String[] args) throws Exception {
		String name = "I am 君山";
		System.out.println(name.toCharArray());
		System.out.println(bytesToHexString(name.getBytes("ISO-8859-1")));
		System.out.println(bytesToHexString(name.getBytes("GB2312")));
		System.out.println(bytesToHexString(name.getBytes("GBK")));
		System.out.println(bytesToHexString(name.getBytes("UTF-16")));
		System.out.println(bytesToHexString(name.getBytes("UTF-8")));
	}

	public static String bytesToHexString(byte[] src){
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
        }
        return stringBuilder.toString();
    }

}
