/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clas.detectors;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.clas.viewer.DetectorMonitor;
import org.jlab.detector.base.DetectorType;
import org.jlab.detector.view.DetectorShape2D;
import org.jlab.groot.data.H1F;
import org.jlab.groot.data.H2F;
import org.jlab.groot.group.DataGroup;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;

/**
 *
 * @author justin
 */
public class RICHmonitor extends DetectorMonitor {

    private final Integer twotilers[] = {3, 5, 7, 12, 15, 19, 24, 28, 33, 39, 44, 50, 57, 63, 70, 78, 85, 93, 102, 110, 119, 129, 138};
    private final int nleftTile[] = {2, 5, 8, 11, 15, 19, 23, 28, 33, 38, 44, 50, 56, 63, 70, 77, 85, 93, 101, 110, 119, 128, 138};
    private final int chan2pix[] = {60, 58, 59, 57, 52, 50, 51, 49, 44, 42, 43, 41, 36, 34, 35, 33, 28, 26, 27, 25, 20, 18, 19, 17, 12, 10, 11, 9, 4, 2, 3, 1, 5, 7, 6, 8, 13, 15, 14, 16, 21, 23, 22, 24, 29, 31, 30, 32, 37, 39, 38, 40, 45, 47, 46, 48, 53, 55, 54, 56, 61, 63, 62, 64};
    private final double pmtW = 8;
    private RICHtile[] rtile = new RICHtile[nleftTile[nleftTile.length - 1]];
    private PixelXY[][] pixXY = new PixelXY[nleftTile[nleftTile.length - 1]][192];
    private Boolean autoScaled = false;

