package thread;

public class TaskABC {
    private static Object mon = new Object();
    private static volatile String currentFigure = "A";
    private static final int value = 5;

    public static void main(String[] args) {

        new Thread(new Runnable() {
            public void run() {
                try {
                    for (int i = 0; i < value; i++) {
                        synchronized (mon) {
                            while (!currentFigure.equals("A")) {
                                mon.wait();
                            }
                            System.out.print("A");
                            currentFigure = "B";
                            mon.notifyAll();
                        }
                    }

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        new Thread(new Runnable() {
            public void run() {
                try {
                    for (int i = 0; i < value; i++) {
                        synchronized (mon) {
                            while (!currentFigure.equals("B")) {
                                mon.wait();
                            }
                            System.out.print("B");
                            currentFigure = "C";
                            mon.notifyAll();
                        }
                    }

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        new Thread(new Runnable() {
            public void run() {
                try {
                    for (int i = 0; i < value; i++) {
                        synchronized (mon) {
                            while (!currentFigure.equals("C")) {
                                mon.wait();
                            }
                            System.out.print("C");
                            currentFigure = "A";
                            mon.notifyAll();
                        }
                    }

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
