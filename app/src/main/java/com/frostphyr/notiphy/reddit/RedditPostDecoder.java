package com.frostphyr.notiphy.reddit;

import com.frostphyr.notiphy.EntryType;
import com.frostphyr.notiphy.Media;
import com.frostphyr.notiphy.MediaType;
import com.frostphyr.notiphy.Message;
import com.frostphyr.notiphy.TextUtils;
import com.frostphyr.notiphy.io.JSONDecoder;

import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

public class RedditPostDecoder implements JSONDecoder<Message> {

    private Parser parser = Parser.builder().build();
    private HtmlRenderer renderer = HtmlRenderer.builder().build();

    @Override
    public Message decode(JSONObject obj) throws JSONException {
        Message.Builder builder = new Message.Builder()
                .setType(EntryType.REDDIT)
                .setCreatedAt(new Date(Long.parseLong(obj.getString("createdAt"))))
                .setTitle(obj.getString("title"))
                .setDescription("r/" + obj.getString("subreddit") + " \u00B7 u/" + obj.getString("user"))
                .setUrl(obj.getString("url"))
                .setNsfw(obj.getBoolean("nsfw"));

        if (obj.has("text")) {
            builder.setText(TextUtils.fromHtml(renderer.render(parser.parse(obj.getString("text")))));
        }
        if (obj.has("link")) {
            builder.setMedia(new Media[] {
                    new Media(
                            obj.getBoolean("video") ? MediaType.VIDEO : MediaType.IMAGE,
                            obj.getString("link"),
                            obj.getString("thumbnailUrl"))
            });
        }

        return builder.build();
    }

}
