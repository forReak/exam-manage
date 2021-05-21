package org.jeecg.modules.exam.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author furao
 * @desc
 * @date 2021/4/11
 * @package org.jeecg.modules.exam.utils
 */
public class CommonUtils {

    public static Date getZeroDate() {
        Date date = new Date();
        SimpleDateFormat sdfzoro = new SimpleDateFormat("yyyy-MM-dd 00:00:00");
        String format1 = sdfzoro.format(date);
        Date parse = null;
        try {
            parse = sdfzoro.parse(format1);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return parse;
    }
}
