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
import org.jlab.io.base.DataEvent;
import org.jlab.io.evio.EvioDataBank;

/**
 *
 * @author devita
 */
public class ECmonitor  extends DetectorMonitor {

    private final int[] npaddles = new int[]{36,36,72};
        
    
    public ECmonitor(String name) {
        super(name);
        
        this.init();
    }

    @Override
    public void createHistos() {
        // initialize canvas and create histograms
        this.setNumberOfEvents(0);
        this.getDetectorCanvas().divide(3, 3);
        this.getDetectorCanvas().setGridX(false);
        this.getDetectorCanvas().setGridY(false);
        String[] stacks = new String[]{"ECin","ECout","PCAL"};
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
            H2F occ = new H2F("occ", "layer " + layer + " Occupancy", this.npaddles[stack-1], 1, npaddles[stack-1]+1, 6, 1, 7);
            occ.setTitleX(stacks[stack-1] + " " + views[view-1] + " strip");
            occ.setTitleY("sector");
            DataGroup dg = new DataGroup(2,1);
            dg.addDataSet(occ, 0);
            this.getDataGroup().add(dg,0,layer,0);
        }
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
        if(event.hasBank("EC::dgtz")==true){
	    EvioDataBank bank = (EvioDataBank) event.getBank("EC::dgtz");
	    int rows = bank.rows();
	    for(int loop = 0; loop < rows; loop++){
                int sector = bank.getInt("sector", loop);
                int stack  = bank.getInt("stack", loop);
                int view   = bank.getInt("view", loop);
                int strip  = bank.getInt("strip", loop);
                int adc    = bank.getInt("ADC", loop);
                int tdc    = bank.getInt("TDC", loop);
                int layer  = (stack-1)*3+view;
    //            System.out.println(sector + " " + layer + " " + paddle + " " + adcl + " " + tdcl);
                if(adc>0 && tdc>0) this.getDataGroup().getItem(0,layer,0).getH2F("occ").fill(strip*1.0,sector*1.0);
                if(stack==1) this.getDetectorSummary().getH1F("sumECin").fill(sector*1.0);
                else         this.getDetectorSummary().getH1F("sumECout").fill(sector*1.0);

	    }
    	}
        if(event.hasBank("PCAL::dgtz")==true){
	    EvioDataBank bank = (EvioDataBank) event.getBank("PCAL::dgtz");
	    int rows = bank.rows();
	    for(int loop = 0; loop < rows; loop++){
                int sector = bank.getInt("sector", loop);
                int stack  = bank.getInt("stack", loop);
                int view   = bank.getInt("view", loop);
                int strip  = bank.getInt("strip", loop);
                int adc    = bank.getInt("ADC", loop);
                int tdc    = bank.getInt("TDC", loop);
                int layer  = (stack+1)*3+view;
    //            System.out.println(sector + " " + layer + " " + paddle + " " + adcl + " " + tdcl);
                if(adc>0 && tdc>0) this.getDataGroup().getItem(0,layer,0).getH2F("occ").fill(strip*1.0,sector*1.0);
                this.getDetectorSummary().getH1F("sumPCAL").fill(sector*1.0);
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
        for(int layer=1; layer <=9; layer++) {
            this.getDetectorCanvas().cd((layer-1)+0);
            this.getDetectorCanvas().draw(this.getDataGroup().getItem(0,layer,0).getH2F("occ"));
        }
        this.getDetectorCanvas().update();
        this.getDetectorView().getView().repaint();
        this.getDetectorView().update();
    }


}
