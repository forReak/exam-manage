package org.jeecg.modules.exam.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.jeecg.modules.exam.entity.BizSignList;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.jeecg.modules.exam.entity.StudentPassVo;

/**
 * @Description: 报名信息
 * @Author: jeecg-boot
 * @Date:   2021-02-15
 * @Version: V1.0
 */
public interface BizSignListMapper extends BaseMapper<BizSignList> {

    List<StudentPassVo> getDayPassNumber(String s1, String s2,String userName);
}
