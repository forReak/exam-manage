package org.jeecg.modules.exam.service.impl;

import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.exam.entity.BizWorkType;
import org.jeecg.modules.exam.mapper.BizWorkTypeMapper;
import org.jeecg.modules.exam.service.IBizWorkTypeService;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

/**
 * @Description: biz_work_type
 * @Author: jeecg-boot
 * @Date:   2021-02-10
 * @Version: V1.0
 */
@Service
public class BizWorkTypeServiceImpl extends ServiceImpl<BizWorkTypeMapper, BizWorkType> implements IBizWorkTypeService {

	@Override
	public void addBizWorkType(BizWorkType bizWorkType) {
        String parentId = bizWorkType.getParentId();
        if(oConvertUtils.isEmpty(parentId)){
			bizWorkType.setParentId(IBizWorkTypeService.ROOT_PID_VALUE);
            bizWorkType.setAll_name(bizWorkType.getWorkTypeName());
		}else{
			//如果当前节点父ID不为空 则设置父节点的hasChildren 为1
			BizWorkType parent = baseMapper.selectById(parentId);
			if(parent!=null && !"1".equals(parent.getHasChild())){
				parent.setHasChild("1");
				baseMapper.updateById(parent);
			}
			//设置全称
            if (parent != null) {
                String parentName = parent.getWorkTypeName();
                String grandfatherId = parent.getParentId();
                if("0".equals(grandfatherId)){
                    bizWorkType.setAll_name(parentName+"/"+bizWorkType.getWorkTypeName());
                }else{
                    //获取祖父节点
                    BizWorkType grandFather = baseMapper.selectById(grandfatherId);
                    String grandFatherName = grandFather.getWorkTypeName();
                    bizWorkType.setAll_name(grandFatherName+"/"+parentName+"/"+bizWorkType.getWorkTypeName());
                }
            }
        }

		baseMapper.insert(bizWorkType);
	}

