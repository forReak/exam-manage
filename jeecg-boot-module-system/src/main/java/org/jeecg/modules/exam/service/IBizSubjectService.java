package org.jeecg.modules.exam.service;

import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.exam.entity.BizSubject;
import com.baomidou.mybatisplus.extension.service.IService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Set;

/**
 * @Description: biz_subject
 * @Author: jeecg-boot
 * @Date:   2021-02-10
 * @Version: V1.0
 */
public interface IBizSubjectService extends IService<BizSubject> {

    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response, Class<BizSubject> bizSubjectClass) throws Exception;

    void checkSubjectNumber(Set<String> workTypeList);
}
