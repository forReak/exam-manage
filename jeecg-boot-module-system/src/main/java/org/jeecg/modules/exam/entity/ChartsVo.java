package org.jeecg.modules.exam.entity;

/**
 * @author furao
 * @desc
 * @date 2021/4/28
 * @package org.jeecg.modules.exam.entity
 */
public class ChartsVo {
    private Long studentNums;

    private Long examTime;

    private Long passTime;

    private String passPercent;

    private Long monthStudentNums;

    private Long monthExamTime;

    private Long monthPassTime;

    private String monthPassPercent;

    public Long getStudentNums() {
        return studentNums;
    }

    public void setStudentNums(Long studentNums) {
        this.studentNums = studentNums;
    }

    public Long getExamTime() {
        return examTime;
    }

    public void setExamTime(Long examTime) {
        this.examTime = examTime;
    }

    public Long getPassTime() {
        return passTime;
    }

    public void setPassTime(Long passTime) {
        this.passTime = passTime;
    }

    public String getPassPercent() {
        return passPercent;
    }

    public void setPassPercent(String passPercent) {
        this.passPercent = passPercent;
    }

    public Long getMonthStudentNums() {
        return monthStudentNums;
    }

    public void setMonthStudentNums(Long monthStudentNums) {
        this.monthStudentNums = monthStudentNums;
    }

    public Long getMonthExamTime() {
        return monthExamTime;
    }

    public void setMonthExamTime(Long monthExamTime) {
        this.monthExamTime = monthExamTime;
    }

    public Long getMonthPassTime() {
        return monthPassTime;
    }

    public void setMonthPassTime(Long monthPassTime) {
        this.monthPassTime = monthPassTime;
    }

    public String getMonthPassPercent() {
        return monthPassPercent;
    }

    public void setMonthPassPercent(String monthPassPercent) {
        this.monthPassPercent = monthPassPercent;
    }
}
