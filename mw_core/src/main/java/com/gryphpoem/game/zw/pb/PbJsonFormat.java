package com.gryphpoem.game.zw.pb;

import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.google.protobuf.Message;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.googlecode.protobuf.format.JsonFormat;

/**
 * @ClassName PbJsonFormat.java
 * @Description
 * @author QiuKun
 * @date 2018年6月1日
 */
public class PbJsonFormat extends JsonFormat {
    
  
    public static String printToString(Message message, String... retainField) {
        try {
            StringBuilder text = new StringBuilder();
            print(message, text, retainField);
            return text.toString();
        } catch (IOException e) {
            throw new RuntimeException("Writing to a StringBuilder threw an IOException (should never happen).", e);
        }
    }

    public static void print(Message message, Appendable output, String... retainField) throws IOException {
        JsonGenerator generator = new JsonGenerator(output);
        generator.print("{");
        print(message, generator, retainField);
        generator.print("}");
    }

    protected static void print(Message message, JsonGenerator generator, String... retainField) throws IOException {
        List<String> fieldList = Arrays.asList(retainField);
        int size = fieldList.size();
        for (Iterator<Map.Entry<FieldDescriptor, Object>> iter = message.getAllFields().entrySet().iterator(); iter
                .hasNext();) {
            Map.Entry<FieldDescriptor, Object> field = iter.next();
            if (!fieldList.contains(field.getKey().getName())) continue;
            printField(field.getKey(), field.getValue(), generator);
            if (iter.hasNext() && --size > 0) {
                generator.print(",");
            }
            if (size == 0) break;
        }

        if (message.getUnknownFields().asMap().size() > 0) generator.print(", ");
        printUnknownFields(message.getUnknownFields(), generator);
    }
}
