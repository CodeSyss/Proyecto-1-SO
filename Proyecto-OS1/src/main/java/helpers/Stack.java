/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package helpers;

/**
 *
 * @author fabys
 */
public class Stack<T> {
    private Node<T> top;
    private int size; 
    
     public Stack() {
        this.top = null;
        this.size = 0;
    }
     
     
    public void push(T data) {
        Node<T> newNode = new Node<>(data);

        newNode.next = top;

        top = newNode;

        size++;
        System.out.println("PUSH: " + data + " agregado. Tamaño: " + size);
    }

    public T pop() {
        if (isEmpty()) {
            throw new RuntimeException("Error: Pila Vacía (Stack Underflow).");
        }

        T data = top.data;
        top = top.next;

        size--;
        System.out.println("POP: " + data + " extraído. Tamaño: " + size);
        return data;
    }

    public T peek() {
        if (isEmpty()) {
            throw new RuntimeException("Error: Pila Vacía.");
        }
        return top.data;
    }

    public boolean isEmpty() {
        return top == null;
    }


    public int size() {
        return size;
    }
    
}
