import java.util.Arrays;
import java.util.ArrayList;
import java.util.Random;

/**
 * Tis class is responsible for providing help as a good mix between a Queue and Array.
 * <p>
 * A C++ example: http://gameprogrammingpatterns.com/event-queue.html#what-goes-in-the-queue
 *
 * @author Timo Strating
 */
public class FixedRingArray {

	public boolean debug = false;

	private int head = 0;
	private int tail = 0;
	private float[] elements;
	public boolean hasBeanRound = false;

	/**
	 * The standard constructor for creating a FixedRingArray.
	 *
	 * @param length the length of the FixedArray.
	 */
	public FixedRingArray() {
		elements = new float[31];
	}

	/**
	 * Get the Length of the stored data. We use size because we need to calculate the value.
	 *
	 * @return the length of the stored data.
	 */
	public int size() {
		if (tail > head)
			return tail - head;
		else
			return elements.length - (tail - head);
	}

	/**
	 * Add a value to the array, if there is no space left on the right side of the array than the array will start to loop around on itself
	 * In the case that the array has reached it maximum size than the array will override the fist value with the last.
	 */
	public void put(float value) {
		if (hasBeanRound) {
			if ((tail) % (elements.length) == head) {
				head = (head + 1) % elements.length;
			}
		} else if ((tail + 1) % (elements.length) == 0) {
			hasBeanRound = true;
		}

		elements[(tail++) % (elements.length)] = value;
		tail %= elements.length;


	}

	/**
	 * Get a value out of the float array.
	 *
	 * @param index the N item you want. may cause index out of range Exception, so check te size beforehand.
	 * @return the value out of the float array.
	 */
	public float get(int index) {
		return elements[(head + index) % elements.length];
	}

	/**
	 * helper for converting the data to a string.
	 *
	 * @return the array as text.
	 */
	@Override
	public String toString() {
		return Arrays.toString(elements);
	}


	/**
	 * This is a test to visually show how the FixedRingArray works
	 *
	 * @param args
	 */
	public static void main(String[] args) {
		FixedRingArray[] queues = new FixedRingArray[8000];

		for (int i = 0; i < 8000; i++) {
			queues[i] = new FixedRingArray();
		}
		// Random r = new Random();
		// for (int station = 0; station < 100000; station++) {
		// 	queues[r.nextInt(8000)].put(r.nextInt(50));
		// }
		//
		// System.out.println();
		//
		// for (int bla = 0; bla < 100000; bla++) {
		// 	for (int i=0; i<queues[0].size(); i++) {
		// 		System.out.println(queues[0].get(i));
		// 	}
		// }
	}

	/**
	 * test methode
	 *
	 * @param value the value that you are currently testing
	 */
	public void test(float value) {
		System.out.println("hasBeanRound: " + hasBeanRound + "\t\t value: " + value + "\t\t tail: " + tail + "\t\t head: " + head + "\t" + toString());
	}

}
