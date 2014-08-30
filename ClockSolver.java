package cs.clock;

/**
 *   1 2 3    -3 10 -1
 *   4 5 6    11 12 13
 *   7 8 9    -9 14 -7
 *  (front)    (back)
 */
public class ClockSolver {
	static int[][] moveArr = {
		{ 0, 1, 1, 0, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0},	//UR
		{ 0, 0, 0, 0, 1, 1, 0, 1, 1, 0, 0, 0, 0, 0},	//DR
		{ 0, 0, 0, 1, 1, 0, 1, 1, 0, 0, 0, 0, 0, 0},	//DL
		{ 1, 1, 0, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0},	//UL
		{ 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0},	//U
		{ 0, 1, 1, 0, 1, 1, 0, 1, 1, 0, 0, 0, 0, 0},	//R
		{ 0, 0, 0, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0},	//D
		{ 1, 1, 0, 1, 1, 0, 1, 1, 0, 0, 0, 0, 0, 0},	//L
		{ 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0},	//ALL
		{11, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 1, 1, 0},	//UR
		{ 0, 0, 0, 0, 0, 0,11, 0, 0, 0, 0, 1, 1, 1},	//DR
		{ 0, 0, 0, 0, 0, 0, 0, 0,11, 0, 1, 1, 0, 1},	//DL
		{ 0, 0,11, 0, 0, 0, 0, 0, 0, 1, 1, 1, 0, 0},	//UL
		{11, 0,11, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 0},	//U
		{11, 0, 0, 0, 0, 0,11, 0, 0, 1, 0, 1, 1, 1},	//R
		{ 0, 0, 0, 0, 0, 0,11, 0,11, 0, 1, 1, 1, 1},	//D
		{ 0, 0,11, 0, 0, 0, 0, 0,11, 1, 1, 1, 0, 1},	//L
		{11, 0,11, 0, 0, 0,11, 0,11, 1, 1, 1, 1, 1}		//ALL
	};

	static int[][] Cnk = new int[32][32];

	static {
		for (int i=0; i<32; i++) {
			Cnk[i][i] = 1;
			Cnk[i][0] = 1;
		}
		for (int i=1; i<32; i++) {
			for (int j=1; j<=i; j++) {
				Cnk[i][j] = Cnk[i-1][j] + Cnk[i-1][j-1];
			}
		}

	}

	static int select(int n, int k, int idx) {
		int r = k;
		int val = 0;
		for (int i=n-1; i>=0; i--) {
			if (idx >= Cnk[i][r]) {
				idx -= Cnk[i][r--];
				val |= 1 << i;
			}
		}
		return val;
	}

	//invert table          0  1  2  3  4  5  6  7  8  9 10 11
	static int[] invert = {-1, 1,-1,-1,-1, 5,-1, 7,-1,-1,-1,11};

	//test
	public static void main(String[] args) {
		java.util.Random r = new java.util.Random(42L);
		int[] arr = new int[14];
		int[] solution = new int[18];
		long start = System.nanoTime();
		int mvsum = 0;
		for (int n_solves=0; n_solves<10000; n_solves++) {
			for (int i=0; i<14; i++) {
				arr[i] = r.nextInt(12);
			}
			int nz = solveIn(14, arr, solution);
			mvsum += nz;

			//check isSolved
			int[] clk = new int[14];
			for (int i=0; i<18; i++) {
				if (solution[i] == 0) {
					continue;
				}
				for (int j=0; j<14; j++) {
					clk[j] += solution[i] * moveArr[i][j];
				}
			}
			for (int i=0; i<14; i++) {
				if (clk[i] % 12 != arr[i]) {
					System.out.println("ERROR");
				}
			}

			System.out.print(String.format("%3f", (System.nanoTime() - start) / 1e6 / (n_solves+1)) + "\t\t\t" + mvsum / 1.0 / (n_solves + 1) + "\r");
		}
		System.out.println((System.nanoTime() - start) / 1e6 / 100);
		System.out.println(mvsum / 1e2);
	}

	public static int[] randomState(java.util.Random r) {
		int[] ret = new int[14];
		for (int i=0; i<14; i++) {
			ret[i] = r.nextInt(12);
		}
		return ret;
	}

