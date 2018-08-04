package com.frostphyr.notiphy;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public interface EntryIO {

    Entry read(DataInputStream in) throws IOException;

    void write(DataOutputStream out, Entry entry) throws IOException;

}
