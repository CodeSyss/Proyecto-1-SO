package main.classes;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
/**
 *
 * @author cehernandez
 */
import gui.classes.JFrame_principal;
import helpers.CustomQueue;
import java.util.Random;
import java.util.concurrent.Semaphore;
import javax.swing.SwingUtilities;

public class Simulator implements Runnable {

    private final CPU cpu;
    private final PlanningPolicies planningPolicies;
    private JFrame_principal gui;
    private Random randomGenerator = new Random();

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

    // --- NUEVOS ATRIBUTOS PARA CÁLCULO DE MÉTRICAS ---
    private int cpuBusyCycles = 0;           // Contador de ciclos que la CPU estuvo ocupada
    private int totalProcessesFinished = 0;  // Contador de procesos que han terminado
    private double totalWaitTime = 0.0;      // Suma total del tiempo de espera de todos los procesos terminados
    private double totalResponseTime = 0.0;  // Suma total del tiempo de respuesta

    // --- Atributos para guardar los valores calculados en cada ciclo ---
    private double currentThroughput = 0.0;
    private double currentCpuUtil = 0.0;
    private double currentAvgWaitTime = 0.0;

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
        this.planningPolicies = new PlanningPolicies();

    }

    @Override
    public void run() {
        this.isRunning = true;
        while (this.isRunning) {

            try {

                while (this.isPaused) {
                    Thread.sleep(100);
                }

                if (!cpu.isAvailable()) {
                    cpuBusyCycles++;
                }

                //Liberar CPU
                checkRunningProcess();

                //Revisa si el evento por el que estaba esperando ya finalizó y vuelve a la cola listo.
                checkBlockedQueue();

                // PLANIFICADOR A MEDIO PLAZO 
                //performSwapIn();
                // PLANIFICADOR A LARGO PLAZO 
                longTermScheduler();

                // PLANIFICADOR A CORTO PLAZO 
                dispatchProcessToCpu();

                // --- LÓGICA DE MÉTRICAS: Calcular promedios ---
                if (globalCycle > 0) {
                    this.currentThroughput = (double) totalProcessesFinished / globalCycle;
                    this.currentCpuUtil = (double) cpuBusyCycles / globalCycle;
                }
                if (totalProcessesFinished > 0) {
                    this.currentAvgWaitTime = totalWaitTime / totalProcessesFinished;
                }

                // 6. ACTUALIZAR GUI (Enviar estado actual a la interfaz)
                updateGUI();

                this.globalCycle++;
                System.out.println("Ciclo de Reloj Global: " + this.globalCycle);
                // DESPUÉS de toda la lógica, interrumpe al proceso en CPU (si hay uno) para que haga su próximo ciclo
                if (!cpu.isAvailable()) { // Verifica si la CPU tiene un proceso
                    Thread currentThread = cpu.getThreadProcessActual(); // Obtiene el HILO
                    if (currentThread != null) { // Verifica que el hilo exista
                        currentThread.interrupt(); // ¡Llama a interrupt() en el HILO!
                    }
                }
                Thread.sleep(this.cycleDurationMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    public void setPlanningPolicy(String policyCode) {
        if (this.planningPolicies != null) {
            this.planningPolicies.setPolicy(policyCode);
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
            System.out.println("SIMULATOR: Creando I/O Bound con Name=" + name + ", Instr=" + instructions + ", ExcCycle=" + cyclesForException + ", SatCycle=" + satisfyCycles);
            newProcess = new PCB(name, instructions, cyclesForException, satisfyCycles);
        } else {
            System.out.println("SIMULATOR: Creando CPU Bound con Name=" + name + ", Instr=" + instructions);
            newProcess = new PCB(name, instructions);
        }

        newProcess.setState(PCB.ProcessState.NEW);
        newQueue.enqueue(newProcess);

        System.out.println("DEBUG PCB CREATED: ID=" + newProcess.getProcessID_short()
                + ", Type=" + newProcess.getProcessType()
                + ", State=" + newProcess.getState()
                + ", CyclesForExc=" + newProcess.getCyclesForException());

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
                    admittedProcess.setTimeArrivedReady(this.globalCycle);

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

    private void dispatchProcessToCpu() throws InterruptedException {
        // Solo despachar cada X ciclos o bajo condiciones específicas
        //if (globalCycle % 10 != 0) { // Solo cada 2 ciclos, por ejemplo
        //   return;
        //}

        readyQueueSemaphore.acquire();
        try {
            if (cpu.isAvailable() && !readyQueue.isEmpty()) {
                PCB processToDispatch = planningPolicies.selectNextProcess(readyQueue, this.globalCycle);
                if (processToDispatch != null) {
                    readyQueue.remove(processToDispatch);

                    // --- CÁLCULO DE TIEMPO DE RESPUESTA ---
                    if (processToDispatch.getTimeInCpu() == 0) { // Primera vez que se ejecuta
                        long responseTime = this.globalCycle - processToDispatch.getTimeArrivedReady();
                        this.totalResponseTime += responseTime;
                        System.out.println("   Tiempo de Respuesta para " + processToDispatch.getProcessID_short() + ": " + responseTime + " ciclos.");
                    }
                    // -------------------------------------

                    cpu.loadProcess(processToDispatch);
                    System.out.println("STS: Proceso " + processToDispatch.getProcessID_short() + " despachado a la CPU.");
                }
            }
        } finally {
            readyQueueSemaphore.release();
        }
    }

    private void checkRunningProcess() {
        // Usa isAvailable() si ese es el nombre correcto en tu clase CPU
        if (!cpu.isAvailable()) {
            PCB current = cpu.getProcessActual(); // Usa getProcessActual() o getPcbActual()

            // Imprimir estado actual ANTES de cualquier cambio
            System.out.println("🔍 CHECKING proceso en CPU: " + current.getProcessID_short()
                    + " - PC: " + current.getProgramCounter()
                    + "/" + current.getTotalInstructions()
                    + " - State: " + current.getState());

            // --- CASO 1: Proceso YA HA TERMINADO? ---
            // (Verifica si el PC ya alcanzó el total)
            if (current.getProgramCounter() >= current.getTotalInstructions()) {

                System.out.println("✅ TERMINANDO proceso: " + current.getProcessID_short() + ". Unloading CPU...");
                if (current.getState() != PCB.ProcessState.FINISHED) {
                    current.setState(PCB.ProcessState.FINISHED);
                }

                PCB finishedProcess = cpu.unloadProcess(); // Llama al método correcto en CPU

                if (finishedProcess != null) {

                    // --- ¡ACUMULAR MÉTRICAS! ---
                    this.totalProcessesFinished++;
                    long turnaroundTime = this.globalCycle - finishedProcess.getTimeArrivedReady();
                    long waitTime = turnaroundTime - finishedProcess.getTimeInCpu();
                    this.totalWaitTime += waitTime;

                    finishedQueue.enqueue(finishedProcess);
                    usedMemory -= finishedProcess.getMemorySize();
                    System.out.println("SIM: Proceso " + finishedProcess.getProcessID_short() + " -> Finished Queue.");
                }
                System.out.println("CPU después de unload (FINISHED): " + (cpu.isAvailable() ? "LIBRE" : "OCUPADO"));

                // --- CASO 2: Proceso AÚN NO HA TERMINADO (Debe avanzar) ---
            } else {
                current.setProgramCounter(current.getProgramCounter() + 1);
                System.out.println("PC avanzado a: " + current.getProgramCounter());

                if ("I/O-Bound".equals(current.getProcessType())
                        && current.getCyclesForException() > 0
                        && (current.getProgramCounter() % current.getCyclesForException() == 0)) {
                    // ¡Sí! Es hora de bloquear.
                    System.out.println("🚧 BLOQUEANDO proceso por E/S: " + current.getProcessID_short() + " en PC=" + current.getProgramCounter());
                    PCB blockedProcess = cpu.unloadProcess();

                    if (blockedProcess != null) {
                        blockedProcess.setState(PCB.ProcessState.BLOCKED);
                        blockedQueue.enqueue(blockedProcess);
                        System.out.println("SIM: Proceso " + blockedProcess.getProcessID_short() + " -> Blocked Queue.");
                    }
                    System.out.println("CPU después de unload (I/O): " + (cpu.isAvailable() ? "LIBRE" : "OCUPADO"));
                }
                // Si no es E/S, el proceso simplemente continúa en la CPU para el siguiente ciclo.
            }
        } else {
            System.out.println("SIM: CPU is Idle this cycle.");
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

                processReady.setTimeArrivedReady(this.globalCycle);

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
        } else if (!readyQueue.isEmpty()) {
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
     * @return El proceso con la prioridad más baja, o null si la cola está
     * vacía.
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
     * @return El proceso con la prioridad más alta, o null si la cola está
     * vacía.
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

    // --- MÉTODO PARA ACTUALIZAR LA GUI ---
    private void updateGUI() {
        if (gui != null) {
            final CustomQueue<PCB> readySnapshot = readyQueue;
            final CustomQueue<PCB> blockedSnapshot = blockedQueue;
            final CustomQueue<PCB> finishedSnapshot = finishedQueue;
            final CustomQueue<PCB> readySuspendedSnapshot = readySuspendedQueue;
            final CustomQueue<PCB> blockedSuspendedSnapshot = blockedSuspendedQueue;

            final PCB runningSnapshot = cpu.getProcessActual();
            final CPU.Mode modeSnapshot = cpu.getCurrentMode();
            final int cycleSnapshot = this.globalCycle;
            final int memSnapshot = this.usedMemory;
            final int totalMemSnapshot = total_RAN_Memory;
            final double throughputSnapshot = this.currentThroughput;
            final double cpuUtilSnapshot = this.currentCpuUtil;
            final double avgWaitTimeSnapshot = this.currentAvgWaitTime;

            SwingUtilities.invokeLater(() -> {
                gui.updateReadyQueue(readySnapshot);
                gui.updateBlockedQueue(blockedSnapshot);
                gui.updateFinishedQueue(finishedSnapshot);
                gui.updateReadySuspendedQueue(readySuspendedSnapshot);
                gui.updateBlockedSuspendedQueue(blockedSuspendedSnapshot);
                gui.updateCpuPanel(runningSnapshot, modeSnapshot);
                gui.updateGlobalCycleLabel(cycleSnapshot);
                gui.updateMemoryUsage(memSnapshot, totalMemSnapshot);
                gui.updateMetricas(cycleSnapshot, throughputSnapshot, cpuUtilSnapshot, avgWaitTimeSnapshot);
            });
        }
    }

    public void createRandomProcesses(int count) {
        System.out.println("SIMULATOR: Solicitud para crear " + count + " procesos aleatorios CORTOS...");
        for (int i = 0; i < count; i++) {
            String name = "Proc_Rand_S" + (globalCycle + i);
            // Genera un número entre 0 y 29, luego suma 1. Rango: 1 a 30.
            int instructions = 1 + randomGenerator.nextInt(30);

            String type;
            int cyclesForException = 0;
            int satisfyCycles = 0;
            if (randomGenerator.nextInt(10) < 3) {
                type = "I/O Bound";
                int maxExcCycle = Math.max(1, instructions / 2);
                cyclesForException = 1 + randomGenerator.nextInt(maxExcCycle);
                satisfyCycles = 5 + randomGenerator.nextInt(21);
            } else {
                type = "CPU Bound";
            }
            createProcessFromUI(name, instructions, type, cyclesForException, satisfyCycles);
        }
        System.out.println("SIMULATOR: " + count + " procesos aleatorios CORTOS añadidos a la New Queue.");
    }

    // Método para que el 'main' pueda conectarlos
    public void setGui(JFrame_principal gui) {
        this.gui = gui;
    }

}
