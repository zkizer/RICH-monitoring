/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clas.detectors;

import java.awt.BorderLayout;
import org.clas.viewer.DetectorMonitor;
import org.jlab.groot.data.H1F;
import org.jlab.groot.data.H2F;
import org.jlab.groot.graphics.EmbeddedCanvasTabbed;
import org.jlab.groot.group.DataGroup;
import org.jlab.io.base.DataEvent;

/**
 *
 * @author devita
 */
public class CTOFmonitor  extends DetectorMonitor {        
    
    public CTOFmonitor(String name) {
        super(name);
        
        EmbeddedCanvasTabbed canvas = new EmbeddedCanvasTabbed("Occupancies");
        this.setDetectorCanvas(canvas);
        this.init();
    }

    @Override
    public void createHistos() {
        // initialize canvas and create histograms
        this.setNumberOfEvents(0);
        this.getDetectorCanvas().getCanvas("Occupancies").divide(2, 2);
        this.getDetectorCanvas().getCanvas("Occupancies").setGridX(false);
        this.getDetectorCanvas().getCanvas("Occupancies").setGridY(false);
        H1F summary = new H1F("summary","summary",6,1,7);
        summary.setTitleX("sector");
        summary.setTitleY("CTOF hits");
        summary.setFillColor(37);
        DataGroup sum = new DataGroup(1,1);
        sum.addDataSet(summary, 0);
        this.setDetectorSummary(sum);
        H2F occADC = new H2F("occADC", "occADC", 8, 1, 9, 6, 1, 7);
        H2F occTDC = new H2F("occTDC", "occTDC", 8, 1, 9, 6, 1, 7);
        H2F adc = new H2F("adc", "adc", 50, 0, 50, 48, 1, 49);
        H2F tdc = new H2F("tdc", "tdc", 100, 0, 250, 48, 1, 49);
        DataGroup dg = new DataGroup(2,1);
        dg.addDataSet(occADC, 0);
        dg.addDataSet(occTDC, 1);
        dg.addDataSet(adc, 2);
        dg.addDataSet(tdc, 3);
        this.getDataGroup().add(dg,0,0,0);
        
        // plotting histos
        this.getDetectorCanvas().getCanvas("Occupancies").cd(0);
        this.getDetectorCanvas().getCanvas("Occupancies").draw(this.getDataGroup().getItem(0,0,0).getH2F("occADC"));
        this.getDetectorCanvas().getCanvas("Occupancies").cd(1);
        this.getDetectorCanvas().getCanvas("Occupancies").draw(this.getDataGroup().getItem(0,0,0).getH2F("occTDC"));
        this.getDetectorCanvas().getCanvas("Occupancies").cd(2);
        this.getDetectorCanvas().getCanvas("Occupancies").draw(this.getDataGroup().getItem(0,0,0).getH2F("adc"));
        this.getDetectorCanvas().getCanvas("Occupancies").cd(3);
        this.getDetectorCanvas().getCanvas("Occupancies").draw(this.getDataGroup().getItem(0,0,0).getH2F("tdc"));
        this.getDetectorCanvas().getCanvas("Occupancies").update();
        this.getDetectorView().getView().repaint();
        this.getDetectorView().update();
    }

    public void drawDetector() {
        this.getDetectorView().setName("CTOF");
        this.getDetectorView().updateBox();
    }

    @Override
    public void init() {
        this.getDetectorPanel().setLayout(new BorderLayout());
//        this.drawDetector();
//        JSplitPane   splitPane = new JSplitPane();
//        splitPane.setLeftComponent(this.getDetectorView());
//        splitPane.setRightComponent(this.getDetectorCanvas());
        this.getDetectorPanel().add(this.getDetectorCanvas(),BorderLayout.CENTER);
        this.createHistos();
    }
        
    @Override
    public void processEvent(DataEvent event) {
        // process event info and save into data group

    }

    @Override
    public void resetEventListener() {
        System.out.println("Resetting CTOF histogram");
        this.createHistos();
    }

    @Override
    public void timerUpdate() {

    }


}
