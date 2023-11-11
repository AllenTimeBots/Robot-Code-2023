// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.SwerveControllerCommand;
import edu.wpi.first.wpilibj2.command.button.JoystickButton;
import edu.wpi.first.wpilibj2.command.button.Trigger;
import frc.robot.commands.ResetGyro;
import frc.robot.commands.teleop.MoveArm;
import frc.robot.commands.teleop.MoveClaw;
import frc.robot.commands.teleop.TeleopJoystickDrive;
import frc.robot.constants.Constants;
import frc.robot.constants.Constants.AutoConstants;
import frc.robot.subsystems.arm.Arm;
import frc.robot.subsystems.arm.ArmWithVel;
import frc.robot.subsystems.claw.Claw;
import frc.robot.subsystems.drivetrain.Drivetrain;
import pabeles.concurrency.ConcurrencyOps.Reset;

import java.util.List;

import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.math.controller.ProfiledPIDController;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.trajectory.Trajectory;
import edu.wpi.first.math.trajectory.TrajectoryConfig;
import edu.wpi.first.wpilibj.Joystick;

/**
 * This class is where the bulk of the robot should be declared. Since Command-based is a
 * "declarative" paradigm, very little robot logic should actually be handled in the {@link Robot}
 * periodic methods (other than the scheduler calls). Instead, the structure of the robot (including
 * subsystems, commands, and trigger mappings) should be declared here.
 */

public class RobotContainer {

  //Joystic
  Joystick driveJoystick = new Joystick(0);
  Joystick armJoystick = new Joystick(1);

  // Subsystems
  Drivetrain drivetrain = new Drivetrain();
  Arm arm = new Arm(armJoystick);
  Claw claw = new Claw();
  //ArmWithVel armWithVel = new ArmWithVel()

  //Comands
  ResetGyro resetGyro = new ResetGyro(drivetrain);

  //Teleop Commands
  TeleopJoystickDrive drive = new TeleopJoystickDrive(drivetrain, driveJoystick, null, true);
  MoveArm armWhenMove = new MoveArm(arm, armJoystick);
  //MoveArmToPoint armMoveThing = new MoveArmToPoint(arm, new Vector2(30, 0));
  MoveClaw closeClaw = new MoveClaw(claw, false);
  MoveClaw openClaw = new MoveClaw(claw, true);
  //MoveArmWithVel moveArmWithVel = new MoveArmWithVel(armWithVel, armJoystick);
  //Auto commands


  public RobotContainer() {
    drivetrain.setDefaultCommand(drive);
    arm.setDefaultCommand(armWhenMove);
    //armWithVel.setDefaultCommand(moveArmWithVel);
    // Configure the trigger bindings
    configureBindings();
  }

  /**
   * Use this method to define your trigger->command mappings. Triggers can be created via the
   * {@link Trigger#Trigger(java.util.function.BooleanSupplier)} constructor with an arbitrary
   * predicate, or via the named factories in {@link
   * edu.wpi.first.wpilibj2.command.button.CommandGenericHID}'s subclasses for {@link
   * CommandXboxController Xbox}/{@link edu.wpi.first.wpilibj2.command.button.CommandPS4Controller
   * PS4} controllers or {@link edu.wpi.first.wpilibj2.command.button.CommandJoystick Flight
   * joysticks}.
   */
  private void configureBindings() {
    //new JoystickButton(driveJoystick, 9).whileTrue();
    new JoystickButton(driveJoystick, 8).onTrue(resetGyro);
    //new JoystickButton(armJoystick, 9).whileTrue(armMoveThing);
    new JoystickButton(armJoystick, 3).toggleOnTrue(openClaw);
    new JoystickButton(armJoystick, 4).toggleOnTrue(closeClaw);
    // Schedule `ExampleCommand` when `exampleCondition` changes to `true`

    // Schedule `exampleMethodCommand` when the Xbox controller's B button is pressed,
    // cancelling on release.
  }

  /**
   * Use this to pass the autonomous command to the main {@link Robot} class.
   *
   * @return the command to run in autonomous
   */

  private Command DropConeAndTaxi() {
    System.out.println("DropConeAndTaxi");
    ProfiledPIDController thetaController = new ProfiledPIDController(Constants.AutoConstants.kPThetaController,
    0, 0, Constants.AutoConstants.kThetaControllerConstraints);
    PIDController xController = new PIDController(Constants.AutoConstants.kPXController, 0, 0);
    PIDController yController = new PIDController(Constants.AutoConstants.kPYController, 0, 0);
    // Create config for trajectory
    thetaController.enableContinuousInput(-Math.PI, Math.PI);

    TrajectoryConfig config = new TrajectoryConfig(AutoConstants.MAX_Speed_MetersPerSecond,
            AutoConstants.MAX_Acceleration_MetersPerSecondSquared)
                    // Add kinematics to ensure max speed is actually obeyed
                    .setKinematics(drivetrain.kinematics);

    config.setReversed(false);

    // An example trajectory to follow. All units in meters.
    List<Pose2d> list = List.of(new Pose2d(0, 0, Rotation2d.fromDegrees(0)),
            new Pose2d(0.5, 0, Rotation2d.fromDegrees(0)));

    Trajectory exampleTrajectory = Drivetrain.generateTrajectory(config, list);

    SwerveControllerCommand swerveControllerCommand = new SwerveControllerCommand(exampleTrajectory,
            drivetrain::getPose,
            drivetrain.kinematics,
            // Position controllers
            xController, yController, thetaController, drivetrain::setModuleStates, drivetrain);

    drivetrain.resetPIgeonIMU();

    drivetrain.resetOdometryWithPose2d(exampleTrajectory.getInitialPose());

    return swerveControllerCommand.andThen(() -> drivetrain.drive(0, 0, 0, false));
}
  public Command getAutonomousCommand() {
      return DropConeAndTaxi();
  }
}
