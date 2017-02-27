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
public class SVTmonitor extends DetectorMonitor {
    

    public SVTmonitor(String name) {
        super(name);
        this.setDetectorTabNames("Raw Occupancies","Normalized Occupancies");
        this.init(false);
    }

    
    @Override
    public void createHistos() {
        // create histograms
        this.setNumberOfEvents(0);
        H1F summary = new H1F("summary","summary",6,1,7);
        summary.setTitleX("sector");
        summary.setTitleY("SVT hits");
        summary.setFillColor(33);
        DataGroup sum = new DataGroup(1,1);
        sum.addDataSet(summary, 0);
        this.setDetectorSummary(sum);
        for(int region=1; region <= 4; region++) {
            H2F raw = new H2F("raw_reg" + region, "Region " + region + " Occupancy", 128, 1., 129., 256, 1., 129.);
            raw.setTitleX("strip");
            raw.setTitleY("sensor");
            raw.setTitle("region "+region);
            H2F occ = new H2F("occ_reg" + region, "Region " + region + " Occupancy", 128, 1., 129., 256, 1., 129.);
            occ.setTitleX("strip");
            occ.setTitleY("sensor");
            occ.setTitle("Occupancy");
            
            DataGroup dg = new DataGroup(2,1);
            dg.addDataSet(raw, 0);
            dg.addDataSet(occ, 1);
            this.getDataGroup().add(dg, region,0,0);
        }
    }
        
    @Override
    public void plotHistos() {
        // initialize canvas and plot histograms
        this.getDetectorCanvas().getCanvas("Normalized Occupancies").divide(2, 2);
        this.getDetectorCanvas().getCanvas("Normalized Occupancies").setGridX(false);
        this.getDetectorCanvas().getCanvas("Normalized Occupancies").setGridY(false);
        this.getDetectorCanvas().getCanvas("Raw Occupancies").divide(2, 2);
        this.getDetectorCanvas().getCanvas("Raw Occupancies").setGridX(false);
        this.getDetectorCanvas().getCanvas("Raw Occupancies").setGridY(false);
        for(int region=1; region <=4; region++) {
            this.getDetectorCanvas().getCanvas("Normalized Occupancies").getPad(region-1).getAxisZ().setRange(0, 10.);
            this.getDetectorCanvas().getCanvas("Normalized Occupancies").cd(region-1);
            this.getDetectorCanvas().getCanvas("Normalized Occupancies").draw(this.getDataGroup().getItem(region,0,0).getH2F("occ_reg"+region));
//            this.getDataGroup().getItem(0,region,0).getH2F("occ_sec"+region).
            this.getDetectorCanvas().getCanvas("Raw Occupancies").cd(region-1);
            this.getDetectorCanvas().getCanvas("Raw Occupancies").draw(this.getDataGroup().getItem(region,0,0).getH2F("raw_reg"+region));
        }
        this.getDetectorCanvas().getCanvas("Normalized Occupancies").update();
        this.getDetectorCanvas().getCanvas("Raw Occupancies").update();
    }

    @Override
    public void processEvent(DataEvent event) {
        // process event info and save into data group
        if(event.hasBank("SVT::adc")==true){
            DataBank  bank = event.getBank("SVT::adc");
            this.getDetectorOccupany().addTDCBank(bank);
            int rows = bank.rows();
            for(int i = 0; i < rows; i++){
                int    sector = bank.getByte("sector",i);
                int     layer = bank.getByte("layer",i);
                int      comp = bank.getShort("component",i);
                int       ADC = bank.getInt("ADC",i);
                float    time = bank.getFloat("time",i);
                int     order = bank.getByte("order",i);
                int region =1;
                this.getDataGroup().getItem(region,0,0).getH2F("raw_reg"+region).fill(comp*1.0,Sensor(layer,sector)*1.0);
                this.getDetectorSummary().getH1F("summary").fill(sector*1.0);
            }
       }       
    }
    
    static int Sensor(int layer, int sector) {
        int[] shift = {0, 10, 20, 34, 48, 66, 84, 108};
        if (layer == 0 || sector == 0) {
            return -1;
        }
        return sector + shift[layer - 1] - 1;
    }

    @Override
    public void timerUpdate() {
//        System.out.println("Updating DC");
        if(this.getNumberOfEvents()>0) {
            for(int region=1; region <=4; region++) {
                H2F raw = this.getDataGroup().getItem(region,0,0).getH2F("raw_reg"+region);
                for(int loop = 0; loop < raw.getDataBufferSize(); loop++){
                    this.getDataGroup().getItem(region,0,0).getH2F("occ_reg"+region).setDataBufferBin(loop,100*raw.getDataBufferBin(loop)/this.getNumberOfEvents());
                }
            }
        }
    }

}
