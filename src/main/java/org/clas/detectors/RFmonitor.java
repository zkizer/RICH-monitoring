/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clas.detectors;

import java.util.ArrayList;
import org.clas.viewer.DetectorMonitor;
import org.jlab.groot.data.H1F;
import org.jlab.groot.group.DataGroup;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;

/**
 *
 * @author devita
 */
public class RFmonitor extends DetectorMonitor {
    

    public RFmonitor(String name) {
        super(name);
        this.setDetectorTabNames("RF TDCs");
        this.init(false);
    }

    
    @Override
    public void createHistos() {
        // create histograms
        this.setNumberOfEvents(0);
        H1F summary = new H1F("summary","summary",6,1,7);
        summary.setTitleX("sector");
        summary.setTitleY("DC hits");
        summary.setFillColor(33);
        DataGroup sum = new DataGroup(1,1);
        sum.addDataSet(summary, 0);
        this.setDetectorSummary(sum);
        H1F rf1 = new H1F("rf1","rf1", 100,0.,120000);
        rf1.setTitleX("RF1 tdc");
        rf1.setTitleY("Counts");
        rf1.setFillColor(33);
        H1F rf2 = new H1F("rf2","rf2", 100,0.,120000);
        rf2.setTitleX("RF2 tdc");
        rf2.setTitleY("Counts");
        rf2.setFillColor(36);
        H1F rfdiff = new H1F("rfdiff","rfdiff", 160, 2.,6.);
        rfdiff.setTitleX("RF diff");
        rfdiff.setTitleY("Counts");
        H1F rf1diff = new H1F("rf1diff","rf1diff", 160, 169.,173.);
        rf1diff.setTitleX("RF1 diff (ns)");
        rf1diff.setTitleY("Counts");
        H1F rf2diff = new H1F("rf2diff","rf2diff", 160, 169.,173.);
        rf2diff.setTitleX("RF2 diff (ns)");
        rf2diff.setTitleY("Counts");

        DataGroup dg = new DataGroup(1,5);
        dg.addDataSet(rf1, 0);
        dg.addDataSet(rf2, 1);
        dg.addDataSet(rf1diff, 2);
        dg.addDataSet(rf2diff, 3);
        dg.addDataSet(rfdiff, 4);
        this.getDataGroup().add(dg, 0,0,0);
    }
        
    @Override
    public void plotHistos() {
        // initialize canvas and plot histograms
        this.getDetectorCanvas().getCanvas("RF TDCs").divide(2, 2);
        this.getDetectorCanvas().getCanvas("RF TDCs").setGridX(false);
        this.getDetectorCanvas().getCanvas("RF TDCs").setGridY(false);
        this.getDetectorCanvas().getCanvas("RF TDCs").cd(0);
        this.getDetectorCanvas().getCanvas("RF TDCs").draw(this.getDataGroup().getItem(0,0,0).getH1F("rf1"));
        this.getDetectorCanvas().getCanvas("RF TDCs").cd(1);
        this.getDetectorCanvas().getCanvas("RF TDCs").draw(this.getDataGroup().getItem(0,0,0).getH1F("rf2"));
        this.getDetectorCanvas().getCanvas("RF TDCs").cd(2);
        this.getDetectorCanvas().getCanvas("RF TDCs").draw(this.getDataGroup().getItem(0,0,0).getH1F("rf1diff"));
        this.getDetectorCanvas().getCanvas("RF TDCs").cd(3);
        this.getDetectorCanvas().getCanvas("RF TDCs").draw(this.getDataGroup().getItem(0,0,0).getH1F("rfdiff"));

        this.getDetectorCanvas().getCanvas("RF TDCs").update();
    }

    @Override
    public void processEvent(DataEvent event) {
        // process event info and save into data group
        ArrayList<Integer> rf1 = new ArrayList();
        ArrayList<Integer> rf2 = new ArrayList();
        if(event.hasBank("RF::tdc")==true){
            DataBank  bank = event.getBank("RF::tdc");
            this.getDetectorOccupany().addTDCBank(bank);
            int rows = bank.rows();
            for(int i = 0; i < rows; i++){
                int    sector = bank.getByte("sector",i);
                int     layer = bank.getByte("layer",i);
                int      comp = bank.getShort("component",i);
                int       TDC = bank.getInt("TDC",i);
                int     order = bank.getByte("order",i); 
                if(comp==1) {
                    this.getDataGroup().getItem(0,0,0).getH1F("rf1").fill(TDC*1.0);
                    rf1.add(TDC);
                }
                else {
                    this.getDataGroup().getItem(0,0,0).getH1F("rf2").fill(TDC*1.0);
                    rf2.add(TDC);
                }
                this.getDetectorSummary().getH1F("summary").fill(sector*1.0);
            }
        }
//        System.out.println(rf1.size() + " " +rf2.size());
        if(rf1.size()==rf2.size()) {
            for(int i=0; i<rf1.size(); i++) {
                this.getDataGroup().getItem(0,0,0).getH1F("rfdiff").fill((rf1.get(i)-rf2.get(i))*0.025);
                if(i+1<rf1.size()) this.getDataGroup().getItem(0,0,0).getH1F("rf1diff").fill((rf1.get(i+1)-rf1.get(i))*0.025);
            }
        }
    }

    @Override
    public void timerUpdate() {
//        System.out.println("Updating RF");
    }

}
