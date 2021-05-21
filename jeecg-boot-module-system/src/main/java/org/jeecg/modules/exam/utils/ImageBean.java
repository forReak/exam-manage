package org.jeecg.modules.exam.utils;

import java.io.InputStream;

/**
 * @author furao
 * @desc
 * @date 2021/2/21
 * @package org.jeecg.modules.exam.utils
 */
public class ImageBean {
    private int row;
    private int col;
    //存放图片base64
    private String code1;
    private String code2;

    private String url;

    public int getRow() {
        return row;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public int getCol() {
        return col;
    }

    public void setCol(int col) {
        this.col = col;
    }

    public String getCode1() {
        return code1;
    }

    public void setCode1(String code1) {
        this.code1 = code1;
    }

    public String getCode2() {
        return code2;
    }

    public void setCode2(String code2) {
        this.code2 = code2;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
