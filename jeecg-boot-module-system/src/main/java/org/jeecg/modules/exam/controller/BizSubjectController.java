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
import org.jeecg.modules.exam.entity.BizSubject;
import org.jeecg.modules.exam.entity.GetSubjectParam;
import org.jeecg.modules.exam.service.IBizSubjectService;

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
 * @Description: biz_subject
 * @Author: jeecg-boot
 * @Date:   2021-02-10
 * @Version: V1.0
 */
@Api(tags="biz_subject")
@RestController
@RequestMapping("/exam/bizSubject")
@Slf4j
public class BizSubjectController extends JeecgController<BizSubject, IBizSubjectService> {
	@Autowired
	private IBizSubjectService bizSubjectService;

	/**
	 * 分页列表查询
	 *
	 * @param bizSubject
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@AutoLog(value = "biz_subject-分页列表查询")
	@ApiOperation(value="biz_subject-分页列表查询", notes="biz_subject-分页列表查询")
	@GetMapping(value = "/list")
	public Result<?> queryPageList(BizSubject bizSubject,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) {
		QueryWrapper<BizSubject> queryWrapper = QueryGenerator.initQueryWrapper(bizSubject, req.getParameterMap());
		queryWrapper.orderByAsc("create_time");
		Page<BizSubject> page = new Page<BizSubject>(pageNo, pageSize);
		IPage<BizSubject> pageList = bizSubjectService.page(page, queryWrapper);
		List<BizSubject> records = pageList.getRecords();
		records.forEach( e -> {
			if(e.getPicA()!=null ){
				e.setPicA("data:image/png;base64,"+e.getPicA());
			}
			if(e.getPicB()!=null ){
				e.setPicB("data:image/png;base64,"+e.getPicB());
			}
			if(e.getPicC()!=null ){
				e.setPicC("data:image/png;base64,"+e.getPicC());
			}
		});
		return Result.OK(pageList);
	}

	/**
	 *   添加
	 *
	 * @param bizSubject
	 * @return
	 */
	@AutoLog(value = "biz_subject-添加")
	@ApiOperation(value="biz_subject-添加", notes="biz_subject-添加")
	@PostMapping(value = "/add")
	public Result<?> add(@RequestBody BizSubject bizSubject) {
		bizSubject.setPicA(bizSubject.getPicA().replace("data:image/png;base64,", ""));
		bizSubject.setPicB(bizSubject.getPicB().replace("data:image/png;base64,", ""));
		bizSubject.setPicC(bizSubject.getPicC().replace("data:image/png;base64,", ""));
		bizSubjectService.save(bizSubject);
		return Result.OK("添加成功！");
	}

	/**
	 *  编辑
	 *
	 * @param bizSubject
	 * @return
	 */
	@AutoLog(value = "biz_subject-编辑")
	@ApiOperation(value="biz_subject-编辑", notes="biz_subject-编辑")
	@PutMapping(value = "/edit")
	public Result<?> edit(@RequestBody BizSubject bizSubject) {
		if(bizSubject.getPicA()!=null){
			bizSubject.setPicA(bizSubject.getPicA().replace("data:image/png;base64,", ""));
		}
		if(bizSubject.getPicB()!=null){
			bizSubject.setPicB(bizSubject.getPicB().replace("data:image/png;base64,", ""));
		}
		if(bizSubject.getPicC()!=null){
			bizSubject.setPicC(bizSubject.getPicC().replace("data:image/png;base64,", ""));
		}
		bizSubjectService.updateById(bizSubject);
		return Result.OK("编辑成功!");
	}

	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "biz_subject-通过id删除")
	@ApiOperation(value="biz_subject-通过id删除", notes="biz_subject-通过id删除")
	@DeleteMapping(value = "/delete")
	public Result<?> delete(@RequestParam(name="id",required=true) String id) {
		bizSubjectService.removeById(id);
		return Result.OK("删除成功!");
	}

	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "biz_subject-批量删除")
	@ApiOperation(value="biz_subject-批量删除", notes="biz_subject-批量删除")
	@DeleteMapping(value = "/deleteBatch")
	public Result<?> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		this.bizSubjectService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.OK("批量删除成功!");
	}

	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "biz_subject-通过id查询")
	@ApiOperation(value="biz_subject-通过id查询", notes="biz_subject-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<?> queryById(@RequestParam(name="id",required=true) String id) {
		BizSubject bizSubject = bizSubjectService.getById(id);
		if(bizSubject==null) {
			return Result.error("未找到对应数据");
		}
		return Result.OK(bizSubject);
	}

	 @PostMapping(value = "/getByIdList")
	 public Result<?> getByIdList(@RequestBody GetSubjectParam getSubjectParam) {
		 List<String> ids = getSubjectParam.getIds();
		 if(ids !=null && ids.size()>0){
			List<BizSubject> list = bizSubjectService.lambdaQuery().in(BizSubject::getId, ids).list();
			return Result.OK(list);
		}else{
			return Result.error("传入的题目为空！");
		}
	 }

    /**
    * 导出excel
    *
    * @param request
    * @param bizSubject
    */
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, BizSubject bizSubject) {
        return super.exportXls(request, bizSubject, BizSubject.class, "biz_subject");
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
		Result<?> result;
		try {
			result = bizSubjectService.importExcel(request, response, BizSubject.class);
		} catch (Exception e) {
			e.printStackTrace();
			return Result.error("文件导入失败！"+e.getMessage());
		}
		return result;
	}

}
