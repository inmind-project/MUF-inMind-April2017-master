/* -------------------------------------------------------------------------

   GanttChartCompiler.java

   BEAT is Copyright(C) 2000-2001 by the MIT Media Laboratory.  
   All Rights Reserved.

   Developed by Hannes Vilhjalmsson, Timothy Bickmore, Yang Gao and Justine 
   Cassell at the Media Laboratory, MIT, Cambridge, Massachusetts, with 
   support from France Telecom, AT&T and the other generous sponsors of the 
   MIT Media Lab.

   For use by academic research labs, only with prior approval of Professor
   Justine Cassell, MIT Media Lab.

   This distribution is approved by Walter Bender, Director of the Media
   Laboratory, MIT.

   Permission to use, copy, or modify this software for educational and 
   research purposes only and without fee is hereby granted, provided  
   that this copyright notice and the original authors' names appear on all 
   copies and supporting documentation. If individual files are separated 
   from this distribution directory structure, this copyright notice must be 
   included. For any other uses of this software in original or modified form, 
   including but not limited to distribution in whole or in part, specific 
   prior permission must be obtained from MIT.  These programs shall not be 
   used, rewritten, or adapted as the basis of a commercial software or 
   hardware product without first obtaining appropriate licenses from MIT. 
   MIT makes no representation about the suitability of this software for 
   any purpose. It is provided "as is" without express or implied warranty.

   ------------------------------------------------------------------------*/
package beat.compiler;

import java.util.*;
import java.awt.*; 
import java.awt.event.*;

import beat.utilities.XMLWrapper;
import org.w3c.dom.*;

import javax.swing.*;
import javax.swing.table.*;

/** Takes an abstract animation script (see FlattenTreeModule) and
    pops up a Gantt chart showing when various nonverbal modalities
    are used.
    <h3>Change log:</h3>
    <table border="1">
    <tr><th>Date<th>Who<th>What</tr>
    <tr><td>1/30/02<td>T. Bickmore<td> Created. </tr>
    <tr><td>4/9/02<td>H. Vilhjalmsson<td> Added a column. </tr>
    </table>
 */

public class GanttChartCompiler extends Compiler {
	/** The list of NVBMap objects. Each of these is responsible for translating a single
      type of nonverbal behavior into various textual representations. */
	private Vector nvbMaps=new Vector();
	private JLabel[] NVBHeaders;
	public Font textFont=new Font("Helvetica",Font.BOLD,24);
	private TableFrame frame;

