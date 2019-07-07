package net.kno3.season.roverruckus.solus.program.calibration;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.Servo;

import net.kno3.robot.RobotSettings;
import net.kno3.season.roverruckus.solus.robot.Solus;
import net.kno3.util.Threading;


@TeleOp(group = "calibration", name = "Calibrate Intake Flipper Right")
public class CalibrateIntakeFlipperRight extends LinearOpMode{
    @Override
    public void runOpMode() throws InterruptedException {
        telemetry.addData("1", "Press start");
        telemetry.update();
        waitForStart();

        RobotSettings settings = new RobotSettings("Solus");

        //ServoDefaults.resetAllServos(hardwareMap, settings);

        while(opModeIsActive()) {
            Servo intakeFlipright = hardwareMap.servo.get(Solus.INTAKE_FLIPPER_RIGHT_KEY);

            double upSetpoint = settings.getDouble("intake_flipper_right_up");
                while(opModeIsActive() && !gamepad1.a) {
                if(Math.abs(gamepad1.left_stick_x) > 0.05) {
                    upSetpoint += gamepad1.left_stick_x * Math.abs(gamepad1.left_stick_x) / 3000;
                }
                if(Math.abs(gamepad1.right_stick_x) > 0.05) {
                    upSetpoint += gamepad1.right_stick_x * Math.abs(gamepad1.right_stick_x) / 15000;
                }
                intakeFlipright.setPosition(upSetpoint);
                telemetry.addData("up Setpoint: ", upSetpoint);
                telemetry.addData("2", "Press A to continue");
                telemetry.update();
            }

            telemetry.addData("2", "Release A");
            telemetry.update();
            while(opModeIsActive() && gamepad1.a);

            double downSetpoint = settings.getDouble("intake_flipper_right_down");
            while(opModeIsActive() && !gamepad1.a) {
                if(Math.abs(gamepad1.left_stick_x) > 0.05) {
                    downSetpoint += gamepad1.left_stick_x * Math.abs(gamepad1.left_stick_x) / 3000;
                }
                if(Math.abs(gamepad1.right_stick_x) > 0.05) {
                    downSetpoint += gamepad1.right_stick_x * Math.abs(gamepad1.right_stick_x) / 15000;
                }
                intakeFlipright.setPosition(downSetpoint);
                telemetry.addData("Down Setpoint: ", downSetpoint);
                telemetry.addData("2", "Press A to continue");
                telemetry.update();
            }

            telemetry.addData("2", "Release A");
            telemetry.update();
            while(opModeIsActive() && gamepad1.a);

            double transferSetpoint = settings.getDouble("intake_flipper_right_transfer");
            while(opModeIsActive() && !gamepad1.a) {
                if(Math.abs(gamepad1.left_stick_x) > 0.05) {
                    transferSetpoint += gamepad1.left_stick_x * Math.abs(gamepad1.left_stick_x) / 3000;
                }
                if(Math.abs(gamepad1.right_stick_x) > 0.05) {
                    transferSetpoint += gamepad1.right_stick_x * Math.abs(gamepad1.right_stick_x) / 15000;
                }
                intakeFlipright.setPosition(transferSetpoint);
                telemetry.addData("Transfer Setpoint: ", transferSetpoint);
                telemetry.addData("2", "Press A to continue");
                telemetry.update();
            }

            telemetry.addData("2", "Release A");
            telemetry.update();
            while(opModeIsActive() && gamepad1.a);

            telemetry.addData("3", "press B to test");
            telemetry.update();
            while(opModeIsActive() && !gamepad1.b);

            intakeFlipright.setPosition(upSetpoint);
            Threading.delay(4);
            intakeFlipright.setPosition(downSetpoint);
            Threading.delay(4);
            intakeFlipright.setPosition(transferSetpoint);
            Threading.delay(4);

            settings.setDouble("intake_flipper_right_up", upSetpoint);
            settings.setDouble("intake_flipper_right_down", downSetpoint);
            settings.setDouble("intake_flipper_right_transfer", transferSetpoint);
            settings.save();

            telemetry.addData("5", "Saved! Press Y to try again.");
            telemetry.update();
            while(opModeIsActive() && !gamepad1.y);
        }
    }
}
