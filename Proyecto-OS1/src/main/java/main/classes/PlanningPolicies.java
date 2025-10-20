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

    public PlanningPolicies(CustomQueue<PCB> readyQueue) {
        this.readyQueue = readyQueue;
    }
}
