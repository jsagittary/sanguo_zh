//package com.gryphpoem.game.zw.core.loader;
//
//import java.io.FileInputStream;
//import java.io.FileNotFoundException;
//import java.io.IOException;
//import java.text.ParseException;
//import java.text.SimpleDateFormat;
//import java.util.Date;
//import java.util.GregorianCalendar;
//
//import org.apache.log4j.Logger;
//
//import com.thoughtworks.xstream.XStream;
//import com.thoughtworks.xstream.converters.ConversionException;
//import com.thoughtworks.xstream.converters.Converter;
//import com.thoughtworks.xstream.converters.MarshallingContext;
//import com.thoughtworks.xstream.converters.UnmarshallingContext;
//import com.thoughtworks.xstream.io.HierarchicalStreamReader;
//import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
//import com.thoughtworks.xstream.io.xml.DomDriver;
//
//class DateConverter implements Converter {
//
//    @SuppressWarnings("rawtypes")
//    public boolean canConvert(Class arg0) {
//
//        return Date.class == arg0;
//    }
//
//    public void marshal(Object arg0, HierarchicalStreamWriter arg1, MarshallingContext arg2) {
//
//    }
//
//    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext arg1) {
//        GregorianCalendar calendar = new GregorianCalendar();
//        SimpleDateFormat dateFm = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); // 格式化当前系统日期
//        try {
//            calendar.setTime(dateFm.parse(reader.getValue()));
//        } catch (ParseException e) {
//            throw new ConversionException(e.getMessage(), e);
//        }
//        return calendar.getTime();
//
//    }
//
//}
//
//public class XmlLoader implements Loader {
//    private static final Logger logger = Logger.getLogger(XmlLoader.class);
//
//    protected XStream xs = new XStream(new DomDriver());
//    protected Formatter formatter;
//
//    public XmlLoader(Formatter formatter) {
//        xs.registerConverter(new DateConverter());
//        this.formatter = formatter;
//    }
//
//    public Object load(String path) {
//        FileInputStream is = null;
//        try {
//            is = new FileInputStream(path);
//            formatter.format(xs);
//            return xs.fromXML(is);
//        } catch (FileNotFoundException e) {
//            logger.error(e, e);
//            return null;
//        } finally {
//            if (is != null) {
//                try {
//                    is.close();
//                } catch (IOException e) {
//                    logger.error(e, e);
//                }
//            }
//        }
//    }
//}
