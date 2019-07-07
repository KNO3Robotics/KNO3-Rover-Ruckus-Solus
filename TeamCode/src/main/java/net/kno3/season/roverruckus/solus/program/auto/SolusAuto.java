package net.kno3.season.roverruckus.solus.program.auto;

import android.util.Log;

import net.kno3.opMode.AutonomousProgram;
import net.kno3.robot.Robot;
import net.kno3.season.roverruckus.solus.robot.HopperSystem;
import net.kno3.season.roverruckus.solus.robot.IntakeSystem;
import net.kno3.season.roverruckus.solus.robot.Solus;
import net.kno3.season.roverruckus.solus.robot.DriveSystem;
import net.kno3.season.roverruckus.solus.robot.SolusPersist;
import net.kno3.util.SimpleColor;

public abstract class SolusAuto extends AutonomousProgram {
    public DriveSystem drive;
    public HopperSystem hopper;
    public IntakeSystem intake;

    public SolusAuto() {
        SolusPersist.lastWasAuto = false;
        Log.d("KNO3Encoder", "last was auto set to false (SolusAuto ctor)");
    }

    @Override
    protected Robot buildRobot() {
        Solus solus = new Solus(this);
        drive = solus.getSubSystem(DriveSystem.class);
        hopper = solus.getSubSystem(HopperSystem.class);
        intake = solus.getSubSystem(IntakeSystem.class);
        return solus;
    }

    @Override
    public void postInit() {
        drive.modeSpeed();
        SolusPersist.lastWasAuto = true;
        Log.d("KNO3Encoder", "last was auto set to true (SolusAuto postinit)");
    }
}
