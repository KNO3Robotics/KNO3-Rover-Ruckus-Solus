package net.kno3.season.roverruckus.solus.robot;


import android.util.Log;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.Servo;

import net.kno3.robot.Robot;
import net.kno3.robot.SubSystem;
import net.kno3.season.roverruckus.solus.program.teleop.PracticeTeleop;
import net.kno3.util.MotorPair;

public class IntakeSystem extends SubSystem {
    public DcMotor intakeSlides, collector;
    public Servo silverSort, goldSortLeft, goldSortRight, intakeFlipperLeft, intakeFlipperRight, markerDumper;
    public int intakeSlideZero;

    private HopperSystem hopperSystem;

    private boolean xLock = false, aLock = false, bLock = false, yLock = false, intakeLock = true, g2rblock = false;
    public boolean silverMode = true;
    private boolean rightBumperWasPressed = false;
    private boolean wasIn = true;
    private boolean hasMovedup = false;
    private int lastSlide = 0;
    private boolean neutralMode = false;

    private boolean intSlidePowerWasRunning = false;
    private int prevIntTarget = 0;

    private IntakeFlipperState intakeFlipperState = IntakeFlipperState.DOWN;
    private GoldSortState goldSortState;
    private SilverSortState silverSortState;

    private boolean wasIntakeRunning2ndController = false;



    public IntakeSystem(Robot robot) {
        super(robot);

        GoldSortState.OPEN.updateSettings(robot);
        GoldSortState.CLOSE.updateSettings(robot);
        GoldSortState.TRANSFER.updateSettings(robot);
        SilverSortState.OPEN.updateSettings(robot);
        SilverSortState.CLOSE.updateSettings(robot);
        SilverSortState.IDLE.updateSettings(robot);
        SilverSortState.TRANSFER.updateSettings(robot);
        IntakeFlipperState.UP.updateSettings(robot);
        IntakeFlipperState.DOWN.updateSettings(robot);
        IntakeFlipperState.TRANSFER.updateSettings(robot);
        MarkerDumpState.DOWN.updateSettings(robot);
        MarkerDumpState.UP.updateSettings(robot);
        MarkerDumpState.OUT.updateSettings(robot);

    }
    @Override
    public void init() {
        this.hopperSystem = robot.getSubSystem(HopperSystem.class);

        this.silverSort = hardwareMap().servo.get(Solus.SILVER_SORTER_KEY);
        this.goldSortLeft = hardwareMap().servo.get(Solus.GOLD_SORTER_LEFT_KEY);
        this.goldSortRight = hardwareMap().servo.get(Solus.GOLD_SORTER_RIGHT_KEY);
        this.intakeFlipperLeft = hardwareMap().servo.get(Solus.INTAKE_FLIPPER_LEFT_KEY);
        this.intakeFlipperRight = hardwareMap().servo.get(Solus.INTAKE_FLIPPER_RIGHT_KEY);
        this.markerDumper = hardwareMap().servo.get(Solus.TEAM_MARKER_DUMPER_KEY);

        intakeSlides = hardwareMap().dcMotor.get(Solus.INTAKE_SLIDE_MOTOR_KEY);
        collector = hardwareMap().dcMotor.get(Solus.COLLECTOR_KEY);
        collector.setDirection(DcMotorSimple.Direction.REVERSE);

        intakeSlides.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        intakeSlides.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        zeroIntakeSlideEnc();
        prevIntTarget = getIntakeSlideEncoder() + intakeSlideZero;
        telemetry().addData("intake slide zero", intakeSlideZero);
        telemetry().addData("intake slide pos", getIntakeSlideEncoder());

        setSilverSortState(SilverSortState.IDLE);
        setMarkerDumpState(MarkerDumpState.UP);
        setGoldSortState(GoldSortState.CLOSE);
        setIntakeFlipperState(IntakeFlipperState.DOWN);

    }

