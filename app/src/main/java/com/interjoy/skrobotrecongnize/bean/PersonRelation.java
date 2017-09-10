package com.interjoy.skrobotrecongnize.bean;

import java.util.List;

/**
 * Title:
 * Description:
 * Company: 北京盛开互动科技有限公司
 * Tel: 010-62538800
 * Mail: support@interjoy.com.cn
 *
 * @author zhangwenwei
 * @date 2017/9/9
 */
public class PersonRelation {

    public int minAge;//最小年龄
    public int maxAge;//最大年龄
    public short relationType;// (1—不限，2—男男，3—女女 4—男女)
    public String relationDescribe;//关系描述
    public short smallPersonMinAge;//较小人的年龄
    public short smallPersonMaxAge;//较大人的年龄
    public List<PersonDescribe> describes;//描述

    public int smallAge = -1;//1,2;
    public static final short ANY_RELATION = 1;
    public static final short BOY_BOY = 2;
    public static final short GIRL_GIRL = 3;
    public static final short BOY_GIRL = 4;

    public static class PersonDescribe {
        public String describe;
        public String songId;

    }


}
