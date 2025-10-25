/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package main.classes;

import helpers.CustomQueue;

/**
 *
 * @author cehernandez
 */
public class PlanningPolicies {
    
    public PlanningPolicies() {
      
    }
 
    private String currentPolicy = "FCFS"; // Política por defecto


    public void setPolicy(String policyCode) {
        this.currentPolicy = policyCode;
    }
    
    public PCB selectNextProcess(CustomQueue<PCB> readyQueue) {
        if (readyQueue.isEmpty()) {
            return null; 
        }

        switch (currentPolicy) {
            case "SJF":
                // return selectSJF(readyQueue); // A implementar
                System.out.println("PLANNER: SJF selected (using FCFS logic for now).");
                return selectFCFS(readyQueue); // Usa FCFS mientras no esté listo
            case "SRT":
                // return selectSRT_Candidate(readyQueue); // A implementar
                System.out.println("PLANNER: SRT selected (using FCFS logic for now).");
                return selectFCFS(readyQueue); // Usa FCFS mientras no esté listo
            case "HRRN":
                // return selectHRRN(readyQueue); // A implementar
                System.out.println("PLANNER: HRRN selected (using FCFS logic for now).");
                return selectFCFS(readyQueue); // Usa FCFS mientras no esté listo
             case "MLQ":
                // Lógica más compleja requerida
                System.out.println("PLANNER: MLQ selected (using FCFS logic for now).");
                return selectFCFS(readyQueue);
             case "MLFQ":
                 // Lógica más compleja requerida
                System.out.println("PLANNER: MLFQ selected (using FCFS logic for now).");
                return selectFCFS(readyQueue);

            case "FCFS": // FCFS (FIFO)
            case "RR":   // Round Robin también selecciona el primero
            default:
                System.out.println("PLANNER: FCFS/RR selected.");
                return selectFCFS(readyQueue);
        }
    }
        
    private PCB selectFCFS(CustomQueue<PCB> readyQueue) {
        return readyQueue.peek(); 
    }


}


