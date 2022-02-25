package com.gryphpoem.game.zw.resource.util;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.CRC32;

import org.apache.ibatis.reflection.DefaultReflectorFactory;
import org.apache.ibatis.reflection.MetaClass;
import org.apache.ibatis.reflection.ReflectorFactory;
import org.apache.ibatis.reflection.invoker.Invoker;

/**
 * @ClassName DbDataCheckSum.java
 * @Description 数据CRC监测工具
 * @author QiuKun
 * @date 2019年4月17日
 */
public class DbDataCheckSum {
    private static final ReflectorFactory reflectorFactory = new DefaultReflectorFactory();
    // <id,CRCData>
    Map<Long, CRCData> crcDataCache = new ConcurrentHashMap<>();
    private Class<?> type;
    private List<String> requisiteColumn;
    private List<String> byteColumn;
    /** 主键名 */
    private String idColumn;
    private MetaClass metaClass;

    public DbDataCheckSum(Class<?> type, String idColumn) {
        this.type = type;
        metaClass = MetaClass.forClass(type, reflectorFactory);
        this.idColumn = idColumn;
        init();
    }

    private void init() {
        List<String> tmpByteColumn = new ArrayList<>();
        List<String> tmpRequisiteColumn = new ArrayList<>();
        String[] getterNames = metaClass.getGetterNames();
        for (String n : getterNames) {
            if (!metaClass.hasGetter(n) || !metaClass.hasSetter(n)) {
                continue;
            }
            Class<?> type = metaClass.getGetterType(n);
            if (type.isAssignableFrom(byte[].class)) {
                tmpByteColumn.add(n);
            } else {
                tmpRequisiteColumn.add(n);
            }
        }
        this.requisiteColumn = (List<String>) Collections.unmodifiableList(tmpRequisiteColumn);
        this.byteColumn = (List<String>) Collections.unmodifiableList(tmpByteColumn);
    }

    static class CRCData {
        private final CRC32 crc;
        // <column,CRCCode>
        private Map<String, Long> columnCrcData;

        public CRCData() {
            this.crc = new CRC32();
            this.columnCrcData = new HashMap<>();
        }

        public boolean isSameCRCCode(String column, byte[] data) {
            if (data == null) {
                return true;
            }
            crc.reset();
            crc.update(data);
            long nCRCCode = crc.getValue();
            Long oCRCCode = columnCrcData.get(column);
            boolean isSame = false;
            if (oCRCCode != null && nCRCCode == oCRCCode.longValue()) {
                isSame = true;
            }
            columnCrcData.put(column, nCRCCode);
            return isSame;
        }
    }

    private Object getValue(Object data, String column) {
        Invoker getInvoker = metaClass.getGetInvoker(column);
        try {
            return getInvoker.invoke(data, null);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Map<String, Object> saveParam(Object data) {
        if (data.getClass().isAssignableFrom(type)) {
            Map<String, Object> params = new HashMap<>();
            Object idVal = getValue(data, idColumn);
            Long idV = null;
            if (idVal instanceof Long) {
                idV = (Long) idVal;
            } else if (idVal instanceof Integer) {
                Integer tmpIdVal = (Integer) idVal;
                idV = Long.valueOf(tmpIdVal.longValue());
            }
            if (idV == null) {
                return null;
            }
            CRCData mCRCData = crcDataCache.computeIfAbsent(idV, i -> new CRCData());
            // 必存字段
            for (String column : requisiteColumn) {
                Object value = getValue(data, column);
                if (value != null) {
                    params.put(column, value);
                }
            }
            // 需要检查的字段
            for (String column : byteColumn) {
                byte[] byteData = (byte[]) getValue(data, column);
                if (byteData != null && !mCRCData.isSameCRCCode(column, byteData)) {
                    params.put(column, byteData);
                }
            }
            return params;
        }
        return null;
    }

    public static void main(String[] args) {

        // DbDataCheckSum checkSum = new DbDataCheckSum(MailData.class, "lordId");
        //
        // MailData dataNew = new MailData();
        // dataNew.setLordId(132456789);
        // dataNew.setMails(new byte[] { 1, 2, 3, 4, 5 });
        // dataNew.setReports(new byte[] { 1, 2, 3, 4, 5 });
        // Map<String, Object> saveParam = checkSum.saveParam(dataNew);
        // System.out.println(saveParam);
        // Invoker getInvoker = metaClass.getGetInvoker("lordId");
        // try {
        // Object idVal = getInvoker.invoke(dataNew, null);
        // Long idV = null;
        // if (idVal instanceof Long) {
        // idV = (Long) idVal;
        // } else if (idVal instanceof Integer) {
        // Integer tmpIdVal = (Integer) idVal;
        // idV = Long.valueOf(tmpIdVal.longValue());
        // }
        // System.out.println(idV);
        // } catch (IllegalAccessException | InvocationTargetException e) {
        // e.printStackTrace();
        // }

        // String[] getterNames = metaClass.getGetterNames();
        // for (String n : getterNames) {
        //
        // // String name = byte[].class.getName();
        // Class<?> type = metaClass.getGetterType(n);
        // if (type.isAssignableFrom(byte[].class)) {
        // System.out.println("byte[]: " + n);
        // } else {
        // System.out.println("other: " + n);
        //
        // }
        // }

    }
}
