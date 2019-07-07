package net.kno3.util;

public interface IAdafruitIMU {
    double getHeading();
    void zeroHeading();
    double getPitch();
    void zeroPitch();
    double getRoll();
    void zeroRoll();
    void update();
}