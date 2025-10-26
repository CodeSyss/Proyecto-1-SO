/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package gui.classes;

/**
 *
 * @author Carlos Hernandez
 */

import java.awt.BorderLayout;
import java.awt.Color;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
// --- ¡CAMBIOS EN LOS IMPORTS! ---
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
// ---------------------------------

public class graphics extends JPanel {

    private final XYSeries throughputSeries;
    private final XYSeries cpuUsageSeries;
    private final XYSeries avgWaitTimeSeries;
    private final XYSeriesCollection dataset;
    
    private final JFreeChart chart;

    public graphics() {
        this.throughputSeries = new XYSeries("Throughput (proc/ciclo)");
        this.cpuUsageSeries = new XYSeries("Utilización CPU (%)");
        this.avgWaitTimeSeries = new XYSeries("Espera Promedio (ciclos)");
        this.dataset = new XYSeriesCollection();
        
        setPreferredSize(new java.awt.Dimension(300, 140));

        this.dataset.addSeries(throughputSeries);
        this.dataset.addSeries(cpuUsageSeries);
        this.dataset.addSeries(avgWaitTimeSeries);

        this.chart = ChartFactory.createXYLineChart(
                "Rendimiento del Sistema", "Ciclo Global", "Valor",
                dataset, PlotOrientation.VERTICAL, true, true, false
        );

        setBackground(Color.WHITE);
        XYPlot plot = chart.getXYPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setDomainGridlinePaint(Color.LIGHT_GRAY);
        plot.setRangeGridlinePaint(Color.LIGHT_GRAY);

        XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) plot.getRenderer();
        renderer.setSeriesPaint(0, Color.BLUE);    // Throughput
        renderer.setSeriesPaint(1, Color.RED);     // CPU Usage
        renderer.setSeriesPaint(2, Color.ORANGE);  // Wait Time
        renderer.setSeriesShapesVisible(0, false);
        renderer.setSeriesShapesVisible(1, false);
        renderer.setSeriesShapesVisible(2, false);

        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setMouseWheelEnabled(true);
        setLayout(new BorderLayout());
        add(chartPanel, BorderLayout.CENTER);
    }

    /**
     * Añade un nuevo punto de datos a cada serie del gráfico.
     * ¡Este método ahora funcionará!
     */
    public void updateData(int cycle, double throughput, double cpuUsage, double avgWaitTime) {
        SwingUtilities.invokeLater(() -> {
            try {
                this.throughputSeries.addOrUpdate(cycle, throughput);
                this.cpuUsageSeries.addOrUpdate(cycle, cpuUsage * 100); // Multiplicar para %
                this.avgWaitTimeSeries.addOrUpdate(cycle, avgWaitTime);
            } catch (Exception e) {
                System.err.println("Error actualizando el gráfico: " + e.getMessage());
            }
        });
    }

    /**
     * Limpia todos los datos del gráfico para una nueva simulación.
     */
    public void clearChart() {
        this.throughputSeries.clear();
        this.cpuUsageSeries.clear();
        this.avgWaitTimeSeries.clear();
    }
}