package org.jeecg.modules.exam.entity;

import java.util.List;

/**
 * @author furao
 * @desc
 * @date 2021/4/5
 * @package org.jeecg.modules.exam.entity
 */
public class BatchAddVo {
    List<String> examDayList;

    public List<String> getExamDayList() {
        return examDayList;
    }

    public void setExamDayList(List<String> examDayList) {
        this.examDayList = examDayList;
    }
}
