/* Copyright (c) 2017 FIRST. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted (subject to the limitations in the disclaimer below) provided that
 * the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list
 * of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice, this
 * list of conditions and the following disclaimer in the documentation and/or
 * other materials provided with the distribution.
 *
 * Neither the name of FIRST nor the names of its contributors may be used to endorse or
 * promote products derived from this software without specific prior written permission.
 *
 * NO EXPRESS OR IMPLIED LICENSES TO ANY PARTY'S PATENT RIGHTS ARE GRANTED BY THIS
 * LICENSE. THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package opmodes;

import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.util.Range;

/*
 * This file contains an example of an iterative (Non-Linear) "OpMode".
 * An OpMode is a 'program' that runs in either the autonomous or the teleop period of an FTC match.
 * The names of OpModes appear on the menu of the FTC Driver Station.
 * When a selection is made from the menu, the corresponding OpMode
 * class is instantiated on the Robot Controller and executed.
 * This particular OpMode just executes a basic Tank Drive Teleop for a two wheeled robot
 * It includes all the skeletal structure that all iterative OpModes contain.
 *
 * Use Android Studio to Copy this Class, and Paste it into your team's code folder with a new name.
 * Remove or comment out the @Disabled line to add this OpMode to the Driver Station OpMode list
 */

@TeleOp(name="Playing w/ Telemetry :)", group="Software Class")
// @Disabled
public class TelemetryPractice extends OpMode
{
    // Declare OpMode members.
    private ElapsedTime runtime = new ElapsedTime();
    private DcMotor leftDrive = null;
    private DcMotor rightDrive = null;

    Servo claw;
    private final double POSITION_ONE = 0.3;
    private final double POSITION_TWO = 0.5;


    private Servo servo;
    final double OPEN_SERVO = 0.5;
    final double CLOSE_SERVO = 0;
    private double curr_servo_pos;

    private Servo.Direction servoDirection; // choices are FORWARDS or BACKWARDS

    private enum ServoState {
        SERVO_OPEN_STATE,
        SERVO_CLOSED_STATE
    }
    private ServoState servoState;

    /*
     * Code to run ONCE when the driver hits INIT
     */
    @Override
    public void init() {
        // telemetry.addData("Status", "Initialized Start");

        // Initialize the hardware variables. Note that the strings used here as parameters
        // to 'get' must correspond to the names assigned during the robot configuration
        // step (using the FTC Robot Controller app on the phone).
        leftDrive  = hardwareMap.get(DcMotor.class, "left_drive");
        rightDrive = hardwareMap.get(DcMotor.class, "right_drive");

        servo = hardwareMap.get(Servo.class, "Servo");
        claw = hardwareMap.servo.get("claw");

        // To drive forward, most robots need the motor on one side to be reversed, because the axles point in opposite directions.
        // Pushing the left stick forward MUST make robot go forward. So adjust these two lines based on your first test drive.
        // Note: The settings here assume direct drive on left and right wheels. Gear Reduction or 90 Deg drives may require direction flips
        leftDrive.setDirection(DcMotor.Direction.REVERSE); // changed again
        rightDrive.setDirection(DcMotor.Direction.FORWARD);

        // Tell the driver that initialization is complete.
        // telemetry.addData("Status", "Initialized End");
    }

    /*
     * Code to run REPEATEDLY after the driver hits INIT, but before they hit PLAY
     */
    @Override
    public void init_loop() {
    }

    /*
     * Code to run ONCE when the driver hits PLAY
     */
    @Override
    public void start() {
        // telemetry.addData("Status", " Called start method");
        servoClose();
        servoDirection = servo.getDirection();
        telemetry.addData("Servo Direction: ",  servoDirection);
        runtime.reset();
        if (gamepad2.a) {
            claw.setPosition(POSITION_ONE);
            telemetry.addData("Status","Claw position at 0.3");
        } else if (gamepad2.b) {
            claw.setPosition(POSITION_TWO);
            telemetry.addData("Status","Claw Position at 0.5");
        } else if (gamepad2.y) {
            claw.setPosition(gamepad1.left_stick_y);
            telemetry.addData("Status","Claw Position aligned with left Y-stick");
        }
    }
    /*
     * Code to run REPEATEDLY after the driver hits PLAY but before they hit STOP
     */
    @Override
    public void loop() {
        // Setup a variable for each drive wheel to save power level for telemetry
        double leftPower;
        double rightPower;

        // Choose to drive using either Tank Mode, or POV Mode
        // Comment out the method that's not used.  The default below is POV.

        // POV Mode uses left stick to go forward, and right stick to turn.
        // - This uses basic math to combine motions and is easier to drive straight.
        double drive = -gamepad1.left_stick_y;
        double extra1 = -gamepad1.left_stick_x;
        telemetry.addData("Status", "Left Stick X: " + extra1);
        telemetry.addData("Status", "Right Stick Y: " + drive);
        double turn  =  gamepad1.right_stick_x;
        double extra2 = gamepad1.right_stick_y;
        telemetry.addData("Status", "Right Stick X: " + turn);
        telemetry.addData("Status", "Right Stick Y: " + extra2);
        leftPower    = Range.clip(drive + turn, -1.0, 1.0) ;
        rightPower   = Range.clip(drive - turn, -1.0, 1.0) ;

        // Tank Mode uses one stick to control each wheel.
        // This requires no math, but it is hard to drive forward slowly and keep straight.
        // leftPower  = -gamepad1.left_stick_y ;
        // rightPower = -gamepad1.right_stick_x;

        // if x button on the gamepad is pressed, then change direction and move servo
        if (gamepad1.x) {
           if (servoDirection == Servo.Direction.FORWARD) {
                servoDirection = Servo.Direction.REVERSE;
            } else {
            servoDirection = Servo.Direction.FORWARD;
            }
            servo.setDirection(servoDirection);
           if (servoState == ServoState.SERVO_CLOSED_STATE) {
               servoOpen();
           } else {
               servoClose();
           }
        }


        // Send calculated power to wheels
        leftDrive.setPower(leftPower);
        rightDrive.setPower(rightPower);

        // Show the elapsed game time and wheel power.
        // telemetry.addData("Status", "Run Time: " + runtime.toString());
        // telemetry.addData("Motors", "left (%.2f), right (%.2f)", leftPower, rightPower);
        // telemetry.addData("Motors", "left (%.2f)", leftPower);
        // telemetry.addData("Motors", "right (%.2f)", rightPower);
    }

    /*
     * Code to run ONCE after the driver hits STOP
     */

    public void servoClose() {
        servo.setPosition(CLOSE_SERVO);
        servoState = ServoState.SERVO_CLOSED_STATE;
        telemetry.addData("Servo Close: Position ",  CLOSE_SERVO);

    }
    public void servoOpen() {
        servo.setPosition(OPEN_SERVO);
        servoState = ServoState.SERVO_OPEN_STATE;
        telemetry.addData("Servo Open: Position ", OPEN_SERVO);

    }
    @Override
    public void stop() {
    }

}

