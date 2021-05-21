package org.jeecg.modules.exam.controller;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
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
import org.jeecg.modules.exam.entity.BizExamList;
import org.jeecg.modules.exam.entity.BizSignList;
import org.jeecg.modules.exam.entity.ExamHistory;
import org.jeecg.modules.exam.service.IBizExamListService;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;

import org.jeecg.modules.exam.service.IBizSignListService;
import org.jeecgframework.poi.excel.ExcelImportUtil;
import org.jeecgframework.poi.excel.def.NormalExcelConstants;
import org.jeecgframework.poi.excel.entity.ExportParams;
import org.jeecgframework.poi.excel.entity.ImportParams;
import org.jeecgframework.poi.excel.view.JeecgEntityExcelView;
import org.jeecg.common.system.base.controller.JeecgController;
import org.springframework.beans.BeanUtils;
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
 * @Description: 考试列表
 * @Author: jeecg-boot
 * @Date:   2021-03-13
 * @Version: V1.0
 */
@Api(tags="考试列表")
@RestController
@RequestMapping("/exam/bizExamList")
@Slf4j
public class BizExamListController extends JeecgController<BizExamList, IBizExamListService> {
	@Autowired
	private IBizExamListService bizExamListService;
	@Autowired
	private IBizSignListService bizSignListService;

	/**
	 * 分页列表查询
	 *
	 * @param bizExamList
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@AutoLog(value = "考试列表-分页列表查询")
	@ApiOperation(value="考试列表-分页列表查询", notes="考试列表-分页列表查询")
	@GetMapping(value = "/list")
	public Result<?> queryPageList(BizExamList bizExamList,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) {
		QueryWrapper<BizExamList> queryWrapper = QueryGenerator.initQueryWrapper(bizExamList, req.getParameterMap());
		queryWrapper.isNotNull("score");
		Page<BizExamList> page = new Page<BizExamList>(pageNo, pageSize);
		IPage<BizExamList> pageList = bizExamListService.page(page, queryWrapper);
		IPage<ExamHistory> convert = pageList.convert(this::cov);
		List<ExamHistory> records = convert.getRecords();
		List<String> signIdList = records.stream().map(BizExamList::getSignId).collect(Collectors.toList());
		List<BizSignList> signList = new ArrayList<>();
		if(signIdList.size()>0){
			signList = bizSignListService.lambdaQuery().in(BizSignList::getId, signIdList).list();
		}
		Map<String, BizSignList> collect = signList.stream().collect(Collectors.toMap(BizSignList::getId, bizSignList -> bizSignList));
		for (ExamHistory examHistory : records) {
			BizSignList bizSignList = collect.get(examHistory.getSignId());
			examHistory.setUserName(bizSignList.getUserName());
			examHistory.setSex(bizSignList.getSex());
			examHistory.setIdCard(bizSignList.getIdCard());
			examHistory.setWorkType(bizSignList.getWorkTypeId());
			Integer score = examHistory.getScore();
			examHistory.setResult(score!=null?(score>=80?"通过":"不通过"):"未考试");
		}
		return Result.OK(convert);
	}

	public ExamHistory cov(BizExamList bizExamList){
		ExamHistory examHistory = new ExamHistory();
		BeanUtils.copyProperties(bizExamList,examHistory);
		return examHistory;

	}
	/**
	 *   添加
	 *
	 * @param bizExamList
	 * @return
	 */
	@AutoLog(value = "考试列表-添加")
	@ApiOperation(value="考试列表-添加", notes="考试列表-添加")
	@PostMapping(value = "/add")
	public Result<?> add(@RequestBody BizExamList bizExamList) {
		bizExamListService.save(bizExamList);
		return Result.OK("添加成功！");
	}

	/**
	 *  编辑
	 *
	 * @param bizExamList
	 * @return
	 */
	@AutoLog(value = "考试列表-编辑")
	@ApiOperation(value="考试列表-编辑", notes="考试列表-编辑")
	@PutMapping(value = "/edit")
	public Result<?> edit(@RequestBody BizExamList bizExamList) {
		bizExamListService.updateById(bizExamList);
		return Result.OK("编辑成功!");
	}

	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "考试列表-通过id删除")
	@ApiOperation(value="考试列表-通过id删除", notes="考试列表-通过id删除")
	@DeleteMapping(value = "/delete")
	public Result<?> delete(@RequestParam(name="id",required=true) String id) {
		bizExamListService.removeById(id);
		return Result.OK("删除成功!");
	}

	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "考试列表-批量删除")
	@ApiOperation(value="考试列表-批量删除", notes="考试列表-批量删除")
	@DeleteMapping(value = "/deleteBatch")
	public Result<?> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		this.bizExamListService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.OK("批量删除成功!");
	}

	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "考试列表-通过id查询")
	@ApiOperation(value="考试列表-通过id查询", notes="考试列表-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<?> queryById(@RequestParam(name="id",required=true) String id) {
		BizExamList bizExamList = bizExamListService.getById(id);
		if(bizExamList==null) {
			return Result.error("未找到对应数据");
		}
		return Result.OK(bizExamList);
	}

    /**
    * 导出excel
    *
    * @param request
    * @param bizExamList
    */
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, BizExamList bizExamList) {
        return super.exportXls(request, bizExamList, BizExamList.class, "考试列表");
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
        return super.importExcel(request, response, BizExamList.class);
    }

}
