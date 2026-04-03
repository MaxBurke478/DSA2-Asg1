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
            PixelReader pixelReader = input.getPixelReader();  //pixelreader so it can access every pixel colour

            if(pixelReader == null){
                return new Detection(new WritableImage(1,1), new ArrayList<>(),new HashMap<>(),new HashMap<>()); //if for some reason javaFx cant read image pixels, through simple result
            }

            boolean[] mask = new boolean[width*height];   //create a boolean mask array, if mask[id]= true, then its a leaf so white, if false then its not so black
            for(int y=0;y<height;y++){ //loop to read through pixels
                int rowStart = y*width;
                for(int x=0;x<width;x++){
                    Color pixel = pixelReader.getColor(x,y);
                    if(isLeafPixel(pixel,samples,hueTol,satMin,brightMin,brightMax)){ //if pixel found is a leaf colour, set the mask to true
                        mask[rowStart+x] = true;
                    }

                }
            }

            mask= applyOpening(mask,width,height,1);

            UnionFind uf = new UnionFind(width*height); //create a disjoint set structure
            for(int y=0;y<height;y++){  //loop through pixels
                int rowStart = y*width;
                for(int x=0;x<width;x++){
                    int id = rowStart + x;
                    if(!mask[id]) continue;  //if pixel is black, skip it
                    if(x>0 && mask[id-1]){
                        uf.union(id, id-1);// if  its white, union it with neighbouring pixels
                    }
                    if(y>0 && mask[id-width]){
                        uf.union(id, id-width); //connects left neighbours and top neighbours
                    }
                    if (useEight) { //using 8 neighbour, so connecting diagonally with pixels as well
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
            Map<Integer, Integer> pixelRootMap = new HashMap<>();
            Map<Integer, List<int[]>> componentPixels = new HashMap<>();
            Map<Integer, BlueBox> boxes = new HashMap<>(); //create a map that stores the root id of a component, and the blue box for that component(leaf)
            for(int y=0;y<height;y++){ // loop through again
                int rowStart = y*width;
                for(int x=0;x<width;x++){
                    int id = rowStart + x;//       for each white pixel
                    if(!mask[id]) continue;
                    int root = uf.find(id);    //    find its union root
                    BlueBox bb = boxes.get(root);   //get the bluebox for that root or create on if there isnt
                    if(bb == null){
                        bb = new BlueBox(x, y);
                        boxes.put(root, bb);
                    }
                    bb.expandForPixels(x,y);  //expand the bluebox to include this pixel
                    pixelRootMap.put(id, root);
                    componentPixels.computeIfAbsent(root, k -> new ArrayList<>()).add(new int[]{x, y});
                }
            }
            List<BlueBox> filtered = new ArrayList<>();  //filter noise since union find will detect anything like bushes and grass.
            for(BlueBox bb : boxes.values()){
                int size = bb.getPixelCount();
                if(size >= minCompSize && size <= maxCompSize){  //if the size of the box fits the values, add it to the array
                    filtered.add(bb);
                }
            }
            filtered.sort((a,b) -> Integer.compare(b.getPixelCount(), a.getPixelCount())); //sorts boxes from biggest to smallest
            for(int i =0;i<filtered.size();i++){
                filtered.get(i).setRank(i+1); //sets the rank of each cluster, starting at 1 , it would start at 0 if not adding the +1 line
            }
            WritableImage maskImage = new WritableImage(width,height); //create the black/white image
            for(int y=0;y<height;y++){
                int rowStart = y*width;
                for(int x=0;x<width;x++){
                    int id = rowStart + x;
                    maskImage.getPixelWriter().setColor(x,y,mask[id]? Color.WHITE : Color.BLACK);  //basically uses the pixel ids to build the image, leafs are white everything else is black
                }
            }

            return new Detection(maskImage,filtered,componentPixels,pixelRootMap);   //return this image, and the boxes for drawing rectangles
        }
        private boolean isLeafPixel(Color pixel, List<ColourSample> samples, double hueTol, double satMin, double brightMin,double brightMax){
            if(samples==null||samples.isEmpty()) return false;
            double h = pixel.getHue();
            double s = pixel.getSaturation();
            double b = pixel.getBrightness();
            if(b<brightMin|| b>brightMax) return false;


            for(ColourSample sample : samples){
                double d = hueDistance(h,sample.getHue());
                if(d<=hueTol) {
                    if(s >= satMin*.5) return true;
                }
                if(d<=hueTol*.5){
                    if(s>=0.05) return true;
                }
            }
            return false;
        }
        private double hueDistance(double h1, double h2) {
            double d = Math.abs(h1 - h2);
            return Math.min(d,360-d);
        }

        private boolean[] applyOpening(boolean[] mask, int width, int height, int radius){  //method to remove single pixels from the image
            boolean[] eroded = erode(mask,width,height,radius);
            return dilate(eroded,width,height,radius);
        }
        private boolean[] erode(boolean[] mask, int width, int height, int radius){
            boolean[] out = new boolean[width*height];
            for(int y = 0; y<height; y++){
                for(int x = 0; x<width; x++){
                    boolean keep = true;
                    outer:
                    for(int dy= -radius; dy <= radius; dy++){
                        for(int dx= -radius; dx <= radius; dx++){
                            int nx = x + dx, ny = y + dy;
                            if(nx<0||nx>=width|| ny<0 || ny>=height || !mask[ny*width+nx]){
                                keep = false;
                                break outer;
                            }
                        }
                    }
                    out[y*width+x]=keep;
                }
            }
            return out;
        }


        private boolean[] dilate(boolean[] mask, int width, int height, int radius){
            boolean[] out = new boolean[width * height];
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    outer:
                    for (int dy = -radius; dy <= radius; dy++) {
                        for (int dx = -radius; dx <= radius; dx++) {
                            int nx = x + dx, ny = y + dy;
                            if (nx >= 0 && nx < width && ny >= 0 && ny < height && mask[ny * width + nx]) {
                                out[y * width + x] = true;
                                break outer;
                            }
                        }
                    }
                }
            }
            return out;
        }
    }

