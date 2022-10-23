package com.frostphyr.notiphy;

import com.frostphyr.notiphy.notification.Message;

import java.util.Map;

public interface MessageDecoder {

    Message decode(Map<String, String> data);

}
