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
    // La cpu ejecuta procesos de manera independiente --> MonoProcesador 
    public enum Mode {
        KERNEL,
        USER
    }
    
    private static final CPU INSTANCE = new CPU();
    
    public static CPU getInstance() {
        return INSTANCE;
    }

    private Mode currentMode;
    private PCB processActual;
    private Thread threadProcessActual;

    public CPU() {
        this.currentMode = Mode.KERNEL;
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
    
    public PCB unloadProcess(){
        if (this.threadProcessActual != null){
            this.threadProcessActual.interrupt();
        }
        
        //Guarda el proceso actual antes de configurarlo para no perderlo
        PCB unloadedProcess = this.processActual; 
        
        // Vuelve el contructor a su estado inicial
        this.currentMode = Mode.KERNEL;
        this.processActual = null;
        this.threadProcessActual = null;
        
        return unloadedProcess;
    }

    public Mode getCurrentMode() {
        return currentMode;
    }
    
    public PCB getProcessActual() {
        return processActual;
    }
    public Thread getThreadProcessActual() {
        return threadProcessActual;
    }

}


