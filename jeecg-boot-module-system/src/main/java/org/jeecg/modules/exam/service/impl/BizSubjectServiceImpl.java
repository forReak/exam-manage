package org.jeecg.modules.exam.service.impl;

import org.apache.commons.io.FileUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.exam.entity.BizExamSet;
import org.jeecg.modules.exam.entity.BizSubject;
import org.jeecg.modules.exam.mapper.BizSubjectMapper;
import org.jeecg.modules.exam.service.IBizExamSetService;
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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static org.jeecg.modules.exam.service.impl.BizSignListServiceImpl.readExcel;

/**
 * @Description: biz_subject
 * @Author: jeecg-boot
 * @Date:   2021-02-10
 * @Version: V1.0
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class BizSubjectServiceImpl extends ServiceImpl<BizSubjectMapper, BizSubject> implements IBizSubjectService {
    @Autowired
    IBizWorkTypeService bizWorkTypeService;
    @Autowired
    IBizExamSetService bizExamSetService;
    @Override
    public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response, Class<BizSubject> bizSubjectClass) throws Exception {
        Map<String, String> workTypeIdMap = bizWorkTypeService.getAllSystemWorkType(null, null);
        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
        Map<String, MultipartFile> fileMap = multipartRequest.getFileMap();
        List<BizSubject> allExcel = new ArrayList<>();
        for (Map.Entry<String, MultipartFile> entity : fileMap.entrySet()) {
            MultipartFile mfile = entity.getValue();// 获取上传文件对象
            ImportParams params = new ImportParams();
            params.setNeedSave(true);

                //读取除图片外的其他信息
                List<BizSubject> excelSubjectInfoList = ExcelImportUtil.importExcel(mfile.getInputStream(), BizSubject.class, params);

                //读取图片
                File sigFile = new File(Objects.requireNonNull(mfile.getOriginalFilename()));
                FileUtils.copyInputStreamToFile(mfile.getInputStream(), sigFile);
                List<ImageBean> subjectImgList = readExcel(sigFile);

                for (int i = 0; i < excelSubjectInfoList.size(); i++) {
                    BizSubject oneSubject = excelSubjectInfoList.get(i);
                    String workTypeId = oneSubject.getWorkTypeId();
                    for (ImageBean imageBean : subjectImgList) {
                        if(imageBean.getRow()==(i+1)){
                            int col = imageBean.getCol();
                            if(col == 8 ){
                                oneSubject.setPicA(imageBean.getCode1());
                            }else if (col ==9){
                                oneSubject.setPicB(imageBean.getCode1());
                            }else{
                                oneSubject.setPicC(imageBean.getCode1());
                            }
                        }
                    }

                    //检验考试题目
                    try {
                        checkSubjectInfo(oneSubject);
                    } catch (Exception e) {
                        throw new RuntimeException("题目模板第"+(i+2)+"行有误！"+e.getMessage());
                    }

                    if(workTypeId!=null && workTypeIdMap.containsKey(workTypeId)){
                        allExcel.add(oneSubject);
                    }

                }
//            } catch (Exception e) {
//                e.printStackTrace();
//            }

        }

        for (BizSubject bizSubject : allExcel) {
            try {
                this.save(bizSubject);
            } catch (Exception e) {
                e.printStackTrace();
                if(e instanceof DuplicateKeyException){
                    String subDesc = bizSubject.getSubDesc();
                    throw new RuntimeException("【"+subDesc+"】题目重复！");
                }
            }
        }
        return Result.ok("文件导入成功！数据行数：" + allExcel.size());
    }

    public void checkSubjectInfo(BizSubject subject){
        Integer subType = subject.getSubType();
        if(subType==null || (subType!=1&&subType!=2)){
            throw new RuntimeException("没有题目类型！");
        }
        String workTypeId = subject.getWorkTypeId();
        if(workTypeId == null){
            throw new RuntimeException("该题目没有工种！");
        }
        String subDesc = subject.getSubDesc();
        if(subDesc==null || subDesc.isEmpty()){
            throw new RuntimeException("没有题干！");
        }
        //如果是选择题
        if(subType==1){
            String switchResult = subject.getSwitchResult();
            if(switchResult==null || switchResult.isEmpty()){
                throw new RuntimeException("没有选择题答案！");
            }
            String resultA = subject.getResultA();
            String resultB = subject.getResultB();
            String resultC = subject.getResultC();
            if(resultA==null || resultA.isEmpty()){
                throw new RuntimeException("没有选择题A选项！");
            }
            if(resultB==null || resultB.isEmpty()){
                throw new RuntimeException("没有选择题B选项！");
            }
            if(resultC==null || resultC.isEmpty()){
                throw new RuntimeException("没有选择题C选项！");
            }
        }
        else{
            String judgeResult = subject.getJudgeResult();
            if(judgeResult==null || judgeResult.isEmpty()){
                throw new RuntimeException("没有判断题答案！");
            }
        }
    }


    /**
     * 检查试题是否足够
     * @param workTypeList
     */
    @Override
    public void checkSubjectNumber(Set<String> workTypeList){

        //获取工种和中文名
        Map<String, String> workTypeIdMap = bizWorkTypeService.getAllSystemWorkType(null, null);


        if(workTypeList==null || workTypeList.size()==0){
            throw new RuntimeException("没有考试工种信息！");
        }
        //查询题目id，所属工种，题目类型
        List<BizSubject> list = this.lambdaQuery().select(BizSubject::getWorkTypeId,BizSubject::getId,BizSubject::getSubType).in(BizSubject::getWorkTypeId, workTypeList).list();
        if(list==null || list.size()==0 ){
            throw new RuntimeException("没有此工种的题目信息！");
        }

        //根据工种类型分组
        Map<String, List<BizSubject>> workTypeSubjectMap = list.stream().filter(e -> e.getWorkTypeId() != null).collect(Collectors.groupingBy(BizSubject::getWorkTypeId));

        //获取题型设置
        List<BizExamSet> bizExamSetList = bizExamSetService.lambdaQuery().in(BizExamSet::getWorkTypeId, workTypeList).list();
        if (bizExamSetList == null || bizExamSetList.size()==0){
            throw new RuntimeException("没有考试设置信息！");
        }

        //根据工种获取考试设置
        Map<String, BizExamSet> workTypeExamSetMap = bizExamSetList.stream().filter(e -> e.getWorkTypeId() != null).collect(Collectors.toMap(BizExamSet::getWorkTypeId, bizExamSet -> bizExamSet));

        for (Map.Entry<String, List<BizSubject>> entry : workTypeSubjectMap.entrySet()) {
            String k = entry.getKey();
            List<BizSubject> v = entry.getValue();
            //k:工种id v：题库中的题目
            BizExamSet examSet = workTypeExamSetMap.get(k);
            Integer switchNum = examSet.getSwitchNum();
            Integer judgeNum = examSet.getJudgeNum();
            int switchindb = 0;
            int judgeindb = 0;
            for (BizSubject bizSubject : v) {
                if (bizSubject.getSubType().equals(1)) {
                    switchindb++;
                } else {
                    judgeindb++;
                }
                if (switchindb >= switchNum && judgeindb > judgeNum) {
                    //如果该工种提前满足条件，则退出该工种判断
                    break;
                }
            }
            //如果选择题或判断题任意一个小于设置的题目数量
            String workTypeName = workTypeIdMap.get(k);
            if (switchindb < switchNum) {
                throw new RuntimeException(workTypeName + "选择题数量不够！无法报名考试！");
            }
            if ( judgeindb < judgeNum) {
                throw new RuntimeException(workTypeName + "判断题数量不够！无法报名考试！");
            }
        }
    }
}
