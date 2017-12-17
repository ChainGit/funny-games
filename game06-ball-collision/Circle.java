package com.chain.test.day11;

import static java.lang.Math.abs;
import static java.lang.Math.acos;
import static java.lang.Math.cos;
import static java.lang.Math.pow;
import static java.lang.Math.round;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;

/**
 * 球
 * 
 * @author chain
 *
 */
public class Circle {

	private int width;
	private int height;
	private int weight;
	private int radius;
	private int diameter;
	private int lx;
	private int ly;
	private double vx;
	private double vy;
	// 当vx和vy绝对值小于1时使用
	private double px;
	private double py;

	public Circle(int width, int height) {
		this.width = width;
		this.height = height;
	}

	public void setLocation(int lx, int ly) {
		this.lx = lx;
		this.ly = ly;
	}

	public void setSpeed(double vx, double vy) {
		this.vx = vx;
		this.vy = vy;
	}

	public void setWeight(int weight) {
		this.weight = weight;
	}

	public int getWeigth() {
		return this.weight;
	}

	public double getSpeedX() {
		return vx;
	}

	public double getSpeedY() {
		return vy;
	}

	public double getGatherX() {
		return px;
	}

	public double getGatherY() {
		return py;
	}

	public void setRadius(int radius) {
		this.radius = radius;
		this.diameter = radius << 1;
	}

	public int getRadius() {
		return this.radius;
	}

	public int getDiameter() {
		return this.diameter;
	}

	public int getLocationX() {
		return lx;
	}

	public int getLocationY() {
		return ly;
	}

	/**
	 * 小球沿着vx和vy移动，并对此时移动过程中撞击到墙做判断和处理（先解决球碰墙，再解决球碰球）
	 * 
	 * 每次移动一个单位的vx和vy
	 * 
	 * 需要打磨算法才能准确
	 */
	public void move() {
		// 当X轴上碰到墙时,X轴行进方向改变
		if (lx + vx + radius >= width) {
			// 只有确实是在向右墙运动时才掉头，防止和collide中的调整出现冲突，以下类似
			if (vx > 0) {
				vx *= -1;
			} else {
				if (abs(vx) < 1) {
					px += vx;
					if (abs(px) >= 1) {
						lx += round(px);
						px = 0;
					}
				} else {
					lx += vx;
					px = 0;
				}
			}
		} else if (lx + vx - radius <= 0) {
			if (vx < 0) {
				vx *= -1;
			} else {
				if (abs(vx) < 1) {
					px += vx;
					if (abs(px) >= 1) {
						lx += round(px);
						px = 0;
					}
				} else {
					lx += vx;
					px = 0;
				}
			}
		}
		// 没碰壁时继续前进
		else {
			if (abs(vx) < 1) {
				px += vx;
				if (abs(px) >= 1) {
					lx += round(px);
					px = 0;
				}
			} else {
				lx += vx;
				px = 0;
			}
		}

		// 当在Y轴上碰到墙时,Y轴行进方向改变
		if (ly + vy + radius >= height) {
			if (vy > 0) {
				vy *= -1;
			} else {
				if (abs(vy) < 1) {
					py += vy;
					if (abs(py) >= 1) {
						ly += round(py);
						py = 0;
					}
				} else {
					ly += vy;
					py = 0;
				}
			}
		} else if (ly + vy - radius <= 0) {
			if (vy < 0) {
				vy *= -1;
			} else {
				if (abs(vy) < 1) {
					py += vy;
					if (abs(py) >= 1) {
						ly += round(py);
						py = 0;
					}
				} else {
					ly += vy;
					py = 0;
				}
			}
		}
		// 没碰壁时继续前进
		else {
			if (abs(vy) < 1) {
				py += vy;
				if (abs(py) >= 1) {
					ly += round(py);
					py = 0;
				}
			} else {
				ly += vy;
				py = 0;
			}
		}
	}

