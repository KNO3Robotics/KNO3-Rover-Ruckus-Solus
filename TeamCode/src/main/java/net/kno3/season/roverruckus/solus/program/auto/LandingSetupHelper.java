package net.kno3.season.roverruckus.solus.program.auto;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

import net.kno3.season.roverruckus.solus.program.dogecvtests.CroppingGoldDetector;
import net.kno3.season.roverruckus.solus.program.dogecvtests.KNO3Vision;
import net.kno3.season.roverruckus.solus.robot.HopperSystem;
import net.kno3.util.AutoTransitioner;
import net.kno3.util.SimpleColor;
import net.kno3.util.Threading;

@Autonomous(name = "RUN FIRST !!!!! Landing setup helper")
public class LandingSetupHelper extends SolusAuto{
    @Override
    public void postInit() {
        super.postInit();

        hopper.setLandState(HopperSystem.LandPosition.LOCKED);
    }

    @Override
    public void main() {

    }
}
