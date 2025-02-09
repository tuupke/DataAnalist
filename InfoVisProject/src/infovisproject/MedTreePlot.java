package infovisproject;

import java.awt.Shape;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RectangularShape;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Iterator;
import javax.swing.JPopupMenu;
import prefuse.Constants;
import prefuse.Display;
import prefuse.Visualization;
import prefuse.action.ActionList;
import prefuse.action.RepaintAction;
import prefuse.action.assignment.ColorAction;
import prefuse.action.assignment.DataColorAction;
import prefuse.action.assignment.DataSizeAction;
import prefuse.action.layout.Layout;
import prefuse.action.layout.graph.ForceDirectedLayout;
import prefuse.activity.Activity;
import prefuse.controls.Control;
import prefuse.controls.ControlAdapter;
import prefuse.controls.DragControl;
import prefuse.controls.PanControl;
import prefuse.controls.ZoomControl;
import prefuse.data.Graph;
import prefuse.data.Node;
import prefuse.data.Schema;
import prefuse.data.Table;
import prefuse.data.Tuple;
import prefuse.render.AbstractShapeRenderer;
import prefuse.render.DefaultRendererFactory;
import prefuse.render.EdgeRenderer;
import prefuse.render.LabelRenderer;
import prefuse.render.Renderer;
import prefuse.render.ShapeRenderer;
import prefuse.util.ColorLib;
import prefuse.util.FontLib;
import prefuse.util.PrefuseLib;
import prefuse.visual.DecoratorItem;
import prefuse.visual.NodeItem;
import prefuse.visual.VisualItem;
import prefuse.visual.expression.InGroupPredicate;

/**
 * A simple visualization that can show three attributes of data items at the
 * same time, in the form of markers on a scatter plot: - Numeric attribute to
 * the marker X-coordinate; - Numeric attribute to the marker Y-coordinate; -
 * Nominal attribute to the marker shape.
 *
 * This code is derived from the Prefuse demo files, which are authored by
 * Jeffrey Heer.
 */
public class MedTreePlot extends Display {


    public static final String GROUP = "data";
    // Renders markers as attribute-derived shapes
    // with a base size of 10 pixels.
    private ShapeRenderer shapeRenderer = new ShapeRenderer(10);

