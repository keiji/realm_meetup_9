package io.keiji.realmsample2.realm.util;

import android.text.TextUtils;

import net.vvakame.util.jsonpullparser.JsonFormatException;
import net.vvakame.util.jsonpullparser.JsonPullParser;
import net.vvakame.util.jsonpullparser.util.OnJsonObjectAddListener;
import net.vvakame.util.jsonpullparser.util.TokenConverter;

import java.io.IOException;

public class AgeTokenConverter extends TokenConverter<Integer> {

    static AgeTokenConverter converter = null;

    public static AgeTokenConverter getInstance() {
        if (converter == null) converter = new AgeTokenConverter();

        return converter;
    }

    @Override
    public Integer parse(JsonPullParser parser, OnJsonObjectAddListener listener) throws IOException,
            JsonFormatException {
        Integer value = null;
        if (parser.getEventType() == JsonPullParser.State.VALUE_STRING) {
            String str = parser.getValueString();
            try {
                value = !TextUtils.isEmpty(str) ? Integer.parseInt(str) : null;
            } catch (NumberFormatException e) {
            }
        }

        return value;
    }

}
