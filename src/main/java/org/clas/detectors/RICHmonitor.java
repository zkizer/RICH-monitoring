/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clas.detectors;

import java.util.Arrays;
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

    private final Integer twotilers[] = {3, 5, 7, 12, 15, 19, 24, 28, 33, 39, 44, 50, 57, 63, 70, 78, 85, 93, 102, 110, 119, 129, 138};
    private final int nleftTile[] = {2, 5, 8, 11, 15, 19, 23, 28, 33, 38, 44, 50, 56, 63, 70, 77, 85, 93, 101, 110, 119, 128, 138};
    private final int chan2pix[] = {60, 58, 59, 57, 52, 50, 51, 49, 44, 42, 43, 41, 36, 34, 35, 33, 28, 26, 27, 25, 20, 18, 19, 17, 12, 10, 11, 9, 4, 2, 3, 1, 5, 7, 6, 8, 13, 15, 14, 16, 21, 23, 22, 24, 29, 31, 30, 32, 37, 39, 38, 40, 45, 47, 46, 48, 53, 55, 54, 56, 61, 63, 62, 64};
    private final double pmtW = 8;
    private RICHtile[] rtile = new RICHtile[nleftTile[nleftTile.length - 1]];
    private PixelXY[][] pixXY = new PixelXY[nleftTile[nleftTile.length - 1]][192];

    public RICHmonitor(String name) {
        super(name);
        this.setDetectorTabNames("RICH Occupancy", "TDC");
        this.init(true);
    }

    @Override
    public void createHistos() {
        this.setNumberOfEvents(0);
        H2F occRICH = new H2F("occRICH", "TDC", 240, -120, 120, 210, 0, 210);
        DataGroup occrich = new DataGroup(1, 1);
        occrich.addDataSet(occRICH, 0);
        this.getDataGroup().add(occrich, 0, 0, 0);

        //build histograms for pixels
        int pmtcount = 1;
        for (int irow = 1; irow <= 1; irow++) {
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
        /*
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
         */
    }

    @Override
    public void plotHistos() {
        this.getDetectorCanvas().getCanvas("RICH Occupancy").draw(this.getDataGroup().getItem(0, 0, 0));
    }

    public void updateHistos(DetectorShape2D shape) {
        //when shape is selected, draw the histogram for the 8x8 pixel array
        int pmt = shape.getDescriptor().getComponent();

        this.getDetectorCanvas().getCanvas("TDC").clear();
        this.getDetectorCanvas().getCanvas("TDC").draw(this.getDataGroup().getItem(pmt, 0, 0));
        this.getDetectorCanvas().getCanvas("TDC").update();
    }

    @Override
    public void processEvent(DataEvent event) {
        // process event info and save into data group

        if (event.hasBank("RICH::tdc") == true) {
            DataBank bank = event.getBank("RICH::tdc");
            int rows = bank.rows();
            for (int i = 0; i < rows; i++) {
                int sector = bank.getByte("sector", i);
                int tileID = bank.getByte("layer", i) & 0xFF;
                short channel = bank.getShort("component", i);
                int TDC = bank.getInt("TDC", i);

                int pmt = 0;
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

                    this.getDetectorView().getView().getAxis("RICH").setMinMax(0.0, 1);
                    this.getDetectorView().getView().getColorAxis().setRange(0.0, 1);
                }
            }
            this.getDetectorView().update();
        }
    }

    @Override
    public void drawDetector() {
        this.getDetectorView().setName("RICH");

        double y0 = 0;
        for (int irow = 0, itile = 0; irow < nleftTile.length; irow++) {
            double x0 = (3 + irow * 0.5) * pmtW + Math.ceil((6 + irow) / 3.0);
            for (; itile < nleftTile[irow]; itile++) {
                System.out.println(itile);
                RICHtile r1 = new RICHtile(itile + 1, Arrays.asList(twotilers).contains(itile + 1) ? 2 : 3);
                r1.setPosition(x0 - r1.getWidth(), y0);
                for (DetectorShape2D pmt : r1.pmts) {
                    this.getDetectorView().getView().addShape("RICH", pmt);
                }
                rtile[itile] = r1;
                x0 -= r1.getWidth() + 1;
                PixelXY pp = r1.getPixel(0,0);
                System.out.println(pp.x+" "+pp.y);
                pp = r1.getPixel(0,16);
                System.out.println(pp.x+" "+pp.y);
                pp = r1.getPixel(0,63);
                System.out.println(pp.x+" "+pp.y);
                pp = r1.getPixel(1,0);
                System.out.println(pp.x+" "+pp.y);
            }
            y0 -= pmtW + 1;
        }

    }

    private class PixelXY {

        double x, y;

        PixelXY(double x, double y) {
            this.x = x;
            this.y = y;
        }
    }

    private class RICHtile {

        private int nmapmts;
        private DetectorShape2D pmts[];
        PixelXY[] pxy = new PixelXY[192];

        RICHtile(int id) {
            this(id, 3);
        }

        RICHtile(int id, int nmapmts) {
            this.nmapmts = nmapmts;
            pmts = new DetectorShape2D[nmapmts];

            for (int imaroc = 0; imaroc < nmapmts; imaroc++) {
                pmts[imaroc] = new DetectorShape2D();
                pmts[imaroc].getDescriptor().setType(DetectorType.RICH);
                pmts[imaroc].getDescriptor().setSectorLayerComponent(1, id, imaroc);
                pmts[imaroc].createBarXY(pmtW, pmtW);
            }
        }

        double getWidth() {
            return pmtW * nmapmts;
        }

        void setPosition(double x0, double y0) {
            for (int imaroc = 0; imaroc < nmapmts; imaroc++) {
                double x1 = x0 + (nmapmts - imaroc - 1) * pmtW;
                pmts[imaroc].getShapePath().translateXYZ(x1, y0, 0.0);
                for(int irow=0;irow<8;irow++){
                    for(int icol=0;icol<8;icol++){
                        pxy[imaroc*64+irow*8+icol] = new PixelXY(x1+icol, y0+irow);
                    }
                }
            }
        }

        PixelXY getPixel(int imaroc, int ipix){
            return pxy[imaroc*64+ipix];
        }
    }
}
