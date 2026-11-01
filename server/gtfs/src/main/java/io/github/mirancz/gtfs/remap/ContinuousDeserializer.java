package io.github.mirancz.gtfs.remap;

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.BeanProperty;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;

import java.io.IOException;

public class ContinuousDeserializer extends ValueDeserializer<Object> {

    private final IdType type;

    public ContinuousDeserializer() {
        this(null);
    }



    private ContinuousDeserializer(IdType type) {
        this.type = type;
    }

    @Override
    public Object deserialize(JsonParser p, DeserializationContext ctx) throws JacksonException {
        String text = p.getString().trim();

        return Mapper.map(type, text);
    }

    @Override
    public ValueDeserializer<?> createContextual(DeserializationContext ctx, BeanProperty prop) {
        Continuous ann = (prop == null) ? null : prop.getAnnotation(Continuous.class);

        if (ann == null) {
            throw new IllegalStateException("@Continuous is missing on the property being deserialized");
        }

        return new ContinuousDeserializer(ann.value());
    }
}
