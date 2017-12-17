package com.chain.test.day11;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.util.List;

/**
 * 球的绘制
 * 
 * @author chain
 *
 */
public class CircleCanvas extends Canvas {

	private static final long serialVersionUID = 1L;
	private int width;
	private int height;

	private Image imgBuf;
	private Graphics imgGraphics;

	private volatile List<Circle> circles;

	public CircleCanvas(int width, int height) {
		this.width = width;
		this.height = height;
	}

	public void setCircles(List<Circle> circles) {
		this.circles = circles;
	}

	// canvas双缓冲
	@Override
	public void update(Graphics globalGraphics) {
		// 设置画笔颜色
		imgGraphics.setColor(this.getBackground());
		// 清空画布（填满背景颜色）
		imgGraphics.fillRect(0, 0, width, height);
		// 绘制圆
		drawCircles();
		// 将画布上已经绘制好的图形绘制到界面上去
		globalGraphics.drawImage(imgBuf, 0, 0, this);
	}

	// 初始化
	public void init() {
		// 初始化图形
		if (imgBuf == null) {
			imgBuf = this.createImage(width, height);
			imgGraphics = imgBuf.getGraphics();
		}
	}

	private void drawCircles() {
		// 设置画笔颜色
		imgGraphics.setColor(Color.RED);
		int size = circles.size();
		for (int i = 0; i < size; i++) {
			// 画圆
			Circle c = circles.get(i);
			int radius = c.getRadius();
			int diameter = c.getDiameter();
			imgGraphics.drawOval(c.getLocationX() - radius, c.getLocationY() - radius, diameter, diameter);
		}
	}

}
