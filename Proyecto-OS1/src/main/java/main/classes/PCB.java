/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package main.classes;

import java.util.UUID;

/**
 *
 * @author cehernandez
 */
public class PCB implements Runnable {

    public enum ProcessState {
        NEW,
        READY,
        RUNNING,
        BLOCKED,
        FINISHED,
        READY_SUSPENDED,
        BLOCKED_SUSPENDED
    }

    private UUID processID;
    private String processName;
    private String user;

    private ProcessState state;
    private int priority;
    private int cyclesForException; //  I/0 bound 
    private int satisfyExceptionCycles; //  I/0 bound 

    private int programCounter;
    private int remainingInstructions;
    private int timeInCpu;
    private int stackPointer;
    private int totalInstructions;
    private int memoryAddressRegister;
    private String processType;

    public PCB(String processName, int totalInstructions) {
        this(processName, totalInstructions, "CPU-Bound", 0, 0);
    }

    public PCB(String processName, int totalInstructions, int cyclesForException, int satisfyExceptionCycles) {
        this(processName, totalInstructions, "I/O-Bound", cyclesForException, satisfyExceptionCycles);
    }

    private PCB(String processName, int totalInstructions, String processType, int cyclesForException, int satisfyExceptionCycles) {
        this.processID = UUID.randomUUID();
        this.processName = processName;
        this.totalInstructions = totalInstructions;
        this.processType = processType;
        this.cyclesForException = cyclesForException;
        this.satisfyExceptionCycles = satisfyExceptionCycles;

        this.remainingInstructions = this.totalInstructions;
        this.state = ProcessState.NEW;
        this.programCounter = 0;
        this.timeInCpu = 0;
        this.stackPointer = 0;
        this.memoryAddressRegister = 0;

    }

    @Override
    public void run() {
        while (this.programCounter < this.totalInstructions) {

            this.programCounter++;
            this.timeInCpu++;

            System.out.println("    [CPU EXEC] -> PCB ID: " + this.getProcessID().toString().substring(0, 4)
                    + " executing instruction " + this.programCounter);
        }
        this.setState(ProcessState.FINISHED);
        System.out.println("    [CPU EXEC] -> PCB ID: " + this.getProcessID().toString().substring(0, 4) + " FINISHED!");
    }

    @Override
    public String toString() {
        return "PCB{"
                + "ID=" + getProcessID().toString().substring(0, 8)
                + ", Name='" + getProcessName() + '\''
                + ", State='" + getState() + '\''
                + 
                ", Type='" + getProcessType() + '\''
                + 
                '}';
    }

    public String getProcessName() {
        return processName;
    }

    public void setProcessName(String processName) {
        this.processName = processName;
    }

    public UUID getProcessID() {
        return processID;
    }

    public void setProcessID(UUID processID) {
        this.processID = processID;
    }

    public ProcessState getState() {
        return state;
    }

    public void setState(ProcessState state) {
        this.state = state;
    }

    public int getTotalInstructions() {
        return totalInstructions;
    }

    public void setTotalInstructions(int totalInstructions) {
        this.totalInstructions = totalInstructions;
    }

    public int getRemainingInstructions() {
        return remainingInstructions;
    }

    public void setRemainingInstructions(int remainingInstructions) {
        this.remainingInstructions = remainingInstructions;
    }

    public String getProcessType() {
        return processType;
    }

    public void setProcessType(String processType) {
        this.processType = processType;
    }

    public int getProgramCounter() {
        return programCounter;
    }

    public void setProgramCounter(int programCounter) {
        this.programCounter = programCounter;
    }

    public int getTimeInCpu() {
        return timeInCpu;
    }

    public void setTimeInCpu(int timeInCpu) {
        this.timeInCpu = timeInCpu;
    }

    public int getStackPointer() {
        return stackPointer;
    }

    public void setStackPointer(int stackPointer) {
        this.stackPointer = stackPointer;
    }

}
