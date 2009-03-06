package rvision;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;
import java.util.logging.*;
import javax.imageio.*;

/**
 * Attaches to a visual component to allow the
 * provided {@link ManagedCamera} to give point-and-click
 * access to redirect the camera to a point on screen.
 * That is, when registered as MouseListener, MouseWheelListener,
 * and MouseMotionListener on a visual component displaying
 * live video, this permits you to click on the video, and the camera
 * will center itself (approximately) on that point.
 * @author robert.harder
 */
public class ManagedCameraVisualComponentListener implements MouseListener, MouseWheelListener, MouseMotionListener{

    private static Logger LOGGER = Logger.getLogger(ManagedCameraVisualComponentListener.class.getName());
    
    private ManagedCamera camera;
    private Component component;
    private SnapshotHandler snapshotHandler;
    
    
    /**
     * Creates a ManagedCameraVisualComponentListener with the
     * {@link ManagedCamera} to control and the visual component
     * <tt>c</tt> to which this will attach listeners.
     * The {@link SnapshotHandler} is optional, if you wish to
     * enable the right-click snapshot feature.
     * the required
     * @param camera
     * @param c
     * @param snap
     */
    public ManagedCameraVisualComponentListener( ManagedCamera camera, Component c, SnapshotHandler snap ){
        this.camera = camera;
        this.component = c;
        this.snapshotHandler = snap;
        c.addMouseListener(this);
        c.addMouseWheelListener(this);
        c.addMouseMotionListener(this);
        if( c instanceof Container ){
            for( Component cc : ((Container)c).getComponents() ){
                new ManagedCameraVisualComponentListener( camera, cc, snap );
            }
        }
    }
    
    
    
    
    
/* ********  M O U S E   L I S T E N E R  ******** */
    
    
    /**
     * When the mouse is clicked (press and release),
     * then if it is a right mouse click,
     * then a snapshot is taken, if a
     * {@link SnapshotHandler} was set.
     * @param e
     */
    public void mouseClicked(MouseEvent e) {
        LOGGER.info(e.toString());
        if( e.getButton() == e.BUTTON3 ){
            if( this.snapshotHandler != null ){
                this.snapshotHandler.takeSnapshot();
            } else {
                LOGGER.warning( "No snapshot handler was provided." );
            }
        } else {
            //camera.panTiltStop();
        }   // end else
    }

    /**
     * When the mouse is pressed (not yet released),
     * the {@link ManagedCamera} is instructed to
     * pan/tilt the camera to center on the point.
     * @param e
     */
    public void mousePressed(MouseEvent e) {
        LOGGER.info(e.toString());
        if( e.getButton() == e.BUTTON1 ){
            Component comp = (Component) e.getSource();
            double width = comp.getWidth();     // Actually an int
            double height = comp.getHeight();   // Actually an int
            double x = e.getX();                // Actually an int
            double y = e.getY();                // Actually an int

            camera.panTilt( x / width, (height-y) / height );    // Why we use double

        }   // end else
    }

    /**
     * When the mouse is released, pan/tilt is stopped.
     * @param e
     */
    public void mouseReleased(MouseEvent e) {
        LOGGER.info(e.toString());
        camera.panTiltStop();
    }

    /** Does nothing. */
    public void mouseEntered(MouseEvent e) {
    }

    /** Does nothing. */
    public void mouseExited(MouseEvent e) {
    }
    
    
/* ********  M O U S E   W H E E L   L I S T E N E R  ******** */    

    /**
     * Zooms the camera in and out.
     * @param e
     */
    public void mouseWheelMoved(MouseWheelEvent e) {
        LOGGER.info(e.toString());
        int rot = e.getWheelRotation();
        if( rot < 0 ){
            //camera.zoomIn(.5,200);
            camera.setZoom( camera.getZoom() + .05 );
        } else {
            //camera.zoomOut(.5,200);
            camera.setZoom( camera.getZoom() - .05 );
        }
    }

    
/* ********  M O U S E   M O T I O N   L I S T E N E R  ******** */
    
    /** Does nothing. */
    public void mouseDragged(MouseEvent e) {
    }

    /** Does nothing. */
    public void mouseMoved(MouseEvent e) {
    }

}
