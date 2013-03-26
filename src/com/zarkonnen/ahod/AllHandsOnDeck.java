package com.zarkonnen.ahod;

import com.zarkonnen.catengine.Condition;
import com.zarkonnen.catengine.Draw;
import com.zarkonnen.catengine.Fount;
import com.zarkonnen.catengine.Frame;
import com.zarkonnen.catengine.Game;
import com.zarkonnen.catengine.Img;
import com.zarkonnen.catengine.Input;
import com.zarkonnen.catengine.MusicCallback;
import com.zarkonnen.catengine.SlickEngine;
import com.zarkonnen.catengine.util.Clr;
import com.zarkonnen.catengine.util.Pt;
import com.zarkonnen.catengine.util.ScreenMode;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Random;

public class AllHandsOnDeck implements Game, MusicCallback {
	public static void main(String[] args) {
		SlickEngine se = new SlickEngine("All Hands on Deck", "/com/zarkonnen/ahod/images/", "/com/zarkonnen/ahod/sounds/", 60);
		se.setup(new AllHandsOnDeck());
		se.runUntil(Condition.ALWAYS);
	}

	public static final String ALPHABET = " qwertyuiopasdfghjklzxcvbnmQWERTYUIOPASDFGHJKLZXCVBNM1234567890-=+_!?<>,.;:\"'@£$%^&*()[]{}|\\~/±";
	public static final Fount GOUNT = new Fount("LiberationMono64", 50, 84, 50, 84, ALPHABET);

	public static final Fount FOUNT = new Fount("LiberationMono18", 14, 24, 12, 24, ALPHABET);
	int tick = 0;
	
	String[] BOARDERS = { "1", "2", "3", "4", "5", "6", "7", "8", "9", "0" };
	String[] CANNON = { "Q", "W", "E", "R", "T", "Y", "U", "I", "O", "P" };
	String[] FIRES = { "A", "S", "D", "F", "G", "H", "J", "K", "L" };
	String[] HOLES = { "Z", "X", "C", "V", "B", "N", "M" };
	
	boolean boarderMvRight = false;
	boolean hasBoarder = false;
	double boarderX;
	int[] cannonReload = new int[CANNON.length];
	int[] cannonAim = new int[CANNON.length];
	int[] fireLeft = new int[FIRES.length];
	double[] fireDamage = new double[FIRES.length];
	int[] ticksSinceFirePlug = new int[FIRES.length];
	boolean[] holes = new boolean[HOLES.length];
	int[] ticksSinceHolePlug = new int[HOLES.length];
	int rifleReload = 0;
	int[] crew = new int[BOARDERS.length];
	int boarderIncoming = 0;
	
	double water = 0;
	
	int enemyHp = 100;
	int enemyReload = 30;
	
	boolean victory;
	boolean defeat;
	int endTimer = 0;
	boolean music = false;
	
	int crew() {
		int amt = 0;
		for (int i = 0; i < crew.length; i++) {
			amt += (100 - crew[i]);
		}
		return amt / crew.length;
	}
	
	double hp() {
		int total = 0;
		for (int i = 0; i < FIRES.length; i++) {
			if (fireDamage[i] < 100) {
				total++;
			}
		}
		return total * 100 / FIRES.length;
	}
	
	Random r = new Random();

	@Override
	public void run(String m, double volume) {
		music = false;
	}
	
	static class Flash {
		int life = 9;
		double x, y;
		public boolean tick() {
			return life-- <= 0;
		}

		public Flash(double x, double y) {
			this.x = x;
			this.y = y;
		}
	}
	
	LinkedList<Flash> flashes = new LinkedList<Flash>();
	LinkedList<Smoke> smokes = new LinkedList<Smoke>();
	LinkedList<Blood> bloods = new LinkedList<Blood>();
	
	class Smoke {
		double x, y, dy = -1.5;
		public boolean tick() {
			dy -= 0.03;
			y += dy;
			return y < -300;
		}

		public Smoke(double x, double y) {
			this.x = x;
			this.y = y;
		}
	}
	
