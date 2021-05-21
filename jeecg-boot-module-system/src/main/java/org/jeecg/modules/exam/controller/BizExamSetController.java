package org.jeecg.modules.exam.controller;

import java.util.Arrays;
import java.util.HashMap;
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
import org.jeecg.modules.exam.entity.BizExamSet;
import org.jeecg.modules.exam.entity.BizWorkType;
import org.jeecg.modules.exam.service.IBizExamSetService;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;

import org.jeecg.modules.exam.service.IBizSignListService;
import org.jeecg.modules.exam.service.IBizWorkTypeService;
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
 * @Description: biz_exam_set
 * @Author: jeecg-boot
 * @Date:   2021-02-10
 * @Version: V1.0
 */
@Api(tags="biz_exam_set")
@RestController
@RequestMapping("/exam/bizExamSet")
@Slf4j
public class BizExamSetController extends JeecgController<BizExamSet, IBizExamSetService> {
	@Autowired
	private IBizExamSetService bizExamSetService;
	@Autowired
	private IBizWorkTypeService bizWorkTypeService;
	@Autowired
	private IBizSignListService bizSignListService;
	/**
	 * 分页列表查询
	 *
	 * @param bizExamSet
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@AutoLog(value = "biz_exam_set-分页列表查询")
	@ApiOperation(value="biz_exam_set-分页列表查询", notes="biz_exam_set-分页列表查询")
	@GetMapping(value = "/list")
	public Result<?> queryPageList(BizExamSet bizExamSet,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) {
		QueryWrapper<BizExamSet> queryWrapper = QueryGenerator.initQueryWrapper(bizExamSet, req.getParameterMap());
		Page<BizExamSet> page = new Page<BizExamSet>(pageNo, pageSize);
		IPage<BizExamSet> pageList = bizExamSetService.page(page, queryWrapper);
		return Result.OK(pageList);
	}

	/**
	 *   添加
	 *
	 * @param bizExamSet
	 * @return
	 */
	@AutoLog(value = "biz_exam_set-添加")
	@ApiOperation(value="biz_exam_set-添加", notes="biz_exam_set-添加")
	@PostMapping(value = "/add")
	public Result<?> add(@RequestBody BizExamSet bizExamSet) {
		try {
			checkExamSet(bizExamSet);
		} catch (Exception e) {
			return Result.error(e.getMessage());
		}

		bizExamSetService.save(bizExamSet);
		return Result.OK("添加成功！");
	}

	 private void checkExamSet(@RequestBody BizExamSet bizExamSet) {
		 if(bizExamSet.getWorkTypeId()==null || bizExamSet.getWorkTypeId().trim().length()==0){
			 throw new RuntimeException("请选择工种！");
		 }
		 if(bizExamSet.getTime()==null || bizExamSet.getTime()==0){
			 throw new RuntimeException("请设置考试时间！");
		 }
		 if(bizExamSet.getTotalScore()==null || bizExamSet.getTotalScore()==0){
			 throw new RuntimeException("请设置试卷总分！");
		 }
		 if(bizExamSet.getPassScore()==null || bizExamSet.getPassScore()==0){
			 throw new RuntimeException("请设置分数线！");
		 }
		 if(bizExamSet.getPassScore()>bizExamSet.getTotalScore()){
			 throw new RuntimeException("分数线不能大于试卷总分！");
		 }
		 if(bizExamSet.getSwitchScore()==null || bizExamSet.getSwitchScore()==0){
			 throw new RuntimeException("请设置选择题分值！");
		 }
		 if(bizExamSet.getJudgeScore()==null || bizExamSet.getJudgeScore()==0){
			 throw new RuntimeException("请设置判断题分值！");
		 }
		 if(bizExamSet.getSwitchNum()==null || bizExamSet.getSwitchNum()==0){
			 throw new RuntimeException("请设置选择题数量！");
		 }
		 if(bizExamSet.getJudgeNum()==null || bizExamSet.getJudgeNum()==0){
			 throw new RuntimeException("请设置判断题数量！");
		 }
		 if(bizExamSet.getBeginExamTime()==null){
			 throw new RuntimeException("请设置开始考试时间！");
		 }
		 if(bizExamSet.getEndExamTime()==null){
			 throw new RuntimeException("请设置结束考试时间！");
		 }
	 }

	 /**
	 *  编辑
	 *
	 * @param bizExamSet
	 * @return
	 */
	@AutoLog(value = "biz_exam_set-编辑")
	@ApiOperation(value="biz_exam_set-编辑", notes="biz_exam_set-编辑")
	@PutMapping(value = "/edit")
	public Result<?> edit(@RequestBody BizExamSet bizExamSet) {
		String modWorkTypeId = bizExamSet.getWorkTypeId();

		BizExamSet set = bizExamSetService.getById(bizExamSet.getId());
		if(set!=null){
			String dbWorkTypeId = set.getWorkTypeId();
			if(!modWorkTypeId.equals(dbWorkTypeId)){
				return Result.error("不能修改主工种!");
			}
			boolean b = bizSignListService.checkHasNoExamAndNotPass(dbWorkTypeId);
			if(b){
				return Result.error("无法编辑考试设置！还有未考试的报名信息！");
			}else{
				try {
					checkExamSet(bizExamSet);
				} catch (Exception e) {
					return Result.error(e.getMessage());
				}
				bizExamSetService.updateById(bizExamSet);
				return Result.OK("编辑成功!");
			}
		}else{
			return Result.error("找不到考试设置!");
		}
	}

	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "biz_exam_set-通过id删除")
	@ApiOperation(value="biz_exam_set-通过id删除", notes="biz_exam_set-通过id删除")
	@DeleteMapping(value = "/delete")
	public Result<?> delete(@RequestParam(name="id",required=true) String id) {
		BizExamSet set = bizExamSetService.getById(id);
		if(set!=null){
			String workTypeId = set.getWorkTypeId();
			boolean b = bizSignListService.checkHasNoExamAndNotPass(workTypeId);
			if(b){
				return Result.error("无法删除考试设置！还有未考试的报名信息！");
			}else{
				bizExamSetService.removeById(id);
				return Result.OK("删除成功!");
			}
		}else{
			return Result.OK("没有找到考试信息!");
		}

	}

	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "biz_exam_set-批量删除")
	@ApiOperation(value="biz_exam_set-批量删除", notes="biz_exam_set-批量删除")
//	@DeleteMapping(value = "/deleteBatch")
	public Result<?> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		this.bizExamSetService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.OK("批量删除成功!");
	}

	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "biz_exam_set-通过id查询")
	@ApiOperation(value="biz_exam_set-通过id查询", notes="biz_exam_set-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<?> queryById(@RequestParam(name="id",required=true) String id) {
		BizExamSet bizExamSet = bizExamSetService.getById(id);
		if(bizExamSet==null) {
			return Result.error("未找到对应数据");
		}
		return Result.OK(bizExamSet);
	}

    /**
    * 导出excel
    *
    * @param request
    * @param bizExamSet
    */
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, BizExamSet bizExamSet) {
        return super.exportXls(request, bizExamSet, BizExamSet.class, "biz_exam_set");
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
        return super.importExcel(request, response, BizExamSet.class);
    }

}
