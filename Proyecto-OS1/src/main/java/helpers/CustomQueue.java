/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package helpers;

/**
 *
 * @author cehernandez
 */

import java.util.Iterator;
import java.util.NoSuchElementException;
 
public class CustomQueue<T> {

    private Node<T> front; // El primer nodo de la cola (cabeza)
    private Node<T> rear;  // El último nodo de la cola (cola)
    private int size;      // El número de elementos en la cola

    /**
     * Constructor para inicializar una cola vacía.
     */
    public CustomQueue() {
        this.front = null;
        this.rear = null;
        this.size = 0;
    }

    /**
     * Añade un elemento al final (rear) de la cola.
     *
     * @param data El elemento a encolar.
     */
    public void enqueue(T data) {           //ENCOLAR
        Node<T> newNode = new Node<>(data);

        if (isEmpty()) {
            front = newNode;
            rear = newNode;
        } else {
            rear.next = newNode;
            rear = newNode;
        }
        size++;
    }

    /**
     * Elimina y devuelve el elemento del frente (front) de la cola.
     *
     * @return El elemento que estaba al frente de la cola, o null si la cola
     * está vacía.
     */
    public T dequeue() {            //DESENCOLAR
        if (isEmpty()) {
            return null;
        }

        T data = front.data;
        front = front.next;

        if (front == null) {
            rear = null;
        }
        size--;
        return data;
    }

    /**
     * Devuelve el elemento del frente de la cola sin eliminarlo.
     *
     * @return El elemento al frente de la cola, o null si está vacía.
     */
    public T peek() {
        if (isEmpty()) {
            return null;
        }
        return front.data;
    }

    /**
     * Comprueba si la cola está vacía.
     *
     * @return true si la cola no tiene elementos.
     */
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * Devuelve el número de elementos en la cola.
     *
     * @return El tamaño actual de la cola.
     */
    public int size() {
        return size;
    }

    /**
     * Genera una representación en String de la cola.
     *
     * @return Un string con todos los elementos de la cola.
     */
    @Override
    public String toString() {
        if (isEmpty()) {
            return "[Vacía]";
        }
        StringBuilder sb = new StringBuilder();
        Node<T> current = front;
        while (current != null) {
            sb.append(current.data.toString()).append("\n");
            current = current.next;
        }
        return sb.toString();
    }

    /**
     * Devuelve un objeto Iterable que puede ser usado en un bucle for-each.
     * Esto permite recorrer la cola sin exponer su estructura interna.
     *
     * @return Un objeto Iterable para esta cola.
     */
    public Iterable<T> iterable() {
        return new Iterable<T>() {
            @Override
            public Iterator<T> iterator() {
                return new Iterator<T>() {
                    private Node<T> current = front;

                    @Override
                    public boolean hasNext() {
                        return current != null;
                    }

                    @Override
                    public T next() {
                        if (!hasNext()) {
                            throw new NoSuchElementException();
                        }
                        T data = current.data;
                        current = current.next;
                        return data;
                    }
                };
            }
        };
    }

    public boolean remove(T dataToRemove) {
        if (isEmpty() || dataToRemove == null) {
            return false;
        }
        if (front.data.equals(dataToRemove)) {
            dequeue();
            return true;
        }
        Node<T> current = front;
        while (current.next != null) {
            if (current.next.data.equals(dataToRemove)) {
                Node<T> nodeToRemove = current.next;
                current.next = nodeToRemove.next;
                if (nodeToRemove == rear) {
                    rear = current;
                }
                size--;
                return true;
            }
            current = current.next;
        }
        return false;
    }
}
