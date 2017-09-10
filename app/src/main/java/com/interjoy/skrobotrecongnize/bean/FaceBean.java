package com.interjoy.skrobotrecongnize.bean;

import java.util.List;

/**
 * Created by ylwang on 2017/8/15.
 */

public class FaceBean
{

    /**
     * faces : [{"Sex":2,"Glass":1,"name":"NULL","face_rectangle":{"right_bottom":{"x":296,"y":346},"right_top":{"x":296,"y":190},"left_bottom":{"x":140,"y":346},"left_top":{"x":140,"y":190}},"landmark":{"right_eye":{"x":0,"y":0},"nose":{"x":0,"y":0},"mouth_right":{"x":0,"y":0},"left_eye":{"x":0,"y":0},"mouth_left":{"x":0,"y":0}},"Age":27}]
     */
    private List<FacesEntity> faces;

    public void setFaces(List<FacesEntity> faces) {
        this.faces = faces;
    }

    public List<FacesEntity> getFaces() {
        return faces;
    }

    public class FacesEntity {
        /**
         * Sex : 2
         * Glass : 1
         * name : NULL
         * face_rectangle : {"right_bottom":{"x":296,"y":346},"right_top":{"x":296,"y":190},"left_bottom":{"x":140,"y":346},"left_top":{"x":140,"y":190}}
         * landmark : {"right_eye":{"x":0,"y":0},"nose":{"x":0,"y":0},"mouth_right":{"x":0,"y":0},"left_eye":{"x":0,"y":0},"mouth_left":{"x":0,"y":0}}
         * Age : 27
         */
        private int Sex;
        private int Glass;
        private String name;
        private Face_rectangleEntity face_rectangle;
        private LandmarkEntity landmark;
        private int Age;

        public void setSex(int Sex) {
            this.Sex = Sex;
        }

        public void setGlass(int Glass) {
            this.Glass = Glass;
        }

        public void setName(String name) {
            this.name = name;
        }

        public void setFace_rectangle(Face_rectangleEntity face_rectangle) {
            this.face_rectangle = face_rectangle;
        }

        public void setLandmark(LandmarkEntity landmark) {
            this.landmark = landmark;
        }

        public void setAge(int Age) {
            this.Age = Age;
        }

        public int getSex() {
            return Sex;
        }

        public int getGlass() {
            return Glass;
        }

        public String getName() {
            return name;
        }

        public Face_rectangleEntity getFace_rectangle() {
            return face_rectangle;
        }

        public LandmarkEntity getLandmark() {
            return landmark;
        }

        public int getAge() {
            return Age;
        }

        public class Face_rectangleEntity {
            /**
             * right_bottom : {"x":296,"y":346}
             * right_top : {"x":296,"y":190}
             * left_bottom : {"x":140,"y":346}
             * left_top : {"x":140,"y":190}
             */
            private Right_bottomEntity right_bottom;
            private Right_topEntity right_top;
            private Left_bottomEntity left_bottom;
            private Left_topEntity left_top;

            public void setRight_bottom(Right_bottomEntity right_bottom) {
                this.right_bottom = right_bottom;
            }

            public void setRight_top(Right_topEntity right_top) {
                this.right_top = right_top;
            }

            public void setLeft_bottom(Left_bottomEntity left_bottom) {
                this.left_bottom = left_bottom;
            }

            public void setLeft_top(Left_topEntity left_top) {
                this.left_top = left_top;
            }

            public Right_bottomEntity getRight_bottom() {
                return right_bottom;
            }

            public Right_topEntity getRight_top() {
                return right_top;
            }

            public Left_bottomEntity getLeft_bottom() {
                return left_bottom;
            }

            public Left_topEntity getLeft_top() {
                return left_top;
            }

            public class Right_bottomEntity {
                /**
                 * x : 296
                 * y : 346
                 */
                private int x;
                private int y;

                public void setX(int x) {
                    this.x = x;
                }

                public void setY(int y) {
                    this.y = y;
                }

                public int getX() {
                    return x;
                }

                public int getY() {
                    return y;
                }
            }

