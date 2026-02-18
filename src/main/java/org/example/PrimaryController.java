package org.example;

import finds.leafDetector;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import model.BlueBox;
import model.ColourSample;
import model.Detection;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

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
            if (currentImage == null) return;

            addSampleFromImageViewClick(e.getX(), e.getY());
        });

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

        for (BlueBox b : lastResult.getBoxes()) {
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

        ColourSample sample = new ColourSample(
                clicked.getHue(),
                clicked.getSaturation(),
                clicked.getBrightness()
        );

        samples.add(sample);

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
        double scaleX= canvasW/imgW;
        double scaleY= canvasH/imgH;
        for(BlueBox b: result.getBoxes()){
            double x = b.getMinX()*scaleX;
            double y = b.getMinY()*scaleY;
            double w= b.getWidth()*scaleX;
            double h= b.getHeight()*scaleY;
            gc.strokeRect(x, y, w, h);
            gc.fillText(""+b.getRank(),x+3,y+12);
        }

    }


  private void clearOverlay() {
        var gc = overlayCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, overlayCanvas.getWidth(), overlayCanvas.getHeight());
  }

}

