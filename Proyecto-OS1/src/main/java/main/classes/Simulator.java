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
                    performSwapOut(); // Llamada a la lógica de swapping
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

    /**
     * Intenta traer un proceso suspendido de vuelta a la memoria principal
     * (SWAP-IN). POLÍTICA ELEGIDA: Traer de vuelta al de mayor prioridad de la
     * readySuspendedQueue. Se ejecuta en cada ciclo si hay memoria disponible.
     */
    private void performSwapIn() throws InterruptedException {
        if (usedMemory < total_RAN_Memory) {

            PCB candidate = findHighestPriorityProcess(readySuspendedQueue);
            boolean fromReadySuspended = true;

            if (candidate == null && !blockedSuspendedQueue.isEmpty()) {
                candidate = findHighestPriorityProcess(blockedSuspendedQueue);
                fromReadySuspended = false;
            }

            if (candidate != null && (usedMemory + candidate.getMemorySize()) <= total_RAN_Memory) {

                if (fromReadySuspended) {
                    readySuspendedQueue.remove(candidate);
                    candidate.setState(PCB.ProcessState.READY); 
                } else {
                    blockedSuspendedQueue.remove(candidate);
                    candidate.setState(PCB.ProcessState.BLOCKED); 
                }

                usedMemory += candidate.getMemorySize();

                if (fromReadySuspended) {
                    readyQueueSemaphore.acquire();
                    try {
                        readyQueue.enqueue(candidate);
                    } finally {
                        readyQueueSemaphore.release();
                    }
                } else {
                    blockedQueue.enqueue(candidate);
                }

                System.out.println("SWAP-IN: Proceso " + candidate.getProcessID_short() + " reingresado a memoria. Estado: " + candidate.getState());
            }
        }
    }

    /**
     * Intenta liberar memoria suspendiendo un proceso (SWAP-OUT). POLÍTICA
     * ELEGIDA: Suspender el proceso bloqueado de menor prioridad. Si no hay,
     * suspende el proceso listo de menor prioridad. Se activa típicamente
     * cuando el LTS no puede admitir un proceso nuevo.
     */
    private void performSwapOut() {
        PCB victim = null;
        boolean wasBlocked = false;

        if (!blockedQueue.isEmpty()) {
            victim = findLowestPriorityProcess(blockedQueue);
            wasBlocked = true;
        }
        else if (!readyQueue.isEmpty()) {
            victim = findLowestPriorityProcess(readyQueue);
            if (victim == cpu.getProcessActual()) {
                victim = findSecondLowestPriorityProcess(readyQueue);
            }
        }

        if (victim != null) {
            if (wasBlocked) {
                blockedQueue.remove(victim);
                victim.setState(PCB.ProcessState.BLOCKED_SUSPENDED);
                blockedSuspendedQueue.enqueue(victim);
            } else {
                try {
                    readyQueueSemaphore.acquire();
                    readyQueue.remove(victim);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    readyQueueSemaphore.release();
                    return;
                } finally {
                    readyQueueSemaphore.release();
                }
                victim.setState(PCB.ProcessState.READY_SUSPENDED);
                readySuspendedQueue.enqueue(victim);
            }

            usedMemory -= victim.getMemorySize();
            System.out.println("SWAP-OUT: Proceso " + victim.getProcessID_short() + " suspendido para liberar " + victim.getMemorySize() + " de memoria.");
        } else {
            System.out.println("SWAP-OUT: No se encontraron víctimas adecuadas para suspender.");
        }
    }

    /**
     * Encuentra el proceso con la prioridad más BAJA (número más ALTO) en una
     * cola dada.
     *
     * @param queue La cola en la que buscar (readyQueue o blockedQueue).
     * @return El proceso con la prioridad más baja, o null si la cola está vacía.
     */
    private PCB findLowestPriorityProcess(CustomQueue<PCB> queue) {
        if (queue.isEmpty()) {
            return null;
        }

        PCB victim = queue.peek();
        for (PCB process : queue.iterable()) {
            if (process.getPriority() > victim.getPriority()) { 
                victim = process;
            }
        }
        return victim;
    }

    /**
     * Encuentra el proceso con la prioridad más ALTA (número más BAJO) en una
     * cola dada.
     *
     * @param queue La cola en la que buscar (readySuspendedQueue o
     * blockedSuspendedQueue).
     * @return El proceso con la prioridad más alta, o null si la cola está vacía.
     */
    private PCB findHighestPriorityProcess(CustomQueue<PCB> queue) {
        if (queue.isEmpty()) {
            return null;
        }

        PCB candidate = queue.peek();
        for (PCB process : queue.iterable()) {
            if (process.getPriority() < candidate.getPriority()) { // Menor número = Mayor prioridad
                candidate = process;
            }
        }
        return candidate;
    }

    /**
     * Método de ayuda para encontrar el segundo proceso con la prioridad más
     * baja. Útil para el caso de Swap-Out donde el de menor prioridad es el que
     * está en CPU.
     *
     * @param queue La cola readyQueue.
     * @return El segundo Proceso con la prioridad más baja, o null si no hay
     * suficientes procesos.
     */
    private PCB findSecondLowestPriorityProcess(CustomQueue<PCB> queue) {
        if (queue.size() < 2) {
            return null; 
        }

        PCB lowest = queue.peek();
        PCB secondLowest = null;

        for (PCB process : queue.iterable()) {
            if (process.getPriority() > lowest.getPriority()) {
                secondLowest = lowest; 
                lowest = process;          
            } else if (secondLowest == null || process.getPriority() > secondLowest.getPriority()) {
                if (process != lowest) {
                    secondLowest = process;
                }
            }
        }
        return secondLowest;
    }

    public int getUsedMemory() {
        return usedMemory;
    }

    public void setUsedMemory(int usedMemory) {
        this.usedMemory = usedMemory;
    }

}
