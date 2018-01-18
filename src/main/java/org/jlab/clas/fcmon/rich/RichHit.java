/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.clas.fcmon.rich;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author kenjo
 */
public class RichHit {

    public enum Edge {
        LEADING, TRAILING
    };

    public int itile;
    public int imaroc;
    public int ipix;
    public double x, y;
    public List<RichTDC> tdcList = new ArrayList<>();

    public RichHit(int tileId, int imaroc, int ipix) {
        this.itile = tileId - 1;
        this.imaroc = imaroc;
        this.ipix = ipix;
    }

    public void setXY(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public void fill(int edge, int tdc) {
        tdcList.add(new RichTDC(edge, tdc));
    }

    public class RichTDC {

        public Edge edge = Edge.LEADING;
        public int tdc;

        RichTDC(int iedge, int tdc) {
            if (iedge == 0) {
                this.edge = Edge.TRAILING;
            }
            this.tdc = tdc;
        }
    }
}
