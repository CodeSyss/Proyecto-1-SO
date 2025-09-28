/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package main.classes;

/**
 *
 * @author cehernandez
 */
public class PCB {
    private String processName;
    private int processID;
    private String state;
    private int totalInstructions;
    private int remainingInstructions;
    private String processType;
    private int cpuBound;
    private int ioBound;
    private int programCounter;
    private int timeInCpu;
    private int stackPointer;

    public PCB(String processName, int processID, String state, int totalInstructions, int remainingInstructions, String processType, int cpuBound, int ioBound, int programCounter, int timeInCpu, int stackPointer) {
        this.processName = processName;
        this.processID = processID;
        this.state = state;
        this.totalInstructions = totalInstructions;
        this.remainingInstructions = remainingInstructions;
        this.processType = processType;
        this.cpuBound = cpuBound;
        this.ioBound = ioBound;
        this.programCounter = programCounter;
        this.timeInCpu = timeInCpu;
        this.stackPointer = stackPointer;
        
         if (cpuBound > ioBound) {
            this.processType = "CPU-Bound";
        } else if (ioBound > cpuBound) {
            this.processType = "I/O-Bound";
        } else {
            this.processType = "Balanced";
        }
    }

    public String getProcessName() {
        return processName;
    }

    public void setProcessName(String processName) {
        this.processName = processName;
    }

    public int getProcessID() {
        return processID;
    }

    public void setProcessID(int processID) {
        this.processID = processID;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
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

    public int getCpuBound() {
        return cpuBound;
    }

    public void setCpuBound(int cpuBound) {
        this.cpuBound = cpuBound;
    }

    public int getIoBound() {
        return ioBound;
    }

    public void setIoBound(int ioBound) {
        this.ioBound = ioBound;
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
