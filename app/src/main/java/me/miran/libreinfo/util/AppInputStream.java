package me.miran.libreinfo.util;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class AppInputStream extends DataInputStream {

    public AppInputStream(InputStream is) {
        super(is);
    }

    public String readString() throws IOException {
        int strLen = readInt();

        byte[] result = IOUtil.readNBytes(this, strLen);

        return new String(result, StandardCharsets.UTF_8);
    }

}
