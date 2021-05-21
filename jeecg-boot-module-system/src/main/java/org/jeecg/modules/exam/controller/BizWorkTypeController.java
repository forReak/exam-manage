package org.jeecg.modules.exam.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.exam.entity.BizWorkType;
import org.jeecg.modules.exam.service.IBizWorkTypeService;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;

import org.jeecgframework.poi.excel.ExcelImportUtil;
import org.jeecgframework.poi.excel.def.NormalExcelConstants;
import org.jeecgframework.poi.excel.entity.ExportParams;
import org.jeecgframework.poi.excel.entity.ImportParams;
import org.jeecgframework.poi.excel.view.JeecgEntityExcelView;
import org.jeecg.common.system.base.controller.JeecgController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;
import com.alibaba.fastjson.JSON;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.jeecg.common.aspect.annotation.AutoLog;

 /**
 * @Description: biz_work_type
 * @Author: jeecg-boot
 * @Date:   2021-02-10
 * @Version: V1.0
 */
@Api(tags="biz_work_type")
@RestController
@RequestMapping("/exam/bizWorkType")
@Slf4j
public class BizWorkTypeController extends JeecgController<BizWorkType, IBizWorkTypeService>{
	@Autowired
	private IBizWorkTypeService bizWorkTypeService;

	/**
	 * 分页列表查询
	 *
	 * @param bizWorkType
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@AutoLog(value = "biz_work_type-分页列表查询")
	@ApiOperation(value="biz_work_type-分页列表查询", notes="biz_work_type-分页列表查询")
	@GetMapping(value = "/rootList")
	public Result<?> queryPageList(BizWorkType bizWorkType,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) {
		String hasQuery = req.getParameter("hasQuery");
        if(hasQuery != null && "true".equals(hasQuery)){
            QueryWrapper<BizWorkType> queryWrapper =  QueryGenerator.initQueryWrapper(bizWorkType, req.getParameterMap());
            List<BizWorkType> list = bizWorkTypeService.queryTreeListNoPage(queryWrapper);
            IPage<BizWorkType> pageList = new Page<>(1, 10, list.size());
            pageList.setRecords(list);
            return Result.OK(pageList);
        }else{
            String parentId = bizWorkType.getParentId();
            if (oConvertUtils.isEmpty(parentId)) {
                parentId = "0";
            }
            bizWorkType.setParentId(null);
            QueryWrapper<BizWorkType> queryWrapper = QueryGenerator.initQueryWrapper(bizWorkType, req.getParameterMap());
            // 使用 eq 防止模糊查询
            queryWrapper.eq("parent_id", parentId);
            Page<BizWorkType> page = new Page<BizWorkType>(pageNo, pageSize);
            IPage<BizWorkType> pageList = bizWorkTypeService.page(page, queryWrapper);
            return Result.OK(pageList);
        }
	}

	 /**
      * 获取子数据
      * @param bizWorkType
      * @param req
      * @return
      */
	@AutoLog(value = "biz_work_type-获取子数据")
	@ApiOperation(value="biz_work_type-获取子数据", notes="biz_work_type-获取子数据")
	@GetMapping(value = "/childList")
	public Result<?> queryPageList(BizWorkType bizWorkType,HttpServletRequest req) {
		QueryWrapper<BizWorkType> queryWrapper = QueryGenerator.initQueryWrapper(bizWorkType, req.getParameterMap());
		List<BizWorkType> list = bizWorkTypeService.list(queryWrapper);
		IPage<BizWorkType> pageList = new Page<>(1, 10, list.size());
        pageList.setRecords(list);
		return Result.OK(pageList);
	}

    /**
      * 批量查询子节点
      * @param parentIds 父ID（多个采用半角逗号分割）
      * @return 返回 IPage
      * @param parentIds
      * @return
      */
	@AutoLog(value = "biz_work_type-批量获取子数据")
    @ApiOperation(value="biz_work_type-批量获取子数据", notes="biz_work_type-批量获取子数据")
    @GetMapping("/getChildListBatch")
    public Result getChildListBatch(@RequestParam("parentIds") String parentIds) {
        try {
            QueryWrapper<BizWorkType> queryWrapper = new QueryWrapper<>();
            List<String> parentIdList = Arrays.asList(parentIds.split(","));
            queryWrapper.in("parent_id", parentIdList);
            List<BizWorkType> list = bizWorkTypeService.list(queryWrapper);
            IPage<BizWorkType> pageList = new Page<>(1, 10, list.size());
            pageList.setRecords(list);
            return Result.OK(pageList);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return Result.error("批量查询子节点失败：" + e.getMessage());
        }
    }

	/**
	 *   添加
	 *
	 * @param bizWorkType
	 * @return
	 */
	@AutoLog(value = "biz_work_type-添加")
	@ApiOperation(value="biz_work_type-添加", notes="biz_work_type-添加")
	@PostMapping(value = "/add")
	public Result<?> add(@RequestBody BizWorkType bizWorkType) {
		bizWorkTypeService.addBizWorkType(bizWorkType);
		return Result.OK("添加成功！");
	}

	/**
	 *  编辑
	 *
	 * @param bizWorkType
	 * @return
	 */
	@AutoLog(value = "biz_work_type-编辑")
	@ApiOperation(value="biz_work_type-编辑", notes="biz_work_type-编辑")
	//@PutMapping(value = "/edit")
	public Result<?> edit(@RequestBody BizWorkType bizWorkType) {
		bizWorkTypeService.updateBizWorkType(bizWorkType);
		return Result.OK("编辑成功!");
	}

	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "biz_work_type-通过id删除")
	@ApiOperation(value="biz_work_type-通过id删除", notes="biz_work_type-通过id删除")
	@DeleteMapping(value = "/delete")
	public Result<?> delete(@RequestParam(name="id",required=true) String id) {
		bizWorkTypeService.deleteBizWorkType(id);
		return Result.OK("删除成功!");
	}

	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "biz_work_type-批量删除")
	@ApiOperation(value="biz_work_type-批量删除", notes="biz_work_type-批量删除")
	@DeleteMapping(value = "/deleteBatch")
	public Result<?> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		this.bizWorkTypeService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.OK("批量删除成功！");
	}

	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "biz_work_type-通过id查询")
	@ApiOperation(value="biz_work_type-通过id查询", notes="biz_work_type-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<?> queryById(@RequestParam(name="id",required=true) String id) {
		BizWorkType bizWorkType = bizWorkTypeService.getById(id);
		if(bizWorkType==null) {
			return Result.error("未找到对应数据");
		}
		return Result.OK(bizWorkType);
	}

    /**
    * 导出excel
    *
    * @param request
    * @param bizWorkType
    */
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, BizWorkType bizWorkType) {
		return super.exportXls(request, bizWorkType, BizWorkType.class, "biz_work_type");
    }

    /**
      * 通过excel导入数据
    *
    * @param request
    * @param response
    * @return
    */
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
		return super.importExcel(request, response, BizWorkType.class);
    }

}