	@Override
	public void updateBizWorkType(BizWorkType bizWorkType) {
		BizWorkType entity = this.getById(bizWorkType.getId());
		if(entity==null) {
			throw new JeecgBootException("未找到对应实体");
		}
		String old_pid = entity.getParentId();
		String new_pid = bizWorkType.getParentId();
		if(!old_pid.equals(new_pid)) {
			updateOldParentNode(old_pid);
			if(oConvertUtils.isEmpty(new_pid)){
				bizWorkType.setParentId(IBizWorkTypeService.ROOT_PID_VALUE);
			}
			if(!IBizWorkTypeService.ROOT_PID_VALUE.equals(bizWorkType.getParentId())) {
				baseMapper.updateTreeNodeStatus(bizWorkType.getParentId(), IBizWorkTypeService.HASCHILD);
			}
		}
		baseMapper.updateById(bizWorkType);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void deleteBizWorkType(String id) throws JeecgBootException {
		//查询选中节点下所有子节点一并删除
        id = this.queryTreeChildIds(id);
        if(id.indexOf(",")>0) {
            StringBuffer sb = new StringBuffer();
            String[] idArr = id.split(",");
            for (String idVal : idArr) {
                if(idVal != null){
                    BizWorkType bizWorkType = this.getById(idVal);
                    String pidVal = bizWorkType.getParentId();
                    //查询此节点上一级是否还有其他子节点
                    List<BizWorkType> dataList = baseMapper.selectList(new QueryWrapper<BizWorkType>().eq("parent_id", pidVal).notIn("id",Arrays.asList(idArr)));
                    if((dataList == null || dataList.size()==0) && !Arrays.asList(idArr).contains(pidVal)
                            && !sb.toString().contains(pidVal)){
                        //如果当前节点原本有子节点 现在木有了，更新状态
                        sb.append(pidVal).append(",");
                    }
                }
            }
            //批量删除节点
            baseMapper.deleteBatchIds(Arrays.asList(idArr));
            //修改已无子节点的标识
            String[] pidArr = sb.toString().split(",");
            for(String pid : pidArr){
                this.updateOldParentNode(pid);
            }
        }else{
            BizWorkType bizWorkType = this.getById(id);
            if(bizWorkType==null) {
                throw new JeecgBootException("未找到对应实体");
            }
            updateOldParentNode(bizWorkType.getParentId());
            baseMapper.deleteById(id);
        }
	}

	@Override
    public List<BizWorkType> queryTreeListNoPage(QueryWrapper<BizWorkType> queryWrapper) {
        List<BizWorkType> dataList = baseMapper.selectList(queryWrapper);
        List<BizWorkType> mapList = new ArrayList<>();
        for(BizWorkType data : dataList){
            String pidVal = data.getParentId();
            //递归查询子节点的根节点
            if(pidVal != null && !"0".equals(pidVal)){
                BizWorkType rootVal = this.getTreeRoot(pidVal);
                if(rootVal != null && !mapList.contains(rootVal)){
                    mapList.add(rootVal);
                }
            }else{
                if(!mapList.contains(data)){
                    mapList.add(data);
                }
            }
        }
        return mapList;
    }

	/**
	 * 根据所传pid查询旧的父级节点的子节点并修改相应状态值
	 * @param pid
	 */
	private void updateOldParentNode(String pid) {
		if(!IBizWorkTypeService.ROOT_PID_VALUE.equals(pid)) {
			Integer count = baseMapper.selectCount(new QueryWrapper<BizWorkType>().eq("parent_id", pid));
			if(count==null || count<=1) {
				baseMapper.updateTreeNodeStatus(pid, IBizWorkTypeService.NOCHILD);
			}
		}
	}

	/**
     * 递归查询节点的根节点
     * @param pidVal
     * @return
     */
    private BizWorkType getTreeRoot(String pidVal){
        BizWorkType data =  baseMapper.selectById(pidVal);
        if(data != null && !"0".equals(data.getParentId())){
            return this.getTreeRoot(data.getParentId());
        }else{
            return data;
        }
    }

    /**
     * 根据id查询所有子节点id
     * @param ids
     * @return
     */
    private String queryTreeChildIds(String ids) {
        //获取id数组
        String[] idArr = ids.split(",");
        StringBuffer sb = new StringBuffer();
        for (String pidVal : idArr) {
            if(pidVal != null){
                if(!sb.toString().contains(pidVal)){
                    if(sb.toString().length() > 0){
                        sb.append(",");
                    }
                    sb.append(pidVal);
                    this.getTreeChildIds(pidVal,sb);
                }
            }
        }
        return sb.toString();
    }

    /**
     * 递归查询所有子节点
     * @param pidVal
     * @param sb
     * @return
     */
    private StringBuffer getTreeChildIds(String pidVal,StringBuffer sb){
        List<BizWorkType> dataList = baseMapper.selectList(new QueryWrapper<BizWorkType>().eq("parent_id", pidVal));
        if(dataList != null && dataList.size()>0){
            for(BizWorkType tree : dataList) {
                if(!sb.toString().contains(tree.getId())){
                    sb.append(",").append(tree.getId());
                }
                this.getTreeChildIds(tree.getId(),sb);
            }
        }
        return sb;
    }

    //获取当前系统内所有工种
    @Override
    public Map<String,String> getAllSystemWorkType(String getMap,String getMap2){
        List<BizWorkType> list = this.list();
        Map<String, String> map = list.stream().filter(e -> e.getWorkTypeName() != null).filter(e -> e.getId() != null)
                .collect(Collectors.toMap(BizWorkType::getId, BizWorkType::getWorkTypeName));
        return map;
    }

    @Override
    public Set<String> getAllSystemWorkType(String getSet){
        List<BizWorkType> list = this.list();
        Set<String> collect = list.stream().filter(e -> e.getWorkTypeName() != null).filter(e -> e.getId() != null)
                .map(BizWorkType::getWorkTypeName)
                .collect(Collectors.toSet());
        return collect;
    }

}
