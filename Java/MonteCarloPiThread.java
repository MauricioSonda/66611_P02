import java.awt.*;
import javax.swing.*;
import java.util.Random;

public class MonteCarloPiThread extends JPanel {
    private int n;
    private double[] aproximaciones;
    private double piFinal;
    private double[] xDentro, yDentro, xFuera, yFuera;
    private int dentroCirculo, fueraCirculo;

    public MonteCarloPiThread(int n) {
        this.n = n;
        xDentro = new double[n];
        yDentro = new double[n];
        xFuera = new double[n];
        yFuera = new double[n];
        aproximaciones = new double[n];
        dentroCirculo = 0;
        fueraCirculo = 0;
        simularEnHilo(); // primera simulación en un hilo
    }

    private void simular() {
        Random rand = new Random();
        int dentro = 0;
        int idxIn = 0, idxOut = 0;

        for (int i = 1; i <= n; i++) {
            double x = rand.nextDouble() * 2 - 1; // [-1, 1]
            double y = rand.nextDouble() * 2 - 1; // [-1, 1]

            if (x * x + y * y <= 1) {
                if (idxIn < n) {
                    xDentro[idxIn] = x;
                    yDentro[idxIn] = y;
                    idxIn++;
                }
                dentro++;
            } else {
                if (idxOut < n) {
                    xFuera[idxOut] = x;
                    yFuera[idxOut] = y;
                    idxOut++;
                }
            }

            double piAprox = 4.0 * dentro / i;
            aproximaciones[i - 1] = piAprox;
        }

        dentroCirculo = idxIn;
        fueraCirculo = idxOut;
        piFinal = aproximaciones[n - 1];
    }

    private void simularEnHilo() {
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                simular(); // se ejecuta en un hilo aparte
                return null;
            }

            @Override
            protected void done() {
                repaint(); // al terminar, repinta en el EDT
            }
        };
        worker.execute();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        int width = getWidth();
        int height = getHeight();
        int size = Math.min(width, height);

        // Círculo
        g.setColor(Color.LIGHT_GRAY);
        g.drawOval((width - size) / 2, (height - size) / 2, size, size);

        // Puntos dentro
        g.setColor(Color.BLUE);
        for (int i = 0; i < dentroCirculo; i++) {
            int px = (int) (width / 2 + xDentro[i] * (size / 2));
            int py = (int) (height / 2 - yDentro[i] * (size / 2));
            g.fillOval(px, py, 2, 2);
        }

        // Puntos fuera
        g.setColor(Color.RED);
        for (int i = 0; i < fueraCirculo; i++) {
            int px = (int) (width / 2 + xFuera[i] * (size / 2));
            int py = (int) (height / 2 - yFuera[i] * (size / 2));
            g.fillOval(px, py, 2, 2);
        }

        // Texto
        g.setColor(Color.BLACK);
        g.setFont(new Font("Comic Sans", Font.BOLD, 18));
        String texto = String.format("Pi: %.6f", piFinal);
        g.drawString(texto, 20, 30);
    }

    public double getPiFinal() {
        return piFinal;
    }

    public void repetirSimulacion() {
        simularEnHilo(); // ahora usa hilo
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Metodo de Monte Carlo para Pi con Hilos");
        MonteCarloPiThread panel = new MonteCarloPiThread(10000);

        JButton botonRepetir = new JButton("Volver a aproximar Pi");
        botonRepetir.addActionListener(e -> panel.repetirSimulacion());

        JPanel contenedor = new JPanel(new BorderLayout());
        contenedor.add(panel, BorderLayout.CENTER);
        contenedor.add(botonRepetir, BorderLayout.SOUTH);

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setContentPane(contenedor);
        frame.setSize(600, 650);
        frame.setVisible(true);

        System.out.println("Pi: " + panel.getPiFinal());
    }
}