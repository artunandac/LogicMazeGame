
public class Stack {
    private int top;
    private Object[] elements;

    Stack(int capacity) {
        elements = new Object[capacity];
        top = -1;
    }

    boolean isEmpty() {
        return top == -1;
    }

    boolean isFull() {
        return top + 1 == elements.length;
    }

    void push(Object data) {
        if(!isFull()) {
            top++;
            elements[top] = data;
        }
    }

    Object pop() {
        if(!isEmpty()) {
            Object data = elements[top];
            elements[top] = null;
            top--;
            return data;
        }
        else {
            return null;
        }
    }

    Object peek() {
        if(!isEmpty()) {
            return elements[top];
        }
        else {
            return null;
        }
    }

    int size() {
        return top + 1;
    }
}