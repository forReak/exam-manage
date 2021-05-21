package org.jeecg.modules.exam.service.impl;

import cn.hutool.core.util.IdcardUtil;
import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import org.apache.commons.io.FileUtils;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.*;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.exam.entity.BizExamDay;
import org.jeecg.modules.exam.entity.BizSignList;
import org.jeecg.modules.exam.entity.StudentPassVo;
import org.jeecg.modules.exam.mapper.BizSignListMapper;
import org.jeecg.modules.exam.service.IBizExamDayService;
import org.jeecg.modules.exam.service.IBizSignListService;
import org.jeecg.modules.exam.service.IBizSubjectService;
import org.jeecg.modules.exam.service.IBizWorkTypeService;
import org.jeecg.modules.exam.utils.ImageBean;
import org.jeecgframework.poi.excel.ExcelImportUtil;
import org.jeecgframework.poi.excel.entity.ImportParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

import static org.jeecg.modules.exam.utils.CommonUtils.getZeroDate;


/**
 * @Description: 报名信息
 * @Author: jeecg-boot
 * @Date:   2021-02-15
 * @Version: V1.0
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class BizSignListServiceImpl extends ServiceImpl<BizSignListMapper, BizSignList> implements IBizSignListService {

    @Autowired
    IBizWorkTypeService bizWorkTypeService;
    @Autowired
    IBizSubjectService bizSubjectService;
    @Autowired
    IBizExamDayService bizExamDayService;
    @Resource
    BizSignListMapper bizSignListMapper;

    @Override
    public Result<?> importExcel(String workTypeId, HttpServletRequest request, HttpServletResponse response, Class<BizSignList> bizSignListClass) throws Exception {
        //获取所有工种的map
        Map<String, String> workTypeIdMap = bizWorkTypeService.getAllSystemWorkType(null, null);
        if(workTypeIdMap==null || workTypeIdMap.size()==0){
            throw new RuntimeException("管理员没有设置工种！报名失败！");
        }
        Date todayZoro = getZeroDate();
        List<BizExamDay> list = bizExamDayService.lambdaQuery().ge(BizExamDay::getExamDay,todayZoro).list();
        //今天之后的所有考试日期
        List<Date> allExamDay = list.stream().filter(e -> e.getExamDay() != null).map(BizExamDay::getExamDay).collect(Collectors.toList());
        if(allExamDay.size()==0){
            throw new RuntimeException("管理员没有设置考试日期！报名失败！");
        }
        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
        Map<String, MultipartFile> fileMap = multipartRequest.getFileMap();
        List<BizSignList> allExcel = new ArrayList<>();
        for (Map.Entry<String, MultipartFile> entity : fileMap.entrySet()) {
            MultipartFile mfile = entity.getValue();// 获取上传文件对象
            ImportParams params = new ImportParams();
            params.setNeedSave(true);

                //读取除图片外的其他信息
                List<BizSignList> excelSignInfoList = ExcelImportUtil.importExcel(mfile.getInputStream(), BizSignList.class, params);
                if(excelSignInfoList.size()>50){
                    return Result.error("请上传小于50行的报名记录！");
                }
                //读取图片
                File sigFile = new File(Objects.requireNonNull(mfile.getOriginalFilename()));
                FileUtils.copyInputStreamToFile(mfile.getInputStream(), sigFile);
                List<ImageBean> userPicList = readExcel(sigFile);

                if(excelSignInfoList.size()!=userPicList.size()){
                    return Result.error("照片和行数不匹配，请检查excel中的照片是否放置正确！");
                }
                //将读取的excel中的报名人员信息和图片进行绑定
                for (int i = 0, objectsSize = excelSignInfoList.size(); i < objectsSize; i++) {
                    BizSignList oneSignInfo = excelSignInfoList.get(i);
                    //默认设置
                    oneSignInfo.setPassword("123456");
//                    oneSignInfo.setWorkTypeId(workTypeId);
                    oneSignInfo.setHasExam("0");
                    //对excel读取进行去空格
                    oneSignInfo.setUserName(oneSignInfo.getUserName().trim());
                    oneSignInfo.setIdCard(oneSignInfo.getIdCard().trim());
                    oneSignInfo.setPhone(oneSignInfo.getPhone());
                    //设置图片
                    for (ImageBean imageBean : userPicList) {
                        if(imageBean.getRow()==(i+1)){
                            //todo base64 压缩
                            oneSignInfo.setUserPic(imageBean.getCode1());
                            break;
                        }
                    }
                    try {
                        //校验报名信息
                        checkSignInfo(oneSignInfo,workTypeIdMap,allExamDay);
                    } catch (Exception e) {
                        throw new RuntimeException("报名模板信息第"+(i+2)+"行有误！"+e.getMessage());
                    }
                    allExcel.add(oneSignInfo);
                }


        }


        //获取本次导入所有考试工种，判断题目是否存在
        Set<String> workTypeSet = allExcel.stream().filter(e -> e.getWorkTypeId() != null).map(BizSignList::getWorkTypeId).collect(Collectors.toSet());
        //检查工种题目
        try {
            bizSubjectService.checkSubjectNumber(workTypeSet);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }

        List<String> dupSignList = new ArrayList<>();

        //批量导入
        //判断本次导入中的人员是否报名重复
        for (BizSignList bizSignList : allExcel) {
            String userName = bizSignList.getUserName();
            Date examTime = bizSignList.getExamTime();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String format = sdf.format(examTime);
            String idCard = bizSignList.getIdCard();
            String workTypeId1 = bizSignList.getWorkTypeId();
            List<BizSignList> ex = checkSignDup(examTime, idCard, workTypeId1);
            if(ex!=null && ex.size()>1){
                throw new RuntimeException("【"+userName+"】在【"+format+"】天考试报名记录重复！请查询确认他的报名记录！");
            }
            String key = userName+ "_" + bizSignList.getWorkTypeId() + "_"+format;
            if(!dupSignList.contains(key)){
                dupSignList.add(key);
            }else{
                throw new RuntimeException("【"+userName+"】在【"+format+"】天考试报名记录重复！请查询确认他的报名记录！");
            }
//            try {
//                this.save(bizSignList);
//            } catch (Exception e) {
//                e.printStackTrace();
//                if(e instanceof DuplicateKeyException){
//                    String userName = bizSignList.getUserName();
//                    Date examTime = bizSignList.getExamTime();
//                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
//                    String format = sdf.format(examTime);
//                    throw new RuntimeException("【"+userName+"】在【"+format+"】天考试报名记录重复！请查询确认他的报名记录！");
//                }
//            }
        }
        for (BizSignList bizSignList : allExcel) {
            try {
                this.save(bizSignList);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
//        this.saveBatch(allExcel);
        return Result.ok("报名成功！总人场次：" + allExcel.size());
    }

    /**
     * 判断有没有重复的考试记录
     * @param examTime
     * @param idCard
     * @param workTypeId1
     * @return
     */
    @Override
    public List<BizSignList> checkSignDup(Date examTime, String idCard, String workTypeId1) {
        return this.lambdaQuery().eq(BizSignList::getIdCard, idCard)
                        .eq(BizSignList::getWorkTypeId, workTypeId1)
                        .eq(BizSignList::getExamTime, examTime)
                        .eq(BizSignList::getHasExam, 0)
                        .list();
    }

    /**
     * 校验报名参数
     * @param object 一个报名信息
     * @param workTypeIdMap 所有工种map
     * @param allExamDay 所有管理员设置的考试日
     */
    @Override
    public void checkSignInfo(BizSignList object,Map<String, String> workTypeIdMap, List<Date> allExamDay ) {
        String userName = object.getUserName();
        if(userName==null || userName.isEmpty()){
            throw new RuntimeException("没有姓名！");
        }
        Date examTime = object.getExamTime();
        if(examTime==null){
            throw new RuntimeException("没有预约考试日期！");
        }
        //判断用户导入的是否是设置过考试日的考试日期
        if(!allExamDay.contains(examTime)){
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String format = sdf.format(examTime);
            throw new RuntimeException("管理员没有设置"+format+"为考试日期！不能报名当天的考试！");
        }

        String idCard = object.getIdCard();

        if(idCard == null){
            throw new RuntimeException("没有身份证信息！");
        }
        boolean validCard18 = IdcardUtil.isValidCard18(idCard);
        if(!validCard18){
            throw new RuntimeException("身份证无效！");
        }
        String workTypeId = object.getWorkTypeId();
        if(workTypeId == null || workTypeId.isEmpty() || (!workTypeIdMap.containsKey(workTypeId))){
            throw new RuntimeException("没有报名工种信息！");
        }
        String sex = object.getSex();
        if(sex == null || sex.isEmpty()){
            throw new RuntimeException("没有性别信息！");
        }
        String phone = object.getPhone();
        if(phone == null || phone.length()!=11){
            throw new RuntimeException("没有联系方式信息，联系方式必须为手机号！");
        }
        String userPic = object.getUserPic();
        if(userPic == null || userPic.isEmpty()){
            throw new RuntimeException("没有考生照片信息！");
        }
    }
    /**
     * 考试设置需要
     * 判断是否有 没有考试 且 没过期 的报名信息
     * 如果有，返回true
     * @param workTypeId 工种
     * @return
     */
    @Override
    public boolean checkHasNoExamAndNotPass(String workTypeId) {
        LocalDateTime localDateTime = LocalDate.now().atStartOfDay();
        Date date = Date.from( localDateTime.atZone( ZoneId.systemDefault()).toInstant());
        Integer count = this.lambdaQuery().eq(BizSignList::getWorkTypeId, workTypeId)
                .ge(BizSignList::getExamTime, date)
                .eq(BizSignList::getHasExam, "0")
                .count();

        return count > 0;
    }



    /**
     * 读取excel中的图片，返回图片list
     * @param file excel
     * @return
     */
    public static List<ImageBean> readExcel(File file){
        int sheetIndex = 0;

        ExcelReader reader = ExcelUtil.getReader(file, sheetIndex);
        List<ImageBean> list = new ArrayList<>();
        Workbook workbook = reader.getWorkbook();
        if(file.getName().endsWith(".xlsx")){
            XSSFSheet sheet = (XSSFSheet) workbook.getSheetAt(sheetIndex);
            if(sheet.getDrawingPatriarch()!=null){
                for (XSSFShape shape : sheet.getDrawingPatriarch().getShapes()) {
                    XSSFClientAnchor anchor = (XSSFClientAnchor) shape.getAnchor();
                    XSSFPicture pic = (XSSFPicture) shape;
                    //获取行编号
                    int row = anchor.getRow2();
                    //获取列编号
                    int col = anchor.getCol2();
                    XSSFPictureData pictureData = pic.getPictureData();
                    String code1 = Base64.getEncoder().encodeToString(pictureData.getData());
                    ImageBean bean = new ImageBean();
                    bean.setRow(row);
                    bean.setCol(col);
                    bean.setCode1(code1);
                    list.add(bean);

                }
            }

        }else if(file.getName().endsWith(".xls")){
            HSSFSheet sheet = (HSSFSheet) workbook.getSheetAt(sheetIndex);
            if(sheet.getDrawingPatriarch()!=null){
                for (HSSFShape shape : sheet.getDrawingPatriarch().getChildren()) {
                    HSSFClientAnchor anchor = (HSSFClientAnchor) shape.getAnchor();
                    HSSFPicture pic = (HSSFPicture) shape;
                    //获取行编号
                    int row = anchor.getRow2();
                    //获取列编号
                    int col = anchor.getCol2();
                    HSSFPictureData pictureData = pic.getPictureData();
                    String code1 = Base64.getEncoder().encodeToString(pictureData.getData());
                    ImageBean bean = new ImageBean();
                    bean.setRow(row);
                    bean.setCol(col);
                    bean.setCode1(code1);
                    list.add(bean);
                }
            }

        }

        return list;
    }


    /**
     * 获取通过次数
     * @return
     */
    @Override
    public Long getDayPassTime(String s1,String s2,String userName) {
        List<StudentPassVo> result = bizSignListMapper.getDayPassNumber(s1,s2,userName);
//        long count = result.stream().filter(e -> e.getScore() != null).filter(e -> e.getScore() >= 80).count();
        long count = 0;
        for (StudentPassVo studentPassVo : result) {
            if(studentPassVo!=null && studentPassVo.getScore()!=null && studentPassVo.getScore()>=80){
                count++;
            }
        }
        return count;
    }
}
