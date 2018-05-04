/**
 * 
 */
package com.guttv.pm.utils;

/**
 * @author Peter
 *
 */
public class Fibonacci {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println(System.currentTimeMillis());
		System.out.println(fibonacci(180));
		System.out.println(System.currentTimeMillis());
		System.out.println(fibonacci2(180));
		System.out.println(System.currentTimeMillis());
	}

	//费博那契数列 递归 n大于40后，将很难算出来
	public static int fibonacci(int n) {
		return n < 1 ? 0 : (n > 2 ? (fibonacci(n - 1) + fibonacci(n - 2)) : 1);
	}
	
	//费博那契数列 循环  比递归方法快很多  当n>40时，快5个以上数量级
	public static long fibonacci2(int n) {
		if(n<1) return 0;
		
		if(n<3) return 1;
		
		long sum = 0;
		long before = 1;
		long preBefore = 1;
		int current = 3;
		while(current++<=n) {
			sum = before + preBefore;
			preBefore = before;
			before = sum;
		}
		return sum;
	}
}
