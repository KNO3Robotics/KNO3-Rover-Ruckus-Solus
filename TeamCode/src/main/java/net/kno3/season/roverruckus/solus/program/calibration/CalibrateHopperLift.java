package net.kno3.season.roverruckus.solus.program.calibration;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;


@TeleOp(group = "calibration", name = "calibrate hopper lift")
public class CalibrateHopperLift extends LinearOpMode {
    @Override
    public void runOpMode() throws InterruptedException {
        telemetry.addData("1", "Press start");
        telemetry.update();

        waitForStart();

        DcMotor hopperMotorLeft = hardwareMap.dcMotor.get("Hopper Motor Left");
        hopperMotorLeft.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        hopperMotorLeft.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        DcMotor hopperMotorRight = hardwareMap.dcMotor.get("Hopper Motor Right");
        hopperMotorRight.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        hopperMotorRight.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        while(opModeIsActive()) {
            telemetry.addData("encoder pos", hopperMotorLeft.getCurrentPosition());
            telemetry.addData("encoder pos", hopperMotorRight.getCurrentPosition());
            telemetry.update();
        }

    }
}
