package net.kno3.season.roverruckus.solus.robot;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.DistanceSensor;
import com.qualcomm.robotcore.hardware.Servo;

import net.kno3.robot.Robot;
import net.kno3.robot.SubSystem;
import net.kno3.util.MotorPair;
import net.kno3.util.SynchronousPID;
import net.kno3.util.Threading;

import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;

public class HopperSystem extends SubSystem {
    public MotorPair hopperLift;
    public Servo hopperFlipper, hopperSweeper, hopperRoof, hangServo, land;

    private IntakeSystem intakeSystem;
    private LEDSystem ledSystem;


    private RoofPosition roofState = RoofPosition.OPEN;

    private boolean upLock = false;
    private boolean backLock = false;
    private boolean dpadLock = false;
    private boolean hopperFlipped = false;
    public boolean sameCrater = true;
    public boolean hangMode = false;
    private boolean hasMovedUp = false;
    private boolean startLock = false;
    private int lastHop = 0;


    public HopperSystem(Robot robot) {
        super(robot);

        FlipperPosition.GOLD.updateSettings(robot);
        FlipperPosition.SILVER.updateSettings(robot);
        FlipperPosition.DOWN.updateSettings(robot);
        FlipperPosition.IDLE.updateSettings(robot);
        FlipperPosition.HANG.updateSettings(robot);
        SweeperPosition.IN.updateSettings(robot);
        SweeperPosition.OUT.updateSettings(robot);
        SweeperPosition.IDLE.updateSettings(robot);
        RoofPosition.OPEN.updateSettings(robot);
        RoofPosition.SILVER.updateSettings(robot);
        RoofPosition.GOLD.updateSettings(robot);
        RoofPosition.IDLE.updateSettings(robot);
        HangPosition.OPEN.updateSettings(robot);
        HangPosition.CLOSE.updateSettings(robot);
        LandPosition.IDLE.updateSettings(robot);
        LandPosition.OPEN.updateSettings(robot);
        LandPosition.LOCKED.updateSettings(robot);

    }
    @Override
    public void init() {
        this.intakeSystem = robot.getSubSystem(IntakeSystem.class);
        this.ledSystem = robot.getSubSystem(LEDSystem.class);

        this.hopperFlipper = hardwareMap().servo.get(Solus.HOPPER_FLIPPER_KEY);
        this.hopperSweeper = hardwareMap().servo.get(Solus.HOPPER_SWEEPER_KEY);
        this.hopperRoof = hardwareMap().servo.get(Solus.HOPPER_ROOF_KEY);
        this.hangServo = hardwareMap().servo.get(Solus.HANG_SERVO_KEY);
        this.land = hardwareMap().servo.get(Solus.LAND_SERVO_KEY);
        DcMotor hopperLeft = hardwareMap().dcMotor.get(Solus.HOPPER_LIFT_LEFT_KEY);
        DcMotor hopperRight = hardwareMap().dcMotor.get(Solus.HOPPER_LIFT_RIGHT_KEY);

        hopperLeft.setDirection(DcMotorSimple.Direction.REVERSE);

        this.hopperLift = new MotorPair(hopperLeft, hopperRight);

        if(SolusPersist.lastWasAuto) {
            hopperLift.resetEncoder(SolusPersist.lastLiftZero);
        } else {
            SolusPersist.lastLiftZero = hopperLift.resetEncoder();
        }
        hopperLift.brakeMode();
        hopperLift.runMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);

