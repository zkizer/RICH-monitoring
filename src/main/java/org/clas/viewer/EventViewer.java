package org.clas.viewer;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.clas.detectors.*;
import org.jlab.detector.decode.CLASDecoder;
import org.jlab.detector.decode.CodaEventDecoder;
import org.jlab.detector.decode.DetectorEventDecoder;
import org.jlab.detector.view.DetectorListener;
import org.jlab.detector.view.DetectorPane2D;
import org.jlab.detector.view.DetectorShape2D;
import org.jlab.detector.base.DetectorType;
import org.jlab.groot.base.GStyle;
import org.jlab.groot.data.TDirectory;
import org.jlab.groot.graphics.EmbeddedCanvasTabbed;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.io.base.DataEventType;
import org.jlab.io.evio.EvioDataEvent;
import org.jlab.io.hipo.HipoDataEvent;
import org.jlab.io.task.DataSourceProcessorPane;
import org.jlab.io.task.IDataEventListener;

/**
 *
 * @author ziegler
 */
public class EventViewer implements IDataEventListener, DetectorListener, ActionListener, ChangeListener {

    List<DetectorPane2D> DetectorPanels = new ArrayList<DetectorPane2D>();
    JTabbedPane tabbedpane = null;
    JPanel mainPanel = null;
    JMenuBar menuBar = null;
    DataSourceProcessorPane processorPane = null;
    EmbeddedCanvasTabbed CLAS12Canvas = null;

    CodaEventDecoder decoder = new CodaEventDecoder();
    CLASDecoder clasDecoder = new CLASDecoder();
    DetectorEventDecoder detectorDecoder = new DetectorEventDecoder();

    private int canvasUpdateTime = 2000;
    private int analysisUpdateTime = 100;
    private int runNumber = 0;

    RICHmonitor rmon = new RICHmonitor("RICH");
    DetectorMonitor[] monitors = {
        rmon
    };

    public EventViewer() {

        // create main panel
        mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());

        tabbedpane = new JTabbedPane();

        processorPane = new DataSourceProcessorPane();
        processorPane.setUpdateRate(analysisUpdateTime);

        mainPanel.add(tabbedpane);
        mainPanel.add(processorPane, BorderLayout.PAGE_END);

        GStyle.getAxisAttributesX().setTitleFontSize(18);
        GStyle.getAxisAttributesX().setLabelFontSize(14);
        GStyle.getAxisAttributesY().setTitleFontSize(18);
        GStyle.getAxisAttributesY().setLabelFontSize(14);
        CLAS12Canvas = new EmbeddedCanvasTabbed("CLAS12");

        JPanel CLAS12View = new JPanel(new BorderLayout());
        JSplitPane splitPanel = new JSplitPane();
        splitPanel.setLeftComponent(CLAS12View);
        splitPanel.setRightComponent(CLAS12Canvas);
        for (int k = 0; k < this.monitors.length; k++) {
            this.tabbedpane.add(this.monitors[k].getDetectorPanel(), this.monitors[k].getDetectorName());
            this.monitors[k].getDetectorView().getView().addDetectorListener(this);
        }
        this.processorPane.addEventListener(this);
    }

    public void actionPerformed(ActionEvent e) {
        System.out.println(e.getActionCommand());
    }


    public JPanel getPanel() {
        return mainPanel;
    }

    private int getRunNumber(DataEvent event) {
        int rNum = this.runNumber;
        DataBank bank = event.getBank("RUN::config");
        if (bank != null) {
            rNum = bank.getInt("run", 0);
        }
        return rNum;
    }

    @Override
    public void dataEventAction(DataEvent event) {

        //EvioDataEvent decodedEvent = deco.DecodeEvent(event, decoder, table);
        //decodedEvent.show();
        HipoDataEvent hipo = null;
        if (event != null) {
//            event.show();

            if (event.getType() == DataEventType.EVENT_START) {
                this.runNumber = this.getRunNumber(event);
            }
            if (this.runNumber != this.getRunNumber(event)) {
//                this.saveToFile("mon12_histo_run_" + runNumber + ".hipo");
                this.runNumber = this.getRunNumber(event);
                resetEventListener();
            }
            if (event instanceof EvioDataEvent) {
                hipo = (HipoDataEvent) clasDecoder.getDataEvent(event);
            } else {
                hipo = (HipoDataEvent) event;

            }

            for (int k = 0; k < this.monitors.length; k++) {
                this.monitors[k].dataEventAction(hipo);
            }
        }
    }

    @Override
    public void processShape(DetectorShape2D shape) {
        System.out.println("SHAPE SELECTED = " + shape.getDescriptor());
        if (shape.getDescriptor().getType() == DetectorType.RICH) {
            rmon.updateHistos(shape);
        }
    }

    @Override
    public void resetEventListener() {
        for (int k = 0; k < this.monitors.length; k++) {
            this.monitors[k].resetEventListener();
            this.monitors[k].timerUpdate();
        }
        //this.plotSummaries();
    }

    public void stateChanged(ChangeEvent e) {
        this.timerUpdate();
    }

    @Override
    public void timerUpdate() {
//        System.out.println("Time to update ...");
        for (int k = 0; k < this.monitors.length; k++) {
            this.monitors[k].timerUpdate();
        }
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("MON12");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        EventViewer viewer = new EventViewer();
        //frame.add(viewer.getPanel());
        frame.add(viewer.mainPanel);
        frame.setJMenuBar(viewer.menuBar);
        frame.setSize(1400, 800);
        frame.setVisible(true);
    }

}
