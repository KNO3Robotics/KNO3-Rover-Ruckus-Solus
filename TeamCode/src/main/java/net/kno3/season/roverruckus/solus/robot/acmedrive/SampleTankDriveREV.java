package net.kno3.season.roverruckus.solus.robot.acmedrive;

import com.acmerobotics.roadrunner.control.PIDCoefficients;
import com.qualcomm.hardware.bosch.BNO055IMU;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorImpl;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.PIDFCoefficients;

import net.kno3.season.roverruckus.solus.robot.DriveSystem;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

import static net.kno3.season.roverruckus.solus.program.roadrunnertests.DriveVelocityPIDTuner.MOTOR_PID;

/*
 * Simple tank drive hardware implementation for REV hardware. If your hardware configuration
 * satisfies the requirements, SampleTankDriveREVOptimized is highly recommended.
 */
public class SampleTankDriveREV extends SampleTankDriveBase {
    private DriveSystem driveSystem;

    public SampleTankDriveREV(DriveSystem driveSystem) {
        super();
        this.driveSystem = driveSystem;
    }

    @Override
    public PIDCoefficients getPIDCoefficients(DcMotor.RunMode runMode) {
        PIDFCoefficients coefficients = ((DcMotorEx) driveSystem.left.motor1).getPIDFCoefficients(runMode);
        return new PIDCoefficients(coefficients.p, coefficients.i, coefficients.d);
    }

    @Override
    public void setPIDCoefficients(DcMotor.RunMode runMode, PIDCoefficients coefficients) {
        ((DcMotorEx) driveSystem.left.motor1).setPIDFCoefficients(runMode, new PIDFCoefficients(coefficients.kP, coefficients.kI, coefficients.kD, 1));
        ((DcMotorEx) driveSystem.left.motor2).setPIDFCoefficients(runMode, new PIDFCoefficients(coefficients.kP, coefficients.kI, coefficients.kD, 1));
        ((DcMotorEx) driveSystem.right.motor1).setPIDFCoefficients(runMode, new PIDFCoefficients(coefficients.kP, coefficients.kI, coefficients.kD, 1));
        ((DcMotorEx) driveSystem.right.motor2).setPIDFCoefficients(runMode, new PIDFCoefficients(coefficients.kP, coefficients.kI, coefficients.kD, 1));
    }

    @NotNull
    @Override
    public List<Double> getWheelPositions() {
        double leftSum = 0, rightSum = 0;
        leftSum += driveSystem.left.motor1.getCurrentPosition();
        leftSum += driveSystem.left.motor2.getCurrentPosition();
        rightSum += driveSystem.right.motor1.getCurrentPosition();
        rightSum += driveSystem.right.motor2.getCurrentPosition();
        return Arrays.asList(DriveConstants.encoderTicksToInches((int) (leftSum / 2)), DriveConstants.encoderTicksToInches((int) (rightSum / 2)));

    }

    @Override
    public void setMotorPowers(double v, double v1) {
        driveSystem.set(v, v1);
    }

    @Override
    public double getExternalHeading() {
        return driveSystem.imu.getHeading();
    }
}
