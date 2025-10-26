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
    
    public PCB selectNextProcess(CustomQueue<PCB> readyQueue, int globalCycle) {
        if (readyQueue.isEmpty()) {
            return null; 
        }
        switch (currentPolicy) {
            case "SJF":
                System.out.println("PLANNER: SJF selected (using FCFS logic for now).");
                return selectSJF(readyQueue); 
            case "SRT":
                System.out.println("PLANNER: SRT selected (using FCFS logic for now).");
                return selectSRT(readyQueue);
            case "HRRN":
                System.out.println("PLANNER: HRRN selected (using FCFS logic for now).");
                return selectHRRN(readyQueue, globalCycle);
             case "MLQ":
                System.out.println("PLANNER: MLQ selected (using FCFS logic for now).");
                return selectFCFS(readyQueue);
             case "MLFQ":
                System.out.println("PLANNER: MLFQ selected (using FCFS logic for now).");
                return selectMLFQ_Priority(readyQueue);

            case "FCFS": 
            case "RR":  
            default:
                System.out.println("PLANNER: FCFS/RR selected.");
                return selectFCFS(readyQueue);
        }
    }
        
    private PCB selectFCFS(CustomQueue<PCB> readyQueue) {
        return readyQueue.peek(); 
    }

    

  public void dispatchNextProcess(CustomQueue<PCB> readyQueue, int globalCycle) {
    
    if (!CPU.getInstance().isAvailable()) {
        return; 
    }
    
    PCB selectedPCB = selectNextProcess(readyQueue, globalCycle); 

    if (selectedPCB != null) {
        
        if (currentPolicy.equals("FCFS") || currentPolicy.equals("RR")) {
            readyQueue.dequeue();
        }

        CPU.getInstance().loadProcess(selectedPCB);
    }
}

    
    // 2. Shortest Job First (SJF) - No apropiativo
    private PCB selectSJF(CustomQueue<PCB> readyQueue) {
        PCB shortestJob = null;
        int minRemainingInstructions = Integer.MAX_VALUE;

        for (PCB pcb : readyQueue.iterable()) {
            int remaining = pcb.getTotalInstructions();
            if (remaining < minRemainingInstructions) {
                minRemainingInstructions = remaining;
                shortestJob = pcb;
            }
        }
        
        return shortestJob; 
    }
    
    // Shortest Remaining Time (SRT)
    private PCB selectSRT(CustomQueue<PCB> readyQueue) {
        return selectSJF(readyQueue); 
    }
    
    // 4. Highest Response Ratio Next (HRRN) - No apropiativo
    private PCB selectHRRN(CustomQueue<PCB> readyQueue, long globalCycle) {
        PCB bestCandidate = null;
        double maxResponseRatio = -1.0;
        
        for (PCB pcb : readyQueue.iterable()) {
            double serviceTime = (double) pcb.getRemainingInstructions();
            
            // Asumiendo que has añadido `timeArrivedReady` a PCB y el Simulator lo actualiza
            long waitingTime = globalCycle - pcb.getTimeArrivedReady(); 
            
            if (serviceTime <= 0) continue; 

           double responseRatio = ((double)waitingTime + serviceTime) / serviceTime;
           
            if (responseRatio > maxResponseRatio) {
               maxResponseRatio = responseRatio;
                bestCandidate = pcb;
            }
       }
        
        return bestCandidate; // Se elimina en selectNextProcess si es HRRN
    }
    
    
    
    // Multi-Level Feedback Queue (MLFQ) / Prioridad
    private PCB selectMLFQ_Priority(CustomQueue<PCB> readyQueue) {
        PCB highestPriorityProcess = null;
        int minPriorityValue = Integer.MAX_VALUE; 

        for (PCB pcb : readyQueue.iterable()) {
            if (pcb.getPriority() < minPriorityValue) {
                minPriorityValue = pcb.getPriority();
                highestPriorityProcess = pcb;
            } else if (pcb.getPriority() == minPriorityValue) {
                if (highestPriorityProcess == null) {
                    highestPriorityProcess = pcb;
                }
            }
        }
        
        return highestPriorityProcess;
    }
}




