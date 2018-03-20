package com.lezhin.panther.util;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;

/**
 * DateSerializer for json. Long to String.
 * @author seoeun
 * @since 2018.03.15
 */
public class DateSerializer extends StdSerializer<Long> {

    public DateSerializer() {
        this(Long.class);
    }

    public DateSerializer(Class<Long> t) {
        super(t);
    }

    @Override
    public void serialize(Long value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeString(DateUtil.format(value.longValue(), DateUtil.ASIA_SEOUL_ZONE, "yyyy-MM-dd HH:mm:ss"));
    }

}


