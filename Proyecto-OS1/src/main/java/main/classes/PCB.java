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

    private int memorySize; //Tamano para cada proceso.
    private int BASE_MEMORY = 64;
    private boolean ioRequestFlag = false;
    private int cyclesSpentBlocked = 0;      // Cronómetro para el tiempo en estado de bloqueo cuando es i/o bound 

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public int getCyclesForException() {
        return cyclesForException;
    }

    public void setCyclesForException(int cyclesForException) {
        this.cyclesForException = cyclesForException;
    }

    public int getSatisfyExceptionCycles() {
        return satisfyExceptionCycles;
    }

    public void setSatisfyExceptionCycles(int satisfyExceptionCycles) {
        this.satisfyExceptionCycles = satisfyExceptionCycles;
    }

    public int getCyclesSpentBlocked() {
        return cyclesSpentBlocked;
    }

    public void setCyclesSpentBlocked(int cyclesSpentBlocked) {
        this.cyclesSpentBlocked = cyclesSpentBlocked;
    }

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
        this.memorySize = BASE_MEMORY + (this.totalInstructions / 4);

        if ("I/O-Bound".equals(processType)) {
            this.priority = 1; // Prioridad alta
        } else {
            this.priority = 5; // Prioridad media/baja
        }

    }

    public int getMemorySize() {
        return memorySize;
    }

    public void setMemorySize(int memorySize) {
        this.memorySize = memorySize;
    }

    @Override
    public void run() {
        while (this.programCounter < this.totalInstructions) {

            this.programCounter++;
            this.timeInCpu++;
            this.remainingInstructions--;

            if ("I/O-Bound".equals(this.processType)) {
                if (this.programCounter % this.cyclesForException == 0) {
                    this.ioRequestFlag = true;
                }
            }

            try {
                Thread.sleep(Long.MAX_VALUE);
            } catch (InterruptedException e) {
                if (this.ioRequestFlag) {
                    break;
                }
                continue;
            }

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
                + ", Type='" + getProcessType() + '\''
                + '}';
    }

    public boolean hasIoRequest() {
        return this.ioRequestFlag;
    }

    public void clearIoRequest() {
        this.ioRequestFlag = false;
    }

    public void incrementCyclesBlocked() {
        this.cyclesSpentBlocked++;
    }

    public void resetCyclesBlocked() {
        this.cyclesSpentBlocked = 0;
    }

    public boolean isFinished() {
        return remainingInstructions <= 0;
    }

    public String getProcessID_short() {
        return processID.toString().substring(0, 8);
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
