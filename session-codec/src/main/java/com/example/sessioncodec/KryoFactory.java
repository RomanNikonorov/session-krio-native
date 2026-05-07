package com.example.sessioncodec;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.util.DefaultInstantiatorStrategy;
import org.objenesis.strategy.StdInstantiatorStrategy;

public final class KryoFactory {

    public Kryo create() {
        Kryo kryo = new Kryo();
        kryo.setRegistrationRequired(true);
        kryo.setReferences(true);
        kryo.setInstantiatorStrategy(new DefaultInstantiatorStrategy(new StdInstantiatorStrategy()));
        SessionKryoRegistry.register(kryo);
        return kryo;
    }
}