    public RICHmonitor(String name) {
        super(name);
        this.setDetectorTabNames("TDC0", "TDC1", "delta TDC", "Occupancy 1D", "RICH Occupancy");
        this.init(true);

        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.LINE_AXIS));
        JCheckBox chbox = new JCheckBox("Log Z for occupancy");
        chbox.addChangeListener(new CheckBoxListener());
        JLabel lbl0 = new JLabel("Set maximum");
        JLabel lbl1 = new JLabel("%");

        JFormattedTextField maxField = new JFormattedTextField(NumberFormat.getNumberInstance());
        maxField.setValue(new Double(100));
        maxField.setColumns(3);
        maxField.addActionListener(new MaxFieldListener());

        topPanel.add(lbl0);
        topPanel.add(maxField);
        topPanel.add(lbl1);
        topPanel.add(Box.createHorizontalStrut(15));
        topPanel.add(new JSeparator(JSeparator.VERTICAL));
        topPanel.add(Box.createHorizontalStrut(15));
        topPanel.add(chbox);

        getDetectorView().initUI();
        getDetectorView().getToolbar().add(topPanel);
    }

    private class MaxFieldListener implements ActionListener {

        public void actionPerformed(ActionEvent evt) {
            JFormattedTextField source = (JFormattedTextField) evt.getSource();
            double fraction = Double.parseDouble(source.getText()) / 100.0;
            System.out.println(fraction);
            double newmax = getDataGroup().getItem(0, 0, 0).getH2F("occRICH").getMaximum() * fraction;
            getDetectorCanvas().getCanvas("RICH Occupancy").getPad().getAxisZ().setRange(0, newmax);
            getDetectorCanvas().getCanvas("Occupancy 1D").getPad().getAxisY().setRange(0, newmax);
        }
    }

    private class CheckBoxListener implements ChangeListener {

        public void stateChanged(ChangeEvent e) {
            JCheckBox source = (JCheckBox) e.getSource();
            getDetectorCanvas().getCanvas("RICH Occupancy").getPad().getAxisZ().setLog(source.isSelected());
            getDetectorCanvas().getCanvas("Occupancy 1D").getPad().getAxisY().setLog(source.isSelected());
        }
    }

    @Override
    public void createHistos() {
        this.setNumberOfEvents(0);
        H2F occRICH = new H2F("occRICH", "TDC", 250, -120, 130, 220, -15, 205);
        DataGroup occrich = new DataGroup(1, 1);
        occrich.addDataSet(occRICH, 0);
        this.getDataGroup().add(occrich, 0, 0, 0);
        H1F occRICH1D = new H1F("occRICH", "TDC", 30000, 0, 30000);
        DataGroup grocc1D = new DataGroup(1, 1);
        grocc1D.addDataSet(occRICH1D, 0);
        this.getDataGroup().add(grocc1D, 0, 0, 1);

        for (int itile = 0; itile < nleftTile[nleftTile.length - 1]; itile++) {
            for (int imaroc = 0; imaroc < 3; imaroc++) {
                for (int itdc = 0; itdc < 3; itdc++) {
                    DataGroup grtdc = new DataGroup(8, 8);
                    for (int irow = 0; irow < 8; irow++) {
                        for (int icol = 0; icol < 8; icol++) {
                            int ipix = irow * 8 + icol;
                            H1F htdc;
                            if (itdc < 2) {
                                htdc = new H1F("htdc" + (ipix + 1), "tile " + (itile + 1) + " pmt " + imaroc + " pix " + (ipix + 1), 200, 150, 550);
                            } else {
                                htdc = new H1F("htdc" + (ipix + 1), "tile " + (itile + 1) + " pmt " + imaroc + " pix " + (ipix + 1), 75, 0, 150);
                            }

                            htdc.setFillColor(38);
                            grtdc.addDataSet(htdc, ipix);
                        }
                    }
                    this.getDataGroup().add(grtdc, itdc + 1, itile, imaroc);
                }
            }
        }
    }

    @Override
    public void analyze() {

    }

    @Override
    public void plotHistos() {
        this.getDetectorCanvas().getCanvas("RICH Occupancy").draw(this.getDataGroup().getItem(0, 0, 0));
        this.getDetectorCanvas().getCanvas("Occupancy 1D").draw(this.getDataGroup().getItem(0, 0, 1));
    }

    public void updateHistos(DetectorShape2D shape) {
        //when shape is selected, draw the histogram for the 8x8 pixel array
        int tileId = shape.getDescriptor().getLayer();
        int imaroc = shape.getDescriptor().getComponent();

        String[] ttl = new String[]{"TDC0", "TDC1", "delta TDC"};

        for (int itdc = 0; itdc < ttl.length; itdc++) {
            this.getDetectorCanvas().getCanvas(ttl[itdc]).clear();
            this.getDetectorCanvas().getCanvas(ttl[itdc]).draw(this.getDataGroup().getItem(itdc + 1, tileId - 1, imaroc));
            this.getDetectorCanvas().getCanvas(ttl[itdc]).update();
        }
    }

    @Override
    public void processEvent(DataEvent event) {
        // process event info and save into data group

        Map<Integer, List<Integer>> tdcs = new HashMap<>();

        if (event.hasBank("RICH::tdc") == true) {
            DataBank bank = event.getBank("RICH::tdc");
            int rows = bank.rows();
            for (int irow = 0; irow < rows; irow++) {
                int tileID = bank.getByte("layer", irow) & 0xFF;
                short channel = bank.getShort("component", irow);
                int edge = bank.getByte("order", irow);
                int TDC = bank.getInt("TDC", irow);

                int imaroc = (channel - 1) / 64;
                int ipix = chan2pix[(channel - 1) % 64] - 1;

                PixelXY pxy = rtile[tileID - 1].getPixel(imaroc, ipix);
                this.getDataGroup().getItem(0, 0, 0).getH2F("occRICH").fill(pxy.x, pxy.y);
                this.getDataGroup().getItem(0, 0, 1).getH1F("occRICH").fill(tileID * 192 + channel - 1);

                this.getDataGroup().getItem(edge + 1, tileID - 1, imaroc).getH1F("htdc" + (ipix + 1)).fill(TDC);

                Integer id = tileID * 1000 + imaroc * 100 + ipix;
                if (!tdcs.containsKey(id)) {
                    tdcs.put(id, new ArrayList<Integer>());
                }
                tdcs.get(id).add(TDC);
            }
        }

        for (Map.Entry<Integer, List<Integer>> entry : tdcs.entrySet()) {
            if (entry.getValue().size() > 1) {
                Collections.sort(entry.getValue());
                int tileID = entry.getKey() / 1000;
                int imaroc = (entry.getKey() % 1000) / 100;
                int ipix = entry.getKey() % 64;
                int t0 = entry.getValue().get(0);
                int deltaT = entry.getValue().get(1) - t0;
                this.getDataGroup().getItem(3, tileID - 1, imaroc).getH1F("htdc" + (ipix + 1)).fill(deltaT);
            }
        }
    }

    @Override
    public void drawDetector() {
        this.getDetectorView().setName("RICH");

        double y0 = 0;
        for (int irow = 0, itile = 0; irow < nleftTile.length; irow++) {
            double x0 = (3 + irow * 0.5) * pmtW + Math.ceil((6 + irow) / 6.0);
            for (; itile < nleftTile[irow]; itile++) {
                RICHtile r1 = new RICHtile(itile + 1, Arrays.asList(twotilers).contains(itile + 1) ? 2 : 3);
                r1.setPosition(x0 - r1.getWidth(), y0);
                for (DetectorShape2D pmt : r1.pmts) {
                    this.getDetectorView().getView().addShape("RICH", pmt);
                }
                rtile[itile] = r1;
                x0 -= r1.getWidth() + 1;
            }
            y0 -= pmtW + 1;

        }

    }

    private class PixelXY {

        double x, y;

        PixelXY(double x, double y) {
            this.x = x;
            this.y = y;
        }
    }

    private class RICHtile {

        private int nmapmts;
        private DetectorShape2D pmts[];
        PixelXY[] pxy = new PixelXY[192];

        RICHtile(int id) {
            this(id, 3);
        }

        RICHtile(int id, int nmapmts) {
            this.nmapmts = nmapmts;
            pmts = new DetectorShape2D[nmapmts];

            for (int imaroc = 0; imaroc < nmapmts; imaroc++) {
                pmts[imaroc] = new DetectorShape2D();
                pmts[imaroc].getDescriptor().setType(DetectorType.RICH);
                pmts[imaroc].getDescriptor().setSectorLayerComponent(1, id, nmapmts == 2 && imaroc == 1 ? imaroc + 1 : imaroc);
                pmts[imaroc].createBarXY(pmtW, pmtW);
            }
        }

        double getWidth() {
            return pmtW * nmapmts;
        }

        void setPosition(double x0, double y0) {
            for (int imaroc = 0; imaroc < nmapmts; imaroc++) {
                double x1 = x0 + (nmapmts - imaroc - 1) * pmtW;
                pmts[imaroc].getShapePath().translateXYZ(x1, y0, 0.0);
                for (int irow = 0; irow < 8; irow++) {
                    for (int icol = 0; icol < 8; icol++) {
                        pxy[imaroc * 64 + irow * 8 + icol] = new PixelXY(x1 + icol, -y0 - irow);
                    }
                }
            }
        }

        PixelXY getPixel(int imaroc, int ipix) {
            if (nmapmts == imaroc) {
                imaroc--;
            }
            return pxy[imaroc * 64 + ipix];
        }
    }

    @Override
    public void timerUpdate() {
        /*
        int newmax = (int) this.getDataGroup().getItem(0, 0, 0).getH2F("occRICH").getMaximum();
        slider.setMaximum(newmax);
        slider.setMajorTickSpacing(newmax);
        this.getDetectorView().update();
         */
    }

}
