/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package helpers;
import main.classes.PCB;
import main.classes.PCB.ProcessState;

/**
 *
 * @author fabys
 */

public class RoundRobin {
    private static final int QUANTUM = 4;
    
    public static void planificar(PCB[] procesos) {
        CustomQueue<PCB> colaListos = new CustomQueue<>();
        
        for (PCB p : procesos) {
            p.setState(ProcessState.READY);
            colaListos.enqueue(p);
        }

        int tiempoTotal = 0;
        
        System.out.println("=== ROUND ROBIN - Quantum Fijo: " + QUANTUM + " ===");
        System.out.println("Procesos en cola: " + colaListos.size());

        while (!colaListos.isEmpty()) {
            PCB procesoActual = colaListos.dequeue();
            procesoActual.setState(ProcessState.RUNNING);
            
            int instruccionesAEjecutar = Math.min(procesoActual.getRemainingInstructions(), QUANTUM);
            
            int pcInicial = procesoActual.getProgramCounter();
            procesoActual.setProgramCounter(pcInicial + instruccionesAEjecutar);
            procesoActual.setRemainingInstructions(procesoActual.getRemainingInstructions() - instruccionesAEjecutar);
            procesoActual.setTimeInCpu(procesoActual.getTimeInCpu() + instruccionesAEjecutar);
            
            tiempoTotal += instruccionesAEjecutar;

            System.out.println("\n[T=" + tiempoTotal + "] " + procesoActual.getProcessName() + 
                             " ejecutó " + instruccionesAEjecutar + " instrucciones" +
                             " (PC: " + pcInicial + "→" + procesoActual.getProgramCounter() + ")");

            if (procesoActual.getRemainingInstructions() <= 0) {
                procesoActual.setState(ProcessState.FINISHED);
                System.out.println("✓ " + procesoActual.getProcessName() + " TERMINADO");
            } else {
                procesoActual.setState(ProcessState.READY);
                colaListos.enqueue(procesoActual);
                System.out.println("↻ " + procesoActual.getProcessName() + 
                                 " vuelve a cola (" + procesoActual.getRemainingInstructions() + " inst. restantes)");
            }
        }

        System.out.println("\n=== TODOS LOS PROCESOS TERMINADOS ===");
        System.out.println("Tiempo total de ejecución: " + tiempoTotal);
    }
    
   
}