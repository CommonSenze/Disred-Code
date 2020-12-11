package me.commonsenze.core.Managers.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;

public class TimerManager {

	private HashMap<String, Long> timers;
	
	public TimerManager() {
		timers = new HashMap<>();
	}
	
	public void put(String name, int seconds) {
		timers.put(name, System.currentTimeMillis()+(seconds*1000));
	}
	
	public boolean inTimer(String name) {
		checkTime(name);
		return timers.containsKey(name);
	}
	
	public void remove(String name) {
		timers.remove(name);
	}
	
	public double getTime(String name) {
		checkTime(name);
		if (!timers.containsKey(name))return 0;
		return new BigDecimal((timers.get(name) - System.currentTimeMillis())/1000.0).setScale(1, RoundingMode.HALF_UP).doubleValue();
	}
	
	private void checkTime(String name) {
		if (!timers.containsKey(name))return;
		if (timers.get(name) < System.currentTimeMillis()) {
			timers.remove(name);
		}
	}
}
