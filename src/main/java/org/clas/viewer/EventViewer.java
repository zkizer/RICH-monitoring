package org.clas.viewer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.ImageObserver;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.border.SoftBevelBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import org.clas.detectors.*;

import org.jlab.detector.decode.CodaEventDecoder;
import org.jlab.detector.decode.DetectorEventDecoder;
import org.jlab.detector.view.DetectorListener;
import org.jlab.detector.view.DetectorPane2D;
import org.jlab.detector.view.DetectorShape2D;
import org.jlab.groot.data.H2F;
import org.jlab.groot.graphics.EmbeddedCanvas;
import org.jlab.io.base.DataEvent;
import org.jlab.io.base.DataEventType;
import org.jlab.io.task.DataSourceProcessorPane;
import org.jlab.io.task.IDataEventListener;

/**
 *
 * @author ziegler
 */
public class EventViewer implements IDataEventListener, DetectorListener, ActionListener, ChangeListener {
    
    List<DetectorPane2D> DetectorPanels 	= new ArrayList<DetectorPane2D>();
    JTabbedPane tabbedpane           		= null;
    JPanel mainPanel 				= null;
    JPanel mainBar 				= null;
    JPanel buttonPanel 				= null;
    DataSourceProcessorPane processorPane 	= null;
    EmbeddedCanvas CLAS12Canvas                 = null;
    
    CodaEventDecoder               decoder = new CodaEventDecoder();
    DetectorEventDecoder   detectorDecoder = new DetectorEventDecoder();
       
    
    TreeMap<String, List<H2F>>  histos = new TreeMap<String,List<H2F>>();
    
    
   // detector monitors
    DetectorMonitor[] monitors = {
    		new DCmonitor("DC"),
                new TRKmonitor("TRK"),
    		new FTOFmonitor("FTOF"),
    		new HTCCmonitor("HTCC"),
    		new LTCCmonitor("LTCC"),
    		new ECmonitor("EC")
    };
        
    public EventViewer() {    	
        		
	mainPanel = new JPanel();	
	mainPanel.setLayout(new BorderLayout());
        
        mainBar   = new JPanel();	
	mainBar.setLayout(new BorderLayout());
	mainBar.setBorder(BorderFactory.createSoftBevelBorder(SoftBevelBorder.RAISED));
        
        buttonPanel = new JPanel();
        buttonPanel.setLayout(new BorderLayout());
	buttonPanel.setBorder(BorderFactory.createSoftBevelBorder(SoftBevelBorder.LOWERED));
        
      	tabbedpane 	= new JTabbedPane();

        processorPane = new DataSourceProcessorPane();
        processorPane.setUpdateRate(100);

        JButton resetButton = new JButton("Reset");
        resetButton.setActionCommand("ResetHistos");
        resetButton.addActionListener(this);
        buttonPanel.add(resetButton);
        mainBar.add(processorPane);
        mainBar.add(buttonPanel,BorderLayout.LINE_END);

        mainPanel.add(tabbedpane);
        mainPanel.add(mainBar,BorderLayout.PAGE_END);
        
    
        CLAS12Canvas    = new EmbeddedCanvas();
        CLAS12Canvas.divide(3,4);
        CLAS12Canvas.setGridX(false);
        CLAS12Canvas.setGridY(false);
        JPanel    CLAS12View = new JPanel(new BorderLayout());
        JSplitPane splitPanel = new JSplitPane();
        splitPanel.setLeftComponent(CLAS12View);
        splitPanel.setRightComponent(CLAS12Canvas);
        JTextPane clas12Text   = new JTextPane();
        clas12Text.setText("CLAS12\n monitoring plots\n V1.0");
        clas12Text.setEditable(false);
        StyledDocument styledDoc = clas12Text.getStyledDocument();
        SimpleAttributeSet center = new SimpleAttributeSet();
        StyleConstants.setAlignment(center, StyleConstants.ALIGN_CENTER);
        styledDoc.setParagraphAttributes(0, styledDoc.getLength(), center, false);
        clas12Text.setBackground(CLAS12View.getBackground());
        clas12Text.setFont(new Font("Avenir",Font.PLAIN,20));
        JLabel clas12Design = this.getImage("https://www.jlab.org/Hall-B/clas12-web/sidebar/clas12-design.jpg",0.1);
        JLabel clas12Logo   = this.getImage("https://www.jlab.org/Hall-B/pubs-web/logo/CLAS-frame-low.jpg", 0.3);
//        CLAS12View.add(clas12Name,BorderLayout.PAGE_START);
        CLAS12View.add(clas12Design);
        CLAS12View.add(clas12Text,BorderLayout.PAGE_END);
 
        
        tabbedpane.add(splitPanel,"CLAS12");
        tabbedpane.addChangeListener(this);
       
        for(int k =0; k<this.monitors.length; k++) {
                this.tabbedpane.add(this.monitors[k].getDetectorPanel(), this.monitors[k].getDetectorName());
        	this.monitors[k].getDetectorView().getView().addDetectorListener(this);
        }
        this.processorPane.addEventListener(this);
        
    }
      
