package org.example;

import finds.leafDetector;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import model.BlueBox;
import model.ColourSample;
import model.Detection;

import java.io.File;
import java.util.*;

public class PrimaryController {

    @FXML
    private Label brightMaxLabel;

    @FXML
    private Slider brightMaxSlider;

    @FXML
    private Label brightMinLabel;

    @FXML
    private Slider brightMinSlider;

    @FXML
    private Button clearButton;

    @FXML
    private Label countLabel;

    @FXML
    private Button detectButton;

    @FXML
    private Slider hueSlider;

    @FXML
    private Label hueSliderLabel;

    @FXML
    private StackPane leftPane;

    @FXML
    private Button loadButton;

    @FXML
    private ImageView maskImageView;

    @FXML
    private Label maxSizeLabel;

    @FXML
    private Slider maxSizeSlider;

    @FXML
    private Label minSizeLabel;

    @FXML
    private Slider minSizeSlider;

    @FXML
    private ImageView originalImageView;

    @FXML
    private Canvas overlayCanvas;

    @FXML
    private StackPane rightPane;

    @FXML
    private Slider satSlider;
   @FXML
   private Label sampleLabel;
    @FXML private ToggleButton highlightModeButton;
    @FXML
    private Button randomColourButton;
    @FXML
    private Label saturationLabel;
    private final Tooltip leafTooltip = new Tooltip();
    private BlueBox hoveredBlueBox= null;
    private Image currentImage;
    private final leafDetector detector= new leafDetector();
    private final List<ColourSample> samples = new ArrayList<>();
    private Detection lastResult;
    int hueNumber;
    double saturationNumber;
    double brightMinNumber;
    double brightMaxNumber;
    int minSizeNumber;
    int maxSizeNumber;
    private WritableImage colouredMaskImage;
    private Map<Integer,Color> componentColours;
    private Timeline tspTimeline;
    private enum ClickMode { SAMPLE, HIGHLIGHT, TSP_START }
    private ClickMode clickMode = ClickMode.SAMPLE;
    @FXML private ToggleButton tspModeButton;
    @FXML private ToggleButton sampleToggle;

