package net.kno3.season.roverruckus.solus.program.teleop;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import net.kno3.opMode.DriverControlledProgram;
import net.kno3.robot.Robot;
import net.kno3.season.roverruckus.solus.robot.Solus;

@TeleOp(name = "Solus Teleop")
public class SolusTeleop extends DriverControlledProgram {


    @Override
    protected Robot buildRobot() {
        return new Solus(this);
    }
}