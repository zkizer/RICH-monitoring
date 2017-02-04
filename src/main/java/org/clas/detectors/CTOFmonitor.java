/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clas.detectors;

import org.clas.viewer.DetectorMonitor;
import org.jlab.groot.data.H1F;
import org.jlab.groot.data.H2F;
import org.jlab.groot.group.DataGroup;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;

/**
 *
 * @author devita
 */
public class CTOFmonitor  extends DetectorMonitor {        
    
    public CTOFmonitor(String name) {
        super(name);
        
        this.setDetectorTabNames("ADC Occupancies", "TDC Occupancies");
        this.init(false);
    }

    @Override
    public void createHistos() {
        // initialize canvas and create histograms
        this.setNumberOfEvents(0);
        H1F summary = new H1F("summary","summary",6,1,7);
        summary.setTitleX("sector");
        summary.setTitleY("CTOF hits");
        summary.setFillColor(38);
        DataGroup sum = new DataGroup(1,1);
        sum.addDataSet(summary, 0);
        this.setDetectorSummary(sum);
        H1F occADCL = new H1F("occADCL", "occADCL", 48, 1, 49);
        occADCL.setTitleX("PMT Upstream");
        occADCL.setTitleY("Counts");
        occADCL.setFillColor(38);
        H1F occADCR = new H1F("occADCR", "occADCR", 48, 1, 49);
        occADCR.setTitleX("PMT Downstream");
        occADCR.setTitleY("Counts");
        occADCR.setFillColor(38);
        H2F adcL = new H2F("adcL", "adcL", 50, 0, 5000, 48, 1, 49);
        adcL.setTitleX("ADC Upstream");
        adcL.setTitleY("Counts");
        H2F adcR = new H2F("adcR", "adcR", 50, 0, 5000, 48, 1, 49);
        adcR.setTitleX("ADC Downstream");
        adcR.setTitleY("Counts");   
        H1F occTDCL = new H1F("occTDCL", "occTDCL", 48, 1, 49);
        occTDCL.setTitleX("PMT Upstream");
        occTDCL.setTitleY("Counts");
        occTDCL.setFillColor(38);
        H1F occTDCR = new H1F("occTDCR", "occTDCR", 48, 1, 49);
        occTDCR.setTitleX("PMT Downstream");
        occTDCR.setTitleY("Counts");
        occTDCR.setFillColor(38);
        H2F tdcL = new H2F("tdcL", "tdcL", 50, 0, 50000, 48, 1, 49);
        tdcL.setTitleX("TDC Upstream");
        tdcL.setTitleY("Counts");
        H2F tdcR = new H2F("tdcR", "tdcR", 50, 0, 50000, 48, 1, 49);
        tdcR.setTitleX("TDC Downstream");
        tdcR.setTitleY("Counts"); 
        DataGroup dg = new DataGroup(2,2);
        dg.addDataSet(occADCL, 0);
        dg.addDataSet(occADCR, 1);
        dg.addDataSet(adcL, 2);
        dg.addDataSet(adcR, 3);
        dg.addDataSet(occTDCL, 4);
        dg.addDataSet(occTDCR, 5);
        dg.addDataSet(tdcL, 6);
        dg.addDataSet(tdcR, 7);
        this.getDataGroup().add(dg,0,0,0);
    }
        
