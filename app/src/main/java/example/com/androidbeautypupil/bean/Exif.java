package example.com.androidbeautypupil.bean;

/**
 * @author fandong
 * @date 2016/9/13
 * @description exif信息
 */

public class Exif {
    private int rotate;
    private float focusLength;

    public int getRotate() {
        return rotate;
    }

    public void setRotate(int rotate) {
        this.rotate = rotate;
    }

    public float getFocusLength() {
        return focusLength;
    }

    public void setFocusLength(float focusLength) {
        this.focusLength = focusLength;
    }
}
