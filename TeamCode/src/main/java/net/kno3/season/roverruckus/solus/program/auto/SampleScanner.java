package net.kno3.season.roverruckus.solus.program.auto;

import android.graphics.Bitmap;
import android.util.Log;

import com.disnodeteam.dogecv.detectors.roverrukus.SamplingOrderDetector;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.vuforia.Image;
import com.vuforia.PIXEL_FORMAT;
import com.vuforia.Vuforia;

import net.kno3.util.ClosableVuforiaLocalizer;
import net.kno3.util.FileUtil;
import net.kno3.util.SimpleColor;
import net.kno3.util.Threading;

import org.firstinspires.ftc.robotcontroller.internal.FtcOpModeRegister;
import org.firstinspires.ftc.robotcore.external.ClassFactory;
import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.firstinspires.ftc.robotcore.external.matrices.OpenGLMatrix;
import org.firstinspires.ftc.robotcore.external.navigation.RelicRecoveryVuMark;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaLocalizer;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaTrackable;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaTrackables;
import org.firstinspires.ftc.robotcore.internal.system.SystemProperties;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.util.Date;

/**
 * Created by robotics on 10/29/2017.
 */

//TODO obselete?? delete??

public class SampleScanner{
    private ClosableVuforiaLocalizer vuforia;
    private VuforiaTrackables relicTrackables;
    private VuforiaTrackable relicTemplate;
    public SamplingOrderDetector detector = new SamplingOrderDetector();

    private boolean initialized;
    private RelicRecoveryVuMark lastValid = RelicRecoveryVuMark.UNKNOWN;
    private SamplingOrderDetector.GoldLocation lastLocation = null;

    private Mat lastMat = null;

    private WebcamName webcam;

    public SampleScanner(HardwareMap hardwareMap, Telemetry telemetry) {
        Threading.async(() -> {
            telemetry.addData("vuin", "Initializing Vuforia...");
            telemetry.update();
            webcam = hardwareMap.get(WebcamName.class, "Webcam");
            int cameraMonitorViewId = hardwareMap.appContext.getResources().getIdentifier("cameraMonitorViewId", "id", hardwareMap.appContext.getPackageName());
            ClosableVuforiaLocalizer.Parameters parameters = new ClosableVuforiaLocalizer.Parameters(cameraMonitorViewId);
            parameters.vuforiaLicenseKey = "AfzmxGH/////AAABmdC69b4uIEwCqJNwM+TdI010xRC/5E9djsrlrKjP3yF2XEa8VZDPJcmaYe/O9tt8nxJ9EXHmDd8HyYKj1n50EMKu2yI8Nnp2tBLDzqtv5RUvNMApagW/IyO+HcRo9yP1Ump8oKJs7Tot6D6hlLrifuyIe74SItZvK2zNK65rNdWebXT3OCNfb0UgiaE8n0LDGOuxCUIrUU0Pjm0U/PDeB5PgOk6TDkXJsB5cWH+N8zadc1dMbs78lC2fFTlmTxQSDgR+t3uzCJsfXiFmKFGwxYr7jLlUEI+YZhW8QAaCNCPAo1WgHPRRIgt7eMcfCztp4VlFy69vph1CqGbwlc4WZwc3ZgQtMTR90rs6/Sd9lGob";
            parameters.cameraName = webcam;
            this.vuforia = new ClosableVuforiaLocalizer(parameters);
            Vuforia.setFrameFormat(PIXEL_FORMAT.RGB565, true);
            this.vuforia.setFrameQueueCapacity(1);
            relicTrackables = this.vuforia.loadTrackablesFromAsset("RelicVuMark");
            relicTemplate = relicTrackables.get(0);
            relicTemplate.setName("relicVuMarkTemplate"); // can help in debugging; otherwise not necessary
            relicTrackables.activate();
            initialized = true;
            Log.i("vuf", "Vuforia Initialized");
            telemetry.addData("vuin", () -> "Vuforia Ready!");
            telemetry.update();
        });
    }