    public void initialize() {
        overlayCanvas.setMouseTransparent(true);
        hueSlider.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number number, Number t1) {
                hueNumber =(int) hueSlider.getValue();
                hueSliderLabel.setText("Hue: "+String.valueOf(hueNumber));
            }
        });
        satSlider.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number number, Number t1) {
                saturationNumber =(double) satSlider.getValue();
                saturationLabel.setText("Saturation: "+String.valueOf(saturationNumber));
            }
        });
        brightMinSlider.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number number, Number t1) {
                brightMinNumber =(double) brightMinSlider.getValue();
                brightMinLabel.setText("Brightness min: "+String.valueOf(brightMinNumber));
            }
        });
        brightMaxSlider.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number number, Number t1) {
                brightMaxNumber =(double) brightMaxSlider.getValue();
                brightMaxLabel.setText("Brightness Max: "+String.valueOf(brightMaxNumber));
            }
        });
        minSizeSlider.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number number, Number t1) {
                minSizeNumber =(int) minSizeSlider.getValue();
                minSizeLabel.setText("Min size: "+String.valueOf(minSizeNumber));
            }
        });
        maxSizeSlider.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number number, Number t1) {
                maxSizeNumber =(int) maxSizeSlider.getValue();
                maxSizeLabel.setText("Max Size: "+String.valueOf(maxSizeNumber));
            }
        });
        originalImageView.setOnMouseMoved(e -> handleHover(e.getX(), e.getY()));
        originalImageView.setOnMouseExited(e -> {
            leafTooltip.hide();
            hoveredBlueBox = null;
        });
        originalImageView.setOnMouseClicked(e -> {
            if (clickMode == ClickMode.SAMPLE) {
                addSampleFromImageViewClick(e.getX(), e.getY());
            } else if (clickMode == ClickMode.HIGHLIGHT) {
                highlightComponentsAtClick(e.getX(), e.getY());
            } else if (clickMode == ClickMode.TSP_START) {
                startTSPFromClick(e.getX(), e.getY());
            }
        });
        tspModeButton.setOnAction(e -> clickMode = ClickMode.TSP_START);
        sampleToggle.setOnAction(e -> clickMode = ClickMode.SAMPLE);
        highlightModeButton.setOnAction(e -> clickMode = ClickMode.HIGHLIGHT);
        randomColourButton.setOnAction(e -> randomlyColourComponents());
        countLabel.setText("Leaves: 0");
        sampleLabel.setText("Samples: 0");
    }

    private void handleHover(double mouseX, double mouseY) {

        if (currentImage == null || lastResult == null) {
            leafTooltip.hide();
            hoveredBlueBox = null;
            return;
        }

        double imageWidth = currentImage.getWidth();
        double imageHeight = currentImage.getHeight();

        double viewWidth = originalImageView.getBoundsInLocal().getWidth();
        double viewHeight = originalImageView.getBoundsInLocal().getHeight();

        double scale = Math.min(viewWidth / imageWidth, viewHeight / imageHeight);

        double displayedWidth = imageWidth * scale;
        double displayedHeight = imageHeight * scale;

        double offsetX = (viewWidth - displayedWidth) / 2;
        double offsetY = (viewHeight - displayedHeight) / 2;

        // If outside image area, hide tooltip
        if (mouseX < offsetX || mouseX > offsetX + displayedWidth ||
                mouseY < offsetY || mouseY > offsetY + displayedHeight) {

            leafTooltip.hide();
            hoveredBlueBox = null;
            return;
        }

        int imgX = (int) ((mouseX - offsetX) / scale);
        int imgY = (int) ((mouseY - offsetY) / scale);

        BlueBox found = null;

        for (BlueBox b : lastResult.getBoxes()) {   //loop to find the correct box
            if (imgX >= b.getMinX() && imgX <= b.getMaxX()
                    && imgY >= b.getMinY() && imgY <= b.getMaxY()) {
                found = b;
                break;
            }
        }

        if (found == null) {
            leafTooltip.hide();
            hoveredBlueBox = null;
            return;
        }

        if (hoveredBlueBox == found) return;
        hoveredBlueBox = found;

        leafTooltip.setText(
                "Leaf/Cluster Number: " + found.getRank() +
                        "\nEstimated Size (pixel units): " + found.getPixelCount()
        );

        var screenPoint = originalImageView.localToScreen(mouseX, mouseY);
        leafTooltip.show(originalImageView, screenPoint.getX() + 15, screenPoint.getY() + 15);
    }




    private void addSampleFromImageViewClick(double mouseX, double mouseY) {

        if (currentImage == null) return;

        double imageWidth = currentImage.getWidth();
        double imageHeight = currentImage.getHeight();

        double viewWidth = originalImageView.getBoundsInLocal().getWidth();
        double viewHeight = originalImageView.getBoundsInLocal().getHeight();

        double scale = Math.min(viewWidth / imageWidth, viewHeight / imageHeight);

        double displayedWidth = imageWidth * scale;
        double displayedHeight = imageHeight * scale;
        double offsetX = (viewWidth - displayedWidth) / 2;
        double offsetY = (viewHeight - displayedHeight) / 2;
        if (mouseX < offsetX || mouseX > offsetX + displayedWidth ||
                mouseY < offsetY || mouseY > offsetY + displayedHeight) {

            System.out.println("Clicked outside the actual image area.");
            return;
        }
        int imgX = (int) ((mouseX - offsetX) / scale);
        int imgY = (int) ((mouseY - offsetY) / scale);
        imgX = Math.max(0, Math.min(imgX, (int) imageWidth - 1));
        imgY = Math.max(0, Math.min(imgY, (int) imageHeight - 1));

        PixelReader reader = currentImage.getPixelReader();
        if (reader == null) return;

        Color clicked = reader.getColor(imgX, imgY);

        ColourSample sample = new ColourSample(    //when user clicks image, get the hue,sat and brightness
                clicked.getHue(),
                clicked.getSaturation(),
                clicked.getBrightness()
        );

        samples.add(sample);  //add it to sample array

        sampleLabel.setText("Samples: " + samples.size());
    }

    @FXML void onLoadImage(){
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose an Image");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.jpg", "*.png","*.jpeg"));
        File file = fileChooser.showOpenDialog(new Stage());
        if(file != null){
            currentImage = new Image(file.toURI().toString());
            originalImageView.setImage(currentImage);
            maskImageView.setImage(null);
            lastResult=null;
            clearOverlay();

            countLabel.setText("Image loaded");
        }
    }
    @FXML
    private void onClearSamples() {
        samples.clear();
        sampleLabel.setText("Samples: 0");
        countLabel.setText("Samples cleared");
    }

    @FXML
    private void onDetect() {

        if (currentImage == null) {
            countLabel.setText("Load an image first!");
            return;
        }
        if(samples.isEmpty()){
            countLabel.setText("Click leaves to sample colours");
        }
        Detection result = detector.detect(currentImage,samples,(double)hueSlider.getValue(),(double)satSlider.getValue(),(double)brightMinSlider.getValue(),(double)brightMaxSlider.getValue(),(int)minSizeSlider.getValue(),(int)maxSizeSlider.getValue(),true);
        maskImageView.setImage(result.getMaskImage());
        drawBlueBoxes(result);
        lastResult = result;
        countLabel.setText("Leaves: "+result.getCount());
    }

    private void drawBlueBoxes(Detection result){
        clearOverlay();
        if(result==null)return;
        if(currentImage==null) return;

        var gc = overlayCanvas.getGraphicsContext2D();
        gc.setLineWidth(2);
        gc.setStroke(javafx.scene.paint.Color.BLUE);
        gc.setFill(javafx.scene.paint.Color.BLUE);
        double imgW=currentImage.getWidth();
        double imgH=currentImage.getHeight();
        double canvasW=overlayCanvas.getWidth();
        double canvasH=overlayCanvas.getHeight();
        double scale = Math.min(canvasW / imgW, canvasH / imgH);

        double displayedW = imgW * scale;
        double displayedH = imgH * scale;
        double offsetX = (canvasW - displayedW) / 2;
        double offsetY = (canvasH - displayedH) / 2;

        for (BlueBox b : result.getBoxes()) {
            double x = offsetX + b.getMinX() * scale;
            double y = offsetY + b.getMinY() * scale;
            double w = b.getWidth() * scale;
            double h = b.getHeight() * scale;
            gc.strokeRect(x, y, w, h);
            gc.fillText("" + b.getRank(), x + 3, y + 12);
        }

    }

    private void highlightComponentsAtClick(double mouseX, double mouseY){
        if(currentImage==null||lastResult==null) return;
        double imageWidth = currentImage.getWidth();
        double imageHeight = currentImage.getHeight();
        double viewWidth = originalImageView.getBoundsInLocal().getWidth();
        double viewHeight = originalImageView.getBoundsInLocal().getHeight();
        double scale = Math.min(viewWidth / imageWidth, viewHeight / imageHeight);
        double offsetX =(viewWidth - imageWidth*scale) / 2;
        double offsetY =(viewHeight - imageHeight*scale) / 2;
        int imgX = (int) ((mouseX - offsetX) / scale);
        int imgY = (int) ((mouseY - offsetY) / scale);
        Integer root = lastResult.getRootForPixel(imgX,imgY,(int)imageWidth);  //find the root of the clicked pixel cluster
        if(root==null)return;
        List<int[]>pixels = lastResult.getComponentPixels().get(root);
        if(pixels==null)return;
        int w = (int) lastResult.getMaskImage().getWidth();
        int h = (int) lastResult.getMaskImage().getHeight();
        WritableImage highlight = new WritableImage(w,h);
        var pw = highlight.getPixelWriter();
        for(int y=0;y<h;y++)  //set everthing to black that isnt a leaf
            for(int x = 0; x<w; x++)
                pw.setColor(x,y,Color.BLACK);
        for(Map.Entry<Integer,List<int[]>> entry: lastResult.getComponentPixels().entrySet())
            for(int[] p :entry.getValue())
                pw.setColor(p[0],p[1],Color.WHITE);  //set all leaves white
        for(int[] p: pixels)
            pw.setColor(p[0],p[1],Color.RED);  //set chosen leaf red
        maskImageView.setImage(highlight);
    }

    private void randomlyColourComponents(){
        if(lastResult==null)return;
        int w = (int) lastResult.getMaskImage().getWidth();
        int h = (int) lastResult.getMaskImage().getHeight();
        WritableImage coloured = new WritableImage(w,h);
        var pw = coloured.getPixelWriter();

        for (int y = 0; y < h; y++)
            for (int x = 0; x < w; x++)
                pw.setColor(x, y, Color.BLACK);   //set background black
        Random random = new Random();    //set up a RNG object
        Map<Integer,Color> colourMap = new HashMap<>();
        for(Map.Entry<Integer,List<int[]>> entry: lastResult.getComponentPixels().entrySet()){
            Color c = colourMap.computeIfAbsent(entry.getKey(),k -> Color.hsb(random.nextDouble()*360, 0.8, 1.0));  //get a random colour
            for(int[] p : entry.getValue()){
                pw.setColor(p[0],p[1],c);  //for each leaf apply the random colour
            }
        }
        maskImageView.setImage(coloured);
    }

  private void clearOverlay() {
        var gc = overlayCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, overlayCanvas.getWidth(), overlayCanvas.getHeight());
  }

    private List<BlueBox> computeTSPPath(BlueBox start, List<BlueBox> boxes) {
        List<BlueBox> remaining = new ArrayList<>(boxes);   //all boxes in the image are remaining
        List<BlueBox> path = new ArrayList<>(); //empty array list for the path that will close when all remaining boxes are went through
        BlueBox current = start;   //whichever box is clicked is the start
        remaining.remove(start);  //it is removed from the remaining boxes and added to the path and so on
        path.add(start);

        while (!remaining.isEmpty()) {
            BlueBox nearest = null;
            double bestDist = Double.MAX_VALUE; //set largest distance for base
            for (BlueBox b : remaining) {     //for every blue box remaining
                double dist = distance(current, b);  // get the distance from it to the current box in array
                if (dist < bestDist) {  // if its less than the best distance
                    bestDist = dist; //it is the new best distance
                    nearest = b; //and is set as the nearest until the lowest distance is found
                }
            }
            path.add(nearest);
            remaining.remove(nearest);
            current = nearest;  //loops
        }
        return path;
    }

    private double distance(BlueBox a, BlueBox b) {
        double cx1 = (a.getMinX() + a.getMaxX()) / 2.0;
        double cy1 = (a.getMinY() + a.getMaxY()) / 2.0;
        double cx2 = (b.getMinX() + b.getMaxX()) / 2.0;
        double cy2 = (b.getMinY() + b.getMaxY()) / 2.0;
        return Math.sqrt(Math.pow(cx2 - cx1, 2) + Math.pow(cy2 - cy1, 2));
    }
    private void animateTSPPath(List<BlueBox> path) {
        if (tspTimeline != null) tspTimeline.stop();  //validation buffer if user clicks 2 leaves if the first animation hasnt finished yet

        int n = path.size();
        if (n == 0) return;

        // total 5 seconds split across all steps
        double stepDuration = 5000.0 / n;  //divides the animation path between the number of leaves so it still takes 5 seconds but can be slower or faster depending

        tspTimeline = new Timeline();

        for (int i = 0; i < n; i++) {
            final int index = i;
            final BlueBox box = path.get(i); //current box is i
            final BlueBox prev = (i > 0) ? path.get(i - 1) : null; //if first box the previous is null, if not then its the previous

            // at this keyframe: highlight current box yellow, revert previous to blue
            KeyFrame kf = new KeyFrame(
                    Duration.millis(stepDuration * i),
                    e -> {
                        drawTSPStep(path, index, box, prev);   //for each step, recolour everything with the new box being yellow and the line growing
                    }
            );
            tspTimeline.getKeyFrames().add(kf);
        }

        tspTimeline.getKeyFrames().add(new KeyFrame( // final keyframe: revert everything back to blue
                Duration.millis(5000),
                e -> drawBlueBoxes(lastResult)
        ));

        tspTimeline.play();
    }

    private void drawTSPStep(List<BlueBox> path, int currentIndex, BlueBox current, BlueBox prev) {
        if (currentImage == null || lastResult == null) return;

        double imgW = currentImage.getWidth();
        double imgH = currentImage.getHeight();
        double canvasW = overlayCanvas.getWidth();
        double canvasH = overlayCanvas.getHeight();
        double scale = Math.min(canvasW / imgW, canvasH / imgH);
        double offsetX = (canvasW - imgW * scale) / 2;
        double offsetY = (canvasH - imgH * scale) / 2;

        var gc = overlayCanvas.getGraphicsContext2D();
        clearOverlay();


        gc.setLineWidth(2);    // draw all boxes blue first
        gc.setFill(Color.BLUE);
        for (BlueBox b : lastResult.getBoxes()) {
            double x = offsetX + b.getMinX() * scale;
            double y = offsetY + b.getMinY() * scale;
            double w = b.getWidth() * scale;
            double h = b.getHeight() * scale;
            gc.setStroke(Color.BLUE);
            gc.strokeRect(x, y, w, h);
            gc.fillText("" + b.getRank(), x + 3, y + 12);
        }


        gc.setStroke(Color.GREEN);   // draw path lines up to current index
        gc.setLineWidth(2);
        for (int i = 1; i <= currentIndex; i++) {
            BlueBox a = path.get(i - 1);
            BlueBox b = path.get(i);
            double x1 = offsetX + ((a.getMinX() + a.getMaxX()) / 2.0) * scale;
            double y1 = offsetY + ((a.getMinY() + a.getMaxY()) / 2.0) * scale;
            double x2 = offsetX + ((b.getMinX() + b.getMaxX()) / 2.0) * scale;
            double y2 = offsetY + ((b.getMinY() + b.getMaxY()) / 2.0) * scale;
            gc.strokeLine(x1, y1, x2, y2);
        }


        double cx = offsetX + current.getMinX() * scale;   // highlight current box yellow
        double cy = offsetY + current.getMinY() * scale;
        double cw = current.getWidth() * scale;
        double ch = current.getHeight() * scale;
        gc.setStroke(Color.YELLOW);
        gc.setLineWidth(3);
        gc.strokeRect(cx, cy, cw, ch);
        gc.setFill(Color.YELLOW);
        gc.fillText("" + current.getRank(), cx + 3, cy + 12);
    }




    private void startTSPFromClick(double mouseX, double mouseY) {
        if (lastResult == null || lastResult.getBoxes().isEmpty()) return;

        double imageWidth = currentImage.getWidth();
        double imageHeight = currentImage.getHeight();
        double viewWidth = originalImageView.getBoundsInLocal().getWidth();
        double viewHeight = originalImageView.getBoundsInLocal().getHeight();
        double scale = Math.min(viewWidth / imageWidth, viewHeight / imageHeight);
        double offsetX = (viewWidth - imageWidth * scale) / 2;
        double offsetY = (viewHeight - imageHeight * scale) / 2;

        int imgX = (int) ((mouseX - offsetX) / scale);
        int imgY = (int) ((mouseY - offsetY) / scale);


        BlueBox clicked = null;    // find which box was clicked
        for (BlueBox b : lastResult.getBoxes()) {
            if (imgX >= b.getMinX() && imgX <= b.getMaxX()
                    && imgY >= b.getMinY() && imgY <= b.getMaxY()) {
                clicked = b;
                break;
            }
        }


        if (clicked == null) clicked = lastResult.getBoxes().get(0); // if they didn't click a box, just start from rank 1

        List<BlueBox> path = computeTSPPath(clicked, lastResult.getBoxes());
        animateTSPPath(path);
    }
}

