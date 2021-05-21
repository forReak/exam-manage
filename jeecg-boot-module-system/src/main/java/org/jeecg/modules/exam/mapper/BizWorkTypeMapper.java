package org.jeecg.modules.exam.mapper;

import org.apache.ibatis.annotations.Param;
import org.jeecg.modules.exam.entity.BizWorkType;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * @Description: biz_work_type
 * @Author: jeecg-boot
 * @Date:   2021-02-10
 * @Version: V1.0
 */
public interface BizWorkTypeMapper extends BaseMapper<BizWorkType> {

	/**
	 * 编辑节点状态
	 * @param id
	 * @param status
	 */
	void updateTreeNodeStatus(@Param("id") String id,@Param("status") String status);

}
