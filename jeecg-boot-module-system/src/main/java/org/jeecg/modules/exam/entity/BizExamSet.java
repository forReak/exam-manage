package org.jeecg.modules.exam.entity;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.jeecg.common.aspect.annotation.Dict;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @Description: biz_exam_set
 * @Author: jeecg-boot
 * @Date:   2021-02-10
 * @Version: V1.0
 */
@Data
@TableName("biz_exam_set")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@ApiModel(value="biz_exam_set对象", description="biz_exam_set")
public class BizExamSet implements Serializable {
    private static final long serialVersionUID = 1L;

	/**id*/
	@TableId(type = IdType.ASSIGN_ID)
    @ApiModelProperty(value = "id")
    private String id;
	/**所属工种id*/
	@Excel(name = "所属工种id", width = 15)
    @ApiModelProperty(value = "所属工种id")
    @Dict(dicCode = "id",dictTable="biz_work_type",dicText="work_type_name")
    private String workTypeId;
	/**考试时长*/
	@Excel(name = "考试时长", width = 15)
    @ApiModelProperty(value = "考试时长")
    private Integer time;
	/**试卷总分*/
	@Excel(name = "试卷总分", width = 15)
    @ApiModelProperty(value = "试卷总分")
    private Integer totalScore;
	/**分数线*/
	@Excel(name = "分数线", width = 15)
    @ApiModelProperty(value = "分数线")
    private Integer passScore;
	/**选择题分值*/
	@Excel(name = "选择题分值", width = 15)
    @ApiModelProperty(value = "选择题分值")
    private Integer switchScore;
	/**选择题数量*/
	@Excel(name = "选择题数量", width = 15)
    @ApiModelProperty(value = "选择题数量")
    private Integer switchNum;
	/**判断题分值*/
	@Excel(name = "判断题分值", width = 15)
    @ApiModelProperty(value = "判断题分值")
    private Integer judgeScore;
	/**判断题数量*/
	@Excel(name = "判断题数量", width = 15)
    @ApiModelProperty(value = "判断题数量")
    private Integer judgeNum;
	/**开始考试时间 HH:MM:ss*/
	@Excel(name = "开始考试时间 HH:MM:ss", width = 15)
    @ApiModelProperty(value = "开始考试时间 HH:MM:ss")
    private String beginExamTime;
	/**结束考试时间*/
	@Excel(name = "结束考试时间", width = 15)
    @ApiModelProperty(value = "结束考试时间")
    private String endExamTime;
}
