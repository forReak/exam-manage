package org.jeecg.modules.exam.service;

import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.exam.entity.BizSignList;
import com.baomidou.mybatisplus.extension.service.IService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @Description: 报名信息
 * @Author: jeecg-boot
 * @Date:   2021-02-15
 * @Version: V1.0
 */
public interface IBizSignListService extends IService<BizSignList> {

    Result<?> importExcel(String workTypeId, HttpServletRequest request, HttpServletResponse response, Class<BizSignList> bizSignListClass) throws Exception;

    List<BizSignList> checkSignDup(Date examTime, String idCard, String workTypeId1);

    void checkSignInfo(BizSignList object, Map<String, String> workTypeIdMap, List<Date> allExamDay);

    /**
     * 考试设置需要
     * 判断是否有没有考试且没过期的报名信息
     * 如果有，返回true
     * @param workTypeId 工种
     * @return
     */
    boolean checkHasNoExamAndNotPass(String workTypeId);

    Long getDayPassTime(String today,String tomorrow,String userName);
}
