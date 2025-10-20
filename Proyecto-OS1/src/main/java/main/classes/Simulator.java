package main.classes;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
/**
 *
 * @author cehernandez
 */
import helpers.CustomQueue;

public class Simulator implements Runnable {

    private final CPU cpu;
    private final PlanningPolicies planningPolicies;

    private final CustomQueue<PCB> newQueue; // Cola a largo plazo
    private final CustomQueue<PCB> readyQueue; // Cola a corto plazo
    private final CustomQueue<PCB> blockedQueue; // Cola a corto plazo
    private final CustomQueue<PCB> finishedQueue; // Cola a largo plazo
    private final CustomQueue<PCB> readySuspendedQueue; // Cola a mediano plazo
    private final CustomQueue<PCB> blockedSuspendedQueue; // Cola a mediano plazo

    private volatile int globalCycle;  // Mi reloj Global para cada ciclo 
    private volatile int cycleDurationMs;  // Duración del ciclo de ejecución en mi simulación

    private final int total_RAN_Memory = 2024;
    private int usedMemory;
    private boolean isRunning = false;
    private boolean isPaused = false;

    
    public Simulator() {

        this.cycleDurationMs = 0;
        this.globalCycle = 0;

        this.newQueue = new CustomQueue<>();
        this.readyQueue = new CustomQueue<>();
        this.blockedQueue = new CustomQueue<>();
        this.finishedQueue = new CustomQueue<>();
        this.readySuspendedQueue = new CustomQueue<>();
        this.blockedSuspendedQueue = new CustomQueue<>();

        this.cpu = new CPU();
        this.planningPolicies = new PlanningPolicies(this.readyQueue);

    }

    @Override
    public void run() {
        this.isRunning = true;
        while (this.isRunning) {

            try {

                while (this.isPaused) {
                    Thread.sleep(100);
                }

                // --- 1. PLANIFICADOR A LARGO PLAZO (Admitir nuevos procesos)
                if (!newQueue.isEmpty()) {
                    PCB processToAdmit = newQueue.dequeue();
                    processToAdmit.setState(PCB.ProcessState.READY);
                    readyQueue.enqueue(processToAdmit);

                    System.out.println("(Cycle " + globalCycle + "): Admitted Process ID " + processToAdmit.getProcessID() + " to Ready Queue.");
                    System.out.println("SIMULATOR: Proceso Listo -> " + readyQueue.toString());
                }

                // --- 2. PLANIFICADOR A CORTO PLAZO (Despachar proceso a la CPU) 
                if (cpu.isAvailable() && !readyQueue.isEmpty()) {
                    // Hacer las políticas de planificación para que elijan el siguiente.
                    // Por ahora, asumimos FCFS.
                    PCB processToDispatch = readyQueue.dequeue();

                    cpu.loadProcess(processToDispatch);

                    System.out.println("(Cycle " + globalCycle + "): Dispatched Process ID " + processToDispatch.getProcessID() + " to CPU.");
                }

                // Terminar toda la lógica de un ciclo (planificar, mover procesos, etc.)
                this.globalCycle++;
                System.out.println("Ciclo de Reloj Global: " + this.globalCycle);
                Thread.sleep(this.cycleDurationMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    public void setCycleDuration(int newDurationMs) {
        // Se puede agregar validación por si es un número negativo
        this.cycleDurationMs = newDurationMs;
    }

    public void stopSimulation() {
        this.isRunning = false;
        this.isPaused = false;
        this.globalCycle = 0;
    }

    public void togglePause() {
        this.isPaused = !this.isPaused; 

        if (this.isPaused) {
            System.out.println("SIMULATOR: Paused.");
        } else {
            System.out.println("SIMULATOR: Resumed.");
        }
    }

    public void createProcessFromUI(String name, int instructions, String type, int cyclesForException, int satisfyCycles) {
        PCB newProcess;

        if ("I/O Bound".equals(type)) {
            System.out.println("SIMULATOR: Creando un proceso I/O Bound.");
            newProcess = new PCB(name, instructions, cyclesForException, satisfyCycles);
        } else {
            System.out.println("SIMULATOR: Creando un proceso CPU Bound.");
            newProcess = new PCB(name, instructions);
        }
        newQueue.enqueue(newProcess);
        System.out.println("SIMULATOR: Proceso CREADO -> " + newProcess.toString());

    }

}
