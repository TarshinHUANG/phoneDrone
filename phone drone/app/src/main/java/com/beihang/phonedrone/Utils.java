package com.beihang.phonedrone;

public class Utils {
	//字符串处理，方便显示传感器值
	public static void addLineToSB(StringBuffer sb, String name, Object value) {
        if (sb == null) return;
        sb.
        append((name == null || "".equals(name)) ? "" : name + ": ").
        append(value == null ? "" : value + "").
        append("\n");
    }
	
}
