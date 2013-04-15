/* ***************************************************************************
 *
 *    Copyright (C) 2006 Alterra, Wageningen University and Research centre.
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *    or look at URL www.gnu.org/licenses/lgpl.html
 *
 *****************************************************************************
 *
 * @author Rob Knapen, Alterra B.V., The Netherlands
 * @author Wim de Winter, Alterra B.V., The Netherlands
 *
 *****************************************************************************
 * Changes:
 * 16oct2006 - Rob Knapen
 *      Added initialize() methods that delegate to the composition instance.
 *      Hopefully makes it more clear that start() does not initalize the
 *      composition first, it only prepares it. 
 *
 ****************************************************************************/
package nl.alterra.openmi.sdk.configuration;

import java.util.ArrayList;
import nl.alterra.openmi.sdk.backbone.Event;
import nl.alterra.openmi.sdk.backbone.Publisher;
import nl.alterra.openmi.sdk.backbone.TimeStamp;
import org.openmi.standard.IEvent;
import org.openmi.standard.IArgument;

/**
 * The system deployer is a class for running a composition. Provided with
 * start and end times for the simulation and the desired time step, it can
 * execute a composition either in a blocking or a non blocking fashion.
 * 
 * The time steps between the start and end time will be performed in
 * sequence. When non blocking a thread will be started and run for each
 * time step. Within the time step all triggers registered with the
 * deployer from the composition will be 'pulled' to get their values.
 *
 * Although the SystemDeployer is a IPublisher of OpenMI IEvents, the sender
 * of the event will be the composition the system deployer is running. This
 * is due to the fact that the sender of an OpenMI event must be a linkable
 * component, instead of the more generic IPublisher.
 *  
 * A DataChanged event will be published at the end of each time step. This
 * can be used to extract the calculated values from each trigger by calling
 * its getLastCalculatedValues method.
 * 
 * Non blocking calculations can be controlled by calling the pause(),
 * resume() and stop() methods. Blocking calculations (obviously) can not
 * be controlled this way.
 */
public class SystemDeployer extends Publisher implements Runnable {

    private boolean paused = false;
    private boolean running = false;
    private boolean blocking = false;
    private Composition composition = null;
    private double startTime;
    private double endTime;
    private double currentTime;
    private double timeStep;
    private ArrayList<Trigger> triggers = new ArrayList<Trigger>();
    private ArrayList<Thread> runningThreads = new ArrayList<Thread>();

    /**
     * Creates an instance with the specified ID.
     *
     * @param ID String ID of the deployer
     */
    public SystemDeployer(String ID) {
        super(ID);
        getEventTypes().add(IEvent.EventType.Informative);
        getEventTypes().add(IEvent.EventType.TimeStepProgress);
        getEventTypes().add(IEvent.EventType.DataChanged);
    }

    /**
     * Gets the start time for the simulation.
     *
     * @return double
     */
    public double getStartTime() {
        return startTime;
    }

    /**
     * Sets the start time for the simulation, if no simulation is already
     * running.
     *
     * @param modifiedJulianDay
     */
    public void setStartTime(double modifiedJulianDay) {
        if (!running) {
            startTime = modifiedJulianDay;
        }
    }

    /**
     * Gets the end time for the simulation.
     *
     * @return double
     */
    public double getEndTime() {
        return endTime;
    }

    /**
     * Sets the end time for the simulation, if no simulation is already
     * running.
     *
     * @param modifiedJulianDay
     */
    public void setEndTime(double modifiedJulianDay) {
        if ((!running) || (paused)) {
            endTime = modifiedJulianDay;
        }
    }

    /**
     * Gets the time step for the simulation.
     *
     * @return double Time step in seconds
     */
    public double getTimeStep() {
        return timeStep;
    }

    /**
     * Sets the time step for the simulation, if no simulation is already
     * running.
     *
     * @param timeStep The time step in seconds
     */
    public void setTimeStep(double timeStep) {
        if ((!running) || (paused)) {
            this.timeStep = timeStep;
        }
    }

    /**
     * Sets the blocking state of the deployer, if it is not already running
     * a simulation. If blocking is set to false, a simulation will be run
     * using (background) threads. If set to true the deployer will block and
     * wait until the simulation is finished.
     *
     * @param blocking
     */
    public void setBlocking(boolean blocking) {
        if (!running) {
            this.blocking = blocking;
        }
    }

    /**
     * Gets the list of triggers that are 'pulled' (calculated) by the
     * deployer when it executes the simulation.
     *
     * @return ArrayList<Trigger>
     */
    public ArrayList<Trigger> getTriggers() {
        ArrayList<Trigger> result = new ArrayList<Trigger>();
        result.addAll(triggers);
        return result;
    }

    /**
     * Sets the list of triggers that will be 'pulled' (calculated) by the
     * deployer when it runs a simulation.
     *
     * @param triggers
     */
    public void setTriggers(ArrayList<Trigger> triggers) {
        if ((!running) || (paused)) {
            this.triggers.clear();
            this.triggers.addAll(triggers);
        }
    }

