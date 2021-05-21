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
 * @Description: 考试列表
 * @Author: jeecg-boot
 * @Date:   2021-03-13
 * @Version: V1.0
 */
@Data
@TableName("biz_exam_list")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@ApiModel(value="biz_exam_list对象", description="考试列表")
public class BizExamList implements Serializable {
    private static final long serialVersionUID = 1L;

	/**主键*/
	@TableId(type = IdType.ASSIGN_ID)
    @ApiModelProperty(value = "主键")
    private java.lang.String id;
	/**创建人*/
    @ApiModelProperty(value = "创建人")
    private java.lang.String createBy;
	/**考试日期*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "考试日期")
    private java.util.Date createTime;
	/**更新人*/
    @ApiModelProperty(value = "更新人")
    private java.lang.String updateBy;
	/**更新日期*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "更新日期")
    private java.util.Date updateTime;
	/**选择题列表*/
	@Excel(name = "选择题列表", width = 15)
    @ApiModelProperty(value = "选择题列表")
    private java.lang.String switchList;
	/**判断题列表*/
	@Excel(name = "判断题列表", width = 15)
    @ApiModelProperty(value = "判断题列表")
    private java.lang.String judgeList;
	/**选择题答案*/
	@Excel(name = "选择题答案", width = 15)
    @ApiModelProperty(value = "选择题答案")
    private java.lang.String switchAnswer;
	/**判断题答案*/
	@Excel(name = "判断题答案", width = 15)
    @ApiModelProperty(value = "判断题答案")
    private java.lang.String judgeAnswer;
	/**分数*/
	@Excel(name = "分数", width = 15)
    @ApiModelProperty(value = "分数")
    private java.lang.Integer score;
	/**报名id*/
	@Excel(name = "报名id", width = 15)
    @ApiModelProperty(value = "报名id")
    private java.lang.String signId;
}
