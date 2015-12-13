package com.qualcomm.ftcrobotcontroller.opmodes;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.ColorSensor;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorController;
import com.qualcomm.robotcore.hardware.GyroSensor;
import com.qualcomm.robotcore.util.ElapsedTime;


public class RedRampAuto extends LinearOpMode {

    int ENCODER_TICKS_PER_REVOLUTION = 1120;
    int INCHES_PER_TILE = 24;
    double WHEEL_DIAMETER = 4;
    double WHEEL_CIRCUMFERENCE = 3.14159265359 * WHEEL_DIAMETER;
    double GEAR_RATIO = 11/8;
    double target = 0;
    int initial = 0;
    int initialtemp = 0;


    DcMotor motorLeft;
    DcMotor motorRight;
    DcMotor motorIntake;
    DcMotor motorWinch;
    DcMotor motorArm;
    GyroSensor gyro;
    ColorSensor color;
    DcMotorController driveController;
    DcMotorController encoderController;
    DcMotor leftEncoder;
    public ElapsedTime msecClock = new ElapsedTime();

    int timetowait = 100;



    boolean opModeEnd = false;

    @Override
    public void runOpMode() throws InterruptedException {

        motorRight = hardwareMap.dcMotor.get("rightMotor");
        motorLeft = hardwareMap.dcMotor.get("leftMotor");
        //motorIntake = hardwareMap.dcMotor.get("intake");
        //motorWinch = hardwareMap.dcMotor.get("winch");
        //motorArm = hardwareMap.dcMotor.get("arm");
        motorLeft.setDirection(DcMotor.Direction.REVERSE);
        encoderController = hardwareMap.dcMotorController.get("MC5");
        driveController = hardwareMap.dcMotorController.get("MC0");
        gyro = hardwareMap.gyroSensor.get("gyro");
        //color = hardwareMap.colorSensor.get("color");
        leftEncoder = hardwareMap.dcMotor.get("leftEncoder");

        encoderController.setMotorControllerDeviceMode(DcMotorController.DeviceMode.READ_ONLY);
        waitOneFullHardwareCycle();
        wait1MSec(500);
        telemetry.addData("", "ALL CLEAR");

        waitForStart();

        while(opModeIsActive() && !opModeEnd) {
            driveWithEncoders(1,.5);
            turnWithGyro(-35);
            driveWithEncoders(1.5, .5);
            turnWithGyro(-90);
            driveWithEncoders(1.5, 1);


            opModeEnd = true;
        }




    }


    public void setDriveSpeed(double left, double right) {
        motorLeft.setPower(left);
        motorRight.setPower(right);
    }

    public void waitForEncoders(double power)/* throws InterruptedException*/{
/*
        driveController.setMotorControllerDeviceMode(DcMotorController.DeviceMode.READ_ONLY);
        while(driveController.getMotorControllerDeviceMode() != DcMotorController.DeviceMode.READ_ONLY) {
            waitOneFullHardwareCycle();
        }

        wait1MSec(timetowait);
*/

        initial = leftEncoder.getCurrentPosition();

        while(Math.abs(leftEncoder.getCurrentPosition() - initial) < target && opModeIsActive()) {
            telemetry.addData("target", target);
            telemetry.addData("togo", Math.abs(leftEncoder.getCurrentPosition() - initial));
            telemetry.addData("enc", leftEncoder.getCurrentPosition());
            telemetry.addData("initial", initial);
            telemetry.addData("temp", initialtemp);
            initialtemp = leftEncoder.getCurrentPosition();
            setDriveSpeed(power, power);

        }

        initial = initialtemp;
/*
        driveController.setMotorControllerDeviceMode(DcMotorController.DeviceMode.WRITE_ONLY);
        while(driveController.getMotorControllerDeviceMode() != DcMotorController.DeviceMode.WRITE_ONLY) {
            waitOneFullHardwareCycle();
        }
        wait1MSec(timetowait);
        */
        setDriveSpeed(0, 0);

    }

    public void setTarget(double targetVal) {
        target = targetVal + initial;
    }

    public void wait1MSec(int msec) {
        msecClock.reset();
        while((msecClock.time() * 1000) < (msec + 1)) {

        }

    }



    public void driveWithEncoders(double tiles, double power) throws InterruptedException{
        double multiplier = (power/Math.abs(power));
        double targetPos;
        double ticks = (((tiles * INCHES_PER_TILE)/WHEEL_CIRCUMFERENCE) * ENCODER_TICKS_PER_REVOLUTION * GEAR_RATIO);
        targetPos = (ticks * multiplier) * .9;


        setTarget(targetPos);
        waitForEncoders(power);
    }

    public void turnWithGyro(int degreesToTurn) throws InterruptedException{
        double MOTOR_POWER = 0.5;
        double degreesTurned = 0;
        double initial = gyro.getRotation();

        while(Math.abs(degreesTurned) < Math.abs(degreesToTurn)) {
            sleep(20);

            double rotSpeed = (gyro.getRotation() - initial) * .02;
            degreesTurned += rotSpeed;

            motorLeft.setPower(-MOTOR_POWER);
            motorRight.setPower(MOTOR_POWER);
        }
        setDriveSpeed(0, 0);
    }

    public void checkForStop() {
        if(!opModeIsActive()) {
            FtcOpModeRegister.opModeManager.stopActiveOpMode();
        }
    }

}
