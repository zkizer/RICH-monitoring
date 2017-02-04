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
public class ECmonitor  extends DetectorMonitor {

    private final int[] npaddles = new int[]{68,62,62,36,36,36,36,36,36};
        
    
    public ECmonitor(String name) {
        super(name);

        this.setDetectorTabNames("ADC Occupancies","TDC Occupancies");
        this.init(false);
    }

    @Override
    public void createHistos() {
        // initialize canvas and create histograms
        this.setNumberOfEvents(0);
        this.getDetectorCanvas().getCanvas("ADC Occupancies").divide(3, 3);
        this.getDetectorCanvas().getCanvas("ADC Occupancies").setGridX(false);
        this.getDetectorCanvas().getCanvas("ADC Occupancies").setGridY(false);
        this.getDetectorCanvas().getCanvas("TDC Occupancies").divide(3, 3);
        this.getDetectorCanvas().getCanvas("TDC Occupancies").setGridX(false);
        this.getDetectorCanvas().getCanvas("TDC Occupancies").setGridY(false);
        String[] stacks = new String[]{"PCAL","ECin","ECout"};
        String[] views = new String[]{"u","v","w"};
        DataGroup sum = new DataGroup(3,1);
        for(int i=0; i<3; i++) {
            String name = "sum"+stacks[i];
            H1F sumStack = new H1F("sum"+stacks[i],"sum"+stacks[i],6,1,7);
            sumStack.setTitleX("sector");
            sumStack.setTitleY(stacks[i] + " hits");
            sumStack.setFillColor(35);
            sum.addDataSet(sumStack, i);
        }
        this.setDetectorSummary(sum);
        for(int layer=1; layer <= 9; layer++) {
            int stack = (int) ((layer-1)/3) + 1;
            int view  = layer - (stack-1)*3;
            H2F occADC = new H2F("occADC_lay"+layer, "layer " + layer + " Occupancy", this.npaddles[layer-1], 1, npaddles[layer-1]+1, 6, 1, 7);
            occADC.setTitleX(stacks[stack-1] + " " + views[view-1] + " strip");
            occADC.setTitleY("sector");
            H2F occTDC = new H2F("occTDC_lay"+layer, "layer " + layer + " Occupancy", this.npaddles[layer-1], 1, npaddles[layer-1]+1, 6, 1, 7);
            occTDC.setTitleX(stacks[stack-1] + " " + views[view-1] + " strip");
            occTDC.setTitleY("sector");
            DataGroup dg = new DataGroup(2,2);
            dg.addDataSet(occADC, 0);
            dg.addDataSet(occTDC, 0);
            this.getDataGroup().add(dg,0,layer,0);
        }
    }
        
    @Override
    public void plotHistos() {        
        // plotting histos
        for(int layer=1; layer <=9; layer++) {
            this.getDetectorCanvas().getCanvas("ADC Occupancies").cd((layer-1)+0);
            this.getDetectorCanvas().getCanvas("ADC Occupancies").draw(this.getDataGroup().getItem(0,layer,0).getH2F("occADC_lay"+layer));
            this.getDetectorCanvas().getCanvas("TDC Occupancies").cd((layer-1)+0);
            this.getDetectorCanvas().getCanvas("TDC Occupancies").draw(this.getDataGroup().getItem(0,layer,0).getH2F("occTDC_lay"+layer));
        }
        this.getDetectorCanvas().getCanvas("ADC Occupancies").update();
    }

    @Override
    public void processEvent(DataEvent event) {
        // process event info and save into data group
        if(event.hasBank("ECAL::adc")==true){
	    DataBank bank = event.getBank("ECAL::adc");
	    int rows = bank.rows();
	    for(int loop = 0; loop < rows; loop++){
                int sector = bank.getByte("sector", loop);
                int layer  = bank.getByte("layer", loop);
                int comp   = bank.getShort("component", loop);
                int adc    = bank.getInt("ADC", loop);
                float time = bank.getFloat("time",loop);
//                System.out.println(sector + " " + layer + " " + comp + " " + adc + " " + time);
                if(adc>0 && time>=0) this.getDataGroup().getItem(0,layer,0).getH2F("occADC_lay"+layer).fill(comp*1.0,sector*1.0);
                if(adc>0 && time>=0) {
                    if(layer==1)      this.getDetectorSummary().getH1F("sumPCAL").fill(sector*1.0);
                    else if(layer==2) this.getDetectorSummary().getH1F("sumECin").fill(sector*1.0);
                    else              this.getDetectorSummary().getH1F("sumECout").fill(sector*1.0);
                }
	    }
    	}        
        if(event.hasBank("ECAL::tdc")==true){
            DataBank  bank = event.getBank("ECAL::tdc");
            int rows = bank.rows();
            for(int i = 0; i < rows; i++){
                int    sector = bank.getByte("sector",i);
                int     layer = bank.getByte("layer",i);
                int    paddle = bank.getShort("component",i);
                int       TDC = bank.getInt("TDC",i);
                int     order = bank.getByte("order",i); // order specifies left-right for ADC
//                           System.out.println("ROW " + i + " SECTOR = " + sector
//                                 + " LAYER = " + layer + " PADDLE = "
//                                 + paddle + " TDC = " + TDC);    
                if(TDC>0 ) this.getDataGroup().getItem(0,layer,0).getH2F("occTDC_lay"+layer).fill(paddle*1.0,sector*1.0);
//                if(layer==1)      this.getDetectorSummary().getH1F("sumPCAL").fill(sector*1.0);
//                else if (layer==2)this.getDetectorSummary().getH1F("sumECin").fill(sector*1.0);
//                else              this.getDetectorSummary().getH1F("sumECout").fill(sector*1.0);
            }
        }
    }

    @Override
    public void resetEventListener() {
        System.out.println("Resetting EC histogram");
        this.createHistos();
        this.plotHistos();
    }

    @Override
    public void timerUpdate() {
 //       System.out.println("Updating FTOF canvas");
    }


}
