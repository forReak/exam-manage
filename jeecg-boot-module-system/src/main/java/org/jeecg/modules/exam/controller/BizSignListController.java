package org.jeecg.modules.exam.controller;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.SecurityUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.system.vo.LoginUser;
import org.jeecg.modules.exam.entity.BizExamDay;
import org.jeecg.modules.exam.entity.BizSignList;
import org.jeecg.modules.exam.entity.ChartsVo;
import org.jeecg.modules.exam.service.IBizExamDayService;
import org.jeecg.modules.exam.service.IBizSignListService;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;

import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.modules.exam.service.IBizSubjectService;
import org.jeecg.modules.exam.service.IBizWorkTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.jeecg.common.aspect.annotation.AutoLog;

import static org.jeecg.common.util.DateUtils.getDate;
import static org.jeecg.modules.exam.utils.CommonUtils.getZeroDate;

/**
 * @Description: 报名信息
 * @Author: jeecg-boot
 * @Date:   2021-02-15
 * @Version: V1.0
 */
@Api(tags="报名信息")
@RestController
@RequestMapping("/exam/bizSignList")
@Slf4j
public class BizSignListController extends JeecgController<BizSignList, IBizSignListService> {
	@Autowired
	private IBizSignListService bizSignListService;
	@Autowired
	private IBizSubjectService bizSubjectService;
	@Autowired
	private IBizWorkTypeService bizWorkTypeService;
	@Autowired
	private IBizExamDayService bizExamDayService;

	/**
	 * 分页列表查询
	 *
	 * @param bizSignList
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@AutoLog(value = "报名信息-分页列表查询")
	@ApiOperation(value="报名信息-分页列表查询", notes="报名信息-分页列表查询")
	@GetMapping(value = "/list")
	public Result<?> queryPageList(BizSignList bizSignList,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) {
		QueryWrapper<BizSignList> queryWrapper = QueryGenerator.initQueryWrapper(bizSignList, req.getParameterMap());
		LoginUser user = (LoginUser)SecurityUtils.getSubject().getPrincipal();
		if(!"admin".equals(user.getUsername())){
			queryWrapper.eq("create_by",user.getUsername());
		}
		Page<BizSignList> page = new Page<BizSignList>(pageNo, pageSize);
		IPage<BizSignList> pageList = bizSignListService.page(page, queryWrapper);
		List<BizSignList> records = pageList.getRecords();
		records.forEach(e -> e.setUserPic("data:image/png;base64,"+e.getUserPic())
		);
		return Result.OK(pageList);
	}

	/**
	 *   添加
	 *
	 * @param bizSignList
	 * @return
	 */
	@AutoLog(value = "报名信息-添加")
	@ApiOperation(value="报名信息-添加", notes="报名信息-添加")
	@PostMapping(value = "/add")
	public Result<?> add(@RequestBody BizSignList bizSignList) {
		bizSignList.setPassword("123456");
		bizSignList.setHasExam("0");
		try {
			//获取所有工种的map
			Map<String, String> workTypeIdMap = bizWorkTypeService.getAllSystemWorkType(null, null);
			if(workTypeIdMap==null || workTypeIdMap.size()==0){
				throw new RuntimeException("管理员没有设置工种！报名失败！");
			}
			Date todayZero = getZeroDate();
			List<BizExamDay> list = bizExamDayService.lambdaQuery().ge(BizExamDay::getExamDay, todayZero).list();
			//今天之后的所有考试日期
			List<Date> allExamDay = list.stream().filter(e -> e.getExamDay() != null).map(BizExamDay::getExamDay).collect(Collectors.toList());
			if(allExamDay.size()==0){
				throw new RuntimeException("管理员没有设置考试日期！报名失败！");
			}
			//校验参数
			bizSignListService.checkSignInfo(bizSignList,workTypeIdMap,allExamDay);
			String userPic = bizSignList.getUserPic();
			String replace = userPic.replace("data:image/png;base64,", "");
			bizSignList.setUserPic(replace);

			//判断题目数量
			String workTypeId = bizSignList.getWorkTypeId();
			HashSet<String> set = new HashSet<>();
			set.add(workTypeId);
			bizSubjectService.checkSubjectNumber(set);
			//判断有没有重复的报名记录（没有考试的）
			List<BizSignList> bizSignLists = bizSignListService.checkSignDup(bizSignList.getExamTime(), bizSignList.getIdCard(), bizSignList.getWorkTypeId());
			if(bizSignLists.size()>0){
				return Result.error("报名重复！该考生这一天已经有重复的报名记录！");
			}

		} catch (Exception e) {
			return Result.error(e.getMessage());
		}
		bizSignListService.save(bizSignList);
		return Result.OK("添加成功！");
	}

