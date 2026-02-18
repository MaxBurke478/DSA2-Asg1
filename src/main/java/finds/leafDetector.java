package finds;

import finds.UnionFind;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import model.BlueBox;
import model.ColourSample;
import model.Detection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

    public class leafDetector {


        public Detection detect(Image input, List<ColourSample> samples, double hueTol, double satMin, double brightMin, double brightMax, int minCompSize, int maxCompSize, boolean useEight)
        {
            int width = (int) input.getWidth();
            int height = (int) input.getHeight();
            PixelReader pixelReader = input.getPixelReader();

            if(pixelReader == null){
                return new Detection(new WritableImage(1,1), new ArrayList<>());
            }

            boolean[] mask = new boolean[width*height];
            for(int y=0;y<height;y++){
                int rowStart = y*width;
                for(int x=0;x<width;x++){
                    Color pixel = pixelReader.getColor(x,y);
                    if(isLeafPixel(pixel,samples,hueTol,satMin,brightMin,brightMax)){
                        mask[rowStart+x] = true;
                    }

                }
            }
            UnionFind uf = new UnionFind(width*height);
            for(int y=0;y<height;y++){
                int rowStart = y*width;
                for(int x=0;x<width;x++){
                    int id = rowStart + x;
                    if(!mask[id]) continue;
                    if(x>0 && mask[id-1]){
                        uf.union(id, id-1);
                    }
                    if(y>0 && mask[id-width]){
                        uf.union(id, id-width);
                    }
                    if (useEight) {
                        // up-left
                        if (x > 0 && y > 0 && mask[id - width - 1]) {
                            uf.union(id, id - width - 1);
                        }
                        // up-right
                        if (x < width - 1 && y > 0 && mask[id - width + 1]) {
                            uf.union(id, id - width + 1);
                        }
                    }
                }
            }
            Map<Integer, BlueBox> boxes = new HashMap<>();
            for(int y=0;y<height;y++){
                int rowStart = y*width;
                for(int x=0;x<width;x++){
                    int id = rowStart + x;
                    if(!mask[id]) continue;
                    int root = uf.find(id);
                    BlueBox bb = boxes.get(root);
                    if(bb == null){
                        bb = new BlueBox(x, y);
                        boxes.put(root, bb);
                    }
                    bb.expandForPixels(x,y);
                }
            }
            List<BlueBox> filtered = new ArrayList<>();
            for(BlueBox bb : boxes.values()){
                int size = bb.getPixelCount();
                if(size >= minCompSize && size <= maxCompSize){
                    filtered.add(bb);
                }
            }
            filtered.sort((a,b) -> Integer.compare(b.getPixelCount(), a.getPixelCount()));
            for(int i =0;i<filtered.size();i++){
                filtered.get(i).setRank(i+1);
            }
            WritableImage maskImage = new WritableImage(width,height);
            for(int y=0;y<height;y++){
                int rowStart = y*width;
                for(int x=0;x<width;x++){
                    int id = rowStart + x;
                    maskImage.getPixelWriter().setColor(x,y,mask[id]? Color.WHITE : Color.BLACK);
                }
            }
            return new Detection(maskImage,filtered);
        }
        private boolean isLeafPixel(Color pixel, List<ColourSample> samples, double hueTol, double satMin, double brightMin,double brightMax){
            if(samples==null||samples.isEmpty()) return false;
            double h = pixel.getHue();
            double s = pixel.getSaturation();
            double b = pixel.getBrightness();

            if(s<satMin) return false;
            if(b<brightMin|| b>brightMax) return false;
            for(ColourSample sample : samples){
                double d = hueDistance(h,sample.getHue());
                if(d<=hueTol) {
                    return true;
                }
            }
            return false;
        }
        private double hueDistance(double h1, double h2) {
            double d = Math.abs(h1 - h2);
            return Math.min(d,360-d);
        }
    }

