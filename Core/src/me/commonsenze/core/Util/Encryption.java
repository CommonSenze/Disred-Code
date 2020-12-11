package me.commonsenze.core.Util;

public class Encryption {
	
	public static String encryptIP(String str) {
		String hash = "";
		for (int i = 0; i < str.length(); i++) {
			char ch = Character.toLowerCase(str.charAt(i));
			switch (ch) {
			case 'a':  
                hash += "{";  
                break;  
            case 'b':  
                hash += "}";  
                break;  
            case 'c':  
                hash += "#";  
                break;  
            case 'd':  
                hash += "~";  
                break;  
            case 'e':  
                hash += "+";  
                break;  
            case 'f':  
                hash += "-";  
                break;  
            case 'g':  
                hash += "*";  
                break;  
            case 'h':  
                hash += "@";  
                break;  
            case 'i':  
                hash += "/";  
                break;  
            case 'j':  
                hash += "\\";  
                break;  
            case 'k':  
                hash += "?";  
                break;  
            case 'l':  
                hash += "$";  
                break;  
            case 'm':  
                hash += "!";  
                break;  
            case 'n':  
                hash += "^";  
                break;  
            case 'o':  
                hash += "(";  
                break;  
            case 'p':  
                hash += ")";  
                break;  
            case 'q':  
                hash += "<";  
                break;  
            case 'r':  
                hash += ">";  
                break;  
            case 's' :  
                hash += "=";  
                break;  
            case 't':  
                hash += ";";  
                break;  
            case 'u':  
                hash += ",";  
                break;  
            case 'v' :  
                hash += "_";  
                break;  
            case 'w':  
                hash += "[";  
                break;  
            case 'x' :  
                hash += "]";  
                break;  
            case 'y':  
                hash += ":";  
                break;  
            case 'z' :  
                hash += "\"";
                break;
			case '1':
				hash += "r";
				break;
			case '2':
				hash += "k";
				break;
			case '3':
				hash += "b";
				break;
			case '4':
				hash += "e";
				break;
			case '5':
				hash += "q";
				break;
			case '6':
				hash += "h";
				break;
			case '7':
				hash += "u";
				break;
			case '8':
				hash += "y";
				break;
			case '9':
				hash += "w";
				break;
			case '0':
				hash += "z";
				break;
			case '.':
				hash += "c";
				break;
			case '_':
				hash += "x";
				break;
			default:
				hash += "L";
				break;
			}
		}
		return hash;
	}
	
	public static String decryptIP(String hash) {
		String str = "";
		for (int i = 0; i < hash.length(); i++) {
			char ch = Character.toLowerCase(hash.charAt(i));
			switch (ch) {
			case '{':  
                str += "a";  
                break;  
            case '}':  
                str += "b";  
                break;  
            case '#':  
                str += "c";  
                break;  
            case '~':  
                str += "d";  
                break;  
            case '+':  
                str += "e";  
                break;  
            case '-':  
                str += "f";  
                break;  
            case '*':  
                str += "g";  
                break;  
            case '@':  
                str += "h";  
                break;  
            case '/':  
                str += "i";  
                break;  
            case '\\':  
                str += "j";  
                break;  
            case '?':  
                str += "k";  
                break;  
            case '$':  
                str += "l";  
                break;  
            case '!':  
                str += "m";  
                break;  
            case '^':  
                str += "n";  
                break;  
            case '(':  
                str += "o";  
                break;  
            case ')':  
                str += "p";  
                break;  
            case '<':  
                str += "q";  
                break;  
            case '>':  
                str += "r";  
                break;  
            case '=' :  
                str += "s";  
                break;  
            case ';':  
                str += "t";  
                break;  
            case ',':  
                str += "u";  
                break;  
            case '_' :  
                str += "v";  
                break;  
            case '[':  
                str += "w";  
                break;  
            case ']' :  
                str += "x";  
                break;  
            case ':':  
                str += "y";  
                break;  
            case '\"' :  
                str += "z";
                break;
			case 'r':
				str += "1";
				break;
			case 'k':
				str += "2";
				break;
			case 'b':
				str += "3";
				break;
			case 'e':
				str += "4";
				break;
			case 'q':
				str += "5";
				break;
			case 'h':
				str += "6";
				break;
			case 'u':
				str += "7";
				break;
			case 'y':
				str += "8";
				break;
			case 'w':
				str += "9";
				break;
			case 'z':
				str += "0";
				break;
			case 'c':
				str += ".";
				break;
			case 'x':
				str += "_";
				break;
			}
		}
		return str;
	}
	