	/**
	 *  编辑
	 *
	 * @param bizSignList
	 * @return
	 */
	@AutoLog(value = "报名信息-编辑")
	@ApiOperation(value="报名信息-编辑", notes="报名信息-编辑")
	@PutMapping(value = "/edit")
	public Result<?> edit(@RequestBody BizSignList bizSignList) {
		String userPic = bizSignList.getUserPic();
		String replace = userPic.replace("data:image/png;base64,", "");
		bizSignList.setUserPic(replace);
		try {
			//获取所有工种的map
			Map<String, String> workTypeIdMap = bizWorkTypeService.getAllSystemWorkType(null, null);
			if(workTypeIdMap==null || workTypeIdMap.size()==0){
				throw new RuntimeException("管理员没有设置工种！报名失败！");
			}
			Date todayZero = getZeroDate();
			List<BizExamDay> list = bizExamDayService.lambdaQuery().ge(BizExamDay::getExamDay, todayZero).list();
			//今天之后的所有考试日期
			List<Date> allExamDay = list.stream().filter(e -> e.getExamDay() != null).map(BizExamDay::getExamDay).collect(Collectors.toList());
			if(allExamDay.size()==0){
				throw new RuntimeException("管理员没有设置考试日期！报名失败！");
			}
			bizSignListService.checkSignInfo(bizSignList,workTypeIdMap,allExamDay);
			//判断考试时间
			Date examTime = bizSignList.getExamTime();
			boolean editOrDel = isEditOrDel(examTime,bizSignList.getId());
			if(!editOrDel){
				return Result.error("无法修改过期的报名信息!");
			}
			//判断题目数量
			String workTypeId = bizSignList.getWorkTypeId();
			HashSet<String> set = new HashSet<>();
			set.add(workTypeId);
			bizSubjectService.checkSubjectNumber(set);
		} catch (Exception e) {
			return Result.error(e.getMessage());
		}

		bizSignListService.updateById(bizSignList);
		return Result.OK("编辑成功!");
	}


	 /**
	 * 判断是否能更改/删除报名信息
	 * 返回能不能修改
	 */
	public boolean isEditOrDel(Date examTime ,String signId){
//		LocalDateTime now = LocalDateTime.now();
		LocalDate ld = LocalDate.now();
		LocalDateTime nowZero = ld.atStartOfDay();
		LocalDateTime exam = examTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
		if(exam.isBefore(nowZero)){
			return false;
		}else{
			//判断考过试没
			BizSignList byId = this.bizSignListService.getById(signId);
			if(byId!=null){
				if("1".equals(byId.getHasExam()) || byId.getFirstJoinTime()!=null){
					return false;
				}else{
					return true;
				}
			}else{
				return false;
			}
		}
	}

	/**
	 *   通过id删除
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "报名信息-通过id删除")
	@ApiOperation(value="报名信息-通过id删除", notes="报名信息-通过id删除")
	@DeleteMapping(value = "/delete")
	public Result<?> delete(@RequestParam(name="id",required=true) String id) {
		BizSignList byId = bizSignListService.getById(id);
		boolean editOrDel = isEditOrDel(byId.getExamTime(),id);
		if(!editOrDel){
			return Result.error("无法删除过期的报名信息!");
		}
		bizSignListService.removeById(id);
		return Result.OK("删除成功!");
	}

	/**
	 *  批量删除
	 *
	 * @param ids
	 * @return
	 */
	@AutoLog(value = "报名信息-批量删除")
	@ApiOperation(value="报名信息-批量删除", notes="报名信息-批量删除")
	//@DeleteMapping(value = "/deleteBatch")
	public Result<?> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		this.bizSignListService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.OK("批量删除成功!");
	}

	/**
	 * 通过id查询
	 *
	 * @param id
	 * @return
	 */
	@AutoLog(value = "报名信息-通过id查询")
	@ApiOperation(value="报名信息-通过id查询", notes="报名信息-通过id查询")
	@GetMapping(value = "/queryById")
	public Result<?> queryById(@RequestParam(name="id",required=true) String id) {
		BizSignList bizSignList = bizSignListService.getById(id);
		if(bizSignList==null) {
			return Result.error("未找到对应数据");
		}
		String userPic = bizSignList.getUserPic();
		userPic = "data:image/png;base64,"+userPic;
		bizSignList.setUserPic(userPic);
		return Result.OK(bizSignList);
	}
