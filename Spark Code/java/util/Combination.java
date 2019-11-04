package util;

public class Combination {
	private	long n;
	private	long k;
	private	long[] data;
	private static long LargestV(long a, long b, long x) throws Exception {
		//		System.out.println("Largest v " + a + " " + b + " " + x);
		long v = a - 1;

		while (Choose(v,b) > x)
			--v;

		return v;
	}
	public
	Combination(long n, long k) throws Exception {
		if (n < 0 || k < 0) // normally n >= k
			throw new Exception("Negative parameter in constructor");
		this.n = n;
		this.k = k;
		this.data = new long[(int) k];
		for (int i = 0; i < k; ++i)
			this.data[i] = i;
	}
	public	Combination(long n, long k, long[] a, long k_array) throws Exception {
		if (k != k_array)
			throw new Exception( "Array length does not equal k" );
		this.n = n;
		this.k = k;
		this.data = new long[(int) k];
		for (int i = 0; i < k_array; ++i)
			this.data[i] = a[i];

		if (!this.IsValid())
			throw new Exception ( "Bad value from array! " + n + " " + k + " " + data[0] + data[1] + data[2] );
	}
	public	boolean IsValid() {
		for (int i = 0; i < this.k; ++i)
		{
			if (this.data[i] < 0 || this.data[i] > this.n - 1)
				return false; // value out of range

			for (int j = i+1; j < this.k; ++j)
				if (this.data[i] >= this.data[j])
					return false; // duplicate or not lexicographic
		}
		return true;
	}
	public	String ToString() {
		String s = "{ ";
		for (int i = 0; i < this.k; ++i) {
			s = s + this.data[i] + " " ;
		}
		s += "}";
		return s;
	}
	public Combination Successor() throws Exception {
		if (this.data[0] == this.n - this.k)
			throw new Exception ("exception");

		Combination ans = new Combination(this.n, this.k);

		int i;
		for (i = 0; i < this.k; ++i)
			ans.data[i] = this.data[i];

		for (i = (int) (this.k - 1); i > 0 && ans.data[i] == this.n - this.k + i; --i)
			;

		++ans.data[i];

		for (int j = i; j < this.k - 1; ++j)
			ans.data[j+1] = ans.data[j] + 1;

		return ans;
	}
	public	static long Choose(long n, long k) throws Exception {

		if (n < 0 || k < 0)
			throw new Exception ("Invalid negative parameter in Choose() " + n + " " + k);
		if (n < k)
			return 0;  // special case
		if (n == k)
			return 1;

		long delta, iMax;

		if (k < n-k) // ex: Choose(100,3)
		{
			delta = n-k;
			iMax = k;
		}
		else         // ex: Choose(100,97)
		{
			delta = k;
			iMax = n-k;
		}

		long ans = delta + 1;

		for (long i = 2; i <= iMax; ++i)
		{
			ans = (ans * (delta + i)) / i;
		}
		return ans;
	}
	
	public	Combination Element(long m) throws Exception {
		long[] ans = new long[(int) this.k];

		long a = this.n;
		long b = this.k;
		long x = (Choose(this.n, this.k) - 1) - m; // x is the "dual" of m

		for (int i = 0; i < this.k; ++i)
		{
			ans[i] = LargestV(a,b,x); // largest value v, where v < a and vCb < x
			x = x - Choose(ans[i],b);
			a = ans[i];
			b = b-1;
		}

		for (int i = 0; i < this.k; ++i)
		{
			ans[i] = (n-1) - ans[i];
		}

		return new Combination(this.n, this.k, ans, this.k);
	}

	public	long [] getData() {
		return data;
	}
};