	public GanttChartCompiler() throws Exception {
		super();

		//Right Gesture:
		nvbMaps.addElement(new NVBMap("R GESTURE"){
			void checkRowEntry(Element ele,int start,int end,GanttTableModel dm) {
				if(ele.getNodeName().equals("GESTURE_RIGHT")||ele.getNodeName().equals("GESTURE_BOTH"))
					dm.setInterval(nvbMaps.indexOf(this),start,end,ele.getAttribute("TYPE"),Color.red);
				}
			}
		);

		//Left Gesture:
		nvbMaps.addElement(new NVBMap("L GESTURE"){
			void checkRowEntry(Element ele,int start,int end,GanttTableModel dm) {
				if(ele.getNodeName().equals("GESTURE_LEFT")||ele.getNodeName().equals("GESTURE_BOTH"))
					dm.setInterval(nvbMaps.indexOf(this),start,end,ele.getAttribute("TYPE"),Color.red);
			}});

		//Gaze away:
		nvbMaps.addElement(new NVBMap("GAZE"){
			void checkRowEntry(Element ele,int start,int end,GanttTableModel dm) {
				if(ele.getNodeName().equals("GAZE")) {
					if(ele.getAttribute("DIRECTION").equalsIgnoreCase("AWAY_FROM_HEARER"))
						dm.setInterval(nvbMaps.indexOf(this),start,end,"AWAY",Color.red);
					else
						dm.setInterval(nvbMaps.indexOf(this),start,end,"TOWARDS",Color.green);
				};
			}});

		//Eyebrows:
		nvbMaps.addElement(new NVBMap("EYEBROWS"){
			void checkRowEntry(Element ele,int start,int end,GanttTableModel dm) {
				if(ele.getNodeName().equals("EYEBROWS")) {
					dm.setInterval(nvbMaps.indexOf(this),start,end,"RAISE",Color.red);
				};
			}});

		//Headnod:
		nvbMaps.addElement(new NVBMap("HEAD"){
			void checkRowEntry(Element ele,int start,int end,GanttTableModel dm) {
				if(ele.getNodeName().equals("HEADNOD")) {
					dm.setInterval(nvbMaps.indexOf(this),start,end,"NOD",Color.red);
				};
			}});

		//Posture shift:
		nvbMaps.addElement(new NVBMap("POSTURE"){
			void checkRowEntry(Element ele,int start,int end,GanttTableModel dm) {
				if(ele.getNodeName().equals("POSTURESHIFT")) 
					dm.setInterval(nvbMaps.indexOf(this),start,end,ele.getAttribute("ENERGY"),Color.red);
			}});

		//Pitch accent:
		nvbMaps.addElement(new NVBMap("INT PITCH"){
			void checkRowEntry(Element ele,int start,int end,GanttTableModel dm) {
				if(ele.getNodeName().equals("INTONATION_ACCENT")) 
					dm.setInterval(nvbMaps.indexOf(this),start,end,ele.getAttribute("ACCENT"),Color.pink);
			}});

		//Pitch endtone:
		nvbMaps.addElement(new NVBMap("INT ENDTONE"){
			void checkRowEntry(Element ele,int start,int end,GanttTableModel dm) {
				if(ele.getNodeName().equals("INTONATION_TONE")) 
					dm.setInterval(nvbMaps.indexOf(this),start,end,ele.getAttribute("ENDTONE"),Color.pink);
			}});

		//Pitch accent:
		nvbMaps.addElement(new NVBMap("INT BREAK"){
			void checkRowEntry(Element ele,int start,int end,GanttTableModel dm) {
				if(ele.getNodeName().equals("INTONATION_BREAK")) 
					dm.setInterval(nvbMaps.indexOf(this),start,end,"BREAK",Color.pink);
			}});

		//Addressee gaze:
		nvbMaps.addElement(new NVBMap("HEARER GAZE"){
			void checkRowEntry(Element ele,int start,int end,GanttTableModel dm) {
				if(ele.getNodeName().equals("HEARER_GAZE")) 
					dm.setInterval(nvbMaps.indexOf(this),start,end,ele.getAttribute("HEARER")+":"+ele.getAttribute("DIRECTION"),Color.yellow);
			}});

		//Addressee gaze:
		nvbMaps.addElement(new NVBMap("HEARER NOD"){
			void checkRowEntry(Element ele,int start,int end,GanttTableModel dm) {
				if(ele.getNodeName().equals("HEARER_HEADNOD")) 
					dm.setInterval(nvbMaps.indexOf(this),start,end,ele.getAttribute("HEARER"),Color.yellow);
			}});

		//Addressee gaze:
		nvbMaps.addElement(new NVBMap("HEARER BROWS"){
			void checkRowEntry(Element ele,int start,int end,GanttTableModel dm) {
				if(ele.getNodeName().equals("HEARER_EYEBROWS")) 
					dm.setInterval(nvbMaps.indexOf(this),start,end,ele.getAttribute("HEARER")+"RAISED",Color.yellow);
			}});


		//Table headers:
		NVBHeaders = new JLabel[nvbMaps.size()];
		for(int i=0;i<NVBHeaders.length;i++) NVBHeaders[i]=new JLabel(((NVBMap)nvbMaps.elementAt(i)).getRowLabel());

		//Build window frame but keep it hidden:
		frame=new TableFrame("Gantt Chart - courtesy T.Bickmore");
	}

	public void show() {
		frame.setVisible(true);
	}

	/** Does the work of the module. */
	public String compile(Document xml) throws Exception {
		if(DEBUG) System.out.println("GanttChartCompiler running...\n");
		XMLWrapper xmlw=new XMLWrapper(xml);
		xmlw.computeWordIndex();
		Vector words=XMLWrapper.extractWords(xml);

		// words.addElement(" "); - ok, maybe not so good, adding 1 to columns may be better -hhv
		//System.out.println("\n"+words.size()+"\n"+words);

		//build data array...
		GanttTableModel dm = new GanttTableModel(words,NVBHeaders);
		//Output tree contents to table data model:
		compileTree(xmlw,xml.getDocumentElement(),dm);
		//Create table:
		JTable jt=new JTable(dm);
		jt.setDefaultRenderer(JLabel.class,new LabelRenderer());
		jt.setBackground(Color.white);
		jt.setShowGrid(false);
		jt.setFont(textFont); //doesn't seem to do anything

		jt.setIntercellSpacing(new Dimension());
		jt.setRowSelectionAllowed(false);
		jt.setColumnSelectionAllowed(false);

		JScrollPane jsp=new JScrollPane(jt);
		//jsp.setColumnHeaderView(jt.getTableHeader());
		frame.update(jsp);

		//Have to return something...
		return null;
	}

