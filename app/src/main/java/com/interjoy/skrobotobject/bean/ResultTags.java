package com.interjoy.skrobotobject.bean;

import java.util.List;

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
public class ResultTags extends ResultResponse {
    List<ResultTag> tags;

    public List<ResultTag> getTags() {
        return tags;
    }

    public void setTags(List<ResultTag> tags) {
        this.tags = tags;
    }
}
