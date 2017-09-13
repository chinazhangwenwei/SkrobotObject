package com.interjoy.skrobotobject.bean;

/**
 * Title:
 * Description:
 * Company: 北京盛开互动科技有限公司
 * Tel: 010-62538800
 * Mail: support@interjoy.com.cn
 *
 * @author zhangwenwei
 * @date 2017/9/11
 */
public class ResultTag {
    private String tag;// 标签名
    private int confidence;// 置信度

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public int getConfidence() {
        return confidence;
    }

    public void setConfidence(int confidence) {
        this.confidence = confidence;
    }

    @Override
    public String toString() {
        return "ResultTag [tag=" + tag + ", confidence=" + confidence + "]";
    }

}