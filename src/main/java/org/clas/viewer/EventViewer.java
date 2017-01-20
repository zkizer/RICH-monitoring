package org.clas.viewer;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javax.imageio.ImageIO;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
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
import org.jlab.groot.data.IDataSet;
import org.jlab.groot.data.TDirectory;
import org.jlab.groot.graphics.EmbeddedCanvasTabbed;
import org.jlab.groot.group.DataGroup;
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
    JMenuBar menuBar                            = null;
    DataSourceProcessorPane processorPane 	= null;
    EmbeddedCanvasTabbed CLAS12Canvas                 = null;

    
    CodaEventDecoder               decoder = new CodaEventDecoder();
    DetectorEventDecoder   detectorDecoder = new DetectorEventDecoder();
       
    
    TreeMap<String, List<H2F>>  histos = new TreeMap<String,List<H2F>>();
    
    private int updateTime = 2000;
    
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
        		
	// create menu bar
        menuBar = new JMenuBar();
        JMenu settings = new JMenu("Settings");
        JMenuItem menuItem;
        settings.setMnemonic(KeyEvent.VK_A);
        settings.getAccessibleContext().setAccessibleDescription("Choose monitoring parameters");
        menuItem = new JMenuItem("Set GUI update interval...", KeyEvent.VK_T);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_1, ActionEvent.ALT_MASK));
        menuItem.getAccessibleContext().setAccessibleDescription("Set GUI update interval");
        menuItem.addActionListener(this);
        settings.add(menuItem);
        menuBar.add(settings);
        JMenu save = new JMenu("Save");
        save.setMnemonic(KeyEvent.VK_A);
        save.getAccessibleContext().setAccessibleDescription("Choose monitoring parameters");
        menuItem = new JMenuItem("Save histograms to file", KeyEvent.VK_T);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.ALT_MASK));
        menuItem.getAccessibleContext().setAccessibleDescription("Save histograms to file");
        menuItem.addActionListener(this);
        save.add(menuItem);
        menuBar.add(save);
        
           
        // create main panel
        mainPanel = new JPanel();	
	mainPanel.setLayout(new BorderLayout());
        
      	tabbedpane 	= new JTabbedPane();

        processorPane = new DataSourceProcessorPane();
        processorPane.setUpdateRate(100);

        mainPanel.add(tabbedpane);
        mainPanel.add(processorPane,BorderLayout.PAGE_END);
        
    
        CLAS12Canvas    = new EmbeddedCanvasTabbed();
        CLAS12Canvas.getCanvas("canvas1").divide(3,4);
        CLAS12Canvas.getCanvas("canvas1").setGridX(false);
        CLAS12Canvas.getCanvas("canvas1").setGridY(false);
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
        
        this.setCanvasUpdate(updateTime);
    }
      
    public void actionPerformed(ActionEvent e) {
        System.out.println(e.getActionCommand());
        if(e.getActionCommand()=="Set GUI update interval...") {
            this.chooseUpdateInterval();
        }
        if(e.getActionCommand()=="Save histograms to file") {
            this.saveToFile();
        }
    }

    public void chooseUpdateInterval() {
        String s = (String)JOptionPane.showInputDialog(
                    null,
                    "GUI update interval (ms)",
                    " ",
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    null,
                    "1000");
        if(s!=null){
            int time = 1000;
            try { 
                time= Integer.parseInt(s);
            } catch(NumberFormatException e) { 
                JOptionPane.showMessageDialog(null, "Value must be a positive integer!");
            }
            if(time>0) {
                this.setCanvasUpdate(time);
            }
            else {
                JOptionPane.showMessageDialog(null, "Value must be a positive integer!");
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
//            event.show();

            if (event.getType() == DataEventType.EVENT_START) {
                resetEventListener();
                this.plotSummaries();
            }
            for(int k=0; k<this.monitors.length; k++) {
                this.monitors[k].dataEventAction(event);
            }      
	}
   }

    public void plotSummaries() {
        this.CLAS12Canvas.getCanvas("canvas1").cd(0);
        if(this.monitors[0].getDetectorSummary()!=null) this.CLAS12Canvas.getCanvas("canvas1").draw(this.monitors[0].getDetectorSummary().getH1F("summary"));
        this.CLAS12Canvas.getCanvas("canvas1").cd(1);
        if(this.monitors[1].getDetectorSummary()!=null) this.CLAS12Canvas.getCanvas("canvas1").draw(this.monitors[1].getDetectorSummary().getH1F("sumHBT"));
        this.CLAS12Canvas.getCanvas("canvas1").cd(2);
        if(this.monitors[1].getDetectorSummary()!=null) this.CLAS12Canvas.getCanvas("canvas1").draw(this.monitors[1].getDetectorSummary().getH1F("sumTBT"));
        this.CLAS12Canvas.getCanvas("canvas1").cd(3);
        if(this.monitors[2].getDetectorSummary()!=null) this.CLAS12Canvas.getCanvas("canvas1").draw(this.monitors[2].getDetectorSummary().getH1F("sumP1A"));
        this.CLAS12Canvas.getCanvas("canvas1").cd(4);
        if(this.monitors[2].getDetectorSummary()!=null) this.CLAS12Canvas.getCanvas("canvas1").draw(this.monitors[2].getDetectorSummary().getH1F("sumP1B"));
        this.CLAS12Canvas.getCanvas("canvas1").cd(5);
        if(this.monitors[2].getDetectorSummary()!=null) this.CLAS12Canvas.getCanvas("canvas1").draw(this.monitors[2].getDetectorSummary().getH1F("sumP2"));
        this.CLAS12Canvas.getCanvas("canvas1").cd(6);
        if(this.monitors[5].getDetectorSummary()!=null) this.CLAS12Canvas.getCanvas("canvas1").draw(this.monitors[5].getDetectorSummary().getH1F("sumECin"));
        this.CLAS12Canvas.getCanvas("canvas1").cd(7);
        if(this.monitors[5].getDetectorSummary()!=null) this.CLAS12Canvas.getCanvas("canvas1").draw(this.monitors[5].getDetectorSummary().getH1F("sumECout"));
        this.CLAS12Canvas.getCanvas("canvas1").cd(8);
        if(this.monitors[5].getDetectorSummary()!=null) this.CLAS12Canvas.getCanvas("canvas1").draw(this.monitors[5].getDetectorSummary().getH1F("sumPCAL"));
        this.CLAS12Canvas.getCanvas("canvas1").cd(9);
        if(this.monitors[3].getDetectorSummary()!=null) this.CLAS12Canvas.getCanvas("canvas1").draw(this.monitors[3].getDetectorSummary().getH1F("summary"));
        this.CLAS12Canvas.getCanvas("canvas1").cd(10);
        if(this.monitors[4].getDetectorSummary()!=null) this.CLAS12Canvas.getCanvas("canvas1").draw(this.monitors[4].getDetectorSummary().getH1F("summary"));       
    }
    
    public void setCanvasUpdate(int time) {
        System.out.println("Setting " + time + " ms update interval");
        this.updateTime = time;
        this.CLAS12Canvas.getCanvas("canvas1").initTimer(time);
        this.CLAS12Canvas.getCanvas("canvas1").update();
        for(int k=0; k<this.monitors.length; k++) {
            this.monitors[k].setCanvasUpdate(time);
        }
    }

    @Override
    public void timerUpdate() {
//        System.out.println("Time to update ...");
        for(int k=0; k<this.monitors.length; k++) {
            this.monitors[k].timerUpdate();
        }
   }

    @Override
    public void resetEventListener() {
        for(int k=0; k<this.monitors.length; k++) {
            this.monitors[k].resetEventListener();
            this.monitors[k].timerUpdate();
        }      
        this.plotSummaries();
    }
    
    public void saveToFile() {
        // TXT table summary FILE //
        String fileName = "histo.hipo";
        JFileChooser fc = new JFileChooser();
        fc.setCurrentDirectory(new File(fileName));
	int returnValue = fc.showSaveDialog(null);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            fileName = fc.getSelectedFile().getAbsolutePath();            
        }
        TDirectory dir = new TDirectory();
        for(int k=0; k<this.monitors.length; k++) {
            String folder = "/" + this.monitors[k].getDetectorName();
            dir.mkdir(folder);
            dir.cd(folder);
            System.out.println("Writing to folder " + folder);
            Map<Long, DataGroup> map = this.monitors[k].getDataGroup().getMap();
            for( Map.Entry<Long, DataGroup> entry : map.entrySet()) {
                DataGroup group = entry.getValue();
                int nrows = group.getRows();
                int ncols = group.getColumns();
                int nds   = nrows*ncols;
                for(int i = 0; i < nds; i++){
                    List<IDataSet> dsList = group.getData(i);
                    for(IDataSet ds : dsList){
                        System.out.println("\t --> " + ds.getName());
                        dir.addDataSet(ds);
                    }
                }
            }
        }
        System.out.println("Saving histograms to file " + fileName);
        dir.writeFile(fileName);
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
        frame.setJMenuBar(viewer.menuBar);
        frame.setSize(900, 600);
        frame.setVisible(true);
    }
   
}