	/**
	 *	
	 *	@return the length of the solution (the number of non-zero elements in the solution array)
	 *		-1: invalid input
	 */
	public static int Solution(int[] clock, int[] solution) {
		if (clock.length != 14 || solution.length != 18) {
			return -1;
		}
		int ret = solveIn(14, clock, solution);
		return ret;
	}

	static void swap(int[][] arr, int row1, int row2) {
		int[] tmp = arr[row1];
		arr[row1] = arr[row2];
		arr[row2] = tmp;
	}

	static void addTo(int[][] arr, int row1, int row2, int startidx, int mul) {
		int length = arr[0].length;
		for (int i=startidx; i<length; i++) {
			arr[row2][i] = (arr[row2][i] + arr[row1][i] * mul) % 12;
		}
	}

	//linearly dependent
	static int[] ld_list = {7695, 42588, 47187, 85158, 86697, 156568, 181700, 209201, 231778};

	static int solveIn(int k, int[] numbers, int[] solution) {
		int n = 18;
		int min_nz = k+1;

		for (int idx=0; idx<Cnk[n][k]; idx++) {
			int val = select(n, k, idx);
			boolean isLD = false;
			for (int r: ld_list) {
				if ((val & r) == r) {
					isLD = true;
					break;
				}
			}
			if (isLD) {
				continue;
			}
			int[] map = new int[k];
			int cnt = 0;
			for (int j=0; j<n; j++) {
				if (((val >> j) & 1) == 1) {
					map[cnt++] = j;
				}
			}
			int[][] arr = new int[14][k+1];
			for (int i=0; i<14; i++) {
				for (int j=0; j<k; j++) {
					arr[i][j] = moveArr[map[j]][i];
				}
				arr[i][k] = numbers[i];
			}
			int ret = GaussianElimination(arr);
			if (ret != 0) {
				continue;
			}
			boolean isSolved = true;
			for (int i=k; i<14; i++) {
				if (arr[i][k] != 0) {
					isSolved = false;
					break;
				}
			}
			if (!isSolved) {
				continue;
			}
			backSubstitution(arr);
			int cnt_nz = 0;
			for (int i=0; i<k; i++) {
				if (arr[i][k] != 0) {
					cnt_nz++;
				}
			}
			if (cnt_nz < min_nz) {
				for (int i=0; i<18; i++) {
					solution[i] = 0;
				}
				for (int i=0; i<k; i++) {
					solution[map[i]] = arr[i][k];
				}
				min_nz = cnt_nz;
			}
		}
		return min_nz == k+1 ? -1 : min_nz;
	}

	static int GaussianElimination(int[][] arr) {
		int m = 14;
		int n = arr[0].length;
		for (int i=0; i<n-1; i++) {
			if (invert[arr[i][i]] == -1) {
				int ivtidx = -1;
				for (int j=i+1; j<m; j++) {
					if (invert[arr[j][i]] != -1) {
						ivtidx = j;
						break;
					}
				}
				if (ivtidx == -1) {
					OUT:
					for (int j1=i; j1<m-1; j1++) {
						for (int j2=j1+1; j2<m; j2++) {
							if (invert[(arr[j1][i] + arr[j2][i]) % 12] != -1) {
								addTo(arr, j2, j1, i, 1);
								ivtidx = j1;
								break OUT;
							}
						}
					}
				}
				if (ivtidx == -1) {	//k vectors are linearly dependent
					for (int j=i+1; j<m; j++) {
						if (arr[j][i] != 0) {
							return -1;
						}
					}
					return i + 1;					
				}
				swap(arr, i, ivtidx);
			}
			int inv = invert[arr[i][i]];
			for (int j=i; j<n; j++) {
				arr[i][j] = arr[i][j] * inv % 12;
			}
			for (int j=i+1; j<m; j++) {
				addTo(arr, i, j, i, 12 - arr[j][i]);
			}
		}
		return 0;
	}

	static void backSubstitution(int[][] arr) {
		int m = 14;
		int n = arr[0].length;
		for (int i=n-2; i>0; i--) {
			for (int j=i-1; j>=0; j--) {
				if (arr[j][i] != 0) {
					addTo(arr, i, j, i, 12 - arr[j][i]);
				}
			}
		}
	}
}