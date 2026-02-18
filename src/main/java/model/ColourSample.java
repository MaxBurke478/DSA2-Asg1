package model;

public class ColourSample {
    private final double hue;
    private final double saturation;
    private final double brightness;
    public ColourSample(double hue, double saturation, double brightness) {
        this.hue = hue;
        this.saturation = saturation;
        this.brightness = brightness;
    }

    public double getHue() {
        return hue;
    }

    public double getSaturation() {
        return saturation;
    }

    public double getBrightness() {
        return brightness;
    }
}
