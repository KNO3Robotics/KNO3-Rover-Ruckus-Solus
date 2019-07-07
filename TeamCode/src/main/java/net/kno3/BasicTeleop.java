package net.kno3;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;



@TeleOp(name = "Drive")
public class BasicTeleop extends OpMode {
    private DcMotor frontLeft, rearLeft, frontRight, rearRight;



    @Override
    public void init() {
        frontLeft = hardwareMap.dcMotor.get("frontleft");
        rearLeft = hardwareMap.dcMotor.get("rearleft");
        frontRight = hardwareMap.dcMotor.get("frontright");
        rearRight = hardwareMap.dcMotor.get("rearright");

        frontRight.setDirection(DcMotorSimple.Direction.REVERSE);
        rearLeft.setDirection(DcMotorSimple.Direction.REVERSE);
    }

    @Override
    public void loop() {
        double speed = gamepad1.right_trigger - gamepad1.left_trigger;
        double turn = gamepad1.left_stick_x;

        double leftspeed = ((1-Math.abs(turn))*speed+(1-Math.abs(speed))*turn+turn+speed) / 2;
        double rightspeed = ((1-Math.abs(turn))*speed-(1-Math.abs(speed))*turn-turn+speed) / 2;

        frontLeft.setPower(leftspeed);
        rearLeft.setPower(leftspeed);
        frontRight.setPower(rightspeed);
        rearRight.setPower(rightspeed);


    }

}
