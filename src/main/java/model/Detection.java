package model;
import javafx.scene.image.WritableImage;

import java.util.List;
import java.util.Map;

public class Detection {
    private final WritableImage maskImage;
    private final List<BlueBox> boxes;
    private final Map<Integer,List<int[]>> componentPixels;
    private final Map<Integer,Integer> pixelRootMap;

    public Detection(WritableImage maskImage, List<BlueBox> boxes,Map<Integer,List<int[]>> componentPixels,Map<Integer,Integer> pixelRootMap ) {
        this.maskImage = maskImage;
        this.boxes = boxes;
        this.componentPixels = componentPixels;
        this.pixelRootMap = pixelRootMap;
    }

    public Map<Integer, List<int[]>> getComponentPixels() {
        return componentPixels;
    }
    public Integer getRootForPixel(int x, int y,int imageWidth) {
        return pixelRootMap.get(imageWidth*y + x);
    }

    public WritableImage getMaskImage() {
        return maskImage;
    }
    public List<BlueBox> getBoxes() {
        return boxes;
    }
    public int getCount(){
        return boxes.size();
    }
}