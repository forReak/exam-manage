package org.jeecg.modules.exam.service;

import org.jeecg.modules.exam.entity.BizWorkType;
import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.common.exception.JeecgBootException;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @Description: biz_work_type
 * @Author: jeecg-boot
 * @Date:   2021-02-10
 * @Version: V1.0
 */
public interface IBizWorkTypeService extends IService<BizWorkType> {

	/**根节点父ID的值*/
	public static final String ROOT_PID_VALUE = "0";

	/**树节点有子节点状态值*/
	public static final String HASCHILD = "1";

	/**树节点无子节点状态值*/
	public static final String NOCHILD = "0";

	/**新增节点*/
	void addBizWorkType(BizWorkType bizWorkType);

	/**修改节点*/
	void updateBizWorkType(BizWorkType bizWorkType) throws JeecgBootException;

	/**删除节点*/
	void deleteBizWorkType(String id) throws JeecgBootException;

	/**查询所有数据，无分页*/
    List<BizWorkType> queryTreeListNoPage(QueryWrapper<BizWorkType> queryWrapper);

	//获取当前系统内所有工种
	Map<String,String> getAllSystemWorkType(String getMap, String getMap2);

	Set<String> getAllSystemWorkType(String getSet);
}
