package com.jay.ai;

import java.util.Locale;

/** Safe local calculator for common arithmetic expressions. */
public final class JayMathEngine {
    private JayMathEngine() {}

    public static String trySolve(String input) {
        if (input == null) return null;
        String s = input.trim();
        String lower = s.toLowerCase(Locale.ROOT);
        if (!(lower.startsWith("math") || lower.startsWith("calculate") || lower.startsWith("solve") || lower.matches(".*\\d\\s*[+\\-*/%^]\\s*\\d.*"))) return null;
        s = s.replaceFirst("(?i)^\\s*(math|calculate|solve)\\s*[:=]?\\s*", "").replace('×','*').replace('÷','/');
        if (s.isEmpty()) return "What calculation should I solve, Sir?";
        try {
            double value = new Parser(s).parse();
            if (!Double.isFinite(value)) return "That calculation does not have a finite result, Sir.";
            if (Math.abs(value - Math.rint(value)) < 1e-10) return "The answer is " + String.format(Locale.US, "%.0f", value) + ", Sir.";
            return "The answer is " + String.format(Locale.US, "%.10f", value).replaceAll("0+$", "").replaceAll("\\.$", "") + ", Sir.";
        } catch (Exception e) {
            return "I couldn't solve that calculation, Sir. Please check the expression.";
        }
    }

    private static final class Parser {
        private final String s; private int p;
        Parser(String s) { this.s = s; }
        double parse() { double v = expression(); skip(); if (p != s.length()) throw new IllegalArgumentException(); return v; }
        double expression() { double v = term(); while (true) { skip(); if (take('+')) v += term(); else if (take('-')) v -= term(); else return v; } }
        double term() { double v = power(); while (true) { skip(); if (take('*')) v *= power(); else if (take('/')) { double d=power(); if (d==0) throw new ArithmeticException(); v /= d; } else if (take('%')) { double d=power(); if (d==0) throw new ArithmeticException(); v %= d; } else return v; } }
        double power() { double v = unary(); skip(); if (take('^')) v = Math.pow(v, power()); return v; }
        double unary() { skip(); if (take('+')) return unary(); if (take('-')) return -unary(); return primary(); }
        double primary() { skip(); if (take('(')) { double v=expression(); if(!take(')')) throw new IllegalArgumentException(); return v; } int start=p; while(p<s.length() && (Character.isDigit(s.charAt(p))||s.charAt(p)=='.')) p++; if(start==p) throw new IllegalArgumentException(); return Double.parseDouble(s.substring(start,p)); }
        boolean take(char c) { skip(); if(p<s.length()&&s.charAt(p)==c){p++;return true;} return false; }
        void skip(){while(p<s.length()&&Character.isWhitespace(s.charAt(p)))p++;}
    }
}
