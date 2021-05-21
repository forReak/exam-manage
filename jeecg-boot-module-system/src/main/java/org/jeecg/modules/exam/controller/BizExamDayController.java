package org.jeecg.modules.exam.controller;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.SecurityUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.exam.entity.BatchAddVo;
import org.jeecg.modules.exam.entity.BizExamDay;
import org.jeecg.modules.exam.entity.BizSignList;
import org.jeecg.modules.exam.service.IBizExamDayService;

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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;
import com.alibaba.fastjson.JSON;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.jeecg.common.aspect.annotation.AutoLog;

import static com.alibaba.fastjson.serializer.SerializerFeature.UseISO8601DateFormat;

/**
 * @Description: 考试排期
 * @Author: jeecg-boot
 * @Date:   2021-02-15
 * @Version: V1.0
 */
@Api(tags="考试排期")
@RestController
@RequestMapping("/exam/bizExamDay")
@Slf4j
public class BizExamDayController extends JeecgController<BizExamDay, IBizExamDayService> {
	@Autowired
	private IBizExamDayService bizExamDayService;
	@Autowired
	private IBizSignListService bizSignListService;
	/**
	 * 分页列表查询
	 *
	 * @param bizExamDay
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@AutoLog(value = "考试排期-分页列表查询")
	@ApiOperation(value="考试排期-分页列表查询", notes="考试排期-分页列表查询")
	@GetMapping(value = "/list")
	public Result<?> queryPageList(BizExamDay bizExamDay,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) {
		QueryWrapper<BizExamDay> queryWrapper = QueryGenerator.initQueryWrapper(bizExamDay, req.getParameterMap());
		Page<BizExamDay> page = new Page<BizExamDay>(pageNo, pageSize);
		IPage<BizExamDay> pageList = bizExamDayService.page(page, queryWrapper);
		return Result.OK(pageList);
	}

	/**
	 * 查找今年设置的考试日期
	 * @return
	 */
	@GetMapping(value = "/getAllDayList")
	public Result<?> getAllDayList(){
		String thisYear = String.valueOf(Calendar.getInstance().get(Calendar.YEAR));
		String yearBeginStr = thisYear+"-01-01";
		String yearEndStr = thisYear+"-12-31";
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		try {
			Date yearBegin = sdf.parse(yearBeginStr);
			Date yearEnd = sdf.parse(yearEndStr);
			List<BizExamDay> list = bizExamDayService.lambdaQuery().between(BizExamDay::getExamDay, yearBegin, yearEnd).list();
			List<String> result = new ArrayList<>();
			if(list.size()>0){
				list.forEach( e -> {
					Date examDay = e.getExamDay();
					result.add(sdf.format(examDay));
				});
			}
			return Result.OK(result);
		} catch (ParseException e) {
			e.printStackTrace();
			return Result.error("时间转换出错！");
		}

	}



	/**
	 *   添加
	 *
	 * @param bizExamDay
	 * @return
	 */
	@AutoLog(value = "考试排期-添加")
	@ApiOperation(value="考试排期-添加", notes="考试排期-添加")
	//@PostMapping(value = "/add")
	public Result<?> add(@RequestBody BizExamDay bizExamDay) {
		bizExamDayService.save(bizExamDay);
		return Result.OK("添加成功！");
	}

	@PostMapping(value = "/batchAdd")
	public Result<?> batchAdd(@RequestBody BatchAddVo batchAddVo){
		List<String> examDayList = batchAddVo.getExamDayList();
		LoginUser principal = (LoginUser) SecurityUtils.getSubject().getPrincipal();
		String username = principal.getUsername();
		if(examDayList!=null && examDayList.size()>0){
			//本次添加的重复考试日
			List<String> dupDate = new ArrayList<>();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			Random random = new Random();
			for (String examDay : examDayList) {
				BizExamDay bizExamDay = new BizExamDay();
				bizExamDay.setCreateTime(new Date());
				Date parsedDay;
				try {
					parsedDay = sdf.parse(examDay);
				} catch (ParseException e) {
					e.printStackTrace();
					continue;
				}
				bizExamDay.setExamDay(parsedDay);
				int i = random.nextInt(1000000);
				String passwd = String.format("%06d", i);
				bizExamDay.setDayPassword(passwd);
				bizExamDay.setCreateBy(username);
				try {
					bizExamDayService.save(bizExamDay);
				} catch (Exception e) {
					e.printStackTrace();
					if(e instanceof DuplicateKeyException){
						dupDate.add(examDay);
					}
				}

			}
			if(dupDate.size()>0){
				return Result.ok("添加成功！其中"+JSON.toJSONString(dupDate)+"日期已经是考试日！");
			}else{
				return Result.ok("考试排期成功!");
			}
		}else{
			return Result.ok("添加成功!");
		}
	}

