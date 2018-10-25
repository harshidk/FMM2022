package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;

@Autonomous(name="RoverAuto", group="7079")
public class RoverAuto extends LinearOpMode {
    /*
    1. Lower from lander
    2. Detach from lander
    3. Turn to see minerals
    4. Use openCV to knock off the gold mineral
    5. Turn to face direction of wall
    6. Drive to wall
    7. Turn at wall
    8. "Wall ride" using range sensor to go to the depot
    9. Drop off marker
    10. Turn around
    11. Go back to the crater for parking

    public void lowerRobot{
      1.  Measure and bring up the motor using the encoders to land
      2. Strafe right to move out hook
    }

    public void detectMinerals{
    1. Turn robot to see minerals
    2. Identify position
    3. Drive to position
    4. Knock off mineral
    5. Back up 18 inches
    }

    public void wallRide{
    1. Turn left to face wall
    2. Drive up to wall
    3. Turn left, and use the range sensor to keep a good distance away from the wall
    4. "Wall ride" till the depot and drop off marker
    5. Drive back till the crater and park in the crater

    }
     */

    // Update from 10/13/18
    private RoverRobot robot = null;
    RoverRobot.Operation operation= null;
    boolean isRedAlliance=false;
    boolean isStartFacingCrater=true;
    boolean isEnableCV=false;
    boolean isStartLatched=false;

    @Override
    public void runOpMode() {
        telemetry.addData("About to start", "Robot Initialization");
        telemetry.update();
        robot = new RoverRobot(new DriveMecanum());


        robot.init(hardwareMap, telemetry,true);
        telemetry.addData("Robot" , "Initialized");

     //   configMode();

        waitForStart();

        double maxTurningPower=.3;
        double maxDrivePower=.5;
        double degreesError=2.0;
        long timeoutMS=4000;

        operation = robot.getOperationRotateToHeading(-75, maxTurningPower, degreesError, timeoutMS);
        operation.run();
        robot.stop();



    }

    void configMode() {

        while (!gamepad1.start) {
            if (gamepad1.x) isRedAlliance= !isRedAlliance;
            if (gamepad1.y) isStartFacingCrater= !isStartFacingCrater;
            if (gamepad1.a) isEnableCV= !isEnableCV;
            if (gamepad1.b) isStartLatched= !isStartLatched;

            // just a test...  to be reworked...  can we do this at start?
            double liftPower=FaltechUtilities.clipDeadzone(gamepad1.right_trigger-gamepad1.left_trigger,.2);
            if (liftPower>0) robot.roverLift.mtrRoverLift.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER); // TODO: remove this...
            robot.roverLift.setPower(liftPower);

            telemetry.addData("Alliance", isRedAlliance?"Red":"Blue");
            telemetry.addData("Facing", isStartFacingCrater?"Crater":"Depot");
            telemetry.addData("CV", isEnableCV?"Enabled":"Disabled");
            telemetry.addData("Hanging", isStartLatched?"Latched":"On The Ground");
            telemetry.addData("ConfigMode" , "Press START to leave config mode.");
            telemetry.update();
            sleep(100);
        }
    }

}