    @Override
    public void handle() {
        double intSlidePower = gamepad2().left_stick_y;


        if(intSlidePower >= 0.1 || intSlidePower <= -0.1) {
            intSlidePower *= .5;
        } else {
            intSlidePower *= 0;
        }

        if(!gamepad1().right_bumper) {
            if (hasMovedup && intSlidePower > 0 && (getIntakeSlideEncoder()) >= 100) {
                intSlidePower = 0;
            }
            if (intSlidePower < 0 && (getIntakeSlideEncoder()) <= -1200) {
                intSlidePower = 0;
            }
            if (getIntakeSlideEncoder() >= 100) {
                hasMovedup = true;
            }
        }

        if(!gamepad1().right_bumper && rightBumperWasPressed) {
            zeroIntakeSlideEnc();
            rightBumperWasPressed = false;
        }
        else if(gamepad1().right_bumper) {
            rightBumperWasPressed = true;
        }

        if(intSlidePower != 0 || intSlidePowerWasRunning) {
            prevIntTarget = (int)(intSlidePower*1250) + getIntakeSlideEncoder() + intakeSlideZero;
        }
        if(intSlidePower != 0)
            intSlidePowerWasRunning = true;
        else
            intSlidePowerWasRunning = false;
        intakeSlides.setTargetPosition(prevIntTarget);

        if(intSlidePower == 0) {
            intakeSlides.setPower(1);
        }
        else {
            intakeSlides.setPower(.7);
        }

        telemetry().addData("Intake Slides raw pos: ", intakeSlides.getCurrentPosition());
        telemetry().addData("intake slide zero", intakeSlideZero);
        telemetry().addData("Intake slide pos", getIntakeSlideEncoder());
        telemetry().addData("silver mode?", silverMode);

        goldSortState = GoldSortState.CLOSE;
        silverSortState = SilverSortState.CLOSE;


        if(gamepad2().right_bumper && !g2rblock) {
            hopperSystem.sameCrater = !hopperSystem.sameCrater;
            silverMode = !silverMode;
            g2rblock = true;
        }
        if(!gamepad2().right_bumper && g2rblock) {
            g2rblock = false;
        }

        //neutral mode toggle
        if(gamepad1().x && !xLock) {
            neutralMode = !neutralMode;
            xLock = true;
        }
        if(!gamepad1().x && xLock) {
            xLock = false;
        }

        if(!silverMode) {
            if(!neutralMode) {
                silverSortState = SilverSortState.CLOSE;
                goldSortState = GoldSortState.CLOSE;
            }
            else {
                silverSortState = SilverSortState.OPEN;
                goldSortState = GoldSortState.CLOSE;
            }
        }
        else {
            silverSortState = SilverSortState.OPEN;
            goldSortState = GoldSortState.OPEN;
        }


        //Silver/Gold Filter and Flipper Transfer Position
        if(gamepad2().b) {
            setSilverSortState(SilverSortState.TRANSFER);
            //setIntakeFlipperState(IntakeFlipperState.TRANSFER);
            intakeFlipperState = IntakeFlipperState.TRANSFER;
        }

        //Intake Flipper Up
        if(gamepad2().y) {
            //setIntakeFlipperState(IntakeFlipperState.UP);
            intakeFlipperState = IntakeFlipperState.UP;
        }

        //Intake Flipper Down
        if(gamepad2().a) {
            //setIntakeFlipperState(IntakeFlipperState.DOWN);
            intakeFlipperState = IntakeFlipperState.DOWN;
        }


        //Collector
        if (gamepad1().a) {
            if(silverMode)
                collect(-1);
            else {
                collect(-1);
            }
        } else if (gamepad1().b) {
            collect(1);
        } else if (gamepad1().y) {
            collect(0);
        } else if (wasIntakeRunning2ndController) {
            collect(0);
            wasIntakeRunning2ndController = false;
        }


        //Automatically closes Gold Gates when within 200 ticks
        if(getIntakeSlideEncoder() > -200) {
            setGoldSortState(GoldSortState.CLOSE);
            setSilverSortState(SilverSortState.OPEN);
        } else {
            setGoldSortState(goldSortState);
            setSilverSortState(silverSortState);
        }

        //Automatically flips Intake up if all the way in
        if(getIntakeSlideEncoder() >= -40 && lastSlide <= -40) {
            intakeFlipperState = IntakeFlipperState.UP;
            wasIn = true;
        }

        if(!gamepad2().a) {
            if (getIntakeSlideEncoder() <= -60 && wasIn) {
                intakeFlipperState = IntakeFlipperState.TRANSFER;
                wasIn = false;
            }
        }

        if(hopperSystem.hangMode) {
            setIntakeFlipperState(IntakeFlipperState.TRANSFER);
            setMarkerDumpState(MarkerDumpState.OUT);
        } else {
            setIntakeFlipperState(intakeFlipperState);
            setMarkerDumpState(MarkerDumpState.UP);
        }

        lastSlide = getIntakeSlideEncoder();
        /*
        setGoldSortState(goldSortState);
        setIntakeFlipperState(intakeFlipperState);
        */

    }