	class Blood {
		double x, y, dx, dy;
		int life = 60;

		public Blood(double x, double y) {
			this.x = x;
			this.y = y;
			double angle = r.nextDouble() * Math.PI * 2;
			double spd = r.nextDouble();
			dx = Math.cos(angle) * 4 * spd;
			dy = Math.sin(angle) * 4 * spd;
		}
		
		public boolean tick() {
			dy += 0.07;
			x += dx;
			y += dy;
			return life-- <= 0;
		}
	}
	
	int lvl = 1;
	int newLvlTick = 0;
	
	@Override
	public void input(Input in) {
		if (tick == 0) {
			for (int i = 0; i < CANNON.length; i++) {
				cannonReload[i] = r.nextInt(400);
			}
			in.setMode(new ScreenMode(800, 600, true));
			in.setCursorVisible(false);
		}
		if (!music) {
			in.playMusic("music", 1.0, null, this);
			music = true;
		}
		tick++;
		newLvlTick++;
		if (newLvlTick < 100) { return; }
		if (!defeat && !victory) {
			if (hasBoarder && boarderIncoming == 0) {
				if (boarderMvRight) {
					boarderX += 0.8 * lvl;
					if (boarderX > 550) {
						boarderMvRight = false;
					}
				} else {
					boarderX -= 0.8 * lvl;
					if (boarderX < 0) {
						boarderMvRight = true;
					}
				}
			}

			if (enemyReload-- == 0) {
				if (!hasBoarder && r.nextInt(4) == 0) {
					hasBoarder = true;
					boarderX = r.nextInt(550);
					boarderMvRight = r.nextBoolean();
					boarderIncoming = 50;
				} else {
					if (r.nextBoolean()) {
						// Fire level
						int target = r.nextInt(FIRES.length);
						flashes.add(new Flash(45 + target * 55 - 16, 345 - 16));
						fireLeft[target] = 3;
						in.play("cannon", 1.0, 1.0, 0, 0);
					} else {
						// Water level
						int target = r.nextInt(HOLES.length);
						flashes.add(new Flash(63 + target * 65 - 16, 393 - 16));
						holes[target] = true;
						in.play("cannon", 1.0, 1.0, 0, 0);
					}
				}
				enemyReload = 100 - lvl * 10;
			}

			for (Iterator<Flash> it = flashes.iterator(); it.hasNext();) {
				if (it.next().tick()) {
					it.remove();
				}
			}
			
			for (Iterator<Smoke> it = smokes.iterator(); it.hasNext();) {
				if (it.next().tick()) {
					it.remove();
				}
			}
			
			for (Iterator<Blood> it = bloods.iterator(); it.hasNext();) {
				if (it.next().tick()) {
					it.remove();
				}
			}
			
			if (rifleReload > 0) {
				rifleReload--;
			} else {
				for (int i = 0; i < BOARDERS.length; i++) {
					if (in.keyPressed(BOARDERS[i])) {
						rifleReload = 120;
						if (hasBoarder && boarderIncoming == 0) {
							int boarderTile = (int) ((boarderX + 15) / 60);
							if (boarderTile == i) {
								in.play("sword", 1.0, 1.0, 0, 0);
								hasBoarder = false;
								for (int j = 0; j < 60; j++) {
									bloods.add(new Blood(boarderX + 10, 260));
								}
							}
						}
					}
				}
			}

			for (int i = 0; i < CANNON.length; i++) {
				if (cannonReload[i] > 0) {
					cannonReload[i]--;
				} else {
					if (in.keyPressed(CANNON[i])) {
						cannonReload[i] = 200 + r.nextInt(400);
						enemyHp -= 4;
						flashes.add(new Flash(28 + i * 55 - 16, 302 - 16));
						smokes.add(new Smoke(28 + i * 55 - 6, 302 - 16));
						in.play("cannon", 1.0, 1.0, 0, 0);
					}
				}
			}

			for (int i = 0; i < FIRES.length; i++) {
				if (fireDamage[i] >= 100) {
					fireLeft[i] = 0;
				}
				if (fireLeft[i] > 1) {
					if (r.nextInt(150) == 0) {
						smokes.add(new Smoke(45 + i * 55, 345));
					}
					if (in.keyPressed(FIRES[i])) {
						ticksSinceFirePlug[i] = 0;
					}
					fireDamage[i] += 0.45 * lvl;
					if (ticksSinceFirePlug[i]++ == 0) {
						fireLeft[i]--;
					}
				}
			}
			for (int i = 0; i < HOLES.length; i++) {
				if (holes[i]) {
					if (in.keyPressed(HOLES[i])) {
						ticksSinceHolePlug[i] = 0;
					}
					if (ticksSinceHolePlug[i]++ > 10) {
						water += 0.02 * lvl;
					}
				}
			}
			if (hasBoarder && boarderIncoming == 0) {
				if ((tick + 15) % 30 == 0) {
					int boarderTile = (int) ((boarderX + 15) / 60);
					if (crew[boarderTile] < 100) {
						in.play("sword", 1.0, 0.6, 0, 0);
						crew[boarderTile] += 25 * lvl;
						if (crew[boarderTile] > 100) {
							crew[boarderTile] = 100;
						}
						for (int j = 0; j < 25; j++) {
							bloods.add(new Blood(boarderX + 10, 260));
						}
					}
				}
			}
			if (boarderIncoming > 0) {
				boarderIncoming--;
			}

			if (hp() <= 0 || water >= 100 || crew() <= 0) {
				defeat = true;
			} else if (enemyHp <= 0) {
				victory = true;
			}
		} else {
			if (defeat && endTimer == 0) {
				in.play("sinking", 1.0, 1.0, 0, 0);
			}
			if (endTimer++ == 300) {
				for (int i = 0; i < BOARDERS.length; i++) {
					crew[i] = 0;
				}
				water = 0;
				endTimer = 0;
				enemyHp = 100;
				hasBoarder = false;
				for (int i = 0; i < CANNON.length; i++) {
					cannonReload[i] = r.nextInt(200);
				}
				for (int i = 0; i < FIRES.length; i++) {
					fireLeft[i] = 0;
					fireDamage[i] = 0;
				}
				for (int i = 0; i < HOLES.length; i++) {
					holes[i] = false;
				}
				victory = false;
				defeat = false;
				if (victory) {
					lvl *= 2;
				} else {
					lvl = 1;
				}
				flashes.clear();
				bloods.clear();
				smokes.clear();
				newLvlTick = 0;
			}
		}
	}
	
