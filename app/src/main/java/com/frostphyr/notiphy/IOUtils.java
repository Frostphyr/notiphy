package com.frostphyr.notiphy;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public final class IOUtils {

    public static String readString(DataInputStream in) throws IOException {
        char[] chars = new char[in.readShort()];
        for (int i = 0; i < chars.length; i++) {
            chars[i] = in.readChar();
        }
        return new String(chars);
    }

    public static void writeString(DataOutputStream out, String s) throws IOException {
        out.writeShort(s.length());
        out.writeChars(s);
    }

}
