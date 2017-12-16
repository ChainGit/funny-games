package com.chain.game.retrosnaker;

import java.util.Random;

/**
 * 苹果类：一次产生一个苹果,且只有一个苹果
 * 
 * @author Chain
 *
 */
public class Apple {

	// 苹果的位置
	private int x;
	private int y;
	// 网格参数
	private State[][] sat;
	private int rows;
	private int columns;
	// 苹果
	private static Apple ap;

	private Apple() {

	}

	private Apple(State[][] s) {
		init(s);
	}

	// 初始化
	private void init(State[][] s) {
		if (sat == null) {
			sat = s;
			rows = sat[0].length;
			columns = sat.length;
		}
	}

	// 获得一个苹果对象并产生一个苹果位置，单例设计模式
	public static Apple getApple(State[][] s) {
		if (ap == null)
			ap = new Apple(s);
		if (ap != null)
			ap.make();
		return ap;
	}

	// 生成新的苹果(只有一个苹果,只要重新生成位置就行)
	public void make() {
		if (ap == null)
			throw new RuntimeException("apple is null");

		if (sat[x][y] == State.APPLE)
			sat[x][y] = State.BLANK;

		Random r = new Random();
		while (true) {
			x = r.nextInt(columns);
			y = r.nextInt(rows);
			State t = sat[x][y];
			if (t == State.BLANK) {
				sat[x][y] = State.APPLE;
				break;
			}
		}
	}

}
