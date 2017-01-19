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
public class FTOFmonitor  extends DetectorMonitor {

    private final int[] npaddles = new int[]{23,62,5};
        
    
    public FTOFmonitor(String name) {
        super(name);
        
        this.init();
    }

    @Override
    public void createHistos() {
        // initialize canvas and create histograms
        this.setNumberOfEvents(0);
        this.getDetectorCanvas().getCanvas("canvas1").divide(2, 3);
        this.getDetectorCanvas().getCanvas("canvas1").setGridX(false);
        this.getDetectorCanvas().getCanvas("canvas1").setGridY(false);
        H1F sumP1A = new H1F("sumP1A","sumP1A",6,1,7);
        sumP1A.setTitleX("sector");
        sumP1A.setTitleY("FTOF P1A hits");
        sumP1A.setFillColor(34);
        H1F sumP1B = new H1F("sumP1B","sumP1B",6,1,7);
        sumP1B.setTitleX("sector");
        sumP1B.setTitleY("FTOF P1B hits");
        sumP1B.setFillColor(34);
        H1F sumP2 = new H1F("sumP2","sumP2",6,1,7);
        sumP2.setTitleX("sector");
        sumP2.setTitleY("FTOF P2 hits");
        sumP2.setFillColor(34);
        DataGroup sum = new DataGroup(3,1);
        sum.addDataSet(sumP1A, 0);
        sum.addDataSet(sumP1B, 1);
        sum.addDataSet(sumP2,  2);
        this.setDetectorSummary(sum);
        for(int layer=1; layer <= 3; layer++) {
            H2F occL = new H2F("occL", "layer " + layer + " Occupancy", this.npaddles[layer-1], 1, npaddles[layer-1]+1, 6, 1, 7);
            occL.setTitleX("paddle");
            occL.setTitleY("sector");
            occL.setTitle("Left PMTs");
            H2F occR = new H2F("occR", "layer " + layer + " Occupancy", this.npaddles[layer-1], 1, npaddles[layer-1]+1, 6, 1, 7);
            occR.setTitleX("paddle");
            occR.setTitleY("sector");
            occR.setTitle("Right PMTs");
           
            DataGroup dg = new DataGroup(2,1);
            dg.addDataSet(occL, 0);
            dg.addDataSet(occR, 1);
            this.getDataGroup().add(dg,0,layer,0);
        }
        for(int layer=1; layer <=3; layer++) {
            this.getDetectorCanvas().getCanvas("canvas1").cd((layer-1)*2+0);
            this.getDetectorCanvas().getCanvas("canvas1").draw(this.getDataGroup().getItem(0,layer,0).getH2F("occL"));
            this.getDetectorCanvas().getCanvas("canvas1").cd((layer-1)*2+1);
            this.getDetectorCanvas().getCanvas("canvas1").draw(this.getDataGroup().getItem(0,layer,0).getH2F("occR"));
        }
        this.getDetectorCanvas().getCanvas("canvas1").update();
        this.getDetectorView().getView().repaint();
        this.getDetectorView().update();
    }

    public void drawDetector() {
        double FTOFSize = 500.0;
        int[]     widths   = new int[]{6,15,25};
        int[]     lengths  = new int[]{6,15,25};

        String[]  names    = new String[]{"FTOF 1A","FTOF 1B","FTOF 2"};
        for(int sector = 1; sector <= 6; sector++){
            double rotation = Math.toRadians((sector-1)*(360.0/6)+90.0);
            for(int layer = 1; layer <=3; layer++){
                int width  = widths[layer-1];
                int length = lengths[layer-1];
                for(int paddle = 1; paddle <= npaddles[layer-1]; paddle++){
                    DetectorShape2D shape = new DetectorShape2D();
                    shape.getDescriptor().setType(DetectorType.FTOF);
                    shape.getDescriptor().setSectorLayerComponent(sector, layer, paddle);
                    shape.createBarXY(20 + length*paddle, width);
                    shape.getShapePath().translateXYZ(0.0, 40 + width*paddle , 0.0);
                    shape.getShapePath().rotateZ(rotation);
                    this.getDetectorView().getView().addShape(names[layer-1], shape);
                }
            }
        }
        this.getDetectorView().setName("FTOF");
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
        if(event.hasBank("FTOF::adc")==true){
            DataBank  bank = event.getBank("FTOF::adc");
            int rows = bank.rows();
            for(int i = 0; i < rows; i++){
                int    sector = bank.getByte("sector",i);
                int     layer = bank.getByte("layer",i);
                int    paddle = bank.getShort("component",i);
                int       ADC = bank.getInt("ADC",i);
                float    time = bank.getFloat("time",i);
                int     order = bank.getByte("order",i); // order specifies left-right for ADC
//                           System.out.println("ROW " + i + " SECTOR = " + sector
//                                 + " LAYER = " + layer + " PADDLE = "
//                                 + paddle + " ADC = " + ADC);    
                if(ADC>0 && order==0) this.getDataGroup().getItem(0,layer,0).getH2F("occL").fill(paddle*1.0,sector*1.0);
                if(ADC>0 && order==1) this.getDataGroup().getItem(0,layer,0).getH2F("occR").fill(paddle*1.0,sector*1.0);
                if(layer==1)      this.getDetectorSummary().getH1F("sumP1A").fill(sector*1.0);
                else if (layer==2)this.getDetectorSummary().getH1F("sumP1B").fill(sector*1.0);
                else              this.getDetectorSummary().getH1F("sumP2").fill(sector*1.0);
            }
        }
    }

    @Override
    public void resetEventListener() {
        System.out.println("Resetting FTOF histogram");
        this.createHistos();
    }

    @Override
    public void timerUpdate() {

    }


}
