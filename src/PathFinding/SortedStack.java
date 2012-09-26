
package PathFinding;

import java.util.AbstractQueue;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;

/**
 *
 * @author G. Meier
 */
@SuppressWarnings("unchecked")
public class SortedStack<E> extends AbstractQueue<E> {

    private Object[] data;
    private int numElements;
    private int incrementSize = 10;
    private boolean isSorted  = false;

    public SortedStack() {
        data        = new Object[incrementSize];
        numElements = -1;
    }

    SortedStack(int i) {
        data        = new Object[i];
        numElements = -1;
    }

    @Override
    public int size() {
        return numElements+1;
    }

    public boolean offer(E e) {
        if(numElements == data.length-1) {
            Object[] tmp = new Object[numElements + incrementSize];
            System.arraycopy(data, 0, tmp, 0, numElements);
            data = tmp;
        }
        data[++numElements] = e;

        isSorted = false;
        return true;
    }

    public E poll() {

        if(numElements < 0) {
            return null;
        }

        internalSort();
        return (E) data[numElements--];
    }

    public E peek() {
        internalSort();
        return (E) data[numElements];
    }

    private void internalSort() {
        if(!isSorted) {
            Arrays.sort(data, 0, numElements+1, Collections.reverseOrder());
            isSorted = true;
        }
    }

    @Override
    public Iterator<E> iterator() {
        throw new UnsupportedOperationException("Mr G. Meier is too lazy to implement all functions.");
    }

}