    /**
     * Returns the paused state of the deployer.
     *
     * @return True if a non blocking simulation is paused
     */
    public boolean isPaused() {
        return paused;
    }

    /**
     * Returns the running state of the deployer.
     *
     * @return True if a simulation is in progress
     */
    public boolean isRunning() {
        return running;
    }

    /**
     * Returns the blocking state of the deployer.
     *
     * @return True if the deployer will be blocked during simulation
     */
    public boolean isBlocking() {
        return blocking;
    }

    /**
     * Gets the composition the deployer is operating with.
     *
     * @return Composition
     */
    public Composition getComposition() {
        return composition;
    }

    /**
     * Sets the composition the deployer will execute. If a non blocking
     * calculation is running, it will be stopped first.
     *
     * @param composition Composition to execute
     */
    public void setComposition(Composition composition) {
        if (running) {
            stop();
        }
        this.composition = composition;
    }

    /**
     * Initializes the composition with the given arguments.
     *
     * @param properties The properties to be initialized
     */
    public final void initialize(IArgument[] properties) {
        if (running) {
            throw new ConfigurationException("Cannot initialize a running system.");
        }

        if (composition != null) {
            composition.initialize(properties);
        }
    }

    /**
     * Starts the execution for the current startTime, endTime and timeStep.
     */
    public void start() {
        start(startTime, endTime, timeStep);
    }

    /**
     * Starts the execution of a calculation for the composition using the
     * specified time information.
     *
     * @param start double for simulation start Modified Julian Day
     * @param end   double for simulation end Modified Julian Day
     * @param step  Simulation time steps in seconds
     */
    public void start(double start, double end, double step) {
        if ((!running) && (composition != null)) {
            runningThreads.clear();
            running = true;
            paused = true;
            startTime = start;
            endTime = end;
            timeStep = step; // TODO: rename timestep -> outputTimeStepInterval...
            currentTime = start;

            sendEvent(new Event(new TimeStamp(currentTime),
                    IEvent.EventType.Informative,
                    composition,
                    String.format("Preparing composition '%s' for calculation", composition)));

            composition.prepare();

            sendEvent(new Event(new TimeStamp(currentTime),
                    IEvent.EventType.Informative,
                    composition,
                    String.format("Starting calculation of composition '%s'", composition)));

            resume();
        }
    }

    /**
     * Stops the execution of a non blocking calculation (at the earliest
     * possible moment).
     */
    public void stop() {
        if (running) {
            running = false;
            paused = false;

            sendEvent(new Event(new TimeStamp(currentTime),
                    IEvent.EventType.Informative,
                    composition,
                    String.format("Finishing after calculation of composition '%s'", composition)));

            composition.finish();
        }
    }

    /**
     * Pauses the execution of a non blocking calculation.
     */
    public void pause() {
        if (running && !paused) {
            paused = true;
        }
    }

    /**
     * Resumes the calculation, when a non blocking execution has been paused.
     */
    public void resume() {
        if (paused) {
            paused = false;

            if (!blocking) {
                Thread runThread = new Thread(this);
                runningThreads.add(runThread);
                runThread.start();
            }
            else {
                run();
            }
        }
    }

    /**
     * Called after calculation for a single time step is done. It sends
     * the notifications, checks if further calculation is needed and if
     * so starts the next time step run.
     */
    private synchronized void timeStepDone(Thread callingThread) {
        sendEvent(new Event(new TimeStamp(currentTime),
                IEvent.EventType.DataChanged,
                composition,
                String.format("Completed calculation for time step")));

        if (currentTime >= endTime) {
            stop();
        }
        else {
            pause();
        }

        resume();
        runningThreads.remove(callingThread);
    }

    /**
     * Runs one time step of the calculation. This method is part of the
     * Runnable interface implementation and not to be called directly. Use
     * the start() method to perform a calculation instead.
     */
    public void run() {
        // run calculation until the next time step
        TimeStamp nextStop = new TimeStamp(currentTime + timeStep / (24 * 3600));

        for (Trigger t : triggers) {
            composition.pull(t, nextStop);
        }

        currentTime = nextStop.getModifiedJulianDay();

        // if not done, start a new thread for the next run
        timeStepDone(Thread.currentThread());
    }

    /**
     * Clears the system deployer. This will only work if the system is not
     * running. Its list of triggers will be cleared.
     */
    public void clear() {
        if (running) {
            throw new ConfigurationException("Cannot clear a running system.");
        }

        stop();
        setTriggers(null);
    }

    /**
     * Kills the system if it is running. All threads will be stopped (which
     * might cause them to block) and the list of running threads cleared.
     */
    public void kill() {
        stop();
        for (Thread t : runningThreads) {
            t.stop();
        }
        runningThreads.clear();
    }

}