    @Override
    public void stop() {
        collector.setPower(0);
        intakeSlides.setPower(0);
    }


    public int getIntakeSlideEncoder() {
        return intakeSlides.getCurrentPosition() - intakeSlideZero;
    }

    public void zeroIntakeSlideEnc() {
        if(SolusPersist.lastWasAuto) {
            Log.d("KNO3Encoder", "last was auto, intake slide zero is " + SolusPersist.lastIntakeZero);
            this.intakeSlideZero = SolusPersist.lastIntakeZero;
        } else {
            this.intakeSlideZero = intakeSlides.getCurrentPosition();
            SolusPersist.lastIntakeZero = this.intakeSlideZero;
            Log.d("KNO3Encoder", "last was NOT auto, intake slide zero is " + this.intakeSlideZero);
        }
    }




    public void collect(double speed) {
        this.collector.setPower(speed);
    }

    public void setIntakeFlipperState(IntakeFlipperState state) {
        intakeFlipperLeft.setPosition(state.getLeft());
        intakeFlipperRight.setPosition(state.getRight());
    }

    public void setGoldSortState(GoldSortState state) {
        goldSortLeft.setPosition(state.getLeft());
        goldSortRight.setPosition(state.getRight());
    }

    public void setSilverSortState(SilverSortState state) {
        silverSort.setPosition(state.getPosition());
    }

    public void setMarkerDumpState(MarkerDumpState state) {
        markerDumper.setPosition(state.getPosition());
    }

    public enum SilverSortState {
        OPEN("silver_sort_open"),
        CLOSE("silver_sort_close"),
        IDLE("silver_sort_idle"),
        TRANSFER("silver_sort_transfer");

        private String key;
        private double pos;

        SilverSortState(String key) {
            this.key = key;
        }

        public double getPosition() {
            return pos;
        }

        public void updateSettings(Robot robot) {
            this.pos = robot.settings.getDouble(key);
        }
    }

    public enum GoldSortState {
        OPEN("gold_sort_left_open", "gold_sort_right_open"),
        CLOSE("gold_sort_left_close", "gold_sort_right_close"),
        TRANSFER("gold_sort_left_transfer", "gold_sort_right_transfer");

        private String left, right;
        private double leftP, rightP;

        GoldSortState(String left, String right) {
            this.left = left;
            this.right = right;
        }

        public double getLeft() {
            return leftP;
        }

        public double getRight() {
            return rightP;
        }

        public void updateSettings(Robot robot) {
            leftP = robot.settings.getDouble(left);
            rightP = robot.settings.getDouble(right);
        }
    }

    public enum IntakeFlipperState {
        UP("intake_flipper_left_up", "intake_flipper_right_up"),
        DOWN("intake_flipper_left_down", "intake_flipper_right_down"),
        TRANSFER("intake_flipper_left_transfer", "intake_flipper_right_transfer");

        private String left, right;
        private double leftP, rightP;

        IntakeFlipperState(String left, String right) {
            this.left = left;
            this.right = right;
        }

        public double getLeft() {
            return leftP;
        }

        public double getRight() {
            return rightP;
        }

        public void updateSettings(Robot robot) {
            leftP = robot.settings.getDouble(left);
            rightP = robot.settings.getDouble(right);
        }
    }

    public enum MarkerDumpState {
        UP("marker_dumper_up"),
        OUT("marker_dumper_out"),
        DOWN("marker_dumper_down");

        private String key;
        private double pos;

        MarkerDumpState(String key) {
            this.key = key;
        }

        public double getPosition() {
            return pos;
        }

        public void updateSettings(Robot robot) {
            this.pos = robot.settings.getDouble(key);
        }
    }
}