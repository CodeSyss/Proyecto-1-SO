/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package main.classes;

/**
 *
 * @author cehernandez
 */
public class CPU {

    public enum Mode {
        KERNEL,
        USER
    }

    private Mode currentMode;
    private PCB processActual;
    private Thread threadProcessActual;

    public CPU() {
        this.currentMode = currentMode.KERNEL;
        this.processActual = null;
        this.threadProcessActual = null;
    }

    public boolean isAvailable() {
        return this.processActual == null;
    }

    public void loadProcess(PCB processToLoad) {
        if (isAvailable()) {
            this.processActual = processToLoad;
            this.currentMode = Mode.USER;
            this.processActual.setState(PCB.ProcessState.RUNNING);
            this.threadProcessActual = new Thread(this.processActual);
            this.threadProcessActual.start();
        }
    }

    public Mode getCurrentMode() {
        return currentMode;
    }

    public void setCurrentMode(Mode currentMode) {
        this.currentMode = currentMode;
    }

    public PCB getProcessActual() {
        return processActual;
    }

    public void setProcessActual(PCB processActual) {
        this.processActual = processActual;
    }

    public Thread getThreadProcessActual() {
        return threadProcessActual;
    }

    public void setThreadProcessActual(Thread threadProcessActual) {
        this.threadProcessActual = threadProcessActual;
    }

}