    @Override
    public void plotHistos() {        
        // plotting histos
        this.getDetectorCanvas().getCanvas("ADC Occupancies").divide(2, 2);
        this.getDetectorCanvas().getCanvas("ADC Occupancies").setGridX(false);
        this.getDetectorCanvas().getCanvas("ADC Occupancies").setGridY(false);
        this.getDetectorCanvas().getCanvas("TDC Occupancies").divide(2, 2);
        this.getDetectorCanvas().getCanvas("TDC Occupancies").setGridX(false);
        this.getDetectorCanvas().getCanvas("TDC Occupancies").setGridY(false);
        this.getDetectorCanvas().getCanvas("ADC Occupancies").cd(0);
        this.getDetectorCanvas().getCanvas("ADC Occupancies").draw(this.getDataGroup().getItem(0,0,0).getH1F("occADCL"));
        this.getDetectorCanvas().getCanvas("ADC Occupancies").cd(1);
        this.getDetectorCanvas().getCanvas("ADC Occupancies").draw(this.getDataGroup().getItem(0,0,0).getH1F("occADCR"));
        this.getDetectorCanvas().getCanvas("ADC Occupancies").cd(2);
        this.getDetectorCanvas().getCanvas("ADC Occupancies").draw(this.getDataGroup().getItem(0,0,0).getH2F("adcL"));
        this.getDetectorCanvas().getCanvas("ADC Occupancies").cd(3);
        this.getDetectorCanvas().getCanvas("ADC Occupancies").draw(this.getDataGroup().getItem(0,0,0).getH2F("adcR"));
        this.getDetectorCanvas().getCanvas("ADC Occupancies").update();
        this.getDetectorCanvas().getCanvas("TDC Occupancies").cd(0);
        this.getDetectorCanvas().getCanvas("TDC Occupancies").draw(this.getDataGroup().getItem(0,0,0).getH1F("occTDCL"));
        this.getDetectorCanvas().getCanvas("TDC Occupancies").cd(1);
        this.getDetectorCanvas().getCanvas("TDC Occupancies").draw(this.getDataGroup().getItem(0,0,0).getH1F("occTDCR"));
        this.getDetectorCanvas().getCanvas("TDC Occupancies").cd(2);
        this.getDetectorCanvas().getCanvas("TDC Occupancies").draw(this.getDataGroup().getItem(0,0,0).getH2F("tdcL"));
        this.getDetectorCanvas().getCanvas("TDC Occupancies").cd(3);
        this.getDetectorCanvas().getCanvas("TDC Occupancies").draw(this.getDataGroup().getItem(0,0,0).getH2F("tdcR"));
        this.getDetectorCanvas().getCanvas("TDC Occupancies").update();
    }

    @Override
    public void processEvent(DataEvent event) {
        // process event info and save into data group
        if(event.hasBank("CTOF::adc")==true){
	    DataBank bank = event.getBank("CTOF::adc");
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
                if(adc>0) {
                    if(order==0) {
                        this.getDataGroup().getItem(0,0,0).getH1F("occADCL").fill(comp*1.0);
                        this.getDataGroup().getItem(0,0,0).getH2F("adcL").fill(adc*1.0,comp*1.0);
                    }
                    else if(order==1) {
                        this.getDataGroup().getItem(0,0,0).getH1F("occADCR").fill(comp*1.0);
                        this.getDataGroup().getItem(0,0,0).getH2F("adcR").fill(adc*1.0,comp*1.0);
                    }
                }
                this.getDetectorSummary().getH1F("summary").fill(sector*1.0);
	    }
    	}
        if(event.hasBank("CTOF::tdc")==true){
            DataBank  bank = event.getBank("CTOF::tdc");
            int rows = bank.rows();
            for(int i = 0; i < rows; i++){
                int    sector = bank.getByte("sector",i);
                int     layer = bank.getByte("layer",i);
                int      comp = bank.getShort("component",i);
                int       tdc = bank.getInt("TDC",i);
                int     order = bank.getByte("order",i); // order specifies left-right for ADC
//                           System.out.println("ROW " + i + " SECTOR = " + sector
//                                 + " LAYER = " + layer + " PADDLE = "
//                                 + paddle + " TDC = " + TDC);    
                if(tdc>0) {
                    if(order==2) {
                        this.getDataGroup().getItem(0,0,0).getH1F("occTDCL").fill(comp*1.0);
                        this.getDataGroup().getItem(0,0,0).getH2F("tdcL").fill(tdc*1.0,comp*1.0);
                    }
                    else if(order==3) {
                        this.getDataGroup().getItem(0,0,0).getH1F("occTDCR").fill(comp*1.0);
                        this.getDataGroup().getItem(0,0,0).getH2F("tdcR").fill(tdc*1.0,comp*1.0);
                    }
                }
            }
        }        
    }

    @Override
    public void timerUpdate() {

    }


}
