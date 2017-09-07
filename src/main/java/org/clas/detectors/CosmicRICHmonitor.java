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
import java.util.HashMap;
/**
 *
 * @author justin
 */
public class CosmicRICHmonitor extends DetectorMonitor {
    
    private final int PMTRows = 23;
    private final double pixsize=1.25;
    private final double dimension = 10;
    private final double PMTsep = dimension/8;
    private DetectorShape2D[][] Pixels = new DetectorShape2D[391][64];
    private int counter[][] = new int[391][64];
    private int max = 0;
    
    public CosmicRICHmonitor(String name){
        super(name);
        this.setDetectorTabNames("TDC1","TDC2","TDC Difference");
        this.init(true);
    }
    
    private static final HashMap<Integer, Integer[]> fibermap = new HashMap<Integer, Integer[]>(){
    {
        put(30 ,new Integer[] { 44, 45, 46});   
        put(31 ,new Integer[] { 47, 48, 49});   
        put( 2 ,new Integer[] { 66, 67, 68});   
        put( 1 ,new Integer[] { 69, 70, 71});   
        put( 0 ,new Integer[] { 72, 73, 74});   
        put( 5 ,new Integer[] { 80, 81, 82});   
        put( 4 ,new Integer[] { 83, 84, 85});   
        put( 3 ,new Integer[] { 86, 87, 88});   
        put( 8 ,new Integer[] { 94, 95, 96});   
        put( 7 ,new Integer[] { 97, 98, 99});   
        put( 6 ,new Integer[] {100,101,102});   
        put(12 ,new Integer[] {108,109,110});   
        put(11 ,new Integer[] {111,112,113});   
        put(10 ,new Integer[] {114,115,116});   
        put( 9 ,new Integer[] {117,118,119});   
        put(16 ,new Integer[] {125,126,127});   
        put(15 ,new Integer[] {128,129,130});   
        put(14 ,new Integer[] {131,132,133});   
        put(13 ,new Integer[] {134,135,136});   
        put(20 ,new Integer[] {142,143,144});   
        put(19 ,new Integer[] {145,146,147});   
        put(18 ,new Integer[] {148,149,150});   
        put(17 ,new Integer[] {151,152,153});   
        put(23 ,new Integer[] {162,163,164});   
        put(22 ,new Integer[] {165,166,167});   
        put(21 ,new Integer[] {168,169,170});   
        put(26 ,new Integer[] {182,183,184});    
        put(25 ,new Integer[] {185,186,187});   
        put(24 ,new Integer[] {188,189,190});   
        put(29 ,new Integer[] {202,203,204});   
        put(28 ,new Integer[] {205,206,207});  
     }
    };
    
    @Override
    public void createHistos(){
    //build histograms for pixels
    int pmtcount=1;
    this.setNumberOfEvents(0);  
        for(int irow=1;irow<=PMTRows;irow++){
            int nPMTinaRow = 5+irow;
            for(int ipmt=1;ipmt<=nPMTinaRow;ipmt++){
                DataGroup tdc1 = new DataGroup(8,8);
                DataGroup tdc2 = new DataGroup(8,8);
                DataGroup tdcdifference = new DataGroup(8,8);
                int pixcount = 1;
                for(int i =1; i<=8;i++){
                    for(int j=1;j<=8;j++){
                        H1F TDC1 = new H1F("TDC1 pixel"+pixcount,"TDC1 pixel"+pixcount,10,15,35);
                        TDC1.setTitle("PMT "+pmtcount+" Pixel "+pixcount);
                        TDC1.setFillColor(38);
                        tdc1.addDataSet(TDC1,pixcount-1);
                        H1F TDC2 = new H1F("TDC2 pixel"+pixcount,"TDC2 pixel"+pixcount,10,0,50);
                        TDC2.setTitle("PMT "+pmtcount+" Pixel "+pixcount);
                        TDC2.setFillColor(38);
                        tdc2.addDataSet(TDC2,pixcount-1);
                        H1F TDCdiff = new H1F("TDC difference pixel"+pixcount,"TDC difference pixel"+pixcount,10,0,50);
                        TDCdiff.setTitle("PMT "+pmtcount+" Pixel "+pixcount);
                        TDCdiff.setFillColor(38);
                        tdcdifference.addDataSet(TDCdiff,pixcount-1);
                        pixcount++;
                    }
                }
                this.getDataGroup().add(tdc1,pmtcount,0,0);
                this.getDataGroup().add(tdc2,0,pmtcount,0);
                this.getDataGroup().add(tdcdifference,0,0,pmtcount);
                pmtcount++;
            }
        }
   }
    
    @Override
    public void analyze() {
//        this.getDetectorView().getView().getAxis("RICH").setMinMax(0.0,this.max);
//        this.getDetectorView().getView().getColorAxis().setRange(0.0,this.max);
        for(int ipmt=0;ipmt<391;ipmt++){
            for(int ipixel=0;ipixel<64; ipixel++){
                this.Pixels[ipmt][ipixel].setCounter(this.counter[ipmt][ipixel]);
                if(this.counter[ipmt][ipixel]>0)
                    this.getDetectorView().getView().addShape("RICH",this.Pixels[ipmt][ipixel]);
            }
        }
        this.getDetectorView().update();
    }
    
