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

    private final CustomQueue<PCB> newQueue;
    private final CustomQueue<PCB> readyQueue;
    private final CustomQueue<PCB> blockedQueue;
    private final CustomQueue<PCB> finishedQueue;
    private final CustomQueue<PCB> readySuspendedQueue;
    private final CustomQueue<PCB> blockedSuspendedQueue;

    private volatile int globalCycle;  // Mi reloj Global para cada ciclo 
    private volatile int cycleDurationMs;  // Duración del ciclo de ejecución en mi simulación

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
        while (true) {
            try {

                // Ejecutar toda la lógica de un ciclo (planificar, mover procesos, etc.)
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

}
