package rvision;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A slightly higher (and restricted) level of manipulation
 * than {@link Camera}, the ManagedCamera maintains state
 * for properties "zoom," "title," and "show_title."
 * In so doing, it is able to pan and tilt proportionally
 * to how zoomed in the camera is which provides for a
 * much smoother experience in moving the camera around.
 * 
 * @author robert.harder
 */
public class ManagedCamera {

    public final static String ZOOM_PROP = "zoom";
    public final static String TITLE_PROP = "title";
    public final static String SHOW_TITLE_PROP = "show_title";
    
    private Camera camera;
    private double zoom;
    private boolean showTitle;
    private String title;
    private PropertyChangeSupport propSupport = new PropertyChangeSupport(this);
    
    
    /**
     * Creates a ManagedCamera but without any underlying
     * {@link Camera} to control. Be sure to set a
     * {@link Camera} with {@link #setCamera}.
     */
    public ManagedCamera(){}
    
    /**
     * Creates a ManagedCamera with a {@link Camera}
     * object to receive the "managed" commands.
     * @param cam
     */
    public ManagedCamera( Camera cam ){
        setCamera(cam);
    }
    
    /**
     * Adopts a new camera as the managing camera and calls
     * setZoom, setTitle, and setShowTitle to "bring it
     * up to speed."
     * @param cam
     * @return
     */
    public synchronized ManagedCamera setCamera( Camera cam ){
        this.camera = cam;
        setZoom(zoom);
        setTitle( title );
        setShowTitle( showTitle );
        return this;
    }
    
    /**
     * Resets the camera control. Helpful if the camera
     * may have changed without your knowing it.
     */
    public synchronized void resetCamera(){
        setCamera(this.camera);
    }
    
    
    /**
     * Returns the underlying {@link Camera} that
     * receives the "managed" commands.
     * @return
     */
    public synchronized Camera getCamera(){
        return this.camera;
    }
    
    
    
    /**
     * Sets the zoom value (not including digital zoom)
     * using the range zero (zoomed out) to one (zoomed in).
     * Values below and above are capped at zero and one.
     * @param newVal
     * @return <tt>this</tt> to aid in stringing commands together
     */
    public synchronized ManagedCamera setZoom( double newVal ){
        newVal = Math.max(0, Math.min(1, newVal ) );
        double oldVal = this.zoom;
        this.zoom = newVal;
        
        if( this.camera != null )
            this.camera.setZoom(this.zoom);
        
        firePropertyChange( ZOOM_PROP, oldVal, newVal );
        
        return this;
    }
    
    /**
     * Returns the current zoom value from zero to one.
     * @return
     */
    public synchronized double getZoom(){
        return this.zoom;
    }
    
    
    
    
    /**
     * Tries to center the camera on the point given, where xPerc and yPerc
     * are percents (0..1) of the position across the frame, starting in 
     * the lower left corner and moving up and right like a standard
     * Cartesian grid
     * @param xPercOfFrame
     * @param yPercOfFrame
     * @return
     */
    public synchronized ManagedCamera panTilt( double xPercOfFrame, double yPercOfFrame    ){
        
        xPercOfFrame = Math.max(0, Math.min(1, xPercOfFrame  ) );  // Cap at 0..1
        yPercOfFrame = Math.max(0, Math.min(1, yPercOfFrame  ) );  // Cap at 0..1
        
        double pan  = xPercOfFrame * 2 - 1;                // Center -1..+1
        double tilt = yPercOfFrame * 2 - 1;                // Center -1..+1
        
        double panMultBase = .7;                    // Initial scaling for pan
        int howLong = 350;                          // Initial millis for pan/tilt operation
        pan *= panMultBase;
        pan  = pan*(1-zoom*.7);    // smaller coefficients = more dramatic movement 
        tilt = tilt*(1-zoom*.8);
        pan  = pan  < 0 ? pan*.9  - .05 : pan*.9  + .05;
        tilt = tilt < 0 ? tilt*.85 - .05 : tilt*.85 + .05;

        if( this.camera != null ){
            camera.panTilt(pan,tilt, (int)(howLong*(1+zoom*.5)));
        }
        
        return this;
    }
    
    
    
    /** 
     * Stops the pan/tilt operation.
     * @return
     */
    public synchronized ManagedCamera panTiltStop(){
        if( this.camera != null )
            camera.panTiltStop();
        return this;
    }
    
    /**
     * Sets the title that can be overlaid on the display.
     * @param title
     * @return
     */
    public synchronized ManagedCamera setTitle(String title){
        String oldVal = this.title;
        String newVal = title;
        this.title = newVal;
        
        if( this.camera != null ){
            camera.setTitle(newVal);
            camera.setShowTitle(true);
        }
        
        firePropertyChange( TITLE_PROP, oldVal, newVal );
        return this;
    }
    
    /**
     * Returns the title that was previously set with {@link #setTitle}.
     * @return
     */
    public synchronized String getTitle(){
        return this.title;
    }
    
    
    /**
     * Sets whether or not to show the title.
     * @param showTitle
     * @return
     */
    public synchronized ManagedCamera setShowTitle( boolean showTitle){
        boolean oldVal = this.showTitle;
        boolean newVal = showTitle;
        this.showTitle = newVal;
        
        if( this.camera != null )
            camera.setShowTitle(newVal);
        
        firePropertyChange( SHOW_TITLE_PROP, oldVal, newVal );
        return this;
    }
    
    /**
     * Returns whether or not the title is showing according
     * to the last call to {@link #setShowTitle}.
     * @return
     */
    public synchronized boolean getShowTitle(){
        return this.showTitle;
    }
    
    
/* ********  P R O P E R T Y   C H A N G E   L I S T E N E R  ******** */    
    
    /**
     * Fire a property change event on the current thread.
     * 
     * @param prop      name of property
     * @param oldVal    old value
     * @param newVal    new value
     */
    protected void firePropertyChange( final String prop, final Object oldVal, final Object newVal ){
//        this.propExec.submit( new Runnable(){
//            public void run(){
                synchronized( propSupport ){
                    try{
                        propSupport.firePropertyChange(prop,oldVal,newVal);
                    } catch( Exception exc ){
                        Logger.getLogger(this.getClass().getName())
                                .log(Level.WARNING,
                                "A property change listener threw an exception: " + exc.getMessage()
                                ,exc);
                    }
                }   // end sync
//            }   // end run
//        });
    }   // end fire
    
    /** Add a property listener. */
    public void addPropertyChangeListener( PropertyChangeListener listener ){
        synchronized( propSupport ){
            propSupport.addPropertyChangeListener(listener);
        }
    }

    
    /** Add a property listener for the named property. */
    public void addPropertyChangeListener( String property, PropertyChangeListener listener ){
        synchronized( propSupport ){
            propSupport.addPropertyChangeListener(property,listener);
        }
    }
    
    
    /** Remove a property listener. */
    public void removePropertyChangeListener( PropertyChangeListener listener ){
        synchronized( propSupport ){
            propSupport.removePropertyChangeListener(listener);
        }
    }

    
    /** Remove a property listener for the named property. */
    public void removePropertyChangeListener( String property, PropertyChangeListener listener ){
        synchronized( propSupport ){
            propSupport.removePropertyChangeListener(property,listener);
        }
    }
    

    
    
}
