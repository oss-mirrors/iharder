package rvision;

import java.awt.Component;

/**
 * I'm not sure why I made this. Maybe it's deprecated.
 * 
 * @author robert.harder
 */
public interface CameraSource {

    
    public abstract Camera getCamera();
    
    
    public abstract String getName();
    
    public abstract Component getEditor();
    
    public abstract void setEditor( Component c );
    
    public abstract void open() throws Exception;
    public abstract void close();
    
}
