package com.chain.test.day11;

import static java.lang.Math.pow;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class CirclesThread extends Thread {

	private volatile List<Circle> circles;

	private int width;
	private int height;

	public CirclesThread(int width, int height) {
		this.width = width;
		this.height = height;
		initCircles();
	}

	private void initCircles() {
		circles = new ArrayList<>(Main.CIRCLE_MAX_AMOUNT);
		new CirclesManageThread().start();
	}

	private class CirclesManageThread extends Thread {

		public void run() {
			Random r = new Random();
			while (circles.size() < Main.CIRCLE_MAX_AMOUNT) {
				try {
					Thread.sleep(Main.CIRCLE_SPEED);
				} catch (InterruptedException e) {
					throw new RuntimeException(e);
				}

				Circle c = new Circle(width, height);
				c.setWeight(Main.CIRCLE_WEIGHT);
				c.setRadius(Main.CIRCLE_RADIUS);
				// 保证小球不会初始时就碰撞（叠加）在一起
				c.setLocation(Main.CIRCLE_RADIUS + 10, Main.CIRCLE_RADIUS + 10);
				// 初始的时候都是向右下角运动
				c.setSpeed(r.nextInt(Main.CIRCLE_MAX_SPEED) + Main.CIRCLE_MIN_SPEED,
						r.nextInt(Main.CIRCLE_MAX_SPEED) + Main.CIRCLE_MIN_SPEED);
				// 只有add操作，只要注意size的使用就无需对circles加锁
				circles.add(c);
			}
		}
	}

	public List<Circle> getCircles() {
		return circles;
	}

	@Override
	public void run() {
		while (true) {
			int size = circles.size();
			for (int i = 0; i < size; i++) {
				circles.get(i).move();
				// 每一个小球移动一次就检测它和其他小球是否碰撞在一起（防止一个小球同时碰到多个球的情况）
				for (int j = 0; j < size; j++)
					if (i != j)
						circles.get(i).collide(circles.get(j));
			}

			calc();

			try {
				Thread.sleep(Main.MOVE_SPEED);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		}
	}

	// 计算总动量
	private void calc() {
		double e = 0.0d;
		int size = circles.size();
		System.out.printf("id\tvx\tvy\tpx\tpy\tlx\tly\n");
		for (int i = 0; i < size; i++) {
			Circle c = circles.get(i);
			double vx = c.getSpeedX();
			double vy = c.getSpeedY();
			int m = c.getWeigth();
			System.out.printf("%d\t%.2f\t%.2f\t%.2f\t%.2f\t%d\t%d\n", i, vx, vy, c.getGatherX(), c.getGatherY(),
					c.getLocationX(), c.getLocationY());
			e += m * (pow(vx, 2) + pow(vy, 2)) / 2;
		}
		System.out.printf("e: %.2f\n", e);
		System.out.println("---------------------------");
	}
}
