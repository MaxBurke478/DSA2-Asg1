package model;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BlueBoxTest {

@Test
    void testInitialPixels() {
    BlueBox bb = new BlueBox(5,5);
    assertEquals(1,bb.getPixelCount());
}

@Test
    void testExpandForPixels() {
    BlueBox bb = new BlueBox(5,5);
    bb.expandForPixels(6,6);
    bb.expandForPixels(7,7);
    assertEquals(3,bb.getPixelCount()); //expanded twice
}

@Test
    void testUpdatedBounds(){
    BlueBox bb = new BlueBox(5,5);
    bb.expandForPixels(10,15);
    assertEquals(10,bb.getMaxX());
    assertEquals(15,bb.getMaxY());
}






}