package org.firstinspires.ftc.teamcode;

// Simple autonomous program that drives bot forward until end of period
// or touch sensor is hit. If touched, backs up a bit and turns 90 degrees
// right and keeps going. Demonstrates obstacle avoidance and use of the
// REV Hub's built in IMU in place of a gyro. Also uses gamepad1 buttons to
// simulate touch sensor press and supports left as well as right turn.
//
// Also uses PID controller to drive in a straight line when not
// avoiding an obstacle.
//
// Use PID controller to manage motor power during 90 degree turn to reduce
// overshoot.



import com.qualcomm.hardware.bosch.BNO055IMU;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.AxesOrder;
import org.firstinspires.ftc.robotcore.external.navigation.AxesReference;
import org.firstinspires.ftc.robotcore.external.navigation.Orientation;

@Autonomous(name="RoverTestPID2", group="7079")
//@Disabled
public class RoverTestPID2 extends LinearOpMode
{
    DcMotor                 leftMotor;
    DcMotor                 rightMotor;
  //  DigitalChannel          touch;
    BNO055IMU               imu;
    Orientation             lastAngles = new Orientation();
    double                  globalAngle, power = .30, correction;
    boolean                 aButton, bButton, touched;
    PIDController           pidRotate, pidDrive;

    // called when init button is  pressed.
    @Override
    public void runOpMode() {
        leftMotor = hardwareMap.dcMotor.get("mtrFL");

        rightMotor = hardwareMap.dcMotor.get("mtrFR");

        leftMotor.setDirection(DcMotor.Direction.FORWARD);
        rightMotor.setDirection(DcMotor.Direction.REVERSE);

        leftMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        rightMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        // get a reference to REV Touch sensor.
     //   touch = hardwareMap.digitalChannel.get("touch_sensor");

        BNO055IMU.Parameters parameters = new BNO055IMU.Parameters();

        parameters.mode                = BNO055IMU.SensorMode.IMU;
        parameters.angleUnit           = BNO055IMU.AngleUnit.DEGREES;
        parameters.accelUnit           = BNO055IMU.AccelUnit.METERS_PERSEC_PERSEC;
        parameters.loggingEnabled      = false;

        // Retrieve and initialize the IMU. We expect the IMU to be attached to an I2C port
        // on a Core Device Interface Module, configured to be a sensor of type "AdaFruit IMU",
        // and named "imu".
        imu = hardwareMap.get(BNO055IMU.class, "imu");

        imu.initialize(parameters);

        // Set PID proportional value to start reducing power at about 50 degrees of rotation.
        pidRotate = new PIDController(.01, 0, 0);

        // Set PID proportional value to produce non-zero correction value when robot veers off
        // straight line. P value controls how sensitive the correction is.
        pidDrive = new PIDController(.05, 0, 0);

        telemetry.addData("Mode", "calibrating...");
        telemetry.update();

        // make sure the imu gyro is calibrated before continuing.
        while (!isStopRequested() && !imu.isGyroCalibrated())
        {
            sleep(50);
            idle();
        }

        telemetry.addData("Mode", "waiting for start");
        telemetry.addData("imu calib status", imu.getCalibrationStatus().toString());
        telemetry.update();

        resetRelativeAngleToZero();

        // wait for start button.

        waitForStart();

        telemetry.addData("Mode", "running");
        telemetry.update();

        sleep(1000);

        // Set up parameters for driving in a straight line.
        pidDrive.setSetpoint(0);
        pidDrive.setOutputRange(0, power);
        pidDrive.setInputRange(0.0, 90.0);
        pidDrive.enable();

        // drive until end of period.

            // Use PID with imu input to drive in a straight line.
        //    correction = pidDrive.performPID(getAngle());

            telemetry.addData("1 imu heading", lastAngles.firstAngle);
            telemetry.addData("2 global heading", globalAngle);
            telemetry.addData("3 correction", correction);
            telemetry.update();

            // set power levels.
//            leftMotor.setPower(-power + correction);
//            rightMotor.setPower(-power);

            // We record the sensor values because we will test them in more than
            // one place with time passing between those places. See the lesson on
            // Timing Considerations to know why.

            aButton = gamepad1.a;
            bButton = gamepad1.b;
      //      touched = touch.getState();

        boolean once=true;
        while (once) {
            once=false;

            if (!opModeIsActive()) break;

            rotate(-45.0, power);
            sleep(2000);

//            if (!opModeIsActive()) break;
//
//            rotate(45.0, power);
//            sleep(2000);
//
//            if (!opModeIsActive()) break;
//
//            rotate(-90.0, power);
//            sleep(2000);
//
//            if (!opModeIsActive()) break;
//
//            rotate(180.0, power);
//            sleep(2000);
//
//            if (!opModeIsActive()) break;

        }

        // turn the motors off.
        rightMotor.setPower(0);
        leftMotor.setPower(0);
    }