	@PostMapping("/removeDay")
	public Result<?> removeDay(@RequestBody BatchAddVo batchAddVo){
		List<String> examDayList = batchAddVo.getExamDayList();
		if(examDayList!=null && examDayList.size()>0){
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			List<Date> remove = new ArrayList<>();
			for (String examDay : examDayList) {
				Date parsedDay;
				try {
					parsedDay = sdf.parse(examDay);
				} catch (ParseException e) {
					e.printStackTrace();
					continue;
				}
				remove.add(parsedDay);
			}

			List<BizExamDay> list = bizExamDayService.lambdaQuery().in(BizExamDay::getExamDay, remove).list();
			if(list.size()>0){
				//依次判断是否有考试日已报名。如果已报名，则无法删除
				for (BizExamDay bizExamDay : list) {
					Date examDay = bizExamDay.getExamDay();
					String format = sdf.format(examDay);
					List<BizSignList> list1 = bizSignListService.lambdaQuery().eq(BizSignList::getExamTime, examDay).list();
					if(list1.size()>0){
						return Result.error("不能删除"+format+"考试日，该已经有报名记录!");
					}
				}

				List<String> ids = list.stream().map(BizExamDay::getId).collect(Collectors.toList());
				bizExamDayService.removeByIds(ids);
				return Result.ok("删除成功!");
			}else{
				return Result.error("当前不是考试日!");
			}
		}else{
			return Result.ok("入参参数校验失败!");
		}
	}

	/**
	 *  编辑
	 *
	 * @param bizExamDay
	 * @return
	 */
	@AutoLog(value = "考试排期-编辑")
	@ApiOperation(value="考试排期-编辑", notes="考试排期-编辑")
	@PutMapping(value = "/edit")
	public Result<?> edit(@RequestBody BizExamDay bizExamDay) {
		bizExamDayService.updateById(bizExamDay);
		return Result.OK("编辑成功!");
	}

	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "考试排期-通过id删除")
	@ApiOperation(value="考试排期-通过id删除", notes="考试排期-通过id删除")
	@DeleteMapping(value = "/delete")
	public Result<?> delete(@RequestParam(name="id",required=true) String id) {
		bizExamDayService.removeById(id);
		return Result.OK("删除成功!");
	}

	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "考试排期-批量删除")
	@ApiOperation(value="考试排期-批量删除", notes="考试排期-批量删除")
	@DeleteMapping(value = "/deleteBatch")
	public Result<?> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		this.bizExamDayService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.OK("批量删除成功!");
	}

	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "考试排期-通过id查询")
	@ApiOperation(value="考试排期-通过id查询", notes="考试排期-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<?> queryById(@RequestParam(name="id",required=true) String id) {
		BizExamDay bizExamDay = bizExamDayService.getById(id);
		if(bizExamDay==null) {
			return Result.error("未找到对应数据");
		}
		return Result.OK(bizExamDay);
	}

    /**
    * 导出excel
    *
    * @param request
    * @param bizExamDay
    */
    @RequestMapping(value = "/exportXls")
    public ModelAndView exportXls(HttpServletRequest request, BizExamDay bizExamDay) {
        return super.exportXls(request, bizExamDay, BizExamDay.class, "考试排期");
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
        return super.importExcel(request, response, BizExamDay.class);
    }

}
