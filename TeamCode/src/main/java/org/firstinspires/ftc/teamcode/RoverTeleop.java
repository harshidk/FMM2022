package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

public class RoverTeleop extends OpMode{
    @TeleOp(name="RoverMecanumTeleop", group="7079")
    public static class RoverMecanumTeleop extends RoverTeleop {
        @Override public void init() {
            robot = new RoverRobot(new DriveMecanum());
            driverMode=0;
            super.init();
        }
    }

    @TeleOp(name="RoverMecanumTeleopMode1", group="7079")
    public static class RoverMecanumTeleopMode1 extends RoverTeleop {
        @Override public void init() {
            robot = new RoverRobot(new DriveMecanum());
            driverMode=1;
            super.init();
        }
    }

    @TeleOp(name="RoverTankTeleop", group="7079")
    public static class RoverTankTeleop extends RoverTeleop {
        @Override public void init() {
            robot = new RoverRobot(new DriveTank());
            super.init();
        }
    }

    RoverRobot robot = null;
    RoverRobot.Operation operation= null;
    int driverMode =0;

    @Override
    public void init() {
        robot.init(hardwareMap, telemetry, false);
        telemetry.addData("Robot" , "Initialized");
        telemetry.update();
    }

    public void start() {
        // do anything we need on button start
    }

    public void stop() {
        robot.stop();
    };

    @Override
    public void loop() {
        doOperations();
        if (operation==null) {
            doDrive();
            doArm();
            doClaw();
            doArmExtender();
            doRobotLift();
            doTeamMarker();
        }
        telemetry.update();
    }

    void doDrive() {
        double leftX = FaltechUtilities.scaleSpeedFunction(gamepad1.left_stick_x);
        double leftY = FaltechUtilities.scaleSpeedFunction(-gamepad1.left_stick_y);
        double rightX = FaltechUtilities.scaleSpeedFunction(gamepad1.right_stick_x);
        double rightY = FaltechUtilities.scaleSpeedFunction(-gamepad1.right_stick_y);

        double forward, rotate, sideways;
        if (driverMode==0) {
            forward=leftY;
            rotate=rightX;
            sideways=leftX;
        } else {
            forward=rightY;
            rotate=leftX;
            sideways=rightX;
        }

        telemetry.addData("Drive:" , "F="+forward+" S="+sideways+" R="+rotate);
        robot.drive.driveFRS(forward,rotate,sideways);
    }


    double currentPosition = 0.0;


    public void doArm() {
        double armSpeed= FaltechUtilities.clipDeadzone(gamepad1.right_trigger-gamepad1.left_trigger,.2);
        double targetPosDegrees=armSpeed>=0 ? 120 : 0;
        robot.roverCollector.setPositionDegrees(targetPosDegrees);
        robot.roverCollector.setSpeed(armSpeed);
        telemetry.addData("Arm Speed =", armSpeed);
        telemetry.update();
    }

    float clawLeft = 0, clawRight = 0;
    boolean clawArmLeft = false, clawArmRight = false;

    public void doClaw() {
         if (!clawArmLeft && gamepad1.x){
             clawArmLeft = true;
             clawLeft = 1 - clawLeft;
             robot.roverCollector.leftClaw.setPosition(clawLeft);
         }
         else if (!gamepad1.x){
             clawArmLeft = false;
         }

        if (!clawArmRight && gamepad1.b){
            clawArmRight = true;
            clawRight = 1 - clawRight;
            robot.roverCollector.rightClaw.setPosition(clawRight);
        }
        else if (!gamepad1.b){
            clawArmRight = false;
        }
    }

    public void doArmExtender() {
        double armPower;
        if (gamepad1.left_bumper) armPower=.8;
        else if (gamepad1.right_bumper) armPower=-0.4;
        else armPower=0.0;
        robot.roverCollector.mtrArmExtender.setPower(armPower);
    }

    public void doRobotLift() {
        double liftPower = FaltechUtilities.clipDeadzone(gamepad2.right_trigger - gamepad2.left_trigger, .1);
        robot.roverLift.setPower(liftPower);
    }

    public void doTeamMarker(){
         if (gamepad2.y)
             robot.teamMarker.setPosition(1);
         else if (gamepad2.x)
             robot.teamMarker.setPosition(0);
    }

    public void doOperations() {
        if (operation!=null) {      // if we have an existing operation
            if (!operation.loop())  // loop the operation
                operation=null;     // and throw it away if it finished
        } else {
            double maxTurningPower=.3;
            double maxDrivePower=.5;
            double degreesError=2.0;
            long timeoutMS=4000;

            if (gamepad1.dpad_right) operation = robot.getOperationRotateToHeading(45, maxTurningPower, degreesError, timeoutMS);
            else if (gamepad1.dpad_left) operation = robot.getOperationRotateToHeading(-45, maxTurningPower, degreesError, timeoutMS);
            else if (gamepad1.dpad_up) {
                if (operation!=null) {operation.done(); operation=null;}
                robot.resetRelativeAngleToZero();
            }
        }
    }

}