    public void actionPerformed(ActionEvent e) {
        if(e.getActionCommand().compareTo("ResetHistos")==0){
            for(int k=0; k<this.monitors.length; k++) {
                this.monitors[k].resetEventListener();
                this.monitors[k].timerUpdate();
            }      
        }
    }
    
    public JPanel  getPanel(){
        return mainPanel;
    }


    private JLabel getImage(String path,double scale) {
        JLabel label = null;
        Image image = null;
        try {
            URL url = new URL(path);
            image = ImageIO.read(url);
        } catch (IOException e) {
        	e.printStackTrace();
                System.out.println("Picture upload from " + path + " failed");
        }
        ImageIcon imageIcon = new ImageIcon(image);
        double width  = imageIcon.getIconWidth()*scale;
        double height = imageIcon.getIconHeight()*scale;
        imageIcon = new ImageIcon(image.getScaledInstance((int) width,(int) height, Image.SCALE_SMOOTH));
        label = new JLabel(imageIcon);
        return label;
    }
    
    @Override
    public void dataEventAction(DataEvent event) {
    	
       // EvioDataEvent decodedEvent = deco.DecodeEvent(event, decoder, table);
        //decodedEvent.show();
        		
	if(event!=null ){
           
            if (event.getType() == DataEventType.EVENT_START) {
                resetEventListener();
            }
            for(int k=0; k<this.monitors.length; k++) {
                this.monitors[k].dataEventAction(event);
            }      
	}
   }

    
    @Override
    public void timerUpdate() {
//        System.out.println("Time to update ...");
        // plot summary graphs on main Canvas
        this.CLAS12Canvas.cd(0);
        if(this.monitors[0].getDetectorSummary()!=null) this.CLAS12Canvas.draw(this.monitors[0].getDetectorSummary().getH1F("summary"));
        this.CLAS12Canvas.cd(1);
        if(this.monitors[1].getDetectorSummary()!=null) this.CLAS12Canvas.draw(this.monitors[1].getDetectorSummary().getH1F("sumHBT"));
        this.CLAS12Canvas.cd(2);
        if(this.monitors[1].getDetectorSummary()!=null) this.CLAS12Canvas.draw(this.monitors[1].getDetectorSummary().getH1F("sumTBT"));
        this.CLAS12Canvas.cd(3);
        if(this.monitors[2].getDetectorSummary()!=null) this.CLAS12Canvas.draw(this.monitors[2].getDetectorSummary().getH1F("sumP1A"));
        this.CLAS12Canvas.cd(4);
        if(this.monitors[2].getDetectorSummary()!=null) this.CLAS12Canvas.draw(this.monitors[2].getDetectorSummary().getH1F("sumP1B"));
        this.CLAS12Canvas.cd(5);
        if(this.monitors[2].getDetectorSummary()!=null) this.CLAS12Canvas.draw(this.monitors[2].getDetectorSummary().getH1F("sumP2"));
        this.CLAS12Canvas.cd(6);
        if(this.monitors[5].getDetectorSummary()!=null) this.CLAS12Canvas.draw(this.monitors[5].getDetectorSummary().getH1F("sumECin"));
        this.CLAS12Canvas.cd(7);
        if(this.monitors[5].getDetectorSummary()!=null) this.CLAS12Canvas.draw(this.monitors[5].getDetectorSummary().getH1F("sumECout"));
        this.CLAS12Canvas.cd(8);
        if(this.monitors[5].getDetectorSummary()!=null) this.CLAS12Canvas.draw(this.monitors[5].getDetectorSummary().getH1F("sumPCAL"));
        this.CLAS12Canvas.cd(9);
        if(this.monitors[3].getDetectorSummary()!=null) this.CLAS12Canvas.draw(this.monitors[3].getDetectorSummary().getH1F("summary"));
        this.CLAS12Canvas.cd(10);
        if(this.monitors[4].getDetectorSummary()!=null) this.CLAS12Canvas.draw(this.monitors[4].getDetectorSummary().getH1F("summary"));
        this.CLAS12Canvas.update();
        // plot graphs on detector tabs
        for(int k=0; k<monitors.length; k++) {
            this.monitors[k].timerUpdate();
        }
    }

    @Override
    public void resetEventListener() {
    }
    
    public void stateChanged(ChangeEvent e) {
        this.timerUpdate();
    }
    
    @Override
    public void processShape(DetectorShape2D shape) {
        System.out.println("SHAPE SELECTED = " + shape.getDescriptor());
    }
    
    public static void main(String[] args){
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        EventViewer viewer = new EventViewer();
        //frame.add(viewer.getPanel());
        frame.add(viewer.mainPanel);
        frame.setSize(900, 600);
        frame.setVisible(true);
    }
   
}