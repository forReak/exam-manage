package org.jeecg.modules.exam.entity;

import java.util.List;

/**
 * @author furao
 * @desc
 * @date 2021/3/21
 * @package org.jeecg.modules.exam.entity
 */
public class GetSubjectParam {
    private List<String> ids;

    public List<String> getIds() {
        return ids;
    }

    public void setIds(List<String> ids) {
        this.ids = ids;
    }
}
