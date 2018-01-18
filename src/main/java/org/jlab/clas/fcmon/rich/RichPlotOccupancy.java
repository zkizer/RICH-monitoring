/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.clas.fcmon.rich;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.swing.JCheckBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.Timer;
import org.jlab.clas.fcmon.RichView;
import org.jlab.clas.fcmon.RichView.RICHtile;
import static org.jlab.clas.fcmon.rich.RichHit.Edge.LEADING;
import org.jlab.clas.fcmon.rich.RichHit.RichTDC;
import org.jlab.detector.view.DetectorShape2D;
import org.jlab.groot.base.TColorPalette;

/**
 *
 * @author kenjo
 */
public final class RichPlotOccupancy extends RichPlot {

    private final JPanel mainPanel = new JPanel(new BorderLayout());
    private final RichPanel evdisPanel = new RichPanel();
    private final JCheckBox evdisBox;
    private Boolean evdisMode = false;
    Map<Integer, RichPixWeight> hits = new ConcurrentHashMap<>();
    TColorPalette colPalette = new TColorPalette();
    private double scaleWeight = 1;

    public RichPlotOccupancy() {
        evdisBox = new JCheckBox("Event Display Mode");
        evdisBox.addActionListener(ev -> evdisMode = evdisBox.isSelected());

        JLabel lbl0 = new JLabel("Set maximum");
        JLabel lbl1 = new JLabel("%");
        JFormattedTextField scaleField = new JFormattedTextField(NumberFormat.getNumberInstance());
        scaleField.setValue(new Double(100));
        scaleField.setColumns(3);
        scaleField.addActionListener(ev -> scaleWeight = Double.parseDouble(scaleField.getText()) / 100.0);

        JToolBar toolBar = new JToolBar();
        toolBar.setLayout(new FlowLayout());
        toolBar.add(evdisBox);
        toolBar.addSeparator();
        toolBar.add(lbl0);
        toolBar.add(scaleField);
        toolBar.add(lbl1);

        mainPanel.add(toolBar, BorderLayout.PAGE_START);
        mainPanel.add(evdisPanel, BorderLayout.CENTER);
        mainPanel.setName("RICH Occupancy");
        reset();
    }

    @Override
    public void reset() {
        hits.clear();
    }

    @Override
    public void fill(Map<Integer, RichHit> rhits) {
        if (evdisMode) {
            hits.clear();
        }

        for (Map.Entry<Integer, RichHit> entry : rhits.entrySet()) {
            if (!hits.containsKey(entry.getKey())) {
                hits.put(entry.getKey(), new RichPixWeight(entry.getValue()));
            }
            hits.get(entry.getKey()).fill(entry.getValue());
        }
        if (evdisMode) {
            evdisPanel.repaint();
        }
    }

    @Override
    public JPanel getPanel() {
        return mainPanel;
    }

    @Override
    public void setCanvasUpdate(int period) {
        Timer updateTimer = new Timer(period, ev -> evdisPanel.repaint());
        updateTimer.start();

    }

    private class RichPixWeight {

        int itile = 0;
        int imaroc = 0;
        int ipix = 0;
        int nleadings = 0;
        int ntrailings = 0;

        RichPixWeight(RichHit rhit) {
            itile = rhit.itile;
            imaroc = rhit.imaroc;
            ipix = rhit.ipix;
        }

        void fill(RichHit rhit) {
            for (RichTDC rtdc : rhit.tdcList) {
                if (rtdc.edge == LEADING) {
                    nleadings++;
                } else {
                    ntrailings++;
                }
            }
        }
    }

    private class RichPanel extends JPanel {

        RichView rview = new RichView();
        List<DetectorShape2D> pmtShapes = new ArrayList<>();
        private double xmin = 1E6, xmax = -1E6, ymin = 1E6, ymax = -1E6;

        public RichPanel() {
            setLayout(null);
            setBackground(Color.black);

            for (RICHtile rtile : rview.getTiles()) {
                for (DetectorShape2D rpmt : rtile.getPMTs()) {
                    for (int ipoint = 0; ipoint < rpmt.getShapePath().size(); ipoint++) {
                        double xx = rpmt.getShapePath().point(ipoint).x();
                        double yy = rpmt.getShapePath().point(ipoint).y();
                        xmin = Math.min(xmin, xx);
                        xmax = Math.max(xmax, xx);
                        ymin = Math.min(ymin, yy);
                        ymax = Math.max(ymax, yy);
                    }
                }
            }

        }

        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setPaint(Color.darkGray);

            double scale = 0.9 * Math.min(getWidth() / (xmax - xmin), getHeight() / (ymax - ymin));
            double offx = (getWidth() - scale * (xmax - xmin)) / 2;
            double offy = (getHeight() - scale * (ymax - ymin)) / 2;
            double ww = 0;

            for (RICHtile rtile : rview.getTiles()) {
                for (DetectorShape2D rpmt : rtile.getPMTs()) {
                    double xx = rpmt.getShapePath().point(0).x();
                    double yy = rpmt.getShapePath().point(0).y();
                    ww = Math.abs(rpmt.getShapePath().point(0).y() - rpmt.getShapePath().point(1).y()) * scale;

                    xx = (xx - xmin) * scale + offx;
                    yy = (yy - ymin) * scale + offy;
                    Rectangle2D.Double pmt = new Rectangle2D.Double(xx, yy, ww, ww);

                    g2.draw(pmt);
                }
            }

            if (!hits.isEmpty()) {
                double maxWeight = 0;
                if (evdisMode) {
                    g2.setPaint(Color.MAGENTA);
                } else {
                    maxWeight = hits.values().stream().max(Comparator.comparing(rw -> rw.nleadings)).get().nleadings * scaleWeight;
                    g2.setPaint(Color.white);
                    g2.fillRect(getWidth() - 60, getHeight() / 4, 60, getHeight() * 3 / 4);
                    colPalette.draw(g2, getWidth() - 55, getHeight() * 9 / 32, 20, getHeight() * 11 / 16, 0, maxWeight, false);
                }

                for (RichPixWeight rpix : hits.values()) {
                    double xx = rview.getTile(rpix.itile).getPixel(rpix.imaroc, rpix.ipix).x;
                    double yy = rview.getTile(rpix.itile).getPixel(rpix.imaroc, rpix.ipix).y;
                    xx = (xx - xmin) * scale + offx;
                    yy = (yy - ymin) * scale + offy;

                    if (!evdisMode) {
                        g2.setPaint(colPalette.getColor3D(rpix.nleadings, maxWeight, false));
                    }
                    g2.fill(new Rectangle2D.Double(xx, yy, ww / 8, ww / 8));
                }
            }
        }
    }
}
