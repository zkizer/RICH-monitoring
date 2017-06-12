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
import org.jlab.groot.group.DataGroup;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
/**
 *
 * @author justin
 */
public class RICHmonitor extends DetectorMonitor {
    
    private final int[] npaddles = new int[]{23,62,5};
    
    public RICHmonitor(String name){
        super(name);
        this.setDetectorTabNames("Histograms");
        this.init(true);
    }
    
    @Override
    public void createHistos(){
      this.setNumberOfEvents(0);
      H1F adc = new H1F("ADC", "ADC",48,1,49);
      adc.setTitle("ADC");
      H1F tdc = new H1F("tdc","tdc",50,0,60);
      tdc.setTitle("TDC");
      DataGroup dg = new DataGroup(2,1);
      dg.addDataSet(adc,0);
      dg.addDataSet(tdc,1);
      this.getDataGroup().add(dg,0,0,0);
    }
    
    @Override
    public void plotHistos(){
        this.getDetectorCanvas().getCanvas("Histograms").divide(2, 1);
        this.getDetectorCanvas().getCanvas("Histograms").setGridX(false);
        this.getDetectorCanvas().getCanvas("Histograms").setGridY(false);
        this.getDetectorCanvas().getCanvas("Histograms").cd(0);
        this.getDetectorCanvas().getCanvas("Histograms").draw(this.getDataGroup().getItem(0,0,0).getH1F("ADC"));
        this.getDetectorCanvas().getCanvas("Histograms").cd(1);
        this.getDetectorCanvas().getCanvas("Histograms").draw(this.getDataGroup().getItem(0,0,0).getH1F("tdc"));
        this.getDetectorCanvas().getCanvas("Histograms").update();
    }     
    
   @Override
    public void processEvent(DataEvent event) {
        // process event info and save into data group
        System.out.println("HIIII");
       
        if(event.hasBank("RICH::true")==true){
                  System.out.println("***YEAHHH");
            DataBank  bank = event.getBank("RICH::dgtz");
            bank.show();
            //this.getDetectorOccupany().addTDCBank(bank);
            int rows = bank.rows();
            for(int i = 0; i < rows; i++){
                /*
                int    sector = bank.getByte("sector",i);
                int     layer = bank.getByte("layer",i);
                int      wire = bank.getShort("component",i);
                int       TDC = bank.getInt("TDC",i);
                int     order = bank.getByte("order",i); 
                this.getDataGroup().getItem(sector,0,0).getH2F("raw_sec"+sector).fill(wire*1.0,layer*1.0);
                */
                int pmt = bank.getInt("pmt", i);
                System.out.println(pmt);
                this.getDataGroup().getItem(0,0,0).getH1F("tdc").fill(pmt);
                        
            }
       }       
    }
    
        @Override
    public void drawDetector() {
        int dimension = 10;
        int PMT_rows = 24;
        double xpos = 0;
        double ypos = 0;
        double PMTsep = 1.5;
        for(int irow=0; irow<PMT_rows; irow++){
            int PMTinRow = 6 + irow;
            ypos = -irow*(dimension+PMTsep);
            for(int ipmt=0; ipmt<PMTinRow; ipmt++){
                DetectorShape2D shape = new DetectorShape2D();
                shape.getDescriptor().setSectorLayerComponent(4,irow, ipmt);
                shape.createBarXY(dimension,dimension);
                shape.getShapePath().translateXYZ(xpos,ypos,0);
                xpos = xpos + dimension+PMTsep;
                this.getDetectorView().getView().addShape("RICH",shape);
            }
            xpos = -(irow+1)*(dimension+PMTsep)/2;
        }
        
        this.getDetectorView().setName("RICH");
        this.getDetectorView().updateBox();
        
/*
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
*/
    }
    
    @Override
    public void timerUpdate(){
        
    }
}