            public class Right_topEntity {
                /**
                 * x : 296
                 * y : 190
                 */
                private int x;
                private int y;

                public void setX(int x) {
                    this.x = x;
                }

                public void setY(int y) {
                    this.y = y;
                }

                public int getX() {
                    return x;
                }

                public int getY() {
                    return y;
                }
            }

            public class Left_bottomEntity {
                /**
                 * x : 140
                 * y : 346
                 */
                private int x;
                private int y;

                public void setX(int x) {
                    this.x = x;
                }

                public void setY(int y) {
                    this.y = y;
                }

                public int getX() {
                    return x;
                }

                public int getY() {
                    return y;
                }
            }

            public class Left_topEntity {
                /**
                 * x : 140
                 * y : 190
                 */
                private int x;
                private int y;

                public void setX(int x) {
                    this.x = x;
                }

                public void setY(int y) {
                    this.y = y;
                }

                public int getX() {
                    return x;
                }

                public int getY() {
                    return y;
                }
            }
        }

        public class LandmarkEntity {
            /**
             * right_eye : {"x":0,"y":0}
             * nose : {"x":0,"y":0}
             * mouth_right : {"x":0,"y":0}
             * left_eye : {"x":0,"y":0}
             * mouth_left : {"x":0,"y":0}
             */
            private Right_eyeEntity right_eye;
            private NoseEntity nose;
            private Mouth_rightEntity mouth_right;
            private Left_eyeEntity left_eye;
            private Mouth_leftEntity mouth_left;

            public void setRight_eye(Right_eyeEntity right_eye) {
                this.right_eye = right_eye;
            }

            public void setNose(NoseEntity nose) {
                this.nose = nose;
            }

            public void setMouth_right(Mouth_rightEntity mouth_right) {
                this.mouth_right = mouth_right;
            }

            public void setLeft_eye(Left_eyeEntity left_eye) {
                this.left_eye = left_eye;
            }

            public void setMouth_left(Mouth_leftEntity mouth_left) {
                this.mouth_left = mouth_left;
            }

            public Right_eyeEntity getRight_eye() {
                return right_eye;
            }

            public NoseEntity getNose() {
                return nose;
            }

            public Mouth_rightEntity getMouth_right() {
                return mouth_right;
            }

            public Left_eyeEntity getLeft_eye() {
                return left_eye;
            }

            public Mouth_leftEntity getMouth_left() {
                return mouth_left;
            }

            public class Right_eyeEntity {
                /**
                 * x : 0
                 * y : 0
                 */
                private int x;
                private int y;

                public void setX(int x) {
                    this.x = x;
                }

                public void setY(int y) {
                    this.y = y;
                }

                public int getX() {
                    return x;
                }

                public int getY() {
                    return y;
                }
            }

            public class NoseEntity {
                /**
                 * x : 0
                 * y : 0
                 */
                private int x;
                private int y;

                public void setX(int x) {
                    this.x = x;
                }

                public void setY(int y) {
                    this.y = y;
                }

                public int getX() {
                    return x;
                }

                public int getY() {
                    return y;
                }
            }

            public class Mouth_rightEntity {
                /**
                 * x : 0
                 * y : 0
                 */
                private int x;
                private int y;

                public void setX(int x) {
                    this.x = x;
                }

                public void setY(int y) {
                    this.y = y;
                }

                public int getX() {
                    return x;
                }

                public int getY() {
                    return y;
                }
            }

            public class Left_eyeEntity {
                /**
                 * x : 0
                 * y : 0
                 */
                private int x;
                private int y;

                public void setX(int x) {
                    this.x = x;
                }

                public void setY(int y) {
                    this.y = y;
                }

                public int getX() {
                    return x;
                }

                public int getY() {
                    return y;
                }
            }

            public class Mouth_leftEntity {
                /**
                 * x : 0
                 * y : 0
                 */
                private int x;
                private int y;

                public void setX(int x) {
                    this.x = x;
                }

                public void setY(int y) {
                    this.y = y;
                }

                public int getX() {
                    return x;
                }

                public int getY() {
                    return y;
                }
            }
        }
    }
}
