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
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;

/**
 *
 * @author devita
 */
public class HTCCmonitor  extends DetectorMonitor {
        
    
    public HTCCmonitor(String name) {
        super(name);
        
        this.setDetectorTabNames("Occupancies and Spectra");
        this.init();
    }

    @Override
    public void createHistos() {
        // initialize canvas and create histograms
        this.setNumberOfEvents(0);
        this.getDetectorCanvas().getCanvas("Occupancies and Spectra").divide(2, 2);
        this.getDetectorCanvas().getCanvas("Occupancies and Spectra").setGridX(false);
        this.getDetectorCanvas().getCanvas("Occupancies and Spectra").setGridY(false);
        H1F summary = new H1F("summary","summary",6,1,7);
        summary.setTitleX("sector");
        summary.setTitleY("HTCC hits");
        summary.setFillColor(36);
        DataGroup sum = new DataGroup(1,1);
        sum.addDataSet(summary, 0);
        this.setDetectorSummary(sum);
        H2F occADC = new H2F("occADC", "occADC", 8, 1, 9, 6, 1, 7);
        occADC.setTitleX("ring");
        occADC.setTitleY("sector");
        H2F occTDC = new H2F("occTDC", "occTDC", 8, 1, 9, 6, 1, 7);
        occTDC.setTitleX("ring");
        occTDC.setTitleY("sector");
        H2F adc = new H2F("adc", "adc", 100, 0, 5000, 48, 1, 49);
        adc.setTitleX("adc");
        adc.setTitleY("pmt");
        H2F tdc = new H2F("tdc", "tdc", 100, 0, 250, 48, 1, 49);
        tdc.setTitleX("adc");
        tdc.setTitleY("pmt");
           
        DataGroup dg = new DataGroup(2,2);
        dg.addDataSet(occADC, 0);
        dg.addDataSet(occTDC, 1);
        dg.addDataSet(adc, 2);
        dg.addDataSet(tdc, 3);
        this.getDataGroup().add(dg,0,0,0);
    }

    public void drawDetector() {
        this.getDetectorView().setName("HTCC");
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
        this.plotHistos();
    }
        
    @Override
    public void plotHistos() {
        // plotting histos
        this.getDetectorCanvas().getCanvas("Occupancies and Spectra").cd(0);
        this.getDetectorCanvas().getCanvas("Occupancies and Spectra").draw(this.getDataGroup().getItem(0,0,0).getH2F("occADC"));
        this.getDetectorCanvas().getCanvas("Occupancies and Spectra").cd(1);
        this.getDetectorCanvas().getCanvas("Occupancies and Spectra").draw(this.getDataGroup().getItem(0,0,0).getH2F("occTDC"));
        this.getDetectorCanvas().getCanvas("Occupancies and Spectra").cd(2);
        this.getDetectorCanvas().getCanvas("Occupancies and Spectra").draw(this.getDataGroup().getItem(0,0,0).getH2F("adc"));
        this.getDetectorCanvas().getCanvas("Occupancies and Spectra").cd(3);
        this.getDetectorCanvas().getCanvas("Occupancies and Spectra").draw(this.getDataGroup().getItem(0,0,0).getH2F("tdc"));
        this.getDetectorCanvas().getCanvas("Occupancies and Spectra").update();
        this.getDetectorView().getView().repaint();
        this.getDetectorView().update();
    }

    @Override
    public void processEvent(DataEvent event) {
        // process event info and save into data group
        if(event.hasBank("HTCC::adc")==true){
	    DataBank bank = event.getBank("HTCC::adc");
	    int rows = bank.rows();
	    for(int loop = 0; loop < rows; loop++){
                int sector  = bank.getByte("sector", loop);
                int layer   = bank.getByte("layer", loop);
                int comp    = bank.getShort("component", loop);
                int order   = bank.getByte("order", loop);
                int adc     = bank.getInt("ADC", loop);
                float time  = bank.getFloat("time", loop);
//                System.out.println("ROW " + loop + " SECTOR = " + sector + " LAYER = " + layer + " COMPONENT = " + comp + " ORDER + " + order +
//                      " ADC = " + adc + " TIME = " + time); 
                if(adc>0) this.getDataGroup().getItem(0,0,0).getH2F("occADC").fill(((layer-1)*2+comp)*1.0,sector*1.0);
                if(time>0) this.getDataGroup().getItem(0,0,0).getH2F("occTDC").fill(((layer-1)*2+comp)*1.0,sector*1.0);
                if(adc>0) this.getDataGroup().getItem(0,0,0).getH2F("adc").fill(adc*1.0,((sector-1)*8+(layer-1)*2+comp)*1.0);
                if(time>0) this.getDataGroup().getItem(0,0,0).getH2F("tdc").fill(time,((sector-1)*8+(layer-1)*2+comp)*1.0);
                this.getDetectorSummary().getH1F("summary").fill(sector*1.0);
	    }
    	}
        
    }

    @Override
    public void resetEventListener() {
        System.out.println("Resetting HTCC histogram");
        this.createHistos();
        this.plotHistos();
    }

    @Override
    public void timerUpdate() {

    }


}
