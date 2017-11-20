/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.clas.viewer;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import org.jlab.detector.base.DetectorOccupancy;
import org.jlab.detector.view.DetectorPane2D;
import org.jlab.groot.base.GStyle;
import org.jlab.groot.data.IDataSet;
import org.jlab.groot.data.TDirectory;
import org.jlab.groot.graphics.EmbeddedCanvasTabbed;
import org.jlab.groot.group.DataGroup;
import org.jlab.io.base.DataEvent;
import org.jlab.io.base.DataEventType;
import org.jlab.io.task.IDataEventListener;
import org.jlab.utils.groups.IndexedList;

/**
 *
 * @author devita
 */
public abstract class DetectorMonitor implements IDataEventListener {

    private final String detectorName;
    private ArrayList<String> detectorTabNames = new ArrayList();
    private IndexedList<DataGroup> detectorData = new IndexedList<DataGroup>(3);
    private DataGroup detectorSummary = null;
    private DetectorOccupancy detectorOccupancy = new DetectorOccupancy();
    protected JPanel detectorPanel = null;
    private EmbeddedCanvasTabbed detectorCanvas = null;
    private DetectorPane2D detectorView = null;
    private int numberOfEvents;

    public DetectorMonitor(String name) {
        GStyle.getAxisAttributesX().setTitleFontSize(18);
        GStyle.getAxisAttributesX().setLabelFontSize(14);
        GStyle.getAxisAttributesY().setTitleFontSize(18);
        GStyle.getAxisAttributesY().setLabelFontSize(14);
        this.detectorName = name;
        this.detectorPanel = new JPanel();
        this.detectorCanvas = new EmbeddedCanvasTabbed();
        this.detectorView = new DetectorPane2D();
        this.numberOfEvents = 0;
    }

    public abstract void analyze();

    public abstract void createHistos();

    @Override
    public void dataEventAction(DataEvent event) {

        this.setNumberOfEvents(this.getNumberOfEvents() + 1);
        if (event.getType() == DataEventType.EVENT_START) {
            processEvent(event);
        } else if (event.getType() == DataEventType.EVENT_SINGLE) {
            processEvent(event);
        } else if (event.getType() == DataEventType.EVENT_ACCUMULATE) {
            processEvent(event);
        } else if (event.getType() == DataEventType.EVENT_STOP) {
            analyze();
        }
    }

    public abstract void drawDetector();

    public EmbeddedCanvasTabbed getDetectorCanvas() {
        return detectorCanvas;
    }

    public ArrayList<String> getDetectorTabNames() {
        return detectorTabNames;
    }

    public IndexedList<DataGroup> getDataGroup() {
        return detectorData;
    }

    public String getDetectorName() {
        return detectorName;
    }

    public DetectorOccupancy getDetectorOccupany() {
        return detectorOccupancy;
    }

    public JPanel getDetectorPanel() {
        return detectorPanel;
    }

    public DataGroup getDetectorSummary() {
        return detectorSummary;
    }

    public DetectorPane2D getDetectorView() {
        return detectorView;
    }

    public int getNumberOfEvents() {
        return numberOfEvents;
    }

    public void init(boolean flagDetectorView) {
        // initialize monitoring application
        // detector view is shown if flag is true
        getDetectorPanel().setLayout(new BorderLayout());
        drawDetector();
        JSplitPane splitPane = new JSplitPane();
        splitPane.setLeftComponent(getDetectorView());
        splitPane.setRightComponent(getDetectorCanvas());
        splitPane.setResizeWeight(0.25);
        if (flagDetectorView) {
            getDetectorPanel().add(splitPane, BorderLayout.CENTER);
        } else {
            getDetectorPanel().add(getDetectorCanvas(), BorderLayout.CENTER);
        }
        createHistos();
        plotHistos();
    }

    public abstract void processEvent(DataEvent event);

    public abstract void plotHistos();

    public void printCanvas(String dir) {
        // print canvas to files
        for (int tab = 0; tab < this.detectorTabNames.size(); tab++) {
            String fileName = dir + "/" + this.detectorName + "_canvas" + tab + ".png";
            System.out.println(fileName);
            this.detectorCanvas.getCanvas(this.detectorTabNames.get(tab)).save(fileName);
        }
    }

    @Override
    public void resetEventListener() {
        System.out.println("Resetting " + this.getDetectorName() + " histogram");
        this.createHistos();
        this.plotHistos();
    }

    public void setCanvasUpdate(int time) {
        for (int tab = 0; tab < this.detectorTabNames.size(); tab++) {
            this.detectorCanvas.getCanvas(this.detectorTabNames.get(tab)).initTimer(time);
        }
    }

    public void setDetectorCanvas(EmbeddedCanvasTabbed canvas) {
        this.detectorCanvas = canvas;
    }

    public void setDetectorTabNames(String... names) {
        for (String name : names) {
            this.detectorTabNames.add(name);
        }
        EmbeddedCanvasTabbed canvas = new EmbeddedCanvasTabbed(names);
        this.setDetectorCanvas(canvas);
    }

    public void setDetectorSummary(DataGroup group) {
        this.detectorSummary = group;
    }

    public void setNumberOfEvents(int numberOfEvents) {
        this.numberOfEvents = numberOfEvents;
    }

}
