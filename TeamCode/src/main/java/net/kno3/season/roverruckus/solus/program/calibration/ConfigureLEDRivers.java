package net.kno3.season.roverruckus.solus.program.calibration;

import android.graphics.Color;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import net.kno3.util.LEDRiver;

import static net.kno3.season.roverruckus.solus.robot.LEDSystem.GREEN_WHEEL;


@TeleOp(group = "calibration", name = "Configure LEDRivers")
public class ConfigureLEDRivers extends LinearOpMode {

    private LEDRiver aesthetic;
    private LEDRiver status;


    @Override
    public void runOpMode() throws InterruptedException {
        this.aesthetic = hardwareMap.get(LEDRiver.IMPL, "ledasthetics");
        aesthetic.setLEDMode(LEDRiver.LEDMode.RGB);
        aesthetic.setColorDepth(LEDRiver.ColorDepth.BIT_24);
        aesthetic.setLEDCount(41);

        this.status = hardwareMap.get(LEDRiver.IMPL, "ledstatus");
        status.setMode(LEDRiver.Mode.INDIVIDUAL);
        status.setLEDMode(LEDRiver.LEDMode.RGB);
        status.setColorDepth(LEDRiver.ColorDepth.BIT_24);
        status.setLEDCount(8);
        status.setBrightness(0.1);


        waitForStart();

        status.setMode(LEDRiver.Mode.INDIVIDUAL);
        for(int i = 0; i <= 7; i++) {
            status.setColor(i, Color.BLACK);
        }
        status.apply();

        Thread.sleep(100);

        status.save();



        aesthetic.setMode(LEDRiver.Mode.PATTERN);
        aesthetic.setPattern(GREEN_WHEEL);
        aesthetic.apply();

        Thread.sleep(100);

        aesthetic.save();

        Thread.sleep(5000);

        telemetry.addData("Done", "done");
        telemetry.update();
    }
}
