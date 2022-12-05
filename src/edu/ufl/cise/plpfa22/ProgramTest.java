package edu.ufl.cise.plpfa22;

public class ProgramTest implements Runnable{
    int a;
    String b;
    boolean c;
    public ProgramTest() {
        super();
    }
    @Override
    public void run() {
        new p().run();
    }
    public class p implements Runnable {
        @Override
        public void run() {
            new q().run();
        }
        public class q implements Runnable {
            String d;
            @Override
            public void run() {
                a = 42;
                b = "hello";
                c = true;
                d = "jd";
                System.out.println(a);
                System.out.println(b);
                System.out.println(c);
                System.out.println(d);
            }
        }
    }
    public void main(String args[]) {
        new ProgramTest().run();
    }
}