	/**
	 * 解决小球之间的斜碰（二维平面，球==圆）
	 * 
	 * 坐标系x轴向右为正，y轴向下为正
	 * 
	 * 需要打磨算法才能准确
	 * 
	 * @param circle
	 */
	public void collide(Circle b) {
		Circle a = this;

		int alx = a.lx, aly = a.ly, blx = b.lx, bly = b.ly;
		double avx = a.vx, avy = a.vy, bvx = b.vx, bvy = b.vy;
		int ar = a.radius, br = b.radius;
		int am = a.weight, bm = b.weight;

		// ----- 第零步：先判断两个圆的位置 -----

		double dn = sqrt(pow(alx - blx, 2) + pow(aly - bly, 2));
		double dc = ar + br;
		// 两圆重合
		if (dn == 0)
			return;
		// 两圆相离则返回
		if (dn > dc + Main.ERROR_APART)
			return;

		// ----- 第一步：计算合速度的大小和方向 -----

		// 计算a的合速度的值（不包含方向）
		double av = sqrt(pow(avx, 2) + pow(avy, 2));
		// 计算a的运行角度（运行方向，[-π ~ π)）
		double a0 = acos(avx / av);
		if (avy != 0)
			a0 *= avy / abs(avy);

		// 计算b的合速度的值
		double bv = sqrt(pow(bvx, 2) + pow(bvy, 2));
		// 计算b的运行角度
		double b0 = acos(bvx / bv);
		if (bvy != 0)
			b0 *= bvy / abs(bvy);

		// System.out.println(av + " " + (a0 * 180 / PI));
		// System.out.println(bv + " " + (b0 * 180 / PI));

		// ----- 第二步：计算共同（相对）坐标系s，以及ab在s中的速度的大小和方向 -----

		// ----- 第二步：第一部分：计算共同（相对）坐标系s的参数 -----

		// 计算圆b圆心相对于圆a圆心的坐标
		int sx = alx - blx;
		int sy = aly - bly;

		// 计算b相对于a的速度方向和大小
		double sz = sqrt(pow(sx, 2) + pow(sy, 2));
		double s0 = acos(sx / sz);
		if (sy != 0)
			s0 *= sy / abs(sy);

		// System.out.println(sz + " " + (s0 * 180 / PI));

		// ----- 第二步：第二部分：解决粘连问题，或者检测到碰撞时已经相交，让两个圆分离 -----

		// 在这里 dc-ERROR_CROSS <= dn <= dc+ERROR_APART 算相切

		// 两圆相交（或两圆粘连）
		if (dn < dc - Main.ERROR_CROSS) {
			double delta = (dc - dn) / 2;
			double dx = delta * cos(s0);
			double dy = delta * sin(s0);
			// 计算ab圆心直线的斜率
			alx += dx;
			aly -= dy;
			blx -= dx;
			bly += dy;
		}

		a.lx = alx;
		a.ly = aly;

		b.lx = blx;
		b.ly = bly;

		// ----- 第二步：第三部分：将ab的速度大小和方向由原坐标系转化为在共同坐标系s中的速度大小和方向 -----

		// 在s坐标系中a的新运动方向
		double sa0 = a0 - s0;
		// 在s坐标系中a的新速度大小
		double sav = av;
		// 在s坐标系中a的新速度在s的x轴上的投影
		double savx = sav * cos(sa0);
		// 在s坐标系中a的新速度在s的y轴上的投影
		double savy = sav * sin(sa0);

		// 在s坐标系中b的新运动方向
		double sb0 = b0 - s0;
		// 在s坐标系中b的新速度大小
		double sbv = bv;
		// 在s坐标系中b的新速度在s的x轴上的投影
		double sbvx = sbv * cos(sb0);
		// 在s坐标系中b的新速度在s的y轴上的投影
		double sbvy = sbv * sin(sb0);

		// ----- 第三步：发生完全弹性斜碰时，在s坐标系中，两球y轴速度不变，x轴速度满足完全弹性正碰（由动能定理和动量守恒推导） -----

		// 碰撞后a球s坐标系x轴的分速度
		double savxp = ((am - bm) * savx + 2 * bm * sbvx) / (am + bm);
		// 碰撞后a球s坐标系y轴的分速度
		double savyp = savy;
		// 碰撞后b球s坐标系x轴的分速度
		double sbvxp = ((bm - am) * sbvx + 2 * am * savx) / (am + bm);
		// 碰撞后b球s坐标系y轴的分速度
		double sbvyp = sbvy;

		// ----- 第四步：计算两球发生碰撞后在s中的各自合速度大小和运动方向-----

		// 碰撞后a球在s坐标系的合速度大小
		double savp = sqrt(pow(savxp, 2) + pow(savyp, 2));
		// 碰撞后a球在s坐标系的运动方向
		double sa0p = acos(savxp / savp);
		if (savyp != 0)
			sa0p *= savyp / abs(savyp);
		// 碰撞后b球在s坐标系的合速度大小
		double sbvp = sqrt(pow(sbvxp, 2) + pow(sbvyp, 2));
		// 碰撞后b球在s坐标系的运动方向
		double sb0p = acos(sbvxp / sbvp);
		if (sbvyp != 0)
			sb0p *= sbvyp / abs(sbvyp);

		// ----- 第五步：将两球速度转化为原坐标系中的速度 -----

		// 碰撞后a球在原坐标系的合速度大小
		double fva = savp;
		// 碰撞后a球在原坐标系的运动方向
		double fa0 = sa0p + s0;
		// 碰撞后a球在原坐标系的合速度大小在x轴上的分量
		double fvax = fva * cos(fa0);
		// 碰撞后a球在原坐标系的合速度大小在y轴上的分量（注意乘-1）
		double fvay = fva * sin(fa0) * -1;

		// 碰撞后b球在原坐标系的合速度大小
		double fvb = sbvp;
		// 碰撞后b球在原坐标系的运动方向
		double fb0 = sb0p + s0;
		// 碰撞后b球在原坐标系的合速度大小在x轴上的分量
		double fvbx = fvb * cos(fb0);
		// 碰撞后b球在原坐标系的合速度大小在y轴上的分量
		double fvby = fvb * sin(fb0) * -1;

		// ----- 第六步：更新 -----
		a.vx = fvax;
		a.vy = fvay;

		b.vx = fvbx;
		b.vy = fvby;
	}

}