    public void UpdatedHistos(DetectorShape2D shape){
        //when shape is selected, draw the histogram for the 8x8 pixel array
        int pmt = shape.getDescriptor().getComponent();
        this.getDetectorCanvas().getCanvas("TDC1").clear();
        this.getDetectorCanvas().getCanvas("TDC1").draw(this.getDataGroup().getItem(pmt,0,0));
        this.getDetectorCanvas().getCanvas("TDC1").update();

        this.getDetectorCanvas().getCanvas("TDC2").clear();
        this.getDetectorCanvas().getCanvas("TDC2").draw(this.getDataGroup().getItem(0,pmt,0));
        this.getDetectorCanvas().getCanvas("TDC2").update();
        
        this.getDetectorCanvas().getCanvas("TDC Difference").clear();
        this.getDetectorCanvas().getCanvas("TDC Difference").draw(this.getDataGroup().getItem(0,0,pmt));
        this.getDetectorCanvas().getCanvas("TDC Difference").update();
    }
    
   @Override
    public void processEvent(DataEvent event) { 
        // process event info and save into data group

        if(event.hasBank("RAW::tdc")==true){      
            DataBank  bank = event.getBank("RAW::tdc");
            int rows = bank.rows();
            //bank.show();
            for(int i = 0; i < rows; i++){
                int crate = bank.getByte("crate", i);
                int slot = bank.getByte("slot", i);
                int channel = bank.getShort("channel", i);

		int ifiber = channel/192;
		int ichan = channel%192;
                System.out.println(ifiber + " "+ichan);
                Integer[] tile = fibermap.get(ifiber);
                int pmt = tile[(ichan)/64];
                int pixel = (ichan)%64;
		int rawtdc = bank.getInt("TDC", i);
		int edge = (rawtdc>>15)&0x1;
		int tdc = rawtdc&0x7FFF;

//			println crate+" "+slot+" "+ifiber+" "+ichan
//			panel.fill(ifiber,ichan)
                //if(pixel>0 && pixel<65) this.getDataGroup().getItem(0,0,0).getH2F("ring").fill(pmt*1.0,sector*1.0);
                if(pixel>0 && pixel<65 && pmt>0 && pmt<=391){
                    this.getDataGroup().getItem(pmt,0,0).getH1F("TDC1 pixel"+pixel).fill(rawtdc*1.0);
                    this.getDetectorView().getView().addShape("RICH",this.Pixels[pmt-1][pixel]);
                    //this.getDataGroup().getItem(0,pmt,0).getH1F("TDC2 pixel"+pixel).fill(TDC2*1.0);
                    //this.getDataGroup().getItem(0,0,pmt).getH1F("TDC difference pixel"+pixel).fill(TDCdifference*1.0);
                    this.counter[pmt-1][pixel-1]++;
                    if(this.counter[pmt-1][pixel-1]>this.max){this.max = counter[pmt-1][pixel-1];}
                }
            }
       }
    }
    
    @Override
    public void plotEvent(DataEvent event){
        System.out.println("here");
       if(event.hasBank("RAW::tdc")==true){      
            DataBank  bank = event.getBank("RAW::tdc");
            int rows = bank.rows();
            //bank.show();
            for(int i = 0; i < rows; i++){
                
                int crate = bank.getByte("crate", i);
                int slot = bank.getByte("slot", i);
                int channel = bank.getShort("channel", i);

		int ifiber = channel/192;
		int ichan = channel%192;
                Integer[] tile = fibermap.get(i);
                int pmt = tile[(ichan-1)/64];
                int pixel = (ichan-1)%64;
		int rawtdc = bank.getInt("TDC", i);

                System.out.println(pmt + " " + pixel);
                if(pixel>0 && pixel<65 && pmt>0 && pmt<=391){
                   // if(this.counter[pmt-1][pixel-1]==1)
                        this.getDetectorView().getView().addShape("RICH",this.Pixels[pmt-1][pixel-1]);
//                    this.getDetectorView().getView().getAxis("RICH").setMinMax(0.0,this.max);
//                    this.getDetectorView().getView().getColorAxis().setRange(0.0,this.max);
                        this.Pixels[pmt-1][pixel-1].setCounter(this.counter[pmt-1][pixel]);
                }
            }
            this.getDetectorView().update();
       }       
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
                shape.getShapePath().translateXYZ(xpos,ypos,0);
                xpos = xpos - (dimension+PMTsep);
                this.getDetectorView().getView().addShape("RICH",shape);
                int pixelcount=0;
                for(int i=0;i<8;i++){
                    for(int j = 0;j<8;j++){
                        DetectorShape2D pixel = new DetectorShape2D();
                        pixel.getDescriptor().setSectorLayerComponent(4,pmtcount,pixelcount);
                        pixel.createBarXY(1.25, 1.25);
                        pixel.setColor(0,0,0);
                        double posx = (irow-1) * (dimension+PMTsep)/2 - dimension/2 - (ipmt-1)*(dimension+PMTsep)+(j)*pixsize+pixsize/2; 
                        double posy = (irow-1)*(dimension+PMTsep)+dimension/2-(i)*pixsize-pixsize/2;
                        pixel.getShapePath().translateXYZ(posx,-posy,0);
                        pixel.setCounter(0);
                        this.Pixels[pmtcount-1][pixelcount]=pixel;
                        pixelcount++;
                        }
                    }
                pmtcount++;
            }
            xpos = (irow)*(dimension+PMTsep)/2;
        }        
        this.getDetectorView().setName("RICH");
        this.getDetectorView().updateBox();
    }

}

