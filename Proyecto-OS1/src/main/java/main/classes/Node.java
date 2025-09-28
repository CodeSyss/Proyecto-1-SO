/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package main.classes;

/**
 *
 * @author fabys
 */
public class Node<T> {
     T data;
    Node<T> next;

    public Node(T data) {
        this.data = data;
        this.next = null; // Inicialmente no apunta a nada
    }
    
}