        setFlipperState(FlipperPosition.DOWN);
        setRoofState(RoofPosition.OPEN);
        setSweeperState(SweeperPosition.IN);
        setHangState(HangPosition.CLOSE);
        setLandState(LandPosition.OPEN);

    }

    @Override
    public void handle() {

        double liftPow = -gamepad2().right_stick_y;
        liftPow *= Math.abs(liftPow);

        //sameCrater mode toggle
        if(gamepad1().dpad_up && !dpadLock) {
            sameCrater = !sameCrater;
            dpadLock = true;
        }
        if(!gamepad1().dpad_up && dpadLock) {
            dpadLock = false;
        }


        if(!gamepad1().dpad_down) {
            if (hasMovedUp && liftPow < 0 && (hopperLift.getEncoder()) >= 100) {
                liftPow = 0;
            }

            //Changes encoder limit if in Hang Mode
            if (!hangMode) {
                if(sameCrater) {
                    if (liftPow > 0 && (hopperLift.getEncoder()) <= -1430) { //adjust height for silver mode
                        liftPow = 0;
                    }
                }
                else {
                    if (liftPow > 0 && (hopperLift.getEncoder()) <= -2600) { //adjust height for gold mode
                        liftPow = 0;
                    }
                }
            } else {
                if (liftPow > 0 && hopperLift.getEncoder() <= -3050) {
                    liftPow = 0;
                }
                if (liftPow > 0)
                    liftPow *= .5;

                telemetry().addData("Same Crater?", sameCrater);
                telemetry().addData("encoder", hopperLift.getEncoder());
                telemetry().update();
            }

            if ((hopperLift.getEncoder()) <= 100) {
                hasMovedUp = true;
            }
        }


        //reset backslide encoder
        if(gamepad1().dpad_down) {
            backLock = true;
        } else if(backLock) {
            backLock = false;
            hopperLift.resetEncoder();
        }


        //toggle hangmode
        if(gamepad2().start && !startLock) {
            hangMode = !hangMode;
            startLock = true;
        }
        if(!gamepad2().start && startLock)
            startLock = false;



        //adjust hang mech
        if(gamepad2().left_bumper) {
            setHangState(HangPosition.OPEN);
        }
        else {
            setHangState(HangPosition.CLOSE);
        }



        if(!hangMode) {
            if (hopperLift.getEncoder() <= -1400 && !intakeSystem.silverMode && !sameCrater) {
                roofState = RoofPosition.GOLD;
            }
            else if (hopperLift.getEncoder() <= -1400 && intakeSystem.silverMode && !sameCrater) {
                roofState = RoofPosition.SILVER;
            }
            else {
                roofState = RoofPosition.OPEN;
            }
        }


        setRoofState(roofState);


        if(!hangMode) {
            if (hopperLift.getEncoder() <= -1350) {
                if(gamepad2().dpad_up && !upLock) {
                    hopperFlipped = !hopperFlipped;
                    upLock = true;


                    if(hopperFlipped) {
                        if(sameCrater) {
                            setFlipperState(FlipperPosition.SILVER);
                        }
                        else if(!sameCrater){
                            setFlipperState(FlipperPosition.GOLD);
                        }
                    }
                    else {
                        setFlipperState(FlipperPosition.DOWN);
                        ledSystem.triggerScore();
                    }
                }
                if(!gamepad2().dpad_up && upLock) {
                    upLock = false;
                }
            }
        }
        else {

            if(hopperLift.getEncoder() <= -1500){
                setFlipperState(FlipperPosition.HANG);
            }
            else {
                setFlipperState(FlipperPosition.DOWN);
            }
        }



        //sweeper position

        if(!intakeSystem.silverMode) {
            if (gamepad2().left_trigger >= .5 && !sameCrater) {
                setSweeperState(SweeperPosition.OUT);
            }
            if (gamepad2().left_trigger < .5) {
                setSweeperState(SweeperPosition.IN);
            }
        }
        else {
            if (gamepad2().left_trigger >= .5 && !sameCrater) {
                setSweeperState(SweeperPosition.IN);
            }
            if (gamepad2().left_trigger < .5) {
                setSweeperState(SweeperPosition.OUT);
            }
        }



        lastHop = hopperLift.getEncoder();



        hopperLift.setPower(liftPow);

    }

    @Override
    public void stop() {
        hopperLift.setPower(0);
    }

    public void setFlipperState(FlipperPosition flipperState) {
        this.hopperFlipper.setPosition(flipperState.getPosition());
    }

    public void setSweeperState(SweeperPosition sweeperState) {
        this.hopperSweeper.setPosition(sweeperState.getPosition());
    }

    public void setRoofState(RoofPosition roofState) {
        this.hopperRoof.setPosition(roofState.getPosition());
    }

    public void setHangState (HangPosition hangState) {
        this.hangServo.setPosition(hangState.getPosition());
    }

    public void setLandState(LandPosition landState) {
        this.land.setPosition(landState.getPosition());
    }


    public enum FlipperPosition {
        GOLD("hopper_flipper_gold"),
        SILVER("hopper_flipper_silver"),
        DOWN("hopper_flipper_down"),
        IDLE("hopper_flipper_idle"),
        HANG("hopper_flipper_hang");

        private String key;
        private double pos;

        FlipperPosition(String key) {
            this.key = key;
        }

        public double getPosition() {
            return pos;
        }

        public void updateSettings(Robot robot) {
            this.pos = robot.settings.getDouble(key);
        }
    }

    public enum SweeperPosition {
        IN("hopper_sweeper_in"),
        OUT("hopper_sweeper_out"),
        IDLE("hopper_sweeper_idle");

        private String key;
        private double pos;

        SweeperPosition(String key) {
            this.key = key;
        }

        public double getPosition() {
            return pos;
        }

        public void updateSettings(Robot robot) {
            this.pos = robot.settings.getDouble(key);
        }
    }

    public enum RoofPosition {
        OPEN("hopper_roof_open"),
        GOLD("hopper_roof_gold"),
        SILVER("hopper_roof_silver"),
        IDLE("hopper_roof_idle");

        private String key;
        private double pos;

        RoofPosition(String key) {
            this.key = key;
        }

        public double getPosition() {
            return pos;
        }

        public void updateSettings(Robot robot) {
            this.pos = robot.settings.getDouble(key);
        }
    }

    public enum HangPosition {
        OPEN("hang_open"),
        CLOSE("hang_close");

        private String key;
        private double pos;

        HangPosition(String key) {
            this.key = key;
        }

        public double getPosition() {
            return pos;
        }

        public void updateSettings(Robot robot) {
            this.pos = robot.settings.getDouble(key);
        }
    }

    public enum LandPosition {
        OPEN("land_open"),
        LOCKED("land_locked"),
        IDLE("land_idle");

        private String key;
        private double pos;

        LandPosition(String key) {
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
