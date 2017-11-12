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

    private final int PMTRows = 23;
    private final double pixsize = 1.25;
    private final double dimension = 10;
    private final double PMTsep = dimension / 8;
    private DetectorShape2D[][] Pixels = new DetectorShape2D[391][64];
    private double PixelPositions[][][] = new double[391][64][2];
    private int counter[][] = new int[391][64];
    private int max = 0;

    int tile2pmt[][] = {{1, 2, 3},
    {4, 5, 6},
    {7, 0, 8},
    {9, 10, 11},
    {12, 0, 13},
    {14, 15, 16},
    {17, 0, 18},
    {19, 20, 21},
    {22, 23, 24},
    {25, 26, 27},
    {28, 29, 30},
    {31, 0, 32},
    {33, 34, 35},
    {36, 37, 38},
    {39, 0, 40},
    {41, 42, 43},
    {44, 45, 46},
    {47, 48, 49},
    {50, 0, 51},
    {52, 53, 54},
    {55, 56, 57},
    {58, 59, 60},
    {61, 62, 63},
    {64, 0, 65},
    {66, 67, 68},
    {69, 70, 71},
    {72, 73, 74},
    {75, 0, 76},
    {77, 78, 79},
    {80, 81, 82},
    {83, 84, 85},
    {86, 87, 88},
    {89, 0, 90},
    {91, 92, 93},
    {94, 95, 96},
    {97, 98, 99},
    {100, 101, 102},
    {103, 104, 105},
    {106, 0, 107},
    {108, 109, 110},
    {111, 112, 113},
    {114, 115, 116},
    {117, 118, 119},
    {120, 0, 121},
    {122, 123, 124},
    {125, 126, 127},
    {128, 129, 130},
    {131, 132, 133},
    {134, 135, 136},
    {137, 0, 138},
    {139, 140, 141},
    {142, 143, 144},
    {145, 146, 147},
    {148, 149, 150},
    {151, 152, 153},
    {154, 155, 156},
    {157, 0, 158},
    {159, 160, 161},
    {162, 163, 164},
    {165, 166, 167},
    {168, 169, 170},
    {171, 172, 173},
    {174, 0, 175},
    {176, 177, 178},
    {179, 180, 181},
    {182, 183, 184},
    {185, 186, 187},
    {188, 189, 190},
    {191, 192, 193},
    {194, 0, 195},
    {196, 197, 198},
    {199, 200, 201},
    {202, 203, 204},
    {205, 206, 207},
    {208, 209, 210},
    {211, 212, 213},
    {214, 215, 216},
    {217, 0, 218},
    {219, 220, 221},
    {222, 223, 224},
    {225, 226, 227},
    {228, 229, 230},
    {231, 232, 233},
    {234, 235, 236},
    {237, 0, 238},
    {239, 240, 241},
    {242, 243, 244},
    {245, 246, 247},
    {248, 249, 250},
    {251, 252, 253},
    {254, 255, 256},
    {257, 258, 259},
    {260, 0, 261},
    {262, 263, 264},
    {265, 266, 267},
    {268, 269, 270},
    {271, 272, 273},
    {274, 275, 276},
    {277, 278, 279},
    {280, 281, 282},
    {283, 284, 285},
    {286, 0, 287},
    {288, 289, 290},
    {291, 292, 293},
    {294, 295, 296},
    {297, 298, 299},
    {300, 301, 302},
    {303, 304, 305},
    {306, 307, 308},
    {309, 0, 310},
    {311, 312, 313},
    {314, 315, 316},
    {317, 318, 319},
    {320, 321, 322},
    {323, 324, 325},
    {326, 327, 328},
    {329, 330, 331},
    {332, 333, 334},
    {335, 0, 336},
    {337, 338, 339},
    {340, 341, 342},
    {343, 344, 345},
    {346, 347, 348},
    {349, 350, 351},
    {352, 353, 354},
    {355, 356, 357},
    {358, 359, 360},
    {361, 362, 363},
    {364, 0, 365},
    {366, 367, 368},
    {369, 370, 371},
    {372, 373, 374},
    {375, 376, 377},
    {378, 379, 380},
    {381, 382, 383},
    {384, 385, 386},
    {387, 388, 389},
    {390, 0, 391}};

    public RICHmonitor(String name) {
        super(name);
        this.setDetectorTabNames("TDC", "RICH Occ");
        this.init(true);
    }

    @Override
    public void createHistos() {
        this.setNumberOfEvents(0);
        H2F occRICH = new H2F("occRICH", "occRICH", 391, 1, 392, 64, 1, 65);
        DataGroup occrich = new DataGroup(1, 1);
        occrich.addDataSet(occRICH, 0);
        this.getDataGroup().add(occrich, 0, 0, 0);

        //build histograms for pixels
        int pmtcount = 1;
        for (int irow = 1; irow <= PMTRows; irow++) {
            int nPMTinaRow = 5 + irow;
            for (int ipmt = 1; ipmt <= nPMTinaRow; ipmt++) {
                DataGroup tdc1 = new DataGroup(8, 8);
                DataGroup tdc2 = new DataGroup(8, 8);
                DataGroup tdcdifference = new DataGroup(8, 8);
                int pixcount = 1;
                for (int i = 1; i <= 8; i++) {
                    for (int j = 1; j <= 8; j++) {
                        H1F TDC1 = new H1F("TDC1 pixel" + pixcount, "TDC1 pixel" + pixcount, 10, 15, 1000);
                        TDC1.setTitle("PMT " + pmtcount + " Pixel " + pixcount);
                        TDC1.setFillColor(38);
                        tdc1.addDataSet(TDC1, pixcount - 1);
                        //H1F TDC2 = new H1F("TDC2 pixel"+pixcount,"TDC2 pixel"+pixcount,10,0,1000);
                        //TDC2.setTitle("PMT "+pmtcount+" Pixel "+pixcount);
                        //TDC2.setFillColor(38);
                        //tdc2.addDataSet(TDC2,pixcount-1);
                        //H1F TDCdiff = new H1F("TDC difference pixel"+pixcount,"TDC difference pixel"+pixcount,10,0,50);
                        //TDCdiff.setTitle("PMT "+pmtcount+" Pixel "+pixcount);
                        //TDCdiff.setFillColor(38);
                        //tdcdifference.addDataSet(TDCdiff,pixcount-1);
                        pixcount++;
                    }
                }
                this.getDataGroup().add(tdc1, pmtcount, 0, 0);
                this.getDataGroup().add(tdc2, pmtcount, 1, 0);
                this.getDataGroup().add(tdcdifference, pmtcount, 2, 0);
                pmtcount++;
            }
        }
    }

    @Override
    public void analyze() {
        this.getDetectorView().getView().getAxis("RICH").setMinMax(0.0, this.max);
        this.getDetectorView().getView().getColorAxis().setRange(0.0, this.max);
        for (int ipmt = 0; ipmt < 391; ipmt++) {
            for (int ipixel = 0; ipixel < 64; ipixel++) {
                this.Pixels[ipmt][ipixel].setCounter(this.counter[ipmt][ipixel]);
                if (this.counter[ipmt][ipixel] == 1) {
                    this.getDetectorView().getView().addShape("RICH", this.Pixels[ipmt][ipixel]);
                }
            }
        }
        this.getDetectorView().update();
    }

    @Override
    public void plotHistos() {
        this.getDetectorCanvas().getCanvas("RICH Occ").draw(this.getDataGroup().getItem(0, 0, 0));
    }

    public void UpdatedHistos(DetectorShape2D shape) {
        //when shape is selected, draw the histogram for the 8x8 pixel array
        int pmt = shape.getDescriptor().getComponent();

        this.getDetectorCanvas().getCanvas("TDC").clear();
        this.getDetectorCanvas().getCanvas("TDC").draw(this.getDataGroup().getItem(pmt, 0, 0));
        this.getDetectorCanvas().getCanvas("TDC").update();
        /*
        this.getDetectorCanvas().getCanvas("TDC2").clear();
        this.getDetectorCanvas().getCanvas("TDC2").draw(this.getDataGroup().getItem(pmt,1,0));
        this.getDetectorCanvas().getCanvas("TDC2").update();
        
        this.getDetectorCanvas().getCanvas("TDC Difference").clear();
        this.getDetectorCanvas().getCanvas("TDC Difference").draw(this.getDataGroup().getItem(pmt,2,0));
        this.getDetectorCanvas().getCanvas("TDC Difference").update();
         */
    }

    @Override
    public void processEvent(DataEvent event) {
        // process event info and save into data group

        if (event.hasBank("RICH::tdc") == true) {
            DataBank bank = event.getBank("RICH::tdc");
            //bank.show();
            int rows = bank.rows();
            for (int i = 0; i < rows; i++) {
                int sector = bank.getByte("sector", i);
                int tileID = bank.getByte("layer", i) & 0xFF;
                short channel = bank.getShort("component", i);
                int TDC = bank.getInt("TDC", i);
                int pmt = tile2pmt[tileID - 1][(channel - 1) / 64];
                int pixel = (channel - 1) % 64;
                if (tileID > 128) {
                    System.out.println(channel);
                }
                //System.out.println(pmt + " " + pixel);
                //int    TDC2 = bank.getInt("TDC2",i);
                //int     TDCdifference = TDC2 - TDC1;
                if (pixel >= 0 && pixel < 65 && pmt > 0 && pmt <= 391) {
                    this.getDataGroup().getItem(0, 0, 0).getH2F("occRICH").fill(pmt, pixel + 1);
                    this.getDataGroup().getItem(pmt, 0, 0).getH1F("TDC1 pixel" + (pixel + 1)).fill(TDC * 1.0);
                    //this.getDataGroup().getItem(pmt,1,0).getH1F("TDC2 pixel"+pixel).fill(TDC2*1.0);
                    //this.getDataGroup().getItem(pmt,2,0).getH1F("TDC difference pixel"+pixel).fill(TDCdifference*1.0);
                    if (this.counter[pmt - 1][pixel] == 1) {
                        this.getDetectorView().getView().addShape("RICH", this.Pixels[pmt - 1][pixel]);
                    }
                    this.getDetectorView().getView().getAxis("RICH").setMinMax(0.0, this.max);
                    this.getDetectorView().getView().getColorAxis().setRange(0.0, this.max);
                    this.Pixels[pmt - 1][pixel].setCounter(this.counter[pmt - 1][pixel]);
                    this.counter[pmt - 1][pixel]++;
                    if (this.counter[pmt - 1][pixel] > this.max) {
                        this.max = counter[pmt - 1][pixel];
                    }
                }
            }
            this.getDetectorView().update();
        }
    }

    /*
    @Override
    public void plotEvent(DataEvent event){
        if(event.hasBank("RICH::tdc")==true){
            DataBank  bank = event.getBank("RICH::tdc");
            int rows = bank.rows();
            for(int i = 0; i < rows; i++){
                int    sector = bank.getByte("sector",i);
                int     pmt = bank.getByte("layer",i);
                int    pixel = bank.getShort("component",i);
                if(pixel>0 && pixel<65 && pmt>0 && pmt<=391){
                    if(this.counter[pmt-1][pixel-1]==1)
                        this.getDetectorView().getView().addShape("RICH",this.Pixels[pmt-1][pixel-1]);
                    this.getDetectorView().getView().getAxis("RICH").setMinMax(0.0,this.max);
                    this.getDetectorView().getView().getColorAxis().setRange(0.0,this.max);
                    this.Pixels[pmt-1][pixel-1].setCounter(this.counter[pmt-1][pixel-1]);
                }
            }
            this.getDetectorView().update();
       }       
    }
     */
    @Override
    public void drawDetector() {
        double xpos = 0;
        double ypos = 0;
        int pmtcount = 1;
        for (int irow = 1; irow <= PMTRows; irow++) {
            int PMTinRow = 5 + irow;
            ypos = -(irow - 1) * (dimension + PMTsep);
            for (int ipmt = 1; ipmt <= PMTinRow; ipmt++) {
                DetectorShape2D shape = new DetectorShape2D();
                shape.getDescriptor().setType(DetectorType.RICH);
                shape.getDescriptor().setSectorLayerComponent(4, 0, pmtcount);
                shape.createBarXY(dimension, dimension);
                shape.getShapePath().translateXYZ(xpos, ypos, 0);
                xpos = xpos - (dimension + PMTsep);
                this.getDetectorView().getView().addShape("RICH", shape);
                int pixelcount = 0;
                for (int i = 0; i < 8; i++) {
                    for (int j = 0; j < 8; j++) {
                        DetectorShape2D pixel = new DetectorShape2D();
                        pixel.getDescriptor().setSectorLayerComponent(4, pmtcount, pixelcount);
                        pixel.createBarXY(1.25, 1.25);
                        pixel.setColor(0, 0, 0);
                        double posx = (irow - 1) * (dimension + PMTsep) / 2 - dimension / 2 - (ipmt - 1) * (dimension + PMTsep) + (j) * pixsize + pixsize / 2;
                        double posy = (irow - 1) * (dimension + PMTsep) + dimension / 2 - (i) * pixsize - pixsize / 2;
                        this.PixelPositions[pmtcount - 1][pixelcount][0] = posx;
                        this.PixelPositions[pmtcount - 1][pixelcount][1] = posy;
                        pixel.getShapePath().translateXYZ(posx, -posy, 0);
                        pixel.setCounter(0);
                        this.Pixels[pmtcount - 1][pixelcount] = pixel;
                        pixelcount++;
                    }
                }
                pmtcount++;
            }
            xpos = (irow) * (dimension + PMTsep) / 2;
        }
        this.getDetectorView().setName("RICH");
        this.getDetectorView().updateBox();
    }

}
