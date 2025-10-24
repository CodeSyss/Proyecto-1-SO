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

    private final CustomQueue<PCB> readyQueue;
    private String typePolicy;
    public PlanningPolicies(CustomQueue<PCB> readyQueue) {
        this.readyQueue = readyQueue;
    }

    public CustomQueue<PCB> getReadyQueue() {
        return readyQueue;
    }

    public String getTypePolicy() {
        return typePolicy;
    }

    public void setTypePolicy(String typePolicy) {
        this.typePolicy = typePolicy;
    }
    
    private String currentPolicy = "FCFS"; // Política por defecto


    public void setPolicy(String policyCode) {
        this.currentPolicy = policyCode;
    }

}


