package me.commonsenze.core.Util;

import java.lang.reflect.Type;
import java.util.List;

import com.google.gson.reflect.TypeToken;

public class TypeUtil {

	public static final Type GENERIC = new TypeToken<Object>() {}.getType();
	public static final Type STRING = new TypeToken<String>() {}.getType();
	public static final Type INT = new TypeToken<Integer>() {}.getType();
	public static final Type LONG = new TypeToken<Long>() {}.getType();
	public static final Type DOUBLE = new TypeToken<Double>() {}.getType();
	public static final Type BOOLEAN = new TypeToken<Boolean>() {}.getType();
	public static final Type CHAR = new TypeToken<Character>() {}.getType();
	public static final Type LIST_GENERIC = new TypeToken<List<?>>() {}.getType();
	public static final Type LIST_STRING = new TypeToken<List<String>>() {}.getType();
	public static final Type LIST_INT = new TypeToken<List<Integer>>() {}.getType();
	public static final Type LIST_LONG = new TypeToken<List<Long>>() {}.getType();
	public static final Type LIST_DOUBLE = new TypeToken<List<Double>>() {}.getType();
	public static final Type LIST_BOOLEAN = new TypeToken<List<Boolean>>() {}.getType();
	public static final Type LIST_CHAR = new TypeToken<List<Character>>() {}.getType();
}