    /**
     *
     */
    public MedTreePlot(Table table, int NUMOFSETS) {
        super(new Visualization());
        DecimalFormat one = new DecimalFormat("#0.0000");

        Graph graph = new Graph();
        graph.addColumn("Label", String.class);
        graph.addColumn("Size", int.class);
        graph.addColumn("Colour", int.class);

        Node n = graph.addNode();
        n.set("Label", "Wine");
        n.set("Colour", 0);
        n.set("Size", 50);
        Node root = n;

        Node[] colnames = new Node[table.getColumnCount() - 1];
        int e = 0;
        for (int i = 0; i <= colnames.length; ++i) {
//            System.out.println(t.getColumnName(i));
            if (!table.getColumnName(i).equals("quality")) {
                n = graph.addNode();
                n.set("Label", table.getColumnName(i));
                n.set("Size", 31);
                n.set("Colour", 1);
                colnames[e] = n;
                graph.addEdge(root, n);
                e++;
            }
        }

        int[][][] values = new int[colnames.length][2][NUMOFSETS];
        double[][] range = new double[colnames.length][NUMOFSETS - 1];

        for (int i = 0; i < colnames.length; ++i) {
            String name = (String) colnames[i].get("Label");
            double[] temp = new double[table.getRowCount()];
            for (int t = 0; t < table.getRowCount(); t++) {
                Tuple tuple = table.getTuple(t);
                temp[t] = (Double) tuple.get(name);
            }
            Arrays.sort(temp);
            for (int s = 0; s < NUMOFSETS - 1; s++) {
                //System.out.println((int) (temp.length * ((s + 1.0) / NUMOFSETS)));
                range[i][s] = temp[(int) (temp.length * ((s + 1.0) / NUMOFSETS))];

            }
        }
        //System.out.println(Arrays.deepToString(range));

        for (int t = 0; t < table.getRowCount(); t++) {
            Tuple tuple = table.getTuple(t);
            for (int i = 0; i < colnames.length; ++i) {
                
//                System.out.println((Double)tuple.get((String) colnames[i].get("Label")));
//                System.out.println(range[i][0]);
//                System.out.println(range[i][1]);
//                System.out.println("ans "+(int)(((Double)tuple.get((String) colnames[i].get("Label"))-range[i][0])/range[i][1]));
                int setNum=0;
                while(setNum<NUMOFSETS-1 && (Double) tuple.get((String) colnames[i].get("Label")) >= range[i][setNum]){
                    setNum++;
                }           
                values[i][0][setNum] += 1;
                values[i][1][setNum] += (Integer) tuple.get("quality");
//                values[i][(Integer) tuple.get("type")][(Integer) tuple.get("quality")][NUMOFSETS] += 1;
            }
        }
//        System.out.println(Arrays.deepToString(values));
//        System.out.println(colnames.length);

        for (int i = 0; i < colnames.length; ++i) {
            for (int s = 0; s < NUMOFSETS; s++) {
                if (values[i][0][s] > 0) {
                    String lab;
                    if(s==0){
                       lab = "<" +  one.format(range[i][0]);
                    }else if(s==NUMOFSETS-1){
                       lab = ">" +  one.format(range[i][s-1]); 
                    }else{
                        lab = ">" +  one.format(range[i][s-1]) +" &\n"+"<" + one.format( range[i][s]); 
                    }
                    lab += "\n avr=" + one.format((values[i][1][s] * 1.0 / values[i][0][s])) ;
                    n = graph.addNode();
                    n.set("Label", "" + lab);
                    n.set("Size", 10 );
                    n.set("Colour", 2+ (int)(values[i][1][s] * 1.0 / values[i][0][s]) );
                    graph.addEdge(colnames[i], n);
                    
                }
            }
        }

//            n = graph.addNode();
//            String name = (String) colnames[i].get("Label");
//            n.set("Label", "max");
//            n.set("Size", table.get(table.getMetadata(name).getMaximumRow(), name));
//            graph.addEdge(colnames[i], n);
//            n = graph.addNode();
//            n.set("Label", "min");
//            n.set("Size", table.get(table.getMetadata(name).getMinimumRow(), name));
//            graph.addEdge(colnames[i], n);
        Renderer nodeR = new FinalRenderer();
        EdgeRenderer edgeR = new EdgeRenderer(prefuse.Constants.EDGE_TYPE_CURVE, prefuse.Constants.EDGE_ARROW_FORWARD);

        m_vis.add("graph", graph);// draw the "name" label for NodeItems
        DefaultRendererFactory drf = new DefaultRendererFactory();
        drf.setDefaultRenderer(nodeR);
        drf.setDefaultEdgeRenderer(edgeR);
        m_vis.setRendererFactory(drf);

        int[] palette = new int[10+2];
        palette[0]=ColorLib.rgb(200, 200, 200);
        palette[1]=ColorLib.rgb(0, 0, 255);
        for (int s = 0; s < 10; s++) {
            if( s <= 10/2){
                palette[s+2]=ColorLib.rgb(255, (int)(255*((s*2)/(10-1.0))), 0);
            }else{
                palette[s+2]=ColorLib.rgb(512-(int)(255*((s*2)/(10-1.0))),255, 0);
            }
        }

        DataColorAction nFill = new DataColorAction("graph.nodes", "Colour",
                Constants.NOMINAL, VisualItem.FILLCOLOR, palette);

        DataSizeAction nSize = new DataSizeAction(GROUP, "Size");
        ColorAction edges = new ColorAction("graph.edges",
                VisualItem.STROKECOLOR, ColorLib.gray(200));
        ColorAction arrow = new ColorAction("graph.edges",
                VisualItem.FILLCOLOR, ColorLib.gray(200));
        ActionList color = new ActionList();
        color.add(nFill);
        color.add(nFill);
        color.add(edges);
        color.add(arrow);
        ActionList size = new ActionList();
        size.add(nSize);

        drf.add(new InGroupPredicate("nodedec"), new LabelRenderer("Label"));

        final Schema DECORATOR_SCHEMA = PrefuseLib.getVisualItemSchema();
        DECORATOR_SCHEMA.setDefault(VisualItem.INTERACTIVE, false);
        DECORATOR_SCHEMA.setDefault(VisualItem.TEXTCOLOR,
                ColorLib.rgb(0, 0, 0));
        DECORATOR_SCHEMA.setDefault(VisualItem.FONT,
                FontLib.getFont("Tahoma", 5));

        m_vis.addDecorators("nodedec", "graph.nodes", DECORATOR_SCHEMA);

// create an action list containing all color assignments
        color.add(edges);

        ActionList layout = new ActionList(Activity.INFINITY);
        layout.add(new FinalDecoratorLayout("nodedec"));
        layout.add(new ForceDirectedLayout("graph"));
        layout.add(new RepaintAction());

        m_vis.putAction("color", color);
        m_vis.putAction("size", size);
        m_vis.putAction("layout", layout);

        setSize(720, 500); // set display size
        pan(360, 250);
        setHighQuality(true);
        addControlListener(new DragControl());
        addControlListener(new PanControl());
        addControlListener(new ZoomControl());

        m_vis.run("color");
        m_vis.run("layout");
    }

    public class FinalRenderer extends AbstractShapeRenderer {

        protected RectangularShape m_box = new Rectangle2D.Double();

        @Override
        protected Shape getRawShape(VisualItem item) {
            int s = (Integer) item.get("Size");
            m_box.setFrame(item.getX() - s / 2, item.getY() - s / 2, s, s);

            return m_box;
        }
    }

    public class FinalDecoratorLayout extends Layout {

        public FinalDecoratorLayout(String group) {
            super(group);
        }

        @Override
        public void run(double frac) {
            Iterator iter = m_vis.items(m_group);
            while (iter.hasNext()) {
                DecoratorItem decorator = (DecoratorItem) iter.next();
                VisualItem decoratedItem = decorator.getDecoratedItem();
                Rectangle2D bounds = decoratedItem.getBounds();

                double x = bounds.getCenterX();
                double y = bounds.getCenterY();

                setX(decorator, null, x);
                setY(decorator, null, y);
            }
        }
    }

    public class FinalControlListener extends ControlAdapter implements Control {

        @Override
        public void itemClicked(VisualItem item, MouseEvent e) {
            if (item instanceof NodeItem) {
                String occupation = ((String) item.get("job"));
                int age = (Integer) item.get("age");

                JPopupMenu jpub = new JPopupMenu();
                jpub.add("Job: " + occupation);
                jpub.add("Age: " + age);
                jpub.show(e.getComponent(), (int) item.getX(),
                        (int) item.getY());
            }
        }
    }
}