    public boolean isInitialized() {
        return initialized;
    }

    public RelicRecoveryVuMark scan() {
        RelicRecoveryVuMark vuMark = RelicRecoveryVuMark.from(relicTemplate);
        if(vuMark != RelicRecoveryVuMark.UNKNOWN)
            lastValid = vuMark;
        return vuMark;
    }

    public SamplingOrderDetector.GoldLocation scanSample() {
        Mat mat = readFrame();
        lastMat = mat;
        detector.process(lastMat);
        if(detector.getLastOrder() != null || detector.getLastOrder() != SamplingOrderDetector.GoldLocation.UNKNOWN) {
            lastLocation = detector.getLastOrder();
            return detector.getLastOrder();
        }
        else return null;
    }


    public SamplingOrderDetector.GoldLocation getLastLocation() {
        return lastLocation;
    }

    public Mat readFrame() {
        //Log.i("vif", "Read frame 1");
        VuforiaLocalizer.CloseableFrame frame;
        Image rgb = null;

        //Log.i("vif", "Read frame 2");

        try {
            // grab the last frame pushed onto the queue
            frame = vuforia.getFrameQueue().take();
        } catch (InterruptedException e) {
            //Log.d("vif", "Problem taking frame off Vuforia queue");
            e.printStackTrace();
            return null;
        }

        //Log.i("vif", "Read frame 3");

        // basically get the number of formats for this frame
        long numImages = frame.getNumImages();


        //Log.i("vif", "Read frame 4");

        // set rgb object if one of the formats is RGB565
        for(int i = 0; i < numImages; i++) {
            if(frame.getImage(i).getFormat() == PIXEL_FORMAT.RGB565) {
                rgb = frame.getImage(i);
                break;
            }
        }


        //Log.i("vif", "Read frame 5");

        if(rgb == null) {
            Log.i("vif", "Image format not found");
            return null;
        }


        //Log.i("vif", "Read frame 6");

        // create a new bitmap and copy the byte buffer returned by rgb.getPixels() to it
        Bitmap bm = Bitmap.createBitmap(rgb.getWidth(), rgb.getHeight(), Bitmap.Config.RGB_565);
        bm.copyPixelsFromBuffer(rgb.getPixels());


        //Log.i("vif", "Read frame 7");

        // construct an OpenCV mat from the bitmap using Utils.bitmapToMat()
        Mat mat = new Mat(bm.getWidth(), bm.getHeight(), CvType.CV_8UC4);
        Utils.bitmapToMat(bm, mat);


        //Log.i("vif", "Read frame 8");

        // convert to BGR before returning
        Imgproc.cvtColor(mat, mat, Imgproc.COLOR_RGB2BGR);

        frame.close();


        //Log.i("vif", "Read frame 9");

        Log.d("vif", "Frame closed");

        return mat;
    }


    public RelicRecoveryVuMark getLastValid() {
        return lastValid;
    }

    public void close() {
        vuforia.close();
        Threading.async(() -> {
            try {
                Log.i("vif", "Writing Frame to file");
                Core.rotate(lastMat, lastMat, Core.ROTATE_90_COUNTERCLOCKWISE);
                Core.flip(lastMat, lastMat, 1);
                Imgproc.cvtColor(lastMat, lastMat, Imgproc.COLOR_BGR2RGBA);
                Bitmap bmp = Bitmap.createBitmap(lastMat.cols(), lastMat.rows(), Bitmap.Config.ARGB_8888);
                Utils.matToBitmap(lastMat, bmp);
                FileUtil.writeImage(FtcOpModeRegister.opModeManager.getActiveOpMode().getClass().getSimpleName() + "-" + System.currentTimeMillis(),
                        bmp);
                Log.i("vif", "Written Frame to file");
            } catch (Exception ex) {
                Log.e("vif", "image writer error", ex);
            }
        });
    }

}