	/** Recurses through the tree. */
	private void compileTree(XMLWrapper xmlw,Node node,GanttTableModel dm) {
		//If a Text node, just output it.
		if(!(node instanceof Text)) {
			int startWord=((Integer)xmlw.getNXMLAttribute(node,"WORDINDEX")).intValue()-1;
			int endWord=startWord+((Integer)xmlw.getNXMLAttribute(node,"WORDCOUNT")).intValue()-1;
			for(int i=0;i<nvbMaps.size();i++) 
				((NVBMap)nvbMaps.elementAt(i)).checkRowEntry((Element)node,startWord,endWord,dm);
			//Recurse on the children..
			NodeList children=node.getChildNodes();
			for(int i=0;i<children.getLength();i++) 
				compileTree(xmlw,children.item(i),dm);
		};      
	}


	/** Inner class of NVBMap object, responsible for translating a single
    type of nonverbal behavior into Gantt chart entries. */
	public abstract class NVBMap {
		private String rowLabel;

		NVBMap(String rowLabel) {this.rowLabel=rowLabel; }

		/** Returns string label for the Gantt chart row. */
		String getRowLabel() { return rowLabel; }

		/** Actually adds an entry (via addGanttInterval above) to the Gantt chart
        if appropriate. Note that <em>every</em> NVBMap entry is called with
	every NVB Element. */
		abstract void checkRowEntry(Element ele,int start,int end,GanttTableModel row);
	}

	public class GanttTableModel extends AbstractTableModel {
		private String[] headers;
		private JLabel[][] data;

		//headers is Vector of col Strings
		public GanttTableModel(Vector headerStrings,JLabel[] NVBHeaders) {
			int rows=NVBHeaders.length;
			int cols=headerStrings.size()+2;  // Added one more column -hhv
			headers=new String[cols];
			data=new JLabel[rows][cols];
			for(int r=0;r<rows;r++) setElement(r,0,NVBHeaders[r]);
			for(int i=0;i<headerStrings.size();i++) headers[i+1]=(String)headerStrings.elementAt(i);
		}
		public int getRowCount() { return data.length; }
		public int getColumnCount() { return headers.length; }
		public Class getColumnClass(int c) { return JLabel.class; }
		public Object getValueAt(int r,int c) { return data[r][c]; }
		public String getColumnName(int c) { return headers[c]; }
		public void setElement(int row,int col,JLabel ele) {
			//	System.out.println("Setting element at "+row+","+col);

			data[row][col]=ele;
		}
		public void setInterval(int row,int fromCol,int toCol,String label,Color bkgdColor) {
			JLabel cell=new JLabel(label);
			if(bkgdColor!=null) cell.setBackground(bkgdColor);
			setElement(row,fromCol+1,cell);
			cell=new JLabel("");
			if(bkgdColor!=null) cell.setBackground(bkgdColor);
			for(int i=fromCol+2;i<=(toCol+1);i++) setElement(row,i,cell);
		}
	}

	public class LabelRenderer extends DefaultTableCellRenderer {
		public Component getTableCellRendererComponent(JTable table,Object value,boolean isSelected,
				boolean hasFocus,int row,int column) {
			if(value==null) {
				setBackground(Color.white);
				setText("");
				return this;
			};
			if(value instanceof JLabel) {
				JLabel from=(JLabel)value;
				setText(from.getText());
				setBackground(from.getBackground());
			};
			return this;
		}

	}

	public class TableFrame extends JFrame {
		public TableFrame(String title) {
			super(title);
			setSize(700,200);
			addWindowListener(new WindowAdapter() {
				public void windowClosing(WindowEvent e) {
					setVisible(false);
				}});
		}
		public void update(Component content) {
			getContentPane().removeAll();
			getContentPane().add(content,BorderLayout.CENTER);      
			getContentPane().doLayout();
			repaint();
		}
	}


}
