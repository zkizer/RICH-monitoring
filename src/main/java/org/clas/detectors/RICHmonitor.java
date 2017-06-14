/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clas.detectors;

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
 * @author justin
 */
public class RICHmonitor extends DetectorMonitor {
    
    private final int PMTRows = 24;
    
    public RICHmonitor(String name){
        super(name);
        this.setDetectorTabNames("Ring","Histograms");
        this.init(true);
    }
    
    @Override
    public void createHistos(){
    //build histograms for pixels
    this.setNumberOfEvents(0);  
        for(int irow=1;irow<=PMTRows;irow++){
            int nPMTinaRow = 5+irow;
            for(int ipmt=1;ipmt<=nPMTinaRow;ipmt++){
                DataGroup dg = new DataGroup(8,8);
                int count = 0;
                for(int i =1; i<=8;i++){
                    for(int j=1;j<=8;j++){
                        H1F histo = new H1F("row"+irow+"pmt"+ipmt+"pixrow"+i+"pixcol"+j,"row"+irow+"pmt"+ipmt+"pixrow"+i+"pixcol"+j,6,1,7);
                        histo.setTitle("Row "+irow+" PMT "+ipmt+" Pixel "+(count+1));
                        dg.addDataSet(histo, count);
                        count++;
                    }
                }
            this.getDataGroup().add(dg,0,irow,ipmt);
            }
        }
        
    //build graph for displaying the ring
    H2F graph = new H2F("ring","ring",120,0,20,120,0,20);
    DataGroup dg1 = new DataGroup(1,1);
    dg1.addDataSet(graph, 0);
    this.getDataGroup().add(dg1,0,0,0);
   }
    
    @Override
    public void plotHistos() {
        this.getDetectorCanvas().getCanvas("Ring").draw(this.getDataGroup().getItem(0,0,0).getH2F("ring"));
    }
    
    public void UpdatedHistos(DetectorShape2D shape){
        int row = shape.getDescriptor().getLayer();
        int pmt = shape.getDescriptor().getComponent();
        this.getDetectorCanvas().getCanvas("Histograms").clear();
        this.getDetectorCanvas().getCanvas("Histograms").draw(this.getDataGroup().getItem(0,row,pmt));
        this.getDetectorCanvas().getCanvas("Histograms").update();
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
                int     order = bank.getByte("order",i);
                //double xpos = -row*(dimension+PMTsep)/2+PMTnum*(dim+PMTsep)+Pixcol*pixelsize
                //double ypos = irow*(dim+PMTsep)+Pixrow*pixelsize
                if(ADC>0 && order==0) this.getDataGroup().getItem(0,0,0).getH2F("ring").fill(paddle*1.0,sector*1.0);
            }
       }       
    }
    
        @Override
    public void drawDetector() {
        int dimension = 20;
        double xpos = 0;
        double ypos = 0;
        double PMTsep = 3;
        for(int irow=1; irow<=PMTRows; irow++){
            int PMTinRow = 5 + irow;
            ypos = -irow*(dimension+PMTsep);
            for(int ipmt=1; ipmt<=PMTinRow; ipmt++){
                DetectorShape2D shape = new DetectorShape2D();
                shape.getDescriptor().setType(DetectorType.UNDEFINED);
                shape.getDescriptor().setSectorLayerComponent(4,irow, ipmt);
                shape.createBarXY(dimension,dimension);
                shape.getShapePath().translateXYZ(xpos,ypos,0);
                xpos = xpos + (dimension+PMTsep);
                this.getDetectorView().getView().addShape("RICH",shape);
            }
            xpos = -(irow)*(dimension+PMTsep)/2;
        }
        
        this.getDetectorView().setName("RICH");
        this.getDetectorView().updateBox();

    }
    
    @Override
    public void timerUpdate(){
        
    }
}
