package net.kno3.season.roverruckus.solus.program.roadrunnertests;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.canvas.Canvas;
import com.acmerobotics.dashboard.telemetry.TelemetryPacket;
import com.acmerobotics.roadrunner.Pose2d;
import com.acmerobotics.roadrunner.trajectory.Trajectory;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

import net.kno3.season.roverruckus.solus.program.auto.SolusAuto;
import net.kno3.season.roverruckus.solus.robot.acmedrive.DashboardUtil;

/*
 * This is a simple routine to test turning capabilities. If this is consistently overshooting or
 * undershooting by a significant amount, re-run TrackWidthCalibrationOpMode.
 */
@Autonomous
public class TurnTestOpMode extends SolusAuto {

    private FtcDashboard dashboard;
    private Trajectory trajectory;

    @Override
    public void postInit() {
        super.postInit();
        dashboard = FtcDashboard.getInstance();

        trajectory = drive.roadrunner.trajectoryBuilder()
                .turnTo(Math.PI / 2)
                .build();
    }


    @Override
    public void main() {
        if (isStopRequested()) return;

        drive.roadrunner.followTrajectory(trajectory);
        while (!isStopRequested() && drive.roadrunner.isFollowingTrajectory()) {
            Pose2d currentPose = drive.roadrunner.getPoseEstimate();

            TelemetryPacket packet = new TelemetryPacket();
            Canvas fieldOverlay = packet.fieldOverlay();

            packet.put("x", currentPose.getX());
            packet.put("y", currentPose.getY());
            packet.put("heading", currentPose.getHeading());

            fieldOverlay.setStrokeWidth(4);
            fieldOverlay.setStroke("green");
            DashboardUtil.drawSampledTrajectory(fieldOverlay, trajectory);

            fieldOverlay.setFill("blue");
            fieldOverlay.fillCircle(currentPose.getX(), currentPose.getY(), 3);

            dashboard.sendTelemetryPacket(packet);

            drive.roadrunner.update();
        }
    }
}
