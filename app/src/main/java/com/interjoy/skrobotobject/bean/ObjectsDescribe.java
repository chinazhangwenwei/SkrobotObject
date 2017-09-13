package com.interjoy.skrobotobject.bean;

/**
 * Title:
 * Description:
 * Company: 北京盛开互动科技有限公司
 * Tel: 010-62538800
 * Mail: support@interjoy.com.cn
 *
 * @author zhangwenwei
 * @date 2017/9/12
 */
public class ObjectsDescribe {


    /**
     * code : 0
     * msg : success
     * data : {"type_name":"牡丹","type_english":"Peony","type_audio":"http://103.254.115.234:18080/Public/audio/牡丹.mp3"}
     */

    private int code;
    private String msg;
    private DataBean data;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public DataBean getData() {
        return data;
    }

    public void setData(DataBean data) {
        this.data = data;
    }

    public static class DataBean {
        /**
         * type_name : 牡丹
         * type_english : Peony
         * type_audio : http://103.254.115.234:18080/Public/audio/牡丹.mp3
         */

        private String type_name;
        private String type_english;
        private String type_audio;

        public String getType_name() {
            return type_name;
        }

        public void setType_name(String type_name) {
            this.type_name = type_name;
        }

        public String getType_english() {
            return type_english;
        }

        public void setType_english(String type_english) {
            this.type_english = type_english;
        }

        public String getType_audio() {
            return type_audio;
        }

        public void setType_audio(String type_audio) {
            this.type_audio = type_audio;
        }
    }
}
