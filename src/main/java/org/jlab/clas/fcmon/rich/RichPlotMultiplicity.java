/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.clas.fcmon.rich;

import java.awt.FlowLayout;
import java.awt.BorderLayout;
import java.util.Map;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import org.jlab.clas.fcmon.rich.RichHit.Edge;
import org.jlab.groot.data.H1F;
import org.jlab.detector.view.DetectorShape2D;

/**
 *
 * @author kenjo
 */
public final class RichPlotMultiplicity extends RichPlot {

    private class HistTDC {

        H1F[] htdc = {new H1F("RICH TDC0", "TDC", 100, 0, 100),
            new H1F("RICH TDC1", "TDC", 100, 0, 100),
            new H1F("RICH TDC2", "TDC", 100, 0, 100)};

        public void setTitle(String title) {
            for (H1F h1 : htdc) {
                String[] titles = title.split(";");
                h1.setTitle(titles[0]);
                if (titles.length > 1) {
                    h1.setTitleX(titles[1]);
                }
                if (titles.length > 2) {
                    h1.setTitleY(titles[2]);
                }
            }
        }
    }

    private final JPanel mainPanel = new JPanel(new BorderLayout());
    private final JComboBox tdcBox, lvlBox;
    private int selectedITile = 0, selectedIMaroc = 0;
    private final int ntiles = 138, nmarocs = 3, npixs = 64;

    private HistTDC hdet = new HistTDC();
    private HistTDC[][] hpmt = new HistTDC[ntiles][nmarocs];

    public RichPlotMultiplicity() {
        lvlBox = new JComboBox(new String[]{"detector", "pmt"});
        lvlBox.addActionListener(ev -> redraw());
        tdcBox = new JComboBox(new String[]{"leading", "trailing", "both"});
        tdcBox.addActionListener(ev -> redraw());

        JToolBar toolBar = new JToolBar();
        toolBar.setLayout(new FlowLayout());
        toolBar.add(tdcBox);
        toolBar.add(lvlBox);

        mainPanel.add(toolBar, BorderLayout.PAGE_START);
        mainPanel.add(canvas, BorderLayout.CENTER);
        mainPanel.setName("Multiplicity");

        reset();
    }

    private void redraw() {
        canvas.clear();

        int itdc = tdcBox.getSelectedIndex();
        if (lvlBox.getSelectedIndex() == 0) {
            canvas.draw(hdet.htdc[itdc]);
        } else {
            canvas.draw(hpmt[selectedITile][selectedIMaroc].htdc[itdc]);
        }
    }

    @Override
    public void reset() {
        hdet = new HistTDC();
        hpmt = new HistTDC[ntiles][nmarocs];

        hdet = new HistTDC();
        hdet.setTitle("Integrated over RICH;number of hits");

        for (int itile = 0; itile < ntiles; itile++) {
            for (int imaroc = 0; imaroc < nmarocs; imaroc++) {
                hpmt[itile][imaroc] = new HistTDC();
                hpmt[itile][imaroc].setTitle("Integrated over PMT: tile " + (itile + 1) + " maroc " + imaroc+";number of hits");
            }
        }

        redraw();
    }

    @Override
    public void fill(Map<Integer, RichHit> rhits) {
        int nleadings = 0, ntrailings = 0;
        for (RichHit rhit : rhits.values()) {
            for (RichHit.RichTDC rtdc : rhit.tdcList) {
                if (rtdc.edge == Edge.LEADING) {
                    nleadings++;
                } else {
                    ntrailings++;
                }
            }
            hpmt[rhit.itile][rhit.imaroc].htdc[0].fill(nleadings);
            hpmt[rhit.itile][rhit.imaroc].htdc[1].fill(ntrailings);
            hpmt[rhit.itile][rhit.imaroc].htdc[2].fill(nleadings + ntrailings);
        }
        hdet.htdc[0].fill(nleadings);
        hdet.htdc[1].fill(ntrailings);
        hdet.htdc[2].fill(nleadings + ntrailings);
    }

    @Override
    public JPanel getPanel() {
        return mainPanel;
    }

    public void processShape(DetectorShape2D shape) {
        selectedITile = shape.getDescriptor().getLayer() - 1;
        selectedIMaroc = shape.getDescriptor().getComponent();
        redraw();
    }
}
