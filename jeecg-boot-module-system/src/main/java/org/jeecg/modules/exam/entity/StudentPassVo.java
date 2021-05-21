package org.jeecg.modules.exam.entity;

/**
 * @author furao
 * @desc
 * @date 2021/4/28
 * @package org.jeecg.modules.exam.entity
 */
public class StudentPassVo extends BizSignList{
    private String signId;
    private Integer score;

    public String getSignId() {
        return signId;
    }

    public void setSignId(String signId) {
        this.signId = signId;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }
}
