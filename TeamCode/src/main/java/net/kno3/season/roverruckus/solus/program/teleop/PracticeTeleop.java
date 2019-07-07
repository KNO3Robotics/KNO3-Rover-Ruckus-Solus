package net.kno3.season.roverruckus.solus.program.teleop;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import net.kno3.opMode.DriverControlledProgram;
import net.kno3.robot.Robot;
import net.kno3.season.roverruckus.solus.robot.Solus;

@TeleOp(name = "Prac Teleop Neutral")
public class PracticeTeleop extends DriverControlledProgram {

    public PracticeTeleop() {
        disableTimer();
    }

    @Override
    protected Robot buildRobot() {
        return new Solus(this);
    }
}