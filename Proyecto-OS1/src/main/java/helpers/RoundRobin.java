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
    public static void planificar(PCB[] procesosIniciales, int quantum) {
        
        // 1. Inicialización de la Cola de Listos
        CustomQueue<PCB> colaListos = new CustomQueue<>();
        
        // Establecer el estado inicial y añadir a la cola
        for (PCB p : procesosIniciales) {
            p.setState(ProcessState.READY); // Lo ponemos en estado listo
            colaListos.enqueue(p); 
        }

        int tiempoActual = 0;
        System.out.println("--- INICIO SIMULACIÓN ROUND ROBIN (Quantum = " + quantum + ") ---");
        System.out.println("Procesos en Ready Queue: " + colaListos.size());

        // 2. El Ciclo de Ejecución
        while (!colaListos.isEmpty()) {
            
            // 2.1. Selección: Sacar el PCB de la cabeza (FRONT)
            PCB pcb = colaListos.dequeue(); 
            pcb.setState(ProcessState.RUNNING);
            
            // 2.2. Determinación del Tiempo de Ejecución (MIN(Instrucciones Restantes, Quantum))
            int instruccionesARealizar = Math.min(pcb.getRemainingInstructions(), quantum);
            
            // 2.3. Simulación y Actualización del PCB
            
            // a) Ejecución de las instrucciones:
            int pcInicial = pcb.getProgramCounter();
            pcb.setProgramCounter(pcInicial + instruccionesARealizar);
            
            // b) Actualización del tiempo y las instrucciones:
            pcb.setRemainingInstructions(pcb.getRemainingInstructions() - instruccionesARealizar);
            pcb.setTimeInCpu(pcb.getTimeInCpu() + instruccionesARealizar);
            
            // c) Actualización del tiempo total de simulación:
            tiempoActual += instruccionesARealizar;

            System.out.println("\n[T:" + tiempoActual + "] Ejecutando: " + pcb.getProcessName() + " (" + 
                               pcb.getProcessID().toString().substring(0, 4) + ")");
            System.out.println("   -> Instrucciones: " + (pcInicial + 1) + " hasta " + pcb.getProgramCounter() + 
                               " (Quantum usado: " + instruccionesARealizar + ")");


            // 2.4. Comprobación (Preemptión o Finalización)
            if (pcb.getRemainingInstructions() <= 0) {
                // Caso 1: Proceso Terminado
                pcb.setState(ProcessState.FINISHED);
                System.out.println("  --> Proceso " + pcb.getProcessName() + " TERMINADO. Tiempo Total de CPU: " + pcb.getTimeInCpu());
            } else {
                // Caso 2: Preemptión (Quantum Expirado)
                // Vuelve al final de la cola (REAR)
                pcb.setState(ProcessState.READY);
                colaListos.enqueue(pcb); 
                System.out.println("  --> Proceso " + pcb.getProcessName() + 
                                   " desalojado. Restantes: " + pcb.getRemainingInstructions() + 
                                   ". Vuelve a la cola. (Estado: READY)");
            }
        }
        
        System.out.println("\n--- SIMULACIÓN FINALIZADA en " + tiempoActual + " Ciclos ---");
    
}
}