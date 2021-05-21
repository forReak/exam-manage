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
 * @Description: biz_subject
 * @Author: jeecg-boot
 * @Date:   2021-02-10
 * @Version: V1.0
 */
@Data
@TableName("biz_subject")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@ApiModel(value="biz_subject对象", description="biz_subject")
public class BizSubject implements Serializable {
    private static final long serialVersionUID = 1L;

	/**id*/
	@TableId(type = IdType.ASSIGN_ID)
    @ApiModelProperty(value = "id")
    private String id;
	/**所属工种id*/
	@Excel(name = "工种", width = 15 ,dicCode = "id",dictTable="biz_work_type",dicText="all_name")
    @ApiModelProperty(value = "所属工种id")
    @Dict(dicCode = "id",dictTable="biz_work_type",dicText="work_type_name")
    private String workTypeId;
	/**题型*/
	@Excel(name = "题型(选择题，判断题)", width = 15, dicCode = "subject_type")
	@Dict(dicCode = "subject_type")
    @ApiModelProperty(value = "题型")
    private Integer subType;
	/**题干*/
	@Excel(name = "题干", width = 15)
    @ApiModelProperty(value = "题干")
    private String subDesc;
	/**选择题答案*/
	@Excel(name = "选择题答案", width = 15)
    @ApiModelProperty(value = "选择题答案（a,b,c）")
    private String switchResult;
	/**判断题答案(y,n)*/
	@Excel(name = "判断题答案(正确，错误)", width = 15,dicCode = "judge_code")
    @ApiModelProperty(value = "判断题答案(y,n)")
    @Dict(dicCode = "judge_code")
    private String judgeResult;
	/**a选项*/
	@Excel(name = "a选项", width = 15)
    @ApiModelProperty(value = "a选项")
    private String resultA;
    @Excel(name = "a选项图片", width = 15)
    @ApiModelProperty(value = "a图片")
    private String picA;
	/**b选项*/
	@Excel(name = "b选项", width = 15)
    @ApiModelProperty(value = "b选项")
    private String resultB;
    @Excel(name = "b选项图片", width = 15)
    @ApiModelProperty(value = "b图片")
    private String picB;
	/**c选项*/
	@Excel(name = "c选项", width = 15)
    @ApiModelProperty(value = "c选项")
    private String resultC;
    @Excel(name = "c选项图片", width = 15)
    @ApiModelProperty(value = "c图片")
    private String picC;
	/**正确选项*/
	@Excel(name = "正确选项", width = 15)
    @ApiModelProperty(value = "正确选项")
    private String resultYes;
	/**错误选项*/
	@Excel(name = "错误选项", width = 15)
    @ApiModelProperty(value = "错误选项")
    private String resultNo;
	/**createTime*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @ApiModelProperty(value = "createTime")
    private Date createTime;
	/**createBy*/
    @ApiModelProperty(value = "createBy")
    private String createBy;
	/**updateTime*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @ApiModelProperty(value = "updateTime")
    private Date updateTime;
	/**updateBy*/
    @ApiModelProperty(value = "updateBy")
    private String updateBy;
}
