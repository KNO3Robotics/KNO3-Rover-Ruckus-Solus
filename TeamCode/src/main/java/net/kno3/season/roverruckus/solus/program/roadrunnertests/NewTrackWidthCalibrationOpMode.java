package net.kno3.season.roverruckus.solus.program.roadrunnertests;

import com.acmerobotics.dashboard.config.Config;
import com.acmerobotics.roadrunner.Pose2d;
import com.acmerobotics.roadrunner.drive.Kinematics;
import com.acmerobotics.roadrunner.drive.MecanumDrive;
import com.acmerobotics.roadrunner.drive.TankDrive;
import com.acmerobotics.roadrunner.profile.MotionProfile;
import com.acmerobotics.roadrunner.profile.MotionProfileGenerator;
import com.acmerobotics.roadrunner.profile.MotionState;
import com.acmerobotics.roadrunner.util.Angle;
import com.acmerobotics.roadrunner.util.NanoClock;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.util.MovingStatistics;

import net.kno3.season.roverruckus.solus.program.auto.SolusAuto;
import net.kno3.season.roverruckus.solus.robot.acmedrive.DriveConstants;

import org.firstinspires.ftc.robotcore.internal.system.Misc;

/*
 * Similar to the deprecated TrackWidthCalibrationOpMode, this routine attempts to automagically
 * determine the drive track width. The basic idea is to use a motion profile to rotate the robot
 * a certain circumferential distance and compare it to the angle swept out (as measured by the
 * IMU). For robustness, this procedure is repeated a few times, and the final track width is
 * averaged over those runs.
 */
@Config
@Autonomous
public class NewTrackWidthCalibrationOpMode extends SolusAuto {
    public static int CIRCUMFERENTIAL_DISTANCE = 500;
    public static int NUM_TRIALS = 5;

    @Override
    public void postInit() {
        super.postInit();
        // it's important that the IMU/gyro/heading sensor is not part of the localization
        drive.roadrunner.setLocalizer(new TankDrive.TankLocalizer(drive.roadrunner, false));

        telemetry.log().add("Press play to begin the track width calibration routine");
        telemetry.log().add("Make sure your robot has enough clearance to turn smoothly");
        telemetry.log().add("Additionally, set the drive's track width to 1");
        telemetry.update();
    }


    @Override
    public void main() {
        if (isStopRequested()) return;

        telemetry.log().clear();
        telemetry.log().add("Running...");
        telemetry.update();

        MovingStatistics trackWidthStats = new MovingStatistics(NUM_TRIALS);
        NanoClock clock = NanoClock.system();
        for (int i = 0; i < NUM_TRIALS; i++) {
            drive.roadrunner.setPoseEstimate(new Pose2d());

            double headingAccumulator = 0;
            double lastHeading = drive.roadrunner.getExternalHeading();
            MotionProfile profile = MotionProfileGenerator.generateSimpleMotionProfile(
                    new MotionState(0, 0, 0, 0),
                    new MotionState(CIRCUMFERENTIAL_DISTANCE, 0, 0, 0),
                    DriveConstants.BASE_CONSTRAINTS.maximumVelocity,
                    DriveConstants.BASE_CONSTRAINTS.maximumAcceleration
            );

            double startTime = clock.seconds();
            while (!isStopRequested()) {
                double elapsedTime = clock.seconds() - startTime;
                if (elapsedTime > profile.duration()) {
                    drive.roadrunner.setVelocity(new Pose2d(0, 0, 0));
                    break;
                }

                double heading = drive.roadrunner.getExternalHeading();
                // accumulator is an unwrapped version of the heading
                headingAccumulator += Angle.norm(heading - lastHeading);
                lastHeading = heading;

                MotionState state = profile.get(elapsedTime);
                drive.roadrunner.setVelocity(new Pose2d(0, 0,
                        Kinematics.calculateMotorFeedforward(
                                state.getV(),
                                state.getA(),
                                DriveConstants.kV,
                                DriveConstants.kA,
                                DriveConstants.kStatic
                        )
                ));

                drive.roadrunner.updatePoseEstimate();
            }

            double trackWidth = drive.roadrunner.getPoseEstimate().getHeading() / headingAccumulator;
            trackWidthStats.add(trackWidth);

            sleep(1000);
        }

        telemetry.log().clear();
        telemetry.log().add("Calibration complete");
        telemetry.log().add(Misc.formatInvariant("Effective track width = %.2f (SE = %.3f)",
                trackWidthStats.getMean(), trackWidthStats.getStandardDeviation() / Math.sqrt(NUM_TRIALS)));
        telemetry.update();

        while (!isStopRequested()) {
            idle();
        }
    }
}
