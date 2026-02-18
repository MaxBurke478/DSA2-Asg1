package model;

public class BlueBox {
    private int minX, minY, maxX, maxY;


    private int pixelCount;

    public BlueBox(int startX,int startY) {
        this.minX = startX;
        this.minY = startY;
        this.maxX = startX;
        this.maxY = startY;
        this.pixelCount = 0;
    }

    public void expandForPixels(int x,int y) {
        if(x<minX)minX = x;
        if(x>maxX)maxX = x;
        if(y<minY)minY = y;
        if(y>maxY)maxY = y;

        pixelCount++;
    }

    public int getMinX() {
        return minX;
    }

    public int getPixelCount() {
        return pixelCount;
    }

    public int getMinY() {
        return minY;
    }

    public int getMaxX() {
        return maxX;
    }

    public int getMaxY() {
        return maxY;
    }

    public int getHeight(){
        return(maxX-minX)+1;
    }
    public int getWidth(){
        return(maxY-minY)+1;
    }

    @Override
    public String toString() {
        return "BlueBox{" +
                "minX=" + minX +
                ", minY=" + minY +
                ", maxX=" + maxX +
                ", maxY=" + maxY +
                ", pixelCount=" + pixelCount +
                '}';
    }
}
