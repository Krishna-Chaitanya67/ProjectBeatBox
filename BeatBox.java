/**
 * Create a list of instruments and list of their channels using the java sound api
 * Create a frame to hold everything and panel and induce borderlayout into the panel
 * Create a list of check boxes and button which will be initiated later
 * Add action itmes to all the buttons or add the buttons created to their action listeners
 * Using box layout in y-axis write all the instrument names on the panel, name on west, buttons on east
 * Create a gridLayout on top of a minipanel, and add this panel to the centre
 * 256 check boxes should be created and placed in the grid
 * Setup Midisystem, create sequencer-> open it -> create sequence -> create track and add it to sequence
 * Add sequence to sequencer and initialize it
 * When the user click start button, a new track should be made with the checkboxes selected in the grid
 * Each selected check box shoulb create an event for that instrument and added to the track
 * Remove any old tracks, place the new track
 * Play and enjoy the beats 
 */

package Drummer;

import java.awt.*;
import javax.swing.*;
import javax.sound.midi.*;
import java.util.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class BeatBox {
    
    JPanel mainPanel;
    ArrayList<JCheckBox> checkboxList;
    Sequencer sequencer;
    Sequence sequence;
    Track track;
    JFrame theFrame;

    //choosing instruments to be listed, all the parts of drums

    String[] instrumentNames = {"Bass Drum", "Closed Hi-Hat", "Open Hi-Hat", "Acoustic Snare", "Crash Cymbal", 
                                "Hand Clap", "High Tom", "Hi Bongo", "Marcas", "Whistle", "Low Conga",
                                "Cowbell", "Vibraslep", "Low mid-tom", "High Agogo", "Open Hi Conga"};

    //Create list of actualy drum keys as per sound api

    int[] instruments = {35,42,46,38,49,39,50,60,70,72,64,56,58,47,67,63};

    public static void main(String[] args){
        new BeatBox().buildGui();
    }

    public void buildGui() {
        theFrame = new JFrame("Cyber BeatBox");
        theFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        BorderLayout layout = new BorderLayout();

        //panel is usually in flow layout we are changing it to borderLayout to arrange our UI
        JPanel background = new JPanel(layout);
        //An empty border gives margin between edges of the panel 
        background.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        //then can be initialized when grid is created on the main panel later
        checkboxList = new ArrayList<JCheckBox>();

        //create button box to add all the buttons
        Box buttonBox = new Box(BoxLayout.Y_AXIS);
        //create 4 buttons for functionalities of start stop, increase and decrease tempo
        //all the action listeners are inner classes which can be initialized after the grid is completed
        JButton start = new JButton("Start");
        start.addActionListener(new StartListener());
        buttonBox.add(start);

        JButton stop = new JButton("Stop");
        stop.addActionListener(new StopListener());
        buttonBox.add(stop);

        JButton upTempo = new JButton("upTempo");
        upTempo.addActionListener(new UpTempoListener());
        buttonBox.add(upTempo);

        JButton downTempo = new JButton("downTempo");
        downTempo.addActionListener(new DownTempoListener());
        buttonBox.add(downTempo);

        JButton ser = new JButton("Serialize");
        ser.addActionListener(new serListener());
        buttonBox.add(ser);

        JButton restore = new JButton("Restore");
        restore.addActionListener(new restoreListener());
        buttonBox.add(restore);

        JButton clear = new JButton("ClearScreen");
        clear.addActionListener(new clearListener());
        buttonBox.add(clear);

        //add all the instruments names in a box which be placed on the left side of main panel

        Box nameBox = new Box(BoxLayout.Y_AXIS);
        for(int i=0; i<16 ; i++) {
            nameBox.add(new Label(instrumentNames[i]));
        }

        //place the name box on the left and button box on the right

        background.add(BorderLayout.EAST, buttonBox);
        background.add(BorderLayout.WEST, nameBox);
        theFrame.getContentPane().add(background);

        //create a grid of 16*16 with gaps and place it on the main panel

        GridLayout grid = new GridLayout(16,16);
        grid.setVgap(1);
        grid.setHgap(2);
        mainPanel = new JPanel(grid);
        background.add(BorderLayout.CENTER, mainPanel);

        //Initialize the grid with checkboxes with none of the checked/ all false

        for(int i=0; i<256; i++) {
            JCheckBox c = new JCheckBox();
            c.setSelected(false);
            checkboxList.add(c);  //this is for tracking
            mainPanel.add(c);   //this is for UI
        }

        //Create sequencer and stuff will be create in new function but called here
        //this will be called when the user checks few boxes and clicks start
        setUpMidi();

        theFrame.setBounds(50,50,300,300);
        theFrame.pack();
        theFrame.setVisible(true);

    }
    //Initialize sequencer, place track in sequence, sequence in sequencer and start it
    public void setUpMidi() {
        try{
            sequencer = MidiSystem.getSequencer();
            sequencer.open();
            sequence = new Sequence(Sequence.PPQ,4);
            track = sequence.createTrack();
            sequencer.setTempoInBPM(120);
        }catch(Exception ex){
            ex.printStackTrace();
        }

    }

    public void buildTrackAndStart() {
        /**
         * Make an array to hold the values for one instrument across 16 beats
         * Array will have zero if unchecked, value at that element is the key if checked
         */

        int[] trackList = null;	

        //delete any old tracks in the sequence and create new track
        sequence.deleteTrack(track);	
        track = sequence.createTrack();	

       //for all 16 rows, set the key that represents which instrument this is
        /**
         * This is how the final tracklist is going to look like
         *            i,j  [1][2][3][4][5][6][7][8][9]
         * Eg: trackList[0] 0  0  0  0  16 0  0  16 16
         *     trackList[1] 32 0  0  32 32 0  0  32 32
         *     trackList[2] 44 44 44 0  0  0  0  0  0   
         */

        for (int i = 0; i < 16; i++) {	
        trackList = new int[16];	
   
        int key = instruments[i]; 

        //check the checkboxes list and mark the tracklist table
        //checkboxList is a list of 256 elements in a single row  

        for (int j = 0; j < 16; j++ ) {         	
            JCheckBox jc = (JCheckBox) checkboxList.get(j + (16*i));	
            if ( jc.isSelected()) {	
               trackList[j] = key;	
            } else {	
               trackList[j] = 0;	
            }                    	
         } 	
        
         // This funciton makes evenets for one instrument at a time
         makeTracks(trackList);	
         track.add(makeEvent(176,1,127,0,16));  	
     } 

     // and event is always added at beat 16 or the beatbox might not get full 16 beats before it starts over
     track.add(makeEvent(192,9,1,0,15));      	
     try {	
         sequencer.setSequence(sequence); 	
       sequencer.setLoopCount(sequencer.LOOP_CONTINUOUSLY);                   	
         sequencer.start();	
         sequencer.setTempoInBPM(120);	
     } catch(Exception e) {e.printStackTrace();}

    }
    /**
     * For the buttons created above, they are treated as inner classes as same method of the interface 
     * ActionLister needs to be implemented four times
     */

    public class StartListener implements ActionListener {

        /**
         * This is where our action starts, as the user selects checkboxes in the grid and clicks start
         * sequence needs to start playing
         */
        @Override
        public void actionPerformed(ActionEvent e) {
            buildTrackAndStart();
        }

    }

    public class StopListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            sequencer.stop();
        }

    }

    public class UpTempoListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            float tempoFactor = sequencer.getTempoFactor();
            sequencer.setTempoFactor((float)(tempoFactor * 1.03));
        }

    }

    public class DownTempoListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            float tempoFactor = sequencer.getTempoFactor();
            sequencer.setTempoFactor((float)(tempoFactor * 0.97));
        }

    }

    /**To save the current state of the check boxes we use serializaiton 
    * Create a checbox boolean array of size 256 and mark it as true if the box was checked
    * Create fileOutputstream(Connection stream), place it in ObjectOutputStrea(chain steram)
    *   and write the object(checkBoxarray) to stream
    */

    public class serListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            boolean[] checkBoxState = new boolean[256];

            for(int i = 0; i < 256; i++){
                JCheckBox check  = (JCheckBox) checkboxList.get(i);
                if(check.isSelected()) {
                    checkBoxState[i] = true;
                }
            }
            try{
                FileOutputStream fs = new FileOutputStream(new File("state.ser"));
                ObjectOutputStream os = new ObjectOutputStream(fs);
                os.writeObject(checkBoxState);
                os.close();
            } catch(Exception ex){
                ex.printStackTrace();
            }

        }

    }
    
    /**To restore the state which was previously saved
     * Pass the serialized object into the file class, and then to file input stream 
     * pass the file input stream to object input stream and you get to read the object
     * Create a checkboxstate array and assign the values from object to it
     * Re-arrange the checkboxList array
     * Stop what is currently playing on the sequence
     * Play the current checkBoxState(restored) using "buildTrackandStart()"
     */
    public class restoreListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            boolean[] checkBoxState = null;
            try{
                FileInputStream fs = new FileInputStream(new File("state.ser"));
                ObjectInputStream os = new ObjectInputStream(fs);
                checkBoxState = (boolean[]) os.readObject();

            } catch(Exception ex){
                System.out.println("Could not restore the version");
                ex.printStackTrace();
            }
            for(int i = 0; i < 256; i++){
                JCheckBox check = (JCheckBox) checkboxList.get(i);
                if(checkBoxState[i]) {
                    check.setSelected(true);
                } else {
                    check.setSelected(false);
                }
            }
            sequencer.stop();
            buildTrackAndStart();
        }
        
    }

    public class clearListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            for(int i=0; i< 256; i++){
                JCheckBox check = (JCheckBox) checkboxList.get(i);
                check.setSelected(false);
            }

        }
        
    }
    //If the indexed value is zero instrument should not play or else make event and add it to track
    //Use of this function is to make it easier for making events
    public void makeTracks(int[] list) {
        for (int i=0; i<16; i++) {
            int key = list[i];
            
            if(key !=0) {
                //Make note ON and OFF events
                track.add(makeEvent(144, 9, key, 100, i));
                track.add(makeEvent(128, 9, key, 100, i+1));
            }
        }
    }
    
    /**
     * A midi event is a message that the sequencer can understand, a midi event migh say "play middle C"
     * or "change current instrument to flute" etc
     * A series of midi events is like a sheet music
     * comd -  message type (start playing , stop playing, change instrument etc)
     * chan -  channel (its a channel in a band eg: 1 keyboard, 9 drummer etc)
     * one  -  note to play (100 - middle c in keybord, 56 - snare in drums etc)
     * two  -  velocity of note
     * tick -  when the event should happen
     */
     public MidiEvent makeEvent(int comd, int chan, int one, int two, int tick) {
        MidiEvent event = null;
        try{
            ShortMessage a = new ShortMessage();
            a.setMessage(comd, chan, one, two);
            //Make a midi event instace for the message
            event = new MidiEvent(a, tick);
        } catch(Exception e){
            e.printStackTrace();
        }
        return event;
    }
}   