	Clr sky = new Clr(120, 120, 255);
	Clr sea = new Clr(30, 50, 200);
	Clr seaTop = new Clr(30, 50, 200, 150);
	Img empty_cannon = new Img("empty_cannon");
	Img flash = new Img("flash");
	Img hole_fire_0 = new Img("hole_fire_0");
	Img hole_fire_1 = new Img("hole_fire_1");
	Img hole = new Img("hole");
	Img loaded_cannon = new Img("loaded_cannon");
	Img pirate_a = new Img("pirate_a");
	Img pirate_b= new Img("pirate_b");
	Img pirate_a_r = new Img("pirate_a").flip();
	Img pirate_b_r = new Img("pirate_b").flip();
	Img ship = new Img("ship");
	Img smoke = new Img("smoke");
	Img burnt = new Img("burnt");
	Img crewman = new Img("crew");
	Img sail = new Img("sail");

	@Override
	public void render(Frame f) {
		Draw d = new Draw(f);
		d.rect(sky, 0, 0, 800, 600);
		d.rect(sea, 0, 400, 800, 200);
		d.shift(120 + 300, 0 + 300);
		d.rotate(Math.sin(tick * 0.03));
		d.shift(-300, -300 + water * 1.15 + (defeat ? endTimer : 0));
		d.blit(ship, 0, 0);
		d.blit(sail, 220, 5, 60 + Math.sin(tick * 0.001) * 15, 240);
		d.blit(sail, 340, 45, 40 + Math.sin(tick * 0.001) * 8, 200);
		for (int i = 0; i < BOARDERS.length; i++) {
			if (crew[i] < 100) {
				d.blit(crewman, 10 + i * 60, 243);
			}
		}
		if (hasBoarder) {
			if (boarderMvRight) {
				if ((tick / 15) % 2 == 0) {
					d.blit(pirate_a_r, boarderX, 243 - boarderIncoming * 10);
				} else {
					d.blit(pirate_b_r, boarderX, 245 - boarderIncoming * 10);
				}
			} else {
				if ((tick / 15) % 2 == 0) {
					d.blit(pirate_a, boarderX + 8, 243 - boarderIncoming * 10);
				} else {
					d.blit(pirate_b, boarderX, 245 - boarderIncoming * 10);
				}
			}
		}
		for (int i = 0; i < CANNON.length; i++) {
			d.blit(cannonReload[i] == 0 ? loaded_cannon : empty_cannon, 28 + i * 55, 302);
		}
		for (int i = 0; i < FIRES.length; i++) {
			if (fireDamage[i] >= 100) {
				d.blit(burnt, 45 + i * 55, 345);
			} else if (fireLeft[i] == 1) {
				d.blit(hole, 45 + i * 55, 345);
			} else if (fireLeft[i] > 1) {
				d.blit((tick / 12 + i * 17) % 2 == 0 ? hole_fire_0 : hole_fire_1, 45 + i * 55, 345);
			}
		}
		for (int i = 0; i < HOLES.length; i++) {
			if (holes[i]) {
				d.blit(hole, 63 + i * 65, 393);
			}
		}
		int boarderTile = (int) ((boarderX + 15) / 60);
		if (hasBoarder && rifleReload == 0) {
			for (int i = 0; i < BOARDERS.length; i++) {
				if (i == boarderTile && crew[i] < 100) {
					d.text("[bg=00000055]" + BOARDERS[i], FOUNT, 10 + i * 60, 260);
				}
			}
		}
		for (int i = 0; i < CANNON.length; i++) {
			if (cannonReload[i] == 0) {
				d.text("[bg=00000055]" + CANNON[i], FOUNT, 41 + i * 55, 310);
			}
		}
		for (int i = 0; i < FIRES.length; i++) {
			if (fireDamage[i] < 100 && fireLeft[i] > 1) {
				d.text((ticksSinceFirePlug[i] <= 10 ? "[GREEN]" : "") + "[bg=00000055]" + FIRES[i], FOUNT, 62 + i * 55, 360);
			}
		}
		for (int i = 0; i < HOLES.length; i++) {
			if (holes[i]) {
				d.text((ticksSinceHolePlug[i] <= 10 ? "[GREEN]" : "") + HOLES[i], FOUNT, 80 + i * 65, 410);
			}
		}
		for (Smoke sm : smokes) {
			d.blit(smoke, null, 0.5, sm.x, sm.y, 0, 0, 0);
		}
		for (Blood bl : bloods) {
			d.rect(Clr.RED, bl.x - 2, bl.y - 2, 4, 4);
		}
		for (Flash fl : flashes) {
			d.blit(flash, fl.x, fl.y);
		}
		d.resetTransforms();
		d.rect(seaTop, 0, 400, 800, 200);
		d.text("Crew: " + (int) crew() + "%\nWater level: " + (int) water + "%\nHull integrity: " + (int) hp() + "%\n\nEnemy: " + (int) enemyHp + "%", FOUNT, 10, 10);
		
		if (victory) {
			Pt sz = d.textSize("VICTORY!", GOUNT);
			d.text("VICTORY!", GOUNT, 400 - sz.x / 2, 200);
		}
		if (defeat) {
			Pt sz = d.textSize("DEFEAT!", GOUNT);
			d.text("DEFEAT!", GOUNT, 400 - sz.x / 2, 200);
		}
		if (newLvlTick < 100) {
			Pt sz = d.textSize(" ALL\nHANDS\n ON\nDECK!", GOUNT);
			d.text(" ALL\nHANDS\n ON\nDECK!", GOUNT, 400 - sz.x / 2, 30);
		}
	}
}