//
//    /**
//    * 导出excel
//    *
//    * @param request
//    * @param bizSignList
//    */
//    @RequestMapping(value = "/exportXls")
//    public ModelAndView exportXls(HttpServletRequest request, BizSignList bizSignList) {
//        return super.exportXls(request, bizSignList, BizSignList.class, "报名信息");
//    }

    /**
      * 通过excel导入数据
    *
    * @param request
    * @param response
    * @return
    */
    @RequestMapping(value = "/importExcel", method = RequestMethod.POST)
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
        return super.importExcel(request, response, BizSignList.class);
    }

    @RequestMapping(value="/customImportExcel", method = RequestMethod.POST)
	public Result<?> customImportExcel(HttpServletRequest request, HttpServletResponse response) {
		Result<?> result;
		String workTypeId = request.getParameter("workTypeId");
//		if(workTypeId==null || workTypeId.length()==0){
//			return Result.error("请选择本次报名工种！");
//		}
    	try {
			result = bizSignListService.importExcel(workTypeId, request, response, BizSignList.class);
		} catch (Exception e) {
			return Result.error("报名信息上传失败！"+e.getMessage());
		}
    	return result;
	}


	@RequestMapping(value = "/count")
	public Result<?> indexCount(){
		LoginUser user = (LoginUser)SecurityUtils.getSubject().getPrincipal();
		LocalDateTime today = LocalDate.now().atStartOfDay();
		LocalDateTime tomorrow = LocalDate.now().plusDays(1).atStartOfDay();
		DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		String todayZeroStr = today.format(df);
		String tomorrowZeroStr = tomorrow.format(df);

		String monthBegin = getMonthBegin(new Date());
		String monthEnd = getMonthEnd(new Date());

		//日考生 => 今日报名人数
		Integer dayStudent = 0;
		List<BizSignList> list = bizSignListService.lambdaQuery()
				.select(BizSignList::getIdCard)
				.ge(BizSignList::getCreateTime, todayZeroStr)
				.lt(BizSignList::getCreateTime, tomorrowZeroStr)
				.eq(BizSignList::getCreateBy, user.getUsername())
				.groupBy(BizSignList::getIdCard).list();
		dayStudent = list.size();

		//获取日考试次数
		Integer count = bizSignListService.lambdaQuery()
				.select(BizSignList::getIdCard)
				.ge(BizSignList::getCreateTime, todayZeroStr)
				.lt(BizSignList::getCreateTime, tomorrowZeroStr)
				.eq(BizSignList::getCreateBy, user.getUsername())
				.eq(BizSignList::getHasExam, 1)
				.count();

		//获取日考生通过次数
		Long passTime = bizSignListService.getDayPassTime(todayZeroStr,tomorrowZeroStr,user.getUsername());


		//月考生 => 今月报名人数
		Integer monthStudent = 0;
		List<BizSignList> list1 = bizSignListService.lambdaQuery()
				.select(BizSignList::getIdCard)
				.ge(BizSignList::getCreateTime, monthBegin)
				.lt(BizSignList::getCreateTime, monthEnd)
				.eq(BizSignList::getCreateBy, user.getUsername())
				.groupBy(BizSignList::getIdCard).list();
		monthStudent = list1.size();

		//获取月考试次数
		Integer monthCount = bizSignListService.lambdaQuery()
				.select(BizSignList::getIdCard)
				.ge(BizSignList::getCreateTime, monthBegin)
				.lt(BizSignList::getCreateTime, monthEnd)
				.eq(BizSignList::getCreateBy, user.getUsername())
				.eq(BizSignList::getHasExam, 1)
				.count();

		//获取月考生通过次数
		Long monthPassTime = bizSignListService.getDayPassTime(monthBegin,monthEnd,user.getUsername());



		Result<ChartsVo> result = new Result<>();
		ChartsVo chartsVo = new ChartsVo();

		chartsVo.setStudentNums((long)dayStudent);
		chartsVo.setMonthStudentNums((long)monthStudent);

		chartsVo.setExamTime((long)count);
		chartsVo.setMonthExamTime((long)monthCount);

		chartsVo.setPassTime(passTime);
		chartsVo.setMonthPassTime(monthPassTime);

		chartsVo.setPassPercent(percent(passTime,(long)count));
		chartsVo.setMonthPassPercent(percent(monthPassTime,(long)monthCount));

		result.setResult(chartsVo);
		return result;
	}

	private static String percent(long l1,long l2){
    	BigDecimal b1 = new BigDecimal(l1);
    	BigDecimal b2 = new BigDecimal(l2);

    	if(l2 == 0){
    		return "0.000";
		}
    	BigDecimal b100 = new BigDecimal("100");

		BigDecimal divide = b1.divide(b2, 3, BigDecimal.ROUND_HALF_UP);
		return divide.multiply(b100).toString();
	}


	private static String getMonthBegin(Date date) {
		SimpleDateFormat aDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Calendar c = Calendar.getInstance();
		c.setTime(date);

		//设置为1号,当前日期既为本月第一天
		c.set(Calendar.DAY_OF_MONTH, 1);
		//将小时至0
		c.set(Calendar.HOUR_OF_DAY, 0);
		//将分钟至0
		c.set(Calendar.MINUTE, 0);
		//将秒至0
		c.set(Calendar.SECOND, 0);
		//将毫秒至0
		c.set(Calendar.MILLISECOND, 0);
		// 获取本月第一天的时间
		return aDateFormat.format(c.getTime());
	}

	public static String getMonthEnd(Date date) {
		SimpleDateFormat aDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Calendar c = Calendar.getInstance();
		c.setTime(date);

		//设置为当月最后一天
		c.set(Calendar.DAY_OF_MONTH, c.getActualMaximum(Calendar.DAY_OF_MONTH));
		//将小时至23
		c.set(Calendar.HOUR_OF_DAY, 23);
		//将分钟至59
		c.set(Calendar.MINUTE, 59);
		//将秒至59
		c.set(Calendar.SECOND, 59);
		//将毫秒至999
		c.set(Calendar.MILLISECOND, 999);
		// 获取本月最后一天的时间
		return aDateFormat.format(c.getTime());
	}

}
