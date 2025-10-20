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
import java.util.concurrent.Semaphore;

public class Simulator implements Runnable {

    private final CPU cpu;
    private final PlanningPolicies planningPolicies;

    private final CustomQueue<PCB> newQueue; // Cola a largo plazo
    private final CustomQueue<PCB> readyQueue; // Cola a corto plazo
    private final CustomQueue<PCB> blockedQueue; // Cola a corto plazo
    private final CustomQueue<PCB> finishedQueue; // Cola a largo plazo
    private final CustomQueue<PCB> readySuspendedQueue; // Cola a mediano plazo
    private final CustomQueue<PCB> blockedSuspendedQueue; // Cola a mediano plazo

    private final Semaphore newQueueSemaphore = new Semaphore(1);
    private final Semaphore readyQueueSemaphore = new Semaphore(1);

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
                
                //Liberar CPU
                checkRunningProcess();
                
                //Revisa si el evento por el que estaba esperando ya finalizó y vuelve a la cola listo.
                checkBlockedQueue();
                
                // PLANIFICADOR A MEDIO PLAZO 
                performSwapIn();
                
                // PLANIFICADOR A LARGO PLAZO 
                longTermScheduler();
               
                // PLANIFICADOR A CORTO PLAZO 
                dispatchProcessToCpu();
                
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

    //Planificador a largo plazo
    private void longTermScheduler() {
        try {
            newQueueSemaphore.acquire();
            if (!newQueue.isEmpty()) {
                PCB candidate = newQueue.peek();
                if ((usedMemory + candidate.getMemorySize()) <= total_RAN_Memory) {
                    PCB admittedProcess = newQueue.dequeue();
                    admittedProcess.setState(PCB.ProcessState.READY);
                    usedMemory += admittedProcess.getMemorySize();

                    readyQueueSemaphore.acquire();
                    try {
                        readyQueue.enqueue(admittedProcess);
                        System.out.println("LTS: Proceso " + admittedProcess.getProcessID_short() + " admitido en memoria.");
                    } finally {
                        readyQueueSemaphore.release();
                    }
                } else {
                    System.out.println("LTS: Memoria insuficiente para admitir " + candidate.getProcessID_short() + ". Se podría activar Swap-Out.");
                    // performSwapOut(); // Llamada a la lógica de swapping
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            newQueueSemaphore.release();
        }
    }

    //Planificador a corto plazo
    private void dispatchProcessToCpu() throws InterruptedException {
        readyQueueSemaphore.acquire();
        try {
            if (cpu.isAvailable() && !readyQueue.isEmpty()) {
                //PCB processToDispatch = planningPolicies.selectNextProcess(readyQueue);
                //if (processToDispatch != null) {
                //    readyQueue.remove(processToDispatch); // Tu CustomQueue necesita un método remove
                //    cpu.loadProcess(processToDispatch);
                //    System.out.println("STS: Proceso " + processToDispatch.getProcessID_short() + " despachado a la CPU.");
                //}
            }
        } finally {
            readyQueueSemaphore.release();
        }
    }

    private void checkRunningProcess() {
        if (!cpu.isAvailable()) {
            PCB runningProcess = cpu.getProcessActual();

            if (runningProcess.getState() == PCB.ProcessState.FINISHED) {
                PCB finishedProcess = cpu.unloadProcess();
                finishedQueue.enqueue(finishedProcess);
                usedMemory -= finishedProcess.getMemorySize(); // Libera memoria
                System.out.println("SIM: Proceso " + finishedProcess.getProcessID_short() + " ha terminado.");
            } else if (runningProcess.hasIoRequest()) {
                PCB blockedProcess = cpu.unloadProcess();
                blockedProcess.setState(PCB.ProcessState.BLOCKED);
                blockedProcess.clearIoRequest();
                blockedQueue.enqueue(blockedProcess);
                System.out.println("SIM: Proceso " + blockedProcess.getProcessID_short() + " bloqueado por E/S.");
            }
        }
    }

    private void checkBlockedQueue() throws InterruptedException {
        if (!blockedQueue.isEmpty()) {
            CustomQueue<PCB> processesToUnblock = new CustomQueue<>();
            for (PCB process : blockedQueue.iterable()) {
                process.incrementCyclesBlocked();
                if (process.getCyclesSpentBlocked() >= process.getSatisfyExceptionCycles()) {
                    processesToUnblock.enqueue(process);
                }
            }
            while (!processesToUnblock.isEmpty()) {
                PCB processReady = processesToUnblock.dequeue();
                blockedQueue.remove(processReady);
                processReady.resetCyclesBlocked();
                processReady.setState(PCB.ProcessState.READY);

                readyQueueSemaphore.acquire();
                try {
                    readyQueue.enqueue(processReady);
                } finally {
                    readyQueueSemaphore.release();
                }
                System.out.println("SIM: Proceso " + processReady.getProcessID_short() + " ha completado E/S y vuelve a la Ready Queue.");
            }
        }
    }

    private void performSwapIn() {
    }

    private void performSwapOut() {
    }

    public int getUsedMemory() {
        return usedMemory;
    }

    public void setUsedMemory(int usedMemory) {
        this.usedMemory = usedMemory;
    }

}
