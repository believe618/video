package com.googlecode.mp4parser.boxes.piff;


import com.coremedia.iso.Hex2;

import java.lang.Class;
import java.lang.IllegalAccessException;
import java.lang.InstantiationException;
import java.lang.Object;
import java.lang.Override;
import java.lang.RuntimeException;
import java.lang.String;
import java.lang.StringBuilder;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


public class ProtectionSpecificHeader2 {
    protected static Map<UUID, Class<? extends ProtectionSpecificHeader2>> uuidRegistry = new HashMap<UUID, Class<? extends ProtectionSpecificHeader2>>();
    ByteBuffer data;

    static {
        uuidRegistry.put(UUID.fromString("9A04F079-9840-4286-AB92-E65BE0885F95"), PlayReadyHeader22.class);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ProtectionSpecificHeader2) {
            if (this.getClass().equals(obj.getClass())) {
                return data.equals(((ProtectionSpecificHeader2) obj).data);
            }
        }
        return false;
    }

    public static ProtectionSpecificHeader2 createFor(UUID systemId, ByteBuffer bufferWrapper) {
        final Class<? extends ProtectionSpecificHeader2> aClass = uuidRegistry.get(systemId);

        ProtectionSpecificHeader2 protectionSpecificHeader2 = new ProtectionSpecificHeader2();
        if (aClass != null) {
            try {
                protectionSpecificHeader2 = aClass.newInstance();

            } catch (InstantiationException e) {
                throw new RuntimeException(e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        protectionSpecificHeader2.parse(bufferWrapper);
        return protectionSpecificHeader2;

    }

    public void parse(ByteBuffer buffer) {
        data = buffer;

    }

    public ByteBuffer getData() {
        return data;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("ProtectionSpecificHeader");
        sb.append("{data=");
        ByteBuffer data = getData().duplicate();
        data.rewind();
        byte[] bytes = new byte[data.limit()];
        data.get(bytes);
        sb.append(Hex2.encodeHex(bytes));
        sb.append('}');
        return sb.toString();
    }
}
