package ac.a14ehsr.platform;

import java.util.ArrayList;
import java.util.List;

public class Permutation {
	/**
	 * 0~n-1の順列の全通りのリストを返す
	 * @param n
	 * @return
	 */
	static List<int[]> of(int n) {
		int[] number = new int[n];
		for (int i = 0; i < n; i++) {
			number[i] = i;
		}
		List<int[]> permList = new ArrayList<>();
		perm(permList, number, n, 0);
		return permList;
	}

	private static void perm(List<int[]> permList, int[] array, int size, int num) {
		int[] subArray = new int[size];
		cpyArray(array, subArray);
		if (num == size) {
			// printIntArray(array," ",""); // 順列決定
			int[] tmpArray = new int[size];
			cpyArray(array, tmpArray);
			permList.add(tmpArray);
		} else {
			perm(permList, array, size, num + 1); // 再帰

			cpyArray(subArray, array);
			for (int i = num; i > 0; i--) {
				int tmp = array[i];
				array[i] = array[i - 1];
				array[i - 1] = tmp;

				perm(permList, array, size, num + 1);
			}
			cpyArray(array, subArray);

		}
	}

	private static void cpyArray(int[] org, int[] cpy) {
		int len = org.length;
		for (int i = 0; i < len; i++)
			cpy[i] = org[i];
	}

	public static void main(String[] args) {
		List<int[]> tmp = of(Integer.parseInt(args[0]));
		for (int[] t : tmp) {
			for (int num : t) {
				System.out.print(num + " ");
			}
			System.out.println();
		}
	}
}