    /**
     * Resets the cumulative angle tracking to zero.
     */
    private void resetRelativeAngleToZero()
    {
        lastAngles = imu.getAngularOrientation(AxesReference.INTRINSIC, AxesOrder.ZYX, AngleUnit.DEGREES);

        globalAngle = 0;
    }

    /**
     * Get current cumulative angle rotation from last reset.
     * @return Angle in degrees. + = left, - = right from zero point.
     */
    private double getRelativeAngle()
    {
        // We experimentally determined the Z axis is the axis we want to use for heading angle.
        // We have to process the angle because the imu works in euler angles so the Z axis is
        // returned as 0 to +180 or 0 to -180 rolling back to -179 or +179 when rotation passes
        // 180 degrees. We detect this transition and track the total cumulative angle of rotation.

        Orientation angles = imu.getAngularOrientation(AxesReference.INTRINSIC, AxesOrder.ZYX, AngleUnit.DEGREES);

        double deltaAngle = angles.firstAngle - lastAngles.firstAngle;

        // remap -180 to +180
        if (deltaAngle < -180) deltaAngle += 360;
        else if (deltaAngle > 180) deltaAngle -= 360;

        globalAngle += deltaAngle;

        return deltaAngle;
    }

    private double getCurrentAngle()
    {

        Orientation angles = imu.getAngularOrientation(AxesReference.INTRINSIC, AxesOrder.ZYX, AngleUnit.DEGREES);

        double currentAngle = angles.firstAngle;

        return currentAngle;
    }




    /**
     * Rotate left or right the number of degrees. Does not support turning more than 180 degrees.
     * @param degrees Degrees to turn, + is left - is right
     */
    private void rotate(double degrees, double maxPower)
    {
        if (degrees==0.0) return ;
        // restart imu angle tracking.
        resetRelativeAngleToZero();


        // start pid controller. PID controller will monitor the turn angle with respect to the
        // target angle and reduce power as we approach the target angle with a minimum of 20%.
        // This is to prevent the robots momentum from overshooting the turn after we turn off the
        // power. The PID controller reports onTarget() = true when the difference between turn
        // angle and target angle is within 2% of target (tolerance). This helps prevent overshoot.
        // The minimum power is determined by testing and must enough to prevent motor stall and
        // complete the turn. Note: if the gap between the starting power and the stall (minimum)
        // power is small, overshoot may still occur. Overshoot is dependant on the motor and
        // gearing configuration, starting power, weight of the robot and the on target tolerance.

        pidRotate.reset();
        pidRotate.setSetpoint(degrees);
        pidRotate.setInputRange(0, 90);
        pidRotate.setOutputRange(.20, maxPower);
        pidRotate.setTolerance(3);
        pidRotate.enable();

        // getAngle() returns + when rotating counter clockwise (left) and - when rotating
        // clockwise (right).

        // rotate until turn is completed.

        double direction= (degrees>=0.0) ? 1.0 : -1.0;
/*
        // On right turn we have to get off zero first.
        while (opModeIsActive() && getRelativeAngle() == 0)
        {
            double minMovePower=maxPower/5;
            leftMotor.setPower(direction *minMovePower);
            rightMotor.setPower(-direction * minMovePower);
            sleep(100);
            telemetry.addData("In zero Angle 1 imu heading", lastAngles.firstAngle);
            telemetry.addData("2 global heading", getRelativeAngle());
            telemetry.addData("3 correction", correction);
        }
*/
        do
        {
            double relativeAngle = getRelativeAngle();
            double currentAngle = getCurrentAngle();
            double drivepower = pidRotate.performPID(relativeAngle); // power will be - on right turn.
            leftMotor.setPower(direction *drivepower);
            rightMotor.setPower(-direction * drivepower);
            telemetry.addData(" last first angle", lastAngles.firstAngle);
            telemetry.addData("relativeAngle", relativeAngle);
            telemetry.addData("currentAngle", currentAngle);
            telemetry.addData("Power is", drivepower);
            telemetry.addData(" correction", correction);
            dumpPID("pidR",pidRotate);
            telemetry.update();

        } while (opModeIsActive() && !pidRotate.onTarget());

        // turn the motors off.
        rightMotor.setPower(0);
        leftMotor.setPower(0);

        // wait for rotation to stop.
        sleep(500);

    }

    void dumpPID (String caption, PIDController pid) {
        telemetry.addData(caption, "P="+pid.getP()+", I="+pid.getI()+", D="+pid.getD()+", E="+pid.getError()+", SP="+pid.getSetpoint());

    }
}