	public static String encryptPlayer(String str) {
		String hash = "";
		for (int i = 0; i < str.length(); i++) {
			char ch = Character.toLowerCase(str.charAt(i));
			switch (ch) {
			case 'a':  
                hash += "%";  
                break;  
            case 'b':  
                hash += "}";  
                break;  
            case 'c':  
                hash += "#";  
                break;  
            case 'd':  
                hash += "~";  
                break;  
            case 'e':  
                hash += "0";  
                break;  
            case 'f':  
                hash += "-";  
                break;  
            case 'g':  
                hash += "*";  
                break;  
            case 'h':  
                hash += "@";  
                break;  
            case 'i':  
                hash += "3";  
                break;  
            case 'j':  
                hash += "\\";  
                break;  
            case 'k':  
                hash += "?";  
                break;  
            case 'l':  
                hash += "$";  
                break;  
            case 'm':  
                hash += "!";  
                break;  
            case 'n':  
                hash += "^";  
                break;  
            case 'o':  
                hash += ":";  
                break;  
            case 'p':  
                hash += ")";  
                break;  
            case 'q':  
                hash += "<";  
                break;  
            case 'r':  
                hash += ">";  
                break;  
            case 's' :  
                hash += "=";  
                break;  
            case 't':  
                hash += ";";  
                break;  
            case 'u':  
                hash += "|";  
                break;  
            case 'v' :  
                hash += "_";  
                break;  
            case 'w':  
                hash += "[";  
                break;  
            case 'x' :  
                hash += "]";  
                break;  
            case 'y':  
                hash += "4";  
                break;
            case 'z' :  
                hash += "1";
                break;
			case '1':
				hash += "`";
				break;
			case '2':
				hash += "k";
				break;
			case '3':
				hash += "b";
				break;
			case '4':
				hash += "e";
				break;
			case '5':
				hash += "q";
				break;
			case '6':
				hash += "5";
				break;
			case '7':
				hash += "u";
				break;
			case '8':
				hash += "y";
				break;
			case '9':
				hash += "w";
				break;
			case '0':
				hash += "2";
				break;
			case '.':
				hash += "c";
				break;
			case '_':
				hash += "x";
				break;
			default:
				hash += "L";
				break;
			}
		}
		return hash;
	}

	public static String decryptPlayer(String hash) {
		String str = "";
		for (int i = 0; i < hash.length(); i++) {
			char ch = Character.toLowerCase(hash.charAt(i));
			switch (ch) {
			case '%':  
                str += "a";  
                break;  
            case '}':  
                str += "b";  
                break;  
            case '#':  
                str += "c";  
                break;  
            case '~':  
                str += "d";  
                break;  
            case '0':  
                str += "e";  
                break;  
            case '-':  
                str += "f";  
                break;  
            case '*':  
                str += "g";  
                break;  
            case '@':  
                str += "h";  
                break;  
            case '3':  
                str += "i";  
                break;  
            case '\\':  
                str += "j";  
                break;  
            case '?':  
                str += "k";  
                break;  
            case '$':  
                str += "l";  
                break;  
            case '!':  
                str += "m";  
                break;  
            case '^':  
                str += "n";  
                break;  
            case ':':  
                str += "o";  
                break;  
            case ')':  
                str += "p";  
                break;  
            case '<':  
                str += "q";  
                break;  
            case '>':  
                str += "r";  
                break;  
            case '=' :  
                str += "s";  
                break;  
            case ';':  
                str += "t";  
                break;  
            case '|':  
                str += "u";  
                break;  
            case '_' :  
                str += "v";  
                break;  
            case '[':  
                str += "w";  
                break;  
            case ']' :  
                str += "x";  
                break;  
            case '4':  
                str += "y";  
                break;
            case '1' :  
                str += "z";
                break;
			case '`':
				str += "1";
				break;
			case 'k':
				str += "2";
				break;
			case 'b':
				str += "3";
				break;
			case 'e':
				str += "4";
				break;
			case 'q':
				str += "5";
				break;
			case '5':
				str += "6";
				break;
			case 'u':
				str += "7";
				break;
			case 'y':
				str += "8";
				break;
			case 'w':
				str += "9";
				break;
			case '2':
				str += "0";
				break;
			case 'c':
				str += ".";
				break;
			case 'x':
				str += "_";
				break;
			}
		}
		return str;
	}
}
