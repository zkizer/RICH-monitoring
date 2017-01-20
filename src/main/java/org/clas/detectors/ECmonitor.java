/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clas.detectors;

import java.awt.BorderLayout;
import javax.swing.JSplitPane;
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
 * @author devita
 */
public class ECmonitor  extends DetectorMonitor {

    private final int[] npaddles = new int[]{72,36,36};
        
    
    public ECmonitor(String name) {
        super(name);
        
        this.init();
    }

    @Override
    public void createHistos() {
        // initialize canvas and create histograms
        this.setNumberOfEvents(0);
        this.getDetectorCanvas().getCanvas("canvas1").divide(3, 3);
        this.getDetectorCanvas().getCanvas("canvas1").setGridX(false);
        this.getDetectorCanvas().getCanvas("canvas1").setGridY(false);
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
            H2F occ = new H2F("occ_lay"+layer, "layer " + layer + " Occupancy", this.npaddles[stack-1], 1, npaddles[stack-1]+1, 6, 1, 7);
            occ.setTitleX(stacks[stack-1] + " " + views[view-1] + " strip");
            occ.setTitleY("sector");
            DataGroup dg = new DataGroup(2,1);
            dg.addDataSet(occ, 0);
            this.getDataGroup().add(dg,0,layer,0);
        }
        
        // plotting histos
        for(int layer=1; layer <=9; layer++) {
            this.getDetectorCanvas().getCanvas("canvas1").cd((layer-1)+0);
            this.getDetectorCanvas().getCanvas("canvas1").draw(this.getDataGroup().getItem(0,layer,0).getH2F("occ_lay"+layer));
        }
        this.getDetectorCanvas().getCanvas("canvas1").update();
        this.getDetectorView().getView().repaint();
        this.getDetectorView().update();

    }

    public void drawDetector() {
        this.getDetectorView().setName("EC");
        this.getDetectorView().updateBox();
    }

    @Override
    public void init() {
        this.getDetectorPanel().setLayout(new BorderLayout());
        this.drawDetector();
        JSplitPane   splitPane = new JSplitPane();
        splitPane.setLeftComponent(this.getDetectorView());
        splitPane.setRightComponent(this.getDetectorCanvas());
        this.getDetectorPanel().add(splitPane,BorderLayout.CENTER); 
        this.createHistos();
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
                if(adc>0 && time>=0) this.getDataGroup().getItem(0,layer,0).getH2F("occ_lay"+layer).fill(comp*1.0,sector*1.0);
                if(layer==1)      this.getDetectorSummary().getH1F("sumPCAL").fill(sector*1.0);
                else if(layer==2) this.getDetectorSummary().getH1F("sumECin").fill(sector*1.0);
                else              this.getDetectorSummary().getH1F("sumECout").fill(sector*1.0);

	    }
    	}        
    }

    @Override
    public void resetEventListener() {
        System.out.println("Resetting EC histogram");
        this.createHistos();
    }

    @Override
    public void timerUpdate() {
 //       System.out.println("Updating FTOF canvas");
    }


}
