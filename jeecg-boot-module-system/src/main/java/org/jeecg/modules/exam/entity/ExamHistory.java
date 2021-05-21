package org.jeecg.modules.exam.entity;

import org.jeecg.common.aspect.annotation.Dict;

/**
 * @author furao
 * @desc
 * @date 2021/3/13
 * @package org.jeecg.modules.exam.entity
 */
public class ExamHistory extends BizExamList{

    private String userName;
    private String sex;
    private String idCard;
    @Dict(dicCode = "id",dicText = "work_type_name",dictTable = "biz_work_type")
    private String workType;
    private String result;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getIdCard() {
        return idCard;
    }

    public void setIdCard(String idCard) {
        this.idCard = idCard;
    }

    public String getWorkType() {
        return workType;
    }

    public void setWorkType(String workType) {
        this.workType = workType;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }
}
