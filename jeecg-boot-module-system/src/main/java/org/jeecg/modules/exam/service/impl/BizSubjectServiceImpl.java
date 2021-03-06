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
            MultipartFile mfile = entity.getValue();// ????????????????????????
            ImportParams params = new ImportParams();
            params.setNeedSave(true);

                //?????????????????????????????????
                List<BizSubject> excelSubjectInfoList = ExcelImportUtil.importExcel(mfile.getInputStream(), BizSubject.class, params);

                //????????????
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

                    //??????????????????
                    try {
                        checkSubjectInfo(oneSubject);
                    } catch (Exception e) {
                        throw new RuntimeException("???????????????"+(i+2)+"????????????"+e.getMessage());
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
                    throw new RuntimeException("???"+subDesc+"??????????????????");
                }
            }
        }
        return Result.ok("????????????????????????????????????" + allExcel.size());
    }

    public void checkSubjectInfo(BizSubject subject){
        Integer subType = subject.getSubType();
        if(subType==null || (subType!=1&&subType!=2)){
            throw new RuntimeException("?????????????????????");
        }
        String workTypeId = subject.getWorkTypeId();
        if(workTypeId == null){
            throw new RuntimeException("????????????????????????");
        }
        String subDesc = subject.getSubDesc();
        if(subDesc==null || subDesc.isEmpty()){
            throw new RuntimeException("???????????????");
        }
        //??????????????????
        if(subType==1){
            String switchResult = subject.getSwitchResult();
            if(switchResult==null || switchResult.isEmpty()){
                throw new RuntimeException("????????????????????????");
            }
            String resultA = subject.getResultA();
            String resultB = subject.getResultB();
            String resultC = subject.getResultC();
            if(resultA==null || resultA.isEmpty()){
                throw new RuntimeException("???????????????A?????????");
            }
            if(resultB==null || resultB.isEmpty()){
                throw new RuntimeException("???????????????B?????????");
            }
            if(resultC==null || resultC.isEmpty()){
                throw new RuntimeException("???????????????C?????????");
            }
        }
        else{
            String judgeResult = subject.getJudgeResult();
            if(judgeResult==null || judgeResult.isEmpty()){
                throw new RuntimeException("????????????????????????");
            }
        }
    }


    /**
     * ????????????????????????
     * @param workTypeList
     */
    @Override
    public void checkSubjectNumber(Set<String> workTypeList){

        //????????????????????????
        Map<String, String> workTypeIdMap = bizWorkTypeService.getAllSystemWorkType(null, null);


        if(workTypeList==null || workTypeList.size()==0){
            throw new RuntimeException("???????????????????????????");
        }
        //????????????id??????????????????????????????
        List<BizSubject> list = this.lambdaQuery().select(BizSubject::getWorkTypeId,BizSubject::getId,BizSubject::getSubType).in(BizSubject::getWorkTypeId, workTypeList).list();
        if(list==null || list.size()==0 ){
            throw new RuntimeException("?????????????????????????????????");
        }

        //????????????????????????
        Map<String, List<BizSubject>> workTypeSubjectMap = list.stream().filter(e -> e.getWorkTypeId() != null).collect(Collectors.groupingBy(BizSubject::getWorkTypeId));

        //??????????????????
        List<BizExamSet> bizExamSetList = bizExamSetService.lambdaQuery().in(BizExamSet::getWorkTypeId, workTypeList).list();
        if (bizExamSetList == null || bizExamSetList.size()==0){
            throw new RuntimeException("???????????????????????????");
        }

        //??????????????????????????????
        Map<String, BizExamSet> workTypeExamSetMap = bizExamSetList.stream().filter(e -> e.getWorkTypeId() != null).collect(Collectors.toMap(BizExamSet::getWorkTypeId, bizExamSet -> bizExamSet));

        for (Map.Entry<String, List<BizSubject>> entry : workTypeSubjectMap.entrySet()) {
            String k = entry.getKey();
            List<BizSubject> v = entry.getValue();
            //k:??????id v?????????????????????
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
                    //????????????????????????????????????????????????????????????
                    break;
                }
            }
            //??????????????????????????????????????????????????????????????????
            String workTypeName = workTypeIdMap.get(k);
            if (switchindb < switchNum) {
                throw new RuntimeException(workTypeName + "?????????????????????????????????????????????");
            }
            if ( judgeindb < judgeNum) {
                throw new RuntimeException(workTypeName + "?????????????????????????????????????????????");
            }
        }
    }
}
