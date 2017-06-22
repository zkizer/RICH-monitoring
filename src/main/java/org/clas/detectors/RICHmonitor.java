/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clas.detectors;

import org.clas.viewer.DetectorMonitor;
import org.jlab.detector.base.DetectorType;
import org.jlab.detector.view.DetectorShape2D;
import org.jlab.geom.prim.Point3D;
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
    
    private final int PMTRows = 23;
    private final double pixsize=1.25;
    private final double dimension = 10;
    private final double PMTsep = 1.25;
    
    public RICHmonitor(String name){
        super(name);
        this.setDetectorTabNames("Histograms");
        this.init(true);
    }
    
    @Override
    public void createHistos(){
    //build histograms for pixels
    int pmtcount=1;
    this.setNumberOfEvents(0);  
        for(int irow=1;irow<=PMTRows;irow++){
            int nPMTinaRow = 5+irow;
            for(int ipmt=1;ipmt<=nPMTinaRow;ipmt++){
                DataGroup dg = new DataGroup(8,8);
                int pixcount = 1;
                for(int i =1; i<=8;i++){
                    int t=6; //for conversion of pixel matrices to match histogram filling. Pixel order: 
                    //  8  7  6  5  4  3  2  1
                    // 16 15 14 13 12 11 10  9
                    for(int j=1;j<=8;j++){
                        H1F histo = new H1F("pixel"+pixcount,"pixel"+pixcount,10,15,35);
                        histo.setTitle("PMT "+pmtcount+" Pixel "+pixcount);
                        histo.setFillColor(38);
                        dg.addDataSet(histo,pixcount+t);
                        pixcount++;
                        t=t-2;
                    }
                }
                this.getDataGroup().add(dg,0,0,pmtcount);
                pmtcount++;
            }
        }
        
    //build graph for displaying the ring
    //H2F graph = new H2F("ring","ring",333,-150,350,266,0,400);
    //DataGroup dg1 = new DataGroup(1,1);
    //dg1.addDataSet(graph, 0);
    //this.getDataGroup().add(dg1,0,0,0);
   }
    
    @Override
    public void plotHistos() {
        //this.getDetectorCanvas().getCanvas("Ring").draw(this.getDataGroup().getItem(0,0,0).getH2F("ring"));
    }
    
    public void UpdatedHistos(DetectorShape2D shape){
        int pmt = shape.getDescriptor().getComponent();
        this.getDetectorCanvas().getCanvas("Histograms").clear();
        this.getDetectorCanvas().getCanvas("Histograms").draw(this.getDataGroup().getItem(0,0,pmt));
        this.getDetectorCanvas().getCanvas("Histograms").update();
    }
    
   @Override
    public void processEvent(DataEvent event) { 
        // process event info and save into data group
        this.plotEvent(event);
        double pos[] = new double [2];
        if(event.hasBank("RICH::tdc")==true){
            DataBank  bank = event.getBank("RICH::tdc");
            int rows = bank.rows();
            for(int i = 0; i < rows; i++){
                int    sector = bank.getByte("sector",i);
                int     pmt = bank.getShort("pmt",i);
                int    pixel = bank.getShort("pixel",i);
                int       TDC = bank.getInt("TDC1",i);
                //if(pixel>0 && pixel<65) this.getDataGroup().getItem(0,0,0).getH2F("ring").fill(pmt*1.0,sector*1.0);
                if(pixel>0 && pixel<65 && pmt>0 && pmt<=391){

                    this.getDataGroup().getItem(0,0,pmt).getH1F("pixel"+pixel).fill(TDC*1.0);
                    int entries = this.getDataGroup().getItem(0,0,pmt).getH1F("pixel"+pixel).getEntries();
                    
                    pos = getxy(pmt,pixel);
                    DetectorShape2D shape = new DetectorShape2D();
                    shape.getDescriptor().setSectorLayerComponent(4,pmt,pixel);
                    shape.createBarXY(1.2,1.2);
                    shape.getShapePath().translateXYZ(pos[0],-pos[1],0);
                    //create color map
                    double min = 0;
                    double max = 20;
                    double f = (entries-min)/(max-min);
                    double a = (1-f)/0.25;
                    int x = (int) Math.floor(a);
                    double y = Math.floor(255*(a-x));
                    int Y = (int) y;
                    switch(x){
                        case 0:  shape.setColor(255,255-Y,0); break;
                        case 1:  shape.setColor(Y,255,0);  break;
                        case 2:  shape.setColor(0, 255, Y); break;
                        case 3:   shape.setColor(0, 255-Y, 255); break;
                        case 4:  shape.setColor(0, 0, 255); break;
                        default: shape.setColor(255, 255, 0); break;
                    }
                    this.getDetectorView().getView().addShape("RICH",shape);
                }
            }
            this.getDetectorView().update();
       }       
    }
    
    public double[] getxy(int pmt, int pixel){
            double[] pos = new double[2];
            int irow=1;
            int part=6;
            int temp1=pmt;
            int temp2=pmt;

            while(temp1>0 && pmt>6){
                temp1 = temp1 - part;
                if(temp1<=0){
                     break;
                }
                irow++;
                part++;
                temp2=temp1;
            }

            int pixcol = (pixel-1) % 8; //rows & cols start with 0
            int pixrow = (pixel-1) / 8;
            
            pos[0] = (irow-1) * (dimension+PMTsep)/2 + dimension/2 - (temp2-1)*(dimension+PMTsep)-(pixcol)*pixsize-pixsize/2; 
            pos[1] = (irow-1)*(dimension+PMTsep)+dimension/2-((pixrow)*pixsize)-pixsize/2;
            
        return pos;
    }
    
    
    @Override
    public void drawDetector() {
        double xpos = 0;
        double ypos = 0;
        int pmtcount=1;
        for(int irow=1; irow<=PMTRows; irow++){
            int PMTinRow = 5 + irow;
            ypos = -(irow-1)*(dimension+PMTsep);
            for(int ipmt=1; ipmt<=PMTinRow; ipmt++){
                DetectorShape2D shape = new DetectorShape2D();
                shape.getDescriptor().setType(DetectorType.UNDEFINED);
                shape.getDescriptor().setSectorLayerComponent(4,0,pmtcount);
                shape.createBarXY(dimension,dimension);
                shape.setColor(230,230,230);
                shape.getShapePath().translateXYZ(xpos,ypos,0);
                xpos = xpos - (dimension+PMTsep);
                this.getDetectorView().getView().addShape("RICH",shape);
                pmtcount++;
            }
            xpos = (irow)*(dimension+PMTsep)/2;
        }
        
        this.getDetectorView().setName("RICH");
        this.getDetectorView().updateBox();

    }
    
    @Override
    public void timerUpdate(){
        
    }
}
