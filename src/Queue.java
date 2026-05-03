public class Queue
{
    private int rear, front, count;
    private Object[] elements;
    private int capacity;

    Queue(int capacity)
    {
        this.capacity = capacity;
        elements = new Object[capacity];
        rear = -1;
        front = 0;
        count = 0;
    }

    void enqueue(Object data)
    {
        if (isFull())
            System.out.println("Queue overflow");
        else
        {
            rear = (rear + 1) % capacity;
            elements[rear] = data;
            count++;
        }
    }

    Object dequeue()
    {
        if (isEmpty())
        {
            System.out.println("Queue is empty");
            return null;
        }
        else
        {
            Object retData = elements[front];
            elements[front] = null;
            front = (front + 1) % capacity;
            count--;
            return retData;
        }
    }

    Object peek()
    {
        if (isEmpty())
        {
            System.out.println("Queue is empty");
            return null;
        }
        else
            return elements[front];
    }

    boolean isEmpty()
    {
        return count == 0;
    }

    boolean isFull()
    {
        return count == capacity;
    }

    int size()
    {
        return count;
    }
}
