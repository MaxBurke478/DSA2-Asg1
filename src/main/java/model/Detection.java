package model;
import javafx.geometry.BoundingBox;
import javafx.scene.image.WritableImage;

import java.util.List;

public class Detection {
    private final WritableImage maskImage;
    private final List<BlueBox> boxes;

    public Detection(WritableImage maskImage, List<BlueBox> boxes) {
        this.maskImage = maskImage;
        this.boxes = boxes;
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