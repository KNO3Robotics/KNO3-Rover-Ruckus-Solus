package net.kno3.season.roverruckus.solus.program.auto;


import com.disnodeteam.dogecv.CameraViewDisplay;
import com.disnodeteam.dogecv.detectors.roverrukus.SamplingOrderDetector;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.hardware.DcMotor;

import net.kno3.season.roverruckus.solus.program.dogecvtests.CroppingGoldDetector;
import net.kno3.season.roverruckus.solus.program.dogecvtests.KNO3Vision;
import net.kno3.season.roverruckus.solus.robot.HopperSystem;
import net.kno3.season.roverruckus.solus.robot.IntakeSystem;
import net.kno3.season.roverruckus.solus.robot.SolusPersist;
import net.kno3.util.AutoTransitioner;
import net.kno3.util.Threading;

import static net.kno3.season.roverruckus.solus.robot.HopperSystem.*;
import static net.kno3.season.roverruckus.solus.robot.IntakeSystem.MarkerDumpState.DOWN;
import static net.kno3.season.roverruckus.solus.robot.IntakeSystem.MarkerDumpState.UP;

@Autonomous(name = "TestAuto")
public class TestAuto extends SolusAuto {
    private KNO3Vision vision;
    private SamplingOrderDetector detector;



    @Override
    public void postInit() {
        super.postInit();
        vision = new KNO3Vision(this, "Webcam 1");

        Threading.async(() -> {
            while(!vision.getDetector().isFound()) {
                if(isStopRequested()) {
                    return;
                }
                Thread.yield();
            }
            while(!isStarted() && !isStopRequested()) {
                CroppingGoldDetector.GoldLocation location = vision.getDetector().getLastOrder();
                telemetry.addData("current location", vision.getDetector().getCurrentOrder().toString());
                telemetry.addData("last location", vision.getDetector().getLastOrder().toString());
                telemetry.addData("gold x", vision.getDetector().getGoldRectX());
                telemetry.update();
                Thread.yield();
            }
        });

        AutoTransitioner.transitionOnStop(this, "Solus Teleop");

    }

    @Override
    public void main(){
        vision.getDetector().disable();
        CroppingGoldDetector.GoldLocation location = vision.getDetector().getLastOrder();
        intake.intakeSlides.setMode(DcMotor.RunMode.RUN_TO_POSITION);

        drive.driveForAlt(15);
        drive.stop();
        waitFor(.5);

        switch(location) {
            case LEFT:
                drive.turnPIDbad(310);
                drive.stop();
                waitFor(.5);

                //extend and retract intake
                intake.collector.setPower(-.5);
                intake.intakeSlides.setTargetPosition(intake.getIntakeSlideEncoder()-750);
                intake.intakeSlides.setPower(.5);
                waitFor(2);
                intake.intakeSlides.setTargetPosition(intake.getIntakeSlideEncoder()+750);
                intake.intakeSlides.setPower(-.5);
                waitFor(1.5);

                intake.collector.setPower(0);

                intake.intakeSlides.setPower(0);

                drive.turnPIDbad(50);
                drive.stop();
                waitFor(.5);
                break;

            case RIGHT:
                drive.turnPIDbad(50);
                drive.stop();
                waitFor(.5);

                //extend and retract intake
                intake.collector.setPower(-.5);
                intake.intakeSlides.setTargetPosition(intake.getIntakeSlideEncoder()-750);
                intake.intakeSlides.setPower(.5);
                waitFor(2);
                intake.intakeSlides.setTargetPosition(intake.getIntakeSlideEncoder()+750);
                intake.intakeSlides.setPower(-.5);
                waitFor(1.5);
                intake.collector.setPower(0);
                intake.intakeSlides.setPower(0);


                drive.turnPIDbad(310);
                drive.stop();
                waitFor(.5);
                break;

            case CENTER:
            case UNKNOWN:
            default:

                //extend and retract intake
                intake.collector.setPower(-.5);
                intake.intakeSlides.setTargetPosition(intake.getIntakeSlideEncoder()-450);
                intake.intakeSlides.setPower(.5);
                waitFor(2);
                intake.intakeSlides.setTargetPosition(intake.getIntakeSlideEncoder()+450);
                intake.intakeSlides.setPower(-.5);
                waitFor(1.5);

                intake.collector.setPower(0);
                intake.intakeSlides.setPower(0);

                break;
        }

        intake.setIntakeFlipperState(IntakeSystem.IntakeFlipperState.TRANSFER);

        intake.intakeSlides.setTargetPosition(intake.getIntakeSlideEncoder()-1100);
        intake.intakeSlides.setPower(.7);
        waitFor(2);
        intake.setMarkerDumpState(IntakeSystem.MarkerDumpState.DOWN);
        waitFor(.5);
        intake.setMarkerDumpState(IntakeSystem.MarkerDumpState.UP);
        intake.intakeSlides.setTargetPosition(intake.getIntakeSlideEncoder()+1100);
        intake.intakeSlides.setPower(-.7);
        waitFor(1.5);

        drive.turnPIDbad(270);
        drive.stop();
        waitFor(.5);

        drive.driveForAlt(36);
        drive.stop();
        waitFor(.5);

        drive.turnPIDbad(345);
        drive.stop();
        waitFor(.5);

        intake.setIntakeFlipperState(IntakeSystem.IntakeFlipperState.TRANSFER);

        intake.intakeSlides.setTargetPosition(intake.getIntakeSlideEncoder()-700);
        intake.intakeSlides.setPower(.5);
        waitFor(2);
        intake.intakeSlides.setPower(0);




        //SolusPersist.lastWasAuto = true;
        //SolusPersist.lastIntakeZero = intake.intakeSlideZero;




